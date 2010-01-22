package seco.notebook;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class HTMLScriptSupportFactory extends ScriptSupportFactory
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static{
        modes.add(new Mode("html","/modes/html.xml"));
        modes.add(new Mode("css","/modes/css.xml"));
        modes.add(new Mode("javascript","/modes/javascript.xml"));
    }
    
    @Override
    public String getEngineName()
    {
        return "html";
    }

    @Override
    public String getModeName()
    {
        return "html";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        ScriptSupport sup = new HTMLScriptSupport(this, el);
        return sup;
    }

}
