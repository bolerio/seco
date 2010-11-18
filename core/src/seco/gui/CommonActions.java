package seco.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;

import seco.ActionManager;
import seco.AppConfig;
import seco.ThisNiche;
import seco.U;
import seco.boot.NicheManager;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.NotifyDescriptor;
import seco.gui.dialog.TopCellTreeDlg;
import seco.gui.layout.LayoutSettingsPanel;
import seco.gui.panel.OpenBookPanel;
import seco.gui.panel.SearchDescriptionPanel;
import seco.gui.piccolo.BirdsEyeView;
import seco.gui.visual.CellContainerVisual;
import seco.gui.visual.NBUIVisual;
import seco.gui.visual.TabbedPaneVisual;
import seco.gui.visual.VisualAttribs;
import seco.notebook.NotebookUI;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.things.IOUtils;
import seco.util.FileUtil;
import seco.util.GUIUtil;
import seco.util.IconManager;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

/*
 * Collection of common actions and utility methods used in them
 */
public class CommonActions
{
    public static final String SELECT_ALL = "Select All";
    public static final String PASTE = "Paste";
    public static final String EXPORT = "Export As...";
    public static final String EXIT = "Exit";
    public static final String SAVE = "Save";
    public static final String IMPORT = "Import";
    public static final String OPEN = "Open";
    public static final String NEW = "New";
    public static final String COPY = "Copy";
    public static final String CUT = "Cut";

    public static class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            putValue(Action.NAME, OPEN);
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("Open16.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(ActionEvent e)
        {
            GUIUtil.createAndShowDlg(GUIUtil.getFrame(e),
                    "Open Or Delete CellGroup", new OpenBookPanel(),
                    new Dimension(500, 500));
        }
    }

    public static class ElementTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            openElementTree();
        }
    }

    public static class ParseTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            openParseTree();
        }
    }

    public static class ShowOutputConsoleAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    CommonActions.showOutputConsole();
                }
            });
        }
    }

    public static class CellTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            CellGroupMember book = ui.getDoc().getBook();
            openCellTree(book);
        }
    }

    public static class TopCellTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            CellGroupMember book = ThisNiche.graph
                    .get(ThisNiche.TOP_CELL_GROUP_HANDLE);
            openCellTree(book);
        }
    }

    public static class DescriptionManagerAction implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            GUIUtil.createAndShowDlg(GUIUtil.getFrame(e),
                    "Manage Descriptions", new SearchDescriptionPanel(),
                    new Dimension(700, 500));
        }
    }

    public static class NewAction extends AbstractAction
    {
        public NewAction()
        {
            putValue(Action.NAME, NEW);
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("New16.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Create New Notebook");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            newNotebook();
        }
    }

    public static class ExportAction extends AbstractAction
    {
        public ExportAction()
        {
            this.putValue(Action.NAME, EXPORT);
            this.putValue(Action.SMALL_ICON,
                    IconManager.resolveIcon("SaveAs16.gif"));
            this.putValue(Action.SHORT_DESCRIPTION, "Export Notebook As XML");
        }

        public void actionPerformed(ActionEvent evt)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            File f = FileUtil.getFile(GUIUtil.getFrame(ui),
                    "Export Notebook As ...", FileUtil.SAVE, null);
            if (f != null)
            {
                IOUtils.exportCellGroup((CellGroup) ui.getDoc().getBook(),
                        f.getAbsolutePath());
            }
        }
    }

    public static class ImportAction extends AbstractAction
    {
        public ImportAction()
        {
            this.putValue(Action.NAME, IMPORT);
            this.putValue(Action.SMALL_ICON,
                    IconManager.resolveIcon("Open16.gif"));
            this.putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            openNotebook();
        }
    }

    public static class ExitAction extends AbstractAction
    {
        public ExitAction()
        {
            this.putValue(Action.NAME, EXIT);
            this.putValue(Action.SHORT_DESCRIPTION, "Exit Seco");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            ThisNiche.guiController.exit();
        }
    }

    public static void showLayoutSettingsDlg(PSwingNode node)
    {
        LayoutSettingsPanel panel = new LayoutSettingsPanel(node);
        JDialog dialog = new JDialog(GUIUtil.getFrame(), "Layout Settings");
        dialog.add(panel);
        dialog.setSize(new Dimension(270, 170));
        dialog.setVisible(true);
    }

    public static void birdsEyeView()
    {
        PCanvas canvas = ThisNiche.getCanvas();
        BirdsEyeView bev = new BirdsEyeView();
        bev.connect(canvas, new PLayer[] { canvas.getLayer() });

        bev.setMinimumSize(new Dimension(180, 180));
        bev.setSize(new Dimension(180, 180));
        bev.updateFromViewed();
        GUIUtil.createAndShowDlg("BirdsEyeView", bev, new Dimension(220, 220));
        bev.revalidate();
    }

    private static String bck_dir = ".seco_bck";

    public static void backup()
    {
        File dir = new File(new File(U.findUserHome()),// AppConfig.getJarDirectory(),
                bck_dir + File.separator
                        + NicheManager.getNicheName(ThisNiche.graph));
        if (!dir.exists()) dir.mkdir();
        System.out.println("Backup in: " + dir.getAbsolutePath());
        int i = 1;
        for (HGHandle h : getOpenedBooks())
        {
            CellGroupMember c = ThisNiche.graph.get(h);
            if (!(c instanceof CellGroup)) continue;

            CellGroup g = (CellGroup) c;
            // escape some illegal chars which could be introduced during
            // previous book import
            String title = CellUtils.getName(g);
            if (title == null) title = "Untitled" + i;
            String fn = title.replace('\\', '_').replace('/', '_')
                    .replace(':', '_');
            if (!fn.endsWith(".nb")) fn += ".nb";
            try
            {
                IOUtils.exportCellGroup(g, new File(dir, fn).getAbsolutePath());
            }
            catch (Exception ex)
            {
                IOUtils.exportCellGroup(g,
                        new File(dir, "BCK" + i).getAbsolutePath() + ".nb");
            }
            i++;
        }
    }

    public static void restoreDefaultGUI()
    {
        ThisNiche.getCanvas().removeAllNodes();
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        CellVisual v = ThisNiche.graph.get(group.getVisual());

        complete_remove(GUIHelper.MENUBAR_HANDLE);
        complete_remove(GUIHelper.TOOLBAR_HANDLE);
        complete_remove(GUIHelper.HTML_TOOLBAR_HANDLE);
        complete_remove(GUIHelper.WIN_ACTIONS_HANDLE);
        complete_remove(GUIHelper.CANVAS_GLOBAL_ACTION_SET_HANDLE);
        complete_remove(GUIHelper.CANVAS_NODE_ACTION_SET_HANDLE);
        complete_remove(GUIHelper.CELL_MENU_ITEMS_HANDLE);
        complete_remove(GUIHelper.CELL_GROUP_MENU_ITEMS_HANDLE);
        complete_remove(GUIHelper.NOTEBOOK_MENU_ITEMS_HANDLE);
        complete_remove(ActionManager.HANDLE);
        complete_remove(NotebookUI.POPUP_HANDLE);

        GUIHelper.makeTopCellGroup();
        v.bind(group);
    }

    private static void complete_remove(HGPersistentHandle h)
    {
        // try
        // {
        ThisNiche.graph.remove(h, true);
        // }catch(Throwable t)
        // {
        // ThisNiche.graph.define(h, null);
        // }
    }

    public static void resetZoom()
    {
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        group.getAttributes().remove(VisualAttribs.zoom);
        PiccoloCanvas canvas = ThisNiche.getCanvas();
        canvas.getCamera().setViewScale(1.0);
        canvas.getCamera().setViewOffset(0, 0);
    }

    // public static void testEmbededContainer()
    // {
    // CellGroup group = new CellGroup("EMBEDED CONTAINER");
    // HGHandle groupH = ThisNiche.graph.add(group);
    // HGHandle cellH1 = CellUtils.createOutputCellH(null, null, new
    // JButton("Test"), false);
    // HGHandle cellH2 = CellUtils.createOutputCellH(null, null, new
    // JCheckBox("Test"), false);
    // group.insert(0, cellH1);
    // group.insert(0, cellH2);
    // GUIHelper.addToTopCellGroup(groupH, CellContainerVisual.getHandle(),
    // null, new Rectangle(200, 200, 500, 500));
    // }

    public static boolean renameCellGroupMember(HGHandle h)
    {
        CellGroupMember cgm = ThisNiche.graph.get(h);
        String name = CellUtils.getName(cgm);
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                GUIUtil.getFrame(), "Name: ", "Rename");
        nd.setInputText(name);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
        {
            String t = nd.getInputText();
            CellUtils.setName(cgm, t);
            return true;
        }
        return false;
    }

    public static boolean editCGMDescription(HGHandle h)
    {
        // CellGroupMember cgm = ThisNiche.graph.get(h);
        String desc = CellUtils.getDescription(h);
        JTextArea area = new JTextArea();
        area.setPreferredSize(new Dimension(300, 200));
        DialogDescriptor dd = new DialogDescriptor(area,
                "Cell/Group Description");
        area.setText(desc);
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
        {
            CellUtils.setDescription(h, area.getText());
            return true;
        }
        return false;
    }

    public static void updateSelectedPSwingCellComponentValue()
    {
        PSwingNode ps = ThisNiche.getCanvas().getSelectedPSwingNode();
        CellGroupMember cell = ThisNiche.graph.get(ps.getHandle());
        if (cell instanceof Cell
                && ((Cell) cell).getValue() instanceof JComponent)
            ((Cell) cell).updateValue(ps.getComponent());
    }

    /**
     * Opens a given notebook(that is already stored in the niche) in the
     * canvas. Do nothing if the notebook is already opened.
     * 
     * @param bookH
     *            The notebook's handle
     */
    public static void openNotebook(HGHandle bookH)
    {
        if (getOpenedBooks().contains(bookH)) return;
        addAsBook(bookH);
    }

    private static void addAsBook(HGHandle h)
    {
        CellGroup group = ThisNiche.graph.get(// (TopFrame.PICCOLO) ?
                ThisNiche.TOP_CELL_GROUP_HANDLE);// :
        // (StandaloneFrame).tabbedPaneGroupHandle);

        if (CellUtils.isBackuped(h)) CellUtils.restoreCell(h);
        CellGroupMember child = ThisNiche.graph.get(h);
        child.setVisual(NBUIVisual.getHandle());
        child.setAttribute(VisualAttribs.rect,
                new Rectangle(100, 100, 500, 400));
        if (!CellUtils.isShowTitle(child)) CellUtils.toggleShowTitle(child);
        group.insert(group.getArity(), h);
    }

    static void newNotebook()
    {
        CellGroup nb = new CellGroup("CG");
        HGHandle nbHandle = ThisNiche.graph.add(nb);
        addAsBook(nbHandle);
    }

    static void openNotebook()
    {
        File file = FileUtil.getFile(GUIUtil.getFrame(), "Load Notebook",
                FileUtil.LOAD, null);
        if (file == null) return;
        importGroup(file);
    }

    static void importGroup(File file)
    {
        try
        {
            String fn = file.getAbsolutePath();
            HGHandle knownHandle = IOUtils.importCellGroup(fn);
            addAsBook(knownHandle);
            AppConfig.getInstance().getMRUF().add(knownHandle);
            AppConfig.getInstance().setMRUD(file.getParent());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(
                    GUIUtil.getFrame(), t, "Could not open: "
                            + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // strange requirement to open new Notebook, if file doesn't exist
            newNotebook();
        }
    }

    public static Set<HGHandle> getOpenedBooks()
    {
        Set<HGHandle> res = new HashSet<HGHandle>();

        CellGroup top = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        search_group(top, res);
        return res;
    }

    private static void search_group(CellGroup top, Set<HGHandle> res)
    {
        for (int i = 0; i < top.getArity(); i++)
        {
            CellGroupMember cgm = top.getElement(i);
            if (cgm == null) continue;
            if (NBUIVisual.getHandle().equals(cgm.getVisual()))
            {
                res.add(top.getTargetAt(i));
                continue;
            }
            if (cgm instanceof CellGroup)
            {
                search_group((CellGroup) cgm, res);
            }
        }
    }

    static void openElementTree()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        JTree tree = new JTree((TreeNode) ui.getDocument()
                .getDefaultRootElement());
        GUIUtil.createAndShowDlg(GUIUtil.getFrame(ui), "Elements Hierarchy",
                new JScrollPane(tree), new Dimension(500, 800));
    }

    static void openParseTree()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        JTree tree = ui.getParseTree(ui.getCaretPosition());
        if (tree == null) return;
        GUIUtil.createAndShowDlg("Parsing Hierarchy", new JScrollPane(tree),
                new Dimension(500, 800));
    }

    static void openCellTree(CellGroupMember cell)
    {
        JDialog dialog = new TopCellTreeDlg(cell);
        dialog.setSize(500, 800);
        dialog.setVisible(true);
    }

    /**
     * Shortcut method that creates an empty container and assigns it default
     * name and size.
     * 
     * @param visualH
     *            CellVisual's handle
     * @return the handle to the newly created container's cell
     */
    public static HGHandle addContainer(HGHandle visualH)
    {
        String name = "ContainerCellGroup";
        if (TabbedPaneVisual.getHandle().equals(visualH)) name = "TabbedPaneCellGroup";
        else if (CellContainerVisual.getHandle().equals(visualH))
            name = "CanvasCellGroup";
        CellGroup c = new CellGroup(name);
        HGHandle h = ThisNiche.graph.add(c);
        Map<Object, Object> attribs = new HashMap<Object, Object>();
        attribs.put(VisualAttribs.showTitle, true);
        return GUIHelper.addToCellGroup(h, (CellGroup) ThisNiche.graph
                .get(ThisNiche.TOP_CELL_GROUP_HANDLE), visualH, null,
                GUIHelper.CONTAINER_RECT, false, attribs, -1);
    }

    static void showOutputConsole()
    {
        GUIHelper.getOutputConsole();
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE,
                GUIHelper.OUTPUT_CONSOLE_HANDLE);
        if (existingH == null)
        {
            CellGroupMember cgm = ThisNiche.graph.get(GUIHelper.addToCellGroup(
                    GUIHelper.OUTPUT_CONSOLE_HANDLE, GUIHelper
                            .getTopCellGroup(), null, null, new Rectangle(5,
                            500, 600, 100), true));
            CellUtils.setName(cgm, "Output Console");
            cgm.setAttribute(VisualAttribs.showTitle, true);
        }
        else
        {
            PSwingNode n = ThisNiche.getCanvas().getPSwingNodeForHandle(
                    existingH);
            if (n != null) n.blink();
        }
    }
}
