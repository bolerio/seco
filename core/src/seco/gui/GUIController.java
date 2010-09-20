package seco.gui;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

public interface GUIController
{
    
    public void blink(String message);

    public void setTitle(String message);
    
    public void setStatusBarMessage(String message);
   
    public  JFrame getFrame();
    
    public PiccoloCanvas getCanvas();

    public CaretListener getNotebookUICaretListener();

    public void exit();

}