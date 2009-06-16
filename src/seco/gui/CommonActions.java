package seco.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;

import seco.ThisNiche;
import seco.boot.NicheManager;
import seco.gui.layout.LayoutHandler;
import seco.gui.layout.LayoutSettingsPanel;
import seco.notebook.AppConfig;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
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
        int i = 1;
        for (HGHandle h: GUIHelper.getOpenedBooks())
        {
            CellGroupMember c = ThisNiche.hg.get(h);
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
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        canvas.getCamera().removeAllChildren();
        canvas.getNodeLayer().removeAllChildren();
        ThisNiche.hg.remove(GUIHelper.MENUBAR_HANDLE, true);
        ThisNiche.hg.remove(GUIHelper.TOOLBAR_HANDLE, true);
        ThisNiche.hg.remove(GUIHelper.HTML_TOOLBAR_HANDLE, true);
        GUIHelper.makeTopCellGroup(ThisNiche.hg);
        CellGroup group = (CellGroup) ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellVisual v = (CellVisual) ThisNiche.hg.get(group.getVisual());
        v.bind(group);
    }
    
    public static void testEmbededContainer()
    {
        CellGroup group = new CellGroup("EMBEDED CONTAINER");
        HGHandle groupH = ThisNiche.hg.add(group);
        HGHandle cellH1 = CellUtils.createOutputCellH(null, null, new JButton("Test"), false);
        HGHandle cellH2 = CellUtils.createOutputCellH(null, null, new JCheckBox("Test"), false);
        group.insert(0, cellH1);
        group.insert(0, cellH2);
        GUIHelper.addToTopCellGroup(groupH, CellContainerVisual.getHandle(), null, new Rectangle(200, 200, 500, 500)); 
    }
    
    public static boolean renameCellGroupMember(HGHandle h)
    {
        CellGroupMember cgm = ThisNiche.hg.get(h);
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
}
