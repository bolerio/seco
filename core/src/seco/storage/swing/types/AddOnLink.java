package seco.storage.swing.types;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class AddOnLink extends HGPlainLink {

	public AddOnLink() {
	}

	public AddOnLink(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}
}
