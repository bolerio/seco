package seco.rtenv;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class NestedContextLink extends HGPlainLink
{
    public NestedContextLink(HGHandle parent, HGHandle child)
    {
        super(parent, child);
    }
    
    public HGHandle getParent()
    {
        return getTargetAt(0);
    }
    
    public HGHandle getChild()
    {
        return getTargetAt(1);
    }

    @Override
    public int getArity()
    {
        return 2;
    }
}

