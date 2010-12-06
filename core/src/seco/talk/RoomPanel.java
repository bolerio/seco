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
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

import seco.ThisNiche;
import seco.talk.PeerList.PeerListModel;
import seco.util.RequestProcessor;
import seco.util.task.Callback;

public class RoomPanel extends BaseChatPanel implements PeerPresenceListener
{
    private static final long serialVersionUID = -6292689880168959513L;
    private String roomId;
    private ChatPane chatPane;
    private TalkInputPane inputPane;
    private PeerList peerList;
    private MultiUserChat thechat;
    private JSplitPane peerListSplit;
    private JSplitPane inputSplit;

    // private boolean roomJoined;
    private PacketListener packetListener = new MyPacketListener();
    private MyParticipantStatusListener participantStatusListener = new MyParticipantStatusListener();

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
            if (peerInterface != null)
                thechat = new MultiUserChat(peerInterface.getConnection(),
                        roomId);
        }
        return thechat;
    }

   private MyParticipantListener participantListener = new MyParticipantListener();
    void joinRoom()
    {
        initSplitterLocations();
        if (getTheChat() == null) return;

        if (getTheChat().isJoined())
        {
            if (getPeerList().getListModel().size() == 0) populatePeerList();
            return;
        }

        getTheChat().removeMessageListener(packetListener);
        getTheChat().addMessageListener(packetListener);

        getTheChat().removeParticipantListener(participantListener);
        getTheChat().addParticipantListener(participantListener);

        getTheChat().removeParticipantStatusListener(participantStatusListener);
        getTheChat().addParticipantStatusListener(participantStatusListener);

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

        populatePeerList();
    }

    static String stripNick(String label)
    {
        int hostPart = label.lastIndexOf("/");
        if (hostPart > -1) label = label.substring(hostPart + 1);
        return label;
    }

    private void populatePeerList()
    {
        // getPeerList().getListModel().removeAllElements();
        for (Iterator<String> i = getTheChat().getOccupants(); i.hasNext();)
        {
            String occ_name = i.next();
            OccupantEx o = new OccupantEx(getTheChat().getOccupant(occ_name));
            if (getConnectionContext().isMe(o))
            {
                getPeerList().getListModel().addElement(o);
                continue;
            }
            if (// getConnectionContext().getPeerIdentity(o) != null &&
            getTheChat().getOccupantPresence(occ_name).isAvailable())
                getPeerList().getListModel().addElement(o);
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
        // peerListSplit.setResizeWeight(1.0);
        peerListSplit.setName("peerListSplit");
        peerList = new PeerList(getPeerID());
        peerList.initComponents();

        inputSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, peerListSplit,
                peerList);
        inputSplit.setName("inputSplit");
        inputSplit.setOneTouchExpandable(true);
        // inputSplit.setResizeWeight(1.0);
        add(inputSplit, BorderLayout.CENTER);
        setDoubleBuffered(false);
        joinRoom();
    }

    private void initSplitterLocations()
    {
        int h = Math.max(300, getHeight());
        int w = Math.max(300, getWidth());
        get_split(get_split(this, "inputSplit"), "peerListSplit")
                .setDividerLocation((int) (0.8 * h));
        get_split(this, "inputSplit").setDividerLocation((int) (0.7 * w));
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
        if (peerList != null) peerList.setPeerID(peerID);
        super.setPeerID(peerID);
    }

    @Override
    public void connected(ConnectionContext ctx)
    {
        joinRoom();
        ctx.getPeer().addPeerPresenceListener(this);
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        getPeerList().getListModel().removeAllElements();
        thechat = null;
        ctx.getPeer().removePeerPresenceListener(this);
    }
    
    public void peerJoined(HGPeerIdentity target)
    {
       //do nothing
    }

    //needed because when a peer is disconnected, no "room left" event is triggered 
    public void peerLeft(HGPeerIdentity target)
    {
        PeerListModel model = getPeerList().getListModel();
       // System.out.println("Peer(Occupant) left: " + target.getName());
        for (int i = 0; i < model.size(); i++)
        {
            OccupantEx ex = (OccupantEx) model.getElementAt(i);
            if(target.getName().equals(ex.getNick()))
            { 
             model.removeElement(ex);
             return;
             }
        }
    }

    @Override
    public void workStarted(ConnectionContext ctx, boolean connect_or_disconnect)
    {
        if (!connect_or_disconnect)
        {
            try
            {
                getTheChat().leave();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        return getTheChat() != null && getTheChat().isJoined();
    }

    class MyPacketListener implements PacketListener
    {
        public void processPacket(Packet packet)
        {
            Message msg = (Message) packet;
            HGPeerIdentity id = new HGPeerIdentity();
            String otherId = (String) msg.getProperty("secoPeer");
            if (otherId == null)
            {
                System.err
                        .println("Received a room message by a non-seco peer."
                                + msg.getBody());
                return;
            }
            else if (otherId.equals(getConnectionContext().getPeer()
                    .getIdentity().getId().toString())) return;
            String from = packet.getFrom();
            int hostPart = from.lastIndexOf("/");
            if (hostPart > -1) from = from.substring(hostPart + 1);
            id.setName(from);
            id.setId(ThisNiche.graph.getHandleFactory().makeHandle());
            getChatPane().chatFrom(id, msg.getBody());
        }
    }

    class MyParticipantListener implements PacketListener
    {
        public void processPacket(Packet packet)
        {
            Presence presence = (Presence) packet;
            final String from = presence.getFrom();
            String myRoomJID = getTheChat().getRoom() + "/"
                    + getTheChat().getNickname();
            boolean isMe = from.equals(myRoomJID);
            //System.out.println("PoomPanel - presence: " + presence.getFrom()
            //        + ":" + presence.getType());
            if (isMe)
            {
                Occupant o = getTheChat().getOccupant(from);
                if (o != null) getPeerList().getListModel().addElement(
                        new OccupantEx(o));
                else
                    // ugly hack, to add Me in the list
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run()
                        {
                            // populatePeerList();
                            Occupant o = getTheChat().getOccupant(from);
                            if (o != null)
                                getPeerList().getListModel().addElement(
                                        new OccupantEx(o));
                        }
                    }, 3000);
            }// else
        }
    }

    class MyParticipantStatusListener extends DefaultParticipantStatusListener
    {

        @Override
        public void joined(String participant)
        {
            Occupant o = getTheChat().getOccupant(participant);
            if (o != null)
            {
                System.out.println("Occupant joined: " + participant + ":"
                        + o.getNick());
                getPeerList().getListModel().addElement(new OccupantEx(o));
            }
        }

        @Override
        public void kicked(String participant, String actor, String reason)
        {
            Occupant o = getTheChat().getOccupant(participant);
            if (o != null)
            {
                System.out.println("Occupant kicked: " + participant + ":"
                        + o.getNick());
                OccupantEx ex = new OccupantEx(o);
                if (getConnectionContext().isMe(ex))
                {
                    disconnected(getConnectionContext());
                    return;
                }
                getPeerList().getListModel().removeElement(ex);
            }
        }

        @Override
        public void left(String participant)
        {
            PeerListModel model = getPeerList().getListModel();
            Occupant oc = getTheChat().getOccupant(participant);
            if (oc != null)
            {
                System.out.println("Occupant left: " + participant + ":"
                        + oc.getNick());
                OccupantEx ex = new OccupantEx(oc);
                if (getConnectionContext().isMe(ex))
                {
                    disconnected(getConnectionContext());
                    return;
                }

                model.removeElement(ex);
                return;
            }
        }
    }
}
