package seco.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import seco.ThisNiche;
import seco.things.CellGroup;
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
        if(ThisNiche.graph == null) System.exit(0);
        CellGroup group = (CellGroup) ThisNiche.graph.get(
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
           getInstance().showHTMLToolBar(false);
        }
    }
 
}