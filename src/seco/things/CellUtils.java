package seco.things;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGAtomRef;
import org.hypergraphdb.query.HGQueryCondition;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EvalResultEventType;
import seco.events.EventDispatcher;
import seco.events.EventPubSub;
import seco.events.handlers.CopyAttributeChangeHandler;
import seco.events.handlers.CopyCellGroupChangeHandler;
import seco.events.handlers.CopyCellTextChangeHandler;
import seco.events.handlers.CopyEvalCellHandler;
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
        {
            styles.put(t, new NBStyle(t));
        }
        styles.get(StyleType.outputCell).put(StyleAttribs.BORDER_COLOR,
                Color.white);
        styles.get(StyleType.outputCell).put(StyleAttribs.FG_COLOR,
                new java.awt.Color(0, 128, 0));
    }

    private CellUtils()
    {
    }

    public static Set<NotebookDocument> getNotebookDocs()
    {
        return CellUtils.findAll(ThisNiche.hg, hg.apply(hg.deref(ThisNiche.hg), hg
                .type(NotebookDocument.class)));
    }

    static void updateCellGroupMember(CellGroupMember c){
        if(c instanceof Cell)
        {
            Cell cell = (Cell) c;
            Object val = cell.getValue();
            cell.ref = new HGAtomRef(ThisNiche.hg.add(val),HGAtomRef.Mode.hard);
        }
        ThisNiche.hg.update(c);
    } 
    
    
    public static CellGroup getParentGroup(CellGroupMember cg)
    {
        HGHandle thisHandle = ThisNiche.hg.getHandle(cg);
        CellGroup c = (CellGroup) hg.getOne(ThisNiche.hg, hg.and(hg
                .type(CellGroup.class), hg.incident(thisHandle), hg
                .orderedLink(new HGHandle[] { thisHandle,
                        HGHandleFactory.anyHandle })));
        return c;
    }

    public static HGHandle makeCellH(String text, String lang)
    {
        Scriptlet s = new Scriptlet(lang, text);
        HGHandle h = ThisNiche.hg.add(s);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        Cell c = new Cell(ref);
        return ThisNiche.hg.add(c);
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
        Boolean b = (Boolean) c.getAttribute(XMLConstants.ATTR_COLLAPSED);
        return b != null && b.booleanValue();
    }

    public static void setCollapsed(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_COLLAPSED, b);
    }

    public static void setError(CellGroupMember c, boolean b)
    {
        c.setAttribute(XMLConstants.ATTR_ERROR, b);
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
        c.ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        //ThisNiche.hg.update(c);
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
        return eng != null ? eng : defaultEngineName;
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
        HGHandle publisher = ThisNiche.handleOf(cell);
        return getOutCellHandle(publisher);
    }

    public static HGHandle getOutCellHandle(HGHandle cellH)
    {
        if (cellH == null) return null;
        //TODO: hg.findAll throws error???
        try{
        List<EventPubSub> subscriptions = hg.findAll(ThisNiche.hg, hg.apply(hg
                .deref(ThisNiche.hg), hg.and(hg.type(EventPubSub.class), hg
                .incident(EvalResultEventType.HANDLE), hg.incident(cellH), hg
                .orderedLink(new HGHandle[] { EvalResultEventType.HANDLE,
                        cellH, HGHandleFactory.anyHandle,
                        HGHandleFactory.anyHandle }))));
        for (EventPubSub s : subscriptions)
        {
            Object handler = ThisNiche.hg.get(s.getEventHandler());
            if (handler instanceof Cell) return s.getEventHandler();
        }
        }catch(Exception ex)
        {
        	System.out.println("ERROR - getOutCellHandle" + ThisNiche.hg.getPersistentHandle(cellH));
        	ex.printStackTrace();
        }
//        Iterator it = ThisNiche.hg.getIncidenceSet(cellH).iterator();
//        while(it.hasNext())
//        {
//           Object m = ThisNiche.hg.get((HGHandle)it.next());
//           if(m instanceof EventPubSub)
//           {
//               //o = ThisNiche.hg.get(m.getSubscriber());
//               if(EvalResultEventType.HANDLE.equals(
//            		   ((EventPubSub)m).getEventType()))
//                 return ((EventPubSub)m).getEventHandler();
//           }
//        }
        return null;
    }

    public static Cell getOutCell(CellGroupMember cell)
    {
        HGHandle h = getOutCellHandle(cell);
        return (h != null) ? (Cell) ThisNiche.hg.get(h) : null;
    }

    public static HGHandle createOutputCellH(HGHandle par, String text,
            Component comp)
    {
        HGHandle h = (comp == null) ? ThisNiche.handleOf(text) : ThisNiche
                .handleOf(comp);
        if (h == null) h = addSerializable(comp == null ? text : comp);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        Cell out = new Cell(ref);

        HGHandle res = ThisNiche.hg.add(out);
        if (par != null) CellUtils.addEventPubSub(EvalResultEventType.HANDLE,
                par, res, res);
        return res;
    }

    public static HGHandle createCellHandle(String text)
    {
        Scriptlet s = new Scriptlet(defaultEngineName, text);
        HGHandle h = ThisNiche.handleOf(s);
        if (h == null) h = ThisNiche.hg.add(s);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        Cell out = new Cell(ref);
        return ThisNiche.hg.add(out);
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

    public static void setOutputCell(HGHandle cellH, Object value)
    {
        HGHandle old_h = getOutCellHandle(cellH);
        if (old_h == null && value == null) return;
        if (old_h != null && value != null)
        {
           // System.out.println("DISPATCH: " + value);
            EventDispatcher.dispatch(EvalResultEventType.HANDLE, cellH, value);
            return;
        } else if (old_h == null && value != null)
        {
            if (value instanceof String) createOutputCellH(cellH,
                    (String) value, null);
            else if (value instanceof HGHandle)
            {
                CellUtils.addEventPubSub(EvalResultEventType.HANDLE, cellH, 
                        (HGHandle)value, (HGHandle)value);
            }
            else
                createOutputCellH(cellH, null, (Component) value);
        } else if (old_h != null && value == null) 
        {
            removeOutputCellSubscription(old_h);
            EventDispatcher.dispatch(EvalResultEventType.HANDLE, cellH, value);
        }
    }

    public static void removeOutputCellSubscription(HGHandle cell_handle)
    {
        System.out.println("removeOutputCell: " + cell_handle);
        Set<HGHandle> set = CellUtils.findAll(ThisNiche.hg, hg.and(hg
                .type(EventPubSub.class), hg
                .incident(EvalResultEventType.HANDLE),
                hg.incident(cell_handle), hg.orderedLink(new HGHandle[] {
                        EvalResultEventType.HANDLE, HGHandleFactory.anyHandle,
                        cell_handle, HGHandleFactory.anyHandle })));
        for (HGHandle s : set)
           if(set != null)
               ThisNiche.hg.remove(s);

    }

    public static HGHandle makeCopy(HGHandle in_h)
    {
        CellGroupMember in = (CellGroupMember) ThisNiche.hg.get(in_h);
        // System.out.println("makeCopy: " + in);
        HGHandle out = null;
        if (in instanceof Cell)
        {
            Object val = ((Cell) in).getValue();
            if (val instanceof Scriptlet)
            {
                Scriptlet s = (Scriptlet) val;
                out = makeCellH(s.getCode(), s.getLanguage());
                HGHandle c = getOutCellHandle(in_h);
                if (c != null) setOutputCell(out, makeCopy(c));
            } else
            {
                HGHandle par = getOutCellParent(in_h);
                return (val instanceof String) ? createOutputCellH(par,
                        (String) val, null) : createOutputCellH(par, null,
                        (Component) val);
            }
        } else
        {
            CellGroup cg = (CellGroup) in;
            CellGroup outG = new CellGroup(cg.getName());
            out = ThisNiche.hg.add(outG);
            for (int i = 0; i < cg.getArity(); i++)
                outG.insert(i, makeCopy(cg.getTargetAt(i)));
        }
       return out;
    }

    public static HGHandle getOutCellParent(HGHandle h)
    {
        //HGHandle h = ThisNiche.handleOf(c);
        for (HGHandle hh : ThisNiche.hg.getIncidenceSet(h))
        {
            Object o = ThisNiche.hg.get(hh);
            if (o instanceof EventPubSub) { return ((EventPubSub) o)
                    .getPublisher(); }
        }
        return null;
    }

    public static void removeEventPubSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        List<HGHandle> subscriptions = getEventPubSubListH(eventType,
                publisher, subscriber, listener);
        for (HGHandle s : subscriptions)
            ThisNiche.hg.remove(s);

    }

    public static void addEventPubSub(HGHandle eventType, HGHandle pub,
            HGHandle sub, HGHandle handler)
    {
        if (!containsEventPubSub(eventType, pub, sub, handler)) {
            EventPubSub e = new EventPubSub(eventType, pub, sub, handler);
            //System.out.println("Adding " + e);
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
        // if (!subscriptions.isEmpty())
        // System.out.println("containsEventPubSub: " + publisher + ":"
        // + subscriber + ":" + (!subscriptions.isEmpty()));
        return !subscriptions.isEmpty();
    }

    public static List<EventPubSub> getEventPubSubList(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        return hg.findAll(ThisNiche.hg, hg.apply(hg.deref(ThisNiche.hg), hg
                .and(hg.type(EventPubSub.class), hg.incident(eventType), hg
                        .incident(publisher), hg.orderedLink(new HGHandle[] {
                        eventType, publisher, subscriber, listener }))));
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
        } else if (master instanceof Cell && copy instanceof Cell) addCellCopyListeners(
                masterH, copyH);
        else
            throw new RuntimeException("Bad Arguments:" + master + "OR" + copy);
    }

    private static void addCellGroupCopyListeners(HGHandle masterH,
            HGHandle copyH)
    {
        CellGroup master = (CellGroup) ThisNiche.hg.get(masterH);
        CellGroup copy = (CellGroup) ThisNiche.hg.get(copyH);
        // System.out
        // .println("addCellGroupCopyListeners: " + master + ":"
        // + master.getArity() + "////////" + copy + ":"
        // + copy.getArity());
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getInstance());
        CellUtils.addMutualEventPubSub(CellGroupChangeEvent.HANDLE, masterH,
                copyH, CopyCellGroupChangeHandler.getInstance());

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup) addCellGroupCopyListeners(
                    master.getTargetAt(i), copy.getTargetAt(i));
            else
                addCellCopyListeners(master.getTargetAt(i), copy.getTargetAt(i));
    }

    private static void addCellCopyListeners(HGHandle masterH, HGHandle copyH)
    {
        CellUtils.addMutualEventPubSub(CellTextChangeEvent.HANDLE, masterH,
                copyH, CopyCellTextChangeHandler.getInstance());

        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, masterH, copyH,
                CopyAttributeChangeHandler.getInstance());

        CellUtils.addMutualEventPubSub(EvalCellEvent.HANDLE, masterH, copyH,
                CopyEvalCellHandler.getInstance());
    }

    public static void removeHandlers(HGHandle masterH)
    {
        if(masterH == null) return;
        Object master = ThisNiche.hg.get(masterH);
        if (master instanceof CellGroup) removeCellGroupHandlers(masterH);
        else
            remove_event_handlers(masterH);
    }

    private static void removeCellGroupHandlers(HGHandle masterH)
    {
        CellGroup master = (CellGroup) ThisNiche.hg.get(masterH);
        remove_event_handlers(masterH);

        for (int i = 0; i < master.getArity(); i++)
            if (master.getElement(i) instanceof CellGroup)
            {
                removeCellGroupHandlers(master.getTargetAt(i));
            } else
            {
                remove_event_handlers(master.getTargetAt(i));
            }
    }

    private static void remove_event_handlers(HGHandle masterH)
    {
        List<HGHandle> subs = getListForPubOrSub(HGHandleFactory.anyHandle,
                masterH, HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
        for (HGHandle s : subs)
            ThisNiche.hg.remove(s);
        subs = getListForPubOrSub(HGHandleFactory.anyHandle,
                HGHandleFactory.anyHandle, masterH, HGHandleFactory.anyHandle);
        for (HGHandle s : subs)
            ThisNiche.hg.remove(s);
    }

    static List<HGHandle> getListForPubOrSub(HGHandle eventType,
            HGHandle publisher, HGHandle subscriber, HGHandle listener)
    {
        HGHandle pub_or_sub = HGHandleFactory.anyHandle.equals(publisher) ? subscriber
                : publisher;
        return hg.findAll(ThisNiche.hg, hg.and(hg.type(EventPubSub.class), hg
                .incident(pub_or_sub), hg.orderedLink(new HGHandle[] {
                eventType, publisher, subscriber, listener })));
    }

    public static HGHandle addSerializable(Object o)
    {
        HGHandle h = null;
        HGTypeSystem ts = ThisNiche.hg.getTypeSystem();
        try
        {
            if (o instanceof Component && o instanceof Serializable && !(o instanceof NotebookUI))
            {
                try{
                    h = ThisNiche.hg.add(o);
                }catch(Throwable ex){
                HGHandle t = ts.getTypeHandle(Serializable.class);
                //hasType will return true after first add, so check to see if the
                //already used type is Serializable
                //if(!ts.hasType(o.getClass()) || t.equals(ts.getTypeHandle(o.getClass()))) 
                   h = ThisNiche.hg.add(o, t);
                //else
                   // h = ThisNiche.hg.add(o);
                }
            }
            else
               h = ThisNiche.hg.add(o);
        }
        catch (Throwable ex)
        {
            System.err.println("Unable to add Cell value: " + o + " Reason: " + ex);
            h = ThisNiche.hg.add(ex.toString());
        }
        return h;
    }
    
    public static void processCelTextChangeEvent(HGHandle cH, CellTextChangeEvent e)
    {
        Cell c = (Cell) ThisNiche.hg.get(cH);
        Scriptlet s = (Scriptlet) c.getValue();
        String code = s.getCode();
        //System.out.println("processEvent: "  + cH + ":" + e + ":" + code.length() + ":" + code);
         StringBuffer res = new StringBuffer(code.substring(0, e.getOffset()));
        if (e.getType() == CellTextChangeEvent.EventType.INSERT)
        {
            res.append(e.getText());
            res.append(code.substring(e.getOffset()));
        } else
        {
            res.append(code.substring(e.getOffset() + e.getLength()));
        }
        s.setCode(res.toString());
        ThisNiche.hg.update(s);
        c.fireCellTextChanged(e);
    }
    
    //TODO: looks like a bug in HG.... sometimes it returns  a list with duplicates  
    public static <T> Set<T> findAll(HyperGraph graph, HGQueryCondition condition)
    {
        Set<T> result = new HashSet<T>();
        Map<T, Boolean> map = new IdentityHashMap<T, Boolean>();
        HGSearchResult<T> rs = null;
        try
        {
            rs = graph.find(condition);
            while (rs.hasNext())
            {
                T o = rs.next();
                if (map.containsKey(o)) // if(result.contains(o))
                {
                    System.err.println("Duplicate entry in findAll: " + o);
                    System.err.println("Duplicate entry in findAll: " + condition);
                    Thread.dumpStack();
                }
                result.add(o);
                map.put(o, true);
            }
            return result;
        }
        finally
        {
            if (rs != null) rs.close();
        }           
    }
}
