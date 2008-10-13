/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import javax.script.Bindings;
import javax.script.SimpleBindings;

/**
 * <p>
 * A <code>RuntimeContext</code> holds the necessary information to construct an 
 * <code>EvaluationContext</code> object. 
 * </p> 
 */
public class RuntimeContext
{
	// the state of the runtime context
	private String name;
	private ClassPath classPath = new ClassPath();
	private Bindings bindings = new SimpleBindings();
	
    public RuntimeContext()
    {        
    }
    
    public RuntimeContext(String name)
    {
        this.name = name;
    }
    
	public ClassPath getClassPath()
	{
		return classPath;
	}
	public void setClassPath(ClassPath classPath)
	{
		this.classPath = classPath;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Bindings getBindings()
	{
		return bindings;
	}
	//this method name is needed to prevent bindings persistence
	public void setBindingsEx(Bindings bindings)
	{
		this.bindings = bindings;
	}
}
