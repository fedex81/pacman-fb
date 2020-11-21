/*
 * TileRomViewer
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 17/11/2020, 17:25
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

public class TileRomViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(TileRomViewer.class.getSimpleName());

    private static int SCALE = 2;

    private byte[] tilerom;
    private Color[] palColors;

    public TileRomViewer(Video video) {
        super(video, getGridContext());
        this.tilerom = video.getTileRom();
        this.palColors = new Color[4];
        int[] idx = video.getPaletteToColorsIdx()[1];
        for (int i = 0; i < idx.length; i++) {
            palColors[i] = allColors[idx[i]];
        }
        initPanel();
    }

    private static GridContext getGridContext() {
        GridContext gc = new GridContext();
        gc.entries = 256;
        gc.rows = 8;
        gc.panelWidth = 8 * SCALE * (gc.entries / gc.rows) + 100;
        gc.panelHeight = gc.panelWidth / 2 + 50;
        gc.title = "Tile ROM Viewer (palette #1)";
        return gc;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                int[][] tileToPaletteIdx = video.getTileToPaletteIdx();
                for (int k = 0; k < gridContext.entries; k++) {
                    int[] pixels = tileToPaletteIdx[k]; //64 pixels per tile
                    BufferedImage bi = createImage(new Dimension(8, 8));
                    ImageIcon image = new ImageIcon(bi);
                    for (int i = 0; i < pixels.length; i++) {
                        bi.setRGB(i % 8, i / 8, palColors[pixels[i]].getRGB());
                    }
                    panelList[k].add(getLabelScaled(bi, 2));
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        });
    }
}
