package test;

import ch.randelshofer.quaqua.QuaquaManager;
import javax.swing.*;
import javax.swing.text.*;

import java.awt.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ColorRenderer extends JLabel implements TableCellRenderer {
    Border m_UnselectedBorder = null;
    
    Border m_SelectedBorder = null;
    
    boolean m_IsBordered = true;
    
    public ColorRenderer(boolean isBordered) {
        super();
        this.m_IsBordered = isBordered;
        setOpaque(true);
    }
    
    public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
    
    {
        setBackground((Color) color);
        if (m_IsBordered) {
            if (isSelected) {
                if (m_SelectedBorder == null) {
                    m_SelectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                }
                setBorder(m_SelectedBorder);
            } else {
                if (m_UnselectedBorder == null) {
                    m_UnselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                }
                setBorder(m_UnselectedBorder);
            }
        }
        return this;
    }
    
    
    static private class ColorEditor extends DefaultCellEditor {
        Color m_CurrentColor = null;
        
        public ColorEditor(JButton b) {
            super(new JCheckBox());
            
            editorComponent = b;
            setClickCountToStart(2);
            
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
        
        public Object getCellEditorValue() {
            return m_CurrentColor;
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ((JButton) editorComponent).setText(value.toString());
            m_CurrentColor = (Color) value;
            return editorComponent;
        }
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame("Color Renderer Test");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                DefaultTableModel model = new DefaultTableModel(new Object[] {"Color", "Color"}, 4) {
                    public Class getColumnClass(int column) {
                        return Color.class;
                    }
                };
                for (int y=0; y < 2; y++) {
                for (int x=0; x < 2; x++) {
                model.setValueAt(Color.red, y, x);
                }
                }
                JTable table = new JTable();
                table.putClientProperty("Quaqua.Table.style","striped");
                table.setModel(model);
                table.setDefaultRenderer(Color.class, new ColorRenderer(true));
                table.setDefaultEditor(Color.class, new ColorEditor(new JButton()));
                JScrollPane sc = new JScrollPane();
                sc.setViewportView(table);
                f.getContentPane().add(sc);
                f.setSize(400,400);
                f.setVisible(true);
            }
        });
    }
}