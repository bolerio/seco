package seco.talk;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import seco.api.Callback;

public class TalkRoom extends JPanel
{
    private static final long serialVersionUID = -6292689880168959513L;
    private String roomId;
    @AtomReference("symbolic")
    private ConnectionPanel connectionPanel;
    private ChatPane chatPane;
    private TalkInputPane inputPane;
    private PeerList peerList;
    private MultiUserChat thechat;
    
    private MultiUserChat getTheChat()
    {
        if (thechat == null)
        {
            XMPPPeerInterface peerInterface = (XMPPPeerInterface)connectionPanel.getThisPeer().getPeerInterface();
            thechat = new MultiUserChat(peerInterface.getConnection(), roomId);            
        }
        return thechat;
    }
        
    private void joinRoom()
    {
        if (!getTheChat().isJoined())
            try
            {
                getTheChat().join(connectionPanel.getThisPeer().getIdentity().getName());
            }
            catch (XMPPException e)
            {
//                e.printStackTrace();
                throw new RuntimeException(e);
            }
        getTheChat().addMessageListener(new PacketListener() {
            public void processPacket(Packet packet)
            {
                Message msg = (Message)packet;
                HGPeerIdentity id = new HGPeerIdentity();
                id.setName(packet.getFrom());
                id.setId(HGHandleFactory.makeHandle());
                chatPane.chatFrom(id, msg.getBody());
            }
        });
        getTheChat().addParticipantListener(new PacketListener() {
            public void processPacket(Packet packet)
            {
                peerList.peers.thePeers.clear();
                for (Iterator<String> i = getTheChat().getOccupants(); i.hasNext(); )
                {
                    String occ = i.next();
                    if (getTheChat().getOccupantPresence(occ).isAvailable())
                    {
                        HGPeerIdentity id = new HGPeerIdentity();
                        id.setName(occ);
                        id.setId(HGHandleFactory.makeHandle());
                        peerList.peers.thePeers.add(id);
                    }
                }
                peerList.peers.fireChangeEvent();
            }
        });
    }
    
    public void initComponents()
    {
        if (thechat != null)
            return;
        
        setLayout(new BorderLayout());  
        inputPane = new TalkInputPane();
        inputPane.inputCallback = new ChatCallBack(this); 
           
        chatPane = new ChatPane();
        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              new JScrollPane(chatPane), 
                                              new JScrollPane(inputPane));
        
        peerList = new PeerList();
        peerList.initComponents();        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              split1, 
                                              peerList);
        this.add(splitPane, BorderLayout.CENTER);
        joinRoom();
    }
    
    public String getRoomId()
    {
        return roomId;
    }
    public void setRoomId(String roomId)
    {
        this.roomId = roomId;
    }
    public ConnectionPanel getConnectionPanel()
    {
        return connectionPanel;
    }
    public void setConnectionPanel(ConnectionPanel panel)
    {
        this.connectionPanel = panel;
    }    
    
    public static class ChatCallBack implements Callback<String>
    {
        private TalkRoom room;
        
        public ChatCallBack()
        {
        }
        public ChatCallBack(TalkRoom room)
        {
            this.room = room;
        }
        
        public void callback(String msg)
        {
            try
            {
                room.getTheChat().sendMessage(msg);
                room.chatPane.chatFrom(
                        room.connectionPanel.getThisPeer().getIdentity(), msg);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        public TalkRoom getRoom()
        {
            return room;
        }
        public void setRoom(TalkRoom room)
        {
            this.room = room;
        }
    }        
}
