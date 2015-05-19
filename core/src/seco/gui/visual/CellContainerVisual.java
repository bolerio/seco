package seco.gui.visual;

import java.awt.Color;

import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.swing.PScrollPane;
import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellGroupChangeEvent;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.gui.GUIHelper;
import seco.gui.OutputConsole;
import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.things.BaseCellGroupMember;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.util.GUIUtil;

public class CellContainerVisual implements CellVisual, GroupVisual, EventHandler
{
    private static final HGPersistentHandle handle =  UUIDHandleFactory.I.makeHandle
            ("cc88ae4c-f70b-4536-814c-95a6ac6a7b62");

    private HGHandle maximized = null;
    
    public static HGPersistentHandle getHandle()
    {
        return handle;
    }

    public JComponent bind(CellGroupMember element)
    {
        CellGroup group = (CellGroup) element;
        HGHandle elementH = ThisNiche.handleOf(element);
        final PiccoloCanvas canvas;
        // container canvas....
        if (isTopContainer(element))
        {
            maximized = null;
            canvas = ThisNiche.getCanvas();
            Rectangle r = (Rectangle) element.getAttribute(VisualAttribs.rect);
            if (r != null && GUIUtil.getFrame() != null) 
                GUIUtil.getFrame().setBounds(r);
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
        CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE, elementH, getHandle(), getHandle());
        if(isTopContainer(element) && maximized != null)
        {
            final PSwingNode ps = canvas.getPSwingNodeForHandle(maximized);
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    canvas.maximizeNode(ps);
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
        final CellGroupMember x = ThisNiche.graph.get(childH);
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
            else if(top_canvas != null)
            {
                PSwingNode ps = top_canvas.getPSwingNodeForHandle(childH);
                if (ps != null)
                    ps.removeFromParent(); 
                 top_canvas.addComponent(comp, x);
            }
           // if(title != null) GUIHelper.handleTitle(x,  comp);
        }
        CellUtils.addEventPubSub(EvalCellEvent.HANDLE, 
        						 childH, 
        						 getHandle(),
        						 getHandle());
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, 
        						 childH,
        						 getHandle(), 
        						 getHandle());
    }

    private void rebind(EvalCellEvent event, HGHandle publisher)
    {
        HGHandle h = publisher;
        CellGroup group = CellUtils.getParentGroup(h);
        //System.out.println("rebind: " + h + ":" + group);
        PiccoloCanvas canvas = getCanvas(group);
        addChild(canvas, h);
    }
    
    //TODO: this is not very correct in general
    private PiccoloCanvas getCanvas(CellGroupMember cm)
    {
        JComponent c = (JComponent) cm.getVisualInstance();
        if(c == null || GUIHelper.getPSwingNode(c) == null) 
            return ThisNiche.getCanvas();        
        if(c instanceof PiccoloCanvas) 
            return (PiccoloCanvas) c;
        if(c == null || GUIHelper.getPSwingNode(c) == null) 
            return ThisNiche.getCanvas();
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
               canvas.maximizeNode(ps);
            }
            else
            {
               canvas.unmaximizeNode(ps); 
            } 
        }else if(event.getName().equals(VisualAttribs.showTitle)
                || event.getName().equals(VisualAttribs.name))
        {
            if(ps == null) return;
            GUIHelper.updateTitle(ps);
        }
    }

    private void handleEvent(CellGroupChangeEvent e)
    {
        CellGroup group = ThisNiche.graph.get(e.getCellGroup());
        if (!(group.getVisualInstance() instanceof PiccoloCanvas)) return;
        PiccoloCanvas canvas = (PiccoloCanvas) group.getVisualInstance();
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        if (removed != null && removed.length > 0)
            for (int i = 0; i < removed.length; i++)
            {
                //TODO: move away this check
                CellGroupMember cgm = ThisNiche.graph.get(removed[i]);
                if(cgm instanceof Cell && 
                        GUIHelper.OUTPUT_CONSOLE_HANDLE.equals(((Cell)cgm).getAtomHandle()))
                    ((OutputConsole)GUIHelper.getOutputConsole()).restoreOldIO();
                
                PSwingNode ps = canvas.getPSwingNodeForHandle(removed[i]);
                if (ps != null) ps.removeFromParent();
                //if(removed[i])
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
