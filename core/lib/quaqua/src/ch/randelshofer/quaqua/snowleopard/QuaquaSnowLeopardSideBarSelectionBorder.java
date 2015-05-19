/*
 * @(#)QuaquaSnowLeopardSideBarSelectionBorder.java
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.snowleopard;

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
 * QuaquaSnowLeopardSideBarSelectionBorder.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaSnowLeopardSideBarSelectionBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaSnowLeopardSideBarSelectionBorder implements Border, UIResource {

    /** Creates a new instance. */
    public QuaquaSnowLeopardSideBarSelectionBorder() {
    }

    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) gr;
        if (QuaquaUtilities.isFocused(c)) {
            // top line: 0x5896d0
            g.setColor(new Color(0x5896d0));
            g.fillRect(x, y, width, 1);
            g.setPaint(new LinearGradientPaint(
                    x, y + 1, new Color(0x6ea6d6),
                    x, y + height - 1, new Color(0x216cb7)
                    ));
        } else {
            if (QuaquaUtilities.isOnActiveWindow(c, true)) {
                // top line: 0xa2b1cb
                g.setColor(new Color(0xa2b1cb));
                g.fillRect(x, y, width, 1);
                g.setPaint(new LinearGradientPaint(
                        x, y + 1, new Color(0xb1bfd8),
                        x, y + height - 1, new Color(0x8296b8)
                        ));

            } else {
                // top line: 0xa8a8a8
                g.setColor(new Color(0xa8a8a8));
                g.fillRect(x, y, width, 1);
                g.setPaint(new LinearGradientPaint(
                        x, y + 1, new Color(0xc1c1c1),
                        x, y + height - 1, new Color(0x9c9c9c)));
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
