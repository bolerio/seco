package seco.langs.javafx;

import javax.swing.text.Element;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class JavaFxScriptSupportFactory extends ScriptSupportFactory
{
    public JavaFxScriptSupportFactory()
    {
        addMode("javafx", new Mode("javafx", "/modes/javafx.xml",this));
    }

    public String getEngineName()
    {
        return "javafx";
    }

    public ScriptSupport createScriptSupport(Element el)
    {
        return new JavaFxScriptSupport(this, el);
    }
}
