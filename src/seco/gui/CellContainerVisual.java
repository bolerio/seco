package seco.gui;

import java.awt.Rectangle;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;

public class CellContainerVisual implements CellVisual, EventHandler
{
    private static final HGPersistentHandle handle = HGHandleFactory
            .makeHandle("cc88ae4c-f70b-4536-814c-95a6ac6a7b62");

    public static HGPersistentHandle getHandle()
    {
        return handle;
    }

    public JComponent bind(CellGroupMember element)
    {
        CellGroup group = (CellGroup) element;
        PiccoloCanvas canvas = null;
        // container canvas....
        if (isTopContainer(element))
        {
            canvas = TopFrame.getInstance().getCanvas();
            Rectangle r = (Rectangle) element.getAttribute(VisualAttribs.rect);
            if (r != null) TopFrame.getInstance().setBounds(r);
        }
        else
        {
            // TODO:
            canvas = new PiccoloCanvas();
        }

        for (int i = 0; i < group.getArity(); i++)
            addChild(canvas, group.getTargetAt(i));
        if (canvas != null) canvas.relayout();
        group.setVisualInstance(canvas);
        CellUtils.addEventPubSub(
                CellGroupChangeEvent.HANDLE, ThisNiche.handleOf(element), 
                getHandle(), getHandle());
        return canvas;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            handleEvent((CellGroupChangeEvent) event);
        }else if (eventType.equals(EvalCellEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            rebind((EvalCellEvent) event, publisher);
        }
    }

    private void addChild(PiccoloCanvas canvas, HGHandle childH)
    {
        CellGroupMember x = ThisNiche.hg.get(childH);
        CellVisual visual = CellUtils.getVisual(x);
        JComponent comp = visual.bind(x);
        if(comp != null)
           canvas.addComponent(comp, x);
        CellUtils.addEventPubSub(
                EvalCellEvent.HANDLE, childH, getHandle(), getHandle());
    }

    private void rebind(EvalCellEvent event, HGHandle publisher)
    {
        HGHandle h = publisher;//event.getCellHandle();
        CellGroup group = CellUtils.getParentGroup(h);
        System.out.println("rebind: " + h + ":" + group);
        //PiccoloCanvas canvas = (PiccoloCanvas) group.getVisualInstance();
        //TODO: this is not correct in general
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        PSwingNode ps = canvas.getPSwingNodeForHandle(h);
        if(ps != null)  ps.removeFromParent();
        addChild(canvas, h);
    }
    
    private void handleEvent(CellGroupChangeEvent e)
    {
        CellGroup group = (CellGroup) ThisNiche.hg.get(e.getCellGroup());
        if (!(group.getVisualInstance() instanceof PiccoloCanvas)) return;
        PiccoloCanvas canvas = (PiccoloCanvas) group.getVisualInstance();
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        if (removed != null && removed.length > 0)
            for (int i = 0; i < removed.length; i++)
            {
                PSwingNode ps = canvas.getPSwingNodeForHandle(removed[i]);
                if(ps != null) 
                   ps.removeFromParent();
                CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, 
                        removed[i], getHandle(), getHandle());
            }
        if (added != null && added.length > 0)
            for (int i = 0; i < added.length; i++)
                addChild(canvas, added[i]);
    }

    private boolean isTopContainer(CellGroupMember element)
    {
        return ThisNiche.TOP_CELL_GROUP_HANDLE.equals(ThisNiche
                .handleOf(element));
    }

}
