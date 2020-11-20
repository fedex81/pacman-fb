package com.fbdev.util;

import com.fbdev.Video.*;
import com.fbdev.helios.util.VideoMode;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.fbdev.Video.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class VideoUtil {

    private static final int tileW = VideoMode.H28_V36.getTileW(),
            tileH = VideoMode.H28_V36.getTileH();

    private static int[] colorBitWeight = {0x21, 0x47, 0x97, 0x21, 0x47, 0x97, 0x51, 0xAE};
    private static BiFunction<Integer, Integer, Integer> cromBitDecodeFn = (d, b) -> (((d >> b) & 1) * colorBitWeight[b]);
    private static Function<Integer, Integer> toRedFn = d -> cromBitDecodeFn.apply(d, 0) + cromBitDecodeFn.apply(d, 1) +
            cromBitDecodeFn.apply(d, 2);
    private static Function<Integer, Integer> toGreenFn = d -> cromBitDecodeFn.apply(d, 3) + cromBitDecodeFn.apply(d, 4) +
            cromBitDecodeFn.apply(d, 5);
    private static Function<Integer, Integer> toBlueFn = d -> cromBitDecodeFn.apply(d, 6) + cromBitDecodeFn.apply(d, 7);

    //well this is ugly
    public static void generateTileMapper(int[] tileMapper) {
        Arrays.fill(tileMapper, -1);
        //top tiles 2*36, skip 8
        int start = 0x3DD;
        int rowIdx = start, colIdx;
        int idx = 2;
        for (; idx < tileW * 2 + 2; idx++) {
//            System.out.printf("%4x ", rowIdx);
            tileMapper[idx] = rowIdx;
            rowIdx--;
            if (idx == tileW + 2 - 1) {
//                System.out.println();
                rowIdx = 0x3FD;
            }
        }
//        System.out.println();
        //center tiles 28*32
        start = 0x3A0;
        idx -= 2;
        rowIdx = colIdx = start;
        int len = idx + tileW * 32;
        for (; idx < len; idx++) {
//            System.out.printf("%4x ", rowIdx);
            tileMapper[idx] = rowIdx;
            rowIdx -= 0x20;
            if ((idx + 1) % tileW == 0) {
//                System.out.println();
                colIdx++;
                rowIdx = colIdx;
            }
        }
//        System.out.println();
        //bottom tiles 2*36, skip 8
        start = 0x01D;
        rowIdx = start;
//        idx += 4;
        len = idx + tileW * 2;
        for (; idx < len; idx++) {
//            System.out.printf("%4x ", rowIdx);
            tileMapper[idx] = rowIdx;
            rowIdx--;
            if ((idx + 1) % tileW == 0) {
//                System.out.println();
                rowIdx = 0x03D;
            }
        }
//        System.out.println();
    }

    public static void generateColors(byte[] crom, Color[] colors) {
        for (int i = 0; i < crom.length; i++) {
            int data = crom[i];
            colors[i] = new Color(toRedFn.apply(data), toGreenFn.apply(data), toBlueFn.apply(data), 0);
        }
    }

    public static void generatePaletteToCromIdx(byte[] palrom, int[][] paletteToColorsIdx) {
        int k = 0;
        for (int i = 0; i < palrom.length / 4; i++) {
            paletteToColorsIdx[i][0] = palrom[k] & 0xF;
            paletteToColorsIdx[i][1] = palrom[k + 1] & 0xF;
            paletteToColorsIdx[i][2] = palrom[k + 2] & 0xF;
            paletteToColorsIdx[i][3] = palrom[k + 3] & 0xF;
            k += 4;
        }
    }

    public static void generateTileToPaletteIdx(byte[] tileRom, int[][] tileToPaletteIdx) {
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
    }


    public static EnumMap<FlipMode, int[][]> generateSpriteToPaletteIdxMap(byte[] spriteRom) {
        EnumMap<FlipMode, int[][]> spriteFlipMap =
                new EnumMap<FlipMode, int[][]>(FlipMode.class);
        int[][] spriteToPaletteIdx = new int[NUM_SPRITES_ROM][SPRITE_PX];
        int[][] spriteToPaletteFlipXIdx = new int[NUM_SPRITES_ROM][SPRITE_PX];
        int[][] spriteToPaletteFlipYIdx = new int[NUM_SPRITES_ROM][SPRITE_PX];
        int[][] spriteToPaletteFlipXYIdx = new int[NUM_SPRITES_ROM][SPRITE_PX];
        VideoUtil.generateSpriteToPaletteNoFlip(spriteRom, spriteToPaletteIdx);
        VideoUtil.generateSpriteToPaletteFlipX(spriteToPaletteIdx, spriteToPaletteFlipXIdx);
        VideoUtil.generateSpriteToPaletteFlipY(spriteToPaletteIdx, spriteToPaletteFlipXIdx);
        VideoUtil.generateSpriteToPaletteFlipXY(spriteToPaletteIdx, spriteToPaletteFlipXIdx);
        spriteFlipMap.put(FlipMode.NO_FLIP, spriteToPaletteIdx);
        spriteFlipMap.put(FlipMode.FLIP_X, spriteToPaletteFlipXIdx);
        spriteFlipMap.put(FlipMode.FLIP_Y, spriteToPaletteFlipXIdx);
        spriteFlipMap.put(FlipMode.FLIP_XY, spriteToPaletteFlipXYIdx);
        return spriteFlipMap;
    }

    public static void generateSpriteToPaletteNoFlip(byte[] spriteRom, int[][] spriteToPaletteIdx) {
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

    public static void generateSpriteToPaletteFlipX(int[][] spriteToPaletteIdx, int[][] spriteToPaletteFlipXIdx) {
        for (int i = 0; i < spriteToPaletteIdx.length; i++) {
            int lineIdx = 0;
            int line = 0;
            for (int j = 0; j < spriteToPaletteIdx[i].length; j++) {
                int idx = (SPRITE_W_PX * (line + 1) - 1) - lineIdx;  //15 -> 0, 14 -> 1 etc
                spriteToPaletteFlipXIdx[i][j] = spriteToPaletteIdx[i][idx];
                lineIdx = (lineIdx + 1) % SPRITE_W_PX;
                if (lineIdx == 0) {
                    line++;
                }
            }
        }
    }

    public static void generateSpriteToPaletteFlipY(int[][] spriteToPaletteIdx, int[][] spriteToPaletteFlipYIdx) {
        for (int i = 0; i < NUM_SPRITES_ROM; i++) {
            int idx = 240;
            int delta = SPRITE_W_PX;
            int lineIdx = 0;
            for (int j = 0; j < SPRITE_PX; j++) {
                int val = idx + lineIdx++;
                spriteToPaletteFlipYIdx[i][j] = spriteToPaletteIdx[i][val];
//                System.out.printf("%3d,%3d   ",j, val);
                if ((j + 1) % SPRITE_H_PX == 0) {
                    idx = idx - delta;
                    lineIdx = 0;
                }
            }
        }
    }

    public static void generateSpriteToPaletteFlipXY(int[][] spriteToPaletteIdx, int[][] spriteToPaletteFlipXYIdx) {
        int[][] temp = new int[NUM_SPRITES_ROM][SPRITE_PX];
        generateSpriteToPaletteFlipX(spriteToPaletteIdx, temp);
        generateSpriteToPaletteFlipY(temp, spriteToPaletteFlipXYIdx);
    }
}
