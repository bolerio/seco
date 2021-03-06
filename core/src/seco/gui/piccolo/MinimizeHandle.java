package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.ImageIcon;

import seco.ThisNiche;
import seco.actions.ScriptletAction;
import seco.gui.PSwingNode;
import seco.gui.TopFrame;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.Scriptlet;
import seco.util.IconManager;
import edu.umd.cs.piccolo.event.PInputEvent;

public class MinimizeHandle extends CommandHandle
{
    public MinimizeHandle(PSwingNode node, int side, Point offsetP)
    {
        super(new OffsetPBoundsLocator(node, side, offsetP));
        setPaint(Color.orange);
        setToolTip("Minimize");
        
        //http://www.kansas.gov/index.php
        ImageIcon ic = IconManager.resolveIcon("Minimize.gif");
        setImage(ic.getImage());
    }
      
    public void performAction(Point2D aLocalPoint, PInputEvent aEvent)
    {
        CellGroupMember cgm = ThisNiche.graph.get(node.getHandle());
        CellUtils.toggleMinimized(cgm);
    }
  
//    public static class Action extends ScriptletAction
//    {
//
//        public Action()
//        {
//            super("node = canvas.getSelectedPSwingNode();"+
//            "CellUtils.toggleMinimized(niche.get(node.getHandle()))");
//            putValue(Action.SMALL_ICON, IconManager.resolveIcon("Minimize.gif"));
//            putValue(Action.SHORT_DESCRIPTION, "Minimize/Restore");
//            // TODO Auto-generated constructor stub
//        }
//   }
}
