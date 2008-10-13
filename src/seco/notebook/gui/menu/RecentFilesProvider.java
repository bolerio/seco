/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;


import java.awt.event.*;
import java.io.File;
import java.io.Serializable;
import javax.swing.*;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.AppForm;
import seco.things.CellGroup;



import java.util.*;

/*
 * Loosely based on jEdit's RecentFilesProvider.java
 */
public class RecentFilesProvider implements DynamicMenuProvider, Serializable
{
	transient MouseListener mouseListener;
	
	public RecentFilesProvider()
	{
		mouseListener = new RFMouseListener();
	}
	
	public boolean updateEveryTime()
	{
		return true;
	} 

	public void update(JMenu menu)
	{
		
		final Vector<HGHandle> recentH = new Vector<HGHandle>(AppForm.getInstance().getConfig().getMRUF());
		final Vector<String> recentVector = new Vector<String>(recentH.size());
		if(recentVector.size() == 0)
		{
			JMenuItem menuItem = new JMenuItem(
				"No Recent Files");
			menuItem.setEnabled(false);
			menu.add(menuItem);
			return;
		}
		
		for(int i = 0; i < recentH.size(); i++)
		    recentVector.add(i, ((CellGroup)ThisNiche.hg.get(recentH.get(i))).getName());
				
		boolean sort = true;
        if(sort)
			Collections.sort(recentVector);
        
		for(String path : recentVector)
		{
			JMenuItem menuItem = new JMenuItem(path);
			menuItem.setActionCommand(path);
			menuItem.removeMouseListener(mouseListener);
			menuItem.addActionListener(new ActionListener()
			        {
		        public void actionPerformed(ActionEvent evt)
		        {
		            int i = recentVector.indexOf(evt.getActionCommand());
		            AppForm.getInstance().open(recentH.get(i));
		            AppForm.getInstance().getStatus().setMessage(null);
		        }
		    } );
			menuItem.addMouseListener(mouseListener);
			menu.add(menuItem);
		}
    }
	
	public static class RFMouseListener extends MouseAdapter
	{
		public void mouseEntered(MouseEvent evt)
		{
			AppForm.getInstance().getStatus().setMessage(
				((JMenuItem)evt.getSource())
				.getActionCommand());
		}

		public void mouseExited(MouseEvent evt)
		{
			AppForm.getInstance().getStatus().setMessage(null);
		}
	} 
}
