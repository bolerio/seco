/*
 * @(#)CompositeIcon.java  1.0  20 March 2005
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.icon;

import javax.swing.*;
import java.awt.*;
/**
 * OverlayIcon.
 *
 * @author  Werner Randelshofer
 * @version 1.0  20 March 2005  Created.
 */
public class OverlayIcon implements Icon {
    private Icon[] icons;
    
    /**
     * Creates a new instance.
     * Constructor with objects only used by BasicQuaquaLookAndFeel classes.
     * This constructor helps to reduce startuc latency, because we don't
     * need to load the Icon interface during the first creation of 
     * BasicQuaquaLookAndFeel class.
     */
    public OverlayIcon(Object first, Object second) {
        this((Icon) first, (Icon) second);
    }
    /** Creates a new instance. */
    public OverlayIcon(Icon first, Icon second) {
        this.icons = new Icon[] { first, second };
    }
    
    public int getIconHeight() {
        return icons[0].getIconHeight();
    }
    
    public int getIconWidth() {
        return icons[0].getIconWidth();
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        for (int i=0; i < icons.length; i++) {
            if (icons[i]!=null) {
            icons[i].paintIcon(c, g, x, y);
            }
        }
    }    
}
