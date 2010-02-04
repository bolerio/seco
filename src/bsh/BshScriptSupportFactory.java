package bsh;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class BshScriptSupportFactory extends ScriptSupportFactory
{
    static final String ENGINE_NAME = "beanshell";
          
    public BshScriptSupportFactory()    
    {
         addMode("java", new Mode("java","/modes/java.xml",this));
         addMode("xml", new Mode("xml","/modes/xml.xml",this));
    }
    
    public String getEngineName()
    {
        return ENGINE_NAME;
    }
    
    public Mode getDefaultMode()
    {
        return getMode("java");
    }
        
    public ScriptSupport createScriptSupport(Element el)
    {
        return new BshScriptSupport(this, el);
    }
}
