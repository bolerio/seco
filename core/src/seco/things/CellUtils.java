package seco.things;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGAtomRef;
import org.hypergraphdb.query.HGQueryCondition;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.BackupLink;
import seco.events.CellGroupChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EvalResult;
import seco.events.EventDispatcher;
import seco.events.EventPubSub;
import seco.events.BackupLink.EventPubSubInfo;
import seco.events.handlers.CopyAttributeChangeHandler;
import seco.events.handlers.CopyCellGroupChangeHandler;
import seco.events.handlers.CopyCellTextChangeHandler;
import seco.events.handlers.CopyEvalCellHandler;
import seco.gui.CellContainerVisual;
import seco.gui.GUIHelper;
import seco.gui.JComponentVisual;
import seco.gui.NBUIVisual;
import seco.gui.PSwingNode;
import seco.gui.TabbedPaneVisual;
import seco.gui.VisualAttribs;
import seco.gui.VisualsManager;
import seco.gui.layout.LayoutHandler;
import seco.gui.piccolo.AffineTransformEx;
import seco.notebook.DocUtil;
import seco.notebook.NBStyle;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.StyleAttribs;
import seco.notebook.StyleType;
import seco.notebook.XMLConstants;
import seco.rtenv.EvaluationContext;
import seco.rtenv.RuntimeContext;
import edu.umd.cs.piccolo.util.PAffineTransform;

public class CellUtils
{
    public static String defaultEngineName = "beanshell";
    static Map<StyleType, NBStyle> styles = new EnumMap<StyleType, NBStyle>(
            StyleType.class);

    static
    {
        for (StyleType t : StyleType.values())
            styles.put(t, new NBStyle(t));
        styles.get(StyleType.outputCell).put(StyleAttribs.BORDER_COLOR,
                Color.white);
        styles.get(StyleType.outputCell).put(StyleAttribs.FG_COLOR,
                new java.awt.Color(0, 128, 0));
    }

    private CellUtils()
    {
    }

    public static void evaluateVisibleInitCells()
    {
        processGroup((CellGroup) ThisNiche.graph
                .get(ThisNiche.TOP_CELL_GROUP_HANDLE));
    }

    private static void processGroup(CellGroup group)
    {
        for (int i = 0; i < group.getArity(); i++)
        {
            CellGroupMember cgm = group.getElement(i);
            if (cgm == null || cgm instanceof Cell) continue;
            CellVisual visual = CellUtils.getVisual(cgm);
            if (visual instanceof NBUIVisual) evalInitCells((CellGroup) cgm);
            else if (visual instanceof CellContainerVisual
                    || visual instanceof TabbedPaneVisual)
                processGroup((CellGroup) cgm);
        }
    }

    public static void evalInitCells(CellGroup group)
    {
        String name = CellUtils.getEngine(group);
        if (name == null) name = defaultEngineName;
        evalInitGroup(group, name, ThisNiche.getTopContext(), CellUtils.isInitCell(group));
    }

    public static void evalInitGroup(CellGroup group, String inherited_engine_name, EvaluationContext ctx,
            boolean inherited_init)
    {
        String name = CellUtils.getEngine(group);
        if (name == null) name = inherited_engine_name;
        if (name == null) name = defaultEngineName;
        HGHandle new_ctxH = CellUtils.getEvalContextH(group);
        if(new_ctxH != null && 
                ThisNiche.graph.get(new_ctxH) instanceof RuntimeContext)
            ctx = ThisNiche.getEvaluationContext(new_ctxH);
        for (int i = 0; i < group.getArity(); i++)
        {
            CellGroupMember cgm = group.getElement(i);
            boolean init = CellUtils.isInitCell(cgm);
            if (!init && inherited_init) init = true;
            if (cgm instanceof CellGroup) evalInitGroup((CellGroup) cgm, name, ctx, init);
            else if (CellUtils.isInputCell(cgm))
            {
                if (!init) continue;
                evalCell(group, (Cell) cgm, name, ctx);
            }
        }
    }
    
    public static void evalGroup(CellGroup group, String inherited_engine_name, EvaluationContext ctx)
    {
        String name = CellUtils.getEngine(group);
        if (name == null) name = inherited_engine_name;
        if (name == null) name = defaultEngineName;
        HGHandle new_ctxH = CellUtils.getEvalContextH(group);
        if(new_ctxH != null && 
                ThisNiche.graph.get(new_ctxH) instanceof RuntimeContext)
            ctx = ThisNiche.getEvaluationContext(new_ctxH);
        for (int i = 0; i < group.getArity(); i++)
        {
            CellGroupMember cgm = group.getElement(i);
            if (cgm instanceof CellGroup) evalGroup((CellGroup) cgm, name, ctx);
            else if (CellUtils.isInputCell(cgm))
               evalCell(group, (Cell) cgm, name, ctx);
       }
    }
    
    public static void evalCell(Cell cell,
            String inherited_engine_name, EvaluationContext ctx)
    {
        CellGroup parent = CellUtils.getParentGroup(ThisNiche.handleOf(cell));
        evalCell(parent, cell, inherited_engine_name, ctx);
    }

    private static void evalCell(CellGroup parent, Cell cell,
            String inherited_engine_name, EvaluationContext ctx)
    {
        HGHandle cellH = ThisNiche.handleOf(cell);
        HGHandle new_ctxH = CellUtils.getEvalContextH(cell);
        if(new_ctxH != null)
            ctx = ThisNiche.getEvaluationContext(new_ctxH);
        EvalResult res = eval_result(cell, inherited_engine_name, ctx);
        EvalCellEvent e = create_eval_event(cellH, res);
        // check if we already have an output cell. if not, add one
        List<Cell> list = getOutCells(cellH);
        if (list.isEmpty())
        {
            HGHandle outH = createOutputCellH(cellH, "", null, false);
            CellGroupChangeEvent ev = new CellGroupChangeEvent(ThisNiche
                    .handleOf(parent), parent.indexOf(cellH) + 1,
                    new HGHandle[] { outH }, new HGHandle[0]);
            parent.batchProcess(ev);
        }
        EventDispatcher.dispatch(EvalCellEvent.HANDLE, e.getCellHandle(), e);
    }

    public static EvalResult eval_result(final Cell cell, String engine_name, EvaluationContext ctx)
    {
        EvalResult res = new EvalResult();
        try
        {
            String name = CellUtils.getEngine(cell);
            if (name == null) name = engine_name;
            Object o = ctx.eval(name, getText(cell));
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
                res.setText(eval_to_string(o, ThisNiche.getTopContext()));

        }
        catch (Throwable ex)
        {
            res.setError(true);
            StringWriter w = new StringWriter();
            PrintWriter writer = new PrintWriter(w);
            ex.printStackTrace(writer);
            res.setText(w.toString());
        }
        return res;
    }

    public static String eval_to_string(Object o, EvaluationContext ctx)
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

    public static EvalCellEvent create_eval_event(HGHandle cellH,
            EvalResult value)
    {
        HGHandle oldH = CellUtils.getOutCellHandle(cellH);
        Cell c = (oldH != null) ? ((Cell) ThisNiche.graph.get(oldH)) : null;
        Object old = (oldH != null) ? c.getValue() : null;
        EvalResult old_res = new EvalResult(old, CellUtils.isError(c));
        return new EvalCellEvent(cellH, value, old_res);
    }

    static void updateCellGroupMember(CellGroupMember c)
    {
        if (c instanceof Cell)
        {
            // Cell cell = (Cell) c;
            // Object val = cell.getValue();
            // cell.ref = new HGAtomRef(ThisNiche.hg.add(val),
            // HGAtomRef.Mode.hard);
        }
        ThisNiche.graph.update(c);
    }

    public static HGHandle getOrCreateCellHForRefH(HGHandle h)
    {
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        HGHandle outH = ThisNiche.handleOf(out);
        if (outH == null) 
            outH = ThisNiche.graph.add(out);
        return outH;
    }

    // TODO: fix this uglyness
    public static CellVisual getVisual(CellGroupMember c)
    {
        HGHandle visH = (c.getVisual() != null) ? c.getVisual()
                : VisualsManager.defaultVisualForAtom(ThisNiche.handleOf(c));
        if (visH != null) return (CellVisual) ThisNiche.graph.get(visH);
        CellVisual visual = null;
        if (visH == null || visH.equals(ThisNiche.graph.getHandleFactory().nullHandle()))
        {
            if (c instanceof CellGroup || CellUtils.isInputCell(c)) visual = new NBUIVisual();
            else
            {
                visual = new JComponentVisual();
            }
        }

        return visual;
    }

    public static CellGroup getParentGroup(HGHandle cgmH)
    {
        // TODO: doesn't always work
        // CellGroup c = (CellGroup) hg.getOne(ThisNiche.hg, hg.and(hg
        // .type(CellGroup.class), hg.incident(cgmH), hg
        // .orderedLink(new HGHandle[] { cgmH,
        // HGHandleFactory.anyHandle })));
        // return c;
        for (HGHandle h : ThisNiche.graph.getIncidenceSet(cgmH))
        {
            Object o = ThisNiche.graph.get(h);
            if (o instanceof CellGroup) return (CellGroup) o;
        }
        return null;
    }

    public static HGHandle makeCellH(String text, String lang)
    {
        Scriptlet s = new Scriptlet(lang, text);
        HGHandle h = ThisNiche.graph.add(s);
        return CellUtils.getOrCreateCellHForRefH(h);
    }

    public static boolean isHTML(CellGroupMember c)
    {
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_HTML);
        return b != null && b.booleanValue();
    }

    public static void setHTML(CellGroupMember c, boolean html)
    {
        c.setAttribute(XMLConstants.ATTR_HTML, html);
    }

    public static boolean isError(CellGroupMember c)
    {
        if (c == null) return false;
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_ERROR);
        return b != null && b.booleanValue();
    }

    public static boolean isReadonly(CellGroupMember c)
    {
        if (c == null) return false;
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_READONLY);
        return b != null && b.booleanValue();
    }

    public static void setReadonly(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_READONLY, b);
    }

    public static boolean isCollapsed(CellGroupMember c)
    {
        if (c == null) return false;
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_COLLAPSED);
        return b != null && b.booleanValue();
    }

    public static boolean isMinimized(CellGroupMember cgm)
    {
        Boolean b = (Boolean) cgm.getAttribute(VisualAttribs.minimized);
        return b != null && b.booleanValue();
    }

    public static void toggleMinimized(CellGroupMember cgm)
    {
        boolean b = isMinimized(cgm);
        // first render in normal state the maximized cell, then minimize
        boolean max = false;
        if (!b && isMaximized(cgm)) 
        {
            toggleMaximized(cgm);
            max = true;
        }
        cgm.setAttribute(VisualAttribs.minimized, !b);
        //restore the max attrib without firing event
        if(max)
            cgm.getAttributes().put(VisualAttribs.maximized, true);
        //when restoring from minimized state, maximize if needed
        if(b && isMaximized(cgm))
        {
            //need to clear the attrib without firing 
            cgm.getAttributes().put(VisualAttribs.maximized, false);
            cgm.setAttribute(VisualAttribs.maximized, true);
        }
    }

    public static boolean isMaximized(CellGroupMember cgm)
    {
        Boolean b = (Boolean) cgm.getAttribute(VisualAttribs.maximized);
        return b != null && b.booleanValue();
    }

    public static void toggleMaximized(CellGroupMember cgm)
    {
        boolean b = isMaximized(cgm);
        // first render in normal state the minimized cell, then maximize
        if (!b && isMinimized(cgm)) toggleMinimized(cgm);
        cgm.setAttribute(VisualAttribs.maximized, !b);
    }

    public static boolean isShowTitle(CellGroupMember cgm)
    {
        Boolean b = (Boolean) cgm.getAttribute(VisualAttribs.showTitle);
        return b != null && b.booleanValue();
    }

    public static void toggleShowTitle(CellGroupMember cgm)
    {
        boolean b = isShowTitle(cgm);
        cgm.setAttribute(VisualAttribs.showTitle, !b);
    }

    public static void toggleShowTitle(HGHandle h)
    {
        toggleShowTitle((CellGroupMember) ThisNiche.graph.get(h));
    }

    public static void setCollapsed(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_COLLAPSED, b);
    }

    public static void setError(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_ERROR, b);
    }

    public static void setName(CellGroupMember c, String title)
    {
        c.setAttribute(VisualAttribs.name, title);
    }

    public static String getName(CellGroupMember c)
    {
        return (String) c.getAttribute(VisualAttribs.name);
    }

    public static void setError(HGHandle h, boolean b)
    {
        CellGroupMember c = (CellGroupMember) ThisNiche.graph.get(h);
        c.setAttribute(XMLConstants.ATTR_ERROR, b);
    }
    
    public static void setDescription(HGHandle h, String d)
    {
       // CellGroupMember c = (CellGroupMember) ThisNiche.graph.get(h);
        HGHandle link = getDescriptionLinkH(h);
        if(link != null)
            ThisNiche.graph.remove(link);
        ThisNiche.graph.add(new DescriptionLink(h,  ThisNiche.graph.add(d)));
    }
    
    public static String getDescription(HGHandle h)
    {
        HGHandle linkH = getDescriptionLinkH(h);
        if(linkH == null) return null;
        DescriptionLink link = ThisNiche.graph.get(linkH);
        return ThisNiche.graph.get(link.getDescriptionHandle());
    }
    
    private static HGHandle getDescriptionLinkH(HGHandle h)
    {
        return hg.findOne(ThisNiche.graph, hg.and(hg.incident(h), hg.type(DescriptionLink.class)));
    }

    public static boolean isInitCell(CellGroupMember c)
    {
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_INIT_CELL);
        return b != null && b.booleanValue();
    }

    public static void setInitCell(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_INIT_CELL, b);
    }

    public static boolean isInputCell(CellGroupMember m)
    {
        if (m instanceof CellGroup) return false;
        Cell c = (Cell) m;
        return c.getValue() instanceof Scriptlet;
    }

    public static void setCellText(Cell c, String text)
    {
        Object o = ThisNiche.graph.get(c.ref.getReferent());
        // System.out.println("CellUtils - setText: " + text);
        if (o instanceof Scriptlet)
        {
            ((Scriptlet) o).setCode(text);
            ThisNiche.graph.update(o);
            return;
        }
        HGHandle h = ThisNiche.handleOf(text);
        if (h == null) h = ThisNiche.graph.add(text);
        c.ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        // ThisNiche.hg.update(c);
    }

    public static void toggleAttribute(CellGroupMember c, Object attr_name)
    {
        Object attr = c.getAttribute(attr_name);
        boolean b = attr != null ? ((Boolean) attr).booleanValue() : false;
        c.setAttribute(attr_name, !b);
    }
    
    public static void setBoolAttribute(CellGroupMember c, Object attr_name, boolean value)
    {
        Object attr = c.getAttribute(attr_name);
        boolean b = attr != null ? ((Boolean) attr).booleanValue() : false;
        if(value != b) c.setAttribute(attr_name, value);
    }

    public static String getText(Cell c)
    {
        Object v = c.getValue();
        if (v instanceof Scriptlet) return ((Scriptlet) v).getCode();
        return (v != null) ? v.toString() : "null";
    }

    public static String getNotNullEngine(CellGroupMember c)
    {
        String eng = (String) c.getAttribute(XMLConstants.ATTR_ENGINE);
        return eng != null ? eng : defaultEngineName;
    }

    public static String getEngine(CellGroupMember c)
    {
        return (String) c.getAttribute(XMLConstants.ATTR_ENGINE);
    }

    public static void setEngine(CellGroupMember c, String s)
    {
        c.setAttribute(XMLConstants.ATTR_ENGINE, s);
    }
    
    public static HGHandle getEvalContextH(CellGroupMember c)
    {
        HGHandle ctxH = ThisNiche.findContextLink(ThisNiche.handleOf(c));
        return ctxH;
    }
    
    public static void setEvalContextH(CellGroupMember c, HGHandle h)
    {
         ThisNiche.setContextFor(ThisNiche.handleOf(c), h);
    }
   
    public static void setBounds(CellGroupMember c, Rectangle r)
    {
        c.setAttribute(VisualAttribs.rect, r);
    }

    public static Rectangle getBounds(CellGroupMember c)
    {
        return (Rectangle) c.getAttribute(VisualAttribs.rect);
    }

    public static void setMinBounds(CellGroupMember c, Rectangle r)
    {
        c.setAttribute(VisualAttribs.minRect, r);
    }

    public static Rectangle getMinBounds(CellGroupMember c)
    {
        return (Rectangle) c.getAttribute(VisualAttribs.minRect);
    }

    public static Rectangle getAppropriateBounds(CellGroupMember c)
    {
        return isMinimized(c) ? getMinBounds(c) : getBounds(c);
    }

    public static void setAppropriateBounds(CellGroupMember c, Rectangle r)
    {
        setAppropriateBounds(c, r, false);
    }

    public static void setAppropriateBounds(CellGroupMember c, Rectangle r,
            boolean remove_other_bounds)
    {
        if (isMinimized(c))
        {
            setMinBounds(c, r);
            if (remove_other_bounds)
                c.getAttributes().remove(VisualAttribs.rect);
        }
        else
        {
            setBounds(c, r);
            if (remove_other_bounds)
                c.getAttributes().remove(VisualAttribs.minRect);
        }

        // AffineTransformEx at = getZoom(ThisNiche.hg.getHandle(c));
        // if(at != null)
        // {
        // at.clearTranslation();
        // c.setAttribute(VisualAttribs.zoom, at);
        // }
    }

    @SuppressWarnings("unchecked")
    public static NBStyle getStyle(CellGroupMember c, StyleType type)
    {
        Map<StyleType, NBStyle> s = (Map<StyleType, NBStyle>) c
                .getAttribute(XMLConstants.CELL_STYLE);
        return s != null ? s.get(type) : styles.get(type);
    }

    @SuppressWarnings("unchecked")
    public static void addStyle(CellGroupMember c, NBStyle style)
    {
        Map<StyleType, NBStyle> s = (Map<StyleType, NBStyle>) c
                .getAttribute(XMLConstants.CELL_STYLE);
        if (s == null)
        {
            s = new HashMap<StyleType, NBStyle>(styles);
            c.setAttribute(XMLConstants.CELL_STYLE, s);
        }
        s.put(style.getStyleType(), style);
    }

    public static HGHandle getOutCellHandle(CellGroupMember cell)
    {
        if (cell instanceof CellGroup) return null;
        return getOutCellHandle(ThisNiche.handleOf(cell));
    }

    public static List<HGHandle> getOutCellHandles(HGHandle cellH)
    {
        List<HGHandle> list = new ArrayList<HGHandle>();
        if (cellH == null) return null;
        try
        {
            List<EventPubSub> subscriptions = hg.getAll(ThisNiche.graph, hg
                    .and(hg.type(EventPubSub.class), hg
                            .incident(EvalCellEvent.HANDLE),
                            hg.incident(cellH), hg.orderedLink(new HGHandle[] {
                                    EvalCellEvent.HANDLE, cellH,
                                    ThisNiche.graph.getHandleFactory().anyHandle(),
                                    ThisNiche.graph.getHandleFactory().anyHandle() })));
            for (EventPubSub s : subscriptions)
            {
                Object handler = ThisNiche.graph.get(s.getEventHandler());
                if (s.getEventHandler().equals(s.getSubscriber())
                        && handler instanceof Cell
                        //&& !CellUtils.isBackuped(s.getEventHandler())
                        )
                    list.add(s.getEventHandler());
            }

        }
        catch (Exception ex)
        {
            System.out.println("ERROR - getOutCellHandle"
                    + ThisNiche.graph.getPersistentHandle(cellH));
            ex.printStackTrace();
        }
        return list;
    }

    public static List<Cell> getOutCells(HGHandle cellH)
    {
        List<Cell> list = new ArrayList<Cell>();
        if (cellH == null) return null;
        List<EventPubSub> subscriptions = hg.getAll(ThisNiche.graph, hg.and(hg
                .type(EventPubSub.class), hg.incident(EvalCellEvent.HANDLE), hg
                .incident(cellH), hg.orderedLink(new HGHandle[] {
                EvalCellEvent.HANDLE, cellH, ThisNiche.graph.getHandleFactory().anyHandle(),
                ThisNiche.graph.getHandleFactory().anyHandle() })));
        for (EventPubSub s : subscriptions)
        {
            Object handler = ThisNiche.graph.get(s.getEventHandler());
            if (s.getEventHandler().equals(s.getSubscriber())
                    && handler instanceof Cell
                    //&& !CellUtils.isBackuped(s.getEventHandler())
                    )
                 list.add((Cell) handler);
        }
        return list;
    }

    public static HGHandle getOutCellHandle(HGHandle cellH)
    {
        List<HGHandle> l = getOutCellHandles(cellH);
        return (l != null && !l.isEmpty()) ? l.get(0) : null;
    }

    public static Cell getOutCell(CellGroupMember cell)
    {
        HGHandle h = getOutCellHandle(cell);
        return (h != null) ? (Cell) ThisNiche.graph.get(h) : null;
    }

    public static HGHandle createOutputCellH(HGHandle par, EvalResult res)
    {
        return createOutputCellH(par, res.getText(), res.getComponent(), res
                .isError());
    }

    public static HGHandle createOutputCellH(HGHandle par, String text,
            Component comp, boolean error)
    {
        HGHandle h = (comp == null) ? ThisNiche.handleOf(text) : ThisNiche
                .handleOf(comp);
        if (h == null) h = addSerializable(comp == null ? text : comp);
        HGHandle res = CellUtils.getOrCreateCellHForRefH(h);
        if (error) setError(res, error);
        if (par != null) addEventPubSub(EvalCellEvent.HANDLE, par, res, res);
        return res;
    }

    public static HGHandle createCellHandle(NotebookDocument doc, String text)
    {
        Scriptlet s = new Scriptlet(doc.getDefaultEngineName(), text);
        HGHandle h = ThisNiche.handleOf(s);
        if (h == null) h = ThisNiche.graph.add(s);
        return CellUtils.getOrCreateCellHForRefH(h);
    }

    static int count = 0;

    public static HGHandle createGroupHandle()
    {
        CellGroup out = new CellGroup("CG" + count++);
        return ThisNiche.graph.add(out); // , HGSystemFlags.MUTABLE);
    }

    public static HGHandle createGroupHandle(String name)
    {
        CellGroup out = new CellGroup(name);
        return ThisNiche.graph.add(out); // , HGSystemFlags.MUTABLE);
    }

    public static void removeOutputCellSubscription(HGHandle cell_handle)
    {
        List<HGHandle> set = hg.findAll(ThisNiche.graph, hg.and(hg
                .type(EventPubSub.class), hg.incident(EvalCellEvent.HANDLE), hg
                .incident(cell_handle), hg.orderedLink(new HGHandle[] {
                EvalCellEvent.HANDLE, ThisNiche.graph.getHandleFactory().anyHandle(), cell_handle,
                ThisNiche.graph.getHandleFactory().anyHandle() })));
        for (HGHandle s : set)
            if (s != null) ThisNiche.graph.remove(s, true);

    }

    public static HGHandle makeCopy(HGHandle in_h)
    {
        CellGroupMember in = (CellGroupMember) ThisNiche.graph.get(in_h);
        if (in instanceof CellGroup) return cellGroupCopy(in_h);
        else if (isInputCell(in))
        {
            HGHandle resH = inputCellCopy(in_h);
           for(HGHandle outH : getOutCellHandles(in_h))
                   addEventPubSub(EvalCellEvent.HANDLE, resH, outH, outH);     
            return resH;
        }
        else
        {
            HGHandle resH = outputCellCopy(in_h);
            HGHandle par = getOutCellInput(in_h);
            addEventPubSub(EvalCellEvent.HANDLE, par, resH, resH);
            return resH;
        }
    }

    // full copy
    static HGHandle cellGroupCopy(HGHandle in_h)
    {
        CellGroup in = (CellGroup) ThisNiche.graph.get(in_h);
        Map<HGHandle, HGHandle> cells = new HashMap<HGHandle, HGHandle>();
        copy_input_cells(in, cells);
        return connect_output_cells(in, cells);
    }

    private static void copy_input_cells(CellGroup in,
            Map<HGHandle, HGHandle> cells)
    {
        for (int i = 0; i < in.getArity(); i++)
        {
            CellGroupMember m = in.getElement(i);
            if (isInputCell(m)) cells.put(in.getTargetAt(i), inputCellCopy(in
                    .getTargetAt(i)));
            else if (m instanceof CellGroup)
                copy_input_cells((CellGroup) m, cells);
        }
    }

    private static HGHandle connect_output_cells(CellGroup in,
            Map<HGHandle, HGHandle> cells)
    {
        CellGroup outG = new CellGroup();
        outG.attributes = in.attributes;
        HGHandle out = ThisNiche.graph.add(outG);
        for (int i = 0; i < in.getArity(); i++)
        {
            CellGroupMember m = in.getElement(i);
            HGHandle copyH;
            if (isInputCell(m)) copyH = cells.get(in.getTargetAt(i));
            else if (m instanceof CellGroup) copyH = connect_output_cells(
                    (CellGroup) m, cells);
            else
            {
                copyH = outputCellCopy(in.getTargetAt(i));
                HGHandle par = getOutCellInput(in.getTargetAt(i));
                if (par != null && cells.containsKey(par))
                    addEventPubSub(EvalCellEvent.HANDLE, cells.get(par), copyH,
                            copyH);
            }
            outG.insert(i, copyH);
        }
        return out;
    }

    private static HGHandle inputCellCopy(HGHandle h)
    {
        CellGroupMember in = (CellGroupMember) ThisNiche.graph.get(h);
        Scriptlet s = (Scriptlet) ((Cell) in).getValue();
        Scriptlet out_s = new Scriptlet(s.getLanguage(), s.getCode());
        HGAtomRef ref = new HGAtomRef(ThisNiche.graph.add(out_s),
                HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        out.attributes = in.getAttributes();
        HGHandle outH = ThisNiche.graph.add(out);
        // System.out.println("inputCellCopy: " + out.getValue().getClass());
        return outH;
    }

    private static HGHandle outputCellCopy(HGHandle in)
    {
        Cell c = (Cell) ThisNiche.graph.get(in);
        Object value = c.getValue();
        // TODO: not very clear when to clone
        if(value instanceof Component)
           value = DocUtil.maybe_clone((Component) value);
        HGHandle h = addSerializable(value);
       // HGHandle res = getOrCreateCellHForRefH(h);
       // return res;
        Cell out = new Cell(new HGAtomRef(h, HGAtomRef.Mode.symbolic));
        out.attributes = c.getAttributes();
        HGHandle outH = ThisNiche.graph.add(out);
        return outH;
    }

    public static HGHandle getOutCellInput(HGHandle h)
    {
        List<EventPubSub> subs = hg.getAll(ThisNiche.graph, hg.and(hg
                .type(EventPubSub.class), hg.incident(h), hg
                .orderedLink(new HGHandle[] { EvalCellEvent.HANDLE,
                        ThisNiche.graph.getHandleFactory().anyHandle(), h, h })));
        for (EventPubSub eps : subs)
        {
            Object pub = ThisNiche.graph.get(eps.getPublisher());
            if (pub instanceof Cell) return (eps.getPublisher());
        }
        return null;
    }

    public static void removeEventPubSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        List<HGHandle> subscriptions = getEventPubSubListH(eventType,
                publisher, subscriber, listener);
        for (HGHandle s : subscriptions)
            ThisNiche.graph.remove(s, true);
    }

    public static void addEventPubSub(HGHandle eventType, HGHandle pub,
            HGHandle sub, HGHandle handler)
    {
        if (!containsEventPubSub(eventType, pub, sub, handler))
        {
            EventPubSub e = new EventPubSub(eventType, pub, sub, handler);
            // System.out.println("Adding " + e);
            ThisNiche.graph.add(e);
        }
    }

    public static void addMutualEventPubSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle handler)
    {
        addEventPubSub(eventType, publisher, subscriber, handler);
        addEventPubSub(eventType, subscriber, publisher, handler);
    }

    public static void removeMutualEventPubSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle handler)
    {
        removeEventPubSub(eventType, publisher, subscriber, handler);
        removeEventPubSub(eventType, subscriber, publisher, handler);
    }

    private static boolean containsEventPubSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        List<EventPubSub> subscriptions = getEventPubSubList(eventType,
                publisher, subscriber, listener);
        return !subscriptions.isEmpty();
    }

    public static List<EventPubSub> getEventPubSubList(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        return hg.getAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class), hg
                .incident(eventType), hg.incident(publisher), hg
                .orderedLink(new HGHandle[] { eventType, publisher, subscriber,
                        listener })));
    }

    public static List<HGHandle> getEventPubSubListH(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        return hg.findAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class),
                hg.incident(eventType), hg.incident(publisher), hg
                        .orderedLink(new HGHandle[] { eventType, publisher,
                                subscriber, listener })));
    }

    public static void addCopyListeners(HGHandle masterH, HGHandle copyH)
    {
        Object master = ThisNiche.graph.get(masterH);
        Object copy = ThisNiche.graph.get(copyH);
        if (master instanceof CellGroup && copy instanceof CellGroup)
        {
            addCellGroupCopyListeners(masterH, copyH);
        }
        else if (master instanceof Cell && copy instanceof Cell) addCellCopyListeners(
                masterH, copyH);
        else
            throw new RuntimeException("Bad Arguments:" + master + "OR" + copy);
    }

    private static void addCellGroupCopyListeners(HGHandle masterH,
            HGHandle copyH)
    {
        CellGroup master = (CellGroup) ThisNiche.graph.get(masterH);
        CellGroup copy = (CellGroup) ThisNiche.graph.get(copyH);
        addMutualEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getHandle());
        addMutualEventPubSub(CellGroupChangeEvent.HANDLE, masterH, copyH,
                CopyCellGroupChangeHandler.getHandle());

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup) addCellGroupCopyListeners(
                    master.getTargetAt(i), copy.getTargetAt(i));
            else
                addCellCopyListeners(master.getTargetAt(i), copy.getTargetAt(i));
    }

    private static void addCellCopyListeners(HGHandle masterH, HGHandle copyH)
    {
        if (isInputCell((CellGroupMember) ThisNiche.graph.get(masterH)))
        {
            addMutualEventPubSub(CellTextChangeEvent.HANDLE, masterH, copyH,
                    CopyCellTextChangeHandler.getHandle());
            addMutualEventPubSub(EvalCellEvent.HANDLE, masterH, copyH,
                    CopyEvalCellHandler.getHandle());
        }
        addMutualEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getHandle());
    }

    public static void removeHandlers(HGHandle masterH)
    {
        if (masterH == null) return;
        Object master = ThisNiche.graph.get(masterH);
        if (master instanceof CellGroup) removeCellGroupHandlers(masterH);
        else
            remove_event_handlers(masterH);
    }

    private static void removeCellGroupHandlers(HGHandle masterH)
    {
        CellGroup master = (CellGroup) ThisNiche.graph.get(masterH);
        remove_event_handlers(masterH);

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup) removeCellGroupHandlers(master
                    .getTargetAt(i));
            else
                remove_event_handlers(master.getTargetAt(i));
    }

    private static void remove_event_handlers(HGHandle masterH)
    {
        HGHandle any = ThisNiche.graph.getHandleFactory().anyHandle();
        List<HGHandle> subs =
            hg.findAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class),
                    hg.incident(masterH), hg.orderedLink(new HGHandle[] {
                            any, masterH, any, any })));
        for (HGHandle s : subs)
            ThisNiche.graph.remove(s, true);
        subs = 
            hg.findAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class),
                    hg.incident(masterH), hg.orderedLink(new HGHandle[] {
                            any, any, masterH, any })));
        for (HGHandle s : subs)
            ThisNiche.graph.remove(s, true);
    }

    public static void backupCell(HGHandle cellH)
    {
        CellGroupMember cgm = ThisNiche.graph.get(cellH);
        if (cgm == null) return;
        if (cgm instanceof CellGroup)
        {
           ThisNiche.graph.add(createBackupLink(cellH));
           for (int i = 0; i < ((CellGroup) cgm)
                .getArity(); i++)
               backupCell(((CellGroup) cgm).getTargetAt(i));
        }
        else
            ThisNiche.graph.add(createBackupLink(cellH));
        CellUtils.removeHandlers(cellH);
    }

    public static boolean isBackuped(HGHandle cell)
    {
        return getBackupLink(cell) != null;

    }

    private static BackupLink getBackupLink(HGHandle cell)
    {
        HGHandle h = hg.findOne(ThisNiche.graph, hg.and(hg
                .type(BackupLink.class), hg.incident(cell)));
        return h != null ? (BackupLink) ThisNiche.graph.get(h) : null;
    }
    
    public static void restoreCell(HGHandle cellH)
    {
        restoreCell(cellH, true);
    }

    public static void restoreCell(HGHandle cellH, boolean fully)
    {
        BackupLink link = getBackupLink(cellH);
        if (link == null) return;
        
        CellGroupMember cgm = ThisNiche.graph.get(link.getCell());
        if (cgm instanceof CellGroup)
        {
           restoreFromLink(link, fully); 
           for (int i = 0; i < ((CellGroup) cgm).getArity(); i++)
            restoreCell(((CellGroup) cgm).getTargetAt(i), fully);
        }
        else
        {
            restoreFromLink(link, fully);
        }
        removeBackupLink(link, false);
    }
    
    private static void restoreFromLink(BackupLink link, boolean fully)
    {
        List<EventPubSubInfo> pubs = ThisNiche.graph.get(link.getPubs());
        for (EventPubSubInfo inf : pubs)
        {
            if(!fully && filter_all_but_eval_eps(inf))  continue;
            addEventPubSub(inf.getEventType(), link
                    .getCell(), inf.getPubOrSub(), inf.getEventHandler());
        }
        List<EventPubSubInfo> subs = ThisNiche.graph.get(link.getSubs());
        for (EventPubSubInfo inf : subs)
        {
            if(!fully && filter_all_but_eval_eps(inf))  continue;
               addEventPubSub(inf.getEventType(), inf
                    .getPubOrSub(), link.getCell(), inf.getEventHandler());
        }
    }
    
    private static boolean filter_all_but_eval_eps(EventPubSubInfo inf)
    {
        if(!EvalCellEvent.HANDLE.equals(inf.getEventType())) return true;
        if (!(ThisNiche.graph.get(inf.getPubOrSub()) instanceof CellGroupMember)) return true;
        if (!(ThisNiche.graph.get(inf.getEventHandler()) instanceof CellGroupMember)) return true;
        return false;
    }

    //remove notebook docs and cells scheduled for deletion during the session
    public static void removeBackupedStuff()
    {
       List<NotebookDocument> docs = hg.getAll(ThisNiche.graph, hg.type(NotebookDocument.class));
       for(NotebookDocument doc: docs)
       {
           if(doc.getBook() == null)
           {
              ThisNiche.graph.remove(doc.getBookHandle()); 
              ThisNiche.graph.remove(doc.getHandle());
           }else if (isBackuped(doc.getBookHandle()))
           {
               removeBackupLink(getBackupLink(doc.getBookHandle()), true);
               ThisNiche.graph.remove(doc.getHandle());
           }
       }
       
       List<BackupLink> res = hg.getAll(ThisNiche.graph, hg
               .type(BackupLink.class));
       for (BackupLink link : res)
           removeBackupLink(link, true);
    }

    private static void removeBackupLink(BackupLink link, boolean cell_too)
    {
        HGHandle linkH = ThisNiche.handleOf(link);
        HGHandle cellH = link.getCell();
        HGHandle pubs = link.getPubs();
        HGHandle subs = link.getSubs();
        ThisNiche.graph.remove(linkH, true);
        if (cell_too) ThisNiche.graph.remove(cellH, true);
        ThisNiche.graph.remove(pubs, true);
        ThisNiche.graph.remove(subs, true);
    }

    private static BackupLink createBackupLink(HGHandle cell)
    {
        HGHandle any = ThisNiche.graph.getHandleFactory().anyHandle();
        List<EventPubSub> pubs = hg.getAll(ThisNiche.graph, hg
                .and(hg.type(EventPubSub.class), hg.incident(cell), hg
                        .orderedLink(new HGHandle[] {
                                any, cell, any, any})));
        List<EventPubSubInfo> pubs_out = new ArrayList<EventPubSubInfo>(pubs
                .size());
        for (EventPubSub eps : pubs)
            pubs_out.add(new EventPubSubInfo(eps.getEventType(), eps
                    .getSubscriber(), eps.getEventHandler()));
        List<EventPubSub> subs = hg.getAll(ThisNiche.graph, hg.and(hg
                .type(EventPubSub.class), hg.incident(cell), hg
                .orderedLink(new HGHandle[] { any, any, cell, any })));
        List<EventPubSubInfo> subs_out = new ArrayList<EventPubSubInfo>(pubs
                .size());
        for (EventPubSub eps : subs)
            subs_out.add(new EventPubSubInfo(eps.getEventType(), eps
                    .getPublisher(), eps.getEventHandler()));
        return new BackupLink(cell, pubs_out, subs_out);
    }

//    static List<HGHandle> getListForPubOrSub(HGHandle eventType,
//            HGHandle publisher, HGHandle subscriber, HGHandle listener)
//    {
//        HGHandle pub_or_sub = //hg.anyHandle().equals(publisher) ||
//        ThisNiche.graph.getHandleFactory().equals(publisher)
//              ? subscriber : publisher;
//        return hg.findAll(ThisNiche.graph, hg.and(hg.type(EventPubSub.class),
//                hg.incident(pub_or_sub), hg.orderedLink(new HGHandle[] {
//                        eventType, publisher, subscriber, listener })));
//    }

    public static HGHandle addSerializable(Object o)
    {
        HGHandle h = null;
        HGTypeSystem ts = ThisNiche.graph.getTypeSystem();
        ClassLoader save = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(
                    ThisNiche.getTopContext().getClassLoader());
            if (o instanceof Component && o instanceof Serializable
                    && !(o instanceof NotebookUI))
            {
                PSwingNode node = null;
                // remove added by Piccolo PSwingNode, which breaks
                // serialization
                if (o instanceof JComponent)
                    node = GUIHelper.getPSwingNode((JComponent) o);
                if (node != null) node.prepareForSerialization();
                try
                {
                    h = ThisNiche.graph.add(o);
                }
                catch (Throwable ex)
                {
                    ex.printStackTrace();
                    HGHandle t = ts.getTypeHandle(Serializable.class);
                    try{
                    h = ThisNiche.graph.add(o, t);
                    }catch(Throwable tt)
                    {
                        h = ThisNiche.graph.add(ex.toString() + 
                                ":" + tt.toString());
                    }
                }
                finally
                {
                    // restore PSwingNode
                    if (node != null) node.restoreAfterSerialization();
                }
            }
            else
                h = ThisNiche.graph.add(o);
        }
        catch (Throwable ex)
        {
            System.err.println("Unable to add Cell value: " + o + " Reason: "
                    + ex);
            ex.printStackTrace();
            h = ThisNiche.graph.add(ex.toString());
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(save);
        }
        return h;
    }

    public static void processCelTextChangeEvent(HGHandle cH,
            CellTextChangeEvent e)
    {
        Cell c = ThisNiche.graph.get(cH);
        Scriptlet s = (Scriptlet) c.getValue();
        String code = s.getCode();
        StringBuffer res = new StringBuffer(code.substring(0, e.getOffset()));
        if (e.getType() == CellTextChangeEvent.EventType.INSERT)
        {
            res.append(e.getText());
            res.append(code.substring(e.getOffset()));
        }
        else
        {
            res.append(code.substring(e.getOffset() + e.getLength()));
        }
        s.setCode(res.toString());
        ThisNiche.graph.update(s);
        fireCellTextChanged(cH, e);
    }

    public static void fireCellTextChanged(HGHandle cellH, CellTextChangeEvent e)
    {
        EventDispatcher.dispatch(CellTextChangeEvent.HANDLE, cellH, e);
    }

    // TODO: looks like a bug in HG.... sometimes it returns a list with
    // duplicates
    public static <T> Set<T> findAll(HyperGraph graph,
            HGQueryCondition condition)
    {
        List<T> HL = hg.findAll(graph, condition);
        Set<T> S = new HashSet<T>();
        S.addAll(HL);
        assert (HL.size() == S.size()) : new RuntimeException(
                "Duplicate results while looking for: " + condition);
        return S;
    }

    static void setLayoutHandler(HGHandle cellH, LayoutHandler lh)
    {
        CellGroupMember cell = ThisNiche.graph.get(cellH);
        cell.setAttribute(VisualAttribs.layoutHandler, lh);
    }

    public static LayoutHandler getLayoutHandler(HGHandle cellH)
    {
        CellGroupMember m = ThisNiche.graph.get(cellH);
        return (LayoutHandler) m.getAttribute(VisualAttribs.layoutHandler);
    }

    public static void setZoom(HGHandle cellH, PAffineTransform tr)
    {
        CellGroupMember cell = ThisNiche.graph.get(cellH);
        // System.out.println("setZoom: " + tr + ":" + cellH);
        cell.setAttribute(VisualAttribs.zoom, new AffineTransformEx(tr));
    }

    public static AffineTransformEx getZoom(HGHandle cellH)
    {
        CellGroupMember m = ThisNiche.graph.get(cellH);
        Object o = m.getAttribute(VisualAttribs.zoom);
        // legacy cleanup
        if (o instanceof AffineTransformEx) return (AffineTransformEx) o;
        else if (o instanceof PAffineTransform)
        {
            AffineTransformEx tr = new AffineTransformEx((PAffineTransform) o);
            m.setAttribute(VisualAttribs.zoom, tr);
            return tr;
        }
        return null;
    }
}
