/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.notebook.NotebookDocument.UpdateAction;
import seco.notebook.gui.DialogDescriptor;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.FindDialog;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.JavaDocPanel;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.RuntimeContextPanel;
import seco.notebook.gui.menu.CellLangProvider;
import seco.notebook.html.HTMLUtils;
import seco.notebook.storage.ClassRepository;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.util.IconManager;
import seco.notebook.view.CellBoxView;
import seco.notebook.view.CellHandleView;
import seco.notebook.view.CellParagraphView;
import seco.notebook.view.ExpandHandleView;
import seco.notebook.view.FakeParagraphView;
import seco.notebook.view.HtmlView;
import seco.notebook.view.InlineView;
import seco.notebook.view.InputCellView;
import seco.notebook.view.InsertionPointView;
import seco.notebook.view.NotebookView;
import seco.notebook.view.ResizableComponentView;
import seco.notebook.view.ViewUtils;
import seco.notebook.view.WholeCellView;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

/**
 * 
 * @author bizi
 */
public class NotebookEditorKit extends StyledEditorKit
{
    private static final long serialVersionUID = -1;

    public static final String CONTENT_TYPE = "text/notebook";
    public static final String evalAction = "Eval Cell";
    public static final String removeOutputCellsAction = "Remove Output Cells";
    public static final String reEvalOutputCellsAction = "Evaluate All Cells";
    public static final String removeTabAction = "Untab";
    public static final String selectCellRangeAction = "Cell Range Selection";
    public static final String selectCellHandleAction = "Cell Handle Selection";
    public static final String evalCellGroupAction = "Eval CellGroup";
    public static final String undoAction = "Undo";
    public static final String redoAction = "Redo";
    public static UndoAction undo = new UndoAction();
    public static RedoAction redo = new RedoAction();
    public static final String escapeAction = "escape";
    public static final String formatAction = "format";
    public static final String deleteCellAction = "Delete Cell";
    public static final String deleteSelectedElementsAction = "Delete Selection";
    public static final String importAction = "Import";
    public static final String htmlAction = "HTML Source";
    public static final String showInputTypePopup = "showInputTypePopup";
    public static final String clearEngineContextAction = "Clear Engine Context";
    public static final String resetCellNumAction = "Normalize Cell Numbering";
    public static final String javaDocManagerAction = "JavaDoc Manager";
    public static final String ctxInspectorAction = "RuntimeContext Inspector";
    public static final String replaceAction = "Replace...";
    public static final String findAction = "Find...";
    public static final String mergeCellsAction = "Merge InputCells";

    static
    {
        JEditorPane.registerEditorKitForContentType(CONTENT_TYPE,
                NotebookEditorKit.class.getName());
    }
    private ViewFactory viewFactory;
    private static final Action[] defaultActions = { new EvalAction(),
            new ReEvalOutputCellsAction(), new EvalCellGroupAction(),
            new RemoveOutputCellsAction(), undo, redo, new EscapeAction(),
            new FormatAction(), new DeletePrevCharAction(),
            new DeleteNextCharAction(), new InsertTabAction(),
            new DeleteCellAction(), new DeleteSelectedElementsAction(),
            new SelectCellHandleAction(), new ImportAction(), new HTMLAction(),
            new ShowInputTypePopupAction(), new ClearEngineContextAction(),
            new ResetCellNumAction(), new JavaDocManagerAction(),
            new CtxInspectorAction(), new FindReplaceAction(true),
            new FindReplaceAction(false), new RemoveTabAction(),
            new MergeCellsAction(), new SelectWordAction(),
            new SelectLineAction(), new SelectAllAction(), };
    private static HashMap<String, Action> actions;

    /** Creates a new instance of NotebookEditorKit */
    public NotebookEditorKit()
    {
        createActionTable();
        viewFactory = new NBViewFactory(super.getViewFactory());
    }

    public String getContentType()
    {
        return CONTENT_TYPE;
    }

    public Action[] getActions()
    {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }

    private void createActionTable()
    {
        actions = new HashMap<String, Action>();
        Action[] actionsArray = getActions();
        for (int i = 0; i < actionsArray.length; i++)
        {
            Action a = actionsArray[i];
            actions.put((String) a.getValue(Action.NAME), a);
        }
    }

    public Action getActionByName(String name)
    {
        return (Action) (actions.get(name));
    }

    public static final String DEFAULT_DOC_NAME = "NotebookEditorKit.DefaultDocumentName";
    public static final CellGroup DEFAULT_TOP_GROUP = new CellGroup(
            DEFAULT_DOC_NAME);
    static final HGPersistentHandle DOC_HANDLE = HGHandleFactory
            .makeHandle("50593b0e-d0c2-11dc-99fb-e94ae2f056ca");

    public Document createDefaultDocument()
    {
        return getDefaultDocument();
    }

    public static NotebookDocument getDefaultDocument()
    {
        NotebookDocument doc = (NotebookDocument) ThisNiche.graph.get(DOC_HANDLE);
        if (doc != null) return doc;

        doc = new NotebookDocument(ThisNiche.graph.add(DEFAULT_TOP_GROUP),
                ThisNiche.getTopContext());
        System.out.println("Adding DOC: " + DEFAULT_TOP_GROUP);
        ThisNiche.graph.define(DOC_HANDLE, doc);
        doc.init();
        return doc;
    }

    public ViewFactory getViewFactory()
    {
        return viewFactory;
    }

    private static class NBViewFactory implements ViewFactory
    {
        private ViewFactory base;

        public NBViewFactory(ViewFactory base)
        {
            this.base = base;
        }

        public View create(Element elem)
        {
            Object o = elem.getAttributes().getAttribute(
                    StyleConstants.NameAttribute);
            // System.out.println("ViewFactory: " + elem.getClass() + ":" + o +
            // ":" + elem.getName());
            if (o instanceof ElementType)
            {
                switch ((ElementType) o)
                {
                case notebook:
                    return new NotebookView(elem);
                case insertionPoint:
                    return new InsertionPointView(elem);
                case inputCellBox:
                case outputCellBox:
                case cellGroupBox:
                    return new CellBoxView(elem);
                case cellHandle:
                    return new CellHandleView(elem);
                case cellGroup:
                    // case wholeCell:
                    return new WholeCellView(elem);
                case commonCell:
                    return new InputCellView(elem);
                case paragraph:
                    return new CellParagraphView(elem);
                case component:
                    return new ResizableComponentView(elem);
                case expandHandle:
                    return new ExpandHandleView(elem);
                case charContent:
                    return new InlineView(elem);
                case htmlCell:
                    return new HtmlView(elem);
                case fakeParagraph:
                    return new FakeParagraphView(elem);
                default:
                    return new InlineView(elem);
                }
            }
            return base.create(elem);
        }
    }

    public static class EvalAction extends BaseAction
    {
        private static final long serialVersionUID = -1024678440877335429L;

        public EvalAction()
        {
            super(evalAction);
        }

        protected void action(final NotebookUI ui) throws Exception
        {
            final NotebookDocument doc = ui.getDoc();
            Element el = ui.getSelectedCellElement();
            if (el == null) return;
            int pos = ui.getCaretPosition();
            // expand the evaluated cell and it's parent
            View root = ui.getUI().getRootView(ui);
            View cellV = ViewUtils.getView(el.getEndOffset() - 1, root);
            Element containerEl = NotebookDocument.getContainerEl(el, false);
            View containerV = ViewUtils.getView(containerEl.getEndOffset() - 1,
                    root);
            if (cellV != null && cellV instanceof CellHandleView)
                ((CellHandleView) cellV).setCollapsed(false);
            if (containerV != null && containerV instanceof CellHandleView)
                ((CellHandleView) containerV).setCollapsed(false);

            doc.evalCellInAuxThread(el); 
          
            Utilities.adjustScrollBar(ui, pos, Position.Bias.Forward);
        }

        public boolean isEnabled(NotebookUI editor)
        {
            return editor.getSelectedCellElement() != null;
        }
    }

    public static class ImportAction extends BaseAction
    {
        private static final long serialVersionUID = 5146297563994337721L;

        public ImportAction()
        {
            super(importAction);
        }

        // TODO: add check to prevent this action in comments and other
        // unsuitable places
        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            int pos = ui.getCaretPosition();
            ScriptSupport sup = doc.getScriptSupport(pos);
            if (sup == null) return;
            int start = javax.swing.text.Utilities.getWordStart(ui, pos);
            int end = javax.swing.text.Utilities.getWordEnd(ui, pos);
            String cls = doc.getText(start, end - start);
            // System.out.println("IMPORT ACTION - sup: " + cls);
            Class<?>[] classes = ClassRepository.getInstance().findClass(cls);
            if (classes.length == 0) return;
            String res = null;
            if (classes.length > 1)
            {
                String names[] = new String[classes.length];
                for (int i = 0; i < classes.length; i++)
                    names[i] = classes[i].getName();
                JList list = new JList(names);
                list.setPreferredSize(new Dimension(200, 100));
                DialogDescriptor dd = new DialogDescriptor(GUIUtilities
                        .getFrame(ui), list, "Select the class to import");
                if (DialogDisplayer.getDefault().notify(dd) != NotifyDescriptor.OK_OPTION)
                    return;
                res = (String) list.getSelectedValue();
            }
            else
            {
                res = classes[0].getName();
            }
            doc.beginCompoundEdit("Package Import");
            ui.select(start, end);
            ui.replaceSelection(res);
            doc.endCompoundEdit();
        }
    }

    public static class RemoveOutputCellsAction extends BaseAction
    {
        private static final long serialVersionUID = -6254206194604430783L;

        public RemoveOutputCellsAction()
        {
            super(removeOutputCellsAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            CellGroup g = (CellGroup) doc.getBook();
            processCellGroup(doc, g);
        }

        private void processCellGroup(NotebookDocument doc, CellGroup g)
        {
            Set<HGHandle> rem = new HashSet<HGHandle>();
            for (int i = 0; i < g.getArity(); i++)
            {
                CellGroupMember m = g.getElement(i);
                if (m instanceof Cell && !CellUtils.isInputCell(m)) rem.add(g
                        .getTargetAt(i));
                else if (m instanceof CellGroup)
                    processCellGroup(doc, (CellGroup) m);
            }
            if (rem.size() == 0) return;
            CellGroupChangeEvent e = new CellGroupChangeEvent(ThisNiche
                    .handleOf(g), -1, new HGHandle[0], rem
                    .toArray(new HGHandle[rem.size()]));
            // g.batchProcess(e);
            doc.fireCellGroupChanged(e);
        }
    }

    public static class DeleteCellAction extends BaseAction
    {
        public DeleteCellAction()
        {
            super(deleteCellAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            Element el = doc.getEnclosingCellElement(ui.getCaretPosition());
            if (el == null) return;
            CellGroup p = (CellGroup) doc.getContainer(el);
            p.remove(NotebookDocument.getNBElement(el));
        }
    }

    public static class SelectCellHandleAction extends BaseAction
    {
        public SelectCellHandleAction()
        {
            super(selectCellHandleAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            ui.selectCellHandle(ui.getCaretPosition());
        }
    }

    public static class DeleteSelectedElementsAction extends BaseAction
    {
        public DeleteSelectedElementsAction()
        {
            super(deleteSelectedElementsAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            ui.deleteSelectedElements();
        }

        // TODO: should make some updatable actions
        public boolean isEnabled(NotebookUI editor)
        {
            if (editor != null)
            {
                Collection<Element> sel = editor.getSelectionManager()
                        .getSelection();
                return (sel.size() == 1 && NotebookDocument.getNBElement(sel
                        .iterator().next()) != null);
            }
            return false;
        }
    }

    public static class ReEvalOutputCellsAction extends BaseAction
    {
        public ReEvalOutputCellsAction()
        {
            super(reEvalOutputCellsAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            doc.beginCompoundEdit("Output Cells Eval");
            doc.update(UpdateAction.evalCells);
            doc.endCompoundEdit();
        }
    }

    public static class EvalCellGroupAction extends BaseAction
    {
        public EvalCellGroupAction()
        {
            super(evalCellGroupAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            Element gr = ui.getSelectedGroupElement();
            if (gr != null)
            {
                try
                {
                    doc.beginCompoundEdit("CellGroup Eval");
                    doc.updateGroup(NotebookDocument.getLowerElement(gr,
                            ElementType.cellGroup),
                            UpdateAction.evalCells, null);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    doc.endCompoundEdit();
                }
            }
        }

        public boolean isEnabled(NotebookUI editor)
        {
            return editor.getSelectedGroupElement() != null;
        }
    }

    public static class UndoAction extends BaseAction
    {
        public UndoAction()
        {
            super(undoAction);
            setEnabled(false);
            putValue(Action.SHORT_DESCRIPTION, "Undo Change");
        }

        protected void action(NotebookUI ui) throws Exception
        {
            if (!ui.getUndoManager().canUndo()) return;
            ui.getUndoManager().undo();
            updateUndoState(ui.getUndoManager());
            redo.updateRedoState(ui.getUndoManager());
        }

        public void updateUndoState(UndoManager undo)
        {
            if (undo.canUndo())
            {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            }
            else
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    public static class RedoAction extends BaseAction
    {
        public RedoAction()
        {
            super(redoAction);
            putValue(Action.SHORT_DESCRIPTION, "Redo Change");
            setEnabled(false);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            if (!ui.getUndoManager().canRedo()) return;
            ui.getUndoManager().redo();
            updateRedoState(ui.getUndoManager());
            undo.updateUndoState(ui.getUndoManager());
        }

        public void updateRedoState(UndoManager undo)
        {
            if (undo.canRedo())
            {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            }
            else
            {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
     * Executed when the Escape key is pressed. By default it hides the popup
     * menu if visible.
     */
    public static class EscapeAction extends StyledTextAction
    {
        public EscapeAction()
        {
            super(escapeAction);
        }

        public void actionPerformed(ActionEvent evt)
        {
            Completion.get().hideCompletion();
        }
    }

    public static class FormatAction extends BaseAction
    {
        public FormatAction()
        {
            super(formatAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            Utilities.formatCell(ui, ui.getCaretPosition());
        }

        public boolean isEnabled(NotebookUI ui)
        {
            return ui.getSelectedCellElement() != null;
        }
    }

    static class DeletePrevCharAction extends BaseAction
    {
        DeletePrevCharAction()
        {
            super(deletePrevCharAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            Caret caret = ui.getCaret();
            int dot = caret.getDot();
            int mark = caret.getMark();
            if (doc.isOutputCell(dot - 1) || doc.isInsertionPoint(dot - 1)
                    || doc.isCellHandle(dot-1))
            {
                UIManager.getLookAndFeel().provideErrorFeedback(ui);
                return;
            }
            int cc_pos = -1;
            if (dot != mark)
            {
                cc_pos = doc
                        .removeEx(Math.min(dot, mark), Math.abs(dot - mark));
            }
            else if (dot > 0)
            {
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
                // check for deleting an empty cell
                Element cell = doc.getUpperElement(dot, ElementType.commonCell);
                if (cell != null
                        && cell.getEndOffset() == cell.getStartOffset() + 1) doc
                        .removeCellBoxElement(cell);
                else
                    cc_pos = doc.removeEx(dot - delChars, delChars);
            }
            if (cc_pos > 0) ui.setCaretPosition(cc_pos);
        }
    }

    /*
     * Deletes the character of content that follows the current caret position.
     * 
     * @see DefaultEditorKit#deleteNextCharAction
     * 
     * @see DefaultEditorKit#getActions
     */
    static class DeleteNextCharAction extends BaseAction
    {
        /* Create this object with the appropriate identifier. */
        DeleteNextCharAction()
        {
            super(DefaultEditorKit.deleteNextCharAction);
        }

        /** The operation to perform when this action is triggered. */
        protected void action(NotebookUI ui) throws Exception
        {
            NotebookDocument doc = ui.getDoc();
            try
            {
                Caret caret = ui.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                int cc_pos = -1;
                if (dot != mark)
                {
                    cc_pos = doc.removeEx(Math.min(dot, mark), Math.abs(dot
                            - mark));
                }
                else if (dot < doc.getLength())
                {
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
                    cc_pos = doc.removeEx(dot, delChars);
                }
                if (cc_pos > 0) ui.setCaretPosition(cc_pos);
            }
            catch (BadLocationException bl)
            {
            }
        }
    }

    public static class InsertTabAction extends BaseAction
    {
        public InsertTabAction()
        {
            super(insertTabAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            int count = Utilities.getTabSpacesCount();
            String tab = Utilities.getTabSubstitute();
            int start = ui.getSelectionStart();
            int end = ui.getSelectionEnd();
            if (start == end) ui.replaceSelection(tab);
            else
            {
                NotebookDocument doc = ui.getDoc();
                int[] offs = Utilities.getSelectionStartOffsets(ui);
                if (offs == null) return;
                try
                {
                    doc.beginCompoundEdit("Tab");
                    for (int i = 0; i < offs.length; i++)
                        doc.insertString(offs[i] + i * count, tab, null);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    doc.endCompoundEdit();
                }
            }
        }
    }

    public static class RemoveTabAction extends BaseAction
    {
        public RemoveTabAction()
        {
            super(removeTabAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            int count = Utilities.getTabSpacesCount();
            int[] offs = Utilities.getSelectionStartOffsets(ui, count);
            if (offs == null) return;
            NotebookDocument doc = ui.getDoc();
            try
            {
                doc.beginCompoundEdit("UnTab");
                for (int i = 0; i < offs.length; i++)
                    doc.remove(offs[i] - i * count, count);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                doc.endCompoundEdit();
            }
        }
    }

    public static class HTMLAction extends StyledTextAction
    {
        public HTMLAction()
        {
            super(htmlAction);
            putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Source.png"));
            putValue(Action.SHORT_DESCRIPTION, "HTML Source On/Off");
        }

        public void actionPerformed(ActionEvent e)
        {
            JEditorPane editor = getEditor(e);
            // System.out.println("HTMLAction: " + editor);
            if (editor != null)
            {
                if (editor instanceof NotebookUI)
                {
                    // ((NotebookDocument) editor.getDocument())
                    // .toggleHTMLCell(editor.getCaretPosition());
                    Element el = ((NotebookDocument) editor.getDocument())
                            .getEnclosingCellElement(editor.getCaretPosition());
                    if (el != null)
                        CellUtils.toggleAttribute(NotebookDocument
                                .getNBElement(el), XMLConstants.ATTR_HTML);
                }
                else if (editor instanceof HtmlView.InnerHTMLEditor)
                {
                    Element el = ((HtmlView.InnerHTMLEditor) editor)
                            .getElement();
                    // ((NotebookDocument) el.getDocument()).toggleHTMLCell(el
                    // .getStartOffset());
                    CellUtils.toggleAttribute(
                            NotebookDocument.getNBElement(el),
                            XMLConstants.ATTR_HTML);
                }
            }
        }
    }

    public static class ShowInputTypePopupAction extends StyledTextAction
    {
        public ShowInputTypePopupAction()
        {
            super(showInputTypePopup);
        }

        public void actionPerformed(ActionEvent e)
        {
            JEditorPane ed = getEditor(e);
            if (ed == null) return;
            NotebookUI editor = (NotebookUI) ed;
            int pos = editor.getCaretPosition();
            NotebookDocument doc = editor.getDoc();
            if (doc.isInsertionPoint(pos))
            {
                editor.setCaretPosition(doc.insPointInsert(pos, ""));
                return;
            }
            Frame f = GUIUtilities.getFrame(editor);
            try
            {
                Rectangle rect = editor.modelToView(pos);
                Point pt = GUIHelper.computePoint(editor, new Point(rect.x,
                        rect.y));
                // SwingUtilities.convertPoint(editor, rect.x, rect.y, f);
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

    static final class ClearEngineContextAction extends BaseAction
    {
        public ClearEngineContextAction()
        {
            super(clearEngineContextAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            ui.getDoc().getEvaluationContext().reboot();
        }
    }

    static final class ResetCellNumAction extends BaseAction
    {
        public ResetCellNumAction()
        {
            super(resetCellNumAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            // ui.getDoc().reNumberCells();
        }
    }

    static final class JavaDocManagerAction extends AbstractAction
    {
        public JavaDocManagerAction()
        {
            super(javaDocManagerAction);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            JDialog dialog = new JDialog(TopFrame.getInstance(),
                    javaDocManagerAction);
            dialog.setSize(500, 500);
            dialog.add(new JavaDocPanel());
            dialog.setVisible(true);
        }
    }

    static final class CtxInspectorAction extends BaseAction
    {
        public CtxInspectorAction()
        {
            super(ctxInspectorAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            JDialog dialog = new JDialog(GUIUtilities.getFrame(ui),
                    ctxInspectorAction);
            dialog.setSize(500, 500);
            dialog.add(new RuntimeContextPanel(ui));
            dialog.setVisible(true);
        }
    }

    public static class FindReplaceAction extends BaseAction
    {
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

        protected void action(NotebookUI ui) throws Exception
        {
            int index = findOrReplace ? 0 : 1;
            if (findDialog == null) findDialog = new FindDialog(ui, index);
            else
                findDialog.setSelectedIndex(index);
            findDialog.setVisible(true);
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

    // MergeCellsAction
    public static class MergeCellsAction extends BaseAction
    {
        public MergeCellsAction()
        {
            super(mergeCellsAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            ui.mergeCells();
        }

        public boolean isEnabled(NotebookUI editor)
        {
            Collection<Element> sel = editor.getSelectionManager()
                    .getSelection();
            return (sel.size() > 1);
        }
    }

    public static class SelectWordAction extends BaseAction
    {
        public SelectWordAction()
        {
            super(selectWordAction);
        }

        protected void action(final NotebookUI ui) throws Exception
        {
            final int s = Utilities.getWordStart(ui, ui.getCaretPosition());
            final int e = Utilities.getWordEnd(ui, ui.getCaretPosition());
            // System.out.println("SelectWordAction: " +
            // ui.getCaretPosition() + ":" + s + ":" + e + ":" + ui);
            ui.select(s, e);
        }
    }

    /*
     * Select the line around the caret
     * 
     * @see DefaultEditorKit#endAction
     * 
     * @see DefaultEditorKit#getActions
     */
    public static class SelectLineAction extends BaseAction
    {

        /**
         * Create this action with the appropriate identifier.
         * 
         * @param nm
         *            the name of the action, Action.NAME.
         * @param select
         *            whether to extend the selection when changing the caret
         *            position.
         */
        public SelectLineAction()
        {
            super(selectLineAction);
        }

        protected void action(final NotebookUI ui) throws Exception
        {
            int offs = ui.getCaretPosition();
            int s = javax.swing.text.Utilities.getRowStart(ui, offs);
            int e = javax.swing.text.Utilities.getRowEnd(ui, offs);
            // System.out.println("SelectLineAction: " + offs + ":" + s + ":" +
            // e + ":" + ui);
            ui.select(s, e);
        }
    }

    // selects the content of the cell which currently contain the cursor
    public static class SelectAllAction extends BaseAction
    {
        public SelectAllAction()
        {
            super(selectAllAction);
        }

        protected void action(final NotebookUI ui) throws Exception
        {
            Element el = ui.getSelectedContentCellElement();
            System.out.println("SelectAllAction: " + el);
            if (el == null) return;
            ui.setCaretPosition(el.getStartOffset());
            ui.moveCaretPosition(el.getEndOffset() - 1);
        }

    }

    public static abstract class BaseAction extends StyledTextAction
    {
        public BaseAction(String actionName)
        {
            super(actionName);
        }

        public void actionPerformed(ActionEvent e)
        {
            // System.out.println("BaseAction: " + this + ":" + isEnabled());
            if (!(this.isEnabled())) return;
            JEditorPane pane = getEditor(e);
            // System.out
            // .println("BaseAction1: " + pane + ":" + pane.isEditable());
            if (pane == null || !pane.isEditable()
                    || !(pane instanceof NotebookUI)) return;
            NotebookUI editor = (NotebookUI) pane;
            if (editor == null) return;
            try
            {
                action(editor);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }
        }

        public boolean isEnabled()
        {
            NotebookUI editor = NotebookUI.getFocusedNotebookUI();
            if (editor == null) return false;
            return isEnabled(editor);
        }

        public boolean isEnabled(NotebookUI editor)
        {
            return true;
        }

        abstract protected void action(NotebookUI editor) throws Exception;
    }

}
