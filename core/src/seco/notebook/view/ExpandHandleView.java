/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;

public class ExpandHandleView extends HidableComponentView
{
	static int X_DIM = 10;
	private CustomButton button = null;
	private static final Stroke stroke = new BasicStroke(2);

	public ExpandHandleView(Element element)
	{
		super(element);
	}

	/*
	 * public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
	 * int direction, Position.Bias[] biasRet) throws BadLocationException {
	 * //System.out.println("ExpandHandle - up: " + (direction ==
	 * SwingConstants.NORTH)); //if (direction == SwingConstants.NORTH) //
	 * return getStartOffset() - 1; return -1; //return
	 * super.getNextVisualPositionFrom(pos, b, a, direction, biasRet); }
	 */

	@Override
	public float getAlignment(int axis)
	{
		if (button != null)
		{
			switch (axis)
			{
			case View.X_AXIS:
				return 0.0f;
			case View.Y_AXIS:
				return 0.0f;
			}
		}
		return super.getAlignment(axis);
	}

	protected short getRightInset()
	{
		return 3;
	}

	protected short getLeftInset()
	{
		return 3;
	}

	@Override
	public int viewToModel(float x, float y, Shape a, Bias[] biasReturn)
	{
		return getElement().getStartOffset() + 1;
	}

	@Override
	public void setVisible(boolean visible)
	{
		if (button != null)
			button.setVisible(visible);
		super.setVisible(visible);
	}

	protected Component createComponent()
	{
		if (button == null)
		{
			button = new CustomButton();
			button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			button.setMargin(new Insets(0, 0, 0, 0));
			View par = getParent();
			Dimension dim = new Dimension(X_DIM,
					(int) par.getPreferredSpan(View.Y_AXIS));
			button.setPreferredSize(dim);
			button.setMinimumSize(dim);
			button.setBackground(Color.white);
			button.setBorderPainted(false);
			button.setToolTipText("Expand/Collapse Error Cell");
			final NotebookUI ui = (NotebookUI) getContainer();
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (!ui.isEditable())
						return;
					button.collapse = !button.collapse;
					expand();
				}
			});
			return button;
		}
		return button;
	}

	private void expand()
	{
		View cell = getParent().getView(0).getView(0);
		for (int i = 1; i < cell.getViewCount(); i++)
		{
			CellParagraphView p = (CellParagraphView) cell.getView(i);
			p.setVisible(!button.collapse);
		}
		((NotebookUI) getContainer()).getDoc().updateElement(
				getParent().getElement());
	}

	class CustomButton extends JButton
	{
		boolean collapse = false;

		public CustomButton()
		{
			super();
			final NotebookUI ui = (NotebookUI) ExpandHandleView.this
					.getContainer();
			this.addKeyListener(new KeyAdapter()
			{
				public void keyReleased(KeyEvent e)
				{
					if (KeyEvent.VK_DELETE == e.getKeyCode())
					{
						ui.deleteSelectedElements();
					}
				}
			});
		}

		@Override
		public void paint(Graphics g1)
		{
			super.paint(g1);
			Graphics2D g = (Graphics2D) g1;
			Rectangle b = this.getBounds();
			int x = b.x;
			int y = b.y;
			int w = b.width;
			int h = b.height;
			Color c = Color.blue;
			g.setColor(c);
			Stroke old = g.getStroke();
			g.setStroke(stroke);
			// |
			if (collapse)
				g.drawLine((x + w / 2), (y + h / 2 - 5), (x + w / 2), (y + h
						/ 2 + 5));
			// -
			g.drawLine((x + w - 10), (y + h / 2), (x + w), (y + h / 2));
			g.setStroke(old);
		}
	}
}
