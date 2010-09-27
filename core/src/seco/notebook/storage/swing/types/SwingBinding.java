package seco.notebook.storage.swing.types;

import java.awt.event.ActionListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.atom.HGAtomRef;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.type.BeanPropertyBasedProjection;
import org.hypergraphdb.type.HGAbstractType;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.hypergraphdb.type.Record;
import org.hypergraphdb.type.Slot;
import org.hypergraphdb.type.TypeUtils;

import seco.gui.GUIHelper;
import seco.notebook.storage.swing.DefaultConverter;

public class SwingBinding extends HGAtomTypeBase implements HGCompositeType
{
    protected HGHandle typeHandle;
    protected SwingType hgType;
    private SwingTypeIntrospector inspector;

    public SwingBinding()
    {
    }

    public SwingBinding(HGHandle typeHandle, SwingType hgType)
    {
        this.typeHandle = typeHandle;
        this.hgType = hgType;
        hgType.setThisHandle(typeHandle);
    }

    protected SwingTypeIntrospector getInspector()
    {
        if (inspector == null)
            inspector = new SwingTypeIntrospector(graph, hgType);
        return inspector;
    }

    public Object make(HGPersistentHandle handle,
            LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet)
    {
        Object bean = null;
        try
        {
            // current make mechanism impose some limitation:
            // class with special constructor can't contain properties
            // that reference this class's instance
            boolean no_spec_ctr = hgType.getCtrHandle() == null
                    || graph.getHandleFactory().nullHandle().equals(
                            hgType.getCtrHandle());
            if (no_spec_ctr)
            {
                bean = instantiate(hgType.getCtrHandle(), null);
                TypeUtils.setValueFor(graph, handle, bean);
            }
            Record record = (Record) hgType.make(handle, targetSet, null);
            if (!no_spec_ctr)
            {
                bean = instantiate(hgType.getCtrHandle(), record);
                TypeUtils.setValueFor(graph, handle, bean);
            }

            // if(bean instanceof JButton && "Connect".equals(
            // ((JButton) bean).getText()))
            // {
            // System.out.println("SwingBinding: " + handle + ":" + bean);
            // }

            if (bean == null)
            {
                System.err.println("Unable to create bean fo type: "
                        + hgType.getJavaClass());
                return null;
            }

            makeBean(bean, record);
            // System.out.println("Make - res: " + bean);
            AddOnLink addons = (AddOnLink) graph.get(hgType.getAddOnsHandle());
            if (addons != null) for (int i = 0; i < addons.getArity(); i++)
            {
                HGRelType l = (HGRelType) graph.get(addons.getTargetAt(i));
                AddOnFactory.processLink(graph, l, record, bean);
            }

        }
        catch (Throwable t)
        {
            throw new HGException("SwingBinding.make: " + t.toString(), t);
        }
        return bean;
    }

    public HGPersistentHandle store(final Object instance)
    {
        HGPersistentHandle result = TypeUtils.getHandleFor(graph, instance);
        if (result == null)
        {
            final Record record = new SwingRecord(typeHandle, instance);
            storeBean(instance, record);
            result = hgType.store(record);
        }
        return result;
    }

    public void release(HGPersistentHandle handle)
    {
        hgType.release(handle);
    }

    private Object instantiate(HGHandle h, Record record)
    {
        ConstructorLink link = (ConstructorLink) graph.get(h);
        // System.out.println("SB - instantiate" +
        // hgType.getJavaClass() + ":" + link);
        if (link != null && link instanceof FactoryConstructorLink)
            return AddOnFactory.instantiateFactoryConstructorLink(graph,
                    hgType, (FactoryConstructorLink) link, record);
        return AddOnFactory.instantiateConstructorLink(graph, hgType, link,
                record);
    }

    protected void makeBean(Object bean, Record record)
    {
        for (HGHandle slotHandle : hgType.getSlots())
        {
            Slot slot = (Slot) graph.get(slotHandle);
            String label = slot.getLabel();
            Object value = record.get(slot);
            if (hgType.getReferenceMode(slotHandle) != null)
                value = graph.get(((HGAtomRef) value).getReferent());
            // System.out.println("Slot: " + slot.getLabel() + ":" + value);
            if (value == null) continue;
            SwingTypeIntrospector insp = this.getInspector();
            if (insp.getPubFieldsMap().containsKey(label)) try
            {
                // System.out.println("Field");
                insp.getPubFieldsMap().get(label).set(bean, value);
            }
            catch (IllegalAccessException ex)
            {
                System.err.println("Unable to set field: " + label + " on "
                        + hgType.getJavaClass().getName());
            }
            else if (insp.getPrivFieldsMap().containsKey(label)) try
            {
                Field f = insp.getPrivFieldsMap().get(label);
                f.setAccessible(true);

                f.set(bean, value);
            }
            catch (IllegalAccessException ex)
            {
                System.err.println("Unable to set field: " + label + " on "
                        + hgType.getJavaClass().getName());
            }
            else if (insp.getEventSetDescriptorsMap().containsKey(label))
            {
                try
                {
                    Method m = insp.getEventSetDescriptorsMap().get(label)
                            .getAddListenerMethod();
                    EventListener[] l = (EventListener[]) value;
                    if (l != null) for (EventListener el : l)
                        m.invoke(bean, new Object[] { el });
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
            else if (insp.getSettersMap().containsKey(label))
                try
                {
                    insp.getSettersMap().get(label).invoke(bean,
                            new Object[] { value });
                }
                catch (Throwable t)
                {
                    System.err.println("Unable to set property: " + label
                            + " on " + hgType.getJavaClass().getName()
                            + ".Reason: " + t);
                }
        }
    }

    protected void storeBean(Object bean, Record record)
    {
        getInspector();
        try
        {
            for (String s : inspector.getGettersMap().keySet())
            {
                setValue(record, s, inspector.getGettersMap().get(s).invoke(
                        bean));
            }
            for (Field f : inspector.getPubFieldsMap().values())
                setValue(record, f.getName(), f.get(bean));

            for (String s : inspector.getEventSetDescriptorsMap().keySet())
            {
                EventSetDescriptor e = inspector.getEventSetDescriptorsMap()
                        .get(s);
                if (e != null && !filterListenersByType(e.getListenerType()))
                {
                    Method m = e.getGetListenerMethod();
                    EventListener[] ls = (EventListener[]) m.invoke(bean);
                    setValue(record, e.getName()
                            + DefaultConverter.LISTENERS_KEY, filterListeners(
                            bean, ls));
                }
            }
            for (Field f : inspector.getPrivFieldsMap().values())
            {
                f.setAccessible(true);
                setValue(record, f.getName(), f.get(bean));
            }
        }
        catch (Exception ex)
        {

        }
    }

    public void setValue(Record rec, String name, Object value)
    {
//        if ("delegate".equals(name))
//        {
//            System.out.println("REMOVE ME");
//        }
        HGHandle slotHandle = hgType.slotHandles.get(name);
        Slot slot = (Slot) graph.get(slotHandle);
        HGAtomRef.Mode refMode = hgType.getReferenceMode(slotHandle);
        if (refMode != null && value != null)
        {
            HGHandle valueAtomHandle = graph.getHandle(value);
            if (valueAtomHandle == null)
            {
                HGAtomType valueType = (HGAtomType) graph.get(slot
                        .getValueType());
                valueAtomHandle = graph.getPersistentHandle(graph.add(value,
                        valueType instanceof HGAbstractType ? graph
                                .getTypeSystem()
                                .getTypeHandle(value.getClass()) : slot
                                .getValueType()));
            }
            value = new HGAtomRef(valueAtomHandle, refMode);
        }
        rec.set(slot, filterValue(value));
    }

    protected Object filterValue(Object val)
    {
        if (val == null) return null;
        if (val.getClass().isAnonymousClass()) return null;

        if (val.getClass().isMemberClass()
                && !Modifier.isStatic(val.getClass().getModifiers()))
        {
            // System.err.println("Filtering1 " + e);
            return null;
        }
        return val;
    }

    public Object getValue(Record record, String name)
    {
//        if ("delegate".equals(name))
//        {
//            System.out.println("REMOVE ME");
//        }
        HGHandle slotHandle = hgType.slotHandles.get(name);
        Slot slot = (Slot) graph.get(slotHandle);
        Object value = record.get(slot);
        if (value != null && hgType.getReferenceMode(slotHandle) != null)
            value = graph.get(((HGAtomRef) value).getReferent());
        return value;
    }

    protected boolean filterListenersByType(Class<?> listenerType)
    {
        if (listenerType == java.awt.event.ComponentListener.class
                || listenerType == AncestorListener.class) return true;

        // JMenuItems have a change listener added to them in
        // their "add" methods to enable accessibility support -
        // see the add method in JMenuItem for details. We cannot
        // instantiate this instance as it is a private inner class
        // and do not need to do this anyway since it will be created
        // and installed by the "add" method. Special case this for now,
        // ignoring all change listeners on JMenuItems.
        if (listenerType == javax.swing.event.ChangeListener.class
                && hgType.getJavaClass() == javax.swing.JMenuItem.class) { return true; }
        return false;
    }

    protected EventListener[] filterListeners(Object instance,
            EventListener[] in)
    {
        if (in == null) return null;
        Set<EventListener> res = new HashSet<EventListener>(in.length);
        for (EventListener e : in)
        {
            if (e instanceof PropertyChangeListener
                    || e instanceof AncestorListener
                    || e instanceof ListDataListener
                    || e instanceof ContainerListener)
            {
                continue;
            }
            int mode = e.getClass().getModifiers();
            if (e.getClass().isAnonymousClass() || !Modifier.isPublic(mode))
            {
                // System.err.println("Filtering0 " + e);
                continue;
            }

            if (e.getClass().isMemberClass() && !Modifier.isStatic(mode))
            {
                // System.err.println("Filtering1 " + e);
                continue;
            }

            // normally those listeners will be added during construction
            if (e.getClass().equals(hgType.getJavaClass()))
            {
                // System.err.println("Filtering3 " + e);
                continue;
            }

            // the action is added as event listener too, so filter it
            if (e instanceof ActionListener
                    && instance instanceof AbstractButton
                    && ((AbstractButton) instance).getAction() != null)
            {
                // System.err.println("Filtering4 " + e);
                continue;
            }
            if (e instanceof DefaultCaret) continue;
            // filter those package private JTextComponent.MutableCaretEvent
            // listers
            // that get re-added in ctr
            if ((instance instanceof JTextComponent || instance instanceof Caret)
                    && e instanceof ChangeListener
                    && e instanceof FocusListener && e instanceof MouseListener)
                continue;

            if (e instanceof ToolTipManager)
                continue;
            res.add(e);
        }
        return res.toArray(new EventListener[res.size()]);
    }

    public Iterator<String> getDimensionNames()
    {
        return hgType.getDimensionNames();
    }

    public HGProjection getProjection(String dimensionName)
    {
        HGProjection p = hgType.getProjection(dimensionName);
        if (p == null) throw new HGException("Could not find projection for '"
                + dimensionName + "' in HG type " + typeHandle + " for "
                + hgType.getJavaClass().getName());
        else
            return new BeanPropertyBasedProjection(p);
    }

    static class SwingRecord extends Record implements
            TypeUtils.WrappedRuntimeInstance
    {
        Object bean;

        SwingRecord(HGHandle h, Object bean)
        {
            super(h);
            this.bean = bean;
        }

        public Object getRealInstance()
        {
            return bean;
        }
    }
}