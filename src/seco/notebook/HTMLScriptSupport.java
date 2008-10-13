/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;



public class HTMLScriptSupport extends ScriptSupport
{
	private static List<Mode> modes = new LinkedList<Mode>();
	static{
		modes.add(new Mode("html","/modes/html.xml"));
		modes.add(new Mode("css","/modes/css.xml"));
	    modes.add(new Mode("javascript","/modes/javascript.xml"));
	}
	
	//private static Formatter formatter;
	//public static final String FORMATTER_OPTIONS = "html_formatter_options";
		
	/*
	public Formatter getFormatter()
	{
		if(formatter == null){
			formatter = new HTMLFormatter();
		}
		return formatter;
	}
	*/
	
	@Override
	public String getScriptEngineName()
	{
		return "html";
	}

	@Override
	public String getModeName()
	{
		return "html";
	}

	@Override
	public List<Mode> getModes()
	{
		return modes;
	}

	@Override
	public NBParser getParser()
	{
		return null;
	}
	
	@Override
	public CompletionProvider[] getCompletionProviders()
	{
		return null;
	}
	
	/*
	private class HTMLFormatter implements Formatter 
	{

		public String format(String code)
		{
			ByteArrayInputStream in = new ByteArrayInputStream(code.getBytes());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
			
			tidy.setRawOut(false); 
			tidy.setQuiet(true);
			tidy.setNumEntities(false);  
			tidy.setIndentAttributes(true);  
			tidy.setIndentContent(true);  
			tidy.setSmartIndent(true); 
			tidy.setXmlOut(false);
			tidy.setXHTML(false);
			tidy.setXmlSpace(false);
			tidy.setCharEncoding(org.w3c.tidy.Configuration.LATIN1);
			tidy.parseDOM(in, out);
			System.out.println("Out: " + out.toString());
			return out.toString();
		}
		
	}
	*/
	
}
