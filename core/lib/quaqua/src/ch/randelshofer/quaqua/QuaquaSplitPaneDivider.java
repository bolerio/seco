/*
 * @(#)QuaquaSplitPaneDivider.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaSplitPaneDivider.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaSplitPaneDivider.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaSplitPaneDivider extends BasicSplitPaneDivider {

    static final Cursor defaultCursor =
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    /**
     * Width or height of the divider based on orientation
     * BasicSplitPaneUI adds two to this.
     */
    protected static final int ONE_TOUCH_SIZE_ = 4;
    protected static final int ONE_TOUCH_OFFSET_ = 2;

    // private ActionListener doubleClickHandler = new DoubleClickActionHandler();
    /**
     * Creates a new instance.
     */
    public QuaquaSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        setLayout(new QuaquaDividerLayout());
        setFocusable(UIManager.getBoolean("SplitPaneDivider.focusable"));
    }

    /**
     * Sets the SplitPaneUI that is using the receiver.
     */
    @Override
    public void setBasicSplitPaneUI(BasicSplitPaneUI newUI) {
        if (splitPane != null) {
            splitPane.removePropertyChangeListener(this);
            if (mouseHandler != null) {
                splitPane.removeMouseListener(mouseHandler);
                splitPane.removeMouseMotionListener(mouseHandler);
                removeMouseListener(mouseHandler);
                removeMouseMotionListener(mouseHandler);
                mouseHandler = null;
            }
        }
        splitPaneUI = newUI;
        if (newUI != null) {
            splitPane = newUI.getSplitPane();
            if (splitPane != null) {
                if (mouseHandler == null) {
                    mouseHandler = new QuaquaMouseHandler();
                }
                splitPane.addMouseListener(mouseHandler);
                splitPane.addMouseMotionListener(mouseHandler);
                addMouseListener(mouseHandler);
                addMouseMotionListener(mouseHandler);
                splitPane.addPropertyChangeListener(this);
                if (splitPane.isOneTouchExpandable()) {
                    oneTouchExpandableChanged();
                }
            }
        } else {
            splitPane = null;
        }
    }

    /**
     * Paints the divider.
     */
    @Override
    public void paint(Graphics g) {
        Dimension size = getSize();
        Insets insets = getInsets();
        boolean isHorizontal = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;

        // We support the following styles:
        // "bar": The divider is a bar, that goes accross the split view.
        // "thumb" (this is the default): The divider is a thumb (dimple) on
        //        the regular background pattern.
        String style = (String) splitPane.getClientProperty("Quaqua.SplitPane.style");
        if (style == null) {
            style = "thumb";
        }

        if (style.equals("bar")) {
            Border border = UIManager.getBorder(
                    (isHorizontal) ? "SplitPane.vBar" : "SplitPane.hBar");
            if (border != null) {
                border.paintBorder(
                        splitPane, g,
                        insets.left, insets.top,
                        size.width - insets.right - insets.left,
                        size.height - insets.top - insets.bottom);
            }
        }

        Icon dimple;
        boolean drawDimple;

        if (style.equals("thumb")) {
            dimple = UIManager.getIcon("SplitPane.thumbDimple");
            drawDimple = true;
        } else {
            dimple = UIManager.getIcon("SplitPane.barDimple");
            drawDimple = size.width >= dimple.getIconWidth()
                    && size.height >= dimple.getIconHeight();
        }

        if (drawDimple) {
            int x = (size.width - dimple.getIconWidth()) / 2;
            int y = (size.height - dimple.getIconHeight()) / 2;

            // Make sure, dimple does not intersect with the buttons
            if (splitPane.isOneTouchExpandable()) {
                if (isHorizontal) {
                    y = Math.min(y, leftButton.getY() - dimple.getIconHeight() - ONE_TOUCH_OFFSET_);
                } else {
                    x = Math.min(x, leftButton.getX() - dimple.getIconWidth() - ONE_TOUCH_OFFSET_);
                }
            }

            dimple.paintIcon(splitPane, g, x, y);
        }
        super.paint(g);
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the left component in the split pane.
     */
    @Override
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {

            @Override
            public Icon getIcon() {
                return UIManager.getIcon(
                        (splitPane.getOrientation() == VERTICAL) ? "SplitPane.leftArrow" : "SplitPane.upArrow");
            }
        };
        b.setBorder(null);
        b.setMinimumSize(new Dimension(ONE_TOUCH_SIZE_, ONE_TOUCH_SIZE_));
        b.setCursor(defaultCursor);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        b.setFocusable(UIManager.getBoolean("SplitPaneDivider.focusable"));
        return b;
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the right component in the split pane.
     */
    @Override
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {

            @Override
            public Icon getIcon() {
                return UIManager.getIcon(
                        (splitPane.getOrientation() == VERTICAL) ? "SplitPane.rightArrow" : "SplitPane.downArrow");
            }
        };
        b.setBorder(null);
        b.setMinimumSize(new Dimension(ONE_TOUCH_SIZE_, ONE_TOUCH_SIZE_));
        b.setCursor(defaultCursor);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        b.setFocusable(UIManager.getBoolean("SplitPaneDivider.focusable"));
        return b;
    }

    /**
     * Used to layout a <code>BasicSplitPaneDivider</code>.
     * Layout for the divider
     * involves appropriately moving the left/right buttons around.
     * <p>
     */
    protected class QuaquaDividerLayout implements LayoutManager {

        public void layoutContainer(Container c) {
            if (leftButton != null && rightButton != null
                    && c == QuaquaSplitPaneDivider.this) {
                if (splitPane.isOneTouchExpandable()) {
                    Insets insets = c.getInsets();

                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        //int extraX = (insets != null) ? insets.left : 0;
                        int blockSize = getHeight();
                        blockSize -= (insets.top + insets.bottom);
                        blockSize = Math.max(blockSize, 0);
                        blockSize = Math.min(blockSize, ONE_TOUCH_SIZE_);
                        int extraX = c.getSize().width
                                - insets.right
                                - ONE_TOUCH_OFFSET_ - blockSize * 4;

                        int y = (c.getSize().height - blockSize) / 2;

                        leftButton.setBounds(extraX, y,
                                blockSize * 2, blockSize);
                        rightButton.setBounds(extraX + blockSize * 2, y,
                                blockSize * 2, blockSize);
                    } else {
                        //int extraY = (insets != null) ? insets.top : 0;
                        int blockSize = getWidth();
                        blockSize -= (insets.left + insets.right);
                        blockSize = Math.max(blockSize, 0);
                        blockSize = Math.min(blockSize, ONE_TOUCH_SIZE_);
                        int extraY = c.getSize().height
                                - insets.bottom
                                - ONE_TOUCH_OFFSET_ - blockSize * 4;

                        int x = (c.getSize().width - blockSize) / 2;

                        leftButton.setBounds(x, extraY,
                                blockSize, blockSize * 2);
                        rightButton.setBounds(x, extraY + blockSize * 2,
                                blockSize, blockSize * 2);
                    }
                } else {
                    leftButton.setBounds(-5, -5, 1, 1);
                    rightButton.setBounds(-5, -5, 1, 1);
                }
            }
        }

        public Dimension minimumLayoutSize(Container c) {
            // NOTE: This isn't really used, refer to
            // BasicSplitPaneDivider.getPreferredSize for the reason.
            // I leave it in hopes of having this used at some point.
            if (c != QuaquaSplitPaneDivider.this || splitPane == null) {
                return new Dimension(0, 0);
            }
            Dimension buttonMinSize = null;

            if (splitPane.isOneTouchExpandable() && leftButton != null) {
                buttonMinSize = leftButton.getMinimumSize();
            }

            Insets insets = getInsets();
            int width = getDividerSize();
            int height = width;

            if (orientation == JSplitPane.VERTICAL_SPLIT) {
                if (buttonMinSize != null) {
                    int size = buttonMinSize.height;
                    if (insets != null) {
                        size += insets.top + insets.bottom;
                    }
                    height = Math.max(height, size);
                }
                width = 1;
            } else {
                if (buttonMinSize != null) {
                    int size = buttonMinSize.width;
                    if (insets != null) {
                        size += insets.left + insets.right;
                    }
                    width = Math.max(width, size);
                }
                height = 1;
            }
            return new Dimension(width, height);
        }

        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }

        public void removeLayoutComponent(Component c) {
        }

        public void addLayoutComponent(String string, Component c) {
        }
    } // End of class BasicSplitPaneDivider.DividerLayout

    /**
     * MouseHandler is responsible for converting mouse events
     * (released, dragged...) into the appropriate DragController
     * methods.
     * <p>
     */
    protected class QuaquaMouseHandler extends MouseHandler {

        /**
         * If dragger is not null it is messaged with completeDrag.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            // The following code is needed, because the mouseReleased implementation
            // in the superclass changes the divider location even when the
            // user hasn't moved it.
            int lastLoc = splitPane.getLastDividerLocation();
            int currentLoc = splitPane.getDividerLocation();
            super.mouseReleased(e);
            if (splitPane.getDividerLocation() == currentLoc) {
                splitPane.setLastDividerLocation(lastLoc);
            }
        }

        /**
         * Double click on the split bar moves it to the bottom or to the left.
         * If it is already at the bottom most or leftmost position, it is moved
         * to its last location.
         */
        @Override
        public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && splitPane.isOneTouchExpandable()) {
                boolean isHorizontal = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;

                Component leftC = splitPane.getLeftComponent();
                Component rightC = splitPane.getRightComponent();

                if (leftC == null || rightC == null) {
                    return;
                }
                Insets insets = splitPane.getInsets();

                int minLoc, maxLoc;
                if (isHorizontal) {
                    if (leftC.isVisible()) {
                        minLoc = leftC.getMinimumSize().width
                                + insets.left;
                    } else {
                        minLoc = insets.left;
                    }
                    if (rightC.isVisible()) {
                        maxLoc = splitPane.getWidth()
                                - rightC.getMinimumSize().width
                                - insets.right
                                - getSize().width;
                    } else {
                        maxLoc = splitPane.getWidth() - insets.right;
                    }
                } else {
                    if (leftC.isVisible()) {
                        minLoc = leftC.getMinimumSize().height
                                + insets.top;
                    } else {
                        minLoc = insets.top;
                    }
                    if (rightC.isVisible()) {
                        maxLoc = splitPane.getHeight()
                                - rightC.getMinimumSize().height
                                - insets.bottom
                                - getSize().height;
                    } else {
                        maxLoc = splitPane.getHeight() - insets.bottom;
                    }
                }
                maxLoc = Math.max(0, maxLoc);
                minLoc = Math.max(0, Math.min(minLoc, maxLoc));

                // FIXME We will provide a client property on the split
                // bar which can be used to specify into which direction the
                // split bar shall be moved on double click.
                if (isHorizontal) {
                    int helper = maxLoc;
                    maxLoc = minLoc;
                    minLoc = helper;
                }

                int lastLoc = splitPane.getLastDividerLocation();
                int currentLoc = splitPaneUI.getDividerLocation(splitPane);
                int newLoc;
                if (currentLoc == maxLoc) {
                    newLoc = lastLoc;
                } else {
                    newLoc = maxLoc;
                }

                if (currentLoc != newLoc) {
                    splitPane.setDividerLocation(newLoc);
                    // We do this in case the dividers notion of the location
                    // differs from the real location.
                    splitPane.setLastDividerLocation(currentLoc);
                }
            }
        }
    }
}
