/*
 * HeliosPmStateHandler
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 25/11/2020, 16:43
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

package com.fbdev.state;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.BaseStateHandler;
import com.fbdev.helios.z80.Z80Provider;

public interface HeliosPmStateHandler extends BaseStateHandler {

    HeliosPmStateHandler EMPTY_STATE = new HeliosPmStateHandler() {

        @Override
        public Type getType() {
            return null;
        }

        @Override
        public String getFileName() {
            return null;
        }

        @Override
        public byte[] getData() {
            return new byte[0];
        }

        @Override
        public void loadZ80(Z80Provider z80, SystemBus bus) {

        }

        @Override
        public void saveZ80(Z80Provider z80, SystemBus bus) {

        }

        @Override
        public void loadMemory(SystemBus bus) {

        }

        @Override
        public void saveMemory(SystemBus bus) {

        }
    };

    default void processState(Z80Provider z80, SystemBus bus) {
        if (getType() == Type.LOAD) {
            loadZ80(z80, bus);
            loadMemory(bus);
            LOG.info("Savestate loaded from: {}", getFileName());
        } else {
            saveZ80(z80, bus);
            saveMemory(bus);
        }
    }

    void loadZ80(Z80Provider z80, SystemBus bus);

    void saveZ80(Z80Provider z80, SystemBus bus);

    void loadMemory(SystemBus bus);

    void saveMemory(SystemBus bus);
}
