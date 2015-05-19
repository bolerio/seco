/*
 * @(#)QuaquaFocusHandler.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.event.*;
import javax.swing.*;
/**
 * QuaquaFocusHandler.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaFocusHandler.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaFocusHandler implements FocusListener {
    private static QuaquaFocusHandler instance;
    
    public static QuaquaFocusHandler getInstance() {
        if (instance == null) {
            instance = new QuaquaFocusHandler();
        }
        return instance;
    }
    
    
    /**
     * Prevent instance creation.
     */
    private QuaquaFocusHandler() {
    }
    
    public void focusGained(FocusEvent event) {
            QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
    }
    
    public void focusLost(FocusEvent event) {
            QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
    }
}

