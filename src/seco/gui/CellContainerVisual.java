package seco.gui;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.PiccoloCanvas;
import seco.notebook.PiccoloFrame;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class CellContainerVisual implements CellVisual
{
    public void bind(CellGroupMember element, Object parentVisual)
    {
        CellGroup group = (CellGroup) element;
        PiccoloCanvas canvas = null;
        //container canvas....
        if (parentVisual == null) // top container
        {
            canvas = PiccoloFrame.getInstance().getCanvas();
        }
        else
        {            
            //TODO:  
        }
        
        for (int i = 0; i < group.getArity(); i++)                
        {
            HGHandle h = group.getTargetAt(i);
            CellGroupMember x = ThisNiche.hg.get(h);
            if(x.getVisual() != null)
            {
            // find visual for member:
            CellVisual visual = ThisNiche.hg.get(x.getVisual());
            
            // what about dimensions, position etc...???
            visual.bind(x, canvas);
            } 
            else
                ;
        }        
    }
}
