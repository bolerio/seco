/*
 * @(#)TableTest.java 
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.util.Methods;
import java.awt.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * TableTest.
 *
 * @author  Werner Randelshofer
 * @version $Id: TableTest.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class TableTest extends javax.swing.JPanel {

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

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Object getValueAt(int row, int column) {
            return data[row][column];
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            data[row][column] = value;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 2;
        }

        @Override
        public Class getColumnClass(int column) {

            return columnClasses[column];
        }
    }

    /** Creates new form. */
    public TableTest() {
        initComponents();
        /*
        plainTable = new JTable() {
        public void repaint(long tm, int x, int y, int w, int h) {
        super.repaint(tm, x, y, w, h);
        
        System.out.println("JTable.repaint("+tm+","+x+","+y+" "+w+" "+h);
        if (w == 192) {
        new Throwable().printStackTrace();
        }
        }
        };
        plainTableScrollPane.setViewportView(plainTable);
         */
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

        bigFontTable.setModel(new MyTableModel());
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);
        cb.putClientProperty("JComponent.sizeVariant", "small");
        bigFontTable.setDefaultEditor(Boolean.class, new DefaultCellEditor2(cb));
        rendererComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        editorComboModel = new DefaultComboBoxModel(new Object[]{"Pop", "Rock", "R&B"});
        cm = bigFontTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(30);
        cm.getColumn(1).setPreferredWidth(160);
        cm.getColumn(2).setPreferredWidth(40);
        cm.getColumn(3).setPreferredWidth(70);
        cm.getColumn(4).setPreferredWidth(50);
        cm.getColumn(4).setCellRenderer(new DefaultCellRenderer(comboBox = new JComboBox(rendererComboModel)));
        comboBox.setEditable(true);
        comboBox = new JComboBox(editorComboModel);
        comboBox.setEditable(true);
        cm.getColumn(4).setCellEditor(new DefaultCellEditor2(comboBox));
        bigFontTable.setRowHeight(bigFontTable.getRowHeight() + 7);
        //largeFontTable.setEnabled(false);

        showHorizontalLinesCheckBox.setSelected(plainTable.getShowHorizontalLines());
        showVerticalLinesCheckBox.setSelected(plainTable.getShowVerticalLines());

        installDefaultEditors(stripedTable);
        installDefaultEditors(plainTable);
        installDefaultEditors(bigFontTable);

        if (QuaquaManager.getProperty("java.version").startsWith("1.5")) {
            enableSortingBox.setVisible(false);
        }

    }

    private void installDefaultEditors(JTable t) {
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);
        cb.putClientProperty("JComponent.sizeVariant", "small");
        t.setDefaultEditor(Boolean.class, new DefaultCellEditor2(cb) {

            @Override
            public boolean shouldSelectCell(EventObject evt) {
                return shouldSelectCellCheckBox.isSelected();
            }
        });
    }

    public static void main(String args[]) {
        try {
            System.setProperty("Quaqua.Table.useJ2SE5MouseHandler", "true");
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame f = new JFrame("Quaqua Table Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new TableTest());
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
        jSeparator12 = new javax.swing.JSeparator();
        bigFontTableScrollPane = new javax.swing.JScrollPane();
        bigFontTable = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        showHorizontalLinesCheckBox = new javax.swing.JCheckBox();
        showVerticalLinesCheckBox = new javax.swing.JCheckBox();
        allowRowSelectionCheckBox = new javax.swing.JCheckBox();
        allowColumnSelectionCheckBox = new javax.swing.JCheckBox();
        shouldSelectCellCheckBox = new javax.swing.JCheckBox();
        enableSortingBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 17, 17, 17));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        plainTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        plainTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        plainTableScrollPane.setViewportView(plainTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jSeparator12, gridBagConstraints);

        bigFontTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        bigFontTableScrollPane.setEnabled(false);

        bigFontTable.setFont(new java.awt.Font("Lucida Grande", 0, 16));
        bigFontTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        bigFontTableScrollPane.setViewportView(bigFontTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(bigFontTableScrollPane, gridBagConstraints);

        jLabel16.setText("Big Font");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(jLabel16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        showHorizontalLinesCheckBox.setText("Show horizontal lines");
        showHorizontalLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateShowHorizontalLines(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(showHorizontalLinesCheckBox, gridBagConstraints);

        showVerticalLinesCheckBox.setText("Show vertical lines");
        showVerticalLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateShowVerticalLines(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(showVerticalLinesCheckBox, gridBagConstraints);

        allowRowSelectionCheckBox.setSelected(true);
        allowRowSelectionCheckBox.setText("Allow row selection");
        allowRowSelectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateRowSelection(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(allowRowSelectionCheckBox, gridBagConstraints);

        allowColumnSelectionCheckBox.setText("Allow column selection");
        allowColumnSelectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateColumnSelection(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(allowColumnSelectionCheckBox, gridBagConstraints);

        shouldSelectCellCheckBox.setSelected(true);
        shouldSelectCellCheckBox.setText("Checkbox selects cell");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(shouldSelectCellCheckBox, gridBagConstraints);

        enableSortingBox.setText("Enable row sorting");
        enableSortingBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSortingPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(enableSortingBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void updateShowVerticalLines(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateShowVerticalLines
        for (int i = 0, n = getComponentCount(); i < n; i++) {
            Component c = getComponent(i);
            if (c instanceof JScrollPane) {
                c = ((JScrollPane) c).getViewport().getView();
            }
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setShowVerticalLines(showVerticalLinesCheckBox.isSelected());
            }
        }
}//GEN-LAST:event_updateShowVerticalLines

    private void updateShowHorizontalLines(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateShowHorizontalLines
        for (int i = 0, n = getComponentCount(); i < n; i++) {
            Component c = getComponent(i);
            if (c instanceof JScrollPane) {
                c = ((JScrollPane) c).getViewport().getView();
            }
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setShowHorizontalLines(showHorizontalLinesCheckBox.isSelected());
            }
        }
    }//GEN-LAST:event_updateShowHorizontalLines

    private void updateRowSelection(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateRowSelection
        for (int i = 0, n = getComponentCount(); i < n; i++) {
            Component c = getComponent(i);
            if (c instanceof JScrollPane) {
                c = ((JScrollPane) c).getViewport().getView();
            }
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setRowSelectionAllowed(allowRowSelectionCheckBox.isSelected());
            }
        }
    }//GEN-LAST:event_updateRowSelection

    private void updateColumnSelection(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateColumnSelection
        for (int i = 0, n = getComponentCount(); i < n; i++) {
            Component c = getComponent(i);
            if (c instanceof JScrollPane) {
                c = ((JScrollPane) c).getViewport().getView();
            }
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.getColumnModel().setColumnSelectionAllowed(allowColumnSelectionCheckBox.isSelected());
            }
        }
    }//GEN-LAST:event_updateColumnSelection

    private void enableSortingPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableSortingPerformed
        boolean b = enableSortingBox.isSelected();

        for (int i = 0, n = getComponentCount(); i < n; i++) {
            Component c = getComponent(i);
            if (c instanceof JScrollPane) {
                c = ((JScrollPane) c).getViewport().getView();
            }
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                // J2SE6 only
                try {
                    Methods.invokeIfExists(table, "setAutoCreateRowSorter", b);
                        Class rsclazz = Class.forName("javax.swing.RowSorter");
                    if (b) {
                        for (int j = 0, m = table.getColumnCount(); j < m; j++) {
                            Class clazz = Class.forName("javax.swing.table.TableRowSorter");
                            Methods.invokeIfExists(table, "setRowSorter", rsclazz,
                                    Methods.newInstance(clazz, new Class[]{TableModel.class}, new Object[]{table.getModel()}));
                        }
                    } else {
                        Methods.invokeIfExists(table, "setRowSorter", rsclazz, null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_enableSortingPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allowColumnSelectionCheckBox;
    private javax.swing.JCheckBox allowRowSelectionCheckBox;
    private javax.swing.JTable bigFontTable;
    private javax.swing.JScrollPane bigFontTableScrollPane;
    private javax.swing.JCheckBox enableSortingBox;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JTable plainTable;
    private javax.swing.JScrollPane plainTableScrollPane;
    private javax.swing.JCheckBox shouldSelectCellCheckBox;
    private javax.swing.JCheckBox showHorizontalLinesCheckBox;
    private javax.swing.JCheckBox showVerticalLinesCheckBox;
    private javax.swing.JTable stripedTable;
    private javax.swing.JScrollPane stripedTableScrollPane;
    // End of variables declaration//GEN-END:variables
}
