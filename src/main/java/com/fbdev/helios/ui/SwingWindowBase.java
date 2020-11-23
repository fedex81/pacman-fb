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

package com.fbdev.helios.ui;

import com.fbdev.SystemLoader;
import com.fbdev.helios.input.InputProvider;
import com.fbdev.helios.input.InputProvider.PlayerNumber;
import com.fbdev.helios.model.DisplayWindow;
import com.fbdev.helios.model.SystemProvider;
import com.fbdev.helios.model.SystemProvider.SystemEvent;
import com.fbdev.helios.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

import static com.fbdev.helios.model.SystemProvider.SystemEvent.*;
import static com.fbdev.helios.ui.SwingWindowBase.FileResourceType.SAVE_STATE_RES;
import static com.fbdev.helios.util.ScreenSizeHelper.*;

public abstract class SwingWindowBase implements DisplayWindow {

    private static final Logger LOG = LogManager.getLogger(SwingWindowBase.class.getSimpleName());
    public static String QUICK_SAVE_FILENAME = "quick_save";
    public static String QUICK_SAVE_PATH = System.getProperty("quick.save.path", ".");
    private final JLabel perfLabel = new JLabel("");
    protected Dimension frameBufferSize;
    protected Dimension viewportSize;
    protected BufferedImage baseImage;
    protected Image destImage;
    protected Canvas screenCanvas;
    protected BufferStrategy strategy;
    protected Graphics2D graphics;
    protected SystemProvider mainEmu;
    //rendering stuff
    protected int[] renderData;
    protected Dimension dimension = new Dimension();
    protected int x, y;
    private double scale = DEFAULT_SCALE_FACTOR;
    private JFrame jFrame;
    private JCheckBoxMenuItem fullScreenItem;
    private JCheckBoxMenuItem muteItem;
    //    private JMenu recentFilesMenu;
    private JMenuItem[] recentFilesItems;
    private Map<PlayerNumber, JMenu> inputMenusMap;
    private final static int screenChangedCheckFrequency = 60;
    private List<JCheckBoxMenuItem> screenItems;
    private int screenChangedCheckCounter = screenChangedCheckFrequency;

    private boolean showDebug = false;
    private Map<SystemEvent, AbstractAction> actionMap = new HashMap<>();
    // Transparent 16 x 16 pixel cursor image.
    private BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    // Create a new blank cursor.
    private Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
    private int showInfoCount = SHOW_INFO_FRAMES_DELAY;
    private Optional<String> actionInfo = Optional.empty();
    private Rectangle viewportBounds = new Rectangle();
    private int viewportW, viewportH;

    public SwingWindowBase(SystemProvider mainEmu) {
        this.mainEmu = mainEmu;
        this.inputMenusMap = new LinkedHashMap<>();
        Arrays.stream(PlayerNumber.values()).
                forEach(pn -> inputMenusMap.put(pn, new JMenu(pn.name())));
    }

    protected abstract void handleSystemEvent(SystemEvent event, Object par, String msg);

    protected abstract KeyStroke getAcceleratorKey(SystemEvent event);

    public abstract void refresh();

    @Override
    public void init() {
        initSwing();
    }

    public void setTitle(String title) {
        jFrame.setTitle(APP_NAME + " " + VERSION + " - " + title);
    }

    private void addKeyAction(JMenuItem component, SystemEvent event, ActionListener l) {
        AbstractAction action = toAbstractAction(component.getText(), l);
        if (event != NONE) {
            action.putValue(Action.ACCELERATOR_KEY, getAcceleratorKey(event));
            actionMap.put(event, action);
        }
        component.setAction(action);
    }

    private void addAction(JMenuItem component, ActionListener act) {
        addKeyAction(component, NONE, act);
    }

    private AbstractAction toAbstractAction(String name, ActionListener listener) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }

            @Override
            public void setEnabled(boolean newValue) {
                super.setEnabled(true);
            }
        };
    }

    private void showHelpMessage(String title, String msg) {
        JTextArea area = new JTextArea(msg);
        area.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(jFrame.getPreferredSize());
        JOptionPane.showMessageDialog(this.jFrame,
                scrollPane, "Help: " + title, JOptionPane.INFORMATION_MESSAGE);
    }

    private BufferedImage createImage(GraphicsDevice gd, Dimension d) {
        BufferedImage bi = gd.getDefaultConfiguration().createCompatibleImage(d.width, d.height);
        if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
            //mmh we need INT_RGB here
            bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        }
        return bi;
    }

    private void showDebugInfo(boolean showDebug) {
        this.showDebug = showDebug;
        SwingUtilities.invokeLater(() -> {
            if (fullScreenItem.getState()) {
                jFrame.getJMenuBar().setVisible(showDebug);
            }
            perfLabel.setVisible(showDebug);
        });
    }

    public void resetScreen() {
        Util.sleep(100);
        SwingUtilities.invokeLater(() -> {
            perfLabel.setText("");
            jFrame.setTitle(FRAME_TITLE_HEAD);
            LOG.info("Blanking screen");
            Arrays.fill(renderData, 0);
            refresh();
        });
    }

    private void fullScreenAction(ActionEvent doToggle) {
        boolean val = doToggle != null ? fullScreenItem.getState() : !fullScreenItem.getState();
        SwingUtilities.invokeLater(() -> setFullScreen(val));
    }

    @Override
    public void setFullScreen(boolean value) {
        fullScreenItem.setState(value);
        LOG.info("Full screen: " + fullScreenItem.isSelected());
        SwingUtilities.invokeLater(() -> {
            jFrame.setVisible(false);
            GraphicsDevice gd = SwingScreenSupport.getGraphicsDevice();
            gd.setFullScreenWindow(value ? jFrame : null);
            if (!value) {
                jFrame.setSize(viewportSize);
            }
            viewportW = jFrame.getWidth();
            viewportH = jFrame.getHeight();

            jFrame.setVisible(true);
            jFrame.invalidate();
            jFrame.repaint();
        });
    }

    protected void showLabel(String label) {
        showInfoCount--;
        if (actionInfo.isPresent()) {
            label += " - " + actionInfo.get();
        }
        if (!label.equalsIgnoreCase(perfLabel.getText())) {
            perfLabel.setText(label);
        }
        if (showInfoCount <= 0) {
            actionInfo = Optional.empty();
        }
    }

    private Optional<File> loadFileDialog(Component parent, FileResourceType type) {
        return fileDialog(parent, type, true);
    }

    private Optional<File> fileDialog(Component parent, FileResourceType type, boolean load) {
        int dialogType = load ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG;
        Optional<File> res = Optional.empty();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setFileFilter(Util.folderFilter);
        fileChooser.setDialogType(dialogType);
        int result = fileChooser.showDialog(parent, null);
        if (result == JFileChooser.APPROVE_OPTION) {
            res = Optional.ofNullable(fileChooser.getSelectedFile());
        }
        return res;
    }

    @Override
    public void showInfo(String info) {
        actionInfo = Optional.of(info);
        showInfoCount = SHOW_INFO_FRAMES_DELAY;
    }

    private Optional<File> loadRomDialog(Component parent) {
        return loadFileDialog(parent, FileResourceType.ROM);
    }

    private Optional<File> loadStateFileDialog(Component parent) {
        return loadFileDialog(parent, SAVE_STATE_RES);
    }

    private void handleLoadState() {
        Optional<File> optFile = loadStateFileDialog(jFrame);
        if (optFile.isPresent()) {
            Path file = optFile.get().toPath();
            handleSystemEvent(LOAD_STATE, file, file.getFileName().toString());
//            PrefStore.lastSaveFolder = file.getParent().toAbsolutePath().toString();
        }
    }

    private void handleQuickLoadState() {
        Path file = Paths.get(QUICK_SAVE_PATH, QUICK_SAVE_FILENAME);
        handleSystemEvent(QUICK_LOAD, file, file.getFileName().toString());
    }

    private void handleQuickSaveState() {
        Path p = Paths.get(QUICK_SAVE_PATH, QUICK_SAVE_FILENAME);
        handleSystemEvent(QUICK_SAVE, p, p.getFileName().toString());
    }

    private void handleSaveState() {
        Optional<File> optFile = fileDialog(jFrame, SAVE_STATE_RES, false);
        if (optFile.isPresent()) {
            handleSystemEvent(SAVE_STATE, optFile.get().toPath(), optFile.get().getName());
        }
    }

    protected void handleNewRomDialog() {
        handleSystemEvent(CLOSE_ROM, null, null);
        Optional<File> optFile = loadRomDialog(jFrame);
        if (optFile.isPresent()) {
            Path file = optFile.get().toPath();
            SystemLoader.getInstance().handleNewRomFile(file);
            showInfo(NEW_ROM + ": " + file.getFileName());
        }
    }

    private void handleNewRomRecent(String path) {
        Path p = Paths.get(path);
        showInfo(NEW_ROM + ": " + p.getFileName());
        SystemLoader.getInstance().handleNewRomFile(p);
    }

    @Override
    public void reloadSystem(SystemProvider systemProvider) {
        Optional.ofNullable(mainEmu).ifPresent(sys -> sys.handleSystemEvent(CLOSE_ROM, null));
        this.mainEmu = systemProvider;

        Arrays.stream(jFrame.getKeyListeners()).forEach(jFrame::removeKeyListener);
        setupFrameKeyListener();
        Optional.ofNullable(mainEmu).ifPresent(sp -> setTitle(""));
    }

    @Override
    public void addKeyListener(KeyListener keyAdapter) {
        jFrame.addKeyListener(keyAdapter);
        screenCanvas.addKeyListener(keyAdapter);
    }

    protected int[] getPixels(BufferedImage img) {
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }

    //TODO this is necessary in fullScreenMode
    public KeyListener setupFrameKeyListener() {
        KeyListener kl = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
//                LOG.info(keyStroke.toString());
                SystemEvent event = KeyBindingsHandler.getInstance().getSystemEventIfAny(keyStroke);
                if (event != null && event != NONE) {
                    //if the menuBar is visible it will handle the event, otherwise we need to perform the action here
                    boolean menuVisible = jFrame.getJMenuBar().isVisible();
                    if (!menuVisible) {
                        Optional.ofNullable(actionMap.get(event)).ifPresent(act -> act.actionPerformed(null));
                    }
                }
            }
        };
        jFrame.addKeyListener(kl);
        screenCanvas.addKeyListener(kl);
        return kl;
    }

    private List<JCheckBoxMenuItem> createAddScreenItems(JMenu screensMenu) {
        List<String> l = SwingScreenSupport.detectScreens();
        screenItems = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            JCheckBoxMenuItem it = new JCheckBoxMenuItem(s);
            it.setState(i == SwingScreenSupport.getCurrentScreen());
            screenItems.add(it);
            screensMenu.add(it);
        }
        for (int i = 0; i < screenItems.size(); i++) {
            final int num = i;
            addKeyAction(screenItems.get(i), NONE, e -> handleScreenChange(screenItems, num));
        }
        return screenItems;
    }

    private void handleScreenChange(List<JCheckBoxMenuItem> items, int newScreen) {
        int cs = SwingScreenSupport.getCurrentScreen();
        if (cs != newScreen) {
            SwingScreenSupport.showOnScreen(newScreen, jFrame);
        }
        handleScreenChangeItems(items, newScreen);
    }

    private void handleScreenChangeItems(List<JCheckBoxMenuItem> items, int newScreen) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == newScreen);
        }
    }

    @Override
    public void reloadControllers(Collection<String> list) {
        for (Map.Entry<PlayerNumber, JMenu> entry : inputMenusMap.entrySet()) {
            PlayerNumber pn = entry.getKey();
            JMenu menu = entry.getValue();
            menu.removeAll();
            List<JCheckBoxMenuItem> l = new ArrayList<>();
            list.forEach(c -> {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(c, InputProvider.KEYBOARD_CONTROLLER.equalsIgnoreCase(c));
                addAction(item, e -> handleSystemEvent(CONTROLLER_CHANGE, pn.name() + ":" + c,
                        pn.name() + ":" + c));
                l.add(item);
            });
            //only allow one selection
            final List<JCheckBoxMenuItem> list1 = new ArrayList<>(l);
            l.stream().forEach(i -> i.addItemListener(e -> {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    list1.stream().filter(i1 -> !i.getText().equals(i1.getText())).forEach(i1 -> i1.setSelected(false));
                }
            }));
            l.forEach(menu::add);
            //fudgePlayer1Using1stController
            if (list.size() > 2 && pn == PlayerNumber.P1) {
                LOG.info("Auto-selecting {} using Controller: {}", pn, l.get(2).getText());
                l.get(2).doClick();
            }
        }
    }

    private Graphics2D getBuffer() {
        if (graphics == null) {
            try {
                graphics = (Graphics2D) strategy.getDrawGraphics();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return null;
            }
        }
        return graphics;
    }

    public void initSwing() {
        GraphicsDevice gd = SwingScreenSupport.setupScreens();
        LOG.info("Emulation viewport size: " + viewportSize);

        jFrame = new JFrame(FRAME_TITLE_HEAD, gd.getDefaultConfiguration());
        jFrame.getContentPane().setBackground(Color.BLACK);
        jFrame.getContentPane().setForeground(Color.BLACK);

        baseImage = createImage(gd, frameBufferSize);
        destImage = baseImage.getSubimage(0, 0, viewportSize.width, viewportSize.height);
        renderData = getPixels(baseImage);

        JMenuBar bar = new JMenuBar();

        JMenu menu = new JMenu("File");
        bar.add(menu);

        JMenu setting = new JMenu("Setting");
        bar.add(setting);

        JMenuItem pauseItem = new JMenuItem("Pause");
        addKeyAction(pauseItem, TOGGLE_PAUSE, e -> handleSystemEvent(TOGGLE_PAUSE, null, null));
        setting.add(pauseItem);

        JMenuItem resetItem = new JMenuItem("Hard Reset");
        addKeyAction(resetItem, RESET, e -> mainEmu.reset());
        setting.add(resetItem);

        JMenuItem softResetItem = new JMenuItem("Soft Reset");
        addKeyAction(softResetItem, SOFT_RESET, e -> handleSystemEvent(SOFT_RESET, null, null));
//        setting.add(softResetItem);

        JMenu screensMenu = new JMenu("Screens");
        createAddScreenItems(screensMenu);
        setting.add(screensMenu);

        JMenu inputMenu = new JMenu("Input");
        reloadControllers(InputProvider.DEFAULT_CONTROLLERS);
        inputMenusMap.values().forEach(inputMenu::add);
        setting.add(inputMenu);

        JMenu menuView = new JMenu("View");
        bar.add(menuView);

        fullScreenItem = new JCheckBoxMenuItem("Full Screen", false);
        addKeyAction(fullScreenItem, TOGGLE_FULL_SCREEN, e -> fullScreenAction(e));
        menuView.add(fullScreenItem);

        muteItem = new JCheckBoxMenuItem("Enable Sound", true);
        addKeyAction(muteItem, TOGGLE_MUTE, e -> handleSystemEvent(TOGGLE_MUTE, null, null));
        menuView.add(muteItem);

        JMenu helpMenu = new JMenu("Help");
        bar.add(helpMenu);
        bar.add(Box.createHorizontalGlue());
        bar.add(perfLabel);

        JMenuItem loadRomItem = new JMenuItem("Load romSet from folder");
        addKeyAction(loadRomItem, NEW_ROM, e -> handleNewRomDialog());

        JMenuItem closeRomItem = new JMenuItem("Close ROM");
        addKeyAction(closeRomItem, CLOSE_ROM, e -> handleSystemEvent(CLOSE_ROM, null, null));

        JMenuItem loadStateItem = new JMenuItem("Load State");
        addKeyAction(loadStateItem, LOAD_STATE, e -> handleLoadState());

        JMenuItem saveStateItem = new JMenuItem("Save State");
        addKeyAction(saveStateItem, SAVE_STATE, e -> handleSaveState());

        JMenuItem quickSaveStateItem = new JMenuItem("Quick Save State");
        addKeyAction(quickSaveStateItem, QUICK_SAVE, e -> handleQuickSaveState());

        JMenuItem quickLoadStateItem = new JMenuItem("Quick Load State");
        addKeyAction(quickLoadStateItem, QUICK_LOAD, e -> handleQuickLoadState());

        JMenuItem exitItem = new JMenuItem("Exit");
        addKeyAction(exitItem, CLOSE_APP, e -> {
            handleSystemEvent(CLOSE_APP, null, null);
            System.exit(0);
        });

        JMenuItem aboutItem = new JMenuItem("About");
        addAction(aboutItem, e -> showHelpMessage(aboutItem.getText(), getAboutString()));

        JMenuItem creditsItem = new JMenuItem("Credits");
        addAction(creditsItem, e -> showHelpMessage(creditsItem.getText(),
                FileUtil.readFileContentAsString("CREDITS.md")
        ));

        JMenuItem keyBindingsItem = new JMenuItem("Key Bindings");
        addAction(keyBindingsItem, e -> showHelpMessage(keyBindingsItem.getText(),
                KeyBindingsHandler.toConfigString()
        ));

        JMenuItem readmeItem = new JMenuItem("Readme");
        addAction(readmeItem, e -> showHelpMessage(readmeItem.getText(),
                FileUtil.readFileContentAsString("README.md")
        ));

        JMenuItem licenseItem = new JMenuItem("License");
        addAction(licenseItem, e -> showHelpMessage(licenseItem.getText(),
                FileUtil.readFileContentAsString("LICENSE.md")
        ));

        JMenuItem historyItem = new JMenuItem("History");
        addAction(historyItem, e -> showHelpMessage(historyItem.getText(),
                FileUtil.readFileContentAsString("HISTORY.md")));

        menu.add(loadRomItem);
        menu.add(closeRomItem);
//        menu.add(loadStateItem);
//        menu.add(saveStateItem);
//        menu.add(quickLoadStateItem);
//        menu.add(quickSaveStateItem);
        menu.add(exitItem);
        helpMenu.add(aboutItem);
        helpMenu.add(keyBindingsItem);
        helpMenu.add(readmeItem);
        helpMenu.add(creditsItem);
        helpMenu.add(historyItem);
        helpMenu.add(licenseItem);

        AbstractAction debugUiAction = toAbstractAction("debugUI", e -> showDebugInfo(!showDebug));
        actionMap.put(SET_DEBUG_UI, debugUiAction);

        jFrame.setMinimumSize(ScreenSizeHelper.DEFAULT_FRAME_SIZE);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(true);
        jFrame.setJMenuBar(bar);
        jFrame.setVisible(true);

        screenCanvas = new Canvas(gd.getDefaultConfiguration());
        screenCanvas.setIgnoreRepaint(true);
        jFrame.add(screenCanvas, -1);
        screenCanvas.createBufferStrategy(2);
        do {
            strategy = screenCanvas.getBufferStrategy();
        } while (strategy == null);
        jFrame.setVisible(false);
        jFrame.pack();

        //get the center location and then reset it
        jFrame.setLocationRelativeTo(null);
        Point centerPoint = jFrame.getLocation();
        jFrame.setLocation(gd.getDefaultConfiguration().getBounds().x + centerPoint.x,
                gd.getDefaultConfiguration().getBounds().y + centerPoint.y);

        viewportH = jFrame.getHeight();
        viewportW = jFrame.getWidth();

        jFrame.setVisible(true);
    }

    protected void refreshStrategy(boolean isBlanked) {
        // Update Graphics
        Graphics2D bg = null;
        viewportBounds = updateViewportBounds();
        int h = viewportBounds.height;
        int w = viewportBounds.width;
        int offsetW = viewportBounds.x;
        int offsetH = viewportBounds.y;
        do {
            try {
                bg = getBuffer();
                if (isBlanked) {
                    bg.setColor(Color.BLACK);
                    bg.fillRect(0, 0, w, h);
                } else {
                    bg.drawImage(destImage, offsetW, offsetH, w + offsetW, h + offsetH
                            , 0, 0, dimension.width, dimension.height, null);
                }
            } finally {
                Optional.ofNullable(bg).ifPresent(Graphics2D::dispose);
            }
        } while (!updateScreen());
        detectUserScreenChange();
    }

    private void detectUserScreenChange() {
        if (--screenChangedCheckCounter == 0) {
            screenChangedCheckCounter = screenChangedCheckFrequency;
            int prev = SwingScreenSupport.getCurrentScreen();
            int newScreen = SwingScreenSupport.detectUserScreenChange(jFrame.getGraphicsConfiguration().getDevice());
            if (prev != newScreen) {
                LOG.info("Detected user change, showing on screen: {}", newScreen);
                handleScreenChangeItems(screenItems, newScreen);
            }
        }
    }

    private Rectangle updateViewportBounds() {
        Dimension d = null;
        double scaleW = scale, scaleH = scale;
        int offsetW = 0, offsetH = 0;
        if (dimension.width != DEFAULT_W || dimension.height != DEFAULT_H) {
            scaleW = scale * DEFAULT_W / dimension.width;
            scaleH = scale * DEFAULT_H / dimension.height;
        }
        int w = (int) (dimension.width * scaleW);
        int h = (int) (dimension.height * scaleH);
        boolean fullScreen = fullScreenItem.getState();

        if (fullScreen) {
            Dimension fs = new Dimension(viewportW, viewportH);
            double ratio = ScreenSizeHelper.getFullScreenScaleFactor(fs, dimension);
            w = (int) (dimension.width * ratio);
            h = (int) (dimension.height * ratio);
            offsetW = Math.max(0, viewportW - w) / 2;
            offsetH = Math.max(0, viewportH - h) / 2;
        }
        viewportBounds.setBounds(offsetW, offsetH, w, h);
        return viewportBounds;
    }

    private boolean updateScreen() {
        graphics.dispose();
        graphics = null;
        try {
            strategy.show();
            Toolkit.getDefaultToolkit().sync();
            return (!strategy.contentsLost());
        } catch (Exception e) {
            return true;
        }
    }

    public int[] acquireRender() {
        return renderData;
    }

    protected boolean updateDimension(boolean force, int w, int h, int newX, int newY) {
        if (w * h == 0) {
            return false;
        }
        boolean change = false;
        if (dimension.width != w || dimension.height != h) {
            String prev = dimension.toString();
            dimension.width = w;
            dimension.height = h;
            LOG.info(prev + " -> " + dimension);
            change = true;
        }
        if (x != newX || y != newY) {
            change = true;
            x = newX;
            y = newY;
        }
        change |= force;
        if (change) {
            destImage = baseImage.getSubimage(x, y, w, h);
        }
        return change;
    }

    public enum FileResourceType {ROM, SAVE_STATE_RES}
}