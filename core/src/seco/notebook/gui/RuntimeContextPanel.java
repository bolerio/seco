/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.util.ArrayList;

import javax.script.Bindings;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import seco.gui.ObjectInspector;
import seco.gui.TopFrame;
import seco.notebook.NotebookUI;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorFactory;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.swing.PercentLayout;

public class RuntimeContextPanel extends PropertySheetPanel
{
	private static final long serialVersionUID = 3366217098891834776L;
    private NotebookUI editor;
	public RuntimeContextPanel(NotebookUI editor){
		this.editor = editor;
		init();
	}
	
	private void init(){
		PropertySheetTable table = new PropertySheetTable();
		table.setEditorFactory(new PropertyEditorFactory(){
		    public PropertyEditor createPropertyEditor(Property property)
		    {
		        return new ObjectInspectorEditor();
		    }
		});
		ArrayList<DefaultProperty> data = new ArrayList<DefaultProperty>();
		Bindings binds = editor.getDoc()
		   .getEvaluationContext().getRuntimeContext().getBindings();
		
		for(String key: binds.keySet())
		{
			MyProperty prop = new MyProperty(key, binds.get(key));
			data.add(prop);
		}
		table.setModel(new MyTableModel(data));
		setTable(table);
		setDescriptionVisible(true);
		//setToolBarVisible(false);
	}
	
	class MyTableModel extends PropertySheetTableModel
	{
		private static final long serialVersionUID = 909365731401215416L;
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
		private static final long serialVersionUID = 7256768642770306541L;
        private String key;
		private Object val;
		public MyProperty(String key, Object val)
		{
			this.key = key;
			this.val = val;
		}
		
		@Override
		public String getShortDescription()
		{
			return (val == null) ? "" : "Class: " + val.getClass().getName();
		}

		@Override
		public Object getValue()
		{
			return val;
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
			return (val != null) ? val.getClass() : null;
		}

		@Override
		public void setValue(Object val)
		{
			//if(val == null) return;
			//System.out.println("setValue: " + val);
		}

//        @Override
//        public boolean isEditable()
//        {
//            return true;
//        }
	}
	
	public class ObjectInspectorEditor extends AbstractPropertyEditor 
	{

	    protected JTextField textfield;
	    private JButton button;
	    private JButton cancelButton;
	    private Object value;
	      
	    public ObjectInspectorEditor() {
	      this(true);
	    }
	    
	    public ObjectInspectorEditor(boolean asTableEditor) {
	      editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
	        public void setEnabled(boolean enabled) {
	          super.setEnabled(enabled);
	          textfield.setEnabled(enabled);
	          button.setEnabled(enabled);
	          cancelButton.setEnabled(enabled);
	        }
	      };
	      ((JPanel)editor).add("*", textfield = new JTextField());
	      ((JPanel)editor).add(button = ComponentFactory.Helper.getFactory()
	        .createMiniButton());
	      if (asTableEditor) {
	        textfield.setBorder(LookAndFeelTweaks.EMPTY_BORDER);
	      }    
	      button.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          showInspector();
	        }
	      });
	    }

	    public Object getValue() {
	      return value;
	    }

	    public void setValue(Object value) 
	    {
	       this.value = value;
	       textfield.setText("" + value);
	   }

	    protected void showInspector()
	    {
	        ObjectInspector propsPanel = new ObjectInspector(value);
	        DialogDescriptor dd = new DialogDescriptor(
	                TopFrame.getInstance(), 
	                new JScrollPane(propsPanel),
	              "Object Inspector: " + ((value == null)? 
	                      "null" : value.getClass().getName()));
	        Object o = DialogDisplayer.getDefault().notify(dd);
	        firePropertyChange(null, value);
	    }
    
	  }
}
