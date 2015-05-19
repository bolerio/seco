/*
 * @(#)QuaquaScrollBarUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaScrollBarUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaScrollBarUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaScrollBarUI extends BasicScrollBarUI {

    protected Dimension smallMinimumThumbSize;
    protected boolean isPlaceButtonsTogether;

    /**
     * Creates a new instance.
     */
    public QuaquaScrollBarUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaScrollBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        smallMinimumThumbSize = (Dimension) UIManager.get("ScrollBar.minimumThumbSize.small");
        updatePlaceButtonsTogether();
        LookAndFeel.installColorsAndFont(scrollbar, "ScrollBar.background", "ScrollBar.foreground", "ScrollBar.font");
        scrollbar.setFocusable(UIManager.getBoolean("ScrollBar.focusable"));
    }

    private void updatePlaceButtonsTogether() {
        Object value = scrollbar.getClientProperty("Quaqua.ScrollBar.placeButtonsTogether");
        if (value == null) {
            isPlaceButtonsTogether = UIManager.getBoolean("ScrollBar.placeButtonsTogether");
        } else {
            isPlaceButtonsTogether = value.equals(Boolean.TRUE);
        }
    }

    private boolean isPlaceButtonsTogether() {
        return isPlaceButtonsTogether;
    }

    @Override
    protected TrackListener createTrackListener() {
        return new QuaquaTrackListener();
    }

    @Override
    protected ModelListener createModelListener() {
        return new QuaquaModelListener();
    }

    @Override
    protected ArrowButtonListener createArrowButtonListener() {
        return new QuaquaArrowButtonListener();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new QuaquaPropertyChangeHandler();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return new QuaquaArrowButton(scrollbar);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new QuaquaArrowButton(scrollbar);
    }

    @Override
    protected ScrollListener createScrollListener() {
        return new QuaquaScrollListener();
    }

    /**
     * Returns true, if the scrollbar is using the small size style.
     */
    private boolean isSmall() {
        // If we are on a JScrollPane, we also use the JScrollPane to
        // determine the size style.
        QuaquaUtilities.SizeVariant sv;
        if (scrollbar.getParent() instanceof JScrollPane) {
            sv = QuaquaUtilities.getSizeVariant(scrollbar.getParent());
        } else {
            sv = QuaquaUtilities.getSizeVariant(scrollbar);
        }
        return sv == QuaquaUtilities.SizeVariant.SMALL
                ||sv == QuaquaUtilities.SizeVariant.MINI;
    }

    /**
     * Return the smallest acceptable size for the thumb.  If the scrollbar
     * becomes so small that this size isn't available, the thumb will be
     * hidden.
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a shared static constant.
     *
     * @return The smallest acceptable size for the thumb.
     * @see #getMaximumThumbSize
     */
    @Override
    protected Dimension getMinimumThumbSize() {
        return isSmall()
                ? smallMinimumThumbSize
                : minimumThumbSize;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension dim = getPreferredSize(c);

        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            dim.height = Integer.MAX_VALUE;
        } else {
            dim.width = Integer.MAX_VALUE;
        }
        return dim;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension dim;
        switch (QuaquaUtilities.getSizeVariant(c)) {
            default:
            case REGULAR:
                dim = UIManager.getDimension("ScrollBar.preferredSize");
                break;
            case SMALL:
                dim = UIManager.getDimension("ScrollBar.preferredSize.small");
                break;
            case MINI:
                dim = UIManager.getDimension("ScrollBar.preferredSize.mini");
                break;
        }
        return (scrollbar.getOrientation() == JScrollBar.VERTICAL)
                ? (Dimension) dim.clone() : new Dimension(dim.height, dim.width);
    }

    /**
     * Return the largest acceptable size for the thumb.  To create a fixed
     * size thumb one make this method and <code>getMinimumThumbSize</code>
     * return the same value.
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a shared static constant.
     *
     * @return The largest acceptable size for the thumb.
     * @see #getMinimumThumbSize
     */
    @Override
    protected Dimension getMaximumThumbSize() {
        return maximumThumbSize;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Debug.paint(g, c, this);
    }

    @Override
    protected void paintThumb(Graphics gr, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty()) {
            return;
        }
        Graphics2D g = (Graphics2D) gr;

        boolean isSmall = isSmall();

        if (!scrollbar.isEnabled()
                || !QuaquaUtilities.isOnActiveWindow(scrollbar)) {
            Border thumbBorder;
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                thumbBorder = UIManager.getBorder(isSmall ? "ScrollBar.thumb.vInactive.small" : "ScrollBar.thumb.vInactive");
            } else {
                thumbBorder = UIManager.getBorder(isSmall ? "ScrollBar.thumb.hInactive.small" : "ScrollBar.thumb.hInactive");
            }
            if (thumbBorder != null) {
                thumbBorder.paintBorder(c, g,
                        thumbBounds.x, thumbBounds.y,
                        thumbBounds.width, thumbBounds.height);
            }
        } else {
            Border thumbBorder;
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                thumbBorder = UIManager.getBorder("ScrollBar.thumb.v");
            } else {
                thumbBorder = UIManager.getBorder("ScrollBar.thumb.h");
            }
            if (thumbBorder != null) {
                thumbBorder.paintBorder(c, g,
                        thumbBounds.x, thumbBounds.y,
                        thumbBounds.width, thumbBounds.height);
                return;
            }

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                Icon[] icons = ((Icon[]) UIManager.get(isSmall ? "ScrollBar.thumb.vFirst.small" : "ScrollBar.thumb.vFirst"));
                if (icons != null) {
                    Icon thumbBegin = icons[thumbBounds.y % 5];
                    thumbBegin.paintIcon(c, g, thumbBounds.x, thumbBounds.y);
                    Icon thumbEnd = ((Icon[]) UIManager.get(isSmall ? "ScrollBar.thumb.vLast.small" : "ScrollBar.thumb.vLast"))[(thumbBounds.y + thumbBounds.height) % 5];
                    thumbEnd.paintIcon(c, g, thumbBounds.x, thumbBounds.y + thumbBounds.height - thumbEnd.getIconHeight());
                    BufferedImage img = (BufferedImage) UIManager.get(isSmall ? "ScrollBar.thumb.vMiddle.small" : "ScrollBar.thumb.vMiddle");
                    TexturePaint paint = new TexturePaint(
                            img,
                            new Rectangle(thumbBounds.x, 0, img.getWidth(), img.getHeight()));
                    g.setPaint(paint);
                    g.fillRect(thumbBounds.x, thumbBounds.y + thumbBegin.getIconHeight(), getPreferredSize(c).width, thumbBounds.height - thumbBegin.getIconHeight() - thumbEnd.getIconHeight());
                }
            } else {
                Icon[] icons = ((Icon[]) UIManager.get(isSmall ? "ScrollBar.thumb.hFirst.small" : "ScrollBar.thumb.hFirst"));
                if (icons != null) {
                    Icon thumbBegin = icons[thumbBounds.x % 5];
                    thumbBegin.paintIcon(c, g, thumbBounds.x, thumbBounds.y);
                    Icon thumbEnd = ((Icon[]) UIManager.get(isSmall ? "ScrollBar.thumb.hLast.small" : "ScrollBar.thumb.hLast"))[(thumbBounds.x + thumbBounds.width) % 5];
                    thumbEnd.paintIcon(c, g, thumbBounds.x + thumbBounds.width - thumbEnd.getIconWidth(), thumbBounds.y);
                    BufferedImage img = (BufferedImage) UIManager.get(isSmall ? "ScrollBar.thumb.hMiddle.small" : "ScrollBar.thumb.hMiddle");
                    TexturePaint paint = new TexturePaint(
                            img,
                            new Rectangle(0, thumbBounds.y, img.getWidth(), img.getHeight()));
                    g.setPaint(paint);
                    g.fillRect(thumbBounds.x + thumbBegin.getIconWidth(), thumbBounds.y, thumbBounds.width - thumbBegin.getIconWidth() - thumbEnd.getIconWidth(), getPreferredSize(c).height);
                }
            }
        }
    }

    /**
     * This method actually paints the track plus the button artwork.
     */
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Dimension sbSize = scrollbar.getSize();
        Insets sbInsets = scrollbar.getInsets();
        if (sbInsets == null) {
            sbInsets = new Insets(0, 0, 0, 0);
        }
        Rectangle contentBounds = new Rectangle(
                sbInsets.left, sbInsets.top,
                sbSize.width - sbInsets.left - sbInsets.right,
                sbSize.height - sbInsets.top - sbInsets.bottom);

        Border trackAndButtons = getTrackAndButtonsBorder();
        if (trackAndButtons == null) {
            return;
        }
        Insets tbInsets = trackAndButtons.getBorderInsets(scrollbar);
        if (tbInsets == null) {
            tbInsets = new Insets(0, 0, 0, 0);
        }
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            if (isPlaceButtonsTogether()
                    && contentBounds.height < tbInsets.top + tbInsets.bottom) {
                // Special treatment, if track and buttons do
                // not fit into available space
                Graphics clipped = g.create(contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
                int buttonsH = incrButton.getPreferredSize().height * 2;
                trackAndButtons.paintBorder(c, g,
                        contentBounds.x,
                        contentBounds.y + contentBounds.height - tbInsets.top - tbInsets.bottom + Math.max(0, (buttonsH - contentBounds.height) / 2),
                        contentBounds.width,
                        tbInsets.top + tbInsets.bottom);
                clipped.dispose();
            } else {
                trackAndButtons.paintBorder(c, g, contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
            }
        } else {
            if (isPlaceButtonsTogether()
                    && contentBounds.width < tbInsets.left + tbInsets.right) {
                // Special treatment, if track and buttons do
                // not fit into available space
                //trackAndButtons.paintBorder(c, g, contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
                Graphics clipped = g.create(contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
                int buttonsW = incrButton.getPreferredSize().width * 2;
                trackAndButtons.paintBorder(c, g,
                        contentBounds.x + contentBounds.width - tbInsets.left - tbInsets.right + Math.max(0, (buttonsW - contentBounds.width) / 2),
                        contentBounds.y,
                        tbInsets.left + tbInsets.right,
                        contentBounds.height);
                clipped.dispose();
            } else {
                trackAndButtons.paintBorder(c, g, contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
            }
        }
    }

    /**
     * We use a border to fill the background of the scroll bar.
     * The border contains the track and the buttons in their current visual
     * representation depending on the various states of the scroll bar
     * ('enabled','pressed','inactive').
     */
    protected Border getTrackAndButtonsBorder() {
        char vh = (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? 'v' : 'h';
        boolean isSmall = isSmall();

        // Return empty track if extent of scroll bar fully visible
        if (scrollbar.getMinimum() + scrollbar.getVisibleAmount() == scrollbar.getMaximum()) {
            return UIManager.getBorder(isSmall ? "ScrollBar.track." + vh + ".small" : "ScrollBar.track." + vh);
        }

        // compute button index
        int buttonsIndex;

        // Scroll Bar arrows are always shown in active state by some Mac OS X
        // Applications and by some they are grayed on inactive windows.
        //if (scrollbar.isEnabled() && QuaquaUtilities.isOnActiveWindow(scrollbar)) {
        if (scrollbar.isEnabled()) {
            if (incrButton.getModel().isArmed() && incrButton.getModel().isPressed()) {
                buttonsIndex = 1;
            } else if (decrButton.getModel().isArmed() && decrButton.getModel().isPressed()) {
                buttonsIndex = 3;
            } else {
                buttonsIndex = 0;
            }
        } else {
            buttonsIndex = 2;
        }

        Border[] borders;
        if (isPlaceButtonsTogether()) {
            borders = (Border[]) UIManager.get(isSmall ? "ScrollBar.buttons." + vh + "Tog.small" : "ScrollBar.buttons." + vh + "Tog");
        } else {
            borders = (Border[]) UIManager.get(isSmall ? "ScrollBar.buttons." + vh + "Sep.small" : "ScrollBar.buttons." + vh + "Sep");
        }
        return (borders == null) ? UIManager.getBorder("ScrollBar.track." + vh) : borders[buttonsIndex];
    }

    /**
     * Indicates whether the user can absolutely position the offset with
     * a mouse click (depending on the settings in "Appearance" panel of
     * " the "System Preferences" application).
     * <p>The return value is determined from the UIManager property
     * ScrollBar.allowsAbsolutePositioning.
     */
    @Override
    public boolean getSupportsAbsolutePositioning() {
        return UIManager.getBoolean("ScrollBar.supportsAbsolutePositioning");
    }

    @Override
    protected void layoutVScrollbar(JScrollBar sb) {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();

        // Width and left edge of the buttons and thumb.
        int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
        int itemX = sbInsets.left;

        /**
         * Hide thumb and buttons if the complete extent is shown.
         * Note that setting the
         * thumbs bounds will cause a repaint.
         */
        if (sb.getMinimum() + sb.getVisibleAmount() == sb.getMaximum()) {
            trackRect.setBounds(sbInsets.left, sbInsets.top, itemW, sbSize.height - (sbInsets.top + sbInsets.bottom));
            decrButton.setBounds(0, 0, 0, 0);
            incrButton.setBounds(0, 0, 0, 0);
            setThumbBounds(0, 0, 0, 0);
            return;
        }

        boolean isSmall = isSmall();

        int incrButtonH, decrButtonH;
        incrButtonH = decrButtonH = UIManager.getInt((isSmall) ? "ScrollBar.buttonHeight.small" : "ScrollBar.buttonHeight");

        // The thumb must fit within the height left over after we
        // subtract the preferredSize of the buttons and the insets.
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;

        int trackY;
        int trackH;

        int thumbH;
        int thumbY;

        int incrButtonY;
        int decrButtonY;

        int sbAvailButtonH = (sbSize.height - sbInsetsH);

        // Compute y-locations and heights
        if (isPlaceButtonsTogether()) {
            incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
            decrButtonY = incrButtonY - decrButtonH;

            // If the buttons don't fit, allocate half of the available
            // space to each.
            if (sbAvailButtonH < sbButtonsH) {
                incrButtonH = sbAvailButtonH / 2;
                decrButtonH = sbAvailButtonH - incrButtonH;
                incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
                decrButtonY = sbInsets.top;
            }

            trackY = sbInsets.top + UIManager.getInsets((isSmall) ? "ScrollBar.trackInsets.tog.small" : "ScrollBar.trackInsets.tog").top;
            trackH = decrButtonY - trackY + UIManager.getInsets((isSmall) ? "ScrollBar.trackInsets.tog.small" : "ScrollBar.trackInsets.tog").bottom;
        } else {
            decrButtonY = sbInsets.top;
            incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);

            // If the buttons don't fit, allocate half of the available
            // space to each.
            if (sbAvailButtonH < sbButtonsH) {
                incrButtonH = decrButtonH = sbAvailButtonH / 2;
                incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
            }

            trackY = decrButtonY + decrButtonH + ((isSmall) ? 0 : -1);
            trackH = incrButtonY - trackY + ((isSmall) ? 2 : 3);
        }
        decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);
        trackRect.setBounds(itemX, trackY, itemW, trackH);


        /* Compute the height and origin of the thumb.   The case
         * where the thumb is at the bottom edge is handled specially
         * to avoid numerical problems in computing thumbY.  Enforce
         * the thumbs min/max dimensions.  If the thumb doesn't
         * fit in the track (trackH) we'll hide it later.
         */
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = sb.getMaximum() - min;
        float value = sb.getValue();
        float ftrackH = (float) trackH;

        thumbH = (range <= 0)
                ? getMaximumThumbSize().height : (int) (ftrackH * (extent / range));
        thumbH = Math.max(thumbH, getMinimumThumbSize().height);
        thumbH = Math.min(thumbH, getMaximumThumbSize().height);

        thumbY = trackY + trackH - thumbH;
        if (sb.getValue() < (sb.getMaximum() - sb.getVisibleAmount())) {
            float thumbRange = ftrackH - thumbH;
            thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
            thumbY += trackY;
        }

        /* If the thumb isn't going to fit, zero it's bounds.  Otherwise
         * make sure it fits into the track.  Note that setting the
         * thumbs bounds will cause a repaint.
         */
        if (thumbH >= trackH) {
            setThumbBounds(0, 0, 0, 0);
        } else {
            setThumbBounds(itemX, thumbY, itemW, thumbH);
        }
    }

    @Override
    protected void layoutHScrollbar(JScrollBar sb) {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();

        /* Height and top edge of the buttons and thumb.
         */
        int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
        int itemY = sbInsets.top;

        /**
         * Hide thumb and buttons if the complete extent is shown.
         * Note that setting the
         * thumbs bounds will cause a repaint.
         */
        if (sb.getMinimum() + sb.getVisibleAmount() == sb.getMaximum()) {
            trackRect.setBounds(sbInsets.left, sbInsets.top, sbSize.width - (sbInsets.left + sbInsets.right), itemH);
            decrButton.setBounds(0, 0, 0, 0);
            incrButton.setBounds(0, 0, 0, 0);
            setThumbBounds(0, 0, 0, 0);
            return;
        }

        boolean isSmall = isSmall();

        int leftButtonW, rightButtonW;
        leftButtonW = rightButtonW = UIManager.getInt((isSmall) ? "ScrollBar.buttonHeight.small" : "ScrollBar.buttonHeight");

        // The thumb must fit within the width left over after we
        // subtract the preferredSize of the buttons and the insets.
        int sbInsetsW = sbInsets.left + sbInsets.right;
        int sbButtonsW = leftButtonW + rightButtonW;

        int trackX;
        int trackW;

        int thumbW;
        int thumbX;

        int rightButtonX;
        int leftButtonX;

        int sbAvailButtonW = (sbSize.width - sbInsetsW);
        boolean ltr;

        if (isPlaceButtonsTogether()) {
            ltr = true;

            // Nominal locations of the buttons, assuming their preferred
            // size will fit.
            rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);
            leftButtonX = rightButtonX - leftButtonW;

            // If the buttons don't fit, allocate half of the available
            // space to each and move the right one over.
            if (sbAvailButtonW < sbButtonsW) {
                leftButtonW = sbAvailButtonW / 2;
                rightButtonW = sbAvailButtonW - leftButtonW;
                leftButtonX = sbInsets.left;
                rightButtonX = leftButtonX + leftButtonW;
            }

            trackX = sbInsets.left + UIManager.getInsets((isSmall) ? "ScrollBar.trackInsets.tog.small" : "ScrollBar.trackInsets.tog").left;
            trackW = leftButtonX - trackX + UIManager.getInsets((isSmall) ? "ScrollBar.trackInsets.tog.small" : "ScrollBar.trackInsets.tog").right;
        } else {
            ltr = sb.getComponentOrientation().isLeftToRight();
            if (!ltr) {
                int helper = leftButtonW;
                leftButtonW = rightButtonW;
                rightButtonW = helper;
            }

            // Nominal locations of the buttons, assuming their preferred
            // size will fit.
            leftButtonX = sbInsets.left;
            rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);

            // If the buttons don't fit, allocate half of the available
            // space to each and move the right one over.
            if (sbAvailButtonW < sbButtonsW) {
                rightButtonW = leftButtonW = sbAvailButtonW / 2;
                rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);
            }

            trackX = leftButtonX + leftButtonW - 1;
            trackW = rightButtonX - trackX + 1;
        }
        (ltr ? decrButton : incrButton).setBounds(leftButtonX, itemY, leftButtonW, itemH);
        (ltr ? incrButton : decrButton).setBounds(rightButtonX, itemY, rightButtonW, itemH);
        trackRect.setBounds(trackX, itemY, trackW, itemH);

        /* Compute the width and origin of the thumb.  Enforce
         * the thumbs min/max dimensions.  The case where the thumb
         * is at the right edge is handled specially to avoid numerical
         * problems in computing thumbX.  If the thumb doesn't
         * fit in the track (trackH) we'll hide it later.
         */
        float min = sb.getMinimum();
        float max = sb.getMaximum();
        float extent = sb.getVisibleAmount();
        float range = max - min;
        float value = sb.getValue();
        float ftrackW = (float) trackW;

        thumbW = (range <= 0)
                ? getMaximumThumbSize().width : (int) (ftrackW * (extent / range));
        thumbW = Math.max(thumbW, getMinimumThumbSize().width);
        thumbW = Math.min(thumbW, getMaximumThumbSize().width);

        thumbX = trackX + trackW - thumbW;
        if (sb.getValue() < (max - sb.getVisibleAmount())) {
            float thumbRange = ftrackW - thumbW;
            thumbX = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
            thumbX += trackX;
        }

        /* Make sure the thumb fits between the buttons.  Note
         * that setting the thumbs bounds causes a repaint.
         */
        if (thumbW >= (int) trackW) {
            setThumbBounds(0, 0, 0, 0);
        } else {
            setThumbBounds(thumbX, itemY, thumbW, itemH);
        }
    }

    @Override
    protected void scrollByUnit(int direction) {
        scrollByUnits(scrollbar, direction, 1, false);
    }
    /*
     * Method for scrolling by a unit increment.
     * Added for mouse wheel scrolling support, RFE 4202656.
     *
     * If limitByBlock is set to true, the scrollbar will scroll at least 1
     * unit increment, but will not scroll farther than the block increment.
     * See BasicScrollPaneUI.Handler.mouseWheelMoved().
     */

    static void scrollByUnits(JScrollBar scrollbar, int direction,
            int units, boolean limitToBlock) {
        // This method is called from QuaquaScrollPaneUI to implement wheel
        // scrolling, as well as from scrollByUnit().
        int delta;
        int limit = -1;

        if (limitToBlock) {
            if (direction < 0) {
                limit = scrollbar.getValue()
                        - scrollbar.getBlockIncrement(direction);
            } else {
                limit = scrollbar.getValue()
                        + scrollbar.getBlockIncrement(direction);
            }
        }

        for (int i = 0; i < units; i++) {
            if (direction > 0) {
                delta = scrollbar.getUnitIncrement(direction);
            } else {
                delta = -scrollbar.getUnitIncrement(direction);
            }

            int oldValue = scrollbar.getValue();
            int newValue = oldValue + delta;

            // Check for overflow.
            if (delta > 0 && newValue < oldValue) {
                newValue = scrollbar.getMaximum();
            } else if (delta < 0 && newValue > oldValue) {
                newValue = scrollbar.getMinimum();
            }
            if (oldValue == newValue) {
                break;
            }

            if (limitToBlock && i > 0) {
                assert limit != -1;
                if ((direction < 0 && newValue < limit)
                        || (direction > 0 && newValue > limit)) {
                    break;
                }
            }
            scrollbar.setValue(newValue);
        }
    }

    @Override
    protected void scrollByBlock(int direction) {
        scrollByBlock(scrollbar, direction);
        trackHighlight = direction > 0 ? INCREASE_HIGHLIGHT : DECREASE_HIGHLIGHT;
        Rectangle dirtyRect = getTrackBounds();
        scrollbar.repaint(dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
    }
    /*
     * Method for scrolling by a block increment.
     * Added for mouse wheel scrolling support, RFE 4202656.
     */

    static void scrollByBlock(JScrollBar scrollbar, int direction) {
        // This method is called from BasicScrollPaneUI to implement wheel
        // scrolling, and also from scrollByBlock().
        int oldValue = scrollbar.getValue();
        int blockIncrement = scrollbar.getBlockIncrement(direction);
        int delta = blockIncrement * ((direction > 0) ? +1 : -1);
        int newValue = oldValue + delta;

        // Check for overflow.
        if (delta > 0 && newValue < oldValue) {
            newValue = scrollbar.getMaximum();
        } else if (delta < 0 && newValue > oldValue) {
            newValue = scrollbar.getMinimum();
        }

        scrollbar.setValue(newValue);
    }

    protected class QuaquaTrackListener extends TrackListener {

        protected transient int direction = +1;

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                    || (!getSupportsAbsolutePositioning()
                    && SwingUtilities.isMiddleMouseButton(e))) {
                return;
            }
            if (!scrollbar.isEnabled()) {
                return;
            }

            Rectangle r = getTrackBounds();
            scrollbar.repaint(r.x, r.y, r.width, r.height);

            trackHighlight = NO_HIGHLIGHT;
            isDragging = false;
            offset = 0;
            scrollTimer.stop();
            scrollbar.setValueIsAdjusting(false);
        }

        /**
         * If the mouse is pressed above the "thumb" component
         * then reduce the scrollbars value by one page ("page up"),
         * otherwise increase it by one page.  If there is no
         * thumb then page up if the mouse is in the upper half
         * of the track.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                    || (!getSupportsAbsolutePositioning()
                    && SwingUtilities.isMiddleMouseButton(e))) {
                return;
            }
            if (!scrollbar.isEnabled()) {
                return;
            }

            if (!scrollbar.hasFocus() && scrollbar.isRequestFocusEnabled()) {
                scrollbar.requestFocus();
            }

            scrollbar.setValueIsAdjusting(true);

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            // Clicked in the Thumb area?
            if (getThumbBounds().contains(currentMouseX, currentMouseY)) {
                switch (scrollbar.getOrientation()) {
                    case JScrollBar.VERTICAL:
                        offset = currentMouseY - getThumbBounds().y;
                        break;
                    case JScrollBar.HORIZONTAL:
                        offset = currentMouseX - getThumbBounds().x;
                        break;
                }
                isDragging = true;
                return;
            } else if (getSupportsAbsolutePositioning()
                    || SwingUtilities.isMiddleMouseButton(e)) {
                switch (scrollbar.getOrientation()) {
                    case JScrollBar.VERTICAL:
                        offset = getThumbBounds().height / 2;
                        break;
                    case JScrollBar.HORIZONTAL:
                        offset = getThumbBounds().width / 2;
                        break;
                }
                isDragging = true;
                setValueFrom(e);
                return;
            }
            isDragging = false;

            Dimension sbSize = scrollbar.getSize();
            direction = +1;

            switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
                    if (getThumbBounds().isEmpty()) {
                        int scrollbarCenter = sbSize.height / 2;
                        direction = (currentMouseY < scrollbarCenter) ? -1 : +1;
                    } else {
                        int thumbY = getThumbBounds().y;
                        direction = (currentMouseY < thumbY) ? -1 : +1;
                    }
                    break;
                case JScrollBar.HORIZONTAL:
                    if (getThumbBounds().isEmpty()) {
                        int scrollbarCenter = sbSize.width / 2;
                        direction = (currentMouseX < scrollbarCenter) ? -1 : +1;
                    } else {
                        int thumbX = getThumbBounds().x;
                        direction = (currentMouseX < thumbX) ? -1 : +1;
                    }
                    if (!scrollbar.getComponentOrientation().isLeftToRight()) {
                        direction = -direction;
                    }
                    break;
            }
            scrollByBlock(direction);

            scrollTimer.stop();
            scrollListener.setDirection(direction);
            scrollListener.setScrollByBlock(true);
            startScrollTimerIfNecessary();
        }

        /**
         * Set the models value to the position of the thumb's top of Vertical
         * scrollbar, or the left/right of Horizontal scrollbar in
         * left-to-right/right-to-left scrollbar relative to the origin of the
         * track.
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                    || (!getSupportsAbsolutePositioning()
                    && SwingUtilities.isMiddleMouseButton(e))) {
                return;
            }
            if (!scrollbar.isEnabled() || getThumbBounds().isEmpty()) {
                return;
            }
            if (isDragging) {
                setValueFrom(e);
            } else {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                startScrollTimerIfNecessary();
            }
        }

        private void setValueFrom(MouseEvent e) {
            BoundedRangeModel model = scrollbar.getModel();
            Rectangle thumbR = getThumbBounds();
            Rectangle trackR = getTrackBounds();
            int thumbMin, thumbMax, thumbPos;

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                thumbMin = trackR.y;
                thumbMax = trackR.y + trackR.height - thumbR.height;

                thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getY() - offset)));
                setThumbBounds(thumbR.x, thumbPos, thumbR.width, thumbR.height);
            } else {
                /*
                if (scrollbar.getComponentOrientation().isLeftToRight()) {
                thumbMin = decrButton.getX() + decrButton.getWidth();
                thumbMax = incrButton.getX() - thumbR.width;
                } else {
                thumbMin = incrButton.getX() + incrButton.getWidth();
                thumbMax = decrButton.getX() - thumbR.width;
                }*/
                thumbMin = trackR.x;
                thumbMax = trackR.x + trackR.width - thumbR.width;

                thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getX() - offset)));
                setThumbBounds(thumbPos, thumbR.y, thumbR.width, thumbR.height);
            }

            /* Set the scrollbars value.  If the thumb has reached the end of
             * the scrollbar, then just set the value to its maximum.  Otherwise
             * compute the value as accurately as possible.
             */
            if (thumbPos == thumbMax) {
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL
                        || scrollbar.getComponentOrientation().isLeftToRight()) {
                    scrollbar.setValue(model.getMaximum() - model.getExtent());
                } else {
                    scrollbar.setValue(model.getMinimum());
                }
            } else {
                float valueMax = model.getMaximum() - model.getExtent();
                float valueRange = valueMax - model.getMinimum();
                float thumbValue = thumbPos - thumbMin;
                float thumbRange = thumbMax - thumbMin;
                int value;
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL
                        || scrollbar.getComponentOrientation().isLeftToRight()) {
                    value = (int) (0.5 + ((thumbValue / thumbRange) * valueRange));
                } else {
                    value = (int) (0.5 + (((thumbMax - thumbPos) / thumbRange) * valueRange));
                }
                scrollbar.setValue(value + model.getMinimum());
            }
        }

        private void startScrollTimerIfNecessary() {
            if (scrollTimer.isRunning()) {
                return;
            }
            switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
                    if (direction > 0) {
                        if (getThumbBounds().y + getThumbBounds().height
                                < ((QuaquaTrackListener) trackListener).currentMouseY) {
                            scrollTimer.start();
                        }
                    } else if (getThumbBounds().y
                            > ((QuaquaTrackListener) trackListener).currentMouseY) {
                        scrollTimer.start();
                    }
                    break;
                case JScrollBar.HORIZONTAL:
                    if (direction > 0) {
                        if (getThumbBounds().x + getThumbBounds().width
                                < ((QuaquaTrackListener) trackListener).currentMouseX) {
                            scrollTimer.start();
                        }
                    } else if (getThumbBounds().x
                            > ((QuaquaTrackListener) trackListener).currentMouseX) {
                        scrollTimer.start();
                    }
                    break;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        private int getCurrentMouseX() {
            return currentMouseX;
        }

        private int getCurrentMouseY() {
            return currentMouseY;
        }
    }

    /**
     * Listener for cursor keys.
     */
    protected class QuaquaArrowButtonListener extends ArrowButtonListener {

        @Override
        public void mousePressed(MouseEvent evt) {
            super.mousePressed(evt);
            repaintButtons();
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            super.mouseReleased(evt);
            repaintButtons();
        }

        @Override
        public void mouseEntered(MouseEvent evt) {
            super.mouseEntered(evt);
            if ((evt.getModifiers() & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
                //if (evt.getButton() != MouseEvent.NOBUTTON) {
                repaintButtons();
            }
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            super.mouseExited(evt);
            if ((evt.getModifiers() & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
                //if (evt.getButton() != MouseEvent.NOBUTTON) {
                repaintButtons();
            }
        }

        protected void repaintButtons() {
            Dimension sbSize = scrollbar.getSize();
            Insets sbInsets = scrollbar.getInsets();
            Rectangle contentBounds = new Rectangle(
                    sbInsets.left, sbInsets.top,
                    sbSize.width - sbInsets.left - sbInsets.right,
                    sbSize.height - sbInsets.top - sbInsets.bottom);

            if (isPlaceButtonsTogether()) {
                Border trackAndButtonsBorder = getTrackAndButtonsBorder();
                Insets tbInsets = trackAndButtonsBorder.getBorderInsets(scrollbar);

                if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                    scrollbar.repaint(
                            contentBounds.x,
                            contentBounds.y + contentBounds.height - tbInsets.bottom,
                            contentBounds.width,
                            tbInsets.bottom);
                } else {
                    scrollbar.repaint(
                            contentBounds.x + contentBounds.width - tbInsets.right,
                            contentBounds.y,
                            tbInsets.right,
                            contentBounds.height);
                }
            } else {
                scrollbar.repaint();
            }
        }
    }

    /**
     * Listener for scrolling events initiated in the
     * <code>ScrollPane</code>.
     */
    protected class QuaquaScrollListener extends ScrollListener {

        int direction = +1;
        boolean useBlockIncrement;

        public QuaquaScrollListener() {
            direction = +1;
            useBlockIncrement = false;
        }

        public QuaquaScrollListener(int dir, boolean block) {
            direction = dir;
            useBlockIncrement = block;
        }

        @Override
        public void setDirection(int direction) {
            this.direction = direction;
        }

        @Override
        public void setScrollByBlock(boolean block) {
            this.useBlockIncrement = block;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QuaquaTrackListener trackListener = (QuaquaTrackListener) QuaquaScrollBarUI.this.trackListener;
            if (useBlockIncrement) {
                scrollByBlock(direction);
                // Stop scrolling if the thumb catches up with the mouse
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                    if (direction > 0) {
                        if (getThumbBounds().y + getThumbBounds().height
                                >= trackListener.getCurrentMouseY()) {
                            ((Timer) e.getSource()).stop();
                        }
                    } else if (getThumbBounds().y <= trackListener.getCurrentMouseY()) {
                        ((Timer) e.getSource()).stop();
                    }
                } else {
                    if (direction > 0) {
                        if (getThumbBounds().x + getThumbBounds().width
                                >= trackListener.getCurrentMouseX()) {
                            ((Timer) e.getSource()).stop();
                        }
                    } else if (getThumbBounds().x <= trackListener.getCurrentMouseX()) {
                        ((Timer) e.getSource()).stop();
                    }
                }
            } else {
                scrollByUnit(direction);
            }

            if (direction > 0
                    && scrollbar.getValue() + scrollbar.getVisibleAmount()
                    >= scrollbar.getMaximum()) {
                ((Timer) e.getSource()).stop();
            } else if (direction < 0
                    && scrollbar.getValue() <= scrollbar.getMinimum()) {
                ((Timer) e.getSource()).stop();
            }
        }
    }

    public class QuaquaPropertyChangeHandler extends BasicScrollBarUI.PropertyChangeHandler {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if ("Frame.active".equals(name)) {
                scrollbar.repaint();
            } else if ("Quaqua.ScrollBar.placeButtonsTogether".equals(name)) {
                updatePlaceButtonsTogether();
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(scrollbar);
            }
            super.propertyChange(e);
        }
    }

    /**
     * A listener to listen for model changes.
     *
     */
    protected class QuaquaModelListener extends BasicScrollBarUI.ModelListener {
        private boolean isValueAdjusting=false;
        @Override
        public void stateChanged(ChangeEvent e) {
            super.stateChanged(e);
            
            boolean newValue=scrollbar.getValueIsAdjusting();
            if (newValue!=isValueAdjusting) {
                isValueAdjusting=newValue;
                scrollbar.repaint();
            }
        }
    }
}
