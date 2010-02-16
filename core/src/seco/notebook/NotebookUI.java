/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import static seco.notebook.ElementType.cellGroupBox;
import static seco.notebook.ElementType.commonCell;
import static seco.notebook.ElementType.inputCellBox;
import static seco.notebook.ElementType.outputCellBox;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.undo.UndoManager;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.TopFrame;
import seco.gui.GUIHelper.CellTreeAction;
import seco.gui.GUIHelper.ElementTreeAction;
import seco.gui.GUIHelper.ParseTreeAction;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.ScriptEngineProvider;
import seco.notebook.gui.UpdatablePopupMenu;
import seco.notebook.gui.menu.CellGroupPropsProvider;
import seco.notebook.gui.menu.CellLangProvider;
import seco.notebook.gui.menu.CellPropsProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.GroupingProvider;
import seco.notebook.gui.menu.NotebookPropsProvider;
import seco.notebook.html.HTMLEditor;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.view.HtmlView;
import seco.rtenv.EvaluationContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import sun.awt.AppContext;


public class NotebookUI extends JTextPane implements DocumentListener,
        AdjustmentListener, NotebookDocument.CaretMoveListener
{
    private static final long serialVersionUID = 7136295508874367948L;
    public static final String LAST_VISIBLE_OFFSET = "lastVisibleOffset";
    public static final HGPersistentHandle POPUP_HANDLE = HGHandleFactory
            .makeHandle("97287a6a-0195-11dd-a1bb-d15dfc7a2992");

    public static final HGPersistentHandle SCRIPT_SUPPORTS_HANDLE = HGHandleFactory
            .makeHandle("76b35260-e5ad-11de-8a39-0800200c9a66");

    private boolean drawCellNums = false;
    protected UndoManager undo = new UndoManager();
    protected SelectionManager selectionManager;
    protected static UpdatablePopupMenu popupMenu;
    protected static PopupListener popupListener = new PopupListener();;
    protected int lastCaretStart = -1;
    protected int lastCaretEnd = -1;

    protected static NBFocusListener nbFocusListener = new NBFocusListener();
    protected static HTMLEditor html_editor;

    // TODO: pending deadlock -
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6195631 looks very
    // similar
    // AWT - Thread:NotebookUI(Component).invalidate() line: 2721
    // Main -Thread:
    // NotebookDocument(AbstractDocument).readLock() line: 1372 [local variables
    // unavailable]
    // BasicTextPaneUI(BasicTextUI).getMinimumSize(JComponent) line: 930
    // NotebookUI(JEditorPane).getScrollableTracksViewportWidth() line: 1546
    // putting main UI creation in AWT thread seems to solve the problem for
    // now...

    public NotebookUI(HGHandle book)
    {
        this(book, ThisNiche.getContextFor(book));
    }

    static NotebookDocument getDocForHandle(Class<?> cl, HGHandle bookH)
    {
        List<HGHandle> list = hg.findAll(ThisNiche.graph, hg.type(cl));
        for (HGHandle h : list)
            if (bookH.equals(((NotebookDocument) ThisNiche.graph.get(h)).bookH))
                return ThisNiche.graph.get(h);
        return null;
    }

    public NotebookUI(HGHandle book, EvaluationContext evalContext)
    {
        super();
        Object o = ThisNiche.graph.get(book);
        NotebookDocument doc = null;
        if (o instanceof CellGroupMember)
        {
            if (CellUtils.isBackuped(book)) CellUtils.restoreCell(book);
            if (o instanceof CellGroup)
            {
                doc = getDocForHandle(NotebookDocument.class, book);
                if (doc == null)
                    ThisNiche.graph.add(doc = new NotebookDocument(book,
                            evalContext));
            }
            else if (CellUtils.isInputCell((CellGroupMember) o))
            {
                doc = getDocForHandle(ScriptletDocument.class, book);
                if (doc == null)
                    ThisNiche.graph.add(doc = new ScriptletDocument(book));
            }
            else
            {
                doc = getDocForHandle(OutputCellDocument.class, book);
                if (doc == null)
                    ThisNiche.graph.add(doc = new OutputCellDocument(book));
            }
        }
        else
            doc = (NotebookDocument) o;
        if (doc == null) return;
        doc.init();
        setDocument(doc);
        this.setCaret(new FixedCaret());
        setCaretPosition(0);
        initKeyBindings();
        setDragEnabled(true);
        setDoubleBuffered(!TopFrame.PICCOLO);
        setTransferHandler(new NotebookTransferHandler());
        createPopup();
        addMouseListener(popupListener);
        doc.addCaretMoveListener(this);
        setNavigationFilter(new CustomNavigationFilter());
        // Start watching for undoable edits
        getDoc().addUndoableEditListener(new MyUndoableEditListener());
        addFocusListener(nbFocusListener);
        ToolTipManager.sharedInstance().registerComponent(this);
        addCaretListener(new CaretListener() {
            private ScriptSupport old;

            public void caretUpdate(CaretEvent e)
            {
                ScriptSupport sup = getDoc().getScriptSupport(e.getDot());
                if (sup != null)
                {
                    sup.markBracket(e.getDot());
                    if (old != null && old != sup) old.unMarkBracket(true);
                    old = sup;
                }
                if (old != null && sup == null) old.unMarkBracket(true);
            }
        });
        addComponentListener(new ComponentAdapter() {

            public void componentHidden(ComponentEvent e)
            {
                lastCaretStart = getSelectionStart();
                lastCaretEnd = getSelectionEnd();
            }

            public void componentShown(ComponentEvent e)
            {
                restoreCaret();
                focused(e.getComponent());
            }

        });
        getDoc().addDocumentListener(this);
        highlighter = new CustomHighlighter();
        highlighter.install(this);
    }

    private static boolean antiAliasing;

    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        if (antiAliasing)
        {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
        }
        super.paintComponent(g2);
    }

    // in piccolo pageUp/Down scrolling won't work as expected
    // so this is a hack over the original SUN hack:)
    public Rectangle getVisibleRect()
    {
        Rectangle visibleRect = new Rectangle();
        //getBounds(visibleRect);
        //if (!TopFrame.PICCOLO) 
        computeVisibleRect(visibleRect);
        return visibleRect;
    }
    
    public void computeVisibleRect(Rectangle visibleRect) {
        computeVisibleRect(this, visibleRect);
    }
    
    static final void computeVisibleRect(Component c, Rectangle visibleRect) {
        Container p = c.getParent();
        Rectangle bounds = c.getBounds();

        if (p == null || p instanceof Window || p instanceof Applet) 
        {
            visibleRect.setBounds(0, 0, bounds.width, bounds.height);
        }else if(TopFrame.PICCOLO)// && p instanceof PiccoloCanvas)
        {
            visibleRect.setBounds(0, 0, bounds.width, bounds.height);
        } 
        else {
            computeVisibleRect(p, visibleRect);
            visibleRect.x -= bounds.x;
            visibleRect.y -= bounds.y;
            SwingUtilities.computeIntersection(0,0,bounds.width,bounds.height,visibleRect);
        }
    }

    public static void setAntiAliasing(boolean _antiAliasing)
    {
        antiAliasing = _antiAliasing;
    }

    public static boolean isAntiAliasing()
    {
        return antiAliasing;
    }

    protected void initKeyBindings()
    {
        InputMap inputMap = getInputMap();
        for (Action a: ActionManager.getInstance().getActions())
            if(a.getValue(Action.ACCELERATOR_KEY) != null)
                inputMap.put((KeyStroke)a.getValue(Action.ACCELERATOR_KEY), a);
    }

    void restoreCaret()
    {
        if (lastCaretStart > -1 && lastCaretStart < getDocument().getLength())
        {
            setCaretPosition(lastCaretStart);
            if (lastCaretEnd > -1) select(lastCaretStart, lastCaretEnd);
        } // else
        // setCaretPosition(0);
    }

    @Override
    public void replaceSelection(String content)
    {
        getSelectionManager().clearSelections();
        int offset = getCaretPosition();
        NotebookDocument doc = getDoc();
        if (doc.isInsertionPoint(offset)) setCaretPosition(getDoc()
                .insPointInsert(offset, content));
        else
            super.replaceSelection(content);
    }

    public UndoManager getUndoManager()
    {
        return undo;
    }

    private static void createPopup()
    {
        if (popupMenu != null) return;
        popupMenu = (UpdatablePopupMenu) ThisNiche.graph.get(POPUP_HANDLE);
        if (popupMenu == null)
        {
            popupMenu = new UpdatablePopupMenu();
            popupMenu.add(new EnhancedMenu("Set Default Language",
                    new ScriptEngineProvider()));
            NotebookEditorKit kit = new NotebookEditorKit();
            popupMenu
                    .add(new EnhancedMenu("Input Type", new CellLangProvider()));
            Action act = kit.getActionByName("Cut");
            JMenuItem mi = new JMenuItem(act);
            popupMenu.add(mi);
            act = kit.getActionByName("Copy");
            mi = new JMenuItem(act);
            popupMenu.add(mi);
            act = kit.getActionByName("Paste");
            mi = new JMenuItem(act);
            popupMenu.add(mi);
            JMenu menu = new JMenu("Paste As");
            menu.add(new JMenuItem(
                    NotebookTransferHandler.javaStringPasteAction));
            popupMenu.add(menu);
            // popupMenu.addSeparator();
            // mi = new JMenuItem(kit.getActionByName(
            // NotebookEditorKit.deleteSelectedElementsAction));
            popupMenu.add(mi);
            popupMenu.addSeparator();
            popupMenu.add(new EnhancedMenu("Cell", new CellPropsProvider()));
            // popupMenu.addSeparator();
            popupMenu.add(new EnhancedMenu("Cell Group",
                    new CellGroupPropsProvider()));
            popupMenu.add(new EnhancedMenu("Notebook",
                    new NotebookPropsProvider()));
            popupMenu.addSeparator();
            popupMenu.add(new EnhancedMenu("Grouping", new GroupingProvider()));
            popupMenu.addSeparator();
            mi = new JMenuItem("View Element Tree");
            mi.addActionListener(new ElementTreeAction());
            popupMenu.add(mi);
            mi = new JMenuItem("View Cells Tree");
            mi.addActionListener(new CellTreeAction());
            popupMenu.add(mi);
            mi = new JMenuItem("View Parse Tree");
            mi.addActionListener(new ParseTreeAction());
            popupMenu.add(mi);

            ThisNiche.graph.define(POPUP_HANDLE, popupMenu);
        }
    }

    protected EditorKit createDefaultEditorKit()
    {
        return new NotebookEditorKit();
    }

    public NotebookDocument getDoc()
    {
        return (NotebookDocument) this.getDocument();
    }

    public boolean isDrawCellNums()
    {
        return drawCellNums;
    }

    public void setDrawCellNums(boolean drawCellNums)
    {
        this.drawCellNums = drawCellNums;
        NotebookDocument doc = this.getDoc();
        doc.updateElement(doc.getRootElements()[0]);
    }

    public void setCellEngine(String engine, final int offset)
    {
        final NotebookDocument doc = getDoc();
        final Element el = doc.getEnclosingCellElement(offset);
        if (el == null) return;
        CellGroupMember nb = NotebookDocument.getNBElement(el);
        if (!(nb instanceof Cell)) return;
        nb.setAttribute(XMLConstants.ATTR_ENGINE, engine);
        if("html".equals(engine))
            nb.setAttribute(XMLConstants.ATTR_HTML, true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    setCaretPosition(el.getStartOffset() + 1);
                }
                catch (IllegalArgumentException iae)
                {
                    // stay silent, mystic concurrent problem
                    // while setting caret
                }
            }
        });
    }

    public PopupListener getPopupListener()
    {
        return popupListener;
    }

    public void deleteSelectedElements()
    {
        if (getDoc() instanceof OutputCellDocument) return;
        int offset = 0;
        for (Element el : new Vector<Element>(getSelectionManager()
                .getSelection()))
        {
            try
            {
                offset = el.getStartOffset();
                getDoc().removeCellBoxElement(el);
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }
        if (offset > 0) setCaretPosition(offset - 1);
        requestFocus();
    }
    
    public void evalSelectedElements()
    {
        if (getDoc() instanceof OutputCellDocument || 
                getDoc() instanceof ScriptletDocument) return;
        
        for (Element el : new Vector<Element>(getSelectionManager()
                .getSelection()))
        {
            try
            {
                if(NotebookDocument.isOutputCell(el)) continue;
                CellGroupMember cgm = NotebookDocument.getNBElement(el);
                if(cgm instanceof CellGroup)
                    getDoc().evalGroup((CellGroup) cgm);
                 else // inputCell
                   getDoc().evalCellInAuxThread(el);
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void mergeCells()
    {
        Vector<Element> elements = new Vector<Element>();
        for (Element e : getSelectionManager().getSelection())
            if (!NotebookDocument.isOutputCell(e)) elements.add(e);
        if (elements.size() < 2) return;
        Element first = elements.get(0);
        int start = first.getStartOffset();
        int end = first.getEndOffset();
        NotebookDocument doc = getDoc();
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n");
        try
        {
            for (int i = 0; i < elements.size(); i++)
            {
                Element el = elements.get(i);
                if (!(NotebookDocument.getNBElement(el) instanceof Cell))
                    return;
                if (i != 0)
                    buffer.append(doc.getText(el.getStartOffset(), el
                            .getEndOffset()
                            - el.getStartOffset() - 1));
            }
        }
        catch (Exception ex)
        {
        }
        try
        {
            doc.beginCompoundEdit("Merge Cells");
            for (int i = 1; i < elements.size(); i++)
            {
                Element el = elements.get(i);
                doc.removeCellBoxElement(el);
            }
            // delete last /n, because we have already one from first cell
            buffer.deleteCharAt(buffer.length() - 1);
            doc.insertString(end - 2, buffer.toString(), null);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        finally
        {
            doc.endCompoundEdit();
        }
        setCaretPosition(start);
        requestFocus();
    }

    // ugly hack, but when entering in sel_mode, cell handle receives focus
    // and keyboard event invoking SelectionManager.right(), which will
    // shift the selection at right again
    boolean entering_sel_mode_in_progress;

    void selectCellHandle(int offset)
    {
        Element el = getDoc().getEnclosingCellElement(offset);
        if (el != null)
        {
            entering_sel_mode_in_progress = true;
            getSelectionManager().clearSelections();
            getSelectionManager().addCellSelection(el);
        }
    }

    public SelectionManager getSelectionManager()
    {
        if (selectionManager == null)
            selectionManager = new SelectionManager(this);
        return selectionManager;
    }

    public Collection<Element> getSelectedElements()
    {
        return getSelectionManager().getSelection();
    }

    @Override
    public String getToolTipText(MouseEvent e)
    {
        int off = viewToModel(e.getPoint());
        ScriptSupport sup = getDoc().getScriptSupport(off);
        if (sup != null)
        {
            String msg = sup.getErrorMsg(off);
            if (msg != null) return msg;
        }
        return super.getToolTipText(e);
    }

    @Override
    public Point getToolTipLocation(MouseEvent e)
    {
        return (TopFrame.PICCOLO) ? GUIHelper.computePoint(this, e.getPoint())
                : super.getToolTipLocation(e);
    }

    public static UpdatablePopupMenu getPopupMenu()
    {
        if (popupMenu == null) createPopup();
        return popupMenu;
    }

    public void showPopup(MouseEvent e)
    {
        getPopupListener().dont_change_pos = true;
        getPopupListener().mouseClicked(e);
        getPopupListener().dont_change_pos = false;
    }

    public static void setFocusedHTMLEditor(HTMLEditor e)
    {
        html_editor = e;
    }

    public static HTMLEditor getFocusedHTMLEditor()
    {
        return html_editor;
    }

    static class PopupListener extends MouseInputAdapter
    {
        private boolean dont_change_pos;

        public void mouseClicked(MouseEvent e)
        {
            if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
            {
                NotebookUI ui = (NotebookUI) e.getComponent();
                if (popupMenu.isVisible())
                {
                    popupMenu.setVisible(false);
                }
                else
                {
                    if (!dont_change_pos && ui.getCaretPosition() <0)
                    {
                        int off = ui.viewToModel(e.getPoint());
                        if (off != -1) 
                            ui.setCaretPosition(off);
                    }
                    popupMenu.update();
                    Frame f = GUIUtilities.getFrame(e.getComponent());
                    Point pt = getPoint(e, f);
                    popupMenu.show(ui, pt.x, pt.y);
                }
            }
        }

        protected Point getPoint(MouseEvent e, Frame f)
        {
            Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getX(),
                    e.getY(), f);
            if (e.getComponent() instanceof JComponent)
                return GUIHelper
                        .computePoint((JComponent) e.getComponent(), pt);
            return pt;
        }
    }

    // This one listens for edits that can be undone.
    protected class MyUndoableEditListener implements UndoableEditListener
    {
        public void undoableEditHappened(UndoableEditEvent e)
        {
            // Remember the edit and update the menus.
            undo.addEdit(e.getEdit());
            NotebookEditorKit.undo.updateUndoState(undo);
            NotebookEditorKit.redo.updateRedoState(undo);
        }
    }

    public class CustomNavigationFilter extends NavigationFilter
    {
        private Position.Bias realBias;

        public void setDot(NavigationFilter.FilterBypass fb, int dot,
                Position.Bias bias)
        {
            getSelectionManager().clearSelections();
            NotebookDocument doc = getDoc();
            // System.out.println("NavigationFilter-setDot: " + dot + ":" + bias
            // + ":" + realBias + ":" + doc.getLength() + ":"
            // + doc.isCellHandle(dot) + ":" + doc.isInputCell(dot));
            if (dot == doc.getLength())
            {
                fb.setDot(dot - 1, bias);
                return;
            }
            boolean up = (realBias == Position.Bias.Backward);
            if (doc.isCellHandle(dot))
            {
                int n = dot;
                do
                {
                    n = (up) ? n - 1 : n + 1;
                }
                while (doc.isCellHandle(n));
                // System.out.println("CustomNavigationFilter - CellHandle: " +
                // n + ":" + realBias + ":" + ":" + doc.getParagraphElement(n));
                fb.setDot(n, realBias);
                return;
            }
            if (doc.isInputCell(dot))
            {
                Element el = doc.getUpperElement(dot, inputCellBox);
                Cell cell = (Cell) NotebookDocument.getNBElement(el);
                if (CellUtils.isHTML(cell))
                {
                    View v = getUI().getRootView(NotebookUI.this);
                    int ind = v.getViewIndex(dot, bias);
                    View inner = v;
                    while (ind != -1)
                    {
                        inner = inner.getView(ind);
                        ind = (inner != null) ? inner.getViewIndex(dot, bias)
                                : -1;
                    }
                    if (inner != null && inner instanceof HtmlView)
                    {
                        lastCaretStart = -1;
                        Component c = ((HtmlView) inner).getComponent();
                        int p = up ? ((HTMLEditor) c).getDocument().getLength() - 1
                                : 1;
                        if (p < 0
                                || p >= ((HTMLEditor) c).getDocument()
                                        .getLength()) p = 0;
                        ((HTMLEditor) c).setCaretPosition(p);
                        c.requestFocus();
                        return;
                    }
                    fb.setDot(dot, realBias);
                }
                else
                    fb.setDot(dot, bias);
            }
            else
            {
                fb.setDot(dot, bias);
            }
        }

        public void moveDot(NavigationFilter.FilterBypass fb, int dot,
                Position.Bias bias)
        {
            // allow selection in a single cell only
            int mark = getCaret().getMark();
            Element el = getDoc().getUpperElement(dot, commonCell);
            if (el == null) return;
            if (mark >= el.getStartOffset() && mark <= el.getEndOffset()) fb
                    .moveDot(dot, bias);
            else if (getDoc().isCellHandle(dot))
                fb.moveDot(el.getEndOffset() + 1, bias);
        }

        public int getNextVisualPositionFrom(JTextComponent text, int pos,
                Position.Bias bias, int direction, Position.Bias[] biasRet)
                throws BadLocationException
        {
            realBias = (direction == SwingConstants.NORTH || direction == SwingConstants.WEST) ? Position.Bias.Backward
                    : Position.Bias.Forward;
            // biasRet[0] = realBias;
            return super.getNextVisualPositionFrom(text, pos, realBias,
                    direction, biasRet);
        }
    }


    /**
     * Returns the edit mode with the specified name.
     * 
     * @param name
     *            The edit mode
     */
//    public static Mode getMode(String name)
//    {
////        getScriptSupportClassesMap();
//        for (int i = 0; i < modes.size(); i++)
//        {
//            Mode mode = (Mode) modes.elementAt(i);
//            if (mode.getName().equals(name)) return mode;
//        }
//        return null;
//    }

//    private static void addMode(Mode mode)
//    {
//        modes.addElement(mode);
//    }

    /**
     * Returns an array of installed edit modes.
     */
//    public static Mode[] getModes()
//    {
//        Mode[] array = new Mode[modes.size()];
//        modes.copyInto(array);
//        return array;
//    }

//    public static void registerScriptSupport(ScriptSupportFactory sup,
//            boolean permanently)
//    {
//        if (sup == null)
//            throw new NullPointerException(
//                    "Attempt to register null ScriptSupport");
//        if (getScriptSupportClassesMap().containsKey(sup.getEngineName()))
//            return;
//        for (Mode m : sup.getModes())
//            if (getMode(m.getName()) == null) addMode(m);
//        getScriptSupportClassesMap().put(sup.getEngineName(),
//                sup.getClass());
//        if (permanently) ThisNiche.graph.update(getScriptSupportClassesMap());
//    }

//    private static Map<String, Class<?>> supports = null;
    
    private static Vector<Mode> modes = null;

//    static Map<String, Class<?>> getScriptSupportClassesMap()
//    {
//        if (supports == null)
//        {
//            supports = (Map<String, Class<?>>) ThisNiche.graph.get(SCRIPT_SUPPORTS_HANDLE);
//            if (supports != null)
//            {
//                //legacy niche opened 
//                if(!supports.get("beanshell").isAssignableFrom(ScriptSupportFactory.class))
//                {
//                   ThisNiche.graph.remove(NotebookUI.SCRIPT_SUPPORTS_HANDLE);
//                   supports = null;
//                }else
//                {
//                   if (modes == null) init_modes();
//                     return supports;
//                 }
//            }
//            supports = new HashMap<String, Class<?>>();
//            modes = new Vector<Mode>();
//            registerScriptSupport(new BshScriptSupportFactory(), false);
//            registerScriptSupport(new JSchemeScriptSupportFactory(), false);
//            registerScriptSupport(new RubyScriptSupportFactory(), false);
//            registerScriptSupport(new HTMLScriptSupportFactory(), false);
//            registerScriptSupport(new PrologScriptSupportFactory(), false);
//            registerScriptSupport(new JSScriptSupportFactory(), false);
//            registerScriptSupport(new GroovyScriptSupportFactory(), false);
//            registerScriptSupport(new JavaFxScriptSupportFactory(), false);
//            ThisNiche.graph.define(SCRIPT_SUPPORTS_HANDLE, supports);
//        }
//        return supports;
//    }

//    static void init_modes()
//    {
//        modes = new Vector<Mode>();
//        for (Class<?> c : getScriptSupportClassesMap().values())
//        {
//            ScriptSupportFactory sup = null;
//            try
//            {
//                sup = (ScriptSupportFactory) c.newInstance();
//            }
//            catch (Exception ex)
//            {
//                System.err.println("Unable to create ScriptSupport for: "
//                        + c.getName());
//            }
//            if (sup != null) for (Mode m : sup.getModes())
//                if (getMode(m.getName()) == null) addMode(m);
//        }
//    }

    // the vertical scrolls don't work as expected, so we need to force them...
    // by the next 2 methods
    @Override
    public Dimension getPreferredSize()
    {
        if (getParent() instanceof JViewport)
        {
            View root = getUI().getRootView(this);
            int width = (int) root.getPreferredSpan(View.X_AXIS);
            int height = (int) root.getPreferredSpan(View.Y_AXIS);
            return new Dimension(width, height);
        }
        return isPreferredSizeSet() ? super.getPreferredSize() : dim;
    }

    // return true only if NBUI is smaller then the viewport, thus we
    // expand it to the whole viewport, avoiding ugly background at the bottom
    // Needed because if (in the opposite case) this method returns true
    // it breaks somehow the ScrollPaneLayout.layout() and
    // the vertical scroll is not always shown accordingly
    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        if (getParent() instanceof JViewport)
        {
            JViewport port = (JViewport) getParent();
            int h = port.getHeight();
            Dimension min = getPreferredSize();
            return (h >= min.height);
        }
        return false;
    }
    
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch(orientation) {
        case SwingConstants.VERTICAL:
        {
            if (getParent() instanceof JViewport)
            {
                JViewport port = (JViewport) getParent();
                return port.getHeight();
            }
            return visibleRect.height;
        }
        case SwingConstants.HORIZONTAL:
            return visibleRect.width;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
        
    }  

    private static final Dimension dim = new Dimension(300, 200);

    public void changedUpdate(DocumentEvent e)
    {
        // do nothing
    }

    public void insertUpdate(final DocumentEvent e)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                ScriptSupport sup = getDoc().getScriptSupport(e.getOffset());
                if (sup != null) sup.insertUpdate(e);
            }
        });
    }

    public void removeUpdate(final DocumentEvent e)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                ScriptSupport sup = getDoc().getScriptSupport(e.getOffset());
                if (sup != null) sup.removeUpdate(e);
            }
        });
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        Object par = this.getParent().getParent();
        if (par != null && par instanceof JScrollPane)
            ((JScrollPane) par).getVerticalScrollBar().addAdjustmentListener(
                    this);
    }

    public void removeNotify()
    {
        super.removeNotify();
        Object par = this.getParent().getParent();
        if (par != null && par instanceof JScrollPane)
            ((JScrollPane) par).getVerticalScrollBar()
                    .removeAdjustmentListener(this);
        close();
    }

    public void close()
    {
        // clear undos
        undo.discardAllEdits();
    }

    public JTree getParseTree(int offset)
    {
        ScriptSupport sup = getDoc().getScriptSupport(offset);
        if (sup != null && sup.getParser() != null)
            return sup.getParser().getAstTree();
        return null;
    }

    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        Rectangle r = ((JViewport) getParent()).getViewRect();
        int last_offset = viewToModel(new Point(20, r.y + r.height));
        getDoc().putProperty(LAST_VISIBLE_OFFSET, last_offset);
    }

    @Override
    public Highlighter getHighlighter()
    {
        return highlighter;
    }

    @Override
    public void setHighlighter(Highlighter h)
    {
        // DO NOTHING;
    }

    private transient Highlighter highlighter;

    public static class CustomHighlighter extends DefaultHighlighter implements
            UIResource
    {
        private static CustomHighlightPainter h = new CustomHighlightPainter(
                new Color(204, 204, 255));

        @Override
        public Object addHighlight(int p0, int p1, HighlightPainter p)
                throws BadLocationException
        {
            return super.addHighlight(p0, p1, h);
        }
    }

    private static class CustomHighlightPainter extends
            DefaultHighlighter.DefaultHighlightPainter
    {
        public CustomHighlightPainter(Color color)
        {
            super(color);
        }

        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c, View view)
        {
            Color color = getColor();
            g.setColor((color == null) ? c.getSelectionColor() : color);
            // System.out.println("paintLayer: " + offs0 + ":" + offs1);
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()
                    && offs0 == view.getElement().getStartOffset()
                    && offs1 == view.getElement().getEndOffset())
            {
                // Contained in view, can just use bounds.
                Rectangle alloc = (bounds instanceof Rectangle) ? (Rectangle) bounds
                        : bounds.getBounds();
                // System.out.println("paintLayer1: " + offs0 + ":" + offs1);
                g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
                return alloc;
            }
            else
            {
                // Should only render part of View.
                try
                {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    Rectangle r = (shape instanceof Rectangle) ? (Rectangle) shape
                            : shape.getBounds();
                    g.fillRect(r.x, r.y, r.width, r.height);
                    return r;
                }
                catch (BadLocationException e)
                {
                    // can't render
                    e.printStackTrace();
                }
            }
            // Only if exception
            return null;
        }
    }

    public void caretMoved(int pos)
    {
        setCaretPosition(pos);
    }

    @Override
    public void setCaretPosition(int position)
    {
        // System.out.println("NotebookUI - setCaretPosition: " + position);
        Document doc = getDocument();
        if (doc != null)
        {
            if (position > doc.getLength() || position < 0) return;
            // throw new IllegalArgumentException("bad position: " + position);
            getCaret().setDot(position);
        }
        // super.setCaretPosition(position);
        lastCaretStart = getCaretPosition();
    }

    public Element getSelectedGroupElement()
    {
        return getSelectedNBElement(cellGroupBox);
    }

    public Element getSelectedOutputCellElement()
    {
        return getSelectedNBElement(outputCellBox);
    }

    public Element getSelectedCellElement()
    {
        return getSelectedNBElement(inputCellBox);
    }

    public Element getSelectedContentCellElement()
    {
        return getSelectedNBElement(commonCell);
    }

    private Element getSelectedNBElement(ElementType type)
    {
        Element el = null;
        Collection<Element> c = getSelectionManager().getSelection();
        if (c.size() == 1) el = c.iterator().next();

        if (el == null)
            return getDoc().getUpperElement(getCaretPosition(), type);

        // some handle is selected, check if it is of the needed type
        if (type == outputCellBox) return el;
        return NotebookDocument.getUpperElement(el, type);
    }

    public static final Object FOCUSED_COMPONENT = new StringBuilder(
            "JTextComponent_FocusedComponent");

    public static final NotebookUI getFocusedNotebookUI()
    {
        return (NotebookUI) AppContext.getAppContext().get(FOCUSED_COMPONENT);
    }

    public static final void setFocusedNotebookUI(NotebookUI ui)
    {
        AppContext.getAppContext().put(FOCUSED_COMPONENT, (NotebookUI) ui);
    }

    private static void focused(Component c)
    {
        if (c instanceof NotebookUI) setFocusedNotebookUI((NotebookUI) c);
    }

    private static class NBFocusListener implements FocusListener
    {
        public void focusGained(FocusEvent e)
        {
            focused(e.getComponent());
        }

        public void focusLost(FocusEvent e)
        {
        }
    }

    public static class FixedCaret extends DefaultCaret
    {
        // preventing IllegalArgumentException which randomly occurs
        // in unclear situations(pending SUN bug issue)
        public void setDot(int dot, Position.Bias dotBias)
        {
            if (dotBias == null) dotBias = Position.Bias.Forward;
            super.setDot(dot, dotBias);
        }
        
        //this is fired to often and erases the selection
        public void focusLost(FocusEvent e) {
           // setVisible(false);
           //     setSelectionVisible(ownsSelection || e.isTemporary());
        }
    }

}
