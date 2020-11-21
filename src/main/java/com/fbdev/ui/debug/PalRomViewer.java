/*
 * PalRomViewer
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 13:00
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

public class PalRomViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(PalRomViewer.class.getSimpleName());

    private byte[] palrom;

    public PalRomViewer(Video video) {
        super(video, getGridContext());
        this.palrom = video.getPalrom();
        initPanel();
    }

    private static BaseViewer.GridContext getGridContext() {
        BaseViewer.GridContext gc = new BaseViewer.GridContext();
        gc.entries = 256;
        gc.rows = 4;
        gc.panelWidth = 8 * (gc.entries / gc.rows) + 100;
        gc.panelHeight = gc.panelWidth / 4 + 50;
        gc.title = "Palette ROM Viewer";
        return gc;
    }

    @Override
    protected void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                int k = 0;
                Color[] colors = allColors;
                int itemsPerRow = gridContext.entries / gridContext.rows;
                for (int i = 0; i < gridContext.entries; i += 4) {
                    Color c1 = colors[palrom[i] & 0xF];
                    Color c2 = colors[palrom[i + 1] & 0xF];
                    Color c3 = colors[palrom[i + 2] & 0xF];
                    Color c4 = colors[palrom[i + 3] & 0xF];
                    panelList[k].setBackground(c1);
                    panelList[k].setForeground(c1);
                    panelList[itemsPerRow + k].setBackground(c2);
                    panelList[itemsPerRow + k].setForeground(c2);
                    panelList[2 * itemsPerRow + k].setBackground(c3);
                    panelList[2 * itemsPerRow + k].setForeground(c3);
                    panelList[3 * itemsPerRow + k].setBackground(c4);
                    panelList[3 * itemsPerRow + k].setForeground(c4);
                    k++;
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        });
    }
}
