package seco.server;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

/**
 * <p>
 * This is just a holder of backup atoms created to save old atom layouts during an update.
 * </p>
 * 
 * @author borislav
 *
 */
public class BackedUpAtomList extends HGPlainLink
{
	public BackedUpAtomList(HGHandle...targets)
	{
		super(targets);
	}
}