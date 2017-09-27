package seco.langs.scala.jsr223;

import javax.script.*;

public class Play
{

	public static void main(String[] args)
	{
		try
		{
			ScriptEngine e = new ScriptEngineManager().getEngineByName("scala");
			System.out.println(new ScriptEngineManager().getEngineFactories());
			System.out.println(e.eval("2+2"));
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}

}
