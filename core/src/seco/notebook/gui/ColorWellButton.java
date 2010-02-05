/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * A button that, when clicked, shows a color chooser.
 *
 * You can get and set the currently selected color using
 * {@link #getSelectedColor()} and {@link #setSelectedColor(Color)}.
 * @author Slava Pestov
 * @version $Id: ColorWellButton.java,v 1.2 2006/09/14 16:09:38 bizi Exp $
 */
public class ColorWellButton extends JButton
{
	public ColorWellButton(Color color)
	{
		setIcon(new ColorWell(color));
		setMargin(new Insets(2,2,2,2));
		addActionListener(new ActionHandler());

		
	} 

	public Color getSelectedColor()
	{
		return ((ColorWell)getIcon()).color;
	} 

	public void setSelectedColor(Color color)
	{
		((ColorWell)getIcon()).color = color;
		repaint();
	} 

	static class ColorWell implements Icon
	{
		Color color;

		ColorWell(Color color)
		{
			this.color = color;
		}

		public int getIconWidth()
		{
			return 35;
		}

		public int getIconHeight()
		{
			return 10;
		}

		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			if(color == null)
				return;

			g.setColor(color);
			g.fillRect(x,y,getIconWidth(),getIconHeight());
			g.setColor(color.darker());
			g.drawRect(x,y,getIconWidth()-1,getIconHeight()-1);
		}
	} 

	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JDialog parent = GUIUtilities.getParentDialog(ColorWellButton.this);
			JDialog dialog;
			if (parent != null)
			{
				dialog = new ColorPickerDialog(parent,
					"Choose Color",
					true);
			}
			else
			{
				dialog = new ColorPickerDialog(
					JOptionPane.getFrameForComponent(
					ColorWellButton.this),
					"Choose Color",
					true);
			}
			dialog.pack();
			dialog.setVisible(true);
		}
	} //}}}

	//{{{ ColorPickerDialog class
	/**
	 * Replacement for the color picker dialog provided with Swing. This version
	 * supports dialog as well as frame parents.
	 * @since jEdit 4.1pre7
	 */
	private class ColorPickerDialog extends EnhancedDialog implements ActionListener
	{
		public ColorPickerDialog(Frame parent, String title, boolean modal)
		{
			super(parent,title,modal);

			init();
		}

		public ColorPickerDialog(Dialog parent, String title, boolean modal)
		{
			super(parent,title,modal);

			getContentPane().setLayout(new BorderLayout());

			init();
		}

		public void ok()
		{
			Color c = chooser.getColor();
			if (c != null)
				setSelectedColor(c);
			setVisible(false);
		}

		public void cancel()
		{
			setVisible(false);
		}

		public void actionPerformed(ActionEvent evt)
		{
			if (evt.getSource() == ok)
				ok();
			else
				cancel();
		}

		//{{{ Private members
		private JColorChooser chooser;
		private JButton ok;
		private JButton cancel;

		private void init()
		{
			Color c = getSelectedColor();
			if(c == null)
				chooser = new JColorChooser();
			else
				chooser = new JColorChooser(c);

			getContentPane().add(BorderLayout.CENTER, chooser);

			Box buttons = new Box(BoxLayout.X_AXIS);
			buttons.add(Box.createGlue());

			ok = new JButton("OK");
			ok.addActionListener(this);
			buttons.add(ok);
			buttons.add(Box.createHorizontalStrut(6));
			getRootPane().setDefaultButton(ok);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			buttons.add(cancel);
			buttons.add(Box.createGlue());

			getContentPane().add(BorderLayout.SOUTH, buttons);
			pack();
			setLocationRelativeTo(getParent());
		} //}}}
	} //}}}
}
