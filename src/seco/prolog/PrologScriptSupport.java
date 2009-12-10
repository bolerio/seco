package seco.prolog;

import java.util.LinkedList;
import java.util.List;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;

public class PrologScriptSupport extends ScriptSupport 
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static{
        modes.add(new Mode("prolog","/modes/prolog.xml"));
    }

    
    @Override
    public String getModeName()
    {
       return "prolog";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }

    @Override
    public String getScriptEngineName()
    {
        return "prolog";
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
