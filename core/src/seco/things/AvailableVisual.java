package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class AvailableVisual extends HGPlainLink
{
	public AvailableVisual(HGHandle...targets)
	{
		super(targets);
	}
    public AvailableVisual(HGHandle atomType, HGHandle visual)
    {
        super(atomType, visual);
    }
    
    public HGHandle getAtomType()
    {
        return getTargetAt(0);
    }
    
    public HGHandle getVisual()
    {
        return getTargetAt(1);
    }
}
