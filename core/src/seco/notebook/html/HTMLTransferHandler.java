/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import seco.notebook.NotebookUI;
import seco.notebook.NotebookTransferHandler.ElementTransferable;


public class HTMLTransferHandler extends TransferHandler
{
	boolean shouldRemove;
	
	public int getSourceActions(JComponent c)
	{
		int actions = COPY;
		if (((JTextComponent) c).isEditable()) actions = COPY_OR_MOVE;
		return actions;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		JTextComponent c = (JTextComponent) comp;
		if (!(c.isEditable() && c.isEnabled())) return false;
		for(DataFlavor df : transferFlavors)
			if(HTMLTextSelection.isDataFlavorSupportedEx(df))
				return true;
		return false;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		shouldRemove = true;
		HTMLEditor pane = (HTMLEditor) c;
		int start = pane.getSelectionStart();
		try
		{
			Document doc = pane.getDocument();
			HTMLText text = new HTMLText(pane, doc.createPosition(start),
					doc.createPosition(pane.getSelectionEnd()));
			return new HTMLTextSelection(text);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		System.out.println("exportDone: " + (action == MOVE) + ":" + shouldRemove);
		
		if (shouldRemove && action == MOVE)
		{
			if (data instanceof HTMLTextSelection)
				((HTMLTextSelection) data).data.removeText();
		}
	}

	@Override
	public boolean importData(JComponent comp, Transferable t)
	{
		HTMLEditor pane = (HTMLEditor) comp;
		boolean imported = false;
		if (t != null)
		{
			try
			{
				if (t.isDataFlavorSupported(HTMLTextSelection.HTML_FLAVOUR))
				{
					HTMLText st = (HTMLText) 
					    t.getTransferData(HTMLTextSelection.HTML_FLAVOUR);
					pane.replaceSelection(st);
				} else if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					String text = (String) t
							.getTransferData(DataFlavor.stringFlavor);
					pane.replaceSelection(text);
				}
				imported = true;
			}
			catch (Exception e)
			{
				pane.getToolkit().beep();
			}
		}
		return imported;
	}
	
	public void exportAsDrag(JComponent comp, InputEvent e, int action)
	{
		System.out.println("exportAsDrag: " + (action == MOVE) + ":" + e);
		shouldRemove = true;//(action == MOVE);
		super.exportAsDrag(comp, e, action);
	}
}
