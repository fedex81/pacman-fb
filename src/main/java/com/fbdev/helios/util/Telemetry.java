/*
 * Telemetry
 * Copyright (c) 2020-2020 Federico Berti
 * Last modified: 18/11/2020, 19:01
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

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Telemetry
 * <p>
 * gnuplot> load 'tel.p'
 *
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class Telemetry {
    public static final boolean enable = false;
    private final static Logger LOG = LogManager.getLogger(Telemetry.class.getSimpleName());
    private static final Function<Map<?, Double>, String> toStringFn = map -> {
        String res = Arrays.toString(map.values().toArray());
        return res.substring(1, res.length() - 2);
    };

    private static Telemetry telemetry = new Telemetry();
    private static NumberFormat fpsFormatter = new DecimalFormat("#0.00");
    private static int STATS_EVERY_FRAMES = 50;
    private Table<String, Long, Double> data = TreeBasedTable.create();
    private Path telemetryFile;
    private long frameCounter = 0;
    private double fpsAccum = 0;


    public static Telemetry getInstance() {
        return telemetry;
    }

    private static void writeToFile(Path file, String res) {
        try {
            Files.write(file, res.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOG.error("Unable to write to: {}", file.toAbsolutePath());
        }
    }

    public void addFpsSample(double value) {
        fpsAccum += value;
        addSample("fps", value);
    }

    public void addSample(String type, double value) {
        if (!enable) {
            return;
        }
        if (frameCounter > 0) {
            data.put(type, frameCounter, value);
        }
    }

    private String getAvgFpsRounded() {
        double r = fpsAccum / STATS_EVERY_FRAMES;
        r = ((int) (r * 100)) / 100d;
        fpsAccum = 0;
        return fpsFormatter.format(r);
    }

    public boolean hasNewStats() {
        return frameCounter % STATS_EVERY_FRAMES == 0; //update fps label every N frames
    }

    public Optional<String> getNewStats() {
        Optional<String> o = Optional.empty();
        if (hasNewStats()) {
//            Optional<String> arc = AudioRateControl.getLatestStats();
            o = Optional.of(getAvgFpsRounded() + "fps"); // + (arc.isPresent() ? ", " + arc.get() : ""));
        }
        return o;
    }

    public void reset() {
        frameCounter = 0;
        data.clear();
        telemetryFile = null;
    }

    public Optional<String> newFrame(double lastFps, double driftNs) {
        addFpsSample(lastFps);
        addSample("driftNs", driftNs / 1000d);
        Optional<String> os = getNewStats();
        newFrame();
        return os;
    }

    public void newFrame() {
        frameCounter++;
        if (!enable) {
            return;
        }
        if (frameCounter == 2) {
            telemetryFile = Paths.get(".", "tel_" + System.currentTimeMillis() + ".log");
            String header = "frame," + data.rowKeySet().stream().collect(Collectors.joining(","));
            writeToFile(telemetryFile, header);
            LOG.info("Logging telemetry file to: {}", telemetryFile.toAbsolutePath());
        }
        if (frameCounter % 600 == 0) {
            String res = "\n" + data.columnKeySet().stream().map(num -> num + "," + toStringFn.apply(data.column(num))).
                    collect(Collectors.joining("\n"));
            data.clear();
            Util.executorService.submit(() -> writeToFile(telemetryFile, res));
        }
    }
}
