package seco.notebook.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.Bindings;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;

import seco.gui.TopFrame;
import seco.notebook.ActionManager;
import seco.notebook.NotebookUI;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

public class ShortcutPanel extends PropertySheetPanel
{
    public ShortcutPanel()
    {
        init();
    }
    
    private void init(){
        PropertySheetTable table = new PropertySheetTable();
        
        ArrayList<DefaultProperty> data = new ArrayList<DefaultProperty>();
        Map<String, Action> map = ActionManager.getInstance().getActionMap();
        for(String key: map.keySet())
        {
            MyProperty prop = new MyProperty(key, map.get(key));
            data.add(prop);
        }
        table.setModel(new MyTableModel(data));
        setTable(table);
        setDescriptionVisible(true);
        table.getEditorRegistry().registerEditor(KeyStroke.class, ShortcutPanel.KeyStrokeEditor.class);
        PropertyRendererRegistry reg = (PropertyRendererRegistry) table.getRendererFactory(); 
        reg.registerRenderer(KeyStroke.class, ShortcutPanel.KeyStrokeRenderer.class);
    }
    
    private static String getVirtualkeyName(int keycode) {
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifiers = f.getModifiers();
            if (Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && Modifier.isFinal(modifiers)
                && f.getType() == Integer.TYPE
                && f.getName().startsWith("VK_")) { // NOI18N
                try {
                    if (f.getInt(KeyEvent.class) == keycode) {
                        return f.getName();
                    }
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace(); // should not happen
                }
            }
        }
        return null;
    }

    private static KeyStroke keyStrokeFromString(String s) {
        StringTokenizer st = new StringTokenizer(s, "+"); // NOI18N
        String token;
        int mods = 0;
        int keycode = 0;

        String alt = TXT_ALT; 
        String shift = TXT_SHIFT;
        String ctrl = TXT_CTRL;  
        String meta = TXT_META; 

        while (st.hasMoreTokens() &&(token = st.nextToken()) != null) {
            if (alt.equalsIgnoreCase(token)) {
                mods |= InputEvent.ALT_MASK;
            } else if (shift.equalsIgnoreCase(token)) {
                mods |= InputEvent.SHIFT_MASK;
            } else if (ctrl.equalsIgnoreCase(token)) {
                mods |= InputEvent.CTRL_MASK;
            } else if (meta.equalsIgnoreCase(token)) {
                mods |= InputEvent.META_MASK;
            } else {
                String keycodeName = "VK_" + token.toUpperCase(); // NOI18N
                try {
                    keycode = KeyEvent.class.getField(keycodeName).getInt(KeyEvent.class);
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }
        if (keycode != 0) {
            return KeyStroke.getKeyStroke(keycode, mods);
        } else {
            return null;
        }
    }

    private static String TXT_CTRL = "CTRL";
    private static String TXT_ALT = "ALT";
    private static String TXT_SHIFT = "SHIFT";
    private static String TXT_META = "META";
    
    private static String keyStrokeAsString(KeyStroke key) {
        if(key == null) return "";
        String alt = TXT_ALT; 
        String shift = TXT_SHIFT;
        String ctrl = TXT_CTRL;  
        String meta = TXT_META; 

        StringBuffer buf = new StringBuffer();
        int mods = key.getModifiers();
        int modMasks[] = { InputEvent.SHIFT_MASK, InputEvent.CTRL_MASK,
                           InputEvent.ALT_MASK, InputEvent.META_MASK };
        String modMaskStrings[] = { shift, ctrl, alt, meta};

        for (int i = 0; i < modMasks.length; i++) {
            if ((mods & modMasks[i]) != 0) {
                buf.append(modMaskStrings[i]);
                buf.append("+"); // NOI18N
            }
        }
        String keyName = getVirtualkeyName(key.getKeyCode());
        if (keyName != null) {
            buf.append(keyName.substring(3));
        }
        return buf.toString();
    }
    
    class MyTableModel extends PropertySheetTableModel
    {
        private ArrayList<DefaultProperty> data;
        public MyTableModel(final ArrayList<DefaultProperty> data){
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
        private Action action;
        public MyProperty(String key, Action action)
        {
            this.key = key;
            this.action = action;
        }
        
        @Override
        public String getShortDescription()
        {
            String descr = (String) action.getValue(Action.SHORT_DESCRIPTION);
            return (descr == null) ? "" : descr;
        }

        @Override
        public Object getValue()
        {
            return action.getValue(Action.ACCELERATOR_KEY);
        }

        @Override
        public String getName()
        {
            return key;
        }
        
        public String getDisplayName(){
            return getName();
        } 

        @Override
        public Class<?> getType()
        {
            return KeyStroke.class;
        }

        @Override
        public void setValue(Object val)
        {
            if(val == null) return;
            ActionManager.getInstance().putAction(action, (KeyStroke) val);
        }
    }
    
    
    public static class KeyStrokeEditor extends AbstractPropertyEditor
    {
        protected JTextField textfield;
        private JButton button;
        private JButton cancelButton;
        
        public KeyStrokeEditor() 
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
            CustomEditor ed = new CustomEditor();
            DialogDescriptor dd = new DialogDescriptor(TopFrame.getInstance(), ed, "KeyStroke Editor");
            if(DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION)
            {
                Object old = getValue(); 
                textfield.setText(keyStrokeAsString(ed.keystroke));
                firePropertyChange(old, ed.keystroke);
                
            }
         }
        
        protected void selectNull() {
            Object oldFile = getValue();
            textfield.setText("");
            firePropertyChange(oldFile, null);
        }
        
       public void setValue(Object value) {
            if (value instanceof KeyStroke) {
                textfield.setText(keyStrokeAsString((KeyStroke)value));
            } else {
                textfield.setText("");
            }
        }
        
        public Object getValue() {
            if ("".equals(textfield.getText().trim())) {
                return null;
            } else {
                return keyStrokeFromString(textfield.getText());
            }
        }
        
           
       private static String[] _virtualKeys;

        private class CustomEditor extends JPanel
        {
            private KeyGrabberField _keyGrabber;
            private JCheckBox _ctrl, _alt, _shift, _meta;
            private JComboBox _virtualKey;
            KeyStroke keystroke;
            
            CustomEditor() {
                setLayout(new GridBagLayout());
                
              
                JLabel virtualKeyLabel = new JLabel("Virtual Key:");
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.insets = new Insets(12, 12, 5, 12);
                add(virtualKeyLabel, gbc);

                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.weighty = 0;
                gbc.insets = new Insets(12, 0, 5, 11);
                add(_virtualKey = new JComboBox(), gbc);
                //_virtualKey.getAccessibleContext().setAccessibleDescription(
                //    bundle.getString("ACSD_VirtualKey")); // NOI18N

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                _ctrl = new JCheckBox();
                _ctrl.setText("Ctrl"); // NOI18N
                panel.add(_ctrl);
                _alt = new JCheckBox();
                _alt.setText("Alt"); // NOI18N
                panel.add(_alt);
                _shift = new JCheckBox();
                _shift.setText("Shift"); // NOI18N
                panel.add(_shift);
                _meta = new JCheckBox();
                _meta.setText("Meta"); // NOI18N
                panel.add(_meta);
                virtualKeyLabel.setLabelFor(_virtualKey);
                
                gbc = new GridBagConstraints();
                gbc.gridx = 2;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.insets = new Insets(12, 0, 5, 12);
                add(panel, gbc);
                
                JLabel keyStrokeLabel = new JLabel("KeyStroke Grabber:");
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.insets = new Insets(0, 12, 0, 12);
                add(keyStrokeLabel, gbc);

                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.weighty = 0;
                gbc.insets = new Insets(0, 0, 0, 11);
                add(_keyGrabber = new KeyGrabberField(), gbc);
                keyStrokeLabel.setLabelFor(_keyGrabber);

                _keyGrabber.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        keystroke = keyStrokeFromString(
                                _keyGrabber.getText());
                    }
                });
                
                // fill in virtual key list

                if (_virtualKeys == null) {
                    java.util.List<String> list = new ArrayList<String>();

                    Field[] fields = KeyEvent.class.getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        Field f = fields[i];
                        int modifiers = f.getModifiers();
                        if (Modifier.isPublic(modifiers)
                            && Modifier.isStatic(modifiers)
                            && Modifier.isFinal(modifiers)
                            && f.getType() == Integer.TYPE
                            && f.getName().startsWith("VK_")) { // NOI18N
                            list.add(f.getName());
                        }
                    }
                    _virtualKeys = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        _virtualKeys[i] = list.get(i);
                    }
                }
                _virtualKey.addItem(""); // NOI18N
                for (int i = 0; i < _virtualKeys.length; i++)
                    _virtualKey.addItem(_virtualKeys[i]);

                keystroke =(KeyStroke) getValue();
                if (keystroke != null)
                    setKeyStroke(keystroke);

                // listeners

                ItemListener il = new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        virtualKeyChanged();
                    }
                };
                _virtualKey.addItemListener(il);
                _ctrl.addItemListener(il);
                _alt.addItemListener(il);
                _shift.addItemListener(il);
                _meta.addItemListener(il);
            }

            KeyGrabberField getKeyGrabber() {
                return _keyGrabber;
            }

            private void setKeyStroke(KeyStroke key) {
                keystroke = key;
                _ctrl.setSelected(0 !=(InputEvent.CTRL_MASK & key.getModifiers()));
                _alt.setSelected(0 !=(InputEvent.ALT_MASK & key.getModifiers()));
                _shift.setSelected(0 !=(InputEvent.SHIFT_MASK & key.getModifiers()));
                _meta.setSelected(0 !=(InputEvent.META_MASK & key.getModifiers()));

                int keycode = key.getKeyCode();
                String keyName = getVirtualkeyName(keycode);
                if (keyName != null) {
                    _virtualKey.setSelectedItem(keyName);
                    _keyGrabber.setText(
                            keyStrokeAsString(keystroke));
                }
            }

            private void virtualKeyChanged() {
                String keyName =(String) _virtualKey.getSelectedItem();
                if ("".equals(keyName)) { // NOI18N
                    _keyGrabber.setText(""); // NOI18N
                    setValue(null);
                    return;
                }

                try {
                    Field f = KeyEvent.class.getDeclaredField(keyName);
                    int keycode = f.getInt(KeyEvent.class);
                    int mods = 0;
                    if (_ctrl.isSelected())
                        mods |= InputEvent.CTRL_MASK;
                    if (_shift.isSelected())
                        mods |= InputEvent.SHIFT_MASK;
                    if (_alt.isSelected())
                        mods |= InputEvent.ALT_MASK;
                    if (_meta.isSelected())
                        mods |= InputEvent.META_MASK;

                   keystroke = KeyStroke.getKeyStroke(keycode, mods);
                   _keyGrabber.setText(keyStrokeAsString(keystroke));
                }
                catch (NoSuchFieldException ex) {
                    ex.printStackTrace(); // should not happen
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace(); // should not happen
                }
            }

            private class KeyGrabberField extends JTextField 
            {
                @Override
                protected void processKeyEvent(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_TAB)
                        super.processKeyEvent(e);
                    else if (e.getID() == KeyEvent.KEY_PRESSED) {
                        int keycode = e.getKeyCode();
                        if (keycode != KeyEvent.VK_CONTROL
                            && keycode != KeyEvent.VK_ALT
                            && keycode != KeyEvent.VK_SHIFT
                            && keycode != KeyEvent.VK_META) {
                            keystroke = KeyStroke.getKeyStroke(keycode, e.getModifiers());
                            setKeyStroke(keystroke);
                        }
                        e.consume();
                    }
                }
            }
        }
    }
    
    public static class KeyStrokeRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            setText(keyStrokeAsString((KeyStroke) value));
            return c;
        }
        
    }
 }