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
		//if not a jar, assure the ending slash is present
		//otherwise the URLClassloader will ignore this entry
		if(!url.endsWith(".jar") && !url.endsWith("/"))
		    url += "/";
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ClassPathEntry other = (ClassPathEntry) obj;
        if (url == null)
        {
            if (other.url != null) return false;
        }
        else if (!url.equals(other.url)) return false;
        return true;
    }
}
