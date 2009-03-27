package seco.gui;

import javax.swing.JComponent;
import javax.swing.JTextArea;

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
	
    public JComponent bind(CellGroupMember element)
    {
        Cell cell = (Cell)element;
        if(!(ThisNiche.hg.get(cell.getAtomHandle()) instanceof JComponent))
            return null;
        Object o = ThisNiche.hg.get(cell.getAtomHandle());
        JComponent comp = null;
        if(o instanceof JComponent) 
            comp = (JComponent) o;
        else
        {
           comp = new JTextArea("" + o);
           ((JTextArea)comp).setEditable(false);
        }
        return comp;
    }
 
}