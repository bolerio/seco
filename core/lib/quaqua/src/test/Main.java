/*
 * @(#)Main.java
 * 
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 * Main.
 *
 * @author Werner Randelshofer
 * @version $Id: Main.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class Main extends javax.swing.JPanel {

    private boolean switchLookAndFeelDecoration = true;

    private static class Item extends DefaultMutableTreeNode {

        private String label;
        private String clazz;
        private JComponent component;

        public Item(String label, String clazz) {
            this.label = label;
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return label;
        }

        public JComponent getComponent() {
            if (component == null) {
                try {
                    component = (JComponent) Class.forName(clazz).newInstance();
                } catch (Throwable ex) {
                    component = new JLabel(ex.toString());
                    ex.printStackTrace();
                }
            }
            return component;
        }
    }

    public static void main(String[] args) {
        final long start = System.currentTimeMillis();

        final java.util.List argList = Arrays.asList(args);
        // Explicitly turn on font antialiasing.
        try {
            System.setProperty("swing.aatext", "true");
        } catch (AccessControlException e) {
            // can't do anything about this
        }

        // Use screen menu bar, if not switched off explicitly
        try {
            if (System.getProperty("apple.laf.useScreenMenuBar") == null
                    && System.getProperty("com.apple.macos.useScreenMenuBar") == null) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            }
        } catch (AccessControlException e) {
            // can't do anything about this
        }

        // Add Quaqua to the lafs
        ArrayList<LookAndFeelInfo> infos = new ArrayList<LookAndFeelInfo>(Arrays.asList(UIManager.getInstalledLookAndFeels()));
        infos.add(new LookAndFeelInfo("Quaqua", QuaquaManager.getLookAndFeelClassName()));
        UIManager.setInstalledLookAndFeels(infos.toArray(new LookAndFeelInfo[infos.size()]));

        // Turn on look and feel decoration when not running on Mac OS X or Darwin.
        // This will still not look pretty, because we haven't got cast shadows
        // for the frame on other operating systems.
        boolean useDefaultLookAndFeelDecoration =
                !System.getProperty("os.name").toLowerCase().startsWith("mac")
                && !System.getProperty("os.name").toLowerCase().startsWith("darwin");
        int index = argList.indexOf("-decoration");
        if (index != -1 && index < argList.size() - 1) {
            useDefaultLookAndFeelDecoration = argList.get(index + 1).equals("true");
        }

        if (useDefaultLookAndFeelDecoration) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        // Launch the test program
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long edtEnd = System.currentTimeMillis();
                int index;
                index = argList.indexOf("-include");
                if (index != -1 && index < argList.size() - 1) {
                    HashSet includes = new HashSet();
                    includes.addAll(Arrays.asList(((String) argList.get(index + 1)).split(",")));

                    QuaquaManager.setIncludedUIs(includes);
                }
                index = argList.indexOf("-exclude");
                if (index != -1 && index < argList.size() - 1) {
                    HashSet excludes = new HashSet();
                    excludes.addAll(Arrays.asList(((String) argList.get(index + 1)).split(",")));

                    QuaquaManager.setExcludedUIs(excludes);
                }
                index = argList.indexOf("-laf");
                String lafName;
                if (index != -1 && index < argList.size() - 1) {
                    lafName = (String) argList.get(index + 1);
                } else {
                    lafName = QuaquaManager.getLookAndFeelClassName();
                }
                long lafCreate = 0;
                if (!lafName.equals("default")) {

                    if (lafName.equals("system")) {
                        lafName = UIManager.getSystemLookAndFeelClassName();
                    } else if (lafName.equals("crossplatform")) {
                        lafName = UIManager.getCrossPlatformLookAndFeelClassName();
                    }

                    try {
                        //UIManager.setLookAndFeel(lafName);
                        System.out.println("   CREATING LAF   " + lafName);

                        LookAndFeel laf = (LookAndFeel) Class.forName(lafName).newInstance();
                        lafCreate = System.currentTimeMillis();
                        System.out.println("   LAF CREATED   ");
                        System.out.println("   SETTING LAF  ");
                        UIManager.setLookAndFeel(laf);
                        System.out.println("   LAF SET   ");
                    } catch (Exception e) {
                        System.err.println("Error setting " + lafName + " in UIManager.");
                        e.printStackTrace();
                        // can't do anything about this
                    }
                }
                long lafEnd = System.currentTimeMillis();
                JFrame f = new JFrame();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setTitle(UIManager.getLookAndFeel().getName() + " "
                        + QuaquaManager.getVersion()
                        + " on Java " + System.getProperty("java.version")
                        + " " + System.getProperty("os.arch"));
                Main ex = new Main();
                f.add(ex);
                f.setJMenuBar(ex.menuBar);
                long createEnd = System.currentTimeMillis();
                //f.pack();
                f.setSize(740, 480);
                ///long packEnd = System.currentTimeMillis();
                f.setVisible(true);
                long end = System.currentTimeMillis();
                System.out.println("QuaquaTest EDT latency=" + (edtEnd - start));
                if (!lafName.equals("default")) {
                    System.out.println("QuaquaTest laf create latency=" + (lafCreate - edtEnd));
                    System.out.println("QuaquaTest set laf latency=" + (lafEnd - lafCreate));
                }
                System.out.println("QuaquaTest create latency=" + (createEnd - lafEnd));
                //System.out.println("Main pack latency  ="+(packEnd - createEnd));
                System.out.println("QuaquaTest total startup latency=" + (end - start));
            }
        });
    }

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        treeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        splitPane.setDividerSize(1);
        splitPane.putClientProperty("Quaqua.SplitPane.style", "bar");
        splitPane.setOneTouchExpandable(false);
        tree.putClientProperty("Quaqua.Tree.style", "sideBar");
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree.setRequestFocusEnabled(false);
//tree.setFont(new Font("Lucida Grande",Font.PLAIN,11)); // FIXME!!!
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultMutableTreeNode n;
        root.add(n = new DefaultMutableTreeNode("BUTTONS"));
        n.add(new Item("Push Button", "test.PushButtonTest"));
        n.add(new Item("Special Buttons", "test.SpecialButtonTest"));
        n.add(new Item("Default Button", "test.DefaultButtonTest"));
        n.add(new Item("Toggle Button", "test.ToggleButtonTest"));
        n.add(new Item("Check Box", "test.CheckBoxTest"));
        n.add(new Item("Radio Button", "test.RadioButtonTest"));
        n.add(new Item("Combo Box", "test.ComboBoxTest"));
        n.add(new Item("Editable Combo Box", "test.EditableComboBoxTest"));
        root.add(n = new DefaultMutableTreeNode("ADJUSTORS"));
        n.add(new Item("Slider", "test.SliderTest"));
        n.add(new Item("Spinner", "test.SpinnerTest"));
        n.add(new Item("Progress Bar", "test.ProgressBarTest"));
        n.add(new Item("Scroll Bar", "test.ScrollBarTest"));
        root.add(n = new DefaultMutableTreeNode("TEXT"));
        n.add(new Item("Editor Pane", "test.EditorPaneTest"));
        n.add(new Item("Formatted Text Field", "test.FormattedTextFieldTest"));
        n.add(new Item("Password Field", "test.PasswordFieldTest"));
        n.add(new Item("Text Area", "test.TextAreaTest"));
        n.add(new Item("Text Field", "test.TextFieldTest"));
        n.add(new Item("Special Text Fields", "test.SpecialTextFieldTest"));
        n.add(new Item("Text Pane", "test.TextPaneTest"));
        root.add(n = new DefaultMutableTreeNode("VIEWS"));
        n.add(new Item("List", "test.ListTest"));
        n.add(new Item("Table", "test.TableTest"));
        n.add(new Item("Tree", "test.TreeTest"));
        n.add(new Item("Scroll Pane", "test.ScrollPaneTest"));
        n.add(new Item("Browser", "test.BrowserTest"));
        root.add(n = new DefaultMutableTreeNode("GROUPING"));
        n.add(new Item("Tabbed Pane", "test.TabbedPaneTest"));
        n.add(new Item("Split Pane", "test.SplitPaneTest"));
        n.add(new Item("Border", "test.BorderTest"));
        n.add(new Item("Box", "test.BoxTest"));
        root.add(n = new DefaultMutableTreeNode("WINDOWS"));
        n.add(new Item("Desktop Pane", "test.DesktopPaneTest"));
        n.add(new Item("Root Pane", "test.RootPaneTest"));
        n.add(new Item("Popup Menu", "test.PopupMenuTest"));
        n.add(new Item("Tool Bar", "test.ToolBarTest"));
        n.add(new Item("Color Chooser", "test.ColorChooserTest"));
        n.add(new Item("File Chooser", "test.FileChooserTest"));
        n.add(new Item("OptionPane", "test.OptionPaneTest"));
        n.add(new Item("Dialog", "test.DialogTest"));
        n.add(new Item("Sheet", "test.SheetTest"));
        n.add(new Item("Palette", "test.PaletteTest"));
        root.add(n = new DefaultMutableTreeNode("LAYOUT"));
        n.add(new Item("Alignment", "test.AlignmentTest"));
        n.add(new Item("Group Layout", "test.GroupLayoutTest"));
        n.add(new Item("Visual Margin", "test.VisualMarginTest"));
        root.add(n = new DefaultMutableTreeNode("BEHAVIOR"));
        n.add(new Item("Drag and Drop", "test.DnDTest"));
        n.add(new Item("Input Verifier", "test.InputVerifierTest"));
        n.add(new Item("Radio Button Focus", "test.RadioButtonFocusTest"));
        root.add(n = new DefaultMutableTreeNode("NATIVE CODE"));
        n.add(new Item("File System", "test.FileSystemTest"));
        n.add(new Item("Clipboard", "test.ClipboardTest"));
        n.add(new Item("Preferences", "test.PreferencesTest"));
        DefaultTreeModel tm = new DefaultTreeModel(root);
        tree.setModel(tm);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = tree.getSelectionPath();
                viewPane.removeAll();
                if (path != null && path.getPathCount() > 0 && (path.getLastPathComponent() instanceof Item)) {
                    viewPane.add(((Item) path.getLastPathComponent()).getComponent());
                    viewPane.applyComponentOrientation(SwingUtilities.getRoot(Main.this).getComponentOrientation());
                }
                viewPane.revalidate();
                viewPane.repaint();
            }
        });

        for (int i = tree.getRowCount(); i >= 0; i--) {
            tree.expandRow(i);
        }

        // Add look and feels to menu bar
        ButtonGroup group = new ButtonGroup();
        for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            final JRadioButtonMenuItem mi = new JRadioButtonMenuItem(info.getName());
            group.add(mi);
            if (UIManager.getLookAndFeel().getClass().toString().equals(info.getClassName())) {
                mi.setSelected(true);
            }
            lafMenu.add(mi);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setLookAndFeel(info, mi, root);
                }
            });
        }
    }

    private void setLookAndFeel(LookAndFeelInfo info, JRadioButtonMenuItem mi, DefaultMutableTreeNode root) {
        try {

            if (switchLookAndFeelDecoration) {
                boolean b = "Metal".equals(info.getName());
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(Main.this);
                if (b != f.isUndecorated()) {
                    f.dispose();
                    f.setUndecorated(b);
                    f.getRootPane().setWindowDecorationStyle(b?JRootPane.FRAME:JRootPane.NONE);
                    f.setVisible(true);
                }
            }



            UIManager.setLookAndFeel(info.getClassName());
            SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(Main.this));
            mi.setSelected(true);

            Window w = SwingUtilities.getWindowAncestor(Main.this);
            if (w instanceof JFrame) {
                ((JFrame) w).setTitle(UIManager.getLookAndFeel().getName() + " "
                      //  + QuaquaManager.getVersion()
                        + " on Java " + System.getProperty("java.version")
                        + " " + System.getProperty("os.arch"));
            }

            for (Enumeration i = root.preorderEnumeration(); i.hasMoreElements();) {
                Object o = i.nextElement();
                if (o instanceof Item) {
                    Item item = (Item) o;
                    item.component = null;
                }
            }

        } catch (Throwable ex) {
            ex.printStackTrace();
            mi.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        lafMenu = new javax.swing.JMenu();
        splitPane = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        rightPane = new javax.swing.JPanel();
        viewPane = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        showClipBoundsBox = new javax.swing.JCheckBox();
        showVisualBoundsBox = new javax.swing.JCheckBox();
        rtlBox = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        lafMenu.setText("Look and Feel");
        menuBar.add(lafMenu);

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(200);

        treeScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        treeScrollPane.setViewportView(tree);

        splitPane.setLeftComponent(treeScrollPane);

        rightPane.setMinimumSize(new java.awt.Dimension(0, 1));
        rightPane.setLayout(new java.awt.BorderLayout());

        viewPane.setLayout(new java.awt.BorderLayout());
        rightPane.add(viewPane, java.awt.BorderLayout.CENTER);

        showClipBoundsBox.setText("Show Clip Bounds");
        showClipBoundsBox.addActionListener(formListener);
        controlPanel.add(showClipBoundsBox);

        showVisualBoundsBox.setText("Show Visual Bounds");
        showVisualBoundsBox.addActionListener(formListener);
        controlPanel.add(showVisualBoundsBox);

        rtlBox.setText("RTL");
        rtlBox.addActionListener(formListener);
        controlPanel.add(rtlBox);

        rightPane.add(controlPanel, java.awt.BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPane);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == showClipBoundsBox) {
                Main.this.showClipBounds(evt);
            }
            else if (evt.getSource() == showVisualBoundsBox) {
                Main.this.showVisualBounds(evt);
            }
            else if (evt.getSource() == rtlBox) {
                Main.this.rtlBoxPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void showClipBounds(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showClipBounds
        UIManager.put("Quaqua.Debug.showClipBounds", showClipBoundsBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        repaint();
    }//GEN-LAST:event_showClipBounds

    private void showVisualBounds(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVisualBounds
        UIManager.put("Quaqua.Debug.showVisualBounds", showVisualBoundsBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        repaint();
    }//GEN-LAST:event_showVisualBounds

    private void rtlBoxPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rtlBoxPerformed
        Container root = (Container) SwingUtilities.getRoot(this);
        root.applyComponentOrientation(rtlBox.isSelected() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        root.validate();
    }//GEN-LAST:event_rtlBoxPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JMenu lafMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel rightPane;
    private javax.swing.JCheckBox rtlBox;
    private javax.swing.JCheckBox showClipBoundsBox;
    private javax.swing.JCheckBox showVisualBoundsBox;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JPanel viewPane;
    // End of variables declaration//GEN-END:variables
}
