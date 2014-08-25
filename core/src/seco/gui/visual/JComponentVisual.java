package seco.gui.visual;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.things.NotSerializableValue;

public class JComponentVisual implements CellVisual
{
	private static final HGPersistentHandle handle = 
	    UUIDHandleFactory.I.makeHandle("f208ef5d-2cc2-41bb-a659-6df359a6c098");
	
	public static HGPersistentHandle getHandle()
	{
		return handle;
	}
	
    public JComponent bind(CellGroupMember element)
    {
        if(CellUtils.isMinimized(element))
            return GUIHelper.getMinimizedUI(element);
        
        Cell cell = (Cell)element;
        //if(!(ThisNiche.hg.get(cell.getAtomHandle()) instanceof JComponent))
        //    return null;
        Object o = ThisNiche.graph.get(cell.getAtomHandle());
        JComponent comp = null;
        if(o instanceof JComponent) 
            comp = (JComponent) o;
        else if (o instanceof NotSerializableValue)
        {
        	NotSerializableValue nsv = (NotSerializableValue)o;
        	if (nsv.initialized())
        	{
        		if (nsv.value() instanceof JComponent)
        			comp = (JComponent)nsv.value();
        		else
        		{
    	           JTextArea area = new JTextArea("" + nsv.value());
    	           area.setEditable(false);
    	           //area.setWsetWrapStyleWord(true);
    	           area.setBackground(Color.white);
    	           comp = area;        			
        		}
        	}
        	else
        		comp = new JLabel("Not available, please evaluate source cell.");        
        }
        else
        {
           JTextArea area = new JTextArea("" + o);
           area.setEditable(false);
           //area.setWsetWrapStyleWord(true);
           area.setBackground(Color.white);
           comp = area;
        }
        
        return comp;
    }
}