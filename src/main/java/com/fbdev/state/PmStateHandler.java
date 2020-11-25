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
import com.fbdev.helios.z80.Z80Provider;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import z80core.Z80;
import z80core.Z80State;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;

public class PmStateHandler implements HeliosPmStateHandler {

    private static final Logger LOG = LogManager.getLogger(PmStateHandler.class.getSimpleName());

    private static final String MAGIC_WORD_STR = "PACMAN-FB";
    private static final byte[] MAGIC_WORD = MAGIC_WORD_STR.getBytes();
    private static final int Z80_REG_lEN = 25;
    private static final int Z80_MISC_LEN = 27;
    private static final int VDP_MISC_LEN = 20;
    private final static String fileExtension = "pms";
    private final static int FIXED_SIZE_LIMIT = 0x1200;

    private static final PmSavestateVersion DEFAULT_SAVE_VERSION = PmSavestateVersion.VER_1;

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
        h.buffer.put((byte) DEFAULT_SAVE_VERSION.getVersion());
//        h.buffer.put(romSet.name().getBytes()); //TODO

        h.buffer.put(FIXED_SIZE_LIMIT - 3, (byte) 'E');
        h.buffer.put(FIXED_SIZE_LIMIT - 2, (byte) 'O');
        h.buffer.put(FIXED_SIZE_LIMIT - 1, (byte) 'F');

        h.version = DEFAULT_SAVE_VERSION.getVersion();
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

    private static void loadMappers(ByteBuffer buffer, SystemBus bus) {
//        bus.write(0xFFFC, buffer.get(), Size.BYTE);
//        bus.write(0xFFFD, buffer.get(), Size.BYTE);
//        bus.write(0xFFFE, buffer.get(), Size.BYTE);
//        bus.write(0xFFFF, buffer.get(), Size.BYTE);
    }

    private static void saveMappers(ByteBuffer buffer, SystemBus bus) {
//        SmsBus smsbus = (SmsBus) bus;
//        int[] frameReg = smsbus.getFrameReg();
//        int control = smsbus.getMapperControl();
//        LOG.info("mapperControl: {}, frameReg: {}", control, Arrays.toString(frameReg));
//        buffer.put((byte) (control & 0xFF));
//        buffer.put(Util.unsignedToByteArray(frameReg));
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
        return this;
    }

    private Z80State loadZ80State(ByteBuffer data) {
        Z80State z80State = new Z80State();
        z80State.setRegAF(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegBC(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegDE(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegHL(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegIX(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegIY(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegPC(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegSP(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegAFx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegBCx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegDEx(Util.getUInt32LE(data.get(), data.get()));
        z80State.setRegHLx(Util.getUInt32LE(data.get(), data.get()));

        int val = data.get();
        Z80.IntMode im = ((val & 2) > 0) ? Z80.IntMode.IM1 : Z80.IntMode.IM0;
        z80State.setIM(im);
        z80State.setIFF1((val & 1) > 0);
        z80State.setIFF2((val & 8) > 0);
        z80State.setHalted((val & 0x80) > 0);

        skip(data, Z80_MISC_LEN);
        return z80State;
    }

//    @Override
//    public void loadVdp(BaseVdpProvider vdp, IMemoryProvider memory, SmsBus bus) {
//        SmsVdp smsVdp = (SmsVdp) vdp;
//        IntStream.range(0, SmsVdp.VDP_REGISTERS_SIZE).forEach(i -> smsVdp.registerWrite(i, buffer.get() & 0xFF));
//        int toSkip = VDP_MISC_LEN - 3;
//        String helString = Util.toStringValue(buffer.get(), buffer.get(), buffer.get());
//        if ("HEL".equals(helString)) {
//            vdpState[0] = buffer.getInt();
//            vdpState[1] = buffer.getInt();
//            vdpState[2] = buffer.getInt();
//            smsVdp.setStateSimple(vdpState);
//            toSkip = VDP_MISC_LEN - (vdpState.length * 4 + 3);
//        }
//        skip(buffer, toSkip);
//        loadMappers(buffer, bus);
//        if (version >= 0xD) {
//            int vdpLine = Util.getUInt32LE(buffer.get(), buffer.get());
//            LOG.info("vdpLine: {}", vdpLine);
//        }
//    }


//    @Override
//    public void saveVdp(BaseVdpProvider vdp, IMemoryProvider memory, Z80BusProvider bus) {
//        IntStream.range(0, SmsVdp.VDP_REGISTERS_SIZE).forEach(i -> buffer.put((byte) vdp.getRegisterData(i)));
//        SmsVdp smsVdp = (SmsVdp) vdp;
//        smsVdp.getStateSimple(vdpState);
//        buffer.put((byte) 'H').put((byte) 'E').put((byte) 'L');
//        buffer.putInt(vdpState[0]).putInt(vdpState[1]).putInt(vdpState[2]);
//        skip(buffer, VDP_MISC_LEN - (vdpState.length * 4 + 3));
//        saveMappers(buffer, bus);
//        buffer.put((byte) 0); //vdpLine
//        buffer.put((byte) 0); //vdpLine
//    }


    @Override
    public void loadZ80(Z80Provider z80, SystemBus bus) {
        Z80State z80State = loadZ80State(buffer);
        z80.loadZ80State(z80State);
    }

    @Override
    public void saveZ80(Z80Provider z80, SystemBus bus) {
        Z80State s = z80.getZ80State();
        setData(buffer, s.getRegF(), s.getRegA(), s.getRegC(), s.getRegB(),
                s.getRegE(), s.getRegD(), s.getRegL(), s.getRegH());
        setData(buffer, s.getRegIX() & 0xFF, s.getRegIX() >> 8, s.getRegIY() & 0xFF,
                s.getRegIX() >> 8, s.getRegPC() & 0xFF, s.getRegPC() >> 8, s.getRegSP() & 0xFF,
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

    ;
}
