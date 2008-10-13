/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;

import static seco.U.hfind;
import static seco.U.hget;
import static seco.U.hhandle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.U;
import seco.notebook.AppForm;
import seco.rtenv.RuntimeContext;



public class RCListProvider implements DynamicMenuProvider 
{
	public void update(JMenu m) 
	{
		ButtonGroup group = new ButtonGroup();
		HGSearchResult<HGHandle> rs = hfind(hg.type(RuntimeContext.class));
		try
		{
			while (rs.hasNext())
			{
				final HGHandle rh = rs.next();
				RuntimeContext rc = (RuntimeContext) hget(rh);			
				final JRadioButtonMenuItem item = new JRadioButtonMenuItem(rc.getName());
				final HGHandle bh = ThisNiche.getHyperGraph().getHandle(
					AppForm.getInstance().getCurrentNotebook().getDoc().getBook());
				if (ThisNiche.getContextHandleFor(bh).equals(rh))
					item.setSelected(true);
				group.add(item);			
				Action act = new AbstractAction() 
				{
					private static final long serialVersionUID = -1;
					public void actionPerformed(ActionEvent e)
					{
						ThisNiche.setContextFor(bh, rh);
						item.setSelected(true);
						AppForm.getInstance().setCurrentEvaluationContext(rh);
					}
				};
				act.putValue(Action.NAME, rc.getName());
				item.setAction(act);
				m.add(item);
			}
		}
		finally
		{
			U.closeNoException(rs);
		}
	}

	public boolean updateEveryTime() 
	{
		return true;
	}
}
