/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html.view;

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

import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import java.awt.Graphics;
import javax.swing.text.View;
import java.awt.Color;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;

import seco.notebook.html.CombinedAttribute;
import seco.notebook.html.Util;

/**
 * <p>A box painter to support individual border rendering, i.e. border width
 * and color of each side of a cell are painted independently according
 * to CSS attributes border-width and border-color.</p>
 *
 * <p>Only border-style solid is supported.</p>
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

public class HTMLBoxPainter {

  private float topMargin;
  private float bottomMargin;
  private float leftMargin;
  private float rightMargin;
  // Bitmask, used to indicate what margins are relative:
  // bit 0 for top, 1 for bottom, 2 for left and 3 for right.
  private short marginFlags;
  private CombinedAttribute borderWidth;
  private CombinedAttribute borderColor;
  private CombinedAttribute padding;
  private CombinedAttribute margin;
  private Color bg;

  /**
   * construct an SHTMLBoxPainter
   */
  public HTMLBoxPainter(AttributeSet a) {
    borderWidth = new CombinedAttribute(CSS.Attribute.BORDER_WIDTH, a, true);
    borderColor = new CombinedAttribute(CSS.Attribute.BORDER_COLOR, a, true);
    padding = new CombinedAttribute(CSS.Attribute.PADDING, a, true);
    margin = new CombinedAttribute(CSS.Attribute.MARGIN, a, true);
    leftMargin = Util.getPtValue(margin.getAttribute(CombinedAttribute.ATTR_LEFT));
    rightMargin = Util.getPtValue(margin.getAttribute(CombinedAttribute.ATTR_RIGHT));
    topMargin = Util.getPtValue(margin.getAttribute(CombinedAttribute.ATTR_TOP));
    bottomMargin = Util.getPtValue(margin.getAttribute(CombinedAttribute.ATTR_BOTTOM));
    bg = Util.styleSheet().getBackground(a);
  }

  public Color getColor(String value) {
    try {
      if (value != null) {
        return new Color(Integer.parseInt(
            value.toString().substring(1).toUpperCase(), 16));
      }
      else {
        return Color.black;
      }
    }
    catch(Exception e) {
      try {
        return Color.getColor(value);
      }
      catch (Exception e2) {
        return Color.black;
      }
    }
  }

  /**
   * Fetches the inset needed on a given side to
   * account for the margin, border, and padding.
   *
   * @param side The size of the box to fetch the
   *  inset for.  This can be View.TOP,
   *  View.LEFT, View.BOTTOM, or View.RIGHT.
   * @param v the view making the request.  This is
   *  used to get the AttributeSet, and may be used to
   *  resolve percentage arguments.
   * @exception IllegalArgumentException for an invalid direction
   */
  public float getInset(int side, View v) {
    AttributeSet a = v.getAttributes();
    float inset = 0;
    switch(side) {
    case View.LEFT:
      inset += leftMargin;
      inset += Util.getPtValue(borderWidth.getAttribute(CombinedAttribute.ATTR_LEFT));
      inset += Util.getPtValue(padding.getAttribute(CombinedAttribute.ATTR_LEFT));
      break;
    case View.RIGHT:
      inset += rightMargin;
      inset += Util.getPtValue(borderWidth.getAttribute(CombinedAttribute.ATTR_RIGHT));
      inset += Util.getPtValue(padding.getAttribute(CombinedAttribute.ATTR_RIGHT));
      break;
    case View.TOP:
      inset += topMargin;
      inset += Util.getPtValue(borderWidth.getAttribute(CombinedAttribute.ATTR_TOP));
      inset += Util.getPtValue(padding.getAttribute(CombinedAttribute.ATTR_TOP));
      break;
    case View.BOTTOM:
      inset += bottomMargin;
      inset += Util.getPtValue(borderWidth.getAttribute(CombinedAttribute.ATTR_BOTTOM));
      inset += Util.getPtValue(padding.getAttribute(CombinedAttribute.ATTR_BOTTOM));
      break;
    default:
      throw new IllegalArgumentException("Invalid side: " + side);
    }
    //System.out.println("getInset side=" + side + " inset=" + inset);
    return inset;
  }

  /**
   * Paints the CSS box according to the attributes
   * given.  This should paint the border, padding,
   * and background.
   *
   * @param g the rendering surface.
   * @param x the x coordinate of the allocated area to
   *  render into.
   * @param y the y coordinate of the allocated area to
   *  render into.
   * @param w the width of the allocated area to render into.
   * @param h the height of the allocated area to render into.
   * @param v the view making the request.  This is
   *  used to get the AttributeSet, and may be used to
   *  resolve percentage arguments.
   */
  public void paint(Graphics g, float x, float y, float w, float h, View v) {
    //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
    //hd.listAttributes(v.getAttributes(), 2);
    Element cell = v.getElement();
    Element thisRow = cell.getParentElement();
    Element table = thisRow.getParentElement();
    int rowIndex = Util.getRowIndex(cell);
    int cellIndex = Util.getElementIndex(cell);
    x += leftMargin;
    y += topMargin;
    w -= leftMargin + rightMargin;
    h -= topMargin + bottomMargin;
    
    if (bg != null) {
        g.setColor(bg);
        g.fillRect((int) x, (int) y, (int) w, (int) h);
    }
    
    if((rowIndex == 0) ||
       canPaintBorder(cell,
       table.getElement(rowIndex-1).getElement(cellIndex),
       CombinedAttribute.ATTR_TOP, v))
    {
      paintBorder(g, x, y, w, h, CombinedAttribute.ATTR_TOP);
    }
    if((cellIndex == 0) ||
       canPaintBorder(cell,
       table.getElement(rowIndex).getElement(cellIndex-1),
       CombinedAttribute.ATTR_LEFT, v))
    {
      paintBorder(g, x, y, w, h, CombinedAttribute.ATTR_LEFT);
    }
    paintBorder(g, x, y, w, h, CombinedAttribute.ATTR_BOTTOM);
    paintBorder(g, x, y, w, h, CombinedAttribute.ATTR_RIGHT);
  }

  /**
   * paint a border for a given side of a table cell
   *
   * @param g the rendering surface.
   * @param x the x coordinate of the allocated area to
   *  render into.
   * @param y the y coordinate of the allocated area to
   *  render into.
   * @param w the width of the allocated area to render into.
   * @param h the height of the allocated area to render into.
   * @param side  the side to render
   */
  private void paintBorder(Graphics g, float x, float y, float w, float h,
                           int side)
  {
    int thickness;
    g.setColor(getColor(borderColor.getAttribute(
        side)));
    thickness = (int) Util.getPtValue(borderWidth.getAttribute(
        side));
    switch(side) {
      case CombinedAttribute.ATTR_TOP:
        g.fillRect((int) x, (int) y, (int) w, thickness);
        break;
      case CombinedAttribute.ATTR_RIGHT:
        g.fillRect((int) x + (int) w - thickness, (int) y, thickness, (int) h);
        break;
      case CombinedAttribute.ATTR_BOTTOM:
        g.fillRect((int) x, (int) y + (int) h - thickness, (int) w, thickness);
        break;
      case CombinedAttribute.ATTR_LEFT:
        g.fillRect((int) x, (int) y, thickness, (int) h);
        break;
    }
  }

  /**
   * find out whether or not the border of a given side of table cell can
   * be painted.
   *
   * @param cell  the cell element to adjust a border for
   * @param oCell  the other cell to look for border and margin settings
   * @param side  the side of 'cell' to adjust adjust a border for, one of
   * CombinedAttribute.ATTR_TOP, CombinedAttribute.ATTR_RIGHT,
   * CombinedAttribute.ATTR_BOTTOM and CombinedAttribute.ATTR_LEFT
   * @see CombinedAttribute
   */
  private boolean canPaintBorder(Element cell, Element oCell, int side, View v)
  {
    boolean canPaint = true;
    try {
      AttributeSet vSet = v.getAttributes();
      SimpleAttributeSet set = new SimpleAttributeSet(vSet);
      set.addAttributes(cell.getAttributes());
      SimpleAttributeSet oSet = new SimpleAttributeSet(vSet);
      oSet.addAttributes(oCell.getAttributes());
      CombinedAttribute otherMargin = new CombinedAttribute(
          CSS.Attribute.MARGIN, oSet, true);
      CombinedAttribute otherBorderWidth = new CombinedAttribute(
          CSS.Attribute.BORDER_WIDTH, oSet, true);
      CombinedAttribute thisMargin = new CombinedAttribute(
          CSS.Attribute.MARGIN, set, true);
      int oppositeSide = otherMargin.getOppositeSide(side);
      if(intFromCA(otherMargin, oppositeSide) == 0 &&
         intFromCA(thisMargin, side) == 0 &&
         intFromCA(otherBorderWidth, oppositeSide) > 0)
      {
        canPaint = false;
      }
    }
    catch(Exception ex) {
      canPaint = false;
      //Util.errMsg(null, null, ex);
    }
    return canPaint;
  }

  /**
   * get an integer value from a CombinedAttribute
   *
   * Caution: This only works for width values in pt
   *
   * @param attr  the CombinedAttribute to get an integer value from
   * @param side  the side of the CombinedAttribute to get the vlaue for
   *
   * @return the integer value of the given side
   */
  private int intFromCA(CombinedAttribute attr, int side) {
    String val = attr.getAttribute(side);
    int iVal = 0;
    if(val != null) {
      iVal = (int) Util.getPtValue(val);
      //System.out.println("SHTMLBoxPainter intFromCA val=" + val + " iVal=" + iVal);
    }
    return iVal;
  }
}
