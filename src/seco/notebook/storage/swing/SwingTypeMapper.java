package seco.notebook.storage.swing;

import java.io.Serializable;
import java.lang.reflect.Modifier;

import javax.swing.ImageIcon;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.indexing.HGIndexer;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.JavaObjectMapper;

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
            || mapAsSerializableObject(javaClass)
            || javaClass.getName().startsWith("seco.talk.B"))
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
