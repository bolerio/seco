package seco.notebook;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.handle.UUIDHandleFactory;
import org.hypergraphdb.type.HGAtomTypeBase;


public class OutputCellDocumentType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        UUIDHandleFactory.I.makeHandle("4963b01e-da54-11dc-9588-d977fb5fb8c4");
    
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
    {
        HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
        OutputCellDocument doc = new OutputCellDocument(layout[0]); 
        return doc;
    }

    public void release(HGPersistentHandle handle) 
    {
    }

    public HGPersistentHandle store(Object instance) 
    {       
        HGPersistentHandle [] layout = new HGPersistentHandle[1];
        OutputCellDocument doc = (OutputCellDocument)instance;
        layout[0] =  graph.getPersistentHandle(doc.bookH);
        return graph.getStore().store(layout);
    }
}
