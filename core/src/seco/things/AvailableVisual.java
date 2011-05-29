package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

/**
 * <p>
 * Each atom type can have one or more possible visual representations. This
 * links associate an atom type (1st target) to an available component (2nd target)
 * that can visual represent that type.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
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