package seco.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

public class Tagger
{	
	public static Collection<HGHandle> tag(HyperGraph graph, HGHandle atom, Collection<HGHandle> tags)
	{
		HashSet<HGHandle> alreadyTagged = new HashSet<HGHandle>();
		List<HGHandle> existingTags = hg.findAll(graph, hg.and(hg.type(HGTag.class), hg.contains(atom)));		
		// First remove current tags 
		for (HGHandle existing : existingTags)
		{
			if (tags.contains(existing))
				alreadyTagged.add(existing);
			else
			{
				HGTag thetag = graph.get(existing);
				thetag.remove(atom);
			}
		}
		for (HGHandle t : tags)
		{
			if (alreadyTagged.contains(t))
				continue;
			HGTag tagSubgraph = hg.getOne(graph, hg.and(hg.type(HGTag.class), hg.eq("tag", t)));
			if (tagSubgraph == null)
			{
				tagSubgraph = new HGTag();
				tagSubgraph.setTag(t);
				graph.add(tagSubgraph);
			}
			tagSubgraph.add(atom);
		}
		return existingTags;
	}
}