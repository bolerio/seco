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

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import seco.notebook.NotebookUI;
import seco.notebook.gui.menu.DynamicMenuProvider;


public class ScriptEngineProvider implements DynamicMenuProvider 
{
	public boolean updateEveryTime() 
	{
		return true;
	}

	public void update(JMenu menu) 
	{		
	    final NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
		String def_name = ui.getDoc().getDefaultEngineName();
		ButtonGroup group = new ButtonGroup();		
		java.util.Iterator<String> all = ui.getDoc().getEvaluationContext().getLanguages();
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
						ui.getDoc().setDefaultEngineName(language);
				}
			});
			group.add(m);
			menu.add(m);
		}
	}
}
