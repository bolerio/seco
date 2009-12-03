package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * <p>
 * A visual component to manage a single network connection.
 * </p>
 * 
 * @author Borislav Iordanov
 * 
 */
public class ConnectionPanel extends BaseChatPanel implements
        PeerPresenceListener
{
    private static final long serialVersionUID = 9019036598512173062L;
    private static final String LABEL_CONNECT = "Connect";
    private static final String LABEL_CONNECTING = "Connecting...";
    private static final String LABEL_DISCONNECT = "Disconnect";
    private static final String LABEL_DISCONNECTING = "Disconnecting...";
   
    private JButton connectButton = null;
    private PeerList peerList;

    public ConnectionPanel()
    {
    }

    public ConnectionPanel(HGPeerIdentity peerID)
    {
        super(peerID);
    }

    public void connect()
    {
        ConnectionContext ctx = ConnectionManager
                .getConnectionContext(getPeerID());
        if (ctx == null)
        {
            // TODO:???
        }
        else
        {
            ctx.addConnectionListener(this);
            if (!ctx.isConnected())
            {
                ctx.connect();
            }
            else
            {
                // ctx already connected, but panel is not
                if (!isConnected()) connected(ctx);
            }
        }
    }

    public void disconnect()
    {
        ConnectionContext ctx = ConnectionManager
                .getConnectionContext(getPeerID());
        if (ctx == null)
        {
            // TODO:???
        }
        else
        {
            ctx.addConnectionListener(this);
            if (ctx.isConnected())
            {
                ctx.disconnect(false);
            }
            else
            {
                // ctx already disconnected, but panel is not
                if (isConnected()) disconnected(ctx);
            }

        }
    }

    public void initComponents()
    {
        if (connectButton != null) return;
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setConnectButton(new JButton(LABEL_CONNECT));
        connectButton.addActionListener(new ButtonListener(this));
        add(connectButton, BorderLayout.NORTH);
        peerList = new PeerList(getPeerID());
        peerList.initComponents();
        add(peerList, BorderLayout.CENTER);
    }

    public boolean isConnected()
    {
        return connectButton.getText().equals(LABEL_DISCONNECT);
    }

    public HyperGraphPeer getThisPeer()
    {
        return getConnectionContext().getPeer();
    }

    public JButton getConnectButton()
    {
        return connectButton;
    }

    public void setConnectButton(JButton button)
    {
        this.connectButton = button;
    }

    public PeerList getPeerList()
    {
        return peerList;
    }

    public void setPeerList(PeerList peerList)
    {
        this.peerList = peerList;
    }

    @Override
    public void connected(ConnectionContext ctx)
    {
        connectButton.setEnabled(true);
        connectButton.setText(LABEL_DISCONNECT);
        populate();
    }

    private void populate()
    {
        getPeerList().getListModel().removeAllElements();
        fetchRooms();
        for (HGPeerIdentity i : ctx.getPeer().getConnectedPeers())
            if(getConnectionContext().isInRoster(i))    
               getPeerList().getListModel().addElement(i);
        getPeerList().setPeerID(getPeerID());
        ctx.getPeer().addPeerPresenceListener(this);
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        connectButton.setEnabled(true);
        connectButton.setText(LABEL_CONNECT);
    }

    @Override
    public void workStarted(ConnectionContext ctx, boolean connect_or_disconnect)
    {
        connectButton.setEnabled(false);
        String text = (connect_or_disconnect) ? LABEL_CONNECTING
                : LABEL_DISCONNECTING;
        connectButton.setText(text);
        getPeerList().getListModel().removeAllElements();
    }

    @Override
    public void peerJoined(HGPeerIdentity target)
    {
        if(getConnectionContext().isInRoster(target))
           getPeerList().getListModel().addElement(target);
    }

    @Override
    public void peerLeft(HGPeerIdentity target)
    {
        getPeerList().getListModel().removeElement(target);
    }

    private void fetchRooms()
    {
        XMPPPeerInterface peerInterface = (XMPPPeerInterface) getThisPeer()
                .getPeerInterface();
        String server = peerInterface.getServerName();
        if (server.indexOf("kobrix") > -1)
            server = ConnectionContext.OPENFIRE_HOST;
        try
        {
            for (HostedRoom room : MultiUserChat.getHostedRooms(peerInterface
                    .getConnection(), "conference." + server))
            {
                peerList.getListModel().addElement(room);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
    
//    private Roster getRoster()
//    {
//        XMPPPeerInterface peerInterface = (XMPPPeerInterface) getThisPeer()
//        .getPeerInterface();
//        return peerInterface.getConnection().getRoster();
//    }
//    
//    public void entriesAdded(Collection<String> addresses) 
//    {
//        System.out.println("New friends: " + addresses);
//        for(String user: addresses)
//        {
//            Presence bestPresence = getRoster().getPresence(user);                   
//            if (bestPresence.getType() == Presence.Type.available)
//               peerJoined(user);
//        }
//    }
//    public void entriesDeleted(Collection<String> addresses) 
//    {
//        System.out.println("Friends left: " + addresses);
//        for(String user: addresses)
//           peerLeft(user);
//    }
//    public void entriesUpdated(Collection<String> addresses) 
//    {
//        //System.out.println("friends changed: " + addresses);
//    }
//    public void presenceChanged(Presence presence) 
//    {
//        String user = presence.getFrom();
//
//        System.out.println("Presence changed: " + presence.getFrom() + " " + presence);                    
//        Presence bestPresence = getRoster().getPresence(user);                   
//        if (bestPresence.getType() == Presence.Type.available)
//            peerJoined(user);
//        else if (bestPresence.getType() == Presence.Type.unavailable)
//        {
//            peerLeft(user);                        
//        }                        
//    }

    public static class ButtonListener implements ActionListener
    {
        private ConnectionPanel panel;

        public ButtonListener()
        {
        }

        public ButtonListener(ConnectionPanel panel)
        {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent ev)
        {
            if (panel.getConnectButton().getText().equals(LABEL_CONNECT)) 
            	panel.connect();
            else if (panel.getConnectButton().getText()
                    .equals(LABEL_DISCONNECT)) 
            	panel.disconnect();
        }

        public ConnectionPanel getPanel()
        {
            return panel;
        }

        public void setPanel(ConnectionPanel panel)
        {
            this.panel = panel;
        }
    }
}