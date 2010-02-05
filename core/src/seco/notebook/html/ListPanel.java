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
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.util.Hashtable;
import javax.swing.text.StyleConstants;

/**
 * A panel for showing and manipulating list format.
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

public class ListPanel extends JPanel implements AttributeComponent {

  /** selector for list type */
  private AttributeComboBox listType;

  /** selector for list position */
  private AttributeComboBox listPosition;

  /** list indent selector */
  private BoundariesPanel bndPanel;

  /** list tag from setValue/getValue (UL or OL) */
  private String listTag;

  /**
   * construct a new ListPanel
   */
  public ListPanel() {

    setLayout(new BorderLayout());

    // have a grid bag layout ready to use
    GridBagLayout g = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    // build list format panel
    JPanel formatPanel = new JPanel(g);

    // add label for list type
    Util.addGridBagComponent(formatPanel, new JLabel("List Type"),
        g, c, 0, 0, GridBagConstraints.EAST);

    // add combo box for list type selection
    
    String[] items = new String[] {
      "no bullet, no numbering",  "1.,2.,3.,4.", "i.,ii.,iii.,iv.",
      "I.,II.,III.,IV.",  "a.,b.,c.,d.", "A.,B.,C.,D.",
       "file symbol as bullet", "round bullet", "square bullet"
      };
    String[] names = new String[] {"none", "decimal", "lower-roman", "upper-roman",
      "lower-alpha", "upper-alpha", "disc", "circle", "square"};
    listType = new AttributeComboBox(items, names,
                                     CSS.Attribute.LIST_STYLE_TYPE, null);
    Util.addGridBagComponent(formatPanel, listType, g, c, 1, 0,
                             GridBagConstraints.WEST);

    // add label for list position
    Util.addGridBagComponent(formatPanel, new JLabel("Position"),
        g, c, 0, 1, GridBagConstraints.EAST);

    // add combo box for list postion selection
    items = new String[] { "inside", "outside"};
    names = items; //new String[] {"inside", "outside"};
    listPosition = new AttributeComboBox(items, names,
        CSS.Attribute.LIST_STYLE_POSITION, null);
    Util.addGridBagComponent(formatPanel, listPosition, g, c, 1, 1,
                             GridBagConstraints.WEST);

    // create list boundaries panel
    bndPanel = new BoundariesPanel(CSS.Attribute.MARGIN);
    bndPanel.setBorder(new TitledBorder(new EtchedBorder(
                  EtchedBorder.LOWERED), "Indent"));

    // add components to this ListPanel
    add(formatPanel, BorderLayout.CENTER);
    add(bndPanel, BorderLayout.SOUTH);
  }

  /**
   * get the list tag currently selected in this <code>ListPanel</code>
   *
   * @return the list tag currently selected or null, if no list is selected
   */
  public String getListTag() {
    return listTag;
  }

  /**
   * translate list types as per CSS attribute list-style-type into list
   * tag (UL or OL).
   */
  private void setTagFromType() {
    int index = listType.getSelectedIndex();
    if(index > 5) {
      listTag = HTML.Tag.UL.toString();
    }
    else if(index > 0) {
      listTag = HTML.Tag.OL.toString();
    }
    else {
      listTag = null;
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
    Object name = a.getAttribute(javax.swing.text.StyleConstants.NameAttribute);
    if(name != null) {
      listTag = name.toString();
    }
    listType.setValue(a);
    listPosition.setValue(a);
    bndPanel.setValue(a);
    return true;
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    setTagFromType();
    SimpleAttributeSet set = new SimpleAttributeSet();
    set.addAttributes(listType.getValue());
    set.addAttributes(listPosition.getValue());
    set.addAttributes(bndPanel.getValue());
    return set;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      setTagFromType();
      SimpleAttributeSet set = new SimpleAttributeSet();
      set.addAttributes(listType.getValue(includeUnchanged));
      set.addAttributes(listPosition.getValue(includeUnchanged));
      set.addAttributes(bndPanel.getValue(includeUnchanged));
      return set;
    }
    else {
      return getValue();
    }
  }
}
