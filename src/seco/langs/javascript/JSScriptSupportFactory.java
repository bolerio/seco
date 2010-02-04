package seco.langs.javascript;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JSScriptSupportFactory extends ScriptSupportFactory
{
    public JSScriptSupportFactory()
    {
        addMode("javascript", new Mode("javascript", "/modes/javascript.xml",this));
    }
    
    public String getEngineName()
    {
        return "javascript";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new JSScriptSupport(this, el);
    }
}