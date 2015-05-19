/*
 * @(#)QuaquaMenuUI.java 
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
/**
 * A replacement for the AquaMenuUI.
 * <p>
 * This class does not fix any particular bug in the Mac LAF or the Aqua LAF.
 * It is just here to achieve a consistent look with the other Quaqua menu UI
 * classes.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaMenuUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaMenuUI extends BasicMenuUI implements QuaquaMenuPainterClient {
    // BasicMenuUI also uses this.
    //Handler handler;

    public static ComponentUI createUI(JComponent x) {
        return new QuaquaMenuUI();
    }
    @Override
    protected void installDefaults() {
        super.installDefaults();
	QuaquaUtilities.installProperty(menuItem, "opaque", Boolean.TRUE);
        //menuItem.setOpaque(true);
    }
    /*
    protected MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
	return getHandler();
    }
    
    protected MouseInputListener createMouseInputListener(JComponent c) {
	return getHandler();
    }
    */
    @Override
    protected void paintMenuItem(Graphics g, JComponent c,
    Icon checkIcon, Icon arrowIcon, Color background,
    Color foreground, int defaultTextIconGap) {
        QuaquaMenuPainter.getInstance().paintMenuItem(this, g, c, checkIcon,
        arrowIcon, background, foreground,
        disabledForeground,
        selectionForeground, defaultTextIconGap,
        acceleratorFont);
    }
    
    @Override
    protected Dimension getPreferredMenuItemSize(JComponent c,
    Icon checkIcon,
    Icon arrowIcon,
    int defaultTextIconGap) {
        Dimension d = QuaquaMenuPainter.getInstance()
        .getPreferredMenuItemSize(c, checkIcon, arrowIcon, defaultTextIconGap, acceleratorFont);
        return d;
    }
    
    
    public void paintBackground(Graphics gr, JComponent component, int menuWidth, int menuHeight) {
        AbstractButton menuItem = (AbstractButton) component;
        
        if(menuItem.isOpaque()) {
            Graphics2D g = (Graphics2D) gr;
            Color oldColor = g.getColor();
            boolean isTopLevel = ((JMenu) menuItem).isTopLevelMenu();
            ButtonModel model = menuItem.getModel();
            boolean isSelected = model.isArmed() || (menuItem instanceof JMenu && model.isSelected());
            if (isSelected) {
                g.setPaint(PaintableColor.getPaint(selectionBackground, menuItem));
            } else {
                if (isTopLevel && component.getParent() != null) {
                    g.setPaint(PaintableColor.getPaint(component.getParent().getBackground(), menuItem));
                } else {
                    g.setPaint(PaintableColor.getPaint(menuItem.getBackground(), menuItem));
                }
            }
            g.fillRect(0,0, menuWidth, menuHeight);
            
            if (isTopLevel) {
                String bbName = (isSelected) ? "MenuBar.selectedBorder" : "MenuBar.border";
                if (UIManager.getBorder(bbName) instanceof BackgroundBorder) {
                    Border bb = ((BackgroundBorder) UIManager.getBorder(bbName)).getBackgroundBorder();
                    bb.paintBorder(component, gr, 0, 0, menuWidth, menuHeight);
                }
                
                Color shadow = UIManager.getColor("MenuBar.shadow");
                if (shadow != null) {
                    g.setColor(shadow);
                    g.fillRect(0, menuHeight - 1, menuWidth, 1);
                }
            }
            g.setColor(oldColor);
        }
        
        if (component.getBorder() instanceof BackgroundBorder) {
            Border b = ((BackgroundBorder) component.getBorder()).getBackgroundBorder();
            b.paintBorder(component, gr, 0, 0, component.getWidth(), component.getHeight());
        }
    }

}

