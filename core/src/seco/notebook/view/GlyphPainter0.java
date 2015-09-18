/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.GlyphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

import seco.notebook.syntax.Chunk;
import seco.notebook.syntax.ChunkCache;
import seco.notebook.syntax.ScriptSupport;
import seco.util.SegmentCache;


/**
 * A class to perform rendering of the glyphs. This can be implemented to be
 * stateless, or to hold some information as a cache to facilitate faster
 * rendering and model/view translation. At a minimum, the GlyphPainter allows a
 * View implementation to perform its duties independent of a particular version
 * of JVM and selection of capabilities (i.e. shaping for i18n, etc).
 * <p>
 * This implementation is intended for operation under the JDK1.1 API of the
 * Java Platform. Since the Java 2 SDK is backward compatible with JDK1.1 API,
 * this class will also function on Java 2. The Java 2 SDK introduces improved
 * API for rendering text however, so the GlyphPainter2 is recommended for the
 * Java 2 SDK.
 * 
 * @author Timothy Prinzing
 * @version 1.13 01/23/03
 * @see GlyphView
 */
class GlyphPainter0 extends GlyphView.GlyphPainter
{
	/**
	 * Determine the span the glyphs given a start location (for tab expansion).
	 */
	public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x)
	{
		sync(v);
		ScriptSupport sup = ((InlineView) v).getScriptSupport();
		int[] info = (sup != null) ? sup.offsetToLineCol(p1-1) : null;
		return getWidth(v, sup, info, p0, p1, e, x);
	}
	
	//the redundant sup and info[] params are here only for performance reasons
	private float getWidth(GlyphView v, ScriptSupport sup, int[] info,  
			 int p0, int p1, TabExpander e, float x)
	{
		float width = 0;
		boolean multiline = !(p0 == v.getStartOffset() && p1 == v.getEndOffset()
				&& (p0 == v.getElement().getStartOffset() && p1 == v
						.getElement().getEndOffset()));
		if (sup != null && !multiline && info!= null)
		{
			ChunkCache.LineInfo lineInfo = sup.getChunkCache().getLineInfo(
					info[0]);
			if (lineInfo != null)
			{
				// in some very obscure situation col become -1
				// and Notebook fall in infinite loop
				if (info[1] == -1) return 0;
				width = Chunk.offsetToX(lineInfo.chunks, info[1]);
				//width = (!multiline) ?
				//		(int) Chunk.offsetToX(lineInfo.chunks, info[1]):
				//		(int) Chunk.offsetToX(lineInfo.chunks, info[1] - (p1 - p0), info[1]);
				return width;
			}
		}
				
		Segment text = v.getText(p0, p1);
		width = Utilities.getTabbedTextWidth(text, metrics, (int) x, e, p0);
		SegmentCache.releaseSharedSegment(text);
		return width;
	}

	public float getHeight(GlyphView v)
	{
		sync(v);
		return metrics.getHeight();
	}

	/**
	 * Fetches the ascent above the baseline for the glyphs corresponding to the
	 * given range in the model.
	 */
	public float getAscent(GlyphView v)
	{
		sync(v);
		return metrics.getAscent();
	}

	/**
	 * Fetches the descent below the baseline for the glyphs corresponding to
	 * the given range in the model.
	 */
	public float getDescent(GlyphView v)
	{
		sync(v);
		return metrics.getDescent();
	}

	/**
	 * Paints the glyphs representing the given range.
	 */
	public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1)
	{
		sync(v);
		TabExpander expander = v.getTabExpander();
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
				.getBounds();
		ScriptSupport sup =((InlineView) v).getScriptSupport();
		int[] info = (sup != null)? sup.offsetToLineCol(p0) : null;
		// determine the x coordinate to render the glyphs
		int x = alloc.x;
		int p = v.getStartOffset();
		g.setFont(metrics.getFont());
		
		if (p != p0)
			x += getWidth(v, sup, info, p, p0, expander, x);
		
		// determine the y coordinate to render the glyphs
		int y = alloc.y + metrics.getHeight() - metrics.getDescent();
		int p2 = v.getElement().getStartOffset();
		if (sup != null)
		{
			paintSyntaxLine(g, expander, sup, info[0], x, y, p0 - p2, p1 - p2);
		} else{
			Segment text = v.getText(p0, p1);
//			System.out.println("drawing text " + text);
			Utilities.drawTabbedText(text, x, y, g, expander, p0);
			SegmentCache.releaseSharedSegment(text);
		}
		
	}

	protected void paintSyntaxLineNoWrap(Segment text, Graphics gfx,
			TabExpander expander, ScriptSupport sup, int line, int x, int y,
			int p)
	{
		ChunkCache cache = sup.getChunkCache();
		cache.expander = expander;
		ChunkCache.LineInfo lineInfo = cache.getLineInfo(line);
		if (lineInfo.chunks != null)
		{
			Chunk.paintChunkListNoWrap(lineInfo.chunks, (Graphics2D) gfx,
					(float) x, (float) y, true);
		}
	}

	protected void paintSyntaxLine(Graphics gfx,
			TabExpander expander, ScriptSupport sup, int line, int x, int y,
			int p, int p1)
	{
		ChunkCache cache = sup.getChunkCache();
		cache.expander = expander;
		ChunkCache.LineInfo lineInfo = cache.getLineInfo(line);
		if (lineInfo != null && lineInfo.chunks != null)
		{
		    Chunk.paintChunkList(lineInfo.chunks, (Graphics2D) gfx, (float) x,
					(float) y, p, p1);
		}
	}

	public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) throws BadLocationException
	{
		
		sync(v);
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		int p0 = v.getStartOffset();
		int p1 = v.getEndOffset();
		TabExpander expander = v.getTabExpander();
		
		if (pos == p1)
		{
			// The caller of this is left to right and borders a right to
			// left view, return our end location.
			return new Rectangle(alloc.x + alloc.width, alloc.y, 0, metrics
					.getHeight());
		}
		if ((pos >= p0) && (pos <= p1))
		{
			int width = 0;
			ScriptSupport sup = ((InlineView) v).getScriptSupport();
			int[] info = (sup != null) ? sup.offsetToLineCol(pos) : null;
			boolean multiline = !(p0 == v.getElement().getStartOffset() && p1 == v.getElement().getEndOffset());
			if (sup != null)
			{
//				if (multiline)
//					System.out.println("multiline on " + v.getElement().toString());
				ChunkCache.LineInfo lineInfo = sup.getChunkCache().getLineInfo(info[0]);
				if (lineInfo != null)
				{
					width = (!multiline) ?
						(int) Chunk.offsetToX(lineInfo.chunks, info[1]):
						(int) Chunk.offsetToX(lineInfo.chunks, info[1] - (pos - p0), info[1]);
					//if(multiline){
					//		System.out.println("GlyphPainter -modelToView: " + pos +
					//				":" + p0 + ":" + p1 + ":" + v.getStartOffset() + ":" + (info[1] - (pos - p0)) +
					//				":" + v.getEndOffset() + ":" + info[0] + ":" + info[1] +":"+ width + ":" + alloc);
					//	}
					return new Rectangle(alloc.x + width, alloc.y, 0, metrics.getHeight());
				}
			}
			
			// determine range to the left of the position
			Segment text = v.getText(p0, pos);
			width = Utilities.getTabbedTextWidth(text, metrics, alloc.x, expander, p0);
			SegmentCache.releaseSharedSegment(text);
			return new Rectangle(alloc.x + width, alloc.y, 0, metrics.getHeight());
		}
		throw new BadLocationException("modelToView - can't convert", p1);
	}

	/**
	 * Provides a mapping from the view coordinate space to the logical
	 * coordinate space of the model.
	 * 
	 * @param v the view containing the view coordinates
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param a the allocated region to render into
	 * @param biasReturn always returns <code>Position.Bias.Forward</code> as
	 * the zero-th element of this array
	 * @return the location within the model that best represents the given
	 * point in the view
	 * @see View#viewToModel
	 */
	public int viewToModel(GlyphView v, float x, float y, Shape a,
			Position.Bias[] biasReturn)
	{
		sync(v);
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
				.getBounds();
		int p0 = v.getStartOffset();
		int p1 = v.getEndOffset();
		// works better with null expander :)
		TabExpander expander = v.getTabExpander();
		Segment text = v.getText(p0, p1);
		int offs = Utilities.getTabbedTextOffset(text, metrics, alloc.x,
				(int) x, expander /*null*/, p0);
		SegmentCache.releaseSharedSegment(text);
		int retValue = p0 + offs;
		if (retValue == p1)
		{
			// No need to return backward bias as GlyphPainter1 is used for
			// ltr text only.
			retValue--;
		}
		
		// ScriptSupport sup =((InlineView) v).getScriptSupport();
		// boolean multiline = !(p0 == v.getElement().getStartOffset() &&
		// p1 == v.getElement().getEndOffset());
		// if(sup != null && !multiline){
		// int line = sup.offsetToLine(p0);
		// ChunkCache.LineInfo lineInfo = sup.getChunkCache().getLineInfo(
		// line);
		// int col = Chunk.xToOffset(lineInfo.chunks, x, true);
		// retValue = sup.lineToOffset(line, col);
		// System.out.println("GlyphPainter -viewToModel: " + retValue
		/// + ":" + sup.lineToOffset(line, col)
		 // + ":" + col + ":" + p1);
		 //}
		biasReturn[0] = Position.Bias.Forward;
		return retValue;
	}

	/**
	 * Determines the best location (in the model) to break the given view. This
	 * method attempts to break on a whitespace location. If a whitespace
	 * location can't be found, the nearest character location is returned.
	 * 
	 * @param v the view
	 * @param p0 the location in the model where the fragment should start its
	 * representation >= 0
	 * @param pos the graphic location along the axis that the broken view would
	 * occupy >= 0; this may be useful for things like tab calculations
	 * @param len specifies the distance into the view where a potential break
	 * is desired >= 0
	 * @return the model location desired for a break
	 * @see View#breakView
	 */
	public int getBoundedPosition(GlyphView v, int p0, float x, float len)
	{
		sync(v);
		TabExpander expander = v.getTabExpander();
		Segment s = v.getText(p0, v.getEndOffset());
		int index = Utilities.getTabbedTextOffset(s, metrics, (int) x,
				(int) (x + len), expander, p0, false);
		SegmentCache.releaseSharedSegment(s);
		int p1 = p0 + index;
		return p1;
	}

	void sync(GlyphView v)
	{
		Font f = v.getFont();
		// System.out.println("InlineView -sync:" + f.getSize() + ":" +
		// f.getFamily());
		if ((metrics == null) || (!f.equals(metrics.getFont())))
		{
			// fetch a new FontMetrics
			Toolkit kit;
			Component c = v.getContainer();
			if (c != null)
			{
				kit = c.getToolkit();
			} else
			{
				kit = Toolkit.getDefaultToolkit();
			}
			metrics = kit.getFontMetrics(f);
		}
	}
	// --- variables ---------------------------------------------
	FontMetrics metrics;
}
