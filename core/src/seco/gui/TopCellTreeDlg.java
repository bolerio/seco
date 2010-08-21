package seco.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.boot.NicheBootListener;
import seco.events.EventPubSub;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.OpenBookPanel;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;

public class TopCellTreeDlg extends JDialog
{
    protected CellGroupMember top;
    protected JTextArea output;

    public TopCellTreeDlg(CellGroupMember cell)
    {
        super(ThisNiche.guiController.getFrame(), false);
        this.top = cell;
        String name = CellUtils.getName(cell);
        if (name == null) name = "";
        setTitle("Cells Hierarchy: " + name);
        setSize(700, 800);
        output = new JTextArea();
        output.setEditable(false);
        output.setBackground(Color.white);
        JTree tree = new NotebookCellsTree();
        JScrollPane pane = new JScrollPane(tree);
        JScrollPane output_pane = new JScrollPane(output);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                output_pane);
        split.setResizeWeight(0.6);
        add(split);
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING
                && NicheBootListener.DEBUG_NICHE) System.exit(0);
        super.processWindowEvent(e);
    }

    private void out(String text)
    {
        output.setText(text);
    }

    public class NotebookTreeModel implements TreeModel
    {
        public Object getChild(Object parent, int index)
        {
            if (parent instanceof CellGroup) return ((CellGroup) parent)
                    .getElement(index);
            else if (parent instanceof Cell)
            {
                List<Cell> list = CellUtils.getOutCells(ThisNiche
                        .handleOf(parent));
                return list.get(index);
            }
            return null;
        }

        public int getChildCount(Object parent)
        {
            if (parent instanceof CellGroup) return ((CellGroup) parent)
                    .getArity();
            else if (parent instanceof Cell) return CellUtils.getOutCells(
                    ThisNiche.handleOf(parent)).size();
            else
                return 0;
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            if (!(parent instanceof CellGroup))
            {
                List<Cell> list = CellUtils.getOutCells(ThisNiche
                        .handleOf(parent));
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
            final CellGroupMember cgm = (CellGroupMember) getLastSelectedPathComponent();
            TreePath selPath = getSelectionModel().getSelectionPath();
            CellGroupMember par = null;
            if (selPath != null && selPath.getParentPath() != null) par = (CellGroupMember) selPath
                    .getParentPath().getLastPathComponent();
            else
                par = null;
            JPopupMenu popup = new JPopupMenu();
            if (cgm instanceof Cell)
            {
                JMenuItem item = new JMenuItem("Print Atom");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        Cell cell = (Cell) cgm;
                        String s = "Cell handle="
                                + ThisNiche.graph.getHandle(cell) + "\n"
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
                                ThisNiche.handleOf(cgm), true);
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
                                ThisNiche.handleOf(cgm), false);
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
                    String s = "Attributes: \n";
                    for (Map.Entry<Object, Object> n : cgm.getAttributes()
                            .entrySet())
                        s += "" + n.getKey() + " = " + n.getValue() + "\n";
                    out(s);
                }
            });
            popup.add(menuItem);

            menuItem = new JMenuItem("Delete Attribute"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    JDialog dialog = new JDialog(GUIUtilities.getFrame(
                            (Component)e.getSource()),
                            "Delete Attributes");
                    dialog.setSize(300, 200);
                    dialog.add(new RemoveAttribsPanel(cgm));
                    dialog.setVisible(true);
                }
            });
            popup.add(menuItem);

            menuItem = new JMenuItem("Value"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    Cell c = (Cell) cgm;
                    Object val = c.getValue();
                    if (val instanceof Scriptlet)
                        val = ((Scriptlet) val).getCode();
                    out("Value: " + val + ":" + ThisNiche.handleOf(cgm) + ":"
                            + ((val != null) ? val.getClass() : ""));
                }
            });
            menuItem.setEnabled(cgm instanceof Cell);
            popup.add(menuItem);
            menuItem = new JMenuItem("Remove"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    HGHandle h = ThisNiche.handleOf(cgm);
                    CellGroup group = CellUtils.getParentGroup(h);
                    if (group != null) group.remove(cgm);

                    // TODO: rather brutal refresh, but the remove operation
                    // is not expected to be very frequent
                    setModel(new NotebookTreeModel());
                }
            });
            popup.add(menuItem);
            
            menuItem = new JMenuItem("Get Handle In Clipboard"); // NOI18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    putInClipboard(ThisNiche.handleOf(cgm));
                }
            });
            popup.add(menuItem);

            return popup;
        }
        
        // This method writes a string to the system clipboard.
        // otherwise it returns null.
        private void putInClipboard(HGHandle h) {
            String str = "h  = org.hypergraphdb.HGHandleFactory.makeHandle(\"" +
            ThisNiche.graph.getPersistentHandle(h) + "\");";
            StringSelection ss = new StringSelection(str);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
        }
    }

    private class EventPubSubsPanel extends JPanel
    {
        private JList list;
        private List<EventPubSub> docs = null;

        public EventPubSubsPanel(HGHandle h, boolean pub_or_sub)
        {
            docs = (pub_or_sub) ? getEventPubSubList(ThisNiche.graph.getHandleFactory().anyHandle(),
                    h, ThisNiche.graph.getHandleFactory().anyHandle(), 
                    ThisNiche.graph.getHandleFactory().anyHandle())
                    : getEventPubSubList(ThisNiche.graph.getHandleFactory().anyHandle(),
                            ThisNiche.graph.getHandleFactory().anyHandle(), h,
                            ThisNiche.graph.getHandleFactory().anyHandle());
            initComponents();
            list.setListData(docs.toArray());
        }

        List<EventPubSub> getEventPubSubList(HGHandle eventType,
                HGHandle publisher, HGHandle subscriber, HGHandle listener)
        {
            HGHandle pub_or_sub = ThisNiche.graph.getHandleFactory().anyHandle().equals(publisher) ? subscriber
                    : publisher;
            return hg.getAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class),
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
                        String s = "" + eps + "\n" + "Pub: "
                                + ThisNiche.graph.get(eps.getPublisher()) + "\n"
                                + "Sub: "
                                + ThisNiche.graph.get(eps.getSubscriber()) + "\n"
                                + "Handler: "
                                + ThisNiche.graph.get(eps.getEventHandler());
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
                ThisNiche.graph.remove(ThisNiche.handleOf(removed[i]), true);
                docs.remove(removed[i]);
            }
            list.setListData(docs.toArray());
        }
    }

    class RemoveAttribsPanel extends JPanel
    {
        private JList list;
        private CellGroupMember cgm;

        public RemoveAttribsPanel(CellGroupMember cgm)
        {
            this.cgm = cgm;
            initComponents();
            list.setListData(cgm.getAttributes().keySet().toArray());
        }

        private void initComponents()
        {
            JScrollPane scroll = new JScrollPane();
            list = new JList();
            JButton btnRemove = new JButton();

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

            btnRemove.setText("Remove");
            btnRemove.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                    if (list.getSelectedIndex() == -1) return;
                    Object[] removed = list.getSelectedValues();
                    for (int i = 0; i < removed.length; i++)
                        cgm.getAttributes().remove(removed[i]);
                    list.setListData(cgm.getAttributes().keySet().toArray());
                }
            });

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            add(btnRemove, gbc);

        }
    }
}
