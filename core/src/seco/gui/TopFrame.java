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
import seco.notebook.util.Log;
import seco.talk.ConnectionManager;
import seco.things.CellUtils;

public abstract class TopFrame extends JFrame implements GUIController 
{
    private static final long serialVersionUID = -4693003403767961820L; 
    
    public static boolean PICCOLO = true;
    public static boolean AUTO_BACKUP = true;
   
   // private static TopFrame instance;

    //protected HGHandle focusedContainerHandle = ThisNiche.TOP_CELL_GROUP_HANDLE;

    protected Image LOGO_IMAGE = Toolkit.getDefaultToolkit().getImage(
            TopFrame.class.getResource(GUIHelper.LOGO_IMAGE_RESOURCE));
    private static Image NO_LOGO =  Toolkit.getDefaultToolkit().getImage(
            TopFrame.class.getResource("/seco/notebook/images/nologo.jpg"));;

    private String original_title;
    
//    /*public*/private static TopFrame getInstance()
//    {
//        if (instance == null)
//            instance = (PICCOLO) ? new PiccoloFrame() : new StandaloneFrame();
//        return instance;
//    }

    public TopFrame() throws HeadlessException
    {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initFrame();
        Log.start();
        addWindowFocusListener(
               new WindowFocusListener(){

                @Override
                public void windowGainedFocus(WindowEvent e)
                {
                    set_original_title_and_icon();
                    //System.out.println("TopFrame - windowGainedFocus: " + original_title);
                }

                @Override
                public void windowLostFocus(WindowEvent e)
                {
                }

        });
    }

    protected void initFrame()
    {
        original_title = "[" + ThisNiche.graph.getLocation() + "] ";
        set_original_title_and_icon();
    }
    
  
    public void blink(String message)
    {
        if(isFocused()) return;
        Toolkit.getDefaultToolkit().beep();
        flash(message, 1000, 500, 5);
    }
    
    private void do_flash(String message, boolean on)
    {
        if (!on)
        {
            set_original_title_and_icon();
            return;
        }

        if (getIconImage() == LOGO_IMAGE)
        {
            setIconImage(NO_LOGO);
            setTitle(message);
        }
        else
        {
            set_original_title_and_icon();
        }
    }
    
    private void set_original_title_and_icon()
    {
        setIconImage(LOGO_IMAGE);
        setTitle(original_title);
    } 

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
    
    // TODO: legacy - called in .scm scripts
    public HGHandle getCurrentRuntimeContext()
    {
        return ThisNiche.TOP_CONTEXT_HANDLE;
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

//    public void showHTMLToolBar(boolean show_or_hide)
//    {
//    }

    public PiccoloCanvas getCanvas()
    {
        return null;
    }

    public  JFrame getFrame()
    {
        return this;
    }

     public CaretListener getNotebookUICaretListener()
    {
        return null;
    }

    public void exit()
    {
        Log.end();
        if (AUTO_BACKUP) CommonActions.backup();
        ConnectionManager.stopConnections(false);
        CellUtils.removeBackupedStuff();
        System.exit(0);
    }

//    public HGHandle getFocusedContainerHandle()
//    {
//        return focusedContainerHandle;
//    }
//
//    public void setFocusedContainerHandle(HGHandle focusedContainerHandle)
//    {
//        this.focusedContainerHandle = focusedContainerHandle;
//    }

}
