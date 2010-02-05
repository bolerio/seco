package seco.notebook.storage.swing.types;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.type.Slot;

public class FactoryConstructorLink extends ConstructorLink
{
	public FactoryConstructorLink(HGHandle[] link)
	{
		super(link);
	}
	
	public Class<?> getDeclaringClass(HyperGraph hg)
	{
		//return hg.getTypeSystem().getClassForType(getTargetAt(0));
		return (Class<?>) hg.get(getTargetAt(0));
	} 
	
//	public void setDeclaringClass(Class<?>)
//	{
//		
//	}
	
	public String getMethodName(HyperGraph hg)
	{
		return (String) hg.get(getTargetAt(1));
	} 
	
	public Class<?> getTypeAt(HyperGraph hg, int index)
	{
		HGLink l = (HGLink) hg.get(getTargetAt(index+2));
        return (Class<?>) hg.get(l.getTargetAt(0));//hg.getTypeSystem().getClassForType(l.getTargetAt(0));
	}
	
	public Slot getSlotAt(HyperGraph hg, int index)
	{
		HGLink l = (HGLink) hg.get(getTargetAt(index+2));
        return (Slot) hg.get(l.getTargetAt(1));
	}
}
