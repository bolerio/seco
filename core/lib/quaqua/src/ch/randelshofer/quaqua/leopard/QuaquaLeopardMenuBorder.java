/*
 * @(#)QuaquaLeopardMenuBorder.java
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
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.UIResource;

/**
 * A replacement for the AquaMenuBorder.
 * <ul>
 * <li>Draws a border at the top and the bottom of JPopupMenu's.
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaLeopardMenuBorder.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaLeopardMenuBorder implements Border, UIResource {

    private static Insets popupBorderInsets;
    private static Insets itemBorderInsets;

    public void paintBorder(Component component, Graphics gr, int x,
            int y, int width, int height) {
        /* empty */

        if (component instanceof JPopupMenu) {
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

            JPopupMenu pm = (JPopupMenu) component;
            Shape fillShape = null;
            if (pm.getInvoker() instanceof JMenu) {
                Rectangle invokerB = pm.getInvoker().getBounds();
                invokerB.setLocation(pm.getInvoker().getLocationOnScreen());
                Rectangle pmB = pm.getBounds();
                pmB.setLocation(pm.getLocationOnScreen());
                if (pmB.y >= invokerB.y + invokerB.height) {
                    // Menu is below invoker : +-+
                    //                         | |
                    //                         \_/ shape
                    GeneralPath p = new GeneralPath();
                    p.moveTo(x, y);
                    p.lineTo(x + width, y);
                    p.lineTo(x + width, y + height - 6);
                    p.quadTo(x + width, y + height, x + width - 6, y + height);
                    p.lineTo(x + 6, y + height);
                    p.quadTo(x, y + height, x, y + height - 6);
                    p.closePath();
                    fillShape = p;
                } else if (pmB.x >= invokerB.x + invokerB.width) {
                    // Menu is on the right of invoker : +-\
                    //                                   | |
                    //                                   \_/ shape
                    GeneralPath p = new GeneralPath();
                    p.moveTo(x, y);
                    p.lineTo(x + width - 6, y);
                    p.quadTo(x + width, y, x + width, y + 6);
                    p.lineTo(x + width, y + height - 6);
                    p.quadTo(x + width, y + height, x + width - 6, y + height);
                    p.lineTo(x + 6, y + height);
                    p.quadTo(x, y + height, x, y + height - 6);
                    p.closePath();
                    fillShape = p;
                } else if (pmB.x + pmB.width <= invokerB.x) {
                    // Menu is on the left of invoker :  /-+
                    //                                   | |
                    //                                   \_/ shape
                    GeneralPath p = new GeneralPath();
                    p.moveTo(x, y+6);
                    p.quadTo(x, y,x+6,y);
                    p.lineTo(x + width, y);
                    p.lineTo(x + width, y + height - 6);
                    p.quadTo(x + width, y + height, x + width - 6, y + height);
                    p.lineTo(x + 6, y + height);
                    p.quadTo(x, y + height, x, y + height - 6);
                    p.closePath();
                    fillShape = p;
                }
            }
            if (fillShape == null) {
                fillShape = new RoundRectangle2D.Float(x, y, width, height, 10f, 10f);
            }
            g.fill(fillShape);

            QuaquaUtilities.endGraphics(g, oldHints);
        }
    }

    public Insets getBorderInsets(Component component) {
        Insets insets;

        if (component instanceof JPopupMenu) {
            if (popupBorderInsets == null) {
                popupBorderInsets = new Insets(4, 0, 4, 0);
            }
            insets = (Insets) popupBorderInsets.clone();
        } else {
            if (itemBorderInsets == null) {
                itemBorderInsets = new Insets(0, 0, 0, 0);
            }
            insets = (Insets) itemBorderInsets.clone();
        }
        return insets;
    }

    public boolean isBorderOpaque() {
        return false;
    }
}
