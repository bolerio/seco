/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import seco.notebook.storage.ClassRepository;
import seco.notebook.storage.DocInfo;
import seco.notebook.storage.JarInfo;
import seco.notebook.storage.RtDocInfo;

import com.l2fprod.common.beans.editor.DirectoryPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class JavaDocPanel extends PropertySheetPanel
{
	public JavaDocPanel(){
		init();
	}
	
	private void init(){
		PropertySheetTable table = new PropertySheetTable();
		ArrayList<DefaultProperty> data = new ArrayList<DefaultProperty> ();
		Map<JarInfo, DocInfo> map = ClassRepository.getInstance().getJavaDocAssoiciations();
		for(JarInfo info: map.keySet())
		{
			MyProperty prop = new MyProperty(info, map.get(info));
			data.add(prop);
		}
		data.add(0, new RtProperty(ClassRepository.getInstance().getRtDocInfo()));
		table.setModel(new MyTableModel(data));
		setTable(table);
		setDescriptionVisible(true);
		//setToolBarVisible(false);
		table.getEditorRegistry().
		    registerEditor(File.class, DirectoryPropertyEditor.class);
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
		private JarInfo jar;
		private DocInfo doc;
		public MyProperty(JarInfo jar, DocInfo doc){
			this.jar = jar;
			this.doc = doc;
		}
		
		@Override
		public String getShortDescription()
		{
			return "Full path: " + jar.getPath() + "\n" +
			"Doc: " + ((doc != null) ? doc.getName(): "Not set");
		}

		@Override
		public Object getValue()
		{
			return (doc != null) ? new File("" + doc) : null;
		}

		@Override
		public String getName()
		{
			String s = jar.getPath();
			return s.substring(s.lastIndexOf("\\") + 1);
		}
		
		public String getDisplayName(){
			return getName();
		} 

		@Override
		public Class getType()
		{
			return File.class;
		}

		@Override
		public void setValue(Object val)
		{
			if(val == null) return;
			doc = new DocInfo(((File)val).getAbsolutePath());
			ClassRepository.getInstance().setJavaDocForJar(
					jar.getPath(), doc.getName());
		}
	}
	
	private static class RtProperty extends DefaultProperty
	{
		private RtDocInfo doc;
		public RtProperty(RtDocInfo doc){
			this.doc = doc;
		}
		
		@Override
		public String getShortDescription()
		{
			return "JavaDoc directory for JVM jars. All JVM jars share a single doc path";
		}

		@Override
		public Object getValue()
		{
			if(doc == null || doc.getName().length() == 0) return null;
			return new File("" + doc);
		}

		@Override
		public String getName()
		{
			return "_JVM JavaDoc_";
		}
		
		public String getDisplayName(){
			return getName();
		} 

		@Override
		public Class getType()
		{
			return File.class;
		}

		@Override
		public void setValue(Object val)
		{
			if(val == null) return;
			doc = new RtDocInfo(((File)val).getAbsolutePath());
			ClassRepository.getInstance().setRtJavaDoc(doc.getName());
		}
	}
}
