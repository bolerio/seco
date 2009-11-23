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
import org.hypergraphdb.util.HGUtils;
import org.jivesoftware.smackx.muc.HostedRoom;

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
    private ConnectionConfig config;
    private boolean active;

    private boolean inProgress;
    private HyperGraphPeer thisPeer;
    private Set<ConnectionListener> listeners = new HashSet<ConnectionListener>();
    //Map<HGPeerIdentity, TalkActivity> talks = Collections
    //        .synchronizedMap(new HashMap<HGPeerIdentity, TalkActivity>());

    // Map<String, TalkRoom> roomPanels = Collections
    // .synchronizedMap(new HashMap<String, TalkRoom>());

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
        //TODO: strange HG exception - missing "peerID" dimension
       // TalkPanel panel = null;//hg.getOne(ThisNiche.hg, hg.and(hg.type(TalkPanel.class),
        //   hg.eq("friend", friend), hg.eq("peerID", getPeer().getIdentity())));
        List<TalkPanel> list = hg.getAll(ThisNiche.hg, hg.and(hg.type(TalkPanel.class),
                hg.eq("friend", friend)));
        for(TalkPanel tp : list)
            if(getPeer().getIdentity().equals(tp.getPeerID()))
               return tp;
        return null;
    }
    
    public void openTalkPanel(HGPeerIdentity friend)
    {
        // Ideally,we'd want to put a TalkPanel component in the workspace and
        // that
        // shows up the next time the system is started.
        TalkPanel panel = getTalkPanel(friend);
        
        HGHandle panelH = null;
         if (panel == null)
         {
            panel = new TalkPanel(friend, getPeer().getIdentity());
            panelH = ThisNiche.hg.add(panel);
        }else
            panelH = ThisNiche.handleOf(panel);
         
               
        //if (panel.getTalkActivity() == null)
       // {
            //TalkActivity activity = new TalkActivity(thisPeer, friend);
            //thisPeer.getActivityManager().initiateActivity(activity);
            //panel.setTalkActivity(activity);
            panel.initTalkActivity(this);
      //  }
        
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
        CellUtils.setName(cgm, friend.getName());
        cgm.setAttribute(VisualAttribs.showTitle, true);
    }

    public void openChatRoom(HostedRoom room)
    {
        TalkRoom panel = hg.getOne(ThisNiche.hg, hg.and(
                hg.type(TalkRoom.class), hg.eq("roomId", room.getJid()), hg.eq(
                        "peerID", thisPeer.getIdentity())));
        HGHandle panelH;
        if (panel == null)
        {
            panel = new TalkRoom(thisPeer.getIdentity());
            panel.setRoomId(room.getJid());
            panel.initComponents();
            panelH = ThisNiche.hg.add(panel);
            addConnectionListener(panel);
        }
        else
            panelH = ThisNiche.handleOf(panel);

        panel.joinRoom();

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
    }
}
