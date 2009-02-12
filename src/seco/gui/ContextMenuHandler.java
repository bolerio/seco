package seco.gui;

import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import seco.notebook.ScriptletAction;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

public class ContextMenuHandler extends PBasicInputEventHandler
{
    public ContextMenuHandler()
    {
    }

    public void mousePressed(PInputEvent event)
    {
        if (event.getPickedNode() instanceof PCamera) return;
        if (((PiccoloCanvas) event.getComponent()).getSelection().isEmpty())
            return;
        event.setHandled(true);
        showMenu(event);
    }

    /**
     * Creates the Appropriate JMenu for the particular node type
     */
    public void showMenu(PInputEvent event)
    {
        PNode node = event.getPickedNode();
        System.out.println("ContextMenuHandler - node: " + node);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(new ScriptletAction(
                "desktop.getCanvas().relayout();"));
        mi.setText("Layout");
        menu.add(mi);
        mi = new JMenuItem(new ScriptletAction(
                "desktop.getCanvas().getCamera().scaleView(1.0);"));
        mi.setText("Reset Zoom");
        menu.add(mi);
        menu.show((PCanvas) event.getComponent(), (int) event
                .getCanvasPosition().getX(), (int) event.getCanvasPosition()
                .getY());

    }

    // scaleView
    // private JMenuItem createMenuItem(Object[] method_info, PNode thing)
    // {
    // }
}
