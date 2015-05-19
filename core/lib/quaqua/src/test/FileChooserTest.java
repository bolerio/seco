/*
 * @(#)FileChooser.java  1.0  13 February 2005
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.filechooser.QuaquaFileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

/**
 * FileChooser.
 *
 * @author  Werner Randelshofer
 * @version 1.0  13 February 2005  Created.
 */
public class FileChooserTest extends javax.swing.JPanel {

    private FileDialog fileDialog;
    private JFileChooser fileChooser;
    private boolean isUseCustomFileSystemView;

    private static class MyFilenameFilter extends javax.swing.filechooser.FileFilter implements FilenameFilter {

        private HashSet extSet = new HashSet();
        private String extensions;

        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }

        public void setExtensions(String newValue) {
            extensions = newValue;
            extSet.clear();

            StringTokenizer tt = new StringTokenizer(newValue);
            while (tt.hasMoreTokens()) {
                extSet.add(tt.nextToken());
            }

            //extSet.addAll(Arrays.asList(newValue.toLowerCase().split(" ")));
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String name = file.getName();
            int p = name.lastIndexOf(".");
            if (p != -1) {
                return extSet.contains(name.substring(p + 1).toLowerCase());
            }
            return false;
        }

        public String getDescription() {
            return extensions;
        }
    }
    private MyFilenameFilter filter = new MyFilenameFilter();

    /** Creates new form. */
    public FileChooserTest() {
        initComponents();
        hiddenFilesItem.setSelected(//
                UIManager.get("FileChooser.fileHidingEnabled") != null //
                && !UIManager.getBoolean("FileChooser.fileHidingEnabled"));
        setSelectedFileField.setText(QuaquaManager.getProperty("user.home"));
    }

    private void configureFileDialog() {
        if (fileDialog == null) {
            fileDialog = new FileDialog((Frame) SwingUtilities.getWindowAncestor(this));
        }
        filter.setExtensions(filterFilesField.getText());
        fileDialog.setFilenameFilter(filterFilesItem.isSelected() ? filter : null);
    }

    private void configureFileChooser() {
        if (fileChooser == null) {
            long start = System.currentTimeMillis();
            fileChooser = new JFileChooser() {
                /*
                public void repaint(long tm, int x, int y, int w, int h) {
                System.out.println("FileChooserTest.repaint "+x+","+y+","+w+","+h);
                super.repaint(tm, x, y, w, h);
                }/*
                public void paint(Graphics g) {
                long start = System.currentTimeMillis();
                super.paint(g);
                long end = System.currentTimeMillis();
                System.out.println("FileChooserTest.paint e="+(end-start)+" "+g.getClipBounds());
                //System.out.println("FileChooserTest.paint "+EventQueue.getCurrentEvent());
                }*/

                @Override
                public void updateUI() {
                    long start = System.currentTimeMillis();
                    super.updateUI();
                    long end = System.currentTimeMillis();
                    System.out.println("FileChooserTest.updateUI e=" + (end - start));
                }
            };
            long end = System.currentTimeMillis();
            System.out.println("FileChooserTest newFileChooser elapsed=" + (end - start));
        }
        fileChooser.setApproveButtonText(
                customApproveItem.isSelected() ? customApproveField.getText() : null);

        filter.setExtensions(filterFilesField.getText());
        fileChooser.setFileFilter(filterFilesItem.isSelected() ? filter : null);
        fileChooser.setFileSelectionMode(
                selectFilesOnlyItem.isSelected() ? JFileChooser.FILES_ONLY : (selectDirectoriesOnlyItem.isSelected() ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES));
        fileChooser.setFileHidingEnabled(!hiddenFilesItem.isSelected());
        fileChooser.setMultiSelectionEnabled(multiSelectionItem.isSelected());
        fileChooser.putClientProperty("Quaqua.FileChooser", ui);
        if (accessoryCheckBox.isSelected()) {
            if (!(fileChooser.getAccessory() instanceof Accessory)) {
                Accessory pa = new Accessory(fileChooser);
                fileChooser.setAccessory(pa);
            }
        } else {
            if (fileChooser.getAccessory() instanceof Accessory) {
                Accessory pa = (Accessory) fileChooser.getAccessory();
                fileChooser.setAccessory(null);
                pa.dispose();
            }
        }
        if (previewCheckBox.isSelected()) {
            if (!(fileChooser.getClientProperty("Quaqua.FileChooser.preview") instanceof Preview)) {
                Preview pa = new Preview(fileChooser);
                fileChooser.putClientProperty("Quaqua.FileChooser.preview", pa);
            }
        } else {
            if (fileChooser.getClientProperty("Quaqua.FileChooser.preview") instanceof Preview) {
                Preview pa = (Preview) fileChooser.getClientProperty("Quaqua.FileChooser.preview");
                fileChooser.putClientProperty("Quaqua.FileChooser.preview", null);
                pa.dispose();
            }
        }

        if (customFileSystemViewItem.isSelected() != isUseCustomFileSystemView) {
            isUseCustomFileSystemView = customFileSystemViewItem.isSelected();
            if (customFileSystemViewItem.isSelected()) {
                fileChooser.setFileSystemView(FileSystemView.getFileSystemView());
            } else {
                fileChooser.setFileSystemView(QuaquaFileSystemView.getQuaquaFileSystemView());
            }
        }
    }

    private void updateButtons() {
        boolean b =
                !customApproveItem.isSelected()
                && selectFilesOnlyItem.isSelected()
                && !multiSelectionItem.isSelected()
                && !hiddenFilesItem.isSelected();

        saveFileDialogButton.setEnabled(b);
        openFileDialogButton.setEnabled(b);
    }

    private void analyzeOption(JFileChooser fc, int option) {
        StringBuffer buf = new StringBuffer();
        switch (option) {
            case JFileChooser.CANCEL_OPTION:
                buf.append("canceled");
                break;
            case JFileChooser.APPROVE_OPTION:
                buf.append("approved\n");
                buf.append("file:" + fc.getSelectedFile() + "\n");
                if (fc.getSelectedFile() != null) {
                    setSelectedFileField.setText(fc.getSelectedFile().toString());
                }
                buf.append("files:" + Arrays.asList(fc.getSelectedFiles()) + "\n");
                buf.append("directory:" + fc.getCurrentDirectory() + "\n");
                break;
            case JFileChooser.ERROR_OPTION:
                buf.append("error");
                break;

        }
        outputField.setText(buf.toString());
    }

    private void analyzeOption(FileDialog fd) {
        StringBuffer buf = new StringBuffer();
        buf.append("file:" + fd.getFile() + "\n");
        outputField.setText(buf.toString());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JFrame f = new JFrame("FileChooserTest");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(new FileChooserTest());
                f.pack();
                f.setVisible(true);
            }
        });
    }
    // TODO code application logic here

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectionTypeGroup = new javax.swing.ButtonGroup();
        settingsPanel = new javax.swing.JPanel();
        selectFilesOnlyItem = new javax.swing.JRadioButton();
        selectDirectoriesOnlyItem = new javax.swing.JRadioButton();
        selectFilesAndDirectoriesItem = new javax.swing.JRadioButton();
        multiSelectionItem = new javax.swing.JCheckBox();
        hiddenFilesItem = new javax.swing.JCheckBox();
        customApproveItem = new javax.swing.JCheckBox();
        customApproveField = new javax.swing.JTextField();
        filterFilesItem = new javax.swing.JCheckBox();
        filterFilesField = new javax.swing.JTextField();
        customFileSystemViewItem = new javax.swing.JCheckBox();
        accessoryCheckBox = new javax.swing.JCheckBox();
        previewCheckBox = new javax.swing.JCheckBox();
        actionPanel = new javax.swing.JPanel();
        openFileDialogButton = new javax.swing.JButton();
        saveFileDialogButton = new javax.swing.JButton();
        openFileButton = new javax.swing.JButton();
        saveFileButton = new javax.swing.JButton();
        resetPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        setSelectedFileButton = new javax.swing.JButton();
        createWithButton = new javax.swing.JButton();
        setSelectedFileField = new javax.swing.JTextField();
        resetPanel1 = new javax.swing.JPanel();
        setDirectoryButton = new javax.swing.JButton();
        setDirectoryField = new javax.swing.JTextField();
        outputPanel = new javax.swing.JPanel();
        outputLabel = new javax.swing.JLabel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputField = new javax.swing.JTextArea();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 20, 20, 20));
        setLayout(new java.awt.GridBagLayout());

        settingsPanel.setLayout(new java.awt.GridBagLayout());

        selectionTypeGroup.add(selectFilesOnlyItem);
        selectFilesOnlyItem.setSelected(true);
        selectFilesOnlyItem.setText("Select files");
        selectFilesOnlyItem.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        settingsPanel.add(selectFilesOnlyItem, gridBagConstraints);

        selectionTypeGroup.add(selectDirectoriesOnlyItem);
        selectDirectoriesOnlyItem.setText("Select directories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(selectDirectoriesOnlyItem, gridBagConstraints);

        selectionTypeGroup.add(selectFilesAndDirectoriesItem);
        selectFilesAndDirectoriesItem.setText("Select files and directories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(selectFilesAndDirectoriesItem, gridBagConstraints);

        multiSelectionItem.setText("Allow multiple selection");
        multiSelectionItem.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 0, 0);
        settingsPanel.add(multiSelectionItem, gridBagConstraints);

        hiddenFilesItem.setText("Show hidden files");
        hiddenFilesItem.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        settingsPanel.add(hiddenFilesItem, gridBagConstraints);

        customApproveItem.setText("Use custom approve button text");
        customApproveItem.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        settingsPanel.add(customApproveItem, gridBagConstraints);

        customApproveField.setText("Approve");
        customApproveField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 0);
        settingsPanel.add(customApproveField, gridBagConstraints);

        filterFilesItem.setText("Filter files");
        filterFilesItem.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 0, 0);
        settingsPanel.add(filterFilesItem, gridBagConstraints);

        filterFilesField.setText("gif jpg png");
        filterFilesField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 48, 0, 0);
        settingsPanel.add(filterFilesField, gridBagConstraints);

        customFileSystemViewItem.setText("Use custom FileSystemView");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        settingsPanel.add(customFileSystemViewItem, gridBagConstraints);

        accessoryCheckBox.setText("Use Accessory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        settingsPanel.add(accessoryCheckBox, gridBagConstraints);

        previewCheckBox.setText("Use custom preview");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 0, 0);
        settingsPanel.add(previewCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(settingsPanel, gridBagConstraints);

        actionPanel.setLayout(new java.awt.GridLayout(0, 2));

        openFileDialogButton.setText("Open AWT File Dialog");
        openFileDialogButton.addActionListener(formListener);
        actionPanel.add(openFileDialogButton);

        saveFileDialogButton.setText("Save AWT File Dialog");
        saveFileDialogButton.addActionListener(formListener);
        actionPanel.add(saveFileDialogButton);

        openFileButton.setText("Open Swing File Chooser");
        openFileButton.addActionListener(formListener);
        actionPanel.add(openFileButton);

        saveFileButton.setText("Save Swing File Chooser");
        saveFileButton.addActionListener(formListener);
        actionPanel.add(saveFileButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        add(actionPanel, gridBagConstraints);

        resetPanel.setLayout(new java.awt.GridBagLayout());

        resetButton.setText("Reset to:");
        resetButton.addActionListener(formListener);
        resetPanel.add(resetButton, new java.awt.GridBagConstraints());

        setSelectedFileButton.setText("Set to:");
        setSelectedFileButton.addActionListener(formListener);
        resetPanel.add(setSelectedFileButton, new java.awt.GridBagConstraints());

        createWithButton.setText("Create With:");
        createWithButton.addActionListener(formListener);
        resetPanel.add(createWithButton, new java.awt.GridBagConstraints());

        setSelectedFileField.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        resetPanel.add(setSelectedFileField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        add(resetPanel, gridBagConstraints);

        resetPanel1.setLayout(new java.awt.GridBagLayout());

        setDirectoryButton.setText("Set directory to:");
        setDirectoryButton.addActionListener(formListener);
        resetPanel1.add(setDirectoryButton, new java.awt.GridBagConstraints());

        setDirectoryField.setText("/Applications");
        setDirectoryField.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        resetPanel1.add(setDirectoryField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(resetPanel1, gridBagConstraints);

        outputPanel.setLayout(new java.awt.GridBagLayout());

        outputLabel.setText("Output:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        outputPanel.add(outputLabel, gridBagConstraints);

        outputField.setEditable(false);
        outputField.setLineWrap(true);
        outputField.setRows(5);
        outputScrollPane.setViewportView(outputField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        outputPanel.add(outputScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        add(outputPanel, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ItemListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == openFileDialogButton) {
                FileChooserTest.this.openFileDialog(evt);
            }
            else if (evt.getSource() == saveFileDialogButton) {
                FileChooserTest.this.saveFileDialog(evt);
            }
            else if (evt.getSource() == openFileButton) {
                FileChooserTest.this.openFileChooser(evt);
            }
            else if (evt.getSource() == saveFileButton) {
                FileChooserTest.this.saveFileChooser(evt);
            }
            else if (evt.getSource() == resetButton) {
                FileChooserTest.this.reset(evt);
            }
            else if (evt.getSource() == setSelectedFileButton) {
                FileChooserTest.this.setTo(evt);
            }
            else if (evt.getSource() == createWithButton) {
                FileChooserTest.this.createWith(evt);
            }
            else if (evt.getSource() == setSelectedFileField) {
                FileChooserTest.this.reset(evt);
            }
            else if (evt.getSource() == setDirectoryButton) {
                FileChooserTest.this.setDirectoryPerformed(evt);
            }
            else if (evt.getSource() == setDirectoryField) {
                FileChooserTest.this.setDirectoryPerformed(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == selectFilesOnlyItem) {
                FileChooserTest.this.selectionTypeGroupChanged(evt);
            }
            else if (evt.getSource() == multiSelectionItem) {
                FileChooserTest.this.multiSelectionItemChanged(evt);
            }
            else if (evt.getSource() == hiddenFilesItem) {
                FileChooserTest.this.hiddenFilesItemChanged(evt);
            }
            else if (evt.getSource() == customApproveItem) {
                FileChooserTest.this.customApproveItemChanged(evt);
            }
            else if (evt.getSource() == filterFilesItem) {
                FileChooserTest.this.filterFilesItemChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents
    private void createWith(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createWith
        reset(evt);
        fileChooser = new JFileChooser(setSelectedFileField.getText());

    }//GEN-LAST:event_createWith

    private void hiddenFilesItemChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hiddenFilesItemChanged
        updateButtons();
    }//GEN-LAST:event_hiddenFilesItemChanged

    private void multiSelectionItemChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_multiSelectionItemChanged
        updateButtons();
    }//GEN-LAST:event_multiSelectionItemChanged

    private void selectionTypeGroupChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectionTypeGroupChanged
        updateButtons();
    }//GEN-LAST:event_selectionTypeGroupChanged

    private void filterFilesItemChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterFilesItemChanged
        filterFilesField.setEnabled(filterFilesItem.isSelected());
    }//GEN-LAST:event_filterFilesItemChanged

    private void customApproveItemChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_customApproveItemChanged
        boolean b = customApproveItem.isSelected();
        customApproveField.setEnabled(b);
        updateButtons();
    }//GEN-LAST:event_customApproveItemChanged

    private void setTo(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setTo
        configureFileChooser();
        fileChooser.setSelectedFile(new File(setSelectedFileField.getText()));
        configureFileDialog();
        fileDialog.setFile(setSelectedFileField.getText());

    }//GEN-LAST:event_setTo

    private void reset(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset
        fileChooser = null;
        fileDialog = null;
        setTo(evt);
    }//GEN-LAST:event_reset

    private void saveFileChooser(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileChooser
        configureFileChooser();
        int option = fileChooser.showSaveDialog(this);
        analyzeOption(fileChooser, option);

    }//GEN-LAST:event_saveFileChooser

    private void openFileChooser(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileChooser
        configureFileChooser();
        int option = fileChooser.showOpenDialog(this);
        analyzeOption(fileChooser, option);

    }//GEN-LAST:event_openFileChooser

    private void saveFileDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileDialog
        configureFileDialog();
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.setVisible(true);
        analyzeOption(fileDialog);

    }//GEN-LAST:event_saveFileDialog

    private void openFileDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileDialog
        configureFileDialog();
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setVisible(true);
        analyzeOption(fileDialog);
    }//GEN-LAST:event_openFileDialog

    private void setDirectoryPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDirectoryPerformed
        configureFileChooser();
        fileChooser.setCurrentDirectory(new File(setDirectoryField.getText()));

}//GEN-LAST:event_setDirectoryPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox accessoryCheckBox;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton createWithButton;
    private javax.swing.JTextField customApproveField;
    private javax.swing.JCheckBox customApproveItem;
    private javax.swing.JCheckBox customFileSystemViewItem;
    private javax.swing.JTextField filterFilesField;
    private javax.swing.JCheckBox filterFilesItem;
    private javax.swing.JCheckBox hiddenFilesItem;
    private javax.swing.JCheckBox multiSelectionItem;
    private javax.swing.JButton openFileButton;
    private javax.swing.JButton openFileDialogButton;
    private javax.swing.JTextArea outputField;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JCheckBox previewCheckBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel resetPanel;
    private javax.swing.JPanel resetPanel1;
    private javax.swing.JButton saveFileButton;
    private javax.swing.JButton saveFileDialogButton;
    private javax.swing.JRadioButton selectDirectoriesOnlyItem;
    private javax.swing.JRadioButton selectFilesAndDirectoriesItem;
    private javax.swing.JRadioButton selectFilesOnlyItem;
    private javax.swing.ButtonGroup selectionTypeGroup;
    private javax.swing.JButton setDirectoryButton;
    private javax.swing.JTextField setDirectoryField;
    private javax.swing.JButton setSelectedFileButton;
    private javax.swing.JTextField setSelectedFileField;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables

    private static class Preview extends JLabel implements PropertyChangeListener {

        private JFileChooser fileChooser;

        public Preview(JFileChooser fc) {
            this.fileChooser = fc;
            setFont(new Font("Dialog", Font.PLAIN, 11));
            fileChooser.addPropertyChangeListener(this);
            setVerticalAlignment(JLabel.TOP);
            setHorizontalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setBackground(Color.WHITE);
            setOpaque(true);
            updatePreview();
        }

        private void updatePreview() {
            final File file = fileChooser.getSelectedFile();
            if (file == null || file.isDirectory()) {
                setIcon(null);
                setText(null);
            } else {
                new Thread() {

                    @Override
                    public void run() {
                        Image img = Toolkit.getDefaultToolkit().createImage(file.getPath());

                        final ImageIcon icon = new ImageIcon(img.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                setIcon(icon);
                                setText(file.getName());
                                repaint();
                            }
                        });
                    }
                }.start();
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                updatePreview();
            }

        }

        public void dispose() {
            fileChooser.removePropertyChangeListener(this);
        }
    }

    private static class Accessory extends JPanel implements PropertyChangeListener {

        private JCheckBox togglePreviewCheckBox;
        private JFileChooser fileChooser;

        public Accessory(JFileChooser fc) {
            this.fileChooser = fc;
            setBorder(new TitledBorder(""));

            JLabel l = new JLabel("Encoding:");
            JComboBox cb = new JComboBox();
            cb.setModel(new DefaultComboBoxModel(new String[]{"UTF-8", "UTF-16BE", "UTF-16LE", "ASCII"}));
            add(l);
            add(cb);

            togglePreviewCheckBox = new JCheckBox("Show custom preview");
            togglePreviewCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (togglePreviewCheckBox.isSelected()) {
                        if (fileChooser.getClientProperty("Quaqua.FileChooser.preview") instanceof Preview) {
                            Preview p = (Preview) fileChooser.getClientProperty("Quaqua.FileChooser.preview");
                            p.dispose();
                        }
                        fileChooser.putClientProperty("Quaqua.FileChooser.preview", new Preview(fileChooser));
                    } else {
                        if (fileChooser.getClientProperty("Quaqua.FileChooser.preview") instanceof Preview) {
                            Preview p = (Preview) fileChooser.getClientProperty("Quaqua.FileChooser.preview");
                            p.dispose();
                        }
                        fileChooser.putClientProperty("Quaqua.FileChooser.preview", null);
                    }
                }
            });
            add(togglePreviewCheckBox);
            togglePreviewCheckBox.setSelected(fileChooser.getClientProperty("Quaqua.FileChooser.preview") != null);

            fc.addPropertyChangeListener(this);
        }

        public void dispose() {
            fileChooser.removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("Quaqua.FileChooser.preview")) {
                togglePreviewCheckBox.setSelected(fileChooser.getClientProperty("Quaqua.FileChooser.preview") != null);
            }
        }
    }
}
