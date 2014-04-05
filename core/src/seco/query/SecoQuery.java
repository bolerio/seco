package seco.query;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.IndexCondition;
import seco.ThisNiche;
import seco.things.ByAttributeIndexer;
import seco.things.CellGroupMember;
import seco.things.WithAttributes;

/**
 * Factory methods for creating useful queries of Seco objects.
 * @author borislav
 *
 */
public class SecoQuery
{
    /**
     * <p>Query for {@link CellGroupMember} by their attribute.</p> 
     */
	public static IndexCondition<String, HGHandle> attr(String name, Object value)
	{
		HyperGraph graph = ThisNiche.graph;
		HGHandle valueType = graph.getTypeSystem().getTypeHandle(value);
		HGIndex<String, HGHandle> idx = graph.getIndexManager().getIndex(
				new ByAttributeIndexer<String, HGHandle>(
						graph.getTypeSystem().getTypeHandle(CellGroupMember.class), name, valueType));
		System.out.println("attr idx cnt " + idx.count());
		return new IndexCondition(idx, value);
	}
}