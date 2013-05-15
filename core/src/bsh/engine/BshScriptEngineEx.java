package bsh.engine;

import javax.script.ScriptException;

import org.hypergraphdb.util.HGUtils;

import seco.ThisNiche;

import bsh.Interpreter;

public class BshScriptEngineEx extends BshScriptEngine
{

    private bsh.Interpreter interpreter;
    
    protected Interpreter getInterpreter()
    {
        if ( interpreter == null ) {
            this.interpreter = new bsh.Interpreter();
            interpreter.setNameSpace(null); // should always be set by context
            loadImports();
        }        
        return interpreter;
    }
    
    //maybe we could load all HG and SECO packages automatically 
    protected void loadImports()
    {
        try{
            importPackage("org.hypergraphdb");
            importPackage("org.hypergraphdb.type");
            
            importPackage("seco");
            importPackage("seco.actions");
            importPackage("seco.gui");
            importPackage("seco.notebook");
            importPackage("seco.things");
            importPackage("seco.util");
        }catch(Exception e)
        {
            System.err.println("loadImports: " + e);
        }
    }
    
    public void importPackage(String pack) throws ScriptException
    {
        eval("import " + pack + ".*;");
    }
}
