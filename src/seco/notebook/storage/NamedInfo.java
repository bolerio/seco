/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.storage;

public class NamedInfo
{
	private String name;
	
	public NamedInfo()
	{
	} 
	
	public NamedInfo(String _name) 
	{
		name = _name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String _name) {
		 name = _name;
	}
	
	public boolean equals(Object object) {
		return (object instanceof NamedInfo)
				&& ((NamedInfo) object).getName().equals(getName());
	}
	
	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		return getName();
	}
}
