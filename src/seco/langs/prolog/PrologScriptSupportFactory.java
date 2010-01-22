package seco.langs.prolog;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class PrologScriptSupportFactory extends ScriptSupportFactory
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
    public String getEngineName()
    {
        return "prolog";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new PrologScriptSupport(this, el);
    }
}
