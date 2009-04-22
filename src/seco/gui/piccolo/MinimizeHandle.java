package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;

import seco.ThisNiche;
import seco.gui.PSwingNode;
import seco.gui.VisualAttribs;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class MinimizeHandle extends PSmallBoundsHandle
{
    private int PREF_DIM = 10;
    protected PSwingNode node;
    
    public MinimizeHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        this.node = node;
        setPaint(Color.orange);
        
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        this.setShape(PNodeEx.RECTANGLE);
        this.setToolTip("Minimize");
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        relocateHandle();
        //setPaint(Color.yellow);
        CellGroupMember cgm = ThisNiche.hg.get(node.getHandle());
        CellUtils.toggleMinimized(cgm);
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

    
    public static class OffsetPBoundsLocator extends PBoundsLocator
    {
        protected Point offsetP;
        
        public OffsetPBoundsLocator(PNode node, int side, Point offsetP)
        {
            super(node, side);
            this.offsetP = offsetP;
        }

        @Override
        public int getSide()
        {
            return super.getSide();
        }

        @Override
        public double locateX()
        {
            return super.locateX() + offsetP.x;
        }

        @Override
        public double locateY()
        {
            return super.locateY() + offsetP.y;
        }

        @Override
        public void setSide(int side)
        {
            super.setSide(side);
        }
        
        
    }
    
}
