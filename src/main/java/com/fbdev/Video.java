package com.fbdev;

import com.fbdev.bus.SystemBus;
import com.fbdev.helios.BaseVdpProvider;
import com.fbdev.helios.input.JoypadProvider;
import com.fbdev.helios.util.VideoMode;
import com.fbdev.util.RomHelper;
import com.fbdev.util.VideoUtil;

import java.awt.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class Video implements BaseVdpProvider {

    private static final int tileW = VideoMode.H28_V36.getTileW(),
            tileH = VideoMode.H28_V36.getTileH();
    private static final int[] tileMapper = new int[tileW * tileH];
    private final byte[] crom, palrom, tileRom, spriteRom;
    private final byte[] ram;
    private final Color[] colors;
    private final int[][] paletteToColorsIdx = new int[64][4];
    private final int[][] tileToPaletteIdx = new int[256][64];
    private final int[][] spriteToPaletteIdx = new int[64][256];

    int[] render = new int[tileW * 8 * tileH * 8];

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
    public void init() {
        VideoUtil.generateTileMapper(tileMapper);
        VideoUtil.generateColors(crom, colors);
        VideoUtil.generatePaletteToCromIdx(palrom, paletteToColorsIdx);
        VideoUtil.generateTileToPaletteIdx(tileRom, tileToPaletteIdx);
        VideoUtil.generateSpriteToPaletteIdx(spriteRom, spriteToPaletteIdx);
    }

    @Override
    public VideoMode getVideoMode() {
        return VideoMode.H28_V36;
    }

    @Override
    public void renderScreenDataLinear(int[] render) {
        composeImage(render);
    }

    private void composeImage(int[] render) {
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
