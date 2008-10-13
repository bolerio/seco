package seco.notebook;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.safehaus.uuid.UUIDGenerator;

public class CellDocumentType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        HGHandleFactory.makeHandle("4963b01e-da54-11dc-9588-d977fb5fb8c4");
    
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
    {
        HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
        CellDocument doc = new CellDocument(layout[0]); 
        return doc;
    }

    public void release(HGPersistentHandle handle) 
    {
    }

    public HGPersistentHandle store(Object instance) 
    {       
        HGPersistentHandle [] layout = new HGPersistentHandle[1];
        CellDocument doc = (CellDocument)instance;
        layout[0] =  graph.getPersistentHandle(doc.bookH);
        return graph.getStore().store(layout);
    }
    
    public static void main(String args[]){
        System.out.println(UUIDGenerator.getInstance().generateTimeBasedUUID());
        System.out.println(UUIDGenerator.getInstance().generateTimeBasedUUID());
    }
}
