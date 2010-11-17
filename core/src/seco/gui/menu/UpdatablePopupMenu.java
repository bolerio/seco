/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui.menu;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class UpdatablePopupMenu extends JPopupMenu
{
	private static final long serialVersionUID = 3296559695629374971L;

    public void update()
	{
		int c = getComponentCount();
		for (int i = 0; i < c; i++)
		{
			Component m = getComponent(i);
			if (m instanceof JMenuItem)
			{
				JMenuItem mi = (JMenuItem) m;
				Action a = mi.getAction();
				if (a != null) mi.setEnabled(a.isEnabled());
			} 
		}
	}
}
