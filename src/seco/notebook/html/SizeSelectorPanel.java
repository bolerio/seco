/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package seco.notebook.html;

import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.Dimension;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.KeyListener;
import java.awt.event.FocusListener;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

/**
 * Panel to show and manipulate a CSS size value
 *
 * <p>Added support for negative integers in stage 8.</p>
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version stage 11, April 27, 2003
 */

public class SizeSelectorPanel extends JPanel implements AttributeComponent,
    ActionListener
{

  private Object attributeKey;
  private Object htmlAttrKey;
  private JSpinner valueSelector;
  private JComboBox unitSelector;
  private JLabel unitName;
  //private LengthValue lv;

  private int setValueCalls = 0;
  private int originalValue = 0;
  private String originalUnit;
  private boolean allowNegative = false;

  public static final String UNIT_PT = "pt";
  public static final String UNIT_PERCENT = "%";
  public static final String[] UNIT_VALUES = {UNIT_PT, UNIT_PERCENT};

  public static final int UNIT_TYPE_PT = 0;
  public static final int UNIT_TYPE_PERCENT = 1;

  public static final int TYPE_NONE = 0;
  public static final int TYPE_LABEL = 1;
  public static final int TYPE_COMBO = 2;

  /**
   * construct a basic SizeSelectorPanel with a
   * JSpinner to select a value
   *
   * @param key  the attribute key this instance of SizeSelectionPanel
   *      represents
   * @param allowNegative  true, if negative values are to be allowed in the
   *      panel, false if not
   */
  public SizeSelectorPanel(Object key, Object htmlKey, boolean allowNegative) {
    super(new FlowLayout());
    attributeKey = key;
    htmlAttrKey = htmlKey;
    valueSelector = new JSpinner(new SpinnerNumberModel());
    Dimension dim = new Dimension(50, 24);
    valueSelector.setMinimumSize(dim);
    valueSelector.setPreferredSize(dim);
    add(valueSelector);
    originalUnit = getUnit();
    this.allowNegative = allowNegative;
  }

  /**
   * construct a SizeSelectorPanel with a
   * JSpinner to select a value and either a JComboBox to select a given
   * unit for the selection value or a JLabel showing a fixed unit.
   *
   * @param key  the attribute key this instance of SizeSelectionPanel
   *      represents
   * @param allowNegative  true, if negative values are to be allowed in the
   *      panel, false if not
   * @param type  the type of unit indicator, one of TYPE_LABEL and
   *      TYPE_COMBO
   */
  public SizeSelectorPanel(Object key, Object htmlKey, boolean allowNegative, int type)
  {
    this(key, htmlKey, allowNegative);
    switch(type) {
      case TYPE_LABEL:
        //System.out.println("SizeSelectorPanel constructor setting Label");
	unitName = new JLabel(UNIT_PT);
	add(unitName);
        break;
      case TYPE_COMBO:
        //System.out.println("SizeSelectorPanel constructor setting COMBO");
	unitSelector = new JComboBox(UNIT_VALUES);
        unitSelector.addActionListener(this);
	add(unitSelector);
        break;
    }
    originalUnit = getUnit();
  }

  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource().equals(unitSelector)) {
      //System.out.println("actionPerformed is unitSelector new value = " + unitSelector.getSelectedItem().toString());
      adjustMinMax(unitSelector.getSelectedItem().toString());
    }
  }

  public void setValue(String val) {
    //System.out.println("SizeSelectorPanel setValue STRING, val=" + val);
    String unit = null;
    int newVal = 0;
    //if(attributeKey instanceof CSS.Attribute) {
    //lv = new LengthValue(val);
    float aVal = Util.getAbsoluteAttrVal(val);
    //System.out.println("SizeSelectorPanel aVal=" + aVal);
    unit = Util.getLastAttrUnit(); //lv.getUnit();
    //System.out.println("SizeSelectorPanel unit=" + unit);
    adjustMinMax(unit);
    if(unitSelector != null) {
      //System.out.println("SizeSelectorPanel setValue setting combo");
      unitSelector.setSelectedItem(unit);
    }
    else if(unitName != null) {
      //System.out.println("SizeSelectorPanel setValue setting label");
      unitName.setText(unit);
    }
    newVal = (int) aVal; // new Float(lv.getValue(100)).intValue();
    //System.out.println("SizeSelectorPanel setValue newVal=" + newVal);
    valueSelector.setValue(new Integer(newVal));
    //}
    /*
    else {
      newVal = Integer.parseInt(val);
      valueSelector.setValue(new Integer(val));
      unit = UNIT_PT;
    }
    */
    if(++setValueCalls < 2) {
      originalValue = newVal;
      originalUnit = unit;
    }
  }

  /**
   * set the value of this <code>AttributeComponent</code>
   *
   * @param a  the set of attributes possibly having an
   *          attribute this component can display
   *
   * @return true, if the set of attributes had a matching attribute,
   *            false if not
   */
  public boolean setValue(AttributeSet a) {
    boolean success = false;
    //System.out.println("SizeSelectorPanel setValue SET attributeKey=" + attributeKey + ", htmlAttrKey=" + htmlAttrKey);
    Object valObj = a.getAttribute(attributeKey);
    if(valObj != null) {
      //System.out.println("SizeSelectorPanel CSS valObj=" + valObj);
      setValue(valObj.toString());
      success = true;
    }
    else {
      if(htmlAttrKey != null) {
        valObj = a.getAttribute(htmlAttrKey);
        if(valObj != null) {
          //System.out.println("SizeSelectorPanel HTML valObj=" + valObj);
          setValue(valObj.toString());
          success = true;
        }
      }
    }
    return success;
  }

  /**
   * adjust the minimum and maximum values of the component
   * according to the unit
   */
  private void adjustMinMax(String unit) {
    //if(lv != null) {
      SpinnerNumberModel model =
		    (SpinnerNumberModel) valueSelector.getModel();
      int minVal = 0;
      if(allowNegative) {
        minVal = Integer.MIN_VALUE;
      }
      if(unit.equalsIgnoreCase(UNIT_PERCENT)) {
        //System.out.println("adjustMinMax percent");
	model.setMinimum(new Integer(minVal));
	model.setMaximum(new Integer(100));
      }
      else {
        //System.out.println("adjustMinMax pt");
	model.setMinimum(new Integer(minVal));
	model.setMaximum(new Integer(Integer.MAX_VALUE));
      }
    //}

  }

  /**
   * get the unit string of this SizeSelectorPanel
   *
   * @return the unit string (one of UNIT_PT and UNIT_PERCENT)
   */
  public String getUnit() {
    String unit = "";
    if(unitSelector != null) {
      unit = unitSelector.getSelectedItem().toString();
    }
    else if(unitName != null) {
      unit = unitName.getText();
    }
    else {
      unit = UNIT_PT;
    }
    if(unit.equalsIgnoreCase(UNIT_PT)) {
      unit = "";
    }
    return unit;
  }

  public boolean valueChanged() {
    Integer value = (Integer) valueSelector.getValue();
    return ((value.intValue() != originalValue) || (getUnit() != originalUnit));
  }

  public String getAttr() {
    Integer value = (Integer) valueSelector.getValue();
    String unit = getUnit();
    return value.toString() + unit;
  }

  public Integer getIntValue() {
    return (Integer) valueSelector.getValue();
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    SimpleAttributeSet a = new SimpleAttributeSet();
    Integer value = getIntValue();
    String unit = getUnit();
    if(valueChanged()) {
      if(attributeKey instanceof CSS.Attribute) {
        //a.addAttribute(attributeKey, value.toString() + unit);
        Util.styleSheet().addCSSAttribute(a,
            (CSS.Attribute) attributeKey, value.toString() + unit);
      }
      else {
        a.addAttribute(attributeKey, value.toString());
        if(htmlAttrKey != null) {
          a.addAttribute(htmlAttrKey, value.toString());
        }
      }
    }
    //System.out.println("SizeSelectorPanel getValue()='" + a + "'");
    return a;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      SimpleAttributeSet a = new SimpleAttributeSet();
      Integer value = getIntValue();
      String unit = getUnit();
      if(attributeKey instanceof CSS.Attribute) {
        //a.addAttribute(attributeKey, value.toString() + unit);
        Util.styleSheet().addCSSAttribute(a,
            (CSS.Attribute) attributeKey, value.toString() + unit);
      }
      else {
        a.addAttribute(attributeKey, value.toString());
        if(htmlAttrKey != null) {
          a.addAttribute(htmlAttrKey, value.toString());
        }
      }
      //System.out.println("SizeSelectorPanel getValue()='" + a + "'");
      return a;
    }
    else {
      return getValue();
    }
  }

  public JSpinner getValueSelector() {
    return valueSelector;
  }

}
