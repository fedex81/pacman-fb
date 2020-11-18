package com.fbdev.bus;

import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.model.BaseBusProvider;
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
public class SystemBus implements BaseBusProvider {

    private final static Logger LOG = LogManager.getLogger(SystemBus.class.getSimpleName());

    private final static int ROM_START = 0x0000;
    public final static int ROM_LENGTH = 0x4000;

    private final static int RAM_START = 0x4000;
    public final static int RAM_END = RAM_START + 0x1000;

    private final static int IO_START = 0x5000;
    private final static int IO_END = IO_START + 0x100;

    public final static int PALETTE_RAM_OFFSET = 0x400;

    public static boolean enableInt = false;

    private byte[] rom, ram;
    private int intHandlerLowByte = 0;
    private PacManPad joypadProvider;

    private int dipSwitchSettings =
            (1 << 0) | //0=free play, 1=1 coin per game
                    (0 << 1) | //2=1 coin per 2 games, 3=2 coins per game
                    (0 << 2) | //# lives per game: 0=1 life,1=2 lives
                    (0 << 3) | //2=3 lives, 3=5 lives
                    (0 << 4) | //Bonus score for extra life: 0=10000 points, 1=15000 points
                    (0 << 5) | //2=20000 points,3=none
                    (1 << 7); //1=normal ghost names, 0=alternate names

    public SystemBus(JoypadProvider joypadProvider) {
        this.rom = RomHelper.getInstance().getRom();
        this.ram = new byte[RAM_END - RAM_START];
        if (joypadProvider instanceof PacManPad) {
            this.joypadProvider = (PacManPad) joypadProvider;
        } else {
            LOG.error("Unexpected joypadProvider: {}", joypadProvider.getClass().getSimpleName());
        }
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
            int res = joypadProvider.getIn0();
//            LOG.info("Read IN0 port: {}", Integer.toHexString(res));
            return res;
        } else if (address < 0x80) {
            int res = joypadProvider.getIn1();
//            LOG.info("Read IN1 port: {}", Integer.toHexString(res));
            return res;
        } else if (address < 0xC0) {
//            LOG.info("Read DIP switch settings port: {}", size);
            return dipSwitchSettings;
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
            if (address >= 0x4ff0) {
                LOG.debug("Write to sprite RAM: {}, {}", Integer.toHexString(address), Integer.toHexString(data));
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

    private void writeIoHandler(int address, int data, Size size) {
        address &= 0xFF;
        switch (address) {
            case 0:
                LOG.debug("Write Interrupt enable: {}", Integer.toHexString(data));
                enableInt = data != 0;
                break;
            case 1:
                LOG.debug("Write Sound enable: {}", Integer.toHexString(data));
                break;
            case 2:
                LOG.debug("Write Aux board enable: {}", Integer.toHexString(data));
                break;
            case 3:
                LOG.debug("Write Flip screen: {}", Integer.toHexString(data));
                break;
            case 4:
                LOG.debug("Write Player 1 start light : {}", Integer.toHexString(data));
                break;
            case 5:
                LOG.debug("Write Player 2 start light : {}", Integer.toHexString(data));
                break;
            case 6:
                LOG.debug("Write Coin lockout : {}", Integer.toHexString(data));
                break;
            case 7:
                LOG.debug("Write Coin counter : {}", Integer.toHexString(data));
                break;
            default:
                if (address >= 0x40 && address < 0x60) {
                    LOG.debug("Write Sound register {} : {}", Integer.toHexString(address),
                            Integer.toHexString(data));
                } else if (address >= 0xC0) {
                    LOG.debug("Write Watchdog reset : {}", Integer.toHexString(data));
                } else if (address >= 0x60 && address < 0x80) { //TODO check 0x70 - 0x80 range
                    LOG.debug("Write Sprite x, y coordinates: {}, {}", Integer.toHexString(address),
                            Integer.toHexString(data));
                } else {
                    LOG.warn("Unsupported IO write at 50{}, {} {}", Long.toHexString(address),
                            Long.toHexString(data), size);
//                    Main.verbose = true;
                    throw new RuntimeException();
                }
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
        if ((port & 0xFF) == 0) {
            LOG.debug("Read Interrupt handler low byte: {}", Integer.toHexString(intHandlerLowByte));
            return intHandlerLowByte;
        } else {
            LOG.error("Invalid read at port {}", Long.toHexString(port));
            throw new RuntimeException();
        }
    }

    public void newFrame() {
        joypadProvider.newFrame();
    }

    public byte[] getRam() {
        return ram;
    }
}
