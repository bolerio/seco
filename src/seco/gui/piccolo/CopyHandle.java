package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.TransferHandler;

import seco.gui.PiccoloCanvas;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class CopyHandle extends PSmallBoundsHandle
{
    private int PREF_DIM = 10;
    private boolean dragStarted = false;
    public CopyHandle(PNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        setPaint(Color.yellow);
        
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        this.setToolTip("Move");
    }
    
    public Cursor getCursorFor(int side)
    {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
    }
    
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent e)
    {
        dragStarted = true;
        setPaint(Color.green);
        PiccoloCanvas canvas = (PiccoloCanvas) e.getComponent();
        canvas.getTransferHandler().exportAsDrag(
        canvas, e.getSourceSwingEvent(), TransferHandler.MOVE);
    }

    public void dragHandle(PDimension aLocalDimension, PInputEvent e)
    {
        offset(aLocalDimension.getWidth(), aLocalDimension.getHeight());
        System.out.println("CopyHandle -drag: ");// + gDist + ":" + dx + "," + dy + ":" + aLocalDimension);
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        dragStarted = false;
        relocateHandle();
        setPaint(Color.yellow);
        System.out.println("CopyHandle - ndHandleDrag");
        //PBoundsLocator l = (PBoundsLocator) getLocator();
        //l.getNode().endResizeBounds();
    }
    
    @Override
    public void relocateHandle()
    {
        // DO NOTHING WHEN DRAGING
        if(!dragStarted)
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
            System.out.println("CopyHandle -locateX: ");
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
