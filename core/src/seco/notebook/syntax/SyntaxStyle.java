/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * SyntaxStyle.java - A simple text style class
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003 Slava Pestov
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

import java.awt.Font;
import java.awt.Color;
import java.io.Serializable;

/**
 * A simple text style class. It can specify the color, italic flag,
 * and bold flag of a run of text.
 * @author Slava Pestov
 * @version $Id: SyntaxStyle.java,v 1.6 2006/09/01 16:18:27 bizi Exp $
 */
public class SyntaxStyle 
{
	private Color fgColor;
	private Color bgColor;
	private Font font;
	
	public SyntaxStyle()
	{		
	}
    
	/**
	 * Creates a new SyntaxStyle.
	 * @param fgColor The text color
	 * @param bgColor The background color
	 * @param font The text font
	 */
	public SyntaxStyle(Color fgColor, Color bgColor, Font font)
	{
		this.fgColor = fgColor;
		this.bgColor = bgColor;
		this.font = font;
	} 

	/**
	 * Returns the text color.
	 */
	public Color getForegroundColor()
	{
		return fgColor;
	} 
	
	public void setForegroundColor(Color c)
	{
		fgColor = c;
	} 

	/**
	 * Returns the background color.
	 */
	public Color getBackgroundColor()
	{
		return bgColor;
	} 
	
	public void setBackgroundColor(Color c)
	{
		bgColor = c;
	} 

	/**
	 * Returns the style font.
	 */
	public Font getFont()
	{
		return font;
	} 
	
	public void setFont(Font f)
	{
		font = f;
	} 

}
