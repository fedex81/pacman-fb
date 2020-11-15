package com.fbdev;

import com.fbdev.bus.SystemBus;
import com.fbdev.util.Util;
import com.fbdev.z80.Z80Helper;
import com.fbdev.z80.Z80MemIoOps;
import com.fbdev.z80.disasm.Z80Dasm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import z80core.IMemIoOps;
import z80core.Z80;

import java.io.IOException;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class Main {

    public final static boolean STOP_ON_EXCEPTION = false;
    private final static Logger LOG = LogManager.getLogger(Main.class.getSimpleName());
    private static Z80Dasm z80Dasm = new Z80Dasm();
    private static IMemIoOps memIoOps = getMemIoOps();
    private static Z80 z80;

    private static int Z80_CLOCK_HZ = 3_072_000;
    private static int Z80_CYCLES_PER_FRAME = Z80_CLOCK_HZ / 60;

    public static void main(String[] args) throws IOException {
        z80 = new Z80(memIoOps, null);
        z80.setRegPC(0);
        z80.setRegSP(SystemBus.RAM_END);
        int counter = 0;
        do {
            printDbgSimple();
            counter += executeInstruction();
            if (counter > Z80_CYCLES_PER_FRAME) {
                counter -= Z80_CYCLES_PER_FRAME;
                if (SystemBus.enableInt) {
                    LOG.info("interrupt");
                    z80.setINTLine(true);
                }
            }
        } while (true);
    }

    private static int executeInstruction() {
        memIoOps.reset();
        try {
            z80.execute();
        } catch (Exception | Error e) {
            LOG.error("z80 exception", e);
            LOG.error("Z80State: {}", Z80Helper.toString(z80.getZ80State()));
            LOG.error("Halting Z80");
            z80.setHalted(true);
            if (STOP_ON_EXCEPTION) {
                Util.waitForever();
            }
        }
        return (int) (memIoOps.getTstates());
    }

    private static void printDbgSimple() {
        LOG.info("===> {}", Z80Helper.dumpInfo(z80Dasm, memIoOps, z80.getRegPC()));
    }

    private static void printDbg() {
        LOG.info("\n===> {}\n{}", Z80Helper.dumpInfo(z80Dasm, memIoOps, z80.getRegPC()),
                Z80Helper.toString(z80.getZ80State()));
    }

    private static IMemIoOps getMemIoOps() {
        SystemBus bus = new SystemBus();
        Z80MemIoOps memIoOps = Z80MemIoOps.createInstance(bus);
        return memIoOps;
    }
}
