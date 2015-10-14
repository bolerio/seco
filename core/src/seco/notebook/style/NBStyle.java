/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.style;

import java.awt.Color;
import java.util.EnumMap;
import org.w3c.dom.Element;

import seco.notebook.XMLConstants;

public class NBStyle extends EnumMap<StyleAttribs, Object>
{
	StyleType tag;

	public NBStyle(StyleType tag)
	{
		super(StyleAttribs.class);
		this.tag = tag;
	}

	public StyleType getStyleType()
	{
		return tag;
	}

	public void fromXMLElement(Element el)
	{
		if (el == null)
			throw new RuntimeException("Null element instead of style");
		for (StyleAttribs sa : StyleAttribs.values())
		{
			String attr = el.getAttribute(sa.getKey());
			if (attr != null && attr.length() > 0)
			{
				this.put(sa, sa.getPropType().fromString(attr));
			}
		}
	}

	public String toXML(int ind_level)
	{
		if (size() == 0) return "";
		String s = XMLConstants.makeIndent(ind_level) + "<" + tag.tag_name;
		for (StyleAttribs sa : this.keySet())
		{
			s += " " + sa.getKey() + "=\""
					+ sa.getPropType().toString(get(sa)) + "\"";
		}
		s += "/>\n";
		;
		return s;
	}

	public Object getDefaultValue(StyleAttribs sa)
	{
		Object val = get(sa);
		if (val == null)
		{
			// TODO: very ugly workaround to provide default red color
			if (sa.equals(StyleAttribs.FG_COLOR) && tag.equals(StyleType.error))
				val = Color.red;
			else
				val = sa.getDefVal();
		}
		return val;
	}
}
