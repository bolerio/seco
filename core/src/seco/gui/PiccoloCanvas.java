package seco.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.gui.layout.LayoutHandler;
import seco.gui.piccolo.AffineTransformEx;
import seco.gui.piccolo.PToolTipHandler;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.util.PAffineTransform;
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
    PSwingNode maximizedNode;

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
        selectionHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON1_MASK));
        addInputEventListener(selectionHandler);
        getRoot().getDefaultInputManager().setKeyboardFocus(selectionHandler);

        setPanEventHandler(null);

        ctxMenuHandler = new ContextMenuHandler();
        ctxMenuHandler.setEventFilter(new PInputEventFilter(
                InputEvent.BUTTON3_MASK));
        addInputEventListener(ctxMenuHandler);

        PZoomEventHandler zoomer = new MyZoomEventHandler(this);
        zoomer.setMinScale(.25);
        zoomer.setMaxScale(3);
        setZoomEventHandler(zoomer);

        getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        PAffineTransform tr = getCamera().getViewTransform();
                        HGHandle h = PiccoloCanvas.this.getGroupH();
                        if (h != null)
                        {
                            AffineTransformEx old = CellUtils.getZoom(h);
                            if (tr != null && tr.equals(old)) return;
                            CellUtils.setZoom(h, tr);
                        }
                    }
                });
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
        if (maximizedNode != null)
        {
            adjust_maximized_node();
            return;
        }
        for (int i = 0; i < getCamera().getChildrenCount(); i++)
        {
            PNode o = getCamera().getChild(i);
            if (!(o instanceof PSwingNode)) continue;
            LayoutHandler vh = CellUtils.getLayoutHandler(((PSwingNode) o)
                    .getHandle());
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
        if (selectionHandler.getSelection().isEmpty()) return;
        if (maximizedNode != null) unmaximizeNode(maximizedNode);
        for (PNode node : selectionHandler.getSelection())
        {
            if (!(node instanceof PSwingNode)) continue;
            PSwingNode outer = GUIHelper.getPSwingNode(((PSwingNode) node)
                    .getCanvas());
            HGHandle groupH = (outer != null) ? outer.getHandle() : getGroupH();
            GUIHelper.removeFromCellGroup(groupH, ((PSwingNode) node)
                    .getHandle(), true);
            // Object ui = ((PSwingNode) node).getComponent();
            // if (ui instanceof NotebookUI) remove_and_clean((NotebookUI) ui);
            node.removeFromParent();
        }
    }

    private void remove_and_clean(NotebookUI ui)
    {
        NotebookDocument doc = ui.getDoc();
        HGHandle h = ThisNiche.handleOf(ui);
        if (h != null) ThisNiche.graph.remove(h, true);
        // CellUtils.removeHandlers(doc.getBookHandle());
        CellUtils.removeHandlers(doc.getHandle());
        ThisNiche.graph.remove(doc.getHandle(), true);
    }

    public Collection<PNode> getSelection()
    {
        return selectionHandler.getSelection();
    }

    public PSwingNode getSelectedPSwingNode()
    {
        if (nested)
            return ThisNiche.getCanvas().getSelectedPSwingNode();
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
        CellGroupMember cm = (CellGroupMember) ThisNiche.graph.get(p
                .getHandle());
        if (cm != null)
        {
            if (CellUtils.isMinimized(cm)) return;
            cm.setAttribute(VisualAttribs.rect, p.getFullBounds().getBounds());
        }
    }

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

    public void maximizeNode(PSwingNode n)
    {
        if (n == null) return;
        if (n.getCanvas() != this)
        {
            n.getCanvas().maximizeNode(n);
            return;
        }
        if (maximizedNode != null) return;
        maximizedNode = n;
        for (int i = 0; i < getNodes().size(); i++)
        {
            PNode o = getNode(i);
            if (!o.equals(n)) o.setVisible(false);
        }
        // System.out.println("maximize: " + nested + ":" +
        // (n.getCanvas() == TopFrame.getInstance().getCanvas()));
        if (!nested) for (int i = 0; i < getFixedNodes().size(); i++)
        {
            PNode o = getFixedNode(i);
            if (!o.equals(n)) o.setVisible(false);
        }

        adjust_maximized_node();
        n.moveToFront();
        toggle_scroll_bars(false);
    }

   
    public void unmaximizeNode(PSwingNode n)
    {
        if (n.getCanvas() != this)
        {
            n.getCanvas().unmaximizeNode(n);
            return;
        }
        if (maximizedNode == null) return;
        maximizedNode = null;
        placeNode(n);
        showAllNodes(n);
        relayout();

        toggle_scroll_bars(true);
    }

    void showAllNodes(PSwingNode except)
    {
        for (int i = 0; i < getNodeLayer().getChildrenCount(); i++)
        {
            PNode n = getNodeLayer().getChild(i);
            n.setVisible(true);
            if (n instanceof PSwingNode)
                ((PSwingNode) n).getComponent().revalidate();
        }
        for (int i = 0; i < getCamera().getChildrenCount(); i++)
        {
            PNode n = getCamera().getChild(i);
            n.setVisible(true);
            if (n instanceof PSwingNode)
                ((PSwingNode) n).getComponent().revalidate();
        }
    }
    
    private static int offset_y = 2; //16;
    private static int offset_x = 2;//16;

    private void adjust_maximized_node()
    {
       // double w = (nested) ? getBounds().getWidth() :TopFrame.getInstance().getContentPane().getWidth();
      //  double h = (nested) ? getBounds().getHeight() :TopFrame.getInstance().getContentPane().getHeight();
        double w = getBounds().getWidth(); 
        double h = getBounds().getHeight();
        maximizedNode.setBounds(0, 0, w - offset_x, h - 2*offset_y);
        PBounds b = maximizedNode.getFullBounds();
        maximizedNode.translate(-b.x + offset_x / 2, -b.y + offset_y/ 2);
    }

    private void toggle_scroll_bars(boolean on)
    {
        JScrollPane scroll = (JScrollPane) getParent().getParent();
        if (!on)
        {

            if (scroll.getVerticalScrollBar().isShowing()
                    || scroll.getHorizontalScrollBar().isShowing())
            {
                JViewport vp = scroll.getViewport();
                if (vp != null && vp.getView() != null)
                    vp.setViewPosition(new Point(0, 0));
            }
            scroll
                    .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll
                    .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }
        else
        {
            scroll
                    .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll
                    .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
    }

    PSwingNode addComponent(JComponent comp, CellGroupMember cell)
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
            addFixedNode(p);
            vh.layout(this, p);
        }
        else
        {
            addNode(p);
            placeNode(p);
        }
        GUIHelper.handleTitle(p);
        comp.revalidate();
        // force the appearance of scroll bars if necessary
        if (nested) revalidate();
        return p;
    }

    void placeNode(PSwingNode p)
    {
        CellGroupMember cgm = ThisNiche.graph.get(p.getHandle());
        boolean minim = (CellUtils.isMinimized(cgm));
        Rectangle r = CellUtils.getAppropriateBounds(cgm);
        if (r != null)
        {
            normalize(r);
            p.setBounds(0, 0, r.width, r.height);
            p.translate(r.x, r.y);
            p.storeBounds(r);
            return;
        }

        Dimension dim = (minim) ? GUIHelper.getMinimizedUISize() : p
                .getComponent().getPreferredSize();
        if (minim && CellUtils.getBounds(cgm) != null)
        {
            p.setBounds(new Rectangle(0, 0, dim.width, dim.height));
            p.translate(CellUtils.getBounds(cgm).x, CellUtils.getBounds(cgm).y);
        }
        else
        {
            // on some components getPreferredSize() return weird stuff so...
            // at least show something rather well visible, that user could
            // resize later
            if (dim.width < 100 || dim.width > 1000) dim.width = 100;
            if (dim.height < 100 || dim.height > 800) dim.height = 100;

            p.setBounds(0, 0, dim.width, dim.height);
            if (nested) p.translate(20, 20);
            else
                p.translate(100, 100);
        }
    }

    // TODO: temp solution during testing
    private void normalize(Rectangle r)
    {
        // System.out.println("normalize1: " + r);
        // if(r.height == 18)
        // Thread.currentThread().dumpStack();
        if (r.x < 0) r.x = 60;
        if (r.x > 1000) r.x = 1000;
        if (r.y < 0) r.y = 60;
        if (r.y > 1000) r.y = 1000;
        if (r.width > 2000) r.width = 2000;
        if (r.width <= 0) r.width = 50;
        if (r.height > 1000) r.height = 1000;
        if (r.height <= 0) r.height = 50;
        // System.out.println("normalize2: " + r);
    }

    public PLayer getNodeLayer()
    {
        return nodeLayer;
    }

    public void addNode(PNode node)
    {
        getNodeLayer().addChild(node);
    }

    public void removeNode(PNode node)
    {
        getNodeLayer().removeChild(node);
    }

    public PNode getNode(int i)
    {
        return getNodeLayer().getChild(i);
    }

    public void addFixedNode(PNode node)
    {
        getCamera().addChild(node);
    }

    public void removeFixedNode(PNode node)
    {
        getCamera().removeChild(node);
    }

    public PNode getFixedNode(int i)
    {
        return getCamera().getChild(i);
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

    void removeAllNodes()
    {
        getCamera().removeAllChildren();
        getNodeLayer().removeAllChildren();
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

    static class MyZoomEventHandler extends PZoomEventHandler
    {
        PInputEvent original = null;
        PiccoloCanvas canvas;

        public MyZoomEventHandler(PiccoloCanvas canvas)
        {
            this.canvas = canvas;
        }

        @Override
        public void mousePressed(PInputEvent e)
        {
            if (accept((MouseEvent) e.getSourceSwingEvent())) original = e;
        }

        @Override
        public void mouseReleased(PInputEvent e)
        {
            e.getComponent().setInteracting(false);
            original = null;
        }

        private boolean accept(MouseEvent e)
        {
            return e.getButton() == MouseEvent.BUTTON3 && !e.isControlDown()
                    && !e.isShiftDown() && !e.isAltDown();
        }

        @Override
        public void mouseDragged(PInputEvent e)
        {
            if (original == null || // some node is under the mouse
                    !(e.getPath().getPickedNode() instanceof PCamera)) return;
            PCamera camera = (PCamera) e.getPath().getPickedNode();
            e.getComponent().setInteracting(true);
            super.mouseDragged(e);
            doZoom(camera, original, e);
        }

        protected void doZoom(PCamera camera, PInputEvent ref, PInputEvent now)
        {
            Point2D zoomPt = ref.getCanvasPosition();

            double dx = ((MouseEvent) ref.getSourceSwingEvent()).getX()
                    - ((MouseEvent) now.getSourceSwingEvent()).getX();
            double scaleDelta = (1.0 + (0.001 * dx));
            double currentScale = camera.getViewScale();
            double newScale = currentScale * scaleDelta;

            if (newScale < getMinScale())
                scaleDelta = getMinScale() / currentScale;

            if ((getMaxScale() > 0) && (newScale > getMaxScale()))
                scaleDelta = getMaxScale() / currentScale;

            camera
                    .scaleViewAboutPoint(scaleDelta, zoomPt.getX(), zoomPt
                            .getY());
            original = now;
        }
    }
}