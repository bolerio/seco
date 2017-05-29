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
import javax.swing.JRadioButton;

/**
 * @author Eric.Atienza
 * 
 */
public class JBindedRadioButton extends JRadioButton {

	ModelBindedBeanProperty<Boolean> property = new ModelBindedBeanProperty<Boolean>("surfaceModel") {
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue();
			if (newValue != null)
				setSelected((Boolean) newValue);
		}
	};

	/**
	 * 
	 */
	public JBindedRadioButton() {}

	/**
	 * @param icon
	 */
	public JBindedRadioButton(Icon icon) {
		super(icon);
	}

	/**
	 * @param a
	 */
	public JBindedRadioButton(Action a) {
		super(a);
	}

	/**
	 * @param text
	 */
	public JBindedRadioButton(String text) {
		super(text);
	}

	/**
	 * @param icon
	 * @param selected
	 */
	public JBindedRadioButton(Icon icon, boolean selected) {
		super(icon, selected);
	}

	/**
	 * @param text
	 * @param selected
	 */
	public JBindedRadioButton(String text, boolean selected) {
		super(text, selected);
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JBindedRadioButton(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * @param text
	 * @param icon
	 * @param selected
	 */
	public JBindedRadioButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		// TODO Auto-generated constructor stub
	}

	/* intercept the actionperformed to fire my own
	 */
	@Override protected void fireActionPerformed(ActionEvent event) {
		// toogles the property 
		Boolean old = property.getProperty();
		if (old != null && !old)
			property.setProperty(true);
		super.fireActionPerformed(event);
	}

	// ##########################################################################
	// DELEGATED SECTION BEGIN
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
	// DELEGATED SECTION END
	// ##########################################################################

}
