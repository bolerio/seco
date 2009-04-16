/**
 * 
 */
package seco.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.things.CellGroupMember;

import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public class PSwingNode extends PSwing
{
	private static final long serialVersionUID = 4732523747800268384L;
	public boolean deleteable;
    private HGHandle handle; 

    public PSwingNode(PSwingCanvas canvas, JComponent component,
            HGHandle handle)
    {
        super(component);
        this.handle = handle;
    }

    public PSwingNode(PSwingCanvas canvas, JComponent component)
    {
        super(component);
    }

    public boolean isDeleteable()
    {
        return deleteable;
    }

    
    @Override
    public boolean setBounds(double x, double y, double width, double height)
    {
        boolean b = super.setBounds(x, y, width, height);
        getComponent().setPreferredSize(
                new Dimension((int) width, (int) height));
        return b;
    }
    
    public void endResizeBounds() 
    {
       // System.out.println("PSwingNode: endResizeBounds");
        getComponent().revalidate();
        if(handle == null || ThisNiche.hg == null) return;
        CellGroupMember cm = (CellGroupMember) ThisNiche.hg.get(getHandle());
        if (cm != null)
            cm.setAttribute(VisualAttribs.rect, getFullBounds().getBounds());
    }

    public HGHandle getHandle()
    {
        return handle;
    }

    public void setHandle(HGHandle handle)
    {
        this.handle = handle;
    }

    @Override
    public String toString()
    {
        return "PSwing0: " + getComponent();
    }

}