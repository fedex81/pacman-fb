/*
 * BaseViewer
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 20/11/2020, 13:39
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

public abstract class BaseViewer {

    private static final Logger LOG = LogManager.getLogger(BaseViewer.class.getSimpleName());

    protected Video video;
    protected JPanel[] panelList;
    protected GridContext gridContext;
    protected Color[] allColors;
    private JPanel panel;
    private JFrame frame;

    public BaseViewer(Video video, GridContext gridContext) {
        this.frame = new JFrame();
        this.panel = new JPanel();
        this.video = video;
        this.panelList = new JPanel[gridContext.entries];
        this.gridContext = gridContext;
        addAlphaToColors();
    }

    private void addAlphaToColors() {
        Color[] cs = video.getColors();
        allColors = new Color[cs.length];
        for (int i = 0; i < allColors.length; i++) {
            allColors[i] = new Color(cs[i].getRGB()); //add alpha component
        }
    }

    public void showFrame() {
        initFrame();
        update();
    }

    protected void initPanel() {
        int labelPerLine = gridContext.entries / gridContext.rows;
        this.panel = new JPanel(new GridLayout(gridContext.rows + 1, labelPerLine));
        panel.setBackground(Color.GRAY);
        panel.setSize(gridContext.panelWidth - 25, gridContext.panelHeight - 25);
        panel.add(new JLabel());
        for (int i = 0; i < labelPerLine; i++) {
            JLabel label = new JLabel(Integer.toHexString(i));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            panel.add(label);
        }
        int k = 0;
        int rowCnt = 0;
        for (int i = 0; i < gridContext.entries; i++) {
            if (k % labelPerLine == 0) {
                JLabel label = new JLabel(Integer.toHexString(rowCnt));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
                panel.add(label);
                rowCnt++;
            }
            JPanel cpanel = new JPanel();
            cpanel.setBackground(Color.GRAY);
            cpanel.setForeground(Color.GRAY);
            cpanel.setName("Panel" + k);
            cpanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panelList[k] = cpanel;
            panel.add(cpanel);
            k++;
        }
    }

    private void initFrame() {
        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame();
            frame.add(panel);
            frame.setMinimumSize(new Dimension(gridContext.panelWidth, gridContext.panelHeight));
            frame.setTitle(gridContext.title);
            frame.pack();
            frame.setVisible(true);
        });
    }

    protected abstract void update();

    protected BufferedImage createImage(Dimension d) {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage(d.width, d.height);
        if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
            //mmh we need INT_RGB here
            bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        }
        return bi;
    }

    protected JLabel getLabelScaled(BufferedImage bi, int scale) {
        Image scaled = bi.getScaledInstance(bi.getWidth() * scale,
                bi.getHeight() * scale, Image.SCALE_FAST);
        ImageIcon image = new ImageIcon(scaled);
        return new JLabel(image);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getTitle() {
        return gridContext.title;
    }

    static class GridContext {
        String title;
        int entries;
        int rows;
        int panelWidth;
        int panelHeight;
    }
}
