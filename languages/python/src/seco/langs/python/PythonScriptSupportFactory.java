package seco.langs.python;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class PythonScriptSupportFactory extends ScriptSupportFactory
{
    public PythonScriptSupportFactory()
    {
        addMode("python", new Mode("python","/modes/python.xml",this));
    }
    
    public String getEngineName()
    {
        return "python";
    }
    
    public Mode getDefaultMode()
    {
        return getMode("python");
    }

    public ScriptSupport createScriptSupport(Element el)
    {
        return new PythonScriptSupport(this, el);
    }
}