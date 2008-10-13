/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

public enum StyleType
{
	global(XMLConstants.GLOBAL_STYLE, "Global"), inputCell(
			XMLConstants.CELL_STYLE, "InputCell"), outputCell(
			XMLConstants.OUTPUT_CELL_STYLE, "OutputCell"), error(
			XMLConstants.OUTPUT_ERROR_STYLE, "Error");
	public final String tag_name;
	String description;

	StyleType(String tag_name, String description)
	{
		this.tag_name = tag_name;
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}
}
