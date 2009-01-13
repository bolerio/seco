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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
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
import org.hypergraphdb.atom.HGAtomRef.Mode;
import org.wonderly.swing.tabs.TabCloseEvent;
import org.wonderly.swing.tabs.TabCloseListener;

import seco.ThisNiche;
import seco.gui.CellContainerVisual;
import seco.gui.JComponentVisual;
import seco.gui.TabbedPaneVisual;
import seco.gui.VisualAttribs;
import seco.notebook.gui.CloseableDnDTabbedPane;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.OpenBookPanel;
import seco.notebook.gui.ScriptEngineProvider;
import seco.notebook.gui.ToolbarButton;
import seco.notebook.gui.NotifyDescriptor.InputLine;
import seco.notebook.gui.menu.CellGroupPropsProvider;
import seco.notebook.gui.menu.CellPropsProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.RCListProvider;
import seco.notebook.gui.menu.RecentFilesProvider;
import seco.notebook.gui.menu.VisPropsProvider;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.util.FileUtil;
import seco.notebook.util.IconManager;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.IOUtils;

public class GUIHelper
{
    // Create the edit menu.
    private static JMenu createEditMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new GUIHelper.NBMenu("Edit");
        menu.setMnemonic('e');
        menu.add(new JMenuItem(man.putAction(NotebookEditorKit.undo, KeyStroke
                .getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK),
                IconManager.resolveIcon("Undo16.gif"))));
        menu.add(new JMenuItem(man.putAction(NotebookEditorKit.redo, KeyStroke
                .getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK),
                IconManager.resolveIcon("Redo16.gif"))));
        menu.addSeparator();
        menu.addMenuListener(new GUIHelper.EditMenuListener());
        Action act = GUIHelper.kit.getActionByName(DefaultEditorKit.cutAction);
        act.putValue(Action.NAME, CUT);
        act.putValue(Action.SHORT_DESCRIPTION, "Cut");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Cut16.gif"))));
        act = GUIHelper.kit.getActionByName(DefaultEditorKit.copyAction);
        act.putValue(Action.SHORT_DESCRIPTION, "Copy");
        act.putValue(Action.NAME, COPY);
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Copy16.gif"))));
        act = GUIHelper.kit.getActionByName(DefaultEditorKit.pasteAction);
        act.putValue(Action.NAME, PASTE);
        act.putValue(Action.SHORT_DESCRIPTION, "Paste");
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK), IconManager
                .resolveIcon("Paste16.gif"))));
        menu.addSeparator();
        act = GUIHelper.kit.getActionByName(DefaultEditorKit.selectAllAction);
        act.putValue(Action.NAME, SELECT_ALL);
        menu.add(new JMenuItem(man.putAction(act, KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK))));
        menu.addSeparator();
        menu.add(new JMenuItem(man.putAction(GUIHelper.kit
                .getActionByName(NotebookEditorKit.findAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(GUIHelper.kit
                .getActionByName(NotebookEditorKit.replaceAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK))));
        return menu;
    }

    private static JMenu createFileMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new GUIHelper.NBMenu("File");
        menu.setMnemonic('f');
        menu.add(new JMenuItem(man.putAction(new GUIHelper.NewAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new GUIHelper.OpenAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new GUIHelper.ImportAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new GUIHelper.ExportAction())));
        menu.add(new JSeparator());
        menu.add(new EnhancedMenu("Recent Files", new RecentFilesProvider()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(man.putAction(new GUIHelper.ExitAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK))));
        // TODO: Just for testing purposes, would be removed
        JMenuItem mi = new JMenuItem("View Element Tree");
        mi.addActionListener(new GUIHelper.ElementTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Cells Tree");
        mi.addActionListener(new GUIHelper.CellTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Parse Tree");
        mi.addActionListener(new GUIHelper.ParseTreeAction());
        menu.add(mi);
        return menu;
    }

    private static JMenu createFormatMenu()
    {
        JMenu menu = new GUIHelper.NBMenu("Format");
        menu.setMnemonic('o');
        final JCheckBoxMenuItem m = new JCheckBoxMenuItem("Cell Numbers");
        m.addItemListener(new GUIHelper.CellNumItemListener());
        menu.add(m);
        menu.addSeparator();
        menu.add(new EnhancedMenu("Visual Properties", new VisPropsProvider()));
        Action act = GUIHelper.kit.getActionByName(NotebookEditorKit.formatAction);
        act.putValue(Action.NAME, "Format");
        JMenuItem mi = new JMenuItem(act);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
        menu.add(mi);
        return menu;
    }

    private static JMenu createToolsMenu()
    {
        JMenu menu = new GUIHelper.NBMenu("Tools");
        menu.setMnemonic('t');
        Action act = GUIHelper.kit.getActionByName(NotebookEditorKit.evalAction);
        menu.add(new JMenuItem(act));
        act = GUIHelper.kit.getActionByName(NotebookEditorKit.evalCellGroupAction);
        menu.add(new JMenuItem(act));
        act = GUIHelper.kit.getActionByName(NotebookEditorKit.reEvalOutputCellsAction);
        menu.add(new JMenuItem(act));
        act = GUIHelper.kit.getActionByName(NotebookEditorKit.removeOutputCellsAction);
        menu.add(new JMenuItem(act));
    
        act = ActionManager.getInstance().putAction(
                GUIHelper.kit.getActionByName(NotebookEditorKit.mergeCellsAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
    
        menu.add(new JMenuItem(act));
        act = GUIHelper.kit.getActionByName(NotebookEditorKit.clearEngineContextAction);
        menu.add(new JMenuItem(act));
        act = GUIHelper.kit.getActionByName(NotebookEditorKit.resetCellNumAction);
        menu.add(new JMenuItem(act));
        menu.add(new GUIHelper.GlobMenuItem(GUIHelper.kit
                .getActionByName(NotebookEditorKit.javaDocManagerAction)));
        menu.add(new JMenuItem(GUIHelper.kit
                .getActionByName(NotebookEditorKit.ctxInspectorAction)));
        menu.add(new EnhancedMenu("Cell", new CellPropsProvider()));
        menu.add(new EnhancedMenu("CellGroup", new CellGroupPropsProvider()));
        return menu;
    }

    public static final HGPersistentHandle TOOLBAR_HANDLE = HGHandleFactory
    .makeHandle("d40c99be-f108-11dc-a860-d9a9d2c59ef1");
    public static final HGPersistentHandle MENUBAR_HANDLE = HGHandleFactory
    .makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    public static final HGPersistentHandle HTML_TOOLBAR_HANDLE = HGHandleFactory
    .makeHandle("56371f73-025d-11dd-b650-ef87b987c94a");
    
    public static final HGPersistentHandle TABBED_PANE_GROUP_HANDLE = HGHandleFactory
        .makeHandle("7b01b680-e186-11dd-ad8b-0800200c9a66");
    
    public static JTabbedPane tabbedPane;
    
    public static JTabbedPane getJTabbedPane()
    {
        if(tabbedPane != null) return tabbedPane;
        if(ThisNiche.hg.get(TABBED_PANE_GROUP_HANDLE) == null)
        {
            CellGroup group = new CellGroup("TabbedPaneCellGroup");
            ThisNiche.hg.define(TABBED_PANE_GROUP_HANDLE, group);
            group.setVisual(ThisNiche.hg.add(new TabbedPaneVisual()));
            group.setAttribute(VisualAttribs.rect, new Rectangle(0, 90, 600, 600));
            ThisNiche.hg.update(group);
        }
        if (DRAGGABLE_TABS)
        {
            tabbedPane = new CloseableDnDTabbedPane();
            ((CloseableDnDTabbedPane) tabbedPane).setPaintGhost(true);
            ((CloseableDnDTabbedPane) tabbedPane)
                    .addTabCloseListener(new GUIHelper.TabbedPaneCloseListener());
        } else
            tabbedPane = new JTabbedPane();
        tabbedPane.setDoubleBuffered(!AppForm.PICCOLO);
        tabbedPane.putClientProperty(
                com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addMouseListener(new GUIHelper.TabbedPaneMouseListener());
        return tabbedPane;
    }
    
    public static JToolBar getMainToolBar()
    {        
        JToolBar toolBar = (JToolBar) ThisNiche.hg.get(TOOLBAR_HANDLE);
        
        if (toolBar != null)   return toolBar;
    
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
        toolBar.add(new ToolbarButton(GUIHelper.kit
                .getActionByName(NotebookEditorKit.htmlAction),
                "HTML Preview ON/OFF"));
        ThisNiche.hg.define(TOOLBAR_HANDLE, toolBar);
        return toolBar;
    }

    public static HTMLToolBar getHTMLToolBar()
    {
        HTMLToolBar htmlToolBar = (HTMLToolBar) ThisNiche.hg.get(HTML_TOOLBAR_HANDLE);
        if (htmlToolBar != null) return htmlToolBar;
        htmlToolBar = new HTMLToolBar();
        htmlToolBar.init();
        htmlToolBar.setEnabled(false);
        ThisNiche.hg.define(HTML_TOOLBAR_HANDLE, htmlToolBar);
        return htmlToolBar;
    }

    public static final String TAB_INDEX = "tab_index";
    public static final String LOGO_IMAGE_RESOURCE = "/seco/resources/logoicon.gif";


    // disable menuItems if no notebook presented
    // use GlobMenuItem to prevent disabling
    public static class NBMenu extends JMenu implements MenuListener
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
            boolean b = AppForm.getInstance().getCurrentNotebook() != null;
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
            AppForm.getInstance().newNotebook();
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
            AppForm.getInstance().openNotebook();
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
            AppForm app = AppForm.getInstance();
            File f = FileUtil.getFile(app, "Export Notebook As ...",
                    FileUtil.SAVE, null);
            if (f != null)
            {
                IOUtils.exportCellGroup((CellGroup) app.getCurrentNotebook()
                        .getDoc().getBook(), f.getAbsolutePath());
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
            AppForm.getInstance().exit();
        }
    }

    public static class CellTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            GUIHelper.openCellTree(evt);
        }
    }

    public static class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            putValue(Action.NAME, OPEN);
            putValue(Action.SMALL_ICON, IconManager
                    .resolveIcon("Open16.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }
    
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            JDialog dialog = new JDialog(AppForm.getInstance(),
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
            NotebookUI ui = AppForm.getInstance().getCurrentNotebook();
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
            GUIHelper.openElementTree(evt);
        }
    }

    public static class ParseTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            GUIHelper.openParseTree(evt);
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
                    || res == JOptionPane.CLOSED_OPTION) return;
            AppForm.getInstance().closeAt(evt.getClosedTab());
        }
    
        public boolean closeTab(int tabIndexToClose)
        {
            int res = promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION) return false;
            AppForm.getInstance().closeAt(tabIndexToClose);
            return true;
        }
    }

    public static class CellNumItemListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == null
                    || !(e.getSource() instanceof JCheckBoxMenuItem)) return;
            NotebookUI ui = AppForm.getInstance().getCurrentNotebook();
            if (ui != null) ui.setDrawCellNums(((JCheckBoxMenuItem) e
                    .getSource()).isSelected());
        }
    }

   

    public static JMenuBar getMenuBar()
    {
        JMenuBar menuBar = (JMenuBar) ThisNiche.hg.get(GUIHelper.MENUBAR_HANDLE);
        if (menuBar == null)
        {
            menuBar = new JMenuBar();
            menuBar.add(createFileMenu());
            menuBar.add(createEditMenu());
            menuBar.add(createFormatMenu());
            menuBar.add(createToolsMenu());
            menuBar.add(createRuntimeMenu());
            ThisNiche.hg.define(GUIHelper.MENUBAR_HANDLE, menuBar);
            //force the creation of the NotebookUI static popup
            NotebookUI.getPopupMenu();
        }
        return menuBar;
    }

    private static JMenu createRuntimeMenu()
    {
        JMenu menu = new JMenu("Runtime");
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

    public static JPopupMenu getTabPopupMenu()
    {
        if (GUIHelper.tabPopupMenu != null) return GUIHelper.tabPopupMenu;
        GUIHelper.tabPopupMenu = new JPopupMenu();
        Action act = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("AppForm - Close:");
                int res = promptAndSaveDoc();
                if (res == JOptionPane.CANCEL_OPTION
                        || res == JOptionPane.CLOSED_OPTION) return;
                int i = ((Integer) GUIHelper.tabPopupMenu.getClientProperty(TAB_INDEX));
                AppForm.getInstance().closeAt(i);
            }
        };
        // TODO: the shortcut doesn't work this way
        // KeyStroke key = KeyStroke
        // .getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK);
        // act = ActionManager.getInstance().putAction(act, key);
        // tabbedPane.getInputMap().put(key, act);
        GUIHelper.tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All") {
            public void actionPerformed(ActionEvent e)
            {
                JTabbedPane tp = getJTabbedPane();
                tp.setSelectedIndex(tp.getTabCount() - 1);
                for (int i = tp.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION) continue;
                    AppForm.getInstance().closeAt(i);
                }
            }
        };
        GUIHelper.tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All But Active") {
            public void actionPerformed(ActionEvent e)
            {
                JTabbedPane tp = GUIHelper.getJTabbedPane();
                tp.setSelectedIndex(tp.getTabCount() - 1);
                int index = ((Integer) GUIHelper.tabPopupMenu
                        .getClientProperty(TAB_INDEX));
                for (int i = tp.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION || i == index) continue;
                    AppForm.getInstance().closeAt(i);
                }
            }
        };
        GUIHelper.tabPopupMenu.add(new JMenuItem(act));
        GUIHelper.tabPopupMenu.add(new EnhancedMenu("Set Default Language",
                new ScriptEngineProvider(AppForm.getInstance())));
        GUIHelper.tabPopupMenu.add(new EnhancedMenu("Set Runtime Context",
                new RCListProvider()));
        act = new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e)
            {
                String name = AppForm.getInstance().getCurrentNotebook().getDoc().getTitle();
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                        AppForm.getInstance(), "Name: ", "Rename CellGroup");
                nd.setInputText(name);
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
                {
                    // HGHandle h = currentBook.getDoc().bookH;
                    AppForm.getInstance().getCurrentNotebook().getDoc().setTitle(nd.getInputText());
                    // ThisNiche.hg.replace(h, currentBook.getDoc().getBook());
                    AppForm.getInstance().updateTitle();
                }
            }
        };
        GUIHelper.tabPopupMenu.add(new JMenuItem(act));
        return GUIHelper.tabPopupMenu;
    }

    static JPopupMenu tabPopupMenu;
    
    public static final class TabbedPaneMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            final AppForm app = AppForm.getInstance();
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
                        GUIHelper.getTabPopupMenu().putClientProperty(GUIHelper.TAB_INDEX, i);
                        if (AppForm.PICCOLO)
                        {
                            Frame f = GUIUtilities.getFrame(e.getComponent());
                            pt = SwingUtilities.convertPoint(e.getComponent(),
                                    e.getX(), e.getY(), f);
                        }
                        GUIHelper.getTabPopupMenu().show(tabbedPane, pt.x, pt.y);
                        break;
                    }
                }
                e.consume();
            }
            app.repaint();
        }
    }

    public static final class TabbedPaneChangeListener implements
            ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            AppForm app = AppForm.getInstance();
            if (getJTabbedPane().getSelectedIndex() == -1) return;
            JScrollPane comp = (JScrollPane) getJTabbedPane()
                    .getComponentAt(getJTabbedPane().getSelectedIndex()); 
            app.setCurrentNotebook((NotebookUI) comp.getViewport().getView());
            app.updateTitle(true);
            // System.out.println("TabbedPaneChangeListener: " + e);
            if (AppForm.PICCOLO) PiccoloFrame.getInstance().setTitle(
                    app.getTitle());
        }
    }

    public static final NotebookEditorKit kit = new NotebookEditorKit();
    public static void openElementTree(ActionEvent evt)
    {
        if (AppForm.getInstance().getCurrentNotebook() == null) return;
        // Notebook book = getCurrentNotebook().getDoc().getBook();
        JDialog dialog = new JDialog(AppForm.getInstance()); // , book.getFilename());
        dialog.setSize(500, 800);
        JTree tree = new JTree((TreeNode) AppForm.getInstance().getCurrentNotebook().getDocument()
                .getDefaultRootElement());
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    public static void openParseTree(ActionEvent evt)
    {
        NotebookUI ui = AppForm.getInstance().getCurrentNotebook();
        if (ui == null) return;
        JDialog dialog = new JDialog(AppForm.getInstance());
        dialog.setSize(500, 800);
        JTree tree = ui.getDoc().getParseTree(ui.getCaretPosition());
        if (tree == null) return;
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    public static void openCellTree(ActionEvent evt)
    {
        NotebookUI ui = AppForm.getInstance().getCurrentNotebook();
        if (ui == null) return;
        CellGroup book = (CellGroup) ui.getDoc().getBook();
        JDialog dialog = new JDialog(AppForm.getInstance(), book.getName());
        dialog.setSize(500, 800);
        JTree tree = new NotebookCellsTree(new NotebookTreeModel(book));
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    static int promptAndSaveDoc()
    {
        if (AppForm.getInstance().getCurrentNotebook() == null) return -1;
        // NotebookDocument doc = getCurrentNotebook().getDoc();
        AppForm.getInstance().getCurrentNotebook().close();
        // doc.save();
        return JOptionPane.OK_OPTION;
    }

    public static void makeTopCellGroup(HyperGraph hg)
    {
        CellGroup group = new CellGroup("TOP_CELL_GROUP");
        hg.define(ThisNiche.TOP_CELL_GROUP_HANDLE, group);
        group.setVisual(hg.add(new CellContainerVisual()));
        HGHandle visualH = hg.getPersistentHandle(hg.add(new JComponentVisual()));
        getMenuBar();
        addToTopCellGroup(hg, MENUBAR_HANDLE, group, visualH, new Rectangle(0, 0, 200, 27));
        getMainToolBar();
        addToTopCellGroup(hg, TOOLBAR_HANDLE, group, visualH, new Rectangle(0, 30, 260, 28));
        getHTMLToolBar();
        addToTopCellGroup(hg, HTML_TOOLBAR_HANDLE, group, visualH, new Rectangle(0, 60, 600, 28));
        getJTabbedPane();
        group.insert(group.getArity(), TABBED_PANE_GROUP_HANDLE);
        
        hg.update(group);
    }
    
    public static HGHandle addToTopCellGroup(HyperGraph hg, HGHandle h, CellGroup group, HGHandle visualH, Rectangle r)
    {
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        Cell out = new Cell(ref);
        HGHandle cellH = hg.add(out);
        out.setAttribute(VisualAttribs.rect, r);
        out.setVisual(visualH);
        group.insert(group.getArity(), out);
        return cellH;
    }
    
    public static void removeFromTopCellGroup()
    {
        //TODO:
    }

    static final boolean DRAGGABLE_TABS = !AppForm.PICCOLO;
   
}
