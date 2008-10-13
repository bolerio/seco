package seco.notebook.piccolo;

/* 
 * Copyright (C) 2002-@year@ by University of Maryland, College Park, MD 20742, USA 
 * All rights reserved. 
 * 
 * Piccolo was written at the Human-Computer Interaction Laboratory 
 * www.cs.umd.edu/hcil by Jesse Grosjean under the supervision of Ben Bederson. 
 * The Piccolo website is www.cs.umd.edu/hcil/piccolo 
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import seco.notebook.NotebookUI;
import seco.notebook.PiccoloFrame.PSwing0;


import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * <code>PSelectionHandler</code> provides standard interaction for selection.
 * Clicking selects the object under the cursor. Shift-clicking allows multiple
 * objects to be selected. Dragging offers marquee selection. Pressing the
 * delete key deletes the selection by default.
 * 
 * @version 1.0
 * @author Ben Bederson
 */
public class ScribaSelectionHandler extends PDragSequenceEventHandler
{
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";
    final static int DASH_WIDTH = 5;
    final static int NUM_STROKES = 10;
    private Map<PNode, Boolean> selection = null; // The current selection
    private ArrayList<PNode> selectableParents = null; // List of nodes whose
    // children can be selected
    private PPath marquee = null;
    private PNode marqueeParent = null; // FNode that marquee is added to as a
    // child
    private Point2D presspt = null;
    private Point2D canvasPressPt = null;
    private Stroke[] strokes = null;
    private Map<PNode, Boolean> allItems = null; // Used within drag handler
    // temporarily
    private ArrayList<PNode> unselectList = null; // Used within drag handler
    // temporarily
    private HashMap<PNode, Boolean> marqueeMap = null;
    private PNode pressNode = null; // FNode pressed on (or null if none)
    private boolean deleteKeyActive = true; // True if DELETE key should
    // delete
    // selection
    private PCamera camera;

    /**
     * Creates a selection event handler.
     * 
     * @param marqueeParent
     *            The node to which the event handler dynamically adds a marquee
     *            (temporarily) to represent the area being selected.
     * @param selectableParent
     *            The node whose children will be selected by this event
     *            handler.
     */
    public ScribaSelectionHandler(PNode marqueeParent, PNode selectableParent,
            PCamera camera)
    {
        this.marqueeParent = marqueeParent;
        this.selectableParents = new ArrayList<PNode>();
        this.selectableParents.add(selectableParent);
        this.camera = camera;
        init();
    }

    protected void init()
    {
        // float[] dash = { DASH_WIDTH, DASH_WIDTH };
        strokes = new Stroke[NUM_STROKES];
        for (int i = 0; i < NUM_STROKES; i++)
        {
            if (System.getProperty("os.name").startsWith("Mac"))
            {
                strokes[i] = new BasicStroke(10);
            } else
            {
                strokes[i] = new PFixedWidthStroke(3);// ,
                // BasicStroke.CAP_BUTT,
                // BasicStroke.JOIN_MITER,
                // 1, dash, i);
            }
        }
        selection = new HashMap<PNode, Boolean>();
        allItems = new HashMap<PNode, Boolean>();
        unselectList = new ArrayList<PNode>();
        marqueeMap = new HashMap<PNode, Boolean>();
    }

    // /////////////////////////////////////////////////////
    // Public static methods for manipulating the selection
    // /////////////////////////////////////////////////////
    public void select(Collection items)
    {
        Iterator itemIt = items.iterator();
        while (itemIt.hasNext())
        {
            PNode node = (PNode) itemIt.next();
            select(node);
        }
    }

    public void select(Map items)
    {
        Iterator itemIt = items.keySet().iterator();
        while (itemIt.hasNext())
        {
            PNode node = (PNode) itemIt.next();
            select(node);
        }
    }

    public void select(PNode node)
    {
        if (isSelected(node))
        {
            return;
        }
        if (node == null)
            return;
        selection.put(node, Boolean.TRUE);
        decorateSelectedNode(node);
        // PNotificationCenter.defaultCenter().postNotification("SELECTION_ADDED_NOTIFICATION",
        // node);
    }

    public void decorateSelectedNode(PNode node)
    {
        PSmallBoundsHandle.addBoundsHandlesTo(node);
    }

    public void unselect(Collection items)
    {
        Iterator itemIt = items.iterator();
        while (itemIt.hasNext())
        {
            PNode node = (PNode) itemIt.next();
            unselect(node);
        }
    }

    public void unselect(PNode node)
    {
        if (!isSelected(node))
        {
            return;
        }
        undecorateSelectedNode(node);
        selection.remove(node);
        // PNotificationCenter.defaultCenter().postNotification("SELECTION_REMOVED_NOTIFICATION",
        // node);
    }

    public void undecorateSelectedNode(PNode node)
    {
        PSmallBoundsHandle.removeBoundsHandlesFrom(node);
    }

    public void unselectAll()
    {
        // PNotificationCenter.defaultCenter().postNotification("SELECTION_CLEARED_NOTIFICATION"
        // , null);
        for (PNode node : selection.keySet())
            unselect(node);
        selection.clear();
    }

    public boolean isSelected(PNode node)
    {
        if ((node != null) && (selection.containsKey(node)))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public Collection<PNode> getSelection()
    {
        ArrayList<PNode> sel = new ArrayList<PNode>();
        for (PNode node : selection.keySet())
            sel.add(node);
        return sel;
    }

    /**
     * Determine if the specified node is selectable (i.e., if it is a child of
     * the one the list of selectable parents.
     */
    protected boolean isSelectable(PNode node)
    {
        boolean selectable = false;
        Iterator parentsIt = selectableParents.iterator();
        while (parentsIt.hasNext())
        {
            PNode parent = (PNode) parentsIt.next();
            if (parent.getChildrenReference().contains(node))
            {
                selectable = true;
                break;
            } else if (parent instanceof PCamera)
            {
                for (int i = 0; i < ((PCamera) parent).getLayerCount(); i++)
                {
                    PLayer layer = ((PCamera) parent).getLayer(i);
                    if (layer.getChildrenReference().contains(node))
                    {
                        selectable = true;
                        break;
                    }
                }
            }
        }
        return selectable;
    }

    // ////////////////////////////////////////////////////
    // Methods for modifying the set of selectable parents
    // ////////////////////////////////////////////////////
    public void addSelectableParent(PNode node)
    {
        selectableParents.add(node);
    }

    public void removeSelectableParent(PNode node)
    {
        selectableParents.remove(node);
    }

    public void setSelectableParent(PNode node)
    {
        selectableParents.clear();
        selectableParents.add(node);
    }

    public void setSelectableParents(Collection<PNode> c)
    {
        selectableParents.clear();
        selectableParents.addAll(c);
    }

    public Collection<PNode> getSelectableParents()
    {
        return (ArrayList<PNode>) selectableParents.clone();
    }

    // //////////////////////////////////////////////////////
    // The overridden methods from PDragSequenceEventHandler
    // //////////////////////////////////////////////////////
    protected void startDrag(PInputEvent e)
    {
        super.startDrag(e);
        initializeSelection(e);
        if (isMarqueeSelection(e))
        {
            initializeMarquee(e);
            if (!isOptionSelection(e))
            {
                startMarqueeSelection(e);
            } else
            {
                startOptionMarqueeSelection(e);
            }
        } else
        {
            if (!isOptionSelection(e))
            {
                startStandardSelection(e);
            } else
            {
                startStandardOptionSelection(e);
            }
        }
        try
        {
            Color bg = (Color) camera.getPaint();
            marquee.setStrokePaint(new Color(255 - bg.getRed(), 255 - bg
                    .getGreen(), 135));
        }
        catch (Exception ex)
        {
        }
    }

    protected void drag(PInputEvent e)
    {
        super.drag(e);
        if (isMarqueeSelection(e))
        {
            updateMarquee(e);
            if (!isOptionSelection(e))
            {
                computeMarqueeSelection(e);
            } else
            {
                computeOptionMarqueeSelection(e);
            }
        } else
        {
            dragStandardSelection(e);
        }
    }

    protected void endDrag(PInputEvent e)
    {
        super.endDrag(e);
        if (isMarqueeSelection(e))
        {
            endMarqueeSelection(e);
        } else
        {
            endStandardSelection(e);
        }
    }

    // //////////////////////////
    // Additional methods
    // //////////////////////////
    public boolean isOptionSelection(PInputEvent pie)
    {
        return pie.isShiftDown();
    }

    protected boolean isMarqueeSelection(PInputEvent pie)
    {
        return false; // (pressNode == null);
    }

    protected void initializeSelection(PInputEvent pie)
    {
        canvasPressPt = pie.getCanvasPosition();
        presspt = pie.getPosition();
        pressNode = pie.getPath().getPickedNode();
        if (pressNode instanceof PCamera)
            pressNode = null;
        if (pressNode != null)
        {
            int onmask = InputEvent.ALT_DOWN_MASK
                    | InputEvent.BUTTON1_DOWN_MASK;
            int offmask = InputEvent.CTRL_DOWN_MASK;
            if (!((pie.getModifiersEx() & (onmask | offmask)) == onmask))
                pressNode = null;
        }
    }

    protected void initializeMarquee(PInputEvent e)
    {
        marquee = PPath.createRectangle((float) presspt.getX(), (float) presspt
                .getY(), 0, 0);
        marquee.setPaint(null);
        marquee.setStrokePaint(Color.yellow);
        marquee.setStroke(strokes[0]);
        marqueeParent.addChild(marquee);
        marqueeMap.clear();
    }

    protected void startOptionMarqueeSelection(PInputEvent e)
    {
    }

    protected void startMarqueeSelection(PInputEvent e)
    {
        unselectAll();
    }

    protected void startStandardSelection(PInputEvent pie)
    {
        // Option indicator not down - clear selection, and start fresh
        if (!isSelected(pressNode))
        {
            unselectAll();
            // if (isSelectable(pressNode)) {
            select(pressNode);
            if(pressNode != null) pie.getInputManager().setMouseFocus(pie.getPath());
            // }
        }
    }

    protected void startStandardOptionSelection(PInputEvent pie)
    {
        // Option indicator is down, toggle selection
        // if (isSelectable(pressNode)) {
        if (isSelected(pressNode))
        {
            unselect(pressNode);
        } else
        {
            select(pressNode);
        }
        // }
    }

    protected void updateMarquee(PInputEvent pie)
    {
        PBounds b = new PBounds();
        if (marqueeParent instanceof PCamera)
        {
            b.add(canvasPressPt);
            b.add(pie.getCanvasPosition());
        } else
        {
            b.add(presspt);
            b.add(pie.getPosition());
        }
        marquee.setPathToRectangle((float) b.x, (float) b.y, (float) b.width,
                (float) b.height);
        b.reset();
        b.add(presspt);
        b.add(pie.getPosition());
        allItems.clear();
        PNodeFilter filter = createNodeFilter(b);
        for (PNode parent : selectableParents)
        {
            Collection items;
            if (parent instanceof PCamera)
            {
                items = new ArrayList();
                for (int i = 0; i < ((PCamera) parent).getLayerCount(); i++)
                {
                    ((PCamera) parent).getLayer(i).getAllNodes(filter, items);
                }
            } else
            {
                items = parent.getAllNodes(filter, null);
            }
            Iterator itemsIt = items.iterator();
            while (itemsIt.hasNext())
            {
                allItems.put((PNode) itemsIt.next(), Boolean.TRUE);
            }
        }
    }

    protected void computeMarqueeSelection(PInputEvent pie)
    {
        unselectList.clear();
        // Make just the items in the list selected
        // Do this efficiently by first unselecting things not in the list
        for (PNode node : selection.keySet())
            if (!allItems.containsKey(node))
                unselectList.add(node);
        unselect(unselectList);
        // Then select the rest
        for (PNode node : allItems.keySet())
        {
            if (!selection.containsKey(node) && !marqueeMap.containsKey(node)
                    && isSelectable(node))
            {
                marqueeMap.put(node, Boolean.TRUE);
            } else if (!isSelectable(node))
            {
                allItems.remove(node);
            }
        }
        select(allItems);
    }

    protected void computeOptionMarqueeSelection(PInputEvent pie)
    {
        unselectList.clear();
        for (PNode node : selection.keySet())
            if (!allItems.containsKey(node) && marqueeMap.containsKey(node))
            {
                marqueeMap.remove(node);
                unselectList.add(node);
            }
        unselect(unselectList);
        // Then select the rest
        for (PNode node : allItems.keySet())
        {
            if (!selection.containsKey(node) && !marqueeMap.containsKey(node)
                    && isSelectable(node))
            {
                marqueeMap.put(node, Boolean.TRUE);
            } else if (!isSelectable(node))
            {
                allItems.remove(node);
            }
        }
        select(allItems);
    }

    protected PNodeFilter createNodeFilter(PBounds bounds)
    {
        return new BoundsFilter(bounds);
    }

    protected PBounds getMarqueeBounds()
    {
        if (marquee != null)
        {
            return marquee.getBounds();
        }
        return new PBounds();
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

    protected void endMarqueeSelection(PInputEvent e)
    {
        // Remove marquee
        marquee.removeFromParent();
        marquee = null;
    }

    protected void endStandardSelection(PInputEvent e)
    {
        pressNode = null;
    }

    // /**
    // * This gets called continuously during the drag, and is used to animate
    // the marquee
    // */
    // protected void dragActivityStep(PInputEvent aEvent) {
    // if (marquee != null) {
    // float origStrokeNum = strokeNum;
    // strokeNum = (strokeNum + 0.5f) % NUM_STROKES; // Increment by partial
    // steps to slow down animation
    // if ((int)strokeNum != (int)origStrokeNum) {
    // marquee.setStroke(strokes[(int)strokeNum]);
    // }
    // }
    // }

    /**
     * Delete selection when delete key is pressed (if enabled)
     */
    public void keyPressed(PInputEvent e)
    {
        System.out.println("ScribaSelectionHandler - keyPressed: " + e);
        
        switch (e.getKeyCode())
        {
        case KeyEvent.VK_DELETE:
            deleteSelection(e);
        }
    }
    
    public void deleteSelection(PInputEvent e)
    {
        System.out.println("TSH - deleteSelection:" + e.getComponent());
        if (deleteKeyActive)
        {
            for (PNode node : selection.keySet())
            {
                if (node instanceof PSwing0 && !((PSwing0) node).isDeleteable())
                    continue;
               // Object ui =  ((PSwing0) node).getComponent();
               // if(ui instanceof NotebookUI)
               //     PiccoloSync.getInstance().removeAuxDoc(
               //             ((NotebookUI) ui).getDoc());
                node.removeFromParent();
            }
            selection.clear();
        }
    }

    public boolean getSupportDeleteKey()
    {
        return deleteKeyActive;
    }

    public boolean isDeleteKeyActive()
    {
        return deleteKeyActive;
    }

    /**
     * Specifies if the DELETE key should delete the selection
     */
    public void setDeleteKeyActive(boolean deleteKeyActive)
    {
        this.deleteKeyActive = deleteKeyActive;
    }

    // ////////////////////
    // Inner classes
    // ////////////////////
    protected class BoundsFilter implements PNodeFilter
    {
        PBounds localBounds = new PBounds();
        PBounds bounds;

        protected BoundsFilter(PBounds bounds)
        {
            this.bounds = bounds;
        }

        public boolean accept(PNode node)
        {
            localBounds.setRect(bounds);
            node.globalToLocal(localBounds);
            boolean boundsIntersects = node.intersects(localBounds);
            boolean isMarquee = (node == marquee);
            return (node.getPickable() && boundsIntersects && !isMarquee
                    && !selectableParents.contains(node) && !isCameraLayer(node));
        }

        public boolean acceptChildrenOf(PNode node)
        {
            return selectableParents.contains(node) || isCameraLayer(node);
        }

        public boolean isCameraLayer(PNode node)
        {
            if (node instanceof PLayer)
            {
                for (Iterator i = selectableParents.iterator(); i.hasNext();)
                {
                    PNode parent = (PNode) i.next();
                    if (parent instanceof PCamera)
                    {
                        if (((PCamera) parent).indexOfLayer((PLayer) node) != -1)
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
