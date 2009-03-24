package seco.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.rtenv.ContextLink;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class NBUIVisual implements CellVisual
{
    private static final HGPersistentHandle handle = 
        HGHandleFactory.makeHandle("e870f4b0-13c7-11de-8c30-0800200c9a66");
    
    public static HGPersistentHandle getHandle()
    {
        return handle;
    }
    
    public JComponent bind(CellGroupMember element)
    {
        Cell cell = (Cell) element;
        final NotebookUI ui = new NotebookUI(cell.getAtomHandle());
        final NotebookDocument doc = ui.getDoc();
        if (TopFrame.getInstance().getCaretListener() != null)
            ui.addCaretListener(TopFrame.getInstance().getCaretListener());
        
        final JScrollPane scrollPane = new JScrollPane(ui);
        scrollPane.setDoubleBuffered(!TopFrame.PICCOLO);
        scrollPane.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce)
            {
                ui.requestFocusInWindow();
            }
        });
        // if(scrollPane.isVisible())
        scrollPane.setViewportView(ui);
        HGHandle ctxH = ThisNiche.getContextHandleFor(doc.getBookHandle());
        if(ThisNiche.TOP_CONTEXT_HANDLE.equals(ctxH))
        {
           ThisNiche.hg.add(new ContextLink(doc.getBookHandle(), 
                   TopFrame.getCurrentEvaluationContext()));
           ctxH = ThisNiche.getContextHandleFor(doc.getBookHandle());
        }
        TopFrame.setCurrentEvaluationContext(ctxH);
        scrollPane.setName(TabbedPaneU.makeTabTitle(doc.getTitle()));
        //JPanel outer = new JPanel();
        //outer.setName(scrollPane.getName());
        //outer.setLayout(new java.awt.BorderLayout());
       // outer.add(scrollPane, java.awt.BorderLayout.CENTER);
        return scrollPane;
    }

}
