/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;

import seco.ThisNiche;
import seco.actions.CommonActions;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.rtenv.RuntimeContext;
import seco.talk.ConnectionManager;
import seco.things.CellGroup;
import seco.things.CellUtils;
import seco.things.CellVisual;

public class NicheBootListener implements HGListener
{
    public static boolean DEBUG_NICHE = false;
    public Result handle(HyperGraph hg, HGEvent event)
    {
    	ThisNiche.bindNiche(hg);
    	ThisNiche.initGUIController();
    	final JFrame f = ThisNiche.guiController.getFrame();
        RuntimeContext topRuntime = ThisNiche.getTopContext().getRuntimeContext(); 
        topRuntime.getBindings().put("desktop", ThisNiche.guiController);
        topRuntime.getBindings().put("canvas", ThisNiche.getCanvas());
        topRuntime.getBindings().put("frame", f);
        ThisNiche.graph.update(topRuntime);
        
        // We need to make sure that we have a TOP_CELL_GROUP, no matter what, even if
        // it was deleted by mistake.
        if (hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE) == null)
            GUIHelper.makeTopCellGroup();
        final CellGroup group = hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        final CellVisual v = hg.get(group.getVisual());
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                if(DEBUG_NICHE)
                {
                    new CommonActions.TopCellTreeAction().actionPerformed(null);
                    DEBUG_NICHE = false; 
                }
                else if (TopFrame.PICCOLO)
                {
                   CellUtils.evaluateVisibleInitCells();
                   v.bind(group);
                   if(f != null)
                      f.setVisible(true);
                    ConnectionManager.startConnections();
                }
                else if (f != null)
                	f.setVisible(true);
            	
            	Thread.currentThread().setUncaughtExceptionHandler(GUIHelper.getUncaughtExceptionHandler());                
            }
        });
        return Result.ok;
    }
}
