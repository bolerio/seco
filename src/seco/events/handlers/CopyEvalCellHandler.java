package seco.events.handlers;

import java.awt.Component;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventDispatcher;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.IOUtils;



public class CopyEvalCellHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if(instance == null){instance = hg.findOne(
                ThisNiche.hg, hg.and(hg.type(CopyEvalCellHandler.class)));
        if(instance == null)
            instance = ThisNiche.hg.add(new CopyEvalCellHandler());
        }
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE))
        {
            EvalCellEvent e = (EvalCellEvent) event;
            Object sub = ThisNiche.hg.get(subscriber);
            Object pub = ThisNiche.hg.get(publisher);
            if (pub instanceof CellGroupMember && sub instanceof CellGroupMember)
            {
                CellUtils.removeEventPubSub(EvalCellEvent.HANDLE,
                        subscriber, publisher, getInstance());
                Object val = e.getValue();
                if(val instanceof Component)
                    val = IOUtils.cloneCellComp((Component) val);
                CellUtils.setOutputCell(subscriber, val);
                EvalCellEvent n = new EvalCellEvent(subscriber, e.getValue(), e.getOldValue());
                EventDispatcher.dispatch(EvalCellEvent.HANDLE, e.getCellHandle(), n);
                CellUtils.addEventPubSub(EvalCellEvent.HANDLE,
                        subscriber, publisher, getInstance());
            } 
        }
    }
}
