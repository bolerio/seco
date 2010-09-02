package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroupMember;


public class AttributeChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "381d68f0-b673-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new AttributeChangeHandler());
        return HANDLE;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(AttributeChangeEvent.HANDLE))
        {
            AttributeChangeEvent e = (AttributeChangeEvent) event;
            Object pub = ThisNiche.graph.get(publisher);
            Object sub = ThisNiche.graph.get(subscriber);
            if (pub instanceof NotebookDocument
                    && sub instanceof CellGroupMember)
            {
                if (e.getCellGroupMember().equals(subscriber))
                    ((CellGroupMember) sub).setAttribute(e.getName(), e
                            .getValue());
            } else if (pub instanceof CellGroupMember
                    && sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).attributeChanged(e);
            }
        }
    }

}
