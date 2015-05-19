/*
 * @(#)TableTest.java  1.0  13 February 2005
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.util.Methods;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * TableTest.
 *
 * @author  Werner Randelshofer
 * @version 1.0  13 February 2005  Created.
 */
public class TableHighlightCellTest extends javax.swing.JPanel {
    /** A table cell renderer */
    private static class MyTableCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(isSelected ? Color.RED.darker() : Color.RED);
            return c;
        }
        
    }

    /**
     * Some bogus data to populate the table.
     */
    private static class MyTableModel extends AbstractTableModel {

        private Object[][] data;
        private Class[] columnClasses;
        private String[] columnNames;

        public MyTableModel() {
            columnClasses = new Class[]{
                        Boolean.class,
                        String.class, String.class, String.class, String.class,
                        Integer.class
                    };
            columnNames = new String[]{"", "Name", "Time", "Artist", "Genre", "Year"};
            data = new Object[6][columnClasses.length];
            for (int i = 0; i < data.length; i++) {
                data[i][0] = Boolean.TRUE;
                data[i][1] = (i % 2 == 0) ? "Fooing In The Wind" : "Baring The Sea";
                data[i][2] = (i % 2 == 0) ? "3:51" : "3:42";
                data[i][3] = (i % 2 == 0) ? "Foo Guy" : "Bar Girl";
                data[i][4] = (i % 2 == 0) ? "Pop" : "Rock";
                data[i][5] = (i % 2 == 0) ? new Integer(2007) : new Integer(2008);
            }
        }

        public int getRowCount() {
            return data.length;
        }

        public int getColumnCount() {
            return data[0].length;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Object getValueAt(int row, int column) {
            return data[row][column];
        }

        public void setValueAt(Object value, int row, int column) {
            data[row][column] = value;
        }

        public boolean isCellEditable(int row, int column) {
            return column != 2;
        }

        public Class getColumnClass(int column) {

            return columnClasses[column];
        }
    }

    /** Creates new form. */
    public TableHighlightCellTest() {
        initComponents();

        plainTable.setDefaultRenderer(Integer.class, new MyTableCellRenderer());
        stripedTable.setDefaultRenderer(Integer.class, new MyTableCellRenderer());

        plainTable.setModel(new MyTableModel());

        DefaultComboBoxModel rendererComboModel, editorComboModel;
        JComboBox comboBox;

        rendererComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        editorComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        TableColumnModel cm = plainTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(30);
        cm.getColumn(1).setPreferredWidth(120);
        cm.getColumn(2).setPreferredWidth(40);
        cm.getColumn(3).setPreferredWidth(60);
        cm.getColumn(4).setPreferredWidth(50);
        cm.getColumn(4).setCellRenderer(new DefaultCellRenderer(comboBox = new JComboBox(rendererComboModel)));
        cm.getColumn(4).setCellEditor(new DefaultCellEditor2(comboBox = new JComboBox(editorComboModel)));
        plainTable.putClientProperty("Quaqua.Table.style", "plain");
        plainTable.getColumnModel().setColumnSelectionAllowed(true);
        plainTable.setRowSelectionAllowed(true);

        stripedTable.setModel(new MyTableModel());
        rendererComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        editorComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        cm = stripedTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(30);
        cm.getColumn(1).setPreferredWidth(120);
        cm.getColumn(2).setPreferredWidth(40);
        cm.getColumn(3).setPreferredWidth(60);
        cm.getColumn(4).setPreferredWidth(50);
        cm.getColumn(4).setCellRenderer(new DefaultCellRenderer(comboBox = new JComboBox(rendererComboModel)));
        comboBox.setEditable(true);
        comboBox = new JComboBox(editorComboModel);
        comboBox.setEditable(true);
        cm.getColumn(4).setCellEditor(new DefaultCellEditor2(comboBox));
        stripedTable.putClientProperty("Quaqua.Table.style", "striped");
        stripedTable.setShowHorizontalLines(false);
        stripedTable.setShowVerticalLines(true);

        // J2SE6 only
        Methods.invokeIfExists(plainTable, "setAutoCreateRowSorter", true);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame f = new JFrame("Quaqua Table Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new TableHighlightCellTest());
        ((JComponent) f.getContentPane()).setBorder(new EmptyBorder(9, 17, 17, 17));
        f.pack();
        f.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        plainTableScrollPane = new javax.swing.JScrollPane();
        plainTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jSeparator11 = new javax.swing.JSeparator();
        stripedTableScrollPane = new javax.swing.JScrollPane();
        stripedTable = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        plainTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        plainTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        plainTableScrollPane.setViewportView(plainTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(plainTableScrollPane, gridBagConstraints);

        jLabel12.setText("Plain Style");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jLabel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jSeparator11, gridBagConstraints);

        stripedTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        stripedTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        stripedTable.setIntercellSpacing(new java.awt.Dimension(4, 1));
        stripedTableScrollPane.setViewportView(stripedTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(stripedTableScrollPane, gridBagConstraints);

        jLabel15.setText("Striped Style");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(jLabel15, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JTable plainTable;
    private javax.swing.JScrollPane plainTableScrollPane;
    private javax.swing.JTable stripedTable;
    private javax.swing.JScrollPane stripedTableScrollPane;
    // End of variables declaration//GEN-END:variables
}
