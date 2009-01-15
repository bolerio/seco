package seco.notebook;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.gui.VisualAttribs;
import seco.gui.VisualsManager;
import seco.notebook.PiccoloFrame.PSwing0;
import seco.notebook.piccolo.ScribaSelectionHandler;
import seco.notebook.piccolo.pswing.PSwing;
import seco.notebook.piccolo.pswing.PSwingCanvas;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;

public class PiccoloCanvas extends PSwingCanvas
{
	private static final long serialVersionUID = 8650227944556777541L;
	// private static final String DIM_MAP = "PiccoloCanvas.CompMap";
    // private final Map<HGHandle, BeanRect> compMap = new HashMap<HGHandle,
    // BeanRect>();
    // private Set<PSwing0> comps = new HashSet<PSwing0>();

    PLayer nodeLayer;
    ScribaSelectionHandler sel_handler;

    private void init()
    {
        setPanEventHandler(null);
        nodeLayer = new PLayer() {
            public void addChild(int index, PNode child)
            {
                PNode oldParent = child.getParent();
                if (oldParent != null)
                {
                    oldParent.removeChild(child);
                }
                child.setParent(this);
                getChildrenReference().add(index, child);
                child.invalidatePaint();
                invalidateFullBounds();
                firePropertyChange(0, PROPERTY_CHILDREN, null,
                        getChildrenReference());
            }
        };
        setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getLayer().addChild(nodeLayer);
        sel_handler = new ScribaSelectionHandler(getLayer(), getNodeLayer(),
                getCamera());
        addInputEventListener(sel_handler);
        // TODO: this should be handled better, but ...
        addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == KeyEvent.VK_DELETE)
                {
                    System.out.println("keyTyped: " + e);
                    removeComponents(sel_handler.getSelection());
                }
            }
        });
    }

    public PiccoloCanvas()
    {
        init();
    }

    public PiccoloCanvas(PNode parent)
    {
        super(parent);
        init();
    }

    private void deleteSelection()
    {
        removeComponents(sel_handler.getSelection());
    }

    // public void loadDims()
    // {
    // Map<HGHandle, BeanRect> map =
    // (Map<HGHandle, BeanRect>) AppConfig.getInstance().getProperty(DIM_MAP);
    // if (map == null) return;
    // PLayer nodeLayer = getNodeLayer();
    // System.out.println("PiccoloCanvas - loadDims: " + map);
    // for (HGHandle h : map.keySet())
    // {
    // JComponent comp = (JComponent) ThisNiche.hg.get(h);
    // if (comp == null) return;
    // Rectangle r = map.get(h).getRect();
    // comp.setPreferredSize(new Dimension(r.width, r.height));
    // PSwing0 p = new PSwing0(this, comp);
    // p.deleteable = true;
    // p.setBounds(r);
    // p.translate(r.x, r.y);
    // nodeLayer.addChild(p);
    // comps.add(p);
    // }
    // }

    void saveDims()
    {
        for (Object o : getNodeLayer().getAllNodes())
        {
            if (!(o instanceof PSwing0))
                continue;
            PSwing0 p = (PSwing0) o;
            CellGroupMember cm = (CellGroupMember) ThisNiche.hg.get(p
                    .getHandle());
            if (cm != null)
                cm.setAttribute(VisualAttribs.rect, p.getFullBounds()
                        .getBounds());
            // Component c = p.getComponent();
            // HGHandle h = ThisNiche.handleOf(c);
            // if (h == null) h = CellUtils.addSerializable(c);
            // if(c instanceof NotebookUI) ((NotebookUI) c).getDoc().save();
            // System.out.println("PiccoloCanvas - saveDims: " + h + ":" + c);
        }
    }

    public void addComponent(HGHandle h, HGHandle masterH)
    {
        Object nb = ThisNiche.hg.get(h);
        JComponent comp = null;
        if (nb instanceof Cell)// && ((Cell) nb).getValue() instanceof
        // JComponent)
        {
            comp = new NotebookUI(h);
            comp.setPreferredSize(new Dimension(200, 200));
            HGHandle par = CellUtils.getOutCellParent(masterH);
            CellUtils.setOutputCell(par, null);
            CellUtils.removeEventPubSub(EvalCellEvent.HANDLE, par,
                    HGHandleFactory.anyHandle, HGHandleFactory.anyHandle);
            CellUtils.addEventPubSub(EvalCellEvent.HANDLE, par,
                    ((NotebookUI) comp).getDoc().getHandle(),
                    CellDocument.CopyEvalCellHandler.getInstance());
        } else
        {
            comp = new NotebookUI(h);
            comp.setPreferredSize(new Dimension(200, 200));
            CellUtils.addCopyListeners(h, masterH);
        }

        if (comp != null)
        {
            PSwing0 ps = new PSwing0(this, comp);
            // ps.addInputEventListener(del_handler);
            ps.deleteable = true;
            getNodeLayer().addChild(ps);
            adjust_place(ps, true);
            h = CellUtils.addSerializable(comp);
            ps.setHandle(GUIHelper.addToTopCellGroup(ThisNiche.hg, h,
                    (CellGroup) ThisNiche.hg
                            .get(ThisNiche.TOP_CELL_GROUP_HANDLE), VisualsManager.defaultVisualForType(h), ps.getFullBounds()
                            .getBounds()));
        }
    }

    public PSwing addComponent(JComponent comp, Rectangle r, HGHandle cellH)
    {
        PSwing0 p = new PSwing0(this, comp, cellH);
        getNodeLayer().addChild(p);
        if (r != null)
        {
            p.setBounds(r);
            p.translate(r.x, r.y);
        }
        return p;
    }

    // private static DelEventHandler del_handler= new DelEventHandler();

    void removeComponents(Collection<PNode> selection)
    {
        for (PNode node : selection)
        {
            if (node instanceof PSwing0 && !((PSwing0) node).isDeleteable())
                continue;
            Object ui = ((PSwing0) node).getComponent();
            if (ui instanceof NotebookUI)
                remove_and_clean((NotebookUI) ui);
            node.removeFromParent();
            // comps.remove(node);
            // TODO
            GUIHelper.removeFromTopCellGroup();
        }
    }

    void remove_and_clean(NotebookUI ui)
    {
        NotebookDocument doc = ui.getDoc();
        HGHandle h = ThisNiche.handleOf(ui);
        if (h != null)
            ThisNiche.hg.remove(h);
        CellUtils.removeHandlers(doc.bookH);
        CellUtils.removeHandlers(doc.getHandle());
        ThisNiche.hg.remove(doc.getHandle());
    }

    PLayer getNodeLayer()
    {
        return nodeLayer;
    }

    public void adjust_place(PNode node, boolean rigth_or_down)
    {
        PBounds or = node.getGlobalFullBounds();
        PBounds out = new PBounds(or);
        ArrayList<Object> results = new ArrayList<Object>();
        Object[] allnodes = node.getRoot().getAllNodes(new PSwingFilter(),
                results).toArray();
        boolean[] visited = new boolean[allnodes.length];
        for (int i = 0; i < allnodes.length; i++)
        {
            PNode n = (PNode) allnodes[i];
            PBounds b = n.getGlobalFullBounds();
            if (n.fullIntersects(out) && !visited[i])
            {
                if (rigth_or_down) out.x = b.x + b.width + 10;
                else
                    out.y = b.y + b.height + 10;
                visited[i] = true;
                i = 0;
            }
        }
        // node.setBounds(out);
        node.translate(out.x, out.y);
    }

    private static class PSwingFilter implements PNodeFilter
    {

        public boolean accept(PNode node)
        {
            return node instanceof PSwing0;
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
                PiccoloFrame.getInstance().getCanvas().deleteSelection();
        }

    }
}
