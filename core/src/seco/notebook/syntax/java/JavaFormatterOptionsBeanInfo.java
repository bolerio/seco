/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.java;

import com.l2fprod.common.beans.BaseBeanInfo;

public class JavaFormatterOptionsBeanInfo extends BaseBeanInfo
{
	public JavaFormatterOptionsBeanInfo()
	{
		super(JavaFormatterOptions.class);
		addProperty("prefLineLength").setShortDescription("Preffered line length.");
		addProperty("lineLengthDeviation").setShortDescription("Line length deviation.");
		addProperty("spaceIndent").setShortDescription("Number of spaces per one indent level.");
		addProperty("bracketOnNewline").setShortDescription("Put brackets on new line.");
		addProperty("bracketIndent").setShortDescription("Indent brackets.");
		addProperty("switchIndent").setShortDescription("Indent switch statement.");
	}
}
