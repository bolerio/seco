/**
 * @(#)QuaquaPopupFactory.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * QuaquaPopupFactory to work around a bug with heavy weight popups
 * on Java 1.4 in full screen mode.
 * <p>
 * This class must not be used with J2SE 6 on OS X 10.6, because it causes
 * popups to appear behind modal dialog windows if one of the ancestor windows
 * has "isAlwaysOnTop" set to true.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaPopupFactory.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaPopupFactory extends PopupFactory {

    /**
     * Key used to indicate a light weight popup should be used.
     */
    static final int LIGHT_WEIGHT_POPUP = 0;
    /**
     * Key used to indicate a medium weight Popup should be used.
     */
    static final int MEDIUM_WEIGHT_POPUP = 1;

    /*
     * Key used to indicate a heavy weight Popup should be used.
     */
    static final int HEAVY_WEIGHT_POPUP = 2;
    /**
     * Default type of Popup to create.
     */
    private int popupType = HEAVY_WEIGHT_POPUP;

    /**
     * Returns the preferred type of Popup to create.
     */
    int getPopupType(Component owner) {
        if (owner instanceof JComponent) {
            JComponent c = (JComponent) owner;
            Float alpha = (Float) c.getClientProperty("Quaqua.PopupMenu.alpha");
            if (alpha == null) {
                alpha = new Float(0.75f);
            }
            if (alpha.floatValue() == 1f) {
                return HEAVY_WEIGHT_POPUP;
            }
        }
        return UIManager.getBoolean("PopupMenu.enableHeavyWeightPopup") ? HEAVY_WEIGHT_POPUP : MEDIUM_WEIGHT_POPUP;
    }

    /**
     * Returns the popup type to use for the specified parameters.
     */
    private int getPopupType(Component owner, Component contents,
            int ownerX, int ownerY) {
        return getPopupType(owner);
    }

    @Override
    public Popup getPopup(Component owner, Component contents,
            int x, int y) throws IllegalArgumentException {
        if (contents == null) {
            throw new IllegalArgumentException(
                    "Popup.getPopup must be passed non-null contents");
        }

        int pType = getPopupType(owner, contents, x, y);
        Popup popup = getPopup(owner, contents, x, y, pType);

        if (popup == null) {
            // Didn't fit, force to heavy.
            popup = getPopup(owner, contents, x, y, HEAVY_WEIGHT_POPUP);
        }
        return popup;
    }

    /**
     * Obtains the appropriate <code>Popup</code> based on
     * <code>popupType</code>.
     */
    private Popup getPopup(Component owner, Component contents,
            int ownerX, int ownerY, int popupType) {
        /*if (GraphicsEnvironment.isHeadless()) {
        return getHeadlessPopup(owner, contents, ownerX, ownerY);
        }*/
        switch (popupType) {
            case LIGHT_WEIGHT_POPUP:
                return getLightWeightPopup(owner, contents, ownerX, ownerY);
            case MEDIUM_WEIGHT_POPUP:
                return getMediumWeightPopup(owner, contents, ownerX, ownerY);
            case HEAVY_WEIGHT_POPUP:
                return getHeavyWeightPopup(owner, contents, ownerX, ownerY);
        }
        return getLightWeightPopup(owner, contents, ownerX, ownerY);
//       return null;
    }

    /**
     * Creates a light weight popup.
     */
    private Popup getLightWeightPopup(Component owner, Component contents,
            int ownerX, int ownerY) {
        return LightWeightPopup.getLightWeightPopup(owner, contents, ownerX,
                ownerY);
    }

    /**
     * Creates a medium weight popup.
     */
    private Popup getMediumWeightPopup(Component owner, Component contents,
            int ownerX, int ownerY) {
        return MediumWeightPopup.getMediumWeightPopup(owner, contents,
                ownerX, ownerY);
    }

    /**
     * Creates a heavy weight popup.
     */
    private Popup getHeavyWeightPopup(Component owner, Component contents,
            int ownerX, int ownerY) {
        return HeavyWeightPopup.getHeavyWeightPopup(owner, contents, ownerX,
                ownerY);
    }
    /**
     * Max number of items to store in any one particular cache.
     */
    private static final int MAX_CACHE_SIZE = 5;

    /**
     * ContainerPopup consolidates the common code used in the light/medium
     * weight implementations of <code>Popup</code>.
     */
    private static class ContainerPopup extends Popup {

        /** Component we are to be added to. */
        Component owner;
        /** Desired x location. */
        int x;
        /** Desired y location. */
        int y;
        // the component which represents the popup
        Component component;
        // the component which represents the contents
        Component contents;

        /**
         * Returns the <code>Component</code> returned from
         * <code>createComponent</code> that will hold the <code>Popup</code>.
         */
        Component getComponent() {
            if (component == null) {
                component = createComponent(owner);
            }
            return component;
        }

        Component createComponent(Component owner) {
            return null;
        }

        @Override
        public void hide() {
            Component c = getComponent();

            if (c != null) {
                Container parent = c.getParent();

                if (parent != null) {
                    Rectangle bounds = c.getBounds();

                    parent.remove(c);
                    parent.repaint(bounds.x, bounds.y, bounds.width,
                            bounds.height);
                }
            }
            owner = null;
        }

        public void pack() {
            Component c = getComponent();

            if (c != null) {
                c.setSize(c.getPreferredSize());
            }
        }

        void reset(Component owner, Component contents, int ownerX,
                int ownerY) {
            if ((owner instanceof JFrame) || (owner instanceof JDialog)
                    || (owner instanceof JWindow)) {
                // Force the content to be added to the layered pane, otherwise
                // we'll get an exception when adding to the RootPaneContainer.
                owner = ((RootPaneContainer) owner).getLayeredPane();
            }
////            super.reset(owner, contents, ownerX, ownerY);

            x = ownerX;
            y = ownerY;
            this.owner = owner;
            this.contents = contents;
        }

        boolean overlappedByOwnedWindow() {
            Component c = getComponent();
            if (owner != null && c != null) {
                Window w = SwingUtilities.getWindowAncestor(owner);
                if (w == null) {
                    return false;
                }
                Window[] ownedWindows = w.getOwnedWindows();
                if (ownedWindows != null) {
                    Rectangle bnd = c.getBounds();
                    for (int i = 0; i < ownedWindows.length; i++) {
                        Window owned = ownedWindows[i];
                        if (owned.isVisible()
                                && bnd.intersects(owned.getBounds())) {

                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Returns true if the Popup can fit on the screen.
         */
        boolean fitsOnScreen() {
            Component c = getComponent();

            if (owner != null && c != null) {
                Container parent;
                int width = c.getWidth();
                int height = c.getHeight();
                for (parent = owner.getParent(); parent != null;
                        parent = parent.getParent()) {
                    if (parent instanceof JFrame
                            || parent instanceof JDialog
                            || parent instanceof JWindow) {

                        Rectangle r = parent.getBounds();
                        Insets i = parent.getInsets();
                        r.x += i.left;
                        r.y += i.top;
                        r.width -= (i.left + i.right);
                        r.height -= (i.top + i.bottom);
                        return SwingUtilities.isRectangleContainingRectangle(
                                r, new Rectangle(x, y, width, height));

                    } else if (parent instanceof JApplet) {
                        Rectangle r = parent.getBounds();
                        Point p = parent.getLocationOnScreen();

                        r.x = p.x;
                        r.y = p.y;
                        return SwingUtilities.isRectangleContainingRectangle(
                                r, new Rectangle(x, y, width, height));
                    } else if (parent instanceof Window
                            || parent instanceof Applet) {
                        // No suitable swing component found
                        break;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Converts the location <code>x</code> <code>y</code> to the
     * parents coordinate system, returning the location.
     */
    static Point convertScreenLocationToParent(Container parent, int x, int y) {
        for (Container p = parent; p != null; p = p.getParent()) {
            if (p instanceof Window) {
                Point point = new Point(x, y);

                SwingUtilities.convertPointFromScreen(point, parent);
                return point;
            }
        }
        throw new Error("convertScreenLocationToParent: no window ancestor");
    }

    /**
     * Popup implementation that uses a JPanel as the popup.
     */
    private static class LightWeightPopup extends ContainerPopup {

        /**
         * Returns a light weight <code>Popup</code> implementation. If
         * the <code>Popup</code> needs more space that in available in
         * <code>owner</code>, this will return null.
         */
        static Popup getLightWeightPopup(Component owner, Component contents,
                int ownerX, int ownerY) {
            LightWeightPopup popup = new LightWeightPopup();
            popup.reset(owner, contents, ownerX, ownerY);
            /*
            if (!popup.fitsOnScreen() ||
            popup.overlappedByOwnedWindow()) {
            popup.hide();
            return null;
            }*/
            return popup;
        }

        //
        // Popup methods
        //
        @Override
        public void hide() {
            super.hide();

            Container c = (Container) getComponent();

            c.removeAll();
        }

        @Override
        public void show() {
            Container parent = null;

            if (owner != null) {
                parent = (owner instanceof Container ? (Container) owner : owner.getParent());
            }

            // Try to find a JLayeredPane and Window to add 
            for (Container p = parent; p != null; p = p.getParent()) {
                if (p instanceof JRootPane) {
                    if (p.getParent() instanceof JInternalFrame) {
                        continue;
                    }
                    parent = ((JRootPane) p).getLayeredPane();
                    // Continue, so that if there is a higher JRootPane, we'll
                    // pick it up.
                } else if (p instanceof Window) {
                    if (parent == null) {
                        parent = p;
                    }
                    break;
                } else if (p instanceof JApplet) {
                    // Painting code stops at Applets, we don't want
                    // to add to a Component above an Applet otherwise
                    // you'll never see it painted.
                    break;
                }
            }

            Point p = /*SwingUtilities.*/ convertScreenLocationToParent(parent, x,
                    y);
            Component c = getComponent();

            c.setLocation(p.x, p.y);
            if (parent instanceof JLayeredPane) {
                ((JLayeredPane) parent).add(c,
                        JLayeredPane.POPUP_LAYER, 0);
            } else {
                parent.add(c);
            }
        }

        @Override
        Component createComponent(Component owner) {
            JComponent c = new JPanel(new BorderLayout(), true);
            c.setBorder(new LineBorder(new Color(0xb2b2b2)));
            c.setOpaque(true);
            return c;
        }

        //
        // Local methods
        //
        /**
         * Resets the <code>Popup</code> to an initial state.
         */
        @Override
        void reset(Component owner, Component contents, int ownerX,
                int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);

            JComponent c = (JComponent) getComponent();

            c.setLocation(ownerX, ownerY);
            c.add(contents, BorderLayout.CENTER);
            contents.invalidate();
            pack();
        }
    }

    /**
     * Popup implementation that uses a Panel as the popup.
     */
    private static class MediumWeightPopup extends ContainerPopup {

        private static final Object mediumWeightPopupCacheKey =
                new StringBuffer("PopupFactory.mediumPopupCache");
        /** Child of the panel. The contents are added to this. */
        private JRootPane rootPane;

        /**
         * Returns a medium weight <code>Popup</code> implementation. If
         * the <code>Popup</code> needs more space that in available in
         * <code>owner</code>, this will return null.
         */
        static Popup getMediumWeightPopup(Component owner, Component contents,
                int ownerX, int ownerY) {
            MediumWeightPopup popup = new MediumWeightPopup();

            if (popup == null) {
                popup = new MediumWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            /*
            if (!popup.fitsOnScreen() ||
            popup.overlappedByOwnedWindow()) {
            popup.hide();
            return null;
            }*/
            return popup;
        }

        //
        // Popup
        //
        @Override
        public void hide() {
            super.hide();
            rootPane.getContentPane().removeAll();
        }

        @Override
        public void show() {
            Component c = getComponent();
            Container parent = null;

            if (owner != null) {
                parent = owner.getParent();
            }
            /*
            Find the top level window,
            if it has a layered pane,
            add to that, otherwise
            add to the window. */
            while (!(parent instanceof Window || parent instanceof Applet)
                    && (parent != null)) {
                parent = parent.getParent();
            }
            // Set the visibility to false before adding to workaround a
            // bug in Solaris in which the Popup gets added at the wrong
            // location, which will result in a mouseExit, which will then
            // result in the ToolTip being removed.
            if (parent instanceof RootPaneContainer) {
                parent = ((RootPaneContainer) parent).getLayeredPane();
                Point p = convertScreenLocationToParent(parent,
                        x, y);
                c.setVisible(false);
                c.setLocation(p.x, p.y);
                ((JLayeredPane) parent).add(c, JLayeredPane.POPUP_LAYER,
                        0);
            } else {
                Point p = convertScreenLocationToParent(parent,
                        x, y);

                c.setLocation(p.x, p.y);
                c.setVisible(false);
                if (parent != null) {
                    parent.add(c);
                }
            }
            c.setVisible(true);
        }

        @Override
        Component createComponent(Component owner) {
            Panel c = new Panel(new BorderLayout());

            rootPane = new JRootPane();
            // NOTE: this uses setOpaque vs LookAndFeel.installProperty as
            // there is NO reason for the RootPane not to be opaque. For
            // painting to work the contentPane must be opaque, therefor the
            // RootPane can also be opaque.
            // MacOSX back this change out because it causes problems
            // rootPane.setOpaque(true);
            c.add(rootPane, BorderLayout.CENTER);
            return c;
        }

        /**
         * Resets the <code>Popup</code> to an initial state.
         */
        @Override
        void reset(Component owner, Component contents, int ownerX,
                int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);

            Component c = getComponent();

            c.setLocation(ownerX, ownerY);
            rootPane.getContentPane().add(contents, BorderLayout.CENTER);
            contents.invalidate();
            c.validate();
            pack();
        }
    }

    /**
     * Popup implementation that uses a Window as the popup.
     */
    private static class HeavyWeightPopup extends ContainerPopup {

        /**
         * Returns either a new or recycled <code>Popup</code> containing
         * the specified children.
         */
        static Popup getHeavyWeightPopup(Component owner, Component contents,
                int ownerX, int ownerY) {
            HeavyWeightPopup popup = new HeavyWeightPopup();
            /*
            if (!popup.fitsOnScreen() ||
            popup.overlappedByOwnedWindow()) {
            popup.hide();
            return null;
            }*/
            boolean focusPopup = false;
            if (contents.isFocusable()) {
                if (contents instanceof JPopupMenu) {
                    JPopupMenu jpm = (JPopupMenu) contents;
                    Component popComps[] = jpm.getComponents();
                    for (int i = 0; i < popComps.length; i++) {
                        if (!(popComps[i] instanceof MenuElement)
                                && !(popComps[i] instanceof JSeparator)) {
                            focusPopup = true;
                            break;
                        }
                    }
                }
            }

            popup.reset(owner, contents, ownerX, ownerY);

            if (focusPopup) {
                JWindow wnd = (JWindow) ((HeavyWeightPopup) popup).getComponent();
                wnd.setFocusableWindowState(true);
                // Set window name. We need this in BasicPopupMenuUI
                // to identify focusable popup window.
                wnd.setName("###focusableSwingPopup###");
            }


            return popup;
        }

        @Override
        Component createComponent(Component owner) {
            final JWindow wnd;
            Component c = wnd = new JWindow(SwingUtilities.getWindowAncestor(owner));
            // Set window name. We need this in BasicPopupMenuUI
            // to identify focusable popup window.
            wnd.setName("###focusableSwingPopup###");
            wnd.getRootPane().putClientProperty("Window.shadow", Boolean.TRUE);
            wnd.setAlwaysOnTop(true);
            Float windowAlpha = new Float(0.948);
            if (contents instanceof JComponent) {
                Object value = ((JComponent) contents).getClientProperty(QuaquaPopupMenuUI.WINDOW_ALPHA_PROPERTY);
                if (value instanceof Float) {
                    windowAlpha = (Float) value;
                }
            }
            wnd.getRootPane().putClientProperty("Window.alpha", windowAlpha);
            try {
            wnd.setBackground(new Color(0xffffff, true));
            } catch (UnsupportedOperationException e) {
                // bail
            }
            wnd.addComponentListener(new ComponentListener() {

                private void updateShadow() {
                    Object oldValue = wnd.getRootPane().getClientProperty("apple.awt.windowShadow.revalidateNow");
                    wnd.getRootPane().putClientProperty("apple.awt.windowShadow.revalidateNow", (oldValue instanceof Integer) ? ((Integer) oldValue) + 1 : 1);
                }

                public void componentResized(ComponentEvent e) {
                    updateShadow();
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                    updateShadow();
                }

                public void componentHidden(ComponentEvent e) {
                }
            });
            return c;
        }

        @Override
        void reset(Component owner, Component contents, int ownerX,
                int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);

            Window window = (Window) getComponent();

            window.setLocation(ownerX, ownerY);
            window.add(contents, BorderLayout.CENTER);
            contents.invalidate();
            pack();
        }

        /**
         * Makes the <code>Popup</code> visible. If the <code>Popup</code> is
         * currently visible, this has no effect.
         */
        @Override
        public void show() {
            Component c = getComponent();

            if (c != null) {
                c.show();

            }
        }

        /**
         * Hides and disposes of the <code>Popup</code>. Once a <code>Popup</code>
         * has been disposed you should no longer invoke methods on it. A
         * <code>dispose</code>d <code>Popup</code> may be reclaimed and later used
         * based on the <code>PopupFactory</code>. As such, if you invoke methods
         * on a <code>disposed</code> <code>Popup</code>, indeterminate
         * behavior will result.
         */
        @Override
        public void hide() {
            Component c = getComponent();

            if (c instanceof JWindow) {
                c.hide();
                ((JWindow) c).getContentPane().removeAll();

            }
            dispose();
        }

        /**
         * Frees any resources the <code>Popup</code> may be holding onto.
         */
        void dispose() {
            Component c = getComponent();

            if (c instanceof JWindow) {
                ((Window) c).dispose();
                c = null;
            }
        }


    }
}
