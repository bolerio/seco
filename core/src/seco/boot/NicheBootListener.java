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
import javax.swing.UIManager;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.SecoUncaughtExceptionHandler;
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
        final CellGroup group = (CellGroup) hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        final CellVisual v = (CellVisual) hg.get(group.getVisual());
//        try
//        {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        }
//        catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                if(DEBUG_NICHE)
                {
                    new GUIHelper.TopCellTreeAction().actionPerformed(null);
                    DEBUG_NICHE = false; 
                }else{
                   CellUtils.evaluateVisibleInitCells();
                   v.bind(group);
                   if(f != null)
                      f.setVisible(true);
                   ConnectionManager.startConnections();
                }
            	Thread.currentThread().setUncaughtExceptionHandler(GUIHelper.getUncaughtExceptionHandler());                
            }
        });
        return Result.ok;
    }
}
