/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.classloader;

/**
 * 
 * <p>
 * Represents information about a class - more specifically, its name and
 * its bytecode.  
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class ClassInfo
{
	private String name;
	private byte [] code;
	
	public ClassInfo()
	{		
	}
	
	public ClassInfo(String name, byte [] code)
	{
		this.name = name;
		this.code = code;
	}

	public final byte[] getCode()
	{
		return code;
	}

	public final void setCode(byte[] code)
	{
		this.code = code;
	}

	public final String getName()
	{
		return name;
	}

	public final void setName(String name)
	{
		this.name = name;
	}
}
