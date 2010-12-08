/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import seco.gui.GUIHelper;
import seco.notebook.style.FontEx;
import seco.util.GUIUtil;

public class FontDialog extends JDialog
{
	public static final String[] defFontSizes = new String[] {"8", "9", "10",
		"11", "12", "14", "16", "18", "20", "22", "24", "26",
		"28", "36", "48", "72"};
	public static String[] fontNames =
	 GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	
	protected boolean succeeded = false;
	protected OpenList lstFontName;
	protected OpenList lstFontSize;
	protected MutableAttributeSet attributes;
	protected JCheckBox chkBold;
	protected JCheckBox chkItalic;
	protected JCheckBox chkUnderline;
	protected JCheckBox chkStrikethrough;
	protected JCheckBox chkSubscript;
	protected JCheckBox chkSuperscript;
	protected JComboBox cbColor;
	protected JTextPane preview;
	
	protected FontEx font;
	protected Color fgColor;
	protected Style style;

	public FontDialog(Frame parent, FontEx font, Color fgColor)
	{
		this(parent, font, fgColor, null);
	}
	
	public FontDialog(Frame parent, FontEx font, Color fgColor, int[] sizes)
	{
		super(parent, "Font", true);
		if(parent == null) setIconImage(GUIHelper.LOGO_IMAGE);
		this.font = font;
		this.fgColor = fgColor;
		JPanel pp = new JPanel();
		pp.setBorder(new EmptyBorder(5, 5, 5, 5));
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		JPanel p = new JPanel(new GridLayout(1, 2, 10, 2));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Font"));
		lstFontName = new OpenList(fontNames, "Name:");
		p.add(lstFontName);
		if(sizes == null)
		  lstFontSize = new OpenList(defFontSizes, "Size:");
		else{
			String[] nSizes = new String[sizes.length];
			for(int i = 0; i< sizes.length; i++)
				nSizes[i] = "" + sizes[i];
			lstFontSize = new OpenList(nSizes, "Size:");
		}
		p.add(lstFontSize);
		pp.add(p);
		p = new JPanel(new GridLayout(2, 3, 10, 5));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Effects"));
		chkBold = new JCheckBox("Bold");
		p.add(chkBold);
		chkItalic = new JCheckBox("Italic");
		p.add(chkItalic);
		chkUnderline = new JCheckBox("Underline");
		p.add(chkUnderline);
		chkStrikethrough = new JCheckBox("Strikeout");
		p.add(chkStrikethrough);
		chkSubscript = new JCheckBox("Subscript");
		p.add(chkSubscript);
		chkSuperscript = new JCheckBox("Superscript");
		p.add(chkSuperscript);
		pp.add(p);
		pp.add(Box.createVerticalStrut(5));
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createHorizontalStrut(10));
		p.add(new JLabel("Color:"));
		p.add(Box.createHorizontalStrut(20));
		cbColor = new JComboBox();
		int[] values = new int[] { 0, 128, 192, 255 };
		for (int r = 0; r < values.length; r++)
		{
			for (int g = 0; g < values.length; g++)
			{
				for (int b = 0; b < values.length; b++)
				{
					Color c = new Color(values[r], values[g], values[b]);
					cbColor.addItem(c);
				}
			}
		}
		
		populateFields();
		
		cbColor.setRenderer(new ColorComboRenderer());
		cbColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				//System.out.println("FontDialog-colorSel: " +
				//	cbColor.getSelectedItem() + ":" + cbColor.getSelectedIndex());
				setFontColor((Color) cbColor.getSelectedItem());
				updatePreview();
			}
			
		});
		p.add(cbColor);
		p.add(Box.createHorizontalStrut(10));
		pp.add(p);
		ListSelectionListener lsel = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				updatePreview();
			}
		};
		lstFontName.addListSelectionListener(lsel);
		lstFontSize.addListSelectionListener(lsel);
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				updatePreview();
			}
		};
		chkBold.addActionListener(lst);
		chkItalic.addActionListener(lst);
		chkUnderline.addActionListener(lst); 
		chkStrikethrough.addActionListener(lst); 
		chkSubscript.addActionListener(lst);
		chkSuperscript.addActionListener(lst);
		cbColor.addActionListener(lst);
		p = new JPanel(new BorderLayout());
		p.setBorder(new TitledBorder(new EtchedBorder(), "Preview"));
		preview = new JTextPane();//new JLabel("Preview Font", JLabel.CENTER);
		preview.setText("Preview Font");
		preview.setBackground(Color.white);
		preview.setEditable(false);
		preview.setOpaque(true);
		preview.setBorder(new LineBorder(Color.black));
		preview.setPreferredSize(new Dimension(120, 40));
		if(fgColor != null)
		   preview.setForeground(fgColor);
		if(font != null)
		{
		    style = preview.addStyle("Sample Style", null);
		    StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
			font.populateStyle(style);
			preview.getStyledDocument().setLogicalStyle(0, style);
		}
		
		p.add(preview, BorderLayout.CENTER);
		pp.add(p);
		p = new JPanel(new FlowLayout());
		JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 0));
		JButton btOK = new JButton("OK");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				succeeded = true;
				dispose();
			}
		};
		btOK.addActionListener(lst);
		p1.add(btOK);
		JButton btCancel = new JButton("Cancel");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
		btCancel.addActionListener(lst);
		p1.add(btCancel);
		p.add(p1);
		pp.add(p);
		getContentPane().add(pp, BorderLayout.CENTER);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
	}

	
	public FontEx getFont()
	{
		return font;
	}
	
	public Color getFontColor()
	{
		return fgColor;
	}
	
	private void setFontColor(Color c){
		fgColor = c;
	}

	public boolean succeeded()
	{
		return succeeded;
	}

	protected void updatePreview()
	{
		String name = lstFontName.getSelected();
		int size = lstFontSize.getSelectedInt();
		if (size <= 0) return;
		int s = Font.PLAIN;
		if (chkBold.isSelected()) s |= Font.BOLD;
		if (chkItalic.isSelected()) s |= Font.ITALIC;
		
		font = new FontEx(name, s, size);
		font.setUnderline(chkUnderline.isSelected()); 
		font.setStrikethrough(chkStrikethrough.isSelected()); 
		font.setSubscript(chkSubscript.isSelected());
		font.setSuperscript(chkSuperscript.isSelected());
			
		fgColor = (Color) cbColor.getSelectedItem();
		
		font.populateStyle(style);
		StyleConstants.setForeground(style, fgColor);
		preview.getStyledDocument().setLogicalStyle(0, style);
		preview.setForeground(fgColor);
		preview.repaint();
	}
	
	protected void populateFields()
	{
		if(font == null) return;
		lstFontName.setSelected(font.getFamily());
		lstFontSize.setSelectedInt(font.getSize());
		chkBold.setSelected(font.isBold());
		chkItalic.setSelected(font.isItalic());
		cbColor.setSelectedItem(fgColor);
		chkUnderline.setSelected(font.isUnderline()); 
		chkStrikethrough.setSelected(font.isStrikethrough()); 
		chkSubscript.setSelected(font.isSubscript());
		chkSuperscript.setSelected(font.isSuperscript());
	}

	class OpenList extends JPanel implements ListSelectionListener,
			ActionListener
	{
		protected JLabel titleLabel;
		protected JTextField textField;
		protected JList list;
		protected JScrollPane scroll;

		public OpenList(String[] data, String title)
		{
			setLayout(null);
			titleLabel = new JLabel(title, JLabel.LEFT);
			add(titleLabel);
			textField = new JTextField();
			textField.addActionListener(this);
			add(textField);
			list = new JList(data);
			list.setVisibleRowCount(4);
			list.addListSelectionListener(this);
			list.setFont(textField.getFont());
			scroll = new JScrollPane(list);
			add(scroll);
		}

		public void setSelected(String sel)
		{
			list.setSelectedValue(sel, true);
			textField.setText(sel);
		}

		public String getSelected()
		{
			return textField.getText();
		}

		public void setSelectedInt(int value)
		{
			setSelected(Integer.toString(value));
		}

		public int getSelectedInt()
		{
			try
			{
				return Integer.parseInt(getSelected());
			}
			catch (NumberFormatException ex)
			{
				return -1;
			}
		}

		public void valueChanged(ListSelectionEvent e)
		{
			Object obj = list.getSelectedValue();
			if (obj != null) textField.setText(obj.toString());
		}

		public void actionPerformed(ActionEvent e)
		{
			ListModel model = list.getModel();
			String key = textField.getText().toLowerCase();
			for (int k = 0; k < model.getSize(); k++)
			{
				String data = (String) model.getElementAt(k);
				if (data.toLowerCase().startsWith(key))
				{
					list.setSelectedValue(data, true);
					break;
				}
			}
		}

		public void addListSelectionListener(ListSelectionListener lst)
		{
			list.addListSelectionListener(lst);
		}

		public Dimension getPreferredSize()
		{
			Insets ins = getInsets();
			Dimension d1 = titleLabel.getPreferredSize();
			Dimension d2 = textField.getPreferredSize();
			Dimension d3 = scroll.getPreferredSize();
			int w = Math.max(Math.max(d1.width, d2.width), d3.width);
			int h = d1.height + d2.height + d3.height;
			return new Dimension(w + ins.left + ins.right, h + ins.top
					+ ins.bottom);
		}

		public Dimension getMaximumSize()
		{
			Insets ins = getInsets();
			Dimension d1 = titleLabel.getMaximumSize();
			Dimension d2 = textField.getMaximumSize();
			Dimension d3 = scroll.getMaximumSize();
			int w = Math.max(Math.max(d1.width, d2.width), d3.width);
			int h = d1.height + d2.height + d3.height;
			return new Dimension(w + ins.left + ins.right, h + ins.top
					+ ins.bottom);
		}

		public Dimension getMinimumSize()
		{
			Insets ins = getInsets();
			Dimension d1 = titleLabel.getMinimumSize();
			Dimension d2 = textField.getMinimumSize();
			Dimension d3 = scroll.getMinimumSize();
			int w = Math.max(Math.max(d1.width, d2.width), d3.width);
			int h = d1.height + d2.height + d3.height;
			return new Dimension(w + ins.left + ins.right, h + ins.top
					+ ins.bottom);
		}

		public void doLayout()
		{
			Insets ins = getInsets();
			Dimension d = getSize();
			int x = ins.left;
			int y = ins.top;
			int w = d.width - ins.left - ins.right;
			int h = d.height - ins.top - ins.bottom;
			Dimension d1 = titleLabel.getPreferredSize();
			titleLabel.setBounds(x, y, w, d1.height);
			y += d1.height;
			Dimension d2 = textField.getPreferredSize();
			textField.setBounds(x, y, w, d2.height);
			y += d2.height;
			scroll.setBounds(x, y, w, h - y);
		}
	}

	class ColorComboRenderer extends JPanel implements ListCellRenderer
	{
		protected Color m_color = fgColor;
		protected Color m_focusColor = (Color) UIManager
				.get("List.selectionBackground");
		protected Color m_nonFocusColor = Color.white;

		public Component getListCellRendererComponent(JList list, Object obj,
				int row, boolean sel, boolean hasFocus)
		{
			if (hasFocus || sel)
				setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10,
						m_focusColor), new LineBorder(Color.black)));
			else
				setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10,
						m_nonFocusColor), new LineBorder(Color.black)));
			if (obj instanceof Color) m_color = (Color) obj;
			return this;
		}

		public void paintComponent(Graphics g)
		{
			setBackground(m_color);
			super.paintComponent(g);
		}
	}
}
