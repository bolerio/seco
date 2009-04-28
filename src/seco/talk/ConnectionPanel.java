package seco.talk;

import static org.hypergraphdb.peer.Structs.getPart;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.xmpp.*;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.util.CompletedFuture;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.Collections;

import seco.ThisNiche;
import seco.U;
import seco.api.CallableCallback;
import seco.gui.GUIHelper;
import seco.things.Cell;
import seco.things.CellGroup;

/**
 * <p>
 * A visual component to manage a single network connection. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ConnectionPanel extends JPanel
{
    private static final long serialVersionUID = 9019036598512173062L;

    @HGIgnore
    HyperGraphPeer thisPeer;
    JButton connectButton = null;
    Map<HGPeerIdentity, TalkActivity> talks =
        Collections.synchronizedMap(new HashMap<HGPeerIdentity, TalkActivity>());        
    Map<String, TalkRoom> roomPanels = 
        Collections.synchronizedMap(new HashMap<String, TalkRoom>());
    String peerConfigResource = "seco/talk/xmpp1.json";
    PeerList peerList;
    
    private Future<Boolean> openPeer()
    {
        ConnectionConfig config = getConnectionConfig();
        String configResource = U.getResourceContentAsString(peerConfigResource);
        if (configResource == null)
            throw new RuntimeException(
                    "Unable to find default config " + peerConfigResource);
        JSONReader reader = new JSONReader();
        Map<String, Object> peerConfig = getPart(reader.read(configResource));
        peerConfig.put("localDB", ThisNiche.hg.getLocation());
        peerConfig.put("peerName", config.getUsername());
        Map<String, Object> xmppConfig = getPart(peerConfig, "interfaceConfig");
        xmppConfig.put("anonymous", config.isAnonymousLogin());
        xmppConfig.put("autoRegister", config.isAutoRegister());
        xmppConfig.put("user", config.getUsername());
        xmppConfig.put("password", config.getPassword());
        xmppConfig.put("serverUrl", config.getHostname());
        xmppConfig.put("port", config.getPort());
        thisPeer = new HyperGraphPeer(peerConfig);
        thisPeer.getObjectContext().put(ConnectionPanel.class.getName(), this);
        Future<Boolean> f = thisPeer.start(config.getUsername(), config.getPassword());
        
        thisPeer.addPeerPresenceListener(new PeerPresenceListener() {
            public void peerJoined(HGPeerIdentity target)
            {
                peerList.peers.thePeers.add(target);
                peerList.peers.fireChangeEvent();
            }

            public void peerLeft(HGPeerIdentity target)
            {
                peerList.peers.thePeers.remove(target);
                peerList.peers.fireChangeEvent();
            }
        });
        return f;
    }

    private void fetchRooms()
    {
        XMPPPeerInterface peerInterface = (XMPPPeerInterface)thisPeer.getPeerInterface();
        try
        {
            for (HostedRoom room : MultiUserChat.getHostedRooms(
                                     peerInterface.getConnection(), "conference." + peerInterface.getServerName()))
            {
                peerList.peers.thePeers.add(room);
                peerList.peers.fireChangeEvent();
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
    
    private Future<Boolean> startConnecting()
    {
        if (isConnected())
            return new CompletedFuture<Boolean>(true);
        else
            return openPeer();
    }
    
    public void connect()
    {
        if (connectButton.getText().equals("Disconnect"))
            return;
        else if (isConnected())
        {
            connectButton.setText("Disconnect");
        }
        connectButton.setText("Connecting...");
        connectButton.setEnabled(false);
        U.run(new CallableCallback<Boolean>() {
          public Boolean call() throws Exception 
          { 
              return startConnecting().get(); 
          }
          public void onCompletion(Boolean result, Throwable t)
          {
              if (t == null && result)
              {
                  JOptionPane.showMessageDialog(ConnectionPanel.this, 
                                                "Successfully connected to network.");
                  try { fetchRooms(); }
                  catch (Throwable error) 
                  { 
                      JOptionPane.showMessageDialog(ConnectionPanel.this, 
                      error, "Failed to get chat rooms", JOptionPane.ERROR_MESSAGE);                       
                  }
                  connectButton.setText("Disconnect");                  
              }
              else
              {
                  if (t != null)
                      t.printStackTrace(System.err);
                  JOptionPane.showMessageDialog(ConnectionPanel.this, 
                                                "Failed to connected to network, see error console.");
                  connectButton.setText("Connect");                          
              }
              connectButton.setEnabled(true);                      
          }
        });        
    }
    
    public void disconnect()
    {
        connectButton.setText("Disconnecting...");
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
                    peerList.peers.thePeers.clear();
                    peerList.peers.fireChangeEvent();
                    JOptionPane.showMessageDialog(ConnectionPanel.this, 
                                                  "Successfully disconnect from network.");
                    connectButton.setText("Connect");
                }
                else
                {
                    if (t != null)
                        t.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(ConnectionPanel.this, 
                                                  "Failed to connected to network, see error console.");
                    connectButton.setText("Disconnect");                          
                }
                connectButton.setEnabled(true);                      
            }
          });           
    }
    
    public void initComponents()
    {        
        if (connectButton != null)
            return;
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));        
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev)
            {
                if (connectButton.getText().equals("Connect"))
                    connect();
                else if (connectButton.getText().equals("Disconnect"))
                    disconnect();
            }
        });
        add(connectButton, BorderLayout.NORTH);
        peerList = new PeerList();
        peerList.setConnectionPanel(this);
        peerList.initComponents();
        add(peerList, BorderLayout.CENTER);
    }
    
    public ConnectionPanel()
    {                
    }

    public void openTalkPanel(HGPeerIdentity friend)
    {
        // Ideally,we'd want to put a TalkPanel component in the workspace and that
        // shows up the next time the system is started.
        
        
//        TalkPanel existing = U.hgetOne(hg.and(hg.type(TalkPanel.class), 
//                                              hg.eq("friend", friend)));
//        if (existing == null)
//        {
//            existing = new TalkPanel();
//            existing.setFriend(friend);
//            HGHandle h = ThisNiche.hg.add(existing);
//        }
        TalkActivity activity = talks.get(friend); 
        if (activity == null)
        {
            activity = new TalkActivity(thisPeer, friend);
            thisPeer.getActivityManager().initiateActivity(activity);
            talks.put(friend, activity);
        }
        HGHandle panelHandle = ThisNiche.hg.getHandle(activity.getPanel());
        GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE, 
                                panelHandle, 
                                null, 
                                null, 
                                new Rectangle(500, 200, 200, 100));
    }

    public void openChatRoom(HostedRoom room)
    {
        TalkRoom roomPanel = roomPanels.get(room.getJid());
        if (roomPanel == null)
        {
            roomPanel = hg.getOne(ThisNiche.hg, hg.and(hg.type(TalkRoom.class), 
                                                       hg.eq("roomId", room.getJid())));
            if (roomPanel == null)
            {
                roomPanel = new TalkRoom();
                roomPanel.setRoomId(room.getJid());
                roomPanel.setConnectionPanel(this);
                roomPanel.initComponents();
                ThisNiche.hg.add(roomPanel);
            } 
            roomPanel.initComponents();
        }
        HGHandle panelHandle = ThisNiche.hg.getHandle(roomPanel);
        GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE, 
                                panelHandle, 
                                null, 
                                null, 
                                new Rectangle(500, 200, 200, 100));
    }
    
    public boolean isConnected()
    {
        return thisPeer != null && thisPeer.getPeerInterface().isConnected(); 
    }
    
//    public void setConnectionConfig(ConnectionConfig config)
//    {
//        this.config = config;
//    }
    
    public ConnectionConfig getConnectionConfig()
    {
        ConnectionConfig config = null;
        if (config == null)
        {
            config = hg.getOne(ThisNiche.hg, hg.type(ConnectionConfig.class));
            if (config == null)
            {
                config = new ConnectionConfig();
                ThisNiche.hg.add(config);
            }
        }
        return config;
    }
    
    public HyperGraphPeer getThisPeer()
    {
        return thisPeer;
    }
 
}