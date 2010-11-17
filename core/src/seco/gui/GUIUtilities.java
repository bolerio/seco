/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui;

/*
 * GUIUtilities.java - Various GUI utility functions
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2004 Slava Pestov
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
import javax.swing.*;

import java.awt.*;


/**
 * Various GUI functions.<p>
 */
public class GUIUtilities
{
    private GUIUtilities()
    {}
    
      
    /**
     * Traverses the given component's parent tree looking for an
     * instance of JDialog, and return it. If not found, return null.
     * @param c The component
     */
    public static JDialog getParentDialog(Component c)
    {
        Component p = c.getParent();
        while (p != null && !(p instanceof JDialog))
            p = p.getParent();
        
        return (p instanceof JDialog) ? (JDialog) p : null;
    } 
    
    public static Frame getFrame(Component c)
    {
        Component p = c.getParent();
        while (p != null && !(p instanceof Frame))
        	p = p.getParent();
        
        return (p instanceof Frame) ? (Frame) p : null;
    } 
 
    public static void centerOnScreen(Component c)
    {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
    	int x = (screenSize.width - c.getWidth()) / 2;
    	int y = (screenSize.height - c.getHeight()) / 2;
    	c.setLocation(x, y);    	
    }
}