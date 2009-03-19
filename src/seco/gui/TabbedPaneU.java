package seco.gui;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGAtomRef;
import org.wonderly.swing.tabs.TabCloseEvent;
import org.wonderly.swing.tabs.TabCloseListener;

import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.gui.CloseableDnDTabbedPane;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.ScriptEngineProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.notebook.gui.menu.RCListProvider;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;

public class TabbedPaneU
{
    static final boolean DRAGGABLE_TABS = !TopFrame.PICCOLO;
    private static final String TAB_INDEX = "tab_index";
    private static final String UNTITLED = "Untitled";
    static JPopupMenu tabPopupMenu;
    private static JTabbedPane currentTP;

    private static HGHandle getNotebookHandleAt(JTabbedPane tp, int i)
    {
        JScrollPane comp = (JScrollPane) tp.getComponentAt(i);
        NotebookUI ui = (NotebookUI) comp.getViewport().getView();
        return ui.getDoc().getBookHandle();
    }

    private static void closeAt(JTabbedPane tp, int i)
    {
        HGHandle h = getNotebookHandleAt(tp, i);
        ThisNiche.hg.unfreeze(h);
        CellGroup top = ThisNiche.hg.get(GUIHelper.getTopCellGroupHandle(tp));
        top.remove(i);
        //tp.removeTabAt(i);
        if (tp.getTabCount() == 0) TopFrame.getInstance().setTitle("Seco");
        else
            GUIHelper.updateTitle(true);
    }

    private static int promptAndSaveDoc()
    {
        NotebookUI ui = NotebookUI.getFocusedNotebookUI();
        if (ui == null) return -1;
        ui.close();
        return JOptionPane.OK_OPTION;
    }

    private static JPopupMenu getTabPopupMenu()
    {
        if (tabPopupMenu != null) return tabPopupMenu;
        tabPopupMenu = new JPopupMenu();
        Action act = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e)
            {
                int res = promptAndSaveDoc();
                if (res == JOptionPane.CANCEL_OPTION
                        || res == JOptionPane.CLOSED_OPTION) return;
                int i = ((Integer) tabPopupMenu.getClientProperty(TAB_INDEX));
                closeAt(currentTP, i);
            }
        };

        tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All") {
            public void actionPerformed(ActionEvent e)
            {
                currentTP.setSelectedIndex(currentTP.getTabCount() - 1);
                for (int i = currentTP.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION) continue;
                    closeAt(currentTP, i);
                }
            }
        };
        tabPopupMenu.add(new JMenuItem(act));
        act = new AbstractAction("Close All But Active") {
            public void actionPerformed(ActionEvent e)
            {
                currentTP.setSelectedIndex(currentTP.getTabCount() - 1);
                int index = ((Integer) tabPopupMenu
                        .getClientProperty(TAB_INDEX));
                for (int i = currentTP.getTabCount() - 1; i >= 0; i--)
                {
                    int res = promptAndSaveDoc();
                    if (res == JOptionPane.CANCEL_OPTION
                            || res == JOptionPane.CLOSED_OPTION || i == index)
                        continue;
                    closeAt(currentTP, i);
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
                if (ui == null) return;
                String name = ui.getDoc().getTitle();
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                        GUIUtilities.getFrame(ui), "Name: ", "Rename CellGroup");
                nd.setInputText(name);
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION)
                {
                    String t = nd.getInputText();
                    ui.getDoc().setTitle(t);
                    currentTP.setTitleAt(currentTP.getSelectedIndex(), makeTabTitle(t));
                    GUIHelper.updateTitle();
                }
            }
        };
        tabPopupMenu.add(new JMenuItem(act));
        return tabPopupMenu;
    }

    public static String makeTabTitle(String title)
    {
        if (title == null || title.length() == 0) title = UNTITLED;
        else
        {
            int ind = title.lastIndexOf('/');
            ind = Math.max(ind, title.lastIndexOf('\\'));
            if (ind > 0) title = title.substring(ind + 1);
        }
        // System.out.println("makeTabTitle: " + title);
        return title;
    }

    public static final class TabbedPaneMouseListener extends MouseAdapter
    {
        protected JTabbedPane tabbedPane;

        public TabbedPaneMouseListener()
        {
        }

        public TabbedPaneMouseListener(JTabbedPane tabbedPane)
        {
            this.tabbedPane = tabbedPane;
        }

        public void mouseClicked(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                Point pt = e.getPoint();
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    final Rectangle r = tabbedPane.getBoundsAt(i);
                    if (r == null || !r.contains(pt)) continue;
                    currentTP = tabbedPane;
                    getTabPopupMenu().putClientProperty(TAB_INDEX, i);
                    if (TopFrame.PICCOLO)
                    {
                        Frame f = GUIUtilities.getFrame(e.getComponent());
                        pt = SwingUtilities.convertPoint(e.getComponent(), e
                                .getX(), e.getY(), f);
                    }
                    getTabPopupMenu().show(tabbedPane, pt.x, pt.y);
                    break;
                }
            }
            TopFrame.getInstance().repaint();
        }
    }

    public static final class TabbedPaneChangeListener implements
            ChangeListener
    {
        protected JTabbedPane tabbedPane;

        public TabbedPaneChangeListener(JTabbedPane tabbedPane)
        {
            this.tabbedPane = tabbedPane;
        }

        public TabbedPaneChangeListener()
        {
            super();
        }

        public void stateChanged(ChangeEvent e)
        {
            if (tabbedPane.getSelectedIndex() == -1) return;
            JScrollPane comp = (JScrollPane) tabbedPane
                    .getComponentAt(tabbedPane.getSelectedIndex());
            NotebookUI.setFocusedNotebookUI((NotebookUI) comp.getViewport()
                    .getView());
            GUIHelper.updateTitle(true);
        }
    }

    public static final class TabbedPaneCloseListener implements // CloseableTabbedPane.
            TabCloseListener
    {
        public void tabClosed(TabCloseEvent evt)
        {
            int res = promptAndSaveDoc();
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION) return;
            closeAt((JTabbedPane) evt.getSource(), evt.getClosedTab());
        }
    }

    public static void addNotebookTab(JTabbedPane tabbedPane,
            final NotebookUI book, boolean insert_in_top_cell_group)
    {
        final NotebookDocument doc = book.getDoc();
        if (TopFrame.getInstance().getCaretListener() != null)
            book.addCaretListener(TopFrame.getInstance().getCaretListener());
        if (TopFrame.getInstance().getDocListener() != null)
            doc
                    .addModificationListener(TopFrame.getInstance()
                            .getDocListener());
        final JScrollPane scrollPane = new JScrollPane(book);
        scrollPane.setDoubleBuffered(!TopFrame.PICCOLO);
        scrollPane.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce)
            {
                book.requestFocusInWindow();
            }
        });
        // if(scrollPane.isVisible())
        scrollPane.setViewportView(book);
        tabbedPane.addTab(makeTabTitle(doc.getTitle()), scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        TopFrame.setCurrentEvaluationContext(ThisNiche.getContextHandleFor(doc
                .getBookHandle()));
        if (insert_in_top_cell_group)
        {
            addTabToTabbedPaneGroup(
                    GUIHelper.getTopCellGroupHandle(tabbedPane), doc.getBookHandle());
         }
    }

    public static void addTabToTabbedPaneGroup(HGHandle groupH, HGHandle h)
    {
        CellGroup group = (CellGroup) ThisNiche.hg.get(groupH);
        HGAtomRef ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        Cell out = new Cell(ref);
        HGHandle outH = ThisNiche.handleOf(out);
        if (outH == null) outH = ThisNiche.hg.add(out);
        group.insert(group.getArity(), outH);
        ThisNiche.hg.update(group);
    }

    public static JTabbedPane createTabbedPane(CellGroup group)
    {
        JTabbedPane tabbedPane = null;
        if (DRAGGABLE_TABS)
        {
            tabbedPane = new CloseableDnDTabbedPane();
            ((CloseableDnDTabbedPane) tabbedPane).setPaintGhost(true);
            ((CloseableDnDTabbedPane) tabbedPane)
                    .addTabCloseListener(new TabbedPaneCloseListener());
        }
        else
            tabbedPane = new SecoTabbedPane(ThisNiche.handleOf(group));
        tabbedPane.setDoubleBuffered(!TopFrame.PICCOLO);
        tabbedPane.putClientProperty(
                com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addMouseListener(new TabbedPaneMouseListener(tabbedPane));
        tabbedPane.addChangeListener(new TabbedPaneChangeListener(tabbedPane));
        return tabbedPane;
    }
}
