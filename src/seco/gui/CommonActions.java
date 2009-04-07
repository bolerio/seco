package seco.gui;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.hypergraphdb.HyperGraph;

import seco.ThisNiche;
import seco.boot.NicheManager;
import seco.gui.layout.LayoutSettingsPanel;
import seco.notebook.AppConfig;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.things.IOUtils;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

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
                bck_dir + File.separator + NicheManager.getNicheName(ThisNiche.hg));
        if (!dir.exists()) dir.mkdir();
        System.out.println("Backup in: " + dir.getAbsolutePath());
        CellGroup group = (CellGroup) ThisNiche.hg
                .get(ThisNiche.TABBED_PANE_GROUP_HANDLE);
        for (int i = 0; i < group.getArity(); i++)
        {
            CellGroupMember c = group.getElement(i);
            
            if(!(c instanceof CellGroup)) continue;
           
            CellGroup g = (CellGroup) c;
            // escape some illegal chars which could be introduced during
            // previous book import
            String fn = g.getName().replace('\\', '_').replace('/', '_')
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
        }
    }
    
    public static void restoreDefaultGUI()
    {
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        canvas.getCamera().removeAllChildren();
        canvas.getNodeLayer().removeAllChildren();
        GUIHelper.makeTopCellGroup(ThisNiche.hg);
        CellGroup group = (CellGroup) ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellVisual v = (CellVisual) ThisNiche.hg.get(group.getVisual());
        v.bind(group);
    }
    
    
}
