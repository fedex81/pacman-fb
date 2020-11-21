/*
 * SpriteRomViewer
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 13:13
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

package com.fbdev.ui.debug;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */

import com.fbdev.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SpriteRomViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(SpriteRomViewer.class.getSimpleName());

    private static int SCALE = 2;
    private static int IMG_W = 16, IMG_H = 16;
    private byte[] spriterom;
    private Color[] palColors;

    public SpriteRomViewer(Video video) {
        super(video, getGridContext());
        this.spriterom = video.getTileRom();
        this.palColors = new Color[4];
        int[] idx = video.getPaletteToColorsIdx()[1];
        for (int i = 0; i < idx.length; i++) {
            palColors[i] = allColors[idx[i]];
        }
        initPanel();
    }

    private static GridContext getGridContext() {
        GridContext gc = new GridContext();
        gc.entries = 64;
        gc.rows = 4;
        gc.panelWidth = IMG_W * SCALE * (gc.entries / gc.rows) + 100;
        gc.panelHeight = gc.panelWidth / 2 + 50;
        gc.title = "Sprite ROM Viewer (palette #1)";
        return gc;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                int[][] spriteToPaletteIdx = video.getSpriteToPaletteIdx();
                for (int k = 0; k < gridContext.entries; k++) {
                    int[] pixels = spriteToPaletteIdx[k]; //256 pixels per sprite
                    BufferedImage bi = createImage(new Dimension(IMG_W, IMG_H));
                    for (int i = 0; i < pixels.length; i++) {
                        bi.setRGB(i % IMG_W, i / IMG_H, palColors[pixels[i]].getRGB());
                    }
                    panelList[k].add(getLabelScaled(bi, SCALE));
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        });
    }
}
