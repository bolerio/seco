/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * Chunk.java - A syntax token with extra information required for painting it
 * on screen
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Slava Pestov
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

import javax.swing.text.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * A syntax token with extra information required for painting it on screen.
 * @since jEdit 4.1pre1
 */
public class Chunk extends Token implements TabExpander
{
	/**
	 * Paints a chunk list.
	 * @param chunks The chunk list
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The width of the painted text
	 * @since jEdit 4.2pre1
	 */
	public static float paintChunkListNoWrap(Chunk chunks, Graphics2D gfx,
			float x, float y, boolean glyphVector)
	{
		Rectangle clipRect = gfx.getClipBounds();
		float _x = 0.0f;
		while (chunks != null)
		{
			// only paint visible chunks
			if (x + _x + chunks.width > clipRect.x
					&& x + _x < clipRect.x + clipRect.width)
			{
				if (chunks.accessable && chunks.visible)
				{
					gfx.setFont(chunks.style.getFont());
					gfx.setColor(chunks.style.getForegroundColor());
					if (glyphVector && chunks.gv != null)
						gfx.drawGlyphVector(chunks.gv, x + _x, y);
					else if (chunks.str != null)
					{
						gfx.drawString(chunks.str, (int) (x + _x), (int) y);
					}
				}
			}
			_x += chunks.width;
			chunks = (Chunk) chunks.next;
		}
		return _x;
	}

	public static float paintChunkList(Chunk chunks, Graphics2D gfx, float x,
			float y, int start, int end)
	{
		float _x = 0.0f;
		chunks = getChunkAtOffset(chunks, start);
		while (chunks != null)
		{
			if (chunks.offset >= end) return _x;
			if (chunks.accessable && chunks.visible)
			{
				gfx.setFont(chunks.style.getFont());
				if(!Color.white.equals(gfx.getColor()))
				//	System.out.println("GlyphPainter -sel: " + start + ":" + end + gfx.getColor());
				//else	
				  gfx.setColor(chunks.style.getForegroundColor());
				//System.out.println("paintChunkList: " + chunks.str + ":" + x
				// + ":" + _x  + ":" + start + ":" + end + ":" + chunks.gv.getFont());
				if (chunks.str != null)
				{
					if (chunks.offset + chunks.length <= end
							&& chunks.offset >= start)
					{
						if(chunks.gv != null)
							gfx.drawGlyphVector(chunks.gv, x + _x, y);
						else
						   gfx.drawString(chunks.str, (int) (x + _x), (int) y);
					} else if (end - chunks.offset > 0
							&& end - chunks.offset < chunks.length)
					{
						int part = end - chunks.offset;
						//System.out.println("SplitChunk: " + end + ":" + part + 
						//		":" + chunks.offset + ":" +  chunks.length);
						chunks = splitChunk(chunks, part);
						//System.out.println("SplittedChunk: " + chunks.str + ":" + chunks.offset + ":"
						//		+ ":" + chunks.length + ":" + ((Chunk)chunks.next).str);
						gfx.drawGlyphVector(chunks.gv, x + _x, y);
						
						//int part = end - chunks.offset;
						//int part0 = start - chunks.offset;
						//if (part0 < 0) part0 = 0;
						//gfx.drawString(chunks.str.substring(part0, part),
						//		(int) (x + _x), (int) y);
					} else
					// if(chunks.offset < start)
					{
						int part = start - chunks.offset; 
						String rem = chunks.str.substring(part);
						gfx.drawString(rem, (int) (x + _x), (int) y);
						GlyphVector g = chunks.style.getFont().createGlyphVector(
								chunks.gv.getFontRenderContext(), rem);
						float w = (float) g.getLogicalBounds().getWidth();
						// System.out.println("End Chunk: " + rem
						// + ":" + chunks.offset + ":" + chunks.length + ":" + start
						// + ":" + end + ":" + w + ":" + x + ":" + chunks.width);
						_x += w;
					}
					if (chunks.err)
						drawWaveUnderline(gfx, chunks, (x + _x), y);
					if (chunks.bracket)
						drawHighlightedBracket(gfx, chunks, (x + _x), y );
				}
			}
			_x += chunks.width;
			chunks = (Chunk) chunks.next;
		}
		return _x;
	}

	private static void drawHighlightedBracket(Graphics2D gfx, Chunk chunks, 
			float x, float y)
	{
		gfx.setColor(Color.black);
		FontMetrics fm = gfx.getFontMetrics();
		int w = fm.charWidth(chunks.str.charAt(0));
		int h = fm.getHeight();
		
		gfx.drawRect((int)x, (int) y - h + fm.getDescent(), w, h);
	}
	
	private static void drawWaveUnderline(Graphics2D gfx, Chunk chunks, 
			float x, float y)
	{
		
		int waveLength = (int) chunks.width;
		//System.out.println("drawWaveUnderline: " + chunks.str + ":" + x
		//		 + ":" + chunks.width + ":" +
		//		 offsetToX(chunks, chunks.offset) + ":" + x);
		gfx.setColor(Color.RED);
		if (waveLength > 0)
		{
			int[] wf = { 0, +1, 0, -1 };
			int[] xArray = new int[waveLength + 1];
			int[] yArray = new int[waveLength + 1];
			int yBase = (int) (y + 1.5);
			for (int i = 0; i <= waveLength; i++)
			{
				xArray[i] = (int) x + i;
				yArray[i] = yBase + wf[xArray[i] % 4];
			}
			gfx.drawPolyline(xArray, yArray, waveLength);
		}
	}
	
	public static Chunk getChunkAtOffset(Chunk chunks, int offset)
	{
		while (chunks != null)
		{
			if (chunks.offset >= offset
					|| chunks.offset + chunks.length > offset) return chunks;
			chunks = (Chunk) chunks.next;
		}
		return null;
	}

	
	/**
	 * Converts an offset in a chunk list into an x co-ordinate.
	 * @param chunks The chunk list
	 * @param offset The offset
	 * @since jEdit 4.1pre1
	 */
	public static float offsetToX(Chunk chunks, int offset)
	{
		if (chunks != null && offset < chunks.offset)
		{
//			TODO: sometimes this error arises without
			//known reason and no side effects, so keep quiet for now...
			//throw new ArrayIndexOutOfBoundsException(offset + " < "
			//		+ chunks.offset);
		}
		float x = 0.0f;
		while (chunks != null)
		{
			//System.out.println("Chunk - offsetToX: " + chunks.offset + 
			//		":" + chunks.length);
			if (chunks.accessable && offset < chunks.offset + chunks.length)
				return x + chunks.offsetToX(offset - chunks.offset);
			x += chunks.width;
			chunks = (Chunk) chunks.next;
		}
		return x;
	}
	
	public static float offsetToX(Chunk chunks, int start, int offset)
	{
		if (chunks != null && offset < chunks.offset)
		{
			//TODO: sometimes this error arises without
			//known reason and no side effects, so keep quiet for now...
			//throw new ArrayIndexOutOfBoundsException(offset + " < "
			//		+ chunks.offset);
			return 0;
		}
		float x = 0.0f;
		while (chunks != null)
		{
			//System.out.println("Chunk - offsetToX: " + chunks.offset + 
			//		":" + chunks.length + ":" + chunks.str);
			if (chunks.accessable && offset < chunks.offset + chunks.length)
				return x + chunks.offsetToX(offset - chunks.offset);
			if(start <= chunks.offset)
			   x += chunks.width;
			chunks = (Chunk) chunks.next;
		}
		return x;
	}

	/**
	 * Converts an x co-ordinate in a chunk list into an offset.
	 * @param chunks The chunk list
	 * @param x The x co-ordinate
	 * @param round Round up to next letter if past the middle of a letter?
	 * @return The offset within the line, or -1 if the x co-ordinate is too far
	 * to the right
	 * @since jEdit 4.1pre1
	 */
	public static int xToOffset(Chunk chunks, float x, boolean round)
	{
		float _x = 0.0f;
		while (chunks != null)
		{
			if (chunks.accessable && x < _x + chunks.width)
				return chunks.xToOffset(x - _x, round);
			_x += chunks.width;
			chunks = (Chunk) chunks.next;
		}
		return -1;
	}
	public boolean accessable;
	public boolean visible;
	public boolean initialized;
	// set up after init()
	public SyntaxStyle style;
	// this is either style.getBackgroundColor() or
	// styles[defaultID].getBackgroundColor()
	public Color background;
	public float width;
	public GlyphVector gv;
	public String str;

	public Chunk(float width, int offset, ParserRuleSet rules)
	{
		super(Token.NULL, offset, 0, rules);
		this.width = width;
	}

	public Chunk(byte id, int offset, int length, ParserRuleSet rules,
			SyntaxStyle[] styles, byte defaultID)
	{
		super(id, offset, length, rules);
		accessable = true;
		style = styles[id];
		background = style.getBackgroundColor();
		if (background == null)
			background = styles[defaultID].getBackgroundColor();
	}

	public final float[] getPositions()
	{
		if (gv == null) //return null;
		{
			gv = style.getFont().createGlyphVector(fontRenderContext, str);
			width = (float) gv.getLogicalBounds().getWidth();
		}
		if (positions == null)
			positions = gv.getGlyphPositions(0, length, null);
		return positions;
	}

	public final float offsetToX(int offset)
	{
		if (!visible || offset < 0)
			return 0.0f;
		else
			return (float) gv.getGlyphPosition(offset).getX();//getPositions()[offset * 2];
	}

	public final int xToOffset(float x, boolean round)
	{
		if (!visible)
		{
			if (round && width - x < x)
				return offset + length;
			else
				return offset;
		} else
		{
			float[] pos = getPositions();
			for (int i = 0; i < length; i++)
			{
				float glyphX = pos[i * 2];
				float nextX = (i == length - 1 ? width : pos[i * 2 + 2]);
				if (nextX > x)
				{
					if (!round || nextX - x > x - glyphX)
						return offset + i;
					else
						return offset + i + 1;
				}
			}
		}
		// wtf?
		return -1;
	}
	public static int tabSize = 8;

	public float nextTabStop(float x, int tabOffset)
	{
		int ntabs = (int) (x / tabSize);
		return (ntabs + 1) * tabSize;
	}

	public void init(Segment seg, TabExpander expander, float x,
			FontRenderContext fontRenderContext)
	{
		initialized = true;
		if (!accessable)
		{
			// do nothing
		} else if (length == 1 && seg.array[seg.offset + offset] == '\t')
		{
			visible = false;
			if (expander == null) expander = this;
			float newX = expander.nextTabStop(x, offset + length);
			width = newX - x;
		} else
		{
			visible = true;
			str = new String(seg.array, seg.offset + offset, length);
			gv = style.getFont().createGlyphVector(getFontRenderContext(fontRenderContext), str);
			width = (float) gv.getLogicalBounds().getWidth();
			//System.out.println("Chunk - init: " + style.getFont());
		}
	}
	
	private static FontRenderContext fontRenderContext;
	static{
		Graphics2D g2 =
		     new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		fontRenderContext = g2.getFontRenderContext();
	}
	
	private static FontRenderContext getFontRenderContext(FontRenderContext in)
	{
		if(in != null) 
			return in;
		return fontRenderContext;
	}
	
	private static Chunk splitChunk(Chunk chunks, int part)
	{
		String s = chunks.str;
		Chunk new_chunk = new Chunk(chunks, part);
		chunks.str = s.substring(0, part);
		chunks.length = part;
		chunks.gv = chunks.style.getFont().createGlyphVector(
				getFontRenderContext(fontRenderContext), chunks.str);
		chunks.width = (float) chunks.gv.getLogicalBounds().getWidth();
		Chunk next = (Chunk) chunks.next;
		chunks.next = new_chunk;
		new_chunk.next = next;
		new_chunk.prev = chunks;
		if(next != null)
		   next.prev = new_chunk;
		return chunks;
	}
	
	private float[] positions;
	
	private Chunk(Chunk c, int part)
	{
		super(c.id, c.offset + part, c.length - part, c.rules);
		accessable = true;
		visible = true;
		style = c.style;
		background = c.background;
		str = c.str.substring(part);
		initialized = true;
		gv = style.getFont().createGlyphVector(getFontRenderContext(fontRenderContext), str);
		width = (float) gv.getLogicalBounds().getWidth();
	}
}
