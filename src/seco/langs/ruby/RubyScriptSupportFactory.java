package seco.langs.ruby;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class RubyScriptSupportFactory extends ScriptSupportFactory
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static
    {
        modes.add(new Mode("ruby", "/modes/ruby.xml"));
    }

    @Override
    public String getEngineName()
    {
        return "jruby";
    }

    @Override
    public String getModeName()
    {
        return "ruby";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new RubyScriptSupport(this, el);
    }

}
