/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class DialogLayout2
	implements LayoutManager
{
	protected static final int COMP_TWO_COL = 0;
	protected static final int COMP_BIG = 1;
	protected static final int COMP_BUTTON = 2;

	protected int m_divider = -1;
	protected int m_hGap = 10;
	protected int m_vGap = 5;
	protected Vector m_v = new Vector();

	public DialogLayout2() {}

	public DialogLayout2(int hGap, int vGap)
	{
		m_hGap = hGap;
		m_vGap = vGap;
	}

	public void addLayoutComponent(String name, Component comp) {}

	public void removeLayoutComponent(Component comp) {}

	public Dimension preferredLayoutSize(Container parent)
	{
		m_v.removeAllElements();
		int w = 0;
		int h = 0;
		int type = -1;

		for (int k=0 ; k<parent.getComponentCount(); k++)
		{
			Component comp = parent.getComponent(k);
			int newType = getLayoutType(comp);
			if (k == 0)
				type = newType;

			if (type != newType)
			{
				Dimension d = preferredLayoutSize(m_v, type);
				w = Math.max(w, d.width);
				h += d.height + m_vGap;
				m_v.removeAllElements();
				type = newType;
			}

			m_v.addElement(comp);
		}

		Dimension d = preferredLayoutSize(m_v, type);
		w = Math.max(w, d.width);
		h += d.height + m_vGap;

		h -= m_vGap;

		Insets insets = parent.getInsets();
		return new Dimension(w+insets.left+insets.right,
			h+insets.top+insets.bottom);
	}

	protected Dimension preferredLayoutSize(Vector v, int type)
	{
		int w = 0;
		int h = 0;
		switch (type)
		{
		case COMP_TWO_COL:
			int divider = getDivider(v);
			for (int k=1 ; k<v.size(); k+=2)
			{
				Component comp = (Component)v.elementAt(k);
				Dimension d = comp.getPreferredSize();
				w = Math.max(w, d.width);
				h += d.height + m_vGap;
			}
			h -= m_vGap;
			return new Dimension(divider+w, h);

		case COMP_BIG:
			for (int k=0 ; k<v.size(); k++)
			{
				Component comp = (Component)v.elementAt(k);
				Dimension d = comp.getPreferredSize();
				w = Math.max(w, d.width);
				h += d.height + m_vGap;
			}
			h -= m_vGap;
			return new Dimension(w, h);

		case COMP_BUTTON:
			Dimension d = getMaxDimension(v);
			w = d.width + m_hGap;
			h = d.height;
			return new Dimension(w*v.size()-m_hGap, h);
		}
		throw new IllegalArgumentException("Illegal type "+type);
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		return preferredLayoutSize(parent);
	}

	public void layoutContainer(Container parent)
	{
		m_v.removeAllElements();
		int type = -1;

		Insets insets = parent.getInsets();
		int w = parent.getWidth() - insets.left - insets.right;
		int x = insets.left;
		int y = insets.top;

		for (int k=0 ; k<parent.getComponentCount(); k++)
		{
			Component comp = parent.getComponent(k);
			int newType = getLayoutType(comp);
			if (k == 0)
				type = newType;

			if (type != newType)
			{
				y = layoutComponents(m_v, type, x, y, w);
				m_v.removeAllElements();
				type = newType;
			}

			m_v.addElement(comp);
		}

		y = layoutComponents(m_v, type, x, y, w);
		m_v.removeAllElements();
	}

	protected int layoutComponents(Vector v, int type,
		int x, int y, int w)
	{
		switch (type)
		{
		case COMP_TWO_COL:
			int divider = getDivider(v);
			for (int k=1 ; k<v.size(); k+=2)
			{
				Component comp1 = (Component)v.elementAt(k-1);
				Component comp2 = (Component)v.elementAt(k);
				Dimension d = comp2.getPreferredSize();

				comp1.setBounds(x, y, divider, d.height);
				comp2.setBounds(x+divider, y, w-divider, d.height);
				y += d.height + m_vGap;
			}
			//y -= m_vGap;
			return y;

		case COMP_BIG:
			for (int k=0 ; k<v.size(); k++)
			{
				Component comp = (Component)v.elementAt(k);
				Dimension d = comp.getPreferredSize();
				comp.setBounds(x, y, w, d.height);
				y += d.height + m_vGap;
			}
			//y -= m_vGap;
			return y;

		case COMP_BUTTON:
			Dimension d = getMaxDimension(v);
			int ww = d.width*v.size() + m_hGap*(v.size()-1);
			int xx = x + Math.max(0, (w - ww)/2);
			for (int k=0 ; k<v.size(); k++)
			{
				Component comp = (Component)v.elementAt(k);
				comp.setBounds(xx, y, d.width, d.height);
				xx += d.width + m_hGap;
			}
			return y + d.height;
		}
		throw new IllegalArgumentException("Illegal type "+type);
	}

	public int getHGap()
	{
		return m_hGap;
	}

	public int getVGap()
	{
		return m_vGap;
	}

	public void setDivider(int divider)
	{
		if (divider > 0)
			m_divider = divider;
	}

	public int getDivider()
	{
		return m_divider;
	}

	protected int getDivider(Vector v)
	{
		if (m_divider > 0)
			return m_divider;

		int divider = 0;
		for (int k=0 ; k<v.size(); k+=2)
		{
			Component comp = (Component)v.elementAt(k);
			Dimension d = comp.getPreferredSize();
			divider = Math.max(divider, d.width);
		}
		divider += m_hGap;
		return divider;
	}

	protected Dimension getMaxDimension(Vector v)
	{
		int w = 0;
		int h = 0;
		for (int k=0 ; k<v.size(); k++)
		{
			Component comp = (Component)v.elementAt(k);
			Dimension d = comp.getPreferredSize();
			w = Math.max(w, d.width);
			h = Math.max(h, d.height);
		}
		return new Dimension(w, h);
	}

	protected int getLayoutType(Component comp)
	{
		if (comp instanceof AbstractButton)
			return COMP_BUTTON;
		else if (comp instanceof JPanel ||
			comp instanceof JScrollPane ||
			comp instanceof JTabbedPane)
			return COMP_BIG;
		else
			return COMP_TWO_COL;
	}

	public String toString()
	{
		return getClass().getName() + "[hgap=" + m_hGap + ",vgap="
			+ m_vGap + ",divider=" + m_divider + "]";
	}
}

