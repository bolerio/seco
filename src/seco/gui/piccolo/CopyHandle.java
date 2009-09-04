package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.TransferHandler;

import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.notebook.util.IconManager;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

public class CopyHandle extends CommandHandle
{
    private boolean dragStarted = false;
    protected Paint orig_bg = Color.white; //Color.yellow
    protected Paint sel_bg = Color.green;
    public CopyHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        setPaint(orig_bg);
        setToolTip("Move");
        ImageIcon ic = IconManager.resolveIcon("move.gif");
        setImage(ic.getImage());
    }
    
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent e)
    {
        dragStarted = true;
        setPaint(sel_bg);
        PiccoloCanvas canvas = node.getCanvas();
        canvas.getSelectionHandler().select(node);
        canvas.getTransferHandler().exportAsDrag(
           canvas, e.getSourceSwingEvent(), TransferHandler.MOVE);
    }

    public void dragHandle(PDimension aLocalDimension, PInputEvent e)
    {
        offset(aLocalDimension.getWidth(), aLocalDimension.getHeight());
    }
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent)
    {
        dragStarted = false;
        relocateHandle();
        setPaint(orig_bg);
    }
    
    @Override
    public void relocateHandle()
    {
        // DO NOTHING WHEN DRAGING
        if(!dragStarted)
            super.relocateHandle();
    }
}
