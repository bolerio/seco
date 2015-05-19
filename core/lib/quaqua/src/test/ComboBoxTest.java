/*
 * @(#)ComboBoxTest.java  
 *
 * Copyright (c) 2004 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * ComboBoxTest.
 *
 * @author  Werner Randelshofer
 * @version $Id: ComboBoxTest.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class ComboBoxTest extends javax.swing.JPanel {

    private static class ColorIcon implements Icon {

        private Color color = Color.white;

        public void setColor(Color newValue) {
            color = newValue;
        }

        public Color getColor() {
            return color;
        }

        public int getIconHeight() {
            return 12;
        }

        public int getIconWidth() {
            return 24;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (color != null) {
                g.setColor(color);
                g.fillRect(x, y, getIconWidth(), getIconHeight());
                g.setColor(color.darker());
                g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
            }
        }
    }

    private static class ColorComboCellRenderer extends DefaultListCellRenderer {

        private ColorIcon colorIcon = new ColorIcon();

        public ColorComboCellRenderer() {
            setIconTextGap(6);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Color) {
                Color color = (Color) value;
                colorIcon.setColor(color);
                l.setIcon(colorIcon);
                l.setText(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
            } else {
                l.setIcon(null);
            }
            return l;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            /*
            g.setColor(new Color(0xafff00f0,true));
            // g.fillRect(0,0,getWidth(),getHeight());
            Insets i = getInsets();
            System.out.println("ColorComboCellRenderer insets:"+i);
            g.fillRect(i.left,i.top,getWidth()-i.left-i.right,getHeight()-i.top-i.bottom);
             **/
        }
    }

    /** Creates new form. */
    public ComboBoxTest() {
        initComponents();

        String[] items = new String[100];
        for (int i = 0; i < items.length; i++) {
            items[i] = "Item " + (i + 1);
        }
        comboBox2.setModel(new javax.swing.DefaultComboBoxModel(items));


        for (JComponent c : new JComponent[]{smallComboBox1, smallComboBox2, smallComboBox3, smallLabel}) {
            c.putClientProperty("JComponent.sizeVariant", "small");
        }
        for (JComponent c : new JComponent[]{miniComboBox1, miniComboBox2, miniComboBox3,miniLabel}) {
            c.putClientProperty("JComponent.sizeVariant", "mini");
        }
        largeComboBox.putClientProperty("JComponent.sizeVariant", "large");
        iconComboBox.setRenderer(new ColorComboCellRenderer());
        DefaultComboBoxModel cbm = new DefaultComboBoxModel();
        cbm.addElement(Color.red);
        cbm.addElement(Color.green);
        cbm.addElement(Color.blue);
        iconComboBox.setModel(cbm);

        //         iconComboBox.setFont(UIManager.getFont("SmallSystemFont"));

        tableComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        //
        // Try to get a better layout with J2SE6
        try {
            int BASELINE_LEADING = GridBagConstraints.class.getDeclaredField("BASELINE_LEADING").getInt(null);
            GridBagLayout layout = (GridBagLayout) getLayout();
            for (Component c : getComponents()) {
                GridBagConstraints gbc = layout.getConstraints(c);
                if (gbc.anchor == GridBagConstraints.WEST) {
                    gbc.anchor = BASELINE_LEADING;
                    layout.setConstraints(c, gbc);
                }
            }
        } catch (Exception ex) {
            // bail
        }

        comboBox1.addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
               System.out.println("ComboBoxTest.popupMenuWillBecomeVisible "+e);
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
               System.out.println("ComboBoxTest.popupMenuWillBecomeInvisible "+e);
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
               System.out.println("ComboBoxTest.popupMenuCanceled "+e);
            }
        });
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame f = new JFrame("Quaqua ComboBox Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new ComboBoxTest());
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

        comboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        comboBox2 = new javax.swing.JComboBox();
        comboBox3 = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        smallLabel = new javax.swing.JLabel();
        smallComboBox1 = new javax.swing.JComboBox();
        smallComboBox2 = new javax.swing.JComboBox();
        smallComboBox3 = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        miniLabel = new javax.swing.JLabel();
        miniComboBox1 = new javax.swing.JComboBox();
        miniComboBox2 = new javax.swing.JComboBox();
        miniComboBox3 = new javax.swing.JComboBox();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        largeComboBox = new javax.swing.JComboBox();
        iconComboBox = new javax.swing.JComboBox();
        iconLabel = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        tableLabel = new javax.swing.JLabel();
        disabledLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 17, 17, 17));
        setLayout(new java.awt.GridBagLayout());

        comboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(comboBox1, gridBagConstraints);

        jLabel2.setText("Enabled");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 0);
        add(jLabel2, gridBagConstraints);

        comboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(comboBox2, gridBagConstraints);

        comboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        comboBox3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(comboBox3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jSeparator1, gridBagConstraints);

        smallLabel.setText("Small");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(smallLabel, gridBagConstraints);

        smallComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(smallComboBox1, gridBagConstraints);

        smallComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(smallComboBox2, gridBagConstraints);

        smallComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        smallComboBox3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(smallComboBox3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jSeparator2, gridBagConstraints);

        miniLabel.setText("Mini");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(miniLabel, gridBagConstraints);

        miniComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(miniComboBox1, gridBagConstraints);

        miniComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(miniComboBox2, gridBagConstraints);

        miniComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        miniComboBox3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(miniComboBox3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jSeparator3, gridBagConstraints);

        jLabel5.setText("Large Font");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jLabel5, gridBagConstraints);

        largeComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        largeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(largeComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(iconComboBox, gridBagConstraints);

        iconLabel.setText("Icon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(iconLabel, gridBagConstraints);

        tableComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(tableComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 99;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        tableLabel.setText("Table Cell");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(tableLabel, gridBagConstraints);

        disabledLabel.setText("Disabled");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(disabledLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboBox1;
    private javax.swing.JComboBox comboBox2;
    private javax.swing.JComboBox comboBox3;
    private javax.swing.JLabel disabledLabel;
    private javax.swing.JComboBox iconComboBox;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JComboBox largeComboBox;
    private javax.swing.JComboBox miniComboBox1;
    private javax.swing.JComboBox miniComboBox2;
    private javax.swing.JComboBox miniComboBox3;
    private javax.swing.JLabel miniLabel;
    private javax.swing.JComboBox smallComboBox1;
    private javax.swing.JComboBox smallComboBox2;
    private javax.swing.JComboBox smallComboBox3;
    private javax.swing.JLabel smallLabel;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
