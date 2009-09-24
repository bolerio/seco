package seco.talk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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
    private JSplitPane peerListSplit;
    private JSplitPane inputSplit; 
    
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
                String otherId = (String)msg.getProperty("secoPeer");
                if (otherId == null)
                {
                    System.err.println("Received a room message by a non-seco peer.");
                    return;
                }
                else if (otherId.equals(connectionPanel.getThisPeer().getIdentity().getId().toString()))
                    return;
                String from = packet.getFrom();
                int hostPart = from.lastIndexOf("/");
                if (hostPart > -1)
                    from = from.substring(hostPart + 1);                
                id.setName(from);
                id.setId(HGHandleFactory.makeHandle());
                getChatPane().chatFrom(id, msg.getBody());
            }
        });
        getTheChat().addParticipantListener(new PacketListener() {
            public void processPacket(Packet packet)
            {
                getPeerList().getListModel().removeAllElements();
                for (Iterator<String> i = getTheChat().getOccupants(); i.hasNext(); )
                {
                    String occ = i.next();
                    if (getTheChat().getOccupantPresence(occ).isAvailable())
                    {
                        HGPeerIdentity id = new HGPeerIdentity();
                        id.setName(occ);
                        id.setId(HGHandleFactory.makeHandle());
                        getPeerList().getListModel().addElement(id);
                    }
                }
                //peerList.peers.fireChangeEvent();
            }
        });
    }
    
    public void initComponents()
    {
        if (thechat != null)
            return;
        
        setLayout(new BorderLayout());  
        inputPane = new TalkInputPane();
        inputPane.initComponents();
        inputPane.inputCallback = new ChatCallBack(this); 
           
        chatPane = new ChatPane();
        chatPane.initComponents();
        peerListSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              new JScrollPane(chatPane), 
                                              new JScrollPane(inputPane));
        peerListSplit.setResizeWeight(1.0);
        peerListSplit.setName("peerListSplit");
        peerList = new PeerList();
        peerList.initComponents();    
        inputSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                    peerListSplit, 
                                    peerList);
        inputSplit.setName("inputSplit");
        inputSplit.setOneTouchExpandable(true);
        inputSplit.setResizeWeight(1.0);
        this.add(inputSplit, BorderLayout.CENTER);
        
//        this.addComponentListener(new ComponentAdapter() 
//        {            
//            public void componentResized(ComponentEvent e) 
//            {
//                peerListSplit.setDividerLocation((int)(0.8*getHeight()));
//                inputSplit.setDividerLocation((int)(0.7*getWidth()));                
//            }    
//        });
        joinRoom();        
    }
    
    public void initSplitterLocations()
    {
        getPeerList().getListModel().removeAllElements();
        get_split(get_split(this,"inputSplit"), "peerListSplit").setDividerLocation((int)(0.8*getHeight()));
        get_split(this,"inputSplit").setDividerLocation((int)(0.7*getWidth()));  
    }
    
    private JSplitPane get_split(Container cont, String name)
    {
        for(Component c: cont.getComponents())
            if(name.equals(c.getName()))
                return (JSplitPane) c;
        return null;
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
                Message xmpp = room.getTheChat().createMessage();
                xmpp.setBody(msg);
                xmpp.setProperty("secoPeer", room.connectionPanel.getThisPeer().getIdentity().getId().toString());
                room.getTheChat().sendMessage(xmpp);
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

    public PeerList getPeerList()
    {
        return peerList;
    }

    public void setPeerList(PeerList peerList)
    {
        this.peerList = peerList;
    }

    public ChatPane getChatPane()
    {
        return chatPane;
    }

    public void setChatPane(ChatPane chatPane)
    {
        this.chatPane = chatPane;
    }

    public TalkInputPane getInputPane()
    {
        return inputPane;
    }

    public void setInputPane(TalkInputPane inputPane)
    {
        this.inputPane = inputPane;
    }       
}
