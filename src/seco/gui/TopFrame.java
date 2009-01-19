package seco.gui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBluer;

import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.util.Log;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroup;

public abstract class TopFrame extends JFrame
{
    public static boolean PICCOLO = true;
   
    private static TopFrame instance;
    protected NotebookDocument.ModificationListener docListener;
    //current RuntimeContext
    public static HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE; 

    
    public static TopFrame getInstance()
    { 
        if (instance == null) 
            instance = (PICCOLO) ? new PiccoloFrame() : new StandaloneFrame();
        return instance;
    }
    
    public TopFrame() throws HeadlessException
    {
        // XXX
        PlasticLookAndFeel.setPlasticTheme(new DesertBluer());
        try
        {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        }
        catch (Exception e)
        {
        }
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(GUIHelper.LOGO_IMAGE_RESOURCE)));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        docListener = new NotebookDocument.ModificationListener() {
            public void documentModified(Object o)
            {
                GUIHelper.updateTitle(false);
            }
        };
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
        System.exit(0);
    }

    public NotebookDocument.ModificationListener getDocListener()
    {
        return docListener;
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
