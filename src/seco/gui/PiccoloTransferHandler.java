package seco.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class PiccoloTransferHandler extends TransferHandler
{
    private PiccoloCanvas canvas;
    
    public PiccoloTransferHandler(PiccoloCanvas canvas)
    {
        super();
        this.canvas = canvas;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        Transferable t = support.getTransferable();
        if (!hasFlavor(t.getTransferDataFlavors())) return false;
        Point pt = support.getDropLocation().getDropPoint();
        System.out.println("PicTrH-importData: " + support + ":" + pt +
                ":" + support.getComponent());
        try
        {
            if(check_and_handle_in_nodes(support)) return true;
            if (support.isDataFlavorSupported(SecoTransferable.FLAVOR))
                return handleSecoTransfer(support);

            Vector<Element> els = (Vector<Element>) 
                   t.getTransferData(NotebookTransferHandler.FLAVOR);
            //PiccoloCanvas canvas = (PiccoloCanvas) support.getComponent();
            boolean move = (support.getDropAction() == MOVE);
            for (Element e : els)
            {
                HGHandle nbH = NotebookDocument.getNBElementH(e);
                boolean outputC = NotebookDocument.isOutputCell(e);
                HGHandle vis = (outputC) ? JComponentVisual.getHandle() : NBUIVisual.getHandle();
                CellGroup group = ThisNiche.hg.get(canvas.getGroupH());
                if (move)
                {
                    GUIHelper.addToCellGroup(nbH, group, vis, null, new Rectangle(pt.x, pt.y, 200, 200), false); 
                    NotebookDocument doc = ((NotebookDocument) e.getDocument());
                    doc.removeCellBoxElement(e);
                    CellUtils.removeHandlers(nbH, doc.getHandle());
                }
                else
                {
                    HGHandle copyH = CellUtils.makeCopy(nbH);
                    System.out.println("PicTrH-importCopyData: " + copyH);
                    GUIHelper.addToCellGroup(copyH, group, vis, null, new Rectangle(pt.x, pt.y, 200, 200), false); 
                    //canvas.addCopyComponent(copyH, nbH, pt);
                }
            }
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean check_and_handle_in_nodes(TransferSupport support)
    {
        Point pt = support.getDropLocation().getDropPoint();
        for(Object o: canvas.getNodeLayer().getAllNodes())
        {
           if(!(o instanceof PSwingNode)) continue;
           PSwingNode node = (PSwingNode) o;
           //System.out.println("handleSecoTransfer" + pt + ":" + node.getFullBounds());
           if(node.getFullBounds().contains(pt))
           {
               //System.out.println("handleSecoTransfer - inner" +
               //        node.getComponent() + ":" + node.getComponent().getTransferHandler());
               TransferHandler handler = node.getComponent().getTransferHandler();
               if(handler != null && handler.importData(support))
               {
                   System.out.println("handleSecoTransfer - inner done: " +
                           node.getComponent() + ":" +
                           support.getComponent());
                   PSwingNode old = canvas.getSelectedPSwingNode();
                   if(old == null) return true;
                   PSwingNode canvN = GUIHelper.getPSwingNode(old.getCanvas());
                   HGHandle groupH = (canvN != null) ? canvN.getHandle() : canvas.getGroupH();
                   GUIHelper.removeFromCellGroup(groupH, old.getHandle());
                   return true;
               }
           }
        }
        return false;
    }
    
    public boolean handleSecoTransfer(TransferSupport support)
            throws UnsupportedFlavorException, IOException
    {
        Point pt = support.getDropLocation().getDropPoint();
        HGHandle data = 
            (HGHandle) support.getTransferable().getTransferData(SecoTransferable.FLAVOR);
        boolean move = (support.getDropAction() == MOVE);
        if (move)
        {
            if(support.getComponent() == canvas)
            {
                CellGroup top = ThisNiche.hg.get(canvas.getGroupH());
                if(top.indexOf(data) > -1) 
                   return false;
            }
            add_to_top_group(data, pt);
        }
        return true;
    }
    
    private void add_to_top_group(HGHandle h, Point pt)
    {
        CellGroup top = ThisNiche.hg.get(canvas.getGroupH());
        CellGroupMember cgm = ThisNiche.hg.get(h);
        Rectangle r = (Rectangle) cgm.getAttribute(VisualAttribs.rect);
        if(r == null) 
            r = new Rectangle(pt.x, pt.y, 300, 200);
        else
        {
            r.x = pt.x; r.y = pt.y; 
        }
        cgm.setAttribute(VisualAttribs.rect, r);
        top.insert(top.getArity(), h);
    }
    
    protected boolean hasFlavor(DataFlavor[] flavors)
    {
        for (int i = 0; i < flavors.length; i++)
            if (flavors[i].equals(SecoTransferable.FLAVOR) ||
                    flavors[i].equals(NotebookTransferHandler.FLAVOR))
                return true;
        return false;
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        return hasFlavor(flavors);
    }
    
    public int getSourceActions(JComponent c)
    {
        int //actions = COPY;
          actions = COPY_OR_MOVE;
        return actions;
    }
    
    protected Transferable createTransferable(JComponent comp)
    {
       PiccoloCanvas canvas = (PiccoloCanvas) comp;
       PSwingNode node = canvas.getSelectedPSwingNode(); 
       return new SecoTransferable(node.getHandle());
    }

}