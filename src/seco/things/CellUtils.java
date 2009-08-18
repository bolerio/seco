package seco.things;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTabbedPane;

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
import seco.gui.JComponentVisual;
import seco.gui.NBUIVisual;
import seco.gui.TabbedPaneVisual;
import seco.gui.VisualAttribs;
import seco.gui.VisualsManager;
import seco.notebook.DocUtil;
import seco.notebook.NBStyle;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.StyleAttribs;
import seco.notebook.StyleType;
import seco.notebook.XMLConstants;

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

    static void updateCellGroupMember(CellGroupMember c)
    {
        if (c instanceof Cell)
        {
            // Cell cell = (Cell) c;
            // Object val = cell.getValue();
            // cell.ref = new HGAtomRef(ThisNiche.hg.add(val),
            // HGAtomRef.Mode.hard);
        }
        ThisNiche.hg.update(c);
    }

    public static HGHandle getCellHForRefH(HGHandle h)
    {
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        HGHandle outH = ThisNiche.handleOf(out);
        if (outH == null) outH = ThisNiche.hg.add(out);
        return outH;
    }

    //TODO: fix this uglyness
    public static CellVisual getVisual(CellGroupMember c)
    {
        HGHandle visH = (c.getVisual() != null) ? c.getVisual() :
                VisualsManager.defaultVisualForAtom(ThisNiche.handleOf(c));
        if(visH != null)// && TabbedPaneVisual.getHandle() != visH)
            return (CellVisual) ThisNiche.hg.get(visH);
        CellVisual visual = null;
        if(visH == null || visH.equals(HGHandleFactory
                 .nullHandle()))
        {
             if(c instanceof CellGroup || CellUtils.isInputCell(c))
                 visual = new NBUIVisual();
             else
             {
                 Object val = ((Cell) c).getValue();
                 if(val instanceof JTabbedPane)
                     visual = new TabbedPaneVisual();
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
        for (HGHandle h : ThisNiche.hg.getIncidenceSet(cgmH))
        {
            Object o = ThisNiche.hg.get(h);
            if (o instanceof CellGroup) return (CellGroup) o;
        }
        return null;
    }

    public static HGHandle makeCellH(String text, String lang)
    {
        Scriptlet s = new Scriptlet(lang, text);
        HGHandle h = ThisNiche.hg.add(s);
        return CellUtils.getCellHForRefH(h);
    }

    public static boolean isHTML(Cell c)
    {
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_HTML);
        return b != null && b.booleanValue();
    }

    public static void setHTML(Cell c, boolean html)
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
        if(c == null) return false;
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
        cgm.setAttribute(VisualAttribs.minimized, !b);
    }
    
    public static boolean isMaximized(CellGroupMember cgm)
    {
        Boolean b = (Boolean) cgm.getAttribute(VisualAttribs.maximized);
        return b != null && b.booleanValue();
    }
    
    public static void toggleMaximized(CellGroupMember cgm)
    {
        boolean b = isMaximized(cgm);
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
        toggleShowTitle((CellGroupMember) ThisNiche.hg.get(h)); 
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
        CellGroupMember c = (CellGroupMember) ThisNiche.hg.get(h);
        c.setAttribute(XMLConstants.ATTR_ERROR, b);
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
        Object o = ThisNiche.hg.get(c.ref.getReferent());
        // System.out.println("CellUtils - setText: " + text);
        if (o instanceof Scriptlet)
        {
            ((Scriptlet) o).setCode(text);
            ThisNiche.hg.update(o);
            return;
        }
        HGHandle h = ThisNiche.handleOf(text);
        if (h == null) h = ThisNiche.hg.add(text);
        c.ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        // ThisNiche.hg.update(c);
    }

    public static void toggleAttribute(CellGroupMember c, Object attr_name)
    {
        Object attr = c.getAttribute(attr_name);
        boolean b = attr != null ? ((Boolean) attr).booleanValue() : false;
        c.setAttribute(attr_name, !b);
    }

    public static String getText(Cell c)
    {
        Object v = c.getValue();
        if (v instanceof Scriptlet) return ((Scriptlet) v).getCode();
        return (v != null) ? v.toString() : "null";
    }

    public static String getEngine(CellGroupMember c)
    {
        String eng = (String) c.getAttribute(XMLConstants.ATTR_ENGINE);
        return eng; //eng != null ? eng : defaultEngineName;
    }

    public static void setEngine(CellGroupMember c, String s)
    {
        c.setAttribute(XMLConstants.ATTR_ENGINE, s);
    }

    public static NBStyle getStyle(CellGroupMember c, StyleType type)
    {
        Map<StyleType, NBStyle> s = (Map<StyleType, NBStyle>) c
                .getAttribute(XMLConstants.CELL_STYLE);
        return s != null ? s.get(type) : styles.get(type);
    }

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
            List<EventPubSub> subscriptions = hg.getAll(ThisNiche.hg, hg.and(hg
                    .type(EventPubSub.class),
                    hg.incident(EvalCellEvent.HANDLE), hg.incident(cellH), hg
                            .orderedLink(new HGHandle[] { EvalCellEvent.HANDLE,
                                    cellH, HGHandleFactory.anyHandle,
                                    HGHandleFactory.anyHandle })));
            for (EventPubSub s : subscriptions)
            {
                Object handler = ThisNiche.hg.get(s.getEventHandler());
                if (s.getEventHandler().equals(s.getSubscriber())
                        && handler instanceof Cell)
                    list.add(s.getEventHandler());
            }

        }
        catch (Exception ex)
        {
            System.out.println("ERROR - getOutCellHandle"
                    + ThisNiche.hg.getPersistentHandle(cellH));
            ex.printStackTrace();
        }
        return list;
    }

    public static List<Cell> getOutCells(HGHandle cellH)
    {
        List<Cell> list = new ArrayList<Cell>();
        if (cellH == null) return null;
        List<EventPubSub> subscriptions = hg.getAll(ThisNiche.hg, hg.and(hg
                .type(EventPubSub.class), hg.incident(EvalCellEvent.HANDLE), hg
                .incident(cellH), hg.orderedLink(new HGHandle[] {
                EvalCellEvent.HANDLE, cellH, HGHandleFactory.anyHandle,
                HGHandleFactory.anyHandle })));
        for (EventPubSub s : subscriptions)
        {
            Object handler = ThisNiche.hg.get(s.getEventHandler());
            if (s.getEventHandler().equals(s.getSubscriber())
                    && handler instanceof Cell) list.add((Cell) handler);
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
        return (h != null) ? (Cell) ThisNiche.hg.get(h) : null;
    }

    public static void updateCellValue(Cell cell, Object val)
    {
        HGHandle  h = CellUtils.addSerializable(val);
        cell.ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        ThisNiche.hg.update(cell);
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
        HGHandle res = CellUtils.getCellHForRefH(h);
        if (error) setError(res, error);
        if (par != null) addEventPubSub(EvalCellEvent.HANDLE, par, res, res);
        return res;
    }

    public static HGHandle createCellHandle(NotebookDocument doc, String text)
    {
        Scriptlet s = new Scriptlet(doc.getDefaultEngineName(), text);
        HGHandle h = ThisNiche.handleOf(s);
        if (h == null) h = ThisNiche.hg.add(s);
        return CellUtils.getCellHForRefH(h);
    }

    static int count = 0;

    public static HGHandle createGroupHandle()
    {
        CellGroup out = new CellGroup("CG" + count++);
        return ThisNiche.hg.add(out); // , HGSystemFlags.MUTABLE);
    }

    public static HGHandle createGroupHandle(String name)
    {
        CellGroup out = new CellGroup(name);
        return ThisNiche.hg.add(out); // , HGSystemFlags.MUTABLE);
    }

    public static void removeOutputCellSubscription(HGHandle cell_handle)
    {
       List<HGHandle> set = hg.findAll(ThisNiche.hg, hg.and(hg
                .type(EventPubSub.class), hg.incident(EvalCellEvent.HANDLE), hg
                .incident(cell_handle), hg.orderedLink(new HGHandle[] {
                EvalCellEvent.HANDLE, HGHandleFactory.anyHandle, cell_handle,
                HGHandleFactory.anyHandle })));
        for (HGHandle s : set)
            if (s != null) ThisNiche.hg.remove(s, true);

    }

    public static HGHandle makeCopy(HGHandle in_h)
    {
        CellGroupMember in = (CellGroupMember) ThisNiche.hg.get(in_h);
        if (in instanceof CellGroup) return cellGroupCopy(in_h);
        else if (isInputCell(in)) return inputCellCopy(in_h);
        else
            return outputCellCopy(in_h);
    }

    // full copy
    static HGHandle cellGroupCopy(HGHandle in_h)
    {
        CellGroup in = (CellGroup) ThisNiche.hg.get(in_h);
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
        HGHandle out = ThisNiche.hg.add(outG);
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
        CellGroupMember in = (CellGroupMember) ThisNiche.hg.get(h);
        Scriptlet s = (Scriptlet) ((Cell) in).getValue();
        Scriptlet out_s = new Scriptlet(s.getLanguage(), s.getCode());
        HGAtomRef ref = new HGAtomRef(ThisNiche.hg.add(out_s),
                HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        out.attributes = in.getAttributes();
        HGHandle outH = ThisNiche.hg.add(out);
        //System.out.println("inputCellCopy: " + out.getValue().getClass());
        return outH;
    }

    private static HGHandle outputCellCopy(HGHandle in)
    {
        Cell c = (Cell) ThisNiche.hg.get(in);
        boolean error = isError(c);
        Object value = c.getValue();
        //TODO: not very clear when to clone 
       // if(value instanceof Component)
       //     value = DocUtil.maybe_clone((Component) value);
        HGHandle h = addSerializable(value);
        HGHandle res = CellUtils.getCellHForRefH(h);
        if (error) setError(h, error);
        return res;
    }

    public static HGHandle getOutCellInput(HGHandle h)
    {
       List<EventPubSub> subs = hg.getAll(ThisNiche.hg, hg.and(hg
                .type(EventPubSub.class), hg.incident(h), hg
                .orderedLink(new HGHandle[] { EvalCellEvent.HANDLE,
                        HGHandleFactory.anyHandle, h, h })));
        for (EventPubSub eps : subs)
        {
            Object pub = ThisNiche.hg.get(eps.getPublisher());
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
            ThisNiche.hg.remove(s, true);
    }

    public static void addEventPubSub(HGHandle eventType, 
                                      HGHandle pub,
                                      HGHandle sub, 
                                      HGHandle handler)
    {
        if (!containsEventPubSub(eventType, pub, sub, handler))
        {
            EventPubSub e = new EventPubSub(eventType, pub, sub, handler);
            // System.out.println("Adding " + e);
            ThisNiche.hg.add(e);
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
        return hg.getAll(ThisNiche.hg, hg.and(hg.type(EventPubSub.class), hg
                .incident(eventType), hg.incident(publisher), hg
                .orderedLink(new HGHandle[] { eventType, publisher, subscriber,
                        listener })));
    }

    public static List<HGHandle> getEventPubSubListH(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        return hg.findAll(ThisNiche.hg, hg.and(hg.type(EventPubSub.class), hg
                .incident(eventType), hg.incident(publisher), hg
                .orderedLink(new HGHandle[] { eventType, publisher, subscriber,
                        listener })));
    }

    public static void addCopyListeners(HGHandle masterH, HGHandle copyH)
    {
        Object master = ThisNiche.hg.get(masterH);
        Object copy = ThisNiche.hg.get(copyH);
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
        CellGroup master = (CellGroup) ThisNiche.hg.get(masterH);
        CellGroup copy = (CellGroup) ThisNiche.hg.get(copyH);
        addMutualEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getInstance());
        addMutualEventPubSub(CellGroupChangeEvent.HANDLE, masterH, copyH,
                CopyCellGroupChangeHandler.getInstance());

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup) addCellGroupCopyListeners(
                    master.getTargetAt(i), copy.getTargetAt(i));
            else
                addCellCopyListeners(master.getTargetAt(i), copy.getTargetAt(i));
    }

    private static void addCellCopyListeners(HGHandle masterH, HGHandle copyH)
    {
        if (isInputCell((CellGroupMember) ThisNiche.hg.get(masterH)))
        {
            addMutualEventPubSub(CellTextChangeEvent.HANDLE, masterH, copyH,
                    CopyCellTextChangeHandler.getInstance());
            addMutualEventPubSub(EvalCellEvent.HANDLE, masterH, copyH,
                    CopyEvalCellHandler.getInstance());
        }
        addMutualEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getInstance());
    }

     public static void removeHandlers(HGHandle masterH)
    {
        if (masterH == null) return;
        Object master = ThisNiche.hg.get(masterH);
        if (master instanceof CellGroup) 
            removeCellGroupHandlers(masterH);
        else
            remove_event_handlers(masterH);
    }

    private static void removeCellGroupHandlers(HGHandle masterH)
    {
        CellGroup master = (CellGroup) ThisNiche.hg.get(masterH);
        remove_event_handlers(masterH);

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup) 
                removeCellGroupHandlers(master.getTargetAt(i));
            else
                remove_event_handlers(master.getTargetAt(i));
    }

    private static void remove_event_handlers(HGHandle masterH)
    {
        List<HGHandle> subs = getListForPubOrSub(HGHandleFactory.anyHandle,
                masterH, HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
        for (HGHandle s : subs)
            ThisNiche.hg.remove(s, true);
        subs = getListForPubOrSub(HGHandleFactory.anyHandle, HGHandleFactory.anyHandle, masterH,
                HGHandleFactory.anyHandle);
        for (HGHandle s : subs)
            ThisNiche.hg.remove(s, true);
    }
    
    public static void backupCell(HGHandle cell)
    {
        CellGroupMember cgm = ThisNiche.hg.get(cell);
        if(cgm == null) return;
        if(cgm instanceof CellGroup)
            for(int i = 0; i < ((CellGroup) cgm).getArity() ;i++)
                ThisNiche.hg.add(
                        createBackupLink(((CellGroup) cgm).getTargetAt(i)));
        else
            ThisNiche.hg.add(createBackupLink(cell));
        CellUtils.removeHandlers(cell);
    }
    
    public static boolean isBackuped(HGHandle cell)
    {
        return getBackupLink(cell) != null;

    }
    
    private static BackupLink getBackupLink(HGHandle cell)
    {
       HGHandle h = hg.findOne(ThisNiche.hg, hg.and(hg
                .type(BackupLink.class), hg.incident(cell)));
       return h != null ? (BackupLink) ThisNiche.hg.get(h) : null;
    }
    
    public static void restoreCell(HGHandle cell)
    {
        BackupLink link = getBackupLink(cell);
        if(link == null) return;
        
        CellGroupMember cgm = ThisNiche.hg.get(link.getCell());
        if(cgm instanceof CellGroup)
            for(int i = 0; i < ((CellGroup) cgm).getArity() ;i++)
               restoreCell(((CellGroup) cgm).getTargetAt(i));
        else
        {
            List<EventPubSubInfo> pubs = ThisNiche.hg.get(link.getPubs());
            for(EventPubSubInfo inf: pubs)
                ThisNiche.hg.add(new EventPubSub(inf.getEventType(),
                        link.getCell(), inf.getPubOrSub(), inf.getEventHandler()));
            List<EventPubSubInfo> subs = ThisNiche.hg.get(link.getSubs());
            for(EventPubSubInfo inf: subs)
                ThisNiche.hg.add(new EventPubSub(inf.getEventType(),
                        inf.getPubOrSub(), link.getCell(), inf.getEventHandler()));
        }
        removeBackupLink(link, false);
    }
    
    public static void removeBackupedCells()
    {
       List<BackupLink> res = hg.getAll(ThisNiche.hg, hg.type(BackupLink.class));
       for(BackupLink link: res)
            removeBackupLink(link, true);
    }
    
    private static void removeBackupLink(BackupLink link, boolean cell_too)
    {
        HGHandle linkH = ThisNiche.handleOf(link);
        HGHandle cellH = link.getCell();
        HGHandle pubs = link.getPubs();
        HGHandle subs = link.getSubs();
        ThisNiche.hg.remove(linkH, true);
        //if(cell_too)
       //     ThisNiche.hg.remove(cellH, true);
       // ThisNiche.hg.remove(pubs, true);
       // ThisNiche.hg.remove(subs, true);
   }
    
    private static BackupLink createBackupLink(HGHandle cell)
    {
        List<EventPubSub> pubs = hg.getAll(ThisNiche.hg, hg.and(hg
                .type(EventPubSub.class), hg.incident(cell), hg
                .orderedLink(new HGHandle[] { 
                        HGHandleFactory.anyHandle, cell, HGHandleFactory.anyHandle, HGHandleFactory.anyHandle })));
       List<EventPubSubInfo> pubs_out = new ArrayList<EventPubSubInfo>(pubs.size()); 
       for(EventPubSub eps : pubs)
           pubs_out.add(new EventPubSubInfo(eps.getEventType(), eps.getSubscriber(), eps.getEventHandler()));
       List<EventPubSub> subs = hg.getAll(ThisNiche.hg, hg.and(hg
               .type(EventPubSub.class), hg.incident(cell), hg
               .orderedLink(new HGHandle[] { 
                       HGHandleFactory.anyHandle, HGHandleFactory.anyHandle, cell, HGHandleFactory.anyHandle })));
      List<EventPubSubInfo> subs_out = new ArrayList<EventPubSubInfo>(pubs.size()); 
      for(EventPubSub eps : subs)
          subs_out.add(new EventPubSubInfo(eps.getEventType(), eps.getPublisher(), eps.getEventHandler()));
      return new BackupLink(cell, pubs_out, subs_out);
    }
    
    static List<HGHandle> getListForPubOrSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        HGHandle pub_or_sub = hg.anyHandle().equals(publisher) ? subscriber
                : publisher;
        return hg.findAll(ThisNiche.hg, hg.and(hg.type(EventPubSub.class), hg
                .incident(pub_or_sub), hg.orderedLink(new HGHandle[] { eventType,
                publisher, subscriber, listener })));
    }

    public static HGHandle addSerializable(Object o)
    {
        HGHandle h = null;
        HGTypeSystem ts = ThisNiche.hg.getTypeSystem();
        try
        {
            if (o instanceof Component && o instanceof Serializable
                    && !(o instanceof NotebookUI))
            {
                try
                {
                    h = ThisNiche.hg.add(o);
                }
                catch (Throwable ex)
                {
                    ex.printStackTrace();
                    HGHandle t = ts.getTypeHandle(Serializable.class);
                    h = ThisNiche.hg.add(o, t);
                }
            }
            else
                h = ThisNiche.hg.add(o);
        }
        catch (Throwable ex)
        {
            System.err.println("Unable to add Cell value: " + o + " Reason: "
                    + ex);
            ex.printStackTrace();
            h = ThisNiche.hg.add(ex.toString());
        }
        return h;
    }

    public static void processCelTextChangeEvent(HGHandle cH,
            CellTextChangeEvent e)
    {
        Cell c = (Cell) ThisNiche.hg.get(cH);
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
        ThisNiche.hg.update(s);
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
}
