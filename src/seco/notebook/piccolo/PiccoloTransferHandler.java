package seco.notebook.piccolo;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;

import seco.gui.PiccoloCanvas;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;
import seco.things.CellUtils;

public class PiccoloTransferHandler extends TransferHandler
{
    // The data type exported from NotebookUI.
    static DataFlavor elFlavor;
   // static DataFlavor tabFlavor;
    static
    {
        try
        {
            elFlavor = new DataFlavor(NotebookTransferHandler.mimeType);
            //tabFlavor = new DataFlavor(CloseableDnDTabbedPane.mimeType);
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
        try
        {
            Vector<Element> els = (Vector<Element>) t.getTransferData(elFlavor);
            PiccoloCanvas canvas = (PiccoloCanvas) support.getComponent();
            boolean move = (support.getDropAction() == MOVE);
            for (Element e : els)
            {
                HGHandle nbH = NotebookDocument.getNBElementH(e);
                if (move)
                {
                    NotebookDocument doc = ((NotebookDocument)e.getDocument());
                    canvas.addComponent(nbH, pt);
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

    protected boolean hasFlavor(DataFlavor[] flavors)
    {
        for (int i = 0; i < flavors.length; i++)
            if (elFlavor.equals(flavors[i])) //||tabFlavor.equals(flavors[i])) 
                    return true;
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        return hasFlavor(flavors);
    }


}