/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import java.io.File;

/**
 *
 * @author  boris
 */
public class NicheEditDialog extends javax.swing.JDialog {
    private boolean succeeded;
    
    public NicheEditDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    public void setSucceeded(boolean succeeded)
    {
    	this.succeeded = succeeded;
    }
    
    public boolean getSucceeded()
    {
        return succeeded;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        txtNicheName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        fileChooser = new javax.swing.JFileChooser();
        jLabel2 = new javax.swing.JLabel();
        lCurrentLocation = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Niche Properties");
        setModal(true);
        setResizable(false);
        txtNicheName.setFont(new java.awt.Font("Tahoma", 0, 16));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16));
        jLabel1.setText("Niche Name:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtNicheName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 498, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtNicheName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Location Of Niche", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 16)));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setControlButtonsAreShown(false);
        fileChooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.getComponentCount() > 3)
           fileChooser.getComponents()[3].setVisible(false);
        fileChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                directorySelected(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 16));
        jLabel2.setText("Current:");

        lCurrentLocation.setFont(new java.awt.Font("Tahoma", 0, 14));
        lCurrentLocation.setText("[select location below]");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 566, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lCurrentLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 519, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(40, 40, 40))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(lCurrentLocation))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 296, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDone(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancel(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20)
                        .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 144, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 619, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton2)
                    .add(jButton3))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pack();
        txtNicheName.requestFocusInWindow();
    }// </editor-fold>//GEN-END:initComponents

    private void directorySelected(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_directorySelected
        File selection = fileChooser.getSelectedFile();
        if (selection == null)
            selection = fileChooser.getCurrentDirectory();
        if (selection != null)
            lCurrentLocation.setText(selection.getAbsolutePath());
    }//GEN-LAST:event_directorySelected

    private void cmdCancel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancel
        succeeded = false;
        setVisible(false);
    }//GEN-LAST:event_cmdCancel

    private void cmdDone(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDone
        succeeded = true;
        setVisible(false);
    }//GEN-LAST:event_cmdDone
    
    public void setDefaultDirectory(String path)
    {
        fileChooser.setCurrentDirectory(new File(path));
    }
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lCurrentLocation;
    private javax.swing.JTextField txtNicheName;
    // End of variables declaration//GEN-END:variables

    /**
     * Getter for property nicheName.
     * @return Value of property nicheName.
     */
    public String getNicheName() {
        return this.txtNicheName.getText();
    }

    /**
     * Setter for property nicheName.
     * @param nicheName New value of property nicheName.
     */
    public void setNicheName(String nicheName) {
        this.txtNicheName.setText(nicheName);
    }

    /**
     * Getter for property nicheLocation.
     * @return Value of property nicheLocation.
     */
    public java.io.File getNicheLocation() {
        return new File(lCurrentLocation.getText());
    }

    /**
     * Setter for property nicheLocation.
     * @param nicheLocation New value of property nicheLocation.
     */
    public void setNicheLocation(java.io.File nicheLocation) {
        this.fileChooser.setSelectedFile(nicheLocation);
    }

	public javax.swing.JFileChooser getFileChooser()
	{
		return fileChooser;
	}

	public void setFileChooser(javax.swing.JFileChooser fileChooser)
	{
		this.fileChooser = fileChooser;
	}
    
    
}
