package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventDispatcher;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroupMember;

/**
 * <p>
 * Handle scriptlet cell evaluation events.
 * </p>
 * 
 * @author Konstantin Vandev
 *
 */
public class EvalCellHandler implements EventHandler
{
    private static final HGPersistentHandle HANDLE = 
        UUIDHandleFactory.I.makeHandle(
                "ee5bf630-b674-11df-8d81-0800200c9a66");
   
    public static HGHandle getHandle()
    {
        if (ThisNiche.graph.get(HANDLE) == null)
           ThisNiche.graph.define(HANDLE, new EvalCellHandler());
        return HANDLE;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE))
        {
            EvalCellEvent e = (EvalCellEvent) event;
            Object sub = ThisNiche.graph.get(subscriber);
            Object pub = ThisNiche.graph.get(publisher);
            if (pub instanceof NotebookDocument)
            {
                if (e.getCellHandle().equals(subscriber))
                {
                    EventDispatcher.dispatch(EvalCellEvent.HANDLE, e.getCellHandle(), e);
                }
            } else if (pub instanceof CellGroupMember 
                    && sub instanceof NotebookDocument)
            {
                ((NotebookDocument) sub).cellEvaled(e);
            }
        }
    }
}