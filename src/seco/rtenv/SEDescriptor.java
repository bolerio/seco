/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

/**
 * <p>
 * A descriptor bean for a script engine. It holds the engine
 * factory class name, package names of the interpreter and eventually 
 * other dirty, but useful information about a particular scripting engine.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class SEDescriptor
{
	private String language;
    private String factoryClassName;
    private String [] packageNames;
    
    public SEDescriptor()
    {        
    }
    
    public SEDescriptor(String language, String factoryClassName, String [] packageNames)
    {
    	this.language = language;
        this.factoryClassName = factoryClassName;
        this.packageNames = packageNames;
    }
    
    public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getFactoryClassName()
    {
        return factoryClassName;
    }

    public void setFactoryClassName(String factoryClassName)
    {
        this.factoryClassName = factoryClassName;
    }

    public String[] getPackageNames()
    {
        return packageNames;
    }

    public void setPackageNames(String[] packageNames)
    {
        this.packageNames = packageNames;
    }   
}
