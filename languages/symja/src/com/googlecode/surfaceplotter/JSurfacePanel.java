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
package com.googlecode.surfaceplotter;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.googlecode.surfaceplotter.AbstractSurfaceModel.Plotter;
import com.googlecode.surfaceplotter.SurfaceModel.PlotColor;
import com.googlecode.surfaceplotter.SurfaceModel.PlotType;

/**
 * @author Eric.Atienza
 * 
 */
public class JSurfacePanel extends JPanel {

	private VerticalConfigurationPanel configurationPanel;
	private JSurface surface;
	private JLabel title;

	public JSurfacePanel() {
		this(createDefaultSurfaceModel());
	}

	/**
	 * @return
	 */
	private static SurfaceModel createDefaultSurfaceModel() {
		final AbstractSurfaceModel sm = new AbstractSurfaceModel();

		sm.setPlotFunction2(false);
		
		sm.setCalcDivisions(100);
		sm.setDispDivisions(30);
		sm.setContourLines(10);

		sm.setXMin(-3);
		sm.setXMax(3);
		sm.setYMin(-3);
		sm.setYMax(3);

		sm.setBoxed(false);
		sm.setDisplayXY(false);
		sm.setExpectDelay(false);
		sm.setAutoScaleZ(true);
		sm.setDisplayZ(false);
		sm.setMesh(false);
		sm.setPlotType(PlotType.SURFACE);
		//sm.setPlotType(PlotType.WIREFRAME);
		//sm.setPlotType(PlotType.CONTOUR);
		//sm.setPlotType(PlotType.DENSITY);

		sm.setPlotColor(PlotColor.SPECTRUM);
		//sm.setPlotColor(PlotColor.DUALSHADE);
		//sm.setPlotColor(PlotColor.FOG);
		//sm.setPlotColor(PlotColor.OPAQUE);
		
		new Thread(new Runnable() {
			public  float f1( float x, float y)
			{
				float r = x*x+y*y;
				
				if (r == 0 ) return 1f;
				return (float)( Math.sin(r)/(r));
			}
			
			public  float f2( float x, float y)
			{
				return (float)(Math.sin(x*y));
			}
			public void run()
			{
				Plotter p = sm.newPlotter(sm.getCalcDivisions());
				int im=p.getWidth();
				int jm=p.getHeight();
				for(int i=0;i<im;i++)
					for(int j=0;j<jm;j++)
					{
						float x,y;
						x=p.getX(i);
						y=p.getY(j);
						p.setValue(i,j,f1(x,y),f2(x,y) );
					}
			}
		}).start();
		
		return sm;

	}

	public JSurfacePanel(SurfaceModel model) {
		super(new BorderLayout());
		configurationPanel = new VerticalConfigurationPanel();
		surface = new JSurface(model);
		title = new JLabel();
		title.setHorizontalAlignment(JLabel.CENTER);

		add(surface, BorderLayout.CENTER);
		add(configurationPanel, BorderLayout.EAST);
		add(title, BorderLayout.NORTH);
		setModel(model);

	}
	
	public void setModel(SurfaceModel model) {
		if (model instanceof AbstractSurfaceModel)
			configurationPanel.setModel((AbstractSurfaceModel) model);
		else {
			configurationPanel.setVisible(false);
			configurationPanel.setModel(null);
		}
		surface.setModel(model);
	}

	/**
	 * @return
	 * @see java.awt.Component#getFont()
	 */
	public Font getTitleFont() {
		return title.getFont();
	}

	/**
	 * @return
	 * @see javax.swing.JLabel#getIcon()
	 */
	public Icon getTitleIcon() {
		return title.getIcon();
	}

	/**
	 * @return
	 * @see javax.swing.JLabel#getText()
	 */
	public String getTitleText() {
		return title.getText();
	}

	/**
	 * @return
	 * @see java.awt.Component#isVisible()
	 */
	public boolean isTitleVisible() {
		return title.isVisible();
	}

	/**
	 * @param font
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	public void setTitleFont(Font font) {
		title.setFont(font);
	}

	/**
	 * @param icon
	 * @see javax.swing.JLabel#setIcon(javax.swing.Icon)
	 */
	public void setTitleIcon(Icon icon) {
		title.setIcon(icon);
	}

	/**
	 * @param text
	 * @see javax.swing.JLabel#setText(java.lang.String)
	 */
	public void setTitleText(String text) {
		title.setText(text);
	}

	/**
	 * @param aFlag
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setTitleVisible(boolean aFlag) {
		title.setVisible(aFlag);
	}

	/**
	 * @return
	 * @see java.awt.Component#isVisible()
	 */
	public boolean isConfigurationVisible() {
		return configurationPanel.isVisible();
	}

	/**
	 * @param aFlag
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setConfigurationVisible(boolean aFlag) {
		configurationPanel.setVisible(aFlag);
	}
	
	
	

}
