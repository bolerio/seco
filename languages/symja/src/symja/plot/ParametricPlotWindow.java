package symja.plot;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ParametricPlotWindow extends AbstractPlotWindow {
	protected SpinnerNumberModel tMin;
	protected SpinnerNumberModel tMax;

	public ParametricPlotWindow(JFrame parent) {
		super(parent);
	}

	protected ParametricPlotter createPlot() {
		return ParametricPlotter.getParametricPlotter();
	}

	protected JPanel createMinMaxControls() {
		JPanel controls = super.createMinMaxControls();
		xMin.setValue(-2.0);
		xMax.setValue(2.0);
		yMin.setValue(-2.0);
		yMax.setValue(2.0);
		tMin = new SpinnerNumberModel(0.0, -999.0, 999.0, 1.0);
		tMax = new SpinnerNumberModel(7, -999.0, 999.0, 1.0);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);

		c.gridwidth = 1;
		c.weightx = 0.0;
		controls.add(new JLabel("T min: "));

		c.weightx = 1.0;
		controls.add(new JSpinner(tMin), c);

		c.weightx = 0.0;
		controls.add(new JLabel("T max: "));

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		controls.add(new JSpinner(tMax), c);

		tMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = tMin.getNumber().doubleValue();
//				if (tMax.getNumber().doubleValue() <= value)
//					tMax.setValue(value + 1);
				((ParametricPlotter)plot).setTMin(value);
			}
		});
		tMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = tMax.getNumber().doubleValue();
				// if (tMin.getNumber().doubleValue() >= value)
				// tMin.setValue(value - 1);
				((ParametricPlotter)plot).setTMax(value);
			}
		});
		tMin.setValue(0.0);
		((ParametricPlotter)plot).setTMin(0.0);
		tMax.setValue(7.0);
		((ParametricPlotter)plot).setTMax(7.0);
		return controls;
	}


	public void addField() {
//		addField("x(t) = ", "Sin[2*t]");
//		addField("y(t) = ", "Cos[3*t]");
	}
}
