package seco.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.gui.layout.LayoutHandler;
import seco.gui.piccolo.PToolTipHandler;
import seco.notebook.OutputCellDocument;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;
import edu.umd.cs.piccolox.pswing.PSwingEventHandler;

public class PiccoloCanvas extends PSwingCanvas
{
    private static final long serialVersionUID = 8650227944556777541L;

    PLayer nodeLayer;
    PCSelectionHandler selectionHandler;

    private void init()
    {
        nodeLayer = new PLayer() {
            public void addChild(int index, PNode child)
            {
                PNode oldParent = child.getParent();
                if (oldParent != null) oldParent.removeChild(child);
                child.setParent(this);
                getChildrenReference().add(index, child);
                child.invalidatePaint();
                invalidateFullBounds();
                firePropertyChange(0, PROPERTY_CHILDREN, null,
                        getChildrenReference());
            }
        };
        final PCamera camera = getCamera();
        camera.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                       relayout();
                    }
                });
        // camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM,
        // new PropertyChangeListener() {
        // public void propertyChange(PropertyChangeEvent evt)
        // {
        // PAffineTransform t = (PAffineTransform) evt
        // .getNewValue();
        // }
        // });

        getLayer().addChild(nodeLayer);
        setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        selectionHandler = new PCSelectionHandler();
                //getLayer(), getNodeLayer(), getCamera());
        selectionHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON1_MASK));
        addInputEventListener(selectionHandler);
        getRoot().getDefaultInputManager().setKeyboardFocus(selectionHandler);
       
       setPanEventHandler(null);
       PZoomEventHandler zoomer = getZoomEventHandler();
        if(zoomer != null)
        {
            zoomer.setMinScale(.25);
            zoomer.setMaxScale(3);
        }
        setZoomEventHandler(zoomer);
        
        ContextMenuHandler ctxMenuHandler = new ContextMenuHandler();
        ctxMenuHandler.setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
        addInputEventListener(ctxMenuHandler);
        getCamera().addInputEventListener(new PToolTipHandler(getCamera()));
    }

    public PiccoloCanvas()
    {
        //new PSwingEventHandler(this, getCamera()).setActive(true);
        //ugly way to remove parent PSwingEventHandler
        PInputEventListener[] list = getCamera().getListenerList().getListeners(PInputEventListener.class);
        for(PInputEventListener l: list)
            if(l instanceof PSwingEventHandler)
                getCamera().removeInputEventListener(l);
        //then add our fixed one
        new PSwingEventHandlerEx(this, getCamera()).setActive(true);
        init();
    }

    public void relayout()
    {
        for (Object o : getCamera().getChildrenReference())
        {
            if (!(o instanceof PSwingNode)) continue;
            LayoutHandler vh = GUIHelper.getLayoutHandler((PSwingNode) o);
            if (vh != null) vh.layout(PiccoloCanvas.this,(PSwingNode) o);
        }
    }
    
    public void deleteSelection()
    {
        for (PNode node : selectionHandler.getSelection())
        {
            if (node instanceof PSwingNode && false) continue;
            GUIHelper.removeFromTopCellGroup(((PSwingNode) node).getHandle());
            Object ui = ((PSwingNode) node).getComponent();
            if (ui instanceof NotebookUI) remove_and_clean((NotebookUI) ui);
            node.removeFromParent();
        }
    }

    public Collection<PNode> getSelection()
    {
        return selectionHandler.getSelection();
    }
    
    public PSwingNode getSelectedPSwingNode()
    {
        return selectionHandler.getSelectedPSwingNode();
    }
    
    public void saveDims()
    {
        for (Object o : getNodeLayer().getAllNodes())
            set_size_attrib(o);
        for (Object o : getCamera().getAllNodes())
            set_size_attrib(o);
    }

    private void set_size_attrib(Object o)
    {
        if (!(o instanceof PSwingNode)) return;
        PSwingNode p = (PSwingNode) o;
        CellGroupMember cm = (CellGroupMember) ThisNiche.hg.get(p.getHandle());
        if (cm != null)
            cm.setAttribute(VisualAttribs.rect, p.getFullBounds().getBounds());
    }

//    public void addCopyComponent(HGHandle h, HGHandle masterH, Point pt)
//    {
//        Object nb = ThisNiche.hg.get(h);
//        if (nb instanceof Cell)
//        {
//            HGHandle par = CellUtils.getOutCellParent(masterH);
//            if (par == null) // normal cell
//            {
//                par = masterH;
//                CellUtils.addCopyListeners(h, masterH);
//            }
//            // TODO: should we remove the Cell from parent or not?
//            // CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, par,
//            // HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
//            else
//            {
//                CellUtils.addEventPubSub(EvalCellEvent.HANDLE, par,
//                        ((NotebookUI) comp).getDoc().getHandle(),
//                        OutputCellDocument.CopyEvalCellHandler.getInstance());
//            }
//        }
//        else
//        {
//            CellUtils.addCopyListeners(h, masterH);
//        }
//
//        GUIHelper.addToTopCellGroup(h,
//                    VisualsManager.defaultVisualForType(h), ps.getFullBounds()
//                            .getBounds()));
//        
//    }
    
//    public PSwingNode addComponent(HGHandle h, Point pt)
//    {
//        HGHandle topH = GUIHelper.addToTopCellGroup(h);
//        Cell top = ThisNiche.hg.get(topH);
//        CellVisual vis = CellUtils.getVisual(top);
//        JComponent comp = vis.bind(top);
//        PSwingNode ps = new PSwingNode(this, comp);
//        ps.setHandle(topH);
//        getNodeLayer().addChild(ps);
//        ps.translate(pt.x, pt.y);
//        return ps;
//   }

    public PSwingNode getPSwingNodeForHandle(HGHandle h)
    {
        for(Object p: getNodeLayer().getAllNodes())
            if(p instanceof PSwingNode &&
                    (h.equals(((PSwingNode)p).getHandle())))
                return (PSwingNode)p;
        for(Object p: getCamera().getAllNodes())
            if(p instanceof PSwingNode &&
                    (h.equals(((PSwingNode)p).getHandle())))
                return (PSwingNode)p; 
        return null;
    } 
    
    public PSwingNode getOutCellNodeForHandle(HGHandle h)
    {
        for(Object p: getNodeLayer().getAllNodes())
            if(p instanceof PSwingNode &&
                    check_is_output((PSwingNode)p, h))
                        return (PSwingNode)p;
        for(Object p: getCamera().getAllNodes())
            if(p instanceof PSwingNode &&
                    check_is_output((PSwingNode)p, h))
                        return (PSwingNode)p;
        return null;
    } 
    
    private boolean check_is_output(PSwingNode p, HGHandle h)
    {
        if(h.equals(p.getHandle())) return true;
        CellGroupMember cgm = ThisNiche.hg.get(p.getHandle());
        if(cgm instanceof Cell && ((Cell) cgm).getAtomHandle().equals(h))
               return true;
        return false;
    }
    
    public PSwingNode addComponent(JComponent comp, CellGroupMember cell)
    {
        HGHandle cellH = ThisNiche.handleOf(cell);
        PSwingNode p = null;
        p = new PSwingNode(this, comp, cellH);
        LayoutHandler vh = (LayoutHandler) 
           cell.getAttribute(VisualAttribs.layoutHandler);
        if (vh != null)
        {
            getCamera().addChild(p);
            vh.layout(this, p);
        }
        else
        {
            getNodeLayer().addChild(p);
            Rectangle r = (Rectangle) cell.getAttribute(VisualAttribs.rect);
            Dimension dim = comp.getPreferredSize();
            if (r != null)
            {
                normalize(r);
//                if(r.height == 0 || r.width==0);
//                {
//                    
//                    r.width = Math.max(20, dim.width); 
//                    r.width = Math.min(r.width, 500); 
//                    r.height = Math.max(20, dim.height);
//                    r.height = Math.min(r.height, 400);
//                }
                p.setBounds(r);
                p.translate(r.x, r.y);
            }else
                p.setBounds(new Rectangle(0, 0 , dim.width, dim.height));
        }
        return p;
    }
    
    //TODO: temp solution during testing
    private void normalize( Rectangle r)
    {
        //System.out.println("normalize1: " + r);
        if(r.x < 0) r.x = 0;
        if(r.x > 1000) r.x = 1000;
        if(r.y < 0) r.y = 0;
        if(r.y > 1000) r.y = 1000;
        if(r.width > 1000) r.width = 1000;
        if(r.width <= 0) r.width =50;
        if(r.height > 1000) r.height = 1000;
        if(r.height <= 0) r.height =50;
        //System.out.println("normalize2: " + r);
    }
  
    private void remove_and_clean(NotebookUI ui)
    {
        NotebookDocument doc = ui.getDoc();
        HGHandle h = ThisNiche.handleOf(ui);
        if (h != null) ThisNiche.hg.remove(h);
        CellUtils.removeHandlers(doc.getBookHandle());
        CellUtils.removeHandlers(doc.getHandle());
        ThisNiche.hg.remove(doc.getHandle());
    }

    public PLayer getNodeLayer()
    {
        return nodeLayer;
    }


    private static class PSwingFilter implements PNodeFilter
    {

        public boolean accept(PNode node)
        {
            return node instanceof PSwingNode;
        }

        public boolean acceptChildrenOf(PNode node)
        {
            return true;
        }
    }
   
}