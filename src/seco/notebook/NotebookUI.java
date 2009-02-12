/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import static seco.notebook.ElementType.commonCell;
import static seco.notebook.ElementType.inputCellBox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.*;
import javax.swing.Action;
import javax.swing.InputMap;
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
import javax.swing.text.DefaultHighlighter;
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

import seco.ThisNiche;
import seco.gui.TopFrame;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.UpdatablePopupMenu;
import seco.notebook.gui.menu.CellGroupPropsProvider;
import seco.notebook.gui.menu.CellLangProvider;
import seco.notebook.gui.menu.CellPropsProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.GroupingProvider;
import seco.notebook.html.HTMLEditor;
import seco.notebook.jscheme.JSchemeScriptSupport;
import seco.notebook.ruby.RubyScriptSupport;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.TokenMarker;
import seco.notebook.syntax.XModeHandler;
import seco.notebook.view.HtmlView;
import seco.rtenv.EvaluationContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;
import sun.awt.AppContext;
import bsh.BshScriptSupport;
import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;

public class NotebookUI extends JTextPane implements DocumentListener,
        AdjustmentListener, NotebookDocument.CaretMoveListener
{
    public static final String LAST_VISIBLE_OFFSET = "lastVisibleOffset";
    public static final HGPersistentHandle POPUP_HANDLE = HGHandleFactory
    .makeHandle("97287a6a-0195-11dd-a1bb-d15dfc7a2992");

    private boolean drawCellNums = false;
    protected UndoManager undo = new UndoManager();
    protected SelectionManager selectionManager;
    protected static UpdatablePopupMenu popupMenu;
    protected static PopupListener popupListener;
    protected int lastCaretStart = -1;
    protected int lastCaretEnd = -1;

    protected static NBFocusListener nbFocusListener = new NBFocusListener();

    public NotebookUI(HGHandle book)
    {
        this(book, ThisNiche.getContextFor(book));
    }

    public NotebookUI(HGHandle book, EvaluationContext evalContext)
    {
        super();
        Object o = ThisNiche.hg.get(book);
        // System.out.println("NotebookUI: " + book + ":" + o);
        NotebookDocument doc = null;
        if (o instanceof CellGroupMember)
        {
            if(o instanceof CellGroup)
                doc = new NotebookDocument(book, evalContext);
            else if(CellUtils.isInputCell((CellGroupMember)o))
               doc = new  ScriptletDocument(book);
            else
               doc =  new OutputCellDocument(book);
        } else
            doc = (NotebookDocument) o;
        if (doc == null) return;
        doc.init();
        setDocument(doc);
        setCaretPosition(0);
        initKeyBindings();
        setDragEnabled(true);
        setDoubleBuffered(!TopFrame.PICCOLO);
        setTransferHandler(new NotebookTransferHandler());
        if (popupMenu == null)
        {
            createPopup();
            popupListener = new PopupListener();
        }
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

    public static void setAntiAliasing(boolean _antiAliasing)
    {
        antiAliasing = _antiAliasing;
        // putClientProperty(SwingUtilities2.AA_TEXT_PROPERTY_KEY, new
        // Boolean(true));
        // Set it system wide for the next run
        // System.setProperty("swing.aatext", Boolean.toString(antiAliasing));
        // //$NON-NLS-1$
    }

    public static boolean isAntiAliasing()
    {
        return antiAliasing;
    }

    protected void initKeyBindings()
    {
        InputMap inputMap = getInputMap();
        // InputMap inputMap = getInputMap(
        // WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                InputEvent.SHIFT_DOWN_MASK);
        inputMap.put(key, NotebookEditorKit.evalAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                InputEvent.SHIFT_DOWN_MASK);
        inputMap.put(key, NotebookEditorKit.removeTabAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                InputEvent.SHIFT_DOWN_MASK);
        inputMap.put(key, NotebookEditorKit.deleteCellAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK);
        inputMap.put(key, NotebookEditorKit.selectCellHandleAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_MASK
                | InputEvent.CTRL_MASK);
        inputMap.put(key, NotebookEditorKit.importAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key, NotebookEditorKit.showInputTypePopup);
        //NotebookEditorKit kit = new NotebookEditorKit();
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key,NotebookEditorKit.undo);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                InputEvent.CTRL_DOWN_MASK);
        inputMap.put(key,NotebookEditorKit.redo);
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

    UndoManager getUndoManager()
    {
        return undo;
    }

    private static void createPopup()
    {
        if (popupMenu != null) return;
        popupMenu = (UpdatablePopupMenu) ThisNiche.hg.get(POPUP_HANDLE);
        if (popupMenu == null)
        {
            popupMenu = new UpdatablePopupMenu();// JPopupMenu();
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
            popupMenu.addSeparator();
            popupMenu.add(new EnhancedMenu("Grouping", new GroupingProvider()));
            ThisNiche.hg.define(POPUP_HANDLE, popupMenu);
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
        // doc.setCellEngine(engine, offset);
        final Element el = doc.getEnclosingCellElement(offset);
        if (el == null) return;
        CellGroupMember nb = NotebookDocument.getNBElement(el);
        if (!(nb instanceof Cell)) return;
        nb.setAttribute(XMLConstants.ATTR_ENGINE, engine);
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
        if(getDoc() instanceof OutputCellDocument) return;
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
                if (!(NotebookDocument.getNBElement(el) instanceof Cell)) return;
                if (i != 0) buffer.append(doc.getText(el.getStartOffset(), el
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
            // doc.updateGroup(doc.getUpperElement(start,
            // ElementType.cellGroup),
            // UpdateAction.syncronize);
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
        if (selectionManager == null) selectionManager = new SelectionManager(
                this);
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

    public void showPopup(MouseEvent e)
    {
        getPopupListener().dont_change_pos = true;
        getPopupListener().mouseClicked(e);
        getPopupListener().dont_change_pos = false;
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
                } else
                {
                    if (!dont_change_pos)
                    {
                        int off = ui.viewToModel(e.getPoint());
                        if (off != -1) ui.setCaretPosition(off);
                    }
                    popupMenu.update();
                    Frame f = GUIUtilities.getFrame(e.getComponent());
                    Point pt = SwingUtilities.convertPoint(e.getComponent(), e
                            .getX(), e.getY(), f);
                    popupMenu.show(f, pt.x, pt.y);// e.getX(), e.getY());
                }
            }
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
                Cell cell = (Cell) doc.getNBElement(el);
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
                        // System.out.println("CustomNavigationFilter - HTML: "
                        // + inner.getStartOffset() + ":"
                        // + inner.getEndOffset() + ":" + dot + ":"
                        // + doc.getLength() + ":" + up);
                        // fb.setDot(up ? dot - 1 : dot + 1, bias);
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
                } else
                    fb.setDot(dot, bias);
            } else
            {
                fb.setDot(dot, bias);
            }
        }

        public void moveDot(NavigationFilter.FilterBypass fb, int dot,
                Position.Bias bias)
        {
            int mark = getCaret().getMark();
            NotebookDocument doc = getDoc();
            Element el = doc.getUpperElement(dot, commonCell);
            // System.out.println("NavigationFilter-moveDot: " + dot + ":" +
            // mark + ":" + el);
            // allow selection only in one element at a time
            if (el != null)
            {
                if (mark >= el.getStartOffset() && mark <= el.getEndOffset())
                {
                    fb.moveDot(dot, bias);
                    return;
                } else if (doc.isCellHandle(dot))
                {
                    fb.moveDot(el.getEndOffset() + 1, bias);
                    return;
                }
                return;
            }
        }

        public int getNextVisualPositionFrom(JTextComponent text, int pos,
                Position.Bias bias, int direction, Position.Bias[] biasRet)
                throws BadLocationException
        {
            // realBias = (direction == SwingConstants.NORTH || direction ==
            // SwingConstants.WEST) ? Position.Bias.Backward
            // : Position.Bias.Forward;
            // System.out.println("NavigationFilter-getNextVisualPositionFrom: "
            // + pos + ":" + bias
            // + ":" + bias + ":" +
            // realBias + ":" + biasRet[0]);
            // return super.getNextVisualPositionFrom(text, pos, bias,
            // direction,
            // biasRet);
            realBias = (direction == SwingConstants.NORTH || direction == SwingConstants.WEST) ? Position.Bias.Backward
                    : Position.Bias.Forward;
            // biasRet[0] = realBias;
            return super.getNextVisualPositionFrom(text, pos, realBias,
                    direction, biasRet);
        }
    }

    public static void loadMode(Mode mode)
    {
        final String fileName = (String) mode.getProperty("file");
        System.out.println("Loading edit mode " + fileName);
        final XmlParser parser = new XmlParser();
        XModeHandler xmh = new XModeHandler(mode.getName()) {
            public void error(String what, Object subst)
            {
                int line = parser.getLineNumber();
                int column = parser.getColumnNumber();
                String msg;
                if (subst == null) msg = "xmode-error." + what;
                else
                {
                    msg = subst.toString();
                    if (subst instanceof Throwable) System.out
                            .println("ERROR: " + subst);
                }
                System.err.println("XMode error: " + msg + " file: " + fileName
                        + " line: " + line + " column: " + column);
            }

            public TokenMarker getTokenMarker(String modeName)
            {
                Mode mode = getMode(modeName);
                if (mode == null) return null;
                else
                    return mode.getTokenMarker();
            }
        };
        mode.setTokenMarker(xmh.getTokenMarker());
        parser.setHandler(xmh);
        try
        {
            InputStream is = NotebookUI.class.getResourceAsStream(fileName);
            if (is == null) is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(fileName);
            // System.out.println("NotebookUI - loadMode: " + is);
            parser.parse(null, null, is, null); // grammar);
            mode.setProperties(xmh.getModeProperties());
        }
        catch (Throwable e)
        {
            System.err.println("ERROR" + e);
            e.printStackTrace();
            if (e instanceof XmlException)
            {
                XmlException xe = (XmlException) e;
                int line = xe.getLine();
                String message = xe.getMessage();
                System.err.println("XMode error: " + message + " file: "
                        + fileName + " line: " + line);
            }
        }
    }

    /**
     * Returns the edit mode with the specified name.
     * 
     * @param name
     *            The edit mode
     */
    public static Mode getMode(String name)
    {
        for (int i = 0; i < modes.size(); i++)
        {
            Mode mode = (Mode) modes.elementAt(i);
            if (mode.getName().equals(name)) return mode;
        }
        return null;
    }

    public static void addMode(Mode mode)
    {
        // System.out.println("Adding edit mode " + mode.getName());
        modes.addElement(mode);
    }

    /**
     * Returns an array of installed edit modes.
     */
    public static Mode[] getModes()
    {
        Mode[] array = new Mode[modes.size()];
        modes.copyInto(array);
        return array;
    }

    public static void registerScriptSupport(ScriptSupport sup)
    {
        if (sup == null) throw new NullPointerException(
                "Attempt to register null ScriptSupport");
        if (supports.containsKey(sup.getScriptEngineName())) return;
        for (Mode m : sup.getModes())
            if (NotebookUI.getMode(m.getName()) == null) NotebookUI.addMode(m);
        supports.put(sup.getScriptEngineName(), sup.getClass());
    }

    static Map<String, Class> supports = new HashMap<String, Class>();
    private static Vector<Mode> modes = new Vector<Mode>();
    static
    {
        registerScriptSupport(new BshScriptSupport());
        registerScriptSupport(new JSchemeScriptSupport());
        registerScriptSupport(new RubyScriptSupport());
        registerScriptSupport(new HTMLScriptSupport());
    }

    @Override
    // the scrolls don't work as expected, so we need to force them...
    public Dimension getPreferredSize()
    {
        if (getParent() instanceof JViewport)
        {
            View root = getUI().getRootView(this);
            int width = (int) root.getPreferredSpan(View.X_AXIS);
            int height = (int) root.getPreferredSpan(View.Y_AXIS);
            return new Dimension(width, height);
        }
        return isPreferredSizeSet() ? super.getPreferredSize(): dim;
    }
    private static final  Dimension dim = new Dimension(200, 200);
    
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
        if (par != null && par instanceof JScrollPane) ((JScrollPane) par)
                .getVerticalScrollBar().addAdjustmentListener(this);
    }

    public void removeNotify()
    {
        super.removeNotify();
        Object par = this.getParent().getParent();
        if (par != null && par instanceof JScrollPane) ((JScrollPane) par)
                .getVerticalScrollBar().removeAdjustmentListener(this);
        close();
    }

    public void close()
    {
        // clear undos
        undo.discardAllEdits();
    }
    
    JTree getParseTree(int offset)
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
            } else
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
                    // System.out.println("paintLayer2: " + r + ":" + offs0 +
                    // ":"
                    // + offs1 + ":" +
                    // ((InlineView)view).getGlyphPainter().getSpan(
                    // (InlineView)view, offs0, offs1,
                    // ((InlineView)view).getTabExpander(), 0));
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
        // lastCaretStart = position;
        super.setCaretPosition(position);
        lastCaretStart = getCaretPosition();
        // System.out.println("NBUI - setCaretPosition: " + position + ":" +
        // moved);
        // if(moved != lastCaretStart)
        // lastCaretStart = moved;
    }

    public Element getSelectedGroupElement()
    {
        return getSelectedNBElement(ElementType.cellGroupBox);
    }

    public Element getSelectedOutputCellElement()
    {
        return getSelectedNBElement(ElementType.outputCellBox);
    }

    public Element getSelectedCellElement()
    {
        return getSelectedNBElement(ElementType.inputCellBox);
    }

    private Element getSelectedNBElement(ElementType type)
    {
        Element el = null;
        Collection<Element> c = getSelectionManager().getSelection();
        if (c.size() == 1) el = c.iterator().next();

        if (el == null) return getDoc().getUpperElement(getCaretPosition(),
                type);

        CellGroupMember nb = NotebookDocument.getNBElement(el);
        // some handle is selected, check if it's of the needed type
        if (type == ElementType.outputCellBox) return el;
        //if (type == ElementType.wholeCell && nb instanceof CellGroup) return null;
        return NotebookDocument.getUpperElement(el, type);
    }

    private static final Object FOCUSED_COMPONENT = new StringBuilder(
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
        if (c instanceof NotebookUI)
        {
            setFocusedNotebookUI((NotebookUI) c);
           // System.out.println("Focused: " +
            // AppForm.getInstance().currentBook.getDoc().getBook().getName());
        }
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
    
    public static UpdatablePopupMenu getPopupMenu()
    {
        if(popupMenu == null)
            createPopup();
        return popupMenu;
    }

    
}
