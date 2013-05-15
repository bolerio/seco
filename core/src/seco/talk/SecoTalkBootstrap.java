package seco.talk;

import mjson.Json;
import org.hypergraphdb.peer.BootstrapPeer;
import org.hypergraphdb.peer.HyperGraphPeer;

import seco.server.PrimitiveGraphIndexActivity;
import seco.server.QueryPrimitiveGraphsActivity;

public class SecoTalkBootstrap implements BootstrapPeer
{
    public void bootstrap(HyperGraphPeer peer, Json config)
    {
        peer.getActivityManager().registerActivityType(TalkActivity.TYPENAME, TalkActivity.class);        
        peer.getActivityManager().registerActivityType(QueryPrimitiveGraphsActivity.TYPENAME, QueryPrimitiveGraphsActivity.class);
        peer.getActivityManager().registerActivityType(PrimitiveGraphIndexActivity.TYPENAME, PrimitiveGraphIndexActivity.class);
    }
}