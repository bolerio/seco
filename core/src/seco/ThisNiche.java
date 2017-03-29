/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.handle.UUIDHandleFactory;
import org.hypergraphdb.query.And;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.LinkCondition;

import seco.boot.NicheManager;
import seco.classloader.AdaptiveClassLoader;
import seco.gui.GUIController;
import seco.gui.PiccoloCanvas;
import seco.gui.PiccoloFrame;
import seco.gui.StandaloneFrame;
import seco.rtenv.ContextLink;
import seco.rtenv.EvaluationContext;
import seco.rtenv.SEDescriptor;

public final class ThisNiche
{
    //
    // Those three global variables are set by the bootstrapping HG listener.
    // 
    static String name = null;
    public static HyperGraph graph = null;
    static EvaluationContext topContext;
    static HashMap<HGHandle, EvaluationContext> allContexts = new HashMap<HGHandle, EvaluationContext>();

    public static String guiControllerClassName = PiccoloFrame.class.getName();
    public static GUIController guiController;
    
    public static final HGPersistentHandle NICHE_NAME_HANDLE = UUIDHandleFactory.I
            .makeHandle("86a18ae7-391d-11db-b473-e61fbd5cb97a");
    public static final HGPersistentHandle TOP_CONTEXT_HANDLE = UUIDHandleFactory.I
            .makeHandle("8e579278-391d-11db-b473-e61fbd5cb97a");
    public static final HGPersistentHandle TOP_CELL_GROUP_HANDLE = UUIDHandleFactory.I
            .makeHandle("f00a2f20-e177-11dd-ad8b-0800200c9a66");
   // public static final HGPersistentHandle TABBED_PANE_GROUP_HANDLE = HGHandleFactory
   //         .makeHandle("7b01b680-e186-11dd-ad8b-0800200c9a66");
    

    // we assume for now that an entity can only belong to a single context
    // this obviously doesn't make much sense "philosophically" ;)
    public static HGHandle findContextLink(HGHandle entityHandle)
    {
        HGSearchResult<HGHandle> rs = null;
        try
        {
            And qc = new And();
            qc.add(new AtomTypeCondition(ContextLink.class));
            qc.add(new LinkCondition(new HGHandle[] { entityHandle }));
            rs = graph.find(qc);
            return rs.hasNext() ? rs.next() : null;
        }
        finally
        {
            U.closeNoException(rs);
        }
    }
    
    public static void initGUIController()
    {
        if(guiController != null) return;
        AdaptiveClassLoader cl = new AdaptiveClassLoader(new java.util.Vector<Object>(), true);
        try
        {
            Class<?> c = cl.loadClass(guiControllerClassName);
            guiController = (GUIController) c.newInstance();
            guiController.initFrame();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            System.exit(-1);
        }
    }
    
    public static PiccoloCanvas getCanvas()
    {
        return ThisNiche.guiController.getCanvas();
    }

    public static void bindNiche(HyperGraph graph)
    {
        ThisNiche.graph = graph;
        NicheManager.loadPredefinedTypes(graph);
        name = NicheManager.getNicheName(graph);
        graph.freeze(TOP_CONTEXT_HANDLE);
        allContexts.clear();
        topContext = getEvaluationContext(TOP_CONTEXT_HANDLE);
    }

    public static HGHandle getContextHandleFor(HGHandle entityHandle)
    {
        HGHandle h = findContextLink(entityHandle);
        return h == null ? TOP_CONTEXT_HANDLE : ((ContextLink) graph.get(h))
                .getContext();
    }

    public static EvaluationContext getContextFor(HGHandle entityHandle)
    {
        return getEvaluationContext(getContextHandleFor(entityHandle));
    }

    public static void setContextFor(HGHandle entityHandle, HGHandle rcContextHandle)
    {
        HGHandle h = findContextLink(entityHandle);
        if (h != null) graph.remove(h);
        graph.add(new ContextLink(entityHandle, rcContextHandle));
    }

    public static EvaluationContext getEvaluationContext(HGHandle rcContextHandle)
    {
        synchronized (allContexts)
        {
            EvaluationContext result = allContexts.get(rcContextHandle);
            if (result == null)
            {
                graph.freeze(rcContextHandle);            
                result = new EvaluationContext(rcContextHandle);
                allContexts.put(rcContextHandle, result);                
                initEvaluationContext(result);                
            }
            return result;
        }
    }

    /**
     * <p>
     * Add scripting engines and default global variables to a newly created
     * evaluation context.
     * </p>
     * 
     * @param ctx
     */
    static void initEvaluationContext(EvaluationContext ctx)
    {
    	List<HGHandle> allLanguages = hg.findAll(graph, hg.type(SEDescriptor.class));
    	for (HGHandle h : allLanguages)
    	{
    		HGHandle languageContext = findContextLink(h);
    		if (languageContext == null)
    			ctx.addLanguage((SEDescriptor)graph.get(h));
    		else
    		{
    			HGHandle ctxHandle = ((ContextLink)graph.get(languageContext)).getContext();
    			if (allContexts.get(ctxHandle) == ctx)
    				ctx.addLanguage((SEDescriptor)graph.get(h));
    		}
    	}
        ctx.onLoad();
    }

    public static EvaluationContext getTopContext()
    {
        return topContext;
    }

    public static HyperGraph getHyperGraph()
    {
        return graph;
    }

    public static String getName()
    {
        return name;
    }

    public static HGHandle handleOf(Object atom)
    {
        return graph.getHandle(atom);
    }

    // Task execution, keep package private cause it's not clear at this point where
    // this API should go.
    static ExecutorService executorService = Executors.newCachedThreadPool();
}
