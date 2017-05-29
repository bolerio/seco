/*
CAS Computer Algebra System
Copyright (C) 2005  William Tracy

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package symja.plot;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Plots parametric shapes.
 */
public class Plot3DPlotter extends AbstractPlotter3D {
	/**
	 * The number of points to plot on each line on this plot.
	 */
	protected int thisResolution;

	/**
	 * The number of lines to plot.
	 */
	protected int numFuncs;

//	/**
//	 * The minimum bound on the independent variable.
//	 */
//	protected double tMin;
//
//	/**
//	 * The maximum bounds on the independent variable.
//	 */
//	protected double tMax;
//
//	/**
//	 * The range of the independent variable.
//	 */
//	protected double tRange;

	/**
	 * The independent variable.
	 */
	// protected HObject t;

	/**
	 * The x coordinates of the points plotted. They are indexed first by plot
	 * number, then by the number of the point along the plot (i.e.
	 * xPoints[functionNumber][pointNumber]).
	 */
	protected double xPoints[][];

	/**
	 * The y coordinates of the points plotted.
	 */
	protected double yPoints[][];

	protected String f1Function;

	protected String f2Function;

	/**
	 * Contains the hidden plots for re-use.
	 */
	protected static List cache = new LinkedList();

	// public void updatePlot() {
	// thisResolution = newResolution;
	// setupText();
	// EventQueue.invokeLater(this);
	// }

	public Plot3DPlotter() {
		super(createDefaultModel());

	}

	private static StringSurfaceModel createDefaultModel() {
		StringSurfaceModel model = new StringSurfaceModel();
		new Thread(model).start();
		return model;
	}

	public void setFunctions(java.util.List<String> functions) {
		numFuncs = functions.size() / 2;
		//
		// xPoints = new double[numFuncs][thisResolution + 1];
		// yPoints = new double[numFuncs][thisResolution + 1];
		// color = new Color[numFuncs];

		ListIterator<String> i = functions.listIterator();
		String f1 = "";
		String f2 = "";
		if (i.hasNext()) {
			f1 = (String) i.next();
		}
		if (i.hasNext()) {
			f2 = (String) i.next();
		}
		StringSurfaceModel model = new StringSurfaceModel();
		model.setF1Function(f1);
		model.setF2Function(f2);
		model.setXMin((float) xMin);
		model.setXMax((float) xMax);
		model.setYMin((float) yMin);
		model.setYMax((float) yMax);
		new Thread(model).start();
		setModel(model);
		repaint();
	}

//	public void setTMax(double in) {
//		tMax = in;
//		tRange = tMax - tMin;
//	}
//
//	public void setTMin(double in) {
//		tMin = in;
//		tRange = tMax - tMin;
//	}

	/**
	 * Returns either a new plot or a plot from a cache.
	 */
	public static AbstractPlotter3D getPlot3DPlotter() {
		if (cache.isEmpty()) {
			return new Plot3DPlotter();
		} else {
			Plot3DPlotter pp = (Plot3DPlotter) cache.get(0);
			cache.remove(pp);
			return pp;
		}
	}

	/**
	 * Caches the unused plot.
	 */
	public void reclaim() {
		xPoints = null;
		yPoints = null;
		cache.add(this);
	}

	/**
	 * Empties the cache. Called when the applet is stopped, which hoses the Swing
	 * components.
	 */
	public static void clearCache() {
		cache.clear();
	}
}
