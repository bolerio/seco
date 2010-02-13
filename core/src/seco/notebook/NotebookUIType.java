package seco.notebook;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomTypeBase;

public class NotebookUIType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        HGHandleFactory.makeHandle("d5534c73-da35-11dc-9d28-2925b8cbf8cd");
    
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
    {
        HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
        return new NotebookUI(layout[0]);
    }

    public void release(HGPersistentHandle handle) 
    {
    }

    public HGPersistentHandle store(Object instance) 
    {       
        HGPersistentHandle [] layout = new HGPersistentHandle[1];
        NotebookUI ui = (NotebookUI)instance;
        layout[0] = graph.getPersistentHandle(ui.getDoc().getBookHandle());
        return graph.getStore().store(layout);
    }
    
}
