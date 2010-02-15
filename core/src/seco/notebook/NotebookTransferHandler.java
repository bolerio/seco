/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.im.InputContext;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.TextAction;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class NotebookTransferHandler extends TransferHandler
{
    public final static String mimeType = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=javax.swing.text.Element";
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
    private boolean shouldRemove;
    private Vector<Element> elements;
    private NotebookUI exportComp;
    private int p0;
    private int p1;

    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c)
    {
        if (c instanceof JEditorPane)
        {
            for (int i = 0; i < flavors.length; i++)
                if (flavors[i].equals(DataFlavor.stringFlavor)
                        || flavors[i].equals(FLAVOR)) return flavors[i];
        }
        return null;
    }

    private String filterTabs(String in)
    {
        StringBuffer buf = new StringBuffer();
        String tab = Utilities.getTabSubstitute();
        for (int i = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if (c == '\t') buf.append(tab);
            else
                buf.append(c);
        }
        return buf.toString();
    }

    protected void handleStringImport(String s, JTextComponent c)
            throws BadLocationException
    {
        int start = c.getSelectionStart();
        int end = c.getSelectionEnd();
        int length = end - start;
        NotebookDocument doc = (NotebookDocument) c.getDocument();
        if (length > 0) doc.remove(start, length);
        doc.insertString(start, filterTabs(s), null);
        // TODO: the above casting and this workaroud, probably could be avoided
        // in some later refactoring stage...
        if (doc.isInsertionPoint(start)) c.setCaretPosition(start + 1);
    }

    // --- TransferHandler methods ------------------------------------
    public int getSourceActions(JComponent c)
    {
        int actions = COPY;
        if (((JTextComponent) c).isEditable()) actions = COPY_OR_MOVE;
        return actions;
    }

    protected Transferable createTransferable(JComponent comp)
    {
        exportComp = (NotebookUI) comp;
        shouldRemove = true;
        p0 = exportComp.getSelectionStart();
        p1 = exportComp.getSelectionEnd();
        elements = new Vector<Element>(exportComp.getSelectedElements());
        if ((p0 == p1) && elements.isEmpty()) return null;
        if (!elements.isEmpty())
            return new ElementTransferable(exportComp, elements);
        return new TextTransferable(exportComp, p0, p1);
    }

    protected void exportDone(JComponent source, Transferable data, int action)
    {
        // only remove the text if shouldRemove has not been set to
        // false by importData and only if the action is a move
        if (shouldRemove && action == MOVE)
        {
            if (data instanceof TextTransferable)
                ((TextTransferable) data).removeText();
            //done during the import
            if (data instanceof ElementTransferable)
                ((ElementTransferable) data).removeElements();
        }
        exportComp = null;
    }
    
    public boolean importData(TransferSupport support)
    {
        Transferable t = support.getTransferable();
        Component comp = support.getComponent();
        if (!(comp instanceof NotebookUI)) return false;
        NotebookUI c = (NotebookUI) comp;
        // Don't drop on myself.
        if ((c == exportComp && c.getCaretPosition() >= p0 && c
                .getCaretPosition() <= p1))
        {
            shouldRemove = false;
            return true;
        }
        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
        if (importFlavor == null) return false;
        boolean imported = false;
        try
        {
            InputContext ic = c.getInputContext();
            if (ic != null) ic.endComposition();

            if (importFlavor.equals(FLAVOR))
            {
                Vector<Element> els = (Vector<Element>) t
                        .getTransferData(importFlavor);
                boolean succes = c.getDoc().insertElements(
                        c.getCaretPosition(), els);
                if (succes) c.setCaretPosition(c.getCaretPosition() + 1);
                return succes;
            }
            handleStringImport((String) t.getTransferData(importFlavor), c);
            imported = true;
        }
        catch (Exception ioe)
        {
            ioe.printStackTrace();
        }
        return imported;
    }

    public boolean importJavaStringData(JComponent comp, Transferable t, boolean keep_newlines)
    {
        NotebookUI c = (NotebookUI) comp;
        // Don't drop on myself.
        if ((c == exportComp && c.getCaretPosition() >= p0 && c
                .getCaretPosition() <= p1))
        {
            shouldRemove = false;
            return true;
        }

        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
        if (importFlavor == null) return false;
        boolean imported = false;
        try
        {
            InputContext ic = c.getInputContext();
            if (ic != null)
            {
                ic.endComposition();
            }
            if (importFlavor.equals(FLAVOR))
            {
                shouldRemove = false;
                return true;
            }
            handleJavaStringImport((String) t.getTransferData(importFlavor), c, keep_newlines);
            imported = true;
        }
        catch (UnsupportedFlavorException ufe)
        {
            System.err.println("importData: unsupported data flavor");
        }
        catch (BadLocationException ble)
        {
            System.err.println("importData: BadLocationException: " + ble);
        }
        catch (IOException ioe)
        {
            System.err.println("importData: I/O exception");
        }
        return imported;
    }

    protected void handleJavaStringImport(String s, JTextComponent c, boolean keep_newlines)
            throws BadLocationException
    {
        String tab = Utilities.getTabSubstitute();
        StringBuffer sbuff = new StringBuffer(s);
        StringBuffer out = new StringBuffer();
        out.append("\"");
        for (int i = 0; i < sbuff.length(); i++)
        {
            char ch = sbuff.charAt(i);
            if (ch == '\n')
            {
                if (i != sbuff.length() - 1)
                {
                    
                    if(keep_newlines)
                    {
                        out.append("\\n");
                        out.append("\"+ \"");
                    }else // not very clear if the \n should be considered as
                        // a whitespace, but...
                       out.append(' ');
                }
            }
            else if (ch == '"') // || ch == '\'')
            { // don't escape already escaped one
                if ((i > 0 && sbuff.charAt(i - 1) != '\\') || i == 0)
                    out.append('\\');
                out.append(ch);
            }
            else if (ch == '\r') continue;
            else if (ch == '\t') out.append(tab);
            else
                out.append(ch);
        }
        out.append("\"");
        int start = c.getSelectionStart();
        int end = c.getSelectionEnd();
        int length = end - start;
        Document doc = c.getDocument();
        if (length > 0) doc.remove(start, length);
        doc.insertString(start, out.toString(), null);
    }

    public boolean canImport(JComponent comp, DataFlavor[] flavors)
    {
        JTextComponent c = (JTextComponent) comp;
        if (!(c.isEditable() && c.isEnabled())) return false;
        return (getImportFlavor(flavors, c) != null);
    }

    /**
     * A possible implementation of the Transferable interface for text
     * components. For a JEditorPane with a rich set of EditorKit
     * implementations, conversions could be made giving a wider set of formats.
     * This is implemented to offer up only the active content type and
     * text/plain (if that is not the active format) since that can be extracted
     * from other formats.
     */
    static class TextTransferable implements Transferable
    {
        protected String plainData;
        protected Position p0;
        protected Position p1;
        protected JTextComponent c;

        TextTransferable(JTextComponent c, int start, int end)
        {
            this.c = c;
            Document doc = c.getDocument();
            try
            {
                p0 = doc.createPosition(start);
                p1 = doc.createPosition(end);
                plainData = c.getSelectedText();
            }
            catch (BadLocationException ble)
            {
            }
        }

        void removeText()
        {
            if ((p0 != null) && (p1 != null)
                    && (p0.getOffset() != p1.getOffset()))
            {
                try
                {
                    Document doc = c.getDocument();
                    doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
                }
                catch (BadLocationException e)
                {
                }
            }
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.stringFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++)
                if (flavors[i].equals(flavor)) return true;
            return false;
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException
        {
            if (isDataFlavorSupported(flavor))
            {
                String data = plainData;
                data = (data == null) ? "" : data;
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public static class ElementTransferable implements Transferable
    {
        protected Vector<Element> elements;
        protected NotebookUI c;

        public ElementTransferable(NotebookUI c, Vector<Element> el)
        {
            this.c = c;
            elements = new Vector<Element>(el);
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { FLAVOR };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return FLAVOR.equals(flavor);
        }

        void removeElements()
        {
            NotebookDocument doc = c.getDoc();
            for (Element e : elements)
            {
                try
                {
                    doc.removeCellBoxElement(e, true);
                }
                catch (BadLocationException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException
        {
            if (isDataFlavorSupported(flavor)) { return elements; }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public static JavaStringPasteAction javaStringPasteAction = new JavaStringPasteAction();

    public static class JavaStringPasteAction extends TextAction implements
            UIResource
    {
        public JavaStringPasteAction()
        {
            this("Java String");
        }

        JavaStringPasteAction(String name)
        {
            super(name);
            // Will cause the system clipboard state to be updated.
            canAccessSystemClipboard = true;
            canAccessSystemClipboard();
        }

        public void actionPerformed(ActionEvent e)
        {
            JTextComponent ui = getTextComponent(e);
            NotebookTransferHandler th = (NotebookTransferHandler) ui
                    .getTransferHandler();
            Clipboard clipboard = getClipboard(ui);
            Transferable trans = clipboard.getContents(null);
            if (trans != null) th.importJavaStringData(ui, trans, true);
        }

        private Clipboard getClipboard(JComponent c)
        {
            if (canAccessSystemClipboard()) { return c.getToolkit()
                    .getSystemClipboard(); }
            Clipboard clipboard = (Clipboard) sun.awt.AppContext
                    .getAppContext().get(SandboxClipboardKey);
            if (clipboard == null)
            {
                clipboard = new Clipboard("Sandboxed Component Clipboard");
                sun.awt.AppContext.getAppContext().put(SandboxClipboardKey,
                        clipboard);
            }
            return clipboard;
        }

        /**
         * Returns true if it is safe to access the system Clipboard. If the
         * environment is headless or the security manager does not allow access
         * to the system clipboard, a private clipboard is used.
         */
        private boolean canAccessSystemClipboard()
        {
            if (canAccessSystemClipboard)
            {
                if (GraphicsEnvironment.isHeadless())
                {
                    canAccessSystemClipboard = false;
                    return false;
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null)
                {
                    try
                    {
                        sm.checkSystemClipboardAccess();
                        return true;
                    }
                    catch (SecurityException se)
                    {
                        canAccessSystemClipboard = false;
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        /**
         * Indicates if it is safe to access the system clipboard. Once false,
         * access will never be checked again.
         */
        private boolean canAccessSystemClipboard;
        /**
         * Key used in app context to lookup Clipboard to use if access to
         * System clipboard is denied.
         */
        private static Object SandboxClipboardKey = new Object();
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action)
    {
        // System.out.println("exportAsDrag: " + (action == MOVE) + ":" + e);
        shouldRemove = true;
        super.exportAsDrag(comp, e, action);
    }
}
