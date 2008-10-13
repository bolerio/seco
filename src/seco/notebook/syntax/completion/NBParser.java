/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.completion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.util.RequestProcessor;
import seco.notebook.util.RequestProcessor.Task;


public abstract class NBParser
{
	private /*static*/ final RequestProcessor SCANNING_RP = new RequestProcessor("Scanning Queue");
	//private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	protected ScriptSupport support;
	protected boolean up_to_date = true;
	protected boolean scanning = false;
	
	public NBParser(ScriptSupport support)
	{
		this.support = support;
	}
	
	public abstract ParserRunnable getParserRunnable();
	
	public void insertUpdate(DocumentEvent e)
	{
		update();
	}
	
	public void removeUpdate(DocumentEvent e)
	{
		update();
	}
	
	private Task t;
	public void update()
	{
		up_to_date = false;
		if(!scanning){
			if(t!= null) t.cancel();
		    t = SCANNING_RP.post(getParserRunnable(), 0, Thread.MIN_PRIORITY);
		}
		//if(last_f!=null)
		//	last_f.cancel(true);
		//last_f = executor.submit(getParserRunnable());
	}
	
	//TODO: for testing purposes, remove later
	public JTree getAstTree()
	{
		return null;
	}
	
	public abstract class ParserRunnable implements Runnable
	{
        protected abstract boolean doJob();
		public final void run()
		{
			scanning = true;
			up_to_date = doJob();
			scanning = false;
		}
	}
	
}
