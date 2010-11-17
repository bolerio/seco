package seco.talk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.NetworkPeerPresenceListener;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import edu.umd.cs.piccolox.pswing.PSwing;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.GUIUtilities;
import seco.gui.PSwingNode;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.ToolbarButton;
import seco.notebook.ScriptletAction;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.util.IconManager;

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
                ctx.disconnect(true);
            }
            else
            {
                // ctx already disconnected, but panel is not
                if (isConnected()) disconnected(ctx);
            }

        }
    }

    void updateState()
    {
        if (getConnectionContext().isConnected()) connected(getConnectionContext());
        else
            disconnected(getConnectionContext());
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

        add_status_bar();
    }

    private void add_status_bar()
    {
        StatusPanel status = new StatusPanel();
        status.init();
        add(status, BorderLayout.SOUTH);
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
        PSwingNode node = GUIHelper.getPSwingNode(this);
        if (node != null)
        {
            CellGroupMember cgm = ThisNiche.graph.get(node.getHandle());
            String n = ctx.getConfig().getUsername() + " on " + ctx.getNetworkName();
            CellUtils.setName(cgm, n);
        }
    }

    private void populate()
    {
        getPeerList().getListModel().removeAllElements();
        fetchRooms();
        for (HGPeerIdentity i : ctx.getPeer().getConnectedPeers())
            if (getConnectionContext().isInRoster(i))
                getPeerList().getListModel().addElement(i);
        getPeerList().setPeerID(getPeerID());
        ctx.getPeer().addPeerPresenceListener(this);
//        ctx.getPeer().getPeerInterface().addPeerPresenceListener(
//                new NetworkPeerPresenceListener() {
//
//                    @Override
//                    public void peerJoined(Object networkTarget)
//                    {
//                        HGPeerIdentity i = ctx.getPeer().getIdentity(networkTarget);
//                               // .getPeerIdentity((String) networkTarget);
//                        if (i != null && getConnectionContext().isInRoster(i))
//                            getPeerList().getListModel().addElement(i);
//                    }
//
//                    @Override
//                    public void peerLeft(Object networkTarget)
//                    {
//                        //do nothing
//                       // HGPeerIdentity i = ctx
//                       //         .getPeerIdentity((String) networkTarget);
//                       // if (i != null && getConnectionContext().isInRoster(i))
//                       //     getPeerList().getListModel().removeElement(i);
//                    }
//
//                });
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        connectButton.setEnabled(true);
        connectButton.setText(LABEL_CONNECT);
        getPeerList().getListModel().removeAllElements();
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
        if (getConnectionContext().isInRoster(target))
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

    // private Roster getRoster()
    // {
    // XMPPPeerInterface peerInterface = (XMPPPeerInterface) getThisPeer()
    // .getPeerInterface();
    // return peerInterface.getConnection().getRoster();
    // }
    //    
    // public void entriesAdded(Collection<String> addresses)
    // {
    // System.out.println("New friends: " + addresses);
    // for(String user: addresses)
    // {
    // Presence bestPresence = getRoster().getPresence(user);
    // if (bestPresence.getType() == Presence.Type.available)
    // peerJoined(user);
    // }
    // }
    // public void entriesDeleted(Collection<String> addresses)
    // {
    // System.out.println("Friends left: " + addresses);
    // for(String user: addresses)
    // peerLeft(user);
    // }
    // public void entriesUpdated(Collection<String> addresses)
    // {
    // //System.out.println("friends changed: " + addresses);
    // }
    // public void presenceChanged(Presence presence)
    // {
    // String user = presence.getFrom();
    //
    // System.out.println("Presence changed: " + presence.getFrom() + " " +
    // presence);
    // Presence bestPresence = getRoster().getPresence(user);
    // if (bestPresence.getType() == Presence.Type.available)
    // peerJoined(user);
    // else if (bestPresence.getType() == Presence.Type.unavailable)
    // {
    // peerLeft(user);
    // }
    // }

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
            if (panel.getConnectButton().getText().equals(LABEL_CONNECT)) panel
                    .connect();
            else if (panel.getConnectButton().getText()
                    .equals(LABEL_DISCONNECT)) panel.disconnect();
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

    public static class StatusPanel extends JPanel
    {
        private JLabel statusLabel;
        private JToolBar bar;

        public StatusPanel()
        {
        }

        void init()
        {
            setLayout(new GridBagLayout());
            GridBagConstraints gbg = new GridBagConstraints();
            gbg.gridy = 0;
            gbg.gridx = 0;
            gbg.fill = GridBagConstraints.HORIZONTAL;
            gbg.anchor = GridBagConstraints.WEST;
            gbg.weightx = 1.0;
            statusLabel = new JLabel();
            add(statusLabel, gbg);

            gbg = new GridBagConstraints();
            gbg.gridy = 0;
            gbg.gridx = 1;
            gbg.insets = new Insets(0, 0, 0, 0);
            gbg.anchor = GridBagConstraints.EAST;
            // status.add(statusLabel, BorderLayout.EAST);
            bar = new JToolBar();
            bar.add(new ToolbarButton(new AddToRoaster(), "Add To Roaster"));
            bar.add(new ToolbarButton(new RemoveFromRoaster(),
                    "Remove From Roaster"));
            add(bar, gbg);
        }

        public void setMessage(String text)
        {
            getStatusLabel().setText(text);
        }

        public JLabel getStatusLabel()
        {
            return statusLabel;
        }

        public void setStatusLabel(JLabel statusLabel)
        {
            this.statusLabel = statusLabel;
        }

        public JToolBar getBar()
        {
            return bar;
        }

        public void setBar(JToolBar bar)
        {
            this.bar = bar;
        }
    }

    private static ConnectionPanel get_from_event(ActionEvent e)
    {
        Object o = e.getSource();
        while (o instanceof JComponent)
        {
            o = ((JComponent) o).getParent();
            if (o instanceof ConnectionPanel) return (ConnectionPanel) o;
        }
        return null;
    }

    static class AddToRoaster extends AbstractAction
    {

        public AddToRoaster()
        {
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("add.gif"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ConnectionPanel panel = get_from_event(e);
            if (!panel.getConnectionContext().isConnected()) return;
            RoasterDlg dlg = new RoasterDlg(panel.getConnectionContext(), true);
            dlg.setTitle("Add To Roster");
            dlg.setSize(new Dimension(420, 400));
            dlg.setVisible(true);
        }
    }

    static class RemoveFromRoaster extends AbstractAction
    {

        public RemoveFromRoaster()
        {
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("remove.gif"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ConnectionPanel panel = get_from_event(e);
            if (!panel.getConnectionContext().isConnected()) return;
            RoasterDlg dlg = new RoasterDlg(panel.getConnectionContext(), false);
            dlg.setTitle("Remove From Roster");
            dlg.setSize(new Dimension(420, 400));
            dlg.setVisible(true);
        }

    }
}