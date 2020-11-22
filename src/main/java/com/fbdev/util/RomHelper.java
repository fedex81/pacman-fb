/*
 * RomHelper
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 21/11/2020, 22:54
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

package com.fbdev.util;

import com.fbdev.helios.util.Util;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.fbdev.util.RomHelper.RomType.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class RomHelper {

    private final static Logger LOG = LogManager.getLogger(RomHelper.class.getSimpleName());

    public static final String ROMS_FOLDER = "./data";

    private static final Set<RomInfo> pacmanSet = ImmutableSet.<RomInfo>builder().
            add(RomInfo.of("pacman.6e", "e87e059c5be45753f7e9f33dff851f16d6751181", 0, 0x1000, CPU)).
            add(RomInfo.of("pacman.6f", "674d3a7f00d8be5e38b1fdc208ebef5a92d38329", 0x1000, 0x2000, CPU)).
            add(RomInfo.of("pacman.6h", "8e47e8c2c4d6117d174cdac150392042d3e0a881", 0x2000, 0x3000, CPU)).
            add(RomInfo.of("pacman.6j", "d4a70d56bb01d27d094d73db8667ffb00ca69cb9", 0x3000, 0x4000, CPU)).
            add(RomInfo.of("82s126.1m", "bbcec0570aeceb582ff8238a4bc8546a23430081", 0, 0x100, RomType.SOUND)).
            add(RomInfo.of("82s126.3m", "0c4d0bee858b97632411c440bea6948a74759746", 0x100, 0x200, RomType.SOUND)).
            add(RomInfo.of("pacman.5e", "06ef227747a440831c9a3a613b76693d52a2f0a9", -1, -1, RomType.TILE)).
            add(RomInfo.of("pacman.5f", "4a937ac02216ea8c96477d4a15522070507fb599", -1, -1, RomType.SPRITE)).
            add(RomInfo.of("82s123.7f", "8d0268dee78e47c712202b0ec4f1f51109b1f2a5", -1, -1, RomType.CROM)).
            add(RomInfo.of("82s126.4a", "19097b5f60d1030f8b82d9f1d3a241f93e5c75d6", -1, -1, RomType.PAL)).
            build();
    private static final Set<RomInfo> puckmanSet = ImmutableSet.<RomInfo>builder().
            add(RomInfo.of("pm1-3.1m", "bbcec0570aeceb582ff8238a4bc8546a23430081", 0, 0x100, SOUND)).
            add(RomInfo.of("pm1-2.3m", "0c4d0bee858b97632411c440bea6948a74759746", 0x100, 0x200, SOUND)).

            add(RomInfo.of("pm1_prg1.6e", "813cecf44bf5464b1aed64b36f5047e4c79ba176", 0, 0x800, CPU)).
            add(RomInfo.of("pm1_prg2.6k", "b9ca52b63a49ddece768378d331deebbe34fe177", 0x800, 0x1000, RomType.CPU)).
            add(RomInfo.of("pm1_prg3.6f", "9b5ddaaa8b564654f97af193dbcc29f81f230a25", 0x1000, 0x1800, RomType.CPU)).
            add(RomInfo.of("pm1_prg4.6m", "c2f00e1773c6864435f29c8b7f44f2ef85d227d3", 0x1800, 0x2000, RomType.CPU)).
            add(RomInfo.of("pm1_prg5.6h", "afe72fdfec66c145b53ed865f98734686b26e921", 0x2000, 0x2800, RomType.CPU)).
            add(RomInfo.of("pm1_prg6.6n", "08759833f7e0690b2ccae573c929e2a48e5bde7f", 0x2800, 0x3000, RomType.CPU)).
            add(RomInfo.of("pm1_prg7.6j", "d249fa9cdde774d5fee7258147cd25fa3f4dc2b3", 0x3000, 0x3800, RomType.CPU)).
            add(RomInfo.of("pm1_prg8.6p", "eb462de79f49b7aa8adb0cc6d31535b10550c0ce", 0x3800, 0x4000, RomType.CPU)).

            add(RomInfo.of("pm1-1.7f", "8d0268dee78e47c712202b0ec4f1f51109b1f2a5", -1, -1, RomType.CROM)).
            add(RomInfo.of("pm1-4.4a", "19097b5f60d1030f8b82d9f1d3a241f93e5c75d6", -1, -1, RomType.PAL)).

            add(RomInfo.of("pm1_chg1.5e", "6d4ccc27d6be185589e08aa9f18702b679e49a4a", 0, 0x800, RomType.TILE)).
            add(RomInfo.of("pm1_chg2.5h", "79bb456be6c39c1ccd7d077fbe181523131fb300", 0x800, 0x1000, RomType.TILE)).
            add(RomInfo.of("pm1_chg3.5f", "be933e691df4dbe7d12123913c3b7b7b585b7a35", 0, 0x800, RomType.SPRITE)).
            add(RomInfo.of("pm1_chg4.5j", "53771c573051db43e7185b1d188533056290a620", 0x800, 0x1000, RomType.SPRITE)).
            build();

    private final static Map<String, Set<RomInfo>> romSets = ImmutableMap.of(
            "Puck Man", puckmanSet, "Pac Man", pacmanSet);

    private final Map<RomType, ByteBuffer> romTypeMap = new HashMap<>();
    private String romSetName = "";
    private boolean romSetFound = false;

    private RomHelper() {
    }

    public static RomHelper createInstance(Path folder) {
        RomHelper r = new RomHelper();
        r.detectRomSet(folder);
        return r;
    }

    private void detectRomSet(Path folder) {
        for (Map.Entry<String, Set<RomInfo>> e : romSets.entrySet()) {
            LOG.info("Attempting to load: {}", e.getKey());
            if (loadRomSet(folder, e)) {
                romSetName = e.getKey();
                romSetFound = true;
                break;
            }
        }
    }

    public boolean isRomSetFound() {
        return romSetFound;
    }

    public String getRomSetName() {
        return romSetName;
    }

    private boolean loadRomSet(Path folder, Map.Entry<String, Set<RomInfo>> entry) {
        boolean ok = true;
        try {
            for (RomInfo r : entry.getValue()) {
                byte[] data = Files.readAllBytes(Paths.get(folder.toAbsolutePath().toString(), r.fileName));
                ok &= data != null && data.length > 0;
                if (ok) {
                    String hash = Util.sha1(data);
                    if (!r.sha1.equalsIgnoreCase(hash)) {
                        LOG.warn("{} sha1 mismatch\nexpected: {}\nactual:   {}", r.fileName, r.sha1, hash);
                    }
                    int size = r.romEnd > 0 ? r.romEnd : data.length;
                    int startPos = Math.max(r.romStart, 0);
                    ByteBuffer rom = ByteBuffer.allocate(size);
                    ByteBuffer partialRom = romTypeMap.get(r.type);
                    if (partialRom != null && partialRom.limit() > 0) {
                        rom.put(partialRom.array(), 0, startPos);
                    }
                    rom.put(data, 0, data.length);
                    romTypeMap.put(r.type, rom);
                    LOG.info("Loaded: {}", r);
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to load romSet: {}, error on file: {}", entry.getKey(), e.getMessage());
            ok = false;
        }
        return ok;
    }

    public byte[] getCrom() {
        return romTypeMap.get(CROM).array();
    }

    public byte[] getPalRom() {
        return romTypeMap.get(PAL).array();
    }

    public byte[] getRom() {
        return romTypeMap.get(CPU).array();
    }

    public byte[] getSpriteRom() {
        return romTypeMap.get(SPRITE).array();
    }

    public byte[] getTileRom() {
        return romTypeMap.get(TILE).array();
    }

    public byte[] getSoundRom() {
        return romTypeMap.get(SOUND).array();
    }

    enum RomType {CPU, PAL, CROM, TILE, SPRITE, SOUND}

    static class RomInfo {
        String fileName;
        String sha1;
        int romStart;
        int romEnd;
        RomType type;

        public static RomInfo of(String fileName, String sha1, int romStart, int romEnd, RomType type) {
            RomInfo r = new RomInfo();
            r.fileName = fileName;
            r.sha1 = sha1;
            r.romStart = romStart;
            r.romEnd = romEnd;
            r.type = type;
            return r;
        }

        @Override
        public String toString() {
            return "RomInfo{" +
                    "fileName='" + fileName + '\'' +
                    ", sha1='" + sha1 + '\'' +
                    ", romStart=" + romStart +
                    ", romEnd=" + romEnd +
                    ", type=" + type +
                    '}';
        }
    }
}
