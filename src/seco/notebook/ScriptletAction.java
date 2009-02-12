package seco.notebook;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptException;
import javax.swing.AbstractAction;

import seco.ThisNiche;
import seco.rtenv.EvaluationContext;
import seco.things.CellUtils;
import seco.things.Scriptlet;



public class ScriptletAction extends AbstractAction
{
    private Scriptlet scriptlet;
    public ScriptletAction()
    {
    }
    
    public ScriptletAction(Scriptlet scriptlet)
    {
       this.scriptlet = scriptlet;
    }
    
    public ScriptletAction(String language, String code)
    {
       this.scriptlet = new Scriptlet(language, code);
    }
    public ScriptletAction(String code)
    {
        this(CellUtils.defaultEngineName, code);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        EvaluationContext evalContext = ThisNiche.getTopContext();
        if(evalContext == null)
           evalContext = ThisNiche.getEvaluationContext(ThisNiche.TOP_CONTEXT_HANDLE);
        try{
        Object o = evalContext.eval(scriptlet.getLanguage(), scriptlet.getCode());
        }
        catch (ScriptException ex)
        {
            //StringWriter w = new StringWriter();
            //PrintWriter writer = new PrintWriter(w);
            //ex.printStackTrace(writer);
            ex.printStackTrace();
        }
    }

    public Scriptlet getScriptlet()
    {
        return scriptlet;
    }

    public void setScriptlet(Scriptlet scriptlet)
    {
        this.scriptlet = scriptlet;
    }

}
