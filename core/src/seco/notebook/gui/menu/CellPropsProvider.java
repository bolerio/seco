/*
 * This file is part of the Seco source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2010 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.gui.CommonActions;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class CellPropsProvider implements DynamicMenuProvider
{
   private static final long serialVersionUID = 8668523086705739697L;

   public void update(final JMenu menu)
   {
       NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
       if (nbui == null) return;
       int offset = nbui.getCaretPosition();
       _update(menu, nbui, offset);
   }

    protected void _update(JMenu menu, final NotebookUI nbui, final int off)
    {
        final Element el = nbui.getSelectedCellElement();
        boolean enabled = (el != null);
        JCheckBoxMenuItem initCellCheck = new JCheckBoxMenuItem("Init Cell");
        final CellGroupMember nb = (enabled) ? NotebookDocument
                .getNBElement(el) : null;
        initCellCheck.setEnabled(enabled);
        if (enabled)
        {
            initCellCheck.setSelected(CellUtils.isInitCell(nb));
            initCellCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_INIT_CELL);
                }
            });
        }
        menu.add(initCellCheck);
        final JCheckBoxMenuItem readonlyCellCheck = new JCheckBoxMenuItem(
                "Readonly");
        readonlyCellCheck.setEnabled(enabled);
        if (enabled)
        {
            readonlyCellCheck.setSelected(CellUtils.isReadonly(nb));
            readonlyCellCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_READONLY);
                }
            });
        }
        menu.add(readonlyCellCheck);
        if (enabled && (nb != null && "html".equals(CellUtils.getEngine(nb))))
        {
            final JCheckBoxMenuItem htmlCellCheck = new JCheckBoxMenuItem(
                    "Html View");
            htmlCellCheck.setSelected(CellUtils.isHTML((Cell) nb));
            htmlCellCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_HTML);
                }
            });
            menu.add(htmlCellCheck);
        }

        final Element output_el = nbui.getSelectedOutputCellElement();
        if (output_el != null)
        {
            final CellGroupMember out = NotebookDocument
                    .getNBElement(output_el);
            if (out == null) return;
            final JCheckBoxMenuItem outputCellCheck = new JCheckBoxMenuItem(
                    "Error Cell");
            outputCellCheck.setSelected(CellUtils.isError(out));
            outputCellCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    CellUtils.setBoolAttribute(out, XMLConstants.ATTR_ERROR,
                              e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            menu.add(outputCellCheck);
        }
        
        if (enabled && (nb != null))
        {
            JMenuItem m = new JMenuItem("Eval");
            m.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    try{
                     nbui.getDoc().evalCellInAuxThread(el);
                    }catch(BadLocationException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(m);
            
            final JMenuItem mi = new JMenuItem("Remove Output Cells");
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    List<HGHandle> outs = CellUtils.getOutCellHandles(ThisNiche
                            .handleOf(nb));
                    for (HGHandle h : outs)
                        ThisNiche.graph.remove(h, true);
                }
            });
            menu.add(mi);
            menu.add(new EnhancedMenu("Set Runtime Context",
                    new RCListProvider()));
            
            m = new JMenuItem("Add/Edit Description");
            m.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    CommonActions.editCGMDescription(ThisNiche.handleOf(nb));
                }
            });
            menu.add(m);
        }
    }
    
    public boolean updateEveryTime()
    {
        return true;
    }

   
}
