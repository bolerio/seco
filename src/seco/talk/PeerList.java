package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.jivesoftware.smackx.muc.HostedRoom;

public class PeerList extends JPanel
{
    private static final long serialVersionUID = 1L;
    private transient MouseListener mouseListener;
    @HGIgnore
    private JList list;
    @HGIgnore
    private ConnectionPanel connectionPanel;

    public PeerList()
    {
        mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int index = getList().locationToIndex(e.getPoint());
                    Object x = getList().getModel().getElementAt(index);
                    if (x instanceof HGPeerIdentity) getConnectionPanel()
                            .openTalkPanel((HGPeerIdentity) x);
                    else
                        getConnectionPanel().openChatRoom((HostedRoom) x);
                }
            }
        };
    }
    
    public void initComponents()
    {
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setList(new JList(new DefaultListModel()));
        list.setCellRenderer(new PeerItemRenderer());
        add(list, BorderLayout.CENTER);
    }

    public ConnectionPanel getConnectionPanel()
    {
        return connectionPanel;
    }

    public void setConnectionPanel(ConnectionPanel connectionPanel)
    {
        this.connectionPanel = connectionPanel;
    }

    public JList getList()
    {
        if(list == null)
        {
            list = (JList) getComponent(0);
            list.setCellRenderer(new PeerItemRenderer());
            list.addMouseListener(mouseListener);
        }
        return list;
    }
    
    public DefaultListModel getListModel()
    {
        return (DefaultListModel) getList().getModel();
    }

    public void setList(JList l)
    {
        this.list = l;
        list.setCellRenderer(new PeerItemRenderer());
        list.addMouseListener(mouseListener);
        
    }
    
//    public static class PeerListModel implements ListModel
//    {
//        private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
//        ArrayList<Object> thePeers = new ArrayList<Object>();
//
//        void fireChangeEvent()
//        {
//            ListDataEvent ev = new ListDataEvent(this,
//                    ListDataEvent.CONTENTS_CHANGED, 0, thePeers.size());
//            for (ListDataListener l : listeners)
//                l.contentsChanged(ev);
//        }
//
//        public void addListDataListener(ListDataListener l)
//        {
//            listeners.add(l);
//        }
//
//        public Object getElementAt(int index)
//        {
//            return thePeers.get(index);
//        }
//
//        public int getSize()
//        {
//            return thePeers.size();
//        }
//
//        public void removeListDataListener(ListDataListener l)
//        {
//            listeners.remove(l);
//        }
//        
//        public ListDataListener[] getListDataListeners() {
//            return listeners.toArray(
//                    new ListDataListener[listeners.size()]);
//        }
//
//        public ArrayList<Object> getThePeers()
//        {
//            return thePeers;
//        }
//
//        public void setThePeers(ArrayList<Object> thePeers)
//        {
//            this.thePeers = thePeers;
//        }
//    }
}