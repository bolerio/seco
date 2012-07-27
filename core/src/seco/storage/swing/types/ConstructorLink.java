package seco.storage.swing.types;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.type.Slot;

import seco.ThisNiche;

public class ConstructorLink extends HGPlainLink
{

	public ConstructorLink(HGHandle [] link)
	{
		super(link);		
		if (link.length < 1)
			throw new IllegalArgumentException("The HGHandle [] passed to the ConstructorLink constructor must be at least of length 1.");
	}
	
	public Class<?> getTypeAt(HyperGraph hg, int index)
	{
		HGLink l = (HGLink) hg.get(getTargetAt(index));
        Class<?> cl = (Class<?>) hg.get(l.getTargetAt(0));
        //hg.getTypeSystem().getClassForType(l.getTargetAt(0));
        if (cl == null)
        {
        	throw new RuntimeException("noclassfor h " + l.getTargetAt(0));
        }
        return cl;
	}
	
	public Slot getSlotAt(HyperGraph hg, int index)
	{
		HGLink l = (HGLink) hg.get(getTargetAt(index));
        return (Slot) hg.get(l.getTargetAt(1));
	}
	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append("ConstructorLink(");
		//result.append(getParent());
		result.append(",");
		result.append(getArity());
		result.append(")");
		return result.toString();
	}

}

