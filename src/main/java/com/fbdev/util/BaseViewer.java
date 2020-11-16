package com.fbdev.util;

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

    private static final int LABEL_HEIGHT = 200;
    private static final int LABEL_WIDTH = 1;
    private static final int FRAME_HEIGHT = LABEL_HEIGHT + 50;
    private final int entries, rows, frameWidth;
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
        this.entries = gridContext.entries;
        this.rows = gridContext.rows;
        this.frameWidth = LABEL_WIDTH * entries + 50;
        this.panelList = new JPanel[entries];
        this.gridContext = gridContext;
        this.allColors = video.getColors();
    }

    public void showFrame() {
        initFrame();
        update();
    }

    private void initPanel() {
        SwingUtilities.invokeLater(() -> {
            int labelPerLine = entries / rows;
            this.panel = new JPanel(new GridLayout(rows + 1, labelPerLine));
            panel.setBackground(Color.GRAY);
            panel.setSize(frameWidth - 25, FRAME_HEIGHT - 25);
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
            for (int i = 0; i < entries; i++) {
                if (k % labelPerLine == 0) {
                    JLabel label = new JLabel(Integer.toHexString(rowCnt));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                    panel.add(label);
                    rowCnt++;
                }
                JPanel cpanel = new JPanel();
                cpanel.setBackground(Color.BLACK);
                cpanel.setForeground(Color.BLACK);
                cpanel.setName("Panel" + k);
                cpanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cpanel.setMaximumSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
                panelList[k] = cpanel;
                panel.add(cpanel);
                k++;
            }
        });
    }

    private void initFrame() {
        initPanel();
        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame();
            frame.add(panel);
            frame.setMinimumSize(new Dimension(frameWidth, FRAME_HEIGHT));
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

    public JPanel getPanel() {
        return panel;
    }

    static class GridContext {
        String title;
        int entries;
        int rows;
    }
}
