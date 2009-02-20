package seco.gui;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import seco.ThisNiche;
import seco.gui.layout.LayoutSettingsPanel;
import seco.notebook.AppConfig;
import seco.notebook.NotebookDocument;
import seco.notebook.ScriptletAction;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.IOUtils;

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
        if (event.getPickedNode() instanceof PCamera)
        {
            int m = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK;
            if ((event.getModifiersEx() & m) == m)
               showGlobMenu(event);
            return;
        }
        else if (((PiccoloCanvas) event.getComponent()).getSelection().isEmpty())
            return;
        event.setHandled(true);
        showNodeMenu(event);
    }

    public void showGlobMenu(PInputEvent event)
    {
        JPopupMenu menu = new JPopupMenu();
        menu.setLabel("Main Menu");
        JMenuItem mi = new JMenuItem(new ScriptletAction(
                "desktop.getCanvas().relayout();"));
        mi.setText("Layout");
        menu.add(mi);
        mi = new JMenuItem(new ScriptletAction(
        "desktop.getCanvas().getCamera().setViewScale(1.0);"));
        mi.setText("Reset Zoom");
        menu.add(mi);
        mi = new JMenuItem(new ScriptletAction(
                "seco.gui.ContextMenuHandler.backup();"));
        mi.setText("Backup");
        menu.add(mi);
        show_menu(event, menu);
    }

    /**
     * Creates the Appropriate JMenu for the particular node type
     */
    public void showNodeMenu(PInputEvent event)
    {
        PNode node = event.getPickedNode();
        System.out.println("ContextMenuHandler - node: " + node);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(
                new ScriptletAction(
                        "desktop.getCanvas().getCamera().animateViewToCenterBounds("
                                + "desktop.getCanvas().getLayer().getFullBounds(), true, 50l );"));
        mi.setText("Fit To Screen");
        menu.add(mi);
        mi = new JMenuItem(new ScriptletAction(
                "seco.gui.ContextMenuHandler.showLayoutSettingsDlg("
                        + "desktop.getCanvas().getSelection().get(0));"));
        mi.setText("Pin/Unpin");
        menu.add(mi);
        show_menu(event, menu);
    }

    private void show_menu(PInputEvent event, JPopupMenu menu)
    {
        menu.show((PCanvas) event.getComponent(), (int) event
                .getCanvasPosition().getX(), (int) event.getCanvasPosition()
                .getY());
    }

    public static void showLayoutSettingsDlg(PSwingNode node)
    {
        LayoutSettingsPanel panel = new LayoutSettingsPanel(node);
        JDialog dialog = new JDialog(TopFrame.getInstance(), "Layout Settings");
        dialog.add(panel);
        dialog.setSize(new Dimension(270, 170));
        dialog.setVisible(true);
    }

    private static String bck_dir = "seco_bck";

    public static void backup()
    {
        File dir = new File(AppConfig.getConfigDirectory(), bck_dir);
        if (!dir.exists()) dir.mkdir();
        System.out.println("Backup in: " + dir.getAbsolutePath());
        CellGroup group = (CellGroup) ThisNiche.hg
                .get(ThisNiche.TABBED_PANE_GROUP_HANDLE);
        for (int i = 0; i < group.getArity(); i++)
        {
            Cell c = (Cell) group.getElement(i);
            NotebookDocument doc = (NotebookDocument) c.getValue();
            CellGroup g = (CellGroup) doc.getBook();
            IOUtils.exportCellGroup(g, 
                    new File(dir, "BCK" + i).getAbsolutePath() + ".nb");
        }
    }
}
