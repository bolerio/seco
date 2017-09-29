package seco.langs.scala.jsr223;

import javax.swing.text.Element;

import seco.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;

public class ScalaScriptSupport extends ScriptSupport
{
	private static CompletionProvider[] providers = new CompletionProvider[] { new ScalaCompletionProvider() };

	public ScalaScriptSupport(ScriptSupportFactory factory, Element el) {
		super(factory, el);
	}

	private static Formatter formatter;

	public Formatter getFormatter() {
		if (formatter == null) {
			formatter = new JavaFormatter((JavaFormatterOptions) AppConfig.getInstance()
					.getProperty(AppConfig.FORMATTER_OPTIONS, new JavaFormatterOptions()));
		}
		return formatter;
	}

	public void resetFormatter() {
		formatter = null;
	}

	public NBParser getParser() {
		// if(parser == null)
		// parser = new BshAst(this);
		// return parser;
		return null;
	}

	public CompletionProvider[] getCompletionProviders() {
		return providers;
	}

}
