/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import seco.notebook.AppForm;
import seco.notebook.gui.menu.DynamicMenuProvider;


public class ScriptEngineProvider implements DynamicMenuProvider 
{
	protected transient AppForm app;

	public ScriptEngineProvider(final AppForm app) 
	{
		this.app = app;
	}

	public boolean updateEveryTime() 
	{
		return true;
	}

	public void update(JMenu menu) 
	{		
		if(app.getCurrentNotebook() == null)
			return;
		String def_name = app.getCurrentNotebook().getDoc().getDefaultEngineName();
		ButtonGroup group = new ButtonGroup();		
		java.util.Iterator<String> all = app.getCurrentNotebook().getDoc().getEvaluationContext().getLanguages();
		while (all.hasNext())			
		{
			final String language = all.next();
			final JRadioButtonMenuItem m = new JRadioButtonMenuItem(language);
			m.setSelected(language.equals(def_name));
			m.addItemListener(new ItemListener() 
			{
				public void itemStateChanged(ItemEvent e) 
				{
					if (m.isSelected())
						app.getCurrentNotebook().getDoc().setDefaultEngineName(language);
				}
			});
			group.add(m);
			menu.add(m);
		}
	}
}
