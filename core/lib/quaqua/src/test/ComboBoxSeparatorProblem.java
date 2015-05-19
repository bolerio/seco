/*
 * @(#)ComboBoxSeparatorProblem.java  1.0  2011-07-04
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

package test;

/**
 * {@code ComboBoxSeparatorProblem}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-04 Created.
 */
import ch.randelshofer.quaqua.*;

import java.awt.*;
import javax.swing.*;

public class ComboBoxSeparatorProblem {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				final JComboBox comboBox = new JComboBox(new Object[] {
						"One", "Two", "Three"
				});
				comboBox.setRenderer(new DefaultListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
						final Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						if (index == 1) {
							final JPanel panel = new JPanel(new BorderLayout());
							panel.add(new JLabel("--- a separator in a combobox ---"), BorderLayout.NORTH);
							panel.add(renderer, BorderLayout.CENTER);
							panel.setOpaque(true);
							return panel;
						}
						return renderer;
					}
				});

				final JFrame frame = new JFrame();
				frame.setContentPane(comboBox);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}