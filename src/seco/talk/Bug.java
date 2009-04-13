package seco.talk;

//TODO: to be removed, usecase for inner class serialization HG bug
//bug = new Bug();
//h = ThisNiche.hg.add(bug);
////dae2eb8e-c334-4711-8714-d26fa0243bc5
//After restart
//h = org.hypergraphdb.HGHandleFactory.makeHandle("dae2eb8e-c334-4711-8714-d26fa0243bc5");
//ThisNiche.hg.get(h);
public class Bug
{
    private InnerStatic inner;

    public Bug()
    {
       inner = new InnerStatic(this);
    }
    
    public InnerStatic getInner()
    {
        return inner;
    }

    public void setInner(InnerStatic inner)
    {
        this.inner = inner;
    }
    
    public static class InnerStatic
    {
        private Bug outer;
        
        public InnerStatic()
        {
        }
        
        public InnerStatic(Bug outer)
        {
           this.outer = outer;
        }

        public Bug getOuter()
        {
            return outer;
        }

        public void setOuter(Bug outer)
        {
            this.outer = outer;
        }
    }
}
