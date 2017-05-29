package symja.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.TeXUtilities;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.reflection.system.Names;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import symja.plot.AbstractPlotter2D;
import symja.plot.ParametricPlotWindow;
import symja.plot.Plot3DWindow;
import symja.plot.PlotWindow;
import symja.plot.Plotter;
import symja.plot.StringSurfaceModel;

public class MathScriptEngine extends AbstractScriptEngine {
	// public final static String RETURN_OBJECT = "RETURN_OBJECT";
	private static final int FONT_SIZE_TEX = 24;

	// private Util fUtility;
	private EvalEngine fEngine;

	// static {
	// run the static groovy code for the MathEclipse domain specific language
	// DSL groovyDSL = new DSL();
	// }
	private static class EvalCallable implements Callable<IExpr> {
		private final EvalEngine fEngine;
		private final IExpr fExpr;

		public EvalCallable(IExpr expr, EvalEngine engine) {
			fExpr = expr;
			fEngine = engine;
		}

		@Override
		public IExpr call() throws Exception {
			// TODO Auto-generated method stub
			return fEngine.evaluate(fExpr);
		}

	}

	public MathScriptEngine() {
		// get the thread local evaluation engine
		fEngine = new EvalEngine(true);
		fEngine.setOutListDisabled(false, 1000);
		// engine.setIterationLimit(10);
		// fUtility = new Util(fEngine, false, false);
		// Config.PARSER_USE_LOWERCASE_SYMBOLS = false;
		F.initSymbols(null, null, true); // console.getDefaultSystemRulesFilename(),
											// null, false);
		// F.Get.setEvaluator(new
		// de.cbarbat.mathematica.symja.function.GetMMA());
	}

	public Bindings createBindings() {
		return null;
	}

	public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
		final BufferedReader f = new BufferedReader(reader);
		final StringBuffer buff = new StringBuffer(1024);
		String line;
		try {
			while ((line = f.readLine()) != null) {
				buff.append(line);
				buff.append('\n');
			}
			return eval(buff.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load the documentation fro ressources folder if available ad print to
	 * output.
	 * 
	 * @param symbolName
	 */
	private static void printDocumentation(String symbolName, StringBuilder buf) {
		// read markdown file
		String fileName = symbolName + ".md";

		// Get file from resources folder
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		try {
			InputStream is = classloader.getResourceAsStream(fileName);
			if (is != null) {
				final BufferedReader f = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				boolean emptyLine = false;
				while ((line = f.readLine()) != null) {
					if (line.startsWith("```")) {
						continue;
					}
					if (line.trim().length() == 0) {
						if (emptyLine) {
							continue;
						}
						emptyLine = true;
					} else {
						emptyLine = false;
					}
					buf.append(line);
					buf.append("\n");
				}
				f.close();
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Object eval(final String script, final ScriptContext context) throws ScriptException {
		// final ArrayList<ISymbol> list = new ArrayList<ISymbol>();
		try {
			String trimmedInput = script.trim();
			if (trimmedInput.length() > 1 && trimmedInput.charAt(0) == '?') {
				String name = trimmedInput.substring(1);
				StringBuilder buf = new StringBuilder(256);
				IAST list = Names.getNamesByPrefix(name);
				for (int i = 1; i < list.size(); i++) {
					buf.append(list.get(i).toString());
					if (i != list.size() - 1) {
						buf.append(", ");
					}
				}
				System.out.println();
				if (list.size() == 2) {
					printDocumentation(list.get(1).toString(), buf);
				} else if (list.size() == 1
						&& (name.equals("D") || name.equals("E") || name.equals("I") || name.equals("N"))) {
					printDocumentation(name, buf);
				}
				return buf.toString();
			}
			// first assign the EvalEngine to the current thread:

			// evaluate an expression
			fEngine.reset();
			IExpr result = fEngine.parse(trimmedInput);
			// IExpr result = Util.convert(fEngine, trimmedInput);
			if (result == null) {
				return "ParserError: No valid expression parsed!";
			}
			if (result.isAST()) {
				Object obj = handlePlotCommands(result);
				if (obj != null) {
					return obj;
				}
			}

			return timeConstrainedEval(result);

		} catch (final Exception e) {
			if (Config.SHOW_STACKTRACE) {
				e.printStackTrace();
			}
			return e.getMessage();
		} finally {
			// if (list.size() > 0) {
			// for (int i = 0; i < list.size(); i++) {
			// list.get(i).popLocalVariable();
			// }
			// }
		}

	}

	private Object timeConstrainedEval(IExpr expr) {
		TimeLimiter timeLimiter = new SimpleTimeLimiter();
		Callable<IExpr> work = new EvalCallable(expr, fEngine);

		try {
			expr = timeLimiter.callWithTimeout(work, 30, TimeUnit.SECONDS, true);
			if (expr != null) {
				fEngine.addOut(expr);
				if (expr.equals(F.Null)) {
					return "";
				}

				// JComponent component = createTeXFormula(result);
				// if (component != null) {
				// return component;
				// }

				final StringWriter buf = new StringWriter();
				OutputFormFactory.get(false).convert(buf, expr);
				// print the result in the console
				return buf.toString();
			}
		} catch (java.util.concurrent.TimeoutException e) {
			return "Aborted after 30 seconds";
		} catch (com.google.common.util.concurrent.UncheckedTimeoutException e) {
			return "Aborted after 30 seconds";
		} catch (Exception e) {
			if (Config.SHOW_STACKTRACE) {
				e.printStackTrace();
			}
			return e.getMessage();
		}
		return "";
	}

	private Object handlePlotCommands(IExpr result) {
		IAST ast = (IAST) result;
		if (result.isAST("Plot")) {
			if (result.isAST0()) {
				PlotWindow window = new PlotWindow(null);
				if (ast.size() >= 2 && !ast.arg1().isList()) {
					IExpr temp = fEngine.evaluate(ast.arg1());
					window.addField("y(x) = ", temp.toString());
				} else {
					// default example
					window.addField("y(x) = ", Config.PARSER_USE_LOWERCASE_SYMBOLS ? "Sin(x)^3" : "Sin[x]^3");
				}
				window.pack();
				window.setVisible(true);
				window.doGraph();
				return "";
			}
			AbstractPlotter2D plot = Plotter.getPlotter();
			java.util.List<String> l = new java.util.ArrayList<String>();
			if (ast.size() >= 2 && !ast.arg1().isList()) {
				IExpr temp = fEngine.evaluate(ast.arg1());
				l.add(temp.toString());
			} else {
				// default example
				l.add(Config.PARSER_USE_LOWERCASE_SYMBOLS ? "Sin(x)^3" : "Sin[x]^3");
			}
			plot.setXMin(-10.0);
			plot.setXMax(10.0);
			plot.setYMin(-2.0);
			plot.setYMax(2.0);
			plot.setFunctions(l);

			return plot;

		}
		if (result.isAST("ParametricPlot")) {
			ParametricPlotWindow window = new ParametricPlotWindow(null);
			if (ast.size() >= 2 && ast.arg1().isList() && ((IAST) ast.arg1()).size() >= 3) {
				IAST list = (IAST) ast.arg1();
				IExpr temp = fEngine.evaluate(list.arg1());
				window.addField("x(t) = ", temp.toString());
				temp = fEngine.evaluate(list.arg2());
				window.addField("y(t) = ", temp.toString());
			} else {
				// default example
				window.addField("x(t) = ", Config.PARSER_USE_LOWERCASE_SYMBOLS ? "Sin(2*t)" : "Sin[2*t]");
				window.addField("y(t) = ", Config.PARSER_USE_LOWERCASE_SYMBOLS ? "Cos(3*t)" : "Cos[3*t]");
			}
			window.pack();
			window.setVisible(true);
			window.doGraph();
			return "";
		}
		if (result.isAST("Plot3D")) {
			Plot3DWindow window = new Plot3DWindow(null);

			if (ast.size() >= 2 && !ast.arg1().isList()) {
				IExpr temp = fEngine.evaluate(ast.arg1());
				window.addField("f1(x,y) = ", temp.toString());
				window.addField("f2(x,y) = ", "");
			} else {
				// default example
				window.addField("f1(x,y) = ", StringSurfaceModel.F1_DEFAULT_FUNCTION);
				window.addField("f2(x,y) = ", StringSurfaceModel.F2_DEFAULT_FUNCTION);
			}
			window.pack();
			window.setVisible(true);
			window.doGraph();
			return "";
		}
		return null;
	}

	private JComponent createTeXFormula(IExpr result) {
		final TeXUtilities texUtil = new TeXUtilities(fEngine, false);
		try {
			if (result != null) {
				final StringWriter buf1 = new StringWriter();
				texUtil.toTeX(result, buf1);
				TeXFormula formula = new TeXFormula(buf1.toString());
				TeXIcon ticon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, FONT_SIZE_TEX,
						TeXConstants.UNIT_PIXEL, 80, TeXConstants.ALIGN_LEFT);
				return new JLabel(ticon);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ScriptEngineFactory getFactory() {
		return new MathScriptEngineFactory();
	}
}
