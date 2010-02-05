/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;  
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.wonderly.swing.tabs.CloseableTabbedPane;

public class CloseableDnDTabbedPane extends org.wonderly.swing.tabs.CloseableTabbedPane//JTabbedPane
{
	private static final int LINEWIDTH = 3;
	public static final String mimeType = DataFlavor.javaJVMLocalObjectMimeType + 
	 ";class=seco.notebook.gui.CloseableDnDTabbedPane"; 
	public static final String NAME = "CloseableDnDTabbedPane";
	private final GhostGlassPane glassPane = new GhostGlassPane();
	private final Rectangle2D lineRect = new Rectangle2D.Double();
	private final DragSource dragSource = new DragSource();
	private final DropTarget dropTarget;
	private int dragTabIndex = -1;
	
	public CloseableDnDTabbedPane()
	{
		super();
		final DragSourceListener dsl = new DragSourceListener() {
			public void dragEnter(DragSourceDragEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragExit(DragSourceEvent e)
			{
				e.getDragSourceContext()
						.setCursor(DragSource.DefaultMoveNoDrop);
				lineRect.setRect(0, 0, 0, 0);
				glassPane.setPoint(new Point(-1000, -1000));
				glassPane.repaint();
			}

			public void dragOver(DragSourceDragEvent e)
			{
				Point p = (Point) e.getLocation().clone();
				SwingUtilities.convertPointFromScreen(p, CloseableDnDTabbedPane.this);
				// if(getTabAreaBound().contains(p) &&
				// dragTabIndex!=indexAtLocation(p.x, p.y)) {
				if (getTabAreaBound().contains(p))
				{
					e.getDragSourceContext().setCursor(
							DragSource.DefaultMoveDrop);
				} else
				{
					e.getDragSourceContext().setCursor(
							DragSource.DefaultMoveNoDrop);
				}
			}

			public void dragDropEnd(DragSourceDropEvent e)
			{
				lineRect.setRect(0, 0, 0, 0);
				dragTabIndex = -1;
				if (hasGhost())
				{
					glassPane.setVisible(false);
					glassPane.setImage(null);
				}
			}

			public void dropActionChanged(DragSourceDragEvent e)
			{
			}
		};
		final Transferable t = new Transferable() {
			private final DataFlavor FLAVOR = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType, NAME);

			public Object getTransferData(DataFlavor flavor)
			{
				return CloseableDnDTabbedPane.this;
			}

			public DataFlavor[] getTransferDataFlavors()
			{
				DataFlavor[] f = new DataFlavor[1];
				f[0] = this.FLAVOR;
				return f;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor)
			{
				return (flavor.getHumanPresentableName().equals(NAME)) ? true
						: false;
			}
		};
		final DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e)
			{
				initGlassPane(e.getComponent(), e.getDragOrigin());
				try
				{
					e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
				}
				catch (InvalidDnDOperationException idoe)
				{
					idoe.printStackTrace();
				}
			}
		};
		dropTarget = new DropTarget(glassPane,
				DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(),
				true);
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, dgl);
	}

	public void setSelectedIndex(int index)
	{
		Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.getFocusOwner();
		
		// if no tabs are selected
		// -OR- the current focus owner is me
		// -OR- I request focus from another component and get it
		// then proceed with the tab switch
		if (getSelectedIndex() == -1 || comp == this || requestFocus(false))
		{
			super.setSelectedIndex(index);
		}
		
	}

	class CDropTargetListener implements DropTargetListener
	{
		public void dragEnter(DropTargetDragEvent e)
		{
			if (isDragAcceptable(e))
				e.acceptDrag(e.getDropAction());
			else
				e.rejectDrag();
		}

		public void dragExit(DropTargetEvent e)
		{
		}

		public void dropActionChanged(DropTargetDragEvent e)
		{
		}

		public void dragOver(final DropTargetDragEvent e)
		{
			initTargetLine(getTargetTabIndex(e.getLocation()));
			if (hasGhost())
			{
				glassPane.setPoint(e.getLocation());
				glassPane.repaint();
			}
		}

		public void drop(DropTargetDropEvent e)
		{
			//Transferable t = e.getTransferable();
			//DataFlavor[] f = t.getTransferDataFlavors();
			if (isDropAcceptable(e))
			{
				convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
				e.dropComplete(true);
			} else
			{
				e.dropComplete(false);
			}
			repaint();
		}

		public boolean isDragAcceptable(DropTargetDragEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null) return false;
			DataFlavor[] f = e.getCurrentDataFlavors();
			if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0)
			{
				return true;
			}
			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null) return false;
			DataFlavor[] f = t.getTransferDataFlavors();
			Point p = (Point) e.getLocation().clone();
			if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0
					&& dragTabIndex != indexAtLocation(p.x, p.y))
			{
				return true;
			}
			return false;
		}
	}
	private boolean hasghost = true;

	public void setPaintGhost(boolean flag)
	{
		hasghost = flag;
	}

	public boolean hasGhost()
	{
		return hasghost;
	}

	private int getTargetTabIndex(Point pt)
	{
		Point p = (Point) pt.clone();
		SwingUtilities.convertPointToScreen(p, glassPane);
		SwingUtilities.convertPointFromScreen(p, this);
		for (int i = 0; i < getTabCount(); i++)
		{
			Rectangle rect = getBoundsAt(i);
			rect.setRect(rect.x - rect.width / 2, rect.y, rect.width,
					rect.height);
			if (rect.contains(p))
			{
				return i;
			}
		}
		Rectangle rect = getBoundsAt(getTabCount() - 1);
		rect.setRect(rect.x + rect.width / 2, rect.y, rect.width + 100,
				rect.height);
		if (rect.contains(p))
		{
			return getTabCount();
		} else
		{
			return -1;
		}
	}

	private void convertTab(int prev, int next)
	{
		if (next < 0 || prev == next)
		{
			// Logger.global.info("press="+prev+" next="+next);
			return;
		}
		Component cmp = getComponentAt(prev);
		String str = getTitleAt(prev);
		if (next == getTabCount())
		{
			// Logger.global.info("last: press="+prev+" next="+next);
			remove(prev);
			addTab(str, cmp);
			setSelectedIndex(getTabCount() - 1);
		} else if (prev > next)
		{
			// Logger.global.info(" >: press="+prev+" next="+next);
			remove(prev);
			insertTab(str, null, cmp, null, next);
			setSelectedIndex(next);
		} else
		{
			// Logger.global.info(" <: press="+prev+" next="+next);
			remove(prev);
			insertTab(str, null, cmp, null, next - 1);
			setSelectedIndex(next - 1);
		}
	}

	private void initTargetLine(int next)
	{
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1)
		{
			lineRect.setRect(0, 0, 0, 0);
		} else if (next == getTabCount())
		{
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y,
					LINEWIDTH, rect.height);
		} else if (next == 0)
		{
			Rectangle rect = getBoundsAt(0);
			lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
		} else
		{
			Rectangle rect = getBoundsAt(next - 1);
			lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y,
					LINEWIDTH, rect.height);
		}
		repaint();
	}

	//public void addNotify()
	//{
	//	setGlassPane(this);
	//}
	
	private void setGlassPane(Component c)
	{
		Frame frame = GUIUtilities.getFrame(c);
		if(frame == null || !(frame instanceof JFrame)) {
			System.out.println("Could not find frame: " + frame);
			return;
		}
		((JFrame)frame).setGlassPane(glassPane);
	}
	
	private void initGlassPane(Component c, Point pt)
	{
		// if(!hasGhost()) return;
		setGlassPane(c);
		Point p = (Point) pt.clone();
		dragTabIndex = indexAtLocation(p.x, p.y);
		if (dragTabIndex == -1) return;
		if (hasGhost())
		{
			Rectangle rect = null;
			try
			{
				rect = getBoundsAt(dragTabIndex);
			}
			catch (IndexOutOfBoundsException ex)
			{
				// do nothing, this happen sometimes when while dragging the
				// mouse is released outside the frame
				return;
			}
			BufferedImage image = new BufferedImage(c.getWidth(),
					c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			glassPane.setImage(image);
		}
		glassPane.setVisible(true);
		SwingUtilities.convertPointToScreen(p, c);
		SwingUtilities.convertPointFromScreen(p, glassPane);
		glassPane.setPoint(p);
	}

	private Rectangle getTabAreaBound()
	{
		Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
		return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (dragTabIndex >= 0)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(SystemColor.activeCaption);
			g2.fill(lineRect);
		}
	}


class GhostGlassPane extends JPanel
{
	private final AlphaComposite composite;
	private Point location = new Point(0, 0);
	private BufferedImage dragged = null;

	public GhostGlassPane()
	{
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	}

	public void setImage(BufferedImage dragged)
	{
		this.dragged = dragged;
	}

	public void setPoint(Point location)
	{
		this.location = location;
	}

	public void paintComponent(Graphics g)
	{
		if (dragged == null) return;
		Graphics2D g2 = (Graphics2D) g;
		// Composite old = g2.getComposite();
		g2.setComposite(composite);
		double xx = location.getX() - (dragged.getWidth(this) / 2);
		double yy = location.getY() - (dragged.getHeight(this) / 2);
		g2.drawImage(dragged, (int) xx, (int) yy, null);
		// g2.setComposite(old);
	}
}}
