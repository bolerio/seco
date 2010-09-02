package seco.events.handlers;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventHandler;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class CopyAttributeChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "bea1a390-b674-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new CopyAttributeChangeHandler());
        return HANDLE;
    }
   

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(AttributeChangeEvent.HANDLE))
        {
            AttributeChangeEvent e = (AttributeChangeEvent) event;
            Object sub = ThisNiche.graph.get(subscriber);
            if (e.getCellGroupMember().equals(publisher))
            if (sub instanceof CellGroupMember)
            {
                CellGroupMember c = (CellGroupMember) sub;
                CellUtils.removeEventPubSub(AttributeChangeEvent.HANDLE, publisher, subscriber, getHandle());
                c.setAttribute(e.getName(), e.getValue());
                CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE,
                         publisher, subscriber, getHandle());
            }
        }
    }

  

}