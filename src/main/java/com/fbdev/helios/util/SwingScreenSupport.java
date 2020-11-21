/*
 * SwingScreenSupport
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 21/11/2020, 11:38
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

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SwingScreenSupport
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class SwingScreenSupport {

    public static final int DEFAULT_SCREEN = 1;

    private static final Logger LOG = LogManager.getLogger(SwingScreenSupport.class.getSimpleName());

    private static GraphicsDevice[] graphicsDevices =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

    private static int currentScreen = graphicsDevices.length > 1 ? DEFAULT_SCREEN : 0;

    public static GraphicsDevice setupScreens() {
        LOG.info("Screen detected: #{}", graphicsDevices.length);
        GraphicsDevice gd = graphicsDevices[currentScreen];
        LOG.info("Initial screen: {}", gd.getIDstring());
        return gd;
    }

    public static List<String> detectScreens() {
        return Arrays.stream(graphicsDevices).map(GraphicsDevice::toString).collect(Collectors.toList());
    }

    public static int getCurrentScreen() {
        return currentScreen;
    }

    public static GraphicsDevice getGraphicsDevice() {
        return graphicsDevices[currentScreen];
    }

    public static int detectUserScreenChange(GraphicsDevice currentDevice) {
        GraphicsDevice prevDevice = graphicsDevices[currentScreen];
        if (!currentDevice.equals(prevDevice)) {
            for (int i = 0; i < graphicsDevices.length; i++) {
                if (graphicsDevices[i].equals(currentDevice)) {
                    currentScreen = i;
                    return i;
                }
            }
        }
        return currentScreen;
    }

    public static void showOnScreen(int screen, JFrame frame) {
        GraphicsDevice[] gd = graphicsDevices;
        int width = 0, height = 0;
        if (screen > -1 && screen < gd.length) {
            Rectangle bounds = gd[screen].getDefaultConfiguration().getBounds();
            width = bounds.width;
            height = bounds.height;
            frame.setLocation(
                    ((width / 2) - (frame.getSize().width / 2)) + bounds.x,
                    ((height / 2) - (frame.getSize().height / 2)) + bounds.y
            );
            frame.setVisible(true);
            LOG.info("Showing on screen: {}", screen);
            currentScreen = screen;
        } else {
            LOG.error("Unable to set screen: {}", screen);
        }
    }
}
