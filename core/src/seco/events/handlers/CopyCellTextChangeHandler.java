package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;



public class CopyCellTextChangeHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if(instance == null)
            instance = hg.findOne(
                ThisNiche.graph, hg.and(hg.type(CopyCellTextChangeHandler.class)));
        if(instance == null || ThisNiche.handleOf(instance) == null)
            instance = ThisNiche.graph.add(new CopyCellTextChangeHandler());
        
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellTextChangeEvent.HANDLE))
        {
            CellTextChangeEvent e = (CellTextChangeEvent) event;
            Object pub = ThisNiche.graph.get(publisher);

            if (pub instanceof CellGroupMember)
            {
                if (e.getCell().equals(publisher))
                  processEvent(publisher, subscriber, e);
            }
//            else if (pub instanceof NotebookDocument)
//            {
//                if (e.getCell().equals(subscriber))
//                    ((NotebookDocument) pub).cellTextChanged(e);
//            }
        }
    }

    protected void processEvent(HGHandle publisher, HGHandle subscriber, CellTextChangeEvent e)
    {
        CellUtils.removeEventPubSub(CellTextChangeEvent.HANDLE,
                subscriber, publisher, getInstance());
        CellTextChangeEvent n = new CellTextChangeEvent(subscriber, e.getType(),
                e.getText(), e.getOffset(), e.getLength());
        CellUtils.processCelTextChangeEvent(subscriber, n);
        CellUtils.addEventPubSub(CellTextChangeEvent.HANDLE,
                subscriber, publisher, getInstance());
    }
    
    
}
