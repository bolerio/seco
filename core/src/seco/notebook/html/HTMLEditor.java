/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoManager;

import seco.gui.menu.UpdatablePopupMenu;
import seco.util.GUIUtil;
import sun.awt.AppContext;

public class HTMLEditor extends JTextPane
{
    public static final String TOOLBAR_NAME = "htmlEditorToolBar";
    public static final String PROP_ELEM = "prop_elem";
    //protected static HTMLEditor editor;
    protected UndoManager undo = new UndoManager();
    private UpdatablePopupMenu popupMenu;
    protected static HyperlinkListener hyperlinkListener = new MyHyperlinkListener();
    protected PopupListener popupListener;
       
    public HTMLEditor()
    {
        this(null);
    }

    public HTMLEditor(String text)
    {
        setEditable(true);
        // putClientProperty(W3C_LENGTH_UNITS, true);
        setMargin(new Insets(0, 3, 3, 3));
        installKeyActions();
        setTransferHandler(new HTMLTransferHandler());
        setDragEnabled(true);
        getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e)
            {
                undo.addEdit(e.getEdit());
                MyHTMLEditorKit.undo.updateUndoState(undo);
                MyHTMLEditorKit.redo.updateRedoState(undo);
                //setModified(true);
            }
        });
        if (text != null) setContent(text);

        addHyperlinkListener(hyperlinkListener);
        addMouseListener(getPopupListener());
    }

    protected PopupListener getPopupListener()
    {
        if (popupListener == null) popupListener = new PopupListener();
        return popupListener;
    }

    protected void installKeyActions()
    {
        // createDefaultEditorKit();
        MyHTMLEditorKit htmlKit = (MyHTMLEditorKit) getEditorKit();
        InputMap inputMap = getInputMap();
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                InputEvent.CTRL_DOWN_MASK);
        Action act = htmlKit.getActionByName("font-bold");
        act.putValue(Action.ACCELERATOR_KEY, key);
        inputMap.put(key, htmlKit.getActionByName("font-bold"));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
        act = htmlKit.getActionByName("font-italic");
        act.putValue(Action.ACCELERATOR_KEY, key);
        inputMap.put(key, htmlKit.getActionByName("font-italic"));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);
        act = htmlKit.getActionByName("font-underline");
        act.putValue(Action.ACCELERATOR_KEY, key);
        inputMap.put(key, htmlKit.getActionByName("font-underline"));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, htmlKit.getActionByName(MyHTMLEditorKit.undoAction));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, htmlKit.getActionByName(MyHTMLEditorKit.redoAction));
        // key = KeyStroke.getKeyStroke(KeyEvent.VK_F,
        // InputEvent.CTRL_DOWN_MASK);
        // inputMap.put(key,
        // htmlKit.getActionByName(MyHTMLEditorKit.fontAction));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, htmlKit
                .getActionByName(MyHTMLEditorKit.listFormatAction));
        key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, MyHTMLEditorKit.showInputTypePopup);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, MyHTMLEditorKit.findAction);
    }
    
    public boolean isModified()
    {
        return getDoc().isModified();
    }

    public void setModified(boolean modified)
    {
        getDoc().setModified(modified);
    }

    UndoManager getUndoManager()
    {
        return undo;
    }

    protected String cachedText = null;
    /**
     * Sets the content as HTML
     */
    public void setContent(final String content)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                Reader in = new StringReader(content);
                try
                {
                    MyHTMLDocument doc = getDoc();
                    doc.remove(0, doc.getLength());
                    getEditorKit().read(in, getDocument(), 0);
                    // doc.adjustP();
                    setCaretPosition(0);
                    scrollRectToVisible(new Rectangle(0, 0, 0, 0));
                    // clear undos
                    undo.discardAllEdits();
                    cachedText = content;
                    doc.setModified(false);
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
                catch (BadLocationException ble)
                {
                    ble.printStackTrace();
                }
            }
        });
    }

    public String getContent()
    {
        Writer w = new StringWriter();
        MyHTMLDocument doc = this.getDoc();
        if(!doc.isModified()) return cachedText;
        try
        {
            getEditorKit().write(w, doc, 0, doc.getLength());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        cachedText = w.toString();
        doc.setModified(false);
        return cachedText;
    }

    /** Sets javadoc background color */
    public void setBGColor(Color bgColor)
    {
        setBackground(bgColor);
    }

    protected EditorKit createDefaultEditorKit()
    {
        return new MyHTMLEditorKit();
    }

    MyHTMLEditorKit getMyEditorKit()
    {
        return (MyHTMLEditorKit) getEditorKit();
    }

    protected void setAttributeSet(AttributeSet attr)
    {
        setAttributeSet(attr, false);
    }

    public MyHTMLDocument getDoc()
    {
        return (MyHTMLDocument) getDocument();
    }

    protected void setAttributeSet(AttributeSet attr,
            boolean setParagraphAttributes)
    {
        int xStart = getSelectionStart();
        int xFinish = getSelectionEnd();
        if (setParagraphAttributes) getDoc().setParagraphAttributes(xStart,
                xFinish - xStart, attr, false);
        else
        {
            if (xStart != xFinish) getDoc().setCharacterAttributes(xStart,
                    xFinish - xStart, attr, false);
            else
            {
                MutableAttributeSet inputAttributes = getMyEditorKit()
                        .getInputAttributes();
                inputAttributes.addAttributes(attr);
            }
        }
    }

    public void insertUnicodeChar(String sChar) throws IOException,
            BadLocationException, RuntimeException
    {
        int caretPos = getCaretPosition();
        if (sChar != null)
        {
            getDoc().insertString(caretPos, sChar, null);
            setCaretPosition(caretPos + 1);
        }
    }

    public void replaceSelection(HTMLText content)
    {
        MyHTMLDocument doc = getDoc();
        Caret caret = getCaret();
        if (doc != null)
        {
            try
            {
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                if (p0 != p1)
                {
                    doc.remove(p0, p1 - p0);
                }
                if (content != null)
                {
                    content.pasteHTML(doc, p0);
                }
            }
            catch (Exception e)
            {
                getToolkit().beep();
            }
        }
    }

    void removeListItem(Element el) throws BadLocationException, IOException
    {
        Element par = el.getParentElement();
        int offset = par.getEndOffset();
        //Element td = HTMLUtils.getTag(this, HTML.Tag.TD, offset);
        // System.out.println("removeListElement: " + par + ":" + el + ":" + td
        // + ":" + offset);
        HTMLUtils.listOff(this, HTMLUtils.getName(par));
        setCaretPosition(offset - 1);
    }

    void insertListItem(Element element, String s) throws BadLocationException,
            IOException
    {
        // System.out.println("insertListItem: " + element + ":" + s);
        if (s == null) s = "&nbsp;";
        int caretPos = this.getCaretPosition();
        HTML.Tag tag = HTMLUtils.getName(element.getParentElement());
        insertListItem(tag, s);
        this.setCaretPosition(caretPos + 2);
    }

    void insertListItem(HTML.Tag baseTag, String text)
            throws BadLocationException, IOException
    {
        Element el = HTMLUtils
                .getTag(this, HTML.Tag.LI, getCaretPosition() - 1);
        getDoc().insertAfterEnd(el, "<li>" + text + "</li>");
    }

    public void setLink(String linkText, String href, String linkImage,
            Dimension size)
    {
        MyHTMLDocument doc = (MyHTMLDocument) getDocument();
        Element e = Util.findLinkElementUp(doc
                .getCharacterElement(getSelectionStart()));
        if (linkImage == null) setTextLink(e, href, linkText, doc);
        else
            setImageLink(doc, e, href, linkImage, size);
    }

    /**
     * set an image link replacing the current selection
     * 
     * @param doc
     *            the document to apply the link to
     * @param e
     *            the link element found at the selection, or null if none was
     *            found
     * @param href
     *            the link reference
     * @param linkImage
     *            the file name of the image be used for the link
     * @param size
     *            the size of the image
     */
    private void setImageLink(MyHTMLDocument doc, Element e, String href,
            String linkImage, Dimension size)
    {
        String a = HTML.Tag.A.toString();
        SimpleAttributeSet set = new SimpleAttributeSet();
        set.addAttribute(HTML.Attribute.HREF, href);
        // set.addAttribute(HTML.Attribute.CLASS, className);
        StringWriter sw = new StringWriter();
        MyHTMLWriter w = new MyHTMLWriter(sw);
        try
        {
            w.startTag(a, set);
            set = new SimpleAttributeSet();
            set.addAttribute(HTML.Attribute.SRC, Util.getRelativePath(new File(
                    doc.getBase().getFile()), new File(linkImage)));
            set.addAttribute(HTML.Attribute.BORDER, "0");
            if (size != null)
            {
                set.addAttribute(HTML.Attribute.WIDTH, Integer
                        .toString(new Double(size.getWidth()).intValue()));
                set.addAttribute(HTML.Attribute.HEIGHT, Integer
                        .toString(new Double(size.getHeight()).intValue()));
            }
            w.startTag(HTML.Tag.IMG.toString(), set);
            w.endTag(a);
            if (e != null)
            {
                doc.setOuterHTML(e, sw.getBuffer().toString());
            }
            else
            {
                int start = getSelectionStart();
                if (start < getSelectionEnd())
                {
                    replaceSelection("");
                    doc.insertAfterEnd(doc.getCharacterElement(start), sw
                            .getBuffer().toString());
                }
            }
        }
        catch (Exception ex)
        {
            Util.errMsg(this, ex.getMessage(), ex);
        }
    }

    /**
     * set a text link replacing the current selection
     * 
     * @param e
     *            the link element found at the selection, or null if none was
     *            found
     * @param href
     *            the link reference
     * @param linkText
     *            the text to show as link
     * @param doc
     *            the document to apply the link to
     */
    private void setTextLink(Element e, String href, String linkText,
            MyHTMLDocument doc)
    {
        SimpleAttributeSet aSet = new SimpleAttributeSet();
        aSet.addAttribute(HTML.Attribute.HREF, href);
        SimpleAttributeSet set = new SimpleAttributeSet();
        if (e != null)
        {
            // replace existing link
            set.addAttributes(e.getAttributes());
            set.addAttribute(HTML.Tag.A, aSet);
            int start = e.getStartOffset();
            try
            {
                doc.replace(start, e.getEndOffset() - start, linkText, set);
            }
            catch (BadLocationException ex)
            {
                Util.errMsg(this, ex.getMessage(), ex);
            }
        }
        else
        {
            // create new link for text selection
            int start = getSelectionStart();
            if (start < getSelectionEnd())
            {
                set.addAttribute(HTML.Tag.A, aSet);
                replaceSelection(linkText);
                doc
                        .setCharacterAttributes(start, linkText.length(), set,
                                false);
            }
        }
    }

    public void insertTable(int colCount)
    {
        int start = getSelectionStart();
        StringWriter sw = new StringWriter();
        MyHTMLWriter w = new MyHTMLWriter(sw);
        // some needed constants
        String table = HTML.Tag.TABLE.toString();
        String tr = HTML.Tag.TR.toString();
        String td = HTML.Tag.TD.toString();
        String p = HTML.Tag.P.toString();
        // the attribute set to use for applying attributes to tags
        SimpleAttributeSet set = new SimpleAttributeSet();
        // build table attribute
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH, "80%");
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_STYLE,
                "solid");
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_TOP_WIDTH,
                "0");
        Util.styleSheet().addCSSAttribute(set,
                CSS.Attribute.BORDER_RIGHT_WIDTH, "0");
        Util.styleSheet().addCSSAttribute(set,
                CSS.Attribute.BORDER_BOTTOM_WIDTH, "0");
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.BORDER_LEFT_WIDTH,
                "0");
        set.addAttribute(HTML.Attribute.BORDER, "0");
        try
        {
            w.startTag(table, set);
            // start row tag
            w.startTag(tr, null);
            // get width of each cell according to column count
            String tdWidth = Integer.toString(100 / colCount);
            // build cell width attribute
            Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH,
                    Integer.toString(100 / colCount) + Util.pct);
            set.addAttribute(HTML.Attribute.VALIGN, "top");
            Util.styleSheet().addCSSAttribute(set,
                    CSS.Attribute.BORDER_TOP_WIDTH, "1");
            Util.styleSheet().addCSSAttribute(set,
                    CSS.Attribute.BORDER_RIGHT_WIDTH, "1");
            Util.styleSheet().addCSSAttribute(set,
                    CSS.Attribute.BORDER_BOTTOM_WIDTH, "1");
            Util.styleSheet().addCSSAttribute(set,
                    CSS.Attribute.BORDER_LEFT_WIDTH, "1");
            SimpleAttributeSet pSet = new SimpleAttributeSet();
            Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_TOP,
                    "1");
            Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_RIGHT,
                    "1");
            Util.styleSheet().addCSSAttribute(pSet,
                    CSS.Attribute.MARGIN_BOTTOM, "1");
            Util.styleSheet().addCSSAttribute(pSet, CSS.Attribute.MARGIN_LEFT,
                    "1");
            set.removeAttribute(HTML.Attribute.BORDER);
            // add cells
            for (int i = 0; i < colCount; i++)
            {
                w.startTag(td, set);
                w.startTag(p, pSet);
                w.endTag(p);
                w.endTag(td);
            }
            // end row and table tags
            w.endTag(tr);
            w.endTag(table);
            // read table html into document
            Element para = getDoc().getParagraphElement(getSelectionStart());
            if (para != null)
            {
                getDoc().insertAfterEnd(para, sw.getBuffer().toString());
            }
        }
        catch (Exception ex)
        {
            Util.errMsg(null, ex.getMessage(), ex);
        }
        select(start, start);
    }

    public void applyTableAttributes(AttributeSet a)
    {
        Element cell = getCurTableCell();
        if (cell != null)
        {
            Element table = cell.getParentElement().getParentElement();
            if (a.getAttributeCount() > 0) getDoc().addAttributes(table, a);
        }
    }

    private Element getCurTableCell()
    {
        return HTMLUtils.getTag(this, HTML.Tag.TD, getCaretPosition());
    }

    public static final int THIS_CELL = 0;
    public static final int THIS_COLUMN = 1;
    public static final int THIS_ROW = 2;
    public static final int ALL_CELLS = 3;

    public void applyCellAttributes(AttributeSet a, int range)
    {
        // System.out.println("SHTMLEditorPane applyCellAttributes a=" + a);
        Element cell = getCurTableCell();
        int cIndex = 0;
        int rIndex = 0;
        if (cell != null)
        {
            Element row = cell.getParentElement();
            Element table = row.getParentElement();
            Element aCell;
            switch (range)
            {
            case THIS_CELL:
                getDoc().addAttributes(cell, a);
                break;
            case THIS_ROW:
                for (cIndex = 0; cIndex < row.getElementCount(); cIndex++)
                {
                    aCell = row.getElement(cIndex);
                    getDoc().addAttributes(aCell, a);
                }
                break;
            case THIS_COLUMN:
                cIndex = Util.getElementIndex(cell); // getColNumber(cell);
                for (rIndex = 0; rIndex < table.getElementCount(); rIndex++)
                {
                    aCell = table.getElement(rIndex).getElement(cIndex);
                    getDoc().addAttributes(aCell, a);
                }
                break;
            case ALL_CELLS:
                while (rIndex < table.getElementCount())
                {
                    row = table.getElement(rIndex);
                    cIndex = 0;
                    while (cIndex < row.getElementCount())
                    {
                        aCell = row.getElement(cIndex);
                        getDoc().addAttributes(aCell, a);
                        cIndex++;
                    }
                    rIndex++;
                }
                break;
            }
        }
    }

    public void insertTableColumn()
    {
        Element cell = getCurTableCell();
        if (cell != null)
            createTableColumn(cell, Util.getElementIndex(cell), true);
    }

    public void appendTableColumn()
    {
        Element cell = getCurTableCell();
        if (cell != null)
        {
            Element lastCell = getLastTableCell(cell);
            createTableColumn(lastCell, Util.getElementIndex(cell), false);
        }
    }

    private Element getLastTableCell(Element cell)
    {
        Element table = cell.getParentElement().getParentElement();
        Element lastRow = table.getElement(table.getElementCount() - 1);
        Element lastCell = lastRow.getElement(lastRow.getElementCount() - 1);
        return lastCell;
    }

    private void createTableColumn(Element cell, int cIndex, boolean before)
    {
        // get the new width setting for this column and the new column
        MyHTMLDocument doc = getDoc();
        Element table = cell.getParentElement().getParentElement();
        Element srcCell = table.getElement(0).getElement(cIndex);
        SimpleAttributeSet set = new SimpleAttributeSet(srcCell.getAttributes());
        Object attr = set.getAttribute(CSS.Attribute.WIDTH);
        if (attr != null)
        {
            int width = (int) Util.getAbsoluteAttrVal(attr); // Util.getAttrValue(attr);
            String unit = Util.getLastAttrUnit();
            String widthString = Integer.toString(width / 2) + unit;
            Util.styleSheet().addCSSAttribute(set, CSS.Attribute.WIDTH,
                    widthString);
        }
        for (int rIndex = 0; rIndex < table.getElementCount(); rIndex++)
        {
            srcCell = table.getElement(rIndex).getElement(cIndex);
            doc.addAttributes(srcCell, set);
            try
            {
                if (before)
                {
                    doc.insertBeforeStart(srcCell, getTableCellHTML(srcCell));
                }
                else
                {
                    doc.insertAfterEnd(srcCell, getTableCellHTML(srcCell));
                }
            }
            catch (IOException ioe)
            {
                Util.errMsg(null, ioe.getMessage(), ioe);
            }
            catch (BadLocationException ble)
            {
                Util.errMsg(null, ble.getMessage(), ble);
            }
        }
    }

    public void appendTableRow()
    {
        Element cell = getCurTableCell();
        if (cell != null)
        {
            Element table = cell.getParentElement().getParentElement();
            Element lastRow = table.getElement(table.getElementCount() - 1);
            createTableRow(lastRow, Util.getRowIndex(lastRow.getElement(0)),
                    false);
        }
    }

    public void insertTableRow()
    {
        Element cell = getCurTableCell();
        if (cell != null)
            createTableRow(cell.getParentElement(), Util.getRowIndex(cell),
                    true);
    }

    private void createTableRow(Element srcRow, int rowIndex, boolean before)
    {
        try
        {
            if (before)
            {
                getDoc().insertBeforeStart(srcRow, getTableRowHTML(srcRow));
                if (rowIndex == 0) rowIndex++;
            }
            else
            {
                getDoc().insertAfterEnd(srcRow, getTableRowHTML(srcRow));
                rowIndex++;
            }
        }
        catch (Exception ioe)
        {
        }
    }

    public String getTableRowHTML(Element srcRow)
    {
        HTML.Tag tr = HTML.Tag.TR;
        HTML.Tag td = HTML.Tag.TD;
        HTML.Tag p = HTML.Tag.P;
        StringWriter sw = new StringWriter();
        MyHTMLWriter w = new MyHTMLWriter(sw);
        try
        {
            w.startTag(tr, srcRow.getAttributes());
            for (int i = 0; i < srcRow.getElementCount(); i++)
            {
                w.startTag(td, srcRow.getElement(i).getAttributes());
                w.startTag(p, srcRow.getElement(i).getElement(0)
                        .getAttributes());
                w.endTag(p);
                w.endTag(td);
            }
            w.endTag(tr);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return sw.getBuffer().toString();
    }

    public String getTableCellHTML(Element srcCell)
    {
        HTML.Tag td = HTML.Tag.TD;
        HTML.Tag p = HTML.Tag.P;
        StringWriter sw = new StringWriter();
        MyHTMLWriter w = new MyHTMLWriter(sw);
        try
        {
            w.startTag(td, srcCell.getAttributes());
            w.startTag(p, null);
            w.endTag(p);
            w.endTag(td);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return sw.getBuffer().toString();
    }

    public void deleteTableRow()
    {
        Element cell = getCurTableCell();
        if (cell != null)
        {
            removeElement(cell.getParentElement());
        }
    }

    private void removeElement(Element e)
    {
        int start = e.getStartOffset();
        try
        {
            getDoc().remove(start, e.getEndOffset() - start);
        }
        catch (BadLocationException ble)
        {
            Util.errMsg(null, ble.getMessage(), ble);
        }
    }

    public void deleteTableCol()
    {
        Element cell = getCurTableCell();
        if (cell != null)
        {
            Element row = cell.getParentElement();
            int lastColIndex = row.getElementCount() - 1;
            if (lastColIndex > 0)
            {
                int cIndex = Util.getElementIndex(cell); // getColNumber(cell);
                int offset = -1; // adjacent cell is left of current cell
                if (cIndex == 0)
                { // if current cell is in first column...
                    offset *= -1; // ...adjacent cell is right of current cell
                }
                Object attrC = cell.getAttributes().getAttribute(
                        CSS.Attribute.WIDTH);
                Object attrA = row.getElement(cIndex + offset).getAttributes()
                        .getAttribute(CSS.Attribute.WIDTH);
                SimpleAttributeSet set = null;
                if (attrC != null && attrA != null)
                {
                    // LengthValue lvC = new LengthValue(attrC);
                    // LengthValue lvA = new LengthValue(attrA);
                    int widthC = (int) Util.getAbsoluteAttrVal(attrC); // Util.getAttrValue(attrC);
                    String cUnit = Util.getLastAttrUnit();
                    // String cUnit = lvC.getUnit();
                    int widthA = (int) Util.getAbsoluteAttrVal(attrA); // Util.getAttrValue(attrA);
                    String aUnit = Util.getLastAttrUnit();
                    if (aUnit.equalsIgnoreCase(cUnit))
                    {
                        int width = 0;
                        width += widthC;
                        width += widthA;
                        if (width > 0)
                        {
                            String widthString = Integer.toString(width)
                                    + cUnit;
                            set = new SimpleAttributeSet(row.getElement(
                                    cIndex + offset).getAttributes());
                            Util.styleSheet().addCSSAttribute(set,
                                    CSS.Attribute.WIDTH, widthString);
                        }
                    }
                }
                Element table = row.getParentElement();
                MyHTMLDocument doc = getDoc();
                if (cIndex < lastColIndex) offset = 0;
                for (int rIndex = table.getElementCount() - 1; rIndex >= 0; rIndex--)
                {
                    row = table.getElement(rIndex);
                    try
                    {
                        doc.removeElements(row, cIndex, 1);
                        /*
                         * the following line does not work for the last column
                         * in a table so we use above code instead
                         * 
                         * removeElement(row.getElement(cIndex));
                         */
                    }
                    catch (BadLocationException ble)
                    {
                        Util.errMsg(null, ble.getMessage(), ble);
                    }
                    if (set != null)
                    {
                        doc.addAttributes(row.getElement(cIndex + offset), set);
                    }
                    // adjustColumnBorders(table.getElement(0).getElement(cIndex
                    // + offset));
                }
            }
        }
    }

    /**
     * refresh the whole contents of this editor pane with brute force
     */
    void refresh()
    {
        int pos = getCaretPosition();
        String data = getText();
        setText("");
        setText(data);
        setCaretPosition(pos);
    }

    protected UpdatablePopupMenu getPopup()
    {
        if (popupMenu == null) createPopup();
        return popupMenu;
    }

    protected UpdatablePopupMenu createPopup()
    {
        if (popupMenu != null) return popupMenu;
        popupMenu = new UpdatablePopupMenu();
        MyHTMLEditorKit htmlKit = new MyHTMLEditorKit();

        JMenu menu = new JMenu("Table");
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.formatTableAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableRowAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableColAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.appendTableRowAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.appendTableColAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.deleteTableRowAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.deleteTableColAction)));
        popupMenu.add(menu);
        menu = new JMenu("List");
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.unorderedListAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.orderedListAction)));
        menu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.listFormatAction)));
        popupMenu.add(menu);
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.refreshAction)));
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem(MyHTMLEditorKit.undo));
        popupMenu.add(new JMenuItem(MyHTMLEditorKit.redo));
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem(htmlKit
                .getActionByName(MyHTMLEditorKit.elementsTreeAction)));
        return popupMenu;
    }

    public AttributeSet getMaxAttributes(String elemName)
    {
        Element e = getDoc().getCharacterElement(getSelectionStart());
        if (elemName != null && elemName.length() > 0)
        {
            e = Util.findElementUp(elemName, e);
        }
        StyleSheet s = getDoc().getStyleSheet();
        return getMaxAttributes(e, s);
    }

    public static AttributeSet getMaxAttributes(Element e, StyleSheet s)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        Element cElem = e;
        AttributeSet attrs;
        Vector<Element> elements = new Vector<Element>();
        Object classAttr;
        String styleName;
        String elemName;
        while (e != null)
        {
            elements.insertElementAt(e, 0);
            e = e.getParentElement();
        }
        for (int i = 0; i < elements.size(); i++)
        {
            e = (Element) elements.elementAt(i);
            classAttr = e.getAttributes().getAttribute(HTML.Attribute.CLASS);
            elemName = e.getName();
            styleName = elemName;
            if (classAttr != null)
            {
                styleName = elemName + "." + classAttr.toString();
                a.addAttribute(HTML.Attribute.CLASS, classAttr);
            }
            // System.out.println("getMaxAttributes name=" + styleName);
            attrs = s.getStyle(styleName);
            if (attrs != null)
            {
                a.addAttributes(Util.resolveAttributes(attrs));
            }
            else
            {
                attrs = s.getStyle(elemName);
                if (attrs != null)
                {
                    a.addAttributes(Util.resolveAttributes(attrs));
                }
            }
            a.addAttributes(Util.resolveAttributes(e.getAttributes()));
        }
        if (cElem != null)
        {
            // System.out.println("getMaxAttributes cElem.name=" +
            // cElem.getName());
            a.addAttributes(cElem.getAttributes());
        }
        return new AttributeMapper(a)
                .getMappedAttributes(AttributeMapper.toJava);
    }

    public static final Object FOCUSED_COMPONENT = new StringBuilder(
            "HTMLEditor_FocusedComponent");

    public static final HTMLEditor getFocusedEditor()
    {
        return (HTMLEditor) AppContext.getAppContext().get(FOCUSED_COMPONENT);
    }

    public static final void setFocusedEditor(HTMLEditor ui)
    {
        AppContext.getAppContext().put(FOCUSED_COMPONENT, (HTMLEditor) ui);
    }

    static class MyHyperlinkListener implements HyperlinkListener
    {

        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent)
                {
                    HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                    HTMLDocument doc = (HTMLDocument) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                }
                else
                {
                    try
                    {
                        String url = e.getURL() != null ? e.getURL().toString()
                                : e.getDescription();
                        BrowserLauncher.openURL(url);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    protected static class PopupListener extends MouseInputAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
            {
                if (!(e.getComponent() instanceof HTMLEditor)) return;
                HTMLEditor ed = (HTMLEditor) e.getComponent();
                if (ed.getPopup().isVisible())
                {
                    ed.getPopup().setVisible(false);
                }
                else
                {
                    ed.getPopup().update();
                    Frame f = GUIUtil.getFrame(e.getComponent());
                    Point pt = SwingUtilities.convertPoint(e.getComponent(), e
                            .getX(), e.getY(), f);
                    pt = GUIUtil.adjustPointInPicollo(ed, pt);
                    ed.getPopup().show(f, pt.x, pt.y);
                }
            }
        }
    }
}
