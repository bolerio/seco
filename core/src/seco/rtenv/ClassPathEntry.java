/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import java.net.URL;
import java.io.File;

/**
 * <p>
 * Represents a class path entry. The <code>URL</code> string representation is stored
 * in this class. 
 * </p>
 * 
 * @author Borislav Iordanov
 */
public class ClassPathEntry
{
	private String url;
	
	public ClassPathEntry()
	{		
	}
	
	public ClassPathEntry(String url)
	{
		this.url = url;		
	}

    public ClassPathEntry(URL url)
    {
        this.url = url.toString();
    }

    public ClassPathEntry(File f)
    {
        try
        {
            url = f.toURL().toString();
        }
        catch (java.net.MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}	
    
    public String toString()
    {
        return "ClassPathEntry(" + getUrl() + ")";
    }
}
