package bsh;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class BshScriptSupportFactory extends ScriptSupportFactory
{
    static final String MODE_NAME = "java";
    static final String ENGINE_NAME = "beanshell";
    
    
    private static List<Mode> modes = new LinkedList<Mode>();
    static{
         modes.add(new Mode(MODE_NAME,"/modes/java.xml"));
         modes.add(new Mode("xml","/modes/xml.xml"));
    }
    
    public String getEngineName()
    {
        return ENGINE_NAME;
    }
    
    public String getModeName()
    {
        return MODE_NAME;
    }
    
    public List<Mode> getModes()
    {
        return modes;
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new BshScriptSupport(this, el);
    }
}
