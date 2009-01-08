/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import static seco.notebook.Actions.COPY;
import static seco.notebook.Actions.CUT;
import static seco.notebook.Actions.EXIT;
import static seco.notebook.Actions.EXPORT;
import static seco.notebook.Actions.NEW;
import static seco.notebook.Actions.OPEN;
import static seco.notebook.Actions.PASTE;
import static seco.notebook.Actions.SELECT_ALL;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.tree.TreeNode;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.type.HGAtomType;
import org.wonderly.swing.tabs.TabCloseEvent;
import org.wonderly.swing.tabs.TabCloseListener;

import seco.ThisNiche;
import seco.boot.StartMeUp;
import seco.notebook.gui.AKDockLayout;
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
import seco.notebook.util.FileUtil;
import seco.notebook.util.IconManager;
import seco.notebook.util.Log;
import seco.rtenv.ContextLink;
import seco.rtenv.EvaluationContext;
import seco.rtenv.RuntimeContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellGroupType;
import seco.things.CellType;
import seco.things.IOUtils;


import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBluer;
// seco.notebook.gui.CloseableTabbedPane;

/**
 * 
 * @author bizi
 */
public class AppForm extends javax.swing.JFrame
{
    public static boolean PICCOLO = true;

    public static final String LOGO_IMAGE_RESOURCE = "/seco/resources/logoicon.gif";
    private static final boolean DRAGGABLE_TABS = !PICCOLO;
    private static final String TAB_INDEX = "tab_index";
    static final String UNTITLED = "Untitled";
    private static final NotebookEditorKit kit = new NotebookEditorKit();
    // dir for additional jars/plugins
    private static final String EXT_DIR = "lib";
    private static URLClassLoader classLoader;
    private static ScriptEngineManager scriptEngineManager;
    HyperGraph graph;
    NotebookUI currentBook;
    StatusBar status;
    NotebookDocument.ModificationListener docListener;
    private ChangeListener changeListener;
    private static AppForm instance;
    JTabbedPane tabbedPane;
    JMenuBar menuBar;
    JPanel statusPane;
    JPopupMenu tabPopupMenu;
    JToolBar toolBar;
    HTMLToolBar htmlToolBar;
    HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE; // current
    // RuntimeContext

    static final HGPersistentHandle TOOLBAR_HANDLE = HGHandleFactory
            .makeHandle("d40c99be-f108-11dc-a860-d9a9d2c59ef1");
    static final HGPersistentHandle MENUBAR_HANDLE = HGHandleFactory
            .makeHandle("1d3b7df9-f109-11dc-9512-073dfab2b15a");
    static final HGPersistentHandle HTML_TOOLBAR_HANDLE = HGHandleFactory
    .makeHandle("56371f73-025d-11dd-b650-ef87b987c94a");
    private AppForm()
    {
    }

    private static boolean loaded;

    public void loadComponents()
    {
        graph = ThisNiche.getHyperGraph();
        HGTypeSystem ts = graph.getTypeSystem();
        if (ts.getType(CellGroupType.HGHANDLE) == null)
        {
            HGAtomType type = new CellGroupType();
            type.setHyperGraph(graph);
            ts.addPredefinedType(CellGroupType.HGHANDLE, type, CellGroup.class);
            type = new CellType();
            type.setHyperGraph(graph);
            ts.addPredefinedType(CellType.HGHANDLE, type, Cell.class);
            type = new NotebookDocumentType();
            type.setHyperGraph(graph);
            ts.addPredefinedType(NotebookDocumentType.HGHANDLE, type,
                    NotebookDocument.class);
            type = new NotebookUIType();
            type.setHyperGraph(graph);
            ts.addPredefinedType(NotebookUIType.HGHANDLE, type,
                    NotebookUI.class);
            type = new CellDocumentType();
            type.setHyperGraph(graph);
            ts.addPredefinedType(CellDocumentType.HGHANDLE, type,
                    CellDocument.class);
        }
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(LOGO_IMAGE_RESOURCE)));
        initComponents();
        changeListener = new TabbedPaneChangeListener();
        tabbedPane.addChangeListener(changeListener);
        docListener = new NotebookDocument.ModificationListener() {
            public void documentModified(Object o)
            {
                updateTitle();
            }
        };

    }

    public static void defineTypeClasses(HyperGraph graph, String resource)
    {
        InputStream in = null;
        try
        {
            in = AppForm.class.getResourceAsStream(resource);
            System.out.println("IN: " + in);
            Properties props = new Properties();
            props.load(in);
            for (Iterator i = props.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry e = (Map.Entry) i.next();
                Class clazz = Class.forName(e.getKey().toString().trim());
                HGPersistentHandle handle = HGHandleFactory.makeHandle(e
                        .getValue().toString().trim());
                graph.getTypeSystem().defineTypeAtom(handle, clazz);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            if (in != null) try
            {
                in.close();
            }
            catch (Throwable t)
            {
            }
        }
    }

    public AppForm loadFrame()
    {
        if (loaded) return this;
        loadComponents();
        setJMenuBar(menuBar);
        getContentPane().setLayout(new AKDockLayout());
        toolBar.setFloatable(true);
        getContentPane().add(toolBar, AKDockLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        Log.start();
        loaded = true;
        return this;
    }

    public JComponent getTabbedPane()
    {
        return this.tabbedPane;
    }

    public JComponent getBar()
    {
        return this.menuBar;
    }

    public JComponent getToolBar()
    {
        return this.toolBar;
    }
    
    public HTMLToolBar getHTMLToolBar(){
        return this.htmlToolBar;
    }

    public void openBooks()
    {
        if (StartMeUp.firstTime)
        {
            if (System.getenv("SCRIBA_HOME") != null) importGroup(new File(
                    new File(new File(System.getenv("SCRIBA_HOME")), "examples"),
                    "scribawelcome.nb"));
            changeListener.stateChanged(null);
        } else
        {
            Set<HGHandle> set = getConfig().getOpenedGroups();
            for (HGHandle h : set)
            {
                if (allready_opened(h, true)) return;
                Object o = ThisNiche.hg.get(h);
                if (o == null) continue;
                NotebookUI ui = new NotebookUI(h);
                addNotebookTab(ui);
            }
            if (!set.isEmpty()) changeListener.stateChanged(null);
        }
    }

    public void open(HGHandle h)
    {
        if (allready_opened(h, true)) return;
        NotebookUI ui = new NotebookUI(h);
        addNotebookTab(ui);
        getConfig().getOpenedGroups().add(ui.getDoc().getHandle());
    }

    public NotebookUI getCurrentNotebook()
    {
        return currentBook;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents()
    {
        // XXX
        PlasticLookAndFeel.setPlasticTheme(new DesertBluer());
        try
        {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        }
        catch (Exception e)
        {
        }

        if (DRAGGABLE_TABS)
        {
            tabbedPane = new CloseableDnDTabbedPane();
            ((CloseableDnDTabbedPane) tabbedPane).setPaintGhost(true);
            ((CloseableDnDTabbedPane) tabbedPane)
                    .addTabCloseListener(new TabbedPaneCloseListener());
        } else
            tabbedPane = new JTabbedPane();
        tabbedPane.setDoubleBuffered(!AppForm.PICCOLO);
        tabbedPane.putClientProperty(
                com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addMouseListener(new TabbedPaneMouseListener());
        // Create the status area.
        statusPane = new JPanel(new GridLayout(1, 1));
        status = new StatusBar(this);
        status.propertiesChanged();
        statusPane.add(status);

        createMenuBar();
        createMainToolBar();
        createHTMLToolBar();
    }

    private void createMenuBar()
    {
        menuBar = (JMenuBar) ThisNiche.hg.get(MENUBAR_HANDLE);
        if (menuBar == null)
        {
            menuBar = new JMenuBar();
            menuBar.add(createFileMenu());
            menuBar.add(createEditMenu());
            menuBar.add(createFormatMenu());
            menuBar.add(createToolsMenu());
            menuBar.add(createRuntimeMenu());
            ThisNiche.hg.define(MENUBAR_HANDLE, menuBar);
            //force the creation of the NotebookUI static popup
            NotebookUI.getPopupMenu();
        }
    }
    
    private JMenu createRuntimeMenu()
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

    private JPopupMenu getTabPopupMenu()
    {
        if (tabPopupMenu != null) return tabPopupMenu;
        tabPopupMenu = new JPopupMenu();
        Action act = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("AppForm - Close:");
                int res = promptAndSaveDoc();
                if (res == JOptionPane.CANCEL_OPTION
                        || res == JOptionPane.CLOSED_OPTION) return;
                int i = ((Integer) tabPopupMenu.getClientProperty(TAB_INDEX));
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
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION) continue;
                    closeAt(i);
                }
            }
        };
        tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All But Active") {
            public void actionPerformed(ActionEvent e)
            {
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                int index = ((Integer) tabPopupMenu
                        .getClientProperty(TAB_INDEX));
                for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION || i == index) continue;
                    closeAt(i);
                }
            }
        };
        tabPopupMenu.add(new JMenuItem(act));
        tabPopupMenu.add(new EnhancedMenu("Set Default Language",
                new ScriptEngineProvider(this)));
        tabPopupMenu.add(new EnhancedMenu("Set Runtime Context",
                new RCListProvider()));
        act = new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e)
            {
                String name = currentBook.getDoc().getTitle();
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                        AppForm.getInstance(), "Name: ", "Rename CellGroup");
                nd.setInputText(name);
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
                {
                    // HGHandle h = currentBook.getDoc().bookH;
                    currentBook.getDoc().setTitle(nd.getInputText());
                    // ThisNiche.hg.replace(h, currentBook.getDoc().getBook());
                    updateTitle();
                }
            }
        };
        tabPopupMenu.add(new JMenuItem(act));
        return tabPopupMenu;
    }

    void exit()
    {
        getConfig().getOpenedGroups().clear();
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--)
        {
            tabbedPane.setSelectedIndex(i);
            currentBook = (NotebookUI) ((JScrollPane) tabbedPane
                    .getSelectedComponent()).getViewport().getView();
//            int res = promptAndSaveDoc();
//            if (res == JOptionPane.CANCEL_OPTION
//                    || res == JOptionPane.CLOSED_OPTION) return;
            HGHandle h = getCurrentNotebook().getDoc().getHandle();
            if (h != null) getConfig().getOpenedGroups().add(h);
            // CellGroupMember cg = getCurrentNotebook().getDoc().getBook();
            // Cell c = (Cell)((CellGroup)((CellGroup)
            // cg).getElement(0)).getElement(0);
            // HGHandle hh = ((CellGroup)((CellGroup)
            // cg).getElement(0)).getTargetAt(0);
            // System.out.println("BEFORE: " + c.getValue() + ":" + hh);
            // ThisNiche.hg.update(cg);
            // System.out.println("AFTER: " + c.getValue());

        }
        if (AppForm.PICCOLO) PiccoloFrame.getInstance().saveDims();
        Log.end();
        System.exit(0);
    }

    public static ScriptEngineManager getScriptEngineManager()
    {
        if (scriptEngineManager == null) scriptEngineManager = new ScriptEngineManager(
                getClassLoader());
        return scriptEngineManager;
    }

    public static URLClassLoader getClassLoader()
    {
        if (classLoader == null)
        {
            // plugins
            Set<URL> pluginURLs = new HashSet<URL>();
            File[] files = (new File(getConfigDirectory(), EXT_DIR))
                    .listFiles();
            if (files != null) for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory()
                        || !files[i].getName().endsWith(".jar")) continue;
                String plugin = EXT_DIR + "/" + files[i].getName();
                URL url;
                try
                {
                    url = new URL("file", "", plugin);
                    pluginURLs.add(url);
                }
                catch (Exception ue)
                {
                    System.err.println("Jar: " + files[i].getAbsolutePath()
                            + "was not a valid URL");
                }
            }
            classLoader = new URLClassLoader(pluginURLs
                    .toArray(new URL[pluginURLs.size()]), AppForm.class
                    .getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return classLoader;
    }

    public static File getConfigDirectory()
    {
        // if (true) return new File(HARDCODED);
        try
        {
            CodeSource cs = AppForm.class.getProtectionDomain().getCodeSource();
            URL url = null;
            if (cs != null)
            {
                url = cs.getLocation();
                if (url == null)
                {
                    // Try to find 'cls' definition as a resource; this is not
                    // documented to be legal, but Sun's implementations seem to
                    // allow this:
                    final ClassLoader clsLoader = AppForm.class
                            .getClassLoader();
                    final String clsAsResource = AppForm.class.getName()
                            .replace('.', '/').concat(".class");
                    url = clsLoader != null ? clsLoader
                            .getResource(clsAsResource) : ClassLoader
                            .getSystemResource(clsAsResource);
                }
            }
            if (url != null)
            {
                // System.out.println("Self: " + url.getPath());
                return (new File(url.getPath())).getParentFile();
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(
                    "Unable to find installation directory:", ex);
        }
        return null;
    }

    // Add a couple of emacs key bindings for navigation.
    protected void addBindings()
    {
        InputMap inputMap = getCurrentNotebook().getInputMap();
        // Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);
        // Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);
        // Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);
        // Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);
        // Ctrl-z to removeUndo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
        inputMap.put(key, NotebookEditorKit.undoAction);
        // Ctrl-y to redo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
        inputMap.put(key, NotebookEditorKit.redoAction);
    }

    protected JMenu createFileMenu()
    {
        ActionManager man = ActionManager.getInstance();
        JMenu menu = new NBMenu("File");
        menu.setMnemonic('f');
        menu.add(new JMenuItem(man.putAction(new NewAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new AppForm.OpenAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new ImportAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK))));
        menu.add(new JMenuItem(man.putAction(new ExportAction())));
        menu.add(new JSeparator());
        menu.add(new EnhancedMenu("Recent Files", new RecentFilesProvider()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(man.putAction(new ExitAction(), KeyStroke
                .getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK))));
        // TODO: Just for testing purposes, would be removed
        JMenuItem mi = new JMenuItem("View Element Tree");
        mi.addActionListener(new ElementTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Cells Tree");
        mi.addActionListener(new CellTreeAction());
        menu.add(mi);
        mi = new JMenuItem("View Parse Tree");
        mi.addActionListener(new ParseTreeAction());
        menu.add(mi);
        return menu;
    }

    protected JMenu createFormatMenu()
    {
        JMenu menu = new NBMenu("Format");
        menu.setMnemonic('o');
        final JCheckBoxMenuItem m = new JCheckBoxMenuItem("Cell Numbers");
        m.addItemListener(new CellNumItemListener());
        menu.add(m);
        menu.addSeparator();
        menu.add(new EnhancedMenu("Visual Properties", new VisPropsProvider()));
        Action act = kit.getActionByName(NotebookEditorKit.formatAction);
        act.putValue(Action.NAME, "Format");
        JMenuItem mi = new JMenuItem(act);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
        menu.add(mi);
        return menu;
    }

    // Create the edit menu.
    protected JMenu createEditMenu()
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
        menu.addSeparator();
        act = kit.getActionByName(DefaultEditorKit.selectAllAction);
        act.putValue(Action.NAME, SELECT_ALL);
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

    protected JMenu createToolsMenu()
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
        act = kit.getActionByName(NotebookEditorKit.resetCellNumAction);
        menu.add(new JMenuItem(act));
        menu.add(new GlobMenuItem(kit
                .getActionByName(NotebookEditorKit.javaDocManagerAction)));
        menu.add(new JMenuItem(kit
                .getActionByName(NotebookEditorKit.ctxInspectorAction)));
        menu.add(new EnhancedMenu("Cell", new CellPropsProvider()));
        menu.add(new EnhancedMenu("CellGroup", new CellGroupPropsProvider()));
        return menu;
    }

    private void newNotebook()
    {
        NotebookUI ui = null;
        EvaluationContext ctx = ThisNiche.getEvaluationContext(currentRC);
        CellGroup nb = new CellGroup("CG");
        HGHandle nbHandle = graph.add(nb); // HGSystemFlags.MUTABLE |
        // HGSystemFlags.MANAGED);
        graph.freeze(nbHandle);
        ui = new NotebookUI(nbHandle, ctx);
        graph.add(new ContextLink(nbHandle, currentRC));
        ui.setCaretPosition(0);
        ui.getDoc().setModified(true);
        addNotebookTab(ui);
        getConfig().getOpenedGroups().add(ui.getDoc().getHandle());

    }

    private void addNotebookTab(final NotebookUI book)
    {
        currentBook = book;
        addBindings();
        final NotebookDocument doc = book.getDoc();
        book.addCaretListener(status);
        doc.addModificationListener(docListener);
        final JScrollPane scrollPane = new JScrollPane(book);
        scrollPane.setDoubleBuffered(!AppForm.PICCOLO);
        scrollPane.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce)
            {
                book.requestFocusInWindow();
                currentBook = book;
            }
        });
        scrollPane.setViewportView(book);
        tabbedPane.addTab(makeTabTitle(doc.getTitle()), scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        focusOnNotebook(book, false);
    }

    private void openNotebook()
    {
        File file = FileUtil
                .getFile(this, "Load Notebook", FileUtil.LOAD, null);
        if (file == null) return;
        importGroup(file);
    }

    public void importGroup(File file)
    {
        try
        {
            String fn = file.getAbsolutePath();
            HGHandle knownHandle = IOUtils.importCellGroup(fn);
            graph.add(new ContextLink(knownHandle, currentRC));
            NotebookUI ui = new NotebookUI(knownHandle);
            addNotebookTab(ui);
            getConfig().getMRUF().add(ui.getDoc().getHandle());
            getConfig().getOpenedGroups().add(ui.getDoc().getHandle());
            getConfig().setMRUD(file.getParent());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(
                    this, t, "Could not open: " + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // TODO: strange requirement to open new Notebook, if file doesnt
            // exist
            newNotebook();
        }
    }

    private boolean allready_opened(HGHandle h, boolean focus)
    {
        // NotebookDocument group = (NotebookDocument) ThisNiche.hg.get(h);
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            JScrollPane comp = (JScrollPane) tabbedPane.getComponentAt(i);
            NotebookUI ui = (NotebookUI) comp.getViewport().getView();
            if (ui.getDoc().getHandle().equals(h))
            {
                if (focus)
                {
                    tabbedPane.setSelectedIndex(i);
                    currentBook = ui;
                }
                return true;
            }
        }
        return false;
    }

    private CellGroup getNotebookAt(int i)
    {
        JScrollPane comp = (JScrollPane) tabbedPane.getComponentAt(i);
        NotebookUI ui = (NotebookUI) comp.getViewport().getView();
        return (CellGroup) ui.getDoc().getBook();
    }

    private void closeAt(int i)
    {
        graph.unfreeze(graph.getHandle(getNotebookAt(i)));
        tabbedPane.removeTabAt(i);
        if (tabbedPane.getTabCount() == 0)
        {
            currentBook = null;
            setTitle("Seco");
        } else
        {
            currentBook = (NotebookUI) ((JScrollPane) tabbedPane
                    .getSelectedComponent()).getViewport().getView();
            updateTitle(true);
        }
        if (AppForm.PICCOLO) PiccoloFrame.getInstance().repaintTabbedPane();
    }

    private int promptAndSaveDoc()
    {
        if (getCurrentNotebook() == null) return -1;
        // NotebookDocument doc = getCurrentNotebook().getDoc();
        getCurrentNotebook().close();
        // doc.save();
        return JOptionPane.OK_OPTION;
    }

    void updateTitle()
    {
        updateTitle(false);
    }

    private void updateTitle(boolean forced)
    {
        if (getCurrentNotebook() == null) return;
        CellGroupMember book = getCurrentNotebook().getDoc().getBook();
        String name = (book instanceof CellGroup) ? ((CellGroup) book).getName() : "Cell";
        RuntimeContext rcInstance = (RuntimeContext) graph.get(currentRC);
        String title = rcInstance.getName() + " " + name;
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), makeTabTitle(name));
        if (AppForm.PICCOLO)
        {
            PiccoloFrame.getInstance().setTitle(title);
            PiccoloFrame.getInstance().repaintTabbedPane();
        }
        // else
        setTitle(title);

    }

    private String makeTabTitle(String title)
    {
        if (title == null || title.length() == 0) title = UNTITLED;// +
        // count++;
        else
        {
            int ind = title.lastIndexOf('/');
            ind = Math.max(ind, title.lastIndexOf('\\'));
            if (ind > 0) title = title.substring(ind + 1);
        }
        // System.out.println("makeTabTitle: " + title);
        return title;
    }

    private void openElementTree(ActionEvent evt)
    {
        if (getCurrentNotebook() == null) return;
        // Notebook book = getCurrentNotebook().getDoc().getBook();
        JDialog dialog = new JDialog(this); // , book.getFilename());
        dialog.setSize(500, 800);
        JTree tree = new JTree((TreeNode) getCurrentNotebook().getDocument()
                .getDefaultRootElement());
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    private void openParseTree(ActionEvent evt)
    {
        NotebookUI ui = getCurrentNotebook();
        if (ui == null) return;
        JDialog dialog = new JDialog(this);
        dialog.setSize(500, 800);
        JTree tree = ui.getDoc().getParseTree(ui.getCaretPosition());
        if (tree == null) return;
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    private void openCellTree(ActionEvent evt)
    {
        NotebookUI ui = getCurrentNotebook();
        if (ui == null) return;
        CellGroup book = (CellGroup) ui.getDoc().getBook();
        JDialog dialog = new JDialog(this, book.getName());
        dialog.setSize(500, 800);
        JTree tree = new NotebookCellsTree(new NotebookTreeModel(book));
        JScrollPane pane = new JScrollPane(tree);
        dialog.add(pane);
        dialog.setVisible(true);
    }

    private void focusOnNotebook(NotebookUI currentBook, boolean restore_caret)
    {
        updateTitle(true);
        HGHandle h = currentBook.getDoc().bookH;
        if (h == null) throw new NullPointerException(
                "Null CellGroup Handle for Doc: " + currentBook.getDoc());
        setCurrentEvaluationContext(ThisNiche.getContextHandleFor(h));
        if (restore_caret) currentBook.restoreCaret();
    }

    public void setCurrentEvaluationContext(HGHandle ch)
    {
        if (ch == null) return;
        currentRC = ch;
        RuntimeContext rcInstance = (RuntimeContext) graph.get(currentRC);
        rcInstance.getBindings()
                .put("notebook", currentBook.getDoc().getBook());
        currentBook.getDoc().evalContext = ThisNiche.getEvaluationContext(ch);
    }

    public StatusBar getStatus()
    {
        return status;
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[])
    {
        final AppForm ed = AppForm.getInstance();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                ed.setVisible(true);
            }
        });
    }

    public static AppForm getInstance()
    {
        if (instance == null) instance = new AppForm();
        return instance;
    }

    public AppConfig getConfig()
    {
        return AppConfig.getInstance();
    }
    
    private JToolBar createHTMLToolBar()
    {
        htmlToolBar = (HTMLToolBar) ThisNiche.hg.get(HTML_TOOLBAR_HANDLE);
        if (htmlToolBar != null) return htmlToolBar;
        htmlToolBar = new HTMLToolBar();
        htmlToolBar.init();
        htmlToolBar.setEnabled(false);
        ThisNiche.hg.define(HTML_TOOLBAR_HANDLE, htmlToolBar);
        return htmlToolBar;
    }

    private JToolBar createMainToolBar()
    {        
        toolBar = (JToolBar) ThisNiche.hg.get(TOOLBAR_HANDLE);
        
        if (toolBar != null) //return toolBar = new JToolBar("Main");
            return toolBar;
        // ThisNiche.hg.getTypeSystem().getJavaTypeFactory().getSwingTypeMapper()
        // .addClass(seco.notebook.gui.ToolbarButton.class);
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
        ThisNiche.hg.define(TOOLBAR_HANDLE, toolBar);
        return toolBar;
    }

    private boolean html_toolbar_added;
    public void showHTMLToolBar(boolean show_or_hide)
    {
        if(!PICCOLO)
        {
            if(!html_toolbar_added){
            getContentPane().add(htmlToolBar, AKDockLayout.NORTH);
            getContentPane().invalidate();
            getContentPane().repaint();
            html_toolbar_added = true;
            }
        }
        htmlToolBar.setEnabled(show_or_hide);
    }

    public HGHandle getCurrentRuntimeContext()
    {
        return currentRC;
    }

    private static final class TabbedPaneMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            final AppForm app = AppForm.getInstance();
            if (SwingUtilities.isRightMouseButton(e))
            {
                Point pt = e.getPoint();
                for (int i = 0; i < app.tabbedPane.getTabCount(); i++)
                {
                    final Rectangle r = app.tabbedPane.getBoundsAt(i);
                    // System.out.println("AppForm: " + pt + ":" + r
                    // + ":" + r.contains(pt));
                    if (r != null && r.contains(pt))
                    {
                        app.getTabPopupMenu().putClientProperty(TAB_INDEX, i);
                        if (AppForm.PICCOLO)
                        {
                            Frame f = GUIUtilities.getFrame(e.getComponent());
                            pt = SwingUtilities.convertPoint(e.getComponent(),
                                    e.getX(), e.getY(), f);
                        }
                        app.getTabPopupMenu().show(app.tabbedPane, pt.x, pt.y);
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
            if (app.tabbedPane.getSelectedIndex() == -1) return;
            JScrollPane comp = (JScrollPane) app.tabbedPane
                    .getComponentAt(app.tabbedPane.getSelectedIndex());
            app.currentBook = (NotebookUI) comp.getViewport().getView();
            app.updateTitle(true);
            // System.out.println("TabbedPaneChangeListener: " + e);
            if (AppForm.PICCOLO) PiccoloFrame.getInstance().setTitle(
                    app.getTitle());
        }
    }

    public static final class TabbedPaneCloseListener implements // CloseableTabbedPane.
            TabCloseListener
    {
        public void tabClosed(TabCloseEvent evt)
        {
            // System.out.println("tabClosed: " + ":" + evt);
            // TODO: this get called twice on a Cancel option?
            int res = AppForm.getInstance().promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION) return;
            AppForm.getInstance().closeAt(evt.getClosedTab());
        }

        public boolean closeTab(int tabIndexToClose)
        {
            int res = AppForm.getInstance().promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION) return false;
            AppForm.getInstance().closeAt(tabIndexToClose);
            return true;
        }
    }

    private static class CellNumItemListener implements ItemListener
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

    // disable menuItems if no notebook presented
    // use GlobMenuItem to prevent disabling
    private static class NBMenu extends JMenu implements MenuListener
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
    private static class GlobMenuItem extends JMenuItem
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

    /* public */private static class ExitAction extends AbstractAction
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
            AppForm.getInstance().openCellTree(evt);
        }
    }

    public static class ParseTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            AppForm.getInstance().openParseTree(evt);
        }
    }

    public static class ElementTreeAction implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            AppForm.getInstance().openElementTree(evt);
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

    public static class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            this.putValue(Action.NAME, OPEN);
            this.putValue(Action.SMALL_ICON, IconManager
                    .resolveIcon("Open16.gif"));
            this.putValue(Action.SHORT_DESCRIPTION, "Import Notebook");
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            JDialog dialog = new JDialog(getInstance(),
                    "Open Or Delete CellGroup");
            dialog.setSize(500, 500);
            dialog.add(new OpenBookPanel());
            dialog.setVisible(true);
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() != WindowEvent.WINDOW_CLOSING) super
                .processWindowEvent(e);
        else
            exit();
    }
}
