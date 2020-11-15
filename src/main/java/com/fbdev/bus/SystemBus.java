package com.fbdev.bus;

import com.fbdev.model.BaseBusProvider;
import com.fbdev.util.Size;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class SystemBus implements BaseBusProvider {

    private final static Logger LOG = LogManager.getLogger(SystemBus.class.getSimpleName());

    private final static int ROM_START = 0x0000;
    private final static int ROM_LENGTH = 0x4000;

    private final static int RAM_START = 0x4000;
    public final static int RAM_END = RAM_START + 0x1000;

    private final static int IO_START = 0x5000;
    private final static int IO_END = IO_START + 0x100;
    public static boolean enableInt = false;
    private static byte[] rom = new byte[ROM_LENGTH];
    private static byte[] ram = new byte[RAM_END - RAM_START];
    private int intHandlerLowByte = 0;

    public SystemBus() {
        loadRom();
    }

    private static void loadRom() {
        try {
            byte[] r6e = Files.readAllBytes(Paths.get("./data", "pacman.6e"));
            byte[] r6f = Files.readAllBytes(Paths.get("./data", "pacman.6f"));
            byte[] r6h = Files.readAllBytes(Paths.get("./data", "pacman.6h"));
            byte[] r6j = Files.readAllBytes(Paths.get("./data", "pacman.6j"));
            System.arraycopy(r6e, 0, rom, 0, r6e.length);
            System.arraycopy(r6f, 0, rom, 0x1000, r6f.length);
            System.arraycopy(r6h, 0, rom, 0x2000, r6h.length);
            System.arraycopy(r6j, 0, rom, 0x3000, r6j.length);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e);
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
//                return 0xFF;
    }

    private int readIoHandler(int address, Size size) {
        address &= 0xFF;
        if (address < 0x40) {
            LOG.info("Read IN0 port: {}", size);
        } else if (address < 0x80) {
            LOG.info("Read IN1 port: {}", size);
        } else if (address < 0xC0) {
            LOG.info("Read DIP switch settings port: {}", size);
            return 0; //free play - 1 life
        } else {
            LOG.warn("Unsupported IO read {}, {}", Long.toHexString(address), size);
            throw new RuntimeException();
        }
        return 0xFF;
    }

    @Override
    public void write(long addressL, long dataL, Size size) {
        if (size != Size.BYTE) {
            LOG.error("Invalid write at {}, {} {}", Long.toHexString(addressL),
                    Long.toHexString(dataL), size);
            throw new RuntimeException();
        }
        int address = (int) addressL;
        byte data = (byte) (dataL & 0xFF);
        if (address >= RAM_START && address < RAM_END) {
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
                LOG.info("Interrupt enable: {}", Integer.toHexString(data));
                enableInt = data != 0;
                break;
            case 1:
                LOG.info("Sound enable: {}", Integer.toHexString(data));
                break;
            case 2:
                LOG.info("Aux board enable: {}", Integer.toHexString(data));
                break;
            case 3:
                LOG.info("Flip screen: {}", Integer.toHexString(data));
                break;
            case 4:
                LOG.info("Player 1 start light : {}", Integer.toHexString(data));
                break;
            case 5:
                LOG.info("Player 2 start light : {}", Integer.toHexString(data));
                break;
            case 6:
                LOG.info("Coin lockout : {}", Integer.toHexString(data));
                break;
            case 7:
                LOG.info("Coin counter : {}", Integer.toHexString(data));
                break;
            default:
                if (address >= 0x40 && address < 0x60) {
                    LOG.info("Sound register {} : {}", Integer.toHexString(address & 0x5f), Integer.toHexString(data));
                } else if (address >= 0xC0) {
                    LOG.info("Watchdog reset : {}", Integer.toHexString(data));
                } else {
                    LOG.warn("Unsupported IO write at {}, {} {}", Long.toHexString(address),
                            Long.toHexString(data), size);
                    throw new RuntimeException();
                }
        }
    }


    @Override
    public void writeIoPort(int port, int value) {
        if ((port & 0xFF) == 0) {
            intHandlerLowByte = value;
            LOG.info("Interrupt handler low byte: {}", Integer.toHexString(intHandlerLowByte));
        } else {
            LOG.error("Invalid write at port {}, {}", Long.toHexString(port), Long.toHexString(value));
            throw new RuntimeException();
        }
    }

    @Override
    public int readIoPort(int port) {
        if ((port & 0xFF) == 0) {
            LOG.info("Read Interrupt handler low byte: {}", Integer.toHexString(intHandlerLowByte));
            return intHandlerLowByte;
        } else {
            LOG.error("Invalid read at port {}", Long.toHexString(port));
            throw new RuntimeException();
        }
    }
}
