/*
 * Z80DasmIntf
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 18/11/2020, 11:28
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

package com.fbdev.helios.z80.disasm;
// license:BSD-3-Clause
// copyright-holders:Juergen Buchmueller

/**
 * Java translation
 * Federico Berti 2020
 */

import static com.fbdev.helios.z80.disasm.Z80DasmIntf.e_mnemonics.*;

public class Z80DasmIntf {


    // Disassembler constants for the return value
    static int SUPPORTED = 0x80000000;   // are disassembly flags supported?
    static int STEP_OUT = 0x40000000;   // this instruction should be the end of a step out sequence
    static int STEP_OVER = 0x20000000;   // this instruction should be stepped over by setting a breakpoint afterwards
    static int OVERINSTMASK = 0x18000000;   // number of extra instructions to skip when stepping over
    static int OVERINSTSHIFT = 27;           // bits to shift after masking to get the value
    static int LENGTHMASK = 0x0000ffff;   // the low 16-bits contain the actual length
    static String[] s_mnemonic =
            {
                    "adc", "add", "and", "bit", "call", "ccf", "cp", "cpd",
                    "cpdr", "cpi", "cpir", "cpl", "daa", "db", "dec", "di",
                    "djnz", "ei", "ex", "exx", "halt", "im", "in", "inc",
                    "ind", "indr", "ini", "inir", "jp", "jr", "ld", "ldd",
                    "lddr", "ldi", "ldir", "neg", "nop", "or", "otdr", "otir",
                    "out", "outd", "outi", "pop", "push", "res", "ret", "reti",
                    "retn", "rl", "rla", "rlc", "rlca", "rld", "rr", "rra",
                    "rrc", "rrca", "rrd", "rst", "sbc", "scf", "set", "sla",
                    "sll", "sra", "srl", "sub", "xor "
            };
    //const u32 z80_disassembler::s_flags[] =
    static int[] s_flags =
            {
                    0, 0, 0, 0, STEP_OVER, 0, 0, 0,
                    STEP_OVER, 0, STEP_OVER, 0, 0, 0, 0, 0,
                    STEP_OVER, 0, 0, 0, STEP_OVER, 0, 0, 0,
                    0, STEP_OVER, 0, STEP_OVER, 0, 0, 0, 0,
                    STEP_OVER, 0, STEP_OVER, 0, 0, 0, STEP_OVER, STEP_OVER,
                    0, 0, 0, 0, 0, 0, STEP_OUT, STEP_OUT,
                    STEP_OUT, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, STEP_OVER, 0, 0, 0, 0,
                    0, 0, 0, 0, 0
            };
    static z80dasmStruct[] mnemonic_xx_cb = {
            new z80dasmStruct(zRLC, "b=Y"), new z80dasmStruct(zRLC, "c=Y"), new z80dasmStruct(zRLC, "d=Y"), new z80dasmStruct(zRLC, "e=Y"),
            new z80dasmStruct(zRLC, "h=Y"), new z80dasmStruct(zRLC, "l=Y"), new z80dasmStruct(zRLC, "Y"), new z80dasmStruct(zRLC, "a=Y"),
            new z80dasmStruct(zRRC, "b=Y"), new z80dasmStruct(zRRC, "c=Y"), new z80dasmStruct(zRRC, "d=Y"), new z80dasmStruct(zRRC, "e=Y"),
            new z80dasmStruct(zRRC, "h=Y"), new z80dasmStruct(zRRC, "l=Y"), new z80dasmStruct(zRRC, "Y"), new z80dasmStruct(zRRC, "a=Y"),
            new z80dasmStruct(zRL, "b=Y"), new z80dasmStruct(zRL, "c=Y"), new z80dasmStruct(zRL, "d=Y"), new z80dasmStruct(zRL, "e=Y"),
            new z80dasmStruct(zRL, "h=Y"), new z80dasmStruct(zRL, "l=Y"), new z80dasmStruct(zRL, "Y"), new z80dasmStruct(zRL, "a=Y"),
            new z80dasmStruct(zRR, "b=Y"), new z80dasmStruct(zRR, "c=Y"), new z80dasmStruct(zRR, "d=Y"), new z80dasmStruct(zRR, "e=Y"),
            new z80dasmStruct(zRR, "h=Y"), new z80dasmStruct(zRR, "l=Y"), new z80dasmStruct(zRR, "Y"), new z80dasmStruct(zRR, "a=Y"),
            new z80dasmStruct(zSLA, "b=Y"), new z80dasmStruct(zSLA, "c=Y"), new z80dasmStruct(zSLA, "d=Y"), new z80dasmStruct(zSLA, "e=Y"),
            new z80dasmStruct(zSLA, "h=Y"), new z80dasmStruct(zSLA, "l=Y"), new z80dasmStruct(zSLA, "Y"), new z80dasmStruct(zSLA, "a=Y"),
            new z80dasmStruct(zSRA, "b=Y"), new z80dasmStruct(zSRA, "c=Y"), new z80dasmStruct(zSRA, "d=Y"), new z80dasmStruct(zSRA, "e=Y"),
            new z80dasmStruct(zSRA, "h=Y"), new z80dasmStruct(zSRA, "l=Y"), new z80dasmStruct(zSRA, "Y"), new z80dasmStruct(zSRA, "a=Y"),
            new z80dasmStruct(zSLL, "b=Y"), new z80dasmStruct(zSLL, "c=Y"), new z80dasmStruct(zSLL, "d=Y"), new z80dasmStruct(zSLL, "e=Y"),
            new z80dasmStruct(zSLL, "h=Y"), new z80dasmStruct(zSLL, "l=Y"), new z80dasmStruct(zSLL, "Y"), new z80dasmStruct(zSLL, "a=Y"),
            new z80dasmStruct(zSRL, "b=Y"), new z80dasmStruct(zSRL, "c=Y"), new z80dasmStruct(zSRL, "d=Y"), new z80dasmStruct(zSRL, "e=Y"),
            new z80dasmStruct(zSRL, "h=Y"), new z80dasmStruct(zSRL, "l=Y"), new z80dasmStruct(zSRL, "Y"), new z80dasmStruct(zSRL, "a=Y"),
            new z80dasmStruct(zBIT, "b=0,Y"), new z80dasmStruct(zBIT, "c=0,Y"), new z80dasmStruct(zBIT, "d=0,Y"), new z80dasmStruct(zBIT, "e=0,Y"),
            new z80dasmStruct(zBIT, "h=0,Y"), new z80dasmStruct(zBIT, "l=0,Y"), new z80dasmStruct(zBIT, "0,Y"), new z80dasmStruct(zBIT, "a=0,Y"),
            new z80dasmStruct(zBIT, "b=1,Y"), new z80dasmStruct(zBIT, "c=1,Y"), new z80dasmStruct(zBIT, "d=1,Y"), new z80dasmStruct(zBIT, "e=1,Y"),
            new z80dasmStruct(zBIT, "h=1,Y"), new z80dasmStruct(zBIT, "l=1,Y"), new z80dasmStruct(zBIT, "1,Y"), new z80dasmStruct(zBIT, "a=1,Y"),
            new z80dasmStruct(zBIT, "b=2,Y"), new z80dasmStruct(zBIT, "c=2,Y"), new z80dasmStruct(zBIT, "d=2,Y"), new z80dasmStruct(zBIT, "e=2,Y"),
            new z80dasmStruct(zBIT, "h=2,Y"), new z80dasmStruct(zBIT, "l=2,Y"), new z80dasmStruct(zBIT, "2,Y"), new z80dasmStruct(zBIT, "a=2,Y"),
            new z80dasmStruct(zBIT, "b=3,Y"), new z80dasmStruct(zBIT, "c=3,Y"), new z80dasmStruct(zBIT, "d=3,Y"), new z80dasmStruct(zBIT, "e=3,Y"),
            new z80dasmStruct(zBIT, "h=3,Y"), new z80dasmStruct(zBIT, "l=3,Y"), new z80dasmStruct(zBIT, "3,Y"), new z80dasmStruct(zBIT, "a=3,Y"),
            new z80dasmStruct(zBIT, "b=4,Y"), new z80dasmStruct(zBIT, "c=4,Y"), new z80dasmStruct(zBIT, "d=4,Y"), new z80dasmStruct(zBIT, "e=4,Y"),
            new z80dasmStruct(zBIT, "h=4,Y"), new z80dasmStruct(zBIT, "l=4,Y"), new z80dasmStruct(zBIT, "4,Y"), new z80dasmStruct(zBIT, "a=4,Y"),
            new z80dasmStruct(zBIT, "b=5,Y"), new z80dasmStruct(zBIT, "c=5,Y"), new z80dasmStruct(zBIT, "d=5,Y"), new z80dasmStruct(zBIT, "e=5,Y"),
            new z80dasmStruct(zBIT, "h=5,Y"), new z80dasmStruct(zBIT, "l=5,Y"), new z80dasmStruct(zBIT, "5,Y"), new z80dasmStruct(zBIT, "a=5,Y"),
            new z80dasmStruct(zBIT, "b=6,Y"), new z80dasmStruct(zBIT, "c=6,Y"), new z80dasmStruct(zBIT, "d=6,Y"), new z80dasmStruct(zBIT, "e=6,Y"),
            new z80dasmStruct(zBIT, "h=6,Y"), new z80dasmStruct(zBIT, "l=6,Y"), new z80dasmStruct(zBIT, "6,Y"), new z80dasmStruct(zBIT, "a=6,Y"),
            new z80dasmStruct(zBIT, "b=7,Y"), new z80dasmStruct(zBIT, "c=7,Y"), new z80dasmStruct(zBIT, "d=7,Y"), new z80dasmStruct(zBIT, "e=7,Y"),
            new z80dasmStruct(zBIT, "h=7,Y"), new z80dasmStruct(zBIT, "l=7,Y"), new z80dasmStruct(zBIT, "7,Y"), new z80dasmStruct(zBIT, "a=7,Y"),
            new z80dasmStruct(zRES, "b=0,Y"), new z80dasmStruct(zRES, "c=0,Y"), new z80dasmStruct(zRES, "d=0,Y"), new z80dasmStruct(zRES, "e=0,Y"),
            new z80dasmStruct(zRES, "h=0,Y"), new z80dasmStruct(zRES, "l=0,Y"), new z80dasmStruct(zRES, "0,Y"), new z80dasmStruct(zRES, "a=0,Y"),
            new z80dasmStruct(zRES, "b=1,Y"), new z80dasmStruct(zRES, "c=1,Y"), new z80dasmStruct(zRES, "d=1,Y"), new z80dasmStruct(zRES, "e=1,Y"),
            new z80dasmStruct(zRES, "h=1,Y"), new z80dasmStruct(zRES, "l=1,Y"), new z80dasmStruct(zRES, "1,Y"), new z80dasmStruct(zRES, "a=1,Y"),
            new z80dasmStruct(zRES, "b=2,Y"), new z80dasmStruct(zRES, "c=2,Y"), new z80dasmStruct(zRES, "d=2,Y"), new z80dasmStruct(zRES, "e=2,Y"),
            new z80dasmStruct(zRES, "h=2,Y"), new z80dasmStruct(zRES, "l=2,Y"), new z80dasmStruct(zRES, "2,Y"), new z80dasmStruct(zRES, "a=2,Y"),
            new z80dasmStruct(zRES, "b=3,Y"), new z80dasmStruct(zRES, "c=3,Y"), new z80dasmStruct(zRES, "d=3,Y"), new z80dasmStruct(zRES, "e=3,Y"),
            new z80dasmStruct(zRES, "h=3,Y"), new z80dasmStruct(zRES, "l=3,Y"), new z80dasmStruct(zRES, "3,Y"), new z80dasmStruct(zRES, "a=3,Y"),
            new z80dasmStruct(zRES, "b=4,Y"), new z80dasmStruct(zRES, "c=4,Y"), new z80dasmStruct(zRES, "d=4,Y"), new z80dasmStruct(zRES, "e=4,Y"),
            new z80dasmStruct(zRES, "h=4,Y"), new z80dasmStruct(zRES, "l=4,Y"), new z80dasmStruct(zRES, "4,Y"), new z80dasmStruct(zRES, "a=4,Y"),
            new z80dasmStruct(zRES, "b=5,Y"), new z80dasmStruct(zRES, "c=5,Y"), new z80dasmStruct(zRES, "d=5,Y"), new z80dasmStruct(zRES, "e=5,Y"),
            new z80dasmStruct(zRES, "h=5,Y"), new z80dasmStruct(zRES, "l=5,Y"), new z80dasmStruct(zRES, "5,Y"), new z80dasmStruct(zRES, "a=5,Y"),
            new z80dasmStruct(zRES, "b=6,Y"), new z80dasmStruct(zRES, "c=6,Y"), new z80dasmStruct(zRES, "d=6,Y"), new z80dasmStruct(zRES, "e=6,Y"),
            new z80dasmStruct(zRES, "h=6,Y"), new z80dasmStruct(zRES, "l=6,Y"), new z80dasmStruct(zRES, "6,Y"), new z80dasmStruct(zRES, "a=6,Y"),
            new z80dasmStruct(zRES, "b=7,Y"), new z80dasmStruct(zRES, "c=7,Y"), new z80dasmStruct(zRES, "d=7,Y"), new z80dasmStruct(zRES, "e=7,Y"),
            new z80dasmStruct(zRES, "h=7,Y"), new z80dasmStruct(zRES, "l=7,Y"), new z80dasmStruct(zRES, "7,Y"), new z80dasmStruct(zRES, "a=7,Y"),
            new z80dasmStruct(zSET, "b=0,Y"), new z80dasmStruct(zSET, "c=0,Y"), new z80dasmStruct(zSET, "d=0,Y"), new z80dasmStruct(zSET, "e=0,Y"),
            new z80dasmStruct(zSET, "h=0,Y"), new z80dasmStruct(zSET, "l=0,Y"), new z80dasmStruct(zSET, "0,Y"), new z80dasmStruct(zSET, "a=0,Y"),
            new z80dasmStruct(zSET, "b=1,Y"), new z80dasmStruct(zSET, "c=1,Y"), new z80dasmStruct(zSET, "d=1,Y"), new z80dasmStruct(zSET, "e=1,Y"),
            new z80dasmStruct(zSET, "h=1,Y"), new z80dasmStruct(zSET, "l=1,Y"), new z80dasmStruct(zSET, "1,Y"), new z80dasmStruct(zSET, "a=1,Y"),
            new z80dasmStruct(zSET, "b=2,Y"), new z80dasmStruct(zSET, "c=2,Y"), new z80dasmStruct(zSET, "d=2,Y"), new z80dasmStruct(zSET, "e=2,Y"),
            new z80dasmStruct(zSET, "h=2,Y"), new z80dasmStruct(zSET, "l=2,Y"), new z80dasmStruct(zSET, "2,Y"), new z80dasmStruct(zSET, "a=2,Y"),
            new z80dasmStruct(zSET, "b=3,Y"), new z80dasmStruct(zSET, "c=3,Y"), new z80dasmStruct(zSET, "d=3,Y"), new z80dasmStruct(zSET, "e=3,Y"),
            new z80dasmStruct(zSET, "h=3,Y"), new z80dasmStruct(zSET, "l=3,Y"), new z80dasmStruct(zSET, "3,Y"), new z80dasmStruct(zSET, "a=3,Y"),
            new z80dasmStruct(zSET, "b=4,Y"), new z80dasmStruct(zSET, "c=4,Y"), new z80dasmStruct(zSET, "d=4,Y"), new z80dasmStruct(zSET, "e=4,Y"),
            new z80dasmStruct(zSET, "h=4,Y"), new z80dasmStruct(zSET, "l=4,Y"), new z80dasmStruct(zSET, "4,Y"), new z80dasmStruct(zSET, "a=4,Y"),
            new z80dasmStruct(zSET, "b=5,Y"), new z80dasmStruct(zSET, "c=5,Y"), new z80dasmStruct(zSET, "d=5,Y"), new z80dasmStruct(zSET, "e=5,Y"),
            new z80dasmStruct(zSET, "h=5,Y"), new z80dasmStruct(zSET, "l=5,Y"), new z80dasmStruct(zSET, "5,Y"), new z80dasmStruct(zSET, "a=5,Y"),
            new z80dasmStruct(zSET, "b=6,Y"), new z80dasmStruct(zSET, "c=6,Y"), new z80dasmStruct(zSET, "d=6,Y"), new z80dasmStruct(zSET, "e=6,Y"),
            new z80dasmStruct(zSET, "h=6,Y"), new z80dasmStruct(zSET, "l=6,Y"), new z80dasmStruct(zSET, "6,Y"), new z80dasmStruct(zSET, "a=6,Y"),
            new z80dasmStruct(zSET, "b=7,Y"), new z80dasmStruct(zSET, "c=7,Y"), new z80dasmStruct(zSET, "d=7,Y"), new z80dasmStruct(zSET, "e=7,Y"),
            new z80dasmStruct(zSET, "h=7,Y"), new z80dasmStruct(zSET, "l=7,Y"), new z80dasmStruct(zSET, "7,Y"), new z80dasmStruct(zSET, "a=7,Y")
    };
    static z80dasmStruct[] mnemonic_cb =
            {
                    new z80dasmStruct(zRLC, "b"), new z80dasmStruct(zRLC, "c"), new z80dasmStruct(zRLC, "d"), new z80dasmStruct(zRLC, "e"),
                    new z80dasmStruct(zRLC, "h"), new z80dasmStruct(zRLC, "l"), new z80dasmStruct(zRLC, "(hl)"), new z80dasmStruct(zRLC, "a"),
                    new z80dasmStruct(zRRC, "b"), new z80dasmStruct(zRRC, "c"), new z80dasmStruct(zRRC, "d"), new z80dasmStruct(zRRC, "e"),
                    new z80dasmStruct(zRRC, "h"), new z80dasmStruct(zRRC, "l"), new z80dasmStruct(zRRC, "(hl)"), new z80dasmStruct(zRRC, "a"),
                    new z80dasmStruct(zRL, "b"), new z80dasmStruct(zRL, "c"), new z80dasmStruct(zRL, "d"), new z80dasmStruct(zRL, "e"),
                    new z80dasmStruct(zRL, "h"), new z80dasmStruct(zRL, "l"), new z80dasmStruct(zRL, "(hl)"), new z80dasmStruct(zRL, "a"),
                    new z80dasmStruct(zRR, "b"), new z80dasmStruct(zRR, "c"), new z80dasmStruct(zRR, "d"), new z80dasmStruct(zRR, "e"),
                    new z80dasmStruct(zRR, "h"), new z80dasmStruct(zRR, "l"), new z80dasmStruct(zRR, "(hl)"), new z80dasmStruct(zRR, "a"),
                    new z80dasmStruct(zSLA, "b"), new z80dasmStruct(zSLA, "c"), new z80dasmStruct(zSLA, "d"), new z80dasmStruct(zSLA, "e"),
                    new z80dasmStruct(zSLA, "h"), new z80dasmStruct(zSLA, "l"), new z80dasmStruct(zSLA, "(hl)"), new z80dasmStruct(zSLA, "a"),
                    new z80dasmStruct(zSRA, "b"), new z80dasmStruct(zSRA, "c"), new z80dasmStruct(zSRA, "d"), new z80dasmStruct(zSRA, "e"),
                    new z80dasmStruct(zSRA, "h"), new z80dasmStruct(zSRA, "l"), new z80dasmStruct(zSRA, "(hl)"), new z80dasmStruct(zSRA, "a"),
                    new z80dasmStruct(zSLL, "b"), new z80dasmStruct(zSLL, "c"), new z80dasmStruct(zSLL, "d"), new z80dasmStruct(zSLL, "e"),
                    new z80dasmStruct(zSLL, "h"), new z80dasmStruct(zSLL, "l"), new z80dasmStruct(zSLL, "(hl)"), new z80dasmStruct(zSLL, "a"),
                    new z80dasmStruct(zSRL, "b"), new z80dasmStruct(zSRL, "c"), new z80dasmStruct(zSRL, "d"), new z80dasmStruct(zSRL, "e"),
                    new z80dasmStruct(zSRL, "h"), new z80dasmStruct(zSRL, "l"), new z80dasmStruct(zSRL, "(hl)"), new z80dasmStruct(zSRL, "a"),
                    new z80dasmStruct(zBIT, "0,b"), new z80dasmStruct(zBIT, "0,c"), new z80dasmStruct(zBIT, "0,d"), new z80dasmStruct(zBIT, "0,e"),
                    new z80dasmStruct(zBIT, "0,h"), new z80dasmStruct(zBIT, "0,l"), new z80dasmStruct(zBIT, "0,(hl)"), new z80dasmStruct(zBIT, "0,a"),
                    new z80dasmStruct(zBIT, "1,b"), new z80dasmStruct(zBIT, "1,c"), new z80dasmStruct(zBIT, "1,d"), new z80dasmStruct(zBIT, "1,e"),
                    new z80dasmStruct(zBIT, "1,h"), new z80dasmStruct(zBIT, "1,l"), new z80dasmStruct(zBIT, "1,(hl)"), new z80dasmStruct(zBIT, "1,a"),
                    new z80dasmStruct(zBIT, "2,b"), new z80dasmStruct(zBIT, "2,c"), new z80dasmStruct(zBIT, "2,d"), new z80dasmStruct(zBIT, "2,e"),
                    new z80dasmStruct(zBIT, "2,h"), new z80dasmStruct(zBIT, "2,l"), new z80dasmStruct(zBIT, "2,(hl)"), new z80dasmStruct(zBIT, "2,a"),
                    new z80dasmStruct(zBIT, "3,b"), new z80dasmStruct(zBIT, "3,c"), new z80dasmStruct(zBIT, "3,d"), new z80dasmStruct(zBIT, "3,e"),
                    new z80dasmStruct(zBIT, "3,h"), new z80dasmStruct(zBIT, "3,l"), new z80dasmStruct(zBIT, "3,(hl)"), new z80dasmStruct(zBIT, "3,a"),
                    new z80dasmStruct(zBIT, "4,b"), new z80dasmStruct(zBIT, "4,c"), new z80dasmStruct(zBIT, "4,d"), new z80dasmStruct(zBIT, "4,e"),
                    new z80dasmStruct(zBIT, "4,h"), new z80dasmStruct(zBIT, "4,l"), new z80dasmStruct(zBIT, "4,(hl)"), new z80dasmStruct(zBIT, "4,a"),
                    new z80dasmStruct(zBIT, "5,b"), new z80dasmStruct(zBIT, "5,c"), new z80dasmStruct(zBIT, "5,d"), new z80dasmStruct(zBIT, "5,e"),
                    new z80dasmStruct(zBIT, "5,h"), new z80dasmStruct(zBIT, "5,l"), new z80dasmStruct(zBIT, "5,(hl)"), new z80dasmStruct(zBIT, "5,a"),
                    new z80dasmStruct(zBIT, "6,b"), new z80dasmStruct(zBIT, "6,c"), new z80dasmStruct(zBIT, "6,d"), new z80dasmStruct(zBIT, "6,e"),
                    new z80dasmStruct(zBIT, "6,h"), new z80dasmStruct(zBIT, "6,l"), new z80dasmStruct(zBIT, "6,(hl)"), new z80dasmStruct(zBIT, "6,a"),
                    new z80dasmStruct(zBIT, "7,b"), new z80dasmStruct(zBIT, "7,c"), new z80dasmStruct(zBIT, "7,d"), new z80dasmStruct(zBIT, "7,e"),
                    new z80dasmStruct(zBIT, "7,h"), new z80dasmStruct(zBIT, "7,l"), new z80dasmStruct(zBIT, "7,(hl)"), new z80dasmStruct(zBIT, "7,a"),
                    new z80dasmStruct(zRES, "0,b"), new z80dasmStruct(zRES, "0,c"), new z80dasmStruct(zRES, "0,d"), new z80dasmStruct(zRES, "0,e"),
                    new z80dasmStruct(zRES, "0,h"), new z80dasmStruct(zRES, "0,l"), new z80dasmStruct(zRES, "0,(hl)"), new z80dasmStruct(zRES, "0,a"),
                    new z80dasmStruct(zRES, "1,b"), new z80dasmStruct(zRES, "1,c"), new z80dasmStruct(zRES, "1,d"), new z80dasmStruct(zRES, "1,e"),
                    new z80dasmStruct(zRES, "1,h"), new z80dasmStruct(zRES, "1,l"), new z80dasmStruct(zRES, "1,(hl)"), new z80dasmStruct(zRES, "1,a"),
                    new z80dasmStruct(zRES, "2,b"), new z80dasmStruct(zRES, "2,c"), new z80dasmStruct(zRES, "2,d"), new z80dasmStruct(zRES, "2,e"),
                    new z80dasmStruct(zRES, "2,h"), new z80dasmStruct(zRES, "2,l"), new z80dasmStruct(zRES, "2,(hl)"), new z80dasmStruct(zRES, "2,a"),
                    new z80dasmStruct(zRES, "3,b"), new z80dasmStruct(zRES, "3,c"), new z80dasmStruct(zRES, "3,d"), new z80dasmStruct(zRES, "3,e"),
                    new z80dasmStruct(zRES, "3,h"), new z80dasmStruct(zRES, "3,l"), new z80dasmStruct(zRES, "3,(hl)"), new z80dasmStruct(zRES, "3,a"),
                    new z80dasmStruct(zRES, "4,b"), new z80dasmStruct(zRES, "4,c"), new z80dasmStruct(zRES, "4,d"), new z80dasmStruct(zRES, "4,e"),
                    new z80dasmStruct(zRES, "4,h"), new z80dasmStruct(zRES, "4,l"), new z80dasmStruct(zRES, "4,(hl)"), new z80dasmStruct(zRES, "4,a"),
                    new z80dasmStruct(zRES, "5,b"), new z80dasmStruct(zRES, "5,c"), new z80dasmStruct(zRES, "5,d"), new z80dasmStruct(zRES, "5,e"),
                    new z80dasmStruct(zRES, "5,h"), new z80dasmStruct(zRES, "5,l"), new z80dasmStruct(zRES, "5,(hl)"), new z80dasmStruct(zRES, "5,a"),
                    new z80dasmStruct(zRES, "6,b"), new z80dasmStruct(zRES, "6,c"), new z80dasmStruct(zRES, "6,d"), new z80dasmStruct(zRES, "6,e"),
                    new z80dasmStruct(zRES, "6,h"), new z80dasmStruct(zRES, "6,l"), new z80dasmStruct(zRES, "6,(hl)"), new z80dasmStruct(zRES, "6,a"),
                    new z80dasmStruct(zRES, "7,b"), new z80dasmStruct(zRES, "7,c"), new z80dasmStruct(zRES, "7,d"), new z80dasmStruct(zRES, "7,e"),
                    new z80dasmStruct(zRES, "7,h"), new z80dasmStruct(zRES, "7,l"), new z80dasmStruct(zRES, "7,(hl)"), new z80dasmStruct(zRES, "7,a"),
                    new z80dasmStruct(zSET, "0,b"), new z80dasmStruct(zSET, "0,c"), new z80dasmStruct(zSET, "0,d"), new z80dasmStruct(zSET, "0,e"),
                    new z80dasmStruct(zSET, "0,h"), new z80dasmStruct(zSET, "0,l"), new z80dasmStruct(zSET, "0,(hl)"), new z80dasmStruct(zSET, "0,a"),
                    new z80dasmStruct(zSET, "1,b"), new z80dasmStruct(zSET, "1,c"), new z80dasmStruct(zSET, "1,d"), new z80dasmStruct(zSET, "1,e"),
                    new z80dasmStruct(zSET, "1,h"), new z80dasmStruct(zSET, "1,l"), new z80dasmStruct(zSET, "1,(hl)"), new z80dasmStruct(zSET, "1,a"),
                    new z80dasmStruct(zSET, "2,b"), new z80dasmStruct(zSET, "2,c"), new z80dasmStruct(zSET, "2,d"), new z80dasmStruct(zSET, "2,e"),
                    new z80dasmStruct(zSET, "2,h"), new z80dasmStruct(zSET, "2,l"), new z80dasmStruct(zSET, "2,(hl)"), new z80dasmStruct(zSET, "2,a"),
                    new z80dasmStruct(zSET, "3,b"), new z80dasmStruct(zSET, "3,c"), new z80dasmStruct(zSET, "3,d"), new z80dasmStruct(zSET, "3,e"),
                    new z80dasmStruct(zSET, "3,h"), new z80dasmStruct(zSET, "3,l"), new z80dasmStruct(zSET, "3,(hl)"), new z80dasmStruct(zSET, "3,a"),
                    new z80dasmStruct(zSET, "4,b"), new z80dasmStruct(zSET, "4,c"), new z80dasmStruct(zSET, "4,d"), new z80dasmStruct(zSET, "4,e"),
                    new z80dasmStruct(zSET, "4,h"), new z80dasmStruct(zSET, "4,l"), new z80dasmStruct(zSET, "4,(hl)"), new z80dasmStruct(zSET, "4,a"),
                    new z80dasmStruct(zSET, "5,b"), new z80dasmStruct(zSET, "5,c"), new z80dasmStruct(zSET, "5,d"), new z80dasmStruct(zSET, "5,e"),
                    new z80dasmStruct(zSET, "5,h"), new z80dasmStruct(zSET, "5,l"), new z80dasmStruct(zSET, "5,(hl)"), new z80dasmStruct(zSET, "5,a"),
                    new z80dasmStruct(zSET, "6,b"), new z80dasmStruct(zSET, "6,c"), new z80dasmStruct(zSET, "6,d"), new z80dasmStruct(zSET, "6,e"),
                    new z80dasmStruct(zSET, "6,h"), new z80dasmStruct(zSET, "6,l"), new z80dasmStruct(zSET, "6,(hl)"), new z80dasmStruct(zSET, "6,a"),
                    new z80dasmStruct(zSET, "7,b"), new z80dasmStruct(zSET, "7,c"), new z80dasmStruct(zSET, "7,d"), new z80dasmStruct(zSET, "7,e"),
                    new z80dasmStruct(zSET, "7,h"), new z80dasmStruct(zSET, "7,l"), new z80dasmStruct(zSET, "7,(hl)"), new z80dasmStruct(zSET, "7,a")
            };
    static z80dasmStruct[] mnemonic_ed =
            {
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zIN, "b,(c)"), new z80dasmStruct(zOUT, "(c),b"), new z80dasmStruct(zSBC, "hl,bc"), new z80dasmStruct(zLD, "(W),bc"),
                    new z80dasmStruct(zNEG, null), new z80dasmStruct(zRETN, null), new z80dasmStruct(zIM, "0"), new z80dasmStruct(zLD, "i,a"),
                    new z80dasmStruct(zIN, "c,(c)"), new z80dasmStruct(zOUT, "(c),c"), new z80dasmStruct(zADC, "hl,bc"), new z80dasmStruct(zLD, "bc,(W)"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETI, null), new z80dasmStruct(zIM, "0"), new z80dasmStruct(zLD, "r,a"),
                    new z80dasmStruct(zIN, "d,(c)"), new z80dasmStruct(zOUT, "(c),d"), new z80dasmStruct(zSBC, "hl,de"), new z80dasmStruct(zLD, "(W),de"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETN, null), new z80dasmStruct(zIM, "1"), new z80dasmStruct(zLD, "a,i"),
                    new z80dasmStruct(zIN, "e,(c)"), new z80dasmStruct(zOUT, "(c),e"), new z80dasmStruct(zADC, "hl,de"), new z80dasmStruct(zLD, "de,(W)"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETI, null), new z80dasmStruct(zIM, "2"), new z80dasmStruct(zLD, "a,r"),
                    new z80dasmStruct(zIN, "h,(c)"), new z80dasmStruct(zOUT, "(c),h"), new z80dasmStruct(zSBC, "hl,hl"), new z80dasmStruct(zLD, "(W),hl"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETN, null), new z80dasmStruct(zIM, "0"), new z80dasmStruct(zRRD, "(hl)"),
                    new z80dasmStruct(zIN, "l,(c)"), new z80dasmStruct(zOUT, "(c),l"), new z80dasmStruct(zADC, "hl,hl"), new z80dasmStruct(zLD, "hl,(W)"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETI, null), new z80dasmStruct(zIM, "0"), new z80dasmStruct(zRLD, "(hl)"),
                    new z80dasmStruct(zIN, "0,(c)"), new z80dasmStruct(zOUT, "(c),0"), new z80dasmStruct(zSBC, "hl,sp"), new z80dasmStruct(zLD, "(W),sp"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETN, null), new z80dasmStruct(zIM, "1"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zIN, "a,(c)"), new z80dasmStruct(zOUT, "(c),a"), new z80dasmStruct(zADC, "hl,sp"), new z80dasmStruct(zLD, "sp,(W)"),
                    new z80dasmStruct(zNEG, "*"), new z80dasmStruct(zRETI, null), new z80dasmStruct(zIM, "2"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLDI, null), new z80dasmStruct(zCPI, null), new z80dasmStruct(zINI, null), new z80dasmStruct(zOUTI, null),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLDD, null), new z80dasmStruct(zCPD, null), new z80dasmStruct(zIND, null), new z80dasmStruct(zOUTD, null),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLDIR, null), new z80dasmStruct(zCPIR, null), new z80dasmStruct(zINIR, null), new z80dasmStruct(zOTIR, null),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLDDR, null), new z80dasmStruct(zCPDR, null), new z80dasmStruct(zINDR, null), new z80dasmStruct(zOTDR, null),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?")
            };
    static z80dasmStruct[] mnemonic_xx =
            {
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zADD, "I,bc"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zADD, "I,de"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zLD, "I,N"), new z80dasmStruct(zLD, "(W),I"), new z80dasmStruct(zINC, "I"),
                    new z80dasmStruct(zINC, "Ih"), new z80dasmStruct(zDEC, "Ih"), new z80dasmStruct(zLD, "Ih,B"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zADD, "I,I"), new z80dasmStruct(zLD, "I,(W)"), new z80dasmStruct(zDEC, "I"),
                    new z80dasmStruct(zINC, "Il"), new z80dasmStruct(zDEC, "Il"), new z80dasmStruct(zLD, "Il,B"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zINC, "X"), new z80dasmStruct(zDEC, "X"), new z80dasmStruct(zLD, "X,B"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zADD, "I,sp"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "b,Ih"), new z80dasmStruct(zLD, "b,Il"), new z80dasmStruct(zLD, "b,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "c,Ih"), new z80dasmStruct(zLD, "c,Il"), new z80dasmStruct(zLD, "c,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "d,Ih"), new z80dasmStruct(zLD, "d,Il"), new z80dasmStruct(zLD, "d,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "e,Ih"), new z80dasmStruct(zLD, "e,Il"), new z80dasmStruct(zLD, "e,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "Ih,b"), new z80dasmStruct(zLD, "Ih,c"), new z80dasmStruct(zLD, "Ih,d"), new z80dasmStruct(zLD, "Ih,e"),
                    new z80dasmStruct(zLD, "Ih,Ih"), new z80dasmStruct(zLD, "Ih,Il"), new z80dasmStruct(zLD, "h,X"), new z80dasmStruct(zLD, "Ih,a"),
                    new z80dasmStruct(zLD, "Il,b"), new z80dasmStruct(zLD, "Il,c"), new z80dasmStruct(zLD, "Il,d"), new z80dasmStruct(zLD, "Il,e"),
                    new z80dasmStruct(zLD, "Il,Ih"), new z80dasmStruct(zLD, "Il,Il"), new z80dasmStruct(zLD, "l,X"), new z80dasmStruct(zLD, "Il,a"),
                    new z80dasmStruct(zLD, "X,b"), new z80dasmStruct(zLD, "X,c"), new z80dasmStruct(zLD, "X,d"), new z80dasmStruct(zLD, "X,e"),
                    new z80dasmStruct(zLD, "X,h"), new z80dasmStruct(zLD, "X,l"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zLD, "X,a"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zLD, "a,Ih"), new z80dasmStruct(zLD, "a,Il"), new z80dasmStruct(zLD, "a,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zADD, "a,Ih"), new z80dasmStruct(zADD, "a,Il"), new z80dasmStruct(zADD, "a,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zADC, "a,Ih"), new z80dasmStruct(zADC, "a,Il"), new z80dasmStruct(zADC, "a,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zSUB, "Ih"), new z80dasmStruct(zSUB, "Il"), new z80dasmStruct(zSUB, "X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zSBC, "a,Ih"), new z80dasmStruct(zSBC, "a,Il"), new z80dasmStruct(zSBC, "a,X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zAND, "Ih"), new z80dasmStruct(zAND, "Il"), new z80dasmStruct(zAND, "X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zXOR, "Ih"), new z80dasmStruct(zXOR, "Il"), new z80dasmStruct(zXOR, "X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zOR, "Ih"), new z80dasmStruct(zOR, "Il"), new z80dasmStruct(zOR, "X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zCP, "Ih"), new z80dasmStruct(zCP, "Il"), new z80dasmStruct(zCP, "X"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "cb"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zPOP, "I"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zEX, "(sp),I"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zPUSH, "I"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zJP, "(I)"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zLD, "sp,I"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"),
                    new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?"), new z80dasmStruct(zDB, "?")
            };
    static z80dasmStruct[] mnemonic_main =
            {

                    new z80dasmStruct(zNOP, null), new z80dasmStruct(zLD, "bc,N"), new z80dasmStruct(zLD, "(bc),a"), new z80dasmStruct(zINC, "bc"),
                    new z80dasmStruct(zINC, "b"), new z80dasmStruct(zDEC, "b"), new z80dasmStruct(zLD, "b,B"), new z80dasmStruct(zRLCA, null),
                    new z80dasmStruct(zEX, "af,af'"), new z80dasmStruct(zADD, "hl,bc"), new z80dasmStruct(zLD, "a,(bc)"), new z80dasmStruct(zDEC, "bc"),
                    new z80dasmStruct(zINC, "c"), new z80dasmStruct(zDEC, "c"), new z80dasmStruct(zLD, "c,B"), new z80dasmStruct(zRRCA, null),
                    new z80dasmStruct(zDJNZ, "O"), new z80dasmStruct(zLD, "de,N"), new z80dasmStruct(zLD, "(de),a"), new z80dasmStruct(zINC, "de"),
                    new z80dasmStruct(zINC, "d"), new z80dasmStruct(zDEC, "d"), new z80dasmStruct(zLD, "d,B"), new z80dasmStruct(zRLA, null),
                    new z80dasmStruct(zJR, "O"), new z80dasmStruct(zADD, "hl,de"), new z80dasmStruct(zLD, "a,(de)"), new z80dasmStruct(zDEC, "de"),
                    new z80dasmStruct(zINC, "e"), new z80dasmStruct(zDEC, "e"), new z80dasmStruct(zLD, "e,B"), new z80dasmStruct(zRRA, null),
                    new z80dasmStruct(zJR, "nz,O"), new z80dasmStruct(zLD, "hl,N"), new z80dasmStruct(zLD, "(W),hl"), new z80dasmStruct(zINC, "hl"),
                    new z80dasmStruct(zINC, "h"), new z80dasmStruct(zDEC, "h"), new z80dasmStruct(zLD, "h,B"), new z80dasmStruct(zDAA, null),
                    new z80dasmStruct(zJR, "z,O"), new z80dasmStruct(zADD, "hl,hl"), new z80dasmStruct(zLD, "hl,(W)"), new z80dasmStruct(zDEC, "hl"),
                    new z80dasmStruct(zINC, "l"), new z80dasmStruct(zDEC, "l"), new z80dasmStruct(zLD, "l,B"), new z80dasmStruct(zCPL, null),
                    new z80dasmStruct(zJR, "nc,O"), new z80dasmStruct(zLD, "sp,N"), new z80dasmStruct(zLD, "(W),a"), new z80dasmStruct(zINC, "sp"),
                    new z80dasmStruct(zINC, "(hl)"), new z80dasmStruct(zDEC, "(hl)"), new z80dasmStruct(zLD, "(hl),B"), new z80dasmStruct(zSCF, null),
                    new z80dasmStruct(zJR, "c,O"), new z80dasmStruct(zADD, "hl,sp"), new z80dasmStruct(zLD, "a,(W)"), new z80dasmStruct(zDEC, "sp"),
                    new z80dasmStruct(zINC, "a"), new z80dasmStruct(zDEC, "a"), new z80dasmStruct(zLD, "a,B"), new z80dasmStruct(zCCF, null),
                    new z80dasmStruct(zLD, "b,b"), new z80dasmStruct(zLD, "b,c"), new z80dasmStruct(zLD, "b,d"), new z80dasmStruct(zLD, "b,e"),
                    new z80dasmStruct(zLD, "b,h"), new z80dasmStruct(zLD, "b,l"), new z80dasmStruct(zLD, "b,(hl)"), new z80dasmStruct(zLD, "b,a"),
                    new z80dasmStruct(zLD, "c,b"), new z80dasmStruct(zLD, "c,c"), new z80dasmStruct(zLD, "c,d"), new z80dasmStruct(zLD, "c,e"),
                    new z80dasmStruct(zLD, "c,h"), new z80dasmStruct(zLD, "c,l"), new z80dasmStruct(zLD, "c,(hl)"), new z80dasmStruct(zLD, "c,a"),
                    new z80dasmStruct(zLD, "d,b"), new z80dasmStruct(zLD, "d,c"), new z80dasmStruct(zLD, "d,d"), new z80dasmStruct(zLD, "d,e"),
                    new z80dasmStruct(zLD, "d,h"), new z80dasmStruct(zLD, "d,l"), new z80dasmStruct(zLD, "d,(hl)"), new z80dasmStruct(zLD, "d,a"),
                    new z80dasmStruct(zLD, "e,b"), new z80dasmStruct(zLD, "e,c"), new z80dasmStruct(zLD, "e,d"), new z80dasmStruct(zLD, "e,e"),
                    new z80dasmStruct(zLD, "e,h"), new z80dasmStruct(zLD, "e,l"), new z80dasmStruct(zLD, "e,(hl)"), new z80dasmStruct(zLD, "e,a"),
                    new z80dasmStruct(zLD, "h,b"), new z80dasmStruct(zLD, "h,c"), new z80dasmStruct(zLD, "h,d"), new z80dasmStruct(zLD, "h,e"),
                    new z80dasmStruct(zLD, "h,h"), new z80dasmStruct(zLD, "h,l"), new z80dasmStruct(zLD, "h,(hl)"), new z80dasmStruct(zLD, "h,a"),
                    new z80dasmStruct(zLD, "l,b"), new z80dasmStruct(zLD, "l,c"), new z80dasmStruct(zLD, "l,d"), new z80dasmStruct(zLD, "l,e"),
                    new z80dasmStruct(zLD, "l,h"), new z80dasmStruct(zLD, "l,l"), new z80dasmStruct(zLD, "l,(hl)"), new z80dasmStruct(zLD, "l,a"),
                    new z80dasmStruct(zLD, "(hl),b"), new z80dasmStruct(zLD, "(hl),c"), new z80dasmStruct(zLD, "(hl),d"), new z80dasmStruct(zLD, "(hl),e"),
                    new z80dasmStruct(zLD, "(hl),h"), new z80dasmStruct(zLD, "(hl),l"), new z80dasmStruct(zHLT, null), new z80dasmStruct(zLD, "(hl),a"),
                    new z80dasmStruct(zLD, "a,b"), new z80dasmStruct(zLD, "a,c"), new z80dasmStruct(zLD, "a,d"), new z80dasmStruct(zLD, "a,e"),
                    new z80dasmStruct(zLD, "a,h"), new z80dasmStruct(zLD, "a,l"), new z80dasmStruct(zLD, "a,(hl)"), new z80dasmStruct(zLD, "a,a"),
                    new z80dasmStruct(zADD, "a,b"), new z80dasmStruct(zADD, "a,c"), new z80dasmStruct(zADD, "a,d"), new z80dasmStruct(zADD, "a,e"),
                    new z80dasmStruct(zADD, "a,h"), new z80dasmStruct(zADD, "a,l"), new z80dasmStruct(zADD, "a,(hl)"), new z80dasmStruct(zADD, "a,a"),
                    new z80dasmStruct(zADC, "a,b"), new z80dasmStruct(zADC, "a,c"), new z80dasmStruct(zADC, "a,d"), new z80dasmStruct(zADC, "a,e"),
                    new z80dasmStruct(zADC, "a,h"), new z80dasmStruct(zADC, "a,l"), new z80dasmStruct(zADC, "a,(hl)"), new z80dasmStruct(zADC, "a,a"),
                    new z80dasmStruct(zSUB, "b"), new z80dasmStruct(zSUB, "c"), new z80dasmStruct(zSUB, "d"), new z80dasmStruct(zSUB, "e"),
                    new z80dasmStruct(zSUB, "h"), new z80dasmStruct(zSUB, "l"), new z80dasmStruct(zSUB, "(hl)"), new z80dasmStruct(zSUB, "a"),
                    new z80dasmStruct(zSBC, "a,b"), new z80dasmStruct(zSBC, "a,c"), new z80dasmStruct(zSBC, "a,d"), new z80dasmStruct(zSBC, "a,e"),
                    new z80dasmStruct(zSBC, "a,h"), new z80dasmStruct(zSBC, "a,l"), new z80dasmStruct(zSBC, "a,(hl)"), new z80dasmStruct(zSBC, "a,a"),
                    new z80dasmStruct(zAND, "b"), new z80dasmStruct(zAND, "c"), new z80dasmStruct(zAND, "d"), new z80dasmStruct(zAND, "e"),
                    new z80dasmStruct(zAND, "h"), new z80dasmStruct(zAND, "l"), new z80dasmStruct(zAND, "(hl)"), new z80dasmStruct(zAND, "a"),
                    new z80dasmStruct(zXOR, "b"), new z80dasmStruct(zXOR, "c"), new z80dasmStruct(zXOR, "d"), new z80dasmStruct(zXOR, "e"),
                    new z80dasmStruct(zXOR, "h"), new z80dasmStruct(zXOR, "l"), new z80dasmStruct(zXOR, "(hl)"), new z80dasmStruct(zXOR, "a"),
                    new z80dasmStruct(zOR, "b"), new z80dasmStruct(zOR, "c"), new z80dasmStruct(zOR, "d"), new z80dasmStruct(zOR, "e"),
                    new z80dasmStruct(zOR, "h"), new z80dasmStruct(zOR, "l"), new z80dasmStruct(zOR, "(hl)"), new z80dasmStruct(zOR, "a"),
                    new z80dasmStruct(zCP, "b"), new z80dasmStruct(zCP, "c"), new z80dasmStruct(zCP, "d"), new z80dasmStruct(zCP, "e"),
                    new z80dasmStruct(zCP, "h"), new z80dasmStruct(zCP, "l"), new z80dasmStruct(zCP, "(hl)"), new z80dasmStruct(zCP, "a"),
                    new z80dasmStruct(zRET, "nz"), new z80dasmStruct(zPOP, "bc"), new z80dasmStruct(zJP, "nz,A"), new z80dasmStruct(zJP, "A"),
                    new z80dasmStruct(zCALL, "nz,A"), new z80dasmStruct(zPUSH, "bc"), new z80dasmStruct(zADD, "a,B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "z"), new z80dasmStruct(zRET, null), new z80dasmStruct(zJP, "z,A"), new z80dasmStruct(zDB, "cb"),
                    new z80dasmStruct(zCALL, "z,A"), new z80dasmStruct(zCALL, "A"), new z80dasmStruct(zADC, "a,B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "nc"), new z80dasmStruct(zPOP, "de"), new z80dasmStruct(zJP, "nc,A"), new z80dasmStruct(zOUT, "(P),a"),
                    new z80dasmStruct(zCALL, "nc,A"), new z80dasmStruct(zPUSH, "de"), new z80dasmStruct(zSUB, "B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "c"), new z80dasmStruct(zEXX, null), new z80dasmStruct(zJP, "c,A"), new z80dasmStruct(zIN, "a,(P)"),
                    new z80dasmStruct(zCALL, "c,A"), new z80dasmStruct(zDB, "dd"), new z80dasmStruct(zSBC, "a,B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "po"), new z80dasmStruct(zPOP, "hl"), new z80dasmStruct(zJP, "po,A"), new z80dasmStruct(zEX, "(sp),hl"),
                    new z80dasmStruct(zCALL, "po,A"), new z80dasmStruct(zPUSH, "hl"), new z80dasmStruct(zAND, "B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "pe"), new z80dasmStruct(zJP, "(hl)"), new z80dasmStruct(zJP, "pe,A"), new z80dasmStruct(zEX, "de,hl"),
                    new z80dasmStruct(zCALL, "pe,A"), new z80dasmStruct(zDB, "ed"), new z80dasmStruct(zXOR, "B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "p"), new z80dasmStruct(zPOP, "af"), new z80dasmStruct(zJP, "p,A"), new z80dasmStruct(zDI, null),
                    new z80dasmStruct(zCALL, "p,A"), new z80dasmStruct(zPUSH, "af"), new z80dasmStruct(zOR, "B"), new z80dasmStruct(zRST, "V"),
                    new z80dasmStruct(zRET, "m"), new z80dasmStruct(zLD, "sp,hl"), new z80dasmStruct(zJP, "m,A"), new z80dasmStruct(zEI, null),
                    new z80dasmStruct(zCALL, "m,A"), new z80dasmStruct(zDB, "fd"), new z80dasmStruct(zCP, "B"), new z80dasmStruct(zRST, "V")
            };

    enum e_mnemonics {
        zADC, zADD, zAND, zBIT, zCALL, zCCF, zCP, zCPD,
        zCPDR, zCPI, zCPIR, zCPL, zDAA, zDB, zDEC, zDI,
        zDJNZ, zEI, zEX, zEXX, zHLT, zIM, zIN, zINC,
        zIND, zINDR, zINI, zINIR, zJP, zJR, zLD, zLDD,
        zLDDR, zLDI, zLDIR, zNEG, zNOP, zOR, zOTDR, zOTIR,
        zOUT, zOUTD, zOUTI, zPOP, zPUSH, zRES, zRET, zRETI,
        zRETN, zRL, zRLA, zRLC, zRLCA, zRLD, zRR, zRRA,
        zRRC, zRRCA, zRRD, zRST, zSBC, zSCF, zSET, zSLA,
        zSLL, zSRA, zSRL, zSUB, zXOR
    }

    static class z80dasmStruct {
        e_mnemonics mnemonic;
        String arguments;

        public z80dasmStruct(e_mnemonics mnemonic, String arguments) {
            this.mnemonic = mnemonic;
            this.arguments = arguments;
        }
    }
}


