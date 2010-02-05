/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import seco.gui.StandaloneFrame;
import seco.notebook.DocUtil;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.view.HtmlView;
import seco.things.Cell;
import seco.things.CellGroupMember;

public class CellLangProvider implements DynamicMenuProvider
{
    private static final long serialVersionUID = 5406878794567628416L;
    
    protected transient NotebookUI nbui;
    protected transient StandaloneFrame app;
    transient MouseListener mouseListener;

    public CellLangProvider()
    {
    }

    public boolean updateEveryTime()
    {
        return true;
    }

    public void update(final JMenu menu)
    {
        nbui = NotebookUI.getFocusedNotebookUI();
        if (nbui == null) return;
        Collection<JMenuItem> items = getLanguages(nbui);
        for (JMenuItem item : items)
            menu.add(item);
    }

    static final Collection<JMenuItem> EMPTY = new LinkedList<JMenuItem>();
    static Collection<JMenuItem> langMenuItems;

    public static Collection<JMenuItem> getLanguages(final Object ui)
    {
        final NotebookUI nbui = (ui instanceof NotebookUI) ? (NotebookUI) ui
                : ((HtmlView.InnerHTMLEditor) ui).getNotebookUI();
        final NotebookDocument doc = nbui.getDoc();
        final int offset = nbui.getCaretPosition();
        CellGroupMember nb = NotebookDocument.getNBElement(doc
                .getEnclosingCellElement(offset));
        if (nb == null || !(nb instanceof Cell)) return EMPTY;
        final Cell cell = (Cell) nb;
        String engine_name = DocUtil.getEngineName(doc, cell);
        if (langMenuItems == null) initLangMenuItems(nbui, cell);

        for (final JMenuItem m : langMenuItems)
        {
            for (ItemListener l : m.getItemListeners())
                m.removeItemListener(l);
            m.setSelected(engine_name.equals(m.getText()));
            m.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    if (m.isSelected())
                        nbui.setCellEngine(m.getText(), offset);
                }
            });
        }

        return langMenuItems;
    }

    private static void initLangMenuItems(final NotebookUI nbui, final Cell cell)
    {
        NotebookDocument doc = nbui.getDoc();
        ButtonGroup group = new ButtonGroup();
        Iterator<String> languages = doc.getEvaluationContext().getLanguages();
        // String engine_name = DocUtil.getEngineName(doc, cell);
        langMenuItems = new HashSet<JMenuItem>();
        while (languages.hasNext())
        {
            JMenuItem m = new JRadioButtonMenuItem(languages.next());
            group.add(m);
            langMenuItems.add(m);
        }
    }
}
