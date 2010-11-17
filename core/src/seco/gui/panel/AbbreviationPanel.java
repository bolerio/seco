package seco.gui.panel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import seco.ThisNiche;
import seco.gui.dialog.DialogDescriptor;
import seco.gui.dialog.DialogDisplayer;
import seco.gui.dialog.NotifyDescriptor;
import seco.notebook.AbbreviationManager;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

/**
 * Panel for configuring notebook abbreviations.
 */
public class AbbreviationPanel extends JPanel
{
    private static final long serialVersionUID = -7581546696445962582L;
    SheetPanel propPanel;

    /**
     * Create the panel
     */
    public AbbreviationPanel()
    {
        init();
    }

    private void init()
    {
        setLayout(new GridBagLayout());
        final JButton addButton = new JButton();
        addButton.setText("Add");
        final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
        gridBagConstraints_1.anchor = GridBagConstraints.NORTH;
        gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints_1.gridx = 4;
        gridBagConstraints_1.gridy = 0;
        gridBagConstraints_1.weightx = 1;
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addProperty();
            }
        });
        add(addButton, gridBagConstraints_1);
        final JButton removeButton = new JButton();
        removeButton.setText("Remove");
        final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
        gridBagConstraints_2.anchor = GridBagConstraints.NORTH;
        gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints_2.gridx = 4;
        gridBagConstraints_2.gridy = 1;
        gridBagConstraints_2.weightx = 1;
        add(removeButton, gridBagConstraints_2);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                removeProperty();
            }
        });
        propPanel = new SheetPanel();
        propPanel.setMinimumSize(new Dimension(300, 300));
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weighty = 2;
        gridBagConstraints.weightx = 2;
        add(propPanel, gridBagConstraints);
    }

    private void addProperty()
    {
        NameTextAreaInputPanel panel = new NameTextAreaInputPanel();
        DialogDescriptor d = new DialogDescriptor(ThisNiche.guiController.getFrame(),
                panel, "Add Abrreviation");
        d.setModal(true);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION)
        {
            AbbreviationManager.getInstance().addAbbreviation(panel.getName(),
                    panel.getValue());
            propPanel.init();
        }
    }

    private void removeProperty()
    {
        int row = propPanel.getTable().getSelectedRow();
        if (row == -1) return;
        PropertySheetTableModel.Item item = (PropertySheetTableModel.Item) propPanel
                .getTable().getValueAt(row, 0);
        AbbreviationManager.getInstance().removeAbbreviation((String) item.getKey());
        // System.out.println("Key:" + item.getKey() + ":" + item.getName());
        propPanel.init();
    }

    class SheetPanel extends PropertySheetPanel
    {
        public SheetPanel()
        {
            init();
        }

        void init()
        {
            PropertySheetTable table = new PropertySheetTable();
            ArrayList<DefaultProperty> data = new ArrayList<DefaultProperty>();
            Map<String, String> map = AbbreviationManager.getInstance().getAbbreviations();
            for (String info : map.keySet())
            {
                MyProperty prop = new MyProperty(info, map.get(info));
                data.add(prop);
            }
            table.setModel(new MyTableModel(data));
            setTable(table);
            setDescriptionVisible(true);
            table.getEditorRegistry().
               registerEditor(String.class, StringEditor.class);
        }
    }
    
    public static class StringEditor extends AbstractPropertyEditor
    {
        protected JTextField textfield;
        private JButton button;
        private JButton cancelButton;
        private String value;
        
        public StringEditor() 
        {
            editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    textfield.setEnabled(enabled);
                    button.setEnabled(enabled);
                    cancelButton.setEnabled(enabled);
                }
            };
            ((JPanel) editor).add("*", textfield = new JTextField());
            ((JPanel) editor).add(button = ComponentFactory.Helper.getFactory()
                    .createMiniButton());
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openEditor();
                }
            });
            ((JPanel) editor).add(cancelButton = ComponentFactory.Helper
                    .getFactory().createMiniButton());
            cancelButton.setText("X");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectNull();
                }
            });
        }
        
        protected void openEditor()
        {
            JTextArea textarea = new JTextArea();
            textarea.setText("" + getValue());
            textarea.setLineWrap(true);
            textarea.setWrapStyleWord (true);
            JScrollPane ed = new JScrollPane(textarea);
            ed.setSize(300, 300);
            DialogDescriptor dd = new DialogDescriptor(ThisNiche.guiController.getFrame(),
                    ed, "String Editor");
            if(DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                Object old = getValue(); 
                value = textarea.getText();
                textfield.setText(value);
                firePropertyChange(old, value);
            }
         }
        
        protected void selectNull() {
            Object oldFile = getValue();
            textfield.setText("");
            value = null;
            firePropertyChange(oldFile, null);
        }
        
       public void setValue(Object value) {
            if (value instanceof String) {
                textfield.setText((String)value);
                this.value = (String)value;
            } else {
                textfield.setText("");
            }
        }
        
        public Object getValue() {
            if ("".equals(textfield.getText().trim())) {
                return null;
            } else {
                return value; //textfield.getText();
            }
        }
      }

    class MyTableModel extends PropertySheetTableModel
    {
        private ArrayList<DefaultProperty> data;

        public MyTableModel(final ArrayList<DefaultProperty> data)
        {
            this.data = data;
            setProperties(data.toArray(new DefaultProperty[data.size()]));
        }

        public int getColumnCount()
        {
            return 2;
        }

        public int getRowCount()
        {
            return data.size();
        }
    }

    private static class MyProperty extends DefaultProperty
    {
        private String key;
        private String value;

        public MyProperty(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getShortDescription()
        {
            return value;
        }

        @Override
        public Object getValue()
        {
            return value;
        }

        @Override
        public String getName()
        {
            return key;
        }

        public String getDisplayName()
        {
            return getName();
        }

        @Override
        public Class<?> getType()
        {
            return String.class;
        }

        @Override
        public void setValue(Object val)
        {
            if (val == null) return;
            value = (String) val;
            AbbreviationManager.getInstance().addAbbreviation(key, value);
        }
    }
    
    public static class NameTextAreaInputPanel extends JPanel 
    {
        private static final long serialVersionUID = 2941023799059546189L;
        private JTextField nameLabel;
        private JTextArea textArea;
        
        public NameTextAreaInputPanel() {
            initComponents();
        }
        
        public String getName()
        {
            return nameLabel.getText();
        }
        
        public String getValue()
        {
            return textArea.getText();
        }

        private void initComponents() {

            JLabel jLabel1 = new JLabel("Abbreviation: ");
            JLabel jLabel2 = new JLabel("Full Text: ");
            nameLabel = new JTextField();
            JScrollPane jScrollPane1 = new JScrollPane();
            textArea = new JTextArea();
            textArea.setColumns(20);
            textArea.setRows(5);
            textArea.setWrapStyleWord(true);
            jScrollPane1.setViewportView(textArea);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(nameLabel, GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(92, 92, 92))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                            .addContainerGap())))
            );
        }
     }
}
