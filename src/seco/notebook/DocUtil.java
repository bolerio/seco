package seco.notebook;

import static seco.notebook.ElementType.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.DefaultStyledDocument.ElementSpec;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EvalResult;
import seco.events.handlers.AttributeChangeHandler;
import seco.events.handlers.CellGroupChangeHandler;
import seco.events.handlers.CellTextChangeHandler;
import seco.events.handlers.EvalCellHandler;
import seco.rtenv.EvaluationContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.IOUtils;
import seco.things.Scriptlet;

abstract class DocUtil
{
    static final String ATTR_CELL = "CELL";
    private static final char[] NEWLINE = new char[] { '\n' };

    protected static void addSubContent(char[] data, int offs,
            Vector<ElementSpec> vec)
    {
        ElementSpec es1 = new ElementSpec(NotebookDocument.charAttrSet,
                ElementSpec.StartTagType);
        vec.addElement(es1);
        ElementSpec es = new ElementSpec(NotebookDocument.contentAttrSet,
                ElementSpec.ContentType, data, offs, data.length);
        vec.addElement(es);
        vec.addElement(new ElementSpec(null, ElementSpec.EndTagType));
    }

    protected static void addContent(char[] data, int offs,
            Vector<ElementSpec> vec, int start)
    {
        // System.out.println("addContent:" + data.length + ":" + start);
        int end = data.length;
        if (start == end) return;
        for (int i = start; i < end; i++)
        {
            char c = data[i];
            if (c == NEWLINE[0])
            {
                // System.out.println("NEW - addContent - inner1:" + (i+1 -
                // start));
                char[] data1 = new char[(i + 1) - start];
                System.arraycopy(data, start, data1, 0, data1.length);
                addSubContent(data1, 0, vec);
                addContent(data, 0, vec, i + 1);
                return;
            }
        }
        // if we are here, means no \n - add all
        if (data.length == 0) return;
        char[] data1 = new char[end - start];
        System.arraycopy(data, start, data1, 0, data1.length);
        // System.out.println("NEW - addContent - inner3:" + data1.length);
        addSubContent(data1, offs + end, vec);
    }

    protected static void startTag(ElementType e, MutableAttributeSet a,
            int pos, Vector<ElementSpec> vec)
    {
        a.addAttribute(StyleConstants.NameAttribute, e);
        ElementSpec es = new ElementSpec(a.copyAttributes(),
                ElementSpec.StartTagType);
        vec.addElement(es);
    }

    protected static void endTag(Vector<ElementSpec> vec)
    {
        vec.addElement(new ElementSpec(null, ElementSpec.EndTagType));
    }

    static void createComponent(Component c, MutableAttributeSet attr,
            Vector<ElementSpec> vec)
    {
        if (c == null) return;
        System.out.println("createComponent: " + c);
        attr.addAttribute(StyleConstants.ComponentAttribute, c);
        startTag(component, attr, 0, vec);
        addSubContent(NEWLINE, 0, vec);
        endTag(vec);
    }

    static void populateDocStyle(Style doc_style, NBStyle style)
    {
        StyleConstants.setForeground(doc_style, (Color) style
                .getDefaultValue(StyleAttribs.FG_COLOR));
        FontEx f = (FontEx) style.getDefaultValue(StyleAttribs.FONT);
        f.populateStyle(doc_style);
    }

    static void createCellHandle(MutableAttributeSet attr,
            Vector<ElementSpec> vec)
    {
        startTag(cellHandle, attr, 0, vec);
        addSubContent(NEWLINE, 0, vec);
        endTag(vec);
    }

    static void createInsertionPoint(MutableAttributeSet attr,
            Vector<ElementSpec> vec)
    {
        Object old = attr.getAttribute(NotebookDocument.ATTR_CELL);
        if (old != null) attr.removeAttribute(NotebookDocument.ATTR_CELL);
        startTag(insertionPoint, attr, 0, vec);
        addSubContent(NEWLINE, 0, vec);
        endTag(vec);
        if (old != null) attr.addAttribute(NotebookDocument.ATTR_CELL, old);
    }

    static void createCellGroupMember(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        CellGroupMember cgm = (CellGroupMember) ThisNiche.hg.get(cellH);
        if (cgm instanceof CellGroup) createCellGroup(doc, cellH, attr, vec);
        else if (cgm instanceof Cell)
        {
            if (!CellUtils.isInputCell(cgm)) createOutputCell(doc, cellH, attr,
                    vec);
            else
                createCell(doc, cellH, attr, vec);
        }
    }

    static void createCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {

        Cell cell = (Cell) ThisNiche.hg.get(cellH);
        attr = getDocStyle(doc, StyleType.inputCell);
        attr.addAttribute(ATTR_CELL, cellH);
        startTag(inputCellBox, attr, 0, vec);
        // attr.removeAttribute(ATTR_CELL);
        if (!CellUtils.isHTML(cell)) startTag(commonCell, attr, 0, vec);
        else
            startTag(htmlCell, attr, 0, vec);
        String text = CellUtils.getText(cell);
        if (!text.endsWith("\n")) text += "\n";
        addContent(text.toCharArray(), 0, vec, 0);
        endTag(vec);
        attr.addAttribute(ATTR_CELL, cellH);
        createCellHandle(attr, vec);
        attr.removeAttribute(ATTR_CELL);
        endTag(vec);
        createInsertionPoint(attr, vec);
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, cellH, doc
                .getHandle(), AttributeChangeHandler.getInstance());
        CellUtils.addMutualEventPubSub(CellTextChangeEvent.HANDLE, cellH, doc
                .getHandle(), CellTextChangeHandler.getInstance());
        CellUtils.addMutualEventPubSub(EvalCellEvent.HANDLE, cellH, doc
                .getHandle(), EvalCellHandler.getInstance());

// now done in a special action        
//        if (CellUtils.isInitCell(cell))
//        {
//            EvalResult res = eval_result(doc, cell);
//            createOutputCell(doc, CellUtils.createOutputCellH(cellH, res
//                    .getText(), res.getComponent(), false), attr, vec);
//        }
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec, boolean genInsP)
    {
        createOutputCell(doc, cellH, attr, vec, genInsP, null);
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec, boolean genInsP,
            EvalCellEvent e)
    {
        attr.addAttribute(ATTR_CELL, cellH);
        Cell c = (Cell) ThisNiche.hg.get(cellH);
        startTag(outputCellBox, attr, 0, vec);
        attr.removeAttribute(ATTR_CELL);
        boolean isError = (e != null) ? e.getValue().isError() : CellUtils
                .isError(c);
        attr = isError ? getDocStyle(doc, StyleType.error) : getDocStyle(doc,
                StyleType.outputCell);
        createOutputCellContents(doc, c, attr, vec, e);
        attr.addAttribute(ATTR_CELL, cellH);
        createCellHandle(attr, vec);
        attr.removeAttribute(ATTR_CELL);
        endTag(vec);
        if (genInsP) createInsertionPoint(attr, vec);
        CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, cellH, doc
                .getHandle(), AttributeChangeHandler.getInstance());
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        createOutputCell(doc, cellH, attr, vec, true, null);
    }

    private static void createOutputCellContents(NotebookDocument doc, Cell c,
            MutableAttributeSet attr, Vector<ElementSpec> vec, EvalCellEvent e)
    {
        startTag(commonCell, attr, 0, vec);
        Object val = (e != null) ? e.getValue().getComponent() : c.getValue();
        String text = (e != null) ? e.getValue().getText() : "";
        if (text == null) text = "";
        Component comp = null;
        if (val != null)
        {
            if (!(val instanceof Component)) text = val.toString();
            else
                comp = (Component) val;
        }

        if (text.length() > 0)
        {
            if (!text.endsWith("\n")) text += "\n";
            addContent(text.toCharArray(), 0, vec, 0);
        }
        else
            createComponent(comp, attr, vec);
        endTag(vec);
    }

    static javax.swing.text.Style getDocStyle(NotebookDocument doc,
            StyleType type)
    {
        javax.swing.text.Style doc_style = doc.getStyle(type.toString());
        if (doc_style != null) return doc_style;
        doc_style = doc.addStyle(type.toString(), null);
        populateDocStyle(doc_style, CellUtils.getStyle(doc.getBook(), type));
        return doc_style;
    }

    static EvalResult eval_result(NotebookDocument doc, Cell cell)
    {
        EvalResult res = new EvalResult();
        try
        {
            String name = CellUtils.getEngine(cell);
            if (ThisNiche.getHyperGraph() == null) // are we running a niche or
            // some other testing/development mode?
            {
                ScriptEngine eng = doc.scriptManager.getEngineByName(name);
                if (eng != null)
                {
                    Object o = eng.eval(CellUtils.getText(cell), doc.context);
                    if (o instanceof Component) res
                            .setComponent(maybe_clone((Component) o));
                    else
                        res.setText("" + o);
                }
                else
                {
                    res.setError(true);
                    res.setText("Unknown scripting engine: " + name);
                }
            }
            else
            {
                if (doc.evalContext == null)
                    doc.evalContext = ThisNiche.getTopContext();
                if (doc.evalContext == null)
                    doc.evalContext = ThisNiche
                            .getEvaluationContext(ThisNiche.TOP_CONTEXT_HANDLE);
                Object o = doc.evalContext.eval(name, CellUtils.getText(cell));
                if (o instanceof Component)
                {
                    Component c = (Component) o;
                    if (c instanceof Window)
                    {
                        res.setText("Window: " + c);
                    }
                    else if (c.getParent() != null)
                    {
                        // If this component is displayed in some output cell,
                        // detach it from there,
                        // otherwise we don't own the component so we
                        // don't display it.
                        if (c.getParent() instanceof seco.notebook.view.ResizableComponent)
                        {
                            c.getParent().remove(c);
                            res.setComponent(c);
                        }
                        else
                            res.setText("AWT Component - " + c.toString()
                                    + " -- belongs to parent component "
                                    + c.getParent().toString());
                    }
                    else
                        res.setComponent(c);
                }
                else
                    res.setText(eval_to_string(o, doc.evalContext));
            }
        }
        catch (ScriptException ex)
        {
            res.setError(true);
            StringWriter w = new StringWriter();
            PrintWriter writer = new PrintWriter(w);
            ex.printStackTrace(writer);
            res.setText(w.toString());
        }
        return res;
    }

    private static String eval_to_string(Object o, EvaluationContext ctx)
    {
        ClassLoader save = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(ctx.getClassLoader());
            return "" + o;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(save);
        }
    }

    static Component maybe_clone(Component c)
    {
        if (c.getParent() != null)
        {
            Component out = IOUtils.cloneCellComp(c);
            if (out != null) return out;
        }
        return c;
    }

    static void createCellGroup(NotebookDocument doc, HGHandle cell_groupH,
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        attr.addAttribute(ATTR_CELL, cell_groupH);
        startTag(cellGroupBox, attr, 0, vec);
        startTag(cellGroup, attr, 0, vec);
        CellGroup cell_group = (CellGroup) ThisNiche.hg.get(cell_groupH);
        attr.removeAttribute(ATTR_CELL);
        for (int i = 0; i < cell_group.getArity(); i++)
        {
            if (i == 0) createInsertionPoint(attr, vec);
            createCellGroupMember(doc, cell_group.getTargetAt(i), attr, vec);
        }
        endTag(vec);
        // refresh damaged ATTR_CELL attribute
        attr.addAttribute(ATTR_CELL, cell_groupH);
        createCellHandle(attr, vec);
        attr.removeAttribute(ATTR_CELL);
        endTag(vec);
        createInsertionPoint(attr, vec);
        CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE,
                cell_groupH, doc.getHandle(), AttributeChangeHandler
                        .getInstance());
        CellUtils.addMutualEventPubSub(CellGroupChangeEvent.HANDLE,
                cell_groupH, doc.getHandle(), CellGroupChangeHandler
                        .getInstance());
    }
}
