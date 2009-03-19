package seco.gui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandle;

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

    protected Data data;
    
    public SecoTransferable(JComponent comp, HGHandle h)
    {
        data = new Data(comp, h);
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
    
    public static class Data
    {
        protected JComponent component;
        protected HGHandle top_cell_handle;
        
        public Data(JComponent c, HGHandle top_cell_handle)
        {
            super();
            this.component = c;
            this.top_cell_handle = top_cell_handle;
        }

        public JComponent getComponent()
        {
            return component;
        }

        public HGHandle getHandle()
        {
            return top_cell_handle;
        }
       
    }
}