package seco.talk;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.peer.HGPeerIdentity;

public class AtomProposedEvent
{
    private HGPeerIdentity from;
    private HGPersistentHandle atomHandle;
    
    public AtomProposedEvent(HGPeerIdentity from, HGPersistentHandle atomHandle)
    {
        this.from = from;
        this.atomHandle = atomHandle;
    }

    public HGPeerIdentity getFrom()
    {
        return from;
    }

    public void setFrom(HGPeerIdentity from)
    {
        this.from = from;
    }

    public HGPersistentHandle getAtomHandle()
    {
        return atomHandle;
    }

    public void setAtomHandle(HGPersistentHandle atomHandle)
    {
        this.atomHandle = atomHandle;
    }    
}
