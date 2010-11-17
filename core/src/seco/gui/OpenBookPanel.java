package seco.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookEditorKit;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class OpenBookPanel extends JPanel
{
    private JButton btnAdd;
    private JButton btnRemove;
    private JList list;
    private List<NotebookDocument> docs = null;

    /** Creates new form AddRemoveListPanel */
    public OpenBookPanel()
    {
        docs = hg.getAll(ThisNiche.graph, hg.type(NotebookDocument.class));
        filterDocs();
        initComponents();
        list.setListData(docs.toArray());
    }

    private void filterDocs()
    {
        docs.remove(NotebookEditorKit.getDefaultDocument());
        Set<HGHandle> opened = GUIHelper.getOpenedBooks();
        for (NotebookDocument doc : new LinkedList<NotebookDocument>(docs))
        {
            if (opened.contains(doc.getBookHandle())) docs.remove(doc);
        }
    }

    private void initComponents()
    {
        JScrollPane scroll = new JScrollPane();
        list = new JList();
        btnAdd = new JButton();
        btnRemove = new JButton();

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

        btnAdd.setText("Open");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                open(evt);
            }
        });

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        add(btnAdd, gbc);

        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                delete(evt);
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(btnRemove, gbc);

    }

    protected void delete(java.awt.event.ActionEvent evt)
    {
        if (list.getSelectedIndex() == -1) return;
        else
        {
            Object[] removed = list.getSelectedValues();
            for (int i = 0; i < removed.length; i++)
            {
                NotebookDocument nb = (NotebookDocument) removed[i];
                // remove the book from its parent,e.g. from canvas if it's
                // opened there
                CellGroup gr = CellUtils.getParentGroup(nb.getBookHandle());
                if (gr != null)
                    gr.remove((CellGroupMember) ThisNiche.graph.get(nb
                            .getBookHandle()));

                ThisNiche.graph.remove(ThisNiche.handleOf(nb), true);
                docs.remove(removed[i]);
            }
            list.setListData(docs.toArray());
        }
    }

    protected void open(java.awt.event.ActionEvent evt)
    {
        if (list.getSelectedIndex() == -1) return;
        Object[] removed = list.getSelectedValues();
        for (int i = 0; i < removed.length; i++)
        {
            NotebookDocument gr = (NotebookDocument) removed[i];
            GUIHelper.openNotebook(gr.getBookHandle());
            docs.remove(removed[i]);
        }
        list.setListData(docs.toArray());
    }
}
