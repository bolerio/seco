/*
 * @(#)UnifiedToolBarTest16.java  1.0  2009-09-27
 * 
 * Copyright (c) 2009 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package test;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * UnifiedToolBarTest16.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-27 Created.
 */
public class UnifiedToolBarTest16 extends javax.swing.JPanel {

    /** Creates new form UnifiedToolBarTest16 */
    public UnifiedToolBarTest16() {
        initComponents();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final JFrame frame = new JFrame("Toolbar test "+UIManager.getLookAndFeel().getName());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
                frame.setContentPane(createContentPane());
                frame.pack();
                // frame.setMinimumSize(frame.getMinimumSize());
                frame.setSize(400, 300);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    private static JComponent createContentPane() {
        final JToolBar mainToolBar = new JToolBar();
        mainToolBar.setFloatable(false);
        mainToolBar.add(createToolBarButton("Open", "/ch/randelshofer/quaqua/images/Tree.openIcon.png"));
        mainToolBar.add(createToolBarButton("Leaf", "/ch/randelshofer/quaqua/images/Tree.leafIcon.png"));
        mainToolBar.putClientProperty("Quaqua.ToolBar.style", "title");

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(mainToolBar, BorderLayout.NORTH);
        panel.add(createInnerComponent(), BorderLayout.CENTER);

        final JToolBar bottomBar = new JToolBar();
        bottomBar.setFloatable(false);
        bottomBar.putClientProperty("Quaqua.ToolBar.style", "bottom");
        bottomBar.add(new JLabel("A status bar"));

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private static JComponent createInnerComponent() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        // panel.setBackground(new Color(0xF0F0F0));
        JSplitPane splitPane;
        panel.add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftComponent(), createRightComponent()), BorderLayout.CENTER);
        splitPane.setDividerSize(1);
        splitPane.putClientProperty("Quaqua.SplitPane.style", "bar");
        return panel;
    }

    private static JComponent createToolBarButton(String text, String iconResourceName) {
        final Icon icon = new ImageIcon(UnifiedToolBarTest16.class.getResource(iconResourceName));
        final JButton button = new JButton(text, icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFocusable(false);
        return button;
    }

    private static JComponent createLeftComponent() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.putClientProperty("Quaqua.ToolBar.style", "gradient");
        toolBar.putClientProperty("Quaqua.ToolBar.isDividerDrawn", Boolean.FALSE);
        toolBar.add(createToolBarButton(null, "/ch/randelshofer/quaqua/images/Tree.openIcon.png"));
        toolBar.add(createToolBarButton(null, "/ch/randelshofer/quaqua/images/Tree.leafIcon.png"));

        JTree tree;
        final JScrollPane treeScroller = new JScrollPane(tree = new JTree(new Object[]{"foo", "bar"}));
        treeScroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        tree.putClientProperty("Quaqua.Tree.style", "sideBar");

        return createComponent("Directories", toolBar, treeScroller);
    }

    private static JComponent createRightComponent() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.putClientProperty("Quaqua.ToolBar.style", "gradient");
        toolBar.putClientProperty("Quaqua.ToolBar.isDividerDrawn", Boolean.FALSE);
        toolBar.add(createToolBarButton(null, "/ch/randelshofer/quaqua/images/Tree.openIcon.png"));
        toolBar.add(createToolBarButton(null, "/ch/randelshofer/quaqua/images/Tree.leafIcon.png"));

        final JComponent tableScroller = new JScrollPane(new JTable(new Object[][]{
                    {"a", "bc"},
                    {"d", "ef"}
                }, new Object[]{
                    "bla", "blub"
                }));
        tableScroller.setBorder(new EmptyBorder(0, 0, 0, 0));

        return createComponent("Files", toolBar, tableScroller);
    }

    private static JComponent createComponent(String title, JToolBar toolBar, JComponent scroller) {
        JPanel panel = new JPanel();
        final JToolBar gradientBar = new JToolBar();
        gradientBar.setFloatable(false);
        gradientBar.putClientProperty("Quaqua.ToolBar.style", "gradient");
        JLabel label = new javax.swing.JLabel(title);

        javax.swing.GroupLayout gradientBarLayout = new javax.swing.GroupLayout(gradientBar);
        gradientBar.setLayout(gradientBarLayout);
        gradientBarLayout.setHorizontalGroup(
                gradientBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(gradientBarLayout.createSequentialGroup().addComponent(label).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE).addComponent(toolBar)));
        gradientBarLayout.setVerticalGroup(
                gradientBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(gradientBarLayout.createSequentialGroup().addGroup(gradientBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(toolBar).addComponent(label))));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)//
                .addGroup(layout.createSequentialGroup()//
                .addComponent(scroller, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))//
                .addComponent(gradientBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup()//
                .addComponent(scroller, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)//
                .addComponent(gradientBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)//
                ));

        return panel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
