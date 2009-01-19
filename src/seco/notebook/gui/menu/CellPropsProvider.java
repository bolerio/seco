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

import seco.gui.StandaloneFrame;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.notebook.gui.GUIUtilities;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class CellPropsProvider implements DynamicMenuProvider
{
	public boolean updateEveryTime()
	{
		return true;
	}

	protected void _update(JMenu menu, final NotebookUI nbui, final int off)
	{
		final Element el = nbui.getSelectedCellElement();
		boolean enabled = (el != null);
		JCheckBoxMenuItem initCellCheck = new JCheckBoxMenuItem("Init Cell");
		final CellGroupMember nb = (enabled) ? NotebookDocument.getNBElement(el) : null;
		initCellCheck.setEnabled(enabled);
		if (enabled)
		{
			initCellCheck.setSelected(CellUtils.isInitCell(nb));
			initCellCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e)
				{
					//nbui.getDoc().toggleInitCell(el.getStartOffset(), true);
				    CellUtils.toggleAttribute(nb,XMLConstants.ATTR_INIT_CELL);
				}
			});
		}
		menu.add(initCellCheck);
		final JCheckBoxMenuItem readonlyCellCheck = new JCheckBoxMenuItem("Readonly");
		readonlyCellCheck.setEnabled(enabled);
		if (enabled) 
		{
			readonlyCellCheck.setSelected(CellUtils.isReadonly(nb));
		    readonlyCellCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				//nbui.getDoc().toggleReadonlyCell(el, true);
			    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_READONLY);
			}
		});
		}
		menu.add(readonlyCellCheck);
		if (enabled && (nb != null && "html".equals( CellUtils.getEngine(nb))))
		{
			final JCheckBoxMenuItem htmlCellCheck = new JCheckBoxMenuItem(
					"Html View");
			htmlCellCheck.setSelected(CellUtils.isHTML((Cell)nb));
			htmlCellCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e)
				{
					//nbui.getDoc().toggleHTMLCell(el.getStartOffset());
				    CellUtils.toggleAttribute(nb, XMLConstants.ATTR_HTML);
				}
			});
			menu.add(htmlCellCheck);
		}
		
		final Element output_el = nbui.getSelectedOutputCellElement();
		if (output_el != null)
		{
			final CellGroupMember out = NotebookDocument.getNBElement(output_el);
			if(out == null) return;
			final JCheckBoxMenuItem outputCellCheck = 
				new JCheckBoxMenuItem("Error Cell");
			outputCellCheck.setSelected(CellUtils.isError(out));
			outputCellCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e)
				{
					//nbui.getDoc().toggleErrorCell(output_el.getStartOffset());
				    CellUtils.toggleAttribute(out, XMLConstants.ATTR_ERROR);
				}
			});
			menu.add(outputCellCheck);
		}
	}

	public void update(final JMenu menu)
	{
		//AppForm app = (AppForm) GUIUtilities.getFrame(menu);
		//if (app == null) return;
		NotebookUI nbui = NotebookUI.getFocusedNotebookUI();//app.getCurrentNotebook();
		if (nbui == null) return;
		int offset = nbui.getCaretPosition();
		_update(menu, nbui, offset);
	}
}
