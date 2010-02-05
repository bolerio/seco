package seco.things;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGAtomTypeBase;

public class CellGroupType extends HGAtomTypeBase 
{
    public static final HGPersistentHandle HGHANDLE = 
        HGHandleFactory.makeHandle("18e788ff-9f4c-11dc-a936-9b6691b5bb38");
   
	public Object make(HGPersistentHandle valueHandle, 
					   LazyRef<HGHandle[]> targets, 
					   IncidenceSetRef incidenceSet) 
	{
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);	
		HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
		//System.out.println("CellGroupType-make: " + valueHandle);
		Map<Object, Object> attributes = (Map<Object, Object>)mapType.make(layout[0], null, null);
		CellGroup group = new CellGroup();
		group.attributes = Collections.synchronizedMap(attributes);
		if (layout.length > 1)
        {
		    for (int i = 1; i < layout.length; i++)
                group.setTargetAt(i - 1, layout[i]);
        }
		return group;
	}

	public void release(HGPersistentHandle handle) 
	{
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);
		HGPersistentHandle [] layout = graph.getStore().getLink(handle);
		mapType.release(layout[0]);
	}

	public HGPersistentHandle store(Object instance) 
	{
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);		
		CellGroup group = (CellGroup)instance;
		HGPersistentHandle [] layout = new HGPersistentHandle[group.getArity() + 1];
        layout[0] = mapType.store(group.attributes);
		for (int i = 1; i < layout.length; i++)
		    layout[i] = graph.getPersistentHandle(
		            group.getTargetAt(i - 1));
		//System.out.println("store: " + group.getName() + ":" + layout.length);
        
		return graph.getStore().store(layout);
	}
	
	
}