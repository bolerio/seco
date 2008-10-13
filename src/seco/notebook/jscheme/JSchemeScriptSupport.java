/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.jscheme;

import java.util.LinkedList;
import java.util.List;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;


public class JSchemeScriptSupport extends ScriptSupport
{
	private static List<Mode> modes = new LinkedList<Mode>();
	static{
		modes.add(new Mode("scheme","/modes/scheme.xml"));
	}
	
	@Override
	public String getScriptEngineName()
	{
		return "jscheme";
	}

	@Override
	public String getModeName()
	{
		return "scheme";
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
	
}
