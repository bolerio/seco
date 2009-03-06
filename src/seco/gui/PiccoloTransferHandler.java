package seco.gui;

import java.awt.Point;
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
import seco.things.CellUtils;

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
                    canvas.addComponent(nbH, pt);
                    NotebookDocument doc = ((NotebookDocument) e.getDocument());
                    doc.removeCellBoxElement(e);
                    CellUtils.removeHandlers(nbH, doc.getHandle());
                }
                else
                {
                    HGHandle copyH = CellUtils.makeCopy(nbH);
                    canvas.addCopyComponent(copyH, nbH, pt);
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
               if(node.getComponent().getTransferHandler().importData(support))
               {
                   System.out.println("handleSecoTransfer - inner done");
                   PSwingNode old = canvas.getSelectedPSwingNode();
                   if(old == null) return true;
                   GUIHelper.removeFromTopCellGroup(old.getHandle());
                   old.removeFromParent();
                   return true;
               }
           }
        }
        JComponent comp = (JComponent) support.getTransferable()
                .getTransferData(tabFlavor);
        boolean move = (support.getDropAction() == MOVE);
        if (move)
        {
             HGHandle h = (comp instanceof NotebookUI) ?
               ((NotebookUI) comp).getDoc().getBookHandle() 
                    : ThisNiche.handleOf(comp);
             canvas.addComponent(h, pt);
        }
        return true;
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
       return new SecoTransferable(node.getComponent());
    }

}