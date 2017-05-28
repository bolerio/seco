package seco.langs.groovy;

import groovy.lang.Closure;

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.net.URL;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.codehaus.groovy.runtime.MethodClosure;

import seco.langs.groovy.GroovyScriptSupport.GroovyScriptParser;
import seco.langs.groovy.jsr.GroovyScriptEngine;
import seco.langs.groovy.jsr.ShellLikeGroovyEngine;
import seco.notebook.NotebookDocument;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.AsyncCompletionQuery;
import seco.notebook.syntax.completion.AsyncCompletionTask;
import seco.notebook.syntax.completion.BaseAsyncCompletionQuery;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionDocumentation;
import seco.notebook.syntax.completion.CompletionItem;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.CompletionResultSet;
import seco.notebook.syntax.completion.CompletionTask;
import seco.notebook.syntax.completion.CompletionU;
import seco.notebook.syntax.completion.JavaDocManager;
import seco.notebook.syntax.java.JavaResultItem;
import seco.storage.ClassRepository;
import seco.storage.NamedInfo;
import seco.util.DocumentUtilities;

public class GroovyCompletionProvider implements CompletionProvider
{
    public int getAutoQueryTypes(JTextComponent component, String typedText)
    {
        if (".".equals(typedText)) return COMPLETION_QUERY_TYPE;
        if (" ".equals(typedText)) return TOOLTIP_QUERY_TYPE;
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent component)
    {
        int offset = component.getCaret().getDot();
        ScriptSupport sup = ((NotebookDocument) component.getDocument())
                .getScriptSupport(offset);
        if (sup.isCommentOrLiteral(offset - 1)) return null;
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(component.getCaret()
                    .getDot()), component);
        // else if (queryType == DOCUMENTATION_QUERY_TYPE) return new
        // AsyncCompletionTask(
        // new DocQuery(null), component);
        // else if (queryType == TOOLTIP_QUERY_TYPE)
        // return new AsyncCompletionTask(new ToolTipQuery(), component);
        return null;
    }

    static final class Query extends BaseAsyncCompletionQuery
    {
        GroovyScriptParser p;
        ScriptSupport sup;
        String prefix;
       
        public Query(int caretOffset)
        {
            super(caretOffset);
        }

        protected void query(CompletionResultSet resultSet,
                NotebookDocument doc, int offset)
        {
            sup = doc.getScriptSupport(offset);
            queryCaretOffset = offset;
            queryAnchorOffset = offset;
            p = (GroovyScriptParser) sup.getParser();
            prefix = sup.getCommandBeforePt(offset);
            if(p.parserResult == null) return;
            if(!complete(resultSet)) return;
            
            queryResult = resultSet;
            resultSet.finish();
        }

        private void add_metas(CompletionResultSet resultSet)
        {
            for(CompletionItem i: BuiltIns.getGroovyMetas())
            {
                i.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(i);
            }
        }
        
        private boolean complete(final CompletionResultSet resultSet)
        {
            ModuleNode root = p.parserResult.getRootElement();
            
            if(packageCompletion(resultSet)) return true;
            
            AstPath realPath = new AstPath(root, queryCaretOffset - 1, sup.getElement());
            ClassNodePair pair = resolve(realPath);
            if (pair == null) return false;
            //
            if(root.getScriptClassDummy().equals(pair.node))
            {
                populateThis(resultSet, p.engine);
                return true;
            }
            resultSet.setTitle(pair.node.getName());
            populateObject(resultSet, pair, prefix);

            return true;
        }
        
         private void populateObject(CompletionResultSet resultSet,
                ClassNodePair pair, String prefix)
        {

            Class<?> type = pair.node.isResolved() ? pair.node.getTypeClass() : null;
            
            System.out.println("populateObject: " + pair.node.getName() + ":" + type);
            if (type != null)
            {
                int mod = (pair.isStatic) ? Modifier.STATIC : Modifier.PRIVATE;
                CompletionU.populateClass(resultSet, type, mod, queryCaretOffset);
                add_metas(resultSet);
                return;
            }

            for (ClassNode inter : pair.node.getInterfaces())
            {
                System.out.println("interface: " + inter.getName());
                if(inter.getName().equals("groovy.lang.GroovyObject"))
                    add_metas(resultSet);
                // populateObject(resultSet, source, inter, prefix, anchor);
            }

            for (MethodNode m : pair.node.getMethods())
            {
                if(m.getName().indexOf('$') > -1 ||
                        m.getName().indexOf('<') > -1)
                    continue;
                ClassNode cn = m.getReturnType();
                Parameter[] ps = m.getParameters();
                String types[] = new String[ps.length];
                String names[] = new String[ps.length];
                for (int i = 0; i < ps.length; i++)
                {
                    types[i] = get_cls_name(ps[i].getDeclaringClass());
                    names[i] = ps[i].getName();
                }
                JavaResultItem item = new JavaResultItem.MethodItem(
                        m.getName(), get_cls_name(cn), types, names, m
                                .getModifiers());
                item.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(item);
            }
            
            for (FieldNode f : pair.node.getFields())
            {
                if(f.getName().indexOf('$') > -1 ||
                        f.getName().indexOf('<') > -1) continue;
                JavaResultItem item = new JavaResultItem.FieldResultItem(
                        f.getName(), get_cls_name(f.getType()), 
                                f.getModifiers());
                item.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(item);
            }
        }
         
         private void populateThis(CompletionResultSet resultSet,
                 ShellLikeGroovyEngine engine)
         {
             resultSet.setTitle("Global Context");
             Bindings b = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
             for (String key : b.keySet())
             {
                 JavaResultItem item = new JavaResultItem.VarResultItem(key, b.get(
                         key).getClass(), Modifier.PUBLIC);
                 item.setSubstituteOffset(queryCaretOffset);
                 resultSet.addItem(item);
             }
//             for (String s : engine.getContext(). .globalClosures.keySet())
//             {
//                 Closure c = engine.globalClosures.get(s);
//                 String m = (String) c.getProperty("method");
//                 if (m == null || m.indexOf("$") > -1) continue;
//                
//                 JavaResultItem item = new ClosureItem(c);
//                 item.setSubstituteOffset(queryCaretOffset);
//                 resultSet.addItem(item);
//             }
             add_metas(resultSet);
         } 
         
        private ClassNodePair resolve(AstPath realPath)
        {
            ModuleNode moduleNode = (ModuleNode) realPath.root();
            TypeInferenceVisitor typeVisitor = new TypeInferenceVisitor(moduleNode.getContext(),
                    realPath, sup.getElement(), queryCaretOffset, p.engine.getContext());
            typeVisitor.collect();
           
            if(prefix.indexOf(".") < 0)  //simple var
            {
                ClassNode guessedType = typeVisitor.vars.get(prefix);
                if (guessedType != null)
                    return new ClassNodePair(guessedType, typeVisitor.isStatic);
            }
            
            ASTNode leaf = realPath.leaf();
            if (leaf instanceof Expression)
            {
                //prop invocation ends with ConstantExpression
                if(leaf instanceof ConstantExpression)
                    leaf = realPath.leafParent();
                ClassNode n = typeVisitor.resolveExpression((Expression) leaf);
                if(n != null) return new ClassNodePair(n, typeVisitor.isStatic);
            }
            return null;
        }
        
        private boolean packageCompletion(CompletionResultSet resultSet)
        {
            if(prefix.indexOf("(") > -1) return false;
            NamedInfo[]info = ClassRepository.getInstance().findSubElements(prefix);
            if (info.length > 0)
            {
              CompletionU.populatePackage(resultSet,
                    new CompletionU.DBPackageInfo(info, prefix),
                    queryCaretOffset);
              return true;
            }
            return false;
        }

       private static String get_cls_name(ClassNode n)
       {
           return n != null ? n.getName() : "Object";
       }
       
    }
    
    private static class ClassNodePair
    {
        ClassNode node;
        boolean isStatic;
        public ClassNodePair(ClassNode node, boolean isStatic)
        {
            this.node = node;
            this.isStatic = isStatic;
        }
    }

    static class ToolTipQuery extends AsyncCompletionQuery
    {
        private JTextComponent component;
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private JToolTip queryToolTip;
        /**
         * Method/constructor '(' position for tracking whether the method is
         * still being completed.
         */
        private Position queryMethodParamsStartPos = null;
        private boolean otherMethodContext;

        protected void query(CompletionResultSet resultSet,
                NotebookDocument doc, int caretOffset)
        {
            // Position oldPos = queryMethodParamsStartPos;
            queryMethodParamsStartPos = null;
            ScriptSupport sup = doc.getScriptSupport(caretOffset);
            if (sup == null || !(sup.getParser() instanceof GroovyScriptParser))
                return;
            GroovyScriptParser p = (GroovyScriptParser) sup.getParser();
            // TODO:
            //if (p.getRootNode() == null)
           // {
           //     resultSet.finish();
           //     return;
           // }
        }

        protected void prepareQuery(JTextComponent component)
        {
            this.component = component;
        }

        protected boolean canFilter(JTextComponent component)
        {
            CharSequence text = null;
            int textLength = -1;
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            try
            {
                if (caretOffset - queryCaretOffset > 0) text = DocumentUtilities
                        .getText(doc, queryCaretOffset, caretOffset
                                - queryCaretOffset);
                else if (caretOffset - queryCaretOffset < 0) text = DocumentUtilities
                        .getText(doc, caretOffset, queryCaretOffset
                                - caretOffset);
                else
                    textLength = 0;
            }
            catch (BadLocationException e)
            {
            }
            if (text != null)
            {
                textLength = text.length();
            }
            else if (textLength < 0) { return false; }
            boolean filter = true;
            int balance = 0;
            for (int i = 0; i < textLength; i++)
            {
                char ch = text.charAt(i);
                switch (ch)
                {
                case ',':
                    filter = false;
                    break;
                case '(':
                    balance++;
                    filter = false;
                    break;
                case ')':
                    balance--;
                    filter = false;
                    break;
                }
                if (balance < 0) otherMethodContext = true;
            }
            if (otherMethodContext && balance < 0) otherMethodContext = false;
            if (queryMethodParamsStartPos == null
                    || caretOffset <= queryMethodParamsStartPos.getOffset())
                filter = false;
            return otherMethodContext || filter;
        }

        protected void filter(CompletionResultSet resultSet)
        {
            if (!otherMethodContext)
            {
                resultSet.setAnchorOffset(queryAnchorOffset);
                resultSet.setToolTip(queryToolTip);
            }
            resultSet.finish();
        }
    }

    static class ClosureItem extends JavaResultItem.MethodItem
    {
        Closure closure;

        public ClosureItem(Closure closure)
        {
            super(closure instanceof MethodClosure ? ((MethodClosure) closure)
                    .getMethod() : "Closure", Object.class, closure
                    .getParameterTypes(), null);
            this.modifiers = Modifier.PUBLIC;
        }

        protected boolean isAddParams()
        {
            return true;
        }
    }

}
