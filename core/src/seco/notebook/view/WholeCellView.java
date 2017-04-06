/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

import seco.notebook.NotebookDocument;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class WholeCellView extends HidableBoxView implements CollapsibleView
{
	protected boolean collapsed = false;

	public WholeCellView(Element el)
	{
		super(el, BoxView.Y_AXIS);
	}

	//public boolean isVisible()
	//{
	//	return !NotebookDocument.getNBElement(getElement()).isCollapsed();
	//}
	
	public boolean collapse(boolean collapse)
	{
	    CellGroupMember nb = NotebookDocument.getNBElement(getElement());
		// System.out.println("WholeCellView - collapse: " + collapse + ":" + nb
		// + ":" + getElement());
		collapsed = collapse;
		if (nb instanceof CellGroup)
		{
			collapsed = false;
		} else if (nb instanceof Cell)
		{
			//if (((Cell) nb).getOutputCell() == null)
			//	collapsed = false;
			//else
			//{
				for (int i = 1; i < getViewCount(); i++)
					((HidableView) getView(i)).setVisible(!collapsed);
			//}
		}
		return collapsed;
	}

	public void setVisible(boolean _visible)
	{
		// System.out.println("WholeCellView - setVisible: " + _visible + ":"
		// + collapsed + ":" + getElement());
		super.setVisible(_visible);
		if (collapsed && _visible) collapse(true);
	}

	@Override
	protected short getBottomInset()
	{
		if (!isVisible()) return 0;
		return 2;
	}

	@Override
	protected short getLeftInset()
	{
		return 0;
	}

	@Override
	protected short getRightInset()
	{
		return 0;
	}

	@Override
	protected short getTopInset()
	{
		if (!isVisible()) return 0;
		return 2;// super.getTopInset();
	}

	public float getPreferredSpan(int axis)
	{
		if (!isVisible()) return 0;
		if (axis == X_AXIS)
		{
			int containerWidth = getContainer().getWidth(); 
			return Math.max(0, containerWidth - 20);
		}
		else
			return super.getPreferredSpan(axis);
	}

	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
			int[] spans)
	{
		super.layoutMinorAxis(targetSpan, axis, offsets, spans);
//		System.out.println("WholeCellView - layoutMinorAxis: " +
//				targetSpan + ":" + getElement());
		for (int i = 0; i < getViewCount(); i++)
			if (getView(i) instanceof InsertionPointView)
					spans[i] = targetSpan - 20;
	}
	
	

	@Override
	public float getMinimumSpan(int axis) {
		return super.getMinimumSpan(axis);
	}

	public void setParent(View parent)
	{
		super.setParent(parent);
		CellGroupMember nb = NotebookDocument.getNBElement(getElement());
		if(CellUtils.isCollapsed(nb))
			collapse(true);
	}

}
