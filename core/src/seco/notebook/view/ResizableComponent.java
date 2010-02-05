/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 */
public class ResizableComponent extends JPanel
{
	//fired after finishing the component resizing
	public static final String PROP_RESIZED = "resized";
	//fired before the selection border is drawn, giving a chance to listeners
	//to clear their own selection if interested
	public static final String PROP_TO_BE_SELECTED = "border_to_be_selected";
	private Border ORIG_BORDER;
	private static Border BORDER = new SelectBorder();
	private Component comp;
	private MouseHandler mouseHandler;
	private MyGlassPane glass;
    //used to preserve some minimal size, otherwise the comp
	//could be set unintentionaly to 0,0 while dragging 
	//and disappear from the screen
	private int MIN_SIZE = 5;
	private static final Dimension MIN_DIM = new Dimension(100,50);
	
	public ResizableComponent(Component comp)
	{
		super(new BorderLayout());
		this.comp = comp;
		setOpaque(true);
		add(comp, BorderLayout.CENTER);
		
		//comp.setPreferredSize(MIN_DIM);
		//comp.setMinimumSize(MIN_DIM);
		//comp.setMaximumSize(MIN_DIM);
		comp.setPreferredSize(getPreferredSize());
		//Dimension min = comp.getMinimumSize();
		//int max_min = Math.max(min.height, min.width);
		//if(max_min > MIN_SIZE)
		//	MIN_SIZE = max_min;
		mouseHandler = new MouseHandler(null);
		comp.addMouseListener(mouseHandler);
		comp.addMouseMotionListener(mouseHandler);
       	setAutoscrolls(true);
	}

	public void addNotify()
	{
		super.addNotify();
		ORIG_BORDER = getBorder();
		setGlassPane();
	}
	
	private void setGlassPane()
	{
		JRootPane pane = ResizableComponent.this.getRootPane();
		if(pane == null) return;
		if (!(pane.getGlassPane() instanceof MyGlassPane))
		{
			glass = new MyGlassPane(mouseHandler);
			pane.setGlassPane(glass);
		} else
			glass = (MyGlassPane) pane.getGlassPane();
	}
	
	public void setSelected(boolean selected)
	{
		setBorder((!selected) ? ORIG_BORDER : BORDER);
	}

	
	private static final Stroke stroke = new BasicStroke(1,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
			new float[] { 3, 3 }, 0);
	private static final int D = 2;
	private static final int D2 = 2 * D;

	private static void draw_border(Graphics g, int x, int y, int width,
			int height)
	{
		g.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) g;
		Stroke old = g2.getStroke();
		g2.setStroke(stroke);
		g2.drawRect(x, y, width, height);
		g2.setStroke(old);
		g2.fillRect(x - D, y - D, D2, D2);
		g2.fillRect(x + width - D, y - D, D2, D2);
		g2.fillRect(x - D, y + height - D, D2, D2);
		g2.fillRect(x + width - D, y + height - D, D2, D2);
	}

	/**
	 * Hand press/drag/release by interactively resizing the component. We also
	 * handle showing the popup menu here.
	 */
	private class MouseHandler implements MouseListener, MouseMotionListener
	{
		/*
		 * MW - "Margin Width". Mouse gestures tha occur within the margin mean
		 * resize or drag.
		 */
		private final static int MW = 8;
		private final static int MW2 = 2 * MW;
		/*
		 * The following constants are used to encode the eight "margin" areas
		 * that will support move or resize operations. They are: TL T TR L R BL
		 * B BR The corners support resizing up to a minimum size of MW2 x MW2,
		 * and the four edges support moving.
		 */
		private final static int T = 0; /* Top */
		private final static int L = 1; /* Left */
		private final static int R = 2; /* Right */
		private final static int B = 3; /* Bottom */
		private final static int TL = 4; /* Top Left */
		private final static int TR = 5; /* Top Right */
		private final static int BL = 6; /* Bottom Left */
		private final static int BR = 7; /* Bottom Right */
		private final JPopupMenu popup;
		private final Point startPoint = new Point();
		private final Rectangle startBounds = new Rectangle();
		private int edge = -1;
		private Point origPoint;
		private Rectangle adjBounds = new Rectangle();
		private boolean maybe_drag;
		private boolean start_drag;

		public MouseHandler(JPopupMenu popup)
		{
			this.popup = popup;
		}

		/**
		 * Return the edge/corner under the mouse or -1.
		 */
		private int toEdge(MouseEvent e)
		{
			int x = e.getX(), y = e.getY();
			int r = e.getComponent().getWidth() - MW;
			int b = e.getComponent().getHeight() - MW;
			if (y < MW)
			{
				if (x < MW)
					return TL;
				else if (x > r)
					return TR;
				else
					return T;
			} else if (y > b)
			{
				if (x < MW)
					return BL;
				else if (x > r)
					return BR;
				else
					return B;
			} else if (x < MW)
				return L;
			else if (x > r)
				return R;
			else
				return -1;
		}

		private boolean maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger() && popup != null)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
				return true;
			}
			return false;
		}

		public void mousePressed(MouseEvent e)
		{
			if (maybeShowPopup(e)) return;
			if((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == 0)
				return;
			setGlassPane();
			maybe_drag = true;
			//System.out.println("mousePressed: " +  maybe_drag 
			//		+ " :" + start_drag);
			// restore original border and return
			if (BORDER.equals(getBorder()))
			{
				setBorder(ORIG_BORDER);
				maybe_drag = false;
				start_drag = false;
				return;
			} else
			{
				firePropertyChange(PROP_TO_BE_SELECTED, null, null);
				setBorder(BORDER);
			}
			startPoint.setLocation(e.getX(), e.getY());
			startBounds.setBounds(getBounds());
			edge = toEdge(e);
		}

		private void setClippedBounds(int x, int y, int w, int h)
		{
			JRootPane pane = ResizableComponent.this.getRootPane();
			if(pane == null) return;
			Container p = pane.getParent();
			Insets insets = p.getInsets();
			// System.out.println("ClipBounds -x:" + x + " y:" + y + " h :" + h
			// + " insets: " + insets.left + ":" + insets.top);
			int x1 = Math.max(x, insets.left);
			int y1 = Math.max(y, insets.top);
			int w1 = Math.min(w - (x1 - x), (p.getWidth() - insets.right) - x);
			int h1 = Math
					.min(h - (y1 - y), (p.getHeight() - insets.bottom) - y);
			if ((w1 > 0) && (h1 > 0))
			{
				origPoint = convertToGlassCoord(new Point(x, y));
				adjBounds.setBounds(origPoint.x, origPoint.y, w, h);
			}
		}

		/**
		 * Clip x,y so that this TestBorderFill panel doesn't extend outside of
		 * the insets area of this.getParent().
		 */
		private void setClippedOrigin(int x, int y)
		{
			JRootPane pane = ResizableComponent.this.getRootPane();
			Container p = pane.getParent();
			Insets insets = p.getInsets();
			int x1 = Math.min(x, p.getWidth() - (getWidth() + insets.right));
			int x2 = Math.max(x1, insets.left);
			int y1 = Math.min(y, p.getHeight() - (getHeight() + insets.bottom));
			int y2 = Math.max(y1, insets.top);
			origPoint = convertToGlassCoord(new Point(x2, y2));
			adjBounds.setBounds(origPoint.x, origPoint.y, getWidth(),
					getHeight());
		}

		/**
		 * If the mouse is being dragged over a T,L,R,B edge than move this. If
		 * the mouse is being dragged over a TL,TR,BL,BR corner then resize
		 * this. Otherwise sweep out a child TestBorderFill panel.
		 */
		public void mouseDragged(MouseEvent e)
		{
			int button1Mask = InputEvent.BUTTON1_DOWN_MASK;
			if ((e.getModifiersEx() & button1Mask) != button1Mask) return;
			if (!maybe_drag) return;
			//System.out.println("mouseDragged: " +  maybe_drag 
			//		+ " :" + start_drag);
			if (maybe_drag && !start_drag)
			{
				startDrag();
				start_drag = true;
			}
			int x, y, w, h, r, b;
			int dx = e.getX() - startPoint.x;
			int dy = e.getY() - startPoint.y;
			switch (edge)
			{
			case T:
			case L:
			case R:
			case B:
				setClippedOrigin(getX() + dx, getY() + dy);
				break;
			case TL:
				r = getX() + getWidth();
				b = getY() + getHeight();
				x = Math.max(0, Math.min(r - MW2, getX() + dx));
				y = Math.max(0, Math.min(b - MW2, getY() + dy));
				w = Math.abs(r - x);
				h = Math.abs(b - y);
				setClippedBounds(x, y, w, h);
				break;
			case TR:
				b = getY() + getHeight();
				y = Math.max(0, Math.min(b - MW2, getY() + dy));
				h = Math.abs(b - y);
				w = Math.max(MW2, startBounds.width + dx);
				setClippedBounds(getX(), y, w, h);
				break;
			case BR:
				w = Math.max(MW2, startBounds.width + dx);
				h = Math.max(MW2, startBounds.height + dy);
				setClippedBounds(getX(), getY(), w, h);
				break;
			case BL:
				r = getX() + getWidth();
				b = getY() + getHeight();
				x = Math.max(0, Math.min(r - MW2, getX() + dx));
				w = Math.abs(r - x);
				h = Math.max(MW2, startBounds.height + dy);
				setClippedBounds(x, getY(), w, h);
				break;
			default:
				x = Math.min(startPoint.x, e.getX());
				y = Math.min(startPoint.y, e.getY());
				w = Math.abs(dx);
				h = Math.abs(dy);
				setClippedBounds(x, y, w, h);
			}
			if (getBorder() != null)
				setBorder(ORIG_BORDER);
			glass.repaint();
		}

		private Point convertToGlassCoord(Point pt)
		{
			SwingUtilities.convertPointToScreen(pt, ResizableComponent.this);
			SwingUtilities.convertPointFromScreen(pt, glass);
			return pt;
		}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
			// System.out.println("mouseReleased() " + maybe_drag);
			if (start_drag) endDrag();
			maybe_drag = false;
			start_drag = false;
		}

		public void mouseClicked(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mouseMoved(MouseEvent e)
		{
			int c = Cursor.DEFAULT_CURSOR;
			switch (toEdge(e))
			{
			case T:
			case L:
			case R:
			case B:
				c = Cursor.MOVE_CURSOR;
				break;
			case TL:
				c = Cursor.NW_RESIZE_CURSOR;
				break;
			case TR:
				c = Cursor.NE_RESIZE_CURSOR;
				break;
			case BL:
				c = Cursor.SW_RESIZE_CURSOR;
				break;
			case BR:
				c = Cursor.SE_RESIZE_CURSOR;
				break;
			}
			if (getCursor().getType() != c)
			{
				setCursor(Cursor.getPredefinedCursor(c));
			}
		}

		private void startDrag()
		{
			glass.setVisible(true);
			glass.setMouseHandler(this);
			Point pt = new Point(getLocation());
			SwingUtilities.convertPointToScreen(pt, ResizableComponent.this);
			SwingUtilities.convertPointFromScreen(pt, glass);
			origPoint = pt;
			repaint();
		}

		private void endDrag()
		{
			Dimension dim = new Dimension(adjBounds.width, adjBounds.height);
			if(dim.height < MIN_SIZE || dim.width < MIN_SIZE)
			{
				glass.setVisible(false);
				return;
			}
			setPreferredSize(dim);
			setMinimumSize(dim);
			setMaximumSize(dim);
			Dimension dim1 = new Dimension(adjBounds.width - 2,
					adjBounds.height - 2);
			comp.setPreferredSize(dim1);
			comp.setMinimumSize(dim1);
			comp.setMaximumSize(dim1);
			
			firePropertyChange(PROP_RESIZED, null, null);
			
			glass.setVisible(false);
			revalidate();
			repaint();
		}
	}

	public static class MyGlassPane extends JComponent
	{
		MouseHandler handler;

		public MyGlassPane(MouseHandler handler)
		{
			this.handler = handler;
		}

		public void setMouseHandler(MouseHandler handler)
		{
			this.handler = handler;
		}

		protected void paintComponent(Graphics g)
		{
			//System.out.println("GLASS: " + handler + " :" + handler.maybe_drag 
			//		+ " :" + handler.start_drag);
			if (handler != null && handler.maybe_drag)
			{
				draw_border(g, handler.origPoint.x, handler.origPoint.y,
						handler.adjBounds.width, handler.adjBounds.height);
			}
		}
	}

	public static class SelectBorder extends AbstractBorder
	{
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height)
		{
			draw_border(g, x + 2, y + 2, width - 4, height - 4);
		}

		public Insets getBorderInsets(Component c)
		{
			return new Insets(2, 2, 2, 2);
		}

		public Insets getBorderInsets(Component c, Insets insets)
		{
			insets.left = insets.top = insets.right = insets.bottom = 2;
			return insets;
		}
	}
	
//	 just for testing
	public static void main(String[] args) throws Exception
	{
		JFrame f = new JFrame("Border Fill");
		ResizableComponent p = new ResizableComponent(new JLabel("Something"));
		JPanel p1 = new JPanel();
		p1.add(new JLabel("Other Panel"));
		// p1.setBorder(border0);
		JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
		JMenu jMenu1 = new javax.swing.JMenu();
		jMenu1.setText("Notebook");
		jMenuBar1.add(jMenu1);
		f.setJMenuBar(jMenuBar1);
		// p.setPreferredSize(new Dimension(640, 480));
		// f.setGlassPane(new MyGlassPane(null));
		f.getContentPane().add(p, BorderLayout.CENTER);
		f.getContentPane().add(p1, BorderLayout.NORTH);
		// f.getContentPane().add(new XPanel(), BorderLayout.SOUTH);
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		};
		f.addWindowListener(l);
		f.pack();
		f.setVisible(true);
	}
}
