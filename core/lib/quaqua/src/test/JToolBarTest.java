/*
 * @(#)JToolBarTest.java  1.0  2011-08-10
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.EventQueue;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 * {@code JToolBarTest}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-10 Created.
 */
public class JToolBarTest {

protected void startup() {
// Set the Look and Feel to use
try { UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (Exception e) { e.printStackTrace(); }

createGUI();
}

private void createGUI() { JFrame frame = new JFrame(); frame.setContentPane(createToolBar()); frame.pack(); frame.setLocationRelativeTo(null); frame.setVisible(true); }

private JToolBar createToolBar() { JToolBar toolBar = new JToolBar(); toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS)); toolBar.add(createButton("1")); toolBar.add(createButton("2")); toolBar.add(createButton("3")); toolBar.add(createButton("4")); return toolBar; }

private JButton createButton(String p_text) { JButton button = new JButton(p_text); return button; }

public static void main(String[] args) {
EventQueue.invokeLater(new Runnable() {
@Override
public void run() { JToolBarTest test = new JToolBarTest(); test.startup(); }
});
}
}