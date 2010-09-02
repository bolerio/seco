package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventDispatcher;
import seco.events.EventHandler;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class CopyEvalCellHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "4fee29a0-b688-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new CopyEvalCellHandler());
        return HANDLE;
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
                        subscriber, publisher, getHandle());
                //CellUtils.removeEventPubSub(EvalCellEvent.HANDLE,
                //        publisher, subscriber, getInstance());
                EvalCellEvent n = new EvalCellEvent(subscriber, e.getValue(), e.getOldValue());
                EventDispatcher.dispatch(EvalCellEvent.HANDLE, e.getCellHandle(), n);
                CellUtils.addEventPubSub(EvalCellEvent.HANDLE,
                        subscriber, publisher, getHandle());
                //CellUtils.addEventPubSub(EvalCellEvent.HANDLE,
                //        publisher, subscriber, getInstance());
            } 
        }
    }
}
