/*
 * SystemProvider
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 13/10/19 15:41
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

package com.fbdev.helios.model;

import java.nio.file.Path;

public interface SystemProvider extends Device {

    SystemProvider systemProvider = new SystemProvider() {
        @Override
        public void handleSystemEvent(SystemEvent event, Object parameter) {
        }

        @Override
        public boolean isRomRunning() {
            return true;
        }

        @Override
        public Path getRomPath() {
            return null;
        }
    };

    void handleSystemEvent(SystemEvent event, Object parameter);

    /**
     * STATE
     **/

    boolean isRomRunning();

    Path getRomPath();

    enum SystemEvent {
        NONE,
        NEW_ROM,
        CLOSE_ROM,
        CLOSE_APP,
        RESET,
        LOAD_STATE,
        SAVE_STATE,
        QUICK_SAVE,
        QUICK_LOAD,
        TOGGLE_PAUSE,
        TOGGLE_MUTE,
        TOGGLE_FULL_SCREEN,
        TOGGLE_THROTTLE,
        CONTROLLER_CHANGE,
        SET_DEBUG_UI,
        SOFT_RESET
    }
}
