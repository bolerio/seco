/*
 * @(#)DefaultCellEditor2.java  1.0  20 March 2005
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package test;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Please use  this default cell editor instead of
 * javax.swing.DefaultCellEditor.
 * <p>
 * This default cell editor properly honours the font settings of the table
 * which contains the cell editor, and it also properly renders a border
 * around the field which is currently being edit.
 *
 * @author  Werner Randelshofer
 * @version 1.0  20 March 2005  Created.
 */
public class DefaultCellEditor2 extends DefaultCellEditor {
    
    /**
     * Constructs a <code>DefaultCellEditor</code> that uses a text field.
     *
     * @param textField  a <code>JTextField</code> object
     */
    public DefaultCellEditor2(JTextField textField) {
        super(textField);
        textField.setBorder(new LineBorder(Color.black));
    }
    
    /**
     * Constructs a <code>DefaultCellEditor</code> object that uses a check box.
     *
     * @param checkBox  a <code>JCheckBox</code> object
     */
    public DefaultCellEditor2(JCheckBox checkBox) {
        super(checkBox);
        checkBox.setBorder(new LineBorder(Color.black));
    }
    
    /**
     * Constructs a <code>DefaultCellEditor</code> object that uses a
     * combo box.
     *
     * @param comboBox  a <code>JComboBox</code> object
     */
    public DefaultCellEditor2(JComboBox comboBox) {
        super(comboBox);
        comboBox.setBorder(new LineBorder(Color.black));
    }
}
