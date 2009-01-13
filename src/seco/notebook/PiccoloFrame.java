package seco.notebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.hypergraphdb.HGHandle;


import seco.notebook.piccolo.PiccoloTransferHandler;
import seco.notebook.piccolo.pswing.PSwing;
import seco.notebook.piccolo.pswing.PSwingCanvas;


import edu.umd.cs.piccolo.PCamera;
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

 
    void saveDims()
    {
        canvas.saveDims();
    }

   

     // TODO: very, very hacky
    void repaintTabbedPane()
    {
        //PBounds b = pTabbedPane.getBounds();
       // b.width = b.width + 1;
       // pTabbedPane.setBounds(b);
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
        return canvas;
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
        PSwing0 p = new PSwing0(canvas, c);
        canvas.getNodeLayer().addChild(p);
        p.setBounds(r);
        p.translate(r.x, r.y);
        return p;
    }

}
