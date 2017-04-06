/*
 * This file is part of the Seco source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.Position;


public class HidableBoxView extends BoxView implements HidableView
{
	protected boolean visible = true;

	public HidableBoxView(Element elem, int axis)
	{
		super(elem, axis);
	}

	public void setVisible(boolean _visible)
	{
		this.visible = _visible;
		for (int i = 0; i < getViewCount(); i++)
			((HidableView) getView(i)).setVisible(visible);
	}

	public boolean isVisible()
	{
		return visible;
	}

	public boolean isCollapsed()
	{
		return //!NotebookDocument.getNBElement(getElement()).isCollapsed();
		  isVisible();
	}

	public float getPreferredSpan(int axis)
	{
		if (!isVisible()) return 0;
		return super.getPreferredSpan(axis);
	}

	public float getMinimumSpan(int axis)
	{
		if (!isVisible()) return 0;
		return super.getMinimumSpan(axis);
	}

	public float getMaximumSpan(int axis)
	{
		if (!isVisible()) return 0;
		return super.getMaximumSpan(axis);
	}

	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
		int p = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
		if (!isVisible())
		{
			return -1;
		}
		return p;
	}

	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
			int[] spans)
	{
		super.layoutMinorAxis(targetSpan, axis, offsets, spans);
	}
	
	public int getBreakWeight(int axis, float pos, float len) {
		//if (len > getPreferredSpan(axis)) {
		//	return GoodBreakWeight;
		//}
		return GoodBreakWeight;
	}
}
