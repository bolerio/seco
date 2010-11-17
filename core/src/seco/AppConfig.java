/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

import seco.gui.StandaloneFrame;



public class AppConfig
{
	private static AppConfig instance;
	private static HyperGraph graph;
	public static final String SPACES_PER_TAB = "SPACES_PER_TAB";
	public static final String FORMATTER_OPTIONS = "java_formatter_options";


	 //Most-Recently-Used-Dir
	private String mrud = null;
    //Most-Recently-Used-Files
	private HashSet<HGHandle> mrufs = new HashSet<HGHandle>(10);
	private Map<String, Object> properties = new HashMap<String, Object>();
    static URLClassLoader classLoader;
    // dir for additional jars/plugins
    static final String EXT_DIR = "lib";
	
	public AppConfig()
	{
		if (instance != null)
			throw new RuntimeException("Can't construct AppConfig twice...it's a singleton.");
	}

	public static AppConfig getInstance()
	{
		if (instance == null)
		{
            graph = ThisNiche.getHyperGraph();
            instance = hg.getOne(graph, hg.type(AppConfig.class));
            if (instance == null)
            {
            	instance = new AppConfig();
            	graph.add(instance); //, HGSystemFlags.MUTABLE);
            }
		}
		return instance;
	}

	public String getMRUD()
	{
		return mrud;
	}

	public void setMRUD(String f)
	{
		mrud = f;
	}
	
	public HashSet<HGHandle> getMRUF()
	{
		return mrufs;
	}
	
	public void setMRUF(HashSet<HGHandle> m)
	{
		mrufs = m;
	}

	public Object getProperty(String key)
	{
		return properties.get(key);
	}

	public void removeProperty(String key)
	{
		properties.remove(key);
	}

	public Object getProperty(String key, Object def)
	{
		if (properties.containsKey(key)) 
			 return properties.get(key);
		properties.put(key, def);
		return def;
	}

	public void setProperty(String key, Object def)
	{
		properties.put(key, def);
		graph.update(this);
	}
	
	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public static File getJarDirectory()
	{
	    return getJarDirectory(AppConfig.class);
	}
	
	//if cls is null, AppConfig is used 
    public static File getJarDirectory(Class<?> class_from_the_jar)
    {
        if(class_from_the_jar == null)
            class_from_the_jar = AppConfig.class;
        try
        {
            CodeSource cs = class_from_the_jar.getProtectionDomain().getCodeSource();
            URL url = null;
            if (cs != null)
            {
                url = cs.getLocation();
                if (url == null)
                {
                    // Try to find 'cls' definition as a resource; this is not
                    // documented to be legal, but Sun's implementations seem to
                    // allow this:
                    final ClassLoader clsLoader = class_from_the_jar.getClassLoader();
                    final String clsAsResource = class_from_the_jar.getName()
                            .replace('.', '/').concat(".class");
                    url = clsLoader != null ? clsLoader
                            .getResource(clsAsResource) : ClassLoader
                            .getSystemResource(clsAsResource);
                }
            }
            if (url != null)
            {
                // System.out.println("Self: " + url.getPath());
                return (new File(url.getPath())).getParentFile();
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(
                    "Unable to find installation directory:", ex);
        }
        return null;
    }

    public static URLClassLoader getClassLoader()
    {
        if (AppConfig.classLoader == null)
        {
            // plugins
            Set<URL> pluginURLs = new HashSet<URL>();
            File[] files = (new File(AppConfig.getJarDirectory(AppConfig.class), AppConfig.EXT_DIR))
                    .listFiles();
            if (files != null) for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory()
                        || !files[i].getName().endsWith(".jar")) continue;
                String plugin = AppConfig.EXT_DIR + "/" + files[i].getName();
                URL url;
                try
                {
                    url = new URL("file", "", plugin);
                    pluginURLs.add(url);
                }
                catch (Exception ue)
                {
                    System.err.println("Jar: " + files[i].getAbsolutePath()
                            + "was not a valid URL");
                }
            }
            AppConfig.classLoader = new URLClassLoader(pluginURLs
                    .toArray(new URL[pluginURLs.size()]), StandaloneFrame.class
                    .getClassLoader());
            Thread.currentThread().setContextClassLoader(AppConfig.classLoader);
        }
        return AppConfig.classLoader;
    }

}
