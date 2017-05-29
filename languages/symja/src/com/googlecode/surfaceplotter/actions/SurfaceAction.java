package com.googlecode.surfaceplotter.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.googlecode.surfaceplotter.JSurface;

/** Generic main action for JSurface. to implement :
 * 
 * function1/function2 toggler (f1, f2, both)
 * colortype (5 values)
 * plottype (4 values)
 * boolean attribute 
 * 	- autoscale
 *  - boxed
 *  - displayGrid
 *  - displayXY axis
 *  - displayZ axis
 *  - mesh 
 *  - scalebox
 * 
 * 
 * 
 * @author Eric.Atienza
 *
 */
public abstract class SurfaceAction extends AbstractAction{

	
	JSurface surface;
	
	

	/**
	 * 
	 */
	public SurfaceAction() {
		super();
	}

	/**
	 * @param name
	 * @param icon
	 */
	public SurfaceAction(String name, Icon icon) {
		super(name, icon);
	}

	/**
	 * @param name
	 */
	public SurfaceAction(String name) {
		super(name);
	}
	
	
	public JSurface getSurface() {
		return surface;
	}

	public void setSurface(JSurface surface) {
		this.surface = surface;
	}
}
