package seco.talk;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.Occupant;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.notebook.NotebookEditorKit;
import seco.notebook.NotebookUI;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.UpdatablePopupMenu;

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
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
                {
                    if ((getList().getSelectedValue() instanceof HostedRoom))
                        return;
                    if (PeerList.this.getPopup().isVisible()) popupMenu
                            .setVisible(false);
                    else
                    {
                        popupMenu.update();
                        Frame f = GUIUtilities.getFrame(e.getComponent());
                        Point pt = getPoint(e, f);
                        popupMenu.show(f, pt.x, pt.y);
                    }
                    return;
                }

                if (e.getClickCount() == 2
                        && !SwingUtilities.isRightMouseButton(e))
                {
                    int index = getList().locationToIndex(e.getPoint());
                    if (index < 0 || index >= getList().getModel().getSize())
                        return;
                    Object x = getList().getModel().getElementAt(index);
                    ConnectionContext ctx = ConnectionManager
                            .getConnectionContext(getPeerID());
                    if (ctx == null) return;
                    if (x instanceof HGPeerIdentity) ctx
                            .openTalkPanel((HGPeerIdentity) x);
                    else if (x instanceof HostedRoom) ctx
                            .openChatRoom((HostedRoom) x);
                    else if (x instanceof Occupant)
                        ctx.openTalkPanel((Occupant) x);
                }
            }

            protected Point getPoint(MouseEvent e, Frame f)
            {
                Point pt = SwingUtilities.convertPoint(e.getComponent(), e
                        .getX(), e.getY(), f);
                if (e.getComponent() instanceof JComponent)
                    return GUIHelper.computePoint(
                            (JComponent) e.getComponent(), pt);
                return pt;
            }

        };
    }

    protected UpdatablePopupMenu popupMenu;

    private UpdatablePopupMenu getPopup()
    {
        if (popupMenu != null) return popupMenu;

        popupMenu = new UpdatablePopupMenu();
        JMenuItem mi = new JMenuItem(new AbstractAction() {
            @Override
            public boolean isEnabled()
            {
                Object x = getList().getSelectedValue();
                if (!(x instanceof Occupant)) return false;
                ConnectionContext ctx = ConnectionManager
                        .getConnectionContext(getPeerID());
                return !ctx.isMe((Occupant) x) && !ctx.isInRoster((Occupant) x);// ctx.getPeerIdentity((Occupant)
                                                                                // x)
                                                                                // ==
                                                                                // null;
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                ConnectionContext ctx = ConnectionManager
                        .getConnectionContext(getPeerID());
                ctx.addRoster((Occupant) getList().getSelectedValue());
            }

        });
        mi.setText("Add To Roaster");
        popupMenu.add(mi);

        mi = new JMenuItem(new AbstractAction() {
            @Override
            public boolean isEnabled()
            {
                Object x = getList().getSelectedValue();
                ConnectionContext ctx = ConnectionManager
                        .getConnectionContext(getPeerID());
                if (x instanceof Occupant) return !ctx.isMe((Occupant) x)
                        && ctx.isInRoster((Occupant) x);// ctx.getPeerIdentity((Occupant)
                                                        // x) != null;
                else if (x instanceof HGPeerIdentity)
                    return !ctx.isMe((HGPeerIdentity) x)
                            && ctx.isInRoster((HGPeerIdentity) x);
                return false;
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object x = getList().getSelectedValue();
                ConnectionContext ctx = ConnectionManager
                        .getConnectionContext(getPeerID());
                if (x instanceof Occupant) ctx.removeRoster((Occupant) x);
                else if (x instanceof HGPeerIdentity)
                    ctx.removeRoster((HGPeerIdentity) x);
            }
        });
        mi.setText("Remove From Roaster");
        popupMenu.add(mi);

        return popupMenu;
    }

    public PeerList(HGPeerIdentity peerID)
    {
        this();
        this.peerID = peerID;
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
            if (data.contains(obj)) return;
            //no equals() defined in HostedRoom 
            if (obj instanceof HostedRoom)
                for (Object o : data)
                    if (o instanceof HostedRoom
                            && ((HostedRoom) o).getJid().equals(
                                    ((HostedRoom) obj).getJid())) return;
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