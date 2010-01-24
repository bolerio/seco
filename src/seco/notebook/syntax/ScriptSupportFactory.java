package seco.notebook.syntax;

import java.util.List;

import javax.swing.text.Element;

public abstract class ScriptSupportFactory
{
    //script engine name
    public abstract String getEngineName();
    
    //list of Modes used for text highlighting
    public abstract List<Mode> getModes();

    //name of the main mode
    public abstract String getModeName();
    
    public abstract ScriptSupport createScriptSupport(Element el);
   
}
