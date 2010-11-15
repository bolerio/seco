package seco.gui.rtctx;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.notebook.gui.DialogDescriptor;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.NotifyDescriptor;
import seco.rtenv.NestedContextLink;
import seco.rtenv.RtU;
import seco.rtenv.RuntimeContext;

public class EditRuntimeContextDialog extends JDialog
{
    private static final long serialVersionUID = 7894500605980514017L;
    RuntimeContext top;

    public EditRuntimeContextDialog(RuntimeContext ctx)
    {
        super(ThisNiche.guiController.getFrame(), "Manage Runtime Context");
        top = ctx;
        getContentPane().add(new RtConfigPanel());
        setSize(480, 350);
    };

    public class RtConfigPanel extends JPanel
    {
        private static final long serialVersionUID = 8791197785571236892L;
        private ClassPathPanel cpPanel;
        private JTree tree;
        private JTextField textRtCtx;

        /** Creates new form RtConfigPanel */
        public RtConfigPanel()
        {
            initComponents();
        }

        private void initComponents()
        {
            JLabel jLabel1 = new JLabel("Context Name:");
            textRtCtx = new JTextField();
            textRtCtx.setEditable(false);
            JLabel jLabel2 = new JLabel("Context Tree");
            tree = new RtTree();
            tree.setCellRenderer(new RtCellRenderer());
            tree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e)
                {
                    RuntimeContext rc = (RuntimeContext) tree
                            .getLastSelectedPathComponent();
                    if (rc == null) return;
                    textRtCtx.setText(rc.getName());
                    cpPanel.setRuntimeContext(rc);
                }
            });
            cpPanel = new ClassPathPanel();
            JScrollPane jScrollPane1 = new JScrollPane(tree);
            setLayout(new GridBagLayout());
            JPanel jPanel1 = new JPanel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            add(jLabel1, gridBagConstraints);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.ipadx = 354;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            add(textRtCtx, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.NORTH;
            gridBagConstraints.insets = new Insets(15, 0, 0, 0);
            add(cpPanel, gridBagConstraints);

            jPanel1.setLayout(new GridBagLayout());

            jLabel2.setText("Context Tree");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            jPanel1.add(jLabel2, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.ipadx = 113;
            gridBagConstraints.ipady = 209;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            jPanel1.add(jScrollPane1, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.NORTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(15, 0, 0, 4);
            add(jPanel1, gridBagConstraints);
        }
    }

    public class RtTreeModel implements TreeModel
    {
        public Object getChild(Object parent, int index)
        {
            List<RuntimeContext> list = RtU.getChildContexts(ThisNiche
                    .handleOf(parent));
            return list.get(index);
        }

        public int getChildCount(Object parent)
        {
            return RtU.getChildContextLinks(ThisNiche.handleOf(parent)).size();
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            List<RuntimeContext> list = RtU.getChildContexts(ThisNiche
                    .handleOf(parent));
            return list.indexOf(child);
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

    private class RtTree extends JTree
    {
        private static final long serialVersionUID = 4796662500258622619L;

        public RtTree()
        {
            addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e)
                {
                    if (SwingUtilities.isRightMouseButton(e))
                    {
                        TreePath currentSelection = getSelectionPath();
                        TreePath selPath = getPathForLocation(e.getX(),
                                e.getY());
                        if (currentSelection != null
                                && currentSelection == selPath)
                        {
                            JPopupMenu popup = makePopupMenu();
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
            setModel(new RtTreeModel());
        }

        private JPopupMenu makePopupMenu()
        {
            final RuntimeContext ctx = (RuntimeContext) getLastSelectedPathComponent();
            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Add Runtime Context");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    Collection<RuntimeContext> ctx_list = RtU
                            .getAllRtContextsExcept(ctx);
                    if (ctx_list.isEmpty())
                    {
                        JOptionPane.showMessageDialog(
                                ThisNiche.guiController.getFrame(),
                                "No available runtime contexts.");
                        return;
                    }
                    JList list = new JList();
                    DefaultListModel m = new DefaultListModel();
                    for (RuntimeContext rc : ctx_list)
                        m.addElement(rc);
                    list.setModel(m);
                    list.setCellRenderer(new RtListCellRenderer());
                    list.setPreferredSize(new Dimension(200, 150));
                    // list.setBorder(new TitledBorder("Runtime Contexts"));
                    DialogDescriptor d = new DialogDescriptor(
                            ThisNiche.guiController.getFrame(),
                            new JScrollPane(list), "Select RuntimeContext:");
                    d.setModal(true);
                    d.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
                    d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION)
                    {
                        RuntimeContext new_ctx = (RuntimeContext) list
                                .getSelectedValue();
                        if (new_ctx != null)
                            ThisNiche.graph.add(new NestedContextLink(ThisNiche
                                    .handleOf(ctx), ThisNiche.handleOf(new_ctx)));
                        RtTree.this.setModel(new RtTreeModel());
                    }
                }
            });
            popup.add(item);
            item = new JMenuItem("Remove");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (GUIHelper
                            .showConfirmDlg("The relationships to this RuntimeContext will be permanently deleted. Are you sure?"))
                    {
                        List<NestedContextLink> list = RtU
                                .getChildContextLinks(ThisNiche.handleOf(ctx));
                        for (NestedContextLink l : list)
                            ThisNiche.graph.remove(ThisNiche.handleOf(l));
                        list = RtU.getParentContextLinks(ThisNiche
                                .handleOf(ctx));
                        for (NestedContextLink l : list)
                            ThisNiche.graph.remove(ThisNiche.handleOf(l));
                        RtTree.this.setModel(new RtTreeModel());
                    }
                }
            });
            popup.add(item);
            return popup;
        }
    }

    static class RtCellRenderer extends DefaultTreeCellRenderer
    {

        private static final long serialVersionUID = 4314887120842416201L;

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus)
        {

            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            adjustText(value);
            setIcon(null);
            return this;
        }

        private void adjustText(Object value)
        {
            if (value instanceof RuntimeContext)
            {
                RuntimeContext rc = (RuntimeContext) value;
                setText(rc.getName());
            }
        }
    }

    static class RtListCellRenderer extends DefaultListCellRenderer
    {
        private static final long serialVersionUID = -3342353797704430123L;

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            Component c = super.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
            setText(((RuntimeContext) value).getName());
            return c;
        }
    }
}
