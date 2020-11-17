package com.fbdev.util;

import com.fbdev.bus.SystemBus;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class RomHelper {

    private final static Logger LOG = LogManager.getLogger(RomHelper.class.getSimpleName());
    private static final RomHelper INSTANCE;

    private static final Map<String, String> sha1Hashes = ImmutableMap.<String, String>builder().
            put("82s123.7f", "8d0268dee78e47c712202b0ec4f1f51109b1f2a5").
            put("82s126.1m", "bbcec0570aeceb582ff8238a4bc8546a23430081").
            put("82s126.3m", "0c4d0bee858b97632411c440bea6948a74759746").
            put("82s126.4a", "19097b5f60d1030f8b82d9f1d3a241f93e5c75d6").
            put("pacman.5e", "06ef227747a440831c9a3a613b76693d52a2f0a9").
            put("pacman.5f", "4a937ac02216ea8c96477d4a15522070507fb599").
            put("pacman.6e", "e87e059c5be45753f7e9f33dff851f16d6751181").
            put("pacman.6f", "674d3a7f00d8be5e38b1fdc208ebef5a92d38329").
            put("pacman.6h", "8e47e8c2c4d6117d174cdac150392042d3e0a881").
            put("pacman.6j", "d4a70d56bb01d27d094d73db8667ffb00ca69cb9").build();

    static {
        INSTANCE = new RomHelper();
    }

    private byte[] rom = new byte[SystemBus.ROM_LENGTH];
    private byte[] soundRom = new byte[0x200];
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
            System.arraycopy(r6e, 0, INSTANCE.rom, 0, r6e.length);
            System.arraycopy(r6f, 0, INSTANCE.rom, 0x1000, r6f.length);
            System.arraycopy(r6h, 0, INSTANCE.rom, 0x2000, r6h.length);
            System.arraycopy(r6j, 0, INSTANCE.rom, 0x3000, r6j.length);
            INSTANCE.crom = Files.readAllBytes(Paths.get("./data", "82s123.7f"));
            INSTANCE.palRom = Files.readAllBytes(Paths.get("./data", "82s126.4a"));
            INSTANCE.tileRom = Files.readAllBytes(Paths.get("./data", "pacman.5e"));
            INSTANCE.spriteRom = Files.readAllBytes(Paths.get("./data", "pacman.5f"));
            byte[] s1 = Files.readAllBytes(Paths.get("./data", "82s126.1m"));
            byte[] s2 = Files.readAllBytes(Paths.get("./data", "82s126.3m"));
            System.arraycopy(s1, 0, INSTANCE.soundRom, 0, s1.length);
            System.arraycopy(s2, 0, INSTANCE.soundRom, 0x100, s2.length);
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
