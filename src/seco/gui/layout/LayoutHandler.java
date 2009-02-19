package seco.gui.layout;

import seco.gui.PSwingNode;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public interface LayoutHandler
{
    public RefPoint getRefPoint();
    void setRefPoint(RefPoint p);
    DRect getBounds();
    void setBounds(DRect r);
    void layout(PSwingCanvas canvas, PSwingNode ps);
}
