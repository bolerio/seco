package seco.langs.groovy;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import org.mozilla.javascript.UniqueTag;

import seco.langs.javascript.jsr.ExternalScriptable;
import seco.langs.javascript.jsr.RhinoScriptEngine;
import seco.notebook.AppConfig;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.completion.NBParser.ParserRunnable;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;
import sun.org.mozilla.javascript.internal.Context;

public class GroovyScriptSupport extends ScriptSupport
{
    
    public GroovyScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    private static CompletionProvider[] providers = 
              new CompletionProvider[]{new GroovyCompletionProvider()};
    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return providers;
    }

   private GroovyScriptParser parser = null;

    @Override
    public NBParser getParser()
    {
        if (parser == null)
             parser = new GroovyScriptParser(this);
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
    
    static class GroovyScriptParser extends NBParser
    {
        RhinoScriptEngine engine;

        public GroovyScriptParser(final ScriptSupport support)
        {
            super(support);
            engine = (RhinoScriptEngine) support.getDocument()
                    .getEvaluationContext().getEngine("javascript");
        }
    
         
        @Override
        public ParserRunnable getParserRunnable()
        {
            return null;
        }
        
        public Object resolveVar(String s, int offset)
        {
            if (s == null || s.length() == 0) return null;
           // if (getRootNode() == null) return null;

            try
            {
                Object o = null;//scope.get0(s, scope);
                if (o != null && !(o instanceof UniqueTag)) return o;
                if (s.indexOf("(") < 0)
                {
                    o = support.getDocument().getEvaluationContext().eval(
                            support.getFactory().getEngineName(), s);
                    if (o != null) return o;
                }else
                    resolveMethod(s, offset);
            }
            catch (Exception err)
            {
                err.printStackTrace();
            }
            return null;
        }
        
        public Object resolveMethod(String s, int offset)
        {
            return null;
        }
        
    }

}
