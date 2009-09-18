package seco.notebook.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
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
import seco.gui.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookEditorKit;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;



public class OpenBookPanel extends JPanel
{
    private JButton Add;
    private JButton Remove;
    private JScrollPane jScrollPane1;
    private JList list;
    private List<NotebookDocument> docs = null;
   
    /** Creates new form AddRemoveListPanel */
    public OpenBookPanel()
    {
        docs = hg.getAll(ThisNiche.hg, hg.type(NotebookDocument.class));
        filterDocs();
        initComponents();
        list.setListData(docs.toArray());
    }
    
    private void filterDocs()
    {
        docs.remove(NotebookEditorKit.getDefaultDocument());
        Set<HGHandle> opened = GUIHelper.getOpenedBooks();
        for(NotebookDocument doc: 
            new LinkedList<NotebookDocument>(docs))
        {
            if(opened.contains(doc.getBookHandle()))
               docs.remove(doc);
        }
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

        Add.setText("Open");
        Add.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                open(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        add(Add, gridBagConstraints);

        Remove.setText("Remove");
        Remove.addActionListener(new java.awt.event.ActionListener()
        {
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
        if(list.getSelectedIndex() == -1)
            return;
        else
        {
            Object[] removed = list.getSelectedValues();
            for(int i = 0; i < removed.length; i++)
            {
               NotebookDocument nb = (NotebookDocument) removed[i];
               //remove the book from its parent,e.g. from canvas if it's opened there  
               CellGroup gr = CellUtils.getParentGroup(nb.getBookHandle());
               if(gr != null)
                 gr.remove((CellGroupMember) ThisNiche.hg.get(nb.getBookHandle()));
               
               ThisNiche.hg.remove(ThisNiche.handleOf(nb), true);
               docs.remove(removed[i]);
            }
            list.setListData(docs.toArray());
        }
    }
    
    protected void open(java.awt.event.ActionEvent evt)
    {
        if(list.getSelectedIndex() == -1)
            return;
        Object[] removed = list.getSelectedValues();
        for(int i=0; i < removed.length; i++)
        {
            NotebookDocument gr = (NotebookDocument) removed[i];
            GUIHelper.openNotebook(gr.getBookHandle());
        }
    }    
}
