/*
 * @(#)QuaquaLeopardComboBoxPopupBorder.java
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.leopard;

import ch.randelshofer.quaqua.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A replacement for the AquaComboBoxPopupBorder.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaLeopardComboBoxPopupBorder.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaLeopardComboBoxPopupBorder implements Border {

    protected static Insets popupBorderInsets;
    protected static Insets itemBorderInsets;

    public void paintBorder(Component component, Graphics gr, int x,
            int y, int width, int height) {
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // Punch out a hole and then draw a rounded rectangle over it
        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0xffffff, true));
        g.fillRect(x, y, width, height);
        g.setComposite(composite);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(x, y, width, height, 10f, 10f));
        QuaquaUtilities.endGraphics(g, oldHints);
    }

    public Insets getBorderInsets(Component component) {
        return new Insets(4, 0, 4, 0);
    }

    public boolean isBorderOpaque() {
        return false;
    }
}
