package seco.gui;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.NotebookUI;
import seco.notebook.util.Log;
import seco.rtenv.RuntimeContext;
import seco.talk.ConnectionManager;
import seco.things.CellUtils;

public abstract class TopFrame extends JFrame
{
    public static boolean PICCOLO = true;
    public static boolean AUTO_BACKUP = true;
    // current RuntimeContext
    public static HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE;
    private static TopFrame instance;

    protected HGHandle focusedContainerHandle = ThisNiche.TOP_CELL_GROUP_HANDLE;

    protected Image LOGO_IMAGE = Toolkit.getDefaultToolkit().getImage(
            TopFrame.class.getResource(GUIHelper.LOGO_IMAGE_RESOURCE));
    private static Image NO_LOGO =  Toolkit.getDefaultToolkit().getImage(
            TopFrame.class.getResource("/seco/notebook/images/nologo.jpg"));;

    private String original_title;
    
    public static TopFrame getInstance()
    {
        if (instance == null)
            instance = (PICCOLO) ? new PiccoloFrame() : new StandaloneFrame();
        return instance;
    }

    public TopFrame() throws HeadlessException
    {
        setIconImage(LOGO_IMAGE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initFrame();
        Log.start();
        addWindowFocusListener(
               new WindowFocusListener(){

                @Override
                public void windowGainedFocus(WindowEvent e)
                {
                    setIconImage(LOGO_IMAGE);
                    setTitle(original_title);
                    System.out.println("TopFrame - windowGainedFocus: " + original_title);
                }

                @Override
                public void windowLostFocus(WindowEvent e)
                {
                }

        });
    }

    protected void initFrame()
    {
        setTitle("[" + ThisNiche.graph.getLocation() + "] ");
    }
    
    @Override
    public void setTitle(String title)
    {
        super.setTitle(title);
        if(!is_blinking()) 
        {
            original_title = title;
            if(original_title.startsWith("New"))
                Thread.dumpStack();
        }
    }

    public void blink(String message)
    {
        if(/*true ||*/ isFocused()) return;
        //original_title = getTitle();
        //blinking = true;
        Toolkit.getDefaultToolkit().beep();
        flash(message, 600, 300, 5);
    }
    
    private boolean blink = false;
    
    //synchronized 
    boolean is_blinking()
    {
        return blink;
    }
    
    //synchronized 
    void set_blinking(boolean blink)
    {
        this.blink = blink;
    }

    private void do_flash(String message, boolean on)
    {
        set_blinking(true);
        if (!on)
        {
            setIconImage(LOGO_IMAGE);
            setTitle(original_title);
            set_blinking(false);
            return;
        }

        if (getIconImage() == LOGO_IMAGE)
        {
            setIconImage(NO_LOGO);
            setTitle(message);
        }
        else
        {
            setIconImage(LOGO_IMAGE);
            setTitle(original_title);
        }
        set_blinking(false);
    }

   // protected Thread flashThread;  
    private void flash(final String message, final int intratime, final int intertime, final int count)
    {
        Thread flashThread = new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    // flash on and off each time
                    for (int i = 0; i < count; i++)
                    {
                        do_flash(message, true);
                        Thread.sleep(intratime);
                        do_flash(message, true);
                        Thread.sleep(intertime);
                    }
                    // turn the flash off
                    do_flash(message, true);
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            }
        });
       flashThread.start();
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
        if (AUTO_BACKUP) CommonActions.backup();
        ConnectionManager.stopConnections(false);
        CellUtils.removeBackupedCells();
        System.exit(0);
    }

    // TODO: legacy - called in .scm scripts
    public HGHandle getCurrentRuntimeContext()
    {
        return currentRC;
    }

    public static void setCurrentRuntimeContext(HGHandle ch)
    {
        if (ch == null) return;
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        currentRC = ch;
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.graph
                .get(currentRC);
        rcInstance.getBindings().put("notebook", ui.getDoc().getBook());
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
