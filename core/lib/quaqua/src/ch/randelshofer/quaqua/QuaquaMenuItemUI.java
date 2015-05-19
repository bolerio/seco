/*
 * @(#)QuaquaMenuItemUI.java 
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
import javax.swing.border.Border;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * A replacement for the AquaMenuItemUI.
 * <p>
 * This class provides the following workarounds for issues in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>MenuItems are highlighted with a blue background color and white text and
 * accelerator instead of higlighting with a striped background, white text and black
 * accelerator.
 * </li>
 * <li>Menu item accelerators use character symbols instead of writing "Meta",
 * "Delete" or "Backspace".
 * </li>
 * </ul>
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id: QuaquaMenuItemUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaMenuItemUI extends BasicMenuItemUI
        implements QuaquaMenuPainterClient {

    boolean fIsScreenMenuItem = false;
    int fType;
    static final int kPlain = 0;
    static final int kCheckBox = 1;
    static final int kRadioButton = 2;
    static final String[] sPropertyPrefixes = {"MenuItem", "CheckBoxMenuItem", "RadioButtonMenuItem"};
    // BasicMenuUI also uses this.
    //Handler handler;

    QuaquaMenuItemUI(int type) {
        fType = type;
    }

    public static ComponentUI createUI(JComponent jcomponent) {
        int i = 0;
        if (jcomponent instanceof JCheckBoxMenuItem) {
            i = 1;
        } else if (jcomponent instanceof JRadioButtonMenuItem) {
            i = 2;
        }
        return new QuaquaMenuItemUI(i);
    }

    protected String getPropertyPrefix() {
        return sPropertyPrefixes[fType];
    }

    /*
    protected void installListeners() {
    if ((mouseInputListener = createMouseInputListener(menuItem)) != null) {
    menuItem.addMouseListener(mouseInputListener);
    menuItem.addMouseMotionListener(mouseInputListener);
    }
    if ((menuDragMouseListener = createMenuDragMouseListener(menuItem)) != null) {
    menuItem.addMenuDragMouseListener(menuDragMouseListener);
    }
    if ((menuKeyListener = createMenuKeyListener(menuItem)) != null) {
    menuItem.addMenuKeyListener(menuKeyListener);
    if (menuKeyListener instanceof PropertyChangeListener) {
    menuItem.addPropertyChangeListener((PropertyChangeListener) menuKeyListener);
    }
    }
    }*/
    protected void updateListenersForScreenMenuItem() {
        setIsScreenMenu(true);
    }

    protected void setIsScreenMenu(boolean bool) {
        if (fIsScreenMenuItem != bool) {
            fIsScreenMenuItem = bool;
            if (fIsScreenMenuItem) {
                removeListeners();
            } else {
                addListeners();
            }
        }
    }

    protected void removeListeners() {
        //menuItem.removeMouseListener(getHandler());
        //menuItem.removeMouseMotionListener(getHandler());
        //menuItem.removeMenuDragMouseListener(getHandler());
        menuItem.removeMouseListener(mouseInputListener);
        menuItem.removeMouseMotionListener(mouseInputListener);
        menuItem.removeMenuDragMouseListener(menuDragMouseListener);
    }

    protected void addListeners() {
        menuItem.addMouseListener(mouseInputListener);
        menuItem.addMouseMotionListener(mouseInputListener);
        menuItem.addMenuDragMouseListener(menuDragMouseListener);
    }

    protected void paintMenuItem(Graphics g, JComponent c,
            Icon checkIcon, Icon arrowIcon, Color background,
            Color foreground, int defaultTextIconGap) {

        QuaquaMenuPainter.getInstance().paintMenuItem(this, g, c, checkIcon,
                arrowIcon, background, foreground,
                disabledForeground,
                selectionForeground, defaultTextIconGap,
                acceleratorFont);
    }

    protected Dimension getPreferredMenuItemSize(JComponent c,
            Icon checkIcon,
            Icon arrowIcon,
            int defaultTextIconGap) {
        return QuaquaMenuPainter.getInstance().getPreferredMenuItemSize(c, checkIcon, arrowIcon, defaultTextIconGap, acceleratorFont);
    }

    public void update(Graphics graphics, JComponent jcomponent) {
        this.paint(graphics, jcomponent);
    }

    public void paintBackground(Graphics g, JComponent component, int menuWidth, int menuHeight) {
        Color bgColor = selectionBackground;
        AbstractButton menuItem = (AbstractButton) component;
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();

        if (menuItem.isOpaque()) {
            if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected())) {
                //g.setColor(bgColor);
                ((Graphics2D) g).setPaint(PaintableColor.getPaint(bgColor, component));
                g.fillRect(0, 0, menuWidth, menuHeight);
            } else {
                //g.setColor(menuItem.getBackground());
                ((Graphics2D) g).setPaint(PaintableColor.getPaint(menuItem.getBackground(), component));
                g.fillRect(0, 0, menuWidth, menuHeight);
            }
            g.setColor(oldColor);
        }
/*
        if (component.getBorder() instanceof BackgroundBorder) {
            Border b = ((BackgroundBorder) component.getBorder()).getBackgroundBorder();
            b.paintBorder(component, g, 0, 0, component.getWidth(), component.getHeight());
    }*/
}
}
