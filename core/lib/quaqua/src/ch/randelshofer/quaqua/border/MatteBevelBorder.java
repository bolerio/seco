/*
 * @(#)MatteBevelBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import java.awt.*;
import javax.swing.border.*;
/**
 * MatteBevelBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: MatteBevelBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class MatteBevelBorder implements Border {
    private Insets borderInsets;
    private Border bevelBorder;
    
    /** Creates a new instance. */
    public MatteBevelBorder(Insets borderInsets, Border bevelBorder) {
        this.borderInsets = borderInsets;
        this.bevelBorder = bevelBorder;
    }
    

    public Insets getBorderInsets(Component c) {
        return (Insets) borderInsets.clone();
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        bevelBorder.paintBorder(c, g, x, y, width, height);
    }
    
}
