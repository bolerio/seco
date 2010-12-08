package seco.gui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.actions.CommonActions;
import seco.talk.ConnectionManager;
import seco.things.CellUtils;
import seco.util.Log;

public abstract class TopFrame extends JFrame implements GUIController
{
    private static final long serialVersionUID = -4693003403767961820L;

    public static boolean PICCOLO = true;
    public static boolean AUTO_BACKUP = true;

    private String original_title;

    public TopFrame() throws HeadlessException
    {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        initFrame();
        Log.start();
        addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                set_original_title_and_icon();
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
        if (isFocused()) return;
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

        if (getIconImage() == GUIHelper.LOGO_IMAGE)
        {
            setIconImage(GUIHelper.NO_LOGO);
            setTitle(message);
        }
        else
        {
            set_original_title_and_icon();
        }
    }

    private void set_original_title_and_icon()
    {
        setIconImage(GUIHelper.LOGO_IMAGE);
        setTitle(original_title);
    }

    public void setTitle(String title)
    {
        super.setTitle(title);
    }

    public void setStatusBarMessage(String message)
    {
    }

    private void flash(final String message, final int intratime,
            final int intertime, final int count)
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

    public PiccoloCanvas getCanvas()
    {
        return null;
    }

    public JFrame getFrame()
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

}
