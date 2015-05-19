/*
 * @(#)QuaquaJaguarFileChooserUI.java  
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.jaguar;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.filechooser.*;

//import ch.randelshofer.gui.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A replacement for the AquaFileChooserUI. Provides a column view similar
 * to the one provided with the native Aqua user interface on Mac OS X 10.2
 * (Jaguar).
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaJaguarFileChooserUI.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaJaguarFileChooserUI extends BasicFileChooserUI {

    // Implementation derived from MetalFileChooserUI
    /* Models. */
    private DirectoryComboBoxModel directoryComboBoxModel;
    private Action directoryComboBoxAction = new DirectoryComboBoxAction();
    private FileView fileView;
    private FilterComboBoxModel filterComboBoxModel;
    private FileSystemTreeModel model = null;
    // Preferred and Minimum sizes for the dialog box
    private static int PREF_WIDTH = 430;
    private static int PREF_HEIGHT = 330;
    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);
    private static int MIN_WIDTH = 430;
    private static int MIN_HEIGHT = 330;
    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);
    // Labels, mnemonics, and tooltips (oh my!)
    private int lookInLabelMnemonic = 0;
    private String lookInLabelText = null;
    private String saveInLabelText = null;
    private int fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;
    ///private int filesOfTypeLabelMnemonic = 0;
    ///private String filesOfTypeLabelText = null;
    ///private String upFolderToolTipText = null;
    ///private String upFolderAccessibleName = null;
    ///private String homeFolderToolTipText = null;
    ///private String homeFolderAccessibleName = null;
    private String newFolderToolTipText = null;
    ///private String newFolderAccessibleName = null;
    protected String chooseButtonText = null;
    private String newFolderDialogPrompt,  newFolderDefaultName,  newFolderErrorText,  newFolderExistsErrorText;
    ///private String newFolderButtonText;
    private String newFolderTitleText;

    /**
     * This listener is used to determine whether the JFileChooser is showing.
     */
    private AncestorListener ancestorListener;
    /**
     * This listener is used to handle files that were dropped on the file chooser.
     */
    private FileTransferHandler fileTransferHandler;
    /**
     * Actions.
     */
    private Action newFolderAction = new NewFolderAction();
    private Action approveSelectionAction = new ApproveSelectionAction();
    /**
     * Values greater zero indicate that the UI is adjusting.
     * This is required to prevent the UI from changing the FileChooser's state
     * while processing a PropertyChangeEvent fired from the FileChooser.
     */
    private int isAdjusting = 0;
    // Variables declaration - do not modify
    private javax.swing.JPanel accessoryPanel;
    private javax.swing.JButton approveButton;
    private ch.randelshofer.quaqua.JBrowser browser;
    private javax.swing.JScrollPane browserScrollPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox directoryComboBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel filesOfTypeLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JPanel formatPanel;
    private javax.swing.JPanel formatPanel2;
    private javax.swing.JPanel fromPanel;
    private javax.swing.JLabel lookInLabel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JPanel separatorPanel;
    private javax.swing.JPanel separatorPanel1;
    private javax.swing.JPanel separatorPanel2;
    private javax.swing.JPanel strutPanel1;
    private javax.swing.JPanel strutPanel2;
    // End of variables declaration

    //
    // ComponentUI Interface Implementation methods
    //
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaJaguarFileChooserUI((JFileChooser) c);
    }

    public QuaquaJaguarFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
    }

    @Override
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        buttonPanel = null;
    }

    @Override
    public void installComponents(JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();

        // Form definition  - do not modify
        java.awt.GridBagConstraints gridBagConstraints;

        fromPanel = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        strutPanel1 = new javax.swing.JPanel();
        lookInLabel = new javax.swing.JLabel();
        directoryComboBox = new javax.swing.JComboBox();
        strutPanel2 = new javax.swing.JPanel();
        separatorPanel1 = new javax.swing.JPanel();
        separatorPanel2 = new javax.swing.JPanel();
        browserScrollPane = new javax.swing.JScrollPane();
        browser = new ch.randelshofer.quaqua.JBrowser();
        newFolderButton = new javax.swing.JButton();
        separatorPanel = new javax.swing.JPanel();
        formatPanel = new javax.swing.JPanel();
        formatPanel2 = new javax.swing.JPanel();
        filesOfTypeLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox();
        accessoryPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        approveButton = new javax.swing.JButton();

        fc.setLayout(new java.awt.GridBagLayout());

        fromPanel.setLayout(new java.awt.GridBagLayout());

        fileNameLabel.setText(UIManager.getString("FileChooser.fileNameLabelText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        fromPanel.add(fileNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 14, 0);
        gridBagConstraints.weightx = 1.0;
        fromPanel.add(fileNameTextField, gridBagConstraints);

        strutPanel1.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 5;
        fromPanel.add(strutPanel1, gridBagConstraints);

        lookInLabel.setText(UIManager.getString("FileChooser.fromLabelText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        fromPanel.add(lookInLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        fromPanel.add(directoryComboBox, gridBagConstraints);

        strutPanel2.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 5;
        fromPanel.add(strutPanel2, gridBagConstraints);

        separatorPanel1.setLayout(new java.awt.BorderLayout());

        separatorPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weightx = 1.0E-4;
        fromPanel.add(separatorPanel1, gridBagConstraints);

        separatorPanel2.setLayout(new java.awt.BorderLayout());

        separatorPanel2.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 1;
        fromPanel.add(separatorPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(fromPanel, gridBagConstraints);

        browserScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        browserScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        browserScrollPane.setViewportView(browser);
        browserScrollPane.setPreferredSize(new Dimension(388, 298));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 23, 0, 23);
        fc.add(browserScrollPane, gridBagConstraints);

        newFolderButton.setText(UIManager.getString("FileChooser.newFolderButtonText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        fc.add(newFolderButton, gridBagConstraints);

        separatorPanel.setLayout(new java.awt.BorderLayout());

        separatorPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(separatorPanel, gridBagConstraints);

        formatPanel.setLayout(new java.awt.GridBagLayout());

        formatPanel2.setLayout(new java.awt.BorderLayout(2, 0));

        filesOfTypeLabel.setText(UIManager.getString("FileChooser.filesOfTypeLabelText"));
        formatPanel2.add(filesOfTypeLabel, java.awt.BorderLayout.WEST);

        formatPanel2.add(filterComboBox, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 40);
        formatPanel.add(formatPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(formatPanel, gridBagConstraints);

        accessoryPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(14, 20, 0, 20);
        fc.add(accessoryPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText(UIManager.getString("FileChooser.cancelButtonText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 16, 0);
        buttonPanel.add(cancelButton, gridBagConstraints);

        approveButton.setText(UIManager.getString("FileChooser.openButtonText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 16, 22);
        buttonPanel.add(approveButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(buttonPanel, gridBagConstraints);
        // End of form definition

        //Configure JBrowser
        browser.setColumnCellRenderer(
                new FileRenderer(
                fc,
                UIManager.getIcon("Browser.expandingIcon"),
                UIManager.getIcon("Browser.expandedIcon"),
                UIManager.getIcon("Browser.selectedExpandingIcon"),
                UIManager.getIcon("Browser.selectedExpandedIcon"),
                UIManager.getIcon("Browser.focusedSelectedExpandingIcon"),
                UIManager.getIcon("Browser.focusedSelectedExpandedIcon")
                ));
        if (fc.isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        browser.setModel(getTreeModel());
        browser.setPrototypeCellValue(getTreeModel().getPrototypeValue());
        browser.addTreeSelectionListener(createBrowserSelectionListener(fc));
        browser.addMouseListener(createDoubleClickListener(fc));

        // Configure separator panels
        separatorPanel.setOpaque(true);
        separatorPanel1.setOpaque(true);
        separatorPanel2.setOpaque(true);

        // Configure Format Panel
        formatPanel.setVisible(fc.getChoosableFileFilters().length > 1);

        // Configure Accessory Panel
        JComponent accessory = fc.getAccessory();
        if (accessory != null) {
            getAccessoryPanel().add(accessory);
        } else {
            accessoryPanel.setVisible(false);
        }

        // Text assignment
        lookInLabel.setText(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        newFolderButton.setText(newFolderTitleText);
        newFolderButton.setToolTipText(newFolderToolTipText);
        fileNameLabel.setText(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);

        approveButton.setText(getApproveButtonText(fc));
        // Note: Metal does not use mnemonics for approve and cancel
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));

        cancelButton.setText(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());

        if (!fc.getControlButtonsAreShown()) {
            cancelButton.setVisible(false);
            approveButton.setVisible(false);
        }
        // End of Text assignment

        // Model and Renderer assignment
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox.setModel(filterComboBoxModel);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        // Model and Renderer assignment

        // Listener assignment
        directoryComboBox.addActionListener(directoryComboBoxAction);
        newFolderButton.addActionListener(getNewFolderAction());
        fileNameTextField.addFocusListener(new SaveTextFocusListener());
        fileNameTextField.setDocument(new FilenameDocument());
        fileNameTextField.getDocument().addDocumentListener(new SaveTextDocumentListener());
        fileNameTextField.addActionListener(getApproveSelectionAction());
        // End of listener assignment

        // Drag and drop assignment
        fileTransferHandler = new FileTransferHandler(fc);
        Component[] dropComponents = {
            fc,
            accessoryPanel,
            approveButton,
            browser,
            browserScrollPane,
            buttonPanel,
            cancelButton,
            directoryComboBox,
            fileNameLabel,
            fileNameTextField,
            filesOfTypeLabel,
            filterComboBox,
            formatPanel,
            formatPanel2,
            fromPanel,
            lookInLabel,
            newFolderButton,
            separatorPanel,
            separatorPanel1,
            separatorPanel2,
            strutPanel1,
            strutPanel2
        };
        for (int i = 0; i < dropComponents.length; i++) {
            new DropTarget(dropComponents[i], DnDConstants.ACTION_COPY, fileTransferHandler);
        }
        // End of drag and drop assignment


        // Change component visibility to match the dialog type
        boolean isSave = (fc.getDialogType() == JFileChooser.SAVE_DIALOG) || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
        lookInLabel.setText((isSave) ? saveInLabelText : lookInLabelText);
        fileNameLabel.setVisible(isSave);
        fileNameTextField.setVisible(isSave);
        fileNameTextField.setEnabled(isSave);
        updateSeparatorPanelVisibility();
        separatorPanel1.setVisible(isSave);
        separatorPanel2.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        newFolderButton.setVisible(isSave);


        // Enforce layout, so that the selected file is visible when the
        // file chooser is opened with its preferred size.
        Dimension ps = getMinimumSize(fc);
        fc.setBounds(0, 0, ps.width, ps.height);
        fc.doLayout();
    }

    @Override
    public JPanel getAccessoryPanel() {
        return accessoryPanel;
    }

    @Override
    protected void installDefaults(JFileChooser fc) {
        super.installDefaults(fc);

        Object value = UIManager.get("FileChooser.fileHidingEnabled");
        boolean booleanValue = (value instanceof Boolean) ? ((Boolean)value).booleanValue() : true;
        fc.setFileHidingEnabled(booleanValue);
    }

    @Override
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);

        Locale l;
        try {
            l = fc.getLocale();
        } catch (IllegalComponentStateException e) {
            l = Locale.getDefault();
        }

        chooseButtonText = UIManager.getString("FileChooser.chooseButtonText"/*,l*/);

        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText"/*,l*/);
        if (lookInLabelText == null) {
            lookInLabelText = UIManager.getString("FileChooser.fromLabelText");
        }
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText"/*,l*/);
        if (saveInLabelText == null) {
            saveInLabelText = UIManager.getString("FileChooser.whereLabelText");
        }

        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText"/*,l*/);
        // XXX - Localize "Save as:" text.
        //if (fileNameLabelText == null || fileNameLabelText.charAt(fileNameLabelText.length() -1) != ':') fileNameLabelText = "Save as:";

        ///filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        ///filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText"/*,l*/);

        ///upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText"/*,l*/);
        ///upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName"/*,l*/);

        ///homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText"/*,l*/);
        ///homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName"/*,l*/);

        // New Folder Dialog
        newFolderErrorText = getString("FileChooser.newFolderErrorText", l, "Error occured during folder creation");
        newFolderExistsErrorText = getString("FileChooser.newFolderExistsErrorText", l, "That name is already taken");
        ///newFolderButtonText = getString("FileChooser.newFolderButtonText", l, "New");
        newFolderTitleText = getString("FileChooser.newFolderTitleText", l, "New Folder");
        newFolderDialogPrompt = getString("FileChooser.newFolderPromptText", l, "Name of new folder:");
        newFolderDefaultName = getString("FileChooser.untitledFolderName", l, "untitled folder");
        newFolderTitleText = UIManager.getString("FileChooser.newFolderTitleText"/*, l*/);
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText"/*, l*/);
        ///newFolderAccessibleName = getString("FileChooser.newFolderAccessibleName", l, newFolderTitleText);
    }

    /**
     * Gets a locale dependent string.
     */
    private String getString(String string, Locale l, String defaultValue) {
        String value = UIManager.getString(string/*, l*/);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Installs listeners.
     * We install the same listeners as BasicFileChooserUI plus an
     * AncestorListener.
     */
    @Override
    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        ancestorListener = createAncestorListener(fc);
        if (ancestorListener != null) {
            fc.addAncestorListener(ancestorListener);
        }
    }

    @Override
    protected void uninstallListeners(JFileChooser fc) {
        super.uninstallListeners(fc);
        if (ancestorListener != null) {
            fc.removeAncestorListener(ancestorListener);
        }
    }

    /**
     * Creates an AncestorListener.
     * The AncestorListener is used to take an action when the JFileChooser becomes
     * showing on screen.
     */
    protected AncestorListener createAncestorListener(JFileChooser fc) {
        return new FileChooserAncestorListener();
    }

    @Override
    public void createModel() {
        JFileChooser fc = getFileChooser();
        model = new FileSystemTreeModel(fc);
        model.setResolveFileLabels(false);
        fileView = QuaquaFileSystemView.getQuaquaFileSystemView().createFileView(fc);
        // FIXME - We should not overwrite the FileView attribute
        // of the JFileChooser.
        fc.setFileView(fileView);

        // FIXME - We should not overwrite the FileSystemView attribute
        // of the JFileChooser.
        fc.setFileSystemView(QuaquaFileSystemView.getQuaquaFileSystemView());
    }

    public FileSystemTreeModel getTreeModel() {
        return model;
    }

    @Override
    public void uninstallUI(JComponent c) {
        // Remove listeners
        c.removePropertyChangeListener(filterComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());

        super.uninstallUI(c);
    }

    /**
     * The array contains the selected file(s) of the JFileChooser.
     * All files have an absolute path.
     * If no file is selected, the length of the array is 0.
     * Always returns a non-null value.
     * All array elements are non-null.
     */
    private File[] getSelectedFiles() {
        JFileChooser fc = getFileChooser();

        if (fc.isMultiSelectionEnabled()) {
            File[] selectedFiles = fc.getSelectedFiles();
            ArrayList list = new ArrayList(selectedFiles.length);
            for (int i = 0; i < selectedFiles.length; i++) {
                if (selectedFiles[i] != null) {
                    if (selectedFiles[i].isAbsolute()) {
                        list.add(selectedFiles[i]);
                    } else {
                        list.add(new File(fc.getCurrentDirectory(), selectedFiles[i].getName()));
                    }
                }
            }
            return (File[]) list.toArray(new File[list.size()]);
        } else {
            File f = fc.getSelectedFile();
            if (f == null) {
                return new File[0];
            } else {
                if (f.isAbsolute()) {
                    return new File[]{f};
                } else {
                    return new File[]{new File(fc.getCurrentDirectory(), f.getName())};
                }
            }
        }
    }

    /**
     * Updates the selection in the JBrowser, to match the selected file/s
     * of the JFileChooser.
     */
    private void updateSelection() {
        JFileChooser fc = getFileChooser();

        File[] files = getSelectedFiles();
        if (files.length != 0) {
            TreePath[] paths = new TreePath[files.length];
            ArrayList list = new ArrayList(paths.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                boolean isDirectory = file.isDirectory() && fc.isTraversable(file);
                if (files.length == 1 || !isDirectory || fc.isDirectorySelectionEnabled()) {
                    list.add(getTreeModel().toPath(file, browser.getSelectionPath()));
                }
            }
            if (list.size() == 0 && files.length > 0) {
                list.add(fc.getFileSystemView().getParentDirectory(files[0]));
            }
            browser.setSelectionPaths((TreePath[]) list.toArray(new TreePath[list.size()]));

            // XXX If the selected file is not accepted by the file
            // name filters, we have to write its name into the file name field.
            if (files.length == 1 && !files[0].isDirectory() || !fc.isTraversable(files[0])) {
                setFileName(files[0].getName());
            }
        }

        if (files.length == 0) {
            directoryComboBoxModel.addItem(fc.getCurrentDirectory());
        } else if (files[0].isDirectory()) {
            directoryComboBoxModel.addItem(files[0]);
        } else {
            directoryComboBoxModel.addItem(files[0].getParentFile());
        }

        if (files.length == 1) {
            ensureFileIsVisible(fc, files[0]);
        }
        updateApproveButtonState();
    }

    /**
     * Returns true, if the file name field contains a file name.
     */
    private boolean isFileNameFieldValid() {
        String string = getFileName();
        return string != null && !string.equals("");
    }

    /**
     * Returns true, if the file name field is visible.
     */
    private boolean isFileNameFieldVisible() {
        JFileChooser fc = getFileChooser();
        return (fc.getDialogType() == JFileChooser.SAVE_DIALOG) || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
    }

    private void updateApproveButtonState() {
        JFileChooser fc = getFileChooser();

        if (fc.getControlButtonsAreShown()) {
            File[] files = getSelectedFiles();

            boolean isFileSelected = false;
            boolean isDirectorySelected = false;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory() && fc.isTraversable(files[i])) {
                    isDirectorySelected = true;
                } else {
                    isFileSelected = true;
                }
            }
            boolean isEnabled = false;
            switch (fc.getFileSelectionMode()) {
                case JFileChooser.FILES_ONLY:
                    isEnabled = isFileSelected || isFileNameFieldVisible() && isFileNameFieldValid();
                    break;
                case JFileChooser.DIRECTORIES_ONLY:
                    /*
                    isEnabled = ! isFileSelected
                    && (isDirectorySelected || isFileNameFieldVisible() && isFileNameFieldValid());
                     **/
                    isEnabled = !isFileSelected || files.length == 1 && !files[0].exists();
                    break;
                case JFileChooser.FILES_AND_DIRECTORIES:
                    /*
                    isEnabled = isFileSelected || isDirectorySelected
                    || isFileNameFieldVisible() && isFileNameFieldValid();
                     */
                    isEnabled = true;
                    break;
            }
            approveButton.setEnabled(isEnabled);
            if (isEnabled) {
                JRootPane rp = approveButton.getRootPane();
                if (rp != null) {
                    rp.setDefaultButton(approveButton);
                }
            }
        }
    }

    private void updateApproveButtonText() {
        JFileChooser fc = getFileChooser();

        approveButton.setText(getApproveButtonText(fc));
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        approveButton.setMnemonic(getApproveButtonMnemonic(fc));
    //cancelButton.setToolTipText(getCancelButtonToolTipText(fc));
    }

    protected TreeSelectionListener createBrowserSelectionListener(JFileChooser fc) {
        return new BrowserSelectionListener();
    }

    /**
     * Selection listener for the list of files and directories.
     */
    protected class BrowserSelectionListener implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            if (isAdjusting != 0) {
                return;
            }
            JFileChooser fc = getFileChooser();
            TreePath path = browser.getSelectionPath();

            if (path != null) {
                model.lazyInvalidatePath(path);
                model.validatePath(path);
            }


            TreePath[] paths = browser.getSelectionPaths();

            // Determine the selected files. If multiple files are selected,
            // we strip directories from this list, if the JFileChooser does
            // not allow directory selection.
            int count = 0;
            File[] files = new File[(paths == null) ? 0 : paths.length];
            ArrayList list = new ArrayList(files.length);
            for (int i = 0; i < files.length; i++) {
                File file = ((FileSystemTreeModel.Node) paths[i].getLastPathComponent()).getResolvedFile();
                boolean isDirectory = file.isDirectory() && fc.isTraversable(file);
                if (files.length == 1 || !isDirectory || fc.isDirectorySelectionEnabled()) {
                    list.add(file);
                }
            }


            if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles((File[]) list.toArray(new File[list.size()]));
            } else {
                fc.setSelectedFile((list.size() > 0) ? (File) list.get(0) : null);
            }
        }
    }

    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(
                    Math.max(d.width, PREF_SIZE.width),
                    Math.max(d.height, PREF_SIZE.height));
        } else {
            return new Dimension(PREF_SIZE.width, PREF_SIZE.height);
        }
    }

    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }

    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /* The following methods are used by the PropertyChange Listener */
    private void doSelectedFileChanged(PropertyChangeEvent e) {
        updateSelection();
    }

    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        updateSelection();
    }

    private void doDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();

        File[] files = getSelectedFiles();

        if (files.length == 0) {
            File dir = (File) e.getNewValue();
            directoryComboBoxModel.addItem(dir);
            browser.setSelectionPath(model.toPath(dir, browser.getSelectionPath()));
            model.lazyInvalidatePath(browser.getSelectionPath());

            if (dir != null) {
                getNewFolderAction().setEnabled(dir.canWrite());
                getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(dir));

                if (fc.getDialogType() == JFileChooser.OPEN_DIALOG) {
                    updateApproveButtonState();
                }

            }
        }
    }

    private void doFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
        model.invalidatePath(browser.getSelectionPath());
        if (getFileChooser().isShowing()) {
            model.validatePath(browser.getSelectionPath());
        }
    }

    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        //Commented out, because there is no reason for clearing the icon cache
        //in this situation.
        //clearIconCache();

        updateApproveButtonText();
        updateApproveButtonState();
    }

    private void doMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            getFileChooser().setSelectedFiles(null);
        }
    }

    private void doChoosableFilterChanged(PropertyChangeEvent e) {
        boolean isChooserVisible = ((FileFilter[]) e.getNewValue()).length > 1;
        formatPanel.setVisible(isChooserVisible);
        updateSeparatorPanelVisibility();
    }

    private void doAccessoryChanged(PropertyChangeEvent e) {
        if (getAccessoryPanel() != null) {
            if (e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if (accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
            accessoryPanel.setVisible(accessory != null);
        }
        updateSeparatorPanelVisibility();
    }

    private void doApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }

    private void doDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        approveButton.setText(getApproveButtonText(fc));
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        boolean isSave = isFileNameFieldVisible();
        lookInLabel.setText((isSave) ? saveInLabelText : lookInLabelText);
        fileNameLabel.setVisible(isSave);
        fileNameTextField.setVisible(isSave);
        fileNameTextField.setEnabled(isSave);
        updateSeparatorPanelVisibility();
        separatorPanel1.setVisible(isSave);
        separatorPanel2.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        newFolderButton.setVisible(isSave);
    //model.setResolveAliasesToFiles(! isSave);
    }

    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        // Note: Metal does not use mnemonics for approve and cancel
    }

    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if (getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }

    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    @Override
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                isAdjusting++;

                String s = e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if (s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    doMultiSelectionChanged(e);
                } else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if (s.equals(JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY)) {
                    doChoosableFilterChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                        s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals("componentOrientation")) {
                    /* FIXME - This needs JDK 1.4 to work.
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser fc = (JFileChooser)e.getSource();
                    if (o != (ComponentOrientation)e.getOldValue()) {
                    fc.applyComponentOrientation(o);
                    }
                     */
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        // Ancestor was added, ensure path is visible and
                        // set initial focus
                        browser.ensurePathIsVisible(browser.getSelectionPath());
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }

                isAdjusting--;
            }
        };
    }

    private void updateSeparatorPanelVisibility() {
        JFileChooser fc = getFileChooser();

        boolean isSave = (fc.getDialogType() == JFileChooser.SAVE_DIALOG) || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);

        separatorPanel.setVisible(
                isSave && (fc.getControlButtonsAreShown() || !fc.isAcceptAllFileFilterUsed() || fc.getAccessory() != null));
    }

    protected void removeControlButtons() {
        buttonPanel.setVisible(false);
        updateSeparatorPanelVisibility();
    }

    protected void addControlButtons() {
        buttonPanel.setVisible(true);
        updateSeparatorPanelVisibility();
    }

    private void ensurePathIsVisible(TreePath path) {
        browser.ensurePathIsVisible(path);
    }

    @Override
    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }

    @Override
    public void setFileName(String filename) {
        if (fileNameTextField != null && (filename == null || !fileNameTextField.getText().equals(filename))) {
            fileNameTextField.setText(filename);
        }
    }

    private DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }

    //
    // Renderer for DirectoryComboBox
    //
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer {

        final File root = new File("/");
        IndentIcon ii = new IndentIcon();

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {


            // String objects are used to denote delimiters.
            if (value instanceof String) {
                super.getListCellRendererComponent(list, value, index, false, cellHasFocus);
                setText((String) value);
                setPreferredSize(new Dimension(10, 14));
                return this;
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setPreferredSize(null);
            File directory = (File) value;
            /*
            if (directory == null || directory.equals(root)) {
            setText(getFileChooser().getName(root));
            //ii.icon = getFileChooser().getIcon(root);
            ii.icon = UIManager.getIcon("FileView.computerIcon");
            } else {*/
            setText(getFileChooser().getName(directory));
            ii.icon = getFileChooser().getIcon(directory);
            //}
            ii.depth = 0;
            setIcon(ii);
            return this;
        }
    }
    final static int space = 10;

    private static class IndentIcon implements Icon {

        Icon icon = null;
        int depth = 0;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon != null) {
                if (c.getComponentOrientation().isLeftToRight()) {
                    icon.paintIcon(c, g, x + depth * space, y);
                } else {
                    icon.paintIcon(c, g, x, y);
                }
            }
        }

        public int getIconWidth() {
            return (icon == null) ? depth * space : icon.getIconWidth() + depth * space;
        }

        public int getIconHeight() {
            return (icon == null) ? 0 : icon.getIconHeight();
        }
    }

    //
    // DataModel for DirectoryComboxbox
    //
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }

    /**
     * Data model for a directory selection combo-box.
     */
    protected class DirectoryComboBoxModel extends AbstractListModel
            implements ComboBoxModel {

        Object directories[] = new Object[5];
        Object selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();

        public DirectoryComboBoxModel() {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }

            // Hardcode this.
            // The QuaquaJaguarFileChooserUI only works on Mac OS X anyway.
            directories[0] = new File(QuaquaManager.getProperty("user.home"));
            directories[1] = ""; // We use empty String's to denote separators.
            directories[2] = new File(QuaquaManager.getProperty("user.home"), "Desktop");
            directories[3] = new File(QuaquaManager.getProperty("user.home"));
            directories[4] = new File("/");
        }

        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(File directory) {
            isAdjusting++;
            directories[0] = directory;
            selectedDirectory = directory;
            fireContentsChanged(this, -1, -1);
            fireContentsChanged(this, 0, 0);
            isAdjusting--;
        }

        public void setSelectedItem(Object selectedDirectory) {
            if (selectedDirectory instanceof File) {
                this.selectedDirectory = (File) selectedDirectory;
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            return selectedDirectory;
        }

        public int getSize() {
            return directories.length;
        }

        public Object getElementAt(int index) {
            return directories[index];
        }
    }

    //
    // Renderer for Types ComboBox
    //
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }

    /**
     * Render different type sizes and styles.
     */
    public static class FilterComboBoxRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter) value).getDescription());
            }

            return this;
        }
    }

    //
    // DataModel for Types Comboxbox
    //
    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }

    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterComboBoxModel
            extends AbstractListModel
            implements ComboBoxModel, PropertyChangeListener {

        protected FileFilter[] filters;

        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }

        public void setSelectedItem(Object filter) {
            if (filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                // Don't clear the filename field, when the user changes
                // the filename filter.
                // FIXME - Maybe we should disable the save
                // button when the name is not matched by the filter?
                //setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if (currentFilter != null) {
                for (int i = 0; i < filters.length; i++) {
                    if (filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if (found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }

        public int getSize() {
            if (filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }

        public Object getElementAt(int index) {
            if (index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if (filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }

    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    protected class DirectoryComboBoxAction extends AbstractAction {

        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }

        public void actionPerformed(ActionEvent e) {
            if (isAdjusting != 0) {
                return;
            }

            JFileChooser fc = getFileChooser();
            File file = (File) directoryComboBox.getSelectedItem();
            if (file != null) {
                if (fc.isMultiSelectionEnabled()) {
                    fc.setSelectedFiles(new File[]{file});
                } else {
                    fc.setSelectedFile(file);
                }
            }
        }
    }

    @Override
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }

    @Override
    public Action getApproveSelectionAction() {
        return approveSelectionAction;
    }

    protected class DoubleClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            // Note: We must not react on mouse clicks with clickCount=1.
            //       Because this interfers with the mouse handling code in
            //       the JBrowser which does list selection.
            JFileChooser fc = getFileChooser();
            if (SwingUtilities.isLeftMouseButton(e) //
                    && e.getClickCount() == 2 //
                    && fc.getDialogType() != JFileChooser.SAVE_DIALOG) {

                // Only react on double click if all selected files are
                // acceptable
                for (TreePath tp:browser.getSelectionPaths()) {
                    FileSystemTreeModel.Node n =(FileSystemTreeModel.Node)tp.getLastPathComponent();
                    if (! fc.accept(n.getFile())) {
                        return;
                    }
                }

                maybeApproveSelection();
            }
        }
    }

    /**
     * Responds to an Open or Save request
     */
    protected class ApproveSelectionAction extends AbstractAction {

        protected ApproveSelectionAction() {
            super("approveSelection");
        }

        public void actionPerformed(ActionEvent e) {
            maybeApproveSelection();
        }
    }

    /**
     * This method is called, when the user double clicks the JBrowser, or
     * when she clicks at the approve button.
     */
    private void maybeApproveSelection() {
        JFileChooser fc = getFileChooser();
        File selectedFile = null;
        File[] selectedFiles = null;

        String filename = null;
        if (isFileNameFieldVisible()) {
            filename = getFileName();
            if (filename.equals("")) {
                filename = null;
            }
        }

        if (fc.isMultiSelectionEnabled()) {
            TreePath[] selectedPaths = browser.getSelectionPaths();
            if (filename != null) {
                File f = new File(
                        ((FileSystemTreeModel.Node) selectedPaths[0].getLastPathComponent()).getResolvedFile().getParent(),
                        filename);
                    selectedFiles = new File[]{f};
            } else {
                ArrayList<File> a = new ArrayList<File>();
                for (int i = 0; i < selectedPaths.length; i++) {
                    File f = ((FileSystemTreeModel.Node) selectedPaths[i].getLastPathComponent()).getResolvedFile();
                        a.add(f);
                }
                if (a.size() > 0) {
                    selectedFiles = a.toArray(new File[a.size()]);
                }
            }

        } else {
            selectedFile = ((FileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent()).getResolvedFile();
            if (filename != null) {
                selectedFile = new File(selectedFile.isDirectory() && fc.isTraversable(selectedFile) ? selectedFile : fc.getFileSystemView().getParentDirectory(selectedFile), filename);
            }
            if (fc.getFileSelectionMode() == JFileChooser.FILES_ONLY && selectedFile.isDirectory() && fc.isTraversable(selectedFile)) {
                // Abort we cannot approve a directory
                return;
            }
        }

        if (selectedFiles != null || selectedFile != null) {
            if (selectedFiles != null) {
                fc.setSelectedFiles(selectedFiles);
            } else if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles(new File[]{selectedFile});
            } else {
                fc.setSelectedFile(selectedFile);
            }
            fc.approveSelection();
        } else {
            if (fc.isMultiSelectionEnabled()) {
                fc.setSelectedFiles(null);
            } else {
                fc.setSelectedFile(null);
            }
            fc.cancelSelection();
        }

    }

    // *****************************
    // ***** Directory Actions *****
    // *****************************
    @Override
    public Action getNewFolderAction() {
        return newFolderAction;
    }

    /**
     * Creates a new folder.
     */
    protected class NewFolderAction extends AbstractAction {

        protected NewFolderAction() {
            super("New Folder");
        }

        private String showNewFolderDialog() {
            JOptionPane optionPane = new JOptionPane(
                    newFolderDialogPrompt,
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            // Setup Input
            optionPane.setWantsInput(true);
            optionPane.putClientProperty("PrivateQuaqua.OptionPane.InputFieldDocument",
                    new FilenameDocument());
            optionPane.setInitialSelectionValue(newFolderDefaultName);

            // Setup Options
            optionPane.setOptions(new Object[]{
                UIManager.getString("FileChooser.createFolderButtonText"),
                UIManager.getString("FileChooser.cancelButtonText")
            });
            optionPane.setInitialValue(UIManager.getString("FileChooser.createFolderButtonText"));

            // Show the dialog
            JDialog dialog = optionPane.createDialog(getFileChooser(), newFolderTitleText);
            dialog.setVisible(true);
            dialog.dispose();

            return (optionPane.getValue() == UIManager.getString("FileChooser.createFolderButtonText"))
                    ? (String) optionPane.getInputValue() : null;
        }

        public void actionPerformed(ActionEvent actionevent) {
            JFileChooser fc = getFileChooser();
            String newFolderName = showNewFolderDialog();

            if (newFolderName != null) {

                File newFolder;
                File currentFile = ((FileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent()).getResolvedFile();
                if (!currentFile.isDirectory() || !fc.isTraversable(currentFile)) {
                    currentFile = fc.getFileSystemView().getParentDirectory(currentFile);
                }
                newFolder = new File(currentFile, newFolderName);
                if (newFolder.exists()) {
                    JOptionPane.showMessageDialog(
                            fc,
                            newFolderExistsErrorText,
                            newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    if (! newFolder.mkdir()) {
                        if (! newFolder.isDirectory()) {
                            throw new IOException("Couldn't create folder \""+newFolder.getName()+"\".");
                        }
                    }
                    fc.rescanCurrentDirectory();
                    if (fc.isMultiSelectionEnabled()) {
                        fc.setSelectedFiles(new File[]{newFolder});
                    } else {
                        fc.setSelectedFile(newFolder);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            fc,
                            newFolderErrorText,
                            newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    protected class SaveTextFocusListener implements FocusListener {

        public void focusGained(FocusEvent focusevent) {
            updateApproveButtonState();
        }

        public void focusLost(FocusEvent focusevent) {
            /* empty */
        }
    }

    protected class SaveTextDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent documentevent) {
            textChanged();
        }

        public void removeUpdate(DocumentEvent documentevent) {
            textChanged();
        }

        public void changedUpdate(DocumentEvent documentevent) {
            /* empty */
        }

        private void textChanged() {
            if (isAdjusting != 0) {
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JFileChooser fc = getFileChooser();
                    File file = ((FileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent()).getResolvedFile();
                    if (fileNameTextField.getText().length() != 0) {
                        if (file.isDirectory() && fc.isTraversable(file)) {
                            file = new File(file, fileNameTextField.getText());
                        } else {
                            file = new File(fc.getFileSystemView().getParentDirectory(file), fileNameTextField.getText());
                        }
                    }
                    if (fc.isMultiSelectionEnabled()) {
                        fc.setSelectedFiles(new File[]{file});
                    } else {
                        fc.setSelectedFile(file);
                    }
                }
            });
        }
    }

    /**
     * The FileChooserAncestorListener listens for visibility changes of
     * the JFileChooser.
     * This is used to do validations (refreshes) of the tree model only,
     * when the JFileChooser is showing.
     */
    private class FileChooserAncestorListener implements AncestorListener {

        public void ancestorAdded(AncestorEvent event) {
            if (model != null) {
                model.setAutoValidate(UIManager.getBoolean("FileChooser.autovalidate"));
                model.validatePath(browser.getSelectionPath());
            }
            // We update the approve button state here, because the approve
            // button can only be made the default button, if it has a root pane
            // ancestor.
            updateApproveButtonState();
        }

        public void ancestorRemoved(AncestorEvent event) {
            if (model != null) {
                model.setAutoValidate(false);
                model.stopValidation();
                model.invalidatePath(browser.getSelectionPath());
                clearIconCache();
            }
        }

        public void ancestorMoved(AncestorEvent event) {
        }
    }
    // *******************************************************
    // ************* FileChooserUI PLAF methods **************
    // *******************************************************

    @Override
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        if (f != null) {
            if (!f.isAbsolute()) {
                f = new File(fc.getCurrentDirectory(), f.getName());
            }
            ensurePathIsVisible(getTreeModel().toPath(f, browser.getSelectionPath()));
        }
    }

    @Override
    public String getApproveButtonText(JFileChooser fc) {
        String buttonText = fc.getApproveButtonText();
        if (buttonText != null) {
            return buttonText;
        } else if (fc.isDirectorySelectionEnabled() && chooseButtonText != null) {
            return chooseButtonText;
        } else if (fc.getDialogType() == JFileChooser.OPEN_DIALOG) {
            return openButtonText;
        } else if (fc.getDialogType() == JFileChooser.SAVE_DIALOG) {
            return saveButtonText;
        } else {
            return null;
        }
    }

    @Override
    public FileView getFileView(JFileChooser fc) {
        return fileView;
    }

    @Override
    public void rescanCurrentDirectory(JFileChooser fc) {
        // Validation is only necessary, when the JFileChooser is showing.
        if (fc.isShowing()) {
            //clearIconCache();
            model.lazyInvalidatePath(browser.getSelectionPath());
            model.validatePath(browser.getSelectionPath());
        }
    }
    // *******************************************************
    // ******** End of FileChooserUI PLAF methods ************
    // *******************************************************

    // *******************************************************
    // ********** BasicFileChooserUI PLAF methods ************
    // *******************************************************
    @Override
    public void clearIconCache() {
        try {
            fileView.getClass().getMethod("clearIconCache", new Class[0]).invoke(fileView, new Object[0]);
        } catch (Exception e) {
            // empty
        }
    }

    protected MouseListener createDoubleClickListener(JFileChooser fc) {
        return new DoubleClickListener();
    }
    // *******************************************************
    // ******* End of BasicFileChooserUI PLAF methods ********
    // *******************************************************
}