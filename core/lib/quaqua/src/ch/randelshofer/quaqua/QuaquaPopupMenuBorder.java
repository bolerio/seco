/*
 * @(#)QuaquaPopupMenuBorder.java
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.border.*;
/**
 * A replacement for the AquaMenuBorder.
 * <p>
 * This class provides the following workaround for a bug in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Draws a border at the top and the bottom of JPopupMenu's.
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaPopupMenuBorder.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaPopupMenuBorder implements Border {
    public void paintBorder(Component component, Graphics g, int x,
    int y, int width, int height) {
        /* empty */
    }
    
    public Insets getBorderInsets(Component component) {
return new Insets(4, 0, 4, 0);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
}