package seco.langs.groovy.jsr;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.codehaus.groovy.tools.shell.Interpreter;
import org.codehaus.groovy.tools.shell.Parser;
import org.codehaus.groovy.tools.shell.util.ScriptVariableAnalyzer;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

public class ShellLikeGroovyEngine  extends AbstractScriptEngine
{
	GroovyScriptEngineFactory factory;
    Parser parser = null;

    class State {
    	GroovyClassLoader loader;
    	final List<String> imports = new ArrayList<String>();
    	Object lastResult = null;
    	Interpreter interp;
    }
    
    State state(ScriptContext context)
    {
		// TODO - what about thread safety here?
    	State state = (State)context.getAttribute(this.getClass().getName(), ScriptContext.ENGINE_SCOPE);	    
	    if (state == null)
		{
	    	state = new State(); 
	    	state.loader = new GroovyClassLoader(getParentLoader());
	    	state.interp = new Interpreter(state.loader, new GroovyScriptContextBinding(context));
	    	context.setAttribute(this.getClass().getName(), state, ScriptContext.ENGINE_SCOPE);
		}    	
    	return state;
    }
    
    public ShellLikeGroovyEngine(GroovyScriptEngineFactory factory)
    {
    	this.factory = factory;
    	parser = new Parser();    	
    }
    
    public GroovyClassLoader getClassLoader()
    {
    	return new GroovyClassLoader(getParentLoader());
    }    
	
    @Override
	public Object eval(String script, ScriptContext context) throws ScriptException
	{
	    State state = state(context);
	    
        Object result = null;
        String variableBlocks = "";
        // To make groovysh behave more like an interpreter, we need to retrive all bound
        // vars at the end of script execution, and then update them into the groovysh Binding context.
        Set<String> boundVars = ScriptVariableAnalyzer.getBoundVars(script);
        variableBlocks += "$COLLECTED_BOUND_VARS_MAP_VARNAME = new HashMap();";
        if (boundVars != null && !boundVars.isEmpty()) 
        {
            for (String varname : boundVars)
            {
                // bound vars can be in global or some local scope.
                // We discard locally scoped vars by ignoring MissingPropertyException
                variableBlocks += "try {" +
						"$COLLECTED_BOUND_VARS_MAP_VARNAME[\"" + varname + "\"] = " + varname + "; " +
					"} catch (MissingPropertyException e){}";
            }
        }

        // Evaluate the current buffer w/imports and dummy statement
        List<String> buff = new ArrayList<String>();
        buff.addAll(state.imports);
        buff.add("try {");
        buff.add("true");
        buff.add(script);
        buff.add("} finally {");
        buff.add(variableBlocks);
        buff.add("}");
        		
        result = state.interp.evaluate(buff);

        state.interp.getContext().setProperty("_", result);
        		
        @SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, Object> boundVarValues = (Map)state.interp.getContext().getVariable("$COLLECTED_BOUND_VARS_MAP_VARNAME");
        for (Map.Entry<String, Object> e : boundVarValues.entrySet())
        {
        	state.interp.getContext().setVariable(e.getKey(), e.getValue());
        }
        return result;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException
	{
		return eval(readFully(reader), context);
	}

	@Override
	public Bindings createBindings()
	{
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory()
	{
		return factory;
	}

	private ClassLoader getParentLoader()
	{
		// check whether thread context loader can "see" Groovy Script class
		ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Class<?> c = ctxtLoader.loadClass("org.codehaus.groovy.Script");
			if (c == Script.class)
			{
				return ctxtLoader;
			}
		}
		catch (ClassNotFoundException cnfe)
		{
		}
		// exception was thrown or we get wrong class
		return Script.class.getClassLoader();
	}
	
	private String readFully(Reader reader) throws ScriptException
	{
		char[] arr = new char[8 * 1024]; // 8K at a time
		StringBuilder buf = new StringBuilder();
		int numChars;
		try
		{
			while ((numChars = reader.read(arr, 0, arr.length)) > 0)
			{
				buf.append(arr, 0, numChars);
			}
		}
		catch (IOException exp)
		{
			throw new ScriptException(exp);
		}
		return buf.toString();
	}
	
}
