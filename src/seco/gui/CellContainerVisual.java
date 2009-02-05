package seco.gui;

import java.awt.Rectangle;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class CellContainerVisual implements CellVisual
{
	private static final HGPersistentHandle handle = 
		HGHandleFactory.makeHandle("cc88ae4c-f70b-4536-814c-95a6ac6a7b62");
	
	public static HGPersistentHandle getHandle()
	{
		return handle;
	}
	
    public void bind(CellGroupMember element, Object parentVisual)
    {
        CellGroup group = (CellGroup) element;
        PiccoloCanvas canvas = null;
        //container canvas....
        if (parentVisual == null) // top container
        {
            canvas = TopFrame.getInstance().getCanvas();
            Rectangle r = (Rectangle) element.getAttribute(VisualAttribs.rect);
            if(r != null) 
                TopFrame.getInstance().setBounds(r);
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
            else //TODO:??
                new JComponentVisual().bind(x, canvas);
        }        
    }
}
