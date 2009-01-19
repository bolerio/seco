package seco.notebook.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import seco.gui.AppForm;
import seco.notebook.NotebookUI;
import seco.notebook.StyleType;
import seco.notebook.gui.CellPropsDialog;
import seco.notebook.gui.DialogDescriptor;
import seco.notebook.gui.DialogDisplayer;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.gui.NotifyDescriptor;
import seco.notebook.gui.SettingsPreviewPane;
import seco.notebook.gui.SyntaxHiliteOptionPane;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.java.JavaFormatterOptionsPane;

public class VisPropsProvider implements DynamicMenuProvider, Serializable
{
    // transient ActionListener actionListener;
    transient MouseListener mouseListener;

    public VisPropsProvider()
    {
        // actionListener = new MIActionListener();
    }

    public boolean updateEveryTime()
    {
        return false;
    }

    public void update(JMenu menu)
    {
        menu.removeAll();
        for (final StyleType s : StyleType.values())
        {
            JMenuItem item = new JMenuItem(s.getDescription());
            item.addActionListener(new MIActionListener(s));
            menu.add(item);
        }

        JMenuItem item = new JMenuItem("Syntax Styles");
        item.addActionListener(new SyntaxStyleAction());
        menu.add(item);
        item = new JMenuItem("Formatter Properties");
        item.addActionListener(new FormatAction());
        menu.add(item);
    }

    private static final class FormatAction implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if(ui == null) return;
            JavaFormatterOptionsPane pane = new JavaFormatterOptionsPane();
            SettingsPreviewPane outer = new SettingsPreviewPane(pane);
            DialogDescriptor dd = new DialogDescriptor(GUIUtilities.getFrame(ui),
                    outer, "Formatter Properties");
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
                outer.save();
        }
    }

    private static class SyntaxStyleAction implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            ScriptSupport sup = ui.getDoc().getScriptSupport(
                    ui.getCaretPosition());
            if (sup == null)
                return;
            SyntaxHiliteOptionPane pane = new SyntaxHiliteOptionPane(sup);
            SettingsPreviewPane outer = new SettingsPreviewPane(ui.getDoc(),
                    pane, null);
            DialogDescriptor dd = new DialogDescriptor(GUIUtilities.getFrame(ui), outer,
                    "Syntax Styles");
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                outer.save();
                ui.getDoc().updateStyles();
            }
        }
    }

    static class MIActionListener extends AbstractAction
    {
        public StyleType stype;

        public MIActionListener()
        {
        }

        public MIActionListener(StyleType s)
        {
            super(s.toString());
            stype = s;
        }

        public void actionPerformed(ActionEvent evt)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null)
                return;
            CellPropsDialog dlg = new CellPropsDialog(
                    GUIUtilities.getFrame(ui), ui.getDoc(), stype);
            dlg.setVisible(true);
            if (dlg.succeeded())
            {
                ui.revalidate();
                ui.repaint();
            }
        }
    }

}
