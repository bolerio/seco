package seco.gui.piccolo;

import java.awt.Cursor;
import java.awt.geom.Point2D;

import seco.gui.PSwingNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolox.util.PLocator;

/*
 * Base class for handles shown upon <code>PSwingNode</code> selection in canvas.
 */
public class CommandHandle extends PSmallBoundsHandle
{
    //handle's dimension  
    protected int PREF_DIM = 16;
    //the node on which selection this handle is shown 
    protected PSwingNode node;
    
    public CommandHandle(PBoundsLocator locator)
    {
        super(locator);
        node = (PSwingNode) locator.getNode();
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        setShape(PNodeEx.RECTANGLE);
    }
    
    public void setLocator(PLocator aLocator)
    {
        node = (PSwingNode) ((PBoundsLocator)aLocator).getNode();
        super.setLocator(aLocator);
    }
    
    public Cursor getCursorFor(int side)
    {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        relocateHandle();
        performAction(aLocalPoint, aEvent);
    }
    
    /*
     * Gets called on mouse click. Override this method to perform some action. 
     */
    public void performAction(Point2D aLocalPoint, PInputEvent aEvent)
    {
        
    }

}
