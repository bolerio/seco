package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroupMember;



public class AttributeChangeHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if (instance == null)
           instance = hg.findOne(ThisNiche.graph, hg.and(hg
                    .type(AttributeChangeHandler.class)));
           if(instance == null || ThisNiche.handleOf(instance) == null)
                instance = ThisNiche.graph.add(new AttributeChangeHandler());
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(AttributeChangeEvent.HANDLE))
        {
            AttributeChangeEvent e = (AttributeChangeEvent) event;
            Object pub = ThisNiche.graph.get(publisher);
            Object sub = ThisNiche.graph.get(subscriber);
            if (pub instanceof NotebookDocument
                    && sub instanceof CellGroupMember)
            {
                if (e.getCellGroupMember().equals(subscriber))
                    ((CellGroupMember) sub).setAttribute(e.getName(), e
                            .getValue());
            } else if (pub instanceof CellGroupMember
                    && sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).attributeChanged(e);
            }
        }
    }

}
