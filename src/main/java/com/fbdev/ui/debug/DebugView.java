/*
 * DebugView
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 10:59
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

import com.fbdev.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class DebugView {

    private static final Logger LOG = LogManager.getLogger(DebugView.class.getSimpleName());

    private static boolean DEBUG_VIEWER_ENABLED;

    static {
        DEBUG_VIEWER_ENABLED =
                Boolean.parseBoolean(System.getProperty("show.vdp.debug.viewer", "false"));
        if (DEBUG_VIEWER_ENABLED) {
            LOG.info("Debug viewer enabled");
        }
    }

    private JFrame frame;
    private JPanel panel;
    private Video video;

    public DebugView(Video video) {
        this.video = video;
        init();
    }

    private void init() {
        CromViewer c = new CromViewer(video);
        PalRomViewer p = new PalRomViewer(video);
        TileRomViewer t = new TileRomViewer(video);
        SpriteRomViewer s = new SpriteRomViewer(video);
        c.update();
        p.update();
        t.update();
        s.update();

        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame();
            this.panel = new JPanel();
            this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.GRAY);
            JPanel cromPanel = c.getPanel();
            JPanel palPanel = p.getPanel();
            JPanel tilePanel = t.getPanel();
            JPanel spritePanel = s.getPanel();
            this.panel.add(new JLabel(c.getTitle()));
            this.panel.add(cromPanel);
            this.panel.add(new JLabel(p.getTitle()));
            this.panel.add(palPanel);
            this.panel.add(new JLabel(t.getTitle()));
            this.panel.add(tilePanel);
            this.panel.add(new JLabel(s.getTitle()));
            this.panel.add(spritePanel);
            int w = palPanel.getWidth();
            int h = palPanel.getHeight() + cromPanel.getHeight() + tilePanel.getHeight() + spritePanel.getHeight();
            this.panel.setSize(new Dimension(w, h));
            frame.add(panel);
            frame.setMinimumSize(panel.getSize());
            frame.setTitle("Debug Viewer");
            frame.pack();
            frame.setVisible(true);
        });
    }
}
