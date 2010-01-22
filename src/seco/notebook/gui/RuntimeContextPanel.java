/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.util.ArrayList;
import javax.script.Bindings;

import seco.notebook.NotebookUI;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

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
	}
}
