/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

import seco.notebook.NotebookUI;
import seco.notebook.SelectionManager;
import seco.notebook.view.CellHandleView.CustomButton;



public class InsertionPointView extends HidableComponentView 
{
	private CustomButton button = null;

	public InsertionPointView(Element element)
	{
		super(element);
	}

	protected Component createComponent()
	{
		if (button == null)
		{
			button = new CustomButton();
			button.setCursor(Cursor
							.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			Dimension dim = new Dimension((int) getPreferredSpan(View.X_AXIS),
					3);
			button.setPreferredSize(dim);
			button.setBackground(Color.white);
		}
		return button;
	}
	
	public void setVisible(boolean _visible)
	{
		super.setVisible(_visible);
		if(button != null)
		button.setVisible(_visible);
	}
	
		
	@Override
	public int viewToModel(float x, float y, Shape a, Position.Bias[] bias)
	{
		return getStartOffset();
	}
	
	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
			int direction, Position.Bias[] biasRet) throws BadLocationException
	{
//		let super decide where it should be
	    int next = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);

	    //for invisible instance, accept that
	    if (!isVisible())
	      return next;

	    //for out-of-range, accept that
	    int start = getStartOffset();
	    int end = getEndOffset();
	    if (next < start || end < next)
	      return next;

	    //if right on the start, accept that
	    if (next == start)
	      return next;

	    //move out of this instance's interior based on a number of considerations

	    //if moving WEST, always land at start
	    if (direction == WEST)
	      return start;

	    //if moving EAST, leave this instance on end of non-appendable row
	    if (direction == EAST)
	    {
	      biasRet[0] = Position.Bias.Backward;

	      if (pos == end && next == end && b == b.Backward)
	        return end; //deal with selecting the instance

	     // if (isEndOfRow() == true && canEditEndOfRow() == false)
	      //  return -1;

	      return end;
	    }

	    //otherwise, use the mid-point method
	    int mid = (start + end) / 2;
	    next = (next <= mid) ? start : end;
	    return next;
	}
	
	
	class CustomButton extends JLabel implements SelectionManager.Selection
	{
		SelectionManager selectionManager;
		
		public CustomButton()
		{
			super();
			NotebookUI ui = (NotebookUI) getContainer();
			if(ui == null) return;
			selectionManager = ui.getSelectionManager();
			selectionManager.put(getElement(),this);
			putClientProperty("Plastic.is3D", Boolean.FALSE);
		}
		
		public void removeNotify()
		{
			selectionManager.remove(getElement());
			super.removeNotify();
		}

		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			if (((NotebookUI) getContainer()).getCaretPosition() != getElement()
					.getStartOffset() || !getContainer().isFocusOwner()) return;
			Rectangle bounds = this.getBounds();
			g.setColor(java.awt.Color.black);
			g.drawLine(bounds.x, bounds.y + 1, bounds.x + bounds.width,
					bounds.y + 1);
		}
		
		public void setSelected(boolean selected)
		{
			repaint();
		}
  }

}
