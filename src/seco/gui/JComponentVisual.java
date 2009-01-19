package seco.gui;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class JComponentVisual implements CellVisual
{
	private static final HGPersistentHandle handle = 
		HGHandleFactory.makeHandle("f208ef5d-2cc2-41bb-a659-6df359a6c098");
	
	public static HGPersistentHandle getHandle()
	{
		return handle;
	}
	
    public void bind(CellGroupMember element, Object parentVisual)
    {
        Cell cell = (Cell)element;
        JComponent comp = (JComponent)ThisNiche.hg.get(cell.getAtomHandle());
        PiccoloCanvas canvas = (PiccoloCanvas)parentVisual;
        if(canvas != null)
          canvas.addComponent(comp, element);
    }
 
}