/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import javax.script.*;
import java.util.*;
import org.hypergraphdb.*;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.U;
import seco.notebook.NotebookUI;
import seco.things.Scriptlet;



/**
 * <p>
 * Manages a set of interpreters (a.k.a. script engines) operating within 
 * a runtime context. A given <code>EvaluationContext</code> may be used in
 * several related notebooks. However it is always bound to one and only one
 * <code>RuntimeContext</code>.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class EvaluationContext
{
	private HashMap<String, SEDescriptor> engineFactories = new HashMap<String, SEDescriptor>();
	private HashMap<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();
	private HGHandle ctxHandle;
	private RuntimeContext runtimeContext;
	private ClassLoader loader = null;
        
	public /*private*/ ScriptEngine getEngine(String language)
	{
		ScriptEngine E = engines.get(language);
		if (E == null)
		{
			// Try to find engine and load it.
			SEDescriptor seDesc = engineFactories.get(language);
            if (seDesc != null)
            {
                try
                {
                    SELoader loader = new SELoader(getClassLoader(), 
                                                   seDesc.getPackageNames());
                    //TODO:??? HTML support doesn't have eval engine, so it's
                    //better to return null here instead of throwinng RTE
                    if(seDesc.getFactoryClassName() == null)  return null;
                    Class<?> factoryClass = loader.loadHere(seDesc.getFactoryClassName());
                    ScriptEngineFactory factory = (ScriptEngineFactory)factoryClass.newInstance();
                    E = factory.getScriptEngine();
                    E.setBindings(runtimeContext.getBindings(), ScriptContext.GLOBAL_SCOPE);
                    engines.put(language, E);
                }
                catch (Exception ex)
                {
                    if (! (ex instanceof RuntimeException))
                        throw new RuntimeException(ex);
                    else
                        throw (RuntimeException)ex;
                }
            }
		}	
		if (E == null)
			throw new RuntimeException("Unknown language '" + language + "'");
		else
			return E;
	}

	private void initReflectiveBindings()
	{
        //Add default bindings.        
        getRuntimeContext().getBindings().put("thisContext", this);
        getRuntimeContext().getBindings().put("niche", ThisNiche.getHyperGraph());		
	}
		
	public void onLoad()
	{
		HGSearchResult<HGHandle> startScripts = null;
		try
		{
			HyperGraph niche = ThisNiche.getHyperGraph();
			startScripts = niche.find(hg.and(hg.eq("on-load"), hg.link(ctxHandle)));
			while (startScripts.hasNext())
			{
				HGLink l = (HGLink)niche.get(startScripts.next());
				Scriptlet scriptlet =(Scriptlet)niche.get(l.getTargetAt(1));
				try
				{
				    this.eval(scriptlet.getLanguage(), scriptlet.getCode());
				}
				catch (ScriptException ex)
				{
					ex.printStackTrace(System.err);
				}
			}
		}
		finally
		{
			U.closeNoException(startScripts);
		}
	}
	
	public void onUnload()
	{
		
	}
	
    /**
     * <p>
     * Add support for a language accessible through this context's class loader. Only the
     * name of the <code>ScriptEngineFactory</code> is needed and the list of packages
     * forming the interpreter (a.k.a. <code>ScriptEngine</code>). The latter is necessary in
     * order to ensure class loading isolation b/w different runtime contexts.
     * </p>
     * 
     * @param desc The language descriptor.
     */
    public void addLanguage(SEDescriptor desc)
    {
        engineFactories.put(desc.getLanguage(), desc);
    }
    
    public SEDescriptor getLanguageDescriptor(String language)
    {
        return engineFactories.get(language);
    }
    
    public Iterator<String> getLanguages()
    {
    	return engineFactories.keySet().iterator();
    }
    
    /**
     * <p>Return <code>true</code> if the passed in language has been defined within this 
     * <code>EvaluationContext</code> and <code>false</code> otherwise.</p>.
     */
    public boolean isLanguageDefined(String language)
    {
        return engineFactories.containsKey(language);
    }
    
	public EvaluationContext(HGHandle runtimeContext)
	{
		this.ctxHandle = runtimeContext;
		this.runtimeContext = (RuntimeContext)ThisNiche.getHyperGraph().get(runtimeContext);
		this.initReflectiveBindings();
	}
	
    public HGHandle getRuntimeContextHandle()
    {
        return ThisNiche.getHyperGraph().getHandle(runtimeContext);
    }
    
	public RuntimeContext getRuntimeContext()
	{
		return runtimeContext;
	}	
	
	public Object eval(String language, String expression) throws ScriptException
	{
        //
        // We switch the thread context loader here, temporarily.
        // Eventually, all evaluation should happen in its own thread, separately.
        //
        ClassLoader save = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            ScriptEngine engine = getEngine(language);
            if(engine == null) return null;
            if(NotebookUI.getFocusedNotebookUI() != null)
              runtimeContext.getBindings().put("notebook", NotebookUI.getFocusedNotebookUI());
    		Object result = engine.eval(expression); //, runtimeContext.getBindings());
    		return result;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(save);
        }
	}
	
	public Evaluator getEvaluator(String language)
	{
		final ScriptEngine E = getEngine(language);
		return new Evaluator()
		{
			public Object eval(String expression) throws ScriptException
			{
				return E.eval(expression); //, runtimeContext.getBindings());
			}
		};
	}

    public ClassLoader getClassLoader()
    {
        if (loader == null)
            if (runtimeContext.getClassPath() != null)
                loader = RtU.createClassLoader(runtimeContext);
                   //runtimeContext.getClassPath().makeLoader();
            else
                return getClass().getClassLoader();
        return loader;
    }
    
    /**
     * <p>
     * Flush current state and reload everything from scratch. This is useful, after
     * the class path has changed for example.
     * </p>
     */
    public void reboot()
    {
        this.engines.clear();
        // Save some "out of context", global bindings 
        Object nb = this.runtimeContext.getBindings().get("notebook");
        Object desktop = this.runtimeContext.getBindings().get("desktop");
        Object canvas = this.runtimeContext.getBindings().get("canvas");
        Object frame = this.runtimeContext.getBindings().get("frame");
        
        this.runtimeContext.getBindings().clear();
        this.initReflectiveBindings();
        
        // maybe we need to clear output cells that hold actual objects loaded
        // by the old class loader? hmm, rebooting won't be so straighforward.
        this.runtimeContext.getBindings().put("notebook", nb);
        this.runtimeContext.getBindings().put("desktop", desktop);
        this.runtimeContext.getBindings().put("canvas", canvas);
        this.runtimeContext.getBindings().put("frame", frame);
        
        // If the reboot is triggered through scripting, then we need to make sure
        // not to use the current classloader as the parent of the newly created one.
        // Otherwise, classes will not be really reloaded.
        ClassLoader save = Thread.currentThread().getContextClassLoader();
        if (this.loader == save)
        	Thread.currentThread().setContextClassLoader(save.getParent());
        this.loader = null;        
        onLoad();
        Thread.currentThread().setContextClassLoader(save);
        //ThisNiche.graph.getTypeSystem().setClassLoader(save);
    }
}