package seco.events.handlers;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.EventHandler;
import seco.events.EventPubSub;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class CopyCellGroupChangeHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "d9f8bbf0-b675-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new CopyCellGroupChangeHandler());
        return HANDLE;
    }

   
    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE))
        {
            CellGroupChangeEvent e = (CellGroupChangeEvent) event;
            if (!e.getCellGroup().equals(publisher)) return;

            Object pub = ThisNiche.graph.get(publisher);
            if (pub instanceof CellGroupMember)
            {
                CellGroup main = (CellGroup) pub;
                CellGroup copy = (CellGroup) ThisNiche.graph.get(subscriber);
                CellUtils.removeEventPubSub(CellGroupChangeEvent.HANDLE,
                        subscriber, publisher, getHandle());

                HGHandle[] added = e.getChildrenAdded();
                HGHandle[] removed = e.getChildrenRemoved();
                HGHandle[] added_copy = null;
                HGHandle[] removed_copy = null; 
                int index = e.getIndex();
                if (removed != null && removed.length > 0)
                {
                    removed_copy = new HGHandle[removed.length];
                    for (int i = 0; i < removed.length; i++)
                    {
                       HGHandle rem = findAppropriateCopy(removed[i], copy);
                       if(rem == null) //shouldn't happen
                         System.err.println("Unable to find copy for: " + removed[i]);
                       else
                         removed_copy[i] = rem;
                    }
                }
                
                if (added != null && added.length > 0)
                {
                    added_copy = new HGHandle[added.length];
                    for (int i = 0; i < added.length; i++)
                    {
                        HGHandle cH = CellUtils.makeCopy(added[i]);
                        CellUtils.addCopyListeners(main.getTargetAt(index), cH);
                        added_copy[i] = cH;
                    }
                }
                CellGroupChangeEvent new_e = new CellGroupChangeEvent(
                        subscriber, index, added_copy, removed_copy);
                copy.batchProcess(new_e);
                CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE,
                        subscriber, publisher, getHandle());
            }
        }
    }
    
    //to find the counterpart of a removed element in the copy group
    //we search for a corresponding CopyAttributeChangeHandler
    private static HGHandle findAppropriateCopy(HGHandle h, CellGroup copy)
    {
         List<EventPubSub> subs = hg.getAll(ThisNiche.graph, 
                hg.and(hg.type(EventPubSub.class), hg
                .incident(h), hg.orderedLink(new HGHandle[] {
                AttributeChangeEvent.HANDLE, ThisNiche.graph.getHandleFactory().anyHandle(), 
                    ThisNiche.graph.getHandleFactory().anyHandle(), 
                ThisNiche.graph.getHandleFactory().anyHandle()})));
        for (EventPubSub eps : subs)
        {
            EventHandler eh = (EventHandler) ThisNiche.graph.get(eps.getEventHandler());
            if(eh instanceof CopyAttributeChangeHandler)
            {
               if (copy.indexOf(eps.getSubscriber()) != -1)
                   return (eps.getSubscriber());
            }
        }
        return null;
    }
}