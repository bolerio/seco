package seco.events.handlers;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventHandler;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class CopyAttributeChangeHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if (instance == null)
        {
            instance = hg.findOne(
                    ThisNiche.graph, hg.and(hg.type(CopyAttributeChangeHandler.class)));
            if(instance == null)
                instance = ThisNiche.graph.add(new CopyAttributeChangeHandler());
        }
        return instance;
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
                CellUtils.removeEventPubSub(AttributeChangeEvent.HANDLE, publisher, subscriber, getInstance());
                c.setAttribute(e.getName(), e.getValue());
                CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE,
                         publisher, subscriber, getInstance());
            }
        }
    }

  

}