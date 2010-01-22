package seco.langs.jscheme;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JSchemeScriptSupportFactory extends ScriptSupportFactory
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static{
        modes.add(new Mode("scheme","/modes/scheme.xml"));
    }
    
    @Override
    public String getEngineName()
    {
        return "jscheme";
    }

    @Override
    public String getModeName()
    {
        return "scheme";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new JSchemeScriptSupport(this, el);
    }

}
