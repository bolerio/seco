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
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.util.HGUtils;
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
import seco.gui.VisualAttribs;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class ConnectionContext
{
    static final String OPENFIRE_HOST = "kobrix.syspark.net";
    
    private ConnectionConfig config;
    private boolean active;

    private boolean inProgress;
    private HyperGraphPeer thisPeer;
    private Set<ConnectionListener> listeners = new HashSet<ConnectionListener>();
    
    Map<HGPeerIdentity, TalkActivity> talks = Collections
            .synchronizedMap(new HashMap<HGPeerIdentity, TalkActivity>());

    public ConnectionContext()
    {
    }

    public ConnectionContext(ConnectionConfig config)
    {
        this.config = config;
    }

    public void addConnectionListener(ConnectionListener l)
    {
        listeners.add(l);
    }

    public void removeConnectionListener(ConnectionListener l)
    {
        listeners.remove(l);
    }

    private void fireConnected()
    {
        for (ConnectionListener l : listeners)
            l.connected(this);
    }

    private void fireDisconnected()
    {
        for (ConnectionListener l : listeners)
            l.disconnected(this);
    }
    
    private void fireJobStarted(boolean connect_or_disconnect)
    {
        for (ConnectionListener l : listeners)
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
        JSONReader reader = new JSONReader();
        Map<String, Object> peerConfig = getPart(reader
                .read(ConnectionManager.JSON_CONFIG));
        peerConfig.put("localDB", ThisNiche.hg.getLocation());
        peerConfig.put("peerName", config.getUsername());
        Map<String, Object> xmppConfig = getPart(peerConfig, "interfaceConfig");
        xmppConfig.put("anonymous", config.isAnonymousLogin());
        xmppConfig.put("autoRegister", config.isAutoRegister());
        xmppConfig.put("user", config.getUsername());
        xmppConfig.put("password", config.getPassword());
        xmppConfig.put("serverUrl", config.getHostname());
        xmppConfig.put("port", config.getPort());
        thisPeer = new HyperGraphPeer(peerConfig, ThisNiche.hg);
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
        ThisNiche.hg.update(this);
        
        U.run(new CallableCallback<Boolean>() {
            public Boolean call() throws Exception
            {
                Future<Boolean> f = getPeer().start(config.getUsername(),
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
                }
                else
                {
                    if (t != null) t.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(TopFrame.getInstance(),
                            HGUtils.getRootCause(t),
                            "Failed to connect to network, see error console.",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }

    public void disconnect()
    {
        if (!isConnected()) return;
        fireJobStarted(false);
        inProgress = false;
        active = false;
        ThisNiche.hg.update(this);
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
                                    TopFrame.getInstance(),
                                    t,
                                    "Failed to disconnected from network, see error console.",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private ConnectionPanel getConnectionPanel()
    {
        return hg.getOne(ThisNiche.hg, hg.and(hg.type(ConnectionPanel.class),
                hg.eq("peerID", getPeer().getIdentity())));
    }

    public ConnectionPanel openConnectionPanel()
    {
        ConnectionPanel panel = getConnectionPanel();
        HGHandle panelHandle = null;
        if (panel == null)
        {
            panel = new ConnectionPanel(getPeer().getIdentity());
            panel.initComponents();
            panelHandle = ThisNiche.hg.add(panel);
        }
        else
            panelHandle = ThisNiche.handleOf(panel);

        addConnectionListener(panel);
        // Find an existing cell with that panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelHandle);
        if (existingH != null)
        {
            PSwingNode n = TopFrame.getInstance().getCanvas()
                    .getPSwingNodeForHandle(existingH);
            n.blink();
            return panel;
        }
        // Create new panel
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        int width = 200;
        int height = 200;
        int x = Math.max(0, canvas.getWidth() - width - width / 5);
        int y = height;

        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cell = ThisNiche.hg.get(GUIHelper.addToCellGroup(
                panelHandle, top, null, null,
                new Rectangle(x, y, width, height), true));
        CellUtils.setName(cell, "Seco Network");
        cell.setAttribute(VisualAttribs.showTitle, true);

        return panel;
    }

    public TalkPanel getTalkPanel(HGPeerIdentity friend)
    {
        if(talks.containsKey(friend))
            return talks.get(friend).getPanel();
        //hg.findOne(arg0, arg1)(cond)
        HGHandle panelH = hg.findOne(ThisNiche.hg, hg.and(hg.type(TalkPanel.class),
          hg.eq("friend", friend), hg.eq("peerID", getPeer().getIdentity())));
        if(panelH == null) return null;
        TalkPanel panel = ThisNiche.hg.get(panelH);
        if(panel != null)
           panel.initTalkActivity(this);
        return panel;
    }
    
    static void clearTalkPanels()
    {
        List<HGHandle> l = hg.findAll(ThisNiche.hg, hg.type(TalkPanel.class));
        for(HGHandle p : l)
            ThisNiche.hg.remove(p);
    }
    
    void openTalkPanel(TalkPanel panel)
     {
         HGHandle panelH = ThisNiche.handleOf(panel);
         // Find an existing cell with that panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelH);
        if (existingH != null)
        {
            PSwingNode n = TopFrame.getInstance().getCanvas()
                    .getPSwingNodeForHandle(existingH);
            n.blink();
            return;
        } 

        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        int width = 200;
        int height = 100;
        int x = Math.max(0, (canvas.getWidth() - width) / 2);
        int y = Math.max(0, (canvas.getHeight() - height) / 2);
        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cgm = ThisNiche.hg.get(GUIHelper.addToCellGroup(panelH,
                top, null, null, new Rectangle(x, y, width, height), true));
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
           ThisNiche.hg.add(panel);
           panel.initComponents();
           panel.initTalkActivity(this); 
           ThisNiche.hg.update(panel);
       }
     
       openTalkPanel(panel);
       return panel;
       
    }
    
    public void openChatRoom(HostedRoom room)
    {
        RoomPanel panel = hg.getOne(ThisNiche.hg, hg.and(
                hg.type(RoomPanel.class), hg.eq("roomId", room.getJid()), hg.eq(
                        "peerID", thisPeer.getIdentity())));
        HGHandle panelH;
        if (panel == null)
        {
            panel = new RoomPanel(thisPeer.getIdentity());
            panel.setRoomId(room.getJid());
            panel.initComponents();
            panelH = ThisNiche.hg.add(panel);
            addConnectionListener(panel);
        }
        else
        {
            panelH = ThisNiche.handleOf(panel);
            panel.initSplitterLocations();
        }

        panel.joinRoom();

        // Find an existing cell with this panel:
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, panelH);
        if (existingH != null)
        {
            PSwingNode n = TopFrame.getInstance().getCanvas()
                    .getPSwingNodeForHandle(existingH);
            n.blink();
            return;
        }

        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        int width = 400;
        int height = 500;
        int x = Math.max(0, (canvas.getWidth() - width) / 2);
        int y = Math.max(0, (canvas.getHeight() - height) / 2);
        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellGroupMember cgm = ThisNiche.hg.get(GUIHelper.addToCellGroup(panelH,
                top, null, null, new Rectangle(x, y, width, height), true));
        CellUtils.setName(cgm, "Chat room " + room.getName());
        cgm.setAttribute(VisualAttribs.showTitle, true);
        panel.initSplitterLocations();
        ThisNiche.hg.update(panel);
    }
    
    HGPeerIdentity getPeerIdentity(Occupant x)
    {
        String occ_name = stripJID(x.getJid());
        for(HGPeerIdentity i : getPeer().getConnectedPeers())
            if(i.getName().equals(occ_name))
                return i;
        return null;
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
        try{
            i.getConnection().getRoster().createEntry(
                    short_name + "@" + OPENFIRE_HOST, nick, null);
        }catch(XMPPException ex)
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
        try{
            RosterEntry entry = i.getConnection().getRoster().getEntry(
                    short_name + "@" + OPENFIRE_HOST); 
            if(entry != null)
                i.getConnection().getRoster().removeEntry(entry);
        }catch(XMPPException ex)
        {
            ex.printStackTrace();
        }
    }
    
    void removeRoster(Occupant x)
    {
        removeRoster(stripJID(x.getJid()));
    }
    
    void openTalkPanel(Occupant x)
    {
        if(isMe(x))
        {
            JOptionPane.showMessageDialog(TopFrame.getInstance(), "Can't talk to yourself");
            return;
        }
        
        HGPeerIdentity i = getPeerIdentity(x);
        if(i != null) 
        {
            openTalkPanel(i);
            return; 
        }
        
        String message = "User " + x.getNick() + 
        " is not currently in your friend list, would you like to add them?";
        int res = JOptionPane.showConfirmDialog(TopFrame.getInstance(), message, "?", 
                JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION)
            addRoster(x);
    }
    
    static String stripJID(String name)
    {
        int ind = name.indexOf("@");
        if (ind > -1)
            name = name.substring(0, ind);
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

    public static interface ConnectionListener
    {
        void connected(ConnectionContext ctx);

        void disconnected(ConnectionContext ctx);
        
        void workStarted(ConnectionContext ctx, boolean connect_or_disconnect);
    }
}
