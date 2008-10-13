package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
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
            instance = hg.findOne(
                    ThisNiche.hg, hg.and(hg.type(CopyCellGroupChangeHandler.class)));
            if(instance == null)
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

                // TODO:??? add stuff one by one or in batch
                HGHandle[] added = e.getChildrenAdded();
                HGHandle[] removed = e.getChildrenRemoved();
                int index = e.getIndex();
                if (removed != null && removed.length > 0)
                    for (int i = 0; i < removed.length; i++)
                        copy.remove(index);
                if (added != null && added.length > 0)
                    for (int i = 0; i < added.length; i++){
                        HGHandle cH = CellUtils.makeCopy(added[i]);
                        copy.insert(index, cH);
                        CellUtils.addCopyListeners(main.getTargetAt(index), cH);
                     }
                // copy.batchProcess(e);
                CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE,
                        subscriber, publisher, getInstance());
            }
       }
    }
}