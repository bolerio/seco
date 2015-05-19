/*
 * @(#)QuaquaLeopardSideBarSelectionBorder.java 
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.leopard;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.ext.batik.ext.awt.LinearGradientPaint;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.*;
import javax.swing.plaf.UIResource;

/**
 * QuaquaLeopardSideBarSelectionBorder.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaLeopardSideBarSelectionBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaLeopardSideBarSelectionBorder implements Border, UIResource {
    
    /** Creates a new instance. */
    public QuaquaLeopardSideBarSelectionBorder() {
    }
    
    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) gr;
        if (QuaquaUtilities.isFocused(c)) {
            // top line: 0x4580c8
            g.setColor(new Color(0x4580c8));
            g.fillRect(x, y, width, 1);
            g.setPaint(new LinearGradientPaint(
                    x, y + 1, new Color(0x5c93d5),
                    x, y + height - 1, new Color(0x1a58ad)
                    
                    ));
        } else {
            if (QuaquaUtilities.isOnActiveWindow(c, true)) {
                // top line: 0x91a0c0
            g.setColor(new Color(0x91a0c0));
            g.fillRect(x, y, width, 1);
                g.setPaint(new LinearGradientPaint(
                        x, y + 1,new Color(0xa9b1d0),
                        x, y + height - 1,new Color(0x6e81a9)
                        
                        ));
                
            } else {
                // top line: 0x979797
            g.setColor(new Color(0x979797));
            g.fillRect(x, y, width, 1);
                g.setPaint(new LinearGradientPaint(
                        x, y + 1,new Color(0xb4b4b4),
                        x, y + height - 1,new Color(0x8a8a8a)
                        ));
            }
        }
        g.fillRect(x, y + 1, width, height - 1);
    }
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }
    
    public boolean isBorderOpaque() {
        return true;
    }
    
}
