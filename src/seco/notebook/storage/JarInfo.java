/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.storage;

public class JarInfo
{
	private String path;
	private long date;
		
	public JarInfo()
	{
	}
	
	public JarInfo(String path, long date)
	{
		this.path = path;
		this.date = date;
	}

	public long getDate()
	{
		return date;
	}
	
	public void setDate(long date)
	{
		this.date = date;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (date ^ (date >>> 32));
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final JarInfo other = (JarInfo) obj;
        if (date != other.date) return false;
        if (path == null)
        {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        return true;
    }
}
