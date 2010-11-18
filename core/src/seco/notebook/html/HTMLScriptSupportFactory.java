package seco.notebook.html;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class HTMLScriptSupportFactory extends ScriptSupportFactory
{
    public HTMLScriptSupportFactory()
    {
        addMode("html", new Mode("html","/modes/html.xml",this));
        addMode("css", new Mode("css","/modes/css.xml",this));
        addMode("javascript", new Mode("javascript","/modes/javascript.xml",this));
    }
    
    public String getEngineName()
    {
        return "html";
    }
  
    public ScriptSupport createScriptSupport(Element el)
    {
        ScriptSupport sup = new HTMLScriptSupport(this, el);
        return sup;
    }
}