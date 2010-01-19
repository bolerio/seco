package seco.notebook.javascript;

import java.util.LinkedList;
import java.util.List;

import seco.notebook.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;

public class JSScriptSupport extends ScriptSupport
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static
    {
        modes.add(new Mode("javascript", "/modes/javascript.xml"));
    }
    
    private static CompletionProvider[] providers = 
        new CompletionProvider[]{
              new JSCompletionProvider()};


    @Override
    public String getModeName()
    {
        return "javascript";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }

    @Override
    public String getScriptEngineName()
    {
        return "javascript";
    }

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
