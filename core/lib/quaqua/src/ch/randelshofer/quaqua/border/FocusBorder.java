/*
 * @(#)ButtonFocusBorder.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * A Border which only draws if the component has focus.
 *
 * @author  Werner Randelshofer
 * @version $Id: FocusBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class FocusBorder implements Border {
    private Border focusRing;
    
    /** Creates a new instance. */
    public FocusBorder(Border focusRing) {
        this.focusRing = focusRing;
    }
    
    public Insets getBorderInsets(Component c) {
        return focusRing.getBorderInsets(c);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c.isEnabled() &&
                QuaquaUtilities.isFocused(c)
        && (! (c instanceof AbstractButton) || ((AbstractButton) c).isFocusPainted())) {
                focusRing.paintBorder(c, g, x, y, width, height);
        }
    }
}
