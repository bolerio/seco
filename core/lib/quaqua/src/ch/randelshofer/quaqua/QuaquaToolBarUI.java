/*
 * @(#)QuaquaToolBarUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.border.BackgroundBorderUIResource;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.util.*;
import javax.swing.event.MouseInputListener;

/**
 * QuaquaToolBarUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaToolBarUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaToolBarUI extends BasicToolBarUI {
    // Rollover button implementation.

    private final static String IS_ROLLOVER = "JToolBar.isRollover";
    /*private*/ final static String TOOLBAR_DRAW_DIVIDER_PROPERTY = "Quaqua.ToolBar.isDividerDrawn";
    public final static String TOOLBAR_STYLE_PROPERTY = "Quaqua.ToolBar.style";
    private static Border rolloverBorder;
    private static Border nonRolloverBorder;
    private static Border nonRolloverToggleBorder;
    private HashMap borderTable = new HashMap();
    private Hashtable rolloverTable = new Hashtable();
    private Handler handler;
    private Point dragWindowOffset = null;
    private Container dockingSource;
    private boolean floating;
    private RootPaneContainer floatingToolBar;
    private int floatingX;
    private int floatingY;
    protected DragWindow0 dragWindow0;
    private int dockingSensitivity = 0;

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaToolBarUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        // Initialize instance vars
        dockingSensitivity = 0;
        floating = false;
        floatingX = floatingY = 0;
        floatingToolBar = null;
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        dragWindow0 = null;
        dockingSource = null;
        floatingToolBar = null;
    }

    @Override
    protected void installDefaults() {
        if (rolloverBorder == null) {
            rolloverBorder = createRolloverBorder();
        }
        if (nonRolloverBorder == null) {
            nonRolloverBorder = createNonRolloverBorder();
        }
        if (nonRolloverToggleBorder == null) {
            nonRolloverToggleBorder = createNonRolloverToggleBorder();
        }
        super.installDefaults();

        // The toolbar is not opaque, because its background color may have
        // an alpha channel.
        QuaquaUtilities.installProperty(toolBar, "opaque", UIManager.get("ToolBar.opaque"));
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Override
    protected ContainerListener createToolBarContListener() {
        return getHandler();
    }

    @Override
    protected FocusListener createToolBarFocusListener() {
        return getHandler();
    }

    @Override
    protected PropertyChangeListener createPropertyListener() {
        return getHandler();
    }

    @Override
    protected MouseInputListener createDockingListener() {
        getHandler().tb = toolBar;
        return getHandler();
    }

    @Override
    protected WindowListener createFrameListener() {
        return new FrameListener();
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        if (c.isOpaque()) {
            Graphics2D g = (Graphics2D) gr;
            if (c.getBorder() instanceof BackgroundBorder) {
                Border bb = ((BackgroundBorder) c.getBorder()).getBackgroundBorder();
                bb.paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
            } else {
                g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }
        }
    }

    /**
     * Paints the contents of the window used for dragging.
     *
     * @param g Graphics to paint to.
     * @throws NullPointerException is <code>g</code> is null
     * @since 1.5
     */
    @Override
    protected void paintDragWindow(Graphics g) {
        int w = dragWindow0.getWidth();
        int h = dragWindow0.getHeight();

        g.setColor(dragWindow0.getBackground());
        g.fillRect(0, 0, w, h);

        Graphics g2 = g.create();
        toolBar.paint(g2);
        g2.dispose();

        g.setColor(dragWindow0.getBorderColor());
        g.drawRect(0, 0, w - 1, h - 1);
    }

    /**
     * Creates a window which contains the toolbar after it has been
     * dragged out from its container
     * @return a <code>RootPaneContainer</code> object, containing the toolbar.
     */
    @Override
    protected RootPaneContainer createFloatingWindow(JToolBar toolbar) {
        class ToolBarDialog extends JDialog {

            public ToolBarDialog(Frame owner, String title, boolean modal) {
                super(owner, title, modal);
            }

            public ToolBarDialog(Dialog owner, String title, boolean modal) {
                super(owner, title, modal);
            }

            // Override createRootPane() to automatically resize
            // the frame when contents change
            @Override
            protected JRootPane createRootPane() {
                JRootPane rootPane = new JRootPane() {

                    private boolean packing = false;

                    @Override
                    public void validate() {
                        putClientProperty(
                                "Quaqua.RootPane.isVertical",
                                toolBar.getOrientation() == JToolBar.VERTICAL ? Boolean.FALSE : Boolean.TRUE);
                        super.validate();
                        if (!packing) {
                            packing = true;
                            pack();
                            packing = false;
                        }
                    }
                };
                rootPane.setFont(UIManager.getFont("ToolBar.titleFont"));
                rootPane.putClientProperty("Quaqua.RootPane.isPalette", Boolean.TRUE);
                //rootPane.setOpaque(true);
                QuaquaUtilities.installProperty(rootPane, "opaque", Boolean.TRUE);
                return rootPane;
            }
        }

        JDialog dialog;
        Window window = SwingUtilities.getWindowAncestor(toolbar);
        if (window instanceof Frame) {
            dialog = new ToolBarDialog((Frame) window, toolbar.getName(), false);
        } else if (window instanceof Dialog) {
            dialog = new ToolBarDialog((Dialog) window, toolbar.getName(), false);
        } else {
            dialog = new ToolBarDialog((Frame) null, toolbar.getName(), false);
        }

        try {
            Methods.invoke(dialog, "setUndecorated", true);
            Methods.invoke(dialog.getRootPane(), "setWindowDecorationStyle", 1);//JRootPane.FRAME);
        } catch (NoSuchMethodException e) {
            // Empty
        }

        dialog.setTitle(toolbar.getName());
        dialog.setResizable(false);
        WindowListener wl = createFrameListener();
        dialog.addWindowListener(wl);

        //RootPaneContainer dialog = super.createFloatingWindow(toolbar);
        dialog.getRootPane().putClientProperty("JDialog.isPalette", Boolean.TRUE);
        dialog.getRootPane().putClientProperty("JFrame.isPalette", Boolean.TRUE);
        dialog.getRootPane().putClientProperty("Dialog.isPalette", Boolean.TRUE);
        dialog.getRootPane().putClientProperty("Frame.isPalette", Boolean.TRUE);
        dialog.getRootPane().putClientProperty("JWindow.isPalette", Boolean.TRUE);
        dialog.getRootPane().putClientProperty("Window.isPalette", Boolean.TRUE);

        return dialog;
    }

    /**
     * Creates a rollover border for toolbar components. The 
     * rollover border will be installed if rollover borders are 
     * enabled. 
     * <p>
     * Override this method to provide an alternate rollover border.
     *
     * @since 1.4
     */
    @Override
    protected Border createRolloverBorder() {
        return UIManager.getBorder("Button.border");
    }

    /**
     * Creates the non rollover border for toolbar components. This
     * border will be installed as the border for components added
     * to the toolbar if rollover borders are not enabled.
     * <p>
     * Override this method to provide an alternate rollover border.
     *
     * @since 1.4
     */
    @Override
    protected Border createNonRolloverBorder() {
        return UIManager.getBorder("Button.border");
    }

    /**
     * Creates a non rollover border for Toggle buttons in the toolbar.
     */
    private Border createNonRolloverToggleBorder() {
        return UIManager.getBorder("Button.border");
    }

    protected DragWindow0 createDragWindow0(JToolBar toolbar) {
        Window frame = null;
        if (toolBar != null) {
            Container p;
            for (p = toolBar.getParent(); p != null && !(p instanceof Window);
                    p = p.getParent());
            if (p != null) {
                frame = (Window) p;
            }
        }
        if (floatingToolBar == null) {
            floatingToolBar = createFloatingWindow(toolBar);
        }
        if (floatingToolBar instanceof Window) {
            frame = (Window) floatingToolBar;
        }
        DragWindow0 dragW = new DragWindow0(frame);

        JRootPane rp = dragW.getRootPane();
        rp.putClientProperty("Window.alpha", new Float(0.5f));

        return dragW;
    }

    /**
     * Sets the border of the component to have a rollover border which
     * was created by <code>createRolloverBorder</code>. 
     *
     * @param c component which will have a rollover border installed 
     * @see #createRolloverBorder
     * @since 1.4
     */
    @Override
    protected void setBorderToRollover(Component c) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;

            Border border = (Border) borderTable.get(b);
            if (border == null || border instanceof UIResource) {
                borderTable.put(b, b.getBorder());
            }

            // Only set the border if its the default border
            if (b.getBorder() instanceof UIResource) {
                b.setBorder(rolloverBorder);
            }

            rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE : Boolean.FALSE);
            b.setRolloverEnabled(true);
        }
    }

    /**
     * Sets the border of the component to have a non-rollover border which
     * was created by <code>createNonRolloverBorder</code>. 
     *
     * @param c component which will have a non-rollover border installed 
     * @see #createNonRolloverBorder
     * @since 1.4
     */
    @Override
    protected void setBorderToNonRollover(Component c) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;

            Border border = (Border) borderTable.get(b);
            if (border == null || border instanceof UIResource) {
                borderTable.put(b, b.getBorder());
            }

            // Only set the border if its the default border
            if (b.getBorder() instanceof UIResource) {
                if (b instanceof JToggleButton) {
                    ((JToggleButton) b).setBorder(nonRolloverToggleBorder);
                } else {
                    b.setBorder(nonRolloverBorder);
                }
            }
            rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE : Boolean.FALSE);
            b.setRolloverEnabled(false);
        }
    }

    /**
     * Sets the border of the component to have a normal border.
     * A normal border is the original border that was installed on the child
     * component before it was added to the toolbar.
     *
     * @param c component which will have a normal border re-installed 
     * @see #createNonRolloverBorder
     * @since 1.4
     */
    @Override
    protected void setBorderToNormal(Component c) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;

            Border border = (Border) borderTable.remove(b);
            b.setBorder(border);

            Boolean value = (Boolean) rolloverTable.remove(b);
            if (value != null) {
                b.setRolloverEnabled(value.booleanValue());
            }
        }
    }

    @Override
    public void setFloatingLocation(int x, int y) {
        floatingX = x;
        floatingY = y;
    }

    @Override
    public boolean isFloating() {
        return floating;
    }

    @Override
    public void setFloating(boolean b, Point p) {
        if (toolBar.isFloatable() == true) {
            if (dragWindow0 != null) {
                dragWindow0.setVisible(false);
            }
            this.floating = b;
            if (b == true) {
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                    dockingSource.remove(toolBar);
                }
                constraintBeforeFloating = calculateConstraint();
                if (propertyListener != null) {
                    UIManager.addPropertyChangeListener(propertyListener);
                }
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                floatingToolBar.getContentPane().add(toolBar, BorderLayout.CENTER);
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).pack();
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setLocation(floatingX, floatingY);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(true);
                }
            } else {
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(false);
                }
                floatingToolBar.getContentPane().remove(toolBar);
                String constraint = getDockingConstraint(dockingSource,
                        p);
                if (constraint == null) {
                    constraint = BorderLayout.NORTH;
                }
                int orientation = mapConstraintToOrientation(constraint);
                setOrientation(orientation);
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                if (propertyListener != null) {
                    UIManager.removePropertyChangeListener(propertyListener);
                }
                dockingSource.add(constraint, toolBar);
            }
            dockingSource.invalidate();
            Container dockingSourceParent = dockingSource.getParent();
            if (dockingSourceParent != null) {
                dockingSourceParent.validate();
            }
            dockingSource.repaint();
        }
    }

    private String getDockingConstraint(Component c, Point p) {
        if (p == null) {
            return constraintBeforeFloating;
        }
        if (c.contains(p)) {
            dockingSensitivity = (toolBar.getOrientation() == JToolBar.HORIZONTAL)
                    ? toolBar.getSize().height
                    : toolBar.getSize().width;
            // North  (Base distance on height for now!)
            if (p.y < dockingSensitivity && !isBlocked(c, BorderLayout.NORTH)) {
                return BorderLayout.NORTH;
            }
            // East  (Base distance on height for now!)
            if (p.x >= c.getWidth() - dockingSensitivity && !isBlocked(c, BorderLayout.EAST)) {
                return BorderLayout.EAST;
            }
            // West  (Base distance on height for now!)
            if (p.x < dockingSensitivity && !isBlocked(c, BorderLayout.WEST)) {
                return BorderLayout.WEST;
            }
            if (p.y >= c.getHeight() - dockingSensitivity && !isBlocked(c, BorderLayout.SOUTH)) {
                return BorderLayout.SOUTH;
            }
        }
        return null;
    }

    private boolean isBlocked(Component comp, Object constraint) {
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            LayoutManager lm = cont.getLayout();
            if (lm instanceof BorderLayout) {
                BorderLayout blm = (BorderLayout) lm;
                Component c = null;
                try {
                    c = (Component) Methods.invoke(blm, "getLayoutComponent", new Class[]{Container.class, Object.class}, new Object[]{cont, constraint});
                } catch (Throwable ex) {
                    //ex.printStackTrace();
                }
                return (c != null && c != toolBar);
            }
        }
        return false;
    }

    @Override
    protected void dragTo(Point position, Point origin) {
        if (toolBar.isFloatable() == true) {
            try {
                if (dragWindow0 == null) {
                    dragWindow0 = createDragWindow0(toolBar);
                }
                Point offset = dragWindow0.getOffset();
                if (offset == null) {
                    Dimension size = toolBar.getPreferredSize();
                    offset = new Point(size.width / 2, size.height / 2);
                    dragWindow0.setOffset(offset);
                }
                Point global = new Point(origin.x + position.x,
                        origin.y + position.y);
                Point dragPoint = new Point(global.x - offset.x,
                        global.y - offset.y);
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                constraintBeforeFloating = calculateConstraint();
                Point dockingPosition = dockingSource.getLocationOnScreen();
                Point comparisonPoint = new Point(global.x - dockingPosition.x,
                        global.y - dockingPosition.y);
                if (canDock(dockingSource, comparisonPoint)) {
                    dragWindow0.setBackground(getDockingColor());
                    String constraint = getDockingConstraint(dockingSource,
                            comparisonPoint);
                    int orientation = mapConstraintToOrientation(constraint);
                    dragWindow0.setOrientation(orientation);
                    dragWindow0.setBorderColor(dockingBorderColor);
                } else {
                    dragWindow0.setBackground(getFloatingColor());
                    dragWindow0.setBorderColor(floatingBorderColor);
                }

                dragWindow0.setLocation(dragPoint.x, dragPoint.y);
                if (dragWindow0.isVisible() == false) {
                    Dimension size = toolBar.getPreferredSize();
                    dragWindow0.setSize(size.width, size.height);
                    dragWindow0.setVisible(true);
                }
            } catch (IllegalComponentStateException e) {
            }
        } else if (isDragMovesWindow()) {
            // Dragging the unified toolbar drags the window

            Window ancestorWindow = SwingUtilities.getWindowAncestor(toolBar);
            if (ancestorWindow != null) {
                if (dragWindowOffset == null) {
                    dragWindowOffset = new Point(position);
                }

                Point loc = ancestorWindow.getLocation();

                ancestorWindow.setLocation(loc.x + position.x - dragWindowOffset.x, loc.y + position.y - dragWindowOffset.y);
            }
        }
    }

    @Override
    protected void floatAt(Point position, Point origin) {
        if (toolBar.isFloatable() == true) {
            try {
                Point offset = dragWindow0.getOffset();
                if (offset == null) {
                    offset = position;
                    dragWindow0.setOffset(offset);
                }
                Point global = new Point(origin.x + position.x,
                        origin.y + position.y);
                setFloatingLocation(global.x - offset.x,
                        global.y - offset.y);
                if (dockingSource != null) {
                    Point dockingPosition = dockingSource.getLocationOnScreen();
                    Point comparisonPoint = new Point(global.x - dockingPosition.x,
                            global.y - dockingPosition.y);
                    if (canDock(dockingSource, comparisonPoint)) {
                        setFloating(false, comparisonPoint);
                    } else {
                        setFloating(true, null);
                    }
                } else {
                    setFloating(true, null);
                }
                dragWindow0.setOffset(null);
            } catch (IllegalComponentStateException e) {
            }
        }
    }

    private int mapConstraintToOrientation(String constraint) {
        int orientation = toolBar.getOrientation();

        if (constraint != null) {
            if (constraint.equals(BorderLayout.EAST) || constraint.equals(BorderLayout.WEST)) {
                orientation = JToolBar.VERTICAL;
            } else if (constraint.equals(BorderLayout.NORTH) || constraint.equals(BorderLayout.SOUTH)) {
                orientation = JToolBar.HORIZONTAL;
            }
        }

        return orientation;
    }

    @Override
    public void setOrientation(int orientation) {
        toolBar.setOrientation(orientation);

        if (dragWindow0 != null) {
            dragWindow0.setOrientation(orientation);
        }
    }

    private String calculateConstraint() {
        String constraint = null;
        LayoutManager lm = dockingSource.getLayout();
        if (lm instanceof BorderLayout) {
            BorderLayout bl = (BorderLayout) lm;
            try {
                constraint = (String) Methods.invoke(bl, "getConstraints", new Class[]{Component.class}, new Object[]{toolBar});
            } catch (Throwable ex) {
                // Suppress silently
                //ex.printStackTrace();
            }

        }
        return (constraint != null) ? constraint : constraintBeforeFloating;
    }

    /** Returns true, if the parent window shall be moved, when the mouse
     * is dragged over the toolbar.
     */
    private boolean isDragMovesWindow() {
        Object toolBarStyle = toolBar.getClientProperty(TOOLBAR_STYLE_PROPERTY);
        if (toolBarStyle == null) {
            JRootPane rootPane = SwingUtilities.getRootPane(toolBar);
            int xOffset = 0, yOffset = 0;
            for (Component c = toolBar; c != rootPane; c = c.getParent()) {
                xOffset += c.getX();
                yOffset += c.getY();
            }
            toolBarStyle = (yOffset == 0) ? "title" : "plain";
        }

        if (UIManager.getBoolean("ToolBar.textured.dragMovesWindow")) {
            boolean isTextured = QuaquaUtilities.isOnTexturedWindow(toolBar);
            return (isTextured && (toolBarStyle.equals("title")) || toolBarStyle.equals("bottom"));
        } else {
            return false;
        }
    }

    private class Handler implements ContainerListener,
            FocusListener, MouseInputListener, PropertyChangeListener {

        //
        // ContainerListener
        //
        public void componentAdded(ContainerEvent evt) {
            Component c = evt.getChild();

            if (toolBarFocusListener != null) {
                c.addFocusListener(toolBarFocusListener);
            }

            if (isRolloverBorders()) {
                setBorderToRollover(c);
            } else {
                setBorderToNonRollover(c);
            }
        }

        public void componentRemoved(ContainerEvent evt) {
            Component c = evt.getChild();

            if (toolBarFocusListener != null) {
                c.removeFocusListener(toolBarFocusListener);
            }

            // Revert the button border
            setBorderToNormal(c);
        }

        //
        // FocusListener
        //
        public void focusGained(FocusEvent evt) {
            Component c = evt.getComponent();
            focusedCompIndex = toolBar.getComponentIndex(c);
        }

        public void focusLost(FocusEvent evt) {
        }
        //
        // MouseInputListener (DockingListener)
        //
        JToolBar tb;
        boolean isDragging = false;
        Point origin = null;

        public void mousePressed(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            isDragging = false;
            dragWindowOffset = null;
        }

        public void mouseReleased(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            if (isDragging == true) {
                Point position = evt.getPoint();
                if (origin == null) {
                    origin = evt.getComponent().getLocationOnScreen();
                }
                floatAt(position, origin);
            }
            origin = null;
            isDragging = false;
            dragWindowOffset = null;
        }

        public void mouseDragged(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            isDragging = true;
            Point position = evt.getPoint();
            if (origin == null) {
                origin = evt.getComponent().getLocationOnScreen();
            }
            dragTo(position, origin);
        }

        public void mouseClicked(MouseEvent evt) {
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }

        public void mouseMoved(MouseEvent evt) {
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName != null && propertyName.equals("Frame.active")) {
                toolBar.repaint();
            } else if (propertyName != null && propertyName.equals("lookAndFeel")) {
                toolBar.updateUI();
            } else if (propertyName != null && propertyName.equals("orientation")) {
                // Search for JSeparator components and change it's orientation
                // to match the toolbar and flip it's orientation.
                Component[] components = toolBar.getComponents();
                int orientation = ((Integer) evt.getNewValue()).intValue();
                JToolBar.Separator separator;

                for (int i = 0; i < components.length; ++i) {
                    if (components[i] instanceof JToolBar.Separator) {
                        separator = (JToolBar.Separator) components[i];
                        if ((orientation == JToolBar.HORIZONTAL)) {
                            separator.setOrientation(JSeparator.VERTICAL);
                        } else {
                            separator.setOrientation(JSeparator.HORIZONTAL);
                        }
                        Dimension size = separator.getSeparatorSize();
                        if (size != null && size.width != size.height) {
                            // Flip the orientation.
                            Dimension newSize =
                                    new Dimension(size.height, size.width);
                            separator.setSeparatorSize(newSize);
                        }
                    }
                }
            } else if (propertyName != null && propertyName.equals(IS_ROLLOVER)) {
                installNormalBorders(toolBar);
                setRolloverBorders(((Boolean) evt.getNewValue()).booleanValue());
            }
        }
    }

    protected class FrameListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent w) {
            if (toolBar.isFloatable() == true) {
                if (dragWindow0 != null) {
                    dragWindow0.setVisible(false);
                }
                floating = false;
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(false);
                }
                floatingToolBar.getContentPane().remove(toolBar);
                String constraint = constraintBeforeFloating;
                if (toolBar.getOrientation() == JToolBar.HORIZONTAL) {
                    if (constraint == "West" || constraint == "East") {
                        constraint = "North";
                    }
                } else {
                    if (constraint == "North" || constraint == "South") {
                        constraint = "West";
                    }
                }
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                if (propertyListener != null) {
                    UIManager.removePropertyChangeListener(propertyListener);
                }
                dockingSource.add(toolBar, constraint);
                dockingSource.invalidate();
                Container dockingSourceParent = dockingSource.getParent();
                if (dockingSourceParent != null) {
                    dockingSourceParent.validate();
                }
                dockingSource.repaint();
            }
        }
    }

    protected class DragWindow0 extends JWindow {

        Color borderColor = Color.gray;
        int orientation = toolBar.getOrientation();
        Point offset; // offset of the mouse cursor inside the DragWindow

        DragWindow0(Window w) {
            super(w);
        }

        public void setOrientation(int o) {
            if (isShowing()) {
                if (o == this.orientation) {
                    return;
                }
                this.orientation = o;
                Dimension size = getSize();
                setSize(new Dimension(size.height, size.width));
                if (offset != null) {
                    if (QuaquaUtilities.isLeftToRight(toolBar)) {
                        setOffset(new Point(offset.y, offset.x));
                    } else if (o == JToolBar.HORIZONTAL) {
                        setOffset(new Point(size.height - offset.y, offset.x));
                    } else {
                        setOffset(new Point(offset.y, size.width - offset.x));
                    }
                }
                repaint();
            }
        }

        public Point getOffset() {
            return offset;
        }

        public void setOffset(Point p) {
            this.offset = p;
        }

        public void setBorderColor(Color c) {
            if (this.borderColor == c) {
                return;
            }
            this.borderColor = c;
            repaint();
        }

        public Color getBorderColor() {
            return this.borderColor;
        }

        @Override
        public void paint(Graphics g) {
            paintDragWindow(g);
            // Paint the children
            super.paint(g);
        }

        @Override
        public Insets getInsets() {
            return new Insets(1, 1, 1, 1);
        }
    }
}
