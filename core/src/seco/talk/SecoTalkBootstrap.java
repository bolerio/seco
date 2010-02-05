package seco.talk;

import java.util.Map;

import org.hypergraphdb.peer.BootstrapPeer;
import org.hypergraphdb.peer.HyperGraphPeer;

public class SecoTalkBootstrap implements BootstrapPeer
{
    public void bootstrap(HyperGraphPeer peer, Map<String, Object> config)
    {
        peer.getActivityManager().registerActivityType(TalkActivity.TYPENAME, TalkActivity.class);        
    }
}