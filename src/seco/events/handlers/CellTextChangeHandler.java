package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellTextChangeEvent;
import seco.events.EventHandler;
import seco.events.CellTextChangeEvent.EventType;
import seco.notebook.NotebookDocument;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;



public class CellTextChangeHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if(instance == null){instance = hg.findOne(
                ThisNiche.hg, hg.and(hg.type(CellTextChangeHandler.class)));
        if(instance == null)
            instance = ThisNiche.hg.add(new CellTextChangeHandler());
        }
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellTextChangeEvent.HANDLE))
        {
            CellTextChangeEvent e = (CellTextChangeEvent) event;
            Object sub = ThisNiche.hg.get(subscriber);
            Object pub = ThisNiche.hg.get(publisher);
            if (pub instanceof NotebookDocument && sub instanceof CellGroupMember)
            {
                if (e.getCell().equals(subscriber))
                    CellUtils.processCelTextChangeEvent(subscriber, e);
            } else if (pub instanceof CellGroupMember 
                    && sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).cellTextChanged(e);
            }
        }
    }
 
}
