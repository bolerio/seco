package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.jivesoftware.smackx.muc.HostedRoom;

public class PeerList extends JPanel
{
    private static final long serialVersionUID = 1L;

    @HGIgnore
    private JList list;
    @HGIgnore
    PeerListModel peers = new PeerListModel(); 
    @AtomReference("symbolic")
    private ConnectionPanel connectionPanel;
    
    public void initComponents()
    {
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        list = new JList(peers);
        list.setCellRenderer(new PeerItemRenderer());
        add(list, BorderLayout.CENTER);
        
        MouseListener mouseListener = new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() == 2) 
                {
                    int index = list.locationToIndex(e.getPoint());
                    Object x = peers.thePeers.get(index);                    
                    if (x instanceof HGPeerIdentity)
                        connectionPanel.openTalkPanel((HGPeerIdentity)x);
                    else
                        connectionPanel.openChatRoom((HostedRoom)x);
                 }
            }
        };
        list.addMouseListener(mouseListener);        
    }
    
    
    static class PeerListModel implements ListModel
    {
        private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
        ArrayList<Object> thePeers = new ArrayList<Object>();

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


    public ConnectionPanel getConnectionPanel()
    {
        return connectionPanel;
    }


    public void setConnectionPanel(ConnectionPanel connectionPanel)
    {
        this.connectionPanel = connectionPanel;
    }    
}