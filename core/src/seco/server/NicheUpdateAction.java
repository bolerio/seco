package seco.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.storage.StorageGraph;
import org.hypergraphdb.util.HGUtils;

public class NicheUpdateAction
{
	private HyperGraph graph;
	private StorageGraph sgraph;
	private Set<HGPersistentHandle> oldAtoms = new HashSet<HGPersistentHandle>();
	private Set<HGHandle> updated = new HashSet<HGHandle>();
	
	public NicheUpdateAction(HyperGraph graph, StorageGraph sgraph)
	{
		this.graph = graph;
		this.sgraph = sgraph;
	}

	private void storeValue(HGPersistentHandle hv)
	{
		if (graph.getStore().containsData(hv))
			return;
		byte [] data = sgraph.getData(hv);
		if (data != null)
		{
			graph.getStore().store(hv, data);
			return;
		}
		HGPersistentHandle [] layout = sgraph.getLink(hv);
		if (layout == null)
			throw new IllegalArgumentException("Value key " + hv + " not found neither locally nor in the update storage graph.");
		graph.getStore().store(hv, layout);
		for (HGPersistentHandle h : layout)
			storeValue(h);
	}
	
	/**
	 * Update an atom with a new layout. The existing layout is saved into a new
	 * atom whose handle is returned. If there's no existing layout, then null is returned.
	 */
	private void updateAtom(HGPersistentHandle atom)
	{
		HGPersistentHandle [] existingLayout = graph.getStore().getLink(atom.getPersistent());
		HGPersistentHandle [] newLayout = sgraph.getLink(atom);
		if (newLayout.length < 2)
			throw new IllegalArgumentException("Atom expected to have a layout of length >= 2 : " + atom);
		graph.getStore().store(atom, newLayout);
		updated.add(atom);
		storeValue(newLayout[1]);
		// What about the type? We assume we have it already, or that we don't care about it.
		//..
		// Store targets of a link
		for (int i = 2; i < newLayout.length; i++)
			if (!updated.contains(newLayout[i]))
				updateAtom(newLayout[i]);
		if (existingLayout == null)
			return;
		HGPersistentHandle [] savedLayout = new HGPersistentHandle[existingLayout.length + 1];
		savedLayout[0] = atom;
		System.arraycopy(existingLayout, 0, savedLayout, 1, existingLayout.length);
		oldAtoms.add(graph.getStore().store(savedLayout));
		
	}
	
	public GUpdate makeUpdate()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<GUpdate>() 
		{
			public GUpdate call() {
				GUpdate gupdate = null;
				for (HGPersistentHandle root : sgraph.getRoots())
				{
					updateAtom(root);
				}
				gupdate = new GUpdate(sgraph.getRoots().toArray(HGUtils.EMPTY_HANDLE_ARRAY));
				BackedUpAtomList backupList = new BackedUpAtomList(oldAtoms.toArray(HGUtils.EMPTY_HANDLE_ARRAY));
				HGHandle backupHandle = graph.add(backupList);
				gupdate.setBackupList(backupHandle);
				return gupdate;
		}});
	}
}