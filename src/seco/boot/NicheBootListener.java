/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import seco.ThisNiche;
import seco.notebook.AppForm;
import seco.notebook.PiccoloFrame;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroup;
import seco.things.CellVisual;

public class NicheBootListener implements HGListener
{
    public Result handle(HyperGraph hg, HGEvent event)
    {
    	ThisNiche.bindNiche(hg);
        PiccoloFrame s = PiccoloFrame.getInstance();
        RuntimeContext topRuntime = ThisNiche.getTopContext().getRuntimeContext(); 
        topRuntime.getBindings().put("desktop", AppForm.getInstance());
        ThisNiche.hg.update(topRuntime);
//            s.loadComponents();
//            AppForm.getInstance().openBooks();
//            ThisNiche.topContext = ThisNiche
//                    .getEvaluationContext(ThisNiche.TOP_CONTEXT_HANDLE);
//            s.getCanvas().loadDims();
        CellGroup group = (CellGroup) hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellVisual v = (CellVisual) hg.get(group.getVisual());
        v.bind(group, null);
        s.setVisible(true);
        return Result.ok;
    }
}
