/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGSystemFlags;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.U;



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
	private HashSet<HGHandle> openedGroupes = new HashSet<HGHandle>(10);
	private Map<String, Object> properties = new HashMap<String, Object>();
	
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
            instance = (AppConfig)hg.getOne(graph, hg.type(AppConfig.class));
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

	public HashSet<HGHandle> getOpenedGroups() 
	{
		return openedGroupes;
	}

	public void setOpenedGroups(HashSet<HGHandle> openedFiles) 
	{
		this.openedGroupes = openedFiles;
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

}
