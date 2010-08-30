package seco.events.handlers;

import java.awt.Component;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EvalResult;
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
        if(instance == null)
            instance = hg.findOne(
                ThisNiche.graph, hg.and(hg.type(CopyEvalCellHandler.class)));
        if(instance == null || ThisNiche.handleOf(instance) == null)
            instance = ThisNiche.graph.add(new CopyEvalCellHandler());
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE))
        {
            EvalCellEvent e = (EvalCellEvent) event;
            Object sub = ThisNiche.graph.get(subscriber);
            Object pub = ThisNiche.graph.get(publisher);
            if (pub instanceof CellGroupMember && sub instanceof CellGroupMember)
            {
                CellUtils.removeEventPubSub(EvalCellEvent.HANDLE,
                        subscriber, publisher, getInstance());
                //CellUtils.removeEventPubSub(EvalCellEvent.HANDLE,
                //        publisher, subscriber, getInstance());
                EvalCellEvent n = new EvalCellEvent(subscriber, e.getValue(), e.getOldValue());
                EventDispatcher.dispatch(EvalCellEvent.HANDLE, e.getCellHandle(), n);
                CellUtils.addEventPubSub(EvalCellEvent.HANDLE,
                        subscriber, publisher, getInstance());
                //CellUtils.addEventPubSub(EvalCellEvent.HANDLE,
                //        publisher, subscriber, getInstance());
            } 
        }
    }
}
