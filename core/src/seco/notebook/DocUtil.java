package seco.notebook;

import static seco.notebook.ElementType.cellGroup;
import static seco.notebook.ElementType.cellGroupBox;
import static seco.notebook.ElementType.cellHandle;
import static seco.notebook.ElementType.commonCell;
import static seco.notebook.ElementType.component;
import static seco.notebook.ElementType.htmlCell;
import static seco.notebook.ElementType.inputCellBox;
import static seco.notebook.ElementType.insertionPoint;
import static seco.notebook.ElementType.outputCellBox;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

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
import seco.rtenv.RuntimeContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.IOUtils;
import seco.util.RequestProcessor;

abstract public class DocUtil
{
    static final String ATTR_CELL = "CELL";
    private static final char[] NEWLINE = new char[] { '\n' };

    protected static void addContent(String data, Vector<ElementSpec> vec)
    {
        int currpos = 0;
        boolean found = false;
        for (int i = 0; i < data.length(); i++)
        {
            char c = data.charAt(i);
            if (c == NEWLINE[0])
            {
                found = true;
                char[] chunk = new char[(i + 1) - currpos];
                data.getChars(currpos, currpos + chunk.length, chunk, 0);
                addSubContent(chunk, 0, vec);
                i++; currpos = i;
            }
        }
        // if we are here, means no \n found at all
        if (!found) addSubContent(data.toCharArray(), data.length(), vec);
    }
    
    private static void addSubContent(char[] data, int offs,
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
        CellGroupMember cgm = (CellGroupMember) ThisNiche.graph.get(cellH);
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

        Cell cell = (Cell) ThisNiche.graph.get(cellH);
        attr = getDocStyle(doc, StyleType.inputCell);
        attr.addAttribute(ATTR_CELL, cellH);
        startTag(inputCellBox, attr, 0, vec);
        // attr.removeAttribute(ATTR_CELL);
        if (!CellUtils.isHTML(cell)) startTag(commonCell, attr, 0, vec);
        else
            startTag(htmlCell, attr, 0, vec);
        String text = CellUtils.getText(cell);
        if (!text.endsWith("\n")) text += "\n";
        addContent(text, vec);
        endTag(vec);
        attr.addAttribute(ATTR_CELL, cellH);
        createCellHandle(attr, vec);
        attr.removeAttribute(ATTR_CELL);
        endTag(vec);
        createInsertionPoint(attr, vec);
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, cellH, doc
                .getHandle(), AttributeChangeHandler.getHandle());
        if (NotebookDocument.DIRECT_EVENTING)
        {
            CellUtils.addEventPubSub(CellTextChangeEvent.HANDLE, cellH, doc
                    .getHandle(), CellTextChangeHandler.getHandle());
            CellUtils.addEventPubSub(EvalCellEvent.HANDLE, cellH, doc
                    .getHandle(), EvalCellHandler.getHandle());

        }
        else
        {
            CellUtils.addMutualEventPubSub(CellTextChangeEvent.HANDLE, cellH,
                    doc.getHandle(), CellTextChangeHandler.getHandle());
            CellUtils.addMutualEventPubSub(EvalCellEvent.HANDLE, cellH, doc
                    .getHandle(), EvalCellHandler.getHandle());
        }
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec, boolean genInsP)
    {
        createOutputCell(doc, cellH, attr, vec, genInsP, null);
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec, boolean genInsP,
            EvalResult e)
    {
        attr.addAttribute(ATTR_CELL, cellH);
        Cell c = (Cell) ThisNiche.graph.get(cellH);
        startTag(outputCellBox, attr, 0, vec);
        attr.removeAttribute(ATTR_CELL);
        boolean isError = (e != null) ? e.isError() : CellUtils.isError(c);
        attr = isError ? getDocStyle(doc, StyleType.error) : getDocStyle(doc,
                StyleType.outputCell);
        createOutputCellContents(doc, c, attr, vec, e);
        attr.addAttribute(ATTR_CELL, cellH);
        createCellHandle(attr, vec);
        attr.removeAttribute(ATTR_CELL);
        endTag(vec);
        if (genInsP) createInsertionPoint(attr, vec);
        if (NotebookDocument.DIRECT_EVENTING) CellUtils.addEventPubSub(
                AttributeChangeEvent.HANDLE, cellH, doc.getHandle(),
                AttributeChangeHandler.getHandle());
        else
            CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, cellH,
                    doc.getHandle(), AttributeChangeHandler.getHandle());
    }

    static void createOutputCell(NotebookDocument doc, HGHandle cellH,
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        createOutputCell(doc, cellH, attr, vec, true, null);
    }

    private static void createOutputCellContents(NotebookDocument doc, Cell c,
            MutableAttributeSet attr, Vector<ElementSpec> vec, EvalResult e)
    {
        startTag(commonCell, attr, 0, vec);
        Object val = (e != null) ? e.getComponent() : c.getValue();
        String text = (e != null) ? e.getText() : "";
        if (text == null) text = "null";
        Component comp = null;
        if (val != null)
        {
            if (!(val instanceof Component)) text = val.toString();
            else
                comp = (Component) val;
        }

        if (comp == null)// text.length() > 0)
        {
            if (!text.endsWith("\n")) text += "\n";
            addContent(text, vec);
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

    public static String getEngineName(NotebookDocument doc, Cell cell)
    {
        String name = CellUtils.getEngine(cell);
        return (name != null) ? name : doc.getDefaultEngineName();
    }

    static void eval_result_in_aux_thread(final NotebookDocument doc,
            final Cell cell)
    {
        Runnable r = new Runnable() {
            @Override
            public void run()
            {
                EvalResult res = CellUtils.eval_result(cell, getEngineName(doc,
                        cell), calc_eval_ctx(doc.getBook(), cell));
                doc.handle_delayed_evaluation(res, ThisNiche.handleOf(cell));
            }
        };
        RequestProcessor.getDefault().post(r);
     }
    
    private static EvaluationContext calc_eval_ctx(CellGroupMember top, CellGroupMember cell)
    {
        HGHandle res = CellUtils.getEvalContextH(cell);
        if(res != null &&  ThisNiche.graph.get(res) instanceof RuntimeContext) 
            return ThisNiche.getEvaluationContext(res);
        CellGroup par = CellUtils.getParentGroup(ThisNiche.handleOf(cell));
        if(par == top || par == null)
        {
            if(par != null)
               res = CellUtils.getEvalContextH(par);
            if(res == null) 
                res = ThisNiche.TOP_CONTEXT_HANDLE;
            return ThisNiche.getEvaluationContext(res);
        }
        return calc_eval_ctx(top, par);
    }
    
    public static Component maybe_clone(Component c)
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
        CellGroup cell_group = (CellGroup) ThisNiche.graph.get(cell_groupH);
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
        if (NotebookDocument.DIRECT_EVENTING)
        {
            CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, cell_groupH,
                    doc.getHandle(), AttributeChangeHandler.getHandle());
            CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE, cell_groupH,
                    doc.getHandle(), CellGroupChangeHandler.getHandle());
        }
        else
        {
            CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE,
                    cell_groupH, doc.getHandle(), AttributeChangeHandler
                            .getHandle());
            CellUtils.addMutualEventPubSub(CellGroupChangeEvent.HANDLE,
                    cell_groupH, doc.getHandle(), CellGroupChangeHandler
                            .getHandle());
        }
    }
}
