package seco.things;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.atom.HGRelType;
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
		HGAtomType stringType = graph.getTypeSystem().getAtomType(String.class);
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);	
		HGPersistentHandle [] layout = graph.getStore().getLink(valueHandle);
		String name = (String)stringType.make(layout[0], null, null);
		Map<Object, Object> attributes = (Map<Object, Object>)mapType.make(layout[1], null, null);
		CellGroup group = new CellGroup(name);
		group.attributes = Collections.synchronizedMap(attributes);
		if (layout.length > 2)
        {
		    for (int i = 2; i < layout.length; i++)
                group.setTargetAt(i - 2, layout[i]);
        }
		return group;
	}

	public void release(HGPersistentHandle handle) 
	{
		HGAtomType stringType = graph.getTypeSystem().getAtomType(String.class);
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);
		
		HGPersistentHandle [] layout = graph.getStore().getLink(handle);
		stringType.release(layout[0]);
		mapType.release(layout[1]);
	}

	public HGPersistentHandle store(Object instance) 
	{
		HGAtomType stringType = graph.getTypeSystem().getAtomType(String.class);
		HGAtomType mapType = graph.getTypeSystem().getAtomType(HashMap.class);		
		CellGroup group = (CellGroup)instance;
		HGPersistentHandle [] layout = new HGPersistentHandle[group.getArity() + 2];
        layout[0] = stringType.store(group.getName());
		layout[1] = mapType.store(group.attributes);
		for (int i = 2; i < layout.length; i++)
		    layout[i] = graph.getPersistentHandle(
		            group.getTargetAt(i - 2));
		//System.out.println("store: " + group.getName() + ":" + layout.length);
        
		return graph.getStore().store(layout);
	}
	
	
}