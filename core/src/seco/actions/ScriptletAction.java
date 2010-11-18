package seco.actions;

import java.awt.event.ActionEvent;

import javax.script.ScriptException;
import javax.swing.AbstractAction;

import seco.ThisNiche;
import seco.rtenv.EvaluationContext;
import seco.things.CellUtils;
import seco.things.Scriptlet;


public class ScriptletAction extends AbstractAction
{
    /*
     * Through this name the scriptlet action can directly access the ActionEvent
     * that was passed during the call to actionPerformed(ActionEvent) method 
     */
    public static final String ACTION_EVENT_VAR_NAME = "ACTION_EVENT_VAR_NAME";
    
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
        evalContext.getRuntimeContext().getBindings().put(ACTION_EVENT_VAR_NAME, 
                e);
        try{
        Object o = evalContext.eval(scriptlet.getLanguage(), scriptlet.getCode());
        }
        catch (ScriptException ex)
        {
            //StringWriter w = new StringWriter();
            //PrintWriter writer = new PrintWriter(w);
            //ex.printStackTrace(writer);
            ex.printStackTrace();
        }finally
        {
            evalContext.getRuntimeContext().getBindings().remove(ACTION_EVENT_VAR_NAME);
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

    @Override
    public String toString()
    {
        return "ScriptletAction: " + scriptlet.getCode();
    }
    
    

}
