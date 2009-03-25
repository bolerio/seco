package seco.events;

import java.util.ArrayList;
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
        
//        List<HGHandle> L1 = hg.findAll(ThisNiche.hg,
//                hg.and(hg.type(EventPubSub.class), 
//                       hg.orderedLink(eventType, publisher, hg.anyHandle(), hg.anyHandle())));
//       
//        List<HGHandle> L2 = new ArrayList<HGHandle>();
//        for(HGHandle h: ThisNiche.hg.getIncidenceSet(publisher))
//        {
//           Object o = ThisNiche.hg.get(h);
//           if(o instanceof EventPubSub)
//           {
//               EventPubSub eps =(EventPubSub) o;
//               if(eps.getEventType().equals(eventType) 
//                       && eps.getPublisher().equals(publisher))
//                   L2.add(h);
//           }
//        }
//        
//        assert (L.size() == L1.size() && L1.size() == L2.size()) : 
//            new RuntimeException("EventDispatcher - dispatch: " + L.size() + 
//                    ":" + L1.size() + ":" + L2.size());
    }
}