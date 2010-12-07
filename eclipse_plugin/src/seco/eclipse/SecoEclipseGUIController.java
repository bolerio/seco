package seco.eclipse;

import javax.swing.JFrame;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import seco.actions.CommonActions;
import seco.gui.GUIController;
import seco.gui.GUIHelper;
import seco.gui.PiccoloCanvas;
import seco.gui.TopFrame;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.syntax.ScriptSupport;
import seco.util.Log;
import seco.talk.ConnectionManager;
import seco.things.CellUtils;

public class SecoEclipseGUIController implements GUIController
{

    private PiccoloCanvas canvas;
    private CaretListener caretL;

    public SecoEclipseGUIController()
    {
        caretL = new EclipseCaretListener();
        canvas = new PiccoloCanvas();
    }

    public void blink(final String message)
    {
        PluginU.runInEclipseGUIThread(new Runnable() {
            public void run()
            {
                PluginU.requestUserAttention(message);
            }
        });

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

    public void setTitle(final String title)
    {
        //TODO: SWT/AWT deadlock
        if(true) return;
        final SecoView view = PluginU.getSecoView();
        if (view != null) PluginU.runInEclipseGUIThread(new Runnable() {
            public void run()
            {
                view.setWinTitle(title);
            }
        });
    }

    public void setStatusBarMessage(String message)
    {
        PluginU.setStatusLineMsg(message);
    }

    public static class EclipseCaretListener implements CaretListener
    {
        public void caretUpdate(CaretEvent e)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            NotebookDocument doc = ui.getDoc();
            int dot = e.getDot();
            StringBuffer buf = new StringBuffer();
            ScriptSupport sup = doc.getScriptSupport(dot);
            if (sup != null)
            {
                buf.append("Caret(pos/line/col): ");
                buf.append(Integer.toString(dot));
                int[] lc = sup.offsetToLineCol(dot);
                buf.append("(" + (lc[0] + 1));
                buf.append(',');
                buf.append(lc[1] + ")");
                buf.append("\tEngine: " + sup.getFactory().getEngineName());
            }

            PluginU.setStatusLineMsg(buf.toString());
            GUIHelper.getHTMLToolBar().setEnabled(false);
        }
    }

}
