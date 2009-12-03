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
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.And;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.LinkCondition;
import static org.hypergraphdb.HGQuery.hg;
import seco.boot.NicheManager;
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

    public static final HGPersistentHandle NICHE_NAME_HANDLE = HGHandleFactory
            .makeHandle("86a18ae7-391d-11db-b473-e61fbd5cb97a");
    public static final HGPersistentHandle TOP_CONTEXT_HANDLE = HGHandleFactory
            .makeHandle("8e579278-391d-11db-b473-e61fbd5cb97a");
    public static final HGPersistentHandle TOP_CELL_GROUP_HANDLE = HGHandleFactory
            .makeHandle("f00a2f20-e177-11dd-ad8b-0800200c9a66");
   // public static final HGPersistentHandle TABBED_PANE_GROUP_HANDLE = HGHandleFactory
   //         .makeHandle("7b01b680-e186-11dd-ad8b-0800200c9a66");

    // we assume for now that an entity can only belong to a single context
    // this obviously doesn't make much sense "philosophically" ;)
    static HGHandle findContextLink(HGHandle entityHandle)
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

    public static void bindNiche(HyperGraph graph)
    {
        ThisNiche.graph = graph;
        NicheManager.loadPredefinedTypes(graph);
        name = NicheManager.getNicheName(graph);
        graph.freeze(TOP_CONTEXT_HANDLE);
        allContexts.clear();
        topContext = getEvaluationContext(TOP_CONTEXT_HANDLE);
        // TODO  - the following won't work with multiple contexts. This
        // is a very though problem to solve because it's hard to 
        // track in what context exactly code is executing, especially
        // for global threads like the Swing dispatcher thread (well perhaps
        // this is the only global thread with this problem, but it's causing
        // enough headaches already).
        graph.getTypeSystem().setClassLoader(topContext.getClassLoader());  
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
                initEvaluationContext(result);
                allContexts.put(rcContextHandle, result);
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
    	List<SEDescriptor> allLanguages = hg.getAll(graph, hg.type(SEDescriptor.class));
    	for (SEDescriptor desc : allLanguages)
    		ctx.addLanguage(desc);
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
