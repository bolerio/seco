package seco.gui.visual;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.TabbedPaneU;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.util.GUIUtil;

/**
 * 
 * <p>
 * The {@link seco.things.CellVisual} of a complete Seco notebook. 
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class NBUIVisual implements CellVisual
{
    private static final HGPersistentHandle handle = 
        UUIDHandleFactory.I.makeHandle("e870f4b0-13c7-11de-8c30-0800200c9a66");
    
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
        }
        else
            h = ThisNiche.handleOf(element); 
        final NotebookUI ui = new NotebookUI(h);
        final NotebookDocument doc = ui.getDoc();
        if (ThisNiche.guiController.getNotebookUICaretListener() != null)
            ui.addCaretListener(ThisNiche.guiController.getNotebookUICaretListener());
        
        final JScrollPane scrollPane = new JScrollPane(ui);
        scrollPane.setDoubleBuffered(GUIUtil.getFrame().doubleBuffer());
        scrollPane.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce)
            {
                ui.requestFocusInWindow();
            }
        });
        
        scrollPane.setViewportView(ui);
//        HGHandle ctxH = ThisNiche.findContextLink(doc.getBookHandle());
//        if(ctxH == null)
//        {
//           HGHandle rtH = TopFrame.getInstance().getCurrentRuntimeContext(); 
//           CellUtils.setEvalContext(doc.getBook(), rtH);
//           ctxH = rtH;
//        }
//        TopFrame.setCurrentRuntimeContext(ctxH);
        scrollPane.setName(TabbedPaneU.makeTabTitle(doc.getTitle()));
        scrollPane.updateUI();
        return scrollPane;
    }

}
