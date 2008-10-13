/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.classloader;

import org.hypergraphdb.*;

/**
 * 
 * <p>
 * This is a <code>ClassLoader</code> that retrieves its classes from
 * a HypeGraphDB instances. The type of class atoms is <code>ManagedClass</code>.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class HGLoader extends ClassLoader
{
	private HyperGraph graph;
	
	public HGLoader(HyperGraph graph)
	{
		this.graph = graph;
	}
	
	public Class<?> loadClass(String name, boolean resolve)
	{
		Class ret = null;
		
		return ret;
	}
}
