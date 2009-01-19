/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import seco.gui.StandaloneFrame;
import seco.notebook.NotebookUI;


public class GroupingProvider implements DynamicMenuProvider
{
	protected NotebookUI nbui;
	protected StandaloneFrame app;
	
	public boolean updateEveryTime()
	{
		return true;
	}

	public void update(JMenu menu)
	{
		nbui = NotebookUI.getFocusedNotebookUI();
		if(nbui == null) return;
		
		JMenuItem mi = new JMenuItem(new AbstractAction("Group Cells"){

			public void actionPerformed(ActionEvent e)
			{
				//System.out.println("GroupingProvider - group: " + nbui.getSelectionManager().getSelection());
				if(!nbui.getSelectionManager().canGroup()) return;
				nbui.getSelectionManager().group();
			}});
		mi.setEnabled(nbui.getSelectionManager().canGroup());
		menu.add(mi);
		mi = new JMenuItem(	new AbstractAction("Ungroup Cells"){

			public void actionPerformed(ActionEvent e)
			{
				//System.out.println("NotebookUI - ungroup: " + nbui.getSelectionManager().getSelection());
				if(!nbui.getSelectionManager().canUngroup()) return;
				nbui.getSelectionManager().ungroup();
			}});
		mi.setEnabled(nbui.getSelectionManager().canUngroup());
		menu.add(mi);
	}
}

