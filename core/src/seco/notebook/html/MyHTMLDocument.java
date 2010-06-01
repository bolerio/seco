/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.net.URL;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AbstractDocument.ElementEdit;
import javax.swing.text.DefaultStyledDocument.AttributeUndoableEdit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction;
import javax.swing.text.html.HTMLDocument.HTMLReader.TagAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import seco.notebook.NotebookDocument.NamedCompoundEdit;

public class MyHTMLDocument extends HTMLDocument
{
	// reusable object for compound events
	protected CompoundEdit cEdit = new NamedCompoundEdit(null);
	// stack for nested compound events
	protected Stack<String> editStack = new Stack<String>();
	protected Vector<UndoableEdit> removeUndo = new EditVector<UndoableEdit>();
	protected Vector<UndoableEdit> insertUndo = new EditVector<UndoableEdit>();

	public HTMLEditorKit.ParserCallback getReader(int pos)
	{
		Object desc = getProperty(Document.StreamDescriptionProperty);
		if (desc instanceof URL)
		{
			setBase((URL) desc);
		}
		MyHTMLReader reader = new MyHTMLReader(pos);
		return reader;
	}

	public MyHTMLDocument()
	{
		super();
	}

	public MyHTMLDocument(Content c, StyleSheet styles)
	{
		super(c, styles);
	}

	public MyHTMLDocument(StyleSheet styles)
	{
		super(styles);
	}
	
	void adjustP(){
		Element body = getRootElements()[0].getElement(1);
		System.out.println("AdjustP: " + body + ":" +
				body.getElementCount());
		if(body.getElementCount() > 1 ||
				HTMLUtils.getName(body.getElement(0)) == HTML.Tag.IMPLIED)
		{
			MutableAttributeSet attr = new SimpleAttributeSet();
			attr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
			//setParagraphAttributes(body.getStartOffset(), 
			//		body.getEndOffset() - body.getStartOffset(), attr,	false);
			setCharacterAttributes(body.getStartOffset(), 
					body.getEndOffset() - body.getStartOffset(), attr,	false);
		}
	}

	void beginCompoundEdit(final String name)
	{
		cEdit = new NamedCompoundEdit(name);
		editStack.push(name);
	}

	void endCompoundEdit()
	{
		editStack.pop();
		if (!editStack.isEmpty()) return;
		cEdit.end();
		super.fireUndoableEditUpdate(new UndoableEditEvent(this, cEdit));
	}

	private boolean isCompoundEditInProgress()
	{
		return editStack != null && !editStack.isEmpty();
	}

	private CompoundEdit makeCompoundEdit(Vector<UndoableEdit> edits,
			DefaultDocumentEvent dde)
	{
		if (edits == null || edits.isEmpty()) return null;
		CompoundEdit cE = new CompoundEdit();
		if (dde != null) cE.addEdit(dde);
		for (UndoableEdit ed : edits)
			if (ed != null) cE.addEdit(ed);
		cE.end();
		edits.clear();
		return cE;
	}

	protected boolean modified = false;
	
	public boolean isModified()
    {
        return modified;
    }

    public void setModified(boolean modified)
    {
        this.modified = modified;
    }

    protected void fireUndoableEditUpdate(UndoableEditEvent e)
	{
	    modified = true;
		if (!(e.getEdit() instanceof DefaultDocumentEvent))
		{
			super.fireUndoableEditUpdate(e);
			return;
		}
		DefaultDocumentEvent dde = (DefaultDocumentEvent) e.getEdit();
		if (isCompoundEditInProgress())
		{
			cEdit.addEdit(dde);
			return;
		}
		CompoundEdit cE = null;
		if (DocumentEvent.EventType.INSERT.equals(dde.getType()))
			cE = makeCompoundEdit(insertUndo, dde);
		else if (DocumentEvent.EventType.REMOVE.equals(dde.getType()))
			cE = makeCompoundEdit(removeUndo, dde);
		if (cE != null)
			super.fireUndoableEditUpdate(new UndoableEditEvent(e.getSource(),
					cE));
		else
			super.fireUndoableEditUpdate(e);
	}

	public void addAttributes(Element e, AttributeSet a)
	{
		if ((e != null) && (a != null))
		{
			try
			{
				writeLock();
				//System.out.println("MyHTMLDocument addAttributes a=" + a);
				int start = e.getStartOffset();
				DefaultDocumentEvent changes = new DefaultDocumentEvent(start,
						e.getEndOffset() - start,
						DocumentEvent.EventType.CHANGE);
				AttributeSet sCopy = a.copyAttributes();
				MutableAttributeSet attr = (MutableAttributeSet) e
						.getAttributes();
				changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
				attr.addAttributes(a);
				changes.end();
				fireChangedUpdate(changes);
				fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
			}
			finally
			{
				writeUnlock();
			}
		}
	}

	public void replaceAttributes(Element e, AttributeSet a)
	{
		if ((e != null) && (a != null))
		{
			try
			{
				writeLock();
				int start = e.getStartOffset();
				DefaultDocumentEvent changes = new DefaultDocumentEvent(start,
						e.getEndOffset() - start,
						DocumentEvent.EventType.CHANGE);
				MutableAttributeSet sCopy = new SimpleAttributeSet(a);
				sCopy.addAttribute(StyleConstants.NameAttribute, HTMLUtils
						.getName(e));
				changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
				MutableAttributeSet attr = (MutableAttributeSet) e
						.getAttributes();
				Enumeration aNames = attr.getAttributeNames();
				while (aNames.hasMoreElements())
				{
					Object aName = aNames.nextElement();
					if (!aName.equals(StyleConstants.NameAttribute))
						attr.removeAttribute(aName);
				}
				attr.addAttributes(a);
				changes.end();
				fireChangedUpdate(changes);
				fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
			}
			finally
			{
				writeUnlock();
			}
		}
	}

	public void removeElements(Element e, int index, int count)
			throws BadLocationException
	{
		writeLock();
		int start = e.getElement(index).getStartOffset();
		int end = e.getElement(index + count - 1).getEndOffset();
		try
		{
			Element[] removed = new Element[count];
			Element[] added = new Element[0];
			for (int counter = 0; counter < count; counter++)
			{
				removed[counter] = e.getElement(counter + index);
			}
			DefaultDocumentEvent dde = new DefaultDocumentEvent(start, end
					- start, DocumentEvent.EventType.REMOVE);
			((AbstractDocument.BranchElement) e).replace(index, removed.length,
					added);
			dde.addEdit(new ElementEdit(e, index, removed, added));
			UndoableEdit u = getContent().remove(start, end - start);
			if (u != null)
			{
				dde.addEdit(u);
			}
			postRemoveUpdate(dde);
			dde.end();
			fireRemoveUpdate(dde);
			if (u != null)
			{
				fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
			}
		}
		finally
		{
			writeUnlock();
		}
	}

	class MyHTMLReader extends HTMLReader
	{
		public MyHTMLReader(int offset, int popDepth, int pushDepth,
				Tag insertTag)
		{
			super(offset, popDepth, pushDepth, insertTag);
		}

		public MyHTMLReader(int offset)
		{
			super(offset);
		}

		@Override
		public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
		{
			// System.out.println("handleStartTag: " + t);
			if ((t == HTML.Tag.HTML || t == HTML.Tag.BODY || t == HTML.Tag.HEAD))
			{
				// System.out.println("handleStartTag: " + t + ":" +
				// a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED));
				if (a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED) != null)
					putProperty(t, true);
			}
			super.handleStartTag(t, a, pos);
		}
		
		@Override
		public void handleEndTag(Tag t, int pos)
		{
			super.handleEndTag(t, pos);
		}
		
		ParagraphAction pa = new ParagraphAction();
	}

	private class EditVector<T extends UndoableEdit> extends Vector<T>
	{
		public synchronized boolean add(T edit)
		{
			if (edit == null) return false;
			return isCompoundEditInProgress() ? cEdit.addEdit(edit) : super
					.add(edit);
		}
	}
}
