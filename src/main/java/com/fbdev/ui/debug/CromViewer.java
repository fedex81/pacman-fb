/*
 * CromViewer
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 12:56
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

public class CromViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(CromViewer.class.getSimpleName());

    public CromViewer(Video video) {
        super(video, getGridContext());
        initPanel();
    }

    private static GridContext getGridContext() {
        GridContext gc = new GridContext();
        gc.entries = 32;
        gc.rows = 1;
        gc.panelWidth = 8 * (gc.entries / gc.rows) + 100;
        gc.panelHeight = gc.panelWidth / 4 + 50;
        gc.title = "CROM Viewer";
        return gc;
    }

    @Override
    protected void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                int k = 0;
                for (int i = 0; i < gridContext.entries; i++) {
                    Color c = allColors[i];
                    JPanel label = panelList[k];
                    label.setBackground(c);
                    label.setForeground(c);
                    k++;
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        });
    }
}
