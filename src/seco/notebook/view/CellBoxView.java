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
		//System.out.println("CellBoxView - collapse: " + collapse + ":" + nb
		//		+ ":" + getElement());
		if (nb == null) return false;
		collapsed = collapse;
		if (nb instanceof CellGroup)
		{
			CellGroup group = (CellGroup) nb;
			if (group.getArity() == 1)
			{
			    CellGroupMember inner = group.getElement(0);
				if (inner instanceof Cell
						&& CellUtils.getOutCell((Cell) inner) == null)
				{
					set_all_but_first(ElementType.commonCell, true);
					return false;
				}
			}
			
			//all cells except the first one 
			set_all_but_first(ElementType.cellGroup, true);
			//the other stuff in first one
			if(collapsed){
			    //set_all_but_first(ElementType.wholeCell, true); ???
			    set_all_but_first(ElementType.inputCellBox, true);
				set_all_but_first(ElementType.commonCell, true);
			}
			else
			    ((HidableView)ViewUtils.getLowerView(this, ElementType.inputCellBox)).setVisible(true);
				//???((HidableView)ViewUtils.getLowerView(this, ElementType.wholeCell)).setVisible(true);
		}
		else if (NotebookDocument.isOutputCell(getElement()))
		{
			if(CellUtils.isError(nb))
			   set_all_but_first(ElementType.commonCell, true);
			else
				collapsed = false;
		}
		else
		{
			if(CellUtils.isHTML((Cell)nb))
			   collapsed = false;
			else
			{
				CollapsibleView v = (CollapsibleView)
				ViewUtils.getUpperView(this, ElementType.inputCellBox);
				    //???ViewUtils.getUpperView(this, ElementType.wholeCell);
			    if(v != null){ 
				collapsed = v.collapse(collapsed);
				set_all_but_first(ElementType.commonCell, true);
				}
			}
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
			return getContainer().getWidth() - 20;
		else
			return super.getPreferredSpan(axis);
	}
	
	float collapsedWidth;
	
	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
			int[] spans)
	{
		//System.out.println("CellBoxView - layoutMinorAxis: " +
		//		targetSpan + ":" + getElement() + ":" + isVisible() +
		//		":" + getPreferredSpan(axis) + ":"+ collapsed);
		collapsedWidth = getView(0).getPreferredSpan(axis);
		spans[0] = (int) collapsedWidth;
		if(spans.length > 1) spans[1] = spans[0];
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
}
