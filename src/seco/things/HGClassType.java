package seco.things;

import java.util.HashSet;
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

@SuppressWarnings("unchecked")
public class HGClassType extends HGAtomTypeBase
{
    private Set<Pattern> storedClassPatterns = null;

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
            LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet)
    {
        try
        {
            if (false)//(graph.getStore().getData(handle) == null)
            {
                HGAtomType type = graph.getTypeSystem().getAtomType(
                        ManagedClass.class);
                ManagedClass mClass = (ManagedClass) type.make(handle,
                        targetSet, incidenceSet);
                // TODO: load class from byte code
                return Class.forName(mClass.getName());
            } else
            {
                HGAtomType type = graph.getTypeSystem().getAtomType(
                        String.class);
                String name = (String) type.make(handle, targetSet,
                        incidenceSet);
                Class c = getPrimitiveType(name);
                return c != null ? c : Class.forName(name);

            }
        }
        catch (Exception ex)
        {
            throw new HGException(ex);
        }
    }

    public void release(HGPersistentHandle handle)
    {
        graph.remove(handle);
    }

    public HGPersistentHandle store(Object instance)
    {
        Class<?> cl = (Class<?>) instance;
        String name = cl.getName();
        if (isStored(name))
        {
            ManagedClass mClass = new ManagedClass();
            mClass.setName(name);
            mClass.setCode(getByteCode(cl));
            HGAtomType type = graph.getTypeSystem().getAtomType(
                    ManagedClass.class);
            return type.store(mClass);
        } else
        {
            HGAtomType type = graph.getTypeSystem().getAtomType(String.class);
            return type.store(name);
        }
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
        //throw new IllegalArgumentException();
    }

}
