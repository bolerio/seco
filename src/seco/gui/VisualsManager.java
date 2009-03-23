package seco.gui;

import java.util.ArrayList;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.atom.HGSubsumes;
import org.hypergraphdb.query.AtomTypeCondition;

import seco.ThisNiche;
import seco.things.AvailableVisual;
import seco.things.DefaultVisual;

/**
 * <p>
 * A helper class to perform lookup, storage and caching in the niche HGDB of visuals 
 * associated with cells.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class VisualsManager
{
	public static HGHandle defaultVisualForType(HGHandle atomType)
	{
		DefaultALGenerator alGenerator = new DefaultALGenerator(ThisNiche.hg, 
																new AtomTypeCondition(HGSubsumes.class),											
												                null,
												                true,
												                false,
												                false);		
		HGBreadthFirstTraversal traversal = new HGBreadthFirstTraversal(atomType, alGenerator);		
		for (HGHandle current = atomType; current != null; current = traversal.hasNext() ? traversal.next().getSecond():null)
		{
		    //TODO:1
			DefaultVisual v = hg.getOne(ThisNiche.hg, 
						hg.and(hg.type(DefaultVisual.class), hg.link(current, hg.anyHandle())));
			if (v != null)
				return v.getVisual();
		}
		return null;
	}
	
	public static HGHandle defaultVisualForAtom(HGHandle atom)
	{
		return defaultVisualForType(ThisNiche.hg.getType(atom));
	}
	
	public static List<HGHandle> availableVisualsForType(HGHandle atomType)
	{
		ArrayList<HGHandle> A = new ArrayList<HGHandle>();
		DefaultALGenerator alGenerator = new DefaultALGenerator(ThisNiche.hg, 
																new AtomTypeCondition(HGSubsumes.class),											
												                null,
												                true,
												                false,
												                false);		
		HGBreadthFirstTraversal traversal = new HGBreadthFirstTraversal(atomType, alGenerator);		
		for (HGHandle current = atomType; current != null; current = traversal.hasNext() ? traversal.next().getSecond():null)
		{
		    //TODO:1
			List<AvailableVisual> L = hg.getAll(ThisNiche.hg, 
					hg.and(hg.type(AvailableVisual.class), hg.link(current, hg.anyHandle())));
			for (AvailableVisual v : L)
				A.add(v.getVisual());
		}
		return A;
	}
	
	public static List<HGHandle> availableVisualsForAtom(HGHandle atom)
	{
		return availableVisualsForType(ThisNiche.hg.getType(atom));
	}
}
