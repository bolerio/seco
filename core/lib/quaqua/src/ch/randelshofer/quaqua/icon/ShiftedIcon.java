/*
 * @(#)ShiftedIcon.java  1.0  May 12, 2006
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.icon;

import java.awt.*;
import javax.swing.*;
/**
 * ShiftedIcon renders a target icon at a different location and can return
 * different width and height values than the target.
 *
 * @author Werner Randelshofer.
 * @version 1.0 May 12, 2006 Created.
 */
public class ShiftedIcon implements Icon {
    private Icon target;
    private Rectangle shift;
    
    /** Creates a new instance. */
    public ShiftedIcon(Icon target, Point shift) {
        this.target = target;
        this.shift = new Rectangle(
                shift.x, shift.y, 
                target.getIconWidth(), 
                target.getIconHeight()
                );
    }
    public ShiftedIcon(Icon target, Rectangle shiftAndSize) {
        this.target = target;
        this.shift = shiftAndSize;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        target.paintIcon(c, g, x + shift.x, y + shift.y);
    }

    public int getIconWidth() {
        return shift.width;
    }

    public int getIconHeight() {
        return shift.height;
    }
    
}
