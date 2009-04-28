package seco.talk;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextPane;

import org.hypergraphdb.annotation.HGIgnore;

import seco.api.Callback;

public class TalkInputPane extends JTextPane
{
    private static final long serialVersionUID = -5098690994202528529L;
    @HGIgnore
    Callback<String> inputCallback;
    
    public TalkInputPane()
    {
        addKeyListener(new KeyListener());        
    }
     
    public static class KeyListener extends KeyAdapter
    {
        public void keyTyped(KeyEvent e)
        {
            TalkInputPane inText = (TalkInputPane) e.getComponent();
            if (e.getKeyChar() == '\n')
                if (!e.isShiftDown())
                {
                    String msg = inText.getText();
                    inText.setText("");
                    inText.inputCallback.callback(msg);

                }
                else
                {
                    inText.setText(inText.getText() + "\n");
                }
        }

    }
    
}
