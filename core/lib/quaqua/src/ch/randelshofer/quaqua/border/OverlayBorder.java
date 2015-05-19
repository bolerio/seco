/*
 * @(#)OverlayBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import java.awt.*;
import javax.swing.border.*;
/**
 * OverlayBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: OverlayBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class OverlayBorder implements Border {
    private Border[] borders;
    
    /** Creates a new instance. */
    public OverlayBorder(Border first, Border second) {
        borders = new Border[] { first, second };
    }
    
    public Insets getBorderInsets(Component c) {
        return (Insets) borders[0].getBorderInsets(c).clone();
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        for (int i=0; i < borders.length; i++) {
            borders[i].paintBorder(c, g, x, y, width, height);
        }
    }    
}
