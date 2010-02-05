package seco.notebook.syntax.completion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seco.notebook.storage.ClassInfo;
import seco.notebook.storage.NamedInfo;
import seco.notebook.storage.PackageInfo;
import seco.notebook.syntax.java.JavaResultItem;

public class CompletionU
{
    //Simple wrapper to query out the package info and simplify class instantiation 
    public static class DBPackageInfo{
    	private NamedInfo[] info;
    	private String pack;
    	public NamedInfo[] getInfo()
    	{
    		return info;
    	}
    	public String getPackage()
    	{
    		return pack;
    	}
    	
    	public DBPackageInfo(NamedInfo[] info, String pack)
    	{
    		this.info = info;
    		this.pack = pack;
    	}
    }

    private CompletionU()
    {
    }

    public static void populatePackage(CompletionResultSet resultSet,
            CompletionU.DBPackageInfo info, int queryCaretOffset)
    {
        resultSet.setTitle(info.getPackage());
        //resultSet.setAnchorOffset(queryAnchorOffset);
        for (NamedInfo p : info.getInfo())
        {
            if (p instanceof PackageInfo)
            {
                JavaResultItem item = new JavaResultItem.PackageResultItem(
                        (PackageInfo) p, false);
                item.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(item);
            } else if (p instanceof ClassInfo)
            {
                try
                {
                    Class<?> cls = Thread.currentThread()
                            .getContextClassLoader().loadClass(
                                    info.getPackage() + "." + p.getName());
                    JavaResultItem item = new JavaResultItem.ClassResultItem(
                            cls, false);
                    item.setSubstituteOffset(queryCaretOffset);
                    resultSet.addItem(item);
                }
                catch (Exception ex)
                {
                    //ex.printStackTrace();
                    //System.err.println("WARNING: " + ex.toString());
                }
            }
        }
    }

    public static void populateClass(CompletionResultSet resultSet,
            Class<?> cls, int modifiers, int queryCaretOffset) 
    {
        resultSet.setTitle(cls.getCanonicalName());
        if (cls.isArray())
        {
            JavaResultItem item = new JavaResultItem.FieldResultItem(
                    "length", Integer.TYPE, Modifier.PUBLIC);
            item.setSubstituteOffset(queryCaretOffset);
            resultSet.addItem(item);
        }
        for (Class<?> c: cls.getDeclaredClasses())
        {
            if(Modifier.isPrivate(c.getModifiers())) continue;
            //anonymous inner classes have empty simple name
            if(c.getSimpleName().length() == 0) continue;
            //System.out.println("BshCompl - inner classes: " + c + ":" + c.getCanonicalName());
            JavaResultItem item = new JavaResultItem.ClassResultItem(
                    c, false);
            item.setSubstituteOffset(queryCaretOffset);
            resultSet.addItem(item);
        }
        for (Field f : CompletionU.getFields(cls, modifiers))
        {
            // when we show the static and private fields some ugly inner
            // members arise too
            if (f.getName().indexOf('$') >= 0) continue;
            JavaResultItem item = new JavaResultItem.FieldResultItem(f, cls);
            item.setSubstituteOffset(queryCaretOffset);
            resultSet.addItem(item);
        }
        for (Method m : CompletionU.getMethods(cls, modifiers))
        {
            if (m.getName().indexOf('$') >= 0) continue;
            JavaResultItem item = new JavaResultItem.MethodItem(m);
            item.setSubstituteOffset(queryCaretOffset);
            resultSet.addItem(item);
        }
    }

    public static Collection<Method> getMethods(Class<?> cls, int modifier)
    {
        Set<Method> set = new HashSet<Method>();
        Method[] ms = cls.getDeclaredMethods();
        for (int i = 0; i < ms.length; i++)
            if (!CompletionU.filterMod(ms[i].getModifiers(), modifier)) set.add(ms[i]);
        ms = cls.getMethods();
        for (int i = 0; i < ms.length; i++)
            if (!CompletionU.filterMod(ms[i].getModifiers(), modifier)) set.add(ms[i]);
        return set;
    }

    public static Collection<Field> getFields(Class<?> cls, int modifier)
    {
        Set<Field> set = new HashSet<Field>();
        Field[] ms = cls.getDeclaredFields();
        for (int i = 0; i < ms.length; i++)
            if (!CompletionU.filterMod(ms[i].getModifiers(), modifier)) set.add(ms[i]);
        ms = cls.getFields();
        for (int i = 0; i < ms.length; i++)
            if (!CompletionU.filterMod(ms[i].getModifiers(), modifier)) 
                set.add(ms[i]);
        return set;
    }

    // needed because there's no package-private modifier,
    // when comp_mod contains Modifier.PRIVATE, we allow
    // everything to pass, otherwise only public members
    private static boolean filterMod(int mod, int comp_mod)
    {
        boolean priv = (comp_mod & Modifier.PRIVATE) != 0;
        boolean stat = (comp_mod & Modifier.STATIC) != 0;
        if (stat && (mod & Modifier.STATIC) == 0) return true;
        if (!priv && (mod & Modifier.PUBLIC) == 0) return true;
        //if (!stat && (mod & Modifier.STATIC) != 0) return true;
        return false;
    }

}
