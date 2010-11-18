package seco.storage.notebook;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.handle.UUIDHandleFactory;
import org.hypergraphdb.type.HGAtomTypeBase;

import seco.notebook.ScriptletDocument;

public class ScriptletDocumentType extends HGAtomTypeBase
{
    public static final HGPersistentHandle HGHANDLE = 
        UUIDHandleFactory.I.makeHandle("E1B1AA72-E648-11DD-8764-7DD055D89593");
    
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
    {
        HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
        ScriptletDocument doc = new ScriptletDocument(layout[0]); 
        return doc;
    }

    public void release(HGPersistentHandle handle) 
    {
    }

    public HGPersistentHandle store(Object instance) 
    {       
        HGPersistentHandle [] layout = new HGPersistentHandle[1];
        ScriptletDocument doc = (ScriptletDocument)instance;
        layout[0] =  graph.getPersistentHandle(doc.getBookHandle());
        return graph.getStore().store(layout);
    }
}
