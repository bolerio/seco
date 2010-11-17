package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import seco.ThisNiche;
import seco.gui.PSwingNode;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.util.IconManager;
import edu.umd.cs.piccolo.event.PInputEvent;

public class MaximizeHandle extends CommandHandle
{
    public MaximizeHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        setPaint(new Color(255, 200, 200));
        setToolTip("Maximize");
        ImageIcon ic = IconManager.resolveIcon("Maximize.gif");
        setImage(ic.getImage());
    }
    
    public void performAction(Point2D aLocalPoint, PInputEvent aEvent)
    {
        CellGroupMember cgm = ThisNiche.graph.get(node.getHandle());
        if(CellUtils.isMinimized(cgm))
            CellUtils.toggleMinimized(cgm);
        CellUtils.toggleMaximized(cgm);
    }
}

