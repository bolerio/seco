package seco.langs.groovy.jsr;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;

import groovy.lang.Binding;

public class GroovyScriptContextBinding extends Binding
{
	private ScriptContext scriptContext;
	private Bindings globalBindings;
	
	public GroovyScriptContextBinding(ScriptContext scriptContext)
	{
		this.scriptContext = scriptContext;
		this.globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
	}
	
	@Override
	public Object getVariable(String name)
	{
		if (globalBindings.containsKey(name))
			return globalBindings.get(name);
		else
			return super.getVariable(name);
	}

	@Override
	public Map getVariables()
	{
		return globalBindings;
//		// TODO Auto-generated method stub
//		return super.getVariables();
	}

	@Override
	public boolean hasVariable(String name)
	{
		if (globalBindings.containsKey(name))
			return true;
		else
			return super.hasVariable(name);
	}

	@Override
	public void setVariable(String name, Object value)
	{
		globalBindings.put(name, value);
//		super.setVariable(name, value);
	}

	@Override
	public Object getProperty(String name)
	{
		if (globalBindings.containsKey(name))
			return globalBindings.get(name);
		else
			return super.getProperty(name);
	}

	@Override
	public void setProperty(String name, Object value)
	{
		globalBindings.put(name, value);
//		// TODO Auto-generated method stub
//		super.setProperty(arg0, arg1);
	}

	
}
