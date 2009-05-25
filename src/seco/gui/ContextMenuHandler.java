package seco.gui;

import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.ScriptletAction;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

public class ContextMenuHandler extends PBasicInputEventHandler
{
    JPopupMenu global_menu;
    JPopupMenu node_menu;
    
    public ContextMenuHandler()
    {
    }

    public void mousePressed(PInputEvent event)
    {
        if (event.getPickedNode() instanceof PCamera
                && (event.getPickedNode().equals(TopFrame.getInstance().getCanvas().getCamera())))
        {
            //System.out.println("ContextMenuHandler - showMenu: " + event.getPickedNode());
            int m = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK;
            if ((event.getModifiersEx() & m) == m) 
                showGlobMenu(event);
            return;
        }
        else if (((PiccoloCanvas) event.getComponent()).getSelectedPSwingNode() == null)
            return;
        event.setHandled(true);
        showNodeMenu(event);
    }

    /**
     * Creates and shows the global JMenu 
     */
    public void showGlobMenu(PInputEvent event)
    {
        if(global_menu != null) {
            show_menu(event, global_menu); return;
        }
        global_menu = new JPopupMenu();
        global_menu.add(makeMenuItem("Restore Default GUI",
                "seco.gui.CommonActions.restoreDefaultGUI();"));
        global_menu.add(makeMenuItem("BirdsEyeView",
                "seco.gui.CommonActions.birdsEyeView();"));
        global_menu.add(makeMenuItem("Reset Zoom",
                "canvas.getCamera().setViewScale(1.0);"));
        global_menu.add(makeMenuItem("Backup", "seco.gui.CommonActions.backup();"));
       //global_menu.add(makeMenuItem("Test Embedded Container", "seco.gui.CommonActions.testEmbededContainer();"));
        show_menu(event, global_menu);
        
    }

    /**
     * Creates and shows the appropriate JMenu for selected node
     */
    public void showNodeMenu(PInputEvent event)
    {
        if(node_menu != null) {
            show_menu(event, node_menu); return;
        }
        node_menu = new JPopupMenu();
        node_menu.add(makeMenuItem("Rename",
                "seco.gui.CommonActions.renameCellGroupMember(canvas.getSelectedPSwingNode().getHandle())"));
        node_menu.add(makeMenuItem("Title On/Off",
        "seco.things.CellUtils.toggleShowTitle(canvas.getSelectedPSwingNode().getHandle())"));

        node_menu.add(makeMenuItem("Pin/Unpin",
                "seco.gui.CommonActions.showLayoutSettingsDlg("
                        + "canvas.getSelectedPSwingNode());"));
        node_menu.add(makeMenuItem("Store Changes",
                "seco.gui.ContextMenuHandler.updateSelPSCell();"));
        show_menu(event, node_menu);
    }

    public static void updateSelPSCell()
    {
        PSwingNode ps = TopFrame.getInstance().getCanvas().getSelectedPSwingNode();
        CellGroupMember cell = ThisNiche.hg.get(ps.getHandle());
        if(cell instanceof Cell && ((Cell) cell).getValue() instanceof JComponent)
           CellUtils.updateCellValue((Cell) cell, ps.getComponent());
    }
    
    private void show_menu(PInputEvent event, JPopupMenu menu)
    {
        
        menu.show((PCanvas) event.getComponent(), (int) event
                .getCanvasPosition().getX(), (int) event.getCanvasPosition()
                .getY());
    }

    private static JMenuItem makeMenuItem(String label, String code)
    {
        JMenuItem mi = new JMenuItem(new ScriptletAction("beanshell", code));
        mi.setText(label);
        return mi;
    }

}
