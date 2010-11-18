package seco.gui;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;


import seco.ThisNiche;
import seco.actions.ScriptletAction;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/*
 * Class responsible for managing the menus shown in canvas. 
 */
public class ContextMenuHandler extends PBasicInputEventHandler
{
    protected JPopupMenu global_menu;
    protected JPopupMenu node_menu;
    protected List<Action> global_actions;
    protected List<Action> node_actions;

    public ContextMenuHandler()
    {
    }
    
    static void clear()
    {
        ThisNiche.graph.remove(GUIHelper.CANVAS_GLOBAL_ACTION_SET_HANDLE);
        ThisNiche.graph.remove(GUIHelper.CANVAS_NODE_ACTION_SET_HANDLE);
    }

    public void addGlobalMenuAction(ScriptletAction a)
    {
        getCanvasGlobalActions().add(a);
        ThisNiche.graph.update(getCanvasGlobalActions());
        global_menu = null;
    }

    public void addNodeMenuAction(ScriptletAction a)
    {
        getNodeActions().add(a);
        ThisNiche.graph.update(getNodeActions());
        node_menu = null;
    }

    public void mousePressed(PInputEvent event)
    {
       // System.out.println("ContextMenuHandler - showMenu: " +
        //         event.getPickedNode() + ":" + event.getComponent());
        if (event == null) return;
        if ((event.getPickedNode().equals(ThisNiche.getCanvas().getCamera())))
        {
            int m = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK;
            if ((event.getModifiersEx() & m) == m) showGlobMenu(event);
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
        if (global_menu == null)
            global_menu = makeJPopupMenu(getCanvasGlobalActions());
        show_menu(event, global_menu);
    }

    /**
     * Creates and shows the appropriate JMenu for selected node
     */
    public void showNodeMenu(PInputEvent event)
    {
        if (node_menu == null) node_menu = makeJPopupMenu(getNodeActions());
        show_menu(event, node_menu);
    }

    private void show_menu(PInputEvent event, JPopupMenu menu)
    {
        menu.show((PCanvas) event.getComponent(), (int) event
                .getCanvasPosition().getX(), (int) event.getCanvasPosition()
                .getY());
    }

    private static ScriptletAction makeScriptletAction(String label, String code)
    {
        ScriptletAction a = new ScriptletAction("beanshell", code);
        a.putValue(Action.NAME, label);
        return a;
    }

    private static JPopupMenu makeJPopupMenu(Collection<Action> set)
    {
        JPopupMenu menu = new JPopupMenu();
        for (Action a : set)
            menu.add(a);
        return menu;
    }

    public List<Action> getCanvasGlobalActions()
    {
        global_actions = ThisNiche.graph.get(GUIHelper.CANVAS_GLOBAL_ACTION_SET_HANDLE);
        if (global_actions == null)
        {
            global_actions = new ArrayList<Action>();
            init_global_actions();
            ThisNiche.graph.define(GUIHelper.CANVAS_GLOBAL_ACTION_SET_HANDLE, global_actions);
        }
        return global_actions;
    }

    protected void init_global_actions()
    {
        global_actions.add(makeScriptletAction("Restore Default GUI",
                "seco.gui.CommonActions.restoreDefaultGUI();"));
        global_actions.add(makeScriptletAction("BirdsEyeView",
                "seco.gui.CommonActions.birdsEyeView();"));
        global_actions.add(makeScriptletAction("Reset Zoom",
                "seco.gui.CommonActions.resetZoom();"));
        global_actions.add(makeScriptletAction("Backup",
                "seco.gui.CommonActions.backup();"));
        // global_menu.add(makeMenuItem("Test Embedded Container",
        // "seco.gui.CommonActions.testEmbededContainer();"));
    }

    public List<Action> getNodeActions()
    {
        node_actions = ThisNiche.graph.get(GUIHelper.CANVAS_NODE_ACTION_SET_HANDLE);
        if (node_actions == null)
        {
            node_actions = new ArrayList<Action>();
            init_node_actions();
            ThisNiche.graph.define(GUIHelper.CANVAS_NODE_ACTION_SET_HANDLE, node_actions);
        }
        return node_actions;
    }

    protected void init_node_actions()
    {
        node_actions
                .add(makeScriptletAction(
                        "Rename",
                        "seco.gui.CommonActions.renameCellGroupMember(canvas.getSelectedPSwingNode().getHandle())"));
        node_actions
                .add(makeScriptletAction(
                        "Title On/Off",
                        "seco.things.CellUtils.toggleShowTitle(canvas.getSelectedPSwingNode().getHandle())"));

        node_actions
                .add(makeScriptletAction(
                        "Pin/Unpin",
                        "seco.gui.CommonActions.showLayoutSettingsDlg("
                                + "canvas.getSelectedPSwingNode());"));
        node_actions
                .add(makeScriptletAction("Store Changes",
                        "seco.gui.CommonActions.updateSelectedPSwingCellComponentValue();"));
    }

}
