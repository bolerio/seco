package seco.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.things.CellGroup; 
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class PiccoloFrame extends TopFrame
{
	private static final long serialVersionUID = 6929648456637555149L;
	private PiccoloCanvas canvas;

    PiccoloFrame() throws HeadlessException
    {
        super();
    }

    protected void initFrame()
    {
        canvas = new PiccoloCanvas();
        canvas.setTransferHandler(new PiccoloTransferHandler());
        // Set up basic frame
        setBounds(50, 50, 750, 750);
        setResizable(true);
        setBackground(null);

        PScrollPane scroll = new PScrollPane(canvas);
        getContentPane().add(scroll, BorderLayout.CENTER);
        validate(); 
    }
 
    public void showHTMLToolBar(boolean show_or_hide)
    {
       GUIHelper.getHTMLToolBar().setEnabled(show_or_hide);
    }
    
    
    void saveDims()
    {
        //canvas.saveDims();
    }

    public void exit()
    {
        CellGroup group = (CellGroup) ThisNiche.hg.get(
                ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.setAttribute(VisualAttribs.rect, getBounds());
        saveDims();
        super.exit();
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
        PSwingNode p = new PSwingNode(pc, new JButton("Test"));
        pc.getNodeLayer().addChild(p);
        p.setBounds(new Rectangle(0,0,400,400));
        p.translate(0, 0);
        
        PSwingNode p1 = new PSwingNode(pc, new JCheckBox("Check"));
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
    

    private PSwingNode add_comp(JComponent c, Rectangle r)
    {
        PSwingNode p = new PSwingNode(canvas, c);
        canvas.getNodeLayer().addChild(p);
        p.setBounds(r);
        p.translate(r.x, r.y);
        return p;
    }
}