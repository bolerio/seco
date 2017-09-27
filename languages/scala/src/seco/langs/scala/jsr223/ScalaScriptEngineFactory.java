package seco.langs.scala.jsr223;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Collections;
import scala.tools.nsc.interpreter.Scripted;

public class ScalaScriptEngineFactory implements ScriptEngineFactory
{
	private ScriptEngineFactory delegate = Scripted.apply$default$1();
	
	@Override
	public String getEngineName()
	{
		//return Scripted.ENGINE;
		return delegate.getEngineName();
	}

	@Override
	public String getEngineVersion()
	{
		//return Scripted.ENGINE_VERSION;
		return delegate.getEngineVersion();
	}

	@Override
	public List<String> getExtensions()
	{
		//return null;
		return delegate.getExtensions();
	}

	@Override
	public List<String> getMimeTypes()
	{
		//return null;
		return delegate.getMimeTypes();
	}

	@Override
	public List<String> getNames()
	{
		//return Collections.singletonList(Scripted.NAME);
		return delegate.getNames();
	}

	@Override
	public String getLanguageName()
	{
		//return Scripted.LANGUAGE;
		return delegate.getLanguageName();
	}

	@Override
	public String getLanguageVersion()
	{
		//return Scripted.LANGUAGE_VERSION;
		return delegate.getLanguageVersion();
	}

	@Override
	public Object getParameter(String key)
	{
		// TODO
		//return null;
		return delegate.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args)
	{
		// TODO Auto-generated method stub
		//return null;
		return delegate.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(String toDisplay)
	{
		// TODO Auto-generated method stub
		//return null;
		return delegate.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(String... statements)
	{
		// TODO Auto-generated method stub
		//return null;
		return delegate.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine()
	{
		return delegate.getScriptEngine();
	}
}
