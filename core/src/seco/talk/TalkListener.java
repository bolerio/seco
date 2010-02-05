package seco.talk;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.peer.HGPeerIdentity;

public interface TalkListener
{
    void chatReceived(HGPeerIdentity friend, String text);
    void atomProposed(HGPeerIdentity friend, HGPersistentHandle handle);
}
