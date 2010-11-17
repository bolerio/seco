/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
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
package seco.notebook.html;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;

/**
 * Panel to show and manipulate border settings for a rectangular object such as
 * a table cell
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 * License, for details see file gpl.txt in the distribution package of this
 * software
 * 
 * @version stage 11, April 27, 2003
 */
public class BorderPanel extends JPanel implements AttributeComponent
{
	private Vector components = new Vector();
	/** the attributes for border width */
	private CombinedAttribute bWidth;
	/** the attributes for border color */
	private CombinedAttribute bColor;
	/** the color value to compare to for determining changes */
	private String oColor;
	/** the width value to compare to for determining changes */
	private String oWidth;
	/** indicates if a call to setValue is for initial setting or for changes */
	private int setValueCalls = 0;

	public BorderPanel()
	{
		super();
		// set layout
		GridBagLayout g = new GridBagLayout();
		setLayout(g);
		// constraints to use on our GridBagLayout
		GridBagConstraints c = new GridBagConstraints();
		addSettings(g, c, "top", CombinedAttribute.ATTR_TOP, 0, 0);
		addSettings(g, c, "right", CombinedAttribute.ATTR_RIGHT, 1, 1);
		addSettings(g, c, "bottom", CombinedAttribute.ATTR_BOTTOM, 1, 0);
		addSettings(g, c, "left", CombinedAttribute.ATTR_LEFT, 0, 1);
	}

	private void addSettings(GridBagLayout g, GridBagConstraints c,
			String title, int side, int x, int y)
	{
		BorderSettings bs = new BorderSettings(title, side);
		Util.addGridBagComponent(this, bs, g, c, x, y, GridBagConstraints.WEST);
		components.addElement(bs);
	}

	/**
	 * set the value of this <code>AttributeComponent</code>
	 * 
	 * @param a the set of attributes possibly having an attribute this
	 * component can display
	 * 
	 * @return true, if the set of attributes had a matching attribute, false if
	 * not
	 */
	public boolean setValue(AttributeSet a)
	{
		boolean success = true;
		Enumeration e = components.elements();
		bWidth = new CombinedAttribute(CSS.Attribute.BORDER_WIDTH, a, true);
		bColor = new CombinedAttribute(CSS.Attribute.BORDER_COLOR, a, true);
		if (++setValueCalls < 2)
		{
			oColor = bColor.getAttribute();
			oWidth = bWidth.getAttribute();
		}
		while (e.hasMoreElements())
		{
			((BorderSettings) e.nextElement()).setValue(bWidth, bColor);
		}
		return success;
	}

	/**
	 * get the value of this <code>AttributeComponent</code>
	 * 
	 * @return the value selected from this component
	 */
	public AttributeSet getValue()
	{
		SimpleAttributeSet set = new SimpleAttributeSet();
		Enumeration e = components.elements();
		BorderSettings bs;
		for (int i = 0; i < components.size(); i++)
		{
			bs = (BorderSettings) components.elementAt(i);
			bColor.setAttribute(i, bs.getBorderColor());
			bWidth.setAttribute(i, bs.getBorderWidth());
		}
		String newValue = bColor.getAttribute();
		if ((((oColor == null) && (newValue != null)) || (!oColor
				.equalsIgnoreCase(newValue))))
		{
			set.addAttribute(CSS.Attribute.BORDER_COLOR, newValue);
		}
		newValue = bWidth.getAttribute();
		if (!oWidth.equalsIgnoreCase(newValue))
		{
			set.addAttribute(CSS.Attribute.BORDER_WIDTH, newValue);
		}
		return set;
	}

	public AttributeSet getValue(boolean includeUnchanged)
	{
		if (includeUnchanged)
		{
			SimpleAttributeSet set = new SimpleAttributeSet();
			Enumeration e = components.elements();
			BorderSettings bs;
			for (int i = 0; i < components.size(); i++)
			{
				bs = (BorderSettings) components.elementAt(i);
				bColor.setAttribute(i, bs.getBorderColor());
				bWidth.setAttribute(i, bs.getBorderWidth());
			}
			String newValue = bColor.getAttribute();
			set.addAttribute(CSS.Attribute.BORDER_COLOR, newValue);
			newValue = bWidth.getAttribute();
			set.addAttribute(CSS.Attribute.BORDER_WIDTH, newValue);
			return set;
		} else
		{
			return getValue();
		}
	}

	/**
	 * Panel to show and manipulate border settings
	 */
	private class BorderSettings extends JPanel
	{
		/** the border side */
		private int side;
		/** selector for border width */
		private SizeSelectorPanel bWidth;
		/** selector for border color */
		private ColorPanel bColor;

		/**
		 * construct a <code>BorderSettings</code> panel
		 * 
		 * @param title the title of this object
		 * @param borderKey the attribute key for the border width this object
		 * represents
		 * @param colorKey the attribute key for the border color this object
		 * represents
		 */
		public BorderSettings(String title, int side)
		{
			super();
			this.side = side;
			// set layout
			GridBagLayout g = new GridBagLayout();
			setLayout(g);
			// constraints to use on our GridBagLayout
			GridBagConstraints c = new GridBagConstraints();
			// set border and title
			setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
					title));
			// add the width control and label
			Util.addGridBagComponent(this, new JLabel("Width:"), g, c, 0, 0,
					GridBagConstraints.EAST);
			bWidth = new SizeSelectorPanel(CSS.Attribute.BORDER_WIDTH, null,
					false, SizeSelectorPanel.TYPE_LABEL);
			Util.addGridBagComponent(this, bWidth, g, c, 1, 0,
					GridBagConstraints.WEST);
			// add the color control and label
			Util.addGridBagComponent(this, new JLabel("Color:"), g, c, 0, 1,
					GridBagConstraints.EAST);
			bColor = new ColorPanel(null, Color.black,
					CSS.Attribute.BORDER_COLOR);
			Util.addGridBagComponent(this, bColor, g, c, 1, 1,
					GridBagConstraints.WEST);
		}

		public String getBorderColor()
		{
			return bColor.getAttr();
		}

		public String getBorderWidth()
		{
			return bWidth.getAttr();
		}

		/**
		 * set the value of this <code>AttributeComponent</code>
		 * 
		 * @param color the <code>CombinedAttribute</code> to take the color
		 * from
		 * 
		 */
		public void setValue(CombinedAttribute borderWidths,
				CombinedAttribute borderColors)
		{
			String attr = borderColors.getAttribute(side);
			// System.out.println("BorderSettings setValue attr='" + attr +
			// "'");
			if (attr != null)
			{
				this.bColor.setValue(attr);
			}
			attr = borderWidths.getAttribute(side);
			if (attr != null)
			{
				this.bWidth.setValue(attr);
			}
		}
	}
}
