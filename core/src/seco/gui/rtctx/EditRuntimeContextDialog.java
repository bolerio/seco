package seco.gui.rtctx;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.NotifyDescriptor;
import seco.rtenv.NestedContextLink;
import seco.rtenv.RtU;
import seco.rtenv.RuntimeContext;
import seco.util.GUIUtil;

public class EditRuntimeContextDialog extends JDialog
{
    private static final long serialVersionUID = 7894500605980514017L;
    RuntimeContext top;

    public EditRuntimeContextDialog(RuntimeContext ctx)
    {
        super(GUIUtil.getFrame(), "Edit Runtime Context");
        if(GUIUtil.getFrame() == null) setIconImage(GUIHelper.LOGO_IMAGE);
        top = ctx;
        getContentPane().add(new RtConfigPanel());
        setSize(800, 600);
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
            setLayout(new GridBagLayout());
            
            JLabel jLabel1 = new JLabel("Context Name:");
        	GridBagConstraints gbc = new GridBagConstraints();
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(jLabel1, gbc);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            textRtCtx = new JTextField();
            textRtCtx.setEditable(false);            
            add(textRtCtx, gbc);
        	           
            cpPanel = new ClassPathPanel();
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

            JScrollPane jScrollPane1 = new JScrollPane(tree);
            JPanel jPanel1 = new JPanel(new GridBagLayout());
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            jPanel1.add(new JLabel("Context Tree"), gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.ipadx = 113;
            gbc.ipady = 209;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            jPanel1.add(jScrollPane1, gbc);

            tree.setSelectionPath(new TreePath(tree.getModel().getRoot()));
        	//Create a split pane with the two scroll panes in it.
        	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        								jPanel1, cpPanel);
        	//splitPane.setOneTouchExpandable(true);
        	
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.weighty = 1.0;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(15, 0, 0, 4);        	
            add(splitPane, gbc);
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0), "close");
            getActionMap().put("close", new AbstractAction() {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });            
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
                                GUIUtil.getFrame(),
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
                    if (GUIUtil
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
