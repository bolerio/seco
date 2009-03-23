package seco.events;

import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.things.CellUtils;

public class EventDispatcher
{
    public static void dispatch(HGHandle eventType, HGHandle publisher,
            Object event)
    {
        Set<EventPubSub> set = CellUtils.findAll(ThisNiche.hg, hg.apply(hg
                .deref(ThisNiche.hg), hg
                .and(hg.type(EventPubSub.class), hg.incident(eventType), hg
                        .incident(publisher), hg.orderedLink(new HGHandle[] {
                        eventType, publisher, HGHandleFactory.anyHandle,
                        HGHandleFactory.anyHandle }))));
        for (EventPubSub s : set)
        {
            EventHandler handler = (EventHandler) ThisNiche.hg.get(s
                    .getEventHandler());
            if (handler != null)
                handler.handle(eventType, event, publisher, s.getSubscriber());
        }

        // TODO1: the above code don't work
        if (set.isEmpty())
        {
            System.out.println("EventDispatcher: " + publisher + ":" + event);
            for (HGHandle h : ThisNiche.hg.getIncidenceSet(publisher))
            {
                Object o = ThisNiche.hg.get(h);
                if (o instanceof EventPubSub
                        && ((EventPubSub) o).getEventType().equals(eventType)
                        && ((EventPubSub) o).getPublisher().equals(publisher))
                {
                    System.out.println("" + o);
                    set.add((EventPubSub) o);
                }
            }
            for (EventPubSub eps : set)
            {
                EventHandler handler = (EventHandler) ThisNiche.hg.get(eps
                        .getEventHandler());
                if (handler != null)
                    handler.handle(eventType, event, publisher, eps
                            .getSubscriber());
            }
        }
    }
}