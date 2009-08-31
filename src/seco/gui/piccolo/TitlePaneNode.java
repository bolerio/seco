package seco.gui.piccolo;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingConstants;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.PSwingNode;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.util.PNodeLocator;

public class TitlePaneNode extends CommandHandle
{
    public static int HEIGHT = 16;

    public TitlePaneNode(PSwingNode node)
    {
        super(new OffsetPBoundsLocator(node, SwingConstants.NORTH, new Point(0,
                -HEIGHT / 2)));
        this.setLabelText(CellUtils.getName((CellGroupMember) ThisNiche.hg
                .get(node.getHandle())));
        this.setHeight(PREF_DIM + 2);
        this.setWidth(node.getWidth());// - 50);
        this.setPaint(new Color(77, 117, 154));
        this.getLabel().setTextPaint(Color.white);
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
        setWidth(((PNodeLocator) getLocator()).getNode().getWidth());// - 50);
    }
}
