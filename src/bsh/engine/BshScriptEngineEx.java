package bsh.engine;

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
    
    //maybe we could load all HGm and SECO packages automatically 
    protected void loadImports()
    {
        try{
            eval("org.hypergraphdb.*");
            eval("org.hypergraphdb.type.*");
            
            eval("import seco.*;");
            eval("import seco.things.*;");
            eval("import seco.gui*;");
            eval("import seco.notebook.*;");
        }catch(Exception e)
        {
            System.err.println("loadImports: " + e);
        }
    }
}
