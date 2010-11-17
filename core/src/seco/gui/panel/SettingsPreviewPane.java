/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.gui.common.OptionPane;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.things.CellGroup;
import seco.things.CellUtils;


public class SettingsPreviewPane extends JSplitPane
{
	private static final long serialVersionUID = -5690086043456987656L;
    private static final String TEMPLATE = "seco/gui/JavaTemplate.txt";
	protected NotebookDocument doc;
	protected NotebookUI previewPane = null;
	protected CellGroup book;
	protected OptionPane optionPane;

	public SettingsPreviewPane(NotebookDocument doc, final OptionPane optionPane,
			NotebookUI _previewPane)
	{
		this.optionPane = optionPane;
		this.doc = doc;
		if (_previewPane == null)
			initSimpleBook();
		else
			previewPane = _previewPane;
		optionPane.setNotebookUI(previewPane);
		optionPane.init();
		JScrollPane previewScroll = new JScrollPane(previewPane);
		JPanel left = new JPanel(new BorderLayout());
		left.add(optionPane.getComponent(), BorderLayout.NORTH);
		JButton reset = new JButton("Reset Defaults");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				optionPane.resetDefaults();
			}
		});
		left.add(reset, BorderLayout.SOUTH);
		left.setBorder(new TitledBorder(optionPane.getName()));
		this.setLeftComponent(left);
		this.setRightComponent(previewScroll);
		this.setPreferredSize(new Dimension(700, 400));
		this.setDividerLocation(0.5);
	}

	public SettingsPreviewPane(final OptionPane optionPane)
	{
		this(null, optionPane, null);
	}

	NotebookUI getPreviewPane()
	{
		return previewPane;
	}

	public void save()
	{
		optionPane.save();
	}

	protected void initSimpleBook()
	{
		HGHandle bookH = CellUtils.createGroupHandle();
		book = (CellGroup) ThisNiche.graph.get(bookH);
		//if(doc != null)
		//  for(Iterator<NBStyle> it = doc.getStyles(); it.hasNext();)
		//      CellUtils.addStyle(book, it.next());
	    book.insert(0, CellUtils.makeCellH(readTemplate(), "beanshell"));
		
		previewPane = new NotebookUI(bookH);
		previewPane.setBackground(Color.white);
		// previewPane.setEditable(false);
		previewPane.setBorder(new TitledBorder("Preview"));
	}
	private static String SAMPLE = "public class Example\n{\n	public static class "
			+ "Pair\n{public String first;public String second;};"
			+ "private LinkedList fList;	public int counter;"
			+ "public Example(LinkedList list)	{fList = list;"
			+ "	counter = 0;}public void push(Pair p){"
			+ "fList.add(p);	++counter;} public Object pop()"
			+ "{--counter; return (Pair) fList.getLast();}}";

	private String readTemplate()
	{
		try
		{
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream(TEMPLATE);
			if(is == null) throw new Exception("Resource: " + TEMPLATE + 
			         " could not be found");
			InputStreamReader in = new InputStreamReader(is);
			char[] buff = new char[1024];
			StringBuffer sbuff = new StringBuffer();
			while ((in.read(buff, 0, buff.length)) != -1)
			{
				sbuff.append(buff);
			}
			return sbuff.toString();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return SAMPLE;
	}
}
