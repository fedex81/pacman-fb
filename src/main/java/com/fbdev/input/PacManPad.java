/*
 * TwoButtonsJoypad
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

package com.fbdev.input;


import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.fbdev.input.InputProvider.PlayerNumber.P1;
import static com.fbdev.input.InputProvider.PlayerNumber.P2;
import static com.fbdev.input.JoypadProvider.JoypadButton.*;

public class PacManPad extends BasePadAdapter {

    private static final Logger LOG = LogManager.getLogger(PacManPad.class.getSimpleName());

    @Override
    public void init() {
        p1Type = JoypadType.BUTTON_2;
        p2Type = JoypadType.BUTTON_2;
        LOG.info("Joypad1: {} - Joypad2: {}", p1Type, p2Type);
        stateMap1 = Maps.newHashMap(releasedMap);
        stateMap2 = Maps.newHashMap(releasedMap);
        value1 = computeIn0();
        value2 = computeIn1();
    }

    @Override
    public void newFrame() {
        value1 = computeIn0();
        value2 = computeIn1();
    }

    private int computeIn0() {
        int coinP1 = getValue(P1, A) == JoypadAction.PRESSED.ordinal() ? 1 : 0;
        int val = getValue(P1, U) | (getValue(P1, L) << 1) |  //(0=pressed, 1 = released)
                (getValue(P1, R) << 2) | (getValue(P1, D) << 3) |
                (1 << 4) | // Rack advance (switch, automatically advance to the next level, 0=on, 1 = off)
                (coinP1 << 5) | //coin slot1 button triggers 0 -> 1
                (0 << 6) | //coin slot2 button triggers 0 -> 1
                (getValue(P1, B) << 7); //Credit button (0 = pressed, 1 = released)
        return val;
    }

    private int computeIn1() {
        int val = getValue(P2, U) | (getValue(P2, L) << 1) | //(0=pressed, 1 = released)
                (getValue(P2, R) << 2) | (getValue(P2, D) << 3) |
                (1 << 4) | // Board test (switch: 0 = on, 1 = off)
                (getValue(P1, S) << 5) | //One player start button (0=pressed, 1 = released)
                (getValue(P2, S) << 6) | //Two player start button
                (0 << 7); //Cabinet mode (0=table, 1 = upright)
        return val;
    }

    public int getIn1() {
        return value2;
    }

    public int getIn0() {
        return value1;
    }
}
