/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui.common;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * A dialog box that handles window closing, the ENTER key and the ESCAPE
 * key for you. All you have to do is implement ok() (called when
 * Enter is pressed) and cancel() (called when Escape is pressed, or window
 * is closed).
 * @author Slava Pestov
 * @version $Id: EnhancedDialog.java,v 1.1 2006/08/29 16:59:51 bizi Exp $
 */
public abstract class EnhancedDialog extends JDialog
{
	public EnhancedDialog(Frame parent, String title, boolean modal)
	{
		super(parent,title,modal);
		_init();
	}
	
	public EnhancedDialog(Dialog parent, String title, boolean modal)
	{
		super(parent,title,modal);
		_init();
	}

	public boolean getEnterEnabled()
	{
		return enterEnabled;
	}

	public void setEnterEnabled(boolean enterEnabled)
	{
		this.enterEnabled = enterEnabled;
	}
	
	public abstract void ok();
	public abstract void cancel();

	//{{{ Private members
	
	private void _init() {
		((Container)getLayeredPane()).addContainerListener(
			new ContainerHandler());
		getContentPane().addContainerListener(new ContainerHandler());

		keyHandler = new KeyHandler();
		addKeyListener(keyHandler);
		addWindowListener(new WindowHandler());

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		enterEnabled = true;
	}

	//}}}
	
	// protected members
	protected KeyHandler keyHandler;
	protected boolean enterEnabled;

	// Recursively adds our key listener to sub-components
	class ContainerHandler extends ContainerAdapter
	{
		public void componentAdded(ContainerEvent evt)
		{
			componentAdded(evt.getChild());
		}

		public void componentRemoved(ContainerEvent evt)
		{
			componentRemoved(evt.getChild());
		}

		private void componentAdded(Component comp)
		{
			comp.addKeyListener(keyHandler);
			if(comp instanceof Container)
			{
				Container cont = (Container)comp;
				cont.addContainerListener(this);
				Component[] comps = cont.getComponents();
				for(int i = 0; i < comps.length; i++)
				{
					componentAdded(comps[i]);
				}
			}
		}

		private void componentRemoved(Component comp)
		{
			comp.removeKeyListener(keyHandler);
			if(comp instanceof Container)
			{
				Container cont = (Container)comp;
				cont.removeContainerListener(this);
				Component[] comps = cont.getComponents();
				for(int i = 0; i < comps.length; i++)
				{
					componentRemoved(comps[i]);
				}
			}
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.isConsumed())
				return;

			if(evt.getKeyCode() == KeyEvent.VK_ENTER
				&& enterEnabled)
			{
				Component comp = getFocusOwner();
				while(comp != null)
				{
					if(comp instanceof JComboBox)
					{
						JComboBox combo = (JComboBox)comp;
						if(combo.isEditable())
						{
							Object selected = combo.getEditor().getItem();
							if(selected != null)
								combo.setSelectedItem(selected);
						}
						break;
					}

					comp = comp.getParent();
				}

				ok();
				evt.consume();
			}
			else if(evt.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				cancel();
				evt.consume();
			}
		}
	}

	class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			cancel();
		}
	}
}

