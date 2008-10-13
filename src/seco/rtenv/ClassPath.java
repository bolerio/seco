/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import java.util.ArrayList;
import java.net.URLClassLoader;
import java.net.URL;

public class ClassPath extends ArrayList<ClassPathEntry>
{
	private static final long serialVersionUID = -1;
	
	/**
	 * <p>Create and return a brand new <code>ClassLoader</code> based
	 * on this <code>ClassPath</code>, using the thread context class loader
	 * as a parent.</p>
	 */
	public ClassLoader makeLoader()
	{
		return makeLoader(Thread.currentThread().getContextClassLoader());
	}
	
	public ClassLoader makeLoader(ClassLoader parent)
	{
		// return makeLoader(new ClassLoader[] { parent } );
		URL [] urls = new URL[size()];
		for (int i = 0; i < urls.length; i++)
			try { urls[i] = new URL(get(i).getUrl()); }
			catch (Throwable t) { throw new RuntimeException(t); }
		return new URLClassLoader(urls, parent);
	}
	
	public ClassLoader makeLoader(ClassLoader [] parents)
	{
		throw new Error("unimplemented");
	}
}
