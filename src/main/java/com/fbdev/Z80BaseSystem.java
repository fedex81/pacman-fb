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
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Duration;

public class Z80BaseSystem extends BaseSystem<SystemBus, BaseStateHandler> {

    private static final Logger LOG = LogManager.getLogger(Z80BaseSystem.class.getSimpleName());
    private static final int Z80_CLOCK_HZ = 3_072_000;
    private static final int Z80_CYCLES_PER_FRAME = Z80_CLOCK_HZ / 60;
    private static int frameCounter;
    protected Z80Provider z80;
    private int nextZ80Cycle = counter;

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
        stateHandler = BaseStateHandler.EMPTY_STATE;
        sound = SoundProvider.NO_SOUND;
//        inputProvider = InputProvider.createInstance(joypad);
        vdp = new Video(RomHelper.getInstance(), bus.getRam(), joypad);
        bus.attach(vdp).attach(joypad);
        reloadWindowState();
//        createAndAddVdpEventListener();
    }

    @Override
    protected BaseStateHandler createStateHandler(Path file, BaseStateHandler.Type type) {
        LOG.error("Not implemented!");
        return stateHandler;
    }

    @Override
    protected void loop() {
        LOG.info("Starting game loop");
        targetNs = (long) (Duration.ofSeconds(1).toNanos() / 60); //60hz

        do {
            try {
                runZ80(counter);
                counter++;
                if (counter >= Z80_CYCLES_PER_FRAME) {
                    nextZ80Cycle -= Z80_CYCLES_PER_FRAME;
                    frameCounter++;
                    if (SystemBus.enableInt) {
//                    LOG.info("interrupt");
                        z80.interrupt(true);
                    }
                    newFrame();
                }
            } catch (Exception e) {
                LOG.error("Error main cycle", e);
                break;
            }
        } while (!runningRomFuture.isDone());
        LOG.info("Exiting rom thread loop");
    }

    @Override
    protected void initAfterRomLoad() {
        z80 = Z80CoreWrapper.createInstance(bus);
        resetAfterRomLoad();
    }

    protected void resetAfterRomLoad() {
        super.resetAfterRomLoad();
        z80.reset();
    }

    @Override
    protected void processSaveState() {
        //Not implemented
    }

    @Override
    protected void resetCycleCounters(int counter) {

    }

    private void runZ80(long counter) {
        if (counter == nextZ80Cycle) {
            int cycleDelay = z80.executeInstruction();
            cycleDelay = Math.max(1, cycleDelay);
            nextZ80Cycle += cycleDelay;
        }
    }
}