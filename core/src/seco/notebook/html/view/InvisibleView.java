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


import javax.swing.text.*;
import java.awt.*;


/**
 * A view to hide HTML tags (e.g. comments)
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

public class InvisibleView extends View {

  /** indicates whether or not this view is to be shown in its component */
  boolean isVisible = false;

  /**
   * constructor
   */
  public InvisibleView(Element e) {
    super(e);
  }

  /**
   * Determines the preferred span for this view along an
   * axis.
   *
   * @param axis may be either <code>View.X_AXIS</code> or
   *		<code>View.Y_AXIS</code>
   * @return   the span the view would like to be rendered into.
   *           Typically the view is told to render into the span
   *           that is returned, although there is no guarantee.
   *           The parent may choose to resize or break the view
   * @see View#getPreferredSpan
   */
  public float getPreferredSpan(int axis) {
    return 0;
  }

  /**
   * Determines the maximum span for this view along an
   * axis.
   *
   * @param axis may be either <code>View.X_AXIS</code> or
   *		<code>View.Y_AXIS</code>
   * @return  the maximum span the view can be rendered into
   * @see View#getPreferredSpan
   */
  public float getMaximumSpan() {
    return 0;
  }

  /**
   * Determines the minimum span for this view along an
   * axis.
   *
   * @param axis may be either <code>View.X_AXIS</code> or
   *		<code>View.Y_AXIS</code>
   * @return  the minimum span the view can be rendered into
   * @see View#getPreferredSpan
   */
  public float getMinimumSpan() {
    return 0;
  }

  /**
   * Renders using the given rendering surface and area on that
   * surface.  The view may need to do layout and create child
   * views to enable itself to render into the given allocation.
   *
   * @param g the rendering surface to use
   * @param allocation the allocated region to render into
   * @see View#paint
   */
  public void paint(Graphics g, Shape allocation) {
    if (isVisible) {
      // paint something here
    }
    else {
      setSize(0, 0);
    }
  }

  /**
   * Provides a mapping from the view coordinate space to the logical
   * coordinate space of the model.  The <code>biasReturn</code>
   * argument will be filled in to indicate that the point given is
   * closer to the next character in the model or the previous
   * character in the model.
   *
   * @param x the X coordinate >= 0
   * @param y the Y coordinate >= 0
   * @param a the allocated region in which to render
   * @return the location within the model that best represents the
   *  given point in the view >= 0.  The <code>biasReturn</code>
   *  argument will be
   * filled in to indicate that the point given is closer to the next
   * character in the model or the previous character in the model.
   */
  public int viewToModel(float x, float y, Shape a, Position.Bias[] parm4) {
    return 0;
  }

  /**
   * Provides a mapping, for a given character,
   * from the document model coordinate space
   * to the view coordinate space.
   *
   * @param pos the position of the desired character (>=0)
   * @param a the area of the view, which encompasses the requested character
   * @param b the bias toward the previous character or the
   *  next character represented by the offset, in case the
   *  position is a boundary of two views; <code>b</code> will have one
   *  of these values:
   * <ul>
   * <li> <code>Position.Bias.Forward</code>
   * <li> <code>Position.Bias.Backward</code>
   * </ul>
   * @return the bounding box, in view coordinate space,
   *		of the character at the specified position
   * @exception BadLocationException  if the specified position does
   *   not represent a valid location in the associated document
   * @exception IllegalArgumentException if <code>b</code> is not one of the
   *		legal <code>Position.Bias</code> values listed above
   * @see View#viewToModel
   */
  public Shape modelToView(int pos, Shape a, Position.Bias b) throws javax.swing.text.BadLocationException {
    return new Rectangle(0, 0);
  }

  /**
   * Establishes the parent view for this view.  This is
   * guaranteed to be called before any other methods if the
   * parent view is functioning properly.  This is also
   * the last method called, since it is called to indicate
   * the view has been removed from the hierarchy as
   * well. When this method is called to set the parent to
   * null, this method does the same for each of its children,
   * propogating the notification that they have been
   * disconnected from the view tree. If this is
   * reimplemented, <code>super.setParent()</code> should
   * be called.
   *
   * @param parent the new parent, or <code>null</code> if the view is
   * 		being removed from a parent
   */
  public void setParent(View parent) {
    if (parent != null) {
      Container host = parent.getContainer();
      if (host != null) {
        isVisible = ((JTextComponent)host).isEditable();
      }
    }
    super.setParent(parent);
  }

  /**
   * @return true if the Component is visible.
   */
  public boolean isVisible() {
    return isVisible;
  }

}
