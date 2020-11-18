/*
 * SwingWindow
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 17/10/19 11:55
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

package com.fbdev.ui;

import com.fbdev.model.SystemProvider;
import com.fbdev.util.KeyBindingsHandler;
import com.fbdev.util.RenderingStrategy;
import com.fbdev.util.VideoMode;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static com.fbdev.util.ScreenSizeHelper.DEFAULT_H;
import static com.fbdev.util.ScreenSizeHelper.DEFAULT_W;

public class SwingWindow extends SwingWindowBase {

    private static final Logger LOG = LogManager.getLogger(SwingWindow.class.getSimpleName());

    private Dimension newDimension = new Dimension(1, 1);

    public SwingWindow(SystemProvider mainEmu) {
        super(mainEmu);
        viewportSize = new Dimension(DEFAULT_W, DEFAULT_H);
        frameBufferSize = viewportSize;
    }

    @Override
    public void init() {
        super.init();
        updateDimension(true, viewportSize.width, viewportSize.height, 0, 0);
    }

    @Override
    protected void handleSystemEvent(SystemProvider.SystemEvent event, Object par, String msg) {
        mainEmu.handleSystemEvent(event, par);
        showInfo(event + (Strings.isNullOrEmpty(msg) ? "" : ": " + msg));
    }

    @Override
    protected KeyStroke getAcceleratorKey(SystemProvider.SystemEvent event) {
        return KeyBindingsHandler.getInstance().getKeyStrokeForEvent(event);
    }

    @Override
    public void refresh() {
//        updateDimension(true, newDimension.width, newDimension.height, 0, 0);
        refreshStrategy(false);
    }

    @Override
    public void renderScreenLinear(int[] data, Optional<String> label, VideoMode videoMode) {
        if (!viewportSize.equals(videoMode.getDimension())) {
            RenderingStrategy.renderNearest(data, renderData, videoMode.getDimension(), viewportSize);
        } else {
            System.arraycopy(data, 0, renderData, 0, data.length);
        }
        this.newDimension = videoMode.getDimension();
        refresh();
        label.ifPresent(this::showLabel);
    }
}
