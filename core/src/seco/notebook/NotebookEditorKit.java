/*
 * This file is part of the Seco source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2010 Kobrix Software, Inc.
 */
package seco.notebook;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.script.Bindings;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.UndoManager;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.gui.ObjectInspector;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.NotifyDescriptor;
import seco.gui.dialog.FindDialog;
import seco.gui.menu.CellLangProvider;
import seco.gui.panel.AbbreviationPanel;
import seco.gui.panel.JavaDocPanel;
import seco.gui.panel.RuntimeContextPanel;
import seco.gui.panel.ShortcutPanel;
import seco.gui.rtctx.NewRuntimeContextDialog;
import seco.notebook.html.HTMLUtils;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.Completion;
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
import seco.storage.ClassRepository;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.util.GUIUtil;
import seco.util.IconManager;

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
    public static final String removeTabAction = "UnTab";
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
    public static final String importAction = "Import Package";
    public static final String htmlAction = "HTML Source";
    public static final String setCellLanguageAction = "Set Cell Language Action";
    public static final String clearEngineContextAction = "Clear Engine Context";
    public static final String resetCellNumAction = "Normalize Cell Numbering";
    public static final String javaDocManagerAction = "JavaDoc Manager";
    public static final String ctxInspectorAction = "RuntimeContext Inspector";
    public static final String shortcutInspectorAction = "KeyStroke Inspector";
    public static final String abbreviationManagerAction = "AbbreviationManager";
    public static final String replaceAction = "Replace...";
    public static final String findAction = "Find...";
    public static final String mergeCellsAction = "Merge Input Cells";
    public static final String addRemoveCommentsAction = "Comment/Uncomment";
    public static final String openObjectInspectorAction = "Inspect variable";
    public static final String selectAllAction = "Select All";
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
            new JavaDocManagerAction(), new SelectAllAction(),
            new CtxInspectorAction(), new ShortcutInspectorAction(),
            new FindReplaceAction(true), new FindReplaceAction(false),
            new RemoveTabAction(), new MergeCellsAction(),
            new SelectWordAction(), new SelectLineAction(),
            new VerticalPageAction(pageUpAction, -1, false),
            new VerticalPageAction(pageDownAction, 1, false),
            new AddRemoveCommentsAction(), new OpenObjectInspectorAction(),
            new AbbreviationManagerAction() };
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
        if (actions != null) return;
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
    static final HGPersistentHandle DOC_HANDLE = UUIDHandleFactory.I
            .makeHandle("50593b0e-d0c2-11dc-99fb-e94ae2f056ca");

    public Document createDefaultDocument()
    {
        return getDefaultDocument();
    }

    public static NotebookDocument getDefaultDocument()
    {
        NotebookDocument doc = (NotebookDocument) ThisNiche.graph
                .get(DOC_HANDLE);
        if (doc != null) return doc;

        doc = new NotebookDocument(ThisNiche.graph.add(DEFAULT_TOP_GROUP),
                ThisNiche.getTopContext());
        // System.out.println("Adding DOC: " + DEFAULT_TOP_GROUP);
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
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                    InputEvent.SHIFT_DOWN_MASK));
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
                DialogDescriptor dd = new DialogDescriptor(
                        GUIUtil.getFrame(ui), list,
                        "Select the class to import");
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
            CellGroupChangeEvent e = new CellGroupChangeEvent(
                    ThisNiche.handleOf(g), -1, new HGHandle[0],
                    rem.toArray(new HGHandle[rem.size()]));
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
            doc.evalGroup((CellGroup) doc.getBook());
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
                    doc.evalGroup((CellGroup) NotebookDocument.getLowerElement(
                            gr, ElementType.cellGroup));
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
                    || doc.isCellHandle(dot - 1))
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
    public static class DeleteNextCharAction extends BaseAction
    {
        /* Create this object with the appropriate identifier. */
        public DeleteNextCharAction()
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
                    cc_pos = doc.removeEx(Math.min(dot, mark),
                            Math.abs(dot - mark));
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
            int[] offs = Utilities.getSelectionStartOffsets(ui,
                    Utilities.getTabSubstitute());
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

    public static class AddRemoveCommentsAction extends BaseAction
    {
        public AddRemoveCommentsAction()
        {
            super(addRemoveCommentsAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            String comment = "//";
            int count = comment.length();

            int[] offs = Utilities.getSelectionStartOffsets(ui);
            if (offs == null || offs.length == 0) return;
            NotebookDocument doc = ui.getDoc();
            try
            {
                boolean addOrRemove = !comment.equals(doc.getText(offs[0],
                        count));
                if (!addOrRemove)
                {
                    // check if all lines are commented
                    offs = Utilities.getSelectionStartOffsets(ui, comment);
                    if (offs == null || offs.length == 0) return;
                }
                doc.beginCompoundEdit("Comment");
                for (int i = 0; i < offs.length; i++)
                {
                    if (addOrRemove) doc.insertString(offs[i] + i * count,
                            comment, null);
                    else
                        doc.remove(offs[i] - i * count, count);
                }
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
            if (editor != null)
            {
                if (editor instanceof NotebookUI)
                {
                    Element el = ((NotebookDocument) editor.getDocument())
                            .getEnclosingCellElement(editor.getCaretPosition());
                    if (el != null)
                        CellUtils.toggleAttribute(
                                NotebookDocument.getNBElement(el),
                                XMLConstants.ATTR_HTML);
                }
                else if (editor instanceof HtmlView.InnerHTMLEditor)
                {
                    Element el = ((HtmlView.InnerHTMLEditor) editor)
                            .getElement();
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
            super(setCellLanguageAction);
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
            Frame f = GUIUtil.getFrame(editor);
            try
            {
                Rectangle rect = editor.modelToView(pos);
                Point pt = new Point(rect.x, rect.y);
                pt = SwingUtilities.convertPoint(editor, rect.x, rect.y, f);
                pt = GUIUtil.adjustPointInPicollo(editor, pt);
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

    public static final class JavaDocManagerAction extends AbstractAction
    {
        public JavaDocManagerAction()
        {
            super(javaDocManagerAction);
        }

        public void actionPerformed(ActionEvent e)
        {
            GUIUtil.createAndShowDlg(GUIUtil.getFrame(e), javaDocManagerAction, 
                    new JavaDocPanel(), new Dimension(500, 500));
        }
    }

    public static final class CtxInspectorAction extends BaseAction
    {
        public CtxInspectorAction()
        {
            super(ctxInspectorAction);
        }

        protected void action(NotebookUI ui) throws Exception
        {
            GUIUtil.createAndShowDlg(GUIUtil.getFrame(ui), ctxInspectorAction,
                    new RuntimeContextPanel(ui), new Dimension(500, 500));
        }
    }

    public static final class ShortcutInspectorAction extends AbstractAction
    {
        public ShortcutInspectorAction()
        {
            super(shortcutInspectorAction);
        }

        public void actionPerformed(ActionEvent e)
        {
            GUIUtil.createAndShowDlg(GUIUtil.getFrame(e),
                    shortcutInspectorAction, new ShortcutPanel(), new Dimension(500, 500));
        }
    }

    public static final class AbbreviationManagerAction extends AbstractAction
    {
        public AbbreviationManagerAction()
        {
            super(abbreviationManagerAction);
        }

        public void actionPerformed(ActionEvent e)
        {
            AbbreviationPanel p = new AbbreviationPanel();
            DialogDescriptor dd = new DialogDescriptor(GUIUtil.getFrame(e), p,
                    "Abbreviation Manager");
            DialogDisplayer.getDefault().notify(dd);
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

        protected void action(NotebookUI ui) throws Exception
        {
            int index = findOrReplace ? 0 : 1;
            if (findDialog == null) findDialog = new FindDialog(ui, index);
            else
                findDialog.setSelectedIndex(index);
            GUIUtil.centerOnScreen(findDialog);
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
            putValue(Action.SMALL_ICON,
                    IconManager.resolveIcon((findOrReplace) ? "Find16.gif"
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
            if (el == null) return;
            ui.setCaretPosition(el.getStartOffset());
            ui.moveCaretPosition(el.getEndOffset() - 1);
        }
    }

    static class VerticalPageAction extends TextAction
    {

        /** Create this object with the appropriate identifier. */
        public VerticalPageAction(String nm, int direction, boolean select)
        {
            super(nm);
            this.select = select;
            this.direction = direction;
        }

        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e)
        {
            JTextComponent target = getTextComponent(e);
            if (target != null)
            {
                Rectangle visible = target.getVisibleRect();
                Rectangle newVis = new Rectangle(visible);
                int selectedIndex = target.getCaretPosition();
                int scrollAmount = direction
                        * target.getScrollableBlockIncrement(visible,
                                SwingConstants.VERTICAL, direction);
                int initialY = visible.y;
                Caret caret = target.getCaret();
                Point magicPosition = caret.getMagicCaretPosition();

                if (selectedIndex != -1)
                {
                    try
                    {
                        Rectangle dotBounds = target.modelToView(selectedIndex);
                        int x = (magicPosition != null) ? magicPosition.x
                                : dotBounds.x;
                        int h = dotBounds.height;
                        if (h > 0)
                        {
                            // We want to scroll by a multiple of caret height,
                            // rounding towards lower integer
                            scrollAmount = scrollAmount / h * h;
                        }
                        newVis.y = constrainY(target, initialY + scrollAmount,
                                visible.height);

                        int newIndex;

                        if (visible.contains(dotBounds.x, dotBounds.y))
                        {
                            // Dot is currently visible, base the new
                            // location off the old, or
                            newIndex = target.viewToModel(new Point(x,
                                    constrainY(target, dotBounds.y
                                            + scrollAmount, 0)));
                        }
                        else
                        {
                            // Dot isn't visible, choose the top or the bottom
                            // for the new location.
                            if (direction == -1)
                            {
                                newIndex = target.viewToModel(new Point(x,
                                        newVis.y));
                            }
                            else
                            {
                                newIndex = target.viewToModel(new Point(x,
                                        newVis.y + visible.height));
                            }
                        }
                        newIndex = constrainOffset(target, newIndex);
                        if (newIndex != selectedIndex)
                        {
                            // Make sure the new visible location contains
                            // the location of dot, otherwise Caret will
                            // cause an additional scroll.
                            adjustScrollIfNecessary(target, newVis, initialY,
                                    newIndex);
                            if (select)
                            {
                                target.moveCaretPosition(newIndex);
                            }
                            else
                            {
                                target.setCaretPosition(newIndex);
                            }
                        }
                    }
                    catch (BadLocationException ble)
                    {
                    }
                }
                else
                {
                    newVis.y = constrainY(target, initialY + scrollAmount,
                            visible.height);
                }
                if (magicPosition != null)
                {
                    caret.setMagicCaretPosition(magicPosition);
                }
                target.scrollRectToVisible(newVis);
            }
        }

        /**
         * Makes sure <code>y</code> is a valid location in <code>target</code>.
         */
        private int constrainY(JTextComponent target, int y, int vis)
        {
            if (y < 0)
            {
                y = 0;
            }
            else if (y + vis > target.getHeight())
            {
                y = Math.max(0, target.getHeight() - vis);
            }
            return y;
        }

        /**
         * Ensures that <code>offset</code> is a valid offset into the model for
         * <code>text</code>.
         */
        private int constrainOffset(JTextComponent text, int offset)
        {
            Document doc = text.getDocument();

            if ((offset != 0) && (offset > doc.getLength()))
            {
                offset = doc.getLength();
            }
            if (offset < 0)
            {
                offset = 0;
            }
            return offset;
        }

        /**
         * Adjusts the rectangle that indicates the location to scroll to after
         * selecting <code>index</code>.
         */
        private void adjustScrollIfNecessary(JTextComponent text,
                Rectangle visible, int initialY, int index)
        {
            try
            {
                Rectangle dotBounds = text.modelToView(index);

                if (dotBounds.y < visible.y
                        || (dotBounds.y > (visible.y + visible.height))
                        || (dotBounds.y + dotBounds.height) > (visible.y + visible.height))
                {
                    int y;

                    if (dotBounds.y < visible.y)
                    {
                        y = dotBounds.y;
                    }
                    else
                    {
                        y = dotBounds.y + dotBounds.height - visible.height;
                    }
                    if ((direction == -1 && y < initialY)
                            || (direction == 1 && y > initialY))
                    {
                        // Only adjust if won't cause scrolling upward.
                        visible.y = y;
                    }
                }
            }
            catch (BadLocationException ble)
            {
            }
        }

        /**
         * Adjusts the Rectangle to contain the bounds of the character at
         * <code>index</code> in response to a page up.
         */
        private boolean select;

        /**
         * Direction to scroll, 1 is down, -1 is up.
         */
        private int direction;
    }

    public static class OpenObjectInspectorAction extends BaseAction
    {

        public OpenObjectInspectorAction()
        {
            super(openObjectInspectorAction);
            putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_MASK
                            | InputEvent.CTRL_MASK));
        }

        protected void action(final NotebookUI ui) throws Exception
        {
            try
            {
                if (ui.getSelectionStart() != ui.getSelectionEnd())
                {
                    open(ui,
                            ui.getDoc().getText(
                                    ui.getSelectionStart(),
                                    ui.getSelectionEnd()
                                            - ui.getSelectionStart()));
                    return;
                }
                int offs = ui.getCaretPosition();
                int start = javax.swing.text.Utilities.getWordStart(ui, offs);
                int end = javax.swing.text.Utilities.getWordEnd(ui, offs);
                String s = ui.getDoc().getText(start, end - start);
                open(ui, s);
            }
            catch (BadLocationException bl)
            {
                UIManager.getLookAndFeel().provideErrorFeedback(ui);
            }
        }

        private void open(NotebookUI ui, String var)
        {
            Bindings binds = ui.getDoc().getEvaluationContext()
                    .getRuntimeContext().getBindings();
            Object value = binds.get(var);
            if (value == null) return;
            ObjectInspector propsPanel = new ObjectInspector(value);
//            DialogDescriptor dd = new DialogDescriptor(GUIUtil.getFrame(ui),
//                    new JScrollPane(propsPanel), "ObjectInspector: " + var
//                            + " -> " + value.getClass().getName());
//            DialogDisplayer.getDefault().notify(dd);
            GUIUtil.createAndShowDlg("ObjectInspector: " + var
                    + " -> " + value.getClass().getName(), new JScrollPane(propsPanel), new Dimension(400, 400));
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
            if (!(this.isEnabled())) return;
            JEditorPane pane = getEditor(e);
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
