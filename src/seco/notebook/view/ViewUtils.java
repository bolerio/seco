/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Shape;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;

import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;

public class ViewUtils
{
    public static View getView(int offset, View v)
    {
         int index = v.getViewIndex(offset, null);
         while(index >= 0)
         {
             v = v.getView(index);
             index = v.getViewIndex(offset, null);
         }
         return v;
    }
    
	public static View getLowerView(View parent, ElementType type){
		if (parent == null || type == null)
			return null;
		if (NotebookDocument.getElementType(parent.getElement()).equals(type))
			return parent;
		for (int i = 0; i < parent.getViewCount(); i++) {
			View child = parent.getView(i);
			ElementType child_type = NotebookDocument.getElementType(child.getElement());
			if (child_type != null && child_type.equals(type))
				return child;
			View e = getLowerView(child, type);
			if (e != null)
				return e;
		}
		return null;
	}
	
	public static View getUpperView(View view, ElementType type) {
		do {
			if (type == NotebookDocument.getElementType(
					view.getElement()))
				return view;
			view = view.getParent();
		} while (view != null);
		return null;
	}
	
	 /**
     * Provides a way to determine the next visually represented model 
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     * <p>
     * This implementation assumes the views are layed out in a logical
     * manner. That is, that the view at index x + 1 is visually after
     * the View at index x, and that the View at index x - 1 is visually
     * before the View at x. There is support for reversing this behavior
     * only if the passed in <code>View</code> is an instance of
     * <code>CompositeView</code>. The <code>CompositeView</code>
     * must then override the <code>flipEastAndWestAtEnds</code> method.
     *
     * @param v View to query
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard;
     *  this may be one of the following: 
     *  <ul>
     *  <li><code>SwingConstants.WEST</code>
     *  <li><code>SwingConstants.EAST</code> 
     *  <li><code>SwingConstants.NORTH</code>
     *  <li><code>SwingConstants.SOUTH</code>  
     *  </ul>
     * @param biasRet an array contain the bias that was checked
     * @return the location within the model that best represents the next
     *  location visual position
     * @exception BadLocationException
     * @exception IllegalArgumentException if <code>direction</code> is invalid
     */
    static int getNextVisualPositionFrom(View v, int pos, Position.Bias b,
                                          Shape alloc, int direction,
                                          Position.Bias[] biasRet)
                             throws BadLocationException {
        if (v.getViewCount() == 0) {
            // Nothing to do.
            return pos;
        }
        boolean top = (direction == SwingConstants.NORTH ||
                       direction == SwingConstants.WEST);
        int retValue;
        if (pos == -1) {
            // Start from the first View.
            int childIndex = (top) ? v.getViewCount() - 1 : 0;
            View child = v.getView(childIndex);
            Shape childBounds = v.getChildAllocation(childIndex, alloc);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds,
                                                       direction, biasRet);
	    if (retValue == -1 && !top && v.getViewCount() > 1) {
		// Special case that should ONLY happen if first view
		// isn't valid (can happen when end position is put at
		// beginning of line.
		child = v.getView(1);
                childBounds = v.getChildAllocation(1, alloc);
		retValue = child.getNextVisualPositionFrom(-1, biasRet[0],
                                                           childBounds,
                                                           direction, biasRet);
	    }
        }
        else {
            int increment = (top) ? -1 : 1;
            int childIndex;
            if (b == Position.Bias.Backward && pos > 0) {
                childIndex = v.getViewIndex(pos - 1, Position.Bias.Forward);
            }
            else {
                childIndex = v.getViewIndex(pos, Position.Bias.Forward);
            }
            View child = v.getView(childIndex);
            Shape childBounds = v.getChildAllocation(childIndex, alloc);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds,
                                                       direction, biasRet);
            //System.out.println("ViewUtils: " + pos + ":" + retValue + ": " + child.getElement());
            if ((direction == SwingConstants.EAST ||
                 direction == SwingConstants.WEST))
            {
                increment *= -1;
            }
            childIndex += increment;
            if (retValue == -1 && childIndex >= 0 &&
                                  childIndex < v.getViewCount()) {
                child = v.getView(childIndex);
                childBounds = v.getChildAllocation(childIndex, alloc);
                retValue = child.getNextVisualPositionFrom(
                                     -1, b, childBounds, direction, biasRet);
                 // If there is a bias change, it is a fake position
                // and we should skip it. This is usually the result
                // of two elements side be side flowing the same way.
                if (retValue == pos && biasRet[0] != b) {
                	 return getNextVisualPositionFrom(v, pos, biasRet[0],
                                                     alloc, direction,
                                                     biasRet);
                }
            }
            else if (retValue != -1 && biasRet[0] != b &&
                     ((increment == 1 && child.getEndOffset() == retValue) ||
                      (increment == -1 &&
                       child.getStartOffset() == retValue)) &&
                     childIndex >= 0 && childIndex < v.getViewCount()) {
                // Reached the end of a view, make sure the next view
                // is a different direction.
                child = v.getView(childIndex);
                childBounds = v.getChildAllocation(childIndex, alloc);
                Position.Bias originalBias = biasRet[0];
               
                int nextPos = child.getNextVisualPositionFrom(
                                    -1, b, childBounds, direction, biasRet);
                if (biasRet[0] == b) {
                    retValue = nextPos;
                }
                else {
                    biasRet[0] = originalBias;
                }
            }
        }
        return retValue;
    }
}
