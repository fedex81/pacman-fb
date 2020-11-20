/*
 * BaseSystem
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 26/10/19 15:21
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fbdev.helios;

import com.fbdev.helios.input.InputProvider;
import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.input.KeyboardInput;
import com.fbdev.helios.model.BaseBusProvider;
import com.fbdev.helios.model.Device;
import com.fbdev.helios.model.DisplayWindow;
import com.fbdev.helios.model.SystemProvider;
import com.fbdev.helios.sound.SoundProvider;
import com.fbdev.helios.util.Telemetry;
import com.fbdev.helios.util.Util;
import com.fbdev.helios.util.VideoMode;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class BaseSystem<BUS extends BaseBusProvider, STH extends BaseStateHandler> implements SystemProvider {

    static final long MAX_DRIFT_NS = Duration.ofMillis(10).toNanos();
    private final static Logger LOG = LogManager.getLogger(BaseSystem.class.getSimpleName());
    private static final long DRIFT_THRESHOLD_NS = Util.MILLI_IN_NS / 10;
    //frame pacing stuff
    private static final boolean fullThrottle;

    static {
        fullThrottle = Boolean.parseBoolean(System.getProperty("helios.fullSpeed", "false"));
    }

    protected JoypadProvider joypad;
    protected SoundProvider sound;
    protected BaseVdpProvider vdp;
    protected InputProvider inputProvider;
    protected BUS bus;
    protected Future<Void> runningRomFuture;
    protected Path romFile;
    protected DisplayWindow emuFrame;
    protected volatile boolean saveStateFlag = false;
    protected volatile STH stateHandler;
    protected volatile boolean futureDoneFlag = false;
    protected volatile boolean softReset = false;
    protected long elapsedWaitNs, frameProcessingDelayNs, startCycle;
    protected long targetNs, startNs = 0;
    protected int counter = 1;
    private String romName;
    private Path romPath;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean pauseFlag = false;
    private long driftNs = 0;
    private Optional<String> stats = Optional.empty();
    final Consumer<String> statsConsumer = st -> stats = Optional.of(st);
    private double lastFps = 0;
    private final CyclicBarrier pauseBarrier = new CyclicBarrier(2);
    protected Telemetry telemetry = Telemetry.getInstance();

    protected BaseSystem(DisplayWindow emuFrame) {
        this.emuFrame = emuFrame;
    }

    protected abstract void loop();

    protected abstract void initAfterRomLoad();

    protected abstract void processSaveState();

    protected abstract void resetCycleCounters(int counter);

    protected abstract STH createStateHandler(Path file, BaseStateHandler.Type type);

    @Override
    public void handleSystemEvent(SystemEvent event, Object parameter) {
        LOG.info("Event: {}, with parameter: {}", event, Objects.toString(parameter));
        switch (event) {
            case NEW_ROM:
                handleNewRom((Path) parameter);
                break;
            case CLOSE_ROM:
                handleCloseRom();
                break;
            case LOAD_STATE:
            case QUICK_LOAD:
                handleLoadState((Path) parameter);
                break;
            case SAVE_STATE:
            case QUICK_SAVE:
                handleSaveState((Path) parameter);
                break;
            case TOGGLE_FULL_SCREEN:
                emuFrame.setFullScreen((Boolean) parameter);
                break;
            case TOGGLE_PAUSE:
                handlePause();
                break;
            case TOGGLE_MUTE:
                sound.setEnabled(!sound.isMute());
                break;
            case CLOSE_APP:
                handleCloseApp();
                break;
            case CONTROLLER_CHANGE:
                String str = parameter.toString();
                String[] s = str.split(":");
                inputProvider.setPlayerController(InputProvider.PlayerNumber.valueOf(s[0]), s[1]);
                break;
            case SOFT_RESET:
                softReset = true;
                break;
            default:
                LOG.warn("Unable to handle event: {}, with parameter: {}", event, Objects.toString(parameter));
                break;
        }
    }

    protected void handleSoftReset() {
        if (softReset) {
            LOG.info("Soft Reset");
        }
        softReset = false;
    }

    @Deprecated
    private void setDebug(boolean value) {
    }

    protected void reloadWindowState() {
        emuFrame.addKeyListener(KeyboardInput.createKeyAdapter(joypad));
        emuFrame.reloadControllers(InputProvider.DEFAULT_CONTROLLERS);
    }

    public void handleNewRom(Path file) {
        init();
        this.romFile = file;
        Runnable runnable = new RomRunnable(file);
        runningRomFuture = executorService.submit(runnable, null);
    }

    private void handleCloseApp() {
        try {
            handleCloseRom();
            sound.close();
        } catch (Exception e) {
            LOG.error("Error while closing app", e);
        }
    }

    private void handleLoadState(Path file) {
        stateHandler = createStateHandler(file, BaseStateHandler.Type.LOAD);
        LOG.info("Savestate action detected: {} , using file: {}",
                stateHandler.getType(), stateHandler.getFileName());
        this.saveStateFlag = true;
    }

    private void handleSaveState(Path file) {
        stateHandler = createStateHandler(file, BaseStateHandler.Type.SAVE);
        LOG.info("Savestate action detected: {} , using file: {}",
                stateHandler.getType(), stateHandler.getFileName());
        this.saveStateFlag = true;
    }

    protected void handleCloseRom() {
        handleRomInternal();
    }

    @Override
    public boolean isRomRunning() {
        return runningRomFuture != null && !runningRomFuture.isDone();
    }

    @Override
    public Path getRomPath() {
        return romPath;
    }

    protected void pauseAndWait() {
        if (!pauseFlag) {
            return;
        }
        LOG.info("Pause: {}", pauseFlag);
        try {
            Util.waitOnBarrier(pauseBarrier);
            LOG.info("Pause: {}", pauseFlag);
        } finally {
            pauseBarrier.reset();
        }
    }

    protected final long syncCycle(long startCycle) {
        long now = System.nanoTime();
        if (fullThrottle) {
            return now;
        }
        long driftDeltaNs = 0;
        if (Math.abs(driftNs) > DRIFT_THRESHOLD_NS) {
            driftDeltaNs = driftNs > 0 ? DRIFT_THRESHOLD_NS : -DRIFT_THRESHOLD_NS;
            driftNs -= driftDeltaNs;
        }
        long baseRemainingNs = startCycle + targetNs + driftDeltaNs;
        long remainingNs = baseRemainingNs - now;
        if (remainingNs > 0) { //too fast
            Util.parkFuzzy(remainingNs);
            remainingNs = baseRemainingNs - System.nanoTime();
        }
        driftNs += remainingNs;
        driftNs = Math.min(MAX_DRIFT_NS, driftNs);
        driftNs = Math.max(-MAX_DRIFT_NS, driftNs);
        return System.nanoTime();
    }

    private void handleRomInternal() {
        if (pauseFlag) {
            handlePause();
        }
        if (isRomRunning()) {
            runningRomFuture.cancel(true);
            while (isRomRunning()) {
                Util.sleep(100);
            }
            LOG.info("Rom thread cancel");
            emuFrame.resetScreen();
            telemetry.reset();
//            sound.reset();
//            bus.closeRom();
            Optional.ofNullable(vdp).ifPresent(Device::reset);
        }
    }

    protected void createAndAddVdpEventListener() {
        vdp.addVdpEventListener(new BaseVdpProvider.VdpEventListener() {
            @Override
            public void onNewFrame() {
                newFrame();
            }
        });
    }

    protected void newFrame() {
        long tstamp = System.nanoTime();
        vdp.renderScreenDataLinear(emuFrame.acquireRender());
        emuFrame.renderScreen(getStats(startCycle), VideoMode.H28_V36);
        long startWaitNs = System.nanoTime();
        elapsedWaitNs = syncCycle(startCycle) - startWaitNs;
        processSaveState();
        pauseAndWait();
        resetCycleCounters(counter);
        counter = 0;
        startCycle = System.nanoTime();
        frameProcessingDelayNs = startCycle - tstamp - elapsedWaitNs;
        futureDoneFlag = runningRomFuture.isDone();
        handleSoftReset();
//        LOG.info("{}, {}", elapsedWaitNs, frameProcessingDelayNs);
    }

    protected Optional<String> getStats(long nowNs) {
        lastFps = (1.0 * Util.SECOND_IN_NS) / ((nowNs - startNs));
        telemetry.newFrame(lastFps, driftNs / 1000d).ifPresent(statsConsumer);
        startNs = nowNs;
        return stats;
    }

    private void handlePause() {
        boolean isPausing = pauseFlag;
        pauseFlag = !pauseFlag;
        sound.setEnabled(pauseFlag);
        if (isPausing) {
            Util.waitOnBarrier(pauseBarrier);
        }
    }

    @Override
    public void reset() {
        handleCloseRom();
        handleNewRom(romFile);
    }

    protected void resetAfterRomLoad() {
        joypad.init();
        bus.init();
        futureDoneFlag = false;
    }

    class RomRunnable implements Runnable {
        private static final String threadNamePrefix = "cycle-";
        private Path file;

        public RomRunnable(Path file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                romPath = file;
                romName = RomHelper.romSetName;
                Thread.currentThread().setName(threadNamePrefix + romName);
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
                emuFrame.setTitle(romName);
                LOG.info("Running roms from folder: {}", romName);
                initAfterRomLoad();
                loop();
            } catch (Exception | Error e) {
                e.printStackTrace();
                LOG.error(e);
            }
            handleCloseRom();
        }
    }
}
