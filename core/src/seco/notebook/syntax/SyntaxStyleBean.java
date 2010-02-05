/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax;

import java.awt.Color;
import java.awt.Font;

public class SyntaxStyleBean
{
	private Color fgColor;
	private Color bgColor;
	private int fontStyle;
		
	public SyntaxStyleBean()
	{		
	}
    
	/**
	 * Creates a new SyntaxStyle.
	 * @param fgColor The text color
	 * @param bgColor The background color
	 * @param font The text font
	 */
	public SyntaxStyleBean(Color fgColor, Color bgColor, int fontStyle)
	{
		this.fgColor = fgColor;
		this.bgColor = bgColor;
		this.fontStyle = fontStyle;
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
	public int getFontStyle()
	{
		return fontStyle;
	} 
	
	public void setFontStyle(int style)
	{
		fontStyle = style;
	} 

	public SyntaxStyle makeSyntaxStyle(Font f){
		return new SyntaxStyle(fgColor, bgColor,
				f.deriveFont(fontStyle));
	}
}
