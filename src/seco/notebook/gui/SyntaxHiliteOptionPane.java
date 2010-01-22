/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.*;

import seco.notebook.AppConfig;
import seco.notebook.NotebookUI;
import seco.notebook.Utilities;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.SyntaxStyleBean;
import seco.notebook.syntax.SyntaxUtilities;
import seco.notebook.syntax.Token;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Style option pane.
 * @author Slava Pestov
 * @version $Id: SyntaxHiliteOptionPane.java,v 1.4 2006/10/11 14:25:44 bizi Exp $
 */
public class SyntaxHiliteOptionPane extends AbstractOptionPane
{
	public static final EmptyBorder noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	protected ScriptSupport support;

	public SyntaxHiliteOptionPane(ScriptSupport scriptSupport)
	{
		super("Syntax Styles For " + scriptSupport.getFactory().getEngineName());
		support = scriptSupport;
	}

	protected void _init()
	{
		setLayout(new BorderLayout(6, 6));
		add(BorderLayout.CENTER, createStyleTableScroller());
		Utilities.formatCell(nbui, 5);
	}

	protected void _save()
	{
		styleModel.save();
	}

	public void resetDefaults()
	{
		AppConfig.getInstance().setProperty(SyntaxUtilities.SYNTAX_STYLES,
				SyntaxUtilities.getDefaultSyntaxStyles());
		Utilities.formatCell(nbui, 5);
	}

	public NotebookUI getNotebookUI()
	{
		return nbui;
	}
	private StyleTableModel styleModel;
	private JTable styleTable;

	private JScrollPane createStyleTableScroller()
	{
		styleModel = createStyleTableModel();
		styleTable = new JTable(styleModel);
		styleTable.setRowSelectionAllowed(false);
		styleTable.setColumnSelectionAllowed(false);
		styleTable.setCellSelectionEnabled(false);
		styleTable.getTableHeader().setReorderingAllowed(false);
		styleTable.addMouseListener(new MouseHandler());
		TableColumnModel tcm = styleTable.getColumnModel();
		TableColumn styleColumn = tcm.getColumn(1);
		styleColumn.setCellRenderer(new StyleTableModel.StyleRenderer(this));
		Dimension d = styleTable.getPreferredSize();
		d.height = 350;// Math.min(d.height, 100);
		d.width = 200;
		JScrollPane scroller = new JScrollPane(styleTable);
		scroller.setPreferredSize(d);
		return scroller;
	}

	private StyleTableModel createStyleTableModel()
	{
		return new StyleTableModel(this);
	}

	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			int row = styleTable.rowAtPoint(evt.getPoint());
			if (row == -1) return;
			SyntaxStyleBean style = new StyleEditor(
					SyntaxHiliteOptionPane.this, (SyntaxStyleBean) styleModel
							.getValueAt(row, 1)).getStyle();
			if (style != null) styleModel.setValueAt(style, row, 1);
		}
	}

	ScriptSupport getScriptingSupport()
	{
		return support;
	}
}

class StyleTableModel extends AbstractTableModel
{
	private Vector<StyleChoice> styleChoices;
	SyntaxHiliteOptionPane pane;

	StyleTableModel(SyntaxHiliteOptionPane pane)
	{
		this.pane = pane;
		List<StyleChoice> temp = new ArrayList<StyleChoice>();
		// start at 1 not 0 to skip Token.NULL
		ArrayList<SyntaxStyleBean> styles = SyntaxUtilities
				.getSyntaxStyles(pane.getScriptingSupport());
		for (int i = 1; i < Token.ID_COUNT; i++)
		{
			String tokenName = Token.tokenToString((byte) i);
			temp.add(new StyleChoice(tokenName, styles.get(i)));
		}
		Collections.sort(temp);
		styleChoices = new Vector<StyleChoice>(temp);
		// System.out.println("styleChoices: " + styleChoices.size());
	} // }}}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return styleChoices.size();
	}

	public Object getValueAt(int row, int col)
	{
		StyleChoice ch = (StyleChoice) styleChoices.elementAt(row);
		switch (col)
		{
		case 0:
			return ch.label;
		case 1:
			return ch.style;
		default:
			return null;
		}
	}

	public void setValueAt(Object value, int row, int col)
	{
		StyleChoice ch = (StyleChoice) styleChoices.elementAt(row);
		if (col == 1) ch.style = (SyntaxStyleBean) value;
		fireTableRowsUpdated(row, row);
		save();
		pane.getNotebookUI().getDoc().updateStyles();
	}

	public String getColumnName(int index)
	{
		switch (index)
		{
		case 0:
			return "Token type";
		case 1:
			return "Text style";
		default:
			return null;
		}
	}

	public void save()
	{
		ArrayList<SyntaxStyleBean> styles = SyntaxUtilities
				.getSyntaxStyles(pane.getScriptingSupport());
		for (int i = 0; i < styleChoices.size(); i++)
		{
			StyleChoice ch = (StyleChoice) styleChoices.elementAt(i);
			styles.set(i + 1, ch.style);
		}
	}

	static class StyleChoice implements Comparable
	{
		String label;
		SyntaxStyleBean style;

		StyleChoice(String label, SyntaxStyleBean style)
		{
			this.label = label;
			this.style = style;
		}

		// for sorting
		public String toString()
		{
			return label;
		}

		public int compareTo(Object arg)
		{
			return label.compareTo(((StyleChoice) arg).label);
		}
	}

	static class StyleRenderer extends JLabel implements TableCellRenderer
	{
		SyntaxHiliteOptionPane pane;

		public StyleRenderer(SyntaxHiliteOptionPane pane)
		{
			this.pane = pane;
			setOpaque(true);
			setBorder(SyntaxHiliteOptionPane.noFocusBorder);
			setText("Hello World");
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean cellHasFocus,
				int row, int col)
		{
			if (value != null)
			{
				SyntaxStyleBean style = (SyntaxStyleBean) value;
				setForeground(style.getForegroundColor());
				if (style.getBackgroundColor() != null)
					setBackground(style.getBackgroundColor());
				else
				{
					// this part sucks
					// ???setBackground(jEdit.getColorProperty(
					// "view.bgColor"));
				}
				Font f = pane.getScriptingSupport().getDocument()
						.getInputCellFont();
				setFont(f.deriveFont(style.getFontStyle()));
			}
			setBorder((cellHasFocus) ? UIManager
					.getBorder("Table.focusCellHighlightBorder")
					: SyntaxHiliteOptionPane.noFocusBorder);
			return this;
		}
	}
}

class StyleEditor extends EnhancedDialog implements ActionListener
{
	StyleEditor(Component comp, SyntaxStyleBean style)
	{
		super(GUIUtilities.getParentDialog(comp), "Style Editor", true);
		JPanel content = new JPanel(new BorderLayout(12, 12));
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);
		JPanel panel = new JPanel(new GridLayout(4, 2, 12, 12));
		italics = new JCheckBox("Italics");
		italics.setSelected((style.getFontStyle() & Font.ITALIC) != 0);
		panel.add(italics);
		panel.add(new JLabel());
		bold = new JCheckBox("Bold");
		bold.setSelected((style.getFontStyle() & Font.BOLD) != 0);
		panel.add(bold);
		panel.add(new JLabel());
		Color fg = style.getForegroundColor();
		fgColorCheckBox = new JCheckBox("Text color:");
		fgColorCheckBox.setSelected(fg != null);
		fgColorCheckBox.addActionListener(this);
		panel.add(fgColorCheckBox);
		fgColor = new ColorWellButton(fg);
		fgColor.setEnabled(fg != null);
		panel.add(fgColor);
		Color bg = style.getBackgroundColor();
		bgColorCheckBox = new JCheckBox("Background color:");
		bgColorCheckBox.setSelected(bg != null);
		bgColorCheckBox.addActionListener(this);
		panel.add(bgColorCheckBox);
		bgColor = new ColorWellButton(bg);
		bgColor.setEnabled(bg != null);
		panel.add(bgColor);
		content.add(BorderLayout.CENTER, panel);
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createGlue());
		box.add(ok = new JButton("OK"));
		getRootPane().setDefaultButton(ok);
		ok.addActionListener(this);
		box.add(Box.createHorizontalStrut(6));
		box.add(cancel = new JButton("Cancel"));
		cancel.addActionListener(this);
		box.add(Box.createGlue());
		content.add(BorderLayout.SOUTH, box);
		pack();
		setLocationRelativeTo(GUIUtilities.getParentDialog(comp));
		setResizable(false);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if (source == ok)
			ok();
		else if (source == cancel)
			cancel();
		else if (source == fgColorCheckBox)
			fgColor.setEnabled(fgColorCheckBox.isSelected());
		else if (source == bgColorCheckBox)
			bgColor.setEnabled(bgColorCheckBox.isSelected());
	}

	public void ok()
	{
		okClicked = true;
		dispose();
	}

	public void cancel()
	{
		dispose();
	}

	public SyntaxStyleBean getStyle()
	{
		if (!okClicked) return null;
		Color foreground = (fgColorCheckBox.isSelected() ? fgColor
				.getSelectedColor() : null);
		Color background = (bgColorCheckBox.isSelected() ? bgColor
				.getSelectedColor() : null);
		return new SyntaxStyleBean(foreground, background, (italics
				.isSelected() ? Font.ITALIC : 0)
				| (bold.isSelected() ? Font.BOLD : 0));
	}
	private JCheckBox italics;
	private JCheckBox bold;
	private JCheckBox fgColorCheckBox;
	private ColorWellButton fgColor;
	private JCheckBox bgColorCheckBox;
	private ColorWellButton bgColor;
	private JButton ok;
	private JButton cancel;
	private boolean okClicked;
}
