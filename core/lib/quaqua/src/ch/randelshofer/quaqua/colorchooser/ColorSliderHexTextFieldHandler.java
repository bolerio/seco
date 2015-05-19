/*
 * @(#)ColorSliderHexTextFieldHandler.java 
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.colorchooser;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

/**
 * ColorSliderHexTextFieldHandler.
 *
 * @author Werner Randelshofer
 * @version $Id: ColorSliderHexTextFieldHandler.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class ColorSliderHexTextFieldHandler extends ColorSliderTextFieldHandler {
    
    /** Creates a new instance. */
    public ColorSliderHexTextFieldHandler(JTextField textField, ColorSliderModel ccModel, int component) {
        super(textField, ccModel, component);
    }
    
    protected void docChanged() {
        if (textField.hasFocus()) {
            BoundedRangeModel brm = ccModel.getBoundedRangeModel(component);
            try {
                int value = Integer.decode("#"+textField.getText()).intValue();
                if (brm.getMinimum() <= value && value <= brm.getMaximum()) {
                    brm.setValue(value);
                }
            } catch (NumberFormatException e) {
                // Don't change value if it isn't numeric.
            }
        }
    }
    public void stateChanged(ChangeEvent e) {
        if (! textField.hasFocus()) {
            String str = Integer.toHexString(
                    ccModel.getBoundedRangeModel(component).getValue()).
                    toUpperCase();
            textField.setText(str.length() == 2 ? str : "0"+str);
        }
    }
}
