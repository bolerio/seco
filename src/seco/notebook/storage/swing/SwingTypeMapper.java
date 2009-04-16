package seco.notebook.storage.swing;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.indexing.HGIndexer;
import org.hypergraphdb.type.BonesOfBeans;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.JavaObjectMapper;
import org.hypergraphdb.type.JavaTypeFactory;
import org.hypergraphdb.type.RecordType;

import seco.notebook.storage.swing.types.ClassGenerator;
import seco.notebook.storage.swing.types.GeneratedClass;
import seco.notebook.storage.swing.types.SwingBinding;
import seco.notebook.storage.swing.types.SwingType;

public class SwingTypeMapper extends JavaObjectMapper
{

    protected HGIndex<String, HGPersistentHandle> getIndex()
    {
        if (idx == null)
        {
            HGHandle t = graph.getTypeSystem()
                    .getTypeHandle(HGSerializable.class);
            HGIndexer indexer = new ByPartIndexer(t, "classname");
            idx = graph.getIndexManager().getIndex(indexer);
            if (idx == null)
            {
                idx = graph.getIndexManager().register(indexer);
            }
            return idx;
        }
        return idx;
    }

    public HGAtomType defineHGType(Class<?> javaClass, HGHandle typeHandle)
    {
        if (javaClass.isEnum())
            return null;
        
        if (ImageIcon.class.isAssignableFrom(javaClass))
            return graph.getTypeSystem().getAtomType(Serializable.class);

        if (javaClass.getName().startsWith("javax")
            || javaClass.getName().startsWith("java.awt")
            || javaClass.getName().startsWith("java.beans")
            || mapAsSerializableObject(javaClass))
        {

            SwingType type = new SwingType(javaClass);
            type.setHyperGraph(graph);
            type.init(typeHandle);
            return type;
        }
        return null;
    }

    public HGAtomType getJavaBinding(HGHandle typeHandle, HGAtomType hgType,
                                     Class<?> javaClass)
    {
        if (hgType instanceof SwingType)
        {
            if (Modifier.isPublic(javaClass.getModifiers()))
            {
                try
                {
                    Class<?> gen = ClassGenerator.getClass(javaClass);
                    if (gen == null)
                        gen = new ClassGenerator(graph, (SwingType) hgType).generate();
                    GeneratedClass inst = (GeneratedClass) gen.newInstance();
                    inst.init(typeHandle, (SwingType) hgType);
                    return inst;
                }
                catch (Throwable ex)
                {
                    System.err.println(ex);
                    ex.printStackTrace();
                }
            }
            return new SwingBinding(typeHandle, (SwingType) hgType);
        }

        return null;
    }

    public void addClass(String classname)
    {
        initClasses();
        try
        {
            Class<?> c = Class.forName(classname);
            for (String existing : classes)
            {
                Class<?> e = null;
                try
                {
                    e = Class.forName(existing);
                }
                catch (Exception ex)
                {
                }
                if (e != null && e.isAssignableFrom(c))
                    return;
            }
            graph.add(new HGSerializable(classname));
            classes.add(classname);
        }
        catch (Exception ex)
        {
            throw new HGException(ex);
        }
    }

    protected boolean checkClass(Class<?> javaClass)
    {
        if (!(classes.contains(javaClass.getName())
                || javaClass.getName().startsWith("javax")
                || javaClass.getName().startsWith("java.awt") || javaClass.getName()
                .startsWith("java.beans")))
        {
            Class<?> parent = javaClass.getSuperclass();
            if (parent == null)
                return false;
            if (checkClass(parent))
                return true;
            for (Class<?> in : javaClass.getInterfaces())
                if (checkClass(in))
                    return true;
            return false;
        }
        else
            return true;
    }

    public static void defineGUIPanelType(HyperGraph graph, Class<?> javaClass, Class<?> parent)
    {
        HGPersistentHandle typeConstructor = 
            graph.getPersistentHandle(
                    graph.getTypeSystem().getTypeHandle(RecordType.class));
        HGAtomType type = makeGUIBeanType(graph, javaClass, parent);
       // graph.getTypeSystem().addPredefinedType(typeConstructor, type, javaClass);
    }
    
    static RecordType makeGUIBeanType(HyperGraph graph, Class<?> javaClass, Class<?> parent)
    {
        HGTypeSystem typeSystem = graph.getTypeSystem();
        JavaTypeFactory javaTypes = typeSystem.getJavaTypeFactory();
        BeanInfo bi = null;
        try{
          bi = Introspector.getBeanInfo(javaClass, parent);
        }catch(IntrospectionException e){
            throw new HGException(e); 
        }
        Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
        for(PropertyDescriptor pd: bi.getPropertyDescriptors())
        {
            //
            // Accept only properties that are both readable and writeable!
            //
            if (!includeProperty(javaClass, pd)) continue;
            descriptors.put(pd.getName(), pd);
        }
        RecordType recordType = new RecordType();
        recordType.setHyperGraph(graph);
        for (Iterator<PropertyDescriptor> i = descriptors.values().iterator(); i.hasNext();) 
        {
            PropertyDescriptor desc = i.next();
            Class<?> propType = desc.getPropertyType();
            if (propType.isPrimitive())
                propType = BonesOfBeans.wrapperEquivalentOf(propType);
            HGHandle valueTypeHandle = typeSystem.getTypeHandle(propType);
            if (valueTypeHandle == null)
                throw new HGException("Unable to get HyperGraph type for Java class " + 
                                      propType.getName() + ": make sure it's default or 'link' constructible.");
            HGHandle slotHandle = javaTypes.getSlotHandle(desc.getName(), 
                                                          valueTypeHandle);
            //Slot slot = graph.get(slotHandle);
            recordType.addSlot(slotHandle);
            // HGAtomRef.Mode refMode = getReferenceMode(javaClass, desc);                     
            // if (refMode != null)
            //    typeSystem.getHyperGraph().add(new AtomProjection(typeHandle, 
            //                                                      slot.getLabel(),
            //                                                      slot.getValueType(), 
            //                                                      refMode));
        }
        return recordType;
    }
    
    private static boolean includeProperty(Class<?> javaClass, PropertyDescriptor desc)
    {
        Method reader = desc.getReadMethod();
        Method writer = desc.getWriteMethod();
        if (reader == null || writer == null)
            return false;
        if (reader.getAnnotation(HGIgnore.class) != null ||
            writer.getAnnotation(HGIgnore.class) != null)
            return false;
        try
        {
            Field field = javaClass.getDeclaredField(desc.getName());
            return field.getAnnotation(HGIgnore.class) == null && 
                  (field.getModifiers() & Modifier.TRANSIENT) == 0; 
        }
        catch (NoSuchFieldException ex)
        {
            return true;
        }
    }
    
    
    public static class HGSerializable
    {
        private String classname;

        public HGSerializable()
        {
        }

        public HGSerializable(String classname)
        {
            this.classname = classname;
        }

        public String getClassname()
        {
            return classname;
        }

        public void setClassname(String classname)
        {
            this.classname = classname;
        }
    }
}
