/*
 * @(#)QuaquaPanelUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
/**
 * QuaquaPanelUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaPanelUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaPanelUI extends BasicPanelUI {
    // Shared UI object
    private static PanelUI panelUI;
    
    public static ComponentUI createUI(JComponent c) {
        if(panelUI == null) {
            panelUI = new QuaquaPanelUI();
        }
        return panelUI;
    }
    @Override
    protected void installDefaults(JPanel p) {
        super.installDefaults(p);
	QuaquaUtilities.installProperty(p, "opaque", UIManager.get("Panel.opaque"));
    }
    
    @Override
    protected void uninstallDefaults(JPanel p) {
        super.uninstallDefaults(p);
    }
    
    public static boolean isInTabbedPane(Component comp) {
        if(comp == null)
            return false;
        Container parent = comp.getParent();
        while (parent != null) {
            if (parent instanceof JTabbedPane) {
                return true;
            } else if (parent instanceof JRootPane) {
                return false;
            } else if (parent instanceof RootPaneContainer) {
                return false;
            } else if (parent instanceof Window) {
                return false;
            }
            parent = parent.getParent();
        }
        return false;
    }
    
    @Override
    public void paint(Graphics gr, JComponent c) {
            Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);
        if (c.isOpaque()) {
            g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        
        Border backgroundBorder = null;
        Insets insets = new Insets(0,0,0,0);
        if (c.getBorder() instanceof BackgroundBorder) {
            backgroundBorder = ((BackgroundBorder) c.getBorder()).getBackgroundBorder();
        } else if (c.getBorder() instanceof TitledBorder) {
            Border titledBorderBorder = ((TitledBorder) c.getBorder()).getBorder();
            if (titledBorderBorder instanceof BackgroundBorder) {
                backgroundBorder = ((BackgroundBorder) titledBorderBorder).getBackgroundBorder();
                insets = c.getBorder().getBorderInsets(c);
            }
        }
        if (backgroundBorder != null) {
            backgroundBorder.paintBorder(c, gr, insets.left, insets.top, c.getWidth() - insets.left - insets.right, c.getHeight() - insets.top - insets.bottom);
        }
        
        Debug.paint(gr, c, this);
        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
    }
}
