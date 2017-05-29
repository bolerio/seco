package symja.plot;

import java.awt.Frame;

import javax.swing.JPanel;

public class PlotWindow extends AbstractPlotWindow {
	private static final long serialVersionUID = 5765572179264246613L;

	public PlotWindow(Frame parent) {
		super(parent);
	}

	public Plotter createPlot() {
		return Plotter.getPlotter();
	}

	public void addField() {
		// addField("y(x) = ", "Sin[x]^3");
	}

	@Override
	protected JPanel createMinMaxControls() {
		JPanel panel = super.createMinMaxControls();
		xMin.setValue(-10.0);
		xMax.setValue(10.0);
		yMin.setValue(-2.0);
		yMax.setValue(2.0);
		return panel;
	}
}
