package seco.talk;

import static org.hypergraphdb.peer.Structs.getPart;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.query.OrderedLinkCondition;
import org.hypergraphdb.query.impl.DefaultKeyBasedQuery;
import org.hypergraphdb.query.impl.PipeQuery;
import org.hypergraphdb.util.ValueSetter;

import seco.ThisNiche;
import seco.U;
import seco.gui.GUIHelper;
import seco.gui.PiccoloCanvas;
import seco.gui.TopFrame;
import seco.gui.VisualAttribs;
import seco.notebook.html.HTMLToolBar;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

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
public class ConnectionManager //implements ConnectionContext.ConnectionListener
{
    //public static HGPersistentHandle CONNECTIONS_MAP = HGHandleFactory
    //        .makeHandle("0604c720-d52b-11de-8a39-0800200c9a66");

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
        List<ConnectionContext> list = hg.getAll(ThisNiche.hg, hg
                .type(ConnectionContext.class));
        for (ConnectionContext ctx : list)
        {
            if(ctx.isActive())
                ctx.connect();
        }
    }
    
    public static ConnectionContext getConnectionContext(HGPeerIdentity peerID)
    {
        if(peerID == null) return null;
        List<ConnectionContext> l = hg.getAll(ThisNiche.hg, hg.type(ConnectionContext.class));
        for(ConnectionContext cc: l)
            if(peerID.equals(cc.getPeer().getIdentity()))
                return cc;
        return null;
    }
    
    public static ConnectionContext getConnectionContext(ConnectionConfig config)
    {
       return hg.getOne(ThisNiche.hg, hg.and(
                hg.type(ConnectionContext.class), 
                hg.eq("config", config)));
    }

    //called from Network menu JScheme script
    public static ConnectionPanel openConnectionPanel(ConnectionConfig config)
    {
        ConnectionContext ctx = getConnectionContext(config);
        if(ctx == null)
        {
            ctx = new ConnectionContext(config);
            ThisNiche.hg.add(ctx);
        }
        return ctx.openConnectionPanel();
    }

    
//    public static Map<ConnectionConfig, Boolean> getConnectionsMap()
//    {
//        Map<ConnectionConfig, Boolean> map = (Map<ConnectionConfig, Boolean>) ThisNiche.hg
//                .get(CONNECTIONS_MAP);
//        if (map != null) return map;
//       
//        map = new HashMap<ConnectionConfig, Boolean>();
//        List<ConnectionConfig> list = hg.getAll(ThisNiche.hg, hg
//                .type(ConnectionConfig.class));
//        for (ConnectionConfig cc : list)
//            map.put(cc, false);
//        ThisNiche.hg.define(CONNECTIONS_MAP, map);
//        return map;
//    }
    
    // static Map<ConnectionConfig, ConnectionContext> 
    // config_to_ctx = new HashMap<ConnectionConfig, ConnectionContext>();

//     private void init()
//     {
//         for(ConnectionConfig cc: getConnectionsMap().keySet())
//         {
//             if(getConnectionsMap().get(cc))
//             {
//                 ConnectionContext ctx = new ConnectionContext(cc);
//                 config_to_ctx.put(cc, ctx);
//                 ctx.addConnectionListener(this);
//                 ctx.connect();
//             }
//         }
//     }
         
//  public void connected(ConnectionContext ctx)
//  {
//      getConnectionsMap().put(ctx.getConfig(), true);
//      ThisNiche.hg.update(getConnectionsMap());
//  }
//  
//  public void disconnected(ConnectionContext ctx)
//  {
//      getConnectionsMap().put(ctx.getConfig(), false);
//      ThisNiche.hg.update(getConnectionsMap());
//  }
//  
    //static Map<ConnectionConfig, ConnectionContext> getConnectionCtxMap()
    //{
       // if (config_to_ctx == null)
       // {
           // List<ConnectionConfig> list = hg.getAll(ThisNiche.hg, hg
          //          .type(ConnectionConfig.class));
           // config_to_ctx = new HashMap<ConnectionConfig, ConnectionContext>();
           // for (ConnectionConfig cc : list)
           // {
          //      config_to_ctx.put(cc, new ConnectionContext(cc));
          //  }
       // }
       // return config_to_ctx;
    //}    
    
//    public static ConnectionPanel openConnectionPanel(ConnectionConfig config)
//    {
//        //
//        // First, we need to find if there's a ConnectionPanel for that
//        // configuration
//        // already open.
//        //
//
//        HGHandle configHandle = ThisNiche.hg.getHandle(config);
//
//        if (configHandle == null) configHandle = ThisNiche.hg.add(config);
//
//        // Hmm, it's almost easier to rewrite the following to directly use
//        // result sets.
//        // The query finds the 1 (expected) panel that is linked with the
//        // configuration
//        // by a plain link. The PipeQuery used to perform this says "all atoms
//        // of type
//        // ConnectionPanel that are form an ordered link with the configuration
//        // atom".
//        // 
//        HGQuery<HGHandle> inquery = HGQuery.make(ThisNiche.hg, hg
//                .type(ConnectionPanel.class));
//        final OrderedLinkCondition cond = hg.orderedLink(hg.anyHandle(),
//                configHandle);
//        HGQuery<HGHandle> query = new PipeQuery<HGHandle, HGHandle>(inquery,
//                new DefaultKeyBasedQuery<HGHandle, HGHandle>(ThisNiche.hg, hg
//                        .apply(hg.targetAt(ThisNiche.hg, 0), cond),
//                        new ValueSetter<HGHandle>() {
//                            public void set(HGHandle h)
//                            {
//                                cond.getTargets()[0] = h;
//                            }
//                        }));
//        query.setHyperGraph(ThisNiche.hg);
//        List<HGHandle> L = hg.findAll(query);
//
//        // for(HGHandle h: L)
//        // ThisNiche.hg.remove(h);
//        // L.clear();
//
//        HGHandle panelHandle = null;
//        if (L.isEmpty())
//        {
//            ConnectionPanel panel = new ConnectionPanel();
//            panel.initComponents();
//
//            panelHandle = ThisNiche.hg.add(panel);
//            ThisNiche.hg.add(new HGPlainLink(panelHandle, configHandle));
//        }
//        else if (L.size() == 1)
//        {
//            panelHandle = L.get(0);
//        }
//        else
//            throw new RuntimeException(
//                    "More than 1 ConnectionPanel associated with configuration "
//                            + configHandle);
//
//        // Find an existing cell with that panel:
//        // hmm, not sure what needs to be done here....
//        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
//        int width = 200;
//        int height = 200;
//        int x = Math.max(0, canvas.getWidth() - width - width / 5);
//        int y = height;
//        CellGroupMember cell = ThisNiche.hg.get(GUIHelper.addIfNotThere(
//                ThisNiche.TOP_CELL_GROUP_HANDLE, panelHandle, null, null,
//                new Rectangle(x, y, width, height)));
//        CellUtils.setName(cell, "Seco Network");
//        cell.setAttribute(VisualAttribs.showTitle, true);
//        ConnectionPanel panel = ThisNiche.hg.get(panelHandle);
//        panel.connect();
//        return panel;
//    }

//    private static Map<HGPeerIdentity, ConnectionPanel> peer_panel_map = new HashMap<HGPeerIdentity, ConnectionPanel>();

//    static ConnectionPanel getConnectionPanel(HGPeerIdentity id)
//    {
//        // HGQuery<HGHandle> inquery = HGQuery.make(ThisNiche.hg,
//        // hg.type(ConnectionPanel.class));
//        // final OrderedLinkCondition cond = hg.orderedLink(hg.anyHandle(),
//        // configHandle);
//        // HGQuery<HGHandle> query = new PipeQuery<HGHandle, HGHandle>
//        // (inquery,
//        // new DefaultKeyBasedQuery<HGHandle, HGHandle>(ThisNiche.hg,
//        // hg.apply(hg.targetAt(ThisNiche.hg, 0), cond),
//        // new ValueSetter<HGHandle>()
//        // {
//        // public void set(HGHandle h)
//        // {
//        // cond.getTargets()[0] = h;
//        // }
//        // }
//        // ));
//        // query.setHyperGraph(ThisNiche.hg);
//        // List<HGHandle> L = hg.findAll(query);
//        // return L.isEmpty() ? null: (ConnectionPanel)
//        // ThisNiche.hg.get(L.get(0));
//        return peer_panel_map.get(id);
//
//    }

//    static void registerConnectionPanel(HGPeerIdentity id, ConnectionPanel pan)
//    {
//        peer_panel_map.put(id, pan);
//    }
//
//    static void unregisterConnectionPanel(HGPeerIdentity id)
//    {
//        peer_panel_map.remove(id);
//    }

    // TalkPanel getTalkPanel(HGPeerIdentity id, HGPeerIdentity friend, boolean
    // create)
    // {
    //       
    // TalkPanel talkPanel = hg.getOne(ThisNiche.hg, hg.and(
    // hg.type(TalkPanel.class), hg.eq("friend", friend)));
    // if (talkPanel == null)
    // {
    // talkPanel = new TalkPanel(this);
    // talkPanel.setFriend(friend);
    // ThisNiche.hg.add(talkPanel);
    // }
    //        
    // if (talkPanel == null)
    // {
    // }
    // talkPanel.setTalkActivity(this);
    // Map<Object, Object> attribs = new HashMap<Object, Object>();
    // // TODO: some sort of naming
    // String title = (friend.getName() != null) ? friend.getName() :
    // "Connection Panel";
    // attribs.put(VisualAttribs.name, title);
    // attribs.put(VisualAttribs.showTitle, true);
    // GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE, ThisNiche.hg
    // .getHandle(talkPanel), null, null, new Rectangle(500, 200, 300,
    // 300), attribs);
    // }

}
