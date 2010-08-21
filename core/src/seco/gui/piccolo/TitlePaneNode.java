package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingConstants;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.PCSelectionHandler;
import seco.gui.PSwingNode;
import seco.gui.TopFrame;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PNodeLocator;

public class TitlePaneNode extends CommandHandle
{
    public static int HEIGHT = 16;
    public static Color BG_COLOR = new Color(77, 117, 154);
    public TitlePaneNode(PSwingNode node)
    {
        super(new OffsetPBoundsLocator(node, SwingConstants.NORTH, new Point(0,
                -HEIGHT / 2)));
        setLabelText(CellUtils.getName((CellGroupMember) ThisNiche.graph
                .get(node.getHandle())));
        setHeight(PREF_DIM + 2);
        setWidth(node.getWidth());// - 50);
        setPaint(BG_COLOR);
        getLabel().setTextPaint(Color.white);
        // this.getLabel().setLabelLocation(SwingConstants.NORTH);
        addActionHandles(node, this);
    }

    public static void addActionHandles(PSwingNode node, PNode tn)
    {
        tn.addChild(new CopyHandle((PSwingNode) node,
                SwingConstants.NORTH_EAST, new Point(-10, -HEIGHT / 2)));

        List<Action> actions = GUIHelper.getWinTitleActions();
        int size = actions.size() - 1;
        for (int i = actions.size() - 1; i >= 0; i--)
        {
            int offset = -25 - ((size - i) * 15);
            tn.addChild(new CommandHandle(new OffsetPBoundsLocator(node,
                    SwingConstants.NORTH_EAST, new Point(offset, -HEIGHT / 2)),
                    actions.get(i)));
        }
    }

    public void parentBoundsChanged()
    {
        setWidth(((PNodeLocator) getLocator()).getNode().getWidth());
    }
    
    public void dragHandle(PDimension aLocalDimension, PInputEvent e)
    {
        node.getCanvas().getSelectionHandler().unselectAll();
        PDimension d = e.getCanvasDelta();
        e.getTopCamera().localToView(d);
        //PNode node = ((PNodeLocator) getLocator()).getNode();
        node.getParent().globalToLocal(d);
        node.offset(d.getWidth(), d.getHeight());
    }
    
    
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent e)
    {
        relocateHandle();
        PCSelectionHandler handler = 
            ThisNiche.getCanvas().getSelectionHandler();
        if(!handler.isSelected(node))
            handler.select(node, true);
        else
            handler.unselect(node);
        node.storeBounds(node.getFullBounds().getBounds());
    }

    @Override
    public void rightClick(PInputEvent e)
    {
        ThisNiche.getCanvas().getSelectionHandler().select(node, false);
        ThisNiche.getCanvas().getContextMenuHandler().showNodeMenu(e);
    }
}
