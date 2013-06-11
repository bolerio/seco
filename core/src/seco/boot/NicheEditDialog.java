/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import java.awt.Dialog;
import java.io.File;

import javax.swing.JFileChooser;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import  javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

/**
 *
 * @author  boris
 */
public class NicheEditDialog extends JDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2168996875672753957L;
	private boolean succeeded;
    
    public NicheEditDialog(Dialog parent, boolean modal) {
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
        jPanel1 = new  JPanel();
        txtNicheName = new  JTextField();
        jLabel1 = new  JLabel();
        jPanel2 = new  JPanel();
        fileChooser = new  JFileChooser();
        jLabel2 = new  JLabel();
        lCurrentLocation = new  JLabel();
        jButton2 = new  JButton();
        jButton3 = new  JButton();

        setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Niche Properties");
        setModal(true);
        setResizable(false);
        txtNicheName.setFont(new java.awt.Font("Tahoma", 0, 16));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16));
        jLabel1.setText("Niche Name:");

        GroupLayout jPanel1Layout = new  GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNicheName, GroupLayout.PREFERRED_SIZE, 498,  GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNicheName,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                .addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Location Of Niche",  TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 16)));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setControlButtonsAreShown(false);
        fileChooser.setDialogType( JFileChooser.CUSTOM_DIALOG);
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
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

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup( GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(fileChooser, GroupLayout.PREFERRED_SIZE, 566,  GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lCurrentLocation,  GroupLayout.PREFERRED_SIZE, 519,  GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lCurrentLocation))
                .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileChooser,  GroupLayout.PREFERRED_SIZE, 296,  GroupLayout.PREFERRED_SIZE)
                .addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2,  GroupLayout.PREFERRED_SIZE, 136,  GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jButton3,  GroupLayout.PREFERRED_SIZE, 144,  GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2,  GroupLayout.PREFERRED_SIZE, 619,  GroupLayout.PREFERRED_SIZE))
                .addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup( GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jPanel2,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
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
    private  JFileChooser fileChooser;
    private  JButton jButton2;
    private  JButton jButton3;
    private  JLabel jLabel1;
    private  JLabel jLabel2;
    private  JPanel jPanel1;
    private  JPanel jPanel2;
    private  JLabel lCurrentLocation;
    private  JTextField txtNicheName;
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

	public  JFileChooser getFileChooser()
	{
		return fileChooser;
	}

	public void setFileChooser( JFileChooser fileChooser)
	{
		this.fileChooser = fileChooser;
	}
    
    
}
