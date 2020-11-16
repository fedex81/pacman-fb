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

public class TileRomViewer extends BaseViewer {

    private static final Logger LOG = LogManager.getLogger(TileRomViewer.class.getSimpleName());

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
    }

    private static GridContext getGridContext() {
        GridContext gc = new GridContext();
        gc.entries = 256;
        gc.rows = 16;
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
                    panelList[k].add(new JLabel(image));
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        });
    }
}
