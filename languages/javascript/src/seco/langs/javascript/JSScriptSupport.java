package seco.langs.javascript;

import javax.swing.text.Element;

import seco.notebook.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;

public class JSScriptSupport extends ScriptSupport
{
    
    public JSScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    private static CompletionProvider[] providers = 
        new CompletionProvider[]{
              new JSCompletionProvider()};
    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return providers;
    }

    private JavaScriptParser parser = null;

    @Override
    public NBParser getParser()
    {
        if (parser == null)
        {
            parser = new JavaScriptParser(this);
        }
        return parser;
    }
    
    private static Formatter formatter;
    public Formatter getFormatter()
    {
        if(formatter == null){
            formatter = new JavaFormatter((JavaFormatterOptions)
                    AppConfig.getInstance().getProperty(
                            AppConfig.FORMATTER_OPTIONS, new JavaFormatterOptions()));
        }
        return formatter;
    }

}
