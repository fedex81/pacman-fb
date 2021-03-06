/*
 * Z80Provider
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 07/04/19 16:01
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

package com.fbdev.helios.z80;

import com.fbdev.helios.model.BaseBusProvider;
import com.fbdev.helios.model.Device;
import z80core.Z80State;

public interface Z80Provider extends Device {

    int RAM_END = 0x4FFF; //pac man

    int executeInstruction();

    boolean interrupt(boolean value);

    void triggerNMI();

    boolean isHalted();

    int readMemory(int address);

    void writeMemory(int address, int data);

    BaseBusProvider getZ80BusProvider();

    void addCyclePenalty(int value);

    void loadZ80State(Z80State z80State);

    Z80State getZ80State();

    enum Interrupt {NMI, IM0, IM1, IM2}
}
