/*
 * @(#)QuaquaComboBoxEditor.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import javax.swing.*;
import javax.swing.plaf.*;

import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * The default editor for Quaqua editable combo boxes.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaComboBoxEditor.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaComboBoxEditor extends BasicComboBoxEditor {
    public QuaquaComboBoxEditor() {
        super();
        //editor.removeFocusListener(this);
        editor = new JTextField("",0) {
            // workaround for 4530952
            public void setText(String s) {
                if (getText().equals(s)) {
                    return;
                }
                super.setText(s);
            }
        };
        
       installKeyboardActions();
    }
    
    protected void installKeyboardActions() {
        InputMap km = getInputMap();
	if (km != null) {
	    SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED, km);
	}
    }

    /**
     * Get the InputMap to use for the UI.  
     */
    InputMap getInputMap() {
	InputMap map = new InputMapUIResource();
	InputMap shared = (InputMap)UIManager.get("ComboBox.editorInputMap");
	if (shared != null) {
	    map.setParent(shared);
	}
	return map;
    }

    /**
     * A subclass of BasicComboBoxEditor that implements UIResource.
     * BasicComboBoxEditor doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with BasicListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends QuaquaComboBoxEditor
    implements javax.swing.plaf.UIResource {
    }
}

