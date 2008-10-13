package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

/**
 * 
 * <p>
 * This class represents a parent-child relationship between a <code>CellGroup</code>
 * (the parent) and either a <code>Cell</code> or a <code>CellGroup</code> (the child).
 * The structure is strictly hierarchical: every Cell or CellGroup can have only one
 * parent. Content of cells can be shared between different cells, but cells themselves
 * are NOT shared. This is because the intended use of cells is as visual representations
 * for arbitrary atoms. And visually something can't be at two different locations at the same
 * time.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class CellContainment extends HGPlainLink
{
	public CellContainment(HGHandle [] targetSet)
	{
		super(targetSet);
	}
	
	public CellContainment(HGHandle parent, HGHandle child)
	{
		super(new HGHandle[] { parent, child });
	}
	
	public HGHandle getParent()
	{
		return getTargetAt(0);
	}
	
	public HGHandle getChild()
	{
		return getTargetAt(1);
	}
}