/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.java;

public class JavaFormatterOptions
{
	/**
	 * The default preferred line length. 
	 */
	protected int prefLineLength = 82;
	/**
	 * The default line length deviation. 
	 */
	protected int lineLengthDeviation = 8;
	/**
	 * The default space indentation. 
	 */
	protected int spaceIndent = 4;
	protected boolean bracketOnNewline = true;
	protected boolean bracketIndent = false;
	
	protected boolean switchIndent = true;
	
	public JavaFormatterOptions(){}
	
	public boolean isBracketOnNewline()
	{
		return bracketOnNewline;
	}
	public void setBracketOnNewline(boolean bracketOnNewline)
	{
		this.bracketOnNewline = bracketOnNewline;
	}
	public int getLineLengthDeviation()
	{
		return lineLengthDeviation;
	}
	public void setLineLengthDeviation(int lineLengthDeviation)
	{
		this.lineLengthDeviation = lineLengthDeviation;
	}
	public int getPrefLineLength()
	{
		return prefLineLength;
	}
	public void setPrefLineLength(int prefLineLength)
	{
		this.prefLineLength = prefLineLength;
	}
	public int getSpaceIndent()
	{
		return spaceIndent;
	}
	public void setSpaceIndent(int spaceIndent)
	{
		this.spaceIndent = spaceIndent;
	}
	public boolean isBracketIndent()
	{
		return bracketIndent;
	}
	public void setBracketIndent(boolean bracketIndent)
	{
		this.bracketIndent = bracketIndent;
	}
	public boolean isSwitchIndent()
	{
		return switchIndent;
	}
	public void setSwitchIndent(boolean switchIndent)
	{
		this.switchIndent = switchIndent;
	}
}
