package seco.gui;

/* 
 * Copyright (C) 2002-@year@ by University of Maryland, College Park, MD 20742, USA 
 * All rights reserved. 
 * 
 * Piccolo was written at the Human-Computer Interaction Laboratory 
 * www.cs.umd.edu/hcil by Jesse Grosjean under the supervision of Ben Bederson. 
 * The Piccolo website is www.cs.umd.edu/hcil/piccolo 
 */
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingConstants;

import seco.ThisNiche;
import seco.gui.piccolo.CopyHandle;
import seco.gui.piccolo.TitlePaneNode;
import seco.gui.piccolo.MaximizeHandle;
import seco.gui.piccolo.MinimizeHandle;
import seco.gui.piccolo.OffsetPBoundsLocator;
import seco.gui.piccolo.PSmallBoundsHandle;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PBoundsLocator;

/**
 * <code>PCSelectionHandler</code> provides standard interaction for selection.
 * Alt - clicking selects the object under the cursor.
 */
public class PCSelectionHandler extends PDragSequenceEventHandler
{
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";
    final static int DASH_WIDTH = 5;
    final static int NUM_STROKES = 10;
    private Map<PNode, Boolean> selection = null; // The current selection
    private PNode pressNode = null; // FNode pressed on (or null if none)

    /**
     * Creates a selection event handler.
     */
    public PCSelectionHandler()
    {
        init();
    }

    protected void init()
    {
        selection = new HashMap<PNode, Boolean>();
    }

    // /////////////////////////////////////////////////////
    // Public methods for manipulating the selection
    // /////////////////////////////////////////////////////
    public void select(Collection<PNode> items)
    {
        for (PNode node : items)
            select(node);
    }

    public void select(PNode node)
    {
        select(node, true);
    }
    
    public void select(PNode node, boolean show_handles)
    {
        if (node == null || isSelected(node)) return;
        unselectAll();
        if (node instanceof TitlePaneNode)
        {
            System.out.println("PCSelectionHandler - select: " + node);
            select(node.getParent(), show_handles);
            return;
        }
        selection.put(node, Boolean.TRUE);
        if(show_handles)
          decorateSelectedNode(node, true);
        node.moveToFront();
    }

    // TODO: provide some means to add/register additional handles
    public static void decorateSelectedNode(PNode node,
            boolean show_resize_handles)
    {
        if (show_resize_handles)
        {
            node.addChild(new PSmallBoundsHandle(PBoundsLocator
                    .createEastLocator(node)));
            node.addChild(new PSmallBoundsHandle(PBoundsLocator
                    .createWestLocator(node)));
            node.addChild(new PSmallBoundsHandle(PBoundsLocator
                    .createSouthLocator(node)));
            node.addChild(new PSmallBoundsHandle(PBoundsLocator
                    .createSouthEastLocator(node)));
            node.addChild(new PSmallBoundsHandle(PBoundsLocator
                    .createSouthWestLocator(node)));

            node
                    .addChild(new PSmallBoundsHandle(new OffsetPBoundsLocator(
                            node, SwingConstants.NORTH, new Point(0,
                                    -TitlePaneNode.HEIGHT))));
            node.addChild(new PSmallBoundsHandle(new OffsetPBoundsLocator(node,
                    SwingConstants.NORTH_EAST, new Point(0,
                            -TitlePaneNode.HEIGHT))));
            node.addChild(new PSmallBoundsHandle(new OffsetPBoundsLocator(node,
                    SwingConstants.NORTH_WEST, new Point(0,
                            -TitlePaneNode.HEIGHT))));
        }
        if (node instanceof PSwingNode)
        {
            CellGroupMember cgm = ThisNiche.graph.get(((PSwingNode) node)
                    .getHandle());
            if (CellUtils.isShowTitle(cgm) && !CellUtils.isMinimized(cgm))
            {
                addTitle(node, new TitlePaneNode((PSwingNode) node));
            }
            else //if(!CellUtils.isMinimized(cgm))
            {
                TitlePaneNode.addActionHandles((PSwingNode) node, node);
            }
        }
    }
    
    private static void addTitle(PNode node, TitlePaneNode titleNode)
    {
        for (Iterator i = node.getChildrenIterator(); i.hasNext();)
        {
            PNode each = (PNode) i.next();
            if (each instanceof TitlePaneNode) 
            {    
                each.removeFromParent();
                break;
            }
        }
        node.addChild(titleNode);
    }

    public void unselect(PNode node)
    {
        if (!isSelected(node)) { return; }
        CellGroupMember cgm = ThisNiche.graph.get(((PSwingNode) node).getHandle());
        undecorateSelectedNode(node, !CellUtils.isShowTitle(cgm));
        selection.remove(node);
    }

    public static void undecorateSelectedNode(PNode node, boolean remove_title)
    {
        ArrayList<PNode> handles = new ArrayList<PNode>();
        Iterator i = node.getChildrenIterator();
        while (i.hasNext())
        {
            PNode each = (PNode) i.next();
            if (each instanceof PSmallBoundsHandle)
            {
                if (each instanceof TitlePaneNode && !remove_title) 
                    continue;
                handles.add(each);
            }
        }
        node.removeChildren(handles);
//        if(compensate){
//            Rectangle r = node.getFullBounds().getBounds();
//            ((PSwingNode)node).storeBounds(
//                    new Rectangle(r.x, r.y, r.width, r.height - TitlePaneNode.HEIGHT));
//        }
    }

    public void unselectAll()
    {
        for (PNode node : getSelection())
            unselect(node);
        selection.clear();
    }

    public boolean isSelected(PNode node)
    {
        return selection.containsKey(node);
    }

    public Collection<PNode> getSelection()
    {
        return new ArrayList<PNode>(selection.keySet());
    }

    public PSwingNode getSelectedPSwingNode()
    {
        if (selection.isEmpty()) return null;
        PNode node = selection.keySet().iterator().next();
        return (node instanceof PSwingNode) ? (PSwingNode) node : null;
    }

    // //////////////////////////////////////////////////////
    // The overridden methods from PDragSequenceEventHandler
    // //////////////////////////////////////////////////////
    protected void startDrag(PInputEvent e)
    {
        super.startDrag(e);
        initializeSelection(e);
        if (!isOptionSelection(e)) startStandardSelection(e);
        else
            startStandardOptionSelection(e);
    }

    protected void drag(PInputEvent e)
    {
        super.drag(e);
        dragStandardSelection(e);
    }

    protected void endDrag(PInputEvent e)
    {
        super.endDrag(e);
        PNode node = pressNode != null ? pressNode : e.getPickedNode();
        node.endResizeBounds();
        if (node != null && node instanceof PSwingNode)
            adjust_bounds((PSwingNode) node);
        endStandardSelection(e);
    }

    private static void adjust_bounds(PSwingNode node)
    {
        PiccoloCanvas pc = node.getCanvas();
        PBounds pcb = pc.getCamera().getViewBounds();
        PBounds ncb = node.getFullBounds();
        if (out(pcb.getBounds(), ncb.getBounds()))
        {
            System.out.println("adjust_bounds1: " + ncb + ":" +pcb);
            double x = 0;
            double y = 0;
            if (ncb.x < pcb.x) x = pcb.x - ncb.x;
            if (ncb.y < pcb.y) y = pcb.y - ncb.y;
            if (ncb.x > pcb.x + pcb.width)
                x = pcb.x + pcb.width - (ncb.x + ncb.width);
            if (ncb.y > pcb.y + pcb.height)
                y = pcb.y + pcb.height - (ncb.y + ncb.height);
            System.out.println("adjust_bounds2: " + x + ":" + y);
            node.translate(x, y);
            node.invalidatePaint();
        }
    }

    private static boolean out(Rectangle big, Rectangle small)
    {
        return ((small.x + small.width < big.x || small.x > big.x + big.width) || (small.y
                + small.height < big.y || small.y > big.y + big.height));
    }

    // //////////////////////////
    // Additional methods
    // //////////////////////////
    public boolean isOptionSelection(PInputEvent pie)
    {
        return pie.isShiftDown();
    }

    protected void initializeSelection(PInputEvent pie)
    {
        pressNode = pie.getPath().getPickedNode();
        if (pressNode instanceof PCamera)
        {
            if (pressNode.getParent() instanceof PRoot) pressNode = null;
            else
                pressNode = pressNode.getParent();
        }

        if (pressNode != null)
        {
            int onmask = InputEvent.ALT_DOWN_MASK
                    | InputEvent.BUTTON1_DOWN_MASK;
            int ctrlmask = InputEvent.CTRL_DOWN_MASK;
            if (!((pie.getModifiersEx() & (onmask | ctrlmask)) == onmask)
                    && !minimized())
                pressNode = null;
        }
    }
    
    private boolean minimized()
    {
        return pressNode instanceof PSwingNode &&
        CellUtils.isMinimized( (CellGroupMember)
                ThisNiche.graph.get(((PSwingNode)pressNode).getHandle()));
    }

    protected void startStandardSelection(PInputEvent pie)
    {
        if (isSelected(pressNode)) return;
        // Option indicator not down - clear selection, and start fresh
        unselectAll();
        select(pressNode);
        if (pressNode != null)
            pie.getInputManager().setMouseFocus(pie.getPath());
    }

    protected void startStandardOptionSelection(PInputEvent pie)
    {
        // Option indicator is down, toggle selection
        if (isSelected(pressNode)) unselect(pressNode);
        else
            select(pressNode);
    }

    protected void dragStandardSelection(PInputEvent e)
    {
        // There was a press node, so drag selection
        PDimension d = e.getCanvasDelta();
        e.getTopCamera().localToView(d);
        PDimension gDist = new PDimension();
        for (PNode node : selection.keySet())
        {
            gDist.setSize(d);
            node.getParent().globalToLocal(d);
            node.offset(d.getWidth(), d.getHeight());
        }
    }

    protected void endStandardSelection(PInputEvent e)
    {
        pressNode = null;
    }

    @Override
    public void keyPressed(PInputEvent e)
    {
        switch (e.getKeyCode())
        {
        case KeyEvent.VK_DELETE:
            TopFrame.getInstance().getCanvas().deleteSelection();
        }
    }

//    public void deleteSelection(PInputEvent e)
//    {
//        for (PNode node : selection.keySet())
//        {
//            // if (node instanceof PSwingNode
//            // && !((PSwingNode) node).isDeleteable()) continue;
//            node.removeFromParent();
//        }
//        selection.clear();
//    }

    @Override
    public void mousePressed(PInputEvent e)
    {
        super.mousePressed(e);
        if (isSelected(e.getPickedNode()))
        {
            e.getSourceSwingEvent().consume();
            e.setHandled(true);
        }
    }

    @Override
    public void mouseReleased(PInputEvent e)
    {
        super.mouseReleased(e);
        if (isSelected(e.getPickedNode()))
        {
            e.getSourceSwingEvent().consume();
            e.setHandled(true);
        }
    }

}
