package seco.gui;

import java.awt.BorderLayout;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;


/**
 * Swing component for inspecting the fields of a given object in a tree table
 * as in most common GUI debuggers.  
 * @author Konstantin Vandev
 */
public class ObjectInspector extends Outline 
{
    private static Set<PropNodeFactory> factories = new HashSet<PropNodeFactory>();
    static
    {
       registerNodeFactory(new CollectionNodeFactory());
       registerNodeFactory(new MapNodeFactory());
       registerNodeFactory(new ArrayNodeFactory());
    }
    
    /**
     * The constructor.
     * @param obj - The Object to be inspected
     */
    public ObjectInspector(Object obj)
    {
        super();
        setRootVisible(false);
        setRenderDataProvider(new RenderData());
        setDefaultRenderer(Object.class, new MyDefaultOutlineCellRenderer());
        setModelObject(obj);
    }
    
    
    /**
     * Sets the object to be inspected
     * @param obj The Object to be inspected
     */
    public void setModelObject(Object obj)
    {
        if(obj == null) return;
        
        TreeModel treeMdl = new PropsTreeModel(obj);
        DefaultOutlineModel mdl = (DefaultOutlineModel)
             DefaultOutlineModel.createOutlineModel(treeMdl,
                new PropsRowModel(), true);
        mdl.setNodesColumnLabel("Property");
        setModel(mdl);
    }

    private static PropNode createPropNode(String name, Object o)
    {
        if(o != null)
        {
            PropNodeFactory f = getNodeFactory(o.getClass());
            if(f != null)
                return f.createNode(name, o);
        }
        return new PropNode(name, o);
    }
    
   
    /**
     * Registers given NodeFactory
     * @param factory
     */
    public static void registerNodeFactory(PropNodeFactory factory)
    {
        factories.add(factory);
    }
    
    
    private static PropNodeFactory getNodeFactory(Class<?> clazz)
    {
        for(PropNodeFactory m : factories)
            if(m.supportsClass(clazz))
                return m;
        return null;
    }
    
    /**
     * Factory interface for creating PropNode for given class 
     */
    public static interface PropNodeFactory {
        public boolean supportsClass(Class<?> c);
        public PropNode createNode(String name, Object o);
    }
    
    /**
     * A node in the tree table representing a given object field alongside its name, value
     * and child nodes. 
     */
    public static class PropNode implements Comparable
    {
        public String name;
        public Object value;
        protected PropNode[] children;
        
        
        /**
         * Constructor
         * @param name Node's name, could be null  
         * @param value Node's value, could be null
         */
        public PropNode(String name, Object value)
        {
            super();
            this.value = value;
            this.name = name;
        }

        /**
         * Constructor
         * @param value Node's value, could be null
         */
        public PropNode(Object object)
        {
            this(null, object);
        }
        
        
        /**
         * Returns node's children
         * @return Node's children
         */
        public PropNode[] children()
        {
            if(children != null) return children;
            if(value == null)
                return children = new PropNode[0];
            populate_children();            
            if(children == null) 
                children = new PropNode[0];
            return children;
        }
        
        protected void populate_children()
        {
            Field[] fs = getFieldsForTypeArray(value.getClass());
            children = new PropNode[fs.length];
            for(int i = 0; i < fs.length; i++)
            {
                Object inner = getPrivateFieldValue(value, 
                        value.getClass(), fs[i].getName());
                children[i] = createPropNode(fs[i].getName(), inner);
            }
            Arrays.sort(children);
        }
        
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o)
        {
            if(o instanceof PropNode)
            {
                PropNode p = (PropNode) o;
                if(p.name != null)
                    return - p.name.compareTo(name);
                else
                {
                    return (name != null) ? 1 : 0;
                }
            }
            return 0;
        }
    }
    
    private static class ArrayNodeFactory implements PropNodeFactory
    {
        public boolean supportsClass(Class<?> c)
        {
           return c.isArray();   
        } 
        
        public PropNode createNode(final String name, final Object o)
        {
            return new PropNode(name, o)
            {
                protected void populate_children()
                {
                    int len = Array.getLength(value);
                    children = new PropNode[len];
                    for(int i = 0; i < len; i++)
                        children[i] = new PropNode("[" + i + "]", Array.get(value, i));
                   // Arrays.sort(children);
                }
            };
        }
    }      
       
    
    private static class CollectionNodeFactory implements PropNodeFactory
    {
        public PropNode createNode(final String name, final Object o)
        {
            return new PropNode(name, o)
            {
                protected void populate_children()
                {
                    Collection<Object> list = (Collection<Object>) value;
                    children  = new PropNode[list.size()];
                    int i = 0;
                    for (Object o: list)
                    {
                        children[i] = new PropNode("[" + i + "]", o);
                        i++;
                    }
                }
            };
        }
        
        public boolean supportsClass(Class<?> c)
        {
           return Collection.class.isAssignableFrom(c);   
        } 
    }
    
    private static class MapNodeFactory implements PropNodeFactory
    {
        public PropNode createNode(final String name, final Object o)
        {
            return new PropNode(name, o)
            {
                protected void populate_children()
                {
                    Map<Object, Object> map = (Map<Object, Object>) value;
                    if(map.entrySet() == null)
                        return;
                    children  = new PropNode[map.size() + 1];
                    children[0] = new PropNode("size", map.size());
                    int i = 1;
                    for (Map.Entry<Object, Object> e: map.entrySet())
                    {
                        if(e == null) continue;
                        children[i] = new PropNode("" + e.getKey(), e.getValue());
                        i++;
                    }
                }
            };
        }
        
        public boolean supportsClass(Class<?> c)
        {
           return Map.class.isAssignableFrom(c);   
        } 
    }

    private static class PropsTreeModel implements TreeModel
    {
        private PropNode node;

        public PropsTreeModel(Object object)
        {
            super();
            this.node = createPropNode(null, object);
        }

        public Object getChild(Object parent, int index)
        {
            PropNode n = (PropNode) parent;
            if(index > n.children().length -1) 
                System.err.println("Problem: " + n.value + ":" + index + ":" +
                        n.children().length);
            Object res = n.children()[index];
            if(res == null)
                System.out.println("NULL Object in: " + index + ":" + parent);
            return res;
            //return n.children()[index];
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            PropNode n = (PropNode) parent;
            return Arrays.asList(n.children()).indexOf(child);
        }

        public int getChildCount(Object parent)
        {
            PropNode n = (PropNode) parent;
            return n.children() != null ? n.children().length : 0;
        }

        public Object getRoot()
        {
            return node;
        }

        public boolean isLeaf(Object node)
        {
           if (node == null) return true;
           return getChildCount(node) == 0;
        }

        public void addTreeModelListener(TreeModelListener l)
        {
            // Do nothing
        }

        public void removeTreeModelListener(TreeModelListener l)
        {
            // Do nothing
        }

        public void valueForPathChanged(TreePath path, Object newValue)
        {
            // Do nothing
        }
    }

    private static class PropsRowModel implements RowModel
    {
        public Class<?> getColumnClass(int column)
        {
            switch (column)
            {
            case 0:
                return Object.class;
            default:
                assert false;
            }
            return null;
        }

        public int getColumnCount()
        {
            return 1;
        }

        public String getColumnName(int column)
        {
            return "Value";
        }

        public Object getValueFor(Object node, int column)
        {
            PropNode n = (PropNode) node;
            return n.value;
        }

        public boolean isCellEditable(Object node, int column)
        {
            return false;
        }

        public void setValueFor(Object node, int column, Object value)
        {
            // do nothing for now
        }
    }

    private static class RenderData implements RenderDataProvider
    {
        public java.awt.Color getBackground(Object o)
        {
            return null;
        }

        public String getDisplayName(Object o)
        {
            return ((PropNode) o).name;
        }

        public java.awt.Color getForeground(Object o)
        {
            return null;
        }

        public javax.swing.Icon getIcon(Object o)
        {
            return null;
        }

        public String getTooltipText(Object o)
        {
            return "" + o; 
        }

        public boolean isHtmlDisplayName(Object o)
        {
            return false;
        }

    }
    
    private static class MyDefaultOutlineCellRenderer extends DefaultOutlineCellRenderer
    {

        @Override
        public void setIcon(Icon icon)
        {
            //Do nothing
        }
    }
    
    private static Map<String, ClassInspector> inspectors = new HashMap<String, ClassInspector>();

    synchronized static ClassInspector getInspector(Class<?> type)
    {
        if (type == null) return null;
        String name = type.getName();
        ClassInspector conv = inspectors.get(name);
        if (conv == null)
        {
            conv = new ClassInspector(type);
            inspectors.put(name, conv);
        }
        return conv;
    }
    
    private static final Field[] EMPTY = new Field[0];
    static synchronized Field[] getFieldsForTypeArray(Class<?> type)
    {
        if(primitiveEquivalentOf(type) != null) //|| String.class.equals(type))
            return EMPTY;
        Set<Field> set  = getInspector(type).getSlots();
        return set.toArray(new Field[set.size()]);
    }
    
    private static class ClassInspector 
    {
        protected Class<?> type;
        protected Set<Field> cachedSlots;
        
        public ClassInspector(Class<?> type)
        {
            this.type = type;
        }

        public Set<Field> getSlots()
        {
            if (cachedSlots != null)
                return cachedSlots;
            cachedSlots = new HashSet<Field>();
            Field[] fs = getAllFields(type);
            for (Field f : fs)
                if (!Modifier.isStatic(f.getModifiers())) 
                    cachedSlots.add(f);
            return cachedSlots;
        }
   }
    
    /**
     * Return a list of all fields (whatever access status, and on whatever
     * superclass they were defined) that can be found on this class.
     * <p>This is like a union of {@link Class#getDeclaredFields()} which
     * ignores and super-classes, and {@link Class#getFields()} which ignored
     * non-public fields
     * @param clazz The class to introspect
     * @return The complete list of fields
     */
    static Field[] getAllFields(Class<?> clazz)
    {
        List<Class<?>> classes = getAllSuperclasses(clazz);
        classes.add(clazz);
        return getAllFields(classes);
    }
    /**
     * As {@link #getAllFields(Class)} but acts on a list of {@link Class}s and
     * uses only {@link Class#getDeclaredFields()}.
     * @param classes The list of classes to reflect on
     * @return The complete list of fields
     */
    private static Field[] getAllFields(List<Class<?>> classes)
    {
        Set<Field> fields = new HashSet<Field>();
        for (Class<?> clazz : classes)
        {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }

        return fields.toArray(new Field[fields.size()]);
    }
    /**
     * Return a List of super-classes for the given class.
     * @param clazz the class to look up
     * @return the List of super-classes in order going up from this one
     */
    static List<Class<?>> getAllSuperclasses(Class<?> clazz)
    {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null)
        {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }

        return classes;
    }
    
    static Object getValue(Object instance, Class<?> cls, String name)
    {
        try
        {
            Field f = getPublicField(cls, name);
            if (f != null)
                return f.get(instance);
            f = getPrivateField(cls, name);
            if (f != null)
            {
                f.setAccessible(true);
                return f.get(instance);
            }
            Method m = getGetMethod(cls, name);
            if (m != null) return m.invoke(instance);
        }
        catch (Exception e)
        {
            System.err.println("Unable to retrieve field: " + name + " for "
                    + instance + "in " + cls + " Reason: " + e);
            //e.printStackTrace();
        }
        return null;
    }
    
    static Method getGetMethod(final Class<?> clazz, final String name)
    {
        final String fieldName = name.substring(0, 1).toUpperCase()
                + name.substring(1);
        Method getMethod = null;
        try
        {
            getMethod = clazz.getMethod("get" + fieldName, new Class<?>[] {});
        }
        catch (final Exception e)
        {
        }
        if (getMethod == null) try
        {
            getMethod = clazz.getMethod("is" + fieldName, new Class<?>[] {});
        }
        catch (final Exception e)
        {
        }
        return getMethod;
    }
    
    static Field getField(Class<?> cls, String name)
    {
        Field f = getPublicField(cls, name);
        if(f == null)
            f = getPrivateField(cls, name);
        return f;
    }
    
    static Field getPublicField(Class<?> cls, String name)
    {
        try
        {
            Field f = cls.getField(name);
            if (f != null && !Modifier.isStatic(f.getModifiers()) &&
                    Modifier.isPublic(f.getModifiers()))
                return f;
        }
        catch (Exception e)
        {
        }
        return null;
    }

    static Field getPrivateField(Class<?> cls, String name)
    {
        try
        {
            Field f = cls.getDeclaredField(name);
            if (f != null &&
                    !Modifier.isStatic(f.getModifiers())) 
                return f;
        }
        catch (Exception e)
        {
        }
        return (cls.getSuperclass() == null) ? null : getPrivateField(cls
                .getSuperclass(), name);
    }

    static Object getPrivateFieldValue(Object instance, Class<?> cls,
            String name)
    {
        int dot = name.indexOf(".");
        if (dot > 0) return getValue(instance, cls, name);
        try
        {
            Field f = getPrivateField(cls, name);
            if (f != null)
            {
                f.setAccessible(true);
                return f.get(instance);
            }
        }
        catch (Exception e)
        {
            System.err.println("Unable to get field: " +
                    name + " in class: " + cls.getName());
        }
        return null;
    }

    /**
     * Mapping from primitive wrapper Classes to their
     * corresponding primitive Classes.
     */
    private static final Map<Class<?>, Class<?>> objectToPrimitiveMap = new HashMap<Class<?>, Class<?>>(13);
    
    static
    { 
        objectToPrimitiveMap.put(Boolean.class, Boolean.TYPE);
        objectToPrimitiveMap.put(Byte.class, Byte.TYPE);
        objectToPrimitiveMap.put(Character.class, Character.TYPE);
        objectToPrimitiveMap.put(Double.class, Double.TYPE);
        objectToPrimitiveMap.put(Float.class, Float.TYPE);
        objectToPrimitiveMap.put(Integer.class, Integer.TYPE);
        objectToPrimitiveMap.put(Long.class, Long.TYPE);
        objectToPrimitiveMap.put(Short.class, Short.TYPE);
    }
    
    /**
     * @param  aClass  a Class
     * @return  the class's primitive equivalent, if aClass is a
     * primitive wrapper.  If aClass is primitive, returns aClass.
     * Otherwise, returns null.
     */
    static Class<?> primitiveEquivalentOf(Class<?> aClass)
    {
        return aClass.isPrimitive()
        ? aClass
        : (Class<?>) objectToPrimitiveMap.get(aClass);
    }
    
    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new BorderLayout());
      Map<String, Object> l = new HashMap<String, Object>();
      l.put("First", "First");
      l.put("Sec",  new JButton("Test"));
      // Object[] l = new Object[]{ "First", new JButton("Test")};
      //Object obj = new JButton("Test");
        ObjectInspector outline = new ObjectInspector(l);
        f.getContentPane().add(new JScrollPane(outline), BorderLayout.CENTER);

        f.setBounds(20, 20, 700, 400);
        f.setVisible(true);
    }

}

