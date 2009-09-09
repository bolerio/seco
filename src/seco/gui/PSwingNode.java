/**
 * 
 */
package seco.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public class PSwingNode extends PSwing implements Serializable
{
    private static final long serialVersionUID = 4732523747800268384L;
    public boolean deleteable;
    private HGHandle handle;

    public PSwingNode(PSwingCanvas canvas, JComponent component, HGHandle handle)
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
        storeBounds(getFullBounds().getBounds());
    }

    protected void storeBounds(Rectangle r)
    {
        if (handle == null || ThisNiche.hg == null) return;
        CellGroupMember cm = (CellGroupMember) ThisNiche.hg.get(getHandle());
        if (CellUtils.isMaximized(cm)) return;
        if (cm != null)
        {
            if (CellUtils.isMinimized(cm))
            {
                Point pt = (Point) cm.getAttribute(VisualAttribs.minPt);
                if(pt == null) pt = new Point();
                pt.x = r.x;  pt.y = r.y;
                cm.setAttribute(VisualAttribs.minPt, pt);
                return;
            }
            Rectangle old = (Rectangle) cm.getAttribute(VisualAttribs.rect);
            if (r.equals(old)) return;
            cm.setAttribute(VisualAttribs.rect, r);
        }
    }

    private Rectangle last_bounds;
    private boolean last_enabled;

    @Override
    public void setVisible(boolean isVisible)
    {
        if (getVisible() == isVisible) return;
        super.setVisible(isVisible);
        if (getParent() instanceof PCamera)
        {
            if (isVisible)
            {
                getComponent().setEnabled(last_enabled);
                setBounds(last_bounds);
            }
            else
            {
                last_bounds = getFullBounds().getBounds();
                last_enabled = getComponent().isEnabled();
                getComponent().setEnabled(false);
                setBounds(0, 0, 0, 0);
                storeBounds(last_bounds);
            }
        }
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
        return "PSwingNode: " + getComponent();
    }

    PiccoloCanvas canv = null;

    public PiccoloCanvas getCanvas()
    {
        if (canv == null) canv = findCanvas(this);
        return canv;
    }

    public void setTooltip(String tip)
    {
        addAttribute("tooltip", tip);
    }

    public String getTooltip()
    {
        return (String) getAttribute("tooltip");
    }

    private PiccoloCanvas findCanvas(PNode node)
    {
        // need to get the full tree for this node
        PNode p = node;
        while (p != null)
        {
            PNode parent = p;
            // System.out.println( "parent = " + parent.getClass() );
            if (parent instanceof PCamera)
            {
                PCamera cam = (PCamera) parent;
                if (cam.getComponent() instanceof PiccoloCanvas) { return (PiccoloCanvas) cam
                        .getComponent(); }
            }
            else if (parent instanceof PLayer)
            {
                PLayer player = (PLayer) parent;
                // System.out.println( "Found player: with " +
                // player.getCameraCount() + " cameras" );
                for (int i = 0; i < player.getCameraCount(); i++)
                {
                    PCamera cam = player.getCamera(i);
                    if (cam.getComponent() instanceof PiccoloCanvas)
                        return (PiccoloCanvas) cam.getComponent();
                }
            }
            p = p.getParent();
        }
        return null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        // out.defaultWriteObject();
        // ((PObjectOutputStream) out).writeConditionalObject(parent);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException
    {

    }
}