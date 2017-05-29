package symja.plot;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.generic.BinaryNumerical;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.parser.client.SyntaxError;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;

//import de.cbarbat.mathematica.symja.Util;

public class StringSurfaceModel extends AbstractSurfaceModel implements Runnable {
	public static final String F1_DEFAULT_FUNCTION = Config.PARSER_USE_LOWERCASE_SYMBOLS
			? "If(x*x+y*y==0,1,Sin(x*x+y*y)/(x*x+y*y))" : "If[x*x+y*y==0,1,Sin[x*x+y*y]/(x*x+y*y)]";
	public static final String F2_DEFAULT_FUNCTION = Config.PARSER_USE_LOWERCASE_SYMBOLS ? "Sin(x*y)" : "Sin[x*y]";

	private final EvalEngine engine = new EvalEngine(true);
	private ISymbol xVar;
	private ISymbol yVar;
	private IExpr f1Node;
	private IExpr f2Node;
	private String f1Function = F1_DEFAULT_FUNCTION;
	private String f2Function = F2_DEFAULT_FUNCTION;
	private BinaryNumerical f1BN;
	private BinaryNumerical f2BN;

	StringSurfaceModel() {
		super();

		setPlotFunction2(false);

		setCalcDivisions(100);
		setDispDivisions(30);
		setContourLines(10);

		setXMin(-3);
		setXMax(3);
		setYMin(-3);
		setYMax(3);

		setBoxed(false);
		setDisplayXY(false);
		setExpectDelay(false);
		setAutoScaleZ(true);
		setDisplayZ(false);
		setMesh(false);
		setPlotType(PlotType.SURFACE);
		// setPlotType(PlotType.WIREFRAME);
		// setPlotType(PlotType.CONTOUR);
		// setPlotType(PlotType.DENSITY);

		setPlotColor(PlotColor.SPECTRUM);
		// setPlotColor(PlotColor.DUALSHADE);
		// setPlotColor(PlotColor.FOG);
		// setPlotColor(PlotColor.OPAQUE);

		// xVar = (ISymbol) Util.convert(engine, "x");
		// yVar = (ISymbol) Util.convert(engine, "y");
		// f1Node = Util.convert(engine, f1Function);
		// f2Node = Util.convert(engine, f2Function);
		xVar = (ISymbol) engine.parse("x");
		yVar = (ISymbol) engine.parse("y");
		f1Node = engine.parse(f1Function);
		f2Node = engine.parse(f2Function);
		f1BN = new BinaryNumerical(f1Node, xVar, yVar);
		f2BN = new BinaryNumerical(f2Node, xVar, yVar);
	}

	/**
	 * @param f1Function
	 *            the f1 function to set
	 */
	public void setF1Function(String f1Function) {
		try {
			// this.f1Node = Util.convert(engine, f1Function);
			this.f1Node = engine.parse(f1Function);
			this.f1Function = f1Function;
		} catch (SyntaxError se) {
			// TODO show error
		}
	}

	/**
	 * @param f2Function
	 *            the f2 function to set
	 */
	public void setF2Function(String f2Function) {
		try {
			// this.f2Node = Util.convert(engine, f2Function);
			this.f2Node = engine.parse(f2Function);
			this.f2Function = f2Function;
		} catch (SyntaxError se) {
			// TODO show error
		}
	}

	public float f1(float x, float y) {
		return (float) f1BN.value(x, y);
	}

	public float f2(float x, float y) {
		return (float) f2BN.value(x, y);
	}

	@Override
	public void run() {
		Plotter p = newPlotter(getCalcDivisions());
		int im = p.getWidth();
		int jm = p.getHeight();
		for (int i = 0; i < im; i++)
			for (int j = 0; j < jm; j++) {
				float x, y;
				x = p.getX(i);
				y = p.getY(j);
				p.setValue(i, j, f1(x, y), f2(x, y));
			}
	}
}
