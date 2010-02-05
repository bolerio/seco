package seco.langs.jscheme;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JSchemeScriptSupportFactory extends ScriptSupportFactory
{
    public JSchemeScriptSupportFactory()
    {
        addMode("schme", new Mode("scheme","/modes/scheme.xml",this));
    }
    
    public String getEngineName()
    {
        return "jscheme";
    }

    public ScriptSupport createScriptSupport(Element el)
    {
        return new JSchemeScriptSupport(this, el);
    }
}