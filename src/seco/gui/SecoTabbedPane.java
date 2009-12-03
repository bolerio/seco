package seco.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.things.CellGroup;
import seco.things.CellUtils;

public class SecoTabbedPane extends JTabbedPane
{
    private HGHandle dragTabHandle = null;
    protected HGHandle groupH;

    public SecoTabbedPane(HGHandle groupH)
    {
        super();
        this.groupH = groupH;
        DragMouseListener ml = new DragMouseListener(this);
        addMouseMotionListener(ml);
        addMouseListener(ml);
        setTransferHandler(new TPTransferHandler(this));
    }

    public CellGroup getCellGroup()
    {
        return (CellGroup) ThisNiche.graph.get(groupH);
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

    private static class DragMouseListener extends MouseAdapter
    {
        private SecoTabbedPane tp;
        private boolean draging = false;
        private int lastX;
        private int lastY;
        //minimal distance between pressed/dragged to start dragging
        //thus preventing unintentional drags 
        private static final int MIN_DIST = 5;

        private DragMouseListener(SecoTabbedPane tp)
        {
            this.tp = tp;
        }

        private void initDrag(MouseEvent e)
        {
            if(Math.abs(lastX - e.getX()) < MIN_DIST &&
                    Math.abs(lastY - e.getY()) < MIN_DIST) return;
            int index = tp.getTargetTabIndex(e.getPoint());
            if (index < 0) {
                tp.dragTabHandle = null;
                return;
            }
            draging = true;
            System.out.println("SecoTP - initDrag: " + index);
            tp.dragTabHandle = tp.getCellGroup().getTargetAt(index);
            TransferHandler handler = tp.getTransferHandler();
            int action = ((e.getModifiers() & MouseEvent.CTRL_MASK) == 0) ? 
                    TransferHandler.MOVE : TransferHandler.COPY;
            handler.exportAsDrag(tp, e, action);
        }

        public void mouseDragged(MouseEvent e)
        {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if(!draging) initDrag(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            draging = false;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            lastX = e.getX(); 
            lastY = e.getY();
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
            return new SecoTransferable(tp.dragTabHandle);
        }

        protected void exportDone(JComponent source, Transferable data,
                int action)
        {
            super.exportDone(source, data, action);
            if (action == MOVE ) 
            {
                int i = tp.getCellGroup().indexOf(tp.dragTabHandle);
                if(i > -1)
                  closeAt(tp, i);
            }
                
        }
        
        public static void closeAt(JTabbedPane tp, int i)
        {
            if(i < 0 || i >= tp.getTabCount()) return;
            HGHandle h = TabbedPaneU.getHandleAt(tp, i);
            ThisNiche.graph.unfreeze(h);
            CellGroup top = CellUtils.getParentGroup(h);
            top.remove(i);
           
            HGHandle visH = CellContainerVisual.getHandle();
            CellUtils.removeEventPubSub(CellGroupChangeEvent.HANDLE, h, visH, visH);
            CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, h, visH, visH);
            CellUtils.removeEventPubSub(AttributeChangeEvent.HANDLE, h, visH, visH);
           
            if (tp.getTabCount() == 0) 
                TopFrame.getInstance().setTitle("Seco");
            else
                GUIHelper.updateFrameTitle(
                        TabbedPaneU.getHandleAt(tp, tp.getSelectedIndex()));
        }

        public boolean importData(TransferSupport support)
        {
            JComponent comp = (JComponent) support.getComponent();
            Transferable t = support.getTransferable();
            System.out.println("TPTransferHandler: " + comp + ":" + tp);
            // Don't drop on myself.
            if (comp == tp) return false;
            DataFlavor fl = getImportFlavor(t.getTransferDataFlavors(), comp);
            if (fl == null) return false;
            try
            {
                if (fl.equals(SecoTransferable.FLAVOR))
                {
                    HGHandle data = (HGHandle) t.getTransferData(fl);
                   // System.out.println("TPTransferHandler1: " + tp.groupH + ":"
                   //         + data);
                    if (tp.getCellGroup().indexOf(data) >= 0) return false;
                    tp.getCellGroup()
                            .insert(tp.getCellGroup().getArity(), data);
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