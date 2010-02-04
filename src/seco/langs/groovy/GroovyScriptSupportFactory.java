package seco.langs.groovy;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class GroovyScriptSupportFactory extends ScriptSupportFactory
{
    public GroovyScriptSupportFactory()
    {
        addMode("groovy", new Mode("groovy", "/modes/groovy.xml",this));
    }

    public String getEngineName()
    {
        return "groovy";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new GroovyScriptSupport(this, el);
    }
}
