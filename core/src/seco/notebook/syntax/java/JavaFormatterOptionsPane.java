/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.java;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import seco.AppConfig;
import seco.gui.common.OptionPane;
import seco.notebook.NotebookUI;
import seco.notebook.Utilities;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;


public class JavaFormatterOptionsPane extends PropertySheetPanel implements OptionPane
{
	protected NotebookUI nbui;
	protected  JavaFormatterOptions options;

	public JavaFormatterOptionsPane()
	{
	}

	public void setNotebookUI(NotebookUI nbui)
	{
		this.nbui = nbui;
	}

	public String getName()
	{
		return "Formatter Properties";
	}

	public Component getComponent()
	{
		return this;
	}

	public void init()
	{
		setPreferredSize(new Dimension(250,200));
		setDescriptionVisible(true);
		setSortingCategories(true);
		setSortingProperties(true);
		setRestoreToggleStates(true);
		//add(sheet, "*");
		options = (JavaFormatterOptions) 
		AppConfig.getInstance().getProperty(
						AppConfig.FORMATTER_OPTIONS,
						new JavaFormatterOptions());
		new BeanBinder(options, this);
		addPropertySheetChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt)
					{
						Utilities.resetFormatter(nbui, 5);
						Utilities.formatCell(nbui, 5);
					}
				});
		Utilities.formatCell(nbui, 5);
	}

	public void resetDefaults()
	{
		options = new JavaFormatterOptions();
		AppConfig.getInstance().setProperty(AppConfig.FORMATTER_OPTIONS,
				options);
	}

	public void save()
	{
		AppConfig.getInstance().setProperty(AppConfig.FORMATTER_OPTIONS,
				options);
	}
}
