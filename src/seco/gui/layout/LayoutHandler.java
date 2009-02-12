package seco.gui.layout;

import seco.gui.PSwingNode;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public interface LayoutHandler
{
    void setRefPoint(RefPoint p);
    void setBounds(DRect r);
    void layout(PSwingCanvas canvas, PSwingNode ps);
}
