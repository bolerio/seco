/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import javax.swing.plaf.TextUI;
import javax.swing.text.Element;
import javax.swing.text.View;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.util.CharSequenceUtilities;
import seco.notebook.util.DocumentUtilities;
import seco.notebook.util.SegmentCache;


/**
 * Various useful editor functions. Some of the methods have the same fontNames
 * and signatures like in javax.swing.Utilities but there is also many other
 * useful methods. All the methods are static so there's no reason to
 * instantiate Utilities.
 * 
 * All the methods working with the document rely on that it is locked against
 * modification so they don't acquire document read/write lock by themselves to
 * guarantee the full thread safety of the execution. It's the user's task to
 * lock the document appropriately before using methods described here.
 * 
 * @author Miloslav Metelka
 * @version 0.10
 */
public class Utilities
{
	private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N
	/** Switch the case to capital letters. Used in changeCase() */
	public static final int CASE_UPPER = 0;
	/** Switch the case to small letters. Used in changeCase() */
	public static final int CASE_LOWER = 1;
	/** Switch the case to reverse. Used in changeCase() */
	public static final int CASE_SWITCH = 2;
	/** Fake TextAction for getting the info of the focused component */
	private static TextAction focusedComponentAction;
	private static final boolean isLoggable = false;// ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL);

	private Utilities()
	{
		// instantiation has no sense
	}

	/**
	 * Get the starting position of the row.
	 * @param doc document to operate on
	 * @param offset position in document where to start searching
	 * @return position of the start of the row or -1 for invalid position
	 */
	public static int getRowStart(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		return getRowStart(doc, offset, 0);
	}

	/**
	 * Get the starting position of the row while providing relative count of
	 * row how the given position should be shifted. This is the most efficient
	 * way how to move by lines in the document based on some position. There is
	 * no similair getRowEnd() method that would have shifting parameter.
	 * @param doc document to operate on
	 * @param offset position in document where to start searching
	 * @param lineShift shift the given offset forward/back relatively by some
	 * amount of lines
	 * @return position of the start of the row or -1 for invalid position
	 */
	public static int getRowStart(NotebookDocument doc, int offset,
			int lineShift) throws BadLocationException
	{
		checkOffsetValid(doc, offset);
		if (lineShift != 0)
		{
			Element lineRoot = doc.getParagraphElement(0).getParentElement();
			int line = lineRoot.getElementIndex(offset);
			line += lineShift;
			if (line < 0 || line >= lineRoot.getElementCount())
			{
				return -1; // invalid line shift
			}
			return lineRoot.getElement(line).getStartOffset();
		} else
		{ // no shift
			return doc.getParagraphElement(offset).getStartOffset();
		}
	}

	/**
	 * Get the end position of the row right before the new-line character.
	 * @param c text component to operate on
	 * @param offset position in document where to start searching
	 * @param relLine shift offset forward/back by some amount of lines
	 * @return position of the end of the row or -1 for invalid position
	 */
	public static int getRowEnd(JTextComponent c, int offset)
			throws BadLocationException
	{
		Rectangle r = c.modelToView(offset);
		if (r == null)
		{
			return -1;
		}
		return c.viewToModel(new java.awt.Point(Integer.MAX_VALUE, r.y));
	}

	public static int getRowEnd(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		checkOffsetValid(doc, offset);
		return doc.getParagraphElement(offset).getEndOffset() - 1;
	}

	/**
	 * Get the identifier around the given position or null if there's no
	 * identifier around the given position. The identifier is not verified
	 * against SyntaxSupport.isIdentifier().
	 * @param c JTextComponent to work on
	 * @param offset position in document - usually the caret.getDot()
	 * @return the block (starting and ending position) enclosing the identifier
	 * or null if no identifier was found
	 */
	public static int[] getIdentifierBlock(JTextComponent c, int offset)
			throws BadLocationException
	{
		CharSequence id = null;
		int[] ret = null;
		Document doc = c.getDocument();
		int idStart = javax.swing.text.Utilities.getWordStart(c, offset);
		if (idStart >= 0)
		{
			int idEnd = javax.swing.text.Utilities.getWordEnd(c, idStart);
			if (idEnd >= 0)
			{
				id = DocumentUtilities.getText(doc, idStart, idEnd - idStart);
				ret = new int[] { idStart, idEnd };
				CharSequence trim = CharSequenceUtilities.trim(id);
				if (trim.length() == 0
						|| (trim.length() == 1 && !Character
								.isJavaIdentifierPart(trim.charAt(0))))
				{
					int prevWordStart = javax.swing.text.Utilities
							.getPreviousWord(c, offset);
					if (offset == javax.swing.text.Utilities.getWordEnd(c,
							prevWordStart))
					{
						ret = new int[] { prevWordStart, offset };
					} else
					{
						return null;
					}
				} else if ((id != null) && (id.length() != 0)
						&& (CharSequenceUtilities.indexOf(id, '.') != -1))
				{ // NOI18N
					int index = offset - idStart;
					int begin = CharSequenceUtilities.lastIndexOf(id
							.subSequence(0, index), '.');
					begin = (begin == -1) ? 0 : begin + 1; // first index after
					// the dot, if
					// exists
					int end = CharSequenceUtilities.indexOf(id, '.', index);
					end = (end == -1) ? id.length() : end;
					ret = new int[] { idStart + begin, idStart + end };
				}
			}
		}
		return ret;
	}

	/**
	 * Get the word around the given position .
	 * @param c component to work with
	 * @param offset position in document - usually the caret.getDot()
	 * @return the word.
	 */
	public static String getWord(JTextComponent c, int offset)
			throws BadLocationException
	{
		int[] blk = getIdentifierBlock(c, offset);
		Document doc = c.getDocument();
		return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
	}

	/**
	 * Tests whether the line contains no characters except the ending new-line.
	 * @param doc document to operate on
	 * @param offset position anywhere on the tested line
	 * @return whether the line is empty or not
	 */
	public static boolean isRowEmpty(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		Element lineElement = doc.getParagraphElement(offset);
		return (lineElement.getStartOffset() + 1 == lineElement.getEndOffset());
	}

	public static int getFirstNonEmptyRow(NotebookDocument doc, int offset,
			boolean downDir) throws BadLocationException
	{
		while (offset != -1 && isRowEmpty(doc, offset))
		{
			offset = getRowStart(doc, offset, downDir ? +1 : -1);
		}
		return offset;
	}

	public static void runInEventDispatchThread(Runnable r)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		} else
		{
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * Display the identity of the document together with the title property and
	 * stream-description property.
	 */
	public static String debugDocument(Document doc)
	{
		return "<"
				+ System.identityHashCode(doc) // NOI18N
				+ ", title='" + doc.getProperty(Document.TitleProperty)
				+ "', stream='"
				+ doc.getProperty(Document.StreamDescriptionProperty) + ", "
				+ doc.toString() + ">"; // NOI18N
	}

	/**
	 * Fetches the text component that currently has focus. It delegates to
	 * TextAction.getFocusedComponent().
	 * @return the component
	 */
	public static JTextComponent getFocusedComponent()
	{
		/** Fake action for getting the focused component */
		class FocusedComponentAction extends TextAction
		{
			FocusedComponentAction()
			{
				super("focused-component"); // NOI18N
			}

			/** adding this method because of protected final getFocusedComponent */
			JTextComponent getFocusedComponent2()
			{
				return getFocusedComponent();
			}

			public void actionPerformed(ActionEvent evt)
			{
			}
		}
		;
		if (focusedComponentAction == null)
		{
			focusedComponentAction = new FocusedComponentAction();
		}
		return ((FocusedComponentAction) focusedComponentAction)
				.getFocusedComponent2();
	}

	/**
	 * Get first view in the hierarchy that is an instance of the given class.
	 * It allows to skip various wrapper-views around the doc-view that holds
	 * the child views for the lines.
	 * 
	 * @param component component from which the root view is fetched.
	 * @param rootViewClass class of the view to return.
	 * @return view being instance of the requested class or null if there is
	 * not one.
	 */
	public static View getRootView(JTextComponent component, Class rootViewClass)
	{
		View view = null;
		TextUI textUI = (TextUI) component.getUI();
		if (textUI != null)
		{
			view = textUI.getRootView(component);
			while (view != null && !rootViewClass.isInstance(view)
					&& view.getViewCount() == 1 // must be wrapper view
			)
			{
				view = view.getView(0); // get the only child
			}
		}
		return view;
	}

	private static void checkOffsetValid(Document doc, int offset)
			throws BadLocationException
	{
		checkOffsetValid(offset, doc.getLength());
	}

	private static void checkOffsetValid(int offset, int limitOffset)
			throws BadLocationException
	{
		if (offset < 0 || offset > limitOffset)
		{
			throw new BadLocationException("Invalid offset=" + offset // NOI18N
					+ " not within <0, " + limitOffset + ">", // NOI18N
					offset);
		}
	}

	/** Annotates a Throwable if ErrorManager.INFORMATIONAL is loggable */
	public static void annotateLoggable(Throwable t)
	{
		if (isLoggable)
		{
			;// ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
			// t);
		}
	}

	public static int getRowFirstNonWhite(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		checkOffsetValid(doc, offset);
		Element lineElement = doc.getParagraphElement(offset);
		return getFirstNonWhiteFwd(doc, lineElement.getStartOffset(),
				lineElement.getEndOffset() - 1);
	}

	public static int getFirstNonWhiteFwd(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		return getFirstNonWhiteFwd(doc, offset, -1);
	}

	public static int getFirstNonWhiteFwd(NotebookDocument doc, int offset,
			int limitPos) throws BadLocationException
	{
		if (offset >= doc.getLength()) return -1;
		if (limitPos == -1) limitPos = offset + 256;
		limitPos = Math.min(doc.getLength(), limitPos);
		Segment seg = SegmentCache.getSharedSegment();
		doc.getText(offset, limitPos, seg);
		for (int i = 0; i < seg.array.length; i++)
			if (!Character.isWhitespace(seg.array[i]))
			{
				SegmentCache.releaseSharedSegment(seg);
				return i;
			}
		SegmentCache.releaseSharedSegment(seg);
		return getFirstNonWhiteFwd(doc, limitPos + 1, -1);
		// doc.find(new FinderFactory.NonWhiteFwdFinder(doc), offset,
		// limitPos);
	}

	public static void formatCell(JEditorPane editor, int pos)
	{
		if (editor != null)
		{
			Document d = editor.getDocument();
			if (!(d instanceof NotebookDocument)) return;
			NotebookDocument doc = (NotebookDocument) d;
			ScriptSupport sup = doc.getScriptSupport(pos);
			if (sup != null && sup.getFormatter() != null)
			{
				try
				{
					Element el = sup.getElement();
					String text = doc.getText(el.getStartOffset(), el
							.getEndOffset()
							- el.getStartOffset());
					String out = sup.getFormatter().format(text);
					doc.beginCompoundEdit("formatting");
					// keep the last \n to preserve the element structure
					doc.remove(el.getStartOffset(), el.getEndOffset()
							- el.getStartOffset() - 1);
					doc.insertString(el.getStartOffset(), out.substring(0, out
							.length() - 2), null);
					// remove the \n
					doc.remove(el.getEndOffset() - 1, 1);
					doc.endCompoundEdit();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				editor.setCaretPosition(pos);
			}
		}
	}

	public static void resetFormatter(JEditorPane editor, int pos)
	{
		if (editor != null)
		{
			NotebookDocument doc = (NotebookDocument) editor.getDocument();
			ScriptSupport sup = doc.getScriptSupport(pos);
			if (sup != null) sup.resetFormatter();
		}
	}

	static int[] getSelectionStartOffsets(NotebookUI target, String tab)
	{
	    int tab_length = (tab != null) ? tab.length() : 0;
		NotebookDocument doc = ((NotebookUI) target).getDoc();
		int start = target.getSelectionStart();
		int end = target.getSelectionEnd();
		Element el = doc.getUpperElement(start, ElementType.commonCell);
		if (el == null) return null;
		int st_idx = el.getElementIndex(start);
		int l_idx = el.getElementIndex(end);
		int[] res = new int[l_idx - st_idx + 1];
		try
		{
			for (int i = st_idx; i <= l_idx; i++)
			{
				Element inner = el.getElement(i);
				res[i - st_idx] = inner.getStartOffset();
				if (tab_length > 0)
				{
					String s = doc.getText(res[i - st_idx], tab_length);
					for (int j = 0; j < tab_length; j++)
						if (s.charAt(j) != tab.charAt(j)) return null;
				}
			}
		}
		catch (Exception ex)
		{
		}
		return res;
	}

	static int[] getSelectionStartOffsets(NotebookUI target)
	{
		return getSelectionStartOffsets(target, null);
	}

	static int getTabSpacesCount()
	{
		return ((Integer) AppConfig.getInstance().getProperty(
				AppConfig.SPACES_PER_TAB, 4)).intValue();
	}

	static String getTabSubstitute()
	{
		int count = getTabSpacesCount();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < count; i++)
			buf.append(' ');
		return buf.toString();
	}

	static void adjustScrollBar(JTextComponent c, int offset, Position.Bias bias)
	{
		try
		{
			Rectangle r = c.modelToView(offset);
			if (r != null)
			{
				Rectangle vis = c.getVisibleRect();
				r.y -= (vis.height / 2);
				r.height = vis.height;
				c.scrollRectToVisible(r);
			}
		}
		catch (BadLocationException ble)
		{
		}
	}

	/**
	 * Get the last non-white character on the line. The document.isWhitespace()
	 * is used to test whether the particular character is white space or not.
	 * @param doc document to operate on
	 * @param offset position in document anywhere on the line
	 * @return position of the last non-white char on the line or -1 if there's
	 * no non-white character on that line.
	 */
	public static int getRowLastNonWhite(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		checkOffsetValid(doc, offset);
		Element lineElement = doc.getParagraphElement(offset);
		return getFirstNonWhiteBwd(doc, lineElement.getEndOffset() - 1,
				lineElement.getStartOffset());
	}

	/**
	 * Get first non-white character in document in backward direction. The
	 * character right before the character at position offset will be searched
	 * as first.
	 * @param doc document to operate on
	 * @param offset position in document where to start searching
	 * @param limitPos position in document (lower or equal than offset) where
	 * the search will stop reporting unsuccessful search by returning -1
	 * @return position of the first non-white character or -1
	 */
	public static int getFirstNonWhiteBwd(NotebookDocument doc, int offset,
			int limitPos) throws BadLocationException
	{
		return find(doc, new FinderFactory.NonWhiteBwdFinder(doc), offset,
							 limitPos);
	}

	/**
	 * Tests whether the line contains only whitespace characters.
	 * @param doc document to operate on
	 * @param offset position anywhere on the tested line
	 * @return whether the line is empty or not
	 */
	public static boolean isRowWhite(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		Element lineElement = doc.getParagraphElement(offset);
		// offset = doc.find(new FinderFactory.NonWhiteFwdFinder(doc),
		// lineElement.getStartOffset(), lineElement.getEndOffset() - 1);
		// return (offset == -1);
		// TODO:???
		return false;
	}

	public static int find(NotebookDocument doc, Finder finder, int startPos,
			int limitPos) throws BadLocationException
	{
		int docLen = doc.getLength();
		if (limitPos == -1)
		{
			limitPos = docLen;
		}
		if (startPos == -1)
		{
			startPos = docLen;
		}
		finder.reset();
		if (startPos == limitPos)
		{
			return -1;
		}
		Segment text = SegmentCache.getSharedSegment();
		try
		{
			int gapStart = 0;
			int pos = startPos; // pos at which the search starts (continues)
			boolean fwdSearch = (startPos <= limitPos); // forward search
			if (fwdSearch)
			{
				while (pos >= startPos && pos < limitPos)
				{
					int p0; // low bound
					int p1; // upper bound
					if (pos < gapStart)
					{ // part below gap
						p0 = startPos;
						p1 = Math.min(gapStart, limitPos);
					} else
					{ // part above gap
						p0 = Math.max(gapStart, startPos);
						p1 = limitPos;
					}
					doc.getText(p0, p1 - p0, text);
					pos = finder.find(p0 - text.offset, text.array,
							text.offset, text.offset + text.count, pos,
							limitPos);
					if (finder.isFound())
					{
						return pos;
					}
				}
			} else
			{ // backward search limitPos < startPos
				pos--; // start one char below the upper bound
				while (limitPos <= pos && pos <= startPos)
				{
					int p0; // low bound
					int p1; // upper bound
					if (pos < gapStart)
					{ // part below gap
						p0 = limitPos;
						p1 = Math.min(gapStart, startPos);
					} else
					{ // part above gap
						p0 = Math.max(gapStart, limitPos);
						p1 = startPos;
					}
					doc.getText(p0, p1 - p0, text);
					pos = finder.find(p0 - text.offset, text.array,
							text.offset, text.offset + text.count, pos,
							limitPos);
					if (finder.isFound())
					{
						return pos;
					}
				}
			}
			return -1; // position outside bounds => not found
		}
		finally
		{
			SegmentCache.releaseSharedSegment(text);
		}
	}

	public static int getWordStart(JTextComponent c, int offset)
			throws BadLocationException
	{
		return getWordStart((NotebookDocument) c.getDocument(), offset);
	}

	public static int getWordStart(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		return find(doc, new FinderFactory.PreviousWordBwdFinder(doc, false,
				true), offset, 0);
	}

	public static int getWordEnd(JTextComponent c, int offset)
			throws BadLocationException
	{
		return getWordEnd((NotebookDocument) c.getDocument(), offset);
	}

	public static int getWordEnd(NotebookDocument doc, int offset)
			throws BadLocationException
	{
		int ret = find(doc,
				new FinderFactory.NextWordFwdFinder(doc, false, true), offset,
				-1);
		return (ret > 0) ? ret : doc.getLength();
	}
}
