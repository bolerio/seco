/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.style.NBStyle;
import seco.notebook.style.StyleAttribs;
import seco.notebook.style.StyleType;
import seco.things.Cell;


/**
 * 
 * @author bolerio
 */
public class InputCellView extends HidableBoxView
{
	static int WHITE_GAP_SPAN = 60;
	static final int NUM_GAP = 5;
	static int NUM_BOX_GAP = 50;
	static final Font FONT = new Font(null, 0, 12);
	private Color bgColor;
	private Color borderColor;
	

	public InputCellView(Element el)
	{
		super(el, BoxView.Y_AXIS);
		setPropertiesFromAttributes();
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

	@Override
	protected short getBottomInset()
	{
		if (!isVisible()) return 0;
		return 2;
	}

	@Override
	protected short getLeftInset()
	{
		if (((NotebookUI) getContainer()).isDrawCellNums())
			return (short) (5 + NUM_BOX_GAP);
		return 5;
	}

	@Override
	protected short getRightInset()
	{
		return 0;// 5;
	}

	@Override
	protected short getTopInset()
	{
		if (!isVisible()) return 0;
		return 2;
	}

	public void paint(Graphics g, Shape allocation)
	{
		//System.out.println("paint: " + this + ":" + getElement() +
		//		":" + isVisible());
		if(!isVisible()) return;
			
		Rectangle r = allocation.getBounds();
		if (((NotebookUI) getContainer()).isDrawCellNums())
		{
			drawNumbering(g, getCaption(), r.x, r.y, r.height);
			r.x += NUM_BOX_GAP;
			r.width -= NUM_BOX_GAP;
		}
		paintBox(g, r.x, r.y, r.width, r.height, this);
		super.paint(g, allocation);
	}
	
	String getCaption()
	{
		return (makeNumLabel((Cell) NotebookDocument
				.getNBElement(((NotebookDocument)getDocument()).getWholeCellElement(getElement().getStartOffset()))));
	}

	String makeNumLabel(Cell cell)
	{
		String s = (isOutputCell()) ? "out" : "in";
		//s += "[" + cell.getIndex() + "]= ";
		return s;
	}

	private int drawNumbering(Graphics g, String str, int ax, int ay, int ah)
	{
		g.setColor(Color.blue);
		g.setFont(FONT);
		FontMetrics fm = g.getFontMetrics();
		int stringwidth = fm.stringWidth(str);
		int x = ax + NUM_BOX_GAP - (stringwidth + NUM_GAP);// - stringwidth -
															// NUM_GAP;
		int y = Math.max(ay + fm.getAscent(), ay + (int) (ah * 0.5));
		g.drawString(str, x, y);
		return stringwidth;
	}

	private void paintBox(Graphics g, float x, float y, float w, float h, View v)
	{
		x += 2;
		y += 2;
		w -= 2;
		h -= 2;
		g.setColor(bgColor);
		int minBoxWidth = getContainer().getWidth() - WHITE_GAP_SPAN;
		if (((NotebookUI) getContainer()).isDrawCellNums())
			minBoxWidth -= NUM_BOX_GAP;
		// Math.min((int) w - WHITE_GAP_SPAN, minBoxWidth);
		g.fillRect((int) x, (int) y, minBoxWidth, (int) h);
		border.paintBorder(null, g, (int) x, (int) y,
				(int) minBoxWidth, (int) h);
	}
	
	private LineBorder border = new LineBorder(Color.black);

	boolean isOutputCell()
	{
		return NotebookDocument.getUpperElement(getElement(),
				ElementType.outputCellBox) != null;
	}

	public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f)
	{
		setPropertiesFromAttributes();
		super.changedUpdate(changes, a, f);
	}

	protected void setPropertiesFromAttributes()
	{
		StyleType type = (isOutputCell()) ? StyleType.outputCell
				: StyleType.inputCell;
		NBStyle style = ((NotebookDocument) getElement().getDocument())
				.getStyle(type);
		bgColor = (Color) style.getDefaultValue(StyleAttribs.BG_COLOR);
		borderColor = (Color) style.getDefaultValue(StyleAttribs.BORDER_COLOR);
		border = new LineBorder(borderColor);
	}
	
	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
		int p = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
		if (!isVisible())
			return -1;
		View v = getViewAtPosition(pos, (Rectangle) a);
		//System.out.println("InputCellView - getNextVisualPositionFrom:  " +
		//		pos + ":" + v);
		if(v != null && v instanceof HidableView)
			if(!((HidableView)v).isVisible())
				if(direction == NORTH || direction == WEST)
					return getNextVisualPositionFrom(v.getStartOffset()-1, b, a,
							 direction, biasRet);
				else
					return -1; //getEndOffset()+ 1;
		
		return p;
	}
}
