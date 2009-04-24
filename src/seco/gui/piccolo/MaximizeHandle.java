package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;

import seco.ThisNiche;
import seco.gui.PSwingNode;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.event.PInputEvent;

public class MaximizeHandle extends PSmallBoundsHandle
{
    private int PREF_DIM = 10;
    protected PSwingNode node;
    
    public MaximizeHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        this.node = node;
        setPaint(new Color(255, 200, 200));
        
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        this.setShape(PNodeEx.RECTANGLE);
        this.setToolTip("Maximize");
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        relocateHandle();
        CellGroupMember cgm = ThisNiche.hg.get(node.getHandle());
        CellUtils.toggleMaximized(cgm);
    }
    
    public Cursor getCursorFor(int side)
    {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
    }
    @Override
    public void relocateHandle()
    {
        // DO NOTHING WHEN DRAGING
        super.relocateHandle();
    }
    
}

