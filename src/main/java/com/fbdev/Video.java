package com.fbdev;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.input.InputProvider;
import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.input.KeyboardInput;
import com.fbdev.helios.model.SystemProvider;
import com.fbdev.helios.util.VideoMode;
import com.fbdev.ui.SwingWindow;
import com.fbdev.util.RomHelper;

import java.awt.*;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class Video {

    private static int[] colorBitWeight = {0x21, 0x47, 0x97, 0x21, 0x47, 0x97, 0x51, 0xAE};
    private static BiFunction<Integer, Integer, Integer> cromBitDecodeFn = (d, b) -> (((d >> b) & 1) * colorBitWeight[b]);
    private static Function<Integer, Integer> toRedFn = d -> cromBitDecodeFn.apply(d, 0) + cromBitDecodeFn.apply(d, 1) +
            cromBitDecodeFn.apply(d, 2);
    private static Function<Integer, Integer> toGreenFn = d -> cromBitDecodeFn.apply(d, 3) + cromBitDecodeFn.apply(d, 4) +
            cromBitDecodeFn.apply(d, 5);
    private static Function<Integer, Integer> toBlueFn = d -> cromBitDecodeFn.apply(d, 6) + cromBitDecodeFn.apply(d, 7);

    private static final int tileW = VideoMode.H28_V32.getDimension().width / 8,
            tileH = VideoMode.H28_V32.getDimension().height / 8;
    private static final int[] tileMapper = new int[tileW * tileH];
    private byte[] crom, palrom, tileRom, spriteRom;

    private Color[] colors;
    private int[][] paletteToColorsIdx = new int[64][4];
    private int[][] tileToPaletteIdx = new int[256][64];
    int[] render = new int[tileW * 8 * tileH * 8];
    boolean frameShown = false;
    SwingWindow window;
    JoypadProvider joypadProvider;
    private int[][] spriteToPaletteIdx = new int[64][256];
    private byte[] ram;

    public Video(RomHelper r, byte[] ram, JoypadProvider joypadProvider) {
        this.crom = r.getCrom();
        this.palrom = r.getPalRom();
        this.tileRom = r.getTileRom();
        this.spriteRom = r.getSpriteRom();
        this.ram = ram;
        this.colors = new Color[crom.length];
        this.joypadProvider = joypadProvider;
        init();
    }

    public static void generateTileMapper() {
        int start = 0x3A0;
        int rowIdx = start, colIdx = start;
        for (int i = 0; i < tileMapper.length; i++) {
//            System.out.printf("%4x ", rowIdx);
            tileMapper[i] = rowIdx;
            rowIdx -= 0x20;
            if ((i + 1) % tileW == 0) {
//                System.out.println();
                colIdx++;
                rowIdx = colIdx;
            }
        }

    }

    private void init() {
        showFrame();
        generateTileMapper();
        for (int i = 0; i < crom.length; i++) {
            int data = crom[i];
            colors[i] = new Color(toRedFn.apply(data), toGreenFn.apply(data), toBlueFn.apply(data));
        }
        int k = 0;
        for (int i = 0; i < palrom.length / 4; i++) {
            paletteToColorsIdx[i][0] = palrom[k] & 0xF;
            paletteToColorsIdx[i][1] = palrom[k + 1] & 0xF;
            paletteToColorsIdx[i][2] = palrom[k + 2] & 0xF;
            paletteToColorsIdx[i][3] = palrom[k + 3] & 0xF;
            k += 4;
        }
        int[] num = {63, 31};
        for (int i = 0; i < tileRom.length; i++) {
            int quadrant = (i % 16) / 8; //an 8x4 area, each tile has 2
            int numTile = i / 16;
            int pos = num[quadrant] - (i % 8);
            int val = tileRom[i];
            int p1TileIdx = ((val >> 3) & 2) | (val & 1);
            int p2TileIdx = ((val >> 4) & 2) | ((val >> 1) & 1);
            int p3TileIdx = ((val >> 5) & 2) | ((val >> 2) & 1);
            int p4TileIdx = ((val >> 6) & 2) | ((val >> 3) & 1);
            tileToPaletteIdx[numTile][pos] = p1TileIdx;
            tileToPaletteIdx[numTile][pos - 8] = p2TileIdx;
            tileToPaletteIdx[numTile][pos - 16] = p3TileIdx;
            tileToPaletteIdx[numTile][pos - 24] = p4TileIdx;
        }
        int[] numq = {255, 63, 127, 191, 247, 55, 119, 183};
        for (int i = 0; i < spriteRom.length; i++) {
            int numSprite = i / 64;
            int quadrant = (i % 64) / 8; //an 8x4 area, each sprite has 8
            int pos = numq[quadrant] - (i % 8);
            int yShift = 16;
            int val = spriteRom[i];
            int p1TileIdx = ((val >> 3) & 2) | (val & 1);
            int p2TileIdx = ((val >> 4) & 2) | ((val >> 1) & 1);
            int p3TileIdx = ((val >> 5) & 2) | ((val >> 2) & 1);
            int p4TileIdx = ((val >> 6) & 2) | ((val >> 3) & 1);
            spriteToPaletteIdx[numSprite][pos] = p1TileIdx;
            spriteToPaletteIdx[numSprite][pos - yShift] = p2TileIdx;
            spriteToPaletteIdx[numSprite][pos - 2 * yShift] = p3TileIdx;
            spriteToPaletteIdx[numSprite][pos - 3 * yShift] = p4TileIdx;
        }
    }

    public void composeImage() {
        int startAddrTile = 0;
        int tilePixels = 64;
        int[] rgbPixels = new int[tilePixels];
        int tilesToProcess = tileW * tileH;
        int startAddrPx = 0;
        int tileLineStartPx = 0;
        for (int i = 0; i < tilesToProcess; i++) {
            int tileLoc = tileMapper[i];
            int tileIdx = ram[tileLoc] & 0xFF;
            int[] paletteIndexes = tileToPaletteIdx[tileIdx]; //64 pixel, a palette index for each
            int paletteIdx = ram[tileLoc + SystemBus.PALETTE_RAM_OFFSET] & 0x3F; // 64 palettes
            int[] paletteCromIdx = paletteToColorsIdx[paletteIdx];
            for (int j = 0; j < rgbPixels.length; j++) {
                rgbPixels[j] = colors[paletteCromIdx[paletteIndexes[j]]].getRGB();
            }
            int startIdx = tileLineStartPx + startAddrPx;
            for (int j = 0; j < rgbPixels.length; j += 8) {
                System.arraycopy(rgbPixels, j, render, startIdx, 8);
                startIdx += tileW * 8;
            }
//            System.out.println("Tile" + i + ", startPx: "+ (tileLineStartPx + startAddrPx));
            if ((i + 1) % tileW == 0) {
                tileLineStartPx += 8 * 8 * tileW; //8 lines
                startAddrPx = 0;
            } else {
                startAddrPx += 8;
            }
        }
        window.renderScreenLinear(render, Optional.empty(), VideoMode.H28_V32);
    }

    private void showFrame() {
        window = new SwingWindow(SystemProvider.systemProvider);
        window.init();
        window.setupFrameKeyListener();
        window.addKeyListener(KeyboardInput.createKeyAdapter(joypadProvider));
        window.reloadControllers(InputProvider.DEFAULT_CONTROLLERS);
    }

    public Color[] getColors() {
        return colors;
    }

    public int[][] getPaletteToColorsIdx() {
        return paletteToColorsIdx;
    }

    public int[][] getTileToPaletteIdx() {
        return tileToPaletteIdx;
    }

    public int[][] getSpriteToPaletteIdx() {
        return spriteToPaletteIdx;
    }

    public byte[] getPalrom() {
        return palrom;
    }

    public byte[] getTileRom() {
        return tileRom;
    }
}
