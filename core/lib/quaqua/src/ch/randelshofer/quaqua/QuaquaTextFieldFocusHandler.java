/*
 * @(#)QuaquaTextFieldFocusHandler.java
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.KeyboardFocusManager;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import sun.awt.CausedFocusEvent;
import sun.awt.CausedFocusEvent.Cause;

/**
 * QuaquaTextFieldFocusHandler. Selects all text of a JTextComponent, if
 * the user used a keyboard focus traversal key to transfer the focus on the
 * JTextComponent.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaTextFieldFocusHandler.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTextFieldFocusHandler implements FocusListener {

    private static QuaquaTextFieldFocusHandler instance;

    public static QuaquaTextFieldFocusHandler getInstance() {
        if (instance == null) {
            instance = new QuaquaTextFieldFocusHandler();
        }
        return instance;
    }

    /**
     * Allow instance creation by UIManager.
     */
    public QuaquaTextFieldFocusHandler() {
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
                    UIManager.getBoolean(uiProperty)) {
                if (event instanceof CausedFocusEvent) {
                    CausedFocusEvent cfEvent = (CausedFocusEvent) event;
                    if (cfEvent.getCause() == Cause.TRAVERSAL_FORWARD ||
                            cfEvent.getCause() == Cause.TRAVERSAL_BACKWARD) {
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

