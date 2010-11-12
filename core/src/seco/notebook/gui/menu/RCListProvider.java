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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.U;
import seco.notebook.NotebookUI;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroupMember;

public class RCListProvider implements DynamicMenuProvider
{
    private static final long serialVersionUID = 3127888958558526166L;
    protected boolean cellOrGroup;
    public RCListProvider()
    {
        
    }
    
    public RCListProvider(boolean cellOrGroup)
    {
        this.cellOrGroup = cellOrGroup;
    }
    
    
    
    public void update(JMenu m)
    {
        CellGroupMember owner = null;
        if (owner == null && NotebookUI.getFocusedNotebookUI() == null) return;
        if(owner == null)
           owner = NotebookUI.getFocusedNotebookUI().getDoc().getBook();
        ButtonGroup group = new ButtonGroup();
        HGSearchResult<HGHandle> rs = hfind(hg.type(RuntimeContext.class));
        try
        {
            final HGHandle bh = ThisNiche.graph.getHandle(owner);
            while (rs.hasNext())
            {
                final HGHandle rh = rs.next();
                RuntimeContext rc = (RuntimeContext) hget(rh);
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem(rc
                        .getName());
                if (ThisNiche.getContextHandleFor(bh).equals(rh))
                    item.setSelected(true);
                group.add(item);
                Action act = new AbstractAction() {
                    private static final long serialVersionUID = -1;

                    public void actionPerformed(ActionEvent e)
                    {
                        ThisNiche.setContextFor(bh, rh);
                        item.setSelected(true);
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
