/*
 * @(#)QuaquaScrollPaneUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * QuaquaScrollPaneUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaScrollPaneUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaScrollPaneUI extends BasicScrollPaneUI implements VisuallyLayoutable {
    //private HierarchyListener hierarchyListener;

    private Handler handler;
    /**
     * State flag that shows whether setValue() was called from a user program
     * before the value of "extent" was set in right-to-left component
     * orientation.
     */
    private boolean setValueCalled = false;
    /**
     * PropertyChangeListener installed on the vertical scrollbar.
     */
    private PropertyChangeListener vsbPropertyChangeListener;
    /**
     * PropertyChangeListener installed on the horizontal scrollbar.
     */
    private PropertyChangeListener hsbPropertyChangeListener;
    private MouseWheelListener mouseScrollListener;

    /** Creates a new instance. */
    public QuaquaScrollPaneUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaScrollPaneUI();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    @Override
    protected void installDefaults(JScrollPane scrollpane) {
        super.installDefaults(scrollpane);
        if (scrollpane.getLayout() instanceof UIResource) {
            ScrollPaneLayout layout = new QuaquaScrollPaneLayout.UIResource();
            scrollpane.setLayout(layout);
            layout.syncWithScrollPane(scrollpane);
        }
        if (QuaquaManager.getProperty("java.version").startsWith("1.5")) {
            scrollpane.setOpaque(UIManager.getBoolean("ScrollPane.opaque"));
        } else {
            QuaquaUtilities.installProperty(scrollpane, "opaque", UIManager.get("ScrollPane.opaque"));
        }
        scrollpane.setFocusable(UIManager.getBoolean("ScrollPane.focusable"));
    }

    @Override
    protected void uninstallDefaults(JScrollPane scrollpane) {
        super.uninstallDefaults(scrollpane);
        if (scrollpane.getLayout() instanceof UIResource) {
            ScrollPaneLayout layout = new ScrollPaneLayout.UIResource();
            scrollpane.setLayout(layout);
            layout.syncWithScrollPane(scrollpane);
        }
    }

    @Override
    protected void installListeners(JScrollPane c) {
        vsbChangeListener = createVSBChangeListener();
        vsbPropertyChangeListener = createVSBPropertyChangeListener();
        hsbChangeListener = createHSBChangeListener();
        hsbPropertyChangeListener = createHSBPropertyChangeListener();
        viewportChangeListener = createViewportChangeListener();
        spPropertyChangeListener = createPropertyChangeListener();

        JViewport viewport = scrollpane.getViewport();
        JScrollBar vsb = scrollpane.getVerticalScrollBar();
        JScrollBar hsb = scrollpane.getHorizontalScrollBar();

        if (viewport != null) {
            viewport.addChangeListener(viewportChangeListener);
        }
        if (vsb != null) {
            vsb.getModel().addChangeListener(vsbChangeListener);
            vsb.addPropertyChangeListener(vsbPropertyChangeListener);
        }
        if (hsb != null) {
            hsb.getModel().addChangeListener(hsbChangeListener);
            hsb.addPropertyChangeListener(hsbPropertyChangeListener);
        }

        scrollpane.addPropertyChangeListener(spPropertyChangeListener);

        mouseScrollListener = createMouseWheelListener();
        scrollpane.addMouseWheelListener(mouseScrollListener);

    }

    @Override
    protected void uninstallListeners(JComponent c) {
        JViewport viewport = scrollpane.getViewport();
        JScrollBar vsb = scrollpane.getVerticalScrollBar();
        JScrollBar hsb = scrollpane.getHorizontalScrollBar();

        if (viewport != null) {
            viewport.removeChangeListener(viewportChangeListener);
        }
        if (vsb != null) {
            vsb.getModel().removeChangeListener(vsbChangeListener);
            vsb.removePropertyChangeListener(vsbPropertyChangeListener);
        }
        if (hsb != null) {
            hsb.getModel().removeChangeListener(hsbChangeListener);
            hsb.removePropertyChangeListener(hsbPropertyChangeListener);
        }

        scrollpane.removePropertyChangeListener(spPropertyChangeListener);

        if (mouseScrollListener != null) {
            scrollpane.removeMouseWheelListener(mouseScrollListener);
        }

        vsbChangeListener = null;
        hsbChangeListener = null;
        viewportChangeListener = null;
        spPropertyChangeListener = null;
        mouseScrollListener = null;
        handler = null;
    }

    /**
     * Creates an instance of MouseWheelListener, which is added to the
     * JScrollPane by installUI().  The returned MouseWheelListener is used
     * to handle mouse wheel-driven scrolling.
     *
     * @return      MouseWheelListener which implements wheel-driven scrolling
     */
    @Override
    protected MouseWheelListener createMouseWheelListener() {
        return getHandler();
    }

    /**
     * Returns a <code>PropertyChangeListener</code> that will be installed
     * on the vertical <code>JScrollBar</code>.
     */
    private PropertyChangeListener createVSBPropertyChangeListener() {
        return getHandler();
    }

    /**
     * Returns a <code>PropertyChangeListener</code> that will be installed
     * on the horizontal <code>JScrollBar</code>.
     */
    private PropertyChangeListener createHSBPropertyChangeListener() {
        return getHandler();
    }

    @Override
    protected ChangeListener createVSBChangeListener() {
        return getHandler();
    }

    @Override
    protected ChangeListener createHSBChangeListener() {
        return getHandler();
    }

    @Override
    protected ChangeListener createViewportChangeListener() {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    public Insets getVisualMargin(Component c) {
        Insets margin = (Insets) ((JComponent) c).getClientProperty("Quaqua.Component.visualMargin");
        if (margin == null) {
            margin = UIManager.getInsets("Component.visualMargin");
        }
        return (margin == null) ? new Insets(0, 0, 0, 0) : margin;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Debug.paint(g, c, this);
    }

    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            Insets margin = getVisualMargin(c);
            g.fillRect(margin.left, margin.top, c.getWidth() - margin.left - margin.right, c.getHeight() - margin.top - margin.bottom);
            paint(g, c);
            Debug.paint(g, c, this);
        }
    }

    public int getBaseline(JComponent c, int width, int height) {
        return -1;
    }

    public Rectangle getVisualBounds(JComponent c, int type, int width, int height) {
        Rectangle bounds = new Rectangle(0, 0, width, height);
        if (type == VisuallyLayoutable.CLIP_BOUNDS) {
            return bounds;
        }

        JScrollPane b = (JScrollPane) c;

        if (type == VisuallyLayoutable.COMPONENT_BOUNDS
                && b.getBorder() != null) {
            Border border = b.getBorder();
            if (border instanceof UIResource) {
                InsetsUtil.subtractInto(getVisualMargin(b), bounds);
            }
            return bounds;
        }

        return bounds;
    }

    protected void syncScrollPaneWithViewport() {
        JViewport viewport = scrollpane.getViewport();
        JScrollBar vsb = scrollpane.getVerticalScrollBar();
        JScrollBar hsb = scrollpane.getHorizontalScrollBar();
        JViewport rowHead = scrollpane.getRowHeader();
        JViewport colHead = scrollpane.getColumnHeader();
        boolean ltr = scrollpane.getComponentOrientation().isLeftToRight();

        if (viewport != null) {
            Dimension extentSize = viewport.getExtentSize();
            Dimension viewSize = viewport.getViewSize();
            Point viewPosition = viewport.getViewPosition();

            if (vsb != null) {
                int extent = extentSize.height;
                int max = viewSize.height;
                int value = Math.max(0, Math.min(viewPosition.y, max - extent));
                vsb.setValues(value, extent, 0, max);
            }

            if (hsb != null) {
                int extent = extentSize.width;
                int max = viewSize.width;
                int value;

                if (ltr) {
                    value = Math.max(0, Math.min(viewPosition.x, max - extent));
                } else {
                    int currentValue = hsb.getValue();

                    /* Use a particular formula to calculate "value"
                     * until effective x coordinate is calculated.
                     */
                    if (setValueCalled && ((max - currentValue) == viewPosition.x)) {
                        value = Math.max(0, Math.min(max - extent, currentValue));
                        /* After "extent" is set, turn setValueCalled flag off.
                         */
                        if (extent != 0) {
                            setValueCalled = false;
                        }
                    } else {
                        if (extent > max) {
                            viewPosition.x = max - extent;
                            viewport.setViewPosition(viewPosition);
                            value = 0;
                        } else {
                            /* The following line can't handle a small value of
                             * viewPosition.x like Integer.MIN_VALUE correctly
                             * because (max - extent - viewPositoiin.x) causes
                             * an overflow. As a result, value becomes zero.
                             * (e.g. setViewPosition(Integer.MAX_VALUE, ...)
                             *       in a user program causes a overflow.
                             *       Its expected value is (max - extent).)
                             * However, this seems a trivial bug and adding a
                             * fix makes this often-called method slow, so I'll
                             * leave it until someone claims.
                             */
                            value = Math.max(0, Math.min(max - extent, max - extent - viewPosition.x));
                        }
                    }
                }
                hsb.setValues(value, extent, 0, max);
            }

            if (rowHead != null) {
                Point p = rowHead.getViewPosition();
                p.y = viewport.getViewPosition().y;
                p.x = 0;
                rowHead.setViewPosition(p);
            }

            if (colHead != null) {
                Point p = colHead.getViewPosition();
                if (ltr) {
                    p.x = viewport.getViewPosition().x;
                } else {
                    p.x = Math.max(0, viewport.getViewPosition().x);
                }
                p.y = 0;
                colHead.setViewPosition(p);
            }
        }
    }

    private void updateHorizontalScrollBar(PropertyChangeEvent pce) {
        updateScrollBar(pce, hsbChangeListener, hsbPropertyChangeListener);
    }

    private void updateVerticalScrollBar(PropertyChangeEvent pce) {
        updateScrollBar(pce, vsbChangeListener, vsbPropertyChangeListener);
    }

    private void updateScrollBar(PropertyChangeEvent pce, ChangeListener cl,
            PropertyChangeListener pcl) {
        JScrollBar sb = (JScrollBar) pce.getOldValue();
        if (sb != null) {
            if (cl != null) {
                sb.getModel().removeChangeListener(cl);
            }
            if (pcl != null) {
                sb.removePropertyChangeListener(pcl);
            }
        }
        sb = (JScrollBar) pce.getNewValue();
        if (sb != null) {
            if (cl != null) {
                sb.getModel().addChangeListener(cl);
            }
            if (pcl != null) {
                sb.addPropertyChangeListener(pcl);
            }
        }
    }

    class Handler implements ChangeListener, PropertyChangeListener, MouseWheelListener {
        //
        // MouseWheelListener
        //

        public void mouseWheelMoved(MouseWheelEvent e) {
            if (scrollpane.isWheelScrollingEnabled()
                    && e.getWheelRotation() != 0) {

                int direction = e.getWheelRotation() < 0 ? -1 : 1;
                int orientation =
                        (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0
                        ? SwingConstants.VERTICAL //
                        : SwingConstants.HORIZONTAL;
                JScrollBar toScroll = orientation == SwingConstants.VERTICAL
                        ? scrollpane.getVerticalScrollBar()
                        : scrollpane.getHorizontalScrollBar();

                // If the scrollpane can not be scrolled in this direction,
                // scroll the parent scrollpane.
                if (orientation == SwingConstants.VERTICAL
                        && scrollpane.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_NEVER
                        || orientation == SwingConstants.HORIZONTAL
                        && scrollpane.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                    JScrollPane parentPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, scrollpane);
                    if (parentPane != null) {
                        parentPane.dispatchEvent(e);
                    }
                    return;
                }


                // find which scrollbar to scroll, or return if none
                if (toScroll == null || !toScroll.isVisible()) {
                    toScroll = scrollpane.getHorizontalScrollBar();
                    if (toScroll == null || !toScroll.isVisible()) {
                        return;
                    }
                }
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    JViewport vp = scrollpane.getViewport();
                    if (vp == null) {
                        return;
                    }
                    Component comp = vp.getView();
                    int units = Math.abs(e.getUnitsToScroll());

                    // When the scrolling speed is set to maximum, it's possible
                    // for a single wheel click to scroll by more units than
                    // will fit in the visible area.  This makes it
                    // hard/impossible to get to certain parts of the scrolling
                    // Component with the wheel.  To make for more accurate
                    // low-speed scrolling, we limit scrolling to the block
                    // increment if the wheel was only rotated one click.
                    boolean limitScroll = Math.abs(e.getWheelRotation()) == 1;

                    // Check if we should use the visibleRect trick
                    Object fastWheelScroll = toScroll.getClientProperty(
                            "JScrollBar.fastWheelScrolling");
                    if (Boolean.TRUE == fastWheelScroll
                            && comp instanceof Scrollable) {
                        // 5078454: Under maximum acceleration, we may scroll
                        // by many 100s of units in ~1 second.
                        //
                        // BasicScrollBarUI.scrollByUnits() can bog down the EDT
                        // with repaints in this situation.  However, the
                        // Scrollable interface allows us to pass in an
                        // arbitrary visibleRect.  This allows us to accurately
                        // calculate the total scroll amount, and then update
                        // the GUI once.  This technique provides much faster
                        // accelerated wheel scrolling.
                        Scrollable scrollComp = (Scrollable) comp;
                        Rectangle viewRect = vp.getViewRect();
                        int startingX = viewRect.x;
                        boolean leftToRight =
                                comp.getComponentOrientation().isLeftToRight();
                        int scrollMin = toScroll.getMinimum();
                        int scrollMax = toScroll.getMaximum()
                                - toScroll.getModel().getExtent();

                        if (limitScroll) {
                            int blockIncr =
                                    scrollComp.getScrollableBlockIncrement(viewRect,
                                    orientation,
                                    direction);
                            if (direction < 0) {
                                scrollMin = Math.max(scrollMin,
                                        toScroll.getValue() - blockIncr);
                            } else {
                                scrollMax = Math.min(scrollMax,
                                        toScroll.getValue() + blockIncr);
                            }
                        }

                        for (int i = 0; i < units; i++) {
                            int unitIncr =
                                    scrollComp.getScrollableUnitIncrement(viewRect,
                                    orientation, direction);
                            // Modify the visible rect for the next unit, and
                            // check to see if we're at the end already.
                            if (orientation == SwingConstants.VERTICAL) {
                                if (direction < 0) {
                                    viewRect.y -= unitIncr;
                                    if (viewRect.y <= scrollMin) {
                                        viewRect.y = scrollMin;
                                        break;
                                    }
                                } else { // (direction > 0
                                    viewRect.y += unitIncr;
                                    if (viewRect.y >= scrollMax) {
                                        viewRect.y = scrollMax;
                                        break;
                                    }
                                }
                            } else {
                                // Scroll left
                                if ((leftToRight && direction < 0)
                                        || (!leftToRight && direction > 0)) {
                                    viewRect.x -= unitIncr;
                                    if (leftToRight) {
                                        if (viewRect.x < scrollMin) {
                                            viewRect.x = scrollMin;
                                            break;
                                        }
                                    }
                                } // Scroll right
                                else if ((leftToRight && direction > 0)
                                        || (!leftToRight && direction < 0)) {
                                    viewRect.x += unitIncr;
                                    if (leftToRight) {
                                        if (viewRect.x > scrollMax) {
                                            viewRect.x = scrollMax;
                                            break;
                                        }
                                    }
                                } else {
                                    assert false : "Non-sensical ComponentOrientation / scroll direction";
                                }
                            }
                        }
                        // Set the final view position on the ScrollBar
                        if (orientation == SwingConstants.VERTICAL) {
                            toScroll.setValue(viewRect.y);
                        } else {
                            if (leftToRight) {
                                toScroll.setValue(viewRect.x);
                            } else {
                                // rightToLeft scrollbars are oriented with
                                // minValue on the right and maxValue on the
                                // left.
                                int newPos = toScroll.getValue()
                                        - (viewRect.x - startingX);
                                if (newPos < scrollMin) {
                                    newPos = scrollMin;
                                } else if (newPos > scrollMax) {
                                    newPos = scrollMax;
                                }
                                toScroll.setValue(newPos);
                            }
                        }
                    } else {
                        // Viewport's view is not a Scrollable, or fast wheel
                        // scrolling is not enabled.
                        QuaquaScrollBarUI.scrollByUnits(toScroll, direction,
                                units, limitScroll);
                    }
                } else if (e.getScrollType()
                        == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
                    QuaquaScrollBarUI.scrollByBlock(toScroll, direction);
                }
            }
        }

        //
        // ChangeListener: This is added to the vieport, and hsb/vsb models.
        //
        public void stateChanged(ChangeEvent e) {
            JViewport viewport = scrollpane.getViewport();

            if (viewport != null) {
                if (e.getSource() == viewport) {
                    viewportStateChanged(e);
                } else {
                    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
                    if (hsb != null && e.getSource() == hsb.getModel()) {
                        hsbStateChanged(viewport, e);
                    } else {
                        JScrollBar vsb = scrollpane.getVerticalScrollBar();
                        if (vsb != null && e.getSource() == vsb.getModel()) {
                            vsbStateChanged(viewport, e);
                        }
                    }
                }
            }
        }

        private void vsbStateChanged(JViewport viewport, ChangeEvent e) {
            BoundedRangeModel model = (BoundedRangeModel) (e.getSource());
            Point p = viewport.getViewPosition();
            p.y = model.getValue();
            viewport.setViewPosition(p);
        }

        private void hsbStateChanged(JViewport viewport, ChangeEvent e) {
            BoundedRangeModel model = (BoundedRangeModel) (e.getSource());
            Point p = viewport.getViewPosition();
            int value = model.getValue();
            if (scrollpane.getComponentOrientation().isLeftToRight()) {
                p.x = value;
            } else {
                int max = viewport.getViewSize().width;
                int extent = viewport.getExtentSize().width;
                int oldX = p.x;

                /* Set new X coordinate based on "value".
                 */
                p.x = max - extent - value;

                /* If setValue() was called before "extent" was fixed,
                 * turn setValueCalled flag on.
                 */
                if ((extent == 0) && (value != 0) && (oldX == max)) {
                    setValueCalled = true;
                } else {
                    /* When a pane without a horizontal scroll bar was
                     * reduced and the bar appeared, the viewport should
                     * show the right side of the view.
                     */
                    if ((extent != 0) && (oldX < 0) && (p.x == 0)) {
                        p.x += value;
                    }
                }
            }
            viewport.setViewPosition(p);
        }

        private void viewportStateChanged(ChangeEvent e) {
            syncScrollPaneWithViewport();
        }

        //
        // PropertyChangeListener: This is installed on both the JScrollPane
        // and the horizontal/vertical scrollbars.
        //
        // Listens for changes in the model property and reinstalls the
        // horizontal/vertical PropertyChangeListeners.
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if ("Frame.active".equals(name)) {
                QuaquaUtilities.repaintBorder((JComponent) e.getSource());
            } else if ("JComponent.sizeVariant".equals(name)) {
                QuaquaUtilities.applySizeVariant(scrollpane);
            }


            if (e.getSource() == scrollpane) {
                scrollPanePropertyChange(e);
            } else {
                sbPropertyChange(e);
            }
        }

        private void scrollPanePropertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();

            if ("verticalScrollBarDisplayPolicy".equals(propertyName)) {
                updateScrollBarDisplayPolicy(e);
            } else if ("horizontalScrollBarDisplayPolicy".equals(propertyName)) {
                updateScrollBarDisplayPolicy(e);
            } else if ("viewport".equals(propertyName)) {
                updateViewport(e);
            } else if ("rowHeader".equals(propertyName)) {
                updateRowHeader(e);
            } else if ("columnHeader".equals(propertyName)) {
                updateColumnHeader(e);
            } else if ("verticalScrollBar".equals(propertyName)) {
                updateVerticalScrollBar(e);
            } else if ("horizontalScrollBar".equals(propertyName)) {
                updateHorizontalScrollBar(e);
            } else if ("componentOrientation".equals(propertyName)) {
                scrollpane.revalidate();
                scrollpane.repaint();
            }
        }

        // PropertyChangeListener for the horizontal and vertical scrollbars.
        private void sbPropertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            Object source = e.getSource();

            if ("model".equals(propertyName)) {
                JScrollBar sb = scrollpane.getVerticalScrollBar();
                BoundedRangeModel oldModel = (BoundedRangeModel) e.getOldValue();
                ChangeListener cl = null;

                if (source == sb) {
                    cl = vsbChangeListener;
                } else if (source == scrollpane.getHorizontalScrollBar()) {
                    sb = scrollpane.getHorizontalScrollBar();
                    cl = hsbChangeListener;
                }
                if (cl != null) {
                    if (oldModel != null) {
                        oldModel.removeChangeListener(cl);
                    }
                    if (sb.getModel() != null) {
                        sb.getModel().addChangeListener(cl);
                    }
                }
            } else if ("componentOrientation".equals(propertyName)) {
                if (source == scrollpane.getHorizontalScrollBar()) {
                    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
                    JViewport viewport = scrollpane.getViewport();
                    Point p = viewport.getViewPosition();
                    if (scrollpane.getComponentOrientation().isLeftToRight()) {
                        p.x = hsb.getValue();
                    } else {
                        p.x = viewport.getViewSize().width - viewport.getExtentSize().width - hsb.getValue();
                    }
                    viewport.setViewPosition(p);
                }
            }
        }
    }
}
