/*
 * @(#)FilePreview.java  
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.panther.filechooser;

import ch.randelshofer.quaqua.osx.OSXFile;
import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.filechooser.*;
import ch.randelshofer.quaqua.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

/**
 * The FilePreview is used to render the preview column in the JBrowser in
 * Quaqua's FileChooserUI's.
 *
 * @author  Werner Randelshofer
 * @version $Id: FilePreview.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class FilePreview extends JPanel implements BrowserPreviewRenderer {

    private JFileChooser fileChooser;
    private static Icon placeholderIcon = new Icon() {

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 128;
        }

        public int getIconHeight() {
            return 128;
        }
    };
    private boolean isFileIconAvailable = true;
    private JPanel emptyPreview;
    private FileInfo info;

    /** Creates new form. */
    public FilePreview(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        initComponents();

        Color bg = UIManager.getColor("List.background");
        Color fgl = UIManager.getColor("FileChooser.previewLabelForeground");
        Color fgv = UIManager.getColor("FileChooser.previewValueForeground");
        Font fl = UIManager.getFont("FileChooser.previewLabelFont");
        Font fv = UIManager.getFont("FileChooser.previewValueFont");

        emptyPreview = new JPanel();
        emptyPreview.setBackground(bg);
        emptyPreview.setOpaque(true);

        Insets labelInsets = UIManager.getInsets("FileChooser.previewLabelInsets");
        GridBagLayout layout = (GridBagLayout) northPanel.getLayout();

        String delimiter = UIManager.getString("FileChooser.previewLabelDelimiter");
        if (delimiter == null) {
            delimiter = "";
        }

        for (int i = 0, n = northPanel.getComponentCount(); i < n; i++) {
            JComponent c = (JComponent) northPanel.getComponent(i);
            if (c != previewLabel) {
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    c.setFont(fl);
                    c.setForeground(fgl);
                    if (labelInsets != null) {
                        GridBagConstraints gbc = layout.getConstraints(c);
                        gbc.insets = labelInsets;
                        layout.setConstraints(c, gbc);
                    }
                    label.setText(label.getText() + delimiter);
                } else {
                    c.setFont(fv);
                    c.setForeground(fgv);
                }
            }
            c.setBackground(bg);
        }

        // We do not show the location of the file, because this information
        // is already provided by the file chooser.
        whereLabel.setVisible(false);
        whereText.setVisible(false);

        // Remove border Margin
        Insets borderMargin = new Insets(0, 0, 0, 0);
        kindLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);
        modifiedLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);
        nameLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);
        originalLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);
        sizeLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);
        whereLabel.putClientProperty("Quaqua.Component.visualMargin", borderMargin);

        // Disable focus traversability
        kindText.setFocusable(false);
        modifiedText.setFocusable(false);
        nameText.setFocusable(false);
        originalText.setFocusable(false);
        sizeText.setFocusable(false);
        whereText.setFocusable(false);

        setBackground(bg);
        northPanel.setBackground(bg);
        setOpaque(true);

        MouseListener mouseHandler = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    FilePreview.this.fileChooser.approveSelection();
                }
            }
        };
        addMouseListener(mouseHandler);
        Component[] c = getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].addMouseListener(mouseHandler);
        }

        if (OSXFile.canWorkWithAliases()) {
            try {
                Preferences prefs = Preferences.userNodeForPackage(QuaquaLookAndFeel.class);
                previewLabel.setVisible(prefs.getBoolean("FileChooser.isPreviewExpanded", false));
                previewCheckBox.setSelected(prefs.getBoolean("FileChooser.isPreviewExpanded", false));
            } catch (UnsatisfiedLinkError err) {
                // Work around for bug in preferences in OS X OpenJDK 1.7.0-ea-b211
                previewLabel.setVisible(false);
                previewCheckBox.setSelected(false);
            }
            previewCheckBox.setIcon(UIManager.getIcon("FileChooser.disclosureButtonIcon"));
            previewCheckBox.setIcon(UIManager.getIcon("FileChooser.disclosureButtonIcon"));
        } else {
            previewCheckBox.setVisible(false);
            previewLabel.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        northPanel = new javax.swing.JPanel();
        previewCheckBox = new javax.swing.JCheckBox();
        previewLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextArea();
        kindLabel = new javax.swing.JLabel();
        kindText = new javax.swing.JTextArea();
        sizeLabel = new javax.swing.JLabel();
        sizeText = new javax.swing.JTextArea();
        modifiedLabel = new javax.swing.JLabel();
        modifiedText = new javax.swing.JTextArea();
        whereLabel = new javax.swing.JLabel();
        whereText = new javax.swing.JTextArea();
        originalLabel = new javax.swing.JLabel();
        originalText = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 4, 4, 4));
        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.GridBagLayout());

        previewCheckBox.setText(UIManager.getString("Filechooser.previewButton.text")); // NOI18N
        previewCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        northPanel.add(previewCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        northPanel.add(previewLabel, gridBagConstraints);

        nameLabel.setText(UIManager.getString("FileChooser.name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(nameLabel, gridBagConstraints);

        nameText.setEditable(false);
        nameText.setLineWrap(true);
        nameText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(nameText, gridBagConstraints);

        kindLabel.setText(UIManager.getString("FileChooser.kind")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(kindLabel, gridBagConstraints);

        kindText.setEditable(false);
        kindText.setLineWrap(true);
        kindText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(kindText, gridBagConstraints);

        sizeLabel.setText(UIManager.getString("FileChooser.size")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(sizeLabel, gridBagConstraints);

        sizeText.setEditable(false);
        sizeText.setLineWrap(true);
        sizeText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(sizeText, gridBagConstraints);

        modifiedLabel.setText(UIManager.getString("FileChooser.modified")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(modifiedLabel, gridBagConstraints);

        modifiedText.setEditable(false);
        modifiedText.setLineWrap(true);
        modifiedText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(modifiedText, gridBagConstraints);

        whereLabel.setText(UIManager.getString("FileChooser.whereLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(whereLabel, gridBagConstraints);

        whereText.setEditable(false);
        whereText.setLineWrap(true);
        whereText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(whereText, gridBagConstraints);

        originalLabel.setText(UIManager.getString("FileChooser.original")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        northPanel.add(originalLabel, gridBagConstraints);

        originalText.setEditable(false);
        originalText.setLineWrap(true);
        originalText.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        northPanel.add(originalText, gridBagConstraints);

        add(northPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void previewButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonPerformed
        boolean b = previewCheckBox.isSelected();
        try {
            Preferences prefs = Preferences.userNodeForPackage(QuaquaLookAndFeel.class);
            prefs.putBoolean("FileChooser.isPreviewExpanded", b);
        } catch (UnsatisfiedLinkError err) {
                // Work around for bug in preferences in OS X OpenJDK 1.7.0-ea-b211
        } catch (NoClassDefFoundError err) {
                // Work around for bug in preferences in OS X OpenJDK 1.7.0-ea-b211
        }
        updatePreviewIcon();
    }//GEN-LAST:event_previewButtonPerformed

    private String toOSXPath(File file) {
        StringBuffer buf = new StringBuffer();
        FileSystemView fsv = QuaquaFileSystemView.getQuaquaFileSystemView();
        if (file != null && file.isDirectory()) {
            buf.append(':');
        }
        while (file != null) {
            buf.insert(0, fileChooser.getName(file));
            file = fsv.getParentDirectory(file);
            if (file != null) {
                buf.insert(0, ':');
            }
        }
        return buf.toString();
    }

    public Component getPreviewRendererComponent(JBrowser browser, TreePath[] paths) {
        Locale locale = Locale.getDefault();
        NumberFormat nf = NumberFormat.getInstance(locale);
        nf.setMaximumFractionDigits(1);
        info = (FileInfo) paths[0].getLastPathComponent();

        if (!info.isAcceptable()) {
            return emptyPreview;
        }

        // We do not display the location of the file, because this is already
        // provided by the file chooser.
        //whereText.setText(toOSXPath(QuaquaManager.getFileSystemView().getParentDirectory(info.getUnresolvedFile())));

        long fileLength = 0;
        if (paths.length == 1) {
            nameLabel.setVisible(true);
            modifiedLabel.setVisible(true);
            modifiedText.setVisible(true);
            nameText.setText(info.getUserName());
            File file = info.getFile();
            fileLength = info.getFileLength();
            if (file != null) {
                modifiedText.setText(DateFormat.getInstance().format(new Date(file.lastModified())));
            } else {
                modifiedText.setText(UIManager.getString("FileChooser.modifiedUnknown"));
            }
            String kind = info.getFileKind();
            kindText.setText(OSXFile.getKindString(file));
            if (kind == "alias") {
                File resolvedFile = info.lazyGetResolvedFile();
                if (resolvedFile != null) {
                    originalText.setText(toOSXPath(resolvedFile));
                } else {
                    originalText.setText("");
                }
                originalLabel.setVisible(true);
                originalText.setVisible(true);
            } else {
                originalLabel.setVisible(false);
                originalText.setVisible(false);
            }
        } else {
            nameLabel.setVisible(false);
            modifiedLabel.setVisible(false);
            modifiedText.setVisible(false);
            nameText.setText(
                    MessageFormat.format(
                    UIManager.getString("FileChooser.items"),
                    new Object[]{nf.format(paths.length)}));
            TreeMap kinds = new TreeMap();
            for (int i = 0; i < paths.length; i++) {
                info = (FileSystemTreeModel.Node) paths[i].getLastPathComponent();
                if (fileLength != -1) {
                    if (info.getFileLength() == -1) {
                        fileLength = -1;
                    } else {
                        fileLength += info.getFileLength();
                    }
                }
                String kind = info.getFileKind();
                Integer kindCount = (Integer) kinds.get(kind);
                kinds.put(kind, (kindCount == null) ? 1 : kindCount + 1);
            }
            StringBuffer buf = new StringBuffer();
            for (Iterator i = kinds.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                buf.append(MessageFormat.format(
                        UIManager.getString("FileChooser." + entry.getKey() + "Count"),
                        new Object[]{entry.getValue()}));
                if (i.hasNext()) {
                    buf.append(", ");
                }
            }
            kindText.setText(buf.toString());
            originalLabel.setVisible(false);
            originalText.setVisible(false);
        }

        String label;
        float scaledLength;
        if (fileLength == -1) {
            label = "FileChooser.sizeUnknown";
            scaledLength = 0f;
        } else {
            if (fileLength >= 1024 * 1024 * 1024) {
                label = "FileChooser.sizeGBytes";
                scaledLength = fileLength / (1024f * 1024f * 1024f);
            } else if (fileLength >= 1024 * 1024) {
                label = "FileChooser.sizeMBytes";
                scaledLength = fileLength / (1024f * 1024f);
            } else if (fileLength >= 1024) {
                label = "FileChooser.sizeKBytes";
                scaledLength = fileLength / (1024f);
            } else {
                label = "FileChooser.sizeBytes";
                scaledLength = (float) fileLength;
            }
        }

        sizeText.setText(
                MessageFormat.format(UIManager.getString(label), new Object[]{
                    scaledLength,
                    fileLength,
                    paths.length
                }));
        updatePreviewIcon();
        return this;
    }

    private void updatePreviewIcon() {
        previewLabel.setVisible(isFileIconAvailable && previewCheckBox.isSelected());
        previewLabel.setIcon(placeholderIcon);

        if (info != null && previewCheckBox.isSelected()) {
            // Retrieving the file icon requires some potentially lengthy I/O
            // operations. Therefore we do this in a worker thread.
            final File file = info.lazyGetResolvedFile();
            if (file != null) {
                new Worker<Image>() {

                    public Image construct() {
                        Image o = null;
                        if (UIManager.getBoolean("FileChooser.quickLookEnabled")
                                && System.getProperty("os.version").compareTo("10.6") >= 0) {
                            o = OSXFile.getQuickLookThumbnailImage(file, 128);
                        }
                        if (o == null) {
                            return OSXFile.getIconImage(file, 128);
                        } else {
                            return o;
                        }
                    }

                    @Override
                    public void done(Image value) {
                        Image fileIconImage = value;
                        isFileIconAvailable = fileIconImage != null;
                        if (isFileIconAvailable) {
                            previewLabel.setIcon(new ImageIcon(fileIconImage));
                        } else {
                            previewLabel.setVisible(false);
                        }
                        previewLabel.getParent().validate();
                    }
                }.start();
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel kindLabel;
    private javax.swing.JTextArea kindText;
    private javax.swing.JLabel modifiedLabel;
    private javax.swing.JTextArea modifiedText;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextArea nameText;
    private javax.swing.JPanel northPanel;
    private javax.swing.JLabel originalLabel;
    private javax.swing.JTextArea originalText;
    private javax.swing.JCheckBox previewCheckBox;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JTextArea sizeText;
    private javax.swing.JLabel whereLabel;
    private javax.swing.JTextArea whereText;
    // End of variables declaration//GEN-END:variables
}
