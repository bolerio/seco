package seco.langs.prolog;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class PrologScriptSupportFactory extends ScriptSupportFactory
{
    public PrologScriptSupportFactory()
    {
        addMode("prolog", new Mode("prolog","/modes/prolog.xml",this));
    }

    public String getEngineName()
    {
        return "prolog";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new PrologScriptSupport(this, el);
    }
}