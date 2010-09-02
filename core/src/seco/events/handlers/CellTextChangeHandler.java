package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.CellTextChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class CellTextChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "1ae87df0-b674-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
        {
           System.out.println("define CellTextChangeHandler"); 
           ThisNiche.graph.define(HANDLE, new CellTextChangeHandler());
        }
        return HANDLE;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellTextChangeEvent.HANDLE))
        {
            CellTextChangeEvent e = (CellTextChangeEvent) event;
            Object sub = ThisNiche.graph.get(subscriber);
            Object pub = ThisNiche.graph.get(publisher);
            if (pub instanceof NotebookDocument && sub instanceof CellGroupMember)
            {
                if (e.getCell().equals(subscriber))
                    CellUtils.processCelTextChangeEvent(subscriber, e);
            } else if (pub instanceof CellGroupMember 
                    && sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).cellTextChanged(e);
            }
        }
    }
 
}
