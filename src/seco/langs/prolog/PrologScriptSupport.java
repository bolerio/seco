package seco.langs.prolog;

import javax.swing.text.Element;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;

public class PrologScriptSupport extends ScriptSupport 
{
       
    public PrologScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return null;
    }
    
    @Override
    public NBParser getParser()
    {
        return null;
    }
 

}
