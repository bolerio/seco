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
import seco.things.CellUtils;
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
        if(CellUtils.isMinimized(element))
            return GUIHelper.getMinimizedUI(element);
            
        HGHandle h = null;
        if(element instanceof Cell) 
        {
           Cell cell = (Cell) element;
           if(cell.getValue() instanceof CellGroupMember)
               h = cell.getAtomHandle();
           else
               h = ThisNiche.handleOf(element);
        }else
           h = ThisNiche.handleOf(element); 
        final NotebookUI ui = new NotebookUI(h);
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
                   TopFrame.getInstance().getCurrentRuntimeContext()));
           ctxH = ThisNiche.getContextHandleFor(doc.getBookHandle());
        }
        TopFrame.setCurrentRuntimeContext(ctxH);
        scrollPane.setName(TabbedPaneU.makeTabTitle(doc.getTitle()));
        scrollPane.updateUI();
        return scrollPane;
    }

}
