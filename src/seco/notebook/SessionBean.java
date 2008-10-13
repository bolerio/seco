/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.HashSet;

public class SessionBean {

    //Opened-Files
	private HashSet<String> openedFiles = new HashSet<String>(10);
	
	public SessionBean() 
	{
		
	}
	
	public HashSet<String> getOpenedFiles() {
		return openedFiles;
	}

	public void setOpenedFiles(HashSet<String> openedFiles) {
		this.openedFiles = openedFiles;
	}
}
