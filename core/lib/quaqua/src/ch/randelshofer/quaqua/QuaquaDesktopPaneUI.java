/*
 * @(#)QuaquaDesktopPaneUI.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
/**
 * QuaquaDesktopPaneUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaDesktopPaneUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaDesktopPaneUI extends BasicDesktopPaneUI {
    
    /** Creates a new instance. */
    public QuaquaDesktopPaneUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaDesktopPaneUI();
    }
}
