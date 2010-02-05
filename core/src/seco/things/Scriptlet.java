/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.things;

import java.io.Serializable;

/**
 * <p>
 * Represents a code snippets in some language. 
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class Scriptlet implements Serializable
{
    private static final long serialVersionUID = -8635998249472782879L;
    
    private String language;
	private String code;
	
	public Scriptlet()
	{		
	}
	
	public Scriptlet(String language, String code)
	{
		this.language = language;
		this.code = code;
	}

	public final String getCode()
	{
		return code;
	}

	public final void setCode(String code)
	{
		this.code = code;
	}

	public final String getLanguage()
	{
		return language;
	}

	public final void setLanguage(String language)
	{
		this.language = language;
	}
	
	public final String toString()
	{
	    return "Scriptlet[[" + language + ", " + code + "]]";
	}
}
