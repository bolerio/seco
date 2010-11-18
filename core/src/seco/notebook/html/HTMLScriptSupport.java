/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import javax.swing.text.Element;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;

public class HTMLScriptSupport extends ScriptSupport
{
    public HTMLScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
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
