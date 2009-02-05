package seco.notebook;

import static seco.notebook.Actions.COPY;
import static seco.notebook.Actions.CUT;
import static seco.notebook.Actions.EXIT;
import static seco.notebook.Actions.EXPORT;
import static seco.notebook.Actions.NEW;
import static seco.notebook.Actions.OPEN;
import static seco.notebook.Actions.PASTE;
import static seco.notebook.Actions.SELECT_ALL;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeNode;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGAtomRef;
import org.wonderly.swing.tabs.TabCloseEvent;
import org.wonderly.swing.tabs.TabCloseListener;

import seco.ThisNiche;
import seco.gui.CellContainerVisual;
import seco.gui.TabbedPaneVisual;
import seco.gui.TopFrame;
import seco.gui.VisualAttribs;
import seco.gui.VisualsManager;
import seco.notebook.gui.CloseableDnDTabbedPane;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.OpenBookPanel;
import seco.notebook.gui.ScriptEngineProvider;
import seco.notebook.gui.ToolbarButton;
import seco.notebook.gui.menu.CellGroupPropsProvider;
import seco.notebook.gui.menu.CellPropsProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.RCListProvider;
import seco.notebook.gui.menu.RecentFilesProvider;
import seco.notebook.gui.menu.VisPropsProvider;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.piccolo.pswing.PSwing;
import seco.notebook.util.FileUtil;
import seco.notebook.util.IconManager;
import seco.rtenv.ContextLink;
import seco.rtenv.EvaluationContext;
import seco.rtenv.RuntimeContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.IOUtils;

public class GUIHelper
{
    public static final HGPersistentHandle TOOLBAR_HANDLE = HGHandleFactory
            .makeHandle("d40c99be-f108-11dc-a860-d9a9d2c59ef1");
    public static final HGPersistentHandle MENUBAR_HANDLE = HGHandleFactory
            .makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    public static final HGPersistentHandle HTML_TOOLBAR_HANDLE = HGHandleFactory
            .makeHandle("56371f73-025d-11dd-b650-ef87b987c94a");

    public static final HGPersistentHandle TABBED_PANE_GROUP_HANDLE = HGHandleFactory
            .makeHandle("7b01b680-e186-11dd-ad8b-0800200c9a66");

    private static final String TAB_INDEX = "tab_index";
    public static final String LOGO_IMAGE_RESOURCE = "/seco/resources/logoicon.gif";

    private static JTabbedPane tabbedPane;

    public static JTabbedPane getJTabbedPane()
    {
        if (tabbedPane != null)
            return tabbedPane;
        if (ThisNiche.hg.get(TABBED_PANE_GROUP_HANDLE) == null)
        {
            CellGroup group = new CellGroup("TabbedPaneCellGroup");
            ThisNiche.hg.define(TABBED_PANE_GROUP_HANDLE, group);
            group.setVisual(ThisNiche.hg.add(new TabbedPaneVisual()));
            group.setAttribute(VisualAttribs.rect, new Rectangle(0, 90, 600,
                    600));
            ThisNiche.hg.update(group);
        }
        if (DRAGGABLE_TABS)
        {
            tabbedPane = new CloseableDnDTabbedPane();
            ((CloseableDnDTabbedPane) tabbedPane).setPaintGhost(true);
            ((CloseableDnDTabbedPane) tabbedPane)
                    .addTabCloseListener(new  TabbedPaneCloseListener());
        } else
            tabbedPane = new JTabbedPane();
        tabbedPane.setDoubleBuffered(!TopFrame.PICCOLO);
        tabbedPane.putClientProperty(
                com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addMouseListener(new  TabbedPaneMouseListener());
         getJTabbedPane().addChangeListener(new TabbedPaneChangeListener());
        return tabbedPane;
    }

    public static JToolBar getMainToolBar()
    {
        JToolBar toolBar = (JToolBar) ThisNiche.hg.get(TOOLBAR_HANDLE);

        if (toolBar != null)
            return toolBar;

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
        toolBar.add(new ToolbarButton( kit
                .getActionByName(NotebookEditorKit.htmlAction),
                "HTML Preview ON/OFF"));
        toolBar.setFloatable(false);
        ThisNiche.hg.define(TOOLBAR_HANDLE, toolBar);
        return toolBar;
    }

    public static HTMLToolBar getHTMLToolBar()
    {
        HTMLToolBar htmlToolBar = (HTMLToolBar) ThisNiche.hg
                .get(HTML_TOOLBAR_HANDLE);
        if (htmlToolBar != null)
            return htmlToolBar;
        htmlToolBar = new HTMLToolBar();
        htmlToolBar.init();
        htmlToolBar.setEnabled(false);
        htmlToolBar.setFloatable(false);
        ThisNiche.hg.define(HTML_TOOLBAR_HANDLE, htmlToolBar);
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
                    if (a != null)
                        b = a.isEnabled();
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
            {
                PSwing p = (PSwing) ((JComponent) getParent())
                        .getClientProperty(PSwing.PSWING_PROPERTY);
                if (p != null)
                {
                    Rectangle r = p.getFullBounds().getBounds();
                    return new Point(pt.x + r.x, pt.y + r.y);
                }
            }
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
            File f = FileUtil.getFile(GUIUtilities.getFrame(ui),
                    "Export Notebook As ...", FileUtil.SAVE, null);
            if (f != null)
            {
                IOUtils.exportCellGroup((CellGroup) ui.getDoc().getBook(), f
                        .getAbsolutePath());
            }
        }
    }

    /* public */public static class ExitAction extends AbstractAction
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
             openCellTree(evt);
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
             openElementTree(evt);
        }
    }

    public static class ParseTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
             openParseTree(evt);
        }
    }

    public static final class TabbedPaneCloseListener implements // CloseableTabbedPane.
            TabCloseListener
    {
        public void tabClosed(TabCloseEvent evt)
        {
            // System.out.println("tabClosed: " + ":" + evt);
            // TODO: this get called twice on a Cancel option?
            int res = promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION)
                return;
             closeAt(evt.getClosedTab());
        }

        public boolean closeTab(int tabIndexToClose)
        {
            int res = promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION)
                return false;
             closeAt(tabIndexToClose);
            return true;
        }
    }

    public static class CellNumItemListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == null
                    || !(e.getSource() instanceof JCheckBoxMenuItem))
                return;
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui != null)
                ui.setDrawCellNums(((JCheckBoxMenuItem) e.getSource())
                        .isSelected());
        }
    }

    public static JMenuBar getMenuBar()
    {
        JMenuBar menuBar = (JMenuBar) ThisNiche.hg
                .get( MENUBAR_HANDLE);
        if (menuBar == null)
        {
            menuBar = new JMenuBar();
            menuBar.add(createFileMenu());
            menuBar.add(createEditMenu());
            menuBar.add(createFormatMenu());
            menuBar.add(createToolsMenu());
            menuBar.add(createRuntimeMenu());
            ThisNiche.hg.define( MENUBAR_HANDLE, menuBar);
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

    private static JPopupMenu getTabPopupMenu()
    {
        if ( tabPopupMenu != null)
            return  tabPopupMenu;
         tabPopupMenu = new JPopupMenu();
        Action act = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e)
            {
                //System.out.println("AppForm - Close:");
                int res = promptAndSaveDoc();
                if (res == JOptionPane.CANCEL_OPTION
                        || res == JOptionPane.CLOSED_OPTION)
                    return;
                int i = ((Integer)  tabPopupMenu
                        .getClientProperty(TAB_INDEX));
                 closeAt(i);
            }
        };
        // TODO: the shortcut doesn't work this way
        // KeyStroke key = KeyStroke
        // .getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK);
        // act = ActionManager.getInstance().putAction(act, key);
        // tabbedPane.getInputMap().put(key, act);
         tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All") {
            public void actionPerformed(ActionEvent e)
            {
                JTabbedPane tp = getJTabbedPane();
                tp.setSelectedIndex(tp.getTabCount() - 1);
                for (int i = tp.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION)
                        continue;
                     closeAt(i);
                }
            }
        };
         tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All But Active") {
            public void actionPerformed(ActionEvent e)
            {
                JTabbedPane tp =  getJTabbedPane();
                tp.setSelectedIndex(tp.getTabCount() - 1);
                int index = ((Integer)  tabPopupMenu
                        .getClientProperty(TAB_INDEX));
                for (int i = tp.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION || i == index)
                        continue;
                     closeAt(i);
                }
            }
        };
         tabPopupMenu.add(new JMenuItem(act));
         tabPopupMenu.add(new EnhancedMenu("Set Default Language",
                new ScriptEngineProvider()));
         tabPopupMenu.add(new EnhancedMenu("Set Runtime Context",
                new RCListProvider()));
        act = new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e)
            {
                NotebookUI ui = NotebookUI.getFocusedNotebookUI();
                if (ui == null)
                    return;
                String name = ui.getDoc().getTitle();
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                        GUIUtilities.getFrame(ui), "Name: ", "Rename CellGroup");
                nd.setInputText(name);
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
                {
                    // HGHandle h = currentBook.getDoc().bookH;
                    ui.getDoc().setTitle(nd.getInputText());
                    // ThisNiche.hg.replace(h, currentBook.getDoc().getBook());
                     updateTitle();
                }
            }
        };
         tabPopupMenu.add(new JMenuItem(act));
        return  tabPopupMenu;
    }

    static JPopupMenu tabPopupMenu;

    public static final class TabbedPaneMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                Point pt = e.getPoint();
                JTabbedPane tabbedPane = getJTabbedPane();
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    final Rectangle r = tabbedPane.getBoundsAt(i);
                    // System.out.println("AppForm: " + pt + ":" + r
                    // + ":" + r.contains(pt));
                    if (r != null && r.contains(pt))
                    {
                         getTabPopupMenu().putClientProperty(
                                 TAB_INDEX, i);
                        if (TopFrame.PICCOLO)
                        {
                            Frame f = GUIUtilities.getFrame(e.getComponent());
                            pt = SwingUtilities.convertPoint(e.getComponent(),
                                    e.getX(), e.getY(), f);
                        }
                         getTabPopupMenu()
                                .show(tabbedPane, pt.x, pt.y);
                        break;
                    }
                }
                e.consume();
            }
            TopFrame.getInstance().repaint();
        }
    }

    public static final class TabbedPaneChangeListener implements
            ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            if (getJTabbedPane().getSelectedIndex() == -1)
                return;
            JScrollPane comp = (JScrollPane) getJTabbedPane().getComponentAt(
                    getJTabbedPane().getSelectedIndex());
            NotebookUI.setFocusedNotebookUI(
                    (NotebookUI)comp.getViewport().getView());
            updateTitle(true);
            // System.out.println("TabbedPaneChangeListener: " + e);
        }
    }

    public static final NotebookEditorKit kit = new NotebookEditorKit();

    public static void openElementTree(ActionEvent evt)
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null)
            return;
        JDialog dialog = new JDialog(GUIUtilities.getFrame(ui)); // ,
                                                                    // book.getFilename());
        dialog.setSize(500, 800);
        JTree tree = new JTree((TreeNode) ui.getDocument()
                .getDefaultRootElement());
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    public static void openParseTree(ActionEvent evt)
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null)
            return;
        JDialog dialog = new JDialog(GUIUtilities.getFrame(ui));
        dialog.setSize(500, 800);
        JTree tree = ui.getParseTree(ui.getCaretPosition());
        if (tree == null)
            return;
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    public static void openCellTree(ActionEvent evt)
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null)
            return;
        CellGroupMember book = ui.getDoc().getBook();
        String title = (book instanceof CellGroup) ? ((CellGroup) book).getName() : "Cell";
        JDialog dialog = new JDialog(TopFrame.getInstance(), title);
        dialog.setSize(500, 800);
        JTree tree = new NotebookCellsTree(new NotebookTreeModel(book));
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    static int promptAndSaveDoc()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null)
            return -1;
        // NotebookDocument doc = getCurrentNotebook().getDoc();
        ui.close();
        // doc.save();
        return JOptionPane.OK_OPTION;
    }

    public static void makeTopCellGroup(HyperGraph hg)
    {
        CellGroup group = new CellGroup("TOP_CELL_GROUP");
        hg.define(ThisNiche.TOP_CELL_GROUP_HANDLE, group);
        group.setVisual(CellContainerVisual.getHandle());
        getMenuBar();
        addToTopCellGroup(hg, MENUBAR_HANDLE, group, VisualsManager
                .defaultVisualForAtom(MENUBAR_HANDLE), new Rectangle(0, 0, 200,
                27));
        getMainToolBar();
        addToTopCellGroup(hg, TOOLBAR_HANDLE, group, VisualsManager
                .defaultVisualForAtom(TOOLBAR_HANDLE), new Rectangle(0, 30,
                260, 28));
        getHTMLToolBar();
        addToTopCellGroup(hg, HTML_TOOLBAR_HANDLE, group, VisualsManager
                .defaultVisualForAtom(HTML_TOOLBAR_HANDLE), new Rectangle(0,
                60, 600, 28));
        getJTabbedPane();
        group.insert(group.getArity(), TABBED_PANE_GROUP_HANDLE);
        // hg.update(group);
    }

    public static HGHandle addToTopCellGroup(HyperGraph hg, HGHandle h,
            CellGroup group, HGHandle visualH, Rectangle r)
    {
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        HGHandle cellH = hg.add(out);
        out.setAttribute(VisualAttribs.rect, r);
        out.setVisual(visualH);
        group.insert(group.getArity(), out);
        return cellH;
    }
    
    public static HGHandle addToTopCellGroup(HyperGraph hg, HGHandle h, HGHandle visualH, Rectangle r)
    {
        CellGroup top = hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        return addToTopCellGroup(hg, h,top, visualH, r);
    }

    public static void removeFromTopCellGroup(HGHandle h)
    {
        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        top.remove((CellGroupMember) ThisNiche.hg.get(h));
    }
    
    private static void removeFromTabbedPaneCellGroup(HGHandle h)
    {
        CellGroup top = ThisNiche.hg.get(TABBED_PANE_GROUP_HANDLE);
        top.remove((CellGroupMember) ThisNiche.hg.get(h));
    }

    static final boolean DRAGGABLE_TABS = !TopFrame.PICCOLO;

    public static void openNotebook(HGHandle h)
    {
        if ( allready_opened(h, true)) return;
        NotebookUI ui = new NotebookUI(h);
         addNotebookTab(ui);
        //AppConfig.getInstance().getOpenedGroups().add(ui.getDoc().getHandle());
        addTabToTabbedPaneGroup(ui.getDoc().getHandle());
    }

    static boolean allready_opened(HGHandle h, boolean focus)
    {
        JTabbedPane tabbedPane = getJTabbedPane();
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            JScrollPane comp = (JScrollPane) tabbedPane.getComponentAt(i);
            NotebookUI ui = (NotebookUI) comp.getViewport().getView();
            if (ui.getDoc().getHandle().equals(h))
            {
                if (focus)
                {
                    tabbedPane.setSelectedIndex(i);
                    //currentBook = ui;
                }
                return true;
            }
        }
        return false;
    }

    static void closeAt(int i)
    {
        JTabbedPane tabbedPane = getJTabbedPane();
        HGHandle h = ThisNiche.handleOf(getNotebookAt(i));
        ThisNiche.hg.unfreeze(h);
        //removeFromTabbedPaneCellGroup(h);
        CellGroup top = ThisNiche.hg.get(TABBED_PANE_GROUP_HANDLE);
        top.remove(i);
        tabbedPane.removeTabAt(i);
        if (tabbedPane.getTabCount() == 0)
            TopFrame.getInstance().setTitle("Seco");
        else
             updateTitle(true);
        
        //TODO:
        //if (TopFrame.PICCOLO)
        //    PiccoloFrame.getInstance().repaintTabbedPane();
    }

    static CellGroup getNotebookAt(int i)
    {
        JTabbedPane tabbedPane = getJTabbedPane();
        JScrollPane comp = (JScrollPane) tabbedPane.getComponentAt(i);
        NotebookUI ui = (NotebookUI) comp.getViewport().getView();
        return (CellGroup) ui.getDoc().getBook();
    }

    public static void newNotebook()
    {
        NotebookUI ui = null;
        EvaluationContext ctx = 
            ThisNiche.getEvaluationContext(TopFrame.getCurrentEvaluationContext());
        CellGroup nb = new CellGroup("CG");
        HGHandle nbHandle = ThisNiche.hg.add(nb); 
        ThisNiche.hg.freeze(nbHandle);
        ui = new NotebookUI(nbHandle, ctx);
        ThisNiche.hg.add(new ContextLink(nbHandle, TopFrame.getCurrentEvaluationContext()));
        ui.setCaretPosition(0);
        ui.getDoc().setModified(true);
         addNotebookTab(ui);
        addTabToTabbedPaneGroup(ui.getDoc().getHandle());
    }

    public static void openNotebook()
    {
        File file = FileUtil
                .getFile(TopFrame.getInstance(), "Load Notebook", FileUtil.LOAD, null);
        if (file == null)
            return;
         importGroup(file);
    }

    static void importGroup(File file)
    {
        try
        {
            String fn = file.getAbsolutePath();
            HGHandle knownHandle = IOUtils.importCellGroup(fn);
            ThisNiche.hg.add(new ContextLink(knownHandle, TopFrame.getCurrentEvaluationContext()));
            NotebookUI ui = new NotebookUI(knownHandle);
             addNotebookTab(ui);
            AppConfig.getInstance().getMRUF().add(ui.getDoc().getHandle());
            //AppConfig.getInstance().getOpenedGroups().add(ui.getDoc().getHandle());
            addTabToTabbedPaneGroup(ui.getDoc().getHandle());
            AppConfig.getInstance().setMRUD(file.getParent());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(
                    TopFrame.getInstance(), t, "Could not open: " + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // TODO: strange requirement to open new Notebook, if file doesnt
            // exist
            newNotebook();
        }
    }
    
    private static void addTabToTabbedPaneGroup(HGHandle h, boolean update)
    {
        CellGroup group = (CellGroup)ThisNiche.hg.get( TABBED_PANE_GROUP_HANDLE);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        HGHandle outH = ThisNiche.handleOf(out);
        if(outH == null)
            outH = ThisNiche.hg.add(out);
        group.insert(group.getArity(), outH);
        if(update)
           ThisNiche.hg.update(group);
    }
    
    static void addTabToTabbedPaneGroup(HGHandle h)
    {
        addTabToTabbedPaneGroup(h, true);
    }

    static void updateTitle()
    {
         updateTitle(false);
    }

    public static void updateTitle(boolean forced)
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return;
        CellGroupMember book = ui.getDoc().getBook();
        String name = (book instanceof CellGroup) ? ((CellGroup) book)
                .getName() : "Cell";
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.hg
                .get(TopFrame.getCurrentEvaluationContext());
        String title = rcInstance.getName() + " " + name;
        JTabbedPane tabbedPane = getJTabbedPane();
        tabbedPane
                .setTitleAt(tabbedPane.getSelectedIndex(),  makeTabTitle(name));
        TopFrame.getInstance().setTitle(title);
    }

    static void focusOnNotebook(NotebookUI currentBook, boolean restore_caret)
    {
        updateTitle(true);
        HGHandle h = currentBook.getDoc().bookH;
        if (h == null)
            throw new NullPointerException("Null CellGroup Handle for Doc: "
                    + currentBook.getDoc());
        TopFrame.setCurrentEvaluationContext(ThisNiche.getContextHandleFor(h));
        if (restore_caret)
            currentBook.restoreCaret();
    }

    private static final String UNTITLED = "Untitled";
    static String makeTabTitle(String title)
    {
        if (title == null || title.length() == 0) 
            title = UNTITLED;
        else
        {
            int ind = title.lastIndexOf('/');
            ind = Math.max(ind, title.lastIndexOf('\\'));
            if (ind > 0)
                title = title.substring(ind + 1);
        }
        // System.out.println("makeTabTitle: " + title);
        return title;
    }

    //TODO:
    public static void addNotebookTab(final NotebookUI book)
    {
        final NotebookDocument doc = book.getDoc();
        if(TopFrame.getInstance().getCaretListener() != null)
        book.addCaretListener(TopFrame.getInstance().getCaretListener());
        if(TopFrame.getInstance().getDocListener() != null)
        doc.addModificationListener(TopFrame.getInstance().getDocListener());
        final JScrollPane scrollPane = new JScrollPane(book);
        scrollPane.setDoubleBuffered(!TopFrame.PICCOLO);
        scrollPane.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce)
            {
                book.requestFocusInWindow();
            }
        });
        scrollPane.setViewportView(book);
        JTabbedPane tabbedPane = getJTabbedPane();
        tabbedPane.addTab(makeTabTitle(doc.getTitle()), scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        focusOnNotebook(book, false);
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
        act =  kit.getActionByName(DefaultEditorKit.copyAction);
        act.putValue(Action.SHORT_DESCRIPTION, "Copy");
        act.putValue(Action.NAME, COPY);
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Copy16.gif"))));
        act =  kit.getActionByName(DefaultEditorKit.pasteAction);
        act.putValue(Action.NAME, PASTE);
        act.putValue(Action.SHORT_DESCRIPTION, "Paste");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Paste16.gif"))));
        menu.addSeparator();
        act =  kit.getActionByName(DefaultEditorKit.selectAllAction);
        act.putValue(Action.NAME, SELECT_ALL);
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK))));
        menu.addSeparator();
        menu.add(new JMenuItem(man.putAction( kit
                .getActionByName(NotebookEditorKit.findAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction( kit
                .getActionByName(NotebookEditorKit.replaceAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK))));
        return menu;
    }

    private static JMenu createFileMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new  NBMenu("File");
        menu.setMnemonic('f');
        menu.add(new JMenuItem(man.putAction(new  NewAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new  OpenAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new  ImportAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new  ExportAction())));
        menu.add(new JSeparator());
        menu.add(new EnhancedMenu("Recent Files", new RecentFilesProvider()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(man.putAction(new  ExitAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK))));
        // TODO: Just for testing purposes, would be removed
        JMenuItem mi = new JMenuItem("View Element Tree");
        mi.addActionListener(new  ElementTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Cells Tree");
        mi.addActionListener(new  CellTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Parse Tree");
        mi.addActionListener(new  ParseTreeAction());
        menu.add(mi);
        return menu;
    }

    private static JMenu createFormatMenu()
    {
        JMenu menu = new  NBMenu("Format");
        menu.setMnemonic('o');
        final JCheckBoxMenuItem m = new JCheckBoxMenuItem("Cell Numbers");
        m.addItemListener(new  CellNumItemListener());
        menu.add(m);
        menu.addSeparator();
        menu.add(new EnhancedMenu("Visual Properties", new VisPropsProvider()));
        Action act =  kit
                .getActionByName(NotebookEditorKit.formatAction);
        act.putValue(Action.NAME, "Format");
        JMenuItem mi = new JMenuItem(act);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
        menu.add(mi);
        return menu;
    }

    private static JMenu createToolsMenu()
    {
        JMenu menu = new  NBMenu("Tools");
        menu.setMnemonic('t');
        Action act =  kit
                .getActionByName(NotebookEditorKit.evalAction);
        menu.add(new JMenuItem(act));
        act =  kit
                .getActionByName(NotebookEditorKit.evalCellGroupAction);
        menu.add(new JMenuItem(act));
        act =  kit
                .getActionByName(NotebookEditorKit.reEvalOutputCellsAction);
        menu.add(new JMenuItem(act));
        act =  kit
                .getActionByName(NotebookEditorKit.removeOutputCellsAction);
        menu.add(new JMenuItem(act));

        act = ActionManager.getInstance().putAction(
                 kit
                        .getActionByName(NotebookEditorKit.mergeCellsAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

        menu.add(new JMenuItem(act));
        act =  kit
                .getActionByName(NotebookEditorKit.clearEngineContextAction);
        menu.add(new JMenuItem(act));
        act =  kit
                .getActionByName(NotebookEditorKit.resetCellNumAction);
        menu.add(new JMenuItem(act));
        menu.add(new  GlobMenuItem( kit
                .getActionByName(NotebookEditorKit.javaDocManagerAction)));
        menu.add(new JMenuItem( kit
                .getActionByName(NotebookEditorKit.ctxInspectorAction)));
        menu.add(new EnhancedMenu("Cell", new CellPropsProvider()));
        menu.add(new EnhancedMenu("CellGroup", new CellGroupPropsProvider()));
        return menu;
    }
}
