/*
 * @(#)FocusedIcon.java  1.0  2011-07-26
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.icon;

import ch.randelshofer.quaqua.border.AbstractFocusedPainter;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Draws a focus ring around the opaque pixels of an icon.
 * The icon must provide space for the focus ring.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-26 Created.
 */
public class FocusedIcon extends AbstractFocusedPainter implements Icon {

    private Icon actualIcon;
    private final static int slack=3;

    public FocusedIcon(Icon actualIcon) {
        this.actualIcon = actualIcon;
    }

    @Override
    public int getIconHeight() {
        return actualIcon.getIconHeight();
    }

    @Override
    public int getIconWidth() {
        return actualIcon.getIconWidth();
    }

    @Override
    public void paintIcon( Component c,  Graphics g,  int x,  int y) {
        paint(c, g, x-slack, y-slack, getIconWidth()+slack*2, getIconHeight()+slack*2);
    }
    @Override
    protected void doPaint( Component c,  Graphics g,  int x,  int y, int w, int h) {
        actualIcon.paintIcon(c, g, x+slack, y+slack);
    }
}
