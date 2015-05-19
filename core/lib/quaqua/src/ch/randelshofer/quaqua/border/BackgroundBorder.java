/*
 * @(#)BackgroundBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import javax.swing.border.*;
/**
 * BackgroundBorder is used by the Quaqua Look And Feel to tag a
 * border which partially needs to be drawn on to the background of a JComponent.
 *
 * @author  Werner Randelshofer
 * @version $Id: BackgroundBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public interface BackgroundBorder extends Border {
    
    /**
     * Returns the border that needs to be drawn onto the background.
     */
    public Border getBackgroundBorder();
}
