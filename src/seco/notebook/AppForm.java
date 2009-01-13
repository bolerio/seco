/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGAtomRef;

import seco.ThisNiche;
import seco.boot.StartMeUp;
import seco.gui.VisualAttribs;
import seco.notebook.gui.AKDockLayout;
import seco.notebook.gui.CloseableDnDTabbedPane;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.util.FileUtil;
import seco.notebook.util.Log;
import seco.rtenv.ContextLink;
import seco.rtenv.EvaluationContext;
import seco.rtenv.RuntimeContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.IOUtils;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBluer;

/**
 * 
 * @author bizi
 */
public class AppForm extends javax.swing.JFrame
{
    public static boolean PICCOLO = true;
    static final String UNTITLED = "Untitled";
    
    private NotebookUI currentBook;
    private StatusBar status;
    NotebookDocument.ModificationListener docListener;
    private ChangeListener changeListener;
    private static AppForm instance;
    //public JTabbedPane tabbedPane;
    private JPanel statusPane;
    private HGHandle currentRC = ThisNiche.TOP_CONTEXT_HANDLE; // current
                                                        // RuntimeContext

    private AppForm()
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
    }

    public void loadComponents()
    {
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(GUIHelper.LOGO_IMAGE_RESOURCE)));
        
        // Create the status area.
        statusPane = new JPanel(new GridLayout(1, 1));
        status = new StatusBar(this);
        status.propertiesChanged();
        statusPane.add(status);
        
        changeListener = new GUIHelper.TabbedPaneChangeListener();
        GUIHelper.getJTabbedPane().addChangeListener(changeListener);
        docListener = new NotebookDocument.ModificationListener() {
            public void documentModified(Object o)
            {
                updateTitle();
            }
        };
    }

    private static boolean loaded;
    public AppForm loadFrame()
    {
        if (loaded) return this;
        loadComponents();
        setJMenuBar(GUIHelper.getMenuBar());
        getContentPane().setLayout(new AKDockLayout());
        GUIHelper.getMainToolBar().setFloatable(true);
        getContentPane().add(GUIHelper.getMainToolBar(), AKDockLayout.NORTH);
        getContentPane().add(GUIHelper.getJTabbedPane(), BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        Log.start();
        loaded = true;
        return this;
    }

   
//    public void openBooks()
//    {
//        if (StartMeUp.firstTime)
//        {
//            if (System.getenv("SCRIBA_HOME") != null)
//                importGroup(new File(new File(new File(System
//                        .getenv("SCRIBA_HOME")), "examples"),
//                        "scribawelcome.nb"));
//            changeListener.stateChanged(null);
//        } else
//        {
//            Set<HGHandle> set = AppConfig.getInstance().getOpenedGroups();
//            for (HGHandle h : set)
//            {
//                if (allready_opened(h, true))
//                    return;
//                Object o = ThisNiche.hg.get(h);
//                if (o == null)
//                    continue;
//                NotebookUI ui = new NotebookUI(h);
//                addNotebookTab(ui);
//            }
//            if (!set.isEmpty())
//                changeListener.stateChanged(null);
//        }
//    }

    public void open(HGHandle h)
    {
        if (allready_opened(h, true)) return;
        NotebookUI ui = new NotebookUI(h);
        addNotebookTab(ui);
        //AppConfig.getInstance().getOpenedGroups().add(ui.getDoc().getHandle());
        addTabToTabbedPaneGroup(ui.getDoc().getHandle());
    }

    public NotebookUI getCurrentNotebook()
    {
        return currentBook;
    }

    public void exit()
    {
        //AppConfig.getInstance().getOpenedGroups().clear();
        clearTabbedPaneGroup();
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--)
        {
            tabbedPane.setSelectedIndex(i);
            currentBook = (NotebookUI) ((JScrollPane) tabbedPane
                    .getSelectedComponent()).getViewport().getView();
            HGHandle h = getCurrentNotebook().getDoc().getHandle();
            if (h != null)
                addTabToTabbedPaneGroup(h);
                //AppConfig.getInstance().getOpenedGroups().add(h);
        }
        if (AppForm.PICCOLO)
            PiccoloFrame.getInstance().saveDims();
        Log.end();
        System.exit(0);
    }

    public void newNotebook()
    {
        NotebookUI ui = null;
        EvaluationContext ctx = ThisNiche.getEvaluationContext(currentRC);
        CellGroup nb = new CellGroup("CG");
        HGHandle nbHandle = ThisNiche.hg.add(nb); // HGSystemFlags.MUTABLE |
        // HGSystemFlags.MANAGED);
        ThisNiche.hg.freeze(nbHandle);
        ui = new NotebookUI(nbHandle, ctx);
        ThisNiche.hg.add(new ContextLink(nbHandle, currentRC));
        ui.setCaretPosition(0);
        ui.getDoc().setModified(true);
        addNotebookTab(ui);
        //AppConfig.getInstance().getOpenedGroups().add(ui.getDoc().getHandle());
        addTabToTabbedPaneGroup(ui.getDoc().getHandle());
    }

    //TODO:
    public void addNotebookTab(final NotebookUI book)
    {
        currentBook = book;
        // addBindings();
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
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
        tabbedPane.addTab(makeTabTitle(doc.getTitle()), scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        focusOnNotebook(book, false);
    }

    public void openNotebook()
    {
        File file = FileUtil
                .getFile(this, "Load Notebook", FileUtil.LOAD, null);
        if (file == null)
            return;
        importGroup(file);
    }

    public void importGroup(File file)
    {
        try
        {
            String fn = file.getAbsolutePath();
            HGHandle knownHandle = IOUtils.importCellGroup(fn);
            ThisNiche.hg.add(new ContextLink(knownHandle, currentRC));
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
                    this, t, "Could not open: " + file.getAbsolutePath());
            DialogDisplayer.getDefault().notify(ex);
            // TODO: strange requirement to open new Notebook, if file doesnt
            // exist
            newNotebook();
        }
    }
    
    private static void clearTabbedPaneGroup()
    {
        CellGroup group = (CellGroup)ThisNiche.hg.get(GUIHelper.TABBED_PANE_GROUP_HANDLE);
        group.removeAll();
        ThisNiche.hg.update(group);
    }
    
    private static void addTabToTabbedPaneGroup(HGHandle h)
    {
        CellGroup group = (CellGroup)ThisNiche.hg.get(GUIHelper.TABBED_PANE_GROUP_HANDLE);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.hard);
        Cell out = new Cell(ref);
        HGHandle outH = ThisNiche.handleOf(out);
        if(outH == null)
            outH = ThisNiche.hg.add(out);
        group.insert(group.getArity(), outH);
        ThisNiche.hg.update(group);
    }

    private boolean allready_opened(HGHandle h, boolean focus)
    {
        // NotebookDocument group = (NotebookDocument) ThisNiche.hg.get(h);
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
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
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
        JScrollPane comp = (JScrollPane) tabbedPane.getComponentAt(i);
        NotebookUI ui = (NotebookUI) comp.getViewport().getView();
        return (CellGroup) ui.getDoc().getBook();
    }

    public void closeAt(int i)
    {
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
        ThisNiche.hg.unfreeze(ThisNiche.hg.getHandle(getNotebookAt(i)));
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
        if (AppForm.PICCOLO)
            PiccoloFrame.getInstance().repaintTabbedPane();
    }

    void updateTitle()
    {
        updateTitle(false);
    }

    void updateTitle(boolean forced)
    {
        if (getCurrentNotebook() == null)
            return;
        CellGroupMember book = getCurrentNotebook().getDoc().getBook();
        String name = (book instanceof CellGroup) ? ((CellGroup) book)
                .getName() : "Cell";
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.hg
                .get(currentRC);
        String title = rcInstance.getName() + " " + name;
        JTabbedPane tabbedPane = GUIHelper.getJTabbedPane();
        tabbedPane
                .setTitleAt(tabbedPane.getSelectedIndex(), makeTabTitle(name));
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

    private void focusOnNotebook(NotebookUI currentBook, boolean restore_caret)
    {
        updateTitle(true);
        HGHandle h = currentBook.getDoc().bookH;
        if (h == null)
            throw new NullPointerException("Null CellGroup Handle for Doc: "
                    + currentBook.getDoc());
        setCurrentEvaluationContext(ThisNiche.getContextHandleFor(h));
        if (restore_caret)
            currentBook.restoreCaret();
    }

    public void setCurrentEvaluationContext(HGHandle ch)
    {
        if (ch == null) return;
        currentRC = ch;
        RuntimeContext rcInstance = (RuntimeContext) ThisNiche.hg
                .get(currentRC);
        rcInstance.getBindings()
                .put("notebook", currentBook.getDoc().getBook());
        currentBook.getDoc().evalContext = ThisNiche.getEvaluationContext(ch);
    }

    public StatusBar getStatus()
    {
        return status;
    }

    public static AppForm getInstance()
    {
        if (instance == null)
            instance = new AppForm();
        return instance;
    }

    private boolean html_toolbar_added;

    public void showHTMLToolBar(boolean show_or_hide)
    {
        if (!PICCOLO)
        {
            if (!html_toolbar_added)
            {
                getContentPane().add(GUIHelper.getHTMLToolBar(), AKDockLayout.NORTH);
                getContentPane().invalidate();
                getContentPane().repaint();
                html_toolbar_added = true;
            }
        }
        GUIHelper.getHTMLToolBar().setEnabled(show_or_hide);
    }

    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() != WindowEvent.WINDOW_CLOSING) super
                .processWindowEvent(e);
        else
            exit();
    }

   
    public void setCurrentNotebook(NotebookUI currentBook)
    {
        this.currentBook = currentBook;
    }
}
