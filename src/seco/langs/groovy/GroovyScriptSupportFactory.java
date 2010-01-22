package seco.langs.groovy;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class GroovyScriptSupportFactory extends ScriptSupportFactory
{
    private List<Mode> modes;
    public GroovyScriptSupportFactory()
    {
        modes = new LinkedList<Mode>();
        modes.add(new Mode("groovy", "/modes/groovy.xml"));
    }

    @Override
    public String getModeName()
    {
        return "groovy";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }

    @Override
    public String getEngineName()
    {
        return "groovy";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new GroovyScriptSupport(this, el);
    }

}
