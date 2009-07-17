package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.TransferHandler;

import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.notebook.util.IconManager;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class CopyHandle extends PSmallBoundsHandle
{
    private int PREF_DIM = 16;
    private boolean dragStarted = false;
    protected PSwingNode node;
    protected Paint orig_bg = Color.white; //Color.yellow
    protected Paint sel_bg = Color.green;
    public CopyHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        this.node = node;
        setPaint(orig_bg);
        
        setWidth(PREF_DIM);
        setHeight(PREF_DIM);
        this.setToolTip("Move");
        
        ImageIcon ic = IconManager.resolveIcon("move.gif");
        setImage(ic.getImage());
        this.setShape(PNodeEx.RECTANGLE);
    }
    
    public Cursor getCursorFor(int side)
    {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
    }
    
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent e)
    {
        dragStarted = true;
        setPaint(sel_bg);
        PiccoloCanvas canvas = (PiccoloCanvas) node.getCanvas();
        System.out.println("CopyHandle - startHandleDrag: " + canvas);
        canvas.getTransferHandler().exportAsDrag(
           canvas, e.getSourceSwingEvent(), TransferHandler.MOVE);
    }

    public void dragHandle(PDimension aLocalDimension, PInputEvent e)
    {
        offset(aLocalDimension.getWidth(), aLocalDimension.getHeight());
        //System.out.println("CopyHandle -drag: ");// + gDist + ":" + dx + "," + dy + ":" + aLocalDimension);
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        dragStarted = false;
        relocateHandle();
        setPaint(orig_bg);
       // System.out.println("CopyHandle - endHandleDrag");
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
}
