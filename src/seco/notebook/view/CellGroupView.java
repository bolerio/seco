/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import seco.notebook.ElementType;

public class CellGroupView extends BoxView
{
	/** Creates a new instance of InputCellView */
	public CellGroupView(Element el)
	{
		super(el, BoxView.Y_AXIS);
	}

	
	public float getPreferredSpan(int axis)
	{
		if(!isVisible())
			return 0;
		if (axis == X_AXIS)
			return getContainer().getWidth() - 20;
		else
			return super.getPreferredSpan(axis);
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
		return 2;
	}

}
