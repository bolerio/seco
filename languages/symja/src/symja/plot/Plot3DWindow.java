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

public class Plot3DWindow extends AbstractPlot3DWindow {
	// protected SpinnerNumberModel tMin;
	// protected SpinnerNumberModel tMax;

	public Plot3DWindow(JFrame parent) {
		super(parent);
	}

	protected AbstractPlotter3D createPlot() {
		return Plot3DPlotter.getPlot3DPlotter();
	}

	protected JPanel createMinMaxControls() {
		JPanel controls = super.createMinMaxControls();
		// tMin = new SpinnerNumberModel(0.0, -999.0, 999.0, 1.0);
		// tMax = new SpinnerNumberModel(Math.PI*2.0, -999.0, 999.0, 1.0);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);

//		c.gridwidth = 1;
//		c.weightx = 0.0;
//		controls.add(new JLabel("T min: "));

//		c.weightx = 1.0;
//		controls.add(new JSpinner(tMin), c);

//		c.weightx = 0.0;
//		controls.add(new JLabel("T max: "));

//		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.weightx = 1.0;
//		controls.add(new JSpinner(tMax), c);

		// tMin.addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// double value = tMin.getNumber().doubleValue();
		// if (tMax.getNumber().doubleValue() <= value)
		// tMax.setValue(value + 1);
		// ((Plot3DPlotter)plot).setTMin(value);
		// }
		// });
		// tMax.addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// double value = tMax.getNumber().doubleValue();
		// if (tMin.getNumber().doubleValue() >= value)
		// tMin.setValue(value - 1);
		// ((Plot3DPlotter)plot).setTMax(value);
		// }
		// });

		return controls;
	}

	public void addField() {
//		addField("f1(x,y) = ", StringSurfaceModel.F1_DEFAULT_FUNCTION);
//		addField("f2/x,y) = ", StringSurfaceModel.F2_DEFAULT_FUNCTION);
	}
}
