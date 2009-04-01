package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

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
 * @author Borislav Iordanov
 *
 */
public class EvalCellHandler implements EventHandler
{
    private static HGHandle instance = null;

    public static HGHandle getInstance()
    {
        if(instance == null){instance = hg.findOne(
                ThisNiche.hg, hg.and(hg.type(EvalCellHandler.class)));
        if(instance == null)
            instance = ThisNiche.hg.add(new EvalCellHandler());
        }
        return instance;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE))
        {
            EvalCellEvent e = (EvalCellEvent) event;
            Object sub = ThisNiche.hg.get(subscriber);
            Object pub = ThisNiche.hg.get(publisher);
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