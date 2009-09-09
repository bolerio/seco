package seco.gui;

import java.awt.Component;
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
import seco.gui.layout.LayoutHandler;
import seco.gui.piccolo.PToolTipHandler;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;
import edu.umd.cs.piccolox.pswing.PSwingEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

/*
 * Main Piccolo container.
 */
public class PiccoloCanvas extends PSwingCanvas
{
    private static final long serialVersionUID = 8650227944556777541L;
    private static PSwingNodeFilter ps_filter = new PSwingNodeFilter();

    PLayer nodeLayer;
    PCSelectionHandler selectionHandler;
    ContextMenuHandler ctxMenuHandler;
    boolean nested;

    public PiccoloCanvas()
    {
        this(false);
    }

    public PiccoloCanvas(boolean nested)
    {
        updatePSwingEventHandler(nested);
        init();
        this.nested = nested;
    }

    void updatePSwingEventHandler(boolean nested)
    {
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
        removePSwingEventHandler();
        new PSwingEventHandlerEx(this, getCamera()).setActive(true);
        getCamera().addInputEventListener(new PToolTipHandler(getCamera()));
    }

    private void init()
    {
        setTransferHandler(new PCTransferHandler(this));
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

        getLayer().addChild(nodeLayer);
        setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        selectionHandler = new PCSelectionHandler();
        // getLayer(), getNodeLayer(), getCamera());
        selectionHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON1_MASK));
        addInputEventListener(selectionHandler);
        getRoot().getDefaultInputManager().setKeyboardFocus(selectionHandler);

        setPanEventHandler(null);
        PZoomEventHandler zoomer = getZoomEventHandler();
        if (zoomer != null)
        {
            zoomer.setMinScale(.25);
            zoomer.setMaxScale(3);
        }
        setZoomEventHandler(zoomer);

        ctxMenuHandler = new ContextMenuHandler();
        ctxMenuHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON3_MASK));
        addInputEventListener(ctxMenuHandler);
    }

    void removePSwingEventHandler()
    {
        if (getCamera().getListenerList() != null)
        {
            PInputEventListener[] list = getCamera().getListenerList()
                    .getListeners(PInputEventListener.class);
            for (PInputEventListener l : list)
                if (l instanceof PSwingEventHandler
                        || l instanceof PSwingEventHandlerEx)
                    getCamera().removeInputEventListener(l);
        }
    }

    public void relayout()
    {
        for (int i = 0; i < getCamera().getChildrenCount(); i++)
        {
            PNode o = getCamera().getChild(i);
            if (!(o instanceof PSwingNode)) continue;
            LayoutHandler vh = GUIHelper.getLayoutHandler((PSwingNode) o);
            if (vh != null) vh.layout(PiccoloCanvas.this, (PSwingNode) o);
        }

    }

    public HGHandle getGroupH()
    {
        PSwingNode p = GUIHelper.getPSwingNode(this);
        return p != null ? p.getHandle() : ThisNiche.TOP_CELL_GROUP_HANDLE;
    }

    public void deleteSelection()
    {
        for (PNode node : selectionHandler.getSelection())
        {
            if (!(node instanceof PSwingNode)) continue;
            PSwingNode outer = GUIHelper.getPSwingNode(((PSwingNode) node)
                    .getCanvas());
            HGHandle groupH = (outer != null) ? outer.getHandle() : getGroupH();
            GUIHelper.removeFromCellGroup(groupH, ((PSwingNode) node)
                    .getHandle(), false);
            //Object ui = ((PSwingNode) node).getComponent();
            //if (ui instanceof NotebookUI) remove_and_clean((NotebookUI) ui);
            node.removeFromParent();
        }
    }
    
    private void remove_and_clean(NotebookUI ui)
    {
        NotebookDocument doc = ui.getDoc();
        HGHandle h = ThisNiche.handleOf(ui);
        if (h != null) ThisNiche.hg.remove(h, true);
        //CellUtils.removeHandlers(doc.getBookHandle());
        CellUtils.removeHandlers(doc.getHandle());
        ThisNiche.hg.remove(doc.getHandle(), true);
    }

    public Collection<PNode> getSelection()
    {
        return selectionHandler.getSelection();
    }

    public PSwingNode getSelectedPSwingNode()
    {
        if (nested)
            return TopFrame.getInstance().getCanvas().getSelectedPSwingNode();
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
        {
            if (CellUtils.isMinimized(cm)) return;
            cm.setAttribute(VisualAttribs.rect, p.getFullBounds().getBounds());
        }
    }

    // public void addCopyComponent(HGHandle h, HGHandle masterH, Point pt)
    // {
    // Object nb = ThisNiche.hg.get(h);
    // if (nb instanceof Cell)
    // {
    // HGHandle par = CellUtils.getOutCellParent(masterH);
    // if (par == null) // normal cell
    // {
    // par = masterH;
    // CellUtils.addCopyListeners(h, masterH);
    // }
    // // TODO: should we remove the Cell from parent or not?
    // // CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, par,
    // // HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
    // else
    // {
    // CellUtils.addEventPubSub(EvalCellEvent.HANDLE, par,
    // ((NotebookUI) comp).getDoc().getHandle(),
    // OutputCellDocument.CopyEvalCellHandler.getInstance());
    // }
    // }
    // else
    // {
    // CellUtils.addCopyListeners(h, masterH);
    // }
    //
    // GUIHelper.addToTopCellGroup(h,
    // VisualsManager.defaultVisualForType(h), ps.getFullBounds()
    // .getBounds()));
    //        
    // }

    // public PSwingNode addComponent(HGHandle h, Point pt)
    // {
    // HGHandle topH = GUIHelper.addToTopCellGroup(h);
    // Cell top = ThisNiche.hg.get(topH);
    // CellVisual vis = CellUtils.getVisual(top);
    // JComponent comp = vis.bind(top);
    // PSwingNode ps = new PSwingNode(this, comp);
    // ps.setHandle(topH);
    // getNodeLayer().addChild(ps);
    // ps.translate(pt.x, pt.y);
    // return ps;
    // }

    public PSwingNode getPSwingNodeForHandle(HGHandle h)
    {
        for (PSwingNode n : getNodes())
        {
            if ((h.equals(n.getHandle()))) return n;
            PSwingNode inner = check_inner_canvas(n, h);
            if (inner != null) return inner;
        }
        for (PSwingNode n : getFixedNodes())
        {
            if (h.equals(n.getHandle())) return n;
            PSwingNode inner = check_inner_canvas(n, h);
            if (inner != null) return inner;
        }
        return null;
    }

    public PSwingNode check_inner_canvas(PSwingNode n, HGHandle h)
    {
        if (n.getComponent() instanceof PScrollPane)
        {
            Component c = ((PScrollPane) n.getComponent()).getViewport()
                    .getView();
            if (c instanceof PiccoloCanvas)
                return ((PiccoloCanvas) c).getPSwingNodeForHandle(h);
        }
        return null;
    }

    public void maximize(PSwingNode n)
    {
        if (n == null) return;
        for (int i = 0; i < getNodeLayer().getChildrenCount(); i++)
        {
            PNode o = getNodeLayer().getChild(i);
            if (!o.equals(n)) o.setVisible(false);
        }
        for (int i = 0; i < getCamera().getChildrenCount(); i++)
        {
            PNode o = getCamera().getChild(i);
            if (!o.equals(n)) o.setVisible(false);
        }

        n.moveToFront();
        n.setBounds(0, 0, getWidth() - 60, getHeight() - 60);
        PBounds b = n.getFullBounds();
        n.translate(-b.x + 20, -b.y + 20);
    }

    public void showAllNodes()
    {
        for (int i = 0; i < getNodeLayer().getChildrenCount(); i++)
            getNodeLayer().getChild(i).setVisible(true);
        for (int i = 0; i < getCamera().getChildrenCount(); i++)
            getCamera().getChild(i).setVisible(true);
        relayout();
    }

    public PSwingNode addComponent(JComponent comp, CellGroupMember cell)
    {
        HGHandle cellH = ThisNiche.handleOf(cell);
        PSwingNode p = null;
        try
        {
            p = new PSwingNode(this, comp, cellH);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
        p.setTooltip((String) comp.getClientProperty("tooltip"));

        LayoutHandler vh = (LayoutHandler) cell
                .getAttribute(VisualAttribs.layoutHandler);
        if (vh != null)
        {
            getCamera().addChild(p);
            vh.layout(this, p);
        }
        else
        {
            getNodeLayer().addChild(p);
            placeNode(p, true);
        }
        GUIHelper.handleTitle(p);
        comp.revalidate();
        return p;
    }

    void placeNode(PSwingNode p, boolean newly_added)
    {
        if (!newly_added)
        {
            Rectangle b = p.getFullBounds().getBounds();
            p.translate(-b.x, -b.y);
        }
        CellGroupMember cell = ThisNiche.hg.get(p.getHandle());
        boolean minim = (CellUtils.isMinimized(cell));
        Rectangle r = (Rectangle) cell.getAttribute(VisualAttribs.rect);
        Dimension dim = (minim) ? GUIHelper.getMinimizedUISize() : p
                .getComponent().getPreferredSize();
        if (r != null)
        {
            normalize(r);
            adjust_bounds(r);
            if (minim)
            {
                p.setHeight(dim.getHeight());
                p.setWidth(dim.getWidth());
                Point pt = (Point) cell.getAttribute(VisualAttribs.minPt);
                if(pt == null) pt = new Point(r.x, r.y);
                p.translate(pt.x, pt.y);
            }
            else
            {
                p.setBounds(0, 0, r.width, r.height);
                p.translate(r.x, r.y);
            }
        }
        else
            p.setBounds(new Rectangle(0, 0, dim.width, dim.height));
    }

    private void adjust_bounds(Rectangle r)
    {
        if (!nested) return;
        PSwingNode canv_node = GUIHelper.getPSwingNode(this);
        if (canv_node == null) return;
        PBounds fb = canv_node.getFullBounds();
        // System.out.println("dispatchEvent0: " + pt + ":" + fb);
        r.x = (int) (r.x - fb.x);
        r.y = (int) (r.y - fb.y);
    }

    // TODO: temp solution during testing
    private void normalize(Rectangle r)
    {
        // System.out.println("normalize1: " + r);
        if (r.x < 0) r.x = 0;
        if (r.x > 1000) r.x = 1000;
        if (r.y < 0) r.y = 0;
        if (r.y > 1000) r.y = 1000;
        if (r.width > 1000) r.width = 1000;
        if (r.width <= 0) r.width = 50;
        if (r.height > 1000) r.height = 1000;
        if (r.height <= 0) r.height = 50;
        // System.out.println("normalize2: " + r);
    }

       public PLayer getNodeLayer()
    {
        return nodeLayer;
    }

    public Collection<PSwingNode> getNodes()
    {
        return (Collection<PSwingNode>) nodeLayer.getAllNodes(ps_filter, null);
    }

    public Collection<PSwingNode> getFixedNodes()
    {
        return (Collection<PSwingNode>) getCamera()
                .getAllNodes(ps_filter, null);
    }

    private static class PSwingNodeFilter implements PNodeFilter
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

    public ContextMenuHandler getContextMenuHandler()
    {
        return ctxMenuHandler;
    }

    public PCSelectionHandler getSelectionHandler()
    {
        return selectionHandler;
    }
}