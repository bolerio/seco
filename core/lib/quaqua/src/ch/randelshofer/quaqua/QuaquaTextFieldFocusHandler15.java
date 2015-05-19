/*
 * @(#)QuaquaTextFieldFocusHandler15.java
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.KeyboardFocusManager;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
/**
 * QuaquaTextFieldFocusHandler15. Selects all text of a JTextComponent, if
 * the user used a keyboard focus traversal key, to transfer the focus on the
 * JTextComponent.
 * <p>
 * This class is here for backwards compatibility with J2SE5.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaTextFieldFocusHandler15.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTextFieldFocusHandler15 implements FocusListener {
    private static QuaquaTextFieldFocusHandler15 instance;
    
    public static QuaquaTextFieldFocusHandler15 getInstance() {
        if (instance == null) {
            instance = new QuaquaTextFieldFocusHandler15();
        }
        return instance;
    }
    
    /**
     * Allow instance creation by UIManager.
     */
    public QuaquaTextFieldFocusHandler15() {
    }
    
    public void focusGained(FocusEvent event) {
        QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
        
        final JTextComponent tc = (JTextComponent) event.getSource();
        if (tc.isEditable() && tc.isEnabled()) {
            
            String uiProperty;
            if (tc instanceof JPasswordField) {
                uiProperty = "PasswordField.autoSelect";
            } else if (tc instanceof JFormattedTextField) {
                uiProperty = "FormattedTextField.autoSelect";
            } else {
                uiProperty = "TextField.autoSelect";
            }
            
            if (tc.getClientProperty("Quaqua.TextComponent.autoSelect") == Boolean.TRUE ||
                    tc.getClientProperty("Quaqua.TextComponent.autoSelect") == null &&
                    UIManager.getBoolean(uiProperty)
                    ) {
                if (KeyboardFocusManager.getCurrentKeyboardFocusManager() instanceof QuaquaKeyboardFocusManager) {
                    QuaquaKeyboardFocusManager kfm = (QuaquaKeyboardFocusManager) KeyboardFocusManager.getCurrentKeyboardFocusManager();
                    if (event.getOppositeComponent() == kfm.getLastKeyboardTraversingComponent()) {
                        tc.selectAll();
                    }
                }
            }
        }
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager() instanceof QuaquaKeyboardFocusManager) {
            QuaquaKeyboardFocusManager kfm = (QuaquaKeyboardFocusManager) KeyboardFocusManager.getCurrentKeyboardFocusManager();
            kfm.setLastKeyboardTraversingComponent(null);
        }
    }
    
    public void focusLost(FocusEvent event) {
        QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
    }
}

