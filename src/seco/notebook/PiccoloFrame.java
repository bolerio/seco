package seco.notebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

import seco.notebook.piccolo.PiccoloTransferHandler;
import seco.notebook.piccolo.ScribaSelectionHandler;
import seco.notebook.piccolo.pswing.PSwing;
import seco.notebook.piccolo.pswing.PSwingCanvas;


import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class PiccoloFrame extends JFrame
{
    private PiccoloCanvas canvas;
    private static PiccoloFrame instance;

    public static PiccoloFrame getInstance()
    {
        if (instance == null) instance = new PiccoloFrame();
        return instance;
    }

    private PiccoloFrame() throws HeadlessException
    {
        super();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        canvas = new PiccoloCanvas();
        canvas.setTransferHandler(new PiccoloTransferHandler());
        // Set up basic frame
        setBounds(50, 50, 750, 750);
        setResizable(true);
        setBackground(null);

        PScrollPane scroll = new PScrollPane(canvas);
        add(scroll, BorderLayout.CENTER);

        validate();
    }

    private boolean loaded;

    public void loadComponents()
    {
        if (loaded) return;
        loaded = true;
        AppForm app = AppForm.getInstance();
        app.loadComponents();
        app.docListener = new NotebookDocument.ModificationListener() {
            public void documentModified(Object o)
            {
                AppForm.getInstance().updateTitle();
                setTitle(AppForm.getInstance().getTitle());
            }
        };
        Map<String, BeanRect> map = loadDims();
        // System.out.println("LOAD_DIMS: " + map);
        JComponent tabbedPane = app.getTabbedPane();
        Rectangle r = map.get("pTabbedPane").getRect();
        tabbedPane.setPreferredSize(new Dimension(r.width, r.height));
        pTabbedPane = add_comp(tabbedPane, r);
        bar = add_comp(app.getBar(), map.get("bar").getRect());
        toolbar = add_comp(app.getToolBar(), map.get("toolbar").getRect());
        html_toolbar = add_comp(app.getHTMLToolBar(), map.get("html_toolbar")
                .getRect());
    }

    private PSwing0 add_comp(JComponent c, Rectangle r)
    {
        PSwing0 p = new PSwing0(canvas, c);
        canvas.getNodeLayer().addChild(p);
        p.setBounds(r);
        p.translate(r.x, r.y);
        return p;
    }

    private PSwing bar;
    private PSwing toolbar;
    private PSwing pTabbedPane;
    private PSwing html_toolbar;
    private static final String DIM_MAP = "PiccoloFrame.Dim.Map";
    private static final Map<String, BeanRect> dimMap = new HashMap<String, BeanRect>();
    static
    {
        dimMap.put("bar", new BeanRect(0, 0, 200, 27));
        dimMap.put("toolbar", new BeanRect(0, 30, 260, 28));
        dimMap.put("html_toolbar", new BeanRect(0, 60, 600, 28));
        dimMap.put("pTabbedPane", new BeanRect(0, 90, 600, 600));
    }

    private Map<String, BeanRect> loadDims()
    {
        return (Map<String, BeanRect>) AppForm.getInstance().getConfig()
                .getProperty(DIM_MAP, dimMap);
    }

    void saveDims()
    {
        AppForm.getInstance().getConfig().setProperty(DIM_MAP, getDims());
        canvas.saveDims();
    }

    public Map<String, BeanRect> getDims()
    {
        Map<String, BeanRect> m = new HashMap<String, BeanRect>();
        m.put("bar", new BeanRect(bar.getFullBounds().getBounds()));
        m.put("toolbar", new BeanRect(toolbar.getFullBounds().getBounds()));
        m.put("html_toolbar", new BeanRect(html_toolbar.getFullBounds().getBounds()));
        m.put("pTabbedPane", new BeanRect(pTabbedPane.getFullBounds()
                .getBounds()));
        // System.out.println("DIMS: " + m);
        return m;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        new PiccoloFrame().setVisible(true);

    }

    // TODO: very, very hacky
    void repaintTabbedPane()
    {
        PBounds b = pTabbedPane.getBounds();
        b.width = b.width + 1;
        pTabbedPane.setBounds(b);
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() != WindowEvent.WINDOW_CLOSING) super
                .processWindowEvent(e);
        else
            AppForm.getInstance().exit();
    }

    public static class PSwing0 extends PSwing
    {
        public boolean deleteable;

        public PSwing0(PSwingCanvas canvas, JComponent component)
        {
            super(canvas, component);
        }

        public boolean isDeleteable()
        {
            return deleteable;
        }

        @Override
        public boolean setBounds(double x, double y, double width, double height)
        {
            boolean b = super.setBounds(x, y, width, height);
            getComponent().setPreferredSize(
                    new Dimension((int) width, (int) height));
            return b;
        }
    }

    public PiccoloCanvas getCanvas()
    {
        return canvas;
    }

}
