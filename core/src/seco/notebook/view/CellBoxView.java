/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class CellBoxView extends HidableBoxView implements CollapsibleView
{
	protected boolean collapsed = false;

	public CellBoxView(Element elem)
	{
		super(elem, View.X_AXIS);
	}

	public boolean collapse(boolean collapse)
	{
	    CellGroupMember nb = NotebookDocument.getNBElement(getElement());
		if (nb == null) return false;
		collapsed = collapse;
		if (nb instanceof CellGroup)
		{
			//all cells except the first one 
			set_all_but_first(ElementType.cellGroup, true);
			//the other stuff in first one
			if(collapsed){
			   
			    set_all_but_first(ElementType.inputCellBox, true);
				set_all_but_first(ElementType.commonCell, true);
			}
			else
			    ((HidableView)ViewUtils.getLowerView(this, ElementType.inputCellBox)).setVisible(true);
		}
		else if (NotebookDocument.isOutputCell(getElement()))
		{
			if(CellUtils.isError(nb))
			   set_all_but_first(ElementType.commonCell, true);
			else
				collapsed = false;
		}
		else //inputCell
		{
			if(CellUtils.isHTML((Cell)nb))
			   collapsed = false;
			else
			   set_all_but_first(ElementType.commonCell, true);
		}
		return collapsed;
	}
	
	private void set_all_but_first(ElementType type, boolean lower )
	{
		View v = (lower) ? ViewUtils.getLowerView(this, type):
			ViewUtils.getUpperView(this, type);
		if(v == null) return;
		int count = 0;
		for (int i = 0; i < v.getViewCount(); i++)
		{
		    //skip first non InsP element
		    if(count == 0 && !(v.getView(i) instanceof InsertionPointView))
		        count++;
		    else
			 ((HidableView) v.getView(i)).setVisible(!collapsed);
		}
	}

	public void setVisible(boolean _visible)
	{
		//System.out.println("CellBoxView - setVisible: " + _visible + ":"
		//		+ collapsed + ":" + getElement());
		super.setVisible(_visible);
		if (collapsed && _visible) 
			collapse(true);
	}

	public float getPreferredSpan(int axis)
	{
		if (!isVisible()) return 0;
		if(collapsed && axis != X_AXIS)
			return collapsedWidth;
		if (axis == X_AXIS)
		{
			int containerWidth = getContainer().getWidth();
			return Math.max(0, containerWidth - 20);
		}
		else
			return super.getPreferredSpan(axis);
	}
	

	@Override
	public float getMinimumSpan(int axis) {
		return Math.min(getPreferredSpan(axis), super.getMinimumSpan(axis));
		
	}
	
	

	@Override
	public float getMaximumSpan(int axis) {
		return Math.max(getPreferredSpan(axis), super.getMaximumSpan(axis));
	}



	float collapsedWidth;
	
	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
			int[] spans)
	{
		//System.out.println("CellBoxView - layoutMinorAxis: " +
		//		targetSpan + ":" + getElement() + ":" + isVisible() +
		//		":" + getPreferredSpan(axis) + ":"+ collapsed);
//	   if(collapsed){
		collapsedWidth = getView(0).getPreferredSpan(axis);
		spans[0] = (int) collapsedWidth;
		if(spans.length > 1) spans[1] = spans[0];
//		}
//	   else
//	    {
//	        super.layoutMinorAxis(targetSpan, axis, offsets, spans);
//	        int n = getViewCount();
//	        for (int i = 0; i < n; i++) {
//	            View v = getView(i);
//	            int max = (int) v.getPreferredSpan(axis);
//	            if (max < targetSpan) {
//	            // can't make the child this wide, align it
//	            float align = v.getAlignment(axis);
//	            offsets[i] = (int) ((targetSpan - max) * align);
//	            spans[i] = max;
//	            } else {
//	            // make it the target width, or as small as it can get.
//	                    int min = (int)v.getMinimumSpan(axis);
//	            offsets[i] = 0;
//	            spans[i] = Math.max(min, targetSpan);
//	            }
//	        }
//	    }
	}
	
	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
		int p = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
		if (!isVisible())
			return -1;
		return p;
	}

	@Override
	public void setParent(View parent)
	{
		super.setParent(parent);
		CellGroupMember nb = NotebookDocument.getNBElement(getElement());
		if(CellUtils.isCollapsed(nb))
			collapse(true);
	}

	@Override
	public String toString() {
		return "CellBoxView [" + getElement() + "]";
	}
	
}
