/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax;

public interface Formatter
{
	//it's quite ugly to work with Strings, but
	//the only way to insert the formatted text back into
	//the Document is by insertString(), so...
	public String format(String code);
}
