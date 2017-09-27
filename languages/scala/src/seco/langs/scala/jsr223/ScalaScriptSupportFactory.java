package seco.langs.scala.jsr223;

import javax.swing.text.Element;

import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class ScalaScriptSupportFactory extends ScriptSupportFactory
{
	private static final String SCALA_NAME = "scala";

	public ScalaScriptSupportFactory()
	{
		addMode(SCALA_NAME, new Mode(SCALA_NAME,"/modes/scala.xml",this));		
	}
	
	@Override
	public String getEngineName()
	{
		return SCALA_NAME;
	}

	@Override
	public ScriptSupport createScriptSupport(Element el)
	{
		return new ScalaScriptSupport(this, el);
    }

}
