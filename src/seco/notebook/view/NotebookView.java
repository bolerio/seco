/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Shape;
import javax.swing.text.*;

/**
 *
 * @author bolerio
 */
public class NotebookView extends BoxView
{
    
  	/** Creates a new instance of NotebookView */
    public NotebookView(Element elem) 
    {
        super(elem, View.Y_AXIS);
    }
    
//    public float getAlignment(int axis)
//    {
//        if (axis == View.Y_AXIS)
//            return 0.0f;
//        else
//            return 0.0f;
//    }    
    
    @Override
	protected short getBottomInset()
	{
		return 40;
	}
        
  	protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) 
    {
    	//super.layoutMajorAxis(targetSpan, axis, offsets, spans);
    	//System.out.println("NotebookView - layoutMajorAxis: " +
		//		targetSpan + ":" + getElement() + ":" + isVisible());
        
    	int n = getViewCount();
        int currentSpan = 0;
        for (int i = 0; i < n; i++)
        {
            View v = getView(i);
            offsets[i] = currentSpan;
            spans[i] = (int)v.getPreferredSpan(axis);
            currentSpan += spans[i];    
          //System.out.println("inner" + i + ":" + offsets[i] + ":" + spans[i]);
        }
        
    }
    
    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) 
    {
    	super.layoutMinorAxis(targetSpan, axis, offsets, spans);
        int n = getViewCount();
        //int currentSpan = 0;
        for (int i = 0; i < n; i++)
        {
            View v = getView(i);
            offsets[i] = 0;
//          force the insP to span over whole box X axe
			if (v instanceof InsertionPointView)
				spans[i] = targetSpan - 20;
			else
               spans[i] = (int)v.getPreferredSpan(axis);
        }
    } 
    
    
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
    	int i = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
		/*
    	switch (direction)
		{
		case NORTH:
		case WEST:
		{
			biasRet[0] = Position.Bias.Backward;
			break;
		}
		case EAST:
		case SOUTH:
		{
			biasRet[0] = Position.Bias.Forward;
			break;
		}
		}*/
		return i;
	}
 
}
