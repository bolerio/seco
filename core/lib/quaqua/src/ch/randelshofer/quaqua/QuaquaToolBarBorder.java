/*
 * @(#)QuaquaToolBarBorder.java
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.ext.batik.ext.awt.LinearGradientPaint;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaToolBarBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaToolBarBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaToolBarBorder
        extends AbstractBorder
        implements SwingConstants, BackgroundBorder {

    private final static Border backgroundBorder = new Border() {

        public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) gr;

            boolean isActive = QuaquaUtilities.isOnActiveWindow(c);

            String style = getStyle(c);
            if (style.equals("gradient") || style.equals("placard")) {
                g.setPaint(
                        new LinearGradientPaint(x, y, new Color(0xfdfdfd),x, height / 2, new Color(0xf5f5f5)));
                g.fillRect(x, y, width, height / 2);
                g.setColor(new Color(0xebebeb));
                g.fillRect(x, y + height / 2, width, height - height / 2);

            } else if (style.equals("bottom") /*&& isTextured*/) {
                Color[] gradient = (Color[]) UIManager.get(isActive ? "ToolBar.bottom.gradient" : "ToolBar.bottom.gradientInactive");
                    if (gradient == null) {
                        g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
                    } else if (gradient.length == 2) {
                        g.setPaint(
                                new LinearGradientPaint(new Point2D.Float(x, y + 1), new Point2D.Float(x, y + height - 2),
                                new float[]{0f, 1f},
                                gradient));
                    } else if (gradient.length == 3) {
                        g.setPaint(
                                new LinearGradientPaint(new Point2D.Float(x, y+1), new Point2D.Float(x, y+height -2),
                                new float[]{0f, 0.25f, 1f},
                                gradient));
                    } else if (gradient.length == 4) {
                        g.setPaint(
                                new LinearGradientPaint(new Point2D.Float(x, y+1), new Point2D.Float(x, y+height -2),
                                new float[]{0f, 0.25f, 0.5f, 1f},
                                gradient));
                    }
                    g.fillRect(x, y+1, width, height-1);
            } else if (style.equals("title")) {
                g.setPaint(PaintableColor.getPaint(UIManager.getColor("ToolBar.title.background"), c));
                g.fillRect(x, y, width, height);
            } else {
                g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
                g.fillRect(x, y, width, height);
            }
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    };

    @Override
    public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
        String style = getStyle(component);
        Color bright = UIManager.getColor("ToolBar.borderBright");
        Color dark = UIManager.getColor("ToolBar.borderDark");
        Color divider = UIManager.getColor(QuaquaUtilities.isOnActiveWindow(component) ?//
                "ToolBar." + style + ".borderDivider" : "ToolBar." + style + ".borderDividerInactive");
        if (divider == null) {
            divider = UIManager.getColor(QuaquaUtilities.isOnActiveWindow(component) ?//
                    "ToolBar.borderDivider" : "ToolBar.borderDividerInactive");
        }

        if ((component instanceof JToolBar) && ((((JToolBar) component).getUI()) instanceof QuaquaToolBarUI)) {
            JToolBar c = (JToolBar) component;
            boolean isDividerDrawn = isDividerDrawn(c);
            int dividerLocation = getDividerLocation(c);
            if (c.isFloatable()) {
                int hx = x, hy = y, hw = w, hh = h;
                if (isDividerDrawn) {
                    hx = (dividerLocation == WEST) ? x + 1 : x;
                    hy = (dividerLocation == NORTH) ? y + 1 : y;
                    hw = (dividerLocation == EAST || dividerLocation == WEST) ? w - 1 : w;
                    hh = (dividerLocation == SOUTH || dividerLocation == NORTH) ? h - 1 : h;
                }
                if (c.getOrientation() == HORIZONTAL) {
                    if (QuaquaUtilities.isLeftToRight(c)) {
                        g.setColor(bright);
                        g.fillRect(hx + 2, hy + 2, 1, hh - 4);
                        g.fillRect(hx + 5, hy + 2, 1, hh - 4);
                        g.setColor(dark);
                        g.fillRect(hx + 3, hy + 2, 1, hh - 4);
                        g.fillRect(hx + 6, hy + 2, 1, hh - 4);
                    } else {
                        g.setColor(bright);
                        g.fillRect(hw - hx - 3, hy + 2, 1, hh - 4);
                        g.fillRect(hw - hx - 5, hy + 2, 1, hh - 4);
                        g.setColor(dark);
                        g.fillRect(hw - hx - 2, hy + 2, 1, hh - 4);
                        g.fillRect(hw - hx - 6, hy + 2, 1, hh - 4);
                    }
                } else // vertical
                {
                    g.setColor(bright);
                    g.fillRect(hx + 2, hy + 2, hw - 4, 1);
                    g.fillRect(hx + 2, hy + 5, hw - 4, 1);
                    g.setColor(dark);
                    g.fillRect(hx + 2, hy + 3, hw - 4, 1);
                    g.fillRect(hx + 2, hy + 6, hw - 4, 1);
                }
            }
            if (isDividerDrawn) {
                g.setColor(divider);
                switch (dividerLocation) {
                    case NORTH:
                        g.fillRect(x, y, w, 1);
                        break;
                    case EAST:
                        g.fillRect(x + w - 1, y, 1, h);
                        break;
                    case SOUTH:
                        g.fillRect(x, y + h - 1, w, 1);
                        break;
                    case WEST:
                        g.fillRect(x, y, 1, h);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    private boolean isDividerDrawn(JToolBar c) {
        Object value = c.getClientProperty(QuaquaToolBarUI.TOOLBAR_DRAW_DIVIDER_PROPERTY);

        return value == null || value.equals(Boolean.TRUE);
    }

    /**
     * Returns SwingConstants.NORTH, .SOUTH, .EAST, .WEST or -1.
     */
    private int getDividerLocation(JToolBar c) {
        if (!((BasicToolBarUI) c.getUI()).isFloating() && c.getParent() != null) {
            Dimension parentSize = c.getParent().getSize();
            Insets parentInsets = c.getParent().getInsets();
            Rectangle bounds = c.getBounds();

            boolean fillsWidth = bounds.width >= parentSize.width - parentInsets.left - parentInsets.right;
            boolean fillsHeight = bounds.height >= parentSize.height - parentInsets.top - parentInsets.bottom;

            if (fillsWidth && fillsHeight) {
                return -1;
            }

            if (fillsWidth) {
                if (bounds.y == parentInsets.top) {
                    return SOUTH;
                } else {
                    return NORTH;
                }
            }

            if (fillsHeight) {
                if (bounds.x == parentInsets.left) {
                    return EAST;
                } else {
                    return WEST;
                }
            }
        }
        return -1;
    }

    @Override
    public Insets getBorderInsets(Component component, Insets newInsets) {
        if ((component instanceof JToolBar) && ((((JToolBar) component).getUI()) instanceof QuaquaToolBarUI)) {
            JToolBar c = (JToolBar) component;
            newInsets.top = newInsets.left = newInsets.bottom = newInsets.right = 0;
            String style = getStyle(component);
            if (style.equals("gradient") || style.equals("placard")) {
                return newInsets;
            }

            boolean isFloatable = c.isFloatable();
            if (isFloatable) {
                if (c.getOrientation() == HORIZONTAL) {
                    if (c.getComponentOrientation().isLeftToRight()) {
                        newInsets.left = 16;
                    } else {
                        newInsets.right = 16;
                    }
                } else {// vertical
                    newInsets.top = 16;
                }
            } else {
                if (c.getOrientation() == HORIZONTAL) {
                    if (c.getComponentOrientation().isLeftToRight()) {
                        //newInsets.left = 7;
                        newInsets.left = 4;
                    } else {
                        //newInsets.right = 7;
                        newInsets.right = 4;
                    }
                } else {// vertical
                    //newInsets.top = 16;
                }
            }
            if (isDividerDrawn(c)) {
                if (isFloatable && ((QuaquaToolBarUI) c.getUI()).isFloating()) {
                    newInsets.top++;
                    //newInsets.bottom++;
                    newInsets.right++;
                    //newInsets.left++;
                } else {
                    switch (getDividerLocation(c)) {
                        case SOUTH:
                            newInsets.bottom++;
                            break;
                        case EAST:
                            newInsets.right++;
                            break;
                        case NORTH:
                            newInsets.top++;
                            break;
                        case WEST:
                            newInsets.left++;
                            break;
                        default:
                            break;
                    }
                }
            }
            Insets margin = c.getMargin();

            if (margin != null) {
                newInsets.left += margin.left;
                newInsets.top += margin.top;
                newInsets.right += margin.right;
                newInsets.bottom += margin.bottom;
            }
            return newInsets;
        } else {
            return new Insets(0, 0, 0, 0);
        }
    }

    public Border getBackgroundBorder() {
        return backgroundBorder;
    }

    private static String getStyle(Component c) {
        Object style = null;
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            style = jc.getClientProperty(QuaquaToolBarUI.TOOLBAR_STYLE_PROPERTY);
        }
        if (style == null || !(style instanceof String)) {
            /*
            boolean isTextured = QuaquaUtilities.isOnTexturedWindow(c);
            if (isTextured) {
            JRootPane rootPane = SwingUtilities.getRootPane(c);
            int xOffset = 0, yOffset = 0;
            for (Component cc = c; cc != rootPane; cc = cc.getParent()) {
            xOffset += cc.getX();
            yOffset += cc.getY();
            }
            style = (yOffset == 0) ? "title" : "plain";
            } else {
            style = "plain";
            }*/
            style = "plain";
        }
        return (String) style;
    }

    public static class UIResource extends QuaquaToolBarBorder implements javax.swing.plaf.UIResource {
    }
}
