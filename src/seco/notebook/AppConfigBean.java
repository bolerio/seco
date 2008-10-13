/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.HashSet;

public class AppConfigBean {

	 //Most-Recently-Used-Dir
	private String mrud = null;
    //Most-Recently-Used-Files
	private HashSet<String> mrufs = new HashSet<String>(10);
	private SessionBean session = new SessionBean();
	
	public String getMRUD()
	{
		return mrud;
	}

	public void setMRUD(String f)
	{
		mrud = f;
	}
	
	public HashSet<String> getMRUF()
	{
		return mrufs;
	}
	
	public void setMRUF(HashSet<String> m)
	{
		mrufs = m;
	}

	public SessionBean getSession() {
		return session;
	}

	public void setSession(SessionBean session) {
		this.session = session;
	}
}
