package seco.gui;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

import org.hypergraphdb.HGHandle;

public interface GUIController
{
    
    public void blink(String message);

    // TODO: legacy - called in .scm scripts
    //public abstract HGHandle getCurrentRuntimeContext();

    //public void showHTMLToolBar(boolean show_or_hide);

    public  JFrame getFrame();
    
    public PiccoloCanvas getCanvas();

    public CaretListener getNotebookUICaretListener();

    public void exit();

}