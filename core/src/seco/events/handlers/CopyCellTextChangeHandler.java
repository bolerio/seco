package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.CellTextChangeEvent;
import seco.events.EventHandler;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class CopyCellTextChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "24f46c50-b688-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new CopyCellTextChangeHandler());
        return HANDLE;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellTextChangeEvent.HANDLE))
        {
            CellTextChangeEvent e = (CellTextChangeEvent) event;
            Object pub = ThisNiche.graph.get(publisher);

            if (pub instanceof CellGroupMember)
            {
                if (e.getCell().equals(publisher))
                  processEvent(publisher, subscriber, e);
            }
//            else if (pub instanceof NotebookDocument)
//            {
//                if (e.getCell().equals(subscriber))
//                    ((NotebookDocument) pub).cellTextChanged(e);
//            }
        }
    }

    protected void processEvent(HGHandle publisher, HGHandle subscriber, CellTextChangeEvent e)
    {
        CellUtils.removeEventPubSub(CellTextChangeEvent.HANDLE,
                subscriber, publisher, getHandle());
        CellTextChangeEvent n = new CellTextChangeEvent(subscriber, e.getType(),
                e.getText(), e.getOffset(), e.getLength());
        CellUtils.processCelTextChangeEvent(subscriber, n);
        CellUtils.addEventPubSub(CellTextChangeEvent.HANDLE,
                subscriber, publisher, getHandle());
    }
    
    
}
