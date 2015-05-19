/*
 * @(#)QuaquaTextComponentPopupHandler.java  
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
/**
 * TextComponentHandler displays a popup menu on a JTextComponent with the
 * cut/copy and paste actions.
 * The Quaqua text component UI's register a shared instance of
 * QuaquaTextComponentPopupHandler as a mouse listener on all JTextComponent's.
 *
 * @author Werner Randelshofer.
 * @version $Id: QuaquaTextComponentPopupHandler.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTextComponentPopupHandler extends MouseAdapter {
    private JPopupMenu popupMenu;
    private AbstractAction cutAction;
    private AbstractAction copyAction;
    private AbstractAction pasteAction;
    
    /** Creates a new instance. */
    public QuaquaTextComponentPopupHandler() {
        popupMenu = new JPopupMenu();
        popupMenu.add(cutAction = new DefaultEditorKit.CutAction());
        popupMenu.add(copyAction = new DefaultEditorKit.CopyAction());
        popupMenu.add(pasteAction = new DefaultEditorKit.PasteAction());
        
        cutAction.putValue(Action.NAME, UIManager.getString("TextComponent.cut"));
        copyAction.putValue(Action.NAME, UIManager.getString("TextComponent.copy"));
        pasteAction.putValue(Action.NAME, UIManager.getString("TextComponent.paste"));
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }
    
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }
    
    protected void showPopup(MouseEvent e) {
        JTextComponent src = (JTextComponent) e.getSource();
        
        boolean isFocusable = Methods.invokeGetter(src, "isFocusable", true);
        
        if (src.getClientProperty("Quaqua.TextComponent.showPopup") != Boolean.FALSE &&
                src.isEnabled() &&
                isFocusable &&
                Methods.invokeGetter(src,"getComponentPopupMenu",null) == null) {
            cutAction.setEnabled(! (src instanceof JPasswordField) &&
                    src.getSelectionEnd() > src.getSelectionStart() &&
                    src.isEditable()
                    );
            copyAction.setEnabled(! (src instanceof JPasswordField) &&
                    src.getSelectionEnd() > src.getSelectionStart()
                    );
            pasteAction.setEnabled(src.isEditable()
            );
            src.requestFocus();
            popupMenu.show(src, e.getX(), e.getY());
            e.consume();
        }
    }
}
