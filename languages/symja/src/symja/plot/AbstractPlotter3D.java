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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.util.List;

import com.googlecode.surfaceplotter.JSurfacePanel;
import com.googlecode.surfaceplotter.SurfaceModel;

/**
 * The parent of all other three-dimensional Plotters.
 * 
 */
public abstract class AbstractPlotter3D extends JSurfacePanel {
	/**
	 * The ascent of the font used.
	 */
	protected int ascent;

	/**
	 * The length (in pixels) of xMinText.
	 */
	protected int xMinLength;

	/**
	 * The length (in pixels) of xMaxText.
	 */
	protected int xMaxLength;

	/**
	 * The minimum bound along the Y axis.
	 */
	protected double yMin;

	/**
	 * The maximum bound along the Y axis.
	 */
	protected double yMax;

	/**
	 * The range along the Y axis.
	 */
	protected double yRange;

	/**
	 * The minimum bound along the X axis.
	 */
	protected double xMin;

	/**
	 * The maximum bound along the X axis.
	 */
	protected double xMax;

	/**
	 * The range along the X axis.
	 */
	protected double xRange;

	/**
	 * The label on the Y axis.
	 */
	protected GlyphVector yLabel;

	/**
	 * The label on the X axis.
	 */
	protected GlyphVector xLabel;

	/**
	 * The length (in pixels) of yLabel.
	 */
	protected int yLabelLength;

	/**
	 * The length (in pixels) of xLabel.
	 */
	protected int xLabelLength;

	/**
	 * The text displayed on the X axis label.
	 */
	protected String xText = "X";

	/**
	 * The text displayed on the Y axis label.
	 */
	protected String yText = "Y";

	/**
	 * The label at the maximum bound of the Y axis.
	 */
	protected GlyphVector yMaxText;

	/**
	 * The label at the minimum bound of the Y axis.
	 */
	protected GlyphVector yMinText;

	/**
	 * The label at the maximum bound of the X axis.
	 */
	protected GlyphVector xMaxText;

	/**
	 * The label at the minimum bound of the X axis.
	 */
	protected GlyphVector xMinText;

	/**
	 * The length of the yMax label (in pixels).
	 */
	protected float yMaxLength;

	/**
	 * The length of the yMin label (in pixels).
	 */
	protected float yMinLength;

	/**
	 * The colors associated with the different plots.
	 */
	protected Color color[];

	/**
	 * The default colors used, unless explicitly overridden.
	 */
	protected static Color COLOR[] = new Color[] { Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.PINK,
			Color.YELLOW, Color.CYAN };

	/**
	 * The number of points to plot on new plots.
	 */
	protected static int newResolution = 1000;

	/**
	 * The number of points to plot on this plot.
	 */
	protected int thisResolution;

	/**
	 * The font used by plots.
	 */
	protected static Font f = new Font("SansSerif", Font.PLAIN, 12);

	/**
	 * Sets the resolution (number of points plotted) on future plots. Does not
	 * affect existing plots that are already displayed.
	 */
	public static void setResolution(int arg) {
		newResolution = arg;
	}

	/**
	 * Creates a new plotter and initializes the frame associated with it.
	 */
	public AbstractPlotter3D(SurfaceModel model) {
		super(model);
		setMinimumSize(new Dimension(0, 0));
		setPreferredSize(new Dimension(640, 640));
		setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		// updatePlot();
	}

	/**
	 * All plots are opaque.
	 */
	public boolean isOpaque() {
		return true;
	}

	/**
	 * Plots a new set of function(s) and readies them for display.
	 */
	// public abstract void updatePlot();

	public abstract void setFunctions(List<String> functions);

	/**
	 * Caches this object for future reuse, preventing memory leaks if the garbage
	 * collector gets sloppy on us.
	 */
	public abstract void reclaim();

	/**
	 * Sets up the text labels for the plot.
	 */
	protected void setupText() {
		FontRenderContext frc = new FontRenderContext(null, false, false);

		ascent = (int) new TextLayout(Double.toString(yMax), f, frc).getAscent();

		yLabel = f.createGlyphVector(frc, yText);
		xLabel = f.createGlyphVector(frc, xText);

		xLabelLength = (int) xLabel.getVisualBounds().getWidth();
		yLabelLength = (int) yLabel.getVisualBounds().getWidth();

		yMaxText = f.createGlyphVector(frc, Double.toString(yMax));
		yMinText = f.createGlyphVector(frc, Double.toString(yMin));
		xMaxText = f.createGlyphVector(frc, Double.toString(xMax));
		xMinText = f.createGlyphVector(frc, Double.toString(xMin));

		xMaxLength = (int) xMaxText.getVisualBounds().getWidth();
		xMinLength = (int) xMinText.getVisualBounds().getWidth();
		yMaxLength = (float) yMaxText.getVisualBounds().getWidth();
		yMinLength = (float) yMinText.getVisualBounds().getWidth();
		xMaxLength += 2;
		xMinLength += 2;
	}

	/**
	 * Fills the background and draws axes.
	 */
	// protected void fillBackground(Graphics2D g2d, int xAxis, int yAxis, int
	// top, int bottom, int left, int right) {
	// g2d.setColor(Color.WHITE);
	// g2d.fillRect(0, 0, getWidth(), getHeight());
	//
	// g2d.setColor(Color.BLACK);
	// g2d.drawLine(yAxis, top - ascent, yAxis, bottom);
	// g2d.drawLine(left, xAxis, right + ascent, xAxis);
	// }

	/**
	 * Paints the labels and arrows on the axes.
	 */
	// protected void paintAxisLabels(Graphics2D g2d, int xAxis, int yAxis, int
	// top, int bottom, int left, int right) {
	// Shape yArrow = new Polygon(new int[] { yAxis, yAxis - ascent / 2, yAxis,
	// yAxis + ascent / 2 }, new int[] { ascent, ascent * 2,
	// ascent * 5 / 3, ascent * 2 }, 4);
	// g2d.setPaint(new GradientPaint(0, ascent, Color.BLACK, 0, ascent * 2,
	// Color.WHITE, true));
	// g2d.fill(yArrow);
	//
	// g2d.setPaint(Color.BLACK);
	// g2d.drawGlyphVector(yLabel, yAxis - (yLabelLength / 2), ascent);
	//
	// g2d.setPaint(new GradientPaint(right, 0, Color.WHITE, right + ascent, 0,
	// Color.BLACK));
	// g2d.fill(new Polygon(new int[] { right, right + ascent, right, right +
	// ascent / 3 }, new int[] { xAxis - ascent / 2, xAxis,
	// xAxis + ascent / 2, xAxis }, 4));
	//
	// g2d.setPaint(Color.BLACK);
	// g2d.drawGlyphVector(xLabel, right + ascent, xAxis + ascent / 2);
	//
	// g2d.drawLine(yAxis, top, yAxis - ascent / 2, top);
	// g2d.drawLine(yAxis, bottom, yAxis - ascent / 2, bottom);
	//
	// g2d.drawGlyphVector(yMaxText, yAxis - ascent - yMaxLength, top + ascent /
	// 2);
	// g2d.drawGlyphVector(yMinText, yAxis - ascent - yMinLength, bottom + ascent
	// / 2);
	//
	// g2d.drawLine(left, xAxis, left, xAxis + ascent / 2);
	// g2d.drawLine(right, xAxis, right, xAxis + ascent / 2);
	//
	// g2d.drawGlyphVector(xMaxText, right - xMaxLength / 2, xAxis + ascent * 2);
	// g2d.drawGlyphVector(xMinText, left - xMinLength / 2, xAxis + ascent * 2);
	// }

	/**
	 * Gets the top edge of the area to draw the plots in.
	 */
	// protected int getTop() {
	// return 2 * ascent;
	// }

	/**
	 * Gets the bottom edge of the area to draw the plots in.
	 */
	// protected int getBottom() {
	// return getHeight() - ascent * 2;
	// }

	/**
	 * Gets the height of the area to draw the plots in.
	 */
	// protected int getDrawableHeight(int top, int bottom) {
	// return bottom - top;
	// }

	/**
	 * Gets the left edge of the area to draw the plots in.
	 */
	// protected int getLeft() {
	// return Math.max(xMinLength / 2, (int) Math.max(yMaxLength + ascent,
	// yMinLength + ascent));
	// }

	/**
	 * Gets the right edge of the area to draw the plots in.
	 */
	// protected int getRight() {
	// int right = getWidth() - (int) (ascent + xLabelLength);
	// if (right + xMaxLength / 2 > getWidth())
	// return getWidth() - xMaxLength / 2;
	// else
	// return right;
	// }

	/**
	 * Gets the width of the area to draw the plots in.
	 */
	// protected int getDrawableWidth(int left, int right) {
	// return right - left;
	// }

	/**
	 * Gets the position of the Y axis.
	 */
	// protected int yAxis(int left, int width) {
	// return left + (int) (-xMin * width / xRange);
	// }

	/**
	 * Gets the position of the X axis.
	 */
	// protected int xAxis(int top, int height) {
	// return top + height - (int) (-yMin * height / yRange);
	// }

	public void setXMin(Double newMin) {
		xMin = newMin;
		xRange = xMax - xMin;
		// updatePlot();
	}

	public void setXMax(Double newMax) {
		xMax = newMax;
		xRange = xMax - xMin;
		// updatePlot();
	}

	public void setYMin(Double newMin) {
		yMin = newMin;
		yRange = yMax - yMin;
		// updatePlot();
	}

	public void setYMax(Double newMax) {
		yMax = newMax;
		yRange = yMax - yMin;
		// updatePlot();
	}
}
