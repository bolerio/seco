package seco.things;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.hypergraphdb.type.Top;

@SuppressWarnings("unchecked")
public class HGClassType extends HGAtomTypeBase implements HGCompositeType
{
    private Set<Pattern> storedClassPatterns = null;
    private HashMap<String, HGProjection> projections = null;
             
    private void initClassPatterns()
    {
        if (storedClassPatterns != null)
            return;

        storedClassPatterns = new HashSet<Pattern>();
        List<NamePattern> L = (List<NamePattern>) (List<?>) hg.getAll(graph, hg
                .type(NamePattern.class));
        for (NamePattern p : L)
            storedClassPatterns.add(Pattern.compile(p.getRegex()));
    }

    private boolean isStored(String name)
    {
        initClassPatterns();
        for (Pattern p : storedClassPatterns)
            if (p.matcher(name).matches())
                return true;
        return false;
    }

    private byte[] getByteCode(Class<?> cl)
    {
        // TODO
        return new byte[0];
    }

    public Object make(HGPersistentHandle handle,
                       LazyRef<HGHandle[]> targetSet, 
                       IncidenceSetRef incidenceSet)
    {
        try
        {
            HGAtomType type = graph.getTypeSystem().getAtomType(ManagedClass.class);
            ManagedClass mClass = (ManagedClass) type.make(handle, targetSet, incidenceSet);
            if(mClass.getCode() != null)
            {
                throw new HGException("THIS IS A TODO.");
            }
            else
            {
                Class c = getPrimitiveType(mClass.getName());
                return c != null ? c : Class.forName(mClass.getName());
            }
        }
        catch (Exception ex)
        {
            throw new HGException(ex);
        }
    }

    public void release(HGPersistentHandle handle)
    {
        if (graph.getStore().getData(handle) == null)
        {
            HGAtomType type = graph.getTypeSystem().getAtomType(ManagedClass.class);
            type.release(handle);
        } 
        else
        {
            HGAtomType type = graph.getTypeSystem().getAtomType(String.class);
            type.release(handle);
        }        
    }

    public HGPersistentHandle store(Object instance)
    {
        Class<?> cl = (Class<?>) instance;
        String name = cl.getName();
        ManagedClass mClass = new ManagedClass();
        mClass.setName(name);
        if (isStored(name))
            mClass.setCode(getByteCode(cl));
        HGAtomType type = graph.getTypeSystem().getAtomType(ManagedClass.class);
        return type.store(mClass);
    }

    private static Class<?> getPrimitiveType(String name)
    {
        if (name.equals("byte"))
            return byte.class;
        if (name.equals("short"))
            return short.class;
        if (name.equals("int"))
            return int.class;
        if (name.equals("long"))
            return long.class;
        if (name.equals("char"))
            return char.class;
        if (name.equals("float"))
            return float.class;
        if (name.equals("double"))
            return double.class;
        if (name.equals("boolean"))
            return boolean.class;
        //if (name.equals("void"))
        //    return void.class;
        return null;
    }

    public Iterator<String> getDimensionNames()
    {
        if (projections == null)
            initProjections();
        return projections.keySet().iterator();
    }

    public HGProjection getProjection(String dimensionName)
    {
        if (projections == null)
            initProjections();
        return projections.get(dimensionName);
    }
    
    public static final String NAME_DIMENSION = "name";
    public static final String CODE_DIMENSION = "code";
    
    private synchronized void initProjections()
    {
        if (projections != null)
            return;
        
        projections = new HashMap<String, HGProjection>();
        projections.put(NAME_DIMENSION, new NameProjection());
        projections.put(CODE_DIMENSION, new CodeProjection());
    }
    
    private final class NameProjection implements HGProjection
    {
        private final int [] layoutPath = new int[] {1};
        
        public String getName() { return NAME_DIMENSION; }
        public HGHandle getType() { return graph.getTypeSystem().getTypeHandle(String.class); }
        public Object project(Object value) { return ((Class)value).getName(); }
        public void inject(Object slot, Object label) { throw new UnsupportedOperationException(); }
        public int [] getLayoutPath() { return layoutPath; } 
    };
    
    private final class CodeProjection implements HGProjection
    {
        private int [] layoutPath = new int[] {0};
        
        public String getName() { return CODE_DIMENSION; }
        public HGHandle getType() { return graph.getTypeSystem().getTypeHandle(Top.class); }       
        public Object project(Object value) { return getByteCode((Class<?>)value); }
        public void inject(Object slot, Object valueType) { throw new UnsupportedOperationException(); }       
        public int [] getLayoutPath() { return layoutPath; } 
    };
}