package seco.events;

import java.util.List;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import seco.ThisNiche;

public class EventDispatcher
{
    public static void dispatch(HGHandle eventType, HGHandle publisher,
            Object event)
    {
        List<EventPubSub> L = hg.getAll(ThisNiche.hg,
                                        hg.and(hg.type(EventPubSub.class), 
                                               hg.orderedLink(eventType, publisher, hg.anyHandle(), hg.anyHandle())));
        for (EventPubSub s : L)
        {
            EventHandler handler = (EventHandler) ThisNiche.hg.get(s
                    .getEventHandler());
            if (handler != null)
                handler.handle(eventType, event, publisher, s.getSubscriber());
        }
    }
}