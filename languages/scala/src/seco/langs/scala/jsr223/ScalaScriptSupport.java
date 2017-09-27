package seco.langs.scala.jsr223;

import javax.swing.text.Element;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;

public class ScalaScriptSupport extends ScriptSupport
{
	public ScalaScriptSupport(ScriptSupportFactory factory, Element el)
	{
		super(factory, el);
	}

	@Override
	public CompletionProvider[] getCompletionProviders()
	{
		return null;
	}

	@Override
	public NBParser getParser()
	{
		return null;
	}

}
