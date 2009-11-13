/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import java.util.LinkedList;
import java.util.List;

import seco.notebook.AppConfig;
import seco.notebook.ruby.JRubyScriptEngine;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;
import bsh.BshAst;

public class BshScriptSupport extends ScriptSupport
{
	static final String MODE_NAME = "java";
	static final String ENGINE_NAME = "beanshell";
	private static Formatter formatter;
	private static CompletionProvider[] providers = 
		new CompletionProvider[]{
		      new BshCompletionProvider()};
	private static List<Mode> modes = new LinkedList<Mode>();
	static{
		 modes.add(new Mode(MODE_NAME,"/modes/java.xml"));
         modes.add(new Mode("xml","/modes/xml.xml"));
	}
	
	public BshScriptSupport()
    {
        super();
    }
	
	public Formatter getFormatter()
	{
		if(formatter == null){
			formatter = new JavaFormatter((JavaFormatterOptions)
					AppConfig.getInstance().getProperty(
							AppConfig.FORMATTER_OPTIONS, new JavaFormatterOptions()));
		}
		return formatter;
	}
	
    public void resetFormatter(){
    	formatter = null;
	}
	
	public String getScriptEngineName()
	{
		return ENGINE_NAME;
	}
	
	public String getModeName()
	{
		return MODE_NAME;
	}
	
	public List<Mode> getModes()
	{
		return modes;
	}
	
	public NBParser getParser()
	{
		if(parser == null)
			parser = new BshAst(this);
		return parser;
	}
	
	public CompletionProvider[] getCompletionProviders()
	{
		return providers;
	}
}


