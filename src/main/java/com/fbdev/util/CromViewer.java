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

public class CromViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(CromViewer.class.getSimpleName());
    private Color[] colors;

    public CromViewer(Video video) {
        super(video, getGridContext());
        this.colors = video.getColors();
    }

    private static GridContext getGridContext() {
        GridContext gc = new GridContext();
        gc.entries = 32;
        gc.rows = 2;
        gc.title = "CROM Viewer";
        return gc;
    }

    @Override
    protected void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                int k = 0;
                for (int i = 0; i < gridContext.entries; i++) {
                    Color c = colors[i];
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
