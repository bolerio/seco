/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * SyntaxUtilities.java - Utility functions
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Slava Pestov
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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Segment;
import javax.swing.text.Style;

import seco.notebook.AppConfig;
import seco.notebook.StyleAttribs;

/**
 * Contains utility functions used by the syntax highlighting code.
 * @since jEdit 4.2pre1
 * @version $Id: SyntaxUtilities.java,v 1.8 2006/10/26 15:57:34 bizi Exp $
 * @author Slava Pestov
 */
public class SyntaxUtilities
{
	//{{{ regionMatches() method
	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * character array.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The character array to match
	 * @since jEdit 4.2pre1
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
		int offset, char[] match)
	{
		int length = offset + match.length;
		if(length > text.offset + text.count)
			return false;
		char[] textArray = text.array;
		for(int i = offset, j = 0; i < length; i++, j++)
		{
			char c1 = textArray[i];
			char c2 = match[j];
			if(ignoreCase)
			{
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	} //}}}
	
//	TODO:remove
	public static final String SYNTAX_STYLES = "syntax_styles_prop";
	static ArrayList<SyntaxStyleBean> styles;
	public static ArrayList<SyntaxStyleBean> getSyntaxStyles(ScriptSupport scriptSupport)
	{
		//System.out.println("getSyntaxStyles:" + scriptSupport);
		return (ArrayList<SyntaxStyleBean>) 
		AppConfig.getInstance().getProperty(scriptSupport.getFactory().getEngineName() + SyntaxUtilities.SYNTAX_STYLES,
				getDefaultSyntaxStyles());
	}
	
	public static void resetSyntaxStyles(ScriptSupport scriptSupport)
	{
		AppConfig.getInstance().setProperty(
				scriptSupport.getFactory().getEngineName() + SyntaxUtilities.SYNTAX_STYLES,
				SyntaxUtilities.getDefaultSyntaxStyles());
	}
	public static ArrayList<SyntaxStyleBean> getDefaultSyntaxStyles()
	{
		if(styles != null) return styles;
		
		styles = new ArrayList<SyntaxStyleBean>(Token.ID_COUNT);
		int f = Font.PLAIN;
		int b = Font.BOLD;
		
		styles.add(/*Token.NULL,*/ new SyntaxStyleBean(Color.black,Color.white,f));
		styles.add(Token.COMMENT1, new SyntaxStyleBean(new Color(0x395DBD),Color.white,f));
		styles.add(Token.COMMENT2, new SyntaxStyleBean(new Color(0x397D5A),Color.white,f));
		styles.add(Token.COMMENT3, new SyntaxStyleBean(new Color(0x395DBD),Color.white,f));
		styles.add(Token.COMMENT4, new SyntaxStyleBean(new Color(0x395DBD),Color.white,f));
		styles.add(Token.DIGIT, new SyntaxStyleBean(Color.black,Color.white,f));
		styles.add(Token.FUNCTION, new SyntaxStyleBean(new Color(0x000600),Color.white,f));
		styles.add(Token.INVALID, new SyntaxStyleBean(Color.red,Color.white,f));
		
		styles.add(Token.KEYWORD1, new SyntaxStyleBean(new Color(0x7B0052),Color.white,b));
		styles.add(Token.KEYWORD2, new SyntaxStyleBean(new Color(0x7B0052),Color.white,b));
		styles.add(Token.KEYWORD3, new SyntaxStyleBean(new Color(0x7B0052),Color.white,b));
		styles.add(Token.KEYWORD4, new SyntaxStyleBean(new Color(0x7B0052),Color.white,b));
		styles.add(Token.LABEL, new SyntaxStyleBean(new Color(0x395DBD),Color.white,b));
		
		styles.add(Token.LITERAL1, new SyntaxStyleBean(new Color(0x2900FF),Color.white,f));
		styles.add(Token.LITERAL2, new SyntaxStyleBean(new Color(0x7B0052),Color.white,b));
		styles.add(Token.LITERAL3, new SyntaxStyleBean(new Color(0x2900FF),Color.white,f));
		styles.add(Token.LITERAL4, new SyntaxStyleBean(new Color(0x2900FF),Color.white,f));
		styles.add(Token.MARKUP, new SyntaxStyleBean(new Color(0x7B0052),Color.white,f));
		styles.add(Token.OPERATOR, new SyntaxStyleBean(Color.black,Color.white,f));
				
		return styles;
	}

}
