/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import javax.script.ScriptEngineFactory;
import javax.swing.border.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.Element;
import javax.swing.*;

import seco.notebook.syntax.ScriptSupport;
import seco.things.Cell;
import seco.things.CellUtils;


import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.*;
import java.text.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The status bar used to display various information to the user. 
 * Loosely based on jEdit's StatusBar
 * <p>
 */
public class StatusBar extends JPanel implements CaretListener
{
	private JPanel panel;
	private Box box;
	private PopupLabel caretStatus;
	private Component messageComp;
	private JLabel message;
	private PopupLabel cellEngine;
	/* package-private for speed */
	StringBuffer buf = new StringBuffer();
	private Timer tempTimer;
	private boolean showCaretStatus;
	private boolean showCellEngine;
	static final String caretTestStr = "9999(99,999)";
	private AppForm view;
	private static final String DEFAULT = "DEFAULT";
    private static final Border BORDER = new BevelBorder(BevelBorder.LOWERED);
    
	public StatusBar(AppForm view)
	{
		super(new BorderLayout());
		setBorder(new CompoundBorder(new EmptyBorder(4, 0, 0, 0), UIManager
				.getBorder("TextField.border")));
		this.view = view;
		panel = new JPanel(new BorderLayout());
		box = new Box(BoxLayout.X_AXIS);
		panel.add(BorderLayout.EAST, box);
		add(BorderLayout.CENTER, panel);
		caretStatus = new PopupLabel();
		caretStatus.setToolTipText("Position (line, col)");
		message = new JLabel(" ");
		setMessageComponent(message);
		cellEngine = new PopupLabel();
		cellEngine.setText(DEFAULT);
		cellEngine.setToolTipText("Current cell scripting engine");
	}

	private void updateCellEngine(final Element el)
	{
		final Cell cell = (Cell) 
		    NotebookDocument.getNBElement(el);
		JPopupMenu menu = cellEngine.getPopupMenu();
		menu.removeAll();
		String engine_name = CellUtils.getEngine(cell);
		if (engine_name != null)
			cellEngine.setText(engine_name);
		else
			cellEngine.setText(DEFAULT);
		
		ButtonGroup group = new ButtonGroup();
		Iterator<String> all = 	view.getCurrentNotebook().
		   getDoc().getEvaluationContext().getLanguages();
		
		//for (final ScriptEngineFactory f : set.values())
		while(all.hasNext())
		{
			final String s = all.next();
			final JRadioButtonMenuItem m = new JRadioButtonMenuItem(s);
			m.setSelected(s.equals(engine_name));
			m.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e)
				{
					if (m.isSelected())
					{
						cellEngine.setText(s);
						NotebookUI ui = view.getCurrentNotebook();
						ui.setCellEngine(s, el.getStartOffset());
					}
				}
			});
			group.add(m);
			menu.add(m);
		}
	}

	public void propertiesChanged()
	{
		Color fg = Color.BLACK;// jEdit.getColorProperty("view.status.foreground");
		Color bg = view.getBackground();// jEdit.getColorProperty("view.status.background");
		showCaretStatus = true; // jEdit.getBooleanProperty("view.status.show-caret-status");
		showCellEngine = true;// jEdit.getBooleanProperty("view.status.show-encoding");
		boolean showMemory = true;// jEdit.getBooleanProperty("view.status.show-memory");
		boolean showClock = true;// jEdit.getBooleanProperty("view.status.show-clock");
		panel.setBackground(bg);
		panel.setForeground(fg);
		caretStatus.setBackground(bg);
		caretStatus.setForeground(fg);
		message.setBackground(bg);
		message.setForeground(fg);
		cellEngine.setBackground(bg);
		cellEngine.setForeground(fg);
		// retarded GTK look and feel!
		Font font = new JLabel().getFont();
		UIManager.getFont("Label.font");
		FontMetrics fm = getFontMetrics(font);
		Dimension dim = null;
		if (showCaretStatus)
		{
			panel.add(BorderLayout.WEST, caretStatus);
			caretStatus.setFont(font);
			dim = new Dimension(fm.stringWidth(caretTestStr), fm.getHeight());
			caretStatus.setPreferredSize(dim);
		} else
			panel.remove(caretStatus);
		box.removeAll();
		if (showCellEngine) box.add(cellEngine);
		if (showMemory) box.add(new MemoryStatus());
		if (showClock) box.add(new Clock());
	}

	/**
	 * Show a message for a short period of time.
	 * @param message The message
	 */
	public void setMessageAndClear(String message)
	{
		setMessage(message);
		tempTimer = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				// so if view is closed in the meantime...
				if (isShowing0()) setMessage(null);
			}
		});
		tempTimer.setInitialDelay(10000);
		tempTimer.setRepeats(false);
		tempTimer.start();
	}

	/**
	 * Displays a status message.
	 */
	public void setMessage(String message)
	{
		if (tempTimer != null)
		{
			tempTimer.stop();
			tempTimer = null;
		}
		setMessageComponent(this.message);
		if (message == null)
			this.message.setText(" ");
		else
			this.message.setText(message);
	}

	public void setMessageComponent(Component comp)
	{
		if (comp == null || messageComp == comp) return;
		messageComp = comp;
		panel.add(BorderLayout.CENTER, messageComp);
	}

	public void caretUpdate(CaretEvent e)
	{
		updateCaretStatus(e.getDot(), e.getMark());
	}

	public void updateCaretStatus(final int dot, final int mark)
	{
		if (!isShowing0() || dot < 0) return;
		NotebookUI ui = view.getCurrentNotebook();
		NotebookDocument doc = ui.getDoc();
		//System.out.println("updateCaretStatus: " + dot + ":" + doc.getBook().getFilename());
		
		if (showCaretStatus)
		{
			buf.setLength(0);
			buf.append(Integer.toString(dot));
			ScriptSupport sup = doc.getScriptSupport(dot);
			if(sup != null)
			{
				int[] lc = sup.offsetToLineCol(dot);
				buf.append("(" + (lc[0]+1));
				buf.append(',');
				buf.append(lc[1] + ")");
			}
			else
			{
			   buf.append(',');
			   int lineCount = doc.getLength();
			   float percent = (float) dot / (float) lineCount * 100.0f;
			   buf.append(Integer.toString((int) percent));
			   buf.append('%');
			}
			caretStatus.setText(buf.toString());
		}
		if (showCellEngine)
		{
			if (current_el != null && doc == current_el.getDocument() &&
					current_el.getStartOffset() <= dot	&& current_el.getEndOffset() >= dot)
				return;
			current_el = doc.getWholeCellElement(dot);
			if (current_el != null)
			{
				updateCellEngine(current_el);
			}
			else
				cellEngine.setText(DEFAULT);
		}
	}
	private Element current_el;

	private boolean isShowing0()
	{
		return view.isVisible();
	}

	class PopupLabel extends JLabel
	{
		private JPopupMenu popupMenu;
        
		public PopupLabel()
		{
			super();
			popupMenu = new JPopupMenu();
			MouseHandler handler = new MouseHandler();
			addMouseListener(handler);
			setBorder(BORDER); //new EtchedBorder());
		}
		
		public Point getToolTipLocation(MouseEvent event)
		{
			return new Point(event.getX(), -20);
		}
		
		public void setPopupMenu(JPopupMenu menu)
		{
			JPopupMenu oldMenu = popupMenu;
			popupMenu = menu;
			firePropertyChange("popup", oldMenu, popupMenu);
		}

		public JPopupMenu getPopupMenu()
		{
			return popupMenu;
		}

		private class MouseHandler extends MouseInputAdapter
		{
			public void mouseClicked(MouseEvent e)
			{
				if (popupMenu.isVisible())
				{
					popupMenu.setVisible(false);
				} else
				{
					popupMenu.show(PopupLabel.this, 0, getHeight());
				}
			}
		}
	}

	class MemoryStatus extends JComponent implements ActionListener
	{
		public MemoryStatus()
		{
			// fucking GTK look and feel
			Font font = new JLabel().getFont();
			MemoryStatus.this.setFont(font);
			FontRenderContext frc = new FontRenderContext(null, false, false);
			Rectangle2D bounds = font.getStringBounds(memoryTestStr, frc);
			Dimension dim = new Dimension((int) bounds.getWidth(), 
					(int) bounds.getHeight() + 5);
			setPreferredSize(dim);
			setMaximumSize(dim);
			lm = font.getLineMetrics(memoryTestStr, frc);
			setForeground(Color.BLACK);
			setBackground(view.getBackground());
			progressForeground = Color.blue;
			progressBackground = new Color(66, 66, 99);
			addMouseListener(new MouseHandler());
			setBorder(BORDER);
		}

		public void addNotify()
		{
			super.addNotify();
			timer = new Timer(2000, this);
			timer.start();
			ToolTipManager.sharedInstance().registerComponent(this);
		}

		public void removeNotify()
		{
			timer.stop();
			ToolTipManager.sharedInstance().unregisterComponent(this);
			super.removeNotify();
		}

		public String getToolTipText()
		{
			Runtime runtime = Runtime.getRuntime();
			int freeMemory = (int) (runtime.freeMemory() / 1024);
			int totalMemory = (int) (runtime.totalMemory() / 1024);
			int usedMemory = (totalMemory - freeMemory);
			return "Java heap memory: " + usedMemory + "Kb/" + totalMemory
					+ "Kb";
		}

		public Point getToolTipLocation(MouseEvent event)
		{
			return new Point(event.getX(), -20);
		}

		public void actionPerformed(ActionEvent evt)
		{
			MemoryStatus.this.repaint();
		}

		public void paintComponent(Graphics g)
		{
			Insets insets = new Insets(0, 0, 0, 0);// MemoryStatus.this.getBorder().getBorderInsets(this);
			Runtime runtime = Runtime.getRuntime();
			int freeMemory = (int) (runtime.freeMemory() / 1024);
			int totalMemory = (int) (runtime.totalMemory() / 1024);
			int usedMemory = (totalMemory - freeMemory);
			int width = MemoryStatus.this.getWidth() - insets.left
					- insets.right;
			int height = MemoryStatus.this.getHeight() - insets.top
					- insets.bottom - 1;
			float fraction = ((float) usedMemory) / totalMemory;
			g.setColor(progressBackground);
			g.fillRect(insets.left, insets.top, (int) (width * fraction),
					height);
			String str = (usedMemory / 1024) + "/" + (totalMemory / 1024)
					+ "Mb";
			FontRenderContext frc = new FontRenderContext(null, false, false);
			Rectangle2D bounds = g.getFont().getStringBounds(str, frc);
			Graphics g2 = g.create();
			g2.setClip(insets.left, insets.top, (int) (width * fraction),
					height);
			g2.setColor(progressForeground);
			g2.drawString(str, insets.left + (int) (width - bounds.getWidth())
					/ 2, (int) (insets.top + lm.getAscent()));
			g2.dispose();
			g2 = g.create();
			g2.setClip(insets.left + (int) (width * fraction), insets.top,
					MemoryStatus.this.getWidth() - insets.left
							- (int) (width * fraction), height);
			g2.setColor(MemoryStatus.this.getForeground());
			g2.drawString(str, insets.left + (int) (width - bounds.getWidth())
					/ 2, (int) (insets.top + lm.getAscent()));
			g2.dispose();
		}
		private static final String memoryTestStr = "999/999Mb";
		private LineMetrics lm;
		private Color progressForeground;
		private Color progressBackground;
		private Timer timer;

		class MouseHandler extends MouseAdapter
		{
			public void mousePressed(MouseEvent evt)
			{
				if (evt.getClickCount() == 2)
				{
					StatusBar.this.showMemoryDialog();
					repaint();
				}
			}
		}
	}

	class Clock extends JLabel implements ActionListener
	{
		public Clock()
		{
			setForeground(Color.BLACK);
			setBackground(view.getBackground());
			setBorder(BORDER);
		}

		public void addNotify()
		{
			super.addNotify();
			update();
			int millisecondsPerMinute = 1000 * 60;
			timer = new Timer(millisecondsPerMinute, this);
			timer.setInitialDelay((int) (millisecondsPerMinute - System
					.currentTimeMillis()
					% millisecondsPerMinute) + 500);
			timer.start();
			ToolTipManager.sharedInstance().registerComponent(this);
		}

		public void removeNotify()
		{
			timer.stop();
			ToolTipManager.sharedInstance().unregisterComponent(this);
			super.removeNotify();
		}

		public String getToolTipText()
		{
			return new Date().toString();
		}

		public Point getToolTipLocation(MouseEvent event)
		{
			return new Point(event.getX(), -20);
		}

		public void actionPerformed(ActionEvent evt)
		{
			update();
		}
		private Timer timer;

		private String getTime()
		{
			return DateFormat.getTimeInstance(DateFormat.SHORT).format(
					new Date());
		}

		private void update()
		{
			setText(getTime());
		}
	}

	/**
	 * Performs garbage collection and displays a dialog box showing memory
	 * status.
	 */
	public void showMemoryDialog()
	{
		Runtime rt = Runtime.getRuntime();
		int before = (int) (rt.freeMemory() / 1024);
		System.gc();
		int after = (int) (rt.freeMemory() / 1024);
		int total = (int) (rt.totalMemory() / 1024);
		JProgressBar progress = new JProgressBar(0, total);
		progress.setValue(total - after);
		progress.setStringPainted(true);
		progress.setString("" + (total - after) + " Kb used, " + total
				+ " Kb total");
		Object[] message = new Object[4];
		message[0] = "Garbage collection released " + (after - before) + " Kb";
		message[1] = Box.createVerticalStrut(12);
		message[2] = progress;
		message[3] = Box.createVerticalStrut(6);
		JOptionPane.showMessageDialog(view, message, "Java Heap Memory",
				JOptionPane.INFORMATION_MESSAGE);
	}
}
