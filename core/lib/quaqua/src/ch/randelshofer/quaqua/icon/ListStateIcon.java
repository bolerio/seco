/*
 * @(#)ListStateIcon.java  
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.icon;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.UIResource;

/**
 * {@code ListStateIcon} draws two different icons depending on the
 * selection state of a list item.
 * The component must set the client property "Quaqua.selected" to true. 
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-04 Created.
 */
public class ListStateIcon implements Icon, UIResource {
    private Icon icon;
    private Icon selectedIcon;

    public ListStateIcon(Icon icon, Icon selectedIcon) {
        this.icon = icon;
        this.selectedIcon = selectedIcon;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        boolean isSelected=false;
        if (c instanceof JComponent) {
            isSelected=(Boolean)((JComponent)c).getClientProperty("Quaqua.selected");
        }
        if (isSelected) {
         selectedIcon.paintIcon(c,g,x,y);   
        } else {
        icon.paintIcon(c,g,x,y);
        }
    }

    public int getIconWidth() {
        return icon.getIconWidth();
    }

    public int getIconHeight() {
        return icon.getIconHeight();
    }
    
}
