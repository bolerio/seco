/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * MiscUtilities.java - Various miscallaneous utility functions
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2004 Slava Pestov
 * Portions copyright (C) 2000 Richard S. Hall
 * Portions copyright (C) 2001 Dirk Moebius
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

import java.util.Stack;
//import com.kobrix.webfaces.standalone.editor.util.Log;

/**
 * Path name manipulation, string manipulation, and more.
 * <p>
 * 
 * The most frequently used members of this class are:
 * <p>
 * 
 * <b>Some path name methods:</b>
 * <p>
 * <ul>
 * <li>{@link #getFileName(String)}</li>
 * <li>{@link #getParentOfPath(String)}</li>
 * <li>{@link #constructPath(String,String)}</li>
 * </ul>
 * <b>String comparison:</b>
 * <p>
 * 
 * A {@link #compareStrings(String,String,boolean)} method that unlike
 * <function>String.compareTo()</function>, correctly recognizes and handles
 * embedded numbers.
 * <p>
 * 
 * This class also defines several inner classes for use with the sorting
 * features of the Java collections API:
 * 
 * <ul>
 * <li>{@link MiscUtilities.StringCompare}</li>
 * <li>{@link MiscUtilities.StringICaseCompare}</li>
 * <li>{@link MiscUtilities.MenuItemCompare}</li>
 * </ul>
 * 
 * For example, you might call:
 * <p>
 * 
 * <code>Arrays.sort(myListOfStrings,
 *     new MiscUtilities.StringICaseCompare());</code>
 * 
 * @author Slava Pestov
 * @author John Gellene (API documentation)
 * @version $Id: MiscUtilities.java,v 1.1 2006/07/14 17:22:44 bizi Exp $
 */
public class MiscUtilities
{
	/**
	 * This encoding is not supported by Java, yet it is useful. A UTF-8 file
	 * that begins with 0xEFBBBF.
	 */
	public static final String UTF_8_Y = "UTF-8Y";

	/**
	 * Returns if two strings are equal. This correctly handles null pointers,
	 * as opposed to calling <code>o1.equals(o2)</code>.
	 * @since jEdit 4.2pre1
	 */
	public static boolean objectsEqual(Object o1, Object o2)
	{
		if (o1 == null)
		{
			if (o2 == null)
				return true;
			else
				return false;
		} else if (o2 == null)
			return false;
		else
			return o1.equals(o2);
	} // }}}

	/**
	 * Converts a Unix-style glob to a regular expression.
	 * <p>
	 *  ? becomes ., * becomes .*, {aa,bb} becomes (aa|bb).
	 * @param glob The glob pattern
	 */
	public static String globToRE(String glob)
	{
		final Object NEG = new Object();
		final Object GROUP = new Object();
		Stack state = new Stack();
		StringBuffer buf = new StringBuffer();
		boolean backslash = false;
		for (int i = 0; i < glob.length(); i++)
		{
			char c = glob.charAt(i);
			if (backslash)
			{
				buf.append('\\');
				buf.append(c);
				backslash = false;
				continue;
			}
			switch (c)
			{
			case '\\':
				backslash = true;
				break;
			case '?':
				buf.append('.');
				break;
			case '.':
			case '+':
			case '(':
			case ')':
				buf.append('\\');
				buf.append(c);
				break;
			case '*':
				buf.append(".*");
				break;
			case '|':
				if (backslash)
					buf.append("\\|");
				else
					buf.append('|');
				break;
			case '{':
				buf.append('(');
				if (i + 1 != glob.length() && glob.charAt(i + 1) == '!')
				{
					buf.append('?');
					state.push(NEG);
				} else
					state.push(GROUP);
				break;
			case ',':
				if (!state.isEmpty() && state.peek() == GROUP)
					buf.append('|');
				else
					buf.append(',');
				break;
			case '}':
				if (!state.isEmpty())
				{
					buf.append(")");
					if (state.pop() == NEG) buf.append(".*");
				} else
					buf.append('}');
				break;
			default:
				buf.append(c);
			}
		}
		return buf.toString();
	} // }}}

}
