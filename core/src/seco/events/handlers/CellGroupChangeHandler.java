package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroup;

public class CellGroupChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "f633ac00-b673-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new CellGroupChangeHandler());
        return HANDLE;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE))
        {
            CellGroupChangeEvent e = (CellGroupChangeEvent) event;
            Object sub = ThisNiche.graph.get(subscriber);
            Object pub = ThisNiche.graph.get(publisher);
            if (sub instanceof CellGroup && pub instanceof NotebookDocument)
            {
                if (e.getCellGroup().equals(subscriber))
                    ((CellGroup) sub).batchProcess(e);
            }
            else if (sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).cellGroupChanged(e);
            }
        }
    }

}