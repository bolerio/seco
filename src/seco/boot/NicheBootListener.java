/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import javax.swing.SwingUtilities;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroup;
import seco.things.CellVisual;

public class NicheBootListener implements HGListener
{
    public Result handle(HyperGraph hg, HGEvent event)
    {
    	ThisNiche.bindNiche(hg);
    	final TopFrame s = TopFrame.getInstance();
        RuntimeContext topRuntime = ThisNiche.getTopContext().getRuntimeContext(); 
        topRuntime.getBindings().put("desktop", TopFrame.getInstance());
        topRuntime.getBindings().put("canvas", TopFrame.getInstance().getCanvas());
        ThisNiche.hg.update(topRuntime);
        
        // We need to make sure that we have a TOP_CELL_GROUP, no matter what, even if
        // it was deleted by mistake.
        if (hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE) == null)
            GUIHelper.makeTopCellGroup(hg);
        final CellGroup group = (CellGroup) hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        final CellVisual v = (CellVisual) hg.get(group.getVisual());
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                v.bind(group);
                s.setVisible(true);
            }
        });
        return Result.ok;
    }
}
