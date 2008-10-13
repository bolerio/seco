package seco.notebook.ruby;

import java.util.LinkedList;
import java.util.List;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;


public class RubyScriptSupport extends ScriptSupport
{
	private static List<Mode> modes = new LinkedList<Mode>();
	static{
		modes.add(new Mode("ruby","/modes/ruby.xml"));
	}
	
	private static CompletionProvider[] providers = 
		new CompletionProvider[]{
		      new RubyCompletionProvider()};

	
	@Override
	public String getScriptEngineName()
	{
		return "jruby";
	}

	@Override
	public String getModeName()
	{
		return "ruby";
	}

	@Override
	public List<Mode> getModes()
	{
		return modes;
	}

	private RubyParser parser = null; 
	
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
