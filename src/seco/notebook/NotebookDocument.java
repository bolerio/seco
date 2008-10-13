/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.script.*;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSystemFlags;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventDispatcher;
import seco.events.EventPubSub;
import seco.events.CellTextChangeEvent.EventType;
import seco.events.handlers.AttributeChangeHandler;
import seco.events.handlers.CellGroupChangeHandler;
import seco.notebook.eval.EvalResult;
import seco.notebook.html.HTMLEditor;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.SyntaxStyle;
import seco.notebook.syntax.SyntaxStyleBean;
import seco.notebook.syntax.SyntaxUtilities;
import seco.notebook.util.Log;
import seco.rtenv.EvaluationContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.EventListenerMap;
import seco.things.IOUtils;
import seco.things.Scriptlet;



import static seco.notebook.ElementType.*;

/**
 * 
 * @author bizi
 */
public class NotebookDocument extends DefaultStyledDocument
{
    static final String ATTR_CELL = "CELL";
    public static final String ATTR_SCRIPT_SUPPORT = "scriptSupport";
    public static final String ATTR_HTML_EDITOR = "HTML_EDITOR";
    static SimpleAttributeSet contentAttrSet = new SimpleAttributeSet();
    static MutableAttributeSet charAttrSet = new SimpleAttributeSet();
    static
    {
        contentAttrSet.addAttribute(StyleConstants.NameAttribute, charContent);
        charAttrSet.addAttribute(StyleConstants.NameAttribute, paragraph);
    }
    protected final HGHandle bookH;
    protected boolean modified;
    protected ScriptEngineManager scriptManager;
    protected ScriptContext context = new SimpleScriptContext();
    protected EvaluationContext evalContext;
    protected Vector<UndoableEdit> removeUndo = new EditVector<UndoableEdit>();
    protected Vector<UndoableEdit> insertUndo = new EditVector<UndoableEdit>();
    // reusable object for compound events
    protected CompoundEdit cEdit = new NamedCompoundEdit(null);
    // stack for nested compound events
    protected Stack<String> editStack = new Stack<String>();
    public ArrayList<SyntaxStyle> styles;
    private Map<String, SyntaxStyle[]> syntaxStyleMap = new HashMap<String, SyntaxStyle[]>();
    private Font outputCellFont = new Font("Default", Font.PLAIN, 12);
    private Font inputCellFont = new Font("Default", Font.PLAIN, 12);

    public NotebookDocument(HGHandle h)
    {
        this(h, ThisNiche.getTopContext());
    }

    public NotebookDocument(HGHandle h, EvaluationContext evalContext)
    {
        bookH = h;
        this.evalContext = evalContext;
    }

    public void init()
    {
        CellGroup book = (CellGroup) ThisNiche.hg.get(bookH);
        Map<StyleType, NBStyle> map = (Map<StyleType, NBStyle>) book
                .getAttribute(XMLConstants.CELL_STYLE);
        if (map != null) for (NBStyle s : map.values())
            addStyle(s);
        Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
        attr.addAttribute(ATTR_CELL, bookH);
        DocUtil.startTag(notebook, attr, 0, parseBuffer);
        if (book.getArity() == 0) DocUtil.createInsertionPoint(attr,
                parseBuffer);
        for (int i = 0; i < book.getArity(); i++)
        {
            DocUtil.createInsertionPoint(attr, parseBuffer);
            CellGroupMember el = book.getElement(i);
            if (el instanceof CellGroup) DocUtil.createCellGroup(this, book
                    .getTargetAt(i), attr, parseBuffer);
            else
                DocUtil.createCell(this, book.getTargetAt(i), attr,
                        parseBuffer, false);
            if (i == book.getArity() - 1) DocUtil.createInsertionPoint(attr,
                    parseBuffer);
        }
        DocUtil.endTag(parseBuffer);
        create(parseBuffer.toArray(new ElementSpec[parseBuffer.size()]));
        update(UpdateAction.tokenize);
        update(UpdateAction.evalInitCells);
        setModified(false);
        CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, bookH,
                getHandle(), AttributeChangeHandler.getInstance());
        CellUtils.addMutualEventPubSub(CellGroupChangeEvent.HANDLE, bookH,
                getHandle(), CellGroupChangeHandler.getInstance());
    }

    protected HGHandle handle = null;

    public HGHandle getHandle()
    {
        if (handle != null) return handle;

        Set<HGHandle> list = CellUtils.findAll(ThisNiche.hg, hg
                .type(NotebookDocument.class));
        for (HGHandle h : list)
            if (bookH.equals(((NotebookDocument) ThisNiche.hg.get(h)).bookH)) return handle = h;
        // System.out.println("Adding DOC: " + getTitle());
        handle = ThisNiche.hg.add(this);

        return handle;
    }

    protected AbstractElement createDefaultRoot()
    {
        // grabs a write-lock for this initialization and
        // abandon it during initialization so in normal
        // operation we can detect an illegitimate attempt
        // to mutate attributes.
        writeLock();
        BranchElement section = new BlockElement(null, null);
        BranchElement paragraph = new BranchElement(section, null);

        LeafElement brk = new LeafElement(paragraph, null, 0, 1);
        Element[] buff = new Element[1];
        buff[0] = brk;
        paragraph.replace(0, 0, buff);

        buff[0] = paragraph;
        section.replace(0, 0, buff);
        writeUnlock();
        return section;
    }

    JTree getParseTree(int offset)
    {
        ScriptSupport man = getScriptSupport(offset);
        if (man != null && man.getParser() != null) return man.getParser()
                .getAstTree();
        return null;
    }

    public ScriptContext getScriptingContext()
    {
        return context;
    }

    public EvaluationContext getEvaluationContext()
    {
        return evalContext;
    }

    public void setEvaluationContext(EvaluationContext ctx)
    {
        evalContext = ctx;
    }

    public CellGroupMember getBook()
    {
        return (CellGroupMember) ThisNiche.hg.get(bookH);
    }

    public void updateCell(Element inner, UpdateAction action)
            throws BadLocationException
    {
        if (wholeCell != getElementType(inner)) return;
        if (UpdateAction.index == action)
        {
            indexes.put(getNBElementH(inner), createPosition(inner
                    .getStartOffset()));
            Element out_el = getLowerElement(inner, outputCellBox);
            if (out_el != null) indexes.put(getNBElementH(out_el),
                    createPosition(out_el.getStartOffset()));
            return;
        }

        if (UpdateAction.removeOutputCells == action) removeOutputCell(inner);
        else if (UpdateAction.evalInitCells == action)
        {
            Cell cell = (Cell) getNBElement(inner);
            if (CellUtils.isInitCell(cell)) insertOutputCell(inner);
        } else if (UpdateAction.reEvaluateOutputCells == action) insertOutputCell(inner);
        else if (UpdateAction.syncronize == action
                || UpdateAction.tokenize == action
                || UpdateAction.resetTokenMarker == action)
        {
            Cell cell = (Cell) getNBElement(inner);
            Element e = getLowerElement(inner, commonCell);
            if (UpdateAction.tokenize == action) createScriptSupport(
                    getLowerElement(inner, inputCellBox), false);
            else if (UpdateAction.resetTokenMarker == action) resetScriptSupport(getLowerElement(
                    inner, inputCellBox));
            else
            {
                // System.out.println("UpdateAction: " + inner + ":\n" + e
                // + ":\n" + cell);
                Element html = getLowerElement(inner, htmlCell);
                if (html != null)
                {
                    HTMLEditor ed = (HTMLEditor) html.getAttributes()
                            .getAttribute(ATTR_HTML_EDITOR);
                    CellUtils.setCellText(cell, ed.getContent());
                } else if (e != null)
                {
                    // CellUtils.setCellText(cell, getText(e.getStartOffset(), e
                    // .getEndOffset()
                    // - e.getStartOffset()));
                    ThisNiche.hg.update(cell);
                    Cell out = CellUtils.getOutCell(cell);
                    if (out != null) ThisNiche.hg.update(out);
                }
            }
        }
    }

    public void update(UpdateAction action)
    {
        Element root = getRootElements()[0];
        if (UpdateAction.index == action) try
        {
            indexes.put(bookH, createPosition(0));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        for (int i = 0; i < root.getElementCount(); i++)
        {
            Element el = root.getElement(i);
            if (cellGroupBox == getElementType(el))
            {
                el = getLowerElement(el, cellGroup);
                if (el != null)
                {
                    try
                    {
                        updateGroup(el, action);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            } else if (wholeCell == getElementType(el))
            {
                try
                {
                    updateCell(el, action);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        setModified(true);
    }

    public void updateStyles()
    {
        syntaxStyleMap.clear();
        update(UpdateAction.resetTokenMarker);
        updateElement(getRootElements()[0]);
    }

    public void updateGroup(Element el, UpdateAction action)
            throws BadLocationException
    {
        if (cellGroup != getElementType(el)) return;
        if (UpdateAction.index == action) indexes.put(getNBElementH(el),
                createPosition(el.getStartOffset()));
        for (int i = 0; i < el.getElementCount(); i++)
        {
            Element inner = el.getElement(i);
            if (cellGroupBox == getElementType(inner)) updateGroup(
                    getLowerElement(inner, cellGroup), action);
            else if (wholeCell == getElementType(inner))
            {
                updateCell(inner, action);
            }
        }
    }

    void createScriptSupport(Element el, boolean force)
    {
        if (el == null) return;
        ScriptSupport sup = getScriptSupport(el.getStartOffset());
        if (sup != null && !force) return;
        CellGroupMember nb = getNBElement(el);
        if (!(nb instanceof Cell)) return;
        Cell cell = (Cell) nb;
        String name = CellUtils.getEngine(cell);
        el = getLowerElement(el, inputCellBox);
        Class cls = NotebookUI.supports.get(name);
        MutableAttributeSet attrs = (MutableAttributeSet) el.getAttributes();
        if (cls == null)
        {
            if (force) attrs.removeAttribute(ATTR_SCRIPT_SUPPORT);
            return;
        }
        sup = null;
        try
        {
            sup = (ScriptSupport) cls.newInstance();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (sup == null)
        {
            if (force) attrs.removeAttribute(ATTR_SCRIPT_SUPPORT);
            return;
        }
        writeLock();
        attrs.addAttribute(ATTR_SCRIPT_SUPPORT, sup);
        // System.out.println("Supp: " + sup);
        writeUnlock();
        sup.init(this, el.getElement(0));
    }

    public void addAttribute(Element el, String name, Object value)
    {
        writeLock();
        MutableAttributeSet set = (MutableAttributeSet) el.getAttributes();
        set.addAttribute(name, value);
        writeUnlock();
    }

    void resetScriptSupport(Element el)
    {
        ScriptSupport sup = getScriptSupport(el.getStartOffset());
        if (sup == null) return;
        el = getLowerElement(el, inputCellBox);
        if (el == null) return;
        sup.init(this, el.getElement(0));
    }

    public ScriptSupport getScriptSupport(int offset)
    {
        Element el = getUpperElement(getParagraphElement(offset), inputCellBox);
        if (el == null) return null;
        return (ScriptSupport) el.getAttributes().getAttribute(
                ATTR_SCRIPT_SUPPORT);
    }

    public void atomicLock()
    {
        writeLock();
    }

    public void atomicUnlock()
    {
        writeUnlock();
    }

    public enum UpdateAction
    {
        evalInitCells, removeOutputCells, reEvaluateOutputCells, syncronize, tokenize, resetTokenMarker, index
    };

    protected boolean evalCell(int offset)
    {
        boolean b = false;
        try
        {
            Element el = getWholeCellElement(offset);
            if (el != null) b = insertOutputCell(el);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return b;
    }

    private void removeOutputCell(Element el) throws BadLocationException
    {
        HGHandle cellH = getNBElementH(el);
        Element out_el = getLowerElement(el, outputCellBox);
        if (out_el != null)
        {
            HGHandle old = CellUtils.getOutCellHandle(cellH);
            if (old != null) create_and_fire_eval_event(cellH, null);
            setModified(true);
        }
    }

    // wholeCell, if OutputCell is null the InputCell will be evaluated
    // return true if the resulting OutputCell is non error
    boolean insertOutputCell(Element el) throws BadLocationException
    {
        Cell outer_cell = (Cell) getNBElement(el);
        Object o = null;
        EvalResult res = DocUtil.eval_result(this, outer_cell);
        o = res.getComponent() != null ? res.getComponent() : res.getText();
        create_and_fire_eval_event(getNBElementH(el), o);
        return res.isError();
    }

    public void cellEvaled(EvalCellEvent e)
    {
        int offset = findElementOffset(e.getCellHandle());
        if (offset < 0) return;
        Element el = getUpperElement(offset, wholeCell);
        Element el1 = getLowerElement(el, inputCellBox);
        Element out_el = getLowerElement(el, outputCellBox);
        try
        {
            beginCompoundEdit("");
            this.supressEvents = true;
            if (out_el != null) super.remove(out_el.getStartOffset(), out_el
                    .getEndOffset()
                    - out_el.getStartOffset());
            if (e.getValue() == null) return;

            Vector<ElementSpec> vec = new Vector<ElementSpec>();
            SimpleAttributeSet attr = new SimpleAttributeSet();
            DocUtil.endTag(vec);
            DocUtil.endTag(vec);
            DocUtil.endTag(vec);
            HGHandle cell = CellUtils.getOutCellHandle(e.getCellHandle());
            DocUtil.createOutputCell(this, cell, attr, vec);
            insert(el1.getEndOffset(), vec.toArray(new ElementSpec[vec.size()]));
        }
        catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            this.supressEvents = false;
            endCompoundEdit();
        }
    }

    // Element at offset should be InsertionPoint
    // Object String or NBElement
    // return the pos for the caret
    int insPointInsert(int offset, Object obj)
    {
        Element insP = getUpperElement(offset, insertionPoint);
        if (insP == null) return offset;
        int i = 0;
        CellGroup book = (CellGroup) ThisNiche.hg.get(bookH);
        CellGroup par = book;
        if (offset > 0)
        {
            Element p = getEnclosingCellElement(offset);
            if (p == getRootElements()[0])
            {
                HGHandle prev_gr = (isOutputCell(offset - 1)) ? getNBElementH(getWholeCellElement(offset - 1))
                        : getNBElementH(getParagraphElement(offset - 1));
                i = par.indexOf(prev_gr) + 1;
            } else
            {
                par = (CellGroup) getNBElement(p);
                int n = 1;
                if (isInsertionPoint(offset - 1)) n++;
                Element temp = p.getElement(0);
                int ind = temp.getElementIndex(offset - n);
                HGHandle prev_gr = getNBElementH(temp.getElement(ind));
                par = (CellGroup) getNBElement(p);
                i = par.indexOf(prev_gr) + 1;
            }
        }

        HGHandle child = (obj instanceof HGHandle) ? (HGHandle) obj : null;
        int result = insP.getEndOffset();
        if (child == null)
        {
            String str = (String) obj;
            if (str == null) str = "\n";
            if (!str.endsWith("\n")) str += "\n";
            result = result + str.length(); // - 1;
            if (par == book)
            {
                child = CellUtils.createGroupHandle();
                CellGroup group = (CellGroup) ThisNiche.hg.get(child);
                group.insert(0, CellUtils.createCellHandle(str));
            } else
            {
                child = CellUtils.createCellHandle(str);
                result--;
            }
        }

        Object childAtom = ThisNiche.hg.get(child);
        
        if (!(childAtom instanceof CellGroup) && par == book)
        {
            HGHandle enc_gr = CellUtils.createGroupHandle();
            CellGroup group = (CellGroup) ThisNiche.hg.get(enc_gr);
            group.insert(0, child);
            // par.insert(i, enc_gr);
            fireCellGroupChanged(new CellGroupChangeEvent(ThisNiche
                    .handleOf(par), i, new HGHandle[] { enc_gr },
                    new HGHandle[0]));
        } else
        {
            // par.insert(i, child);
            fireCellGroupChanged(new CellGroupChangeEvent(ThisNiche
                    .handleOf(par), i, new HGHandle[] { child },
                    new HGHandle[0]));
        }
        return result;
    }

    int findNextInsPoint(int pos)
    {
        Element el = getWholeCellElement(pos);
        // System.out.println("NBDoc - findNextInsPoint: " + pos + "->" + el);
        if (el != null) return el.getEndOffset() - 1;
        return pos;
    }

    boolean insertElements(int offset, Vector<Element> elements)
            throws BadLocationException
    {
        Log.Trace("NBDoc - insertElements: " + offset + ":" + elements + ":"
                + elements.size());
        Element c_el = getUpperElement(offset, wholeCell);
        Element gr_el = getUpperElement(offset, cellGroup);
        CellGroup cg = (CellGroup) getNBElement(gr_el);
        boolean isInsP = getUpperElement(offset, insertionPoint) != null;
        int ind = 0;
        if (!isInsP) ind = cg.indexOf(getNBElementH(c_el));
        for (int i = elements.size() - 1; i >= 0; i--)
        {
            Element el = elements.get(i);
            if (isOutputCell(el)) continue;
            HGHandle child = getNBElementH(el);
            if (isInsP) insPointInsert(offset, CellUtils.makeCopy(child));
            else
                fireCellGroupChanged(new CellGroupChangeEvent(ThisNiche
                        .handleOf(cg), ind + 1, new HGHandle[] { CellUtils
                        .makeCopy(child) }, new HGHandle[0]));
            // cg.insert(ind + 1, CellUtils.makeCopy(child));
        }
        return true;
    }

    void insert_first(HGHandle child, HGHandle parent, int offset)
            throws BadLocationException
    {
        Element el = getUpperElement(offset, parent, true);
        HGHandle par_parH = getContainerH(el);
        CellGroup par_par = (CellGroup) ThisNiche.hg.get(par_parH);
        int ind = par_par.indexOf(parent);
        if (ind == -1) throw new BadLocationException("Wrong index", ind);
        // just remove the old box and insert the newly updated one
        if (notebook != getElementType(el)) removeCellBoxElement0(el);
        insert(parent, par_parH, ind);
    }

    public void insert(HGHandle child, HGHandle parentH, int ind)
            throws BadLocationException
    {
        if (ind == -1) throw new BadLocationException("Wrong index", ind);
        int offset = findElementOffset(parentH);
        CellGroup parent = (CellGroup) ThisNiche.hg.get(parentH);
        boolean top = parentH.equals(bookH);
        if (ind == 0)
        {
            if (!top)
            {
                insert_first(child, parentH, offset + 1);
                return;
            } else
            {
                offset++;
            }
        }

        if (ind > 0 && ind < parent.getArity() - 1)
        {
            offset = findElementOffset(parent.getTargetAt(ind + 1));
            // if(offset == -1)
            // System.err.println("ERRROR: " + parentH + ":" +
            // parent.getTargetAt(ind + 1) + ":" + indexes);
        }

        if((ind > 0 && ind == parent.getArity() - 1) || offset == -1)
        { // last in a group
            HGHandle h = parent.getTargetAt(ind - 1);
            Element prev = getUpperElement(findElementOffset(h), h, true);
            offset = prev.getEndOffset() + 1;
        }

        Vector<ElementSpec> vec = new Vector<ElementSpec>();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        if (!top)
        {
            DocUtil.endTag(vec);
            DocUtil.endTag(vec);
        } else
            DocUtil.endTag(vec);
        boolean cell_group_ins_p = false;
        Element e = getUpperElement(offset - 1, insertionPoint);
        if (e != null && cellGroup == getElementType(e.getParentElement())) cell_group_ins_p = true;
        if (!cell_group_ins_p) DocUtil.endTag(vec);
        Object ch = ThisNiche.hg.get(child);
        if (ch instanceof Cell) DocUtil
                .createCell(this, child, attr, vec, false);
        else
            DocUtil.createCellGroup(this, child, attr, vec);
        if (top) DocUtil.createInsertionPoint(attr, vec);
        Log.Trace("insert - offset: " + offset + "new_cell: " + child
                + " index:" + (ind) + " parent: " + parent);
        insert(offset, vec.toArray(new ElementSpec[vec.size()]));
        update(UpdateAction.tokenize);
    }

    public Element getUpperElement(int offset, ElementType type)
    {
        return getUpperElement(getParagraphElement(offset), type);
    }

    public static Element getUpperElement(Element el, ElementType type,
            boolean check_passed_in_el)
    {
        if (el == null) return null;
        if (check_passed_in_el && type == getElementType(el)) return el;
        return getUpperElement(el.getParentElement(), type, true);
    }

    public static Element getUpperElement(Element el, ElementType type)
    {
        return getUpperElement(el, type, true);
    }

    public Element getUpperElement(int offset, HGHandle type, boolean topmost)
    {
        return getUpperElement(getParagraphElement(offset), type, topmost);
    }

    public Element getUpperElement(Element el, HGHandle type, boolean topmost)
    {
        Log.Trace("getUpperElement: " + el + " type: " + type);
        boolean found = false;
        while (el != null && type != null)
        {
            found = type.equals(getNBElementH(el))
                    && getElementType(el) != insertionPoint;
            // Log.Trace("getUpperElement: " + el + " found:" + found);
            if (found)
            {
                if (!topmost) return el;
                Element par = el.getParentElement();
                if (par != null)
                {
                    if (!type.equals(getNBElementH(par))
                            && getElementType(par) != insertionPoint) return el;
                } else
                    return el;
            }
            el = el.getParentElement();
        }
        // Log.Trace("getUpperElement return default");
        return getDefaultRootElement();
    }

    public Element getLowerElement(int offset, ElementType type)
    {
        return getLowerElement(getParagraphElement(offset), type);
    }

    public static Element getLowerElement(Element parent, ElementType type)
    {
        if (parent == null) return null;
        if (getElementType(parent) == type) return parent;
        for (int i = 0; i < parent.getElementCount(); i++)
        {
            Element child = parent.getElement(i);
            if (getElementType(child) == type) return child;
            Element e = getLowerElement(child, type);
            if (e != null) return e;
        }
        return null;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException
    {
        // Log.Trace("NBDOC - insertString -start: " + supressEvents);
        if (getUpperElement(offset, outputCellBox) != null) return;
        if (getUpperElement(offset, insertionPoint) != null) insPointInsert(
                offset, str);
        else if (!CellUtils
                .isReadonly(getNBElement(getEnclosingCellElement(offset))))
        {
            Element el = getEnclosingCellElement(offset);
            Log.Trace("NBDOC - insertString in: " + el + ":" + offset);
            CellTextChangeEvent e = new CellTextChangeEvent(getNBElementH(el),
                    EventType.INSERT, str, offset - el.getStartOffset(), str
                            .length());
            fireCellTextChanged(e);
        }
        // Log.Trace("NBDOC - insertString - end");
    }

    // hack due to a possible bug in insert(int, ElementSpec[]), which calls
    // super.insertUpdate() thus bypassing our insertUpdate() hook,
    // in which we would normally attach our UndoableEdits.
    // The next problem is the implicit condition for accepting only
    // special types of UndoableEdits so we package our removeUndos here too
    protected void fireUndoableEditUpdate(UndoableEditEvent e)
    {
        setModified(true);
        if (supressEvents) return;
        // Log.Trace("NBDoc -fireUndoableEditUpdate: " + e.getEdit());
        if (!(e.getEdit() instanceof DefaultDocumentEvent))
        {
            super.fireUndoableEditUpdate(e);
            return;
        }
        // Log.Trace("NotebookDoc -fireUndoableEditUpdate2: " +
        // compoundEditInProgress);
        DefaultDocumentEvent dde = (DefaultDocumentEvent) e.getEdit();
        if (isCompoundEditInProgress())
        {
            cEdit.addEdit(dde);
            return;
        }
        CompoundEdit cE = null;
        if (DocumentEvent.EventType.INSERT.equals(dde.getType())) cE = makeCompoundEdit(
                insertUndo, dde);
        else if (DocumentEvent.EventType.REMOVE.equals(dde.getType())) cE = makeCompoundEdit(
                removeUndo, dde);
        if (cE != null) super.fireUndoableEditUpdate(new UndoableEditEvent(e
                .getSource(), cE));
        else
            super.fireUndoableEditUpdate(e);
    }

    void beginCompoundEdit(final String name)
    {
        cEdit = new NamedCompoundEdit(name);
        editStack.push(name);
    }

    void endCompoundEdit()
    {
        editStack.pop();
        if (!editStack.isEmpty()) return;
        cEdit.end();
        super.fireUndoableEditUpdate(new UndoableEditEvent(this, cEdit));
    }

    private boolean isCompoundEditInProgress()
    {
        return editStack != null && !editStack.isEmpty();
    }

    private CompoundEdit makeCompoundEdit(Vector<UndoableEdit> edits,
            DefaultDocumentEvent dde)
    {
        if (edits == null || edits.isEmpty()) return null;
        CompoundEdit cE = new CompoundEdit();
        if (dde != null) cE.addEdit(dde);
        for (UndoableEdit ed : edits)
            if (ed != null) cE.addEdit(ed);
        cE.end();
        edits.clear();
        return cE;
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent e)
    {
        setModified(true);
        super.removeUpdate(e);
    }

    protected void insertUpdate(DefaultDocumentEvent e, AttributeSet attr)
    {
        setModified(true);
        if (attr == null) attr = contentAttrSet;
        super.insertUpdate(e, attr);
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(boolean mod)
    {
        if (modified != mod)
        {
            modified = mod;
            fireModificationChange(null);
        }
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof NotebookDocument)) return false;
        NotebookDocument el = (NotebookDocument) o;
        return bookH.equals(el.bookH);
    }

    protected Element createLeafElement(Element par, AttributeSet a, int p0,
            int p1)
    {
        return new RunElement(par, a, p0, p1);
    }

    protected Element createBranchElement(Element parent, AttributeSet a)
    {
        return new BlockElement(parent, a);
    }

    public class RunElement extends LeafElement
    {
        public RunElement(Element parent, AttributeSet a, int offs0, int offs1)
        {
            super(parent, a, offs0, offs1);
        }

        public AttributeSet getResolveParent()
        {
            return null;
        }

        public String toString()
        {
            return "PEX: " + getName() + ":" + super.toString();
        }
    }

    public class BlockElement extends BranchElement
    {
        public BlockElement(Element parent, AttributeSet a)
        {
            super(parent, a);
        }

        public boolean equals(Object o)
        {
            if (!(o instanceof BlockElement)) return false;
            BlockElement el = (BlockElement) o;
            if (this.getChildCount() == 0 || el.getChildCount() == 0) return super
                    .equals(o);
            if (this.getDocument().equals(el.getDocument())) if (this
                    .getStartOffset() == el.getStartOffset()
                    && this.getEndOffset() == el.getEndOffset()
                    && ((getName() != null && getName().equals(el.getName())) || (getName() == null && el
                            .getName() == null))) return true;
            return false;
        }

        public String getName()
        {
            Object o = getAttribute(ATTR_CELL);
            Object o1 = getAttribute(StyleConstants.NameAttribute);
            if (o != null) return o.toString() + "/" + o1;
            return super.getName();
        }

        public AttributeSet getResolveParent()
        {
            return null;
        }

        public String toString()
        {
            return "PEX: " + getElementType(this) + ":" + super.toString();
        }
    }

    public void save()
    {
        update(UpdateAction.syncronize);
        setModified(false);
    }

    public void updateElement(Element el)
    {
        fireChangedUpdate(new DefaultDocumentEvent(el.getStartOffset(), el
                .getEndOffset(), DocumentEvent.EventType.CHANGE));
    }

    @Override
    public void remove(int offset, int len) throws BadLocationException
    {
        removeEx(offset, len);
    }

    // one necessary workaround leads to another...
    // this method is used to show the correct caret pos, when we are
    // forced to make insert/remove to preserve the element structure
    // on newline deletes and not introducing dependency on NotebookUI
    int removeEx(int offset, int len) throws BadLocationException
    {
        int res = -1;
        Element el = getParagraphElement(offset);
        if (isCellHandle(offset))
        {
            removeCellBoxElement(getEnclosingCellElement(offset));
            return res;
        }
        if (isOutputCell(offset)) return res;
        Element cell = getUpperElement(el, commonCell);
        if (cell != null && offset + len == cell.getEndOffset()) return res;
        setModified(true);
        if (el.getParentElement().getElementCount() == 1)
        {
            Element el0 = this.getEnclosingCellElement(offset);
            if (getNBElement(el0) instanceof Cell)
            {
                CellTextChangeEvent e = new CellTextChangeEvent(
                        getNBElementH(el0), EventType.REMOVE, getText(offset,
                                len), offset - el0.getStartOffset(), len);
                fireCellTextChanged(e);
            } else
                // insPoint
                super.remove(offset, len);
            return res;
        }
        try
        {
            beginCompoundEdit("");
            Element el0 = this.getEnclosingCellElement(offset);
            CellTextChangeEvent e = new CellTextChangeEvent(getNBElementH(el0),
                    EventType.REMOVE, null, offset - el0.getStartOffset(), len);
            fireCellTextChanged(e);
            Element elem = getUpperElement(offset, paragraph);
            // newline was deleted, which results in two content elements
            // in the paragraph
            if (elem != null && elem.getElementCount() > 1)
            {
                int start = elem.getStartOffset();
                int end = elem.getEndOffset();
                res = elem.getElement(0).getEndOffset();
                String s = getText(start, end - start - 1);
                // keep last char to preserve the element structure
                super.remove(start, end - start - 1);
                super.insertString(start, s, contentAttrSet);
                Log.Trace("remove - replace: " + start + " length: "
                        + (end - start - 1) + ":" + s);
            }
        }
        finally
        {
            endCompoundEdit();
        }
        return res;
    }

    void group(Collection<Element> elems) throws BadLocationException
    {
        HGHandle gr_h = CellUtils.createGroupHandle();
        CellGroup gr = (CellGroup) ThisNiche.hg.get(gr_h);
        for (Element el : elems)
            gr.insert(gr.getArity(), CellUtils.makeCopy(getNBElementH(el)));
        Element first_el = elems.iterator().next();
        HGHandle first_child = getNBElementH(first_el);
        CellGroup par = (CellGroup) getContainer(first_el);
        int index = par.indexOf(first_child);
        HGHandle[] removed = new HGHandle[elems.size()];
        int i = 0;
        for (Element el : elems)
        {
            removed[i] = getNBElementH(el);
            i++;
        }
        fireCellGroupChanged(new CellGroupChangeEvent(ThisNiche.handleOf(par),
                index, new HGHandle[] { gr_h }, removed));
        // for (Element el : elems)
        // par.remove(getNBElement(el));
        // par.insert(index, gr_h);
        // endCompoundEdit();

    }

    void ungroup(Element el) throws BadLocationException
    {
        ElementType type = getElementType(el);
        if (cellGroupBox != type) return;
        // beginCompoundEdit("Cell Ungroup");
        CellGroup group = (CellGroup) getNBElement(el);
        CellGroup parent = (CellGroup) getContainer(el);
        int ind = parent.indexOf(getNBElementH(el));
        if (ind < 0)
        {
            System.err.println("Unable to ungroup: " + group.getName()
                    + ". Parent: " + parent.getName());
            return;
        }
        // parent.remove(group);
        // for (int i = group.getArity() - 1; i >= 0; i--)
        // parent.insert(ind, group.getElement(i));
        HGHandle[] added = new HGHandle[group.getArity()];
        for (int i = group.getArity() - 1; i >= 0; i--)
            added[group.getArity() - 1 - i] = group.getTargetAt(i);
        CellGroupChangeEvent e = new CellGroupChangeEvent(ThisNiche
                .handleOf(parent), ind, added, new HGHandle[] { ThisNiche
                .handleOf(group) });
        fireCellGroupChanged(e);
        // endCompoundEdit();
    }

    void removeCellBoxElement0(Element nb_el) throws BadLocationException
    {
        CellGroupMember nb = getNBElement(nb_el);
        ElementType type = (nb instanceof Cell) ? wholeCell : cellGroupBox;
        nb_el = getUpperElement(nb_el, type);
        Log.Trace("1.removeCellBoxElement0 - length: "
                + (nb_el.getEndOffset() - nb_el.getStartOffset()) + " offset: "
                + nb_el.getStartOffset() + " nb: " + nb);
        // + the insPoint after the cell
        super.remove(nb_el.getStartOffset(), nb_el.getEndOffset()
                - nb_el.getStartOffset() + 1);
        setModified(true);
    }

    void removeCellBoxElement(Element el) throws BadLocationException
    {
        removeCellBoxElement(el, true);
    }

    void removeCellBoxElement(Element el, boolean delete_empty)
            throws BadLocationException
    {
        HGHandle nb = getNBElementH(el);
        Element nb_el = getEnclosingCellElement(el);
        if (isOutputCell(nb_el) && delete_empty)
        {
            Element up = getUpperElement(nb_el, wholeCell);
            create_and_fire_eval_event(getNBElementH(up), null);

        } else
        {
            Element gr_el = getContainerEl(nb_el, false);
            if (delete_empty) while (gr_el != getRootElements()[0])
            {
                CellGroup gr = (CellGroup) getNBElement(gr_el);
                if (gr.getArity() != 1) break;
                nb_el = gr_el;
                nb = getNBElementH(gr_el);
                gr_el = getContainerEl(gr_el, false);
            }
            CellGroup gr = (CellGroup) getNBElement(gr_el);
            fireCellGroupChanged(new CellGroupChangeEvent(getNBElementH(gr_el),
                    gr.indexOf(nb), new HGHandle[0], new HGHandle[] { nb }));
            // gr.remove(nb);
        }
    }

    private void create_and_fire_eval_event(HGHandle cellH, Object value)
    {
        HGHandle oldH = CellUtils.getOutCellHandle(cellH);
        Object old = (oldH != null) ? ((Cell) ThisNiche.hg.get(oldH))
                .getValue() : null;
        EvalCellEvent e = new EvalCellEvent(cellH, value, old);
        if (e instanceof UndoableEdit) fireUndoableEditUpdate(new UndoableEditEvent(
                this, (UndoableEdit) e));
        supressEvents = true;
        EventDispatcher.dispatch(EvalCellEvent.HANDLE, getHandle(), e);
        supressEvents = false;
    }

    public String getTitle()
    {
        return ((CellGroup) ThisNiche.hg.get(bookH)).getName();
    }

    public void setTitle(String t)
    {
        ((CellGroup) ThisNiche.hg.get(bookH)).setName(t);
    }

    public static ElementType getElementType(Element el)
    {
        // TODO: some "p" tag is present in doc, so until
        // its removal, we use this rather silly approach
        if (el == null) return null;
        Object obj = el.getAttributes().getAttribute(
                StyleConstants.NameAttribute);
        if (obj instanceof ElementType) return (ElementType) obj;
        return null;
    }

    public static CellGroupMember getNBElement(Element el)
    {
        if (el == null) return null;
        HGHandle h = getNBElementH(el);
        if (h == null) return null;
        return (CellGroupMember) ThisNiche.hg.get(h);
    }

    public static HGHandle getNBElementH(Element el)
    {
        if (el == null) return null;
        return (HGHandle) el.getAttributes().getAttribute(ATTR_CELL);
    }

    public NBStyle getStyle(StyleType type)
    {
        CellGroupMember book = (CellGroupMember) ThisNiche.hg.get(bookH);
        return CellUtils.getStyle(book, type);
    }

    public void addStyle(NBStyle style)
    {
        CellGroupMember book = (CellGroupMember) ThisNiche.hg.get(bookH);
        CellUtils.addStyle(book, style);
        Style doc_style = DocUtil.getDocStyle(this, style.getStyleType());
        DocUtil.populateDocStyle(doc_style, style);
        if (style.getStyleType() == StyleType.outputCell)
        {
            outputCellFont = getFont(doc_style);
            // updateStyles();
        } else if (style.getStyleType() == StyleType.inputCell)
        {
            inputCellFont = getFont(doc_style);
            updateStyles();
        }
        updateElement(getRootElements()[0]);
    }

    public Font getInputCellFont()
    {
        return inputCellFont;
    }

    public Font getOutputCellFont()
    {
        return outputCellFont;
    }

    // return the cell element which enclose the given offset
    Element getWholeCellElement(int offset)
    {
        return getUpperElement(offset, wholeCell);
    }

    public boolean isInputCell(int offset)
    {
        return getUpperElement(offset, inputCellBox) != null;
    }

    public boolean isOutputCell(int offset)
    {
        return getUpperElement(offset, outputCellBox) != null;
    }

    public static boolean isOutputCell(Element el)
    {
        return getElementType(el) == outputCellBox;
    }

    public boolean isCellGroup(int offset)
    {
        return getUpperElement(offset, cellGroup) != null;
    }

    public boolean isCellHandle(int offset)
    {
        return getUpperElement(offset, cellHandle) != null;
    }

    public boolean isInsertionPoint(int offset)
    {
        return getUpperElement(offset, insertionPoint) != null;
    }

    public Element getEnclosingCellElement(int offset)
    {
        return getEnclosingCellElement(getParagraphElement(offset));
    }

    public static Element getEnclosingCellElement(Element el)
    {
        return getContainerEl(el, true);
    }

    static Element getContainerEl(Element el, boolean check_passed_in_el)
    {
        if (el == null) return null;
        ElementType type = getElementType(el);
        if (check_passed_in_el
                && (type == outputCellBox || type == inputCellBox || type == cellGroupBox)) return el;
        Element top = getUpperElement(el, outputCellBox, check_passed_in_el);
        if (top == null) top = getUpperElement(el, inputCellBox,
                check_passed_in_el);
        if (top == null) top = getUpperElement(el, cellGroupBox,
                check_passed_in_el);
        if (top == null) top = el.getDocument().getRootElements()[0];
        return top;
    }

    CellGroupMember getContainer(Element el)
    {
        Element e = getContainerEl(el, false);
        if (e == null) return (CellGroup) ThisNiche.hg.get(bookH);
        return getNBElement(e);
    }

    HGHandle getContainerH(Element el)
    {
        Element e = getContainerEl(el, false);
        if (e == null) return bookH;
        return getNBElementH(e);
    }

    public String getDefaultEngineName()
    {
        CellGroup book = (CellGroup) ThisNiche.hg.get(bookH);
        return CellUtils.getEngine(book);
    }

    public void setDefaultEngineName(String name)
    {
        CellGroup book = (CellGroup) ThisNiche.hg.get(bookH);
        CellUtils.setEngine(book, name);
    }

    public void attributeChanged(AttributeChangeEvent evt)
    {
        HGHandle c = evt.getCellGroupMember();
        int offset = findElementOffset(c);
        Log.Trace("attributeChanged: " + offset + ":" + evt.getName());
        if (offset < 0) return;
        Object key = evt.getName();
        try
        {
            beginCompoundEdit("" + key);
            supressEvents = true;
            if (key.equals(XMLConstants.ATTR_ENGINE)) setCellEngine(
                    (String) evt.getValue(), offset);
            else if (key.equals(XMLConstants.ATTR_ERROR)) toggleErrorCell(offset);
            // DO NOTHING
            // else if(key.equals(XMLConstants.ATTR_COLLAPSED))
            // this.collapse(el);
            // else if (key.equals(XMLConstants.ATTR_INIT_CELL))
            // toggleInitCell(offset, c instanceof Cell);
            // else if(key.equals(XMLConstants.ATTR_READONLY))
            // toggleReadonlyCell(el, c instanceof Cell);
            else if (key.equals(XMLConstants.ATTR_HTML)) toggleHTMLCell(offset);
            if (evt instanceof UndoableEdit) fireUndoableEditUpdate(new UndoableEditEvent(
                    this, (UndoableEdit) evt));
        }
        finally
        {
            supressEvents = false;
            endCompoundEdit();
        }
    }

    public void cellGroupChanged(CellGroupChangeEvent e)
    {
        HGHandle par = e.getCellGroup();
        int offset = findElementOffset(par);
        Log.Trace("cellGroupChanged: " + offset + ":" + e);
        if (offset < 0) return;
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        int index = e.getIndex();
        try
        {
            beginCompoundEdit("");
            supressEvents = true;
            if (removed != null && removed.length > 0)
            {
                for (int i = 0; i < removed.length; i++)
                {
                    int rem_offset = findElementOffset(removed[i]);
                    Element el = getUpperElement(rem_offset, removed[i], true);
                    if (el != null) removeCellBoxElement0(el);
                }
            }
            if (added != null && added.length > 0)
            {
                for (int i = 0; i < added.length; i++)
                    insert(added[i], par, index);
            }
        }
        catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            supressEvents = false;
            endCompoundEdit();
        }
    }

    private void setCellEngine(String engine, int offset)
    {
        Element el = getEnclosingCellElement(offset);
        if (el == null) return;
        CellGroupMember nb = getNBElement(el);
        if (!(nb instanceof Cell)) return;

        Cell cell = (Cell) nb;
        if (CellUtils.isHTML(cell)) toggleHTMLCell(offset);
        createScriptSupport(getEnclosingCellElement(offset), true);
        updateElement(el);
        if (engine == null) engine = CellUtils.defaultEngineName;
        if (engine.equals("html")) toggleHTMLCell(offset);
    }

    private void toggleHTMLCell(int offset)
    {
        Element el = getWholeCellElement(offset);
        if (el == null) return;
        HGHandle cell = getNBElementH(el);
        HGHandle parH = getContainerH(el);
        CellGroup par = (CellGroup) ThisNiche.hg.get(parH);
        int ind = par.indexOf(cell);
        try
        {
            removeCellBoxElement0(el);
            insert(cell, parH, ind);
        }
        catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
    }

    private void toggleErrorCell(int offset)
    {
        Element el = getEnclosingCellElement(offset);
        if (el == null) return;
        Cell cell = (Cell) getNBElement(el);
        try
        {
            Style attr = (CellUtils.isError(cell)) ? DocUtil.getDocStyle(this,
                    StyleType.error) : DocUtil.getDocStyle(this,
                    StyleType.outputCell);
            Log.Trace("Attrs: " + attr.getAttribute(StyleConstants.Foreground));
            writeLock();
            ((AbstractDocument.AbstractElement) el).addAttributes(attr);
            writeUnlock();
            updateElement(getWholeCellElement(offset));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public ScriptEngineManager getScriptManager()
    {
        return scriptManager;
    }

    public SyntaxStyle[] getSyntaxStyles(ScriptSupport support)
    {
        SyntaxStyle[] styles = syntaxStyleMap
                .get(support.getScriptEngineName());
        if (styles == null)
        {
            ArrayList<SyntaxStyleBean> main = SyntaxUtilities
                    .getSyntaxStyles(support);
            styles = new SyntaxStyle[main.size()];
            Font f = getInputCellFont();
            for (int i = 0; i < main.size(); i++)
                styles[i] = main.get(i).makeSyntaxStyle(f);
            syntaxStyleMap.put(support.getScriptEngineName(), styles);
        }
        return styles;
    }

    public void fireCellTextChanged(CellTextChangeEvent e)
    {
        if (e instanceof UndoableEdit) fireUndoableEditUpdate(new UndoableEditEvent(
                this, (UndoableEdit) e));
        supressEvents = true;
        // Log.Trace("NBDOC - fireCellTextChanged: " + e);
        EventDispatcher.dispatch(CellTextChangeEvent.HANDLE, getHandle(), e);
        supressEvents = false;
    }

    void fireCellGroupChanged(CellGroupChangeEvent e)
    {
        if (e instanceof UndoableEdit) fireUndoableEditUpdate(new UndoableEditEvent(
                this, (UndoableEdit) e));
        supressEvents = true;
        EventDispatcher.dispatch(CellGroupChangeEvent.HANDLE, getHandle(), e);
        supressEvents = false;
    }

    public void cellTextChanged(CellTextChangeEvent e)
    {
        int offset = findElementOffset(e.getCell());
        if (offset < 0)
        {
            Log.Trace("Cell: " + e.getCell() + " not found");
            return;
        }
        Log.Trace("NBDOC - cellTextChanged " + e);
        try
        {
            beginCompoundEdit("");
            supressEvents = true;
            if (e.getType() == CellTextChangeEvent.EventType.INSERT) super
                    .insertString(offset + e.getOffset(), e.getText(), null);
            else
                super.remove(offset + +e.getOffset(), e.getLength());
        }
        catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            supressEvents = false;
            endCompoundEdit();
        }
    }

    protected void superRemove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);
    }

    Map<HGHandle, Position> indexes = new HashMap<HGHandle, Position>();

    private int findElementOffset(HGHandle e)
    {
        indexes.clear();
        update(UpdateAction.index);
        if (indexes.containsKey(e)) return indexes.get(e).getOffset();
        return -1;
    }

    public static class NamedCompoundEdit extends CompoundEdit
    {
        private String name;

        public NamedCompoundEdit(String n)
        {
            super();
            name = n;
        }

        public String getRedoPresentationName()
        {
            return name != null ? "Redo " + getPresentationName() : super
                    .getRedoPresentationName();
        }

        public String getUndoPresentationName()
        {
            return name != null ? "Undo " + getPresentationName() : super
                    .getUndoPresentationName();
        }

        public String getPresentationName()
        {
            return name != null ? name : super.getPresentationName();
        }
    }

    private class EditVector<T extends UndoableEdit> extends Vector<T>
    {
        public synchronized boolean add(T edit)
        {
            if (edit == null) return false;
            return isCompoundEditInProgress() ? cEdit.addEdit(edit) : super
                    .add(edit);
        }
    }

    public void addModificationListener(ModificationListener l)
    {
        listenerList.add(ModificationListener.class, l);
    }

    public void removeModificationListener(ModificationListener l)
    {
        listenerList.remove(ModificationListener.class, l);
    }

    protected void fireModificationChange(Object o)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            if (listeners[i] == ModificationListener.class) ((ModificationListener) listeners[i + 1])
                    .documentModified(o);
    }

    public static interface ModificationListener extends EventListener
    {
        void documentModified(Object o);
    }

    boolean supressEvents = false;

    @Override
    public String toString()
    {
        return "NBDOC: " + getTitle() + ":" + getHandle();
    }
}
