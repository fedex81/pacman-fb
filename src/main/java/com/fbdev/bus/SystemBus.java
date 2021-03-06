/*
 * SystemBus
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 15:56
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

package com.fbdev.bus;

import com.fbdev.helios.BaseVdpProvider;
import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.model.BaseBusProvider;
import com.fbdev.helios.model.Device;
import com.fbdev.helios.model.IoProvider;
import com.fbdev.helios.util.Size;
import com.fbdev.input.PacManPad;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class SystemBus implements BaseBusProvider, IoProvider {

    private final static Logger LOG = LogManager.getLogger(SystemBus.class.getSimpleName());

    private final static int ROM_START = 0x0000;
    public final static int ROM_LENGTH = 0x4000;

    private final static int RAM_START = 0x4000;
    public final static int RAM_END = RAM_START + 0x1000;

    public final static int SPRITE_RAM_START = RAM_START + 0xFF0;

    private final static int IO_START = 0x5000;
    private final static int IO_END = IO_START + 0x100;

    private final static int IO_SPRITE_START = 0x5060;
    private final static int IO_SPRITE_END = 0x5070;

    public final static int PALETTE_RAM_OFFSET = 0x400;

    private boolean enableInt = false, soundEnabled = false;

    private byte[] rom, ram, ioReg;
    private int intHandlerLowByte = 0;
    private PacManPad joypadProvider;
    private BaseVdpProvider vdpProvider;

    private static final int defaultDipSwitchSettings =
            (1 << 0) | //0=free play, 1=1 coin per game
                    (0 << 1) | //2=1 coin per 2 games, 3=2 coins per game
                    (1 << 2) | //# lives per game: 0=1 life,1=2 lives
                    (1 << 3) | //2=3 lives, 3=5 lives
                    (0 << 4) | //Bonus score for extra life: 0=10000 points, 1=15000 points
                    (0 << 5) | //2=20000 points,3=none
                    (1 << 6) | //Difficulty (jumper pad): 0 = hard, 1 = normal
                    (1 << 7);  //1=normal ghost names, 0=alternate names

    private final static int dipSwitchSettings =
            Integer.parseInt(System.getProperty("pacman.dip.switch.value", String.valueOf(defaultDipSwitchSettings)));

    public SystemBus() {
        this.ram = new byte[RAM_END - RAM_START];
        this.ioReg = new byte[IO_END - IO_START];
        LOG.info("DipSwitch settings: {}", Integer.toHexString(dipSwitchSettings));
    }

    public void init(RomHelper romHelper) {
        this.rom = romHelper.getRom();
    }

    @Override
    public BaseBusProvider attach(Device obj) {
        if (obj instanceof JoypadProvider) {
            if (obj instanceof PacManPad) {
                this.joypadProvider = (PacManPad) obj;
            } else {
                LOG.error("Unexpected joypadProvider: {}", joypadProvider.getClass().getSimpleName());
            }
        } else if (obj instanceof BaseVdpProvider) {
            this.vdpProvider = (BaseVdpProvider) obj;
        } else {
            LOG.error("Unexpected object: {}", obj.getClass().getSimpleName());
        }
        return this;
    }

    @Override
    public long read(long addressL, Size size) {
        int address = (int) addressL & 0x7FFF;
        if (size != Size.BYTE) {
            LOG.error("Invalid read {}, {}", Long.toHexString(address), size);
            throw new RuntimeException();
        }
        if (address < ROM_LENGTH) {
            return rom[address];
        } else if (address >= RAM_START && address < RAM_END) {
            return ram[address - RAM_START];
        } else if (address >= IO_START && address < IO_END) {
            return readIoHandler(address, size);
        } else {
            LOG.error("Invalid read {}, {}", Long.toHexString(address), size);
            throw new RuntimeException();
        }
    }

    private int readIoHandler(int address, Size size) {
        address &= 0xFF;
        if (address < 0x40) {
            return joypadProvider.getIn0();
        } else if (address < 0x80) {
            return joypadProvider.getIn1();
        } else if (address < 0xC0) {
//            LOG.info("Read DIP switch settings port: {}", size);
            return defaultDipSwitchSettings;
        } else {
            LOG.warn("Unsupported IO read {}, {}", Long.toHexString(address), size);
            throw new RuntimeException();
        }
    }

    @Override
    public void write(long addressL, long dataL, Size size) {
        if (size != Size.BYTE) {
            LOG.error("Invalid write at {}, {} {}", Long.toHexString(addressL),
                    Long.toHexString(dataL), size);
            throw new RuntimeException();
        }
        int address = (int) addressL & 0x7FFF;
        byte data = (byte) (dataL & 0xFF);
        if (address >= RAM_START && address < RAM_END) {
            if (address >= SPRITE_RAM_START) {
                vdpProvider.updateSpriteContext(address, data & 0xFF);
            }
            ram[address - RAM_START] = data;
        } else if (address >= IO_START && address < IO_END) {
            writeIoHandler(address, data, size);
        } else {
            LOG.error("Invalid write at {}, {} {}", Long.toHexString(address),
                    Long.toHexString(data), size);
            throw new RuntimeException();
        }
    }

    //unsupported stuff
//            case 2: LOG.debug("Write Aux board enable: {}", Integer.toHexString(data));
//            case 3: LOG.debug("Write Flip screen: {}", Integer.toHexString(data));
//            case 4: LOG.debug("Write Player 1 start light : {}", Integer.toHexString(data));
//            case 5: LOG.debug("Write Player 2 start light : {}", Integer.toHexString(data));
//            case 6: LOG.debug("Write Coin lockout : {}", Integer.toHexString(data));
//            case 7: LOG.debug("Write Coin counter : {}", Integer.toHexString(data));
    private void writeIoHandler(int address, byte data, Size size) {
        address &= 0xFF;
        ioReg[address] = data;
        switch (address) {
            case 0:
                enableInt = data != 0;
                break;
            case 1:
                LOG.info("Write Sound enable: {}", Integer.toHexString(data));
                soundEnabled = data != 0;
                break;
        }
        //address >= 0x40 && address < 0x60: sound registers
        //address >= 0xC0: Watchdog reset
        if (address >= 0x60 && address < 0x70) {
            vdpProvider.updateSpriteContext(address, data & 0xFF);
        } else if (address >= 0x70 && address < 0xC0) { //getting writes in the 0x70 - 0x80 range
            LOG.warn("Unsupported IO write at 50{}, {} {}", Long.toHexString(address),
                    Long.toHexString(data), size);
        }
    }

    @Override
    public void writeIoPort(int port, int value) {
        if ((port & 0xFF) == 0) {
            intHandlerLowByte = value;
            LOG.debug("Write Interrupt handler low byte: {}", Integer.toHexString(intHandlerLowByte));
        } else {
            LOG.error("Write Invalid write at port {}, {}", Long.toHexString(port), Long.toHexString(value));
            throw new RuntimeException();
        }
    }

    @Override
    public int readIoPort(int port) {
        LOG.error("Invalid read at port {}", Long.toHexString(port));
        throw new RuntimeException();
    }

    @Override
    public boolean isIntEnabled() {
        return enableInt;
    }

    @Override
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @Override
    public int getAddressOnBus() {
        return intHandlerLowByte;
    }

    @Override
    public int readSoundData(int address) {
        return ioReg[address & 0xFF];
    }

    public void newFrame() {
        joypadProvider.newFrame();
    }

    public byte[] getRam() {
        return ram;
    }

    public byte[] getIoReg() {
        return ioReg;
    }


    @Override
    public void init() {
        //after saveState loading, trigger side-effects
        writeIoHandler(0, ioReg[0], Size.BYTE); //enableInt
        writeIoHandler(1, ioReg[1], Size.BYTE); //soundEnabled

        //update vdp
        for (int i = IO_SPRITE_START; i < IO_SPRITE_END; i++) {
            vdpProvider.updateSpriteContext(i, ioReg[i & 0xFF]);
        }
        for (int i = SPRITE_RAM_START; i < RAM_END; i++) {
            vdpProvider.updateSpriteContext(i, ram[i & 0xFFF]);
        }
    }
}
