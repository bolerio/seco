/*
 * This file is part of the Seco source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2010 Kobrix Software, Inc.
 */
package seco.langs.python;

import javax.swing.text.Element;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;

public class PythonScriptSupport extends ScriptSupport
{
    private PythonParser parser = null;

    public PythonScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    @Override
    public NBParser getParser()
    {
        if (parser == null) parser = new PythonParser(this);
        return parser;
    }

    private static CompletionProvider[] providers =
        new CompletionProvider[] { new PythonCompletionProvider() };

    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return providers;
    }

}
