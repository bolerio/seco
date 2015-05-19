/*
 * @(#)PaintableColor.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import java.awt.*;
import javax.swing.*;

/**
 * This is a Color with an optional 'hidden' Paint attribute.
 * This is used to pass our Paint objects 'through' the Swing API, so that users
 * of our Look and Feel can work with Paint objects like with regular colors,
 * but Quaqua UI components will paint using the Paint instead of with the
 * Color.
 *
 * @author  Werner Randelshofer
 * @version $Id: PaintableColor.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public abstract class PaintableColor extends Color {
    /**
     * Creates a new instance.
     */
    public PaintableColor(int argb, boolean hasAlpha) {
        super(argb, hasAlpha);
    }
    public PaintableColor(int rgb) {
        super(rgb);
    }
    public PaintableColor(int r, int g, int b) {
        super(r, g, b);
    }
    public PaintableColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }
    
    public final Paint getPaint(Component c, int xOffset, int yOffset) {
        return getPaint(c, xOffset, yOffset, c.getWidth(), c.getHeight());
    }
    public abstract Paint getPaint(Component c, int x, int y, int width, int height);
    
    /**
     * If the Color is an instance of PaintableColor, returns a Paint
     * object which is properly configured for drawing on the component.
     * If the Color is not an instance of PaintableColor, returns the color.
     */
    public static Paint getPaint(Color color, Component c) {
        return getPaint(color, c, 0, 0);
    }
    /**
     * xOffset and yOffset are used to achieve the shifted texture effect that
     * is used to render tabbed panes with Jaguar design.
     *
     * @param xOffset shifts the paint on the x-axis.
     * @param yOffset shifts the paint on the y-axis.
     */
    public static Paint getPaint(Color color, Component c, int xOffset, int yOffset) {
        return (color instanceof PaintableColor) 
        ? ((PaintableColor) color).getPaint(c, xOffset, yOffset) 
        : color;
    }
    public static Paint getPaint(Color color, Component c, int x, int y, int width, int height) {
        return (color instanceof PaintableColor)
        ? ((PaintableColor) color).getPaint(c, x, y, width, height)
        : color;
    }
    
    /**
     * Returns the relative position (offset) of the component towards its 
     * root pane.
     */
    protected static Point getRootPaneOffset(Component c) {
        int x = 0, y = 0;
        
        if (! (c instanceof Window)) {
        while (c != null && ! (c instanceof JRootPane)) {
            x -= c.getX();
            y -= c.getY();
            c = c.getParent();
        }
        }
        return new Point(x, y);
    }    
}
