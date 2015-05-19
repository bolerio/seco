/*
 * @(#)QuaquaButtonMarginBorder.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * QuaquaButtonMarginBorder is used to honour the margins between button text 
 * and button border.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaButtonMarginBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaButtonMarginBorder extends AbstractBorder {
    
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    
    /**
     * Reinitializes the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     * @return the <code>insets</code> object
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Insets margin = b.getMargin();
            /*
            if (margin == null) {
                margin = getDefaultMargin((JComponent) c);
            }*/
            if (margin != null) {
                insets.top += margin.top;
                insets.left += margin.left;
                insets.bottom += margin.bottom;
                insets.right += margin.right;
            }
        }
        return insets;
    }
}
