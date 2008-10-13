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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;

import seco.notebook.AppForm;
import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.SelectionManager;
import seco.notebook.XMLConstants;
import seco.things.CellUtils;


public class CellHandleView extends HidableComponentView
{
	static int X_DIM = 6;
	private CustomButton button = null;
	private boolean isCellGroup;
	private boolean isOutputCell;

	public CellHandleView(Element element)
	{
		super(element);
		isCellGroup = isCellGroup();
		isOutputCell = isOutputCell();
	}

	@Override
	public float getAlignment(int axis)
	{
		if (button != null) return 0.0f;
		return super.getAlignment(axis);
	}

	@Override
	public int viewToModel(float x, float y, Shape a, Bias[] biasReturn)
	{
		return getElement().getStartOffset() + 1;
	}

	@Override
	public void setVisible(boolean visible)
	{
		if (button != null) button.setVisible(visible);
		super.setVisible(visible);
	}

	protected Component createComponent()
	{
		if (button == null)
		{
			final NotebookUI ui = (NotebookUI) getContainer();
			button = new CustomButton(this);
			button
					.setCursor(Cursor
							.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			button.setMargin(new Insets(0, 0, 0, 0));
			View par = getParent();
			Dimension dim = new Dimension(X_DIM, (int) par
					.getPreferredSpan(View.Y_AXIS));
			// System.out.println("CellHandleView - createComponent(): " + dim);
			button.setPreferredSize(dim);
			button.setMinimumSize(dim);
			button.setBackground(Color.white);
			button.setBorderPainted(false);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					if (!ui.isEditable()) return;
					int mode = e.getModifiers();
					SelectionManager manager = ui.getSelectionManager();
					if (!button.drawInsertionLine)
					{
						if ((mode & ActionEvent.SHIFT_MASK) != 0)
						{
							manager
									.addCellRangeSelection(getEnclosingElement());
						} else if ((mode & ActionEvent.CTRL_MASK) != 0)
						{
							manager.addCellSelection(getEnclosingElement());
						} else
						{
							manager.clearSelections();
							manager.addCellSelection(getEnclosingElement());
						}
						// button.drawInsertionLine = true;
						return;
					}
					button.drawInsertionLine = false;
					// manager.clearSelections();
				}
			});
			return button;
		}
		return button;
	}

	boolean isOutputCell()
	{
		return NotebookDocument.getUpperElement(getElement(),
				ElementType.outputCellBox) != null;
		// return ViewUtils.getUpperView(this, ElementType.outputCellBox) !=
		// null;
	}

	boolean isCellGroup()
	{
		return (!isOutputCell() && !isInputCell());
	}

	boolean isInputCell()
	{
		return NotebookDocument.getUpperElement(getElement(),
				ElementType.inputCellBox) != null;
	}

	private View getEnclosingView()
	{
		View view = ViewUtils.getUpperView(this, ElementType.outputCellBox);
		if (view != null) return view;
		view = ViewUtils.getUpperView(this, ElementType.inputCellBox);
		if (view != null) return view;
		view = ViewUtils.getUpperView(this, ElementType.cellGroupBox);
		if (view != null) return view;
		return null;
	}

	private Element getEnclosingElement()
	{
		return NotebookDocument.getEnclosingCellElement(getElement());
	}

	private void collapse()
	{
		View v = getEnclosingView();
		CellUtils.toggleAttribute(NotebookDocument.getNBElement(getElement()), XMLConstants.ATTR_COLLAPSED);
		//((NotebookDocument) getElement().getDocument()).collapse(getElement());
		boolean b = CellUtils.isCollapsed(NotebookDocument.getNBElement(getElement()));
		((CollapsibleView) v).collapse(b);
		// button.collapse = ((CollapsibleView) v).collapse(!button.collapse);
		final NotebookUI ui = ((NotebookUI) getContainer());
		ui.getDoc().updateElement(getParent().getElement());
//		System.out.println("CellHandleView - collapse: " + ui.getDoc().getBook().getName()+
//		        ":" + ((NotebookDocument) getElement().getDocument()).getBook().getName() +
//		        ":" + NotebookUI.getFocusedComponentEx().getDoc().getBook().getName());
	}

	public static class CustomButton extends JButton implements	SelectionManager.Selection
	{
		boolean drawInsertionLine = false;
		CaretListener caretListener;
		private CellHandleView view;
		// keep reference for use in removeNotify() when getContainer() is null
		private SelectionManager selectionManager = null;
		private static KeyListener keyListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				CustomButton b = (CustomButton) e.getComponent();
				NotebookUI ui = (NotebookUI) b.view.getContainer();
				if (KeyEvent.VK_DELETE == e.getKeyCode())
					ui.deleteSelectedElements();
				else
					processArrowKeys(b.view, e);
			}
		};

		@Override
		public boolean isVisible()
		{
			return view.isVisible();
		}

		public CustomButton(CellHandleView view)
		{
			this.view = view;
			this.addKeyListener(keyListener);
			DragMouseListener m = new DragMouseListener(this);
			addMouseListener(m);
			addMouseMotionListener(m);
			putClientProperty("Plastic.is3D", Boolean.FALSE);
		}

		private static void processArrowKeys(CellHandleView view, KeyEvent e)
		{
			CustomButton b = (CustomButton) e.getComponent();
			final NotebookUI ui = (NotebookUI) b.view.getContainer();
			int mode = e.getModifiers();
			SelectionManager manager = ui.getSelectionManager();
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
			{
				manager.up(view.getEnclosingElement(),
						(mode & ActionEvent.SHIFT_MASK) != 0);
				break;
			}
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
			{
				manager.down(view.getEnclosingElement(),
						(mode & ActionEvent.SHIFT_MASK) != 0);
				break;
			}
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
			{
				manager.left(view.getEnclosingElement(),
						(mode & ActionEvent.SHIFT_MASK) != 0);
				break;
			}
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
			{
				manager.right(view.getEnclosingElement(),
						(mode & ActionEvent.SHIFT_MASK) != 0);
				break;
			}
			}
		}

		public void addNotify()
		{
			super.addNotify();
			final NotebookUI ui = (NotebookUI) view.getContainer();
			selectionManager = ui.getSelectionManager();
			selectionManager.put(view.getEnclosingElement(), this);
			caretListener = new CaretListener() {
				public void caretUpdate(CaretEvent e)
				{
					int dot = e.getDot();
					int start = view.getStartOffset();
					if (dot == start)
					{
						drawInsertionLine = true;
						repaint();
						return;
					}
					repaintIfNeeded();
				}
			};
			ui.addCaretListener(caretListener);
		}

		public void removeNotify()
		{
			selectionManager.remove(view.getEnclosingElement());
			super.removeNotify();
		}

		private void repaintIfNeeded()
		{
			if (drawInsertionLine == true)
			{
				drawInsertionLine = false;
				repaint();
			}
		}

		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			Rectangle b = this.getBounds();
			Color c = Color.blue;
			g.setColor(c);
			if (drawInsertionLine)
			{
				g.fillRect(b.x, b.y, b.width, b.height);
				return;
			}
			int x = b.x + 2;
			int y = b.y + 2;
			int w = b.width - 4;
			int h = b.height - 4;
			// ending |
			g.drawLine((x + w), (y), (x + w), (y + h));
			// upper -
			g.drawLine((x + w - 5), (y), (x + w), (y));
			// lower -
			g.drawLine((x + w - 5), (y + h), (x + w), (y + h));
			if (!view.isCellGroup)
				g.drawLine((x + w - 5), (y), (x + w), (y + 5));
			if (view.isOutputCell)
				g.drawLine((x + w - 5), (y + 5), (x + w), (y + 5));
			if (isCollapsed())
			{
				for (int i = 1; i < 5; i++)
					g.drawLine((x + w - i), (y + h - 5 + i), (x + w), (y + h
							- 5 + i));
			}
		}

		private boolean isCollapsed()
		{
			return CellUtils.isCollapsed(NotebookDocument.getNBElement(
			        view.getElement()));
		}

		public void setSelected(boolean selected)
		{
			// System.out.println("CellHandleView - select: " + getElement() +
			// ":" + selected);
			drawInsertionLine = selected;
			repaint();
		}
	}

	private static class DragMouseListener extends MouseAdapter implements
			MouseMotionListener
	{
		private CustomButton button;

		private DragMouseListener(CustomButton button)
		{
			this.button = button;
		}

		private void initDrag(MouseEvent e)
		{
			JComponent c = (JComponent) button.view.getContainer();
			TransferHandler handler = c.getTransferHandler();
			MouseEvent m = SwingUtilities.convertMouseEvent(button, e, c);
			// convertMouseEvent(button, e, c);
			int action = ((e.getModifiers() & MouseEvent.CTRL_MASK) == 0) ? TransferHandler.MOVE
					: TransferHandler.COPY;
			handler.exportAsDrag(c, m, action);
		}

		// convert MouseEvent and adjust modifiers for the TransferHandler
		private MouseEvent convertMouseEvent(Component source, MouseEvent e,
				Component destination)
		{
			Point p = SwingUtilities.convertPoint(source, new Point(e.getX(), e
					.getY()), destination);
			Component newSource = (destination != null) ? destination : source;
			int modifiers = e.getModifiers();
			if (((e.getModifiers() & MouseEvent.CTRL_MASK) != 0))
				modifiers = 0;
			else if (((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0))
				modifiers = MouseEvent.CTRL_MASK;
			MouseEvent newEvent = new MouseEvent(newSource, e.getID(), e
					.getWhen(), modifiers, p.x, p.y, e.getClickCount(), e
					.isPopupTrigger());
			return newEvent;
		}

		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				button.view.collapse();
				return;
			}
			if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
			{
				NotebookUI ui = (NotebookUI) button.view.getContainer();
				ui.showPopup(SwingUtilities.convertMouseEvent(button, e, ui));
			}
		}

		public void mouseDragged(MouseEvent e)
		{
			if (button.drawInsertionLine)
			{
				initDrag(e);
				e.consume();
			}
		}

		public void mouseMoved(MouseEvent e)
		{
			// DO NOTHING
		}
	}
}
