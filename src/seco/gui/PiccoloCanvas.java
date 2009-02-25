package seco.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JComponent;
import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.gui.layout.LayoutHandler;
import seco.notebook.OutputCellDocument;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.piccolo.ScribaSelectionHandler;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

public class PiccoloCanvas extends PSwingCanvas
{
    private static final long serialVersionUID = 8650227944556777541L;

    PLayer nodeLayer;
    ScribaSelectionHandler selectionHandler;

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
        selectionHandler = new ScribaSelectionHandler(
                getLayer(), getNodeLayer(), getCamera());
        selectionHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON1_MASK));
        addInputEventListener(selectionHandler);
        // TODO: this should be handled better, but ...
       addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == KeyEvent.VK_DELETE)
                {
                    removeComponents(selectionHandler.getSelection());
                }
            }
        });
        
        setPanEventHandler(null);
        //setZoomEventHandler(null);
        PZoomEventHandler zoomer = getZoomEventHandler();
        if(zoomer != null)
        {
            zoomer.setMinScale(.25);
            zoomer.setMaxScale(3);
            //zoomer.setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
        }
        setZoomEventHandler(zoomer);
        
        ContextMenuHandler ctxMenuHandler = new ContextMenuHandler();
        ctxMenuHandler.setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
        addInputEventListener(ctxMenuHandler);
    }

    public PiccoloCanvas()
    {
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
    
    public void hardcoded_pin()
    {
        for(PNode node : getSelection())
        {
            
        }
    }
    
    private void deleteSelection()
    {
        removeComponents(selectionHandler.getSelection());
    }

    public Collection<PNode> getSelection()
    {
        return selectionHandler.getSelection();
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

    public void addCopyComponent(HGHandle h, HGHandle masterH, Point pt)
    {
        Object nb = ThisNiche.hg.get(h);
        JComponent comp = null;
        if (nb instanceof Cell)
        {
            comp = new NotebookUI(h);
            comp.setPreferredSize(new Dimension(200, 200));
            HGHandle par = CellUtils.getOutCellParent(masterH);
            if (par == null) // normal cell
            {
                par = masterH;
                CellUtils.addCopyListeners(h, masterH);
            }
            // TODO: should we remove the Cell from parent or not?
            // CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, par,
            // HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
            else
            {
                CellUtils.addEventPubSub(EvalCellEvent.HANDLE, par,
                        ((NotebookUI) comp).getDoc().getHandle(),
                        OutputCellDocument.CopyEvalCellHandler.getInstance());
            }
        }
        else
        {
            comp = new NotebookUI(h);
            comp.setPreferredSize(new Dimension(200, 200));
            CellUtils.addCopyListeners(h, masterH);
        }

        if (comp != null)
        {
            PSwingNode ps = new PSwingNode(this, comp);
            // ps.addInputEventListener(del_handler);
            getNodeLayer().addChild(ps);
            adjust_place(ps, pt, true);
            h = CellUtils.addSerializable(comp);
            ps.setHandle(GUIHelper.addToTopCellGroup(ThisNiche.hg, h,
                    VisualsManager.defaultVisualForType(h), ps.getFullBounds()
                            .getBounds()));
        }
    }
    
    public PSwingNode addComponent(HGHandle h, Point pt)
    {
        JComponent comp = new NotebookUI(h);
        comp.setPreferredSize(new Dimension(200, 200));
        PSwingNode ps = new PSwingNode(this, comp);
        // ps.addInputEventListener(del_handler);
        getNodeLayer().addChild(ps);
        adjust_place(ps, pt, true);
        h = CellUtils.addSerializable(comp);
        ps.setHandle(GUIHelper.addToTopCellGroup(ThisNiche.hg, h,
                VisualsManager.defaultVisualForType(h), 
                ps.getFullBounds().getBounds()));
        return ps;
   }

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
        Object ui = p.getComponent();
        if(ui instanceof NotebookUI && 
           ((NotebookUI)ui).getDoc() instanceof OutputCellDocument)
        {
            return h.equals(((NotebookUI)ui).getDoc().getBookHandle());
        }
        return false;
    }
    
    public PSwingNode addComponent(JComponent comp, CellGroupMember cell)
    {
        HGHandle cellH = ThisNiche.handleOf(cell);
        PSwingNode p = null;
        try{
         p = new PSwingNode(this, comp, cellH);
        }catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
       // System.out.println("PCanvas - addComponent1: " + p);
        LayoutHandler vh = (LayoutHandler) cell
                .getAttribute(VisualAttribs.layoutHandler);
        System.out.println("PCanvas - addComponent2: " + vh);
        if (vh != null)
        {
            getCamera().addChild(p);
            vh.layout(this, p);
        }
        else
        {
            getNodeLayer().addChild(p);
            Rectangle r = (Rectangle) cell.getAttribute(VisualAttribs.rect);
            if (r != null)
            {
                normalize(r);
                p.setBounds(r);
                p.translate(r.x, r.y);
            }
        }
        return p;
    }
    
    //TODO: temp solution during testing
    private void normalize( Rectangle r)
    {
        System.out.println("normalize1: " + r);
        if(r.x < 0) r.x = 0;
        if(r.x > 1000) r.x = 1000;
        if(r.y < 0) r.y = 0;
        if(r.y > 1000) r.y = 1000;
        if(r.width > 1000) r.width = 1000;
        if(r.width <= 0) r.width =50;
        if(r.height > 1000) r.height = 1000;
        if(r.height <= 0) r.height =50;
        System.out.println("normalize2: " + r);
    }
    // private static DelEventHandler del_handler = new DelEventHandler();

    void removeComponents(Collection<PNode> selection)
    {
        for (PNode node : selection)
        {
            if (node instanceof PSwingNode && false) // && !((PSwing0)
                // node).isDeleteable())
                continue;
            GUIHelper.removeFromTopCellGroup(((PSwingNode) node).getHandle());
            Object ui = ((PSwingNode) node).getComponent();
            if (ui instanceof NotebookUI) remove_and_clean((NotebookUI) ui);
            node.removeFromParent();
        }
    }

    void remove_and_clean(NotebookUI ui)
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

    public void adjust_place(PNode node, Point pt, boolean rigth_or_down)
    {
//        PBounds or = node.getGlobalFullBounds();
//        PBounds out = new PBounds(or);
//        ArrayList<Object> results = new ArrayList<Object>();
//        Object[] allnodes = node.getRoot().getAllNodes(new PSwingFilter(),
//                results).toArray();
//        boolean[] visited = new boolean[allnodes.length];
//        for (int i = 0; i < allnodes.length; i++)
//        {
//            PNode n = (PNode) allnodes[i];
//            PBounds b = n.getGlobalFullBounds();
//            if (n.fullIntersects(out) && !visited[i])
//            {
//                if (rigth_or_down) out.x = b.x + b.width + 10;
//                else
//                    out.y = b.y + b.height + 10;
//                visited[i] = true;
//                i = 0;
//            }
//        }
//        // node.setBounds(out);
//        node.translate(out.x, out.y);
        node.translate(pt.x, pt.y);
        node.setBounds(node.getBounds());
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

    private static class DelEventHandler extends PBasicInputEventHandler
    {
        @Override
        public void keyPressed(PInputEvent e)
        {
            System.out.println("PiccoloCanvas - keyPressed: " + e);
            if (e.getKeyCode() == KeyEvent.VK_DELETE)
                TopFrame.getInstance().getCanvas().deleteSelection();
        }
    }
}