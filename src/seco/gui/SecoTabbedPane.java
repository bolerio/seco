package seco.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.things.CellGroup;

public class SecoTabbedPane extends JTabbedPane
{
    private int dragTabIndex = -1;
    protected HGHandle groupH;

    public SecoTabbedPane(HGHandle groupH)
    {
        super();
        this.groupH = groupH;
        DragMouseListener ml = new DragMouseListener(this);
        addMouseMotionListener(ml);
        setTransferHandler(new TPTransferHandler(this));
    }

    public CellGroup getCellGroup()
    {
        return (CellGroup) ThisNiche.hg.get(groupH);
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
            // System.out.println("SecoTP - initDrag: " + index);
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
        }

        public void mouseMoved(MouseEvent e)
        {
            // DO NOTHING
        }
    }

    public static class TPTransferHandler extends TransferHandler
    {
        private SecoTabbedPane tp;

        public TPTransferHandler(SecoTabbedPane tp)
        {
            this.tp = tp;
        }

        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JComponent c)
        {
            for (int i = 0; i < flavors.length; i++)
                if (flavors[i].equals(SecoTransferable.FLAVOR))
                    return flavors[i];
            return null;
        }

        public int getSourceActions(JComponent c)
        {
            return COPY_OR_MOVE;
        }

        protected Transferable createTransferable(JComponent comp)
        {
            JComponent c = (JComponent) tp.getComponentAt(tp.dragTabIndex);
            return new SecoTransferable(c, TabbedPaneU.getHandleAt(tp,
                    tp.dragTabIndex));
        }

        protected void exportDone(JComponent source, Transferable data,
                int action)
        {
            super.exportDone(source, data, action);
            if (action == MOVE) TabbedPaneU.closeAt(tp, tp.dragTabIndex);
        }

        public boolean importData(TransferSupport support)
        {
            JComponent comp = (JComponent) support.getComponent();
            Transferable t = support.getTransferable();
            System.out.println("TPTransferHandler: " + comp + ":" + tp);
            // Don't drop on myself.
            if (comp == tp)  return true;
            DataFlavor fl = getImportFlavor(t.getTransferDataFlavors(), comp);
            if (fl == null) return false;
            try
            {
                if (fl.equals(SecoTransferable.FLAVOR))
                {
                    SecoTransferable.Data data = (SecoTransferable.Data) t
                            .getTransferData(fl);
                    if (tp.getCellGroup().indexOf(data.getHandle()) >= 0)
                        return false;
                    tp.getCellGroup().insert(tp.getCellGroup().getArity(),
                            data.getHandle());
                    return true;
                }
            }
            catch (Exception ioe)
            {
                ioe.printStackTrace();
            }
            return false;
        }

        public boolean canImport(JComponent comp, DataFlavor[] flavors)
        {
            return (getImportFlavor(flavors, comp) != null);
        }
    }
}