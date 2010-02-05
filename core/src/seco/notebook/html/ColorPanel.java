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

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.text.AttributeSet;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.text.html.CSS;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.StyleSheet;

/**
 * a panel to display and change a color setting
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
public class ColorPanel extends JPanel implements ActionListener, AttributeComponent {

  /** the component showing the chosen color */
  JTextField colorDisplay = new JTextField();

  /** default color */
  private Color defaultColor;

  /** the attribute key, this component returns values for */
  private Object attributeKey;

  /** value to compare for determining changes */
  private Color originalColor;

  /** indicates if setValue is called initially */
  private int setValCount = 0;

  /**
   * construct a color panel
   *
   * @param title  the title of the color panel
   * @param col  the color to be displayed first
   * @param titleFont  font for title
   * @param key  the attribute key this component shall return color values for
   */
  public ColorPanel(String title, Color col, Object key) {
    super(new BorderLayout(5,5));

    this.defaultColor = col;
    this.attributeKey = key;

    /** adjust the color display */
    colorDisplay.setBackground(col);
    Dimension dim = new Dimension(20,15);
    colorDisplay.setMinimumSize(dim);
    colorDisplay.setMaximumSize(dim);
    colorDisplay.setPreferredSize(dim);
    colorDisplay.setEditable(false);

    /** a button to open a color chooser window */
    JButton browseButton = new JButton();
    browseButton.setText("...");
    dim = new Dimension(20,15);
    browseButton.setMinimumSize(dim);
    browseButton.setMaximumSize(dim);
    browseButton.setPreferredSize(dim);
    browseButton.addActionListener(this);

    /** a helper panel for proper component placement */
    JPanel eastPanel = new JPanel(new FlowLayout());
    eastPanel.add(colorDisplay);
    eastPanel.add(browseButton);

    /** set the title */
    if((title != null) && (title.length() > 0)) {
      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(UIManager.getFont("TextField.font"));
      add(titleLabel, BorderLayout.WEST);
      add(eastPanel, BorderLayout.EAST);
    }
    else {
      add(eastPanel, BorderLayout.WEST);
    }

  }

  /**
   * get the selected color
   *
   * @return the selected color
   */
  public Color getColor() {
    return colorDisplay.getBackground();
  }

  /**
   * set the selected color
   *
   * @param color the selected color
   */
  private void setColor(Color color) {
    //System.out.println("ColorPanel setColor attributeKey=" + attributeKey + ", color=" + color);
    colorDisplay.setBackground(color);
    if(++setValCount < 2) {
      originalColor = color;
    }
    fireColorChanged();
  }

  /**
   * open a color chooser when a 'Browse' button
   * is clicked and change the associated color
   * display accordingly, when another color
   * is selected from the color chooser
   */
  public void actionPerformed(ActionEvent e) {
    JColorChooser cc = new JColorChooser();
    Color color = cc.showDialog(this,
				"Select Color",
				colorDisplay.getBackground());
    if(color != null) {
      setColor(color);
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
    if(a.isDefined(getAttributeKey())) {
      //System.out.println("ColorPanel setValue attributeKey=" + attributeKey + ", a=" + a.getAttribute(attributeKey));
      String value = a.getAttribute(getAttributeKey()).toString();
      setValue(value);
      //setColor(Util.styleSheet().getForeground(a));
      success = true;
    }
    else {
      setColor(defaultColor);
    }
    return success;
  }

  public void setValue(String value) {
    //System.out.println("ColorPanel setValue value=" + value);
    try {
      setColor(new Color(Integer.parseInt(
          value.toString().substring(1).toUpperCase(), 16)));
    }
    catch(Exception e) {
      try {
        //setColor(Util.styleSheet().getForeground(a));
        //System.out.println("ColorPanel setValue value=" + value + "=" + Color.getColor(value));
        setColor(Color.getColor(value));
      }
      catch(Exception e2) {
        Util.errMsg(null, null, e2);
      }
    }
  }

  public String getAttr() {
    String color = "#" + Integer.toHexString(
        getColor().getRGB()).substring(2);
    return color;
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    SimpleAttributeSet set = new SimpleAttributeSet();
    Color value = getColor();
    if(value != originalColor) {
      String color = "#" + Integer.toHexString(
          value.getRGB()).substring(2);
      try {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), color);
      }
      catch(Exception e) {
        set.addAttribute(getAttributeKey(), color);
      }
      //System.out.println("ColorPanel getValue color=" + color);
    }
    return set;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      SimpleAttributeSet set = new SimpleAttributeSet();
      Color value = getColor();
      String color = "#" + Integer.toHexString(
          value.getRGB()).substring(2);
      try {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), color);
      }
      catch(Exception e) {
        set.addAttribute(getAttributeKey(), color);
      }
      //System.out.println("ColorPanel getValue color=" + color);
      return set;
    }
    else {
      return getValue();
    }
  }

  /**
   * get the attribute key this object was created for
   *
   * @return the attribute key this ColorPanel returns values for
   */
  public Object getAttributeKey() {
    return attributeKey;
  }

  /* -------------- event listener implementation start ----------- */

  /** the listeners for ColorPanelEvents */
  private Vector listeners = new Vector(0);

  /**
   * add an event listener.
   *
   * @param  listener  the event listener to add
   */
  public void addColorPanelListener(ColorPanelListener listener) {
    listeners.addElement(listener);
  }

  /**
   * remove an event listener.
   *
   * @param  listener  the event listener to remove
   */
  public void removeColorPanelListener(ColorPanelListener listener) {
    listeners.removeElement(listener);
  }

  /** fire a color changed event to all registered listeners */
  void fireColorChanged() {
    Enumeration listenerList = listeners.elements();
    while(listenerList.hasMoreElements()) {
      ((ColorPanelListener) listenerList.nextElement()).colorChanged(
                                          new ColorPanelEvent(this));
    }
  }

  /** the event object definition for ColorPanels */
  class ColorPanelEvent extends EventObject {
    public ColorPanelEvent(Object source) {
      super(source);
    }
  }

  /** the event listener definition for ColorPanels */
  interface ColorPanelListener extends EventListener {
    public void colorChanged(ColorPanelEvent e);
  }

  /* -------------- event listener implementation end ----------- */

}
