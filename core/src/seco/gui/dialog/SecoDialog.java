package seco.gui.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 * <p>
 * Decorating the standard Swing JDialog with some Seco specific behaviors.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class SecoDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    protected void closeWithEsc()
    {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);        
    }
    
    public SecoDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        closeWithEsc();        
    }
}
