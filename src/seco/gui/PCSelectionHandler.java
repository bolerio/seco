package seco.gui;

/* 
 * Copyright (C) 2002-@year@ by University of Maryland, College Park, MD 20742, USA 
 * All rights reserved. 
 * 
 * Piccolo was written at the Human-Computer Interaction Laboratory 
 * www.cs.umd.edu/hcil by Jesse Grosjean under the supervision of Ben Bederson. 
 * The Piccolo website is www.cs.umd.edu/hcil/piccolo 
 */
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.TransferHandler;

import seco.gui.piccolo.PSmallBoundsHandle;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

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
    boolean dragging_copy = false;
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
        if (isSelected(node)) { return; }
        if (node == null) return;
        selection.put(node, Boolean.TRUE);
        decorateSelectedNode(node);
    }

    public void decorateSelectedNode(PNode node)
    {
        PSmallBoundsHandle.addBoundsHandlesTo(node);
    }

    public void unselect(Collection<PNode> items)
    {
        for (PNode node : items)
            unselect(node);
    }

    public void unselect(PNode node)
    {
        if (!isSelected(node)) { return; }
        undecorateSelectedNode(node);
        selection.remove(node);
    }

    public void undecorateSelectedNode(PNode node)
    {
        PSmallBoundsHandle.removeBoundsHandlesFrom(node);
    }

    public void unselectAll()
    {
        for (PNode node : selection.keySet())
            unselect(node);
        selection.clear();
    }

    public boolean isSelected(PNode node)
    {
        return selection.containsKey(node);
    }

    public Collection<PNode> getSelection()
    {
        ArrayList<PNode> sel = new ArrayList<PNode>();
        for (PNode node : selection.keySet())
            sel.add(node);
        return sel;
    }
    
    public PSwingNode getSelectedPSwingNode()
    {
        if(selection.isEmpty()) return null;
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
        if (!isOptionSelection(e)) 
            startStandardSelection(e);
        else
            startStandardOptionSelection(e);
     }

    protected void drag(PInputEvent e)
    {
        super.drag(e);
        if(dragging_copy) return;
        dragStandardSelection(e);
    }

    protected void endDrag(PInputEvent e)
    {
        super.endDrag(e);
        endStandardSelection(e);
        //System.out.println("End drag");
        dragging_copy = false;
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
            if (pressNode.getParent() instanceof PRoot) 
                pressNode = null;
            else
                pressNode = pressNode.getParent();
        }
        if (pressNode != null)
        {
            int onmask = InputEvent.ALT_DOWN_MASK
                    | InputEvent.BUTTON1_DOWN_MASK;
            int ctrlmask = InputEvent.CTRL_DOWN_MASK;
            if((pie.getModifiersEx() &ctrlmask) == ctrlmask)
            {
                System.out.println("ScribaSelectionHandler - ctrl - copy" +
                        pie.getComponent());
               dragging_copy = true;
               this.selection.put(pressNode, true);
               PiccoloCanvas canvas = (PiccoloCanvas) pie.getComponent();
               canvas.getTransferHandler().exportAsDrag(
                       canvas, 
                       pie.getSourceSwingEvent(), TransferHandler.MOVE);
               return;
            }
            if (!((pie.getModifiersEx() & (onmask | ctrlmask)) == onmask))
                pressNode = null;
        }
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
        System.out.println("ScribaSelectionHandler - keyPressed: " + e);
        switch (e.getKeyCode())
        {
           case KeyEvent.VK_DELETE:
               TopFrame.getInstance().getCanvas().deleteSelection();
            //deleteSelection(e);
        }
    }

    public void deleteSelection(PInputEvent e)
    {
        // System.out.println("TSH - deleteSelection:" + e.getComponent());
        for (PNode node : selection.keySet())
        {
            //if (node instanceof PSwingNode
            //        && !((PSwingNode) node).isDeleteable()) continue;
            node.removeFromParent();
        }
        selection.clear();

    }

}
