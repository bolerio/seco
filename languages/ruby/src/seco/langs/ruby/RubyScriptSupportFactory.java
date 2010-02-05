package seco.langs.ruby;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class RubyScriptSupportFactory extends ScriptSupportFactory
{
    public RubyScriptSupportFactory()
    {
        addMode("ruby", new Mode("ruby", "/modes/ruby.xml",this));
    }

    public String getEngineName()
    {
        return "jruby";
    }

    public ScriptSupport createScriptSupport(Element el)
    {
        return new RubyScriptSupport(this, el);
    }
}