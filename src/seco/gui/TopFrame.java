package seco.gui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;
import seco.ThisNiche;
import seco.notebook.NotebookUI;
import seco.notebook.util.Log;
import seco.rtenv.RuntimeContext;
import seco.things.CellUtils;

public abstract class TopFrame extends JFrame
{
    public static boolean PICCOLO = true;
    public static boolean AUTO_BACKUP = true;
    //current RuntimeContext
    public static HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE; 
    private static TopFrame instance;
    
    protected HGHandle focusedContainerHandle = ThisNiche.TOP_CELL_GROUP_HANDLE;
    
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

    protected void initFrame()
    {
        this.setTitle("[" + ThisNiche.hg.getLocation() + "] ");
    }
    
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
        if(AUTO_BACKUP)
            CommonActions.backup();
        CellUtils.removeBackupedCells();
        System.exit(0);
    }

    //TODO: legacy - called in .scm scripts
    public HGHandle getCurrentRuntimeContext()
    {
        return currentRC;
    }
    
    public static void setCurrentRuntimeContext(HGHandle ch)
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

    public HGHandle getFocusedContainerHandle()
    {
        return focusedContainerHandle;
    }

    public void setFocusedContainerHandle(HGHandle focusedContainerHandle)
    {
        this.focusedContainerHandle = focusedContainerHandle;
    }
}
