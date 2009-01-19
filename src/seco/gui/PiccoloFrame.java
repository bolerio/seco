package seco.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.hypergraphdb.HGHandle;


import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.piccolo.PiccoloTransferHandler;
import seco.notebook.piccolo.pswing.PSwing;
import seco.notebook.piccolo.pswing.PSwingCanvas;
import seco.things.CellGroup;


import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class PiccoloFrame extends TopFrame
{
	private static final long serialVersionUID = 6929648456637555149L;
	private PiccoloCanvas container;
//    private static PiccoloFrame instance;
//
//    public static PiccoloFrame getInstance()
//    {
//        if (instance == null) instance = new PiccoloFrame();
//        return instance;
//    }

    PiccoloFrame() throws HeadlessException
    {
        super();
    }

    protected void initFrame()
    {
        container = new PiccoloCanvas();
        container.setTransferHandler(new PiccoloTransferHandler());
        // Set up basic frame
        setBounds(50, 50, 750, 750);
        setResizable(true);
        setBackground(null);

        PScrollPane scroll = new PScrollPane(container);
        getContentPane().add(scroll, BorderLayout.CENTER);
        //getContentPane().add(container, BorderLayout.CENTER);
         validate(); 
    }
 
    public void showHTMLToolBar(boolean show_or_hide)
    {
       GUIHelper.getHTMLToolBar().setEnabled(show_or_hide);
    }
    
    
    void saveDims()
    {
        container.saveDims();
    }

   

     // TODO: very, very hacky
    void repaintTabbedPane()
    {
        //PBounds b = pTabbedPane.getBounds();
       // b.width = b.width + 1;
       // pTabbedPane.setBounds(b);
    }

    public void exit()
    {
        CellGroup group = (CellGroup) ThisNiche.hg.get(
                ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.setAttribute(VisualAttribs.rect, getBounds());
        saveDims();
        super.exit();
    }

    public static class PSwing0 extends PSwing
    {
		private static final long serialVersionUID = 4732523747800268384L;
		public boolean deleteable;
        private HGHandle handle; 

        public PSwing0(PSwingCanvas canvas, JComponent component,
                HGHandle handle)
        {
            super(canvas, component);
            this.handle = handle;
        }

        public PSwing0(PSwingCanvas canvas, JComponent component)
        {
            super(canvas, component);
        }

        public boolean isDeleteable()
        {
            return deleteable;
        }

        @Override
        public void translate(double dx, double dy)
        {
            super.translate(dx, dy);
            //System.out.println("PSwing0 - translate: " + dx + ":" + dy);
            //getComponent().setBounds(this.getFullBounds().getBounds());
            //getComponent().y = (int) dy;
        }

        @Override
        public boolean setBounds(double x, double y, double width, double height)
        {
            boolean b = super.setBounds(x, y, width, height);
            getComponent().setPreferredSize(
                    new Dimension((int) width, (int) height));
            return b;
        }

        public HGHandle getHandle()
        {
            return handle;
        }

        public void setHandle(HGHandle handle)
        {
            this.handle = handle;
        }
    }

    public PiccoloCanvas getCanvas()
    {
        return container;
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        PiccoloFrame f = new PiccoloFrame();
       // JPanel inner = new JPanel();
       // inner.add(new JButton("Outer"));
                
        PiccoloCanvas pc = new PiccoloCanvas();//f.canvas.getRoot());
        pc.setSize(450, 400);
        //pc.setTransferHandler(new PiccoloTransferHandler());
        //inner.add(pc);
        PScrollPane scroll = new PScrollPane(pc);
        scroll.setSize(500,500);
       // inner.add(scroll);
        PSwing0 p = new PSwing0(pc, new JButton("Test"));
        pc.getNodeLayer().addChild(p);
        p.setBounds(new Rectangle(0,0,400,400));
        p.translate(0, 0);
        
        PSwing0 p1 = new PSwing0(pc, new JCheckBox("Check"));
        pc.getNodeLayer().addChild(p1);
        p1.setBounds(new Rectangle(60,66,400,400));
        p1.translate(60,66);
        
        PSwing x = f.add_comp(pc, new Rectangle(0,0,500,500));
        PCamera camera = new PCamera();
//        PLayer layer = new PLayer();
        x.addChild(camera);
//        x.addChild(layer);             
        camera.addLayer(pc.getNodeLayer());
        pc.setCamera(camera);
//        pc.setSize(450, 400);
        pc.setBounds(0, 0, 450, 350);
        f.add_comp(new JButton("Outer"), new Rectangle(0,600,100,100));
        f.validate();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
    

    private PSwing0 add_comp(JComponent c, Rectangle r)
    {
        PSwing0 p = new PSwing0(container, c);
        container.getNodeLayer().addChild(p);
        p.setBounds(r);
        p.translate(r.x, r.y);
        return p;
    }

}
