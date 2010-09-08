/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.storage;

import java.io.File;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGIndexManager;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDHandleFactory;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.query.AtomTypeCondition;
import seco.U;
import seco.notebook.AppConfig;
import seco.notebook.util.RequestProcessor;


public class ClassRepository
{
    public static final String REPOSITORY_NAME = ".secoRepository";
    //Variable that could be set in startup scrpt to bypass the default repository location
    public static final String REPOSITORY_HOME_ENV_VAR = 
        "SECO_CLASS_REPOSITORY_HOME";

    private static final HGPersistentHandle JARS_MAP_HANDLE = UUIDHandleFactory.I.makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    private static final HGPersistentHandle JAVADOC_HANDLE = UUIDHandleFactory.I.makeHandle("b12ecac6-d6d8-4de1-9924-88326993e4e2");

    private static final String PCK_INDEX = "PackageInfo";
    private static final String PCK_NAME_PROP = "name";
    private static final String PCK_FULL_NAME_PROP = "fullName";
    private static final String CLS_INDEX = "ClassInfo";
    private static final String CLS_NAME_PROP = "name";
    private static final String JAR_INDEX = "JarInfo";
    private static final String JAR_PATH_PROP = "path";
    private static HGHandle[] EMPTY_HANDLE_ARRAY = new HGHandle[0];
    private static ClassRepository instance;
    /* private */HyperGraph graph;
    boolean updateInProgress = false;

    private ClassRepository(final HyperGraph hg)
    {
        this.graph = hg;
        // Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        // public void run()
        // {
        // try
        // {
        // if (isUpdateInProgress()) System.out
        // .println("Finishing repository creation."
        // + " Please wait.");
        // while (isUpdateInProgress())
        // Thread.sleep(1000);
        // // hg.close();
        // }
        // catch (Throwable t)
        // {
        // t.printStackTrace(System.err);
        // }
        // }
        // }));
        final String path = System.getProperty("java.home");
        if (path == null) return;
        Thread t = new Thread() {
            public void run()
            {
                updateInProgress = true;
                try
                {
                    createIndexes();
                    addLibDir(
                            new File(new File(path), "lib").getAbsolutePath(),
                            true);
                    addLibDir(new File(AppConfig.getJarDirectory(), "lib")
                            .getAbsolutePath(), false);
                    addJar(new File(AppConfig.getJarDirectory(),
                            "seco.jar").getAbsolutePath(), false);
                    System.out.println("Repository creation finished.");
                }
                finally
                {
                    updateInProgress = false;
                    hg.update(getFinishedJarsMap());
                }
            }
        };
        RequestProcessor.getDefault().post(t, 60000);
    }

    Map<JarInfo, Boolean> jarsMap;

    private Map<JarInfo, Boolean> getFinishedJarsMap()
    {
        if (jarsMap == null)
        {
            jarsMap = (Map<JarInfo, Boolean>) graph.get(JARS_MAP_HANDLE);
            if (jarsMap == null)
            {
                jarsMap = new HashMap<JarInfo, Boolean>();
                graph.define(JARS_MAP_HANDLE, jarsMap);
            }
        }
        return jarsMap;
    }
    
    private Set<PackageInfo> top_packages_cache;
    public Set<PackageInfo> getTopPackages()
    {
        if(top_packages_cache != null)
            return top_packages_cache;
        top_packages_cache = new HashSet<PackageInfo>();
        List<HGHandle> res = hg.findAll(graph, hg.type(JarInfo.class));
        for(HGHandle h: res)
        {
            List<ParentOfLink> links = hg.getAll(graph, 
                    hg.and(hg.type(ParentOfLink.class), hg.incident(h)));
            for(ParentOfLink l: links)
                top_packages_cache.add((PackageInfo)graph.get(l.getTargetAt(1)));
        }
        return top_packages_cache;
    } 

    public static void main(String[] args)
    {
        getInstance();
    }

    public void addLibDir(String dir, boolean lib)
    {
        File[] files = new File(dir).listFiles();
        if (files != null)
            for (int i = 0; i < files.length; i++)
            {
                if (!files[i].isDirectory()
                        && files[i].getName().endsWith(".jar"))
                    addJar(files[i].getAbsolutePath(), lib);
            }
    }

    public void removeJar(String s)
    {
        HGHandle h = lookup(JAR_INDEX, JAR_PATH_PROP, s);
        if (h != null) graph.remove(h);
    }

    private boolean checkJarExistsAndUpToDate(String s)
    {
        HGHandle h = lookup(JAR_INDEX, JAR_PATH_PROP, s);
        if (h != null)
        {
            JarInfo info = (JarInfo) graph.get(h);
            File f = new File(s);
            // System.out.println("Jar: " + s + " present: " +
            // (info.getDate() == f.lastModified()));
            if (info.getDate() != f.lastModified()
                    || !getFinishedJarsMap().containsKey(info))
            {
                ; // hg.remove(h);// TODO: remove the old jar
            }
            return true;
        }
        return false;
    }

    private HGHandle[] findPackage(String s)
    {
        HGSearchResult<HGPersistentHandle> res = null;
        try
        {
            res = lookupAll(PCK_INDEX, PCK_NAME_PROP, s);
            Set<HGHandle> set = new HashSet<HGHandle>();
            while (res.hasNext())
                set.add(res.next());
            return set.toArray(new HGHandle[set.size()]);
        }
        finally
        {
            U.closeNoException(res);
        }
    }

    private HGHandle findPackageByFullName(String s)
    {
        return lookup(PCK_INDEX, PCK_FULL_NAME_PROP, s);
    }

    public JarInfo[] findJars()
    {
        HGHandle[] handles = getJarHandles();
        Set<JarInfo> jars = new HashSet<JarInfo>();
        for (HGHandle h : handles)
            jars.add((JarInfo) graph.get(h));
        return jars.toArray(new JarInfo[jars.size()]);
    }

    private HGHandle[] getJarHandles()
    {
        Set<HGHandle> jars = new HashSet<HGHandle>();
        HGSearchResult<HGPersistentHandle> res = null;
        try
        {
            res = graph.find(new AtomTypeCondition(JarInfo.class));
            while (res.hasNext())
            {
                HGHandle h = (HGHandle) res.next();
                jars.add(h);
            }
            return jars.toArray(new HGHandle[jars.size()]);
        }
        finally
        {
            U.closeNoException(res);
        }
    }

    /**
     * Returns a map of all jars(except J2SE ones) and corresponding JavaDoc
     * paths as defined in repository
     */
    public Map<JarInfo, DocInfo> getJavaDocAssoiciations()
    {
        HGHandle[] handles = getJarHandles();
        Map<JarInfo, DocInfo> res = new HashMap<JarInfo, DocInfo>();
        for (HGHandle h : handles)
        {
            HGHandle docH = getDocInfo(h);
            Object info = (docH != null) ? graph.get(docH) : null;
            // if (info != null && (info instanceof DocInfo))
            // System.out.println("DocInfo: " + ((DocInfo) info).getName()
            // + ":" + "jar: " + ((JarInfo) hg.get(h)).getPath());
            if (info == null || !(info instanceof RtDocInfo))
                res.put((JarInfo) graph.get(h), (DocInfo) info);
        }
        return res;
    }

    public DocInfo getDocInfoForClass(Class<?> cl)
    {
        if (cl == null) return null;
        HGHandle h = getClsHandle(cl.getSimpleName(), false);
        if (h == null || cl.getPackage() == null) return null;
        String pack = cl.getPackage().getName();
        HGHandle[] list = parentOfClass(graph, h);
        for (int j = 0; j < list.length; j++)
        {
            String cls = ((PackageInfo) graph.get(list[j])).getFullName();
            if (cls.equalsIgnoreCase(pack)) return getDocInfoForPackage(pack);
        }
        return null;
    }

    public DocInfo getDocInfoForPackage(String fullName)
    {
        HGHandle packH = findPackageByFullName(fullName);
        if (packH == null) return null;
        HGSearchResult<HGHandle> res = graph.find(HGQuery.hg.and(HGQuery.hg
                .type(JarLink.class), HGQuery.hg.link(packH)));
        try
        {
            if (!res.hasNext()) return null;
            JarLink link = (JarLink) graph.get(res.next());
            // System.out.println("Link: " + link);
            HGHandle docH = getDocInfo(link.getTargetAt(0));
            return (docH != null) ? (DocInfo) graph.get(docH) : null;
        }
        finally
        {
            U.closeNoException(res);
        }
    }

    private HGHandle getDocInfo(HGHandle h)
    {
        HGSearchResult<HGHandle> res = graph.find(new AtomTypeCondition(
                DocLink.class));
        try
        {
            while (res.hasNext())
            {
                DocLink link = (DocLink) graph.get((HGHandle) res.next());
                if (link.getTargetAt(0).equals(h)) return link.getTargetAt(1);
            }
        }
        finally
        {
            U.closeNoException(res);
        }
        return null;
    }

    public void setJavaDocForJar(String jar, String doc)
    {
        HGHandle h = lookup(JAR_INDEX, JAR_PATH_PROP, jar);
        if (h != null)
        {
            // System.out.println(hg.get(h));
            HGHandle d = getDocInfo(h);
            if (d != null) graph.replace(d, new DocInfo(doc));
            else
                graph.add(new DocLink(
                        new HGHandle[] { h, graph.add(new DocInfo(doc)) }));
        }
    }

    public void setJavaDocPath(String doc)
    {
        graph.replace(JAVADOC_HANDLE, new RtDocInfo(doc));
    }

    private HGHandle findTopPackage(HyperGraph hg, String s)
    {
        HGHandle[] hs = findPackage(s);
        for (HGHandle h : hs)
        {
            PackageInfo p = (PackageInfo) hg.get(h);
            if (p.getFullName().startsWith(p.getName())) return h;
        }
        return null;
    }

    private HGHandle[] findSubPackages(String s)
    {
        if (s.indexOf('.') > -1)
            return findSubPackages(findPackageByFullName(s));
        return findSubPackages(findTopPackage(graph, s));
    }

    private HGHandle[] findSubPackages(HGHandle h)
    {
        if (h == null) return EMPTY_HANDLE_ARRAY;
        IncidenceSet subs = graph.getIncidenceSet(h);
        // System.out.println(s + ":" + subs.length);
        Set<HGHandle> res = new HashSet<HGHandle>();
        for (HGHandle ih : subs)
        {
            Object o = graph.get(ih);
            // System.out.println(i + ":" + o);
            if (o instanceof ParentOfLink)
            {
                HGHandle par = graph.getPersistentHandle(((ParentOfLink) o)
                        .getTargetAt(1));
                Object parObject = graph.get(par);
                if (!par.equals(h) && parObject instanceof NamedInfo)
                    res.add(par);
            }
        }
        return res.toArray(new HGHandle[res.size()]);
    }

    // return sub-packages and classes
    public NamedInfo[] findSubElements(String _package)
    {
        HGHandle[] hs = findSubPackages(_package);
        NamedInfo[] packs = new NamedInfo[hs.length];
        // System.out.println("ClassRepository - findSubElements: " +
        // hs.length);
        for (int i = 0; i < hs.length; i++)
            packs[i] = ((NamedInfo) graph.get(hs[i]));
        // Arrays.sort(packs);
        return packs;
    }

    public Class<?>[] findClass(String s)
    {
        if (s.indexOf('.') > 0) s = s.substring(s.lastIndexOf('.') + 1);
        HGHandle h = getClsHandle(s, false);
        if (h == null) return new Class[0];
        String clsName = ((ClassInfo) graph.get(h)).getName();
        // System.out.println(clsName + ":" + h);
        HGHandle[] list = parentOfClass(graph, h);
        Set<Class<?>> set = new HashSet<Class<?>>();
        for (int j = 0; j < list.length; j++)
        {
            String cls = ((PackageInfo) graph.get(list[j])).getFullName() + "."
                    + clsName;
            try
            {
                Class<?> thisClass = Thread.currentThread()
                        .getContextClassLoader().loadClass(cls);
                if (thisClass != null) set.add(thisClass);
            }
            catch (Exception ex)
            {
                System.err.println("Unable to load class: " + cls);
            }
        }
        return set.toArray(new Class[set.size()]);
    }

    private static HGHandle[] parentOfClass(HyperGraph hg, HGHandle h)
    {
        Set<HGHandle> res = new HashSet<HGHandle>();
        IncidenceSet set = hg.getIncidenceSet(h);
        for (HGHandle ih : set)
        {
            Object o = hg.get(ih);
            if (o instanceof ParentOfLink)
            {
                HGHandle par = hg.getPersistentHandle(((ParentOfLink) o)
                        .getTargetAt(0));
                Object parObject = hg.get(par);
                if (!par.equals(h) && parObject instanceof PackageInfo)
                    res.add(par);
            }
        }
        // System.out.println(res.size());
        return res.toArray(new HGHandle[res.size()]);
    }

    public void addJar(String absPath, boolean lib)
    {
        if (checkJarExistsAndUpToDate(absPath)) return;
        n_pack = 0;
        n_cls = 0;
        File f = new File(absPath);
        if (!f.exists())
        {
            System.out.println("*** The file " + absPath + " does not exist.");
            return;
        }
        cache.clear();
        long time = System.currentTimeMillis();
        System.out.println("Creating descriptions for " + absPath);
        try
        {
            JarFile jarFile = new JarFile(absPath);
            JarInfo info = new JarInfo(absPath, f.lastModified());
            HGHandle jarHandle = graph.add(info);
            if (lib)
            {
                graph
                        .add(new DocLink(new HGHandle[] { jarHandle,
                                JAVADOC_HANDLE }));
            }
            //String urlName = "jar:file:/" + absPath;
            // to java virtual style :
            //String virtualURLname = urlName += "!/";
            //URL jarFileURL = new URL(virtualURLname);
            //[] urlList = new URL[] { jarFileURL };
            //URLClassLoader urlClassLoader = new URLClassLoader(urlList);
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements())
            {
                final JarEntry entry = jarEntries.nextElement();
                if (entry.isDirectory()) continue;
                String entryName = entry.getName();
                if (!entryName.endsWith(".class")) continue;
                if (entryName.indexOf('$') > 0) continue;// TODO:
                HGHandle lastP = processPackage(jarHandle, entryName);
                // remove .class ending :
                String classDes = entryName.substring(0, entryName.length() - 6);
                // loading of each class leads to OutOfMemoryExc in some JVM
                // versions, so take the name from file
                int idx = classDes.lastIndexOf('/');
                if(idx == -1)
                    idx = classDes.lastIndexOf('\\');
                if(idx == -1)
                    idx = classDes.lastIndexOf('.');
                String simple = classDes.substring(idx + 1);
                //System.out.println("classDes: " + classDes + " simple: " + simple);
                HGHandle clsH = getClsHandle(simple, true);
                graph.add(new JarLink(new HGHandle[] { jarHandle, clsH }));
                if (lastP != null)
                    graph.add(new ParentOfLink(new HGHandle[] { lastP, clsH }));
            }
            System.out.println("Classes: " + n_cls + "(" + jarFile.size()
                    + ") packs: " + n_pack + " time: "
                    + (System.currentTimeMillis() - time) / 1000);
            getFinishedJarsMap().put(info, true);
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }

    public RtDocInfo getJavaDocPath()
    {
        return (RtDocInfo) graph.get(JAVADOC_HANDLE);
    }

    private static Vector<Set<String>> cache = new Vector<Set<String>>();

    private static boolean isInCache(String s, int level)
    {
        Set<String> set = null;
        if (level < cache.size()) set = cache.get(level);
        if (set != null && set.contains(s)) return true;
        return false;
    }

    private static void putInCache(String s, HGHandle h, int level)
    {
        Set<String> set = null;
        if (level < cache.size()) set = cache.get(level);
        if (set == null)
        {
            set = new HashSet<String>();
            set.add(s);
            cache.add(level, set);
        }
        else
            set.add(s);// , h);
    }

    private static String full_name(String[] packs, int level)
    {
        String res = "";
        for (int i = 0; i <= level; i++)
        {
            res += packs[i];
            if (i <= level - 1) res += ".";
        }
        return res;
    }

    private HGHandle processPackage(HGHandle jarHandle, String name)
    {
        String[] packs = name.split("/");
        // top-level class, no package
        if (packs.length == 1) return null;
        String[] fpacks = new String[packs.length - 1];
        for (int j = 0; j < packs.length - 1; j++)
            fpacks[j] = full_name(packs, j);
        int n = packs.length;
        for (int i = 0; i < n - 1; i++)
        {
            if (isInCache(fpacks[i], i)) continue;
            HGHandle h = getPckHandle(jarHandle, packs[i], fpacks[i], true);
            if (i == 0)
            {
                graph.add(new ParentOfLink(new HGHandle[] { jarHandle, h }));
                putInCache(fpacks[i], h, i);
                continue;
            }
            HGHandle prev = getPckHandle(jarHandle, packs[i - 1],
                    fpacks[i - 1], true);
            IncidenceSet set = graph.getIncidenceSet(prev);
            // System.out.println("contains: " + pathElement + " in " +
            // prevElement + ":" + i);
            if (!set.contains(h))
            {
                graph.add(new ParentOfLink(new HGHandle[] { prev, h }));
            }
            putInCache(fpacks[i], h, i);
        }
        return getPckHandle(jarHandle, packs[n - 2], fpacks[n - 2], true);
    }

    static int n_pack = 0;

    private HGHandle getPckHandle(HGHandle jarHandle, String name,
            String fname, boolean search_db)
    {
        PackageInfo info = new PackageInfo(name, fname);
        HGHandle h = graph.getHandle(info);
        if (h == null && search_db)
        {
            HGRandomAccessResult<HGPersistentHandle> res = null;
            try
            {
                res = lookupAll(PCK_INDEX, PCK_NAME_PROP, name);
                while (res.hasNext())
                {
                    HGHandle resH = (HGHandle) res.next();
                    PackageInfo p = (PackageInfo) graph.get(resH);
                    if (fname.equals(p.getFullName())) return resH;
                }
            }
            finally
            {
                U.closeNoException(res);
            }
        }
        if (h == null)
        {
            h = graph.add(info);
            graph.add(new JarLink(new HGHandle[] { jarHandle, h }));
            // System.out.println("" + n_pack + " Adding package: " + name);
            ++n_pack;
        }
        return h;
    }

    static int n_cls = 0;

    private HGHandle getClsHandle(String name, boolean add_in_db)
    {
        ClassInfo info = new ClassInfo(name);
        HGHandle h = graph.getHandle(info);
        if (h == null)
        {
            h = lookup(CLS_INDEX, CLS_NAME_PROP, name);
            if (h != null) return h;
        }
        if (h == null && add_in_db)
        {
            h = graph.add(info);
            // System.out.println("" + n_cls + " Adding class: " + name);
            ++n_cls;
        }
        return h;
    }

    private HGHandle lookup(String typeAlias, String keyProperty,
            String keyValue)
    {
        // how could this can be null, but such an error was reported....
        HGHandle h = graph.getTypeSystem().getTypeHandle(typeAlias);
        if (h == null) return null;
        HGPersistentHandle typeHandle = graph.getPersistentHandle(h);
        HGIndex<Object, HGPersistentHandle> index = graph.getIndexManager()
                .getIndex(
                        new ByPartIndexer(typeHandle,
                                new String[] { keyProperty }));
        if (index != null) return index.findFirst(keyValue);
        return null;
    }

    private HGRandomAccessResult<HGPersistentHandle> lookupAll(
            String typeAlias, String keyProperty, String keyValue)
    {
        HGPersistentHandle typeHandle = graph.getPersistentHandle(graph
                .getTypeSystem().getTypeHandle(typeAlias));
        HGIndex<Object, HGPersistentHandle> index = graph.getIndexManager()
                .getIndex(
                        new ByPartIndexer(typeHandle,
                                new String[] { keyProperty }));
        if (index != null) return index.find(keyValue);
        return null;
    }

    public static ClassRepository getInstance()
    {
        if (instance == null)
        {
            String repositoryPath = System.getenv().get(REPOSITORY_HOME_ENV_VAR);
            if(repositoryPath == null)
                repositoryPath = new File(new File(U.findUserHome()),
                    REPOSITORY_NAME).getAbsolutePath();
            System.out.println("ClassRepository Path : " + repositoryPath);
            instance = new ClassRepository(HGEnvironment.get(repositoryPath));
        }
        return instance;
    }

    private void createIndexes()
    {
        HGTypeSystem ts = graph.getTypeSystem();
        HGIndexManager im = graph.getIndexManager();
        HGPersistentHandle typeH = graph.getPersistentHandle(ts
                .getTypeHandle(PackageInfo.class));
        // return if already created
        if (!graph.getTypeSystem().findAliases(typeH).isEmpty()) return;

        ts.addAlias(typeH, PCK_INDEX);
        im.register(new ByPartIndexer(typeH, new String[] { PCK_NAME_PROP }));
        im.register(new ByPartIndexer(typeH,
                new String[] { PCK_FULL_NAME_PROP }));
        typeH = graph.getPersistentHandle(ts.getTypeHandle(ClassInfo.class));
        ts.addAlias(typeH, CLS_INDEX);
        im.register(new ByPartIndexer(typeH, new String[] { CLS_NAME_PROP }));
        typeH = graph.getPersistentHandle(ts.getTypeHandle(JarInfo.class));
        ts.addAlias(typeH, JAR_INDEX);
        im.register(new ByPartIndexer(typeH, new String[] { JAR_PATH_PROP }));

        if (graph.get(JAVADOC_HANDLE) == null)
            graph.define(JAVADOC_HANDLE, new RtDocInfo(""));
    }

    public synchronized boolean isUpdateInProgress()
    {
        return updateInProgress;
    }
}