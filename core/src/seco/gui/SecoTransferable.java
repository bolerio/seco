package seco.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.hypergraphdb.HGHandle;

import seco.gui.common.SecoTabbedPane;

public class SecoTransferable implements Transferable
{
    public static final String mimeType = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=" + SecoTabbedPane.class.getName();
    public static DataFlavor FLAVOR = null;

    static
    {
        try
        {
            FLAVOR = new DataFlavor(mimeType);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    };

    protected HGHandle data;
    
    public SecoTransferable(HGHandle h)
    {
        data = h;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[] { FLAVOR };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return FLAVOR.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
    {
        if (isDataFlavorSupported(flavor)) return data;
        return null;
    }
   
}