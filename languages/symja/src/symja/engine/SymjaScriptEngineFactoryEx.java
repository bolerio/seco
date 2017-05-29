package symja.engine;

import javax.script.ScriptEngine;

import bsh.engine.BshScriptEngineFactory;

public class SymjaScriptEngineFactoryEx extends BshScriptEngineFactory
{
    public ScriptEngine getScriptEngine() 
    {
        return new SymjaScriptEngineEx();
    }
}
