package seco.rtenv;

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
    public static List<NestedContextLink> getNestedContextLinks(HGHandle par)
    {
        return hg.getAll(ThisNiche.graph, hg.and(hg.type(NestedContextLink.class),
                 hg.orderedLink(new HGHandle[] {par, ThisNiche.graph.getHandleFactory().anyHandle()})));
    }
    
    public static List<RuntimeContext> getNestedContexts(HGHandle par)
    {
        List<RuntimeContext> res = new ArrayList<RuntimeContext>();
        for(NestedContextLink l : getNestedContextLinks(par))
            res.add((RuntimeContext)ThisNiche.graph.get(l.getChild()));
        return res;
    }
    
    public static Collection<RuntimeContext> getAllRtContextsExcept (RuntimeContext ctx)
    {
        List<RuntimeContext> res = getAllRuntimeContexts();
        res.remove(ctx);
        //TODO: inspect descendent ctx and remove them too 
        return res;
    }
    
    public static List<RuntimeContext> getAllRuntimeContexts()
    {
        return hg.getAll(ThisNiche.graph,
                hg.and(hg.type(RuntimeContext.class)));
    }
}
