package seco.langs.javascript;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JSScriptSupportFactory extends ScriptSupportFactory
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static
    {
        modes.add(new Mode("javascript", "/modes/javascript.xml"));
    }
    
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
    public String getEngineName()
    {
        return "javascript";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new JSScriptSupport(this, el);
    }
}
