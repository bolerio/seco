package seco.langs.groovy;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;

public class GroovyScriptSupport extends ScriptSupport
{
    
    public GroovyScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    private static CompletionProvider[] providers = null;
    //new CompletionProvider[]{new GroovyCompletionProvider()};
    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return providers;
    }

//    private JavaScriptParser parser = null;

    @Override
    public NBParser getParser()
    {
//        if (parser == null)
//        {
//            parser = new JavaScriptParser(this);
//        }
//        return parser;
        return null;
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
