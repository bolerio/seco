package seco.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class GUpdate implements HGLink
{
	// What is the peer that made the update
	private HGHandle peerId;
	// An instance of BackedUpAtomList containing backups of all changed atoms
	private HGHandle backupList;
	// All previous taggings of root atoms
	private Map<HGHandle, Collection<HGHandle>> oldTags = new HashMap<HGHandle, Collection<HGHandle>>();
	// When was the update made
	private long timestamp = System.currentTimeMillis();
	// Arbitrary user comment
	private String comment;
	// The root atoms that were updated (added or replaced) in this update
	private ArrayList<HGHandle> targets = new ArrayList<HGHandle>();
	
	public GUpdate(HGHandle...targets)
	{
		for (HGHandle h : targets)
			this.targets.add(h);
	}
	
	public int getArity()
	{
		return targets.size();
	}

	public HGHandle getTargetAt(int i)
	{
		return targets.get(i);
	}

	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		targets.set(i, handle);
	}

	public void notifyTargetRemoved(int i)
	{
		targets.remove(i);
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public HGHandle getPeerId()
	{
		return peerId;
	}

	public void setPeerId(HGHandle peerId)
	{
		this.peerId = peerId;
	}
		
	public HGHandle getBackupList()
	{
		return backupList;
	}

	public void setBackupList(HGHandle backupList)
	{
		this.backupList = backupList;
	}
	
	public Map<HGHandle, Collection<HGHandle>> getOldTags()
	{
		return oldTags;
	}

	public void setOldTags(Map<HGHandle, Collection<HGHandle>> oldTags)
	{
		this.oldTags = oldTags;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public Json toJson()
	{
		return Json.object(
			"timestamp", timestamp,
			"peer", getPeerId().getPersistent().toString()
		);
	}
}