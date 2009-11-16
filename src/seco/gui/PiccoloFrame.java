package seco.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import seco.ThisNiche;
import seco.notebook.NotebookUI;
import seco.things.CellGroup; 
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class PiccoloFrame extends TopFrame
{
	private static final long serialVersionUID = 6929648456637555149L;
	private PiccoloCanvas canvas;
    private CaretListener caretL; 
    
    PiccoloFrame() throws HeadlessException
    {
        super();
        caretL = new CListener();
    }

    protected void initFrame()
    {
        super.initFrame();        
        canvas = new PiccoloCanvas();
        // Set up basic frame
        setBounds(50, 50, 950, 750);
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
    
    
    public void exit()
    {
        if(ThisNiche.hg == null) System.exit(0);
        CellGroup group = (CellGroup) ThisNiche.hg.get(
                ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.setAttribute(VisualAttribs.rect, getBounds());
       // saveDims();
        super.exit();
    }

    public PiccoloCanvas getCanvas()
    {
        return canvas;
    }
    
    
    public CaretListener getCaretListener()
    {
        return caretL;
    }
    
    private static class CListener implements CaretListener
    {
        public void caretUpdate(CaretEvent e)
        {
            NotebookUI.setFocusedHTMLEditor(null);
            TopFrame.getInstance().showHTMLToolBar(false);
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        PiccoloFrame f = new PiccoloFrame();
       // JPanel inner = new JPanel();
       // inner.add(new JButton("Outer"));
                
        PiccoloCanvas pc = new PiccoloCanvas(true);//f.canvas.getRoot());
        pc.setBackground(new Color(250, 250,255));
        pc.setBorder(new MatteBorder(1,1,1,1, Color.blue));
        pc.setSize(450, 400);
        //pc.setTransferHandler(new PiccoloTransferHandler());
        //inner.add(pc);
        PScrollPane scroll = new PScrollPane(pc);
        //scroll.setSize(500,500);
       // inner.add(scroll);
        JButton b = new JButton("Test");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               System.out.println("Test button clicked!");
            }
            
        });
        PSwingNode p = new PSwingNode(pc, b);
        pc.addNode(p);
        p.setBounds(new Rectangle(0,0,100,30));
        p.translate(0, 0);
        
        PSwingNode p1 = new PSwingNode(pc, new JCheckBox("Check"));
        pc.addNode(p1);
        p1.setBounds(new Rectangle(60,66,100,30));
        p1.translate(60,66);
        
        PSwing x = f.add_comp(scroll, new Rectangle(60,0,500,500));
        PCamera camera = new PCamera();
        x.addChild(camera);
        camera.addLayer(pc.getNodeLayer());
        pc.setCamera(camera);
        pc.setName("Inner Canvas");
        scroll.getViewport().setView(pc);
        
        pc.setBounds(0, 0, 450, 350);
        f.add_comp(new JButton("Outer"), new Rectangle(0,600,100,100));
        f.add_comp(new JButton("Outer1"), new Rectangle(0,600,100,100));
        f.add_comp(new JButton("Outer2"), new Rectangle(0,600,100,100));
        f.add_comp(new JButton("Outer3"), new Rectangle(0,600,100,100));
        f.add_comp(new JButton("Outer4"), new Rectangle(0,600,100,100));
        f.validate();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
    

    private PSwingNode add_comp(JComponent c, Rectangle r)
    {
        PSwingNode p = new PSwingNode(canvas, c);
        canvas.addNode(p);
        p.setBounds(r);
        p.translate(r.x, r.y);
        return p;
    }
}