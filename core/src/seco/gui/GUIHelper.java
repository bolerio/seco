package seco.gui;

import static seco.notebook.Actions.COPY;
import static seco.notebook.Actions.CUT;
import static seco.notebook.Actions.EXIT;
import static seco.notebook.Actions.EXPORT;
import static seco.notebook.Actions.IMPORT;
import static seco.notebook.Actions.NEW;
import static seco.notebook.Actions.OPEN;
import static seco.notebook.Actions.PASTE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreeNode;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.gui.dialog.DialogDisplayer;
import seco.gui.dialog.NotifyDescriptor;
import seco.gui.layout.DRect;
import seco.gui.layout.DValue;
import seco.gui.layout.DefaultLayoutHandler;
import seco.gui.layout.LayoutHandler;
import seco.gui.layout.RefPoint;
import seco.gui.menu.EnhancedMenu;
import seco.gui.menu.RecentFilesProvider;
import seco.gui.menu.VisPropsProvider;
import seco.gui.piccolo.TitlePaneNode;
import seco.gui.visual.CellContainerVisual;
import seco.gui.visual.GroupVisual;
import seco.gui.visual.NBUIVisual;
import seco.gui.visual.TabbedPaneVisual;
import seco.gui.visual.VisualAttribs;
import seco.gui.visual.VisualsManager;
import seco.notebook.ActionManager;
import seco.notebook.Actions;
import seco.notebook.AppConfig;
import seco.notebook.NotebookEditorKit;
import seco.notebook.NotebookUI;
import seco.notebook.ScriptletAction;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.util.FileUtil;
import seco.notebook.util.IconManager;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.IOUtils;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBluer;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.pswing.PSwing;

public class GUIHelper
{
    public static final HGPersistentHandle TOOLBAR_HANDLE = UUIDHandleFactory.I
            .makeHandle("d40c99be-f108-11dc-a860-d9a9d2c59ef1");
    public static final HGPersistentHandle MENUBAR_HANDLE = UUIDHandleFactory.I
            .makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    public static final HGPersistentHandle HTML_TOOLBAR_HANDLE = UUIDHandleFactory.I
            .makeHandle("56371f73-025d-11dd-b650-ef87b987c94a");
    public static final HGPersistentHandle WIN_ACTIONS_HANDLE = UUIDHandleFactory.I
            .makeHandle("8724b420-963b-11de-8a39-0800200c9a66");
    public static final HGPersistentHandle OUTPUT_CONSOLE_HANDLE = UUIDHandleFactory.I
            .makeHandle("6817db80-e35a-11df-85ca-0800200c9a66");
    public static final HGPersistentHandle CELL_MENU_ITEMS_HANDLE = UUIDHandleFactory.I
            .makeHandle("b26a4220-ee65-11df-98cf-0800200c9a66");
    public static final HGPersistentHandle CELL_GROUP_MENU_ITEMS_HANDLE = UUIDHandleFactory.I
            .makeHandle("1aee3830-ee78-11df-98cf-0800200c9a66");
    public static final HGPersistentHandle NOTEBOOK_MENU_ITEMS_HANDLE = UUIDHandleFactory.I
            .makeHandle("2f844cd0-ee78-11df-98cf-0800200c9a66");
    public static final HGPersistentHandle CANVAS_NODE_ACTION_SET_HANDLE = UUIDHandleFactory.I
            .makeHandle("1cfd4670-7b7e-11de-8a39-0800200c9a66");
    public static final HGPersistentHandle CANVAS_GLOBAL_ACTION_SET_HANDLE = UUIDHandleFactory.I
            .makeHandle("12231b80-7b7e-11de-8a39-0800200c9a66");

    public static final String LOGO_IMAGE_RESOURCE = "/seco/resources/logoicon.gif";
    // default rectangle used for adding containers
    public static Rectangle CONTAINER_RECT = new Rectangle(20, 70, 300, 300);
    // default size of the minimized components
    static Dimension MINIMIZED_COMPONENT_SIZE = new Dimension(64, 64);

    private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new SecoUncaughtExceptionHandler();

    static
    {
        PlasticLookAndFeel.setPlasticTheme(new DesertBluer());
        try
        {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        }
        catch (Exception e)
        {
        }
    }

    public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler()
    {
        return uncaughtExceptionHandler;
    }

    public static void setUncaughtExceptionHandler(
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler)
    {
        GUIHelper.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public static boolean showConfirmDlg(String message)
    {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                ThisNiche.guiController.getFrame(), message,
                NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
    }

    /**
     * Creates and returns predefined actions displayed in icon form in the
     * right hand side of each canvas component title bar. The default ones are:
     * Maximize and Minimize. You could add your own ones, just make sure an
     * icon is defined. The following example shows the beanshell code
     * for the Maximize action: <code>
     * 
     * node = canvas.getSelectedPSwingNode();
     * cgm = niche.get(node.getHandle()); 
     * //if minimized, unminimized it first
     * if(CellUtils.isMinimized(cgm))
     *    CellUtils.toggleMinimized(cgm);
     * CellUtils.toggleMaximized(cgm);
     * </code>
     * 
     * @return list with the actions
     */
    public static List<Action> getWinTitleActions()
    {
        @SuppressWarnings("unchecked")
        List<Action> actions = (List<Action>) ThisNiche.graph
                .get(WIN_ACTIONS_HANDLE);
        if (actions != null) return actions;
        actions = new ArrayList<Action>();
        // maximize
        Action a = new ScriptletAction("node = canvas.getSelectedPSwingNode();"
                + "cgm = niche.get(node.getHandle());"
                + "if(seco.things.CellUtils.isMinimized(cgm))"
                + "seco.things.CellUtils.toggleMinimized(cgm);"
                + "seco.things.CellUtils.toggleMaximized(cgm);");
        a.putValue(Action.SMALL_ICON, IconManager.resolveIcon("Maximize.gif"));
        a.putValue(Action.SHORT_DESCRIPTION, "Maximize/Restore");
        actions.add(a);
        // minimize
        a = new ScriptletAction(
                "node = canvas.getSelectedPSwingNode();"
                        + "seco.things.CellUtils.toggleMinimized(niche.get(node.getHandle()))");
        a.putValue(Action.SMALL_ICON, IconManager.resolveIcon("Minimize.gif"));
        a.putValue(Action.SHORT_DESCRIPTION, "Minimize/Restore");
        actions.add(a);

        ThisNiche.graph.define(WIN_ACTIONS_HANDLE, actions);
        return actions;
    }

    /**
     * Creates(if not already created) and returns the default application
     * toolbar. Use this method to add your own toolbar buttons.
     * 
     * @return the default application toolbar
     */
    public static JToolBar getMainToolBar()
    {
        JToolBar toolBar = (JToolBar) ThisNiche.graph
                .get(GUIHelper.TOOLBAR_HANDLE);

        if (toolBar != null) return toolBar;

        ActionManager man = ActionManager.getInstance();
        toolBar = new JToolBar("Main");
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        // JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        // separator.setMaximumSize(new Dimension(3, 24));
        toolBar.add(new ToolbarButton(man.getAction(NEW), "New Document"));
        toolBar.add(new ToolbarButton(man.getAction(OPEN), "Open Document"));
        // toolBar.add(new ToolbarButton(man.getAction(SAVE), "Save Document"));
        toolBar.add(new ToolbarButton(man.getAction(EXPORT),
                "Export Document As XML"));
        // toolBar.add(separator);
        toolBar.add(new ToolbarButton(man
                .getAction(NotebookEditorKit.undoAction), "Undo Change"));
        toolBar.add(new ToolbarButton(man
                .getAction(NotebookEditorKit.redoAction), "Redo Change"));
        // toolBar.add(separator);
        toolBar.add(new ToolbarButton(man.getAction(CUT), "Cut"));
        toolBar.add(new ToolbarButton(man.getAction(COPY), "Copy"));
        toolBar.add(new ToolbarButton(man.getAction(PASTE), "Paste"));
        toolBar.add(new ToolbarButton(man
                .getAction(NotebookEditorKit.findAction), "Find"));
        toolBar.add(new ToolbarButton(man
                .getAction(NotebookEditorKit.replaceAction), "Replace"));
        toolBar.add(new ToolbarButton(kit
                .getActionByName(NotebookEditorKit.htmlAction),
                "HTML Preview ON/OFF"));
        toolBar.setFloatable(false);
        ThisNiche.graph.define(GUIHelper.TOOLBAR_HANDLE, toolBar);
        return toolBar;
    }

    /**
     * Creates(if not already created) and returns the default toolbar for the
     * HTML component. Use this method to add your own toolbar buttons.
     * 
     * @return the default application toolbar
     */
    public static HTMLToolBar getHTMLToolBar()
    {
        HTMLToolBar htmlToolBar = (HTMLToolBar) ThisNiche.graph
                .get(GUIHelper.HTML_TOOLBAR_HANDLE);
        if (htmlToolBar != null) return htmlToolBar;
        htmlToolBar = new HTMLToolBar();
        htmlToolBar.init();
        htmlToolBar.setEnabled(false);
        htmlToolBar.setFloatable(false);
        ThisNiche.graph.define(GUIHelper.HTML_TOOLBAR_HANDLE, htmlToolBar);
        return htmlToolBar;
    }

    public static Component getOutputConsole()
    {
        JScrollPane pane = (JScrollPane) ThisNiche.graph
                .get(GUIHelper.OUTPUT_CONSOLE_HANDLE);
        if (pane != null) return pane.getViewport().getView();
        pane = new JScrollPane(new OutputConsole());
        ThisNiche.graph.define(GUIHelper.OUTPUT_CONSOLE_HANDLE, pane);
        return pane.getViewport().getView();
    }

    public static void showOutputConsole()
    {
        getOutputConsole();
        HGHandle existingH = GUIHelper.getCellHandleByValueHandle(
                ThisNiche.TOP_CELL_GROUP_HANDLE, OUTPUT_CONSOLE_HANDLE);
        if (existingH == null)
        {
            CellGroupMember cgm = ThisNiche.graph.get(GUIHelper.addToCellGroup(
                    OUTPUT_CONSOLE_HANDLE, getTopCellGroup(), null, null,
                    new Rectangle(5, 500, 600, 100), true));
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

    // disable menuItems if no notebook presented
    // use GlobMenuItem to prevent disabling
    public static class NBMenu extends PiccoloMenu implements MenuListener
    {
        public NBMenu()
        {
            super();
            addMenuListener(this);
        }

        public NBMenu(String s)
        {
            super(s);
            addMenuListener(this);
        }

        public void menuSelected(MenuEvent e)
        {
            boolean b = NotebookUI.getFocusedNotebookUI() != null;
            for (int i = 0; i < getMenuComponentCount(); i++)
            {
                Component c = getMenuComponent(i);
                if (/* b == true && */c instanceof JMenuItem)
                {
                    Action a = ((JMenuItem) c).getAction();
                    if (a != null) b = a.isEnabled();
                }
                c.setEnabled(b);
            }
        }

        public void menuCanceled(MenuEvent e)
        {
        }

        public void menuDeselected(MenuEvent e)
        {
        }
    }

    public static class PiccoloMenu extends JMenu
    {
        public PiccoloMenu()
        {
            super();
        }

        public PiccoloMenu(String s)
        {
            super(s);
        }

        @Override
        protected Point getPopupMenuOrigin()
        {
            Point pt = super.getPopupMenuOrigin();
            if (getParent() != null && getParent() instanceof JComponent)
                return adjustPointInPicollo((JComponent) getParent(), pt);
            return pt;
        }
    }

    // JMenuItem that can't be disabled
    public static class GlobMenuItem extends JMenuItem
    {
        public GlobMenuItem()
        {
        }

        public GlobMenuItem(String text)
        {
            super(text);
        }

        public GlobMenuItem(Action a)
        {
            super(a);
        }

        @Override
        public boolean isEnabled()
        {
            return true;
        }

        public void setEnabled(boolean b)
        {
            // DO NOTHING
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

    public static class ImportAction extends AbstractAction
    {
        public ImportAction()
        {
            this.putValue(Action.NAME, Actions.IMPORT);
            this.putValue(Action.SMALL_ICON,
                    IconManager.resolveIcon("Open16.gif"));
            this.putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            openNotebook();
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

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            File f = FileUtil.getFile(GUIUtilities.getFrame(ui),
                    "Export Notebook As ...", FileUtil.SAVE, null);
            if (f != null)
            {
                IOUtils.exportCellGroup((CellGroup) ui.getDoc().getBook(),
                        f.getAbsolutePath());
            }
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
        public void actionPerformed(ActionEvent evt)
        {
            SearchDescriptionPanel p = new SearchDescriptionPanel();
            JDialog dialog = new JDialog(ThisNiche.guiController.getFrame(),
                    "Manage Descriptions");
            dialog.add(p);
            dialog.setSize(new Dimension(700, 500));
            dialog.setVisible(true);
        }
    }

    public static class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            putValue(Action.NAME, OPEN);
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("Open16.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(ActionEvent evt)
        {
            JDialog dialog = new JDialog(GUIUtilities.getFrame((Component) evt
                    .getSource()), "Open Or Delete CellGroup");
            dialog.setSize(500, 500);
            dialog.add(new OpenBookPanel());
            dialog.setVisible(true);
        }
    }

    public static class EditMenuListener implements MenuListener
    {
        public void menuSelected(MenuEvent e)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui != null)
            {
                NotebookEditorKit.undo.updateUndoState(ui.getUndoManager());
                NotebookEditorKit.redo.updateRedoState(ui.getUndoManager());
            }
        }

        public void menuCanceled(MenuEvent e)
        {
        }

        public void menuDeselected(MenuEvent e)
        {
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
            GUIHelper.showOutputConsole();
        }
    }

    public static class CellNumItemListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == null
                    || !(e.getSource() instanceof JCheckBoxMenuItem)) return;
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui != null)
                ui.setDrawCellNums(((JCheckBoxMenuItem) e.getSource())
                        .isSelected());
        }
    }

    /**
     * Creates(if not already created) and returns the default application menu
     * bar. Use this method to add your own menus and menu items.
     * 
     * @return the default application menu bar
     */
    public static JMenuBar getMenuBar()
    {
        JMenuBar menuBar = ThisNiche.graph.get(GUIHelper.MENUBAR_HANDLE);
        if (menuBar == null)
        {
            menuBar = new JMenuBar();
            menuBar.add(createFileMenu());
            menuBar.add(createEditMenu());
            menuBar.add(createFormatMenu());
            menuBar.add(createToolsMenu());
            menuBar.add(createRuntimeMenu());
            menuBar.add(createWindowMenu());
            menuBar.add(createNetworkMenu());

            ThisNiche.graph.define(GUIHelper.MENUBAR_HANDLE, menuBar);
            // force the creation of the NotebookUI static popup
            NotebookUI.getPopupMenu();
        }
        return menuBar;
    }

    private static JMenu createRuntimeMenu()
    {
        JMenu menu = new PiccoloMenu("Runtime");
        menu.setMnemonic('r');
        String code = "new seco.gui.rtctx.NewRuntimeContextDialog().setVisible(true);";
        JMenuItem mi = new JMenuItem(new ScriptletAction(code));
        mi.setText("New Context");
        menu.add(mi);

        code = "new seco.gui.rtctx.EditRuntimeContextDialog( ThisNiche.graph.get(ThisNiche.TOP_CONTEXT_HANDLE)).setVisible(true)";
        mi = new JMenuItem(new ScriptletAction(code));
        mi.setText("Configure Current");
        menu.add(mi);

        code = "new seco.gui.rtctx.ManageRuntimeContextDialog().setVisible(true)";
        mi = new JMenuItem(new ScriptletAction(code));
        mi.setText("Manage Contexts");
        menu.add(mi);
        return menu;
    }

    private static JMenu createWindowMenu()
    {
        JMenu top_menu = new PiccoloMenu("Window");
        top_menu.setMnemonic('w');
        JMenu menu = new PiccoloMenu("Add");
        String code = "import seco.gui.*; GUIHelper.addContainer(CellContainerVisual.getHandle());";
        JMenuItem mi = new JMenuItem(new ScriptletAction(code));
        mi.setText("Container");
        menu.add(mi);

        code = "import seco.gui.*;GUIHelper.addContainer(TabbedPaneVisual.getHandle());";
        mi = new JMenuItem(new ScriptletAction(code));
        mi.setText("Tabbed Pane");
        menu.add(mi);
        top_menu.add(menu);

        return top_menu;
    }

    private static JMenu createNetworkMenu()
    {
        JMenu menu = new PiccoloMenu("Network");
        menu.setMnemonic('n');
        String lang = "jscheme";
        String code = "(load \"jscheme/scribaui.scm\")\n(netdialog-action '())\n";
        ScriptletAction a = new ScriptletAction(lang, code);
        JMenuItem mi = new JMenuItem(a);
        mi.setText("Connection");
        menu.add(mi);
        return menu;
    }

    public static final NotebookEditorKit kit = new NotebookEditorKit();

    static void openElementTree()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        String title = "Elements Hierarchy";
        JDialog dialog = new JDialog(GUIUtilities.getFrame(ui), title);
        dialog.setSize(500, 800);
        JTree tree = new JTree((TreeNode) ui.getDocument()
                .getDefaultRootElement());
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    static void openParseTree()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        String title = "Parsing Hierarchy";
        JDialog dialog = new JDialog(GUIUtilities.getFrame(ui), title);
        dialog.setSize(500, 800);
        JTree tree = ui.getParseTree(ui.getCaretPosition());
        if (tree == null) return;
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    static void openCellTree(CellGroupMember cell)
    {
        JDialog dialog = new TopCellTreeDlg(cell);
        dialog.setSize(500, 800);
        dialog.setVisible(true);
    }

    public static CellGroup getTopCellGroup()
    {
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        if (group == null)
        {
            group = new CellGroup("TOP_CELL_GROUP");
            ThisNiche.graph.define(ThisNiche.TOP_CELL_GROUP_HANDLE, group);
        }
        return group;
    }

    public static void makeTopCellGroup()
    {
        CellGroup group = getTopCellGroup();
        group.setVisual(CellContainerVisual.getHandle());
        getMenuBar();
        if (group.indexOf(GUIHelper.MENUBAR_HANDLE) < 0)
            addToCellGroup(GUIHelper.MENUBAR_HANDLE, group,
                    VisualsManager
                            .defaultVisualForAtom(GUIHelper.MENUBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(0),
                            new DValue(0), new DValue(100, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 0);
        getMainToolBar();
        if (group.indexOf(GUIHelper.TOOLBAR_HANDLE) < 0)
            addToCellGroup(GUIHelper.TOOLBAR_HANDLE, group,
                    VisualsManager
                            .defaultVisualForAtom(GUIHelper.TOOLBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(0),
                            new DValue(28), new DValue(/* 280 */33, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 1);
        getHTMLToolBar();
        if (group.indexOf(GUIHelper.HTML_TOOLBAR_HANDLE) < 0)
            addToCellGroup(
                    GUIHelper.HTML_TOOLBAR_HANDLE,
                    group,
                    VisualsManager
                            .defaultVisualForAtom(GUIHelper.HTML_TOOLBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(/* 280 */33,
                            true), new DValue(28), new DValue(67, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 2);
        initCGMActions();
    }

    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r,
            boolean create_cell, Map<Object, Object> addit_attribs, int index)
    {
        HGHandle cellH = (create_cell) ? CellUtils.getOrCreateCellHForRefH(h)
                : h;
        CellGroupMember out = ThisNiche.graph.get(cellH);
        if (r != null) out.setAttribute(VisualAttribs.rect, r);
        if (visualH != null) out.setVisual(visualH);
        if (lh != null) out.setAttribute(VisualAttribs.layoutHandler, lh);
        if (addit_attribs != null) for (Object key : addit_attribs.keySet())
            out.setAttribute(key, addit_attribs.get(key));
        int i = (index >= 0 && index <= group.getArity()) ? index : group
                .getArity();
        group.insert(i, out);
        return cellH;
    }

    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r, boolean create_cell)
    {
        return addToCellGroup(h, group, visualH, lh, r, create_cell, null, -1);
    }

    static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r,
            boolean create_cell, int index)
    {
        return addToCellGroup(h, group, visualH, lh, r, create_cell, null,
                index);
    }

    static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r, int index)
    {
        return addToCellGroup(h, group, visualH, lh, r, true, index);
    }

    public static HGHandle addToTopCellGroup(HGHandle h, HGHandle visualH,
            LayoutHandler lh, Rectangle r)
    {
        CellGroup top = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        return addToCellGroup(h, top, visualH, lh, r, false);
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
        return addToCellGroup(h,
                (CellGroup) ThisNiche.graph
                        .get(ThisNiche.TOP_CELL_GROUP_HANDLE), visualH, null,
                CONTAINER_RECT, false, attribs, -1);
    }

    public static HGHandle addToTopCellGroup(final Object x, final Rectangle r)
    {
        return ThisNiche.graph.getTransactionManager().transact(
                new Callable<HGHandle>() {
                    public HGHandle call()
                    {
                        HGHandle h = ThisNiche.graph.getHandle(x);
                        if (h == null) h = ThisNiche.graph.add(x);
                        return addToTopCellGroup(h, null, null, r);
                    }
                });
    }

    /**
     * Adds a new object in the given group by wrapping it as a cell value if
     * it's not a CellGroupMember instance. If the object is already present in
     * the group, returns a handle to it's wrapping cell
     * 
     * @param groupHandle
     *            Handle of the group where the new object will be inserted
     * @param objectHandle
     *            Handle to the object to be inserted
     * @param visualHandle
     *            Handle to the CellVisual that will be used for presentation.
     *            Could be null.
     * @param layoutHandler
     *            LayoutHandler to be used. Could be null.
     * @param r
     *            Rectangle specifying the position and size of the component
     * @return the handle to the newly created or existing cell
     */
    public static HGHandle addIfNotThere(HGHandle groupHandle,
            HGHandle objectHandle, HGHandle visualHandle,
            LayoutHandler layoutHandler, Rectangle r)
    {
        return addIfNotThere(groupHandle, objectHandle, visualHandle,
                layoutHandler, r, null);
    }

    /**
     * Adds a new object in the given group by wrapping it as a cell value if
     * it's not a CellGroupMember instance. If the object is already present in
     * the group, returns a handle to it's wrapping cell
     * 
     * @param groupHandle
     *            Handle of the group where the new object will be inserted
     * @param objectHandle
     *            Handle to the object to be inserted
     * @param visualHandle
     *            Handle to the CellVisual that will be used for presentation.
     *            Could be null.
     * @param layoutHandler
     *            LayoutHandler to be used. Could be null.
     * @param r
     *            Rectangle specifying the position and size of the component
     * @param     addit_attribs Map with values that will be assigned as attributes
     * to the newly created cell
     * @return the handle to the newly created or existing cell
     */
    public static HGHandle addIfNotThere(HGHandle groupHandle,
            HGHandle objectHandle, HGHandle visualHandle, LayoutHandler lh,
            Rectangle r, Map<Object, Object> addit_attribs)
    {
        HGHandle existingH = getCellHandleByValueHandle(groupHandle,
                objectHandle);
        if (existingH != null) return existingH;

        CellGroup group = ThisNiche.graph.get(groupHandle);
        Object x = ThisNiche.graph.get(objectHandle);
        return GUIHelper.addToCellGroup(objectHandle, group, null, null, r,
                !(x instanceof CellGroupMember), addit_attribs, -1);
    }

    /**
     * Search in a specified group for a cell containing given value handle. 
     * Inner groups are searched only if their CellVisual is implementing GroupVisual  
     * @param groupHandle The group where to search
     * @param objectHandle The value handle to search
     * @return The cell which meets the search criteria or null
     */
    public static HGHandle getCellHandleByValueHandle(HGHandle groupHandle,
            HGHandle objectHandle)
    {
        CellGroup group = ThisNiche.graph.get(groupHandle);
        for (int i = 0; i < group.getArity(); i++)
        {
            Object x = ThisNiche.graph.get(group.getTargetAt(i));
            if (x instanceof Cell
                    && ((Cell) x).getAtomHandle().equals(objectHandle)) return group
                    .getTargetAt(i);
            else if (x instanceof CellGroup)
            {
                CellGroup inner = (CellGroup) x;
                Object visual = ThisNiche.graph.get(inner.getVisual());
                if (visual instanceof GroupVisual)
                {
                    HGHandle inH = getCellHandleByValueHandle(
                            ThisNiche.handleOf(inner), objectHandle);
                    if (inH != null) return inH;
                }
            }
        }
        return null;
    }

    static void removeFromCellGroup(HGHandle groupH, HGHandle h, boolean backup)
    {
        CellGroup top = ThisNiche.graph.get(groupH);
        top.remove((CellGroupMember) ThisNiche.graph.get(h), backup);
    }

    /**
     * Helper method that returns the PSwingNode of a given component displayed in the canvas  
     * @param c The component
     * @return the PSwingNode
     */
    public static PSwingNode getPSwingNode(JComponent c)
    {
        if (c == null) return null;
        PSwingNode ps = (PSwingNode) c
                .getClientProperty(PSwing.PSWING_PROPERTY);
        if (ps != null) return ps;
        if (c.getParent() instanceof JComponent)
            return getPSwingNode((JComponent) c.getParent());
        return null;
    }

    /**
     * Helper method that performs the necessary transformations to adjust coordinates 
     * of a point given the component to which it belongs  
     * @param c The component displayed in canvas
     * @param pt The point
     * @return adjusted point
     */
    public static Point adjustPointInPicollo(JComponent c, Point pt)
    {
        PSwingNode ps = getPSwingNode(c);
        if (ps == null) return pt;
        PiccoloCanvas canvas = ps.getCanvas();
        PBounds r1c = ps.getBounds();
        ps.localToGlobal(r1c);
        canvas.getCamera().globalToLocal(r1c);
        Rectangle2D r = canvas.getCamera().getViewTransform()
                .createTransformedShape(r1c).getBounds2D();
        return new Point((int) (r.getX() + pt.x), (int) (r.getY() + pt.y));
    }

    /**
     * Opens a given notebook(that is already stored in the niche) in the canvas.
     * Do nothing if the notebook is already opened.
     * @param bookH The notebook's handle 
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
        File file = FileUtil.getFile(ThisNiche.guiController.getFrame(),
                "Load Notebook", FileUtil.LOAD, null);
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
                    ThisNiche.guiController.getFrame(), t, "Could not open: "
                            + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // strange requirement to open new Notebook, if file doesn't exist
            newNotebook();
        }
    }

    static void updateFrameTitle(HGHandle h)
    {
        CellGroupMember book = ThisNiche.graph.get(h);
        String name = CellUtils.getName(book);
        if (name == null) name = "";
        // RuntimeContext rcInstance = (RuntimeContext)
        // ThisNiche.graph.get(TopFrame
        // .getInstance().getCurrentRuntimeContext());
        String title = "[" + ThisNiche.graph.getLocation() + "] "
        /* + rcInstance.getName() + " " */+ name;
        if (ThisNiche.guiController != null)
            ThisNiche.guiController.setTitle(title);
        // ThisNiche.guiController.showHTMLToolBar(false);
        GUIHelper.getHTMLToolBar().setEnabled(false);
    }

    // Create the edit menu.
    private static JMenu createEditMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new NBMenu("Edit");
        menu.setMnemonic('e');
        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.undoAction)));
        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.redoAction)));
        menu.addSeparator();
        menu.addMenuListener(new EditMenuListener());

        menu.add(new JMenuItem(man.getAction(Actions.CUT)));
        menu.add(new JMenuItem(man.getAction(Actions.COPY)));
        menu.add(new JMenuItem(man.getAction(Actions.PASTE)));

        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.selectAllAction)));
        menu.addSeparator();
        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.findAction)));
        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.replaceAction)));
        return menu;
    }

    private static JMenu createFileMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new NBMenu("Notebook");
        menu.setMnemonic('b');
        menu.add(new JMenuItem(man.getAction(NEW)));
        menu.add(new JMenuItem(man.getAction(OPEN)));
        menu.add(new JMenuItem(man.getAction(IMPORT)));
        menu.add(new JMenuItem(man.getAction(EXPORT)));
        menu.add(new JSeparator());
        menu.add(new EnhancedMenu("Recent Files", new RecentFilesProvider()));
        // menu.add(new JSeparator());
        // menu.add(new JMenuItem(man.putAction(new ExitAction(), KeyStroke
        // .getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK))));
        return menu;
    }

    private static JMenu createFormatMenu()
    {
        JMenu menu = new NBMenu("Format");
        menu.setMnemonic('o');
        ActionManager man = ActionManager.getInstance();
        menu.add(new EnhancedMenu("Visual Properties", new VisPropsProvider()));
        menu.add(new GlobMenuItem(man
                .getAction(NotebookEditorKit.shortcutInspectorAction)));
        menu.add(new GlobMenuItem(man
                .getAction(NotebookEditorKit.abbreviationManagerAction)));
        Action act = kit.getActionByName(NotebookEditorKit.formatAction);
        if (act != null)
        {
            act.putValue(Action.NAME, "Format");
            JMenuItem mi = new JMenuItem(act);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                    ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
            menu.add(mi);
        }
        return menu;
    }

    private static JMenu createToolsMenu()
    {
        JMenu menu = new NBMenu("Tools");
        menu.setMnemonic('t');
        ActionManager man = ActionManager.getInstance();
        menu.add(new JMenuItem(man.getAction(NotebookEditorKit.evalAction)));
        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.evalCellGroupAction)));
        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.reEvalOutputCellsAction)));
        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.removeOutputCellsAction)));

        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.mergeCellsAction)));
        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.clearEngineContextAction)));

        menu.add(new GlobMenuItem(man
                .getAction(NotebookEditorKit.javaDocManagerAction)));
        menu.add(new JMenuItem(man
                .getAction(NotebookEditorKit.ctxInspectorAction)));
        GlobMenuItem mi = new GlobMenuItem("Manage Cell Descriptions");
        mi.addActionListener(new DescriptionManagerAction());
        menu.add(mi);
        menu.add(GUIHelper.makeCellMenu());
        menu.add(GUIHelper.makeCellGroupMenu());
        menu.add(GUIHelper.makeNotebookMenu());
        menu.add(new JSeparator());
        mi = new GlobMenuItem("Show Output Console");
        mi.addActionListener(new ShowOutputConsoleAction());
        menu.add(mi);
        mi = new GlobMenuItem("Top CellGroup Tree");
        mi.addActionListener(new TopCellTreeAction());
        menu.add(mi);
        return menu;
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

    public static void handleTitle(PSwingNode node)
    {
        CellGroupMember cgm = ThisNiche.graph.get(node.getHandle());
        if (CellUtils.isMinimized(cgm))
        {
            update_minimized_UI(node, cgm);
            return;
        }
        String title = CellUtils.getName(cgm);
        if (CellUtils.isShowTitle(cgm) && title != null)
        {
            PCSelectionHandler.decorateSelectedNode(node, false);
            // move down the node with the height of the added title pane
            node.translate(0, TitlePaneNode.HEIGHT);
        }
        else if (!CellUtils.isShowTitle(cgm))
        {
            PCSelectionHandler.undecorateSelectedNode(node, true);
        }
    }

    public static JComponent getMinimizedUI(final CellGroupMember cgm)
    {
        return new MinimizedUI(cgm);
    }

    static Dimension getMinimizedUISize()
    {
        return MINIMIZED_COMPONENT_SIZE;
    }

    private static void update_minimized_UI(PSwingNode node, CellGroupMember cgm)
    {
        String text = CellUtils.getName(cgm);
        if (text == null) text = "Untitled";
        MinimizedUI ui = (MinimizedUI) node.getComponent();
        ui.setTitle(text);
    }

    public static JMenu makeNotebookMenu()
    {
        return new CGMActionsHelper.DynamicMenu(NOTEBOOK_MENU_ITEMS_HANDLE, 
                "Book",
                CGMActionsHelper.Scope.book); 
    }
    
    private static void initNotebookMenuItems()
    {
        Collection<JMenuItem> menu = ThisNiche.graph.get(NOTEBOOK_MENU_ITEMS_HANDLE);
        if (menu == null)
        {
            menu = new ArrayList<JMenuItem>();
            menu.add(new JCheckBoxMenuItem(new CGMActionsHelper.InitCellAction(
                    CGMActionsHelper.Scope.book)));
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.ReadOnlyCellAction(
                            CGMActionsHelper.Scope.book)));
            // menu.add(new JMenuItem(new EvalAction(Scope.group)));
            menu.add(new EnhancedMenu(CGMActionsHelper.LABEL_RUNTIME_CONTEXT,
                    new CGMActionsHelper.RCListProvider(
                            CGMActionsHelper.Scope.book)));
            // menu.add(new JMenuItem(new DescriptionAction(Scope.group)));

            ThisNiche.graph.define(NOTEBOOK_MENU_ITEMS_HANDLE, menu);
        }
    }

    public static JMenu makeCellGroupMenu()
    {
        return new CGMActionsHelper.DynamicMenu(CELL_GROUP_MENU_ITEMS_HANDLE, 
                "Cell Group", CGMActionsHelper.Scope.group); 
    }
    
    private static void initCellGroupMenuItems()   { 
        Collection<JMenuItem> menu  = ThisNiche.graph.get(CELL_GROUP_MENU_ITEMS_HANDLE);
        if (menu == null)
        {
            menu = new ArrayList<JMenuItem>();
            menu.add(new JCheckBoxMenuItem(new CGMActionsHelper.InitCellAction(
                    CGMActionsHelper.Scope.group)));
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.ReadOnlyCellAction(
                            CGMActionsHelper.Scope.group)));
            menu.add(new JMenuItem(new CGMActionsHelper.EvalAction(
                    CGMActionsHelper.Scope.group)));
            menu.add(new EnhancedMenu(CGMActionsHelper.LABEL_RUNTIME_CONTEXT,
                    new CGMActionsHelper.RCListProvider(
                            CGMActionsHelper.Scope.group)));
            menu.add(new JMenuItem(new CGMActionsHelper.DescriptionAction(
                    CGMActionsHelper.Scope.group)));
            ThisNiche.graph.define(CELL_GROUP_MENU_ITEMS_HANDLE, menu);
        }
    }
    
    private static void initCGMActions()
    {
        initCellMenuItems();
        initCellGroupMenuItems();
        initNotebookMenuItems();
    }

    private static void initCellMenuItems()
    {
        Collection<JMenuItem> menu = ThisNiche.graph.get(CELL_MENU_ITEMS_HANDLE);
        if (menu == null)
        {
            menu = new ArrayList<JMenuItem>();
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.InitCellAction()));
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.ReadOnlyCellAction()));
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.HtmlCellAction()));
            menu.add(new JCheckBoxMenuItem(
                    new CGMActionsHelper.ErrorCellAction()));

            menu.add(new JMenuItem(new CGMActionsHelper.EvalAction()));
            menu.add(new JMenuItem(
                    new CGMActionsHelper.RemoveOutputCellsAction()));
            menu.add(new EnhancedMenu(CGMActionsHelper.LABEL_RUNTIME_CONTEXT,
                    new CGMActionsHelper.RCListProvider(
                            CGMActionsHelper.Scope.cell)));
            menu.add(new JMenuItem(new CGMActionsHelper.DescriptionAction()));
            ThisNiche.graph.define(CELL_MENU_ITEMS_HANDLE, menu);
        }
    }
    
    public static JMenu makeCellMenu()
    {
      return new CGMActionsHelper.DynamicMenu(CELL_MENU_ITEMS_HANDLE, "Cell", CGMActionsHelper.Scope.cell);
    }
}
