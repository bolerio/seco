/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * ChunkCache.java - Intermediate layer between token lists from a TokenMarker
 * and what you see on screen
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2005 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package seco.notebook.syntax;

import java.util.*;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;

import seco.notebook.NotebookUI;
import seco.notebook.util.SegmentCache;


/**
 * Manages low-level text display tasks.
 * 
 * @author Slava Pestov
 * @version $Id: ChunkCache.java,v 1.4 2006/09/01 16:18:27 bizi Exp $
 */
public class ChunkCache
{
	private LineInfo[] lineInfo;
	private ArrayList<Chunk> out;
	private int firstInvalidLine;
	private int lastScreenLine;
	private boolean needFullRepaint;
	private DisplayTokenHandler tokenHandler;
	protected Document doc;
	protected Element el;
	protected LineManager lineMgr;
	protected ScriptSupport manager;

	public ChunkCache(ScriptSupport manager)
	{
		this.manager = manager;
		this.el = manager.getElement();
		this.lineMgr = manager.getLineMgr();
		out = new ArrayList<Chunk>();
		tokenHandler = new DisplayTokenHandler();
		recalculateVisibleLines();// el.getElementCount());
	}

	void recalculateVisibleLines() // int total)
	{
		LineInfo[] newLineInfo = new LineInfo[el.getElementCount()]; // total];
		int start;
		if (lineInfo == null)
			start = 0;
		else
		{
			start = Math.min(lineInfo.length, newLineInfo.length);
			System.arraycopy(lineInfo, 0, newLineInfo, 0, start);
		}
		for (int i = start; i < newLineInfo.length; i++)
			newLineInfo[i] = new LineInfo();
		lineInfo = newLineInfo;
		lastScreenLine = -1; // lastScreenLineP = -1;
	}

	void invalidateAll()
	{
		firstInvalidLine = 0;
		lastScreenLine = -1;
	}

	void invalidateChunksFrom(int screenLine)
	{
		firstInvalidLine = Math.min(screenLine, firstInvalidLine);
		if (screenLine <= lastScreenLine) lastScreenLine = -1;
	}

	void invalidateChunksFromPhys(int physicalLine)
	{
		for (int i = 0; i < firstInvalidLine; i++)
		{
			LineInfo info = lineInfo[i];
			if (info.physicalLine == -1 || info.physicalLine >= physicalLine)
			{
				firstInvalidLine = i;
				if (i <= lastScreenLine) lastScreenLine = -1;
				break;
			}
		}
	}

	public LineInfo getLineInfo(int screenLine)
	{
		if (updateChunksUpTo(screenLine)) return lineInfo[screenLine];
		if (lineInfo != null && screenLine < lineInfo.length)
			return lineInfo[screenLine];
		return null;
	}

	/**
	 * The needFullRepaint variable becomes true when the number of screen lines
	 * in a physical line changes.
	 */
	boolean needFullRepaint()
	{
		boolean retVal = needFullRepaint;
		needFullRepaint = false;
		return retVal;
	}

	LineInfo[] getLineInfosForPhysicalLine(int physicalLine)
	{
		out.clear();
		lineToChunkList(physicalLine, out);
		if (out.size() == 0) out.add(null);
		ArrayList<LineInfo> returnValue = new ArrayList<LineInfo>(out.size());
		getLineInfosForPhysicalLine(physicalLine, returnValue);
		return (LineInfo[]) returnValue.toArray(new LineInfo[out.size()]);
	}

	private void getLineInfosForPhysicalLine(int physicalLine, List<LineInfo> list)
	{
		for (int i = 0; i < out.size(); i++)
		{
			Chunk chunks = out.get(i);
			LineInfo info = new LineInfo();
			info.physicalLine = physicalLine;
			if (i == 0)
			{
				info.firstSubregion = true;
				info.offset = 0;
			} else
				info.offset = chunks.offset;
			if (i == out.size() - 1)
			{
				info.lastSubregion = true;
				info.length = (el.getElement(physicalLine).getEndOffset() - el
						.getElement(physicalLine).getStartOffset())// textArea.getLineLength(physicalLine)
						- info.offset + 1;
			} else
			{
				info.length = out.get(i + 1).offset - info.offset;
			}
			info.chunks = chunks;
			list.add(info);
		}
	}

	/**
	 * Find a valid line closest to the last screen line.
	 */
	private int getFirstScreenLine()
	{
		for (int i = firstInvalidLine - 1; i >= 0; i--)
		{
			if (lineInfo[i].lastSubregion) return i + 1;
		}
		return 0;
	}

	/**
	 * Return a physical line number.
	 */
	private int getUpdateStartLine(int firstScreenLine)
	{
		// for the first line displayed, take its physical line to be
		// the text area's first physical line
		if (firstScreenLine == 0)
		{
			return 0;
		}
		// otherwise, determine the next visible line
		else
		{
			int prevPhysLine = lineInfo[firstScreenLine - 1].physicalLine;
			// if -1, the empty space at the end of the text area
			// when the buffer has less lines than are visible
			if (prevPhysLine == -1)
				return -1;
			else
			{
				return (prevPhysLine != el.getElementCount() - 1) ? prevPhysLine + 1
						: -1; // TODO:???textArea.displayManager
				// .getNextVisibleLine(prevPhysLine);
			}
		}
	}

	private boolean updateChunksUpTo(int lastScreenLine)
	{
		if (lineInfo == null) return false;
		// this method is a nightmare
		if (lastScreenLine >= lineInfo.length) return false;// throw new
															// ArrayIndexOutOfBoundsException(lastScreenLine);
		// if one line's chunks are invalid, remaining lines are also
		// invalid
		if (lastScreenLine < firstInvalidLine) return false;
		int firstScreenLine = getFirstScreenLine();
		int physicalLine = getUpdateStartLine(firstScreenLine);
		//System.out.println("ChunkCache - Updating chunks from "
		//		+ firstScreenLine + " to " + lastScreenLine);
		// Note that we rely on the fact that when a physical line is
		// invalidated, all screen lines/subregions of that line are
		// invalidated as well. See below comment for code that tries
		// to uphold this assumption.
		out.clear();
		int offset = 0;
		int length = 0;
		Integer lo =(Integer) manager.getDocument().getProperty(NotebookUI.LAST_VISIBLE_OFFSET);
		int lastOffset = (lo != null) ? lo.intValue() : el.getEndOffset();
		int lastDispLine = (el.getEndOffset() < lastOffset) ?
				manager.offsetToLine(lastOffset) : el.getElementCount();
		//System.out.println("ChunkCache " + lo + " to " + lastDispLine);
		
		for (int i = firstScreenLine; i <= lastScreenLine; i++)
		{
			LineInfo info = lineInfo[i];
			Chunk chunks;
			// get another line of chunks
			// unless this is the first time, increment
			// the line number
			if (physicalLine != -1 && i != firstScreenLine)
			{
				physicalLine = (physicalLine != lastDispLine) ? physicalLine + 1
						: -1;// ???textArea.displayManager
				// .getNextVisibleLine(physicalLine);
			}
			// empty space
			if (physicalLine == -1)
			{
				info.chunks = null;
				info.physicalLine = -1;
				// fix the bug where the horiz.
				// scroll bar was not updated
				// after creating a new file.
				info.width = 0;
				continue;
			}
			// chunk the line.
			lineToChunkList(physicalLine, out);
			info.firstSubregion = true;
			// if the line has no text, out.size() == 0
			if (out.size() == 0)
			{
				chunks = null;
				offset = 0;
				length = 1;
			}
			// otherwise, the number of subregions
			else
			{
				chunks = out.get(0);
				out.remove(0);
				offset = chunks.offset;
				if (out.size() != 0)
					length = out.get(0).offset - offset;
				else
					length = getLineLength(physicalLine) - offset + 1;
			}
			boolean lastSubregion = (out.size() == 0);
			if (i == lastScreenLine && lastScreenLine != lineInfo.length - 1)
			{
				/*
				 * if the user changes the syntax token at the end of a line,
				 * need to do a full repaint.
				 */
				if (tokenHandler.getLineContext() != info.lineContext)
				{
					lastScreenLine++;
					needFullRepaint = true;
				}
				/*
				 * If this line has become longer or shorter (in which case the
				 * new physical line number is different from the cached one) we
				 * need to: - continue updating past the last line - advise the
				 * text area to repaint On the other hand, if the line wraps
				 * beyond lastScreenLine, we need to keep updating the chunk
				 * list to ensure proper alignment of invalidation flags (see
				 * start of method)
				 */
				else if (info.physicalLine != physicalLine
						|| info.lastSubregion != lastSubregion)
				{
					lastScreenLine++;
					needFullRepaint = true;
				}
				/*
				 * We only cache entire physical lines at once; don't want to
				 * split a physical line into screen lines and only have some
				 * valid.
				 */
				else if (out.size() != 0) lastScreenLine++;
			}
			info.physicalLine = physicalLine;
			info.lastSubregion = lastSubregion;
			info.offset = offset;
			info.length = length;
			info.chunks = chunks;
			info.lineContext = tokenHandler.getLineContext();
		}
		firstInvalidLine = Math.max(lastScreenLine + 1, firstInvalidLine);
		return true;
	}

	int getLineLength(int physicalLine)
	{
		return el.getElement(physicalLine).getEndOffset()
				- el.getElement(physicalLine).getStartOffset();
	}
	public TabExpander expander;

	private void lineToChunkList(int physicalLine, List<Chunk> out)
	{
		tokenHandler.init(manager.getSyntaxStyles(), null, expander, out, 0.0f);
		markTokens(physicalLine, tokenHandler);
	}

	/**
	 * Returns the syntax tokens for the specified line.
	 * @param lineIndex The line number
	 * @param tokenHandler The token handler that will receive the syntax tokens
	 * @since jEdit 4.1pre1
	 */
	private void markTokens(int lineIndex, TokenHandler tokenHandler)
	{
		Segment seg = SegmentCache.getSharedSegment();
		//unbelievable, but this was reported to happen...
		if(seg == null) return;
		if (lineIndex < 0 || lineIndex >= lineMgr.getLineCount()) return;// throw
																			// new
																			// ArrayIndexOutOfBoundsException(lineIndex);
		int firstInvalidLineContext = lineMgr.getFirstInvalidLineContext();
		int start;
		if (firstInvalidLineContext == -1)
		{
			start = lineIndex;
		} else
		{
			start = Math.min(firstInvalidLineContext, lineIndex);
		}
		//System.out.println("tokenize from " + start + " to " + lineIndex);
		TokenMarker.LineContext oldContext = null;
		TokenMarker.LineContext context = null;
		for (int i = start; i <= lineIndex; i++)
		{
			getLineText(i, seg);
			oldContext = lineMgr.getLineContext(i);
			TokenMarker.LineContext prevContext = ((i == 0) ? null : lineMgr
					.getLineContext(i - 1));
			context = manager.getTokenMarker()
					.markTokens(
							prevContext,
							(i == lineIndex ? tokenHandler
									: DummyTokenHandler.INSTANCE), seg);
			lineMgr.setLineContext(i, context);
		}
		SegmentCache.releaseSharedSegment(seg);
		int lineCount = lineMgr.getLineCount();
		if (lineCount - 1 == lineIndex)
			lineMgr.setFirstInvalidLineContext(-1);
		else if (oldContext != context)
			lineMgr.setFirstInvalidLineContext(lineIndex + 1);
		else if (firstInvalidLineContext == -1)
			/* do nothing */;
		else
		{
			lineMgr.setFirstInvalidLineContext(Math.max(
					firstInvalidLineContext, lineIndex + 1));
		}
	}

	public void getLineText(int line, Segment seg)
	{
		if (line < 0 || line >= lineMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);
		try
		{
			Element el1 = el.getElement(line);
			manager.doc.getText(el1.getStartOffset(), el1.getEndOffset()
					- el1.getStartOffset(), seg);
		}
		catch (Exception ex)
		{
			// ex.printStackTrace();
		}
	}

	public static class LineInfo
	{
		int physicalLine;
		int offset;
		int length;
		boolean firstSubregion;
		boolean lastSubregion;
		public Chunk chunks;
		int width;
		TokenMarker.LineContext lineContext;
	}
}
