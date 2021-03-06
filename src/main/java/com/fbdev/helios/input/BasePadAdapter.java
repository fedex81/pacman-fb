/*
 * BasePadAdapter
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 13/10/19 17:32
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

package com.fbdev.helios.input;

import com.fbdev.helios.input.InputProvider.PlayerNumber;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.fbdev.helios.input.JoypadProvider.JoypadAction.PRESSED;
import static com.fbdev.helios.input.JoypadProvider.JoypadAction.RELEASED;
import static com.fbdev.helios.input.JoypadProvider.JoypadButton.*;

public abstract class BasePadAdapter implements JoypadProvider {

    private static final Logger LOG = LogManager.getLogger(BasePadAdapter.class.getSimpleName());
    protected static Map<JoypadButton, JoypadAction> releasedMap = ImmutableMap.<JoypadButton, JoypadAction>builder().
            put(D, RELEASED).put(U, RELEASED).
            put(L, RELEASED).put(R, RELEASED).
            put(A, RELEASED).put(B, RELEASED).
            put(S, RELEASED).build();
    protected JoypadProvider.JoypadType p1Type;
    protected JoypadProvider.JoypadType p2Type;
    protected Map<JoypadButton, JoypadAction> stateMap1 = Collections.emptyMap();
    protected Map<JoypadButton, JoypadAction> stateMap2 = Collections.emptyMap();
    protected int value1 = 0xFF;
    protected int value2 = 0xFF;

    protected Map<JoypadButton, JoypadAction> getMap(PlayerNumber number) {
        switch (number) {
            case P1:
                return stateMap1;
            case P2:
                return stateMap2;
            default:
                LOG.error("Unexpected joypad number: {}", number);
                break;
        }
        return Collections.emptyMap();
    }

    protected int getValue(PlayerNumber number, JoypadButton button) {
        return getMap(number).get(button).ordinal();
    }

    @Override
    public void setButtonAction(PlayerNumber number, JoypadButton button, JoypadAction action) {
        if (getMap(number).containsKey(button)) {
            getMap(number).replace(button, action);
        }
    }

    public boolean hasDirectionPressed(PlayerNumber number) {
        return Arrays.stream(directionButton).anyMatch(b -> getMap(number).get(b) == PRESSED);
    }

    @Override
    public String getState(PlayerNumber number) {
        return getMap(number).toString();
    }
}
