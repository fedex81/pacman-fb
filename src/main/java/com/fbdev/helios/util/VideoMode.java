/*
 * VideoMode
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 07/04/19 16:01
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

package com.fbdev.helios.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public enum VideoMode {
    H28_V36(28, 36);

    private static Logger LOG = LogManager.getLogger(VideoMode.class.getSimpleName());

    private static Set<VideoMode> values = EnumSet.allOf(VideoMode.class);

    private int pixelH, pixelW, tileH, tileW;
    private Dimension dimension;

    VideoMode(int tileW, int tileH) {
        this.tileW = tileW;
        this.tileH = tileH;
        this.pixelW = tileW * 8;
        this.pixelH = tileH * 8;
        this.dimension = new Dimension(pixelW, pixelH);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public int getPixelW() {
        return pixelW;
    }

    public int getPixelH() {
        return pixelH;
    }

    public int getTileH() {
        return tileH;
    }

    public int getTileW() {
        return tileW;
    }
}
