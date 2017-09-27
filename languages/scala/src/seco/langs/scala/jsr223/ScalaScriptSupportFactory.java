package seco.langs.scala.jsr223;

import javax.swing.text.Element;

import scala.tools.nsc.interpreter.Scripted;
import seco.notebook.syntax.Mode;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;

public class ScalaScriptSupportFactory extends ScriptSupportFactory
{

	public ScalaScriptSupportFactory()
	{
		addMode("scala", new Mode("scala","/modes/scala.xml",this));		
	}
	
	@Override
	public String getEngineName()
	{
		return Scripted.ENGINE;
	}

	@Override
	public ScriptSupport createScriptSupport(Element el)
	{
		return null;
	}

}
