/*
 * @(#)QuaquaLeopardSideBarSelectionBorder.java 
 *
 * Copyright (c) 20011 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.lion;

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
 * @version $Id: QuaquaLionSideBarSelectionBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaLionSideBarSelectionBorder implements Border, UIResource {

    /** Creates a new instance. */
    public QuaquaLionSideBarSelectionBorder() {
    }

    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) gr;
        if (QuaquaUtilities.isFocused(c)) {
            // first top line: 
            g.setColor(new Color(0x60a5dd));
            g.fillRect(x, y, width, 1);
            // second top line
            g.setColor(new Color(0x75bae8));
            g.fillRect(x, y + 1, width, 1);
            // gradient
            g.setPaint(new LinearGradientPaint(
                    x, y + 2, new Color(0x6db1e3),
                    x, y + height - 3, new Color(0x3b89d0)
                    ));
            g.fillRect(x, y + 2, width, height - 3);

            // bottom line
            g.setColor(new Color(0x377cc0));
            g.fillRect(x, y + height - 1, width, 1);
        } else {
            if (QuaquaUtilities.isOnActiveWindow(c, true)) {
                // first top line: 
                g.setColor(new Color(0xbbc5d6));
                g.fillRect(x, y, width, 1);
                // second top line
                g.setColor(new Color(0xc3cde0));
                g.fillRect(x, y + 1, width, 1);
                // gradient
                g.setPaint(new LinearGradientPaint(
                        x, y + 2, new Color(0xbdc7dc),
                        x, y + height - 3, new Color(0x9dabc4)
                        ));
                g.fillRect(x, y + 2, width, height - 3);

                // bottom line
                g.setColor(new Color(0x94a1b9));
                g.fillRect(x, y + height - 1, width, 1);

            } else {
                // first top line: 
                g.setColor(new Color(0xbbc5d6));
                g.fillRect(x, y, width, 1);
                // second top line
                g.setColor(new Color(0xc4ccdf));
                g.fillRect(x, y + 1, width, 1);
                // gradient
                g.setPaint(new LinearGradientPaint(
                        x, y + 2, new Color(0xbdc7dc),
                        x, y + height - 3, new Color(0x9dabc4)
                        ));
                g.fillRect(x, y + 2, width, height - 3);

                // bottom line
                g.setColor(new Color(0x94a1b9));
                g.fillRect(x, y + height - 1, width, 1);

            }
        }
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    public boolean isBorderOpaque() {
        return true;
    }
}
