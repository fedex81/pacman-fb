/*
 * Z80BaseSystem
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 26/10/19 15:49
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

package com.fbdev;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.BaseStateHandler;
import com.fbdev.helios.BaseSystem;
import com.fbdev.helios.model.DisplayWindow;
import com.fbdev.helios.model.SystemProvider;
import com.fbdev.helios.sound.SoundProvider;
import com.fbdev.helios.z80.Z80CoreWrapper;
import com.fbdev.helios.z80.Z80Provider;
import com.fbdev.input.PacManPad;
import com.fbdev.sound.Sound;
import com.fbdev.state.HeliosPmStateHandler;
import com.fbdev.state.PmStateHandler;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

public class Z80BaseSystem extends BaseSystem<SystemBus, HeliosPmStateHandler> {

    private static final Logger LOG = LogManager.getLogger(Z80BaseSystem.class.getSimpleName());
    private static final int FRAMES_HZ = 60;
    private static final int Z80_CLOCK_HZ = 3_072_000;
    private static final int Z80_CYCLES_PER_FRAME = Z80_CLOCK_HZ / FRAMES_HZ;
    protected Z80Provider z80;
    private int nextZ80Cycle = counter;
    private RomHelper romHelper;

    protected Z80BaseSystem(DisplayWindow emuFrame) {
        super(emuFrame);
    }

    public static SystemProvider createNewInstance(DisplayWindow emuFrame) {
        return new Z80BaseSystem(emuFrame);
    }

    @Override
    public void init() {
        joypad = new PacManPad();
        bus = new SystemBus();
        stateHandler = HeliosPmStateHandler.EMPTY_STATE;
        sound = SoundProvider.NO_SOUND;
        vdp = null;
//        inputProvider = InputProvider.createInstance(joypad);
        bus.attach(joypad);
        reloadWindowState();
//        createAndAddVdpEventListener();
    }

    @Override
    protected HeliosPmStateHandler createStateHandler(Path file, BaseStateHandler.Type type) {
        String fileName = file.toAbsolutePath().toString();
        return type == BaseStateHandler.Type.LOAD ? PmStateHandler.createLoadInstance(fileName, romHelper.getRomSet()) :
                PmStateHandler.createSaveInstance(fileName, romHelper.getRomSet());
    }

    Random rnd = new Random();
    int frameCounter;

    @Override
    protected void loop() {
        LOG.info("Starting game loop");
        targetNs = (long) (Duration.ofSeconds(1).toNanos() / FRAMES_HZ); //60hz
        int val = rnd.nextInt(300);
        boolean fs = true;
        do {
            try {
                if (counter == nextZ80Cycle) {
                    int cycleDelay = z80.executeInstruction();
                    cycleDelay = Math.max(1, cycleDelay);
                    nextZ80Cycle += cycleDelay;
                }
                if (counter >= Z80_CYCLES_PER_FRAME) {
                    newFrame();
//                    if(++frameCounter == val){
//                        frameCounter = 0;
//                        val = rnd.nextInt(300);
////                        emuFrame.setFullScreen(fs);
//                        fs = !fs;
//                    }
                    if (runningRomFuture.isDone()) {
                        break;
                    }
                }
                counter++;
            } catch (Exception e) {
                LOG.error("Error main cycle", e);
                break;
            }
        } while (true);
        LOG.info("Exiting rom thread loop");
    }

    @Override
    protected void newFrame() {
        nextZ80Cycle -= Z80_CYCLES_PER_FRAME;
        if (bus.isIntEnabled()) {
            z80.interrupt(true);
        }
        bus.newFrame();
        sound.onNewFrame();
        super.newFrame();
    }

    @Override
    protected void initAfterRomLoad() {
        bus.init(romHelper);
        z80 = Z80CoreWrapper.createInstance(bus);
        sound = new Sound(romHelper, bus);
        vdp = new Video(romHelper, bus.getRam(), joypad);
        bus.attach(vdp);
        resetAfterRomLoad();
    }

    @Override
    public void handleNewRom(Path file) {
        romHelper = RomHelper.createInstance(file);
        if (romHelper.isRomSetFound()) {
            romName = romHelper.getRomSetName();
            super.handleNewRom(file);
        } else {
            LOG.error("Unable to find a supported romSet in folder: {}", file.toAbsolutePath());
        }
    }

    protected void resetAfterRomLoad() {
        super.resetAfterRomLoad();
        z80.reset();
    }

    @Override
    protected void processSaveState() {
        if (saveStateFlag) {
            stateHandler.processState(z80, bus);
            if (stateHandler.getType() == BaseStateHandler.Type.SAVE) {
                stateHandler.storeData();
            }
            stateHandler = HeliosPmStateHandler.EMPTY_STATE;
            saveStateFlag = false;
        }
    }

    @Override
    protected void resetCycleCounters(int counter) {

    }
}