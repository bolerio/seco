/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import seco.ThisNiche;
import seco.boot.NicheManager;
import seco.notebook.syntax.ScriptSupportFactory;

/**
 * <p>
 * A descriptor bean for a script engine. It holds the engine
 * factory class name, package names of the interpreter and eventually 
 * other dirty, but useful information about a particular scripting engine.
 * </p>
 * 
 * <p>
 * The package names are used in the {@link RuntimeContext} class loader to 
 * force defining the classes there and isolate the different the different
 * runtime contexts.
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
    private String editSupportClassName;
    private transient ScriptSupportFactory editSupportFactory;
    
    public SEDescriptor()
    {        
    }
    
    /**
     * @param language The name of the scripting language.
     * @param factoryClassName The name of the class implementing the ScriptEngineFactory interface.
     * @param packageNames Names of packages of the code implementing the script engine: each class loader
     * redefines explicitly classes from those packages.
     * @param editSupportClassName The name of the class implementing the ScriptSupportFactory (for 
     * editing support - syntax highlight and code completion).
     */
    public SEDescriptor(String language, 
                        String factoryClassName, 
                        String [] packageNames, 
                        String editSupportClassName)
    {
    	this.language = language;
        this.factoryClassName = factoryClassName;
        this.packageNames = packageNames;
        this.editSupportClassName = editSupportClassName;
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

    public String getEditSupportClassName()
    {
        return editSupportClassName;
    }

    public void setEditSupportClassName(String editSupportClassName)
    {
        this.editSupportClassName = editSupportClassName;
    }
    
    public ScriptSupportFactory getEditSupportFactory()
    {
        // no need to synchronize here, it's ok if multiple factories get created
        // concurrently, only the last one will remain
        if (editSupportFactory == null)
        {
            try
            {
                if (editSupportClassName != null)
                    editSupportFactory = //(ScriptSupportFactory)ThisNiche.graph.getTypeSystem().loadClass(editSupportClassName).newInstance();
                    		(ScriptSupportFactory)NicheManager.class.getClassLoader().loadClass(editSupportClassName).newInstance();
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return editSupportFactory;
    }
}