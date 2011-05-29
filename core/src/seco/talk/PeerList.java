package seco.talk;

import java.awt.BorderLayout;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.annotation.HGIgnore;
import org.jivesoftware.smackx.muc.HostedRoom;

import seco.gui.menu.UpdatablePopupMenu;
import seco.util.GUIUtil;

public class PeerList extends JPanel
{
    private static final long serialVersionUID = 1L;
    private transient MouseListener mouseListener;
    @HGIgnore
    private JList list;
    @AtomReference("symbolic")
    protected ConnectionContext connectionContext;


    public PeerList()
    {
        mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
                {
                    Object val = getList().getSelectedValue();
                    if (val == null || val instanceof HostedRoom)
                        return;
                    if (connectionContext == null)
                        return;
                    // don't show popup on ME
                    if (val instanceof OccupantEx && connectionContext.isMe((OccupantEx) val))
                        return;

                    if (PeerList.this.getPopup().isVisible())
                        popupMenu.setVisible(false);
                    else
                    {
                        popupMenu.update();
                        Frame f = GUIUtil.getFrame(e.getComponent());
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
                    if (connectionContext == null)
                        return;
                    if (x instanceof String)
                        connectionContext.openTalkPanel((String) x);
                    else if (x instanceof HostedRoom)
                        connectionContext.openChatRoom((HostedRoom) x);
                    else if (x instanceof OccupantEx)
                        connectionContext.openTalkPanel((OccupantEx) x);
                }
            }

            protected Point getPoint(MouseEvent e, Frame f)
            {
                Point pt = SwingUtilities.convertPoint(e.getComponent(),
                                                       e.getX(),
                                                       e.getY(),
                                                       f);
                if (e.getComponent() instanceof JComponent)
                    return GUIUtil.adjustPointInPicollo((JComponent) e.getComponent(),
                                                        pt);
                return pt;
            }

        };
    }

    protected UpdatablePopupMenu popupMenu;

    private UpdatablePopupMenu getPopup()
    {
        if (popupMenu != null)
            return popupMenu;

        popupMenu = new UpdatablePopupMenu();
        JMenuItem mi = new JMenuItem(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled()
            {
                Object x = getList().getSelectedValue();
                if (!(x instanceof OccupantEx))
                    return false;
                return !connectionContext.isMe((OccupantEx) x)
                        && !connectionContext.isInRoster((OccupantEx) x);// ctx.getPeerIdentity((Occupant)
                // null;
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                connectionContext.addRoster((OccupantEx) getList().getSelectedValue());
            }

        });
        mi.setText("Add To Roaster");
        popupMenu.add(mi);

        mi = new JMenuItem(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled()
            {
                Object x = getList().getSelectedValue();
                if (x instanceof OccupantEx)
                    return !connectionContext.isMe((OccupantEx) x)
                            && connectionContext.isInRoster((OccupantEx) x);// ctx.getPeerIdentity((Occupant)
                                                              // x) != null;
                else if (x instanceof String)
                    return !connectionContext.isMe((String) x)
                            && connectionContext.isInRoster((String) x);
                return false;
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object x = getList().getSelectedValue();
                if (x instanceof OccupantEx)
                    connectionContext.removeRoster((OccupantEx) x);
                else if (x instanceof String)
                    connectionContext.removeRoster((String) x);
            }
        });
        mi.setText("Remove From Roaster");
        popupMenu.add(mi);

        return popupMenu;
    }

    public PeerList(ConnectionContext connectionContext)
    {
        this();
        this.connectionContext = connectionContext;
    }

    public void initComponents()
    {
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setList(new JList(new PeerListModel()));
        list.setCellRenderer(new PeerItemRenderer(this.connectionContext));
        add(list, BorderLayout.CENTER);
    }

    public JList getList()
    {
        if (list == null)
        {
            list = (JList) getComponent(0);
            list.setCellRenderer(new PeerItemRenderer(this.connectionContext));
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
        list.setCellRenderer(new PeerItemRenderer(this.connectionContext));
        list.addMouseListener(mouseListener);

    }

    public static class PeerListModel extends AbstractListModel
    {
        private static final long serialVersionUID = 1L;
        private List<Object> data = Collections.synchronizedList(new ArrayList<Object>());
 
        public int getSize()
        {
            return data.size();
        }

        public Object getElementAt(int index)
        {
            return data.get(index);
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
            return data.get(index);
        }

        public void addElement(Object obj)
        {
            if (data.contains(obj))
                return;
            // no equals() defined in HostedRoom
            if (obj instanceof HostedRoom)
                for (Object o : data)
                    if (o instanceof HostedRoom
                            && ((HostedRoom) o).getJid()
                                    .equals(((HostedRoom) obj).getJid()))
                        return;
            int index = data.size();
            data.add(obj);
            fireIntervalAdded(this, index, index);
        }

        public boolean removeElement(Object obj)
        {
            int index = indexOf(obj);
            boolean rv = data.remove(obj);
            if (index >= 0)
            {
                fireIntervalRemoved(this, index, index);
            }
            return rv;
        }

        public void removeAllElements()
        {
            int index1 = data.size() - 1;
            data.clear();
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

    public ConnectionContext getConnectionContext()
    {
        return connectionContext;
    }

    public void setConnectionContext(ConnectionContext connectionContext)
    {
        this.connectionContext = connectionContext;
    }
}