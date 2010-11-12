/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.Element;

import seco.ThisNiche;
import seco.gui.CommonActions;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class CellGroupPropsProvider extends CellPropsProvider
{
    private static final long serialVersionUID = 8459743780230145374L;

    public CellGroupPropsProvider()
    {
    }

    protected void _update(JMenu menu, final NotebookUI nbui, final int off)
    {
        final Element el = nbui.getSelectedGroupElement();
        boolean enabled = (el != null);
        final CellGroupMember nb = (enabled) ? NotebookDocument.getNBElement(el) : null;
        JCheckBoxMenuItem initCellCheck = new JCheckBoxMenuItem("Init Group");
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
        JCheckBoxMenuItem readonlyCellCheck = new JCheckBoxMenuItem("Readonly");
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
       
        if (enabled)
        {
            JMenuItem m = new JMenuItem("Eval");
            m.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    nbui.getDoc().evalGroup(
                            (CellGroup) NotebookDocument.getNBElement(el));
                }
            });
            menu.add(m);
            
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
}
