/*
 * @(#)TestBox.java  1.0  19 March 2005
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */package test;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * Box test.
 *
 * @author  Werner Randelshofer
 * @version $Id: TestBox.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class TestBox {

	public static void main(String[] args) throws Exception {
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			UIManager.setLookAndFeel(
					"ch.randelshofer.quaqua.QuaquaLookAndFeel"
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame();
		JMenuBar bar = new JMenuBar();
		
		JMenu menu = new JMenu("Menu");
		
		JMenuItem menuItem = new JMenuItem("Menu Item");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Working");
			}
		});
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		menu.add(menuItem);
		bar.add(menu);
		
		frame.setJMenuBar(bar);
		
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
}