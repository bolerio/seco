package seco.events.handlers;

import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.events.EventPubSub;
import seco.notebook.NotebookDocument;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class CopyCellGroupChangeHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if (instance == null)
        {
            instance = hg.findOne(ThisNiche.hg, hg.and(hg
                    .type(CopyCellGroupChangeHandler.class)));
            if (instance == null)
                instance = ThisNiche.hg.add(new CopyCellGroupChangeHandler());
        }
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE))
        {
            CellGroupChangeEvent e = (CellGroupChangeEvent) event;
            if (!e.getCellGroup().equals(publisher)) return;

            Object pub = ThisNiche.hg.get(publisher);
            if (pub instanceof CellGroupMember)
            {
                CellGroup main = (CellGroup) pub;
                CellGroup copy = (CellGroup) ThisNiche.hg.get(subscriber);
                CellUtils.removeEventPubSub(CellGroupChangeEvent.HANDLE,
                        subscriber, publisher, getInstance());

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
                        subscriber, publisher, getInstance());
            }
        }
    }
    
    //to find the counterpart of a removed element in the copy group
    //we search for a corresponding CopyAttributeChangeHandler
    private static HGHandle findAppropriateCopy(HGHandle h, CellGroup copy)
    {
         List<EventPubSub> subs = hg.getAll(ThisNiche.hg, 
                hg.and(hg.type(EventPubSub.class), hg
                .incident(h), hg.orderedLink(new HGHandle[] {
                AttributeChangeEvent.HANDLE, HGHandleFactory.anyHandle, 
                HGHandleFactory.anyHandle, HGHandleFactory.anyHandle })));
        for (EventPubSub eps : subs)
        {
            EventHandler eh = (EventHandler) ThisNiche.hg.get(eps.getEventHandler());
            if(eh instanceof CopyAttributeChangeHandler)
            {
               if (copy.indexOf(eps.getSubscriber()) != -1)
                   return (eps.getSubscriber());
            }
        }
        return null;
    }
}