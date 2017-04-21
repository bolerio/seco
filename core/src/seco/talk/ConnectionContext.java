package seco.talk;

import java.awt.Rectangle;
import java.awt.Dialog.ModalityType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfig;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.util.HGUtils;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;


import seco.ThisNiche;
import seco.U;
import seco.gui.GUIHelper;
import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.gui.visual.VisualAttribs;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.util.GUIUtil;
import seco.util.task.CallableCallback;

/**
 * <p>
 * Represents one connection to a HGDB P2P network. Holds the 
 * {@link HyperGraphPeer} instance, current chats, configuration etc.
 * </p>
 * 
 * @author borislav
 *
 */
public class ConnectionContext
{
    private String networkName = "Seco Network";

    // TODO: shouldn't be hardcoded
    static final String OPENFIRE_HOST = "kobrix.syspark.net";

    private ConnectionConfig config;
    private boolean active;

    private boolean inProgress;
    private HyperGraphPeer thisPeer;
    private Set<ConnectionContextListener> listeners = new HashSet<ConnectionContextListener>();

    Map<String, TalkActivity> talks = Collections.synchronizedMap(new HashMap<String, TalkActivity>());

    public ConnectionContext()
    {
    }

    public ConnectionContext(ConnectionConfig config)
    {
        this.config = config;
    }

    public String getDisplayName(String networkId)
    {        
        return stripJID(networkId);
    }
    
    public String getMe()
    {
        return config.getUsername();
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
        if (thisPeer != null) return thisPeer;
        return create_new_peer();
    }

    public String getPeerName(HGPeerIdentity id)
    {
        if (id.equals(this.getPeer().getIdentity()))
            return config.getUsername();
        String netid = (String)getPeer().getNetworkTarget(id);
        if (netid != null)
            netid = stripJID(netid);
        return netid;
    }
    
    // check if the current peer's config is the same
    // before trying to connect it
    private HyperGraphPeer getUpdatedPeer()
    {
        if (thisPeer == null) 
            return create_new_peer();
        return (same_config(thisPeer.getConfiguration())) 
                ? thisPeer
                : create_new_peer();
    }

	private boolean same_config(Json peerConfig)
    {
    	if (!peerConfig.is("peerName", config.getUsername()))
    		return false;
        peerConfig = peerConfig.at(PeerConfig.INTERFACE_CONFIG);
        return peerConfig.is("anonymous", config.isAnonymousLogin()) &&
        	   peerConfig.is("autoRegister", config.isAutoRegister()) &&
               peerConfig.is("user", config.getUsername()) &&
               peerConfig.is("password", config.getPassword()) &&
               peerConfig.is("serverUrl", config.getHostname()) &
               peerConfig.is("port", config.getPort());
    }

	private HyperGraphPeer create_new_peer()
    {
        Json peerConfig = Json.read(ConnectionManager.JSON_CONFIG);
        peerConfig.set("localDB", ThisNiche.graph.getLocation());
        peerConfig.set("peerName", config.getUsername());
        Json xmppConfig = peerConfig.at(PeerConfig.INTERFACE_CONFIG);
        xmppConfig.set("anonymous", config.isAnonymousLogin());
        xmppConfig.set("autoRegister", config.isAutoRegister());
        xmppConfig.set("user", config.getUsername());
        xmppConfig.set("password", config.getPassword());
        xmppConfig.set("serverUrl", config.getHostname());
        xmppConfig.set("port", config.getPort());
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

        try
        {
            U.run(new CallableCallback<Boolean>() {
                public Boolean call() throws Exception
                {
                    HyperGraphPeer peer = getUpdatedPeer();
                    Future<Boolean> f = peer.start();
                    Boolean b = f.get();
                    if (peer.getStartupFailedException() != null)
                        throw peer.getStartupFailedException();
                    return b;
                }

                public void onCompletion(Boolean result, Throwable t)
                {
                    if (t == null && result)
                    {
                        // JOptionPane.showMessageDialog(ConnectionPanel.this,
                        // "Successfully connected to network.");
                        fireConnected();
                        XMPPPeerInterface i = (XMPPPeerInterface) getPeer()
                                .getPeerInterface();
                        i.getConnection().addConnectionListener(new MyConnectionListener());
                    }
                    else
                    {
//                        if (t != null) t.printStackTrace(System.err);
                        JOptionPane.showMessageDialog(GUIUtil.getFrame(),
                                HGUtils.getRootCause(t),
                                "Failed to connect to network, see error console.",
                                JOptionPane.ERROR_MESSAGE);
                        t.printStackTrace(System.err);
                        fireDisconnected();
                        thisPeer = null;
                    }
                }
            });//.get();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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
                    // should be called by MyConnectionListener fireDisconnected();
                    ConnectionContext.this.talks.clear();
                }
                else
                {
                    if (t != null) t.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(
                            GUIUtil.getFrame(),
                            t,
                            "Failed to disconnected from network, see error console.",
                            JOptionPane.ERROR_MESSAGE);
                    // TODO: maybe we should fire new event
                    fireConnected();
                }
            }
        });
    }

    public ConnectionPanel getConnectionPanel()
    {
        return hg.getOne(
                ThisNiche.graph,
                hg.and(hg.type(ConnectionPanel.class),
                        hg.eq("connectionContext", this)));
    }

    public ConnectionPanel openConnectionPanel()
    {
        ConnectionPanel panel = getConnectionPanel();
        HGHandle panelHandle = null;
        if (panel == null)
        {
            panel = new ConnectionPanel(/*getPeer().getIdentity()*/);
            panel.setConnectionContext(this);
            panel.initComponents();
            panelHandle = ThisNiche.graph.add(panel);
        }
        else
        {
            panelHandle = ThisNiche.handleOf(panel);
            panel.updateState();
        }

        addConnectionListener(panel);
        
        if (ThisNiche.getCanvas() == null)
        {
        	JDialog dlg = new JDialog();
        	dlg.setModalityType(ModalityType.MODELESS);
        	dlg.setSize(200, 200);
        	dlg.setLocation(300,  300);
        	dlg.add(panel);
        	dlg.setVisible(true);
        	return panel;
        }
        
        // Find an existing cell with that panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelHandle);
        if (existingH != null)
        {
            PSwingNode n = ThisNiche.getCanvas().getPSwingNodeForHandle(
                    existingH);
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

    public TalkPanel getTalkPanel(String friendId)
    {
        if (talks.containsKey(friendId)) 
            return talks.get(friendId).getPanel();
        // hg.findOne(arg0, arg1)(cond)
        HGHandle panelH = hg.findOne(
                ThisNiche.graph,
                hg.and(hg.type(TalkPanel.class), 
                       hg.eq("friendId", friendId),
                       hg.eq("connectionContext", this)));
        if (panelH == null) return null;
        TalkPanel panel = ThisNiche.graph.get(panelH);
        if (panel != null) 
            panel.initTalkActivity();
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
            PSwingNode n = ThisNiche.getCanvas().getPSwingNodeForHandle(
                    existingH);
            if (n != null) n.blink();
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
        CellUtils.setName(cgm, stripJID(panel.getFriendId()));
        cgm.setAttribute(VisualAttribs.showTitle, true);
        return;
    }

    synchronized TalkPanel openTalkPanel(String friendId)
    {
        TalkPanel panel = getTalkPanel(friendId);
        if (panel == null)
        {
            panel = new TalkPanel();
            panel.setFriendId(friendId);
            panel.setConnectionContext(this);
            ThisNiche.graph.add(panel);
            panel.initComponents();
            panel.initTalkActivity();
            ThisNiche.graph.update(panel);
        }

        openTalkPanel(panel);
        return panel;

    }

    public void openChatRoom(HostedRoom room)
    {
        RoomPanel panel = hg.getOne(ThisNiche.graph, hg.and(
                hg.type(RoomPanel.class), hg.eq("roomId", room.getJid()),
                hg.eq("connectionContext", this)));
        HGHandle panelH;
        if (panel == null)
        {
            panel = new RoomPanel(/*thisPeer.getIdentity()*/);
            panel.setRoomId(room.getJid());
            panel.setConnectionContext(this);
            panel.initComponents();
            panelH = ThisNiche.graph.add(panel);
            addConnectionListener(panel);
        }
        else
        {
            panelH = ThisNiche.handleOf(panel);
        }

        panel.joinRoom();

        if (ThisNiche.getCanvas() == null)
        {
        	JDialog dlg = new JDialog();
        	dlg.setModalityType(ModalityType.MODELESS);
        	dlg.setSize(400, 400);
        	dlg.setLocation(300,  300);
        	dlg.add(panel);
        	dlg.setVisible(true);
        	return;
        }
        
        // Find an existing cell with this panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelH);
        if (existingH != null)
        {
            PSwingNode n = ThisNiche.getCanvas().getPSwingNodeForHandle(
                    existingH);
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
        CellUtils.setName(cgm, this.getConfig().getUsername() + " in room " + room.getName());
        cgm.setAttribute(VisualAttribs.showTitle, true);
        // panel.initSplitterLocations();
        ThisNiche.graph.update(panel);
    }

    HGPeerIdentity getPeerIdentity(String jid)
    {
        if (jid == null) return null;
        String occ_name = stripJID(jid);
        for (HGPeerIdentity i : getPeer().getConnectedPeers())
            if (getPeerName(i).equals(occ_name)) return i;
        return null;
    }

    HGPeerIdentity getPeerIdentity(OccupantEx x)
    {
        return getPeerIdentity(x.getJid());
    }

    boolean isInRoster(String netid)
    {
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        Roster roster = i.getConnection().getRoster();
        //return roster.getEntry(occ_name + "@" + OPENFIRE_HOST/*i.getServerName()*/) != null;
        netid = netid.split("/")[0];
        return roster.getEntry(netid) != null;
    }
    
    boolean isInRoster(OccupantEx x)
    {
        return isInRoster(x.getJid());//String occ_name = stripJID(x.getJid());
    }

    boolean isMe(OccupantEx x)
    {
        if (x.getJid() == null) return false;
        String occ_name = stripJID(x.getJid());
        String me = config.getUsername();
        return occ_name.equals(me);
    }

//    boolean isInRoster(HGPeerIdentity x)
//    {
//        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
//        Roster roster = i.getConnection().getRoster();
//        return roster.getEntry(getPeerName(x) + "@" + OPENFIRE_HOST/*i.getServerName()*/) != null;
//    }

    boolean isMe(String netid)
    {
        return getMyId().equals(netid);
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
            i.getConnection().getRoster()
                    .createEntry(short_name + "@" + i.getServerName(), nick, null);
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
    }

    void addRoster(OccupantEx x)
    {
        String occ_name = stripJID(x.getJid());
        if (occ_name == null) occ_name = x.getNick();
        addRoster(occ_name, x.getNick());
    }

//    void addRoster(HGPeerIdentity x)
//    {
//        addRoster(x.getName(), x.getName());
//    }

    void removeRoster(String short_name)
    {
        XMPPPeerInterface i = (XMPPPeerInterface) getPeer().getPeerInterface();
        try
        {
            RosterEntry entry = i.getConnection().getRoster()
                    .getEntry(short_name + "@" + OPENFIRE_HOST);//i.getServerName()OPENFIRE_HOST);
            if (entry != null)
                i.getConnection().getRoster().removeEntry(entry);
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
    }

    void removeRoster(OccupantEx x)
    {
        HGPeerIdentity id = getPeerIdentity(x);
        if (id != null) 
            removeTalkPanel(x.getJid());
        removeRoster(stripJID(x.getJid()));
    }

//    void removeRoster(HGPeerIdentity x)
//    {
//        removeTalkPanel(x);
//        removeRoster(getPeerName(x));
//    }

    private void removeTalkPanel(String friendId)
    {
        TalkPanel panel = getTalkPanel(friendId);
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

    void openTalkPanel(OccupantEx x)
    {
        if (isMe(x))
        {
            JOptionPane.showMessageDialog(GUIUtil.getFrame(),
                    "Can't talk to yourself");
            return;
        }

        if (thisPeer.getIdentity(x.getJid()) == null)
        {
            JOptionPane.showMessageDialog(GUIUtil.getFrame(), "Not a seco peer");
        }        
        else if (isInRoster(x))
        {
            openTalkPanel(x.getJid());
        }        
        else //if (!inRoster)
        {
            String message = "User "
                    + x.getNick()
                    + " is not currently in your friend list, would you like to add them?";
            int res = JOptionPane.showConfirmDialog(GUIUtil.getFrame(),
                    message, "?", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) addRoster(x);
        }
    }

    static String stripJID(String name)
    {
        if (name == null) return null;
        int ind = name.indexOf("@");
        if (ind > -1) name = name.substring(0, ind);
        return name;
    }

    public String getMyId()
    {
        return config.getUsername() + "@" + config.getHostname() + ":" + config.getPort() +
                "/" + getPeer().getIdentity().getId();
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
            // fireJobStarted(true);
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
    
    public String toString()
    {
        if (this.config == null)
            return "null";
        else
            return config.getUsername() + "@" + config.getHostname() + ":" + config.getPort(); 
    }
}