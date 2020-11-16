package com.fbdev;

import com.fbdev.util.RomHelper;

import java.awt.*;
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
    private byte[] crom, palrom, tileRom, spriteRom;

    private Color[] colors;
    private int[][] paletteToColorsIdx = new int[64][4];
    private int[][] tileToPaletteIdx = new int[256][64];

    public Video(RomHelper r) {
        this.crom = r.getCrom();
        this.palrom = r.getPalRom();
        this.tileRom = r.getTileRom();
        this.spriteRom = r.getSpriteRom();
        this.colors = new Color[crom.length];
        init();
//        new CromViewer(this).showFrame();
//        new PalRomViewer(this).showFrame();
//        new TileRomViewer(this).showFrame();
//        new SpriteRomViewer(this).showFrame();
    }

    private void init() {
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
        int num1 = 63, num2 = 31;
        for (int i = 0; i < tileRom.length; i++) {
            boolean bottomHalfTile = i % 16 < 8;
            int numTile = i / 16;
            int pos = (bottomHalfTile ? num1 : num2) - (i % 8);
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

    public byte[] getPalrom() {
        return palrom;
    }

    public byte[] getTileRom() {
        return tileRom;
    }
}
