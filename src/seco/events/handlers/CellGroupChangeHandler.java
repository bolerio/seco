package seco.events.handlers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EventHandler;
import seco.notebook.NotebookDocument;
import seco.things.CellGroup;
import seco.things.CellGroupMember;



public class CellGroupChangeHandler implements EventHandler
{
    private static HGHandle instance = null;
    
    public static HGHandle getInstance(){
        if(instance == null){
            instance = hg.findOne(
                    ThisNiche.hg, hg.and(hg.type(CellGroupChangeHandler.class)));
            if(instance == null)
                instance = ThisNiche.hg.add(new CellGroupChangeHandler());
         }
        return instance;
    }
    
    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE))
        {
            CellGroupChangeEvent e = (CellGroupChangeEvent) event;
            Object sub = ThisNiche.hg.get(subscriber);
            Object pub = ThisNiche.hg.get(publisher);
            if(sub instanceof CellGroup && pub instanceof NotebookDocument){
                if (e.getCellGroup().equals(subscriber))
                   ((CellGroup)sub).batchProcess(e);
            }else 
            if(sub instanceof NotebookDocument)
            {
                ((NotebookDocument)sub).cellGroupChanged(e);
            }
        }
   }
    
}