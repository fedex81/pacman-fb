package com.fbdev;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.BaseVdpProvider;
import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.util.VideoMode;
import com.fbdev.util.RomHelper;
import com.fbdev.util.VideoUtil;

import java.awt.*;
import java.util.EnumMap;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class Video implements BaseVdpProvider {


    public final static int NUM_SPRITES_ROM = 64;
    public final static int NUM_SPRITES_SCREEN = 8;
    public final static int SPRITE_W_PX = 16, SPRITE_H_PX = 16;
    public final static int SPRITE_PX = SPRITE_W_PX * SPRITE_H_PX;

    private static final int tileW = VideoMode.H28_V36.getTileW(),
            tileH = VideoMode.H28_V36.getTileH();
    private static final int[] tileMapper = new int[tileW * tileH];
    private final byte[] crom, palrom, tileRom, spriteRom;
    private final byte[] ram;
    private final Color[] colors;
    private final int[][] paletteToColorsIdx = new int[64][4];
    private final int[][] tileToPaletteIdx = new int[256][64];

    private SpriteContext[] spriteContexts = new SpriteContext[NUM_SPRITES_SCREEN];

    private EnumMap<FlipMode, int[][]> spriteFlipMap;

    @Override
    public void init() {
        VideoUtil.generateTileMapper(tileMapper);
        VideoUtil.generateColors(crom, colors);
        VideoUtil.generatePaletteToCromIdx(palrom, paletteToColorsIdx);
        VideoUtil.generateTileToPaletteIdx(tileRom, tileToPaletteIdx);
        spriteFlipMap = VideoUtil.generateSpriteToPaletteIdxMap(spriteRom);
        for (int i = 0; i < spriteContexts.length; i++) {
            spriteContexts[i] = new SpriteContext();
        }
    }

    int[] render = new int[tileW * 8 * tileH * 8];

    private void renderSprites(int[] render) {
        int linePx = getVideoMode().getPixelW();
        int lines = getVideoMode().getPixelH();
        int spritePixels = 256, spritePixelsW = 16;
        for (int i = 0; i < spriteContexts.length; i++) {
            SpriteContext sc = spriteContexts[i];
            if (sc.xpos < 16 || sc.xpos > 239 || sc.ypos < 16 || sc.ypos > 255) {
                continue; //TODO refine
            }
            int h28x_br = sc.xpos - 16;
            int h28x_tl = linePx - 1 - h28x_br;
            int v36_tl = lines - 16 - sc.ypos;
            int screenPos = (v36_tl * linePx) + h28x_tl;

            FlipMode flipMode = FlipMode.values[(sc.flipy << 1) | sc.flipx];
            int[] paletteIndexes = spriteFlipMap.get(flipMode)[sc.number];
            int[] paletteCromIdx = paletteToColorsIdx[sc.palette];
            int startIdx = screenPos, spriteLinePx = 0;
            final int blackRgb = 0; //no alpha

            for (int j = 0; j < spritePixels; j++) {
                int rgbPixel = colors[paletteCromIdx[paletteIndexes[j]]].getRGB();
                if (rgbPixel != blackRgb) { //skip transparent px
                    render[startIdx + spriteLinePx] = rgbPixel;
                }
                spriteLinePx++;
                if ((j + 1) % spritePixelsW == 0) {
                    startIdx += linePx;
                    spriteLinePx = 0;
                }
            }
        }
    }

    public Video(RomHelper r, byte[] ram, JoypadProvider joypadProvider) {
        this.crom = r.getCrom();
        this.palrom = r.getPalRom();
        this.tileRom = r.getTileRom();
        this.spriteRom = r.getSpriteRom();
        this.ram = ram;
        this.colors = new Color[crom.length];
        init();
    }

    @Override
    public void renderScreenDataLinear(int[] render) {
        renderTiles(render);
        renderSprites(render);
    }

    @Override
    public VideoMode getVideoMode() {
        return VideoMode.H28_V36;
    }

    @Override
    public void updateSpriteContext(int address, int value) {
        int val = value & 0xFF;
        SpriteContext sc = spriteContexts[(address & 0xF) >> 1];
        if ((address & 0x4FF0) == 0x4FF0) {
            if (address % 2 == 0) {
                sc.number = val >> 2;
                sc.flipy = val & 1;
                sc.flipx = (val >> 1) & 1;
            } else {
                sc.palette = val;
            }
        } else {
            if (address % 2 == 0) {
                sc.xpos = val;
            } else {
                sc.ypos = val;
            }
        }
    }

    public int[][] getSpriteToPaletteIdx() {
        return spriteFlipMap.get(FlipMode.NO_FLIP);
    }

    private void renderTiles(int[] render) {
        int startAddrTile = 0;
        int tilePixels = 64;
        int[] rgbPixels = new int[tilePixels];
        int tilesToProcess = tileW * tileH;
        int lineAddrPx = 0;
        int tileLineStartPx = 0;
        for (int i = 0; i < tilesToProcess; i++) {
            int tileLoc = tileMapper[i];
            if (tileLoc < 0) {
                continue;
            }
            int tileIdx = ram[tileLoc] & 0xFF;
            int[] paletteIndexes = tileToPaletteIdx[tileIdx]; //64 pixel, a palette index for each
            int paletteIdx = ram[tileLoc + SystemBus.PALETTE_RAM_OFFSET] & 0x3F; // 64 palettes
            int[] paletteCromIdx = paletteToColorsIdx[paletteIdx];
            for (int j = 0; j < rgbPixels.length; j++) {
                rgbPixels[j] = colors[paletteCromIdx[paletteIndexes[j]]].getRGB();
            }
            int startIdx = tileLineStartPx + lineAddrPx;
            for (int j = 0; j < rgbPixels.length; j += 8) {
                System.arraycopy(rgbPixels, j, render, startIdx, 8);
                startIdx += tileW * 8;
            }
//            System.out.println("Tile" + i + ", startPx: "+ (tileLineStartPx + lineAddrPx));
            if ((i + 1) % tileW == 0) {
                tileLineStartPx += 8 * 8 * tileW; //skip 8 lines
                lineAddrPx = 0;
            } else {
                lineAddrPx += 8;
            }
        }
    }

    static class SpriteContext {
        int number, flipx, flipy, xpos, ypos, palette;
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

    public enum FlipMode {
        NO_FLIP,
        FLIP_X,
        FLIP_Y,
        FLIP_XY;

        final static FlipMode[] values = FlipMode.values();
    }

    public byte[] getPalrom() {
        return palrom;
    }

    public byte[] getTileRom() {
        return tileRom;
    }
}
