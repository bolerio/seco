package seco.notebook.storage.swing;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGException;
import org.hypergraphdb.type.BonesOfBeans;

public class DefaultConverter // implements Converter
{
    public static final String LISTENERS_KEY = "Listeners";
    protected String[] ctrParamNames = new String[0];
    protected Class<?>[] ctrParamTypes;
    protected Map<String, Class> map = new HashMap<String, Class>();
    protected Constructor<?> ctr;
    protected Class<?> type;
    protected Method factoryMethod;

    DefaultConverter()
    {
    }

    public DefaultConverter(Class<?> type)
    {
        this(type, null);
    }

    public DefaultConverter(Class<?> type, String[] ctrParamNames)
    {
        this.type = type;
        this.ctrParamNames = ctrParamNames;
        initCtr();
    }

    public DefaultConverter(String cls, String[] ctrParamNames)
    {
        this(cls, ctrParamNames, null);
    }

    public DefaultConverter(String cls, String[] ctrParamNames,
            Class<?>[] ctrParamTypes)
    {
        try
        {
            this.type = Class.forName(cls);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Can't create DefaultConverter " + ex);
        }
        this.ctrParamNames = ctrParamNames;
        this.ctrParamTypes = ctrParamTypes;
        initCtr();
    }

    public Method getFactoryCtr()
    {
        return factoryMethod;
    }

    public void setFactoryCtr(Class<?> cls, String method,
            String[] ctrParamNames, Class<?>[] ctrParamTypes)
    {
        this.ctrParamNames = ctrParamNames;
        this.ctrParamTypes = ctrParamTypes;
        try
        {
            initCtrTypes(type);
            factoryMethod = type.getMethod(method, ctrParamTypes);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(
                    "Unable to construct converter for cls: " + type.getName());
        }
    }

    private void initCtr()
    {
        if (ctrParamNames == null) return;
        try
        {
            // System.out.println("DefaultConverter-init: " + type.getName());
            initCtrTypes(type);
            ctr = type.getDeclaredConstructor(ctrParamTypes);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(
                    "Unable to construct converter for cls: " + type.getName(),
                    ex);
        }
    }

    private void initCtrTypes(Class<?> type)
    {
        Map<String, Class<?>> super_map = MetaData.getConverter(
                type.getSuperclass()).getSlots();
        for (int i = 0; i < ctrParamNames.length; i++)
        {
            // System.out.println("DefC-pbpb: " + type + ":" + ctrParamNames[i]
            // +
            // ":" + ctrParamTypes[i]);
            PropertyDescriptor pd = BonesOfBeans.getPropertyDescriptor(type,
                    ctrParamNames[i]);
            if (ctrParamTypes == null) ctrParamTypes = new Class[ctrParamNames.length];
            if (isAcceptable(type, pd))
            {
                if (ctrParamTypes[i] == null) ctrParamTypes[i] = pd
                        .getPropertyType();
                continue;
            }
            if (super_map.get(ctrParamNames[i]) != null)
            {
                if (ctrParamTypes[i] == null) ctrParamTypes[i] = super_map
                        .get(ctrParamNames[i]);
                continue;
            }
            if (ctrParamTypes[i] == null) ctrParamTypes[i] = RefUtils.getType(
                    type, ctrParamNames[i]);
            map.put(ctrParamNames[i], ctrParamTypes[i]);
            // System.out.println("DefC-pbpb: " + type + ":" + ctrParamNames[i]
            // +
            // ":" + pd + ":" + ctrParamTypes[i]);
            if (ctrParamTypes[i] == null) throw new HGException(
                    "Unable to resolve field: " + ctrParamNames[i]
                            + " for class: " + type.getName());
            // System.out.println("DefaultConverter-pbpb: " + ctrParamNames[i] +
            // ":" + cls);
        }
    }

    Map<String, Class<?>> cachedSlots;

    /*
     * (non-Javadoc)
     * 
     * @see seco.notebook.storage.swing.Converter#getSlots()
     */
    public Map<String, Class<?>> getSlots()
    {
        // if (cachedSlots != null)
        // return cachedSlots;
        cachedSlots = new HashMap<String, Class<?>>();
        Class<?> t = type.getSuperclass();
        if (t != null)
        {
            Map<String, Class<?>> inner1 = MetaData.getConverter(t).getSlots();
            if (inner1 != null) for (String key : inner1.keySet())
                cachedSlots.put(key, inner1.get(key));
        }
        BeanInfo info = MetaData.getBeanInfo(type);
        // Properties
        PropertyDescriptor[] pd = info.getPropertyDescriptors();
        for (int i = 0; i < pd.length; ++i)
            if (isAcceptable(type, pd[i])) cachedSlots.put(pd[i].getName(),
                    pd[i].getPropertyType());
        // System.out.println("Props: " + type + ":" + cachedSlots);
        // Public fields
        Field[] fs = type.getFields();
        for (Field f : fs)
            if (Modifier.isPublic(f.getModifiers())
                    && !Modifier.isStatic(f.getModifiers())) cachedSlots.put(f
                    .getName(), f.getType());
        // EventListeners
        EventSetDescriptor[] eventSetDescriptors = info
                .getEventSetDescriptors();
        for (int e = 0; e < eventSetDescriptors.length; e++)
        {
            EventSetDescriptor d = eventSetDescriptors[e];
            Class<?> listenerType = d.getListenerType();
            cachedSlots.put(d.getName() + LISTENERS_KEY, listenerType);
        }

        Map<String, Class> inner = getAuxSlots();
        if (inner != null) for (String key : inner.keySet())
            cachedSlots.put(key, inner.get(key));
        Set<AddOnType> adds = getAllAddOnFields();
        if (adds != null) for (AddOnType a : adds)
            for (int i = 0; i < a.getArgs().length; i++)
                if (!cachedSlots.containsKey(a.getArgs()[i]))
                {
                    if (!cachedSlots.containsKey(a.getArgs()[i]))
                    {
                        if (a.getTypes() != null) cachedSlots.put(
                                a.getArgs()[i], a.getTypes()[i]);
                        else
                            cachedSlots.put(a.getArgs()[i], RefUtils.getType(
                                    type, a.getArgs()[i]));
                    }
                }
        Collection<String> props = MetaData.getTransientProperties(type
                .getName());
        if (props != null) for (String s : props)
            cachedSlots.remove(s);
        return cachedSlots;
    }

    protected Map<String, Class> getAuxSlots()
    {
        return map;
    }

    public Object instantiate(Map<String, Object> props)
    {
        // System.out.println(type.getName() + ":" + props);
        int nArgs = (ctrParamNames != null) ? ctrParamNames.length : 0;
        Object[] constructorArgs = new Object[nArgs];
        Class[] params = new Class[nArgs];
        for (int i = 0; i < nArgs; i++)
            constructorArgs[i] = props.get(ctrParamNames[i]);
        try
        {
            if (ctr == null) ctr = type.getDeclaredConstructor(params);
            ctr.setAccessible(true);
            return ctr.newInstance(constructorArgs);
        }
        catch (Exception e)
        {
            for (int i = 0; i < params.length; i++)
            {
                if (params[i] == null) System.err.println("NullParam for: "
                        + ctrParamNames[i] + ":" + type + ":" + props);
                params[i] = BonesOfBeans.primitiveEquivalentOf(params[i]);
            }
            try
            {
                ctr = type.getDeclaredConstructor(params);
                ctr.setAccessible(true);
                return ctr.newInstance(constructorArgs);
            }
            catch (Exception ex)
            {
                System.err.println("CTR: " + type + ":" + ex.toString());
                for (int i = 0; i < constructorArgs.length; i++)
                {
                    System.err.println("args: " + constructorArgs[i]);
                }
                return null;
            }
        }
    }

    static boolean isAcceptable(Class<?> type, PropertyDescriptor pd)
    {
        return pd != null && pd.getReadMethod() != null
                && pd.getWriteMethod() != null;
    }

    public Constructor<?> getCtr()
    {
        return ctr;
    }

    public String[] getCtrArgs()
    {
        return ctrParamNames;
    }

    public void setCtr(Constructor<?> ctr)
    {
        this.ctr = ctr;
    }

    public Set<AddOnType> getAddOnFields()
    {
        return null;
    }

    private Set<AddOnType> addons;

    public Set<AddOnType> getAllAddOnFields()
    {
        if (addons != null) return addons;
        addons = new HashSet<AddOnType>();
        Class<?> t = type.getSuperclass();
        if (t != null)
        {
            Set<AddOnType> s = MetaData.getConverter(t).getAllAddOnFields();
            if (s != null) for (AddOnType a : s)
                addons.add(a);
        }
        Set<AddOnType> s = getAddOnFields();
        if (s != null) for (AddOnType a : s)
            addons.add(a);
        return addons;
    }

    public Class<?>[] getCtrTypes()
    {
        return ctrParamTypes;
    }

    public Class<?> getType()
    {
        return type;
    }

    public static interface AddOnType
    {
        public Class<?>[] getTypes();

        public String getName();

        public String[] getArgs();
    }

    static class Add implements AddOnType
    {
        private String relName;
        private Class<?>[] types;
        private String[] args;

        public Add(String relName, String[] args, Class<?>[] types)
        {
            this.relName = relName;
            this.types = types;
            this.args = args;
        }

        public Class<?>[] getTypes()
        {
            return types;
        }

        public String getName()
        {
            return relName;
        }

        public String[] getArgs()
        {
            return args;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(args);
            result = prime * result
                    + ((relName == null) ? 0 : relName.hashCode());
            result = prime * result + Arrays.hashCode(types);
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Add other = (Add) obj;
            if (!Arrays.equals(args, other.args)) return false;
            if (relName == null)
            {
                if (other.relName != null) return false;
            } else if (!relName.equals(other.relName)) return false;
            if (!Arrays.equals(types, other.types)) return false;
            return true;
        }

    }
}
