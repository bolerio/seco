package jscheme.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JSchemeScriptEngineFactory implements ScriptEngineFactory
{
	public String getEngineName()
	{
		return "jscheme";
	}

	public String getEngineVersion()
	{
		return "0.8.3";
	}

	public List<String> getExtensions()
	{
		return extensions;
	}

	public String getLanguageName()
	{
		return "jscheme";
	}

	public String getLanguageVersion()
	{
		return "1.8.2";
	}

	public String getMethodCallSyntax(String obj, String m, String... args)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(obj);
		buf.append('.');
		buf.append(m);
		buf.append('(');
		if (args.length != 0)
		{
			int i = 0;
			while (i < args.length - 1)
			{
				buf.append(args[i] + ", ");
				i++;
			}
			buf.append(args[i]);
		}
		buf.append(')');
		return buf.toString();
	}

	public List<String> getMimeTypes()
	{
		return mimeTypes;
	}

	public List<String> getNames()
	{
		return names;
	}

	public String getOutputStatement(String str)
	{
		return "print('" + str + "')";
	}

	public String getParameter(String key)
	{
		if (key.equals("javax.script.engine"))
		{
			return getEngineName();
		}
		if (key.equals("javax.script.engine_version"))
		{
			return getEngineVersion();
		}
		if (key.equals("javax.script.name"))
		{
			return getEngineName();
		}
		if (key.equals("javax.script.language"))
		{
			return getLanguageName();
		}
		if (key.equals("javax.script.language_version"))
		{
			return getLanguageVersion();
		}
		if (key.equals("THREADING"))
		{
			return "MULTITHREADED";
		}
		return null;
	}

	public String getProgram(String... statements)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < statements.length; i++)
		{
			buf.append(statements[i]);
			buf.append(";\n");
		}
		return buf.toString();
	}

	public ScriptEngine getScriptEngine()
	{
		JSchemeScriptEngine engine = new JSchemeScriptEngine(this);
		return engine;
	}

	private static List<String> names = new ArrayList<String>(2);
	private static List<String> extensions;
	private static List<String> mimeTypes;
	
	static
	{
		names.add("jscheme");
		names = Collections.unmodifiableList(names);
		extensions = new ArrayList<String>(1);
		extensions.add("jscheme");
		extensions = Collections.unmodifiableList(extensions);
		mimeTypes = new ArrayList<String>(0);
	}
}
