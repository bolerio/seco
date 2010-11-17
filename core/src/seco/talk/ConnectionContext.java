package seco.talk;

import static org.hypergraphdb.peer.Structs.getPart;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.util.HGUtils;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.Occupant;

import seco.ThisNiche;
import seco.U;
import seco.api.CallableCallback;
import seco.gui.GUIHelper;
import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.gui.TopFrame;
import seco.gui.visual.VisualAttribs;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class ConnectionContext
{
    //TODO: shouldn't be hardcoded
    private String networkName = "Seco Network";
    
    static final String OPENFIRE_HOST = "kobrix.syspark.net";

    private ConnectionConfig config;
    private boolean active;

    private boolean inProgress;
    private HyperGraphPeer thisPeer;
    private Set<ConnectionContextListener> listeners = new HashSet<ConnectionContextListener>();

    Map<HGPeerIdentity, TalkActivity> talks = Collections
            .synchronizedMap(new HashMap<HGPeerIdentity, TalkActivity>());

    public ConnectionContext()
    {
    }

    public ConnectionContext(ConnectionConfig config)
    {
        this.config = config;
    }

    public void addConnectionListener(ConnectionContextListener l)
    {
        listeners.add(l);
    }

    public void removeConnectionListener(ConnectionContextListener l)
    {
        listeners.remove(l);
    }

    private void fireConnected()
    {
        for (ConnectionContextListener l : listeners)
            l.connected(this);
    }

    private void fireDisconnected()
    {
        for (ConnectionContextListener l : listeners)
            l.disconnected(this);
    }

    private void fireJobStarted(boolean connect_or_disconnect)
    {
        for (ConnectionContextListener l : listeners)
            l.workStarted(this, connect_or_disconnect);
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public HyperGraphPeer getPeer()
    {
        if (thisPeer != null) 
            return thisPeer;
        return create_new_peer();
    }

    //check if the current peer's config is the same
    //before trying to connect it
    private HyperGraphPeer getUpdatedPeer()
    {
        if(thisPeer == null)
           return create_new_peer();
        return (same_config(thisPeer.getConfiguration())) ? 
                thisPeer :  create_new_peer();
    }
    
    private boolean same_config(Map<String, Object> peerConfig)
    {
        Object o = peerConfig.get("peerName"); 
        if(o == null || !o.equals(config.getUsername())) return false;
        o = peerConfig.get("anonymous");
        if(o == null || !o.equals(config.isAnonymousLogin())) return false;
        o = peerConfig.get("autoRegister");
        if(o == null || !o.equals(config.isAutoRegister())) return false;
        o = peerConfig.get("user");
        if(o == null || !o.equals(config.getUsername())) return false;
        o = peerConfig.get("password");
        if(o == null || !o.equals(config.getPassword())) return false;
        o = peerConfig.get("serverUrl");
        if(o == null || !o.equals(config.getHostname())) return false;
        o = peerConfig.get("port");
        if(o == null || !o.equals(config.getPort())) return false;
        return true;
    }
    
    private HyperGraphPeer create_new_peer()
    {
        JSONReader reader = new JSONReader();
        Map<String, Object> peerConfig = getPart(reader
                .read(ConnectionManager.JSON_CONFIG));
        peerConfig.put("localDB", ThisNiche.graph.getLocation());
        peerConfig.put("peerName", config.getUsername());
        Map<String, Object> xmppConfig = getPart(peerConfig, "interfaceConfig");
        xmppConfig.put("anonymous", config.isAnonymousLogin());
        xmppConfig.put("autoRegister", config.isAutoRegister());
        xmppConfig.put("user", config.getUsername());
        xmppConfig.put("password", config.getPassword());
        xmppConfig.put("serverUrl", config.getHostname());
        xmppConfig.put("port", config.getPort());
        thisPeer = new HyperGraphPeer(peerConfig, ThisNiche.graph);
        return thisPeer; 
    }

    public boolean isConnected()
    {
        return thisPeer != null && thisPeer.getPeerInterface() != null
                && thisPeer.getPeerInterface().isConnected();
    }

    public void connect()
    {
        if (isConnected()) return;
        fireJobStarted(true);
        active = true;
        inProgress = true;
        ThisNiche.graph.update(this);

        U.run(new CallableCallback<Boolean>() {
            public Boolean call() throws Exception
            {
                Future<Boolean> f = getUpdatedPeer().start(config.getUsername(),
                        config.getPassword());
                return f.get();
            }

            public void onCompletion(Boolean result, Throwable t)
            {
                if (t == null && result)
                {
                    // JOptionPane.showMessageDialog(ConnectionPanel.this,
                    // "Successfully connected to network.");
                    fireConnected();
                    XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
                    i.getConnection().addConnectionListener(new MyConnectionListener());
                }
                else
                {
                    if (t != null) t.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(ThisNiche.guiController.getFrame(),
                            HGUtils.getRootCause(t),
                            "Failed to connect to network, see error console.",
                            JOptionPane.ERROR_MESSAGE);
                    fireDisconnected();
                    thisPeer = null;
                }

            }
        });
    }

    // when persistently = true the connection is marked inactive in HG and
    // won't be
    // activated on the next startup
    public void disconnect(boolean persistently)
    {
        if (!isConnected()) return;
        fireJobStarted(false);
        inProgress = false;
        active = false;
        if (persistently) ThisNiche.graph.update(this);
        U.run(new CallableCallback<Boolean>() {
            public Boolean call() throws Exception
            {
                thisPeer.stop();
                return true;
            }

            public void onCompletion(Boolean result, Throwable t)
            {
                if (t == null && result)
                {
                    // JOptionPane.showMessageDialog(ConnectionPanel.this,
                    // "Successfully disconnect from network.");
                    fireDisconnected();
                    ConnectionContext.this.talks.clear();
                }
                else
                {
                    if (t != null) t.printStackTrace(System.err);
                    JOptionPane
                            .showMessageDialog(
                                    ThisNiche.guiController.getFrame(),
                                    t,
                                    "Failed to disconnected from network, see error console.",
                                    JOptionPane.ERROR_MESSAGE);
                    // TODO: maybe we should fire new event
                    fireConnected();
                }
            }
        });
    }

    private ConnectionPanel getConnectionPanel()
    {
        return hg.getOne(ThisNiche.graph, hg.and(
                hg.type(ConnectionPanel.class), hg.eq("peerID", getPeer()
                        .getIdentity())));
    }

    public ConnectionPanel openConnectionPanel()
    {
        ConnectionPanel panel = getConnectionPanel();
        HGHandle panelHandle = null;
        if (panel == null)
        {
            panel = new ConnectionPanel(getPeer().getIdentity());
            panel.initComponents();
            panelHandle = ThisNiche.graph.add(panel);
        }
        else
        {
            panelHandle = ThisNiche.handleOf(panel);
            panel.updateState();
        }

        addConnectionListener(panel);
        // Find an existing cell with that panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelHandle);
        if (existingH != null)
        {
            PSwingNode n = ThisNiche.getCanvas()
                    .getPSwingNodeForHandle(existingH);
            n.blink();
            return panel;
        }
        // Create new panel
        PiccoloCanvas canvas = ThisNiche.getCanvas();
        int width = 200;
        int height = 200;
        int x = Math.max(0, canvas.getWidth() - width - width / 5);
        int y = height;

        CellGroup top = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cell = ThisNiche.graph.get(GUIHelper.addToCellGroup(
                panelHandle, top, null, null,
                new Rectangle(x, y, width, height), true));
        CellUtils.setName(cell, networkName);
        cell.setAttribute(VisualAttribs.showTitle, true);

        return panel;
    }

    public TalkPanel getTalkPanel(HGPeerIdentity friend)
    {
        if (talks.containsKey(friend)) return talks.get(friend).getPanel();
        // hg.findOne(arg0, arg1)(cond)
        HGHandle panelH = hg.findOne(ThisNiche.graph, hg.and(hg
                .type(TalkPanel.class), hg.eq("friend", friend), hg.eq(
                "peerID", getPeer().getIdentity())));
        if (panelH == null) return null;
        TalkPanel panel = ThisNiche.graph.get(panelH);
        if (panel != null) panel.initTalkActivity(this);
        return panel;
    }

    static void clearTalkPanels()
    {
        List<HGHandle> l = hg
                .findAll(ThisNiche.graph, hg.type(TalkPanel.class));
        for (HGHandle p : l)
            ThisNiche.graph.remove(p);
    }

    void openTalkPanel(TalkPanel panel)
    {
        HGHandle panelH = ThisNiche.handleOf(panel);
        // Find an existing cell with that panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelH);
        if (existingH != null)
        {
            PSwingNode n = ThisNiche.getCanvas()
                    .getPSwingNodeForHandle(existingH);
            if(n != null) n.blink();
            return;
        }

        PiccoloCanvas canvas = ThisNiche.getCanvas();
        int width = 200;
        int height = 100;
        int x = Math.max(0, (canvas.getWidth() - width) / 2);
        int y = Math.max(0, (canvas.getHeight() - height) / 2);
        CellGroup top = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cgm = ThisNiche.graph.get(GUIHelper.addToCellGroup(
                panelH, top, null, null, new Rectangle(x, y, width, height),
                true));
        CellUtils.setName(cgm, panel.getFriend().getName());
        cgm.setAttribute(VisualAttribs.showTitle, true);
        return;
    }

    synchronized TalkPanel openTalkPanel(HGPeerIdentity friend)
    {
        TalkPanel panel = getTalkPanel(friend);
        if (panel == null)
        {
            panel = new TalkPanel(friend, getPeer().getIdentity());
            ThisNiche.graph.add(panel);
            panel.initComponents();
            panel.initTalkActivity(this);
            ThisNiche.graph.update(panel);
        }

        openTalkPanel(panel);
        return panel;

    }

    public void openChatRoom(HostedRoom room)
    {
        RoomPanel panel = hg.getOne(ThisNiche.graph, hg.and(hg
                .type(RoomPanel.class), hg.eq("roomId", room.getJid()), hg.eq(
                "peerID", thisPeer.getIdentity())));
        HGHandle panelH;
        if (panel == null)
        {
            panel = new RoomPanel(thisPeer.getIdentity());
            panel.setRoomId(room.getJid());
            panel.initComponents();
            panelH = ThisNiche.graph.add(panel);
            addConnectionListener(panel);
        }
        else
        {
            panelH = ThisNiche.handleOf(panel);
        }

        panel.joinRoom();

        // Find an existing cell with this panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelH);
        if (existingH != null)
        {
            PSwingNode n = ThisNiche.getCanvas().getPSwingNodeForHandle(existingH);
            n.blink();
            return;
        }

        PiccoloCanvas canvas = ThisNiche.getCanvas();
        int width = 400;
        int height = 500;
        int x = Math.max(0, (canvas.getWidth() - width) / 2);
        int y = Math.max(0, (canvas.getHeight() - height) / 2);
        CellGroup top = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cgm = ThisNiche.graph.get(GUIHelper.addToCellGroup(
                panelH, top, null, null, new Rectangle(x, y, width, height),
                true));
        CellUtils.setName(cgm, "Chat room " + room.getName());
        cgm.setAttribute(VisualAttribs.showTitle, true);
        // panel.initSplitterLocations();
        ThisNiche.graph.update(panel);
    }

    HGPeerIdentity getPeerIdentity(String jid)
    {
        String occ_name = stripJID(jid);
        for (HGPeerIdentity i : getPeer().getConnectedPeers())
            if (i.getName().equals(occ_name)) return i;
        return null;
    }
    
    HGPeerIdentity getPeerIdentity(Occupant x)
    {
        return getPeerIdentity(x.getJid());
    }
    
    boolean isInRoster(Occupant x)
    {
        String occ_name = stripJID(x.getJid());
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        Roster roster = i.getConnection().getRoster();
        return roster.getEntry(occ_name + "@" + OPENFIRE_HOST) != null;
    }

    boolean isMe(Occupant x)
    {
        String occ_name = stripJID(x.getJid());
        String me = stripJID(getPeer().getIdentity().getName());
        return occ_name.equals(me);
    }

    boolean isInRoster(HGPeerIdentity x)
    {
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        Roster roster = i.getConnection().getRoster();
        return roster.getEntry(x.getName() + "@" + OPENFIRE_HOST) != null;
    }

    boolean isMe(HGPeerIdentity x)
    {
        return x.equals(getPeer().getIdentity());
    }

    void addRoster(String short_name, String nick)
    {
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        try
        {
            i.getConnection().getRoster().createEntry(
                    short_name + "@" + OPENFIRE_HOST, nick, null);
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
    }

    void addRoster(Occupant x)
    {
        String occ_name = stripJID(x.getJid());
        addRoster(occ_name, x.getNick());
    }

    void addRoster(HGPeerIdentity x)
    {
        addRoster(x.getName(), x.getName());
    }

    void removeRoster(String short_name)
    {
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        try
        {
            RosterEntry entry = i.getConnection().getRoster().getEntry(
                    short_name + "@" + OPENFIRE_HOST);
            if (entry != null)
                i.getConnection().getRoster().removeEntry(entry);
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
    }

    void removeRoster(Occupant x)
    {
        HGPeerIdentity id = getPeerIdentity(x);
        if (id != null) removeTalkPanel(id);
        removeRoster(stripJID(x.getJid()));
    }

    void removeRoster(HGPeerIdentity x)
    {
        removeTalkPanel(x);
        removeRoster(x.getName());
    }

    private void removeTalkPanel(HGPeerIdentity id)
    {
        TalkPanel panel = getTalkPanel(id);
        if (panel != null)
        {
            // Find an existing cell with that panel:
            HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                    ThisNiche.TOP_CELL_GROUP_HANDLE, ThisNiche.handleOf(panel));
            if (existingH != null)
            {
                CellGroup top = ThisNiche.graph
                        .get(ThisNiche.TOP_CELL_GROUP_HANDLE);
                top.remove(top.indexOf(existingH));
            }
        }
    }

    void openTalkPanel(Occupant x)
    {
        if (isMe(x))
        {
            JOptionPane.showMessageDialog(ThisNiche.guiController.getFrame(),
                    "Can't talk to yourself");
            return;
        }

        HGPeerIdentity i = getPeerIdentity(x);
        if (i != null && isInRoster(i))
        {
            openTalkPanel(i);
            return;
        }

        String message = "User "
                + x.getNick()
                + " is not currently in your friend list, would you like to add them?";
        int res = JOptionPane.showConfirmDialog(ThisNiche.guiController.getFrame(),
                message, "?", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) addRoster(x);
    }

    static String stripJID(String name)
    {
        int ind = name.indexOf("@");
        if (ind > -1) name = name.substring(0, ind);
        return name;
    }

    public ConnectionConfig getConfig()
    {
        return config;
    }

    public boolean isInProgress()
    {
        return inProgress;
    }

    public void setConfig(ConnectionConfig config)
    {
        this.config = config;
    }
    
    public String getNetworkName()
    {
        return networkName;
    }
    
    private class MyConnectionListener implements ConnectionListener
    {

        public void connectionClosed()
        {
            fireDisconnected();
        }

        public void connectionClosedOnError(Exception ex)
        {
            fireDisconnected();
        }

        public void reconnectingIn(int arg0)
        {
            //fireJobStarted(true);
        }

        public void reconnectionFailed(Exception ex)
        {
            fireDisconnected();
        }

        public void reconnectionSuccessful()
        {
            fireConnected();
        }       
    }

    public static interface ConnectionContextListener
    {
        void connected(ConnectionContext ctx);

        void disconnected(ConnectionContext ctx);

        void workStarted(ConnectionContext ctx, boolean connect_or_disconnect);
    }
}
