package seco.rtenv;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;

public class RtU
{
    public static List<NestedContextLink> getChildContextLinks(HGHandle par)
    {
        return hg.getAll(ThisNiche.graph, hg.and(hg.type(NestedContextLink.class),
                 hg.orderedLink(new HGHandle[] {par, ThisNiche.graph.getHandleFactory().anyHandle()})));
    }
    
    public static List<RuntimeContext> getChildContexts(HGHandle par)
    {
        List<RuntimeContext> res = new ArrayList<RuntimeContext>();
        for(NestedContextLink l : getChildContextLinks(par))
            res.add((RuntimeContext)ThisNiche.graph.get(l.getChild()));
        return res;
    }
    
    public static ClassLoader createClassLoader(RuntimeContext ctx)
    {
        List<RuntimeContext> all = getChildContexts(ThisNiche.handleOf(ctx));
        all.add(ctx);
        Set<URL> path = new HashSet<URL>();
        for(RuntimeContext rc : all)
            for(ClassPathEntry e : rc.getClassPath())
            {
              try
              {
                  path.add(new URL(e.getUrl()));
              }
              catch (Throwable t)
              {
                  throw new RuntimeException(t);
              }
            }
        return new URLClassLoader(path.toArray(new URL[path.size()]), 
                Thread.currentThread().getContextClassLoader());
    }
    
    public static List<NestedContextLink> getParentContextLinks(HGHandle child)
    {
        return hg.getAll(ThisNiche.graph, hg.and(hg.type(NestedContextLink.class),
                 hg.orderedLink(new HGHandle[] {ThisNiche.graph.getHandleFactory().anyHandle(), child})));
    }
    
    public static List<RuntimeContext> getParentContexts(HGHandle par)
    {
        List<RuntimeContext> res = new ArrayList<RuntimeContext>();
        for(NestedContextLink l : getParentContextLinks(par))
            res.add((RuntimeContext)ThisNiche.graph.get(l.getParent()));
        return res;
    }
    
    public static Collection<RuntimeContext> getAllRtContextsExcept (RuntimeContext ctx)
    {
        List<RuntimeContext> all = getAllRuntimeContexts();
        Set<RuntimeContext> visited = new HashSet<RuntimeContext>();
        visited.add(ctx);
        scan_rt_children(ctx, visited);
        scan_rt_parents(ctx, visited);
        List <RuntimeContext> res = new ArrayList <RuntimeContext>();
        for(RuntimeContext rc: all)
            if(!visited.contains(rc))
                res.add(rc);
        return res;
    }
    
    private static void scan_rt_children(RuntimeContext ctx, Set<RuntimeContext> visited)
    {
        List<RuntimeContext> children = getChildContexts(ThisNiche.handleOf(ctx));
        for(RuntimeContext rc : children)
        {
            if(visited.contains(rc)) continue;
            visited.add(rc);
            scan_rt_children(rc, visited);
        }
    }
    
    private static void scan_rt_parents(RuntimeContext ctx, Set<RuntimeContext> visited)
    {
        List<RuntimeContext> children = getParentContexts(ThisNiche.handleOf(ctx));
        for(RuntimeContext rc : children)
        {
            if(visited.contains(rc)) continue;
            visited.add(rc);
            scan_rt_parents(rc, visited);
        }
    }
    
    public static List<RuntimeContext> getAllRuntimeContexts()
    {
        return hg.getAll(ThisNiche.graph,
                hg.and(hg.type(RuntimeContext.class)));
    }
}
