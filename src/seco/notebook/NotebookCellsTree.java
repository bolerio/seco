package seco.notebook;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.EventPubSub;
import seco.notebook.gui.GUIUtilities;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;



public class NotebookCellsTree extends JTree
{
    public NotebookCellsTree(NotebookTreeModel model)
    {
        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    TreePath currentSelection = getSelectionPath();
                    TreePath selPath = getPathForLocation(e.getX(), e.getY());
                    if (currentSelection != null && currentSelection == selPath)
                    {
                        JPopupMenu popup = makePopupMenu();
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        this.setModel(model);
    }

    private JPopupMenu makePopupMenu()
    {
        final CellGroupMember node = (CellGroupMember) getLastSelectedPathComponent();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Publisher");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    JDialog dialog = new JDialog(GUIUtilities
                            .getFrame(NotebookCellsTree.this), "Publisher");
                    dialog.setSize(500, 800);
                    EventPubSubsPanel tree = new EventPubSubsPanel(ThisNiche
                            .handleOf(node), true);
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
                    EventPubSubsPanel tree = new EventPubSubsPanel(ThisNiche
                            .handleOf(node), false);
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
        //menuItem.setEnabled(node instanceof Cell);
        popup.add(menuItem);
        menuItem = new JMenuItem("Attributes"); // NOI18N
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                HGHandle h = ThisNiche.handleOf(node);
                CellGroupMember c = (CellGroupMember) ThisNiche.hg.get(h);
                System.out.println("Attributes: " + c.getAttributes());
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
                if(val instanceof Scriptlet)
                    val = ((Scriptlet) val).getCode();
                System.out.println("Value: " + val +
                        ":" + ThisNiche.handleOf(node));
            }
        });
        menuItem.setEnabled(node instanceof Cell);
        popup.add(menuItem);
        return popup;
    }

    class EventPubSubsPanel extends JPanel
    {
        private JButton Add;
        private JButton Remove;
        private JScrollPane jScrollPane1;
        private JList list;
        private Set<EventPubSub> docs = null;

        public EventPubSubsPanel(HGHandle h, boolean pub_or_sub)
        {
            docs = (pub_or_sub) ? getEventPubSubList(HGHandleFactory.anyHandle,
                    h, HGHandleFactory.anyHandle, HGHandleFactory.anyHandle)
                    : getEventPubSubList(HGHandleFactory.anyHandle,
                            HGHandleFactory.anyHandle, h,
                            HGHandleFactory.anyHandle);
            // for(EventPubSub s: reverse)
            // docs.add(s);
            // System.out.println("EventPubSubsPanel - elements: " +
            // docs.size());
            initComponents();
            list.setListData(docs.toArray());
        }

        Set<EventPubSub> getEventPubSubList(HGHandle eventType,
                HGHandle publisher, HGHandle subscriber, HGHandle listener)
        {
            HGHandle pub_or_sub = HGHandleFactory.anyHandle.equals(publisher) ? subscriber
                    : publisher;
            return CellUtils.findAll(ThisNiche.hg, hg.apply(hg.deref(ThisNiche.hg), hg
                    .and(hg.type(EventPubSub.class), hg.incident(pub_or_sub),
                            hg.orderedLink(new HGHandle[] { eventType,
                                    publisher, subscriber, listener }))));
        }

        private void initComponents()
        {
            java.awt.GridBagConstraints gridBagConstraints;

            jScrollPane1 = new JScrollPane();
            list = new JList();
            Add = new JButton();
            Remove = new JButton();

            setLayout(new GridBagLayout());

            setMinimumSize(new Dimension(300, 100));
            jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 80));
            jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 300));
            list.setMaximumSize(new java.awt.Dimension(1000, 800));
            jScrollPane1.setViewportView(list);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridheight = 6;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(jScrollPane1, gridBagConstraints);

             Add.setText("Inspect");
             Add.addActionListener(new java.awt.event.ActionListener()
             {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 Object[] removed = list.getSelectedValues();
                 for (int i = 0; i < removed.length; i++)
                 {
                     EventPubSub eps = (EventPubSub) removed[i];
                     System.out.println(eps);
                     System.out.println("Pub: " + ThisNiche.hg.get(eps.getPublisher()));
                     System.out.println("Sub: " + ThisNiche.hg.get(eps.getSubscriber()));
                     System.out.println("Handler: " + ThisNiche.hg.get(eps.getEventHandler()));
                 }
             }
             });

             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints.anchor =
             java.awt.GridBagConstraints.SOUTHEAST;
             add(Add, gridBagConstraints);

            Remove.setText("Remove");
            Remove.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                    delete(evt);
                }
            });

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
            add(Remove, gridBagConstraints);

        }

        protected void delete(java.awt.event.ActionEvent evt)
        {
            if (list.getSelectedIndex() == -1) return;
            else
            {
                Object[] removed = list.getSelectedValues();
                for (int i = 0; i < removed.length; i++)
                {
                    // EventPubSub gr = (EventPubSub) removed[i];
                    ThisNiche.hg.remove(ThisNiche.handleOf(removed[i]));
                    docs.remove(removed[i]);
                }
                list.setListData(docs.toArray());
            }
        }

    }
}
