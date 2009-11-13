package bsh.engine;

import javax.script.ScriptEngine;

import bsh.engine.BshScriptEngine;
import bsh.engine.BshScriptEngineFactory;

public class BshScriptEngineFactoryEx extends BshScriptEngineFactory
{
    public ScriptEngine getScriptEngine() 
    {
        return new BshScriptEngineEx();
    }
}
