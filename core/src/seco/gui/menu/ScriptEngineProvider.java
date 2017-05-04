/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui.menu;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import seco.ThisNiche;
import seco.notebook.NotebookUI;


public class ScriptEngineProvider implements DynamicMenuProvider 
{
	private boolean contextualized = true;
		
	public void setContextualized(boolean contextualized)
	{
		this.contextualized = contextualized;
	}
	
	public boolean isContextualized()
	{
		return this.contextualized;
	}
	
	public boolean updateEveryTime() 
	{
		return true;
	}

	public void update(JMenu menu) 
	{		
	    final NotebookUI ui = NotebookUI.getFocusedNotebookUI();
	    if (contextualized && ui == null) return;
		String def_name = contextualized ? ui.getDoc().getDefaultEngineName() : ThisNiche.defaultLanguage();
		ButtonGroup group = new ButtonGroup();		
		java.util.Iterator<String> all = contextualized
				? ui.getDoc().getEvaluationContext().getLanguages()
				: ThisNiche.allLanguages().keySet().iterator();
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
					{
						if (contextualized)
							ui.getDoc().setDefaultEngineName(language);
						else
							ThisNiche.defaultLanguage(language);
					}
				}
			});
			group.add(m);
			menu.add(m);
		}
	}
}
