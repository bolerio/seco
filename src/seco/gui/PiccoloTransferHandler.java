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
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;
import seco.notebook.NotebookUI;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;

public class PiccoloTransferHandler extends TransferHandler
{
    // The data type exported from NotebookUI.
    static DataFlavor elFlavor;
    static DataFlavor tabFlavor;
    static
    {
        try
        {
            elFlavor = new DataFlavor(NotebookTransferHandler.mimeType);
            tabFlavor = new DataFlavor(SecoTransferable.mimeType);
        }
        catch (ClassNotFoundException e)
        {
        }
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        Transferable t = support.getTransferable();
        if (!hasFlavor(t.getTransferDataFlavors())) return false;
        Point pt = support.getDropLocation().getDropPoint();
        System.out.println("PicTrH-importData: " + support + ":" + pt);
        try
        {
            if (support.isDataFlavorSupported(tabFlavor))
                return handleSecoTransfer(support);

            Vector<Element> els = (Vector<Element>) t.getTransferData(elFlavor);
            PiccoloCanvas canvas = (PiccoloCanvas) support.getComponent();
            boolean move = (support.getDropAction() == MOVE);
            for (Element e : els)
            {
                HGHandle nbH = NotebookDocument.getNBElementH(e);
                if (move)
                {
                    GUIHelper.addToTopCellGroup(nbH, null, new Rectangle(pt.x, pt.y, 300, 200)); 
                    NotebookDocument doc = ((NotebookDocument) e.getDocument());
                    doc.removeCellBoxElement(e);
                    CellUtils.removeHandlers(nbH, doc.getHandle());
                }
                else
                {
                    HGHandle copyH = CellUtils.makeCopy(nbH);
                    //add_to_top_group(copyH, pt);
                    GUIHelper.addToTopCellGroup(copyH, null, new Rectangle(pt.x, pt.y, 300, 200)); 
                    
                    //canvas.addCopyComponent(copyH, nbH, pt);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean handleSecoTransfer(TransferSupport support)
            throws UnsupportedFlavorException, IOException
    {
        Point pt = support.getDropLocation().getDropPoint();
        PiccoloCanvas canvas = (PiccoloCanvas) support.getComponent();
        for(Object o: canvas.getNodeLayer().getAllNodes())
        {
           if(!(o instanceof PSwingNode)) continue;
           PSwingNode node = (PSwingNode) o;
           if(node.getFullBounds().contains(pt))
           {
               System.out.println("handleSecoTransfer - inner" +
                       node.getComponent() + ":" + node.getComponent().getTransferHandler());
               TransferHandler handler = node.getComponent().getTransferHandler();
               if(handler != null && handler.importData(support))
               {
                   System.out.println("handleSecoTransfer - inner done: " +
                           node.getComponent() + ":" +
                           node.getComponent().getUIClassID());
                   PSwingNode old = canvas.getSelectedPSwingNode();
                   if(old == null) return true;
                   GUIHelper.removeFromTopCellGroup(old.getHandle());
                   return true;
               }
           }
        }
        SecoTransferable.Data data = 
            (SecoTransferable.Data) support.getTransferable().getTransferData(tabFlavor);
        boolean move = (support.getDropAction() == MOVE);
        if (move)
        {
            add_to_top_group(data.getHandle(), pt);
        }
        return true;
    }
    
    private void add_to_top_group(HGHandle h, Point pt)
    {
        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
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
            if (elFlavor.equals(flavors[i]) || tabFlavor.equals(flavors[i]))
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
       return new SecoTransferable(node.getComponent(), node.getHandle());
    }

}