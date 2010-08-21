package seco.gui.piccolo;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.ImageIcon;

import seco.ThisNiche;
import seco.gui.PSwingNode;
import seco.gui.TopFrame;
import seco.notebook.util.IconManager;
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
    protected Action action;
    
    public CommandHandle(PBoundsLocator locator)
    {
        super(locator);
        node = (PSwingNode) locator.getNode();
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        setShape(PNodeEx.RECTANGLE);
    }
    
    public CommandHandle(PBoundsLocator locator, Action a)
    {
        this(locator);
        setToolTip((String) a.getValue(Action.SHORT_DESCRIPTION));
        ImageIcon ic = (ImageIcon) a.getValue(Action.SMALL_ICON);
        if(ic != null)
          setImage(ic.getImage());
        action = a;
    }
    
    public void setLocator(PLocator aLocator)
    {
        node = (PSwingNode) ((PBoundsLocator)aLocator).getNode();
        super.setLocator(aLocator);
    }
    
    public Cursor getCursorFor(int side)
    {
        return new Cursor(Cursor.HAND_CURSOR);//.CROSSHAIR_CURSOR);
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        relocateHandle();
        ThisNiche.getCanvas().getSelectionHandler().select(node, false);
        performAction(aLocalPoint, aEvent);
    }
    
    /*
     * Gets called on mouse click. Override this method to perform some action. 
     */
    public void performAction(Point2D aLocalPoint, PInputEvent aEvent)
    {
        if(action != null)
        {
            ActionEvent ae = new ActionEvent (
                    aEvent.getSourceSwingEvent().getComponent(),
                    ActionEvent.ACTION_FIRST,
                    "click");
            action.actionPerformed(ae);
        }
    }

}
