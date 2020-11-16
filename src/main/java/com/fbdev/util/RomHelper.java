package com.fbdev.util;

import com.fbdev.bus.SystemBus;
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
public class RomHelper {

    private final static Logger LOG = LogManager.getLogger(RomHelper.class.getSimpleName());
    private static final RomHelper INSTANCE;

    static {
        INSTANCE = new RomHelper();
//        loadRom();
    }

    private byte[] rom = new byte[SystemBus.ROM_LENGTH];
    private byte[] crom, palRom, tileRom, spriteRom;

    private RomHelper() {
    }

    public static RomHelper getInstance() {
        return INSTANCE;
    }

    public static void init() {
        loadRom();
    }

    private static void loadRom() {
        try {
            byte[] r6e = Files.readAllBytes(Paths.get("./data", "pacman.6e"));
            byte[] r6f = Files.readAllBytes(Paths.get("./data", "pacman.6f"));
            byte[] r6h = Files.readAllBytes(Paths.get("./data", "pacman.6h"));
            byte[] r6j = Files.readAllBytes(Paths.get("./data", "pacman.6j"));
            INSTANCE.crom = Files.readAllBytes(Paths.get("./data", "82s123.7f"));
            INSTANCE.palRom = Files.readAllBytes(Paths.get("./data", "82s126.4a"));
            INSTANCE.tileRom = Files.readAllBytes(Paths.get("./data", "pacman.5e"));
            INSTANCE.spriteRom = Files.readAllBytes(Paths.get("./data", "pacman.5f"));
            System.arraycopy(r6e, 0, INSTANCE.rom, 0, r6e.length);
            System.arraycopy(r6f, 0, INSTANCE.rom, 0x1000, r6f.length);
            System.arraycopy(r6h, 0, INSTANCE.rom, 0x2000, r6h.length);
            System.arraycopy(r6j, 0, INSTANCE.rom, 0x3000, r6j.length);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e);
        }
    }

    public byte[] getCrom() {
        return crom;
    }

    public byte[] getPalRom() {
        return palRom;
    }

    public byte[] getRom() {
        return rom;
    }

    public byte[] getSpriteRom() {
        return spriteRom;
    }

    public byte[] getTileRom() {
        return tileRom;
    }
}
