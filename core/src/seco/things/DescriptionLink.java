package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class DescriptionLink extends HGPlainLink   
{

    public DescriptionLink(HGHandle [] targetSet)
    {
        super(targetSet);
    }

    public DescriptionLink(HGHandle cellH, HGHandle descrH)
    {
        super(new HGHandle[] {cellH, descrH});
    }

    @Override
    public int getArity()
    {
       return super.getArity();
    }
    
    public HGHandle getCellHandle()
    {
        return getTargetAt(0);
    }
    
    public HGHandle getDescriptionHandle()
    {
        return getTargetAt(1);
    }

}
