package seco.gui;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.swing.PScrollPane;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.things.BaseCellGroupMember;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;

public class CellContainerVisual implements CellVisual, EventHandler
{
    private static final HGPersistentHandle handle = HGHandleFactory
            .makeHandle("cc88ae4c-f70b-4536-814c-95a6ac6a7b62");

    private HGHandle maximized = null;
    
    public static HGPersistentHandle getHandle()
    {
        return handle;
    }

    public JComponent bind(CellGroupMember element)
    {
        CellGroup group = (CellGroup) element;
        final PiccoloCanvas canvas;
        // container canvas....
        if (isTopContainer(element))
        {
            maximized = null;
            canvas = TopFrame.getInstance().getCanvas();
            Rectangle r = (Rectangle) element.getAttribute(VisualAttribs.rect);
            if (r != null) 
                TopFrame.getInstance().setBounds(r);
        }
        else
        {
            if (CellUtils.isMinimized(element))
                return GUIHelper.getMinimizedUI(element);
            canvas = new PiccoloCanvas(true);
            //GUIHelper.handleTitle(element,  canvas);
            //canvas.setBorder(new MatteBorder(1, 1, 1, 1, Color.blue));
        }
        element.setVisualInstance(canvas);
        for (int i = 0; i < group.getArity(); i++)
            try
            {
                addChild(canvas, group.getTargetAt(i));
            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }
        if (canvas != null) canvas.relayout();
        group.setVisualInstance(canvas);
        CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE, ThisNiche
                .handleOf(element), getHandle(), getHandle());
        if(isTopContainer(element) && maximized != null)
        {
            final PSwingNode ps = canvas.getPSwingNodeForHandle(maximized);
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    canvas.maximize(ps);
                }
            });
        }
        return canvas;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            handleEvent((CellGroupChangeEvent) event);
        }
        else if (eventType.equals(EvalCellEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            rebind((EvalCellEvent) event, publisher);
        }
        else if (eventType.equals(AttributeChangeEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            handleAttrEvent((AttributeChangeEvent) event, publisher);
        }
    }

    private void addChild(final PiccoloCanvas top_canvas, HGHandle childH)
    {
        final CellGroupMember x = ThisNiche.hg.get(childH);
        if(x == null) return;
        if(CellUtils.isMaximized(x)) 
            maximized = childH;
        CellVisual visual = CellUtils.getVisual(x);
        final JComponent comp = visual.bind(x);
        if (comp != null)
        {
           // String title = CellUtils.getName(x);
            if (comp instanceof PiccoloCanvas)
            {
                PiccoloCanvas canvas = (PiccoloCanvas) comp;
                PScrollPane scroll = new PScrollPane(canvas);
                scroll.setBorder(new MatteBorder(1, 1, 1, 1, Color.blue));
                PSwingNode ps = top_canvas.addComponent(scroll, x);
                if(ps == null) return;
                PCamera camera = new PCamera();
                ps.addChild(camera);
                camera.addLayer(canvas.getNodeLayer());
                canvas.setCamera(camera);
                scroll.getViewport().setView(canvas);
                scroll.setTransferHandler(canvas.getTransferHandler());
            }
            else
               top_canvas.addComponent(comp, x);
           // if(title != null) GUIHelper.handleTitle(x,  comp);
               
        }
        CellUtils.addEventPubSub(EvalCellEvent.HANDLE, childH, getHandle(),
                getHandle());
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, childH,
                getHandle(), getHandle());
    }

    private void rebind(EvalCellEvent event, HGHandle publisher)
    {
        HGHandle h = publisher;
        CellGroup group = CellUtils.getParentGroup(h);
        //System.out.println("rebind: " + h + ":" + group);
        PiccoloCanvas canvas = getCanvas(group);
        PSwingNode ps = canvas.getPSwingNodeForHandle(h);
        if (ps != null) ps.removeFromParent();
        addChild(canvas, h);
    }
    
    //TODO: this is not very correct in general
    private PiccoloCanvas getCanvas(CellGroupMember cm)
    {
        JComponent c = (JComponent) cm.getVisualInstance();
        if(c == null || GUIHelper.getPSwingNode(c) == null) 
            return TopFrame.getInstance().getCanvas();        
        if(c instanceof PiccoloCanvas) 
            return (PiccoloCanvas) c;
        if(c == null || GUIHelper.getPSwingNode(c) == null) 
            return TopFrame.getInstance().getCanvas();
        return GUIHelper.getPSwingNode(c).getCanvas();
    }

    private void handleAttrEvent(AttributeChangeEvent event, HGHandle publisher)
    {
        CellGroup group = CellUtils.getParentGroup(publisher);
        if(group == null) return;
        PiccoloCanvas canvas = getCanvas(group);
        PSwingNode ps = canvas.getPSwingNodeForHandle(publisher);
        if (event.getName().equals(VisualAttribs.minimized)
                || event.getName().equals(BaseCellGroupMember.VISUAL_HANDLE_KEY))
        {
            if (ps != null) 
                ps.removeFromParent();
            addChild(canvas, publisher);
        }
        else if (event.getName().equals(VisualAttribs.maximized))
        {
            if(((Boolean)event.getValue()).booleanValue())
            {
               canvas.maximize(ps);
            }
            else
            {
               canvas.unmaximize(ps); 
            } 
        }else if(event.getName().equals(VisualAttribs.showTitle)
                || event.getName().equals(VisualAttribs.name))
        {
            if(ps == null) return;
            GUIHelper.handleTitle(ps);
        }
    }

    private void handleEvent(CellGroupChangeEvent e)
    {
        CellGroup group = ThisNiche.hg.get(e.getCellGroup());
        if (!(group.getVisualInstance() instanceof PiccoloCanvas)) return;
        PiccoloCanvas canvas = (PiccoloCanvas) group.getVisualInstance();
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        if (removed != null && removed.length > 0)
            for (int i = 0; i < removed.length; i++)
            {
                PSwingNode ps = canvas.getPSwingNodeForHandle(removed[i]);
                if (ps != null) ps.removeFromParent();
                CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, removed[i],
                        getHandle(), getHandle());
                CellUtils.removeEventPubSub(AttributeChangeEvent.HANDLE, removed[i],
                        getHandle(), getHandle());
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
