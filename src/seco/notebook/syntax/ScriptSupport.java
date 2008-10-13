/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax;

import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.*;

import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.util.DocumentUtilities;

public abstract class ScriptSupport
{
	protected NotebookDocument doc;
	protected Element el;
	protected LineManager lineMgr;
	protected ChunkCache chunkCache;
	protected TokenMarker tokenMarker;
	protected NBParser parser;

	public abstract NBParser getParser();

	public abstract CompletionProvider[] getCompletionProviders();

	public abstract List<Mode> getModes();

	public abstract String getModeName();

	public abstract String getScriptEngineName();

	public Formatter getFormatter()
	{
		return null;
	}

	public void resetFormatter()
	{
	}

	public void init(NotebookDocument doc, Element el)
	{
		this.doc = doc;
		this.el = el;
		lineMgr = new LineManager();
		lineMgr.contentInserted(0, 0, el.getElementCount(), el.getEndOffset()
				- el.getStartOffset());
		chunkCache = new ChunkCache(this);
		tokenMarker = NotebookUI.getMode(getModeName()).getTokenMarker();
		notifyParser();
	}

	public ChunkCache getChunkCache()
	{
		return chunkCache;
	}

	public ChunkCache.LineInfo getLineInfo(int offset)
	{
		return chunkCache.getLineInfo(offsetToLine(offset));
	}

	public int offsetToLine(int offset)
	{
		return el.getElementIndex(offset);
	}

	public int lineToOffset(int line, int col)
	{
		return el.getElement(line).getStartOffset() + col;
	}

	public int[] offsetToLineCol(int offset)
	{
		int[] res = new int[2];
		res[0] = el.getElementIndex(offset);
		res[1] = offset - el.getElement(res[0]).getStartOffset();
		return res;
	}

	public boolean isCommentOrLiteral(int offset)
	{
		int[] lineCol = offsetToLineCol(offset - 1);
		ChunkCache.LineInfo lineInfo = getChunkCache().getLineInfo(lineCol[0]);
		if (lineInfo.chunks != null)
		{
			Chunk chunks = Chunk.getChunkAtOffset(lineInfo.chunks, lineCol[1]);
			// System.out.println("isCommentOrLiteral: " + chunks.str + ":" +
			// chunks.id);
			if (chunks != null)
			{
				if (chunks.id >= Chunk.COMMENT1 && chunks.id <= Chunk.COMMENT4)
					return true;
				if (chunks.id >= Chunk.LITERAL1 && chunks.id <= Chunk.LITERAL4)
				{
					// System.out.println("isCommentOrLiteral: " + chunks.offset
					// + ":" + chunks.length +
					// ":" + lineCol[1] + ":" + offset);
					return chunks.offset + chunks.length != lineCol[1] + 1;
				}
			}
		}
		return false;
	}

	public String getCommandBeforePt(int caretOffset)
			throws BadLocationException
	{
		return getCommandBeforePt(caretOffset, true);
	}

	public String getCommandBeforePt(int caretOffset, boolean lookahead)
			throws BadLocationException
	{
		// before pt + 1 to get in prev chunk
		int[] lineCol = offsetToLineCol(caretOffset - 2);
		ChunkCache.LineInfo lineInfo = getChunkCache().getLineInfo(lineCol[0]);
		if (lineInfo.chunks != null)
		{
			Chunk chunks = Chunk.getChunkAtOffset(lineInfo.chunks, lineCol[1]);
			if (chunks != null)
				if (chunks.id >= Chunk.LITERAL1 && chunks.id <= Chunk.LITERAL4)
					return chunks.str;
		}
		Element el1 = el.getElement(lineCol[0]);
		CharSequence text = DocumentUtilities.getText(doc,
				el1.getStartOffset(), caretOffset - el1.getStartOffset() - 1);
		int closePars = 0;
		for (int i = text.length() - 1; i > 0; i--)
		{
			char c = text.charAt(i);
			if (c == '-' || c == '+' || c == '=' || c == '<' || c == '>'
					|| c == ':' || c == ';' || (c == '(' && closePars == 0))
				return text.subSequence(i + 1, text.length()).toString();
			if (c == ')') closePars++;
			if (c == '(') closePars--;
			if (Character.isWhitespace(c) && closePars == 0)
			{
				String cmd = text.subSequence(i + 1, text.length()).toString();
				if (lookahead)
				{
					String bef = getCommandBeforePt(caretOffset - cmd.length()
							- 1, false);
					// System.out.println("Lookahead: " + bef);
					if ("new".equals(bef)) return "new " + cmd;
					// if ("import".equals(bef)) return "import " + cmd;
				}
				return cmd;
			}
		}
		return text.toString();
	}

	public TokenMarker getTokenMarker()
	{
		return tokenMarker;
	}

	public Element getElement()
	{
		return el;
	}

	public NotebookDocument getDocument()
	{
		return doc;
	}

	public LineManager getLineMgr()
	{
		return lineMgr;
	}
	private Segment seg = new Segment();

	// boolean update_in_progress = false;
	public void insertUpdate(DocumentEvent e)
	{
		// update_in_progress = true;
		int offset = e.getOffset() - el.getStartOffset();
		int numLines = 0;
		try
		{
			doc.getText(e.getOffset(), e.getLength(), seg);
			for (int i = 0; i < seg.count; i++)
				if (seg.array[seg.offset + i] == '\n') numLines++;
		}
		catch (BadLocationException ex)
		{
			ex.printStackTrace();
		}
		int line = el.getElementIndex(e.getOffset());
		// System.out.println("ScriptSupport.insertUpdate:" + line +":"+ offset
		// + ":" + numLines + ":" + e.getLength());
		lineMgr.contentInserted(line, offset, numLines, e.getLength());
		if (numLines > 0) chunkCache.recalculateVisibleLines();
		chunkCache.invalidateChunksFromPhys(line);
		Completion.get().insertUpdate(e, this);
		if (getParser() != null) getParser().insertUpdate(e);
		// update_in_progress = false;
	}

	public void removeUpdate(DocumentEvent e)
	{
		int offset = e.getOffset() - el.getStartOffset();
		int numLines = 0;
		int line = el.getElementIndex(e.getOffset());
		DocumentEvent.ElementChange ch = e.getChange(el);
		if (ch != null)
			numLines = ch.getChildrenRemoved().length
					- ch.getChildrenAdded().length;
		// System.out.println("ScriptSupport.removeUpdate:" + line + ":"+ offset
		// + ":" + numLines + ":" + e.getLength());
		lineMgr.contentRemoved(line, offset, numLines, e.getLength());
		if (numLines > 0) chunkCache.recalculateVisibleLines(); // el.getElementCount()
		// - numLines);
		chunkCache.invalidateChunksFromPhys(line);
		Completion.get().removeUpdate(e);
		if (getParser() != null) getParser().removeUpdate(e);
	}
	protected boolean marked = false;
	protected ErrorMark mark;

	public final void unMarkErrors()
	{/*
		Future f = executor.submit(new Runnable() {
			public void run()
			{
				_unMarkErrors();
			}
		});
		try
		{
			f.get();
			doc.updateElement(el);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}*/
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				_unMarkErrors();
			}
		});
	}

	protected void _unMarkErrors()
	{
		if (!marked) return;
		marked = false;
		mark = null;
		// System.err.println("unMarkErrors(): " + getElement());
		doc.updateElement(el);
	}
	
	
	public final void markError(final ErrorMark mark)
	{
		SwingUtilities.invokeLater(new Runnable() {
		  public void run()
			{
				_markError(mark);
			}
		});
		/*
		Future f = executor.submit(new Callable() {
			public Boolean call()
			{
				return new Boolean(_markError(mark));
			}
		});
		try
		{
			if((Boolean)f.get())
			   doc.updateElement(el);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}*/
	}

	protected boolean _markError(ErrorMark mark)
	{
		// System.err.println("markError: " + mark.line +
		// ", column " + mark.column + ":" + mark.msg + ":" +
		// getChunkCache().lineMgr.getLineCount());
		if (getChunkCache().lineMgr.getLineCount() <= mark.line) return false;
		this.mark = mark;
		ChunkCache.LineInfo lineInfo = getChunkCache().getLineInfo(mark.line);
		if (lineInfo != null && lineInfo.chunks != null)
		{
			Chunk chunks = Chunk.getChunkAtOffset(lineInfo.chunks, mark.column);
			if (chunks != null)
			{
				// System.err.println("Found - chunk: " + chunks.str);
				chunks.err = true;
				marked = true;
				doc.updateElement(el);
				return true;
			}
		}
		return false;
	}

	public String getErrorMsg(int off)
	{
		if (mark != null)
		{
			int i = offsetToLine(off);
			// System.out.println("getErrorMsg: " + i + ":" + mark.line + ":" +
			// mark.msg);
			if (i == mark.line) return mark.msg;
		}
		return null;
	}
	Chunk bracket;
	static char[] op_bracks = new char[] { '(', '{', '[' };
	static char[] cl_bracks = new char[] { ')', '}', ']' };

	private int getBracketIdx(char c, char[] arr)
	{
		for (int i = 0; i < arr.length; i++)
			if (c == arr[i]) return i;
		return -1;
	}

	public final void markBracket(final int offset)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				_markBracket(offset);
			}
		});
	}

	protected void _markBracket(int offset)
	{
		int idx = -1;
		boolean up = false;
		try
		{
			String c = getDocument().getText(offset - 1, 1);
			idx = getBracketIdx(c.charAt(0), op_bracks);
			if (idx == -1)
			{
				idx = getBracketIdx(c.charAt(0), cl_bracks);
				up = true;
			}
		}
		catch (Throwable ex)
		{
		}
		if (idx < 0)
		{
			_unMarkBracket(true);
			return;
		}
		int lineCol[] = offsetToLineCol(offset - 1);
		int lastLine = offsetToLine(getElement().getEndOffset());
		ChunkCache.LineInfo lineInfo = (lineCol[0] >= 0) ? getChunkCache()
				.getLineInfo(lineCol[0]) : null;
		if (lineInfo == null || lineInfo.chunks == null)
		{
			_unMarkBracket(true);
			return;
		}
		Chunk chunks = Chunk.getChunkAtOffset(lineInfo.chunks, lineCol[1]);
		int curr_line = lineCol[0];
		// System.out.println("markBracket: " + lineInfo.chunks + ":" +
		// (chunks == null ? null: chunks.str) + ":" + curr_line);
		if (chunks == null || chunks.str == null || curr_line == -1)
		{
			_unMarkBracket(true);
			return;
		}
		int i = 1;
		do
		{
			chunks = (up) ? (Chunk) chunks.prev : (Chunk) chunks.next;
			// move one line up/down
			if (chunks == null)
				if ((up && (0 < curr_line)) || (!up && curr_line < lastLine))
				{
					//System.out.println("markBracket-nl: " + up + ":" +
					//		lastLine + ":" + curr_line);
					curr_line = (up) ? curr_line - 1 : curr_line + 1;
					ChunkCache.LineInfo lineInfo1 = getChunkCache()
							.getLineInfo(curr_line);
					if (lineInfo1.chunks != null)
					{
						chunks = (up) ? Chunk.getChunkAtOffset(
								lineInfo1.chunks, getChunkCache()
										.getLineLength(curr_line) - 1) : Chunk
								.getChunkAtOffset(lineInfo1.chunks, 0);
					}
				}
			if (chunks != null && chunks.str != null)
			{
				if (chunks.str.charAt(0) == ((up) ? cl_bracks[idx]
						: op_bracks[idx])) i++;
				// System.err.println("Chunk: " + chunks.str + ":"
				// + i + ":" + chunks.offset + ":" + curr_line +
				// (up ? chunks.prev : chunks.next));
				if (chunks.str.charAt(0) == ((up) ? op_bracks[idx]
						: cl_bracks[idx]))
				{
					if (i != 1)
						i--;
					else
					{
						// System.err.println("Bracket Found: " + curr_line);
						_unMarkBracket(false);
						bracket = chunks;
						bracket.bracket = true;
						doc.updateElement(el);
						return;
					}
				}
			}
		} while (chunks != null);
		_unMarkBracket(true);
	}

	public final void unMarkBracket(final boolean update)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				_unMarkBracket(update);
			}
		});
	}

	protected void _unMarkBracket(boolean update)
	{
		if (bracket != null && bracket.bracket == true)
		{
			bracket.bracket = false;
			if (update) doc.updateElement(el);
		}
	}

	public SyntaxStyle[] getSyntaxStyles()
	{
		return doc.getSyntaxStyles(this);
	}

	private void notifyParser()
	{
		// TODO: in startup while many parsers runs in parrallel
		// strange errors get thrown, probably its a BSH engine bug...
		if (getParser() != null) getParser().update();
	}

	public static class ErrorMark
	{
		int line;
		int column;
		String msg;

		public ErrorMark(String msg, int line, int column)
		{
			this.line = line;
			this.column = column;
			this.msg = msg;
		}
	}
}
