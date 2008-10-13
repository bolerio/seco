/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.java;

import jstyle.JSBeautifier;
import jstyle.JSFormatter;
import java.io.*;

import seco.notebook.syntax.Formatter;

/**
 * This class implements <code>CodeFormatter</code> based on Tal Davidson's
 * (davidsont@bigfoot.com) <i>Jstyle</i> Java beautifier. This implementation
 * is very improvised...
 * 
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: JstyleFormatter.java,v 1.5 2002/02/22 07:00:09 cziegeler
 * Exp $
 */
public class JavaFormatter implements Formatter
{
	
	protected JavaFormatterOptions options;
	
	public JavaFormatter(JavaFormatterOptions options)
	{
		this.options = options;
	}
	public String format(String code)
	{
		return format(code, null);
	}
	/**
	 * Format and beautify a <code>String</code> containing source code. This
	 * class has 2 pases: one for beautifying and another one for indentation.
	 * This should be performed in a single step!!!
	 * 
	 * @param code The input source code
	 * @param encoding The encoding used for constant strings embedded in the
	 * source code
	 * @return The formatted source code
	 */
	public String format(String code, String encoding)
	{
		try
		{
			JSFormatter formatter = new JSFormatter();
			formatter.setPreferredLineLength(options.getPrefLineLength());
			formatter.setLineLengthDeviation(options.getLineLengthDeviation());
			formatter.setBracketBreak(options.isBracketOnNewline());
			formatter.setBracketIndent(options.isBracketIndent());
			formatter.setSwitchIndent(options.isSwitchIndent());
			ByteArrayOutputStream out = new ByteArrayOutputStream(code.length());
			formatter.format(new BufferedReader(new StringReader(code)),
					new PrintWriter(out, true));
			JSBeautifier beautifier = new JSBeautifier();
			code = this.getString(out, encoding);
			out = new ByteArrayOutputStream(code.length());
			beautifier.setSpaceIndentation(options.getSpaceIndent());
			beautifier.beautifyReader(
					new BufferedReader(new StringReader(code)),
					new PrintWriter(out, true));
			return this.getString(out, encoding);
		}
		catch (Exception e)
		{
			// getLogger().debug("JstyleFormatter.format()", e);
			return code;
		}
	}

	/**
	 * Convert a byte array stream to string according to a given encoding. The
	 * encoding can be <code>null</code> for the platform's default encoding
	 * 
	 * @param PARAM_NAME Param description
	 * @return the value
	 * @exception EXCEPTION_NAME If an error occurs
	 */
	protected String getString(ByteArrayOutputStream out, String encoding)
			throws UnsupportedEncodingException
	{
		if (encoding == null)
		{
			return out.toString();
		}
		return out.toString(encoding);
	}
}
