package seco.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.boot.NicheManager;
import seco.gui.layout.LayoutSettingsPanel;
import seco.notebook.AppConfig;
import seco.notebook.NotebookUI;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.NotifyDescriptor;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.things.IOUtils;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

/*
 * Collection of utility method used in context menu actions
 */
public class CommonActions
{
    public static void showLayoutSettingsDlg(PSwingNode node)
    {
        LayoutSettingsPanel panel = new LayoutSettingsPanel(node);
        JDialog dialog = new JDialog(TopFrame.getInstance(), "Layout Settings");
        dialog.add(panel);
        dialog.setSize(new Dimension(270, 170));
        dialog.setVisible(true);
    }

    public static void birdsEyeView()
    {
        PCanvas canvas = TopFrame.getInstance().getCanvas();
        BirdsEyeView bev = new BirdsEyeView();
        bev.connect(canvas, new PLayer[] { canvas.getLayer() });

        bev.setMinimumSize(new Dimension(180, 180));
        bev.setSize(new Dimension(180, 180));
        bev.updateFromViewed();
        JDialog dialog = new JDialog(TopFrame.getInstance(), "BirdsEyeView");
        dialog.add(bev);
        dialog.setSize(new Dimension(220, 220));
        dialog.setVisible(true);
        bev.revalidate();
    }

    private static String bck_dir = "seco_bck";
    public static void backup()
    {
        File dir = new File(AppConfig.getConfigDirectory(), 
                bck_dir + File.separator + NicheManager.getNicheName(ThisNiche.graph));
        if (!dir.exists()) dir.mkdir();
        System.out.println("Backup in: " + dir.getAbsolutePath());
        int i = 1;
        for (HGHandle h: GUIHelper.getOpenedBooks())
        {
            CellGroupMember c = ThisNiche.graph.get(h);
            if(!(c instanceof CellGroup)) continue;
           
            CellGroup g = (CellGroup) c;
            // escape some illegal chars which could be introduced during
            // previous book import
            String title = CellUtils.getName(g);
            if(title == null) title = "Untitled" + i;
            String fn = title.replace('\\', '_').replace('/', '_')
                    .replace(':', '_');
            if (!fn.endsWith(".nb")) fn += ".nb";
            try
            {
                IOUtils.exportCellGroup(g, new File(dir, fn).getAbsolutePath());
            }
            catch (Exception ex)
            {
                IOUtils.exportCellGroup(g, new File(dir, "BCK" + i)
                        .getAbsolutePath()
                        + ".nb");
            }
            i++;
        }
    }
    
    public static void restoreDefaultGUI()
    {
        TopFrame.getInstance().getCanvas().removeAllNodes();
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellVisual v = ThisNiche.graph.get(group.getVisual());
        ThisNiche.graph.remove(GUIHelper.MENUBAR_HANDLE, true);
        ThisNiche.graph.remove(GUIHelper.TOOLBAR_HANDLE, true);
        ThisNiche.graph.remove(GUIHelper.HTML_TOOLBAR_HANDLE, true);
        ThisNiche.graph.remove(GUIHelper.WIN_ACTIONS_HANDLE, true);
        ThisNiche.graph.remove(NotebookUI.POPUP_HANDLE, true);
        ThisNiche.graph.remove(ContextMenuHandler.GLOBAL_ACTION_SET_HANDLE, true);
        ThisNiche.graph.remove(ContextMenuHandler.NODE_ACTION_SET_HANDLE, true);
        
     
        
        GUIHelper.makeTopCellGroup(ThisNiche.graph);
        v.bind(group);
    }
    
    public static void resetZoom()
    {
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.getAttributes().remove(VisualAttribs.zoom);
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        canvas.getCamera().setViewScale(1.0);
        canvas.getCamera().setViewOffset(0, 0);
    }
    
    public static void testEmbededContainer()
    {
        CellGroup group = new CellGroup("EMBEDED CONTAINER");
        HGHandle groupH = ThisNiche.graph.add(group);
        HGHandle cellH1 = CellUtils.createOutputCellH(null, null, new JButton("Test"), false);
        HGHandle cellH2 = CellUtils.createOutputCellH(null, null, new JCheckBox("Test"), false);
        group.insert(0, cellH1);
        group.insert(0, cellH2);
        GUIHelper.addToTopCellGroup(groupH, CellContainerVisual.getHandle(), null, new Rectangle(200, 200, 500, 500)); 
    }
    
    public static boolean renameCellGroupMember(HGHandle h)
    {
        CellGroupMember cgm = ThisNiche.graph.get(h);
        String name = CellUtils.getName(cgm);
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                TopFrame.getInstance(), "Name: ", "Rename");
        nd.setInputText(name);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
        {
            String t = nd.getInputText();
            CellUtils.setName(cgm, t);
            return true;
        }
        return false;
    } 
    
    public static void updateSelectedPSwingCellComponentValue()
    {
        PSwingNode ps = TopFrame.getInstance().getCanvas()
                .getSelectedPSwingNode();
        CellGroupMember cell = ThisNiche.graph.get(ps.getHandle());
        if (cell instanceof Cell
                && ((Cell) cell).getValue() instanceof JComponent)
            ((Cell) cell).updateValue(ps.getComponent());
    }
}
