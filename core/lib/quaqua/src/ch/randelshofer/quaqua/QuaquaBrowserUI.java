/*
 * @(#)QuaquaBrowserUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.ViewportPainter;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;

/**
 * QuaquaBrowserUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaBrowserUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaBrowserUI extends BasicBrowserUI implements ViewportPainter {
    /**
     * This scrollbar is used to render vertical bars in the viewport.
     */
    private static JScrollBar verticalBar;
    private static JScrollBar getVerticalBar() {
        if (verticalBar == null) {
            verticalBar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1) {
                /**
                 * To use this scrollBar as a renderer pane component, we have to
                 * always return true in method isShowing.
                 */
                @Override
                public boolean isShowing() {
                    return true;
                }
                
                /**
                 * FIXME - Apparently we have to override paintChildren to enshure that the
                 * children of the scrollbar are actually painted.
                 * This seems odd. Maybe I am missing something here.
                 */
                @Override
                protected void paintChildren(Graphics g) {
                    Component[] c = getComponents();
                    for (int i=0; i < c.length; i++) {
                        Graphics cg = g.create(c[i].getX(), c[i].getY(), c[i].getWidth(), c[i].getHeight());
                        c[i].paint(cg);
                        cg.dispose();
                    }
                }
            };
        }
        return verticalBar;
    }
    
    /** Shared cell renderer pane. */
    private static CellRendererPane cellRendererPane;
    private static CellRendererPane getCellRendererPane() {
        if (cellRendererPane == null) {
            cellRendererPane = new CellRendererPane();
        }
        return cellRendererPane;
    }
    
    /**
     * Creates a new instance.
     */
    public QuaquaBrowserUI() {
    }
    
    /**
     * Returns an instance of the UI delegate for the specified component.
     * Each subclass must provide its own static <code>createUI</code>
     * method that returns an instance of that UI delegate subclass.
     * If the UI delegate subclass is stateless, it may return an instance
     * that is shared by multiple components.  If the UI delegate is
     * stateful, then it should return a new instance per component.
     * The default implementation of this method throws an error, as it
     * should never be invoked.
     */
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaBrowserUI();
    }
    
    public void paintViewport(Graphics g, JViewport c) {
        Dimension vs = c.getSize();
        Dimension bs = browser.getSize();
        
        JScrollBar vb = getVerticalBar();
        
        g.setColor(Color.black);
        Dimension ss = vb.getPreferredSize();
        
        // Paint scroll bar tracks at the right to fill the viewport
        if (bs.width < vs.width) {
            int fixedCellWidth = browser.getFixedCellWidth();
            
            // FIXME - Apparently we have to do layout manually, because
            //         invoking cellRendererPane.paneComponent with
            //         "shouldValidate" set to true does not appear to have
            //         the desired effect.
            try {
                vb.setSize(ss.width, vs.height);
                vb.doLayout();
            } catch (NullPointerException e) {
                // We get NPE here in JDK 1.3. We ignore it.
            }
            
            for (int x = browser.getWidth() + fixedCellWidth; x < vs.width; x += fixedCellWidth + ss.width) {
                getCellRendererPane().paintComponent(g, vb, c, x, 0, ss.width, vs.height, false);
            }
        }
    }
}
