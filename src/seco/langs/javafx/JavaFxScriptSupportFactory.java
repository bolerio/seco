package seco.langs.javafx;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.Element;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JavaFxScriptSupportFactory extends ScriptSupportFactory
{
    private static List<Mode> modes = new LinkedList<Mode>();
    static
    {
        modes.add(new Mode("javafx", "/modes/javafx.xml"));
    }

    @Override
    public String getModeName()
    {
        return "javafx";
    }

    @Override
    public List<Mode> getModes()
    {
        return modes;
    }

    @Override
    public String getEngineName()
    {
        return "javafx";
    }
    
    public ScriptSupport createScriptSupport(Element el)
    {
        return new JavaFxScriptSupport(this, el);
   }
}
