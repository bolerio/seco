package seco.gui;

import static seco.notebook.Actions.COPY;
import static seco.notebook.Actions.CUT;
import static seco.notebook.Actions.EXIT;
import static seco.notebook.Actions.EXPORT;
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
import java.io.File;
import java.util.ArrayList;
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
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeNode;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;

import seco.ThisNiche;
import seco.gui.layout.DRect;
import seco.gui.layout.DValue;
import seco.gui.layout.DefaultLayoutHandler;
import seco.gui.layout.LayoutHandler;
import seco.gui.layout.RefPoint;
import seco.gui.piccolo.TitlePaneNode;
import seco.notebook.ActionManager;
import seco.notebook.Actions;
import seco.notebook.AppConfig;
import seco.notebook.NotebookEditorKit;
import seco.notebook.NotebookUI;
import seco.notebook.ScriptletAction;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.OpenBookPanel;
import seco.notebook.gui.ToolbarButton;
import seco.notebook.gui.menu.CellGroupPropsProvider;
import seco.notebook.gui.menu.CellPropsProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.RecentFilesProvider;
import seco.notebook.gui.menu.VisPropsProvider;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.util.FileUtil;
import seco.notebook.util.IconManager;
import seco.rtenv.RuntimeContext;
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
    public static final HGPersistentHandle TOOLBAR_HANDLE = HGHandleFactory
            .makeHandle("d40c99be-f108-11dc-a860-d9a9d2c59ef1");
    public static final HGPersistentHandle MENUBAR_HANDLE = HGHandleFactory
            .makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    public static final HGPersistentHandle HTML_TOOLBAR_HANDLE = HGHandleFactory
            .makeHandle("56371f73-025d-11dd-b650-ef87b987c94a");
    public static final HGPersistentHandle WIN_ACTIONS_HANDLE = HGHandleFactory
    .makeHandle("8724b420-963b-11de-8a39-0800200c9a66");
    
    public static final String LOGO_IMAGE_RESOURCE = "/seco/resources/logoicon.gif";
    //default rectangle used for adding containers 
    public static Rectangle CONTAINER_RECT = new Rectangle(20, 70, 300, 300);
    
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
    
   // public static final String ANOTHER_ICON = "ANOTHER_ICON";
    public static List<Action> getWinTitleActions()
    {
        List<Action> actions = (List<Action>)ThisNiche.graph.get(WIN_ACTIONS_HANDLE);
        if(actions != null) return actions;
        actions = new ArrayList<Action>();
        //maximize
        Action a = new ScriptletAction(
                "node = desktop.canvas.getSelectedPSwingNode();"+
                "cgm = niche.get(node.getHandle());" +
                "if(seco.things.CellUtils.isMinimized(cgm))" +
                "seco.things.CellUtils.toggleMinimized(cgm);" +
                "seco.things.CellUtils.toggleMaximized(cgm);");
        a.putValue(Action.SMALL_ICON, IconManager.resolveIcon("Maximize.gif"));
        //a.putValue(ANOTHER_ICON, IconManager.resolveIcon("Restore.png"));
        a.putValue(Action.SHORT_DESCRIPTION, "Maximize/Restore");
        actions.add(a);
       //minimize
        a = new ScriptletAction("node = desktop.canvas.getSelectedPSwingNode();"+
        "seco.things.CellUtils.toggleMinimized(niche.get(node.getHandle()))");
        a.putValue(Action.SMALL_ICON, IconManager.resolveIcon("Minimize.gif"));
       // a.putValue(ANOTHER_ICON, IconManager.resolveIcon("Restore.png"));
        a.putValue(Action.SHORT_DESCRIPTION, "Minimize/Restore");
        actions.add(a);
        
        ThisNiche.graph.define(WIN_ACTIONS_HANDLE, actions);
        return actions;
    }

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
                return GUIHelper.computePoint((JComponent) getParent(), pt);
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
            this.putValue(Action.SMALL_ICON, IconManager
                    .resolveIcon("Open16.gif"));
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
            this.putValue(Action.SMALL_ICON, IconManager
                    .resolveIcon("SaveAs16.gif"));
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
                IOUtils.exportCellGroup((CellGroup) ui.getDoc().getBook(), f
                        .getAbsolutePath());
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
            TopFrame.getInstance().exit();
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

    public static class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            putValue(Action.NAME, OPEN);
            putValue(Action.SMALL_ICON, IconManager.resolveIcon("Open16.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            JDialog dialog = new JDialog(TopFrame.getInstance(),
                    "Open Or Delete CellGroup");
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
        String lang = "jscheme";
        String code = "(load \"jscheme/scribaui.scm\")\n(.show (edit-context-dialog #null (RuntimeContext.)))";
        ScriptletAction a = new ScriptletAction(lang, code);
        JMenuItem mi = new JMenuItem(a);
        mi.setText("New Context");
        menu.add(mi);

        code = "(load \"jscheme/scribaui.scm\")\n (let ((h (.getCurrentRuntimeContext desktop)))"
                + " (.show (edit-context-dialog h (.get niche h))))";
        a = new ScriptletAction(lang, code);
        mi = new JMenuItem(a);
        mi.setText("Configure Current");
        menu.add(mi);

        code = "(load \"jscheme/scribaui.scm\")\n(.show (manage-contexts-dialog))";
        a = new ScriptletAction(lang, code);
        mi = new JMenuItem(a);
        mi.setText("Manage Contexts");
        menu.add(mi);
        return menu;
    }

    private static JMenu createWindowMenu()
    {
        JMenu top_menu = new PiccoloMenu("Window");
        top_menu.setMnemonic('w');
        JMenu menu = new PiccoloMenu("Add");
        String lang = "beanshell";
        String code = "import seco.gui.*; GUIHelper.addContainer(CellContainerVisual.getHandle());";
        ScriptletAction a = new ScriptletAction(lang, code);
        JMenuItem mi = new JMenuItem(a);
        mi.setText("Container");
        menu.add(mi);

        code = "import seco.gui.*;GUIHelper.addContainer(TabbedPaneVisual.getHandle());";
        a = new ScriptletAction(lang, code);
        mi = new JMenuItem(a);
        mi.setText("Tabbed Pane");
        menu.add(mi);

        // code =
        // "(load \"jscheme/scribaui.scm\")\n(.show (manage-contexts-dialog))";
        // a = new ScriptletAction(lang, code);
        // mi = new JMenuItem(a);
        // mi.setText("Manage Contexts");
        // menu.add(mi);
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

    public static void openElementTree()
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

    public static void openParseTree()
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

    public static void openCellTree(CellGroupMember cell)
    {
        JDialog dialog = new TopCellTreeDlg(cell);
        dialog.setSize(500, 800);
        dialog.setVisible(true);
    }

    public static void makeTopCellGroup(HyperGraph hg)
    {
        CellGroup group = ThisNiche.graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        if (group == null)
        {
            group = new CellGroup("TOP_CELL_GROUP");
            hg.define(ThisNiche.TOP_CELL_GROUP_HANDLE, group);
        }
        group.setVisual(CellContainerVisual.getHandle());
        getMenuBar();
        if (group.indexOf(GUIHelper.MENUBAR_HANDLE) < 0)
            addToCellGroup(GUIHelper.MENUBAR_HANDLE, group, VisualsManager
                    .defaultVisualForAtom(GUIHelper.MENUBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(0),
                            new DValue(0), new DValue(100, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 0);
        getMainToolBar();
        if (group.indexOf(GUIHelper.TOOLBAR_HANDLE) < 0)
            addToCellGroup(GUIHelper.TOOLBAR_HANDLE, group, VisualsManager
                    .defaultVisualForAtom(GUIHelper.TOOLBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(0),
                            new DValue(28), new DValue(/* 280 */33, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 1);
        getHTMLToolBar();
        if (group.indexOf(GUIHelper.HTML_TOOLBAR_HANDLE) < 0)
            addToCellGroup(GUIHelper.HTML_TOOLBAR_HANDLE, group, VisualsManager
                    .defaultVisualForAtom(GUIHelper.HTML_TOOLBAR_HANDLE),
                    new DefaultLayoutHandler(new DRect(new DValue(/* 280 */33,
                            true), new DValue(28), new DValue(67, true),
                            new DValue(28)), RefPoint.TOP_LEFT), null, 2);

        // getJTabbedPane();
        // if(group.indexOf(ThisNiche.TABBED_PANE_GROUP_HANDLE) < 0)
        // group.insert(group.getArity(), ThisNiche.TABBED_PANE_GROUP_HANDLE);
    }

    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r, boolean create_cell,
            Map<Object, Object> addit_attribs, int index)
    {
        HGHandle cellH = (create_cell) ? CellUtils.getOrCreateCellHForRefH(h) : h;
        CellGroupMember out = ThisNiche.graph.get(cellH);
        if (r != null) out.setAttribute(VisualAttribs.rect, r);
        if (visualH != null) out.setVisual(visualH);
        if (lh != null) out.setAttribute(VisualAttribs.layoutHandler, lh);
        if(addit_attribs != null)
            for(Object key: addit_attribs.keySet())
                out.setAttribute(key, addit_attribs.get(key));
        int i = (index >= 0 && index <= group.getArity()) ? index : group.getArity();
        group.insert(i, out);
        return cellH;
    }
    
    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r, boolean create_cell)
    {
        return addToCellGroup(h, group, visualH, lh, r, create_cell, null, -1);
    }
    
    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
            HGHandle visualH, LayoutHandler lh, Rectangle r, boolean create_cell, int index)
    {
        return addToCellGroup(h, group, visualH, lh, r, create_cell, null, index);
    }

    public static HGHandle addToCellGroup(HGHandle h, CellGroup group,
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

    public static HGHandle addContainer(HGHandle visualH)
    {
        String name = "ContainerCellGroup";
        if (TabbedPaneVisual.getHandle().equals(visualH)) name = "TabbedPaneCellGroup";
        else if (CellContainerVisual.getHandle().equals(visualH))
            name = "CanvasCellGroup";
        CellGroup c = new CellGroup(name);
        HGHandle h = ThisNiche.graph.add(c);
        return addToTopCellGroup(h, visualH, null, CONTAINER_RECT);
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

    public static HGHandle addIfNotThere(HGHandle groupHandle,
            HGHandle objectHandle, HGHandle visualHandle, LayoutHandler lh,
            Rectangle r)
    {
        return addIfNotThere(groupHandle,
                objectHandle, visualHandle, lh, r, null);
    }
    
    public static HGHandle addIfNotThere(HGHandle groupHandle,
            HGHandle objectHandle, HGHandle visualHandle, LayoutHandler lh,
            Rectangle r, Map<Object, Object> addit_attribs)
    {
        HGHandle existingH = 
            getCellHandleByValueHandle(groupHandle, objectHandle);
        if(existingH != null) return existingH;
        
        CellGroup group = ThisNiche.graph.get(groupHandle);
        Object x = ThisNiche.graph.get(objectHandle);
        return GUIHelper.addToCellGroup(objectHandle, group, null, null, r,
                !(x instanceof CellGroupMember), addit_attribs, -1);
    }
    
    public static HGHandle getCellHandleByValueHandle(HGHandle groupHandle, HGHandle objectHandle)
    {
        CellGroup group = ThisNiche.graph.get(groupHandle);
        for (int i = 0; i < group.getArity(); i++)
        {
            Object x = ThisNiche.graph.get(group.getTargetAt(i));
            if (x instanceof Cell
                    && ((Cell) x).getAtomHandle().equals(objectHandle))
                return group.getTargetAt(i);
            else if(x instanceof CellGroup)
            {
                CellGroup inner = (CellGroup) x;
                if(inner.getVisualInstance() instanceof GroupVisual)
                {
                    HGHandle inH = getCellHandleByValueHandle(ThisNiche.handleOf(inner), objectHandle);
                    if(inH != null) return inH;
                }
            }
        }
        return null;
    }

    public static void removeFromCellGroup(HGHandle groupH, HGHandle h, boolean backup)
    {
        CellGroup top = ThisNiche.graph.get(groupH);
        top.remove((CellGroupMember) ThisNiche.graph.get(h), backup);
    }

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

    public static Point computePoint(JComponent c, Point pt)
    {
        PSwingNode ps = getPSwingNode(c);
        if (ps == null) return pt;
        PBounds r = ps.getFullBounds();
        PiccoloCanvas canvas = ps.getCanvas();
        PSwingNode par = GUIHelper.getPSwingNode(canvas);
        if (par == null)
            return new Point((int) (pt.x + r.x), (int) (pt.y + r.y));
        PBounds r1 = par.getFullBounds();
        return new Point((int) (r.x + r1.x + pt.x), (int) (r.y + r1.y + pt.y));
    }

    public static void openNotebook(HGHandle bookH)
    {
        if (getOpenedBooks().contains(bookH)) return;
        addAsBook(bookH);
    }

    private static void addAsBook(HGHandle h)
    {
        CellGroup group = ThisNiche.graph.get(TopFrame.getInstance()
                .getFocusedContainerHandle());
        if(CellUtils.isBackuped(h))
            CellUtils.restoreCell(h);
        CellGroupMember child = ThisNiche.graph.get(h);
        child.setVisual(NBUIVisual.getHandle());
        child.setAttribute(VisualAttribs.rect, new Rectangle(100, 100, 500, 400));
        if(!CellUtils.isShowTitle(child))
           CellUtils.toggleShowTitle(child);
        group.insert(group.getArity(), h);
    }

    public static void newNotebook()
    {
        CellGroup nb = new CellGroup("CG");
        HGHandle nbHandle = ThisNiche.graph.add(nb);
        addAsBook(nbHandle);
    }

    public static void openNotebook()
    {
        File file = FileUtil.getFile(TopFrame.getInstance(), "Load Notebook",
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
                    TopFrame.getInstance(), t, "Could not open: "
                            + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // strange requirement to open new Notebook, if file doesn't exist
            newNotebook();
        }
    }

    public static void updateFrameTitle(HGHandle h)
    {
        CellGroupMember book = ThisNiche.graph.get(h);
        String name = CellUtils.getName(book);
        if (name == null) name = "";
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.graph.get(TopFrame
                .getInstance().getCurrentRuntimeContext());
        String title = "[" + ThisNiche.graph.getLocation() + "] "
                + rcInstance.getName() + " " + name;
        TopFrame.getInstance().setTitle(title);
        TopFrame.getInstance().showHTMLToolBar(false);
    }

    // Create the edit menu.
    private static JMenu createEditMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new NBMenu("Edit");
        menu.setMnemonic('e');
        menu.add(new JMenuItem(man.putAction(NotebookEditorKit.undo, KeyStroke
                .getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK),
                IconManager.resolveIcon("Undo16.gif"))));
        menu.add(new JMenuItem(man.putAction(NotebookEditorKit.redo, KeyStroke
                .getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK),
                IconManager.resolveIcon("Redo16.gif"))));
        menu.addSeparator();
        menu.addMenuListener(new EditMenuListener());
        Action act = kit.getActionByName(DefaultEditorKit.cutAction);
        act.putValue(Action.NAME, CUT);
        act.putValue(Action.SHORT_DESCRIPTION, "Cut");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Cut16.gif"))));
        act = kit.getActionByName(DefaultEditorKit.copyAction);
        act.putValue(Action.SHORT_DESCRIPTION, "Copy");
        act.putValue(Action.NAME, COPY);
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Copy16.gif"))));
        act = kit.getActionByName(DefaultEditorKit.pasteAction);
        act.putValue(Action.NAME, PASTE);
        act.putValue(Action.SHORT_DESCRIPTION, "Paste");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Paste16.gif"))));
      
        act = kit.getActionByName(DefaultEditorKit.selectAllAction);
        act.putValue(Action.NAME, "Select All");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
        KeyEvent.VK_A, ActionEvent.CTRL_MASK))));
        menu.addSeparator();
        menu.add(new JMenuItem(man.putAction(kit
                .getActionByName(NotebookEditorKit.findAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(kit
                .getActionByName(NotebookEditorKit.replaceAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK))));
        return menu;
    }

    private static JMenu createFileMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new NBMenu("Notebook");
        menu.setMnemonic('b');
        menu.add(new JMenuItem(man.putAction(new NewAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new OpenAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new ImportAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new ExportAction())));
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
        // NOT USED
        // final JCheckBoxMenuItem m = new JCheckBoxMenuItem("Cell Numbers");
        // m.addItemListener(new CellNumItemListener());
        // menu.add(m);
        // menu.addSeparator();
        menu.add(new EnhancedMenu("Visual Properties", new VisPropsProvider()));
        Action act = kit.getActionByName(NotebookEditorKit.formatAction);
        act.putValue(Action.NAME, "Format");
        JMenuItem mi = new JMenuItem(act);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
        menu.add(mi);
        return menu;
    }

    private static JMenu createToolsMenu()
    {
        JMenu menu = new NBMenu("Tools");
        menu.setMnemonic('t');
        Action act = kit.getActionByName(NotebookEditorKit.evalAction);
        menu.add(new JMenuItem(act));
        act = kit.getActionByName(NotebookEditorKit.evalCellGroupAction);
        menu.add(new JMenuItem(act));
        act = kit.getActionByName(NotebookEditorKit.reEvalOutputCellsAction);
        menu.add(new JMenuItem(act));
        act = kit.getActionByName(NotebookEditorKit.removeOutputCellsAction);
        menu.add(new JMenuItem(act));

        act = ActionManager.getInstance().putAction(
                kit.getActionByName(NotebookEditorKit.mergeCellsAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

        menu.add(new JMenuItem(act));
        act = kit.getActionByName(NotebookEditorKit.clearEngineContextAction);
        menu.add(new JMenuItem(act));
        // act = kit.getActionByName(NotebookEditorKit.resetCellNumAction);
        // menu.add(new JMenuItem(act));
        menu.add(new GlobMenuItem(kit
                .getActionByName(NotebookEditorKit.javaDocManagerAction)));
        menu.add(new JMenuItem(kit
                .getActionByName(NotebookEditorKit.ctxInspectorAction)));
        menu.add(new EnhancedMenu("Cell", new CellPropsProvider()));
        menu.add(new EnhancedMenu("CellGroup", new CellGroupPropsProvider()));
        menu.add(new JSeparator());
        JMenuItem mi = new GlobMenuItem("Top CellGroup Tree");
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
            if(cgm == null) continue;
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
        if(CellUtils.isMinimized(cgm)){
            update_minimized_UI(node, cgm);
            return;
        }
        String title = CellUtils.getName(cgm);
        if (CellUtils.isShowTitle(cgm) && title != null)
        {
            PCSelectionHandler.decorateSelectedNode(node, false);
            //move down the node with the height of the added title pane
            node.translate(0, TitlePaneNode.HEIGHT);
        }
        else if (!CellUtils.isShowTitle(cgm))
        {
            PCSelectionHandler.undecorateSelectedNode(node, true);
        }
    }
    
    //size of the minimized components 
    static Dimension MINIMIZED_COMPONENT_SIZE = new Dimension(64, 64);

    static JComponent getMinimizedUI(final CellGroupMember cgm)
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
}
