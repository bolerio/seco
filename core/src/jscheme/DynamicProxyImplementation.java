package jscheme;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jsint.Closure;
import jsint.DynamicEnvironment;
import jsint.Generic;
import jsint.LexicalEnvironment;
import jsint.Pair;
import jsint.Procedure;
import jsint.Scheme;
import jsint.Symbol;

public class DynamicProxyImplementation implements InvocationHandler 
{
	private Class<?> [] interfaces;
	private Map<Method, Generic> methods = new HashMap<Method, Generic>();

	public Object newProxy()
	{
		return java.lang.reflect.Proxy.newProxyInstance(
				DynamicProxyImplementation.class.getClassLoader(),
                interfaces,
                this);		
	}
		
	public DynamicProxyImplementation(Object [] interfaces)
	{
		this.interfaces = new Class<?>[interfaces.length];
		for (int i = 0; i < interfaces.length; i++)
			this.interfaces[i] = (Class<?>)interfaces[i];
	}
	
	public DynamicProxyImplementation(Class<?> [] interfaces)
	{
		this.interfaces = interfaces;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (args == null)
			args = new Object[0];
		if (method.getName().equals("toString") && args.length == 0)
			return "Proxy[" + interfaces + "]";
		else if (method.getName().equals("equals") && args.length == 1)
			return proxy == args[0];
		else if (method.getName().equals("hashCode") && args.length == 0)
			return proxy.hashCode();
		Generic generic = methods.get(method);
		if (generic == null)
			throw new UnsupportedOperationException("Can't find method " + method + " in proxy.");
		try
		{
//			if (method.getName().equals("stopCellEditing"))
//				return generic.apply(args);
//			else
				return generic.apply(args);
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			return null;
		}
	}	
	
	public static Generic genericClosure(Symbol name, 
										 Pair types, 
										 Object lambdaExpression, 
										 Pair variables,
										 Object [] values)
	{
		LexicalEnvironment lexenv = new LexicalEnvironment(variables, values, LexicalEnvironment.NULLENV);
	    Object analyzedCode = Scheme.currentEvaluator().analyze(lambdaExpression,
	    														Scheme.getInteractionEnvironment(),lexenv);
	    Closure cl = (Closure)Scheme.currentEvaluator().execute(analyzedCode,lexenv);
	    for (int i = 0; i < values.length; i++)
	    {
	    	Object code = Scheme.currentEvaluator().analyze(values[i], Scheme.getInteractionEnvironment(),lexenv);	    	
	    	values[i] = Scheme.currentEvaluator().execute(code,lexenv);
	    }
		return Generic.defineMethod(name, types, cl.copy(lexenv));
	}
	
	public void setImplementation(Method method, Generic generic)
	{
		methods.put(method, generic);
	}
	
	public void setImplementation(String methodName, Object [] argumentTypes, Generic generic)
	{
		Class<?> [] types = new Class<?>[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++)
			types[i] = (Class<?>)argumentTypes[i];
		setImplementation(methodName, types, generic);
	}
	
	public void setImplementation(String methodName, Class<?> [] argumentTypes, Generic generic)
	{
		Method m = null;
		for (Class<?> cl : interfaces)
		{
			try
			{
				if ( (m = cl.getMethod(methodName, argumentTypes)) != null)
					setImplementation(m, generic);
			}
			catch (Exception ex)
			{
				throw new RuntimeException(ex);
			}
		}
		if (m == null)
		{
			StringBuilder sb1 = new StringBuilder();
			for (Class<?> cl : argumentTypes)
				sb1.append(cl.getName() + " ");
			StringBuilder sb2 = new StringBuilder();
			for (Class<?> cl : interfaces)
				sb2.append(cl.getName() + " ");			
			throw new RuntimeException("Could not find method '" + methodName + "' with parameters " +
					sb1.toString() + " within any of the interfaces " + sb2.toString());
		}
	}
}