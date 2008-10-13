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

import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.Element;
import javax.swing.text.Style;

/**
 * A class to represent an attribute combining several other attributes.
 *
 * <p>The <a href="http://www.w3.org/TR/REC-CSS1">CSS 1.0 specification</a>
 * defines 'shorthand properties' which can hold more than
 * one value separated by blanks. Depending on the number of values inside
 * the property the values are applied following a fixed ratio.</p>
 *
 * <p>Following is an excerpt of the spec for CSS property
 * <code>border-width</code></p>
 * <pre>
 * There can be from one to four values, with the following interpretation:
 *     one value: all four border widths are set to that value
 *     two values: top and bottom border widths are set to the
 *                    first value, right and left are set to the second
 *     three values: top is set to the first, right and left are set to
 *                    the second, bottom is set to the third
 *     four values: top, right, bottom and left, respectively
 * </pre>
 *
 * <p>In SimplyHTML this spec is used on properties margin,
 * padding, border-width and border-color</p>
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

public class CombinedAttribute {

  /** index of top value */
  public static final int ATTR_TOP = 0;

  /** index of right value */
  public static final int ATTR_RIGHT = 1;

  /** index of bottom value */
  public static final int ATTR_BOTTOM = 2;

  /** index of left value */
  public static final int ATTR_LEFT = 3;

  /** the values of this <code>CombinedAttribute</code> */
  private String[] values = new String[4];

  /**
   * the attribute key the values of this
   * <code>CombinedAttribute</code> belong to
   */
  private Object attributeKey;

  /** indicates which sides were present in the attribute set */
  private boolean[] present = new boolean[4];

  /** table with attribute names from the source attribute set */
  private Vector aNames = new Vector();

  /** indicates if attributes of parent elements shall be used */
  private boolean includeParents;

  /**
   * construct a <code>CombinedAttribute</code> for a certain
   * attribute out of a given set of attributes
   *
   * @param key  the attribute key to get single attribute values from
   * @param a  the set of attributes to get the attribute of type 'key'
   */
  public CombinedAttribute(Object key, AttributeSet a, boolean includeParents) {
    attributeKey = key;
    this.includeParents = includeParents;

    // get names in this attribute set to filter out parent attributes later
    Enumeration names = a.getAttributeNames();
    while(names.hasMoreElements()) {
      aNames.addElement(names.nextElement());
    }

    // now load attributes into this object
    Object attr = a.getAttribute(key);
    if(attr != null) {
      //System.out.println("  construct CombinedAttribute attr=" + attr);
      copyValues(Util.tokenize(attr.toString(), " "));
    }
    else {
      copyValues(key, a);
    }
  }

  /**
   * copy the values for individual border settings from a given
   * set of attributes into the structure top, right, bottom, left of
   * this <code>CombinedAttribute</code>
   *
   * <p>Used in cases where attributes are not found for a 'shorthand
   * property' such as PADDING or MARGIN.</p>
   *
   * @param key  the 'shorthand property' to copy individual attributes for
   * @param a  the set of attributes to copy from
   */
  private void copyValues(Object key, AttributeSet a) {
    if(key.equals(CSS.Attribute.BORDER_WIDTH)) {
      setValue(ATTR_TOP, CSS.Attribute.BORDER_TOP_WIDTH, a);
      setValue(ATTR_RIGHT, CSS.Attribute.BORDER_RIGHT_WIDTH, a);
      setValue(ATTR_BOTTOM, CSS.Attribute.BORDER_BOTTOM_WIDTH, a);
      setValue(ATTR_LEFT, CSS.Attribute.BORDER_LEFT_WIDTH, a);
    }
    else if(key.equals(CSS.Attribute.PADDING)) {
      setValue(ATTR_TOP, CSS.Attribute.PADDING_TOP, a);
      setValue(ATTR_RIGHT, CSS.Attribute.PADDING_RIGHT, a);
      setValue(ATTR_BOTTOM, CSS.Attribute.PADDING_BOTTOM, a);
      setValue(ATTR_LEFT, CSS.Attribute.PADDING_LEFT, a);
    }
    else if(key.equals(CSS.Attribute.MARGIN)) {
      setValue(ATTR_TOP, CSS.Attribute.MARGIN_TOP, a);
      setValue(ATTR_RIGHT, CSS.Attribute.MARGIN_RIGHT, a);
      setValue(ATTR_BOTTOM, CSS.Attribute.MARGIN_BOTTOM, a);
      setValue(ATTR_LEFT, CSS.Attribute.MARGIN_LEFT, a);
    }
  }

  /**
   * set the value of a certain side from a given attribute key and
   * set of attributes.
   *
   * @param side  the side to set the value for, one of ATTR_TOP,
   *    ATTR_RIGHT, ATTR_BOTTOM and ATTR_LEFT
   * @param key  the attribute key to get the value from
   * @param a  the set of attributes to get the value from
   */
  private void setValue(int side, Object key, AttributeSet a) {
    if((includeParents) || ((!includeParents) && (aNames.contains(key)))) { // filter out parent attributes
      Object attr = a.getAttribute(key);
      if(attr != null) {
        values[side] = attr.toString();
        present[side] = true;
      }
      else {
        values[side] = defaultValue(attributeKey);
        present[side] = true;
      }
    }
    else { // key not present, set default value
      values[side] = defaultValue(attributeKey);
      present[side] = false;
    }
  }

  /**
   * determine whether or not the set of attributes this
   * <code>CombinedAttribute</code> was created from contained any
   * of the attributes in this <code>CombinedAttribute</code>
   *
   * <p>Can be used for instance to determine whether or not this
   * <code>CombinedAttribute</code> should be written</p>
   *
   * @return true, if this <code>CombinedAttribute</code> contains
   * default values only, false if not
   */
  public boolean isEmpty() {
    boolean notEmpty = false;
    int i = 0;
    while(!notEmpty && i < present.length) {
      notEmpty = present[i];
      i++;
    }
    return !notEmpty;
  }

  /**
   * get the default value for a given key
   *
   * @param key  the attribute key to get the default value for
   *
   * @return the default value for the given key
   */
  private String defaultValue(Object key) {
    String value = "0";
    if(key.equals(CSS.Attribute.BORDER_COLOR)) {
      value = "#000000";
    }
    return value;
  }

  /**
   * get the side opposite of a given side
   *
   * @param side  the side to get the opposite of
   *
   * @return the opposite side of the given side
   */
  public int getOppositeSide(int side) {
    int oppositeSide = -1;
    switch(side) {
      case ATTR_TOP:
        oppositeSide = ATTR_BOTTOM;
        break;
      case ATTR_RIGHT:
        oppositeSide = ATTR_LEFT;
        break;
      case ATTR_BOTTOM:
        oppositeSide = ATTR_TOP;
        break;
      case ATTR_LEFT:
        oppositeSide = ATTR_RIGHT;
        break;
    }
    return oppositeSide;
  }

  /**
   * copy the atribute value(s) found in a 'shorthand property' such
   * as PADDING or MARGIN into the structure top, right, bottom, left of
   * this <code>CombinedAttribute</code>
   *
   * @param v  the array of Strings holding the found values
   */
  private void copyValues(String[] v) {
    switch(v.length) {
      case 1:
        for(int i = 0; i < values.length; i++) {
          values[i] = v[0];
        }
        break;
      case 2:
        values[ATTR_TOP] = v[ATTR_TOP];
        values[ATTR_RIGHT] = v[ATTR_RIGHT];
        values[ATTR_BOTTOM] = v[ATTR_TOP];
        values[ATTR_LEFT] = v[ATTR_RIGHT];
        break;
      case 3:
        values[ATTR_TOP] = v[ATTR_TOP];
        values[ATTR_RIGHT] = v[ATTR_RIGHT];
        values[ATTR_BOTTOM] = v[ATTR_BOTTOM];
        values[ATTR_LEFT] = v[ATTR_RIGHT];
        break;
      case 4:
        for(int i = 0; i < values.length; i++) {
          values[i] = v[i];
        }
        break;
    }
  }

  /**
   * set one attribute of this <code>CombinedAttribute</code>
   *
   * @param side  the side to set the attribute for, one of ATTR_TOP,
   *   ATTR_RIGHT, ATTR_BOTTOM, ATTR_LEFT
   * @param value  the attribute value to set
   */
  public void setAttribute(int side, String value) {
    values[side] = value;
  }

  /**
   * get one attribute of this <code>CombinedAttribute</code>
   *
   * @param side  the side to get the attribute for, one of ATTR_TOP,
   *   ATTR_RIGHT, ATTR_BOTTOM, ATTR_LEFT
   *
   * @return  the attribute value for the specified side or null, if the
   *    attribute key provided in the constructor was not found
   */
  public String getAttribute(int side) {
    return values[side];
  }

  /**
   * get the attribute key this <code>CombinedAttribute</code> represents
   *
   * @return the attribute key
   */
  public Object getAttributeKey() {
    return attributeKey;
  }

  /**
   * get all values of this <code>CombinedAttribute</code>
   * as one attribute.
   *
   * @return a String having all values delimited by blanks
   *     in the order top right, bottom, left or null if no
   *     attributes were found
   */
  public String getAttribute() {
    String result = null;
    StringBuffer buf = new StringBuffer();
    if(values[0] != null) {
      buf.append(values[0]);
      int additionalValueCount = 3;
      if(values[ATTR_RIGHT].equalsIgnoreCase(values[ATTR_LEFT])) {
        --additionalValueCount; // total 3
        if(values[ATTR_TOP].equalsIgnoreCase(values[ATTR_BOTTOM])) {
          --additionalValueCount; // total 2
          if(values[ATTR_TOP].equalsIgnoreCase(values[ATTR_RIGHT])) {
            --additionalValueCount; // total 1
          }
        }
      }
      appendValues(buf, additionalValueCount);
      result = buf.toString();
    }
    return result;
  }

  /**
   * append a given number of values to a given output buffer
   * starting with ATTR_RIGHT and necessarily continuing
   * with ATTR_BOTTOM and ATTR_LEFT ( helper method to getAttribute() )
   *
   * @param buf  the output buffer to append to
   * @param count  the number of values to append
   */
  private void appendValues(StringBuffer buf, int count) {
    for(int i = 1; i < count + 1; i++) {
      buf.append(' ');
      buf.append(values[i]);
    }
  }
}
