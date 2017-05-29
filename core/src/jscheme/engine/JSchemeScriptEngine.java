package jscheme.engine;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jsint.InputPort;
import jsint.Pair;
import jsint.Scheme;
import jsint.Symbol;
import jsint.U;

public class JSchemeScriptEngine extends AbstractScriptEngine
		implements Compilable, Invocable
{
	private JSchemeScriptEngineFactory factory;

	public JSchemeScriptEngine()
	{
		this(null);
	}

	public JSchemeScriptEngine(JSchemeScriptEngineFactory factory)
	{
		this.factory = factory;
	}

	public Object eval(String script, ScriptContext scriptContext)
			throws ScriptException
	{
		return evalSource(script, scriptContext);
	}

	public Object eval(Reader reader, ScriptContext scriptContext)
			throws ScriptException
	{
		return evalSource(reader, scriptContext);
	}

	private Object evalSource(Object source, ScriptContext scriptContext)
			throws ScriptException
	{
		try
		{
			String in = "" + source;
			InputPort port = new InputPort(new StringReader(in));
			StringWriter strWriter = new StringWriter();
			PrintWriter out = new PrintWriter(strWriter);
			for (Object input = port
					.read(); input != InputPort.EOF; input = port.read())
			{
				Object result = evalObject(input);
				U.write(result, out, false);
			}
			out.flush();
			return strWriter.toString();
		}
		catch (Exception ex)
		{
			ScriptException se = new ScriptException(ex.toString());
			throw se;
		}
	}

	private static Object evalObject(Object input)
	{
		Symbol EVAL = Symbol.intern("eval");
		Symbol QUOTE = Symbol.QUOTE;

		Pair to_eval = new Pair(EVAL, new Pair(
				new Pair(QUOTE, new Pair(input, Pair.EMPTY)), Pair.EMPTY));

		return Scheme.eval(to_eval);
	}

	public Bindings createBindings()
	{
		return new SimpleBindings();
	}

	public ScriptEngineFactory getFactory()
	{
		if (this.factory == null)
		{
			this.factory = new JSchemeScriptEngineFactory();
		}
		return this.factory;
	}

	public CompiledScript compile(String script) throws ScriptException
	{
		return compile(new StringReader(script));
	}

	public CompiledScript compile(Reader script) throws ScriptException
	{
		throw new Error("unimplemented");
	}

	public Object invoke(Object thiz, String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		throw new Error("unimplemented");
	}

	public Object invoke(String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		throw new Error("unimplemented");
	}

	public <T> T getInterface(Class<T> clasz)
	{
		throw new Error("unimplemented");
	}

	public <T> T getInterface(Object thiz, Class<T> clasz)
	{
		throw new Error("unimplemented");
	}

	@Override
	public Object invokeMethod(Object thiz, String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invokeFunction(String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		throw new UnsupportedOperationException();
	}
}
