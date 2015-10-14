/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.style;

import java.awt.Color;
import java.awt.Font;

public enum StyleAttribs
{
	// global
	// GLOB_BGCOLOR("defaultBackgroundColor", Color.WHITE, PropTypeEnum.COLOR),
	// GLOB_SEL_COLOR("defaultSloppySelectionColor", Color.GRAY,
	// PropTypeEnum.COLOR),

	BG_COLOR("bgColor", new Color(255, 255, 255), PropTypeEnum.COLOR), 
	BORDER_COLOR("borderColor", new Color(200, 200, 200), PropTypeEnum.COLOR), 
	FG_COLOR("fgColor", Color.BLACK, PropTypeEnum.COLOR), 
	FONT("font", new FontEx("Monospace", Font.PLAIN, 12), PropTypeEnum.FONT), 
	BORDER_WIDTH("borderWidth", 1.0, PropTypeEnum.NUMBER);

	// public static EnumSet<AllVisPropsEnum> GLOBALS = EnumSet.of(GLOB_BGCOLOR,
	// GLOB_SEL_COLOR);

	private String key;
	private PropTypeEnum e;
	private Object defVal;

	StyleAttribs(String key, Object defVal, PropTypeEnum e)
	{
		this.key = key;
		this.e = e;
		this.defVal = defVal;
	}

	public String getKey()
	{
		return key;
	}

	public Object getDefVal()
	{
		return defVal;
	}

	public void setDefVal(Object defVal)
	{
		this.defVal = defVal;
	}

	public PropTypeEnum getPropType()
	{
		return e;
	}

	public String getDescrName()
	{
		switch (this)
		{
		case BG_COLOR:
			return "Background Color";
		case BORDER_COLOR:
			return "Border Color";
		case BORDER_WIDTH:
			return "Border Width";
		case FONT:
			return "Font";
		case FG_COLOR:
			return "Foreground Color";
		default:
			return null;
		}
	}
};
