package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class DefaultVisual  extends HGPlainLink
{
	public DefaultVisual(HGHandle...targets)
	{
		super(targets);
	}
	
    public DefaultVisual(HGHandle atomType, HGHandle visual)
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