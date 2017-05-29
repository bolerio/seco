/*
       ____  _____  ___  ____    __      ____  _____  _    _  ____  ____ 
      (  _ \(  _  )/ __)( ___)  /__\    (  _ \(  _  )( \/\/ )( ___)(  _ \
       )(_) ))(_)(( (__  )__)  /(__)\    )___/ )(_)(  )    (  )__)  )   /
      (____/(_____)\___)(____)(__)(__)  (__)  (_____)(__/\__)(____)(_)\_)

* Created Oct 8, 2010 by : Eric.Atienza@doceapower.com
* Copyright Docea Power 2010
* Any reproduction or distribution prohibited without express written permission from Docea Power
***************************************************************************
*/
package com.googlecode.surfaceplotter.beans;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.Action;
import javax.swing.Icon;


/**
 * @author Eric.Atienza
 *
 */
public class JBindedCheckBox extends javax.swing.JCheckBox {

	
	ModelBindedBeanProperty<Boolean> property = new ModelBindedBeanProperty<Boolean>("surfaceModel") {
		
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue() ;
			if (newValue!=null) setSelected((Boolean) newValue);
		}
	};
	
	/**
	 * 
	 */
	public JBindedCheckBox() {
		super();
	}

	/**
	 * @param a
	 */
	public JBindedCheckBox(Action a) {
		super(a);
	}

	/**
	 * @param icon
	 * @param selected
	 */
	public JBindedCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	/**
	 * @param icon
	 */
	public JBindedCheckBox(Icon icon) {
		super(icon);
	}

	/**
	 * @param text
	 * @param selected
	 */
	public JBindedCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	/**
	 * @param text
	 * @param icon
	 * @param selected
	 */
	public JBindedCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JBindedCheckBox(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * @param text
	 */
	public JBindedCheckBox(String text) {
		super(text);
	}

	
	
	
	
	/* intercept the actionperformed to fire my own
	 */
	@Override protected void fireActionPerformed(ActionEvent event) {
		// toogles the property
		Object old= property.getProperty() ;
		if (old !=null) property.setProperty(! (Boolean) old);
		super.fireActionPerformed(event);
	}

	// ##########################################################################
// DELEGATE TO THE MODELBINDEDBEAN BEGIN
// ##########################################################################
	
	/**
	 * @return
	 * @see com.googlecode.surfaceplotter.beans.BeanProperty#getProperty()
	 */
	public Boolean getProperty() {
		return property.getProperty();
	}

	

	
	/**
	 * @param value
	 * @see com.googlecode.surfaceplotter.beans.BeanProperty#setProperty(java.lang.Object)
	 */
	public void setProperty(Boolean value) {
		property.setProperty(value);
	}

	/**
	 * @return
	 * @see com.googlecode.surfaceplotter.beans.BeanProperty#getPropertyName()
	 */
	public String getPropertyName() {
		return property.getPropertyName();
	}

	/**
	 * @param propertyName
	 * @see com.googlecode.surfaceplotter.beans.BeanProperty#setPropertyName(java.lang.String)
	 */
	public void setPropertyName(String propertyName) {
		property.setPropertyName(propertyName);
	}

	/**
	 * @return
	 * @see com.googlecode.surfaceplotter.beans.ModelBindedBeanProperty#getSourceBean()
	 */
	public ModelSource getSourceBean() {
		return property.getSourceBean();
	}
	
	/**
	 * @param modelSource
	 * @see com.googlecode.surfaceplotter.beans.ModelBindedBeanProperty#setSourceBean(java.lang.Object)
	 */
	public void setSourceBean(ModelSource modelSource) {
		property.setSourceBean(modelSource);
	}

	

	
// ##########################################################################
// DELEGATE TO THE MODELBINDEDBEAN END
// ##########################################################################


	
	
	
}
