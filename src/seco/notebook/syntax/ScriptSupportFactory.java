package seco.notebook.syntax;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Element;

/**
 * 
 * <p>
 * A <code>ScriptSupportFactory</code> contains Seco instance-wide common information
 * for edit support of a particular scripting language.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public abstract class ScriptSupportFactory
{
    private final Map<String, Mode> modes = new HashMap<String, Mode>();
    
    protected void addMode(String name, Mode mode)
    {
        modes.put(name, mode);
    }
    
    /**
     * Return the name of the scripting engine (e.g. "beanshell").
     */
    public abstract String getEngineName();
    
    /**
     * Return the list of supported edit modes within that scripting language.
     * Multiple edit modes may be supported if the language has several
     * different lexical modes (e.g. HTML has XML, CSS and JavaScript). 
     */
    public Iterable<Mode> getModes()
    {
        return modes.values();
    }

    /**
     * Return a supported edit mode by its name.
     */
    public Mode getMode(String name)
    {
        return modes.get(name);
    }
    
    /**
     * Return the default edit mode. If multiple edit modes are supported, one
     * is deemed the default one. 
     */
    public Mode getDefaultMode()
    {
        return getMode(getEngineName());
    }
    
    /**
     * Create an editing support instance for a given text element. The editing
     * support ({@ScriptSupport}) will hold run-time information based on the text of
     * the <code>Element</code> parameter.
     * @param el
     * @return
     */
    public abstract ScriptSupport createScriptSupport(Element el);   
}
