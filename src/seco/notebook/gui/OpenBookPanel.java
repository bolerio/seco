package seco.notebook.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

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
        //filter at least those doc that are opened in TabbedPane 
        CellGroup tp = ThisNiche.hg.get(ThisNiche.TABBED_PANE_GROUP_HANDLE);
        for(int i = 0; i < tp.getArity(); i++)
        {
            HGHandle bookH = tp.getTargetAt(i);
            for(NotebookDocument doc: new ArrayList<NotebookDocument>(docs))
               if(doc.getBookHandle().equals(bookH))
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
            for(int i=0; i < removed.length; i++)
            {
               NotebookDocument gr = (NotebookDocument) removed[i];
               ThisNiche.hg.remove(ThisNiche.handleOf(gr));
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
