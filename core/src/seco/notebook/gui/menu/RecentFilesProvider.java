/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.notebook.AppConfig;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

/*
 * Loosely based on jEdit's RecentFilesProvider.java
 */
public class RecentFilesProvider implements DynamicMenuProvider, Serializable
{
	private static final long serialVersionUID = 8629428510393152418L;
	
    //transient MouseListener mouseListener;
	
	public RecentFilesProvider()
	{
		//mouseListener = new RFMouseListener();
	}
	
	public boolean updateEveryTime()
	{
		return true;
	} 

	public void update(JMenu menu)
	{
		
		final Vector<HGHandle> recentH = new Vector<HGHandle>(AppConfig.getInstance().getMRUF());
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
		    recentVector.add(i, CellUtils.getName((CellGroupMember)
		            ThisNiche.graph.get(recentH.get(i))));
				
		boolean sort = true;
        if(sort)
			Collections.sort(recentVector);
        
		for(String path : recentVector)
		{
			JMenuItem menuItem = new JMenuItem(path);
			menuItem.setActionCommand(path);
			//menuItem.removeMouseListener(mouseListener);
			menuItem.addActionListener(new ActionListener()
			        {
		        public void actionPerformed(ActionEvent evt)
		        {
		            int i = recentVector.indexOf(evt.getActionCommand());
		            GUIHelper.openNotebook(recentH.get(i));
		            //TopFrame.getInstance().setStatusBarMessage(null);
		        }
		    } );
			//menuItem.addMouseListener(mouseListener);
			menu.add(menuItem);
		}
    }
	
//	public static class RFMouseListener extends MouseAdapter
//	{
//		public void mouseEntered(MouseEvent evt)
//		{
//		    TopFrame.getInstance().setStatusBarMessage(
//				((JMenuItem)evt.getSource())
//				.getActionCommand());
//		}
//
//		public void mouseExited(MouseEvent evt)
//		{
//		    TopFrame.getInstance().setStatusBarMessage(null);
//		}
//	} 
}
