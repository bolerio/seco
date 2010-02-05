package seco.langs.ruby;

import javax.swing.text.Element;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;


public class RubyScriptSupport extends ScriptSupport
{
	private static CompletionProvider[] providers = 
		new CompletionProvider[]{
		      new RubyCompletionProvider()};

	private RubyParser parser = null; 
	
	
	public RubyScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    @Override
	public NBParser getParser()
	{
		if(parser == null){
			parser = new RubyParser(this);
		}
		return parser;
	}
	
	@Override
	public CompletionProvider[] getCompletionProviders()
	{
		return providers;
	}
	
}
