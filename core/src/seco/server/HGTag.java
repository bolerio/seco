package seco.server;


import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGSubgraph;

/**
 * <p>
 * A tag subgraph lets you associate a set of atoms with a given "tag" atom. The atoms
 * in the subgraphs are "tagged" with the "tag". The tag can be any other atom.
 * </p>
 * 
 * @author borislav
 *
 */
public class HGTag extends HGSubgraph
{
	private HGHandle tag;

	public HGHandle getTag()
	{
		return tag;
	}

	public void setTag(HGHandle tag)
	{
		this.tag = tag;
	}	
}