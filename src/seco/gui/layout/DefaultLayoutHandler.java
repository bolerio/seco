package seco.gui.layout;

import seco.gui.PSwingNode;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public class DefaultLayoutHandler implements LayoutHandler
{
    protected DRect rect;
    protected RefPoint refPoint;

    public DefaultLayoutHandler(DRect rect, RefPoint refPoint)
    {
        super();
        this.rect = rect;
        this.refPoint = refPoint;
    }

    public DefaultLayoutHandler()
    {
        super();
    }

    public void layout(PSwingCanvas canvas, PSwingNode ps)
    {
        PBounds cb = canvas.getCamera().getGlobalBounds();
       // PBounds b = ps.getFullBounds();
        //System.out.println("PROPERTY_BOUNDS1: " + cb + ":"
        //        + ps.getGlobalBounds() + ":" + this);
        ps.setTransform(new PAffineTransform());
        if (rect == null) return;
        double w = val(rect.width, cb.width);
        if (w > 0) ps.setWidth(w);
        double h = val(rect.height, cb.height);
        if (h > 0) ps.setHeight(h);
        if (refPoint == RefPoint.TOP_LEFT) 
            ps.translate(val(rect.x, cb.width), 
                        val(rect.y, cb.height));
        else if (refPoint == RefPoint.BOTTOM_LEFT)
            ps.translate(val(rect.x, cb.width), 
                    cb.height - (h + val(rect.y, cb.height)));
        else if (refPoint == RefPoint.TOP_RIGHT)
            ps.translate(cb.width - (w + val(rect.x, cb.width)),
                         val(rect.y, cb.height));
        else if (refPoint == RefPoint.BOTTOM_RIGHT)
            ps.translate(cb.width - (w + val(rect.x, cb.width)), 
                    cb.height - (h + val(rect.y, cb.height)));

    }

    static double val(DValue v, double arg)
    {
        if (v == null) return 0;
        if (v.isPercent()) return arg * v.getValue() / 100;
        return v.getValue();
    }

    public void setBounds(DRect r)
    {
        this.rect = r;
    }

    public void setRefPoint(RefPoint p)
    {
        this.refPoint = p;
    }

    public DRect getBounds()
    {
        return rect;
    }

    public RefPoint getRefPoint()
    {
        return refPoint;
    }

    public String toString()
    {
        return "DefaultLayoutHandler: " + refPoint + ":" + rect;
    }
}
