package seco.eclipse;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import seco.gui.CommonActions;
import seco.gui.GUIController;
import seco.gui.PiccoloCanvas;
import seco.gui.PiccoloFrame;
import seco.gui.TopFrame;
import seco.notebook.util.Log;
import seco.talk.ConnectionManager;
import seco.things.CellUtils;

public class SecoEclipseGUIController implements GUIController
{

    private PiccoloCanvas canvas;
    private CaretListener caretL;

    public SecoEclipseGUIController()
    {
        caretL = new PiccoloFrame.NotebookUICaretListener();
        canvas = new PiccoloCanvas();
    }

    public void blink(String message)
    {
    }

    public void exit()
    {
        Log.end();
        if (TopFrame.AUTO_BACKUP) CommonActions.backup();
        ConnectionManager.stopConnections(false);
        CellUtils.removeBackupedStuff();
    }

    public PiccoloCanvas getCanvas()
    {
        return canvas;
    }

    public JFrame getFrame()
    {
        return null;
    }

    public CaretListener getNotebookUICaretListener()
    {
        return caretL;
    }
    
    public void setTitle(String title)
    {
       SecoView view = PluginU.getSecoView();
       if(view != null)
           view.setTitle0(title);
    }

}
