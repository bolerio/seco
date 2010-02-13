package seco.notebook;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomTypeBase;


public class NotebookDocumentType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        HGHandleFactory.makeHandle("d36809b9-c42f-11dc-ab02-2795799ef366");
    
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
    {
        HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
        NotebookDocument doc = new NotebookDocument(layout[0]); 
        return doc;
    }

    public void release(HGPersistentHandle handle) 
    {
    }

    public HGPersistentHandle store(Object instance) 
    {       
        HGPersistentHandle [] layout = new HGPersistentHandle[1];
        //HGAtomType type = graph.getTypeSystem().getAtomType(CellGroup.class);
        NotebookDocument doc = (NotebookDocument)instance;
        layout[0] =  graph.getPersistentHandle(doc.bookH);
        return graph.getStore().store(layout);
    }
}
