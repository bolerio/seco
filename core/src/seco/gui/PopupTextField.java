/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui;

/*
 * Copyright 2005 Patrick Gotthardt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 *
 * @author Patrick Gotthardt
 */
public class PopupTextField extends JTextField {
	private Icon icon;
	private Rectangle iconBounds;
	private JPopupMenu popupMenu;
	private IconifiedBorder border;
	
	public PopupTextField(Icon ico) {
		// initialization
		super();
		popupMenu = new JPopupMenu();
		
		// icon (we can't use the setIcon-method this time, as it relies on the border being set)
		this.icon = ico;
		iconBounds = new Rectangle(0, 0, ico.getIconWidth(), ico.getIconHeight());
		
		// border
		border = new IconifiedBorder(getBorder(), ico, 2);
		setBorder(border);
		
		// controller
		MouseHandler handler = new MouseHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}
	
	public void setPopupMenu(JPopupMenu menu) {
		JPopupMenu oldMenu = popupMenu;
		popupMenu = menu;
		firePropertyChange("popup", oldMenu, popupMenu);
	}
	
	public void setIcon(Icon ico) {
		Icon oldIcon = this.icon;
		this.icon = ico;
		iconBounds = new Rectangle(0, 0, ico.getIconWidth(), ico.getIconHeight());
		border.setIcon(icon);
		firePropertyChange("icon", oldIcon, icon);
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}
	
	public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        Insets insets = getInsets();
		FontMetrics fm = getFontMetrics(getFont());
		size.height = Math.max(fm.getHeight(), icon.getIconHeight())
					+ insets.top + insets.bottom;
        return size;
    }
	
	private class MouseHandler extends MouseInputAdapter {
		public void mouseMoved(MouseEvent e) {
			if(iconBounds.contains(e.getPoint())) {
				setCursor(Cursor.getDefaultCursor());
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			}
		}

		public void mouseClicked(MouseEvent e) {
			if(iconBounds.contains(e.getPoint())) {
				if(popupMenu.isVisible()) {
					popupMenu.setVisible(false);
				} else {
					popupMenu.show(PopupTextField.this, 0, getHeight());
				}
			}
		}
	}
	
	private class IconifiedBorder extends AbstractBorder {
		private Border innerBorder;
		private Icon icon;
		private Insets insets;
		private int spacing;
		
		public IconifiedBorder(Border innerBorder, Icon icon, int spacing) {
			this.innerBorder = innerBorder;
			this.icon = icon;
			this.spacing = spacing;
		}
		
		public void setIcon(Icon ico) {
			this.icon = ico;
			insets = null;
		}
		
		public Border getInnerBorder() {
			return innerBorder;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			innerBorder.paintBorder(c, g, x, y, width, height);
			Insets innerInsets = innerBorder.getBorderInsets(c);
			iconBounds.x = x + innerInsets.left + spacing;
			iconBounds.y = y + innerInsets.top + spacing;
			icon.paintIcon(c, g, iconBounds.x, iconBounds.y);
		}

		public Insets getBorderInsets(Component c) {
			if(insets == null) {
				insets = (Insets)innerBorder.getBorderInsets(c).clone();
				insets.left   += icon.getIconWidth() + spacing * 2;
				insets.top    += spacing;
				insets.bottom += spacing;
			}
			return insets;
		}
	}
}
