package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
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
    private HGPeerIdentity peerID;

    public PeerList()
    {
        mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int index = getList().locationToIndex(e.getPoint());
                    if (index < 0 || index >= getList().getModel().getSize())
                        return;
                    Object x = getList().getModel().getElementAt(index);
                    ConnectionPanel connectionPanel = 
                        ConnectionManager.getConnectionPanel(peerID);
                    if(connectionPanel == null) return;
                    if (x instanceof HGPeerIdentity) 
                        connectionPanel.openTalkPanel((HGPeerIdentity) x);
                    else
                        connectionPanel.openChatRoom((HostedRoom) x);
                }
            }
        };
    }

    public void initComponents()
    {
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setList(new JList(new PeerListModel()));
        list.setCellRenderer(new PeerItemRenderer());
        add(list, BorderLayout.CENTER);
    }

    public JList getList()
    {
        if (list == null)
        {
            list = (JList) getComponent(0);
            list.setCellRenderer(new PeerItemRenderer());
            list.addMouseListener(mouseListener);
        }
        return list;
    }

    public PeerListModel getListModel()
    {
        return (PeerListModel) getList().getModel();
    }

    public void setList(JList l)
    {
        this.list = l;
        list.setCellRenderer(new PeerItemRenderer());
        list.addMouseListener(mouseListener);

    }

    public static class PeerListModel extends AbstractListModel
    {
        private Vector<Object> data = new Vector<Object>();

        public int getSize()
        {
            return data.size();
        }

        public Object getElementAt(int index)
        {
            return data.elementAt(index);
        }

        public int size()
        {
            return data.size();
        }

        public boolean isEmpty()
        {
            return data.isEmpty();
        }

        public boolean contains(Object elem)
        {
            return data.contains(elem);
        }

        public int indexOf(Object elem)
        {
            return data.indexOf(elem);
        }

        public Object elementAt(int index)
        {
            return data.elementAt(index);
        }

        public void addElement(Object obj)
        {
            int index = data.size();
            data.addElement(obj);
            fireIntervalAdded(this, index, index);
        }

        public boolean removeElement(Object obj)
        {
            int index = indexOf(obj);
            boolean rv = data.removeElement(obj);
            if (index >= 0)
            {
                fireIntervalRemoved(this, index, index);
            }
            return rv;
        }

        public void removeAllElements()
        {
            int index1 = data.size() - 1;
            data.removeAllElements();
            if (index1 >= 0)
            {
                fireIntervalRemoved(this, 0, index1);
            }
        }

        public String toString()
        {
            return data.toString();
        }
    }

    public HGPeerIdentity getPeerID()
    {
        return peerID;
    }

    public void setPeerID(HGPeerIdentity peerID)
    {
        this.peerID = peerID;
    }
}