package seco.talk;

import java.util.List;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;

import seco.ThisNiche;
import seco.U;

/**
 * 
 * <p>
 * Static top-level methods to manage/establish network connectivity of this
 * peer with a Seco network.
 * </p>
 * 
 * @author Borislav Iordanov
 * 
 */
public class ConnectionManager 
{
    
    static String peerConfigResource = "seco/talk/xmpp1.json";
    static String JSON_CONFIG;
    static
    {
        JSON_CONFIG = U.getResourceContentAsString(peerConfigResource);
        if (JSON_CONFIG == null)
            throw new RuntimeException("Unable to find default config "
                    + peerConfigResource);

    }

    public static void startConnections()
    {
        List<ConnectionContext> list = hg.getAll(ThisNiche.graph, hg
                .type(ConnectionContext.class));
        for (ConnectionContext ctx : list)
        {
            if(ctx.isActive())
                ctx.connect();
        }
    }
    
    public static void stopConnections(boolean persistently)
    {
        List<ConnectionContext> list = hg.getAll(ThisNiche.graph, hg
                .type(ConnectionContext.class));
        for (ConnectionContext ctx : list)
        {
            if(ctx.isActive())
                ctx.disconnect(persistently);
        }
    }
    
    public static ConnectionContext getConnectionContext(HGPeerIdentity peerID)
    {
        if(peerID == null) return null;
        List<ConnectionContext> l = hg.getAll(ThisNiche.graph, hg.type(ConnectionContext.class));
        for(ConnectionContext cc: l)
            if(peerID.equals(cc.getPeer().getIdentity()))
                return cc;
        return null;
    }
    
    public static ConnectionContext getConnectionContext(ConnectionConfig config)
    {
       return hg.getOne(ThisNiche.graph, hg.and(
                hg.type(ConnectionContext.class), 
                hg.eq("config", config)));
    }

    //called from Network menu with JScheme script
    public static ConnectionPanel openConnectionPanel(ConnectionConfig config)
    {
        ConnectionContext ctx = getConnectionContext(config);
        if(ctx == null)
        {
            ctx = new ConnectionContext(config);
            ThisNiche.graph.add(ctx);
        }
        ConnectionPanel panel = ctx.openConnectionPanel();
        panel.connect();
        return panel;
    }

}
