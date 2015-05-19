/*
 * @(#)QuaquaSliderUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.util.InsetsUtil;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaSliderUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaSliderUI.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaSliderUI extends BasicSliderUI
        implements VisuallyLayoutable {

    private Handler handler;
    private transient boolean isDragging;

    public static ComponentUI createUI(JComponent b) {
        return new QuaquaSliderUI((JSlider) b);
    }

    public QuaquaSliderUI(JSlider b) {
        super(b);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        QuaquaUtilities.installProperty(c, "opaque", UIManager.get("Slider.opaque"));
    }
    
    @Override
    public void installDefaults(JSlider slider) {
        super.installDefaults(slider);
                
        focusInsets = getVisualMargin(slider);
        slider.setRequestFocusEnabled(UIManager.getBoolean("Slider.requestFocusEnabled"));
        slider.setFocusable(UIManager.getBoolean("CheckBox.focusable"));
    }

    @Override
    protected void uninstallListeners(JSlider slider) {
        super.uninstallListeners(slider);
        handler = null;
    }

    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new QuaquaTrackListener();
    }

    @Override
    protected ChangeListener createChangeListener(JSlider slider) {
        return getHandler();
    }

    @Override
    protected ComponentListener createComponentListener(JSlider slider) {
        return getHandler();
    }

    @Override
    protected FocusListener createFocusListener(JSlider slider) {
        return getHandler();
    }

    @Override
    protected ScrollListener createScrollListener(JSlider slider) {
        return new ScrollListener();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener(
            JSlider slider) {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Override
    protected Dimension getThumbSize() {
        Icon thumb = getThumbIcon();
        return new Dimension(thumb.getIconWidth(), thumb.getIconHeight());
    }

    protected boolean isSmall() {
            return QuaquaUtilities.getSizeVariant(slider)==QuaquaUtilities.SizeVariant.SMALL;
    }

    protected Icon getThumbIcon() {
        String suffix = isSmall() ? ".small" : "";
        if (slider.getPaintTicks()) {
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                if (QuaquaUtilities.isLeftToRight(slider)) {
                    return UIManager.getIcon("Slider.southThumb" + suffix);
                } else {
                    return UIManager.getIcon("Slider.northThumb" + suffix);
                }
            } else {
                if (QuaquaUtilities.isLeftToRight(slider)) {
                    return UIManager.getIcon("Slider.eastThumb" + suffix);
                } else {
                    return UIManager.getIcon("Slider.westThumb" + suffix);
                }
            }
        } else {
            return UIManager.getIcon("Slider.roundThumb" + suffix);
        }
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);
        super.paint(g, c);
        QuaquaUtilities.endGraphics(g, oldHints);
        Debug.paint(g, c, this);
    }

    @Override
    public void paintThumb(Graphics g) {
        Rectangle knobBounds = thumbRect;
        int x = knobBounds.x;
        int y = knobBounds.y;
        getThumbIcon().paintIcon(slider, g, x, y);
    }

    @Override
    public void paintLabels(Graphics g) {
        g.setColor(slider.getForeground());
        super.paintLabels(g);
    }

    @Override
    public void paintFocus(Graphics g) {
    // empty
    }

    @Override
    protected void calculateGeometry() {
        focusInsets = getVisualMargin(slider);
        super.calculateGeometry();
    }
    @Override
    protected void calculateContentRect() {
        contentRect.x = focusRect.x + focusInsets.left;
        contentRect.y = focusRect.y + focusInsets.top;
        contentRect.width = focusRect.width - (focusInsets.left + focusInsets.right);
        contentRect.height = focusRect.height - (focusInsets.top + focusInsets.bottom);
    }

    @Override
    protected void calculateThumbLocation() {
        if (slider.getSnapToTicks()) {
            int sliderValue = slider.getValue();
            int snappedValue = sliderValue;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            if (tickSpacing != 0) {
                // If it's not on a tick, change the value
                if ((sliderValue - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (sliderValue - slider.getMinimum()) / (float) tickSpacing;
                    int whichTick = Math.round(temp);
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != sliderValue) {
                    slider.setValue(snappedValue);
                }
            }
        }

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int valuePosition = xPositionForValue(slider.getValue());

            thumbRect.x = valuePosition - (thumbRect.width / 2);
            thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2;
        /*
        if (slider.getPaintTicks()) {
        if (QuaquaUtilities.isLeftToRight(slider)) {
        thumbRect.y += 3;
        } else {
        thumbRect.y -= 3;
        }
        }*/
        } else {
            int valuePosition = yPositionForValue(slider.getValue());

            thumbRect.x = trackRect.x + (trackRect.width - thumbRect.width) / 2;
            thumbRect.y = valuePosition - (thumbRect.height / 2);
        /*
        if (slider.getPaintTicks()) {
        if (QuaquaUtilities.isLeftToRight(slider)) {
        thumbRect.x += 3;
        } else {
        thumbRect.x -= 3;
        }
        }*/
        }
    }

    @Override
    protected void calculateLabelRect() {
        if (slider.getPaintLabels()) {
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                labelRect.x = tickRect.x - trackBuffer;
                labelRect.width = tickRect.width + (trackBuffer * 2);
                labelRect.height = getHeightOfTallestLabel();
                if (QuaquaUtilities.isLeftToRight(slider)) {
                    labelRect.y = tickRect.y + tickRect.height;
                } else {
                    labelRect.y = tickRect.y - labelRect.height;
                }
            } else {
                labelRect.y = tickRect.y - trackBuffer;
                labelRect.height = tickRect.height + (trackBuffer * 2);
                labelRect.width = getWidthOfWidestLabel();
                if (QuaquaUtilities.isLeftToRight(slider)) {
                    labelRect.x = tickRect.x + tickRect.width;
                } else {
                    labelRect.x = tickRect.x - labelRect.width;
                }
            }
        } else {
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                labelRect.x = tickRect.x;
                labelRect.y = tickRect.y + tickRect.height;
                labelRect.width = tickRect.width;
                labelRect.height = 0;
            } else {
                if (QuaquaUtilities.isLeftToRight(slider)) {
                    labelRect.x = tickRect.x + tickRect.width;
                } else {
                    labelRect.x = tickRect.x;
                }
                labelRect.y = tickRect.y;
                labelRect.width = 0;
                labelRect.height = tickRect.height;
            }
        }
    }

    @Override
    protected void calculateTickRect() {
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            tickRect.x = trackRect.x;
            tickRect.width = trackRect.width;
            tickRect.height = getTickLength();

            if (QuaquaUtilities.isLeftToRight(slider)) {
                tickRect.y = trackRect.y + trackRect.height;
            } else {
                tickRect.y = trackRect.y - tickRect.height;
            }
            if (!slider.getPaintTicks()) {
                --tickRect.y;
                tickRect.height = 0;
            }
        } else {
            tickRect.y = trackRect.y;
            tickRect.height = trackRect.height;
            tickRect.width = getTickLength();
            if (QuaquaUtilities.isLeftToRight(slider)) {
                tickRect.x = trackRect.x + trackRect.width;
            } else {
                tickRect.x = trackRect.x - tickRect.width - 1;
            }

            if (!slider.getPaintTicks()) {
                --tickRect.x;
                tickRect.width = 0;
            }
        }
    }

    @Override
    protected void calculateTrackRect() {
        int centerSpacing = 0; // used to center sliders added using BorderLayout.CENTER (bug 4275631)
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            centerSpacing = thumbRect.height;
            if (QuaquaUtilities.isLeftToRight(slider)) {
                if (slider.getPaintTicks()) {
                    centerSpacing += getTickLength();
                }
                if (slider.getPaintLabels()) {
                    centerSpacing += getHeightOfTallestLabel();
                }
            } else {
                if (slider.getPaintTicks()) {
                    centerSpacing -= getTickLength();
                }
                if (slider.getPaintLabels()) {
                    centerSpacing -= getHeightOfTallestLabel();
                }
            }
            trackRect.x = contentRect.x + trackBuffer;
            trackRect.y = contentRect.y + (contentRect.height - centerSpacing - 1) / 2 + 1;
            trackRect.width = contentRect.width - (trackBuffer * 2);
            trackRect.height = thumbRect.height - 2;
        } else {
            centerSpacing = thumbRect.width;
            if (QuaquaUtilities.isLeftToRight(slider)) {
                if (slider.getPaintTicks()) {
                    centerSpacing += getTickLength();
                }
                if (slider.getPaintLabels()) {
                    centerSpacing += getWidthOfWidestLabel();
                }
            } else {
                if (slider.getPaintTicks()) {
                    centerSpacing -= getTickLength();
                }
                if (slider.getPaintLabels()) {
                    centerSpacing -= getWidthOfWidestLabel();
                }
            }
            trackRect.x = contentRect.x + (contentRect.width - centerSpacing - 1) / 2 + 1;
            trackRect.y = contentRect.y + trackBuffer;
            trackRect.width = thumbRect.width - 2;
            trackRect.height = contentRect.height - (trackBuffer * 2);
        }
    }

    @Override
    public void paintTrack(Graphics g) {
        int cx, cy, cw, ch;
        int pad;

        Rectangle trackBounds = trackRect;

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int index = slider.isEnabled() ? 0 : 1;
            Border border = ((Border[]) UIManager.get("Slider.horizontalTracks"))[index];
            Insets insets = border.getBorderInsets(slider);
            int offset = 0;
            if (slider.getPaintTicks()) {
                if (isSmall()) {
                    offset = (QuaquaUtilities.isLeftToRight(slider)) ? -1 : 1;
                } else {
                    offset = (QuaquaUtilities.isLeftToRight(slider)) ? -3 : 3;
                }
            }
            border.paintBorder(
                    slider,
                    g,
                    trackBounds.x - thumbRect.width / 2 + 3,
                    trackBounds.y + (trackBounds.height - insets.top - insets.bottom) / 2 + offset,
                    trackBounds.width + thumbRect.width - 6,
                    insets.top + insets.bottom);
        } else {
            int index = slider.isEnabled() ? 0 : 1;
            Border border = ((Border[]) UIManager.get("Slider.verticalTracks"))[index];
            Insets insets = border.getBorderInsets(slider);
            int offset = 0;
            if (slider.getPaintTicks()) {
                if (isSmall()) {
                    offset = (QuaquaUtilities.isLeftToRight(slider)) ? -1 : 1;
                } else {
                    offset = (QuaquaUtilities.isLeftToRight(slider)) ? -3 : 3;
                }
            }

            int x = trackBounds.x + (trackBounds.width - insets.left - insets.right) / 2;
            border.paintBorder(
                    slider,
                    g,
                    x + offset,
                    trackBounds.y - thumbRect.height / 2 + 3,
                    insets.left + insets.right,
                    trackBounds.height + thumbRect.height - 6);
        }
    }

    @Override
    public void paintTicks(Graphics g) {
        Rectangle tickBounds = tickRect;
        int centerEffect, tickHeight;

        g.setColor(UIManager.getColor("Slider.tickColor"));

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            g.translate(0, tickBounds.y);

            int value = slider.getMinimum();
            int xPos = 0;

            if (slider.getMinorTickSpacing() > 0) {
                int offset = 0;
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    offset = tickBounds.height - tickBounds.height / 2;
                    g.translate(0, offset);
                }
                while (value <= slider.getMaximum()) {
                    xPos = xPositionForValue(value);
                    paintMinorTickForHorizSlider(g, tickBounds, xPos);
                    value += slider.getMinorTickSpacing();
                }
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(0, -offset);
                }
            }

            if (slider.getMajorTickSpacing() > 0) {
                value = slider.getMinimum();
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(0, 1);
                }

                while (value <= slider.getMaximum()) {
                    xPos = xPositionForValue(value);
                    paintMajorTickForHorizSlider(g, tickBounds, xPos);
                    value += slider.getMajorTickSpacing();
                }
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(0, -1);
                }
            }

            g.translate(0, -tickBounds.y);
        } else {
            g.translate(tickBounds.x, 0);

            int value = slider.getMinimum();
            int yPos = 0;

            if (slider.getMinorTickSpacing() > 0) {
                int offset = 0;
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    offset = tickBounds.width - tickBounds.width / 2;
                    g.translate(offset, 0);
                }

                while (value <= slider.getMaximum()) {
                    yPos = yPositionForValue(value);
                    paintMinorTickForVertSlider(g, tickBounds, yPos);
                    value += slider.getMinorTickSpacing();
                }

                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(-offset, 0);
                }
            }

            if (slider.getMajorTickSpacing() > 0) {
                value = slider.getMinimum();
                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(2, 0);
                }

                while (value <= slider.getMaximum()) {
                    yPos = yPositionForValue(value);
                    paintMajorTickForVertSlider(g, tickBounds, yPos);
                    value += slider.getMajorTickSpacing();
                }

                if (!QuaquaUtilities.isLeftToRight(slider)) {
                    g.translate(-2, 0);
                }
            }
            g.translate(-tickBounds.x, 0);
        }
    }

    InputMap getInputMap(int condition, JSlider slider) {
        if (condition == JComponent.WHEN_FOCUSED) {
            InputMap keyMap = (InputMap) UIManager.get(
                    "Slider.focusInputMap");
            InputMap rtlKeyMap;

            if (slider.getComponentOrientation().isLeftToRight() ||
                    ((rtlKeyMap = (InputMap) UIManager.get(
                    "Slider.focusInputMap.RightToLeft")) == null)) {
                return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }

    public Insets getVisualMargin(JSlider tc) {
        Insets margin = (Insets) tc.getClientProperty("Quaqua.Component.visualMargin");
        if (margin == null) {
            margin = UIManager.getInsets("Component.visualMargin");
        }
        return (margin == null) ? new Insets(0, 0, 0, 0) : (Insets) margin.clone();
    }

    public Rectangle getVisualBounds(JComponent c, int type, int width, int height) {
        Rectangle bounds = new Rectangle(0, 0, width, height);
        if (type == VisuallyLayoutable.CLIP_BOUNDS) {
            return bounds;
        }

        JSlider b = (JSlider) c;
        if (type == VisuallyLayoutable.COMPONENT_BOUNDS) {
            Border border = b.getBorder();
            if (border == null || border instanceof UIResource) {
                InsetsUtil.subtractInto(getVisualMargin(b), bounds);
            }
        }
        return bounds;
    }

    private class Handler implements ChangeListener,
            ComponentListener, FocusListener, PropertyChangeListener {
        // Change Handler
        public void stateChanged(ChangeEvent e) {
            if (!isDragging) {
                calculateThumbLocation();
                slider.repaint();
            }
        }

        // Component Handler
        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentResized(ComponentEvent e) {
            calculateGeometry();
            slider.repaint();
        }

        public void componentShown(ComponentEvent e) {
        }

        // Focus Handler
        public void focusGained(FocusEvent e) {
            slider.repaint();
        }

        public void focusLost(FocusEvent e) {
            slider.repaint();
        }

        // Property Change Handler
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name == "orientation" ||
                    name == "inverted" ||
                    name == "labelTable" ||
                    name == "majorTickSpacing" ||
                    name == "minorTickSpacing" ||
                    name == "paintTicks" ||
                    name == "paintTrack" ||
                    name == "paintLabels") {
                calculateGeometry();
                slider.repaint();
            } else if (name == "componentOrientation") {
                calculateGeometry();
                slider.repaint();
                InputMap km = getInputMap(JComponent.WHEN_FOCUSED, slider);
                SwingUtilities.replaceUIInputMap(slider,
                        JComponent.WHEN_FOCUSED, km);
            } else if (name == "model") {
                ((BoundedRangeModel) e.getOldValue()).removeChangeListener(
                        changeListener);
                ((BoundedRangeModel) e.getNewValue()).addChangeListener(
                        changeListener);
                calculateThumbLocation();
                slider.repaint();
            } else if (name == "Frame.active") {
                slider.repaint(thumbRect);
       } else if (name.equals("JComponent.sizeVariant")) {
            QuaquaUtilities.applySizeVariant(slider);
            }
        }
    }

    /**
     * Track mouse movements.
     *
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of {@code Foo}.
     */
    public class QuaquaTrackListener extends TrackListener {
        //protected transient int offset;
        //protected transient int currentMouseX, currentMouseY;
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            offset = 0;
            scrollTimer.stop();

            // This is the way we have to determine snap-to-ticks.  It's
            // hard to explain but since ChangeEvents don't give us any
            // idea what has changed we don't have a way to stop the thumb
            // bounds from being recalculated.  Recalculating the thumb
            // bounds moves the thumb over the current value (i.e., snapping
            // to the ticks).
            if (slider.getSnapToTicks() /*|| slider.getSnapToValue()*/) {
                isDragging = false;
                slider.setValueIsAdjusting(false);
            } else {
                slider.setValueIsAdjusting(false);
                isDragging = false;
            }
            slider.repaint();
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
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();
            
            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            // Clicked in the Thumb area?
            if (thumbRect.contains(currentMouseX, currentMouseY)) {
                switch (slider.getOrientation()) {
                    case JSlider.VERTICAL:
                        offset = currentMouseY - thumbRect.y;
                        break;
                    case JSlider.HORIZONTAL:
                        offset = currentMouseX - thumbRect.x;
                        break;
                }
                isDragging = true;
                return;
            }
            isDragging = false;
            slider.setValueIsAdjusting(true);

            Dimension sbSize = slider.getSize();
            int direction = POSITIVE_SCROLL;

            switch (slider.getOrientation()) {
                case JSlider.VERTICAL:
                    if (thumbRect.isEmpty()) {
                        int scrollbarCenter = sbSize.height / 2;
                        if (!drawInverted()) {
                            direction = (currentMouseY < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        } else {
                            direction = (currentMouseY < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        }
                    } else {
                        int thumbY = thumbRect.y;
                        if (!drawInverted()) {
                            direction = (currentMouseY < thumbY) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        } else {
                            direction = (currentMouseY < thumbY) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        }
                    }
                    break;
                case JSlider.HORIZONTAL:
                    if (thumbRect.isEmpty()) {
                        int scrollbarCenter = sbSize.width / 2;
                        if (!drawInverted()) {
                            direction = (currentMouseX < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        } else {
                            direction = (currentMouseX < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        }
                    } else {
                        int thumbX = thumbRect.x;
                        if (!drawInverted()) {
                            direction = (currentMouseX < thumbX) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        } else {
                            direction = (currentMouseX < thumbX) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        }
                    }
                    break;
            }
            scrollDueToClickInTrack(direction);
            Rectangle r = thumbRect;
            if (!r.contains(currentMouseX, currentMouseY)) {
                if (shouldScroll(direction)) {
                    scrollTimer.stop();
                    scrollListener.setDirection(direction);
                    scrollTimer.start();
                }
            }
        }

        @Override
        public boolean shouldScroll(int direction) {
            Rectangle r = thumbRect;
            if (slider.getOrientation() == JSlider.VERTICAL) {
                if (drawInverted() ? direction < 0 : direction > 0) {
                    if (r.y + r.height <= currentMouseY) {
                        return false;
                    }
                } else if (r.y >= currentMouseY) {
                    return false;
                }
            } else {
                if (drawInverted() ? direction < 0 : direction > 0) {
                    if (r.x + r.width >= currentMouseX) {
                        return false;
                    }
                } else if (r.x <= currentMouseX) {
                    return false;
                }
            }

            if (direction > 0 && slider.getValue() + slider.getExtent() >=
                    slider.getMaximum()) {
                return false;
            } else if (direction < 0 && slider.getValue() <=
                    slider.getMinimum()) {
                return false;
            }

            return true;
        }

        /** 
         * Set the models value to the position of the top/left
         * of the thumb relative to the origin of the track.
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            int thumbMiddle = 0;

            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (!isDragging) {
                return;
            }

            slider.setValueIsAdjusting(true);

            switch (slider.getOrientation()) {
                case JSlider.VERTICAL:
                    int halfThumbHeight = thumbRect.height / 2;
                    int thumbTop = e.getY() - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);
                    int vMax = yPositionForValue(slider.getMaximum() -
                            slider.getExtent());

                    if (drawInverted()) {
                        trackBottom = vMax;
                    } else {
                        trackTop = vMax;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setThumbLocation(thumbRect.x, thumbTop);

                    thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setValue(valueForYPosition(thumbMiddle));
                    break;
                case JSlider.HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = e.getX() - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);
                    int hMax = xPositionForValue(slider.getMaximum() -
                            slider.getExtent());

                    if (drawInverted()) {
                        trackLeft = hMax;
                    } else {
                        trackRight = hMax;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setThumbLocation(thumbLeft, thumbRect.y);

                    thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setValue(valueForXPosition(thumbMiddle));
                    break;
                default:
                    return;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        return -1;
    }
}
