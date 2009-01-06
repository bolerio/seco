/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.*;
import javax.swing.SizeRequirements;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;

import seco.notebook.NotebookUI;

import seco.notebook.*;

/**
 */
public class CellParagraphView extends javax.swing.text.ParagraphView implements
		HidableView
{
	boolean visible = true;
	private short right_inset = (short) InputCellView.WHITE_GAP_SPAN;

	public CellParagraphView(Element elem)
	{
		super(elem);
	}

	public void setParent(View parent)
	{
		super.setParent(parent);
		if (parent != null) setPropertiesFromAttributes();
	}

	protected void setPropertiesFromAttributes()
	{
		super.setPropertiesFromAttributes();
		NotebookUI ui = (NotebookUI) getContainer();
		if (ui == null) return;
		if (!ui.isDrawCellNums())
			right_inset = (short) InputCellView.WHITE_GAP_SPAN;
		else
			right_inset = (short) (InputCellView.WHITE_GAP_SPAN + InputCellView.NUM_BOX_GAP);
	}

	public int getResizeWeight(int axis)
	{
		return 0;
	}

	protected SizeRequirements calculateMinorAxisRequirements(int axis,
			SizeRequirements r)
	{
		r = super.calculateMinorAxisRequirements(axis, r);
		// Offset by the margins so that pref/min/max return the
		// right value.
		int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset()
				: getTopInset() + getBottomInset();
		r.minimum -= margin;
		r.preferred -= margin;
		r.maximum -= margin;
		return r;
	}

	// shift it with the white gap of the parent
	protected short getRightInset()
	{
		return right_inset;
	}

	public void collapse(boolean visible)
	{
		setVisible(visible);
	}

	public boolean isCollapsed()
	{
		return isVisible();
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public boolean isVisible()
	{
		if (!visible) return visible;
		return true;
		/*
		 * int n = getLayoutViewCount() - 1; for (int i = 0; i < n; i++) { View
		 * v = getLayoutView(i); if (v.isVisible()) { return true; } } if (n >
		 * 0) { View v = getLayoutView(n); if ((v.getEndOffset() -
		 * v.getStartOffset()) == 1) { return false; } } // If it's the last
		 * paragraph and not editable, it shouldn't // be visible. if
		 * (getStartOffset() == getDocument().getLength()) { boolean editable =
		 * false; Component c = getContainer(); if (c instanceof JTextComponent) {
		 * editable = ((JTextComponent) c).isEditable(); } if (!editable) {
		 * return false; } } return true;
		 */
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

	@Override
	public String toString()
	{
		return "PEX: " + getElement() + ":" + super.toString();
	}

	public float getMaximumSpan(int axis)
	{
		if (!isVisible()) return 0;
		return super.getMaximumSpan(axis);
	}

	private Rectangle tempRect = new Rectangle();;

	public void paint(Graphics g, Shape a)
	{
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
				.getBounds();
		// tabBase = alloc.x + getLeftInset();
		super.paint(g, a);
		// line with the negative firstLineIndent value needs
		// special handling
		if (firstLineIndent <= 0)
		{
			Shape sh = getChildAllocation(0, a);
				
			if ((sh != null) && sh.intersects(alloc))
			{
				int x = alloc.x + getLeftInset() + firstLineIndent;
				int y = alloc.y + getTopInset();
				Rectangle clip = g.getClipBounds();
				tempRect.x = x + getOffset(X_AXIS, 0);
				tempRect.y = y + getOffset(Y_AXIS, 0);
				tempRect.width = getSpan(X_AXIS, 0) - firstLineIndent;
				tempRect.height = getSpan(Y_AXIS, 0);
				
                if (tempRect.intersects(clip))
				{
					tempRect.x = tempRect.x - firstLineIndent;
					paintChild(g, tempRect, 0);
				}else  //TODO: ugly hack to force proper repaint on the last char
					//after inserting from the keyboard. Should be a better way to do this... 
				{
					//System.err.println("CPV - paintChild - layoutChanged");
					layoutChanged(X_AXIS);
				}
			}
		}
	}
	
}
