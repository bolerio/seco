/*
 * @(#)QuaquaLeopardFileChooserPanel.java  1.0  June 26, 2004
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.leopard.filechooser;

import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * QuaquaLeopardFileChooserPanel.
 * 
 * @author Werner Randelshofer
 * @version 1.0  June 26, 2004  Created.
 */
public class QuaquaLeopardFileChooserPanel extends javax.swing.JPanel {
    /**
     * This is the border painted around the cell area.
     */
    private static final Border cellBorder = QuaquaBorderFactory.create(
    Toolkit.getDefaultToolkit().createImage(QuaquaComboBoxButton.class.getResource("images/ComboBox.cellBorder.png")),
    new Insets(10, 8, 14, 0),
    new Insets(1, 1, 1, 1),
    true
    );
    /**
     * This is the disabled border painted around the cell area.
     */
    private static final Border disabledCellBorder = QuaquaBorderFactory.create(
    Toolkit.getDefaultToolkit().createImage(QuaquaComboBoxButton.class.getResource("images/ComboBox.cellBorder.D.png")),
    new Insets(10, 8, 14, 0),
    new Insets(1, 1, 1, 1),
    true
    );
    /**
     * This is the border painted around the button area.
     */
    private static final Border buttonBorder = QuaquaBorderFactory.create(
    Toolkit.getDefaultToolkit().createImage(QuaquaComboBoxButton.class.getResource("images/ComboBox.buttonBorder.png")),
    new Insets(10, 1, 14, 8),
    new Insets(1, 1, 1, 1),
    true
    );
    /**
     * This is the pressed border painted around the button area.
     */
    private static final Border pressedButtonBorder = QuaquaBorderFactory.create(
    Toolkit.getDefaultToolkit().createImage(QuaquaComboBoxButton.class.getResource("images/ComboBox.buttonBorder.P.png")),
    new Insets(10, 1, 14, 8),
    new Insets(1, 1, 1, 1),
    true
    );
    
    /** Creates new form. */
    public QuaquaLeopardFileChooserPanel() {
        
        initComponents();
        int h;
        h = fileNameLabel.getPreferredSize().height;
        fileNameLabel.setMinimumSize(new Dimension(0,h));
        fileNameLabel.setPreferredSize(new Dimension(0,h));
        fileNameLabel.setMaximumSize(new Dimension(32767,h));

        h = fileNameTextField.getPreferredSize().height;
        fileNameTextField.setPreferredSize(new Dimension(0,h));
        fileNameTextField.setMinimumSize(new Dimension(0,h));
        fileNameTextField.setMaximumSize(new Dimension(32767,h));

        h = directoryComboBox.getPreferredSize().height;
        directoryComboBox.setPreferredSize(new Dimension(0,h));
        directoryComboBox.setMinimumSize(new Dimension(0,h));
        directoryComboBox.setMaximumSize(new Dimension(32767,h));

        Dimension d = new Dimension(28,25);
        Dimension d2 = new Dimension(29,25);
        previousButton.setPreferredSize(d);
        nextButton.setPreferredSize(d2);
        tableToggleButton.setPreferredSize(d);
        browserToggleButton.setPreferredSize(d2);
        previousButton.setMinimumSize(d);
        nextButton.setMinimumSize(d2);
        tableToggleButton.setMinimumSize(d);
        browserToggleButton.setMinimumSize(d2);
        previousButton.setBorder(cellBorder);
        nextButton.setBorder(buttonBorder);
        tableToggleButton.setBorder(cellBorder);
        browserToggleButton.setBorder(buttonBorder);
        
        previousButton.setVisible(false);
        nextButton.setVisible(false);
        tableToggleButton.setVisible(false);
        browserToggleButton.setVisible(false);

        h = navigationButtonsPanel.getPreferredSize().height;
        navigationButtonsPanel.setMinimumSize(new Dimension(0,h));
        navigationButtonsPanel.setPreferredSize(new Dimension(0,h));
        navigationButtonsPanel.setMaximumSize(new Dimension(32767,h));

        h = filesOfTypeLabel.getPreferredSize().height;
        filesOfTypeLabel.setMinimumSize(new Dimension(0,h));
        filesOfTypeLabel.setPreferredSize(new Dimension(0,h));
        filesOfTypeLabel.setMaximumSize(new Dimension(32767,h));

        h = filterComboBox.getPreferredSize().height;
        filterComboBox.setPreferredSize(new Dimension(0,h));
        filterComboBox.setMinimumSize(new Dimension(0,h));
        filterComboBox.setMaximumSize(new Dimension(32767,h));
        
        splitPane.putClientProperty("Quaqua.SplitPane.style","bar");
        separator.putClientProperty("Quaqua.Component.visualMargin", new Insets(3, 0, 3, 0));

    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
        } catch (Throwable t) {
        }
        JFrame f = new JFrame("Open Dialog");
        f.getContentPane().add(new QuaquaLeopardFileChooserPanel());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ((JComponent) f.getContentPane()).setMinimumSize(new Dimension(518,300));
        ((JComponent) f.getContentPane()).setPreferredSize(new Dimension(518,300));
        f.pack();
        f.setVisible(true);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        viewGroup = new javax.swing.ButtonGroup();
        fc = new javax.swing.JPanel();
        fileNamePanel = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        fileNameSpringPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        navigationPanel = new javax.swing.JPanel();
        navigationButtonsPanel = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        tableToggleButton = new javax.swing.JToggleButton();
        browserToggleButton = new javax.swing.JToggleButton();
        directoryComboBox = new javax.swing.JComboBox();
        navigationSpringPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        sidebarScrollPane = new javax.swing.JScrollPane();
        sidebarTree = new javax.swing.JTree();
        viewsPanel = new javax.swing.JPanel();
        browserScrollPane = new javax.swing.JScrollPane();
        browser = new ch.randelshofer.quaqua.JBrowser();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        controlsPanel = new javax.swing.JPanel();
        accessoryPanel = new javax.swing.JPanel();
        formatPanel = new javax.swing.JPanel();
        filesOfTypeLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox();
        formatSpringPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        newFolderButton = new javax.swing.JButton();
        cancelOpenPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        approveButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        fc.setLayout(new java.awt.BorderLayout());

        fc.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 10, 0));
        fileNamePanel.setLayout(new java.awt.GridBagLayout());

        fileNamePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 1, 0));
        fileNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fileNameLabel.setText("Save As:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 6);
        fileNamePanel.add(fileNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        fileNamePanel.add(fileNameTextField, gridBagConstraints);

        fileNameSpringPanel.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        fileNamePanel.add(fileNameSpringPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        fileNamePanel.add(separator, gridBagConstraints);

        fc.add(fileNamePanel, java.awt.BorderLayout.NORTH);

        mainPanel.setLayout(new java.awt.BorderLayout());

        navigationPanel.setLayout(new java.awt.GridBagLayout());

        navigationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 4, 8));
        navigationButtonsPanel.setLayout(new java.awt.GridBagLayout());

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.previousIcon.png")));
        navigationButtonsPanel.add(previousButton, new java.awt.GridBagConstraints());

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.nextIcon.png")));
        navigationButtonsPanel.add(nextButton, new java.awt.GridBagConstraints());

        viewGroup.add(tableToggleButton);
        tableToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.tableIcon.png")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        navigationButtonsPanel.add(tableToggleButton, gridBagConstraints);

        viewGroup.add(browserToggleButton);
        browserToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/randelshofer/quaqua/panther/images/FileChooser.browserIcon.png")));
        browserToggleButton.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        navigationButtonsPanel.add(browserToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        navigationPanel.add(navigationButtonsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        navigationPanel.add(directoryComboBox, gridBagConstraints);

        navigationSpringPanel.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        navigationPanel.add(navigationSpringPanel, gridBagConstraints);

        mainPanel.add(navigationPanel, java.awt.BorderLayout.NORTH);

        splitPane.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        splitPane.setDividerLocation(134);
        splitPane.setDividerSize(1);
        sidebarScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sidebarScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarTree.setRootVisible(false);
        sidebarTree.setShowsRootHandles(true);
        sidebarScrollPane.setViewportView(sidebarTree);

        splitPane.setLeftComponent(sidebarScrollPane);

        viewsPanel.setLayout(new java.awt.CardLayout());

        browserScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        browserScrollPane.setViewportView(browser);

        viewsPanel.add(browserScrollPane, "browser");

        tableScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableScrollPane.setViewportView(table);

        viewsPanel.add(tableScrollPane, "table");

        splitPane.setRightComponent(viewsPanel);

        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        fc.add(mainPanel, java.awt.BorderLayout.CENTER);

        controlsPanel.setLayout(new javax.swing.BoxLayout(controlsPanel, javax.swing.BoxLayout.Y_AXIS));

        accessoryPanel.setLayout(new java.awt.BorderLayout());

        accessoryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 0, 8));
        controlsPanel.add(accessoryPanel);

        formatPanel.setLayout(new java.awt.GridBagLayout());

        formatPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        filesOfTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        filesOfTypeLabel.setText("Format:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        formatPanel.add(filesOfTypeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 250;
        formatPanel.add(filterComboBox, gridBagConstraints);

        formatSpringPanel.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        formatPanel.add(formatSpringPanel, gridBagConstraints);

        controlsPanel.add(formatPanel);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 20, 0, 20));
        newFolderButton.setText("New Folder");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        buttonsPanel.add(newFolderButton, gridBagConstraints);

        cancelOpenPanel.setLayout(new java.awt.GridLayout(1, 0, 8, 0));

        cancelButton.setText("Cancel");
        cancelOpenPanel.add(cancelButton);

        approveButton.setText("Open");
        cancelOpenPanel.add(approveButton);

        buttonsPanel.add(cancelOpenPanel, new java.awt.GridBagConstraints());

        controlsPanel.add(buttonsPanel);

        fc.add(controlsPanel, java.awt.BorderLayout.SOUTH);

        add(fc, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accessoryPanel;
    private javax.swing.JButton approveButton;
    private ch.randelshofer.quaqua.JBrowser browser;
    private javax.swing.JScrollPane browserScrollPane;
    private javax.swing.JToggleButton browserToggleButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cancelOpenPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JComboBox directoryComboBox;
    private javax.swing.JPanel fc;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPanel fileNamePanel;
    private javax.swing.JPanel fileNameSpringPanel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel filesOfTypeLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JPanel formatPanel;
    private javax.swing.JPanel formatSpringPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel navigationButtonsPanel;
    private javax.swing.JPanel navigationPanel;
    private javax.swing.JPanel navigationSpringPanel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JScrollPane sidebarScrollPane;
    private javax.swing.JTree sidebarTree;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToggleButton tableToggleButton;
    private javax.swing.ButtonGroup viewGroup;
    private javax.swing.JPanel viewsPanel;
    // End of variables declaration//GEN-END:variables
    
}
