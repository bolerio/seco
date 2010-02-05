/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.storage;


public class PackageInfo extends NamedInfo
{
	private String fullName;

	public PackageInfo(){}
	
	public PackageInfo(String _name, String _fullName) 
	{
		super(_name);
		fullName = _fullName;
	}

	public boolean equals(Object object) {
		return (object instanceof PackageInfo)
				&& ((PackageInfo) object).getName().equals(getName())
				&& ((PackageInfo) object).getFullName().equals(getFullName());
	}
	
	public int hashCode() {
		return getName().hashCode() + getFullName().hashCode();
	}

	public String toString() {
		return getName();
	}

	public String getFullName()
	{
		return fullName;
	}

	public void setFullName(String _fullName)
	{
		fullName = _fullName;
	}
	
}
