/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import java.util.*;
import java.io.InputStream;

/**
 * <p>
 * A special purpose class loader for loading the classes of a specific scripting engine.
 * Due to the lack of a class loading API in the javax.script package, we are forced
 * to provide isolation between RuntimeContexts by loading a separate instance of a
 * script engine (i.e. a separate instance of its classes)  
 * </p>
 * 
 * @author Borislav Iordanov
 */
public class SELoader extends ClassLoader
{
	private ClassLoader parent; // this is usually the RuntimeContext class loader.
	private Set<String> packages = new HashSet<String>(); // a list of packages for class to be explicitely defined here.
	
	private Class<?> defineIt(String className)
	{
		String resourceName = className.replace('.', '/') + ".class";
		InputStream in = parent.getResourceAsStream(resourceName);
		if (in == null)
			return null;
		try
		{
			byte data [] = new byte[2*1024];
			int total = 0, read = 0;
			while (true)
			{
				read = in.read(data, total, data.length - total);
                if (read <= 0)
                    break;
				total += read;
				if (total == data.length && read > 0)
				{
					// we need to grow the buffer
					byte [] growing = new byte[data.length * 2];
					System.arraycopy(data, 0, growing, 0, data.length);
					data = growing;
				}
			}
			return defineClass(className, data, 0, total);
		}
		catch (Exception ex)
		{
			throw new Error("Failed to load class '" + className + "'", ex);
		}
	}
	
	/**
	 * <p>
	 * 
	 * </p>
	 * @param parent
	 * @param packageNames
	 */
	public SELoader(ClassLoader delegateTo, String [] packageNames)
	{ 
        super(delegateTo);
		if (delegateTo != null)
			parent = delegateTo;
		else
			parent = getClass().getClassLoader();
		for (int i = 0; i < packageNames.length; i++)
			packages.add(packageNames[i]);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		Class<?> result = findLoadedClass(name);
		if (result != null)
			return result;
		else
		{
            int idx = name.lastIndexOf('.');
			String packageName = idx < 0 ? "" : name.substring(0, idx);
			if (packages.contains(packageName))
				result = defineIt(name);
			else
				result = parent.loadClass(name);
		}
		if (result != null)
			return result;
		else
			throw new ClassNotFoundException(name);
	}
    
    public Class<?> loadHere(String name) throws ClassNotFoundException
    {
        Class<?> result = findLoadedClass(name);
        if (result != null)
            return result;
        else
            result = defineIt(name);
        if (result == null)
            throw new ClassNotFoundException(name);
        else
            return result;
    }
}