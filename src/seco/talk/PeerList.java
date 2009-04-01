package seco.talk;

import static org.hypergraphdb.peer.Structs.getPart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.util.CompletedFuture;

import seco.ThisNiche;
import seco.U;
import seco.notebook.util.IconManager;

public class PeerList extends JPanel
{
    private static final long serialVersionUID = 1L;

    private HyperGraphPeer thisPeer;
    private JList list;
    private PeerListModel peers = new PeerListModel();

    private Future<Boolean> openPeer(String peerName)
    {
        String configResource = U.getResourceContentAsString("seco/talk/default-peer-config.json");
        if (configResource == null)
            throw new RuntimeException(
                    "Unable to find default config seco/talk/default-peer-config.json");
        JSONReader reader = new JSONReader();
        Map<String, Object> peerConfig = getPart(reader.read(configResource));
        peerConfig.put("localDB", ThisNiche.hg.getLocation());
        peerConfig.put("peerName", peerName);
        thisPeer = new HyperGraphPeer(peerConfig);
        Future<Boolean> f = thisPeer.start("user", "pwd");

        thisPeer.addPeerPresenceListener(new PeerPresenceListener() {
            public void peerJoined(HGPeerIdentity target)
            {
                peers.thePeers.add(target);
                peers.fireChangeEvent();
            }

            public void peerLeft(HGPeerIdentity target)
            {
                peers.thePeers.remove(target);
                peers.fireChangeEvent();
            }
        });
        return f;
    }

    public PeerList()
    {
        setLayout(new BorderLayout());
        list = new JList(peers);
        list.setCellRenderer(new PeerItemRenderer());
        add(list, BorderLayout.CENTER);
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev)
            {
                startConnecting("seco");
            }
        });
        add(connectButton, BorderLayout.NORTH);
        MouseListener mouseListener = new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() == 2) 
                {
                    int index = list.locationToIndex(e.getPoint());
                    HGPeerIdentity id = peers.thePeers.get(index);
                    System.out.println("Open peer " + id);
                    openTalkPanel(id);
                 }
            }
        };
        list.addMouseListener(mouseListener);                
    }

    public void openTalkPanel(HGPeerIdentity friend)
    {
        TalkPanel existing = U.hgetOne(hg.and(hg.type(TalkPanel.class), 
                                              hg.eq("friend", friend)));
        if (existing == null)
        {
            existing = new TalkPanel();
            existing.setFriend(friend);
            HGHandle h = ThisNiche.hg.add(existing);
        }
    }
    
    public Future<Boolean> startConnecting(String peerName)
    {
        if (isConnected())
            return new CompletedFuture<Boolean>(true);
        else
            return openPeer(peerName);
    }

    public boolean isConnected()
    {
        // TODO: wrong - could have no peers and still be connected!
        return thisPeer != null && !thisPeer.getConnectedPeers().isEmpty(); 
    }

    public void cancelConnection()
    {
        // TODO
    }

    public void disconnect()
    {
        // TODO
    }

    public HyperGraphPeer getThisPeer()
    {
        return thisPeer;
    }

    static class PeerListModel implements ListModel
    {
        private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
        ArrayList<HGPeerIdentity> thePeers = new ArrayList<HGPeerIdentity>();

        void fireChangeEvent()
        {
            ListDataEvent ev = new ListDataEvent(this,
                                                 ListDataEvent.CONTENTS_CHANGED, 
                                                 0, 
                                                 thePeers.size());
            for (ListDataListener l : listeners)
                l.contentsChanged(ev);
        }

        @Override
        public void addListDataListener(ListDataListener l)
        {
            listeners.add(l);
        }

        @Override
        public Object getElementAt(int index)
        {
            return thePeers.get(index);
        }

        @Override
        public int getSize()
        {
            return thePeers.size();
        }

        @Override
        public void removeListDataListener(ListDataListener l)
        {
            listeners.remove(l);
        }
    }
    
    static class PeerItemRenderer extends JLabel implements ListCellRenderer 
    {
        private static final long serialVersionUID = 9045908623545576595L;
        final static ImageIcon icon = new ImageIcon(IconManager.getIcon("seco/talk/peer-icon.jpg"));

        public Component getListCellRendererComponent(
          JList list,              // the list
          Object value,            // value to display
          int index,               // cell index
          boolean isSelected,      // is the cell selected
          boolean cellHasFocus)    // does the cell have focus
        {
            HGPeerIdentity id = (HGPeerIdentity)value;
            setText(id.getName());
            setIcon(icon);
            if (isSelected) 
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } 
            else 
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
    
}