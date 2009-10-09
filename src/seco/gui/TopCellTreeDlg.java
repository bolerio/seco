package seco.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.EventPubSub;
import seco.notebook.gui.GUIUtilities;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;

public class TopCellTreeDlg extends JDialog
{
    protected CellGroupMember top;
    protected JEditorPane output;
    
    public TopCellTreeDlg(CellGroupMember cell)
    {
        super(TopFrame.getInstance(), false);
        this.top = cell;
        String name = CellUtils.getName(cell);
        if (name == null) name = "";
        setTitle("Cells Hierarchy: " + name);
        setSize(700, 800);
        output = new JEditorPane();
        output.setEditable(false);
        JTree tree = new NotebookCellsTree();
        JScrollPane pane = new JScrollPane(tree);
        JScrollPane output_pane = new JScrollPane(output);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                output_pane);
        split.setResizeWeight(0.6);
        add(split);
    }
    
    private void out(String text)
    {
        output.setText(text);
    }

    public class NotebookTreeModel implements TreeModel
    {
        public Object getChild(Object parent, int index)
        {
            if (parent instanceof CellGroup) 
                return ((CellGroup) parent).getElement(index);
            else if (parent instanceof Cell)
            {
                List<Cell> list = CellUtils.getOutCells(ThisNiche.handleOf(parent));
                return list.get(index);
            }
            return null;
        }

        public int getChildCount(Object parent)
        {
            if (parent instanceof CellGroup) return ((CellGroup) parent).getArity();
            else if (parent instanceof Cell) return CellUtils.getOutCells(
                    ThisNiche.handleOf(parent)).size();
            else
                return 0;
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            if (!(parent instanceof CellGroup))
            {
                List<Cell> list = CellUtils.getOutCells(ThisNiche.handleOf(parent));
                for (int i = 0; i < list.size(); i++)
                    if (child.equals(list.get(i))) return i;
                return -1;
            }
            return ((CellGroup) parent).indexOf((CellGroupMember) child);
        }

        public Object getRoot()
        {
            return top;
        }

        public boolean isLeaf(Object node)
        {
            return getChildCount(node) == 0;
        }

        public void addTreeModelListener(TreeModelListener l)
        {
        }

        public void removeTreeModelListener(TreeModelListener l)
        {
        }

        public void valueForPathChanged(TreePath path, Object newValue)
        {
        }
    }

    private class NotebookCellsTree extends JTree
    {
        NotebookCellsTree()
        {
            addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e)
                {
                    if (SwingUtilities.isRightMouseButton(e))
                    {
                        TreePath currentSelection = getSelectionPath();
                        TreePath selPath = getPathForLocation(e.getX(), e
                                .getY());
                        if (currentSelection != null
                                && currentSelection == selPath)
                        {
                            JPopupMenu popup = makePopupMenu();
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
            setModel(new NotebookTreeModel());
        }

        private JPopupMenu makePopupMenu()
        {
            final CellGroupMember node = (CellGroupMember) getLastSelectedPathComponent();
            TreePath selPath = getSelectionModel().getSelectionPath();
            CellGroupMember par = null;
            if (selPath != null && selPath.getParentPath() != null) par = (CellGroupMember) selPath
                    .getParentPath().getLastPathComponent();
            else
                par = null;
            JPopupMenu popup = new JPopupMenu();
            if (node instanceof Cell)
            {
                JMenuItem item = new JMenuItem("Print Atom");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        Cell cell = (Cell) node;
                        String s = "Cell handle="
                                + ThisNiche.hg.getHandle(cell) + "\n"
                                + "Cell atom handle=" + cell.getAtomHandle()
                                + "\n" + "Cell atom " + cell.getValue();
                        out(s);
                    }
                });
                popup.add(item);
            }
            JMenuItem menuItem = new JMenuItem("Publisher");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        JDialog dialog = new JDialog(GUIUtilities
                                .getFrame(NotebookCellsTree.this), "Publisher");
                        dialog.setSize(500, 800);
                        EventPubSubsPanel tree = new EventPubSubsPanel(
                                ThisNiche.handleOf(node), true);
                        JScrollPane pane = new JScrollPane(tree);
                        dialog.add(pane);
                        dialog.setVisible(true);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
            popup.add(menuItem);
            menuItem = new JMenuItem("Subscriber"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        JDialog dialog = new JDialog(GUIUtilities
                                .getFrame(NotebookCellsTree.this), "Subscriber");
                        dialog.setSize(500, 800);
                        EventPubSubsPanel tree = new EventPubSubsPanel(
                                ThisNiche.handleOf(node), false);
                        JScrollPane pane = new JScrollPane(tree);
                        dialog.add(pane);
                        dialog.setVisible(true);

                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
            // menuItem.setEnabled(node instanceof Cell);
            popup.add(menuItem);
            menuItem = new JMenuItem("Attributes"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    HGHandle h = ThisNiche.handleOf(node);
                    CellGroupMember c = (CellGroupMember) ThisNiche.hg.get(h);
                    String s = "Attributes: \n";
                    for(Map.Entry<Object, Object> n : c.getAttributes().entrySet())
                        s += "" + n.getKey() + " = " + n.getValue() + "\n";
                    out(s);
                }
            });
            popup.add(menuItem);
            menuItem = new JMenuItem("Value"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    HGHandle h = ThisNiche.handleOf(node);
                    Cell c = (Cell) ThisNiche.hg.get(h);
                    Object val = c.getValue();
                    if (val instanceof Scriptlet)
                        val = ((Scriptlet) val).getCode();
                    out("Value: " + val + ":" + ThisNiche.handleOf(node) + ":"
                            + val.getClass());
                }
            });
            menuItem.setEnabled(node instanceof Cell);
            popup.add(menuItem);
            menuItem = new JMenuItem("Remove"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    HGHandle h = ThisNiche.handleOf(node);
                    CellGroupMember c = ThisNiche.hg.get(h);
                    CellGroup group = CellUtils.getParentGroup(h);
                    if (group != null) group.remove(c);

                    // TODO: rather brutal refresh, but the remove operation
                    // is not expected to be very frequent
                    setModel(new NotebookTreeModel());
                }
            });
            popup.add(menuItem);
            return popup;
        }
    }

    private class EventPubSubsPanel extends JPanel
    {
        private JList list;
        private List<EventPubSub> docs = null;

        public EventPubSubsPanel(HGHandle h, boolean pub_or_sub)
        {
            docs = (pub_or_sub) ? getEventPubSubList(HGHandleFactory.anyHandle,
                    h, HGHandleFactory.anyHandle, HGHandleFactory.anyHandle)
                    : getEventPubSubList(HGHandleFactory.anyHandle,
                            HGHandleFactory.anyHandle, h,
                            HGHandleFactory.anyHandle);
            initComponents();
            list.setListData(docs.toArray());
        }

        List<EventPubSub> getEventPubSubList(HGHandle eventType,
                HGHandle publisher, HGHandle subscriber, HGHandle listener)
        {
            HGHandle pub_or_sub = HGHandleFactory.anyHandle.equals(publisher) ? subscriber
                    : publisher;
            return hg.getAll(ThisNiche.hg, hg.and(hg.type(EventPubSub.class),
                    hg.incident(pub_or_sub), hg.orderedLink(new HGHandle[] {
                            eventType, publisher, subscriber, listener })));
        }

        private void initComponents()
        {
            JScrollPane scroll = new JScrollPane();
            list = new JList();
            JButton addBtn = new JButton();
            JButton removeBtn = new JButton();

            setLayout(new GridBagLayout());

            setMinimumSize(new Dimension(300, 100));
            scroll.setMinimumSize(new Dimension(400, 80));
            scroll.setPreferredSize(new Dimension(400, 300));
            list.setMaximumSize(new Dimension(1000, 800));
            scroll.setViewportView(list);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridheight = 6;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            add(scroll, gbc);

            addBtn.setText("Inspect");
            addBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt)
                {
                    Object[] removed = list.getSelectedValues();
                    for (int i = 0; i < removed.length; i++)
                    {
                        EventPubSub eps = (EventPubSub) removed[i];
                        String s = "" + eps + "\n" +
                        "Pub: " + ThisNiche.hg.get(eps.getPublisher()) + "\n" +
                        "Sub: " + ThisNiche.hg.get(eps.getSubscriber()) + "\n" +
                        "Handler: " + ThisNiche.hg.get(eps.getEventHandler());
                        TopCellTreeDlg.this.out(s);
                    }
                }
            });

            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.SOUTHEAST;
            add(addBtn, gbc);

            removeBtn.setText("Remove");
            removeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt)
                {
                    delete(evt);
                }
            });

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            add(removeBtn, gbc);
        }

        protected void delete(ActionEvent evt)
        {
            if (list.getSelectedIndex() == -1) return;
            Object[] removed = list.getSelectedValues();
            for (int i = 0; i < removed.length; i++)
            {
                ThisNiche.hg.remove(ThisNiche.handleOf(removed[i]), true);
                docs.remove(removed[i]);
            }
            list.setListData(docs.toArray());
        }
    }
}
