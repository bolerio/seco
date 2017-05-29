package symja.plot;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class AbstractPlot3DWindow extends JDialog {
 
	protected SpinnerNumberModel xMin = new SpinnerNumberModel(-3.0, -999.0, 999.0, 1.0);
	protected SpinnerNumberModel xMax = new SpinnerNumberModel(3.0, -999.0, 999.0, 1.0);
	protected SpinnerNumberModel yMin = new SpinnerNumberModel(-3.0, -999.0, 999.0, 1.0);
	protected SpinnerNumberModel yMax = new SpinnerNumberModel(3.0, -999.0, 999.0, 1.0);

	protected AbstractPlotter3D plot;

	protected Container equations;
	protected Container controls;
	protected Component fieldScroller;

	public AbstractPlot3DWindow(Frame parent) {
		super(parent, "Plot 3D");

		JPanel controls = createControls();
		plot = createPlot();
		xMin.setValue(-3.0);
		xMax.setValue(3.0);
		yMin.setValue(-3.0);
		yMax.setValue(3.0);
		plot.setXMin(-3.0);
		plot.setXMax(3.0);
		plot.setYMin(-3.0);
		plot.setYMax(3.0);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, plot, controls);
		split.setResizeWeight(1);

		getContentPane().add(split);
		// doGraph();
	}

	protected abstract AbstractPlotter3D createPlot();

	protected JComponent createFields() {
		JComponent container = new JPanel();
		container.setLayout(new GridBagLayout());
		equations = new JPanel();
		equations.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		addField();

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(2, 2, 2, 2);

		container.add(equations, gbc);
		gbc.fill = GridBagConstraints.NONE;
		// JButton add = new JButton("Add Function");
		// add.setEnabled(true);
		// container.add(add, gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		container.add(Box.createGlue(), gbc);

		// add.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// addField();
		// fieldScroller.validate();
		// }
		// });

		return container;
	}

	public abstract void addField();

	public void addField(String label, String value) {
		GridBagConstraints gbc = new GridBagConstraints();
		JTextField field = new JTextField(value);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
		equations.add(new JLabel(label), gbc);

		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		equations.add(field, gbc);
		// field.addActionListener(new GraphListener());
	}

	protected JPanel createMinMaxControls() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);

		c.gridwidth = 1;
		c.weightx = 0.0;
		controls.add(new JLabel("X min: "), c);

		c.weightx = 1.0;
		controls.add(new JSpinner(xMin), c);

		c.weightx = 0.0;
		controls.add(new JLabel("X max: "), c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		controls.add(new JSpinner(xMax), c);

		c.gridwidth = 1;
		c.weightx = 0.0;
		controls.add(new JLabel("Y min: "), c);

		c.weightx = 1.0;
		controls.add(new JSpinner(yMin), c);

		c.weightx = 0.0;
		controls.add(new JLabel("Y max: "), c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		controls.add(new JSpinner(yMax), c);

		xMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = xMin.getNumber().doubleValue();
				if (xMax.getNumber().doubleValue() <= value)
					xMax.setValue(value + 1);
				plot.setXMin(value);
			}
		});
		xMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = xMax.getNumber().doubleValue();
				if (xMin.getNumber().doubleValue() >= value)
					xMin.setValue(value - 1);
				plot.setXMax(value);
			}
		});
		yMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = yMin.getNumber().doubleValue();
				if (yMax.getNumber().doubleValue() <= value)
					yMax.setValue(value + 1);
				plot.setYMin(value);
			}
		});
		yMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = yMax.getNumber().doubleValue();
				if (yMin.getNumber().doubleValue() >= value)
					yMin.setValue(value - 1);
				plot.setYMax(value);
			}
		});

		return controls;
	}

	protected JPanel createControls() {
		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.weighty = 0.0;
		controls.add(createMinMaxControls(), c);

		c.weightx = 1.0;
		c.weighty = 1.0;
		fieldScroller = new JScrollPane(createFields());
		controls.add(fieldScroller, c);

		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		JButton update = new JButton("Update");
		update.setEnabled(true);
		controls.add(update, c);

		update.addActionListener(new GraphListener());

		return controls;
	}

	public void doGraph() {
		java.util.List<String> l = new ArrayList<String>();
		Component c[] = equations.getComponents();

		for (int i = 0; i < c.length; ++i) {
			if (c[i] instanceof JTextField)
				l.add(((JTextField) c[i]).getText());
		}

		plot.setFunctions(l);
	}

	protected class GraphListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			doGraph();
		}
	}
}
