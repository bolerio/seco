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
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.Vector;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import java.util.Enumeration;

/**
 * Panel to show and manipulate boundaries of a rectangular object
 * such as a table cell.
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

public class BoundariesPanel extends JPanel implements AttributeComponent {

  /** the components used for single attributes */
  private Vector components = new Vector();

  /** the attributes represented by this compoent */
  private CombinedAttribute ca;

  /** the value to compare to determine changes */
  private String originalValue;

  /** indicates if a call to setValue is for initial setting or for changes */
  private int setValueCalls = 0;

  /** the attribute key this component represents */
  private Object key;

  /**
   * construct a <code>BoundariesPanel</code>.
   */
  public BoundariesPanel(Object attrKey) {
    super();

    this.key = attrKey;

    // set layout
    GridBagLayout g = new GridBagLayout();
    setLayout(g);

    // constraints to use on our GridBagLayout
    GridBagConstraints c = new GridBagConstraints();

    Util.addGridBagComponent(this, new JLabel(
        "Top"),
        g, c, 0, 0, GridBagConstraints.EAST);
    Util.addGridBagComponent(this, new JLabel("Right"),
        g, c, 2, 0, GridBagConstraints.EAST);
    Util.addGridBagComponent(this, new JLabel("Bottom"),
        g, c, 0, 1, GridBagConstraints.EAST);
    Util.addGridBagComponent(this, new JLabel("Left"),
        g, c, 2, 1, GridBagConstraints.EAST);

    addSizeSelector(g, c, attrKey, 1, 0); // top
    addSizeSelector(g, c, attrKey, 3, 0); // right
    addSizeSelector(g, c, attrKey, 1, 1); // bottom
    addSizeSelector(g, c, attrKey, 3, 1); // left
  }

  private void addSizeSelector(GridBagLayout g, GridBagConstraints c,
                               Object attr, int x, int y)
  {
    SizeSelectorPanel ssp = new SizeSelectorPanel(
                                      attr,
                                      null,
                                      false,
                                      SizeSelectorPanel.TYPE_LABEL);
    Util.addGridBagComponent(this, ssp, g, c, x, y, GridBagConstraints.WEST);
    components.addElement(ssp);
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
    boolean success = true;
    ca = new CombinedAttribute(key, a, true);
    if(++setValueCalls < 2) {
      originalValue = ca.getAttribute();
    }
    SizeSelectorPanel ssp;
    for(int i = 0; i < components.size(); i++) {
      ssp = (SizeSelectorPanel) components.elementAt(i);
      ssp.setValue(ca.getAttribute(i));
    }
    return success;
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    SimpleAttributeSet set = new SimpleAttributeSet();
    SizeSelectorPanel ssp;
    for(int i = 0; i < components.size(); i++) {
      ssp = (SizeSelectorPanel) components.elementAt(i);
      if(ssp.valueChanged()) {
        ca.setAttribute(i, ssp.getAttr());
      }
    }
    String newValue = ca.getAttribute();
    if(!originalValue.equalsIgnoreCase(newValue)) {
      set.addAttribute(key, newValue);
      Util.styleSheet().addCSSAttribute(set, (CSS.Attribute) key, newValue);
    }
    return set;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      SimpleAttributeSet set = new SimpleAttributeSet();
      SizeSelectorPanel ssp;
      for(int i = 0; i < components.size(); i++) {
        ssp = (SizeSelectorPanel) components.elementAt(i);
        ca.setAttribute(i, ssp.getAttr());
      }
      String newValue = ca.getAttribute();
      set.addAttribute(key, newValue);
      Util.styleSheet().addCSSAttribute(set, (CSS.Attribute) key, newValue);
      return set;
    }
    else {
      return getValue();
    }
  }

  public void reset() {
    setValueCalls = 0;
    originalValue = null;
  }
}
