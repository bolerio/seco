/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import seco.gui.GUIUtilities;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.NotifyDescriptor;
import seco.gui.dialog.FindDialog;
import seco.gui.dialog.FontDialog;
import seco.gui.menu.CellLangProvider;
import seco.notebook.FontEx;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.notebook.NotebookEditorKit.BaseAction;
import seco.notebook.NotebookEditorKit.FindReplaceAction;
import seco.notebook.html.view.HTMLTableView;
import seco.notebook.html.view.InvisibleView;
import seco.notebook.view.HtmlView;
import seco.things.CellUtils;
import seco.util.IconManager;


public class MyHTMLEditorKit extends HTMLEditorKit
{
    public static final String elementsTreeAction = "Document Tree";
    public static final String refreshAction = "Refresh";
    public static final String sourceAction = "Source";
    public static final String formatTableAction = "Format Table";
    public static final String deleteTableColAction = "Delete Table Column";
    public static final String deleteTableRowAction = "Delete Table Row";
    public static final String insertTableColAction = "Insert Table Column";
    public static final String insertTableRowAction = "Insert Table Row";
    public static final String appendTableColAction = "Append Table Column";
    public static final String appendTableRowAction = "Append Table Row";
    public static final String tableAction = "tableAction";
    public static final String imageAction = "imageAction";
    public static final String linkAction = "linkAction";
    public static final String listFormatAction = "Format List";
    public static final String fontAction = "fontAction";
    public static final String superscriptAction = "superscript";
    public static final String strikeAction = "strikethrough";
    public static final String subscriptAction = "subscript";
    public static final String orderedListAction = "Ordered List";
    public static final String unorderedListAction = "Unordered List";
    public static final String indentLeftAction = "Left Indent";
    public static final String indentRightAction = "Right Indent";
    public static final String clearFormatAction = "Clear Format";
    public static final String undoAction = "Undo";
    public static final String redoAction = "Redo";
    public static final String insertSymbolAction = "Insert Special Symbol";
    public static final String showInputTypePopup = "showInputTypePopup";
    public static final String replaceAction = "Replace...";
    public static final String findAction = "Find...";
    
    static UndoAction undo = new UndoAction();
    static RedoAction redo = new RedoAction();
    private static final Action[] defaultActions = { undo, redo,
            new SourceAction(), new RefreshAction(), new LinkAction(),
            new ImageAction(), new FontAction(), new ListFormatAction(),
            new TableAction(), new FormatTableAction(),
            new InsertTableColAction(), new InsertTableRowAction(),
            new AppendTableColAction(), new AppendTableRowAction(),
            new DeleteTableColAction(), new DeleteTableRowAction(),
            new InsertSymbolAction(),
            new InsertListAction(unorderedListAction, false),
            new InsertListAction(orderedListAction, true),
            new IndentAction(true), new IndentAction(false),
            new ClearFormatAction(), new ElementsTreeAction(),
            new DeleteNextCharAction(), new DeletePrevCharAction(),
            new InsertBreakAction(), new ShowInputTypePopupAction(),  
            new FindReplaceAction(true),
            new FindReplaceAction(false)};
    private static HashMap<String, Action> actions;

    public MyHTMLEditorKit()
    {
        createActionTable();
    }

    public Action[] getActions()
    {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }

    private void createActionTable()
    {
        if (actions != null)
            return;
        actions = new HashMap<String, Action>();
        Action[] actionsArray = getActions();
        for (int i = 0; i < actionsArray.length; i++)
        {
            Action a = actionsArray[i];
            actions.put((String) a.getValue(Action.NAME), a);
        }
        for (int i = 0; i < AlignAction.NAMES.length; i++)
            actions.put(AlignAction.NAMES[i], new AlignAction(i));
        for (int i = 0; i < SubSupStrikeAction.TYPES.length; i++)
            actions.put("" + SubSupStrikeAction.TYPES[i], new SubSupStrikeAction(i));

    }

    public Action getActionByName(String name)
    {
        return (Action) (actions.get(name));
    }

    public Document createDefaultDocument()
    {
        StyleSheet ss = new StyleSheet();
        try
        {
            ss.importStyleSheet(Class.forName(
                    "javax.swing.text.html.HTMLEditorKit").getResource(
                    DEFAULT_CSS));
        }
        catch (Exception e)
        {
        }
        MyHTMLDocument doc = new MyHTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

    @Override
    public void write(Writer out, Document doc, int pos, int len)
            throws IOException, BadLocationException
    {
        if (doc instanceof HTMLDocument)
        {
            MyHTMLWriter w = new MyHTMLWriter(out, (HTMLDocument) doc, pos, len);
            w.write();
        } else
            super.write(out, doc, pos, len);
    }

    /** Shared factory for creating HTML Views. */
    private static final ViewFactory defaultFactory = new MyHTMLFactory();

    /**
     * Fetch a factory that is suitable for producing views of any models that
     * are produced by this kit.
     * 
     * @return the factory
     */
    public ViewFactory getViewFactory()
    {
        return defaultFactory;
    }

    public static class MyHTMLFactory extends HTMLEditorKit.HTMLFactory
            implements ViewFactory
    {
        public View create(Element elem)
        {
            View view = null;
            Object o = elem.getAttributes().getAttribute(
                    StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag)
            {
                HTML.Tag kind = (HTML.Tag) o;
                if (kind == HTML.Tag.TABLE)
                {
                    view = new HTMLTableView(elem);
                } else if (kind == HTML.Tag.COMMENT)
                {
                    view = new InvisibleView(elem);
                } else if (kind instanceof HTML.UnknownTag)
                {
                    view = new InvisibleView(elem);
                } else
                {
                    view = super.create(elem);
                }
            } else
            {
                view = new LabelView(elem);
            }
            return view;
        }
    }

    public static class UndoAction extends StyledTextAction
    {
        public UndoAction()
        {
            super(undoAction);
            setEnabled(false);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Undo16.gif"));
        }

        public void actionPerformed(ActionEvent e)
        {
            JEditorPane pane = getEditor(e);
            if (pane == null || !(pane instanceof HTMLEditor))
                return;
            HTMLEditor editor = (HTMLEditor) pane;
            if (editor == null)
                return;
            try
            {
                editor.getUndoManager().undo();
            }
            catch (CannotUndoException ex)
            {
                // System.out.println("Unable to removeUndo: " + ex);
                // ex.printStackTrace();
            }
            updateUndoState(editor.getUndoManager());
            redo.updateRedoState(editor.getUndoManager());
        }

        void updateUndoState(UndoManager undo)
        {
            setEnabled(undo.canUndo());
            putValue(Action.NAME, (undo.canUndo()) ? undo
                    .getUndoPresentationName() : "Undo");
        }
    }

    public static class RedoAction extends StyledTextAction
    {
        public RedoAction()
        {
            super(redoAction);
            setEnabled(false);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Redo16.gif"));
        }

        public void actionPerformed(ActionEvent e)
        {
            JEditorPane pane = getEditor(e);
            if (pane == null || !(pane instanceof HTMLEditor))
                return;
            HTMLEditor editor = (HTMLEditor) pane;
            try
            {
                editor.getUndoManager().redo();
            }
            catch (CannotRedoException ex)
            {
                // ex.printStackTrace();
            }
            updateRedoState(editor.getUndoManager());
            undo.updateUndoState(editor.getUndoManager());
        }

        void updateRedoState(UndoManager undo)
        {
            setEnabled(undo.canRedo());
            putValue(Action.NAME, (undo.canRedo()) ? undo
                    .getRedoPresentationName() : "Redo");
        }
    }

    public static class InsertListAction extends BaseAction
    {
        private boolean orderedOrUnordered;
              
        public InsertListAction()
        {
            super(unorderedListAction);
        }
        
        public InsertListAction(String sLabel, boolean orderedOrUnordered)
        {
            super(sLabel);
            this.orderedOrUnordered = orderedOrUnordered;
            String path = (orderedOrUnordered) ? "ul.gif" : "ol.gif";
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon(path));
        }

        public void actionPerformed(ActionEvent e)
        {
            super.actionPerformed(e);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            // System.out.println("InsertListAction: " + event);
            Element el = HTMLUtils.getListTag(editor);
            if (el != null)
                HTMLUtils.listOff(editor, HTMLUtils.getName(el));
            else
                HTMLUtils.listOn(editor,orderedOrUnordered?
                        HTML.Tag.UL : HTML.Tag.OL);
        }

        @Override
        protected String getUndoName()
        {
            return "List Manipulation";
        }

        public boolean isOrderedOrUnordered()
        {
            return orderedOrUnordered;
        }

        public void setOrderedOrUnordered(boolean orderedOrUnordered)
        {
            this.orderedOrUnordered = orderedOrUnordered;
        }
    }

    public static class SubSupStrikeAction extends BaseSelAction
    {
        public static final int sub = 0;
        public static final int sup = 1;
        public static final int strike = 2;
        public static final Object[] TYPES = new Object[]{
            StyleConstants.Subscript, StyleConstants.Superscript, StyleConstants.StrikeThrough};
        private int type;
        public SubSupStrikeAction(){
            super(superscriptAction);
        }
        
        public SubSupStrikeAction(int type)
        {
            super(superscriptAction);
            setType(type);
        }

        protected void actionEx(HTMLEditor editor, String sel)
        {
            StyledEditorKit kit = getStyledEditorKit(editor);
            MutableAttributeSet attr = kit.getInputAttributes();
            Boolean b = (Boolean) attr.getAttribute(TYPES[type]);
            boolean exists = (b == null) ? false : b.booleanValue();
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(TYPES[type], Boolean.valueOf(!exists));
            setCharacterAttributes(editor, sas, false);
        }

        public int getType()
        {
            return type;
        }

        public void setType(int type)
        {
            this.type = type;
            String path = "Super16.gif";
            if (type == 1)
                path = "Sub16.gif";
            else if (type == 2)
                path = "Strike16.gif";
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon(path));
            putValue(Action.NAME, "" + TYPES[type]);
        }

       
    }

    public static class FontAction extends BaseAction
    {
        public FontAction()
        {
            super(fontAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Font16.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            AttributeSet attr = editor.getMaxAttributes(null);
            FontEx f = FontEx.fromCSSAttribs(attr);
            FontDialog dlg = new FontDialog(GUIUtilities.getFrame(editor), f,
                    StyleConstants.getForeground(attr), MyHTMLWriter.fontSizes);
            dlg.setVisible(true);
            if (dlg.succeeded())
            {
                MutableAttributeSet res = new SimpleAttributeSet();
                dlg.getFont().populateCSSStyle(res);
                StyleConstants.setForeground(res, dlg.getFontColor());
                setCharacterAttributes(editor, res, false);
            }
        }
    }

    public static class InsertSymbolAction extends BaseAction
    {
        private static UnicodeDialog dlg;

        public InsertSymbolAction()
        {
            super(insertSymbolAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Symbol.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            if (dlg == null)
                dlg = new UnicodeDialog(editor, "Insert Special Symbol", true,
                        UnicodeDialog.UNICODE_MATH);
            dlg.setVisible(true);
        }
    }

    public static class ListFormatAction extends BaseAction
    {
        public ListFormatAction()
        {
            super(listFormatAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            Element list = HTMLUtils.getListTag(editor);
            if (list == null)
                return;
            AttributeSet attr = list.getAttributes();
            ListPanel panel = new ListPanel();
            panel.setValue(attr);
            DialogDescriptor dd = new DialogDescriptor(null, panel,
                    "List Format");
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                editor.getDoc().addAttributes(list, panel.getValue());
            }
        }
    }

    public static class IndentAction extends BaseAction
    {
        private boolean leftIndent = false;
        private final static int INDENT = 20;

        public IndentAction(){
            super(indentLeftAction);
        }
        
        public IndentAction(boolean left_indent)
        {
            super((left_indent) ? indentLeftAction : indentRightAction);
            this.leftIndent = left_indent;
            putValue(Action.SMALL_ICON, HTMLUtils
                    .resolveIcon((left_indent) ? "IndentLeft16.gif"
                            : "IndentRight16.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            int off = editor.getCaretPosition();
            Element list = HTMLUtils.getListTag(editor);
            if (list == null)
            {
                list = editor.getDoc().getCharacterElement(off)
                        .getParentElement();
            }
            if (list == null)
                return;
            AttributeSet attr = list.getAttributes();
            float f = -1;
            if (attr.getAttribute(CSS.Attribute.MARGIN_LEFT) != null)
                f = Util.getAttrValue(attr
                        .getAttribute(CSS.Attribute.MARGIN_LEFT));
            if (f < 0 && leftIndent)
                return;
            f = (leftIndent) ? f - INDENT : f + INDENT;
            if (leftIndent && f < 0)
                f = 0;
            SimpleAttributeSet set = new SimpleAttributeSet(attr);
            Util.styleSheet().addCSSAttribute(set, CSS.Attribute.MARGIN_LEFT,
                    "" + f);
            // System.out.println("IndentAction: " + f);
            editor.getDoc().addAttributes(list, set);
        }

        public boolean isLeftIndent()
        {
            return leftIndent;
        }

        public void setLeftIndent(boolean leftIndent)
        {
            this.leftIndent = leftIndent;
        }
    }

    public static class AlignAction extends BaseAction
    {
        public static final int left = 0;
        public static final int right = 1;
        public static final int center = 2;
        public static final int justify = 3;
        public static final String[] NAMES = new String[]{
            "left", "right", "center", "justify"};

        private int align;

        public AlignAction(){
            super("left");
        }
        
        public AlignAction(int align)
        {
            super("left");
            setAlign(align);
            String icon = "";
            String tip = "";
            switch (align)
            {
            case left:
            {
                icon = "algnLft.gif";
                tip = "Left Align";
                break;
            }
            case center:
            {
                icon = "algnCtr.gif";
                tip = "Align Center";
                break;
            }
            case right:
            {
                icon = "algnRt.gif";
                tip = "Right Align";
                break;
            }
            case justify:
            {
                icon = "algnJu.gif";
                tip = "Justify";
                break;
            }
            }
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon(icon));
            putValue(Action.SHORT_DESCRIPTION, tip);
            putValue(Action.NAME, NAMES[align]);
      
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            MyHTMLDocument doc = editor.getDoc();
            doc.beginCompoundEdit("Align");
            MutableAttributeSet set = new SimpleAttributeSet();
            Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_ALIGN,
                    NAMES[align]);
            editor.setAttributeSet(set, true);
        }

        public int getAlign()
        {
            return align;
        }

        public void setAlign(int align)
        {
           this.align = align;
        }
    }

    public static class ClearFormatAction extends BaseSelAction
    {
        public ClearFormatAction()
        {
            super(clearFormatAction);
            putValue(Action.SMALL_ICON, HTMLUtils
                    .resolveIcon("ClearFormat16.gif"));
        }

        protected void actionEx(HTMLEditor editor, String sel)
        {
            try
            {
                int caretOffset = editor.getSelectionStart();
                editor.getDoc().remove(caretOffset, sel.length());
                editor.getDoc().insertString(caretOffset, sel, null);
            }
            catch (Exception ex)
            {
            }
        }
    }

    public static class LinkAction extends BaseSelAction
    {
        public LinkAction()
        {
            super(linkAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("link.gif"));
        }

        protected void actionEx(HTMLEditor editor, String sel)
        {
            NotifyDescriptor.InputLine dd = new NotifyDescriptor.InputLine(
                    GUIUtilities.getFrame(editor), "Link Target:",
                    "Link Target");
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                editor.setLink(sel, dd.getInputText(), null, null);
            }
        }
    }

    public static class ImageAction extends BaseAction
    {
        public ImageAction()
        {
            super(imageAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("image.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            Frame parent = GUIUtilities.getFrame(editor);
            File f = new File(".");
            try
            {
                f = new File(editor.getDoc().getBase().toURI());
            }
            catch (Exception ex)
            {
            }
            ImageDialog dlg = new ImageDialog(parent, "Insert/Edit Image", f);
            boolean edit = false;
            Element img = editor.getDoc().getCharacterElement(
                    editor.getCaretPosition());
            if (img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString()))
            {
                dlg.setImageAttributes(img.getAttributes());
                edit = true;
            }
            System.out.println("Img: " + img + ":" + edit);
            dlg.setModal(true);
            dlg.setVisible(true);
            if (dlg.getResult() == DialogShell.RESULT_OK)
            {
                try
                {
                    if (edit)
                        editor.getDoc().setOuterHTML(img, dlg.getImageHTML());
                    else
                        editor.getDoc().insertBeforeStart(
                                editor.getDoc().getCharacterElement(
                                        editor.getSelectionEnd()),
                                dlg.getImageHTML());
                }
                catch (Exception ex)
                {
                    Util.errMsg(null, ex.getMessage(), ex);
                }
            }
        }
    }

    public static class TableAction extends BaseAction
    {
        public TableAction()
        {
            super(tableAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("table.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            NotifyDescriptor.InputLine dd = new NotifyDescriptor.InputLine(
                    GUIUtilities.getFrame(editor), "Number of Columns: ",
                    "Insert Table");
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                editor.insertTable(Integer.parseInt(dd.getInputText()));
            }
        }
    }

    public static class FormatTableAction extends BaseAction
    {
        public FormatTableAction()
        {
            super(formatTableAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("fmtTable.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            Element table = HTMLUtils.getTag(editor, HTML.Tag.TABLE, editor
                    .getCaretPosition());
            Element cell = HTMLUtils.getTag(editor, HTML.Tag.TD, editor
                    .getCaretPosition());
            if (table == null || cell == null)
                return;
            TableDialog td = new TableDialog(GUIUtilities.getFrame(editor),
                    "Format Table");
            td.setTableAttributes(table.getAttributes());
            td.setCellAttributes(cell.getAttributes());
            td.setModal(true);
            td.setVisible(true);
            if (td.getResult() == DialogShell.RESULT_OK)
            {
                AttributeSet a = td.getTableAttributes();
                if (a.getAttributeCount() > 0)
                    editor.applyTableAttributes(a);
                a = td.getCellAttributes();
                if (a.getAttributeCount() > 0)
                    editor.applyCellAttributes(a, td.getCellRange());
            }
        }
    }

    public static class InsertTableColAction extends BaseAction
    {
        public InsertTableColAction()
        {
            super(insertTableColAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("insCol.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.insertTableColumn();
        }
    }

    public static class InsertTableRowAction extends BaseAction
    {
        public InsertTableRowAction()
        {
            super(insertTableRowAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("insRow.gif"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.insertTableRow();
        }
    }

    public static class AppendTableColAction extends BaseAction
    {
        public AppendTableColAction()
        {
            super(appendTableColAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.appendTableColumn();
        }
    }

    public static class AppendTableRowAction extends BaseAction
    {
        public AppendTableRowAction()
        {
            super(appendTableRowAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.appendTableRow();
        }
    }

    public static class DeleteTableColAction extends BaseAction
    {
        public DeleteTableColAction()
        {
            super(deleteTableColAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.deleteTableCol();
        }
    }

    public static class DeleteTableRowAction extends BaseAction
    {
        public DeleteTableRowAction()
        {
            super(deleteTableRowAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.deleteTableRow();
        }
    }

    public static class SourceAction extends BaseAction
    {
        public SourceAction()
        {
            super(sourceAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Source.png"));
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            if (editor instanceof HtmlView.InnerHTMLEditor)
            {
                Element el = ((HtmlView.InnerHTMLEditor) editor).getElement();
                CellUtils.toggleAttribute(NotebookDocument.getNBElement(el),
                        XMLConstants.ATTR_HTML);
            }
//            else 
//            {
//                
//                Element el = ((NotebookDocument) editor.getDocument())
//                        .getEnclosingCellElement(editor.getCaretPosition());
//                if(el != null)
//                   CellUtils.toggleAttribute(NotebookDocument.getNBElement(el),
//                        XMLConstants.ATTR_HTML);
//            }
        }

        public void setEnabled(boolean enabled)
        {
            // always enabled
        }
    }

    public static class RefreshAction extends BaseAction
    {
        public RefreshAction()
        {
            super(refreshAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            editor.refresh();
        }
    }

    public static class ElementsTreeAction extends BaseAction
    {
        public ElementsTreeAction()
        {
            super(elementsTreeAction);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            JDialog dialog = new JDialog(GUIUtilities.getFrame(editor),
                    "Elements");
            dialog.setSize(500, 800);
            // JTree tree = new JTree((TreeNode) editor.getDocument()
            // .getDefaultRootElement());
            // JScrollPane pane = new JScrollPane(tree);
            ElementTreePanel pane = new ElementTreePanel(editor);
            dialog.add(pane);
            dialog.setVisible(true);
        }
    }

    public static class DeleteNextCharAction extends BaseAction
    {
        /* Create this object with the appropriate identifier. */
        DeleteNextCharAction()
        {
            super(deleteNextCharAction);
        }

        /** The operation to perform when this action is triggered. */
        public void action(final HTMLEditor editor) throws Exception
        {
            MyHTMLDocument doc = editor.getDoc();
            Caret caret = editor.getCaret();
            final int dot = caret.getDot();
            int mark = caret.getMark();
            if (dot != mark)
            {
                doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
            } else if (dot < doc.getLength())
            {
                if (special_deal(editor, dot))
                    return;
                int delChars = 1;
                if (dot < doc.getLength() - 1)
                {
                    String dotChars = doc.getText(dot, 2);
                    char c0 = dotChars.charAt(0);
                    char c1 = dotChars.charAt(1);
                    if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00'
                            && c1 <= '\uDFFF')
                    {
                        delChars = 2;
                    }
                }
                doc.remove(dot, delChars);
            }
        }

        private boolean special_deal(final HTMLEditor editor, final int dot)
                throws Exception
        {
            Element el = HTMLUtils.getTag(editor, HTML.Tag.LI, dot);
            if (el == null)
                return false;
            Element parent = el.getParentElement();
            // System.out.println("DelNextChar: " + el + ":" + parent + ":" +
            // dot);
            if (el.getEndOffset() - 1 != dot)
                return false;
            // last <li> in list
            if (el.getEndOffset() == parent.getEndOffset())
            {
                HTMLUtils.setCaretPosInAWT(editor, dot + 1);
            } else
            // merge the next <li> in list
            {
                Element next = HTMLUtils.getTag(editor, HTML.Tag.LI, dot + 1);
                MyHTMLDocument doc = editor.getDoc();
                String text = HTMLUtils.putInHTML(doc, next, HTML.Tag.SPAN);
                HTMLUtils.removeTag(editor, next);
                doc.insertBeforeEnd(el.getElement(0), text);
            }
            return true;
        }
    }

    public static class DeletePrevCharAction extends BaseAction
    {
        public DeletePrevCharAction()
        {
            super(deletePrevCharAction);
        }

        public void action(final HTMLEditor editor) throws Exception
        {
            Document doc = editor.getDocument();
            Caret caret = editor.getCaret();
            int dot = caret.getDot();
            int mark = caret.getMark();
            if (dot != mark)
            {
                doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                //HTMLUtils.setCaretPosInAWT(editor, dot - 1);
            } else if (dot > 0)
            {
                if (special_deal(editor, dot))
                    return;
                int delChars = 1;
                if (dot > 1)
                {
                    String dotChars = doc.getText(dot - 2, 2);
                    char c0 = dotChars.charAt(0);
                    char c1 = dotChars.charAt(1);
                    if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00'
                            && c1 <= '\uDFFF')
                    {
                        delChars = 2;
                    }
                }
                doc.remove(dot - delChars, delChars);
                //setCaretPosInAWT0(editor, dot - 1);
            }
            HTMLUtils.setCaretPosInAWT(editor, dot - 1);
        }
        
//        static void setCaretPosInAWT0(final HTMLEditor editor, final int pos){
//            
//            SwingUtilities.invokeLater(new Runnable(){
//                public void run(){
//                    ((DefaultCaret)editor.getCaret()).setDot(pos, Position.Bias.Backward); //.setCaretPosition(pos);
//                }
//            });
//        }

        private boolean special_deal(final HTMLEditor editor, final int dot)
                throws Exception
        {
            if (HTMLUtils.isInTag(editor, HTML.Tag.TABLE))
            {
                editor.setCaretPosition(dot - 1);
                return true;
            }
            Element el = HTMLUtils.getTag(editor, HTML.Tag.LI, dot);
            if (el == null)
                return false;
            Element parent = el.getParentElement();
            // System.out.println("DelPrevChar: " + el + ":" + parent + ":" +
            // dot);
            if (el.getStartOffset() != dot)
                return false;
            MyHTMLDocument doc = editor.getDoc();
            // first <li> in list
            if (el.getStartOffset() == parent.getStartOffset())
            {
                String text = HTMLUtils.putInHTML(doc, el, HTML.Tag.P);
                HTMLUtils.removeTag(editor, el);
                doc.insertBeforeStart(parent, text);
            } else
            // merge the next <li> in list
            {
                Element prev = HTMLUtils.getTag(editor, HTML.Tag.LI, dot - 1);
                String text = HTMLUtils.putInHTML(doc, el, HTML.Tag.SPAN);
                HTMLUtils.removeTag(editor, el);
                doc.insertBeforeEnd(prev.getElement(0), text);
                HTMLUtils.setCaretPosInAWT(editor, dot - 1);
            }
            return true;
        }
    }

    public static class InsertBreakAction extends BaseAction
    {
        public InsertBreakAction()
        {
            super(insertBreakAction);
        }

        public void action(final HTMLEditor editor) throws Exception
        {
            final int pos = editor.getCaretPosition();
            if (HTMLUtils.getListTag(editor) == null)
            // editor.getSelectionStart() != editor.getSelectionEnd())
            {
                editor.replaceSelection("\n");
                HTMLUtils.setCaretPosInAWT(editor, pos + 1);
                return;
            }
            Element elem = HTMLUtils.getTag(editor, HTML.Tag.LI);
            int so = elem.getStartOffset();
            int eo = elem.getEndOffset();
            char[] temp = editor.getText(so, eo - so).toCharArray();
            boolean content = false;
            for (int i = 0; i < temp.length; i++)
            {
                if (!Character.isWhitespace(temp[i]))
                    content = true;
            }
            if (content && HTMLUtils.isNBSP(temp))
                content = false;
            // System.out.println("InsertBreakAction - list: " + content + ":"
            // + elem);
            if (elem.getStartOffset() == elem.getEndOffset() || !content)
            {
                editor.removeListItem(elem);
            } else
            {
                if (editor.getCaretPosition() + 1 == elem.getEndOffset())
                {
                    editor.insertListItem(elem, null);
                } else
                {
                    String tempString = editor.getText(pos, eo - pos);
                    tempString = HTMLUtils.putInHTML(tempString, editor
                            .getCharacterAttributes(), HTML.Tag.SPAN);
                    editor.select(pos, eo - 1);
                    editor.replaceSelection("");
                    editor.insertListItem(elem, tempString);
                }
                HTMLUtils.setCaretPosInAWT(editor, pos + 1);
            }
        }
    }
    
    public static class WrapperAction extends StyledEditorKit.StyledTextAction
    {
        public WrapperAction(String actionName)
        {
            super(actionName);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (!(this.isEnabled()))  return;
            JEditorPane pane = getEditor(e);
            if (pane == null || !pane.isEditable()
                    || !(pane instanceof HTMLEditor))
                return;
            HTMLEditor editor = (HTMLEditor) pane;
            if (editor == null)
                return;
        }
    }
    
    public static abstract class BaseAction extends
            StyledEditorKit.StyledTextAction
    {
        public BaseAction(String actionName)
        {
            super(actionName);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!(this.isEnabled()))  return;
            JEditorPane pane = getEditor(e);
            if (pane == null || !pane.isEditable()
                    || !(pane instanceof HTMLEditor))
                return;
            HTMLEditor editor = (HTMLEditor) pane;
            if (editor == null)
                return;
            editor.getDoc().beginCompoundEdit(getUndoName());
            try
            {
                int p1 = editor.getSelectionStart();
                int p2 = editor.getSelectionEnd();
                action(editor);
                editor.select(p1, p2);
                editor.requestFocusInWindow();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }
            finally
            {
                editor.getDoc().endCompoundEdit();
            }
        }

        protected String getUndoName()
        {
            return (String) getValue(Action.NAME);
        }

        abstract protected void action(HTMLEditor editor) throws Exception;
    }

    public static abstract class BaseSelAction extends BaseAction
    {
        public BaseSelAction(String actionName)
        {
            super(actionName);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            String selTextBase = editor.getSelectedText();
            if (selTextBase == null || selTextBase.length() <= 0)
                return;
            actionEx(editor, selTextBase);
        }

        abstract protected void actionEx(HTMLEditor editor, String sel);
    }

    public static class ShowInputTypePopupAction extends BaseAction
    {
        public ShowInputTypePopupAction()
        {
            super(showInputTypePopup);
        }

        protected void action(HTMLEditor editor) throws Exception
        {
            int pos = editor.getCaretPosition();
            Frame f = GUIUtilities.getFrame(editor);
            try
            {
                Rectangle rect = editor.modelToView(pos);
                Point pt = SwingUtilities.convertPoint(editor, rect.x, rect.y,
                        f);
                Collection<JMenuItem> items = CellLangProvider
                        .getLanguages(editor);
                JPopupMenu popupMenu = new JPopupMenu();
                for (JMenuItem item : items)
                    popupMenu.add(item);
                popupMenu.show(f, pt.x, pt.y);
            }
            catch (BadLocationException ex)
            {
            }
        }
    }
    
    public static class FindReplaceAction extends BaseAction
    {
        private static final long serialVersionUID = -5658596134377861525L;
        private static FindDialog findDialog;
        private boolean findOrReplace;

        public FindReplaceAction()
        {
            super(findAction);
        }

        public FindReplaceAction(boolean find_replace)
        {
            super((find_replace) ? findAction : replaceAction);
            setFindOrReplace(find_replace);
            putValue(Action.SHORT_DESCRIPTION, find_replace ? "Find"
                    : "Find And Replace");
        }

        protected void action(HTMLEditor ui) throws Exception
        {
            int index = findOrReplace ? 0 : 1;
            JEditorPane editor =
             (ui instanceof HtmlView.InnerHTMLEditor) ?
                ((HtmlView.InnerHTMLEditor) ui).getNotebookUI() : ui;
            if (findDialog == null) 
                findDialog = new FindDialog(editor, index);
            else
            {
                findDialog.setEditorPane(editor);
                findDialog.setSelectedIndex(index);
            }
            findDialog.setVisible(true);
            GUIUtilities.centerOnScreen(findDialog);
        }

        public boolean isFindOrReplace()
        {
            return findOrReplace;
        }

        public void setFindOrReplace(boolean findOrReplace)
        {
            this.findOrReplace = findOrReplace;
            putValue(Action.NAME, (findOrReplace) ? findAction : replaceAction);
            putValue(Action.SMALL_ICON, IconManager
                    .resolveIcon((findOrReplace) ? "Find16.gif"
                            : "Replace16.gif"));
        }
    }
}
