package seco.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.text.JTextComponent;

import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookUI;
import seco.things.CellGroup;

public class SecoTabbedPane extends JTabbedPane
{
    private int dragTabIndex = -1;

    public SecoTabbedPane()
    {
        super();
        DragMouseListener ml = new DragMouseListener(this);
        addMouseMotionListener(ml);
        setTransferHandler(new TPTransferHandler(this));
    }

    private int getTargetTabIndex(Point pt)
    {
        for (int i = 0; i < getTabCount(); i++)
        {
            Rectangle r = getBoundsAt(i);
            if (r != null && r.contains(pt)) return i;
        }

        return -1;
    }

    private static class DragMouseListener implements MouseMotionListener
    {
        private SecoTabbedPane tp;

        private DragMouseListener(SecoTabbedPane tp)
        {
            this.tp = tp;
        }

        private void initDrag(MouseEvent e)
        {
            int index = tp.getTargetTabIndex(e.getPoint());
            System.out.println("SecoTP - initDrag: " + index);
            if (index < 0) return;
            tp.dragTabIndex = index;
            TransferHandler handler = tp.getTransferHandler();
            // MouseEvent m = SwingUtilities.convertMouseEvent(button, e, c);
            int action = ((e.getModifiers() & MouseEvent.CTRL_MASK) == 0) ? TransferHandler.MOVE
                    : TransferHandler.COPY;
            handler.exportAsDrag(tp, e, action);
        }

        public void mouseClicked(MouseEvent e)
        {
            if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
            {
                // NotebookUI ui = (NotebookUI) button.view.getContainer();
                // ui.showPopup(SwingUtilities.convertMouseEvent(button, e,
                // ui));
            }
            // else
            // initDrag(e);
        }

        public void mouseDragged(MouseEvent e)
        {
            initDrag(e);
            // e.consume();
        }

        public void mouseMoved(MouseEvent e)
        {
            // DO NOTHING
        }
    }

    public static class TPTransferHandler extends TransferHandler
    {
        private boolean shouldRemove;
        private SecoTabbedPane tp;

        public TPTransferHandler(SecoTabbedPane tp)
        {
            super();
            this.tp = tp;
        }

        public TPTransferHandler()
        {
            super();
        }

        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JComponent c)
        {
            for (int i = 0; i < flavors.length; i++)
                if (flavors[i].equals(SecoTransferable.nbFlavor))
                    return flavors[i];
            return null;
        }

        public int getSourceActions(JComponent c)
        {
            return COPY_OR_MOVE;
        }

        protected Transferable createTransferable(JComponent comp)
        {
           // tp = (SecoTabbedPane) comp;
            shouldRemove = true;
            JScrollPane p = (JScrollPane) tp.getComponentAt(tp.dragTabIndex);
            return new SecoTransferable(p.getViewport().getView());
        }

        protected void exportDone(JComponent source, Transferable data,
                int action)
        {
            if (shouldRemove && action == MOVE)
            {
                tp.removeTabAt(tp.dragTabIndex);
                CellGroup top = ThisNiche.hg.get(GUIHelper.getTopCellGroupHandle(tp)); 
                top.remove(tp.dragTabIndex);
            }
            //tp = null;
        }

        public boolean importData(TransferSupport support)
        {
            JComponent comp = (JComponent) support.getComponent();
            Transferable t = support.getTransferable();
            System.out.println("TPTransferHandler: " + comp + ":" + tp);
         // Don't drop on myself.
            if (comp == tp)
            {
                shouldRemove = false;
                System.out.println("TPTransferHandler - Return");
                return true;
            }
            DataFlavor importFlavor = getImportFlavor(t
                    .getTransferDataFlavors(), comp);
            if (importFlavor == null) return false;
            boolean imported = false;
            try
            {
                if (importFlavor.equals(SecoTransferable.nbFlavor))
                {
                    Component in = (Component) t.getTransferData(importFlavor);
                    if (in instanceof NotebookUI)
                    {
                        NotebookUI ui = (NotebookUI) in;
                        TabbedPaneU.addNotebookTab(tp, ui, true);
                        return true;
                    }
                    
                   // String title = (in.getName() != null && in.getName()
                   //         .length() > 0) ? in.getName() : "Some Component";
                   // tp.addTab(title, in);
                }
            }
            catch (Exception ioe)
            {
               ioe.printStackTrace();
            }
            return imported;
        }

        public boolean canImport(JComponent comp, DataFlavor[] flavors)
        {
            return (getImportFlavor(flavors, comp) != null);
        }
    }
}