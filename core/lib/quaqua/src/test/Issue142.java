/*
 * @(#)Issue142.java  1.0  2011-07-26
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package test;


import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ch.randelshofer.quaqua.QuaquaLookAndFeel;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;


public class Issue142 {


    private static void createAndShowGUI() {
        final JFrame frame = new JFrame("JComboBox dropdown problem");
        final BorderLayout layout = new BorderLayout();
        final JPanel contentPane = new JPanel(layout);
        contentPane.setOpaque(true);
        contentPane.add(createComboBox(), BorderLayout.NORTH);
        contentPane.add(createComboBox(), BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private static JComboBox createComboBox() {
        final JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        final String[] items = new String[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"};
        comboBox.setModel(new javax.swing.DefaultComboBoxModel(items));
        return comboBox;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {


            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.leopard.Quaqua15LeopardCrossPlatformLookAndFeel");
                    //UIManager.setLookAndFeel(new QuaquaLookAndFeel());
                    createAndShowGUI();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }


        });
    }


}