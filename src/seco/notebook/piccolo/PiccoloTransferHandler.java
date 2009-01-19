package seco.notebook.piccolo;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
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
    static String mimeType = NotebookTransferHandler.mimeType;
    static DataFlavor elFlavor;
    static
    {
        try
        {
            elFlavor = new DataFlavor(mimeType);
        }
        catch (ClassNotFoundException e)
        {
        }
    }

    /**
     * Overridden to import a Color if it is available.
     * getChangesForegroundColor is used to determine whether the foreground or
     * the background color is changed.
     */
    public boolean importData(JComponent c, Transferable t)
    {
        if (hasFlavor(t.getTransferDataFlavors()))
        {
            try
            {
                //System.out.println("Piccolo Transfer: " + c);
                Vector<Element> els = (Vector<Element>) t
                        .getTransferData(elFlavor);
                PiccoloCanvas canvas = (PiccoloCanvas) c;
                    //((PiccoloCanvasContainer) c).getCanvas();
                for (Element e : els)
                {
                    HGHandle nbH = NotebookDocument.getNBElementH(e);
                    HGHandle copyH = CellUtils.makeCopy(nbH);
                    canvas.addComponent(copyH, nbH);
                }
                //return false;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Does the flavor list have a Color flavor?
     */
    protected boolean hasFlavor(DataFlavor[] flavors)
    {
        if (elFlavor == null)
            return false;
        for (int i = 0; i < flavors.length; i++)
            if (elFlavor.equals(flavors[i]))
                return true;
        return false;
    }

    /**
     * Overridden to include a check for a color flavor.
     */
    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        return hasFlavor(flavors);
    }

}