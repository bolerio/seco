package seco.gui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;
import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookUI;
import seco.notebook.util.Log;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroup;

public abstract class TopFrame extends JFrame
{
    public static boolean PICCOLO = true;
    public static boolean AUTO_BACKUP = true;
    //current RuntimeContext
    public static HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE; 

    private static TopFrame instance;
    public static TopFrame getInstance()
    { 
        if (instance == null) 
            instance = (PICCOLO) ? new PiccoloFrame() : new StandaloneFrame();
        return instance;
    }
    
    public TopFrame() throws HeadlessException
    {
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(GUIHelper.LOGO_IMAGE_RESOURCE)));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initFrame();
        Log.start();
    }

    protected void initFrame(){}
    
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() != WindowEvent.WINDOW_CLOSING) super
                .processWindowEvent(e);
        else
        {
           exit();
        }
    }
  
    public void showHTMLToolBar(boolean show_or_hide)
    {
    }
    
    public PiccoloCanvas getCanvas()
    {
        return null;
    }
    
    public void setStatusBarMessage(String message)
    {
        
    }
    
    public CaretListener getCaretListener()
    {
        return null;
    }
    
    public void exit()
    {
        Log.end();
        CellGroup group = (CellGroup) ThisNiche.hg.get(
                ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.setAttribute(VisualAttribs.rect, getBounds());
        if(AUTO_BACKUP)
            ContextMenuHandler.backup();
        System.exit(0);
    }

    public static HGHandle getCurrentEvaluationContext()
    {
        return currentRC;
    }
    
    public static void setCurrentEvaluationContext(HGHandle ch)
    {
        if (ch == null) return;
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if(ui == null) return;
        currentRC = ch;
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.hg
                .get(currentRC);
        rcInstance.getBindings()
                .put("notebook", ui.getDoc().getBook());
        ui.getDoc().setEvaluationContext(ThisNiche.getEvaluationContext(ch));
    }
}
