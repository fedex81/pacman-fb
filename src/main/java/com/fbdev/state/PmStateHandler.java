/*
 * MekaStateHandler
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 21/10/19 18:42
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

package com.fbdev.state;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.util.FileUtil;
import com.fbdev.helios.util.Util;
import com.fbdev.helios.z80.Z80Helper;
import com.fbdev.helios.z80.Z80Provider;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import z80core.Z80;
import z80core.Z80State;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;

public class PmStateHandler implements HeliosPmStateHandler {

    private static final Logger LOG = LogManager.getLogger(PmStateHandler.class.getSimpleName());

    private static final String MAGIC_WORD_STR = "PACMAN-FB";
    private static final byte[] MAGIC_WORD = MAGIC_WORD_STR.getBytes();
    private static final int Z80_REG_lEN = 26;
    private static final int Z80_MISC_LEN = 27;
    private static final int VDP_MISC_LEN = 20;
    private static final int SHA1_HASH_CHARS_LEN = 40; //20 bytes or 40 hex chars
    private final static String fileExtension = "pms";
    private final static int FIXED_SIZE_LIMIT = 0x1200;

    private static final PmSavestateVersion CURRENT_SAVE_VERSION = PmSavestateVersion.VER_1;

    private ByteBuffer buffer;
    private int version;
    private String fileName;
    private Type type;
    private RomHelper.RomSet romSet;
    private PmSavestateVersion pmVersion;

    private PmStateHandler() {
    }

    public static PmStateHandler createLoadInstance(String fileName, RomHelper.RomSet romSet) {
        PmStateHandler h = new PmStateHandler();
        h.fileName = handleFileExtension(fileName);
        h.buffer = ByteBuffer.wrap(FileUtil.readFileSafe(Paths.get(h.fileName)));
        h.type = Type.LOAD;
        h.romSet = romSet;
        return h.detectStateFileType();
    }

    public static PmStateHandler createSaveInstance(String fileName, RomHelper.RomSet romSet) {
        PmStateHandler h = new PmStateHandler();
        h.buffer = ByteBuffer.allocate(FIXED_SIZE_LIMIT);
        //file type
        h.buffer.put(MAGIC_WORD);
        h.buffer.put((byte) CURRENT_SAVE_VERSION.getVersion());
        h.buffer.put(RomHelper.getSha1Hash(romSet).getBytes());

        h.buffer.put(FIXED_SIZE_LIMIT - 3, (byte) 'E');
        h.buffer.put(FIXED_SIZE_LIMIT - 2, (byte) 'O');
        h.buffer.put(FIXED_SIZE_LIMIT - 1, (byte) 'F');

        h.version = CURRENT_SAVE_VERSION.getVersion();
        h.romSet = romSet;

        h.fileName = handleFileExtension(fileName);
        h.type = Type.SAVE;
        return h;
    }

    private static String handleFileExtension(String fileName) {
        return fileName + (!fileName.toLowerCase().contains("." + fileExtension) ? "." + fileExtension : "");
    }

    private static void skip(ByteBuffer buf, int len) {
        buf.position(buf.position() + len);
    }

    //2 bytes for a 16 bit int
    private static void setData(ByteBuffer buf, int... data) {
        Arrays.stream(data).forEach(val -> buf.put((byte) (val & 0xFF)));
    }

    private PmStateHandler detectStateFileType() {
        byte[] b = new byte[MAGIC_WORD.length];
        buffer.get(b);
        String fileType = new String(b);
        if (!MAGIC_WORD_STR.equalsIgnoreCase(fileType)) {
            LOG.error("Unable to load savestate of type: {}, size: {}", fileType, buffer.capacity());
            return null;
        }
        version = buffer.get();
        pmVersion = PmSavestateVersion.parseVersion(version);
        if (pmVersion != CURRENT_SAVE_VERSION) {
            LOG.error("Unable to handle savestate version: {}", CURRENT_SAVE_VERSION);
            return null;
        }
        byte[] sha1Chars = new byte[SHA1_HASH_CHARS_LEN];
        buffer.get(sha1Chars);
        String fileHash = new String(sha1Chars);
        String currentHash = RomHelper.getSha1Hash(romSet);
        if (!currentHash.equalsIgnoreCase(fileHash)) {
            LOG.error("Unable to handle savestate {}, current romSet {}\n with sha1hash: {}\n file sha1Hash: {}",
                    fileName, romSet, currentHash, fileHash);
            LOG.error("Known sha1Hashes: {}", RomHelper.getRomSetToSha1());
            return null;
        }
        return this;
    }

    private Z80State loadZ80State(ByteBuffer data) {
        Z80State z80State = new Z80State();
        z80State.setRegAF(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegBC(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegDE(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegHL(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegI(data.get() & 0xFF);
        z80State.setRegIX(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegIY(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegPC(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegSP(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegAFx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegBCx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegDEx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegHLx(Util.getUInt32LE(data.get(), data.get()));

        int val = data.get();
        Z80.IntMode im = Z80Helper.parseIntMode((val >> 1) & 3);
        z80State.setIM(im);
        z80State.setIFF1((val & 1) > 0);
        z80State.setIFF2((val & 8) > 0);
        z80State.setHalted((val & 0x80) > 0);

        skip(data, Z80_MISC_LEN);
        return z80State;
    }

    @Override
    public void loadZ80(Z80Provider z80, SystemBus bus) {
        Z80State z80State = loadZ80State(buffer);
        z80.loadZ80State(z80State);
    }

    @Override
    public void saveZ80(Z80Provider z80, SystemBus bus) {
        Z80State s = z80.getZ80State();
        setData(buffer, s.getRegF(), s.getRegA(), s.getRegC(), s.getRegB(),
                s.getRegE(), s.getRegD(), s.getRegL(), s.getRegH(), s.getRegI());
        setData(buffer, s.getRegIX() & 0xFF, s.getRegIX() >> 8, s.getRegIY() & 0xFF,
                s.getRegIY() >> 8, s.getRegPC() & 0xFF, s.getRegPC() >> 8, s.getRegSP() & 0xFF,
                s.getRegSP() >> 8);
        setData(buffer, s.getRegFx(), s.getRegAx(), s.getRegCx(), s.getRegBx(), s.getRegEx(),
                s.getRegDx(), s.getRegLx(), s.getRegHx());
        int val = (s.isHalted() ? 1 : 0) << 8 | (s.isIFF2() ? 1 : 0) << 3 | s.getIM().ordinal() << 1 |
                (s.isIFF1() ? 1 : 0);
        setData(buffer, val);
        skip(buffer, Z80_MISC_LEN);
    }

    @Override
    public void loadMemory(SystemBus bus) {
        buffer.get(bus.getRam());
        buffer.get(bus.getIoReg());
        bus.writeIoPort(0, buffer.getInt()); //reload addressOnBus
        bus.init();
    }

    @Override
    public void saveMemory(SystemBus bus) {
        buffer.put(bus.getRam());
        buffer.put(bus.getIoReg());
        buffer.putInt(bus.getAddressOnBus());
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getData() {
        return buffer.array();
    }

    enum PmSavestateVersion {
        UNKNOWN(0),
        VER_1(1);

        private int version;

        private PmSavestateVersion(int version) {
            this.version = version;
        }

        public static PmSavestateVersion parseVersion(int v) {
            for (PmSavestateVersion ver : PmSavestateVersion.values()) {
                if (v == ver.version) {
                    return ver;
                }
            }
            return UNKNOWN;
        }

        public int getVersion() {
            return version;
        }
    }

    public static FileFilter SAVE_STATE_FILTER = new FileFilter() {
        @Override
        public String getDescription() {
            return "state files";
        }

        @Override
        public boolean accept(File f) {
            String name = f.getName().toLowerCase();
            return f.isDirectory() || f.getName().toLowerCase().contains("." + fileExtension);
        }
    };
}
