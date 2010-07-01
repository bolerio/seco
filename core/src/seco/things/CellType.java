package seco.things;

import java.util.HashMap;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.atom.HGAtomRef;
import org.hypergraphdb.handle.UUIDHandleFactory;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGAtomTypeBase;

public class CellType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        UUIDHandleFactory.I.makeHandle("45eccdb7-9f4c-11dc-9199-0db27d8f317c");
    
	@SuppressWarnings("unchecked")
    public Object make(HGPersistentHandle valueHandle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) 
	{
		HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
		HGAtomType refType = graph.getTypeSystem().getAtomType(HGAtomRef.class);
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);
		Cell cell = new Cell((HGAtomRef)refType.make(layout[0], null, null));
		cell.attributes = (Map<Object, Object>)mapType.make(layout[1], null, null);
		return cell;
	}

	public void release(HGPersistentHandle handle) 
	{
		HGPersistentHandle [] layout = graph.getStore().getLink(handle);
		HGAtomType refType = graph.getTypeSystem().getAtomType(HGAtomRef.class);
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);
		refType.release(layout[0]);
		mapType.release(layout[1]);
	}

	public HGPersistentHandle store(Object instance) 
	{		
		HGPersistentHandle [] layout = new HGPersistentHandle[2];
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);
		HGAtomType refType = graph.getTypeSystem().getAtomType(HGAtomRef.class);
		Cell cell = (Cell)instance;
		layout[0] = refType.store(cell.ref);
		layout[1] = mapType.store(cell.attributes);
		return graph.getStore().store(layout);
	}
}