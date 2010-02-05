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

import javax.swing.text.Element;

import seco.notebook.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;

public class BshScriptSupport extends ScriptSupport
{
	private static CompletionProvider[] providers = 
        new CompletionProvider[]{
              new BshCompletionProvider()};
	
	public BshScriptSupport(ScriptSupportFactory factory, Element el) 
    {
        super(factory, el);
    }

	private static Formatter formatter;
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


