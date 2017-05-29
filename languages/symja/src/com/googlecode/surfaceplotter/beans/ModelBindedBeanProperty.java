/*
       ____  _____  ___  ____    __      ____  _____  _    _  ____  ____ 
      (  _ \(  _  )/ __)( ___)  /__\    (  _ \(  _  )( \/\/ )( ___)(  _ \
       )(_) ))(_)(( (__  )__)  /(__)\    )___/ )(_)(  )    (  )__)  )   /
      (____/(_____)\___)(____)(__)(__)  (__)  (_____)(__/\__)(____)(_)\_)

* Created Oct 7, 2010 by : Eric.Atienza@doceapower.com
* Copyright Docea Power 2010
* Any reproduction or distribution prohibited without express written permission from Docea Power
***************************************************************************
*/
package com.googlecode.surfaceplotter.beans;

import java.beans.PropertyChangeEvent;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;



/** bind the "bean" attribute to a "source" property of a model provider.
 * @author Eric.Atienza
 * 
 */
public abstract class ModelBindedBeanProperty<PROP> extends BeanProperty<AbstractSurfaceModel, PROP>{
	
	
	
	
	BeanProperty<ModelSource, AbstractSurfaceModel> sourceBeanProperty = new BeanProperty<ModelSource, AbstractSurfaceModel>() {
		
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			ModelBindedBeanProperty.this.setBean((AbstractSurfaceModel) evt.getNewValue());
		}
	};
	
	
	public ModelBindedBeanProperty(String sourcePropertyName) {
		setSourcePropertyName(sourcePropertyName);
	}
	
	/**
	 * @return the modelSource
	 */
	public ModelSource getSourceBean() {
		return sourceBeanProperty.getBean();
	}
	/**
	 * @param modelSource the modelSource to set
	 */
	public void setSourceBean(ModelSource modelSource) {
		sourceBeanProperty.setBean(modelSource);
	}
	/**
	 * @return the modelSourcePropertyName
	 */
	public String getSourcePropertyName() {
		return sourceBeanProperty.getPropertyName();
	}
	/**
	 * @param modelSourcePropertyName the modelSourcePropertyName to set
	 */
	public void setSourcePropertyName(String modelSourcePropertyName) {
		sourceBeanProperty.setPropertyName(modelSourcePropertyName);
	}
	
	
	
}
