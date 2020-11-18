/*
 * SystemLoader
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 17/10/19 14:04
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

package com.fbdev;

import com.fbdev.helios.input.KeyboardInputHelper;
import com.fbdev.helios.model.DisplayWindow;
import com.fbdev.helios.model.SystemProvider;
import com.fbdev.helios.util.Util;
import com.fbdev.ui.SwingWindow;
import com.fbdev.util.RomHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fbdev.helios.model.SystemProvider.SystemEvent.NEW_ROM;
import static com.fbdev.util.RomHelper.ROMS_FOLDER;

public class SystemLoader {

    public static final SystemLoader INSTANCE = new SystemLoader();
    private final static Logger LOG = LogManager.getLogger(SystemLoader.class.getSimpleName());
    private static final String PROPERTIES_FILENAME = "./helios.properties";
    public static boolean debugPerf = false;
    public static boolean showFps = false;
    public static boolean headless = false;
    private static AtomicBoolean init = new AtomicBoolean();
    protected DisplayWindow emuFrame;
    private SystemProvider systemProvider;

    private SystemLoader() {
    }

    protected static void loadProperties() {
        try (
                FileReader reader = new FileReader(PROPERTIES_FILENAME)
        ) {
            System.getProperties().load(reader);
        } catch (Exception e) {
            LOG.error("Unable to load properties file: " + PROPERTIES_FILENAME);
        }
        System.getProperties().list(System.out);
        System.out.println("-- done listing properties --");
        debugPerf = Boolean.parseBoolean(System.getProperty("helios.debug", "false"));
        showFps = Boolean.parseBoolean(System.getProperty("helios.fps", "false"));
        headless = Boolean.parseBoolean(System.getProperty("helios.headless", "false"));
    }

    protected static boolean isHeadless() {
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.isHeadlessInstance() || headless;
    }

    private static void setHeadless(boolean headless) {
        SystemLoader.headless = headless;
    }

    public static void main(String[] args) {
        init();
        //linux pulseaudio can crash if we start too quickly
        Util.sleep(250);
        String filePath = ROMS_FOLDER;
        if (args.length > 0) {
            filePath = args[0];
        } else {
            LOG.info("Launching folder at: {}", filePath);
            RomHelper.init();
            INSTANCE.handleNewRomFile(Paths.get(filePath));
            Util.sleep(1_000); //give the game thread a chance
        }
        if (headless) {
            Util.waitForever();
        }
    }

    public static SystemLoader getInstance() {
        if (init.compareAndSet(false, true)) {
            init();
        }
        return INSTANCE;
    }

    private static void init() {
        loadProperties();
        boolean isHeadless = isHeadless();
        LOG.info("Headless mode: {}", isHeadless);
        SystemLoader.setHeadless(isHeadless);
        KeyboardInputHelper.init();
        initLookAndFeel();
        INSTANCE.createFrame(isHeadless);
        init.set(true);
    }

    private static void initLookAndFeel() {
        try {
            String lf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            LOG.error("Failed to set SwingUI native Look and Feel", e);
        }
    }

    // Create the frame on the event dispatching thread
    protected void createFrame(boolean isHeadless) {
        Runnable frameRunnable = () -> {
            emuFrame = isHeadless ? DisplayWindow.HEADLESS_INSTANCE : new SwingWindow(getSystemAdapter());
            emuFrame.init();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            frameRunnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(frameRunnable);
            } catch (Exception e) {
                LOG.error("Unable to create SwingUI", e);
            }
        }
    }

    public SystemProvider handleNewRomFile(Path file) {
        systemProvider = createSystemProvider(file, debugPerf);
        if (systemProvider != null) {
            emuFrame.reloadSystem(systemProvider);
            systemProvider.handleSystemEvent(NEW_ROM, file);
        }
        return systemProvider;
    }

    SystemProvider getSystemAdapter() {
        return new SystemProvider() {

            @Override
            public void handleSystemEvent(SystemEvent event, Object parameter) {
                LOG.info("Event: {}, with parameter: {}", event, Objects.toString(parameter));
                switch (event) {
                    case NEW_ROM:
                        handleNewRomFile((Path) parameter);
                        break;
                    case CLOSE_ROM:
                        LOG.info("Ignoring event: {}, with parameter: {}", event, Objects.toString(parameter));
                        break;
                    default:
                        LOG.warn("Unable to handle event: {}, with parameter: {}", event, Objects.toString(parameter));
                        break;
                }
            }

            @Override
            public boolean isRomRunning() {
                return false;
            }

            @Override
            public Path getRomPath() {
                return null;
            }
        };
    }

    public SystemProvider createSystemProvider(Path file) {
        return createSystemProvider(file, false);
    }


    public SystemProvider createSystemProvider(Path file, boolean debugPerf) {
        systemProvider = Z80BaseSystem.createNewInstance(emuFrame);
        return systemProvider;
    }

    public SystemProvider getSystemProvider() {
        return systemProvider;
    }
}
