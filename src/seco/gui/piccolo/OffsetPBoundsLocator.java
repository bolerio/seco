package seco.gui.piccolo;

import java.awt.Point;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class OffsetPBoundsLocator extends PBoundsLocator
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
