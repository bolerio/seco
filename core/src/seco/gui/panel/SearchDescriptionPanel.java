package seco.gui.panel;

/*
 * SearchDescriptionPanel.java
 *
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.notebook.NotebookUI;
import seco.things.DescriptionLink;
import seco.util.GUIUtil;

public class SearchDescriptionPanel extends javax.swing.JPanel
{
    private static final long serialVersionUID = 6681375510410865074L;
    private static int MAX_CHARS_IN_LIST = 30;
    private JButton buttSearch;
    private JList descrList;
    private JComponent previewUI;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;
    private JTextField textSearch;
    private DescrModel list_model;
    private JPopupMenu popupMenu = new JPopupMenu();;


    /** Creates new form SearchDescriptionPanel */
    public SearchDescriptionPanel()
    {
        initComponents();
    }

    private void initComponents()
    {

        JLabel jLabel1 = new JLabel("Search for:");
        textSearch = new JTextField();
        textSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    list_model.filter();
                    e.consume();
                }
            }
        });
        buttSearch = new JButton("GO");
        splitPane = new JSplitPane();
        scrollPane = new JScrollPane();
        descrList = new JList();
        descrList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me)
            {
                if (SwingUtilities.isRightMouseButton(me)
                        && !descrList.isSelectionEmpty()
                        && descrList.locationToIndex(me.getPoint()) == descrList
                                .getSelectedIndex())
                {
                    popupMenu.show(descrList, me.getX(), me.getY());
                }
            }
        });
        JMenuItem mi = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                String desc = ThisNiche.graph.get(list_model.links.get(
                        descrList.getSelectedIndex()).getDescriptionHandle());
                JTextArea area = new JTextArea();
                area.setPreferredSize(new Dimension(300, 200));
                DialogDescriptor dd = new DialogDescriptor( area,
                        "Cell/Group Description");
                area.setText(desc);
                DialogDisplayer.getDefault().notify(dd);

            }
        });
        mi.setText("Show Full Description");
        popupMenu.add(mi);
        mi = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                list_model.deleteSelection();

            }
        });
        mi.setText("Delete Description");
        popupMenu.add(mi);

        buttSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                list_model.filter();
            }
        });

        descrList.setModel(list_model = new DescrModel());
        descrList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting()) return;
                if (descrList.getSelectedIndex() < 0)
                {
                    set_empty_panel();
                    return;
                }
               
                //set_empty_panel();
                previewUI = new JScrollPane(new NotebookUI(list_model.links
                        .get(descrList.getSelectedIndex()).getCellHandle()));
                previewUI.setBorder(new TitledBorder("Preview"));
                splitPane.setRightComponent(previewUI);
                splitPane.setDividerLocation(0.4);
            }
        });

        scrollPane.setViewportView(descrList);
        scrollPane.setBorder(new TitledBorder("Description"));

        splitPane.setLeftComponent(scrollPane);

        set_empty_panel();

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout
                .setHorizontalGroup(layout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                layout
                                                        .createParallelGroup(
                                                                GroupLayout.Alignment.LEADING)
                                                        .addComponent(
                                                                splitPane,
                                                                GroupLayout.Alignment.TRAILING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                485,
                                                                Short.MAX_VALUE)
                                                        .addGroup(
                                                                layout
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                jLabel1)
                                                                        .addPreferredGap(
                                                                                LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                textSearch,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                372,
                                                                                Short.MAX_VALUE)
                                                                        .addPreferredGap(
                                                                                LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                buttSearch)))));
        layout.setVerticalGroup(layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addGroup(
                        layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE).addComponent(
                                jLabel1).addComponent(textSearch,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE).addComponent(
                                buttSearch)).addGap(18, 18, 18).addComponent(
                        splitPane, GroupLayout.DEFAULT_SIZE, 334,
                        Short.MAX_VALUE).addContainerGap()));
        
        splitPane.setDividerLocation(300);
    }
    
    

    private void set_empty_panel()
    {
        previewUI = new JPanel();
        GroupLayout l = new GroupLayout(previewUI);
        previewUI.setLayout(l);
        l.setHorizontalGroup(l.createParallelGroup(
                GroupLayout.Alignment.LEADING).addGap(0, 441, Short.MAX_VALUE));
        l.setVerticalGroup(l.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 332, Short.MAX_VALUE));
        previewUI.setBorder(new TitledBorder("Preview"));
        splitPane.setRightComponent(previewUI);
        splitPane.setDividerLocation(0.4);
    }

    private class DescrModel extends AbstractListModel
    {
        String srch;
        List<DescriptionLink> all_links;
        List<DescriptionLink> links = new ArrayList<DescriptionLink>();

        public DescrModel()
        {
            super();
            all_links = hg.getAll(ThisNiche.graph, hg
                    .type(DescriptionLink.class));
            for (DescriptionLink l : all_links)
                links.add(l);
        }

        private void filter()
        {
            if ((srch != null && srch.equals(textSearch.getText()))
                    || (srch == null && textSearch.getText() == null)) return;
            srch = textSearch.getText();
            links.clear();
            for (DescriptionLink l : all_links)
            {
                String d = ThisNiche.graph.get(l.getDescriptionHandle());
                if (d.indexOf(srch) > -1) links.add(l);
            }
            fireContentsChanged(this, 0, links.size() - 1);
            if(links.size() == 0)
                set_empty_panel();
        }

        void deleteSelection()
        {
            HGHandle h = ThisNiche.handleOf(links.get(descrList
                    .getSelectedIndex()));
            all_links.remove(descrList.getSelectedIndex());
            links.remove(descrList.getSelectedIndex());
            ThisNiche.graph.remove(h);
            fireContentsChanged(this, 0, links.size() - 1);
        }

        public int getSize()
        {
            return links.size();
        }

        public Object getElementAt(int i)
        {
            String res = ThisNiche.graph.get(links.get(i)
                    .getDescriptionHandle());
            return res.length() > MAX_CHARS_IN_LIST ? res.substring(0, MAX_CHARS_IN_LIST) + "..." : res;
        }
    }
}
