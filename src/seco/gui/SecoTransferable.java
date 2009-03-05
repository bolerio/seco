package seco.gui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class SecoTransferable implements Transferable
{
    public static final String mimeType = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=" + SecoTabbedPane.class.getName();
    public static DataFlavor nbFlavor = null;

    static
    {
        try
        {
            nbFlavor = new DataFlavor(mimeType);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    };

    protected Component c;
    public SecoTransferable(Component c)
    {
        this.c = c;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[] { nbFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return nbFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
    {
        if (isDataFlavorSupported(flavor)) return c;
        return null;
    }
}