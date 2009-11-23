package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
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
public class ConnectionPanel extends JPanel implements
        ConnectionContext.ConnectionListener, PeerPresenceListener
{
    private static final long serialVersionUID = 9019036598512173062L;

    private JButton connectButton = null;
    private PeerList peerList;

    private HGPeerIdentity peerID;

    public ConnectionPanel()
    {
    }

    public ConnectionPanel(HGPeerIdentity peerID)
    {
        this.peerID = peerID;
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
            if (!ctx.isConnected()) {
                connectButton.setEnabled(false);
                connectButton.setText("Connecting...");
            }
            ctx.addConnectionListener(this); 
            ctx.connect();
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
            if (ctx.isConnected()) 
            {
                connectButton.setEnabled(false);
                connectButton.setText("Disconnecting...");
            }
            ctx.addConnectionListener(this); 
            ctx.disconnect();
        }
    }

    public void initComponents()
    {
        if (connectButton != null) return;
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setConnectButton(new JButton("Connect"));
        connectButton.addActionListener(new ButtonListener(this));
        add(connectButton, BorderLayout.NORTH);
        peerList = new PeerList(peerID);
        peerList.initComponents();
        add(peerList, BorderLayout.CENTER);
    }

    public boolean isConnected()
    {
        return connectButton.getText().equals("Disconnect");
    }

    public HyperGraphPeer getThisPeer()
    {
        return getConnectionContext().getPeer();
    }
    
    ConnectionContext ctx;
    public ConnectionContext getConnectionContext()
    {
        if(ctx == null)
        {
          ctx = ConnectionManager.getConnectionContext(getPeerID());
          if(ctx != null)
          {
             ctx.addConnectionListener(this);
             if(ctx.isConnected() && !isConnected())
                connected(ctx);
             else if(!ctx.isConnected() && isConnected())
                disconnected(ctx);
          }
        }
        return ctx;
        
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

    public HGPeerIdentity getPeerID()
    {
        return peerID;
    }

    public void setPeerID(HGPeerIdentity peerID)
    {
        this.peerID = peerID;
        getConnectionContext();
    }

    @Override
    public void connected(ConnectionContext ctx)
    {
        connectButton.setEnabled(true);
        connectButton.setText("Disconnect");
        populate();
    }
    
    private void populate()
    {
        getPeerList().getListModel().removeAllElements();
        fetchRooms();
        for(HGPeerIdentity i: ctx.getPeer().getConnectedPeers())
            getPeerList().getListModel().addElement(i);
        getPeerList().setPeerID(peerID);
        ctx.getPeer().addPeerPresenceListener(this);
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        connectButton.setEnabled(true);
        connectButton.setText("Connect");
        getPeerList().getListModel().removeAllElements();
    }
    
    @Override
    public void peerJoined(HGPeerIdentity target)
    {
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
        if (server.indexOf("kobrix") > -1) server = "kobrix.syspark.net";
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
            if (panel.getConnectButton().getText().equals("Connect")) panel
                    .connect();
            else if (panel.getConnectButton().getText().equals("Disconnect"))
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