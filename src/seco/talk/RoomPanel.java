package seco.talk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

import seco.api.Callback;

public class RoomPanel extends BaseChatPanel 
{
    private static final long serialVersionUID = -6292689880168959513L;
    private String roomId;
    private ChatPane chatPane;
    private TalkInputPane inputPane;
    private PeerList peerList;
    private MultiUserChat thechat;
    private JSplitPane peerListSplit;
    private JSplitPane inputSplit;

    private boolean roomJoined;

    public RoomPanel(HGPeerIdentity peerID)
    {
        super(peerID);
    }

    public RoomPanel()
    {
    }

    private MultiUserChat getTheChat()
    {
        if (thechat == null)
        {
            if (getConnectionContext() == null) return null;
            XMPPPeerInterface peerInterface = (XMPPPeerInterface) getConnectionContext()
                    .getPeer().getPeerInterface();
            thechat = new MultiUserChat(peerInterface.getConnection(), roomId);
        }
        return thechat;
    }

    void joinRoom()
    {
        if (getTheChat() == null || roomJoined) return;

        if (getTheChat().isJoined()) return;

        try
        {
            getTheChat().join(
                    getConnectionContext().getPeer().getIdentity().getName());
        }
        catch (XMPPException e)
        {
            // e.printStackTrace();
            throw new RuntimeException(e);
        }
        roomJoined = true;
        populatePeerList();
        getTheChat().addMessageListener(new PacketListener() {
            public void processPacket(Packet packet)
            {
                Message msg = (Message) packet;
                HGPeerIdentity id = new HGPeerIdentity();
                String otherId = (String) msg.getProperty("secoPeer");
                if (otherId == null)
                {
                    System.err
                            .println("Received a room message by a non-seco peer.");
                    return;
                }
                else if (otherId.equals(getConnectionContext().getPeer()
                        .getIdentity().getId().toString())) return;
                String from = packet.getFrom();
                int hostPart = from.lastIndexOf("/");
                if (hostPart > -1) from = from.substring(hostPart + 1);
                id.setName(from);
                id.setId(HGHandleFactory.makeHandle());
                getChatPane().chatFrom(id, msg.getBody());
            }
        });
        getTheChat().addParticipantListener(new PacketListener() {
            public void processPacket(Packet packet)
            {
                populatePeerList();
            }
        });
    }

    private void populatePeerList()
    {
        getPeerList().getListModel().removeAllElements();
        for (Iterator<String> i = getTheChat().getOccupants(); i.hasNext();)
        {
            String occ_name = i.next();
            Occupant o = getTheChat().getOccupant(occ_name);
            if (getTheChat().getOccupantPresence(occ_name).isAvailable())
            {
                //HGPeerIdentity id = new HGPeerIdentity();
                //id.setName(occ);
                //id.setId(HGHandleFactory.makeHandle());
                getPeerList().getListModel().addElement(o);
            }
        }
    }

    public void initComponents()
    {
        if (thechat != null) return;

        setLayout(new BorderLayout());
        inputPane = new TalkInputPane();
        inputPane.initComponents();
        inputPane.inputCallback = new ChatCallBack(this);

        chatPane = new ChatPane();
        chatPane.initComponents();
        peerListSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(chatPane), new JScrollPane(inputPane));
        peerListSplit.setResizeWeight(1.0);
        peerListSplit.setName("peerListSplit");
        peerList = new PeerList(getPeerID());
        peerList.initComponents();

        inputSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, peerListSplit,
                peerList);
        inputSplit.setName("inputSplit");
        inputSplit.setOneTouchExpandable(true);
        inputSplit.setResizeWeight(1.0);
        add(inputSplit, BorderLayout.CENTER);

        joinRoom();
    }

    public void initSplitterLocations()
    {
        getPeerList().getListModel().removeAllElements();
        get_split(get_split(this, "inputSplit"), "peerListSplit")
                .setDividerLocation((int) (0.8 * getHeight()));
        get_split(this, "inputSplit").setDividerLocation(
                (int) (0.7 * getWidth()));
    }

    private JSplitPane get_split(Container cont, String name)
    {
        for (Component c : cont.getComponents())
            if (name.equals(c.getName())) return (JSplitPane) c;
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

    public static class ChatCallBack implements Callback<String>
    {
        private RoomPanel room;

        public ChatCallBack()
        {
        }

        public ChatCallBack(RoomPanel room)
        {
            this.room = room;
        }

        public void callback(String msg)
        {
            if (room.getTheChat() == null) return;
            try
            {
                Message xmpp = room.getTheChat().createMessage();
                xmpp.setBody(msg);
                xmpp.setProperty("secoPeer", room.getConnectionContext()
                        .getPeer().getIdentity().getId().toString());
                room.getTheChat().sendMessage(xmpp);
                room.chatPane.chatFrom(room.getConnectionContext().getPeer()
                        .getIdentity(), msg);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }

        public RoomPanel getRoom()
        {
            return room;
        }

        public void setRoom(RoomPanel room)
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

    public void setPeerID(HGPeerIdentity peerID)
    {
        if (peerList != null) 
            peerList.setPeerID(peerID);
        super.setPeerID(peerID);
    }

    @Override
    public void connected(ConnectionContext ctx)
    {
        joinRoom();
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        getPeerList().getListModel().removeAllElements();
    }

    @Override
    public boolean isConnected()
    {
       return roomJoined;
    }
}
