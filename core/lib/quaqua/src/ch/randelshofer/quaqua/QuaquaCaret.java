/*
 * @(#)QuaquaCaret.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.*;
import java.awt.event.*;
import javax.swing.UIManager;

import javax.swing.plaf.*;
import javax.swing.text.*;

/**
 * QuaquaCaret.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaCaret.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaCaret extends DefaultCaret
        implements UIResource {

    boolean isFocused = false;

    public QuaquaCaret(Window window, JTextComponent textComponent) {
    }

    @Override
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return QuaquaHighlighter.painterInstance;
    }

    @Override
    public void setVisible(boolean bool) {
            if (bool == true) {
            // Don't display the caret if text is selected.
                bool = getDot() == getMark();
            }
        super.setVisible(bool);
    }

    @Override
    public boolean isVisible() {
        boolean bool = super.isVisible();
        // Display non-blinking caret when component is non-editable.
        if (UIManager.getBoolean("TextComponent.showNonEditableCaret")) {
        bool|= !getComponent().isEditable() && getComponent().isFocusOwner();
        }else{
        bool&= getComponent().isEditable() && getComponent().isFocusOwner();
        }
        return bool;
    }

    @Override
    protected void fireStateChanged() {
        if (isFocused) {
            setVisible(getComponent().isEditable());
        }
        super.fireStateChanged();
    }

    /**
     * Called when the component containing the caret gains
     * focus.  This is implemented to set the caret to visible
     * if the component is editable.
     *
     * @param evt the focus event
     * @see FocusListener#focusGained
     */
    @Override
    public void focusGained(FocusEvent evt) {
        JTextComponent component = getComponent();
        if (component.isEnabled()) {
            isFocused = true;
        }
        if (component.isEnabled()) {
            // if (component.isEditable()) {
            setVisible(true);
            // }
            setSelectionVisible(true);
        }
    }

    @Override
    public void focusLost(FocusEvent focusevent) {
        isFocused = false;
        super.focusLost(focusevent);
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        if (!evt.isPopupTrigger()) {
            super.mousePressed(evt);
        }
    }
}
