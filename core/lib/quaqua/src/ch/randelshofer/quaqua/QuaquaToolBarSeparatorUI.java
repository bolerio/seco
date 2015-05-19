/*
 * @(#)QuaquaToolBarSeparatorUI.java  1.2  2006-06-16
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
/**
 * QuaquaToolBarSeparatorUI.
 *
 * @author  Werner Randelshofer
 * @version 1.2 2006-06-16 Fixed size values again. 
 * <br>1.1 2006-02-12 Fixed size values and drawing code.
 * <br>1.0  28 March 2005  Created.
 */
public class QuaquaToolBarSeparatorUI extends BasicToolBarSeparatorUI {
    private final static Stroke separatorStroke =
    new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
    new float[] {1f,2f}, 0f);
    
    public static ComponentUI createUI( JComponent c ) {
        return new QuaquaToolBarSeparatorUI();
    }
    protected void installDefaults( JSeparator s ) {
        super.installDefaults(s);
        s.setForeground(UIManager.getColor("ToolBarSeparator.foreground"));
    }
    
    
    public void paint( Graphics gr, JComponent c ) {
        Graphics2D g = (Graphics2D) gr;
        int width = c.getWidth();
        int height = c.getHeight();
        g.setColor(c.getForeground());
        g.setStroke(separatorStroke);
        JToolBar.Separator sep = (JToolBar.Separator) c;
        if (sep.getOrientation() == JSeparator.HORIZONTAL) {
            g.drawLine(2,height / 2, width - 3, height / 2);
        } else {
            g.drawLine(width / 2, 2, width / 2, height - 3);
        }
        Debug.paint(g, c, this);
    }
    
    public Dimension getMinimumSize( JComponent c ) {
        //    return new Dimension(11, 11);
        JToolBar.Separator sep = (JToolBar.Separator) c;
        if (sep.getOrientation() == JSeparator.HORIZONTAL) {
            return new Dimension(1, 11);
        } else {
            return new Dimension(11, 1);
        }
    }
    public Dimension getPreferredSize( JComponent c ) {
      //      return new Dimension(11, 11);
        JToolBar.Separator sep = (JToolBar.Separator) c;
        if (sep.getOrientation() == JSeparator.HORIZONTAL) {
            return new Dimension(1, 11);
        } else {
            return new Dimension(11, 1);
        }
    }
    public Dimension getMaximumSize( JComponent c ) {
        JToolBar.Separator sep = (JToolBar.Separator) c;
        if (sep.getOrientation() == JSeparator.HORIZONTAL) {
            return new Dimension(Integer.MAX_VALUE, 11);
        } else {
            return new Dimension(11, Integer.MAX_VALUE);
        }
    }
}
