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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.text.Element;

import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class CellGroupPropsProvider extends CellPropsProvider
{
	public CellGroupPropsProvider()
	{
	}

	public boolean updateEveryTime()
	{
		return true;
	}

	protected void _update(JMenu menu, final NotebookUI nbui, final int off)
	{
		//final NotebookDocument doc = nbui.getDoc();
		final Element el = nbui.getSelectedGroupElement();
		boolean enabled = (el != null);
		final CellGroupMember nb = (enabled) ? NotebookDocument.getNBElement(el) : null;
		JCheckBoxMenuItem initCellCheck = new JCheckBoxMenuItem("Init Cell");
		initCellCheck.setEnabled(enabled);
		if (enabled)
		{
			initCellCheck.setSelected(CellUtils.isInitCell(nb));
			initCellCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e)
				{
					//doc.toggleInitCell(el.getStartOffset(), false);
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
					//doc.toggleReadonlyCell(el, false);
				    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_READONLY);
				}
			});
		}
		menu.add(readonlyCellCheck);
	}
}
