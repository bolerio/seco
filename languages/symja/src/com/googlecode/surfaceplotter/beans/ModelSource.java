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

import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;

/**
 * @author Eric.Atienza
 *
 */
public class ModelSource {

	SwingPropertyChangeSupport event = new SwingPropertyChangeSupport(this);
	AbstractSurfaceModel surfaceModel;
	
	
	/**
	 * @return the surfaceModel
	 */
	public AbstractSurfaceModel getSurfaceModel() {
		return surfaceModel;
	}

	/**
	 * @param surfaceModel the surfaceModel to set
	 */
	public void setSurfaceModel(AbstractSurfaceModel surfaceModel) {
		Object old = this.surfaceModel ;
		this.surfaceModel = surfaceModel;
		event.firePropertyChange("surfaceModel", old, surfaceModel);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		event.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		event.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		event.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		event.removePropertyChangeListener(propertyName, listener);
	}
	
	
	
	
}
