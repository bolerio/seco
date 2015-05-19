/*
 * @(#)QuaquaRootPaneUI.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.awt.event.*;
import java.awt.peer.*;
import java.beans.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.WeakHashMap;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaRootPaneUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaRootPaneUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaRootPaneUI extends BasicRootPaneUI {

    /**
     * Keys to lookup borders in defaults table.
     */
    private static final String[] borderKeys = new String[]{
        null, "RootPane.frameBorder", "RootPane.plainDialogBorder",
        "RootPane.informationDialogBorder",
        "RootPane.errorDialogBorder", "RootPane.colorChooserDialogBorder",
        "RootPane.fileChooserDialogBorder", "RootPane.questionDialogBorder",
        "RootPane.warningDialogBorder"
    };
    /**
     * Height and width of resize handle on the lower right corner of the window.
     * FIXME - This value depends on font size.
     */
    private static final int BORDER_DRAG_THICKNESS = 15;
    /**
     * Window the <code>JRootPane</code> is in.
     */
    private Window window;
    /**
     * <code>JComponent</code> providing window decorations. This will be
     * null if not providing window decorations.
     */
    private JComponent titlePane;
    /**
     * <code>MouseInputListener</code> that is added to the parent
     * <code>Window</code> the <code>JRootPane</code> is contained in.
     */
    private MouseInputListener mouseInputListener;
    /**
     * The <code>LayoutManager</code> that is set on the
     * <code>JRootPane</code>.
     */
    private LayoutManager layoutManager;
    /**
     * <code>LayoutManager</code> of the <code>JRootPane</code> before we
     * replaced it.
     */
    private LayoutManager savedOldLayout;
    private AncestorListener ancestorListener;
    private ComponentListener componentListener;
    /**
     * This variable is set to false, if we fail to invoke the
     * setWindowModifiedMethod.
     */
    private static boolean isWindowModifiedSupported = true;
    /**
     * This method is used to access the non-API peer methods of Apple's
     * Window peers. The method is different for the different MRJ versions.
     */
    private static Method setWindowModifiedMethod = null;
    /**
     * <code>JRootPane</code> providing the look and feel for.
     */
    private JRootPane root;
    /**
     * Since method Window.getWindows() is only available since Java 1.6,
     * we indirectly keep track of all windows by ourselves by storing
     * all JRootPanes in this weak hash map.
     * We add a JRootPane to this map, upon installUI, and remove a JRootPane
     * from this upoin deinstallUI.
     */
    private static WeakHashMap allRootPanes = new WeakHashMap();
    /**
     * <code>Cursor</code> used to track the cursor set by the user.
     * This is initially <code>Cursor.DEFAULT_CURSOR</code>.
     */
    private Cursor lastCursor =
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaRootPaneUI();
    }

    /** Creates a new instance. */
    public QuaquaRootPaneUI() {
    }

    /**
     * Invokes supers implementation of <code>installUI</code> to install
     * the necessary state onto the passed in <code>JRootPane</code>
     * to render the metal look and feel implementation of
     * <code>RootPaneUI</code>. If
     * the <code>windowDecorationStyle</code> property of the
     * <code>JRootPane</code> is other than <code>JRootPane.NONE</code>,
     * this will add a custom <code>Component</code> to render the widgets to
     * <code>JRootPane</code>, as well as installing a custom
     * <code>Border</code> and <code>LayoutManager</code> on the
     * <code>JRootPane</code>.
     *
     * @param c the JRootPane to install state onto
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        root = (JRootPane) c;
        c.putClientProperty(
                "apple.awt.draggableWindowBackground",
                UIManager.get("RootPane.draggableWindowBackground"));
        c.putClientProperty(
                "apple.awt.windowShadow",
                UIManager.get("RootPane.windowShadow"));

        int style = root.getWindowDecorationStyle();
        if (style != JRootPane.NONE) {
            installClientDecorations(root);
        }
        allRootPanes.put(c, null);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D gr = (Graphics2D) g;
        // Erase background. This is needed for semi-transparent windows.
        if (root.getClientProperty("Window.alpha") instanceof Float) {
            float alpha = ((Float) root.getClientProperty("Window.alpha")).floatValue();
            if (alpha < 1f) {
                if (System.getProperty("java.version").startsWith("1.6")) {
                    Color background = c.getBackground();
                    if (PaintableColor.getPaint(background, c) instanceof Color) {
                        Color bg = background;
                        gr.setPaint(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int) (alpha * 255f)));
                    } else {
                        gr.setPaint(PaintableColor.getPaint(background, c));
                    }
                } else {
                    gr.setPaint(PaintableColor.getPaint(c.getBackground(), c));
                }
                Composite comp = gr.getComposite();
                gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                gr.fillRect(0, 0, c.getWidth(), c.getHeight());
                gr.setComposite(comp);
            }
        } else {
            Color background = UIManager.getColor("RootPane.background");
            gr.setPaint(PaintableColor.getPaint(background, c));
            gr.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        // Paint decorations
        int style = root.getWindowDecorationStyle();
        if (style != JRootPane.NONE) {
            boolean needsResizeIcon = false;
            if (window instanceof Frame) {
                Frame frame = (Frame) window;
                needsResizeIcon = frame.isResizable();
            } else if (window instanceof Dialog) {
                Dialog dialog = (Dialog) window;
                needsResizeIcon = dialog.isResizable();
            }

            if (needsResizeIcon) {
                Icon resizeIcon = UIManager.getIcon("InternalFrame.resizeIcon");
                int w = c.getWidth();
                int h = c.getHeight();
                Insets insets = c.getInsets();
                resizeIcon.paintIcon(c, g,
                        w - resizeIcon.getIconWidth() - insets.right,
                        h - resizeIcon.getIconHeight() - insets.bottom);
            }
        }
    }

    /**
     * Invokes supers implementation to uninstall any of its state. This will
     * also reset the <code>LayoutManager</code> of the <code>JRootPane</code>.
     * If a <code>Component</code> has been added to the <code>JRootPane</code>
     * to render the window decoration style, this method will remove it.
     * Similarly, this will revert the Border and LayoutManager of the
     * <code>JRootPane</code> to what it was before <code>installUI</code>
     * was invoked.
     *
     * @param c the JRootPane to uninstall state from
     */
    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        uninstallClientDecorations(root);

        layoutManager =
                null;
        mouseInputListener =
                null;
        root =
                null;
        allRootPanes.remove(c);
    }

    @Override
    protected void installDefaults(JRootPane c) {
        super.installDefaults(c);
        LookAndFeel.installColorsAndFont(c,
                "RootPane.background",
                "RootPane.foreground",
                "RootPane.font");
        LookAndFeel.installBorder(c, "RootPane.border");

	QuaquaUtilities.installProperty(c, "opaque", UIManager.get("RootPane.opaque"));

        // By default, we should delay window ordering, but
        // it does not seem to work as expected. It appears that we need to
        // at more code.
        // c.putClientProperty("apple.awt.delayWindowOrdering", Boolean.TRUE);
    }

    @Override
    public void update(Graphics gr, final JComponent c) {
        if (c.isOpaque()) {
            Graphics2D g = (Graphics2D) gr;
            g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        /*
        root.putClientProperty(
        "apple.awt.windowShadow.revalidateNow", Boolean.TRUE
        );
         */

        paint(gr, c);
    }

    @Override
    protected void installListeners(JRootPane root) {
        super.installListeners(root);

        ancestorListener =
                createAncestorListener();
        if (ancestorListener != null) {
            root.addAncestorListener(ancestorListener);
        }

        componentListener = createComponentListener();
        if (componentListener != null) {
            root.addComponentListener(componentListener);
        }

    }

    @Override
    protected void uninstallListeners(JRootPane root) {
        super.uninstallListeners(root);

        if (ancestorListener != null) {
            root.removeAncestorListener(ancestorListener);
        }

        if (componentListener != null) {
            root.removeComponentListener(componentListener);
        }

    }

    protected ComponentListener createComponentListener() {
        return new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent e) {
                Timer t = new Timer(200, new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        e.getComponent().repaint();
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        };
    }

    protected AncestorListener createAncestorListener() {
        return new RootPaneAncestorListener();
    }

    /**
     * Returns the <code>JComponent</code> to render the window decoration
     * style.
     */
    private JComponent createTitlePane(JRootPane root) {
        return new QuaquaTitlePane(root, this);
    }

    /**
     * Returns a <code>MouseListener</code> that will be added to the
     * <code>Window</code> containing the <code>JRootPane</code>.
     */
    private MouseInputListener createWindowMouseInputListener(JRootPane root) {
        return new MouseInputHandler();
    }

    /**
     * Returns a <code>LayoutManager</code> that will be set on the
     * <code>JRootPane</code>.
     */
    private LayoutManager createLayoutManager() {
        return new QuaquaRootLayout();
    }

    private static void updateWindowModified(JRootPane rootpane) {
        if (isWindowModifiedSupported) {
            Container parent = rootpane.getParent();
            if (parent != null && (parent instanceof Window)) {
                ComponentPeer peer = parent.getPeer();
                if (peer != null) {
                    if (setWindowModifiedMethod == null) {
                        try {
                            setWindowModifiedMethod = peer.getClass().getMethod("setDocumentEdited", new Class[]{Boolean.TYPE});
                        } catch (NoSuchMethodException ex1) {
                            try {
                                setWindowModifiedMethod = peer.getClass().getMethod("setModified", new Class[]{Boolean.TYPE});
                            } catch (NoSuchMethodException ex2) {
                                isWindowModifiedSupported = false;
                                //ex2.printStackTrace();
                            }

                        } catch (AccessControlException ex1) {
                            isWindowModifiedSupported = false;
                            //System.err.println("Sorry. QuaquaRootPaneUI can not access the native window modified API");
                        }

                    }
                    if (setWindowModifiedMethod != null) {
                        try {
                            Object value = rootpane.getClientProperty("Window.documentModified");
                            if (value == null) {
                                value = rootpane.getClientProperty("windowModified");
                            }

                            if (value == null) {
                                value = Boolean.FALSE;
                            }

                            setWindowModifiedMethod.invoke(peer, new Object[]{value});
                        } catch (IllegalAccessException ex) {
                            isWindowModifiedSupported = false;
                            //ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            isWindowModifiedSupported = false;
                            //ex.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    /**
     * Installs the appropriate <code>Border</code> onto the
     * <code>JRootPane</code>.
     */
    void installBorder(JRootPane root) {
        int style = root.getWindowDecorationStyle();

        if (style == JRootPane.NONE) {
            LookAndFeel.uninstallBorder(root);
        } else {
            LookAndFeel.installBorder(root, borderKeys[style]);
        }

    }

    /**
     * Removes any border that may have been installed.
     */
    private void uninstallBorder(JRootPane root) {
        LookAndFeel.uninstallBorder(root);
    }

    /**
     * Installs the necessary state onto the JRootPane to render client
     * decorations. This is ONLY invoked if the <code>JRootPane</code>
     * has a decoration style other than <code>JRootPane.NONE</code>.
     */
    private void installClientDecorations(JRootPane root) {
        installBorder(root);

        /*
        window = SwingUtilities.getWindowAncestor(root);
        if (window != null) {
        window.setBackground(new Color(0, true));
        }*/

        root.putClientProperty(
                "apple.awt.draggableWindowBackground", Boolean.FALSE);
        root.putClientProperty(
                "apple.awt.windowShadow", Boolean.TRUE);

        JComponent titlePane = createTitlePane(root);

        setTitlePane(root, titlePane);
        installWindowListeners(root, root.getParent());
        installLayout(root);
        if (window != null) {
            root.revalidate();
            root.repaint();
        }

        root.putClientProperty(
                "apple.awt.windowShadow.revalidateNow", new Object());
    }

    /**
     * Installs the necessary Listeners on the parent <code>Window</code>,
     * if there is one.
     * <p>
     * This takes the parent so that cleanup can be done from
     * <code>removeNotify</code>, at which point the parent hasn't been
     * reset yet.
     *
     * @param parent The parent of the JRootPane
     */
    private void installWindowListeners(JRootPane root, Component parent) {
        if (parent instanceof Window) {
            window = (Window) parent;
        } else {
            window = SwingUtilities.getWindowAncestor(parent);
        }

        if (window != null) {
            if (mouseInputListener == null) {
                mouseInputListener = createWindowMouseInputListener(root);
            }

            window.addMouseListener(mouseInputListener);
            window.addMouseMotionListener(mouseInputListener);
        }

    }

    /**
     * Uninstalls the necessary Listeners on the <code>Window</code> the
     * Listeners were last installed on.
     */
    private void uninstallWindowListeners(JRootPane root) {
        if (window != null) {
            window.removeMouseListener(mouseInputListener);
            window.removeMouseMotionListener(mouseInputListener);
        }

    }

    /**
     * Installs the appropriate LayoutManager on the <code>JRootPane</code>
     * to render the window decorations.
     */
    private void installLayout(JRootPane root) {
        if (layoutManager == null) {
            layoutManager = createLayoutManager();
        }

        savedOldLayout = root.getLayout();
        root.setLayout(layoutManager);
    }

    /**
     * Uninstalls the previously installed <code>LayoutManager</code>.
     */
    private void uninstallLayout(JRootPane root) {
        if (savedOldLayout != null) {
            root.setLayout(savedOldLayout);
            savedOldLayout =
                    null;
        }

    }

    private boolean isVertical(JRootPane root) {
        return root.getClientProperty("Quaqua.RootPane.isVertical") == Boolean.TRUE;
    }

    /**
     * Sets the window title pane -- the JComponent used to provide a plaf a
     * way to override the native operating system's window title pane with
     * one whose look and feel are controlled by the plaf.  The plaf creates
     * and sets this value; the default is null, implying a native operating
     * system window title pane.
     *
     * @param content the <code>JComponent</code> to use for the window title pane.
     */
    private void setTitlePane(JRootPane root, JComponent titlePane) {
        JLayeredPane layeredPane = root.getLayeredPane();
        JComponent oldTitlePane = getTitlePane();

        if (oldTitlePane != null) {
            oldTitlePane.setVisible(false);
            layeredPane.remove(oldTitlePane);
        }

        if (titlePane != null) {
            layeredPane.add(titlePane, JLayeredPane.FRAME_CONTENT_LAYER);
            titlePane.setVisible(true);
        }

        this.titlePane = titlePane;
    }

    /**
     * Returns the <code>JComponent</code> rendering the title pane. If this
     * returns null, it implies there is no need to render window decorations.
     *
     * @return the current window title pane, or null
     * @see #setTitlePane
     */
    private JComponent getTitlePane() {
        return titlePane;
    }

    /**
     * Returns the <code>JRootPane</code> we're providing the look and
     * feel for.
     */
    private JRootPane getRootPane() {
        return root;
    }

    /**
     * Uninstalls any state that <code>installClientDecorations</code> has
     * installed.
     * <p>
     * NOTE: This may be called if you haven't installed client decorations
     * yet (ie before <code>installClientDecorations</code> has been invoked).
     */
    private void uninstallClientDecorations(JRootPane root) {
        uninstallBorder(root);
        uninstallWindowListeners(root);
        setTitlePane(root, null);
        uninstallLayout(root);
        // We have to revalidate/repaint root if the style is JRootPane.NONE
        // only. When we needs to call revalidate/repaint with other styles
        // the installClientDecorations is always called after this method
        // imediatly and it will cause the revalidate/repaint at the proper
        // time.
        int style = root.getWindowDecorationStyle();
        if (style == JRootPane.NONE) {
            root.repaint();
            root.revalidate();
        }
// Reset the cursor, as we may have changed it to a resize cursor

        if (window != null) {
            window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        window = null;
    }

    /**
     * Invoked when a property changes on the root pane. If the event
     * indicates the <code>defaultButton</code> has changed, this will
     * reinstall the keyboard actions.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        String name = e.getPropertyName();

        JRootPane rootpane = (JRootPane) e.getSource();
        if (name.equals("Window.windowModified") || name.equals("windowModified")) {
            updateWindowModified(rootpane);
        } else if (name.equals("windowDecorationStyle")) {
            int style = root.getWindowDecorationStyle();

            // This is potentially more than needs to be done,
            // but it rarely happens and makes the install/uninstall process
            // simpler. MetalTitlePane also assumes it will be recreated if
            // the decoration style changes.
            uninstallClientDecorations(root);
            if (style != JRootPane.NONE) {
                installClientDecorations(root);
            }

        } else if (name.equals("JComponent.sizeVariant")) {
            QuaquaUtilities.applySizeVariant(rootpane);
        }






    }

    private static class RootPaneAncestorListener implements AncestorListener, WindowListener {

        public void ancestorAdded(AncestorEvent evt) {
            Container ancestor = evt.getAncestor();
            Window window = (ancestor instanceof Window)
                    ? (Window) ancestor
                    : SwingUtilities.getWindowAncestor(ancestor);
            if (window != null) {
                window.addWindowListener(this);
                updateWindowModified((JRootPane) evt.getSource());
                updateComponentTreeUIActivation(ancestor, window.isActive());

                if (UIManager.getBoolean("ColorChooser.unifiedTitleBar") &&
                        window.getClass().getName().equals("javax.swing.ColorChooserDialog")) {
                    ((JRootPane) evt.getSource()).putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
                }
            }
        }

        public void ancestorMoved(AncestorEvent evt) {
        }

        public void ancestorRemoved(AncestorEvent evt) {
            Container ancestorParent = evt.getAncestorParent();
            if (ancestorParent != null) {
                Window window = (ancestorParent instanceof Window)
                        ? (Window) ancestorParent
                        : SwingUtilities.getWindowAncestor(ancestorParent);
                //Window window = SwingUtilities.getWindowAncestor(ancestorParent);
                if (window != null) {
                    window.removeWindowListener(this);
                }
            }
        }

        public void windowActivated(WindowEvent e) {
            updateComponentTreeUIActivation(e.getComponent(), Boolean.TRUE);
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
            updateComponentTreeUIActivation(e.getComponent(), Boolean.FALSE);
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        private static void updateComponentTreeUIActivation(Component c, Boolean isActive) {
            if (c instanceof JComponent) {
                ((JComponent) c).putClientProperty("Frame.active", isActive);
            }
            Component[] children = null;
            if (c instanceof JMenu) {
                children = ((JMenu) c).getMenuComponents();
            } else if (c instanceof Container) {
                children = ((Container) c).getComponents();
            }
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    updateComponentTreeUIActivation(children[i], isActive);
                }
            }
        }
    }

    /**
     * A custom layout manager that is responsible for the layout of
     * layeredPane, glassPane, menuBar and titlePane, if one has been
     * installed.
     */
// NOTE: Ideally this would extends JRootPane.RootLayout, but that
//       would force this to be non-static.
    private static class QuaquaRootLayout implements LayoutManager2 {

        private boolean isVertical(Container parent) {
            if (parent instanceof JComponent) {
                return ((JComponent) parent).getClientProperty("Quaqua.RootPane.isVertical") == Boolean.TRUE;
            }
            return false;
        }

        /**
         * Returns the amount of space the layout would like to have.
         *
         * @param the Container for which this layout manager is being used
         * @return a Dimension object containing the layout's preferred size
         */
        public Dimension preferredLayoutSize(Container parent) {
            boolean isVertical = isVertical(parent);

            Dimension cpd, mbd, tpd;
            int cpWidth = 0;
            int cpHeight = 0;
            int mbWidth = 0;
            int mbHeight = 0;
            int tpWidth = 0;
            int tpHeight = 0;
            Insets i = parent.getInsets();
            JRootPane root = (JRootPane) parent;

            if (root.getContentPane() != null) {
                cpd = root.getContentPane().getPreferredSize();
            } else {
                cpd = root.getSize();
            }
            if (cpd != null) {
                cpWidth = cpd.width;
                cpHeight = cpd.height;
            }

            if (root.getJMenuBar() != null) {
                mbd = root.getJMenuBar().getPreferredSize();
                if (mbd != null) {
                    mbWidth = mbd.width;
                    mbHeight = mbd.height;
                }
            }

            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof QuaquaRootPaneUI)) {
                JComponent titlePane = ((QuaquaRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    tpd = titlePane.getPreferredSize();
                    if (tpd != null) {
                        tpWidth = tpd.width;
                        tpHeight = tpd.height;
                    }
                }
            }

            if (isVertical) {
                return new Dimension(
                        Math.max(cpWidth, mbWidth) + tpWidth + i.left + i.right,
                        Math.max(cpHeight + mbHeight, tpHeight) + i.top + i.bottom);
            } else {
                return new Dimension(Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
                        cpHeight + mbHeight + tpWidth + i.top + i.bottom);
            }
        }

        /**
         * Returns the minimum amount of space the layout needs.
         *
         * @param the Container for which this layout manager is being used
         * @return a Dimension object containing the layout's minimum size
         */
        public Dimension minimumLayoutSize(Container parent) {
            boolean isVertical = isVertical(parent);

            Dimension cpd, mbd, tpd;
            int cpWidth = 0;
            int cpHeight = 0;
            int mbWidth = 0;
            int mbHeight = 0;
            int tpWidth = 0;
            int tpHeight = 0;
            Insets i = parent.getInsets();
            JRootPane root = (JRootPane) parent;

            if (root.getContentPane() != null) {
                cpd = root.getContentPane().getMinimumSize();
            } else {
                cpd = root.getSize();
            }
            if (cpd != null) {
                cpWidth = cpd.width;
                cpHeight = cpd.height;
            }

            if (root.getJMenuBar() != null) {
                mbd = root.getJMenuBar().getMinimumSize();
                if (mbd != null) {
                    mbWidth = mbd.width;
                    mbHeight = mbd.height;
                }
            }
            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof QuaquaRootPaneUI)) {
                JComponent titlePane = ((QuaquaRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    tpd = titlePane.getMinimumSize();
                    if (tpd != null) {
                        tpWidth = tpd.width;
                        tpHeight = tpd.height;
                    }
                }
            }

            if (isVertical) {
                return new Dimension(
                        Math.max(cpWidth, mbWidth) + tpWidth + i.left + i.right,
                        Math.max(cpHeight + mbHeight, tpHeight) + i.top + i.bottom);
            } else {
                return new Dimension(Math.max(Math.max(cpWidth, mbWidth), tpWidth) + i.left + i.right,
                        cpHeight + mbHeight + tpWidth + i.top + i.bottom);
            }
        }

        /**
         * Returns the maximum amount of space the layout can use.
         *
         * @param the Container for which this layout manager is being used
         * @return a Dimension object containing the layout's maximum size
         */
        public Dimension maximumLayoutSize(Container target) {
            Dimension cpd, mbd, tpd;
            int cpWidth = Integer.MAX_VALUE;
            int cpHeight = Integer.MAX_VALUE;
            int mbWidth = Integer.MAX_VALUE;
            int mbHeight = Integer.MAX_VALUE;
            int tpWidth = Integer.MAX_VALUE;
            int tpHeight = Integer.MAX_VALUE;
            Insets i = target.getInsets();
            JRootPane root = (JRootPane) target;

            if (root.getContentPane() != null) {
                cpd = root.getContentPane().getMaximumSize();
                if (cpd != null) {
                    cpWidth = cpd.width;
                    cpHeight = cpd.height;
                }
            }

            if (root.getJMenuBar() != null) {
                mbd = root.getJMenuBar().getMaximumSize();
                if (mbd != null) {
                    mbWidth = mbd.width;
                    mbHeight = mbd.height;
                }
            }

            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof QuaquaRootPaneUI)) {
                JComponent titlePane = ((QuaquaRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    tpd = titlePane.getMaximumSize();
                    if (tpd != null) {
                        tpWidth = tpd.width;
                        tpHeight = tpd.height;
                    }
                }
            }

            int maxHeight = Math.max(Math.max(cpHeight, mbHeight), tpHeight);

            // Only overflows if 3 real non-MAX_VALUE heights, sum to > MAX_VALUE
            // Only will happen if sums to more than 2 billion units.  Not likely.
            if (maxHeight != Integer.MAX_VALUE) {
                maxHeight = cpHeight + mbHeight + tpHeight + i.top + i.bottom;
            }

            int maxWidth = Math.max(Math.max(cpWidth, mbWidth), tpWidth);
            // Similar overflow comment as above
            if (maxWidth != Integer.MAX_VALUE) {
                maxWidth += i.left + i.right;
            }

            return new Dimension(maxWidth, maxHeight);
        }

        /**
         * Instructs the layout manager to perform the layout for the specified
         * container.
         *
         * @param the Container for which this layout manager is being used
         */
        public void layoutContainer(Container parent) {
            boolean isVertical = isVertical(parent);

            JRootPane root = (JRootPane) parent;
            Rectangle b = root.getBounds();
            Insets i = root.getInsets();
            int nextY = 0;
            int nextX = 0;
            int w = b.width - i.right - i.left;
            int h = b.height - i.top - i.bottom;

            if (root.getLayeredPane() != null) {
                root.getLayeredPane().setBounds(i.left, i.top, w, h);
            }
            if (root.getGlassPane() != null) {
                root.getGlassPane().setBounds(i.left, i.top, w, h);
            }
            // Note: This is laying out the children in the layeredPane,
            // technically, these are not our children.
            if (root.getWindowDecorationStyle() != JRootPane.NONE &&
                    (root.getUI() instanceof QuaquaRootPaneUI)) {
                JComponent titlePane = ((QuaquaRootPaneUI) root.getUI()).getTitlePane();
                if (titlePane != null) {
                    Dimension tpd = titlePane.getPreferredSize();
                    if (tpd != null) {
                        if (isVertical) {
                            int tpWidth = tpd.width;
                            titlePane.setBounds(0, 0, tpWidth, h);
                            nextX += tpWidth;
                        } else {
                            int tpHeight = tpd.height;
                            titlePane.setBounds(0, 0, w, tpHeight);
                            nextY += tpHeight;
                        }
                    }
                }
            }
            if (root.getJMenuBar() != null) {
                Dimension mbd = root.getJMenuBar().getPreferredSize();
                root.getJMenuBar().setBounds(nextX, nextY, w - nextX, mbd.height);
                nextY += mbd.height;
            }
            if (root.getContentPane() != null) {
                //Dimension cpd = root.getContentPane().getPreferredSize();
                root.getContentPane().setBounds(nextX, nextY, w - nextX,
                        h < nextY ? 0 : h - nextY);
            }
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public void addLayoutComponent(Component comp, Object constraints) {
        }

        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }

        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }

        public void invalidateLayout(Container target) {
        }
    }

    /**
     * MouseInputHandler is responsible for handling resize/moving of
     * the Window. It sets the cursor directly on the Window when then
     * mouse moves over a hot spot.
     */
    private class MouseInputHandler implements MouseInputListener {

        /**
         * Set to true if the drag operation is moving the window.
         */
        private boolean isMovingWindow;
        /**
         * Used to determine the corner the resize is occuring from.
         */
        private int dragCursor;
        /**
         * X location the mouse went down on for a drag operation.
         */
        private int dragOffsetX;
        /**
         * Y location the mouse went down on for a drag operation.
         */
        private int dragOffsetY;
        /**
         * Width of the window when the drag started.
         */
        private int dragWidth;
        /**
         * Height of the window when the drag started.
         */
        private int dragHeight;
        /**
         * We cache the screen bounds here, so that we don't have to retrieve
         * them for each mouseDragged event. We clear the cache on mouseReleased.
         */
        private Rectangle cachedScreenBounds;

        public void mousePressed(MouseEvent ev) {
            JRootPane rootPane = getRootPane();

            if (rootPane.getWindowDecorationStyle() == JRootPane.NONE) {
                return;
            }
            Point dragWindowOffset = ev.getPoint();
            Window w = (Window) ev.getSource();
            if (w != null) {
                w.toFront();
            }
            Point convertedDragWindowOffset = SwingUtilities.convertPoint(
                    w, dragWindowOffset, getTitlePane());

            Frame f = null;
            Dialog d = null;

            if (w instanceof Frame) {
                f = (Frame) w;
            } else if (w instanceof Dialog) {
                d = (Dialog) w;
            }

            int frameState = (f != null) ? f.getExtendedState() : 0;

            if (getTitlePane() != null &&
                    getTitlePane().contains(convertedDragWindowOffset)) {
                if (f != null && ((frameState & Frame.MAXIMIZED_BOTH) == 0) || (d != null)) {
                    isMovingWindow = true;
                    dragOffsetX = dragWindowOffset.x;
                    dragOffsetY = dragWindowOffset.y;
                }
            } else if (f != null && f.isResizable() && ((frameState & Frame.MAXIMIZED_BOTH) == 0) || (d != null && d.isResizable())) {
                dragOffsetX = dragWindowOffset.x;
                dragOffsetY = dragWindowOffset.y;
                dragWidth = w.getWidth();
                dragHeight = w.getHeight();
                dragCursor =
                        (dragWindowOffset.x >= dragWidth - BORDER_DRAG_THICKNESS &&
                        dragWindowOffset.y >= dragHeight - BORDER_DRAG_THICKNESS) ? Cursor.SE_RESIZE_CURSOR : 0;
            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (dragCursor != 0 && window != null && !window.isValid()) {
                // Some Window systems validate as you resize, others won't,
                // thus the check for validity before repainting.
                window.validate();
                getRootPane().repaint();
            }
            isMovingWindow = false;
            dragCursor = 0;
            cachedScreenBounds = null;
        }

        public void mouseMoved(MouseEvent ev) {
        }

        private void adjust(Rectangle bounds, Dimension min, int deltaX,
                int deltaY, int deltaWidth, int deltaHeight) {
            bounds.x += deltaX;
            bounds.y += deltaY;
            bounds.width += deltaWidth;
            bounds.height += deltaHeight;
            if (min != null) {
                if (bounds.width < min.width) {
                    int correction = min.width - bounds.width;
                    if (deltaX != 0) {
                        bounds.x -= correction;
                    }
                    bounds.width = min.width;
                }
                if (bounds.height < min.height) {
                    int correction = min.height - bounds.height;
                    if (deltaY != 0) {
                        bounds.y -= correction;
                    }
                    bounds.height = min.height;
                }
            }
        }

        public void mouseDragged(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            Point pt = ev.getPoint();

            if (isMovingWindow) {
                // Sometimes we get mouse dragged events even when we are not
                // showing on screen (?)
                if (w.isShowing()) {
                    Point windowPt = w.getLocationOnScreen();

                    windowPt.x += pt.x - dragOffsetX;
                    windowPt.y += pt.y - dragOffsetY;

                    boolean isOnDefaultScreen =
                            w.getGraphicsConfiguration().getDevice() ==
                            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

                    // If an edge of the window is within the snap distance of the
                    // edge of another window, then align it to it.
                    // ----------------------------------------------------------
                    int snap = UIManager.getInt("RootPane.windowSnapDistance");
                    if (snap > 0 && (ev.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0) {
                        // Collects all bounds to which we want to snap to
                        LinkedList snapBounds;
                        // Collect window bounds
                        do {
                            snapBounds = new LinkedList();
                            try {
                                for (Iterator i = allRootPanes.keySet().iterator(); i.hasNext();) {
                                    JRootPane otherRootPane = (JRootPane) i.next();
                                    Window other = SwingUtilities.getWindowAncestor(otherRootPane);
                                    if (other != null && other.isShowing() && other != w) {
                                        snapBounds.add(other.getBounds());
                                    }
                                }
                            } catch (ConcurrentModificationException e) {
                                // allRootPanes is a WeakHashMap, thus iterating over
                                // it may fail sometimes because the garbage collector
                                // removes items in a worker thread.
                                snapBounds = null;
                            }
                        } while (snapBounds == null);

                        // Collect screen bounds
                        snapBounds.add(w.getGraphicsConfiguration().getBounds());
                        if (isOnDefaultScreen) {
                            Rectangle r = w.getGraphicsConfiguration().getBounds();
                            Insets insets = w.getToolkit().getScreenInsets(w.getGraphicsConfiguration());
                            r.x += insets.left;
                            r.y += insets.top;
                            r.width -= insets.left + insets.right;
                            r.height -= insets.top + insets.bottom;
                            snapBounds.add(r);
                        }

                        Dimension windowDim = w.getSize();
                        Rectangle windowRect = new Rectangle(windowPt.x, windowPt.y, windowDim.width, windowDim.height);
                        Rectangle snapper = new Rectangle();
                        for (Iterator i = snapBounds.iterator(); i.hasNext();) {
                            Rectangle r = (Rectangle) i.next();

                            snapper.setBounds(r);
                            snapper.grow(snap, snap);
                            if (snapper.intersects(windowRect)) {

                                if (windowPt.x > r.x - snap &&
                                        windowPt.x < r.x + snap) {
                                    // align my left edge to frame left edge
                                    windowPt.x = r.x;
                                } else if (windowPt.x > r.x + r.width - snap &&
                                        windowPt.x < r.x + r.width + snap) {
                                    // align my left edge to frame right edge
                                    windowPt.x = r.x + r.width;
                                } else if (windowPt.x + windowDim.width > r.x - snap &&
                                        windowPt.x + windowDim.width < r.x + snap) {
                                    // align my right edge to frame left edge
                                    windowPt.x = r.x - windowDim.width;
                                } else if (windowPt.x + windowDim.width > r.x + r.width - snap &&
                                        windowPt.x + windowDim.width < r.x + r.width + snap) {
                                    // align my right edge to frame right edge
                                    windowPt.x = r.x + r.width - windowDim.width;
                                }
                                if (windowPt.y > r.y - snap && windowPt.y < r.y + snap) {
                                    // align my top edge to frame top edge
                                    windowPt.y = r.y;
                                } else if (windowPt.y > r.y + r.height - snap &&
                                        windowPt.y < r.y + r.height + snap) {
                                    // align my top edge to frame bottom edge
                                    windowPt.y = r.y + r.height;
                                } else if (windowPt.y + windowDim.height > r.y - snap &&
                                        windowPt.y + windowDim.height < r.y + snap) {
                                    // align my bottom edge to frame top edge
                                    windowPt.y = r.y - windowDim.height;
                                } else if (windowPt.y + windowDim.height > r.y + r.height - snap &&
                                        windowPt.y + windowDim.height < r.y + r.height + snap) {
                                    // align my bottom edge to frame bottom edge
                                    windowPt.y = r.y + r.height - windowDim.height;
                                }
                            }
                        }
                    }
                    // Constrain windowPt in order to ensure that a portion of the
                    // title pane is always visible on screen
                    // ----------------------------------------------------------
                    // Get usable screen bounds
                    if (isOnDefaultScreen) {
                        if (cachedScreenBounds == null) {
                            cachedScreenBounds = w.getGraphicsConfiguration().getBounds();
                            Insets screenInsets = w.getToolkit().getScreenInsets(w.getGraphicsConfiguration());
                            cachedScreenBounds.x += screenInsets.left;
                            cachedScreenBounds.y += screenInsets.top;
                            cachedScreenBounds.width -= screenInsets.left + screenInsets.right;
                            cachedScreenBounds.height -= screenInsets.top + screenInsets.bottom;
                        }
                        Rectangle titlePaneBounds = getTitlePane().getBounds();
                        Dimension windowSize = window.getSize();

                        if (isVertical(getRootPane())) {
                            // For vertical title bar, title pane must be fully visible
                            // on x-axis, and at least 20 pixel on y-axis.
                            windowPt.x = Math.max(cachedScreenBounds.x + titlePaneBounds.x, windowPt.x);
                            windowPt.x = Math.min(cachedScreenBounds.x + cachedScreenBounds.width -
                                    titlePaneBounds.x - titlePaneBounds.width, windowPt.x);

                            windowPt.y = Math.max(cachedScreenBounds.y - windowSize.height + 20, windowPt.y);
                            windowPt.y = Math.min(cachedScreenBounds.y + cachedScreenBounds.height - 20, windowPt.y);

                        } else {
                            // For horizontal title bar, title pane must be fully visible
                            // on y-axis, and at least 20 pixel on x-axis.
                            windowPt.y = Math.max(cachedScreenBounds.y + titlePaneBounds.y, windowPt.y);
                            windowPt.y = Math.min(cachedScreenBounds.y + cachedScreenBounds.height -
                                    titlePaneBounds.y - titlePaneBounds.height, windowPt.y);

                            windowPt.x = Math.max(cachedScreenBounds.x - windowSize.width + 20, windowPt.x);
                            windowPt.x = Math.min(cachedScreenBounds.x + cachedScreenBounds.width - 20, windowPt.x);
                        }
                    }
                    w.setLocation(windowPt);
                }
            } else if (dragCursor != 0) {
                Rectangle r = w.getBounds();
                Rectangle startBounds = new Rectangle(r);
                Dimension min = w.getMinimumSize();

                switch (dragCursor) {
                    case Cursor.E_RESIZE_CURSOR:
                        adjust(r, min, 0, 0, pt.x + (dragWidth - dragOffsetX) -
                                r.width, 0);
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        adjust(r, min, 0, 0, 0, pt.y + (dragHeight - dragOffsetY) -
                                r.height);
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        adjust(r, min, 0, pt.y - dragOffsetY, 0,
                                -(pt.y - dragOffsetY));
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        adjust(r, min, pt.x - dragOffsetX, 0,
                                -(pt.x - dragOffsetX), 0);
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        adjust(r, min, 0, pt.y - dragOffsetY,
                                pt.x + (dragWidth - dragOffsetX) - r.width,
                                -(pt.y - dragOffsetY));
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        adjust(r, min, 0, 0,
                                pt.x + (dragWidth - dragOffsetX) - r.width,
                                pt.y + (dragHeight - dragOffsetY) -
                                r.height);
                        break;
                    case Cursor.NW_RESIZE_CURSOR:
                        adjust(r, min, pt.x - dragOffsetX,
                                pt.y - dragOffsetY,
                                -(pt.x - dragOffsetX),
                                -(pt.y - dragOffsetY));
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        adjust(r, min, pt.x - dragOffsetX, 0,
                                -(pt.x - dragOffsetX),
                                pt.y + (dragHeight - dragOffsetY) - r.height);
                        break;
                    default:
                        break;
                }
                if (!r.equals(startBounds)) {
                    w.setBounds(r);
                    // Defer repaint/validate on mouseReleased unless dynamic
                    // layout is active.
                    if (Toolkit.getDefaultToolkit().isDynamicLayoutActive()) {
                        w.validate();
                        getRootPane().repaint();
                    }
                }
            }
        }

        public void mouseEntered(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            lastCursor = w.getCursor();
            mouseMoved(ev);
        }

        public void mouseExited(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            w.setCursor(lastCursor);
        }

        public void mouseClicked(MouseEvent ev) {
            Window w = (Window) ev.getSource();
            Frame f = null;

            if (w instanceof Frame) {
                f = (Frame) w;
            } else {
                return;
            }

            Point convertedPoint = SwingUtilities.convertPoint(
                    w, ev.getPoint(), getTitlePane());

            int state = f.getExtendedState();
            if (getTitlePane() != null &&
                    getTitlePane().contains(convertedPoint)) {
                if ((ev.getClickCount() % 2) == 0 &&
                        ((ev.getModifiers() & InputEvent.BUTTON1_MASK) != 0)) {
                    if (f.isResizable()) {
                        if ((state & Frame.MAXIMIZED_BOTH) != 0) {
                            f.setExtendedState(state & ~Frame.MAXIMIZED_BOTH);
                        } else {
                            f.setExtendedState(state | Frame.MAXIMIZED_BOTH);
                        }
                        return;
                    }
                }
            }
        }
    }
}
