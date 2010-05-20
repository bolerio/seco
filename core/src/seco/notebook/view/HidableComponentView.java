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
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.Position;

public class HidableComponentView extends ComponentView implements HidableView
{
	boolean visible = true;
	
	public HidableComponentView(Element elem)
	{
		super(elem);
	}
	
	public void setVisible(boolean _visible)
	{
		this.visible = _visible;
	}
	
	public boolean isVisible(){
		return visible;
	}
	
	public void collapse(boolean visible){
		setVisible(visible);
	}
	public boolean isCollapsed(){return isVisible();
	}
	
	public float getPreferredSpan(int axis)
	{
		if (!isVisible())
			return 0;
		return super.getPreferredSpan(axis);
	}

	public float getMinimumSpan(int axis)
	{
		if (!isVisible())
			return 0;
		return super.getMinimumSpan(axis);
	}

	public float getMaximumSpan(int axis)
	{
		if (!isVisible())
			return 0;
		return super.getMaximumSpan(axis);
	}
	
	
	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
		if(!isVisible())
			return -1;
		return super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
	}
	

	
}
