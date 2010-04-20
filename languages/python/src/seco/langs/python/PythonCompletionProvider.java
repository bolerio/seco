package seco.langs.python;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.python.antlr.PythonTree;
import org.python.core.PyDictProxy;
import org.python.core.PyList;
import org.python.core.PyMethodDescr;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyType;

import bsh.BshCompletionProvider;

import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.storage.ClassRepository;
import seco.notebook.storage.NamedInfo;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.AsyncCompletionQuery;
import seco.notebook.syntax.completion.AsyncCompletionTask;
import seco.notebook.syntax.completion.BaseAsyncCompletionQuery;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionDocumentation;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.CompletionResultSet;
import seco.notebook.syntax.completion.CompletionTask;
import seco.notebook.syntax.completion.CompletionU;
import seco.notebook.syntax.completion.JavaDocManager;
import seco.notebook.syntax.java.JavaResultItem;
import seco.notebook.syntax.java.JavaResultItem.ParamStr;
import seco.notebook.util.DocumentUtilities;

public class PythonCompletionProvider implements CompletionProvider
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
        PythonParser p;
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
            p = (PythonParser) sup.getParser();
            prefix = sup.getCommandBeforePt(offset);
            if (p.parseTree == null) return;
            if (!complete(resultSet)) return;

            queryResult = resultSet;
            resultSet.finish();
        }

        // private void add_metas(CompletionResultSet resultSet)
        // {
        // for(CompletionItem i: BuiltIns.getGroovyMetas())
        // {
        // i.setSubstituteOffset(queryCaretOffset);
        // resultSet.addItem(i);
        // }
        // }

        private boolean complete(final CompletionResultSet resultSet)
        {
            PythonTree root = p.parseTree;

            if (packageCompletion(resultSet)) return true;
            final AstPath path = AstPath.get(root, queryCaretOffset - 1);
            PythonTypeAnalyzer an = new PythonTypeAnalyzer(root,
                    queryCaretOffset - 1);
            String type = an.getType(prefix);
            if (type != null)
            {
                populateType(resultSet, type);
            }
            // AstPath realPath = new AstPath(root, queryCaretOffset - 1,
            // sup.getElement());
            // ClassNodePair pair = resolve(realPath);
            // if (pair == null) return false;
            // //
            // if(root.getScriptClassDummy().equals(pair.node))
            // {
            // populateThis(resultSet, p.engine);
            // return true;
            // }
            // resultSet.setTitle(pair.node.getName());
            // populateObject(resultSet, pair, prefix);

            return true;
        }

        private void populateType(CompletionResultSet resultSet, String type)
        {
            PyType o = (PyType) PySystemState.getDefaultBuiltins()
                    .__finditem__(type);
            resultSet.setTitle(type);
            PyList list = (PyList) o.__dir__();
            for (int i = 0; i < list.size(); i++)
            {
                String key = list.get(i).toString();
                if (!key.startsWith("__"))
                {
                    PyObject entry = o.fastGetDict().__finditem__(key);
                    if (entry instanceof PyMethodDescr)
                    {
                        PyMethodDescr desc = (PyMethodDescr) entry;
                        // System.out.println(""+ i + ":" + desc.getName());
                        resultSet.addItem(new PyMethodItem(desc));
                    }
                }
            }
        }

        // private void populateObject(CompletionResultSet resultSet,
        // ClassNodePair pair, String prefix)
        // {
        //
        // Class<?> type = pair.node.isResolved() ? pair.node.getTypeClass() :
        // null;
        //            
        // System.out.println("populateObject: " + pair.node.getName() + ":" +
        // type);
        // if (type != null)
        // {
        // int mod = (pair.isStatic) ? Modifier.STATIC : Modifier.PRIVATE;
        // CompletionU.populateClass(resultSet, type, mod, queryCaretOffset);
        // add_metas(resultSet);
        // return;
        // }
        //
        // for (ClassNode inter : pair.node.getInterfaces())
        // {
        // System.out.println("interface: " + inter.getName());
        // if(inter.getName().equals("groovy.lang.GroovyObject"))
        // add_metas(resultSet);
        // // populateObject(resultSet, source, inter, prefix, anchor);
        // }
        //
        // for (MethodNode m : pair.node.getMethods())
        // {
        // if(m.getName().indexOf('$') > -1 ||
        // m.getName().indexOf('<') > -1)
        // continue;
        // ClassNode cn = m.getReturnType();
        // Parameter[] ps = m.getParameters();
        // String types[] = new String[ps.length];
        // String names[] = new String[ps.length];
        // for (int i = 0; i < ps.length; i++)
        // {
        // types[i] = get_cls_name(ps[i].getDeclaringClass());
        // names[i] = ps[i].getName();
        // }
        // JavaResultItem item = new JavaResultItem.MethodItem(
        // m.getName(), get_cls_name(cn), types, names, m
        // .getModifiers());
        // item.setSubstituteOffset(queryCaretOffset);
        // resultSet.addItem(item);
        // }
        //            
        // for (FieldNode f : pair.node.getFields())
        // {
        // if(f.getName().indexOf('$') > -1 ||
        // f.getName().indexOf('<') > -1) continue;
        // JavaResultItem item = new JavaResultItem.FieldResultItem(
        // f.getName(), get_cls_name(f.getType()),
        // f.getModifiers());
        // item.setSubstituteOffset(queryCaretOffset);
        // resultSet.addItem(item);
        // }
        // }
        //         
        // private void populateThis(CompletionResultSet resultSet,
        // GroovyScriptEngine engine)
        // {
        // resultSet.setTitle("Global Context");
        // Bindings b = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        // for (String key : b.keySet())
        // {
        // JavaResultItem item = new JavaResultItem.VarResultItem(key, b.get(
        // key).getClass(), Modifier.PUBLIC);
        // item.setSubstituteOffset(queryCaretOffset);
        // resultSet.addItem(item);
        // }
        // for (String s : engine.globalClosures.keySet())
        // {
        // Closure c = engine.globalClosures.get(s);
        // String m = (String) c.getProperty("method");
        // if (m == null || m.indexOf("$") > -1) continue;
        //                
        // JavaResultItem item = new ClosureItem(c);
        // item.setSubstituteOffset(queryCaretOffset);
        // resultSet.addItem(item);
        // }
        // add_metas(resultSet);
        // }
        //         
        // private ClassNodePair resolve(AstPath realPath)
        // {
        // ModuleNode moduleNode = (ModuleNode) realPath.root();
        // TypeInferenceVisitor typeVisitor = new
        // TypeInferenceVisitor(moduleNode.getContext(),
        // realPath, sup.getElement(), queryCaretOffset, p.engine.getContext());
        // typeVisitor.collect();
        //           
        // if(prefix.indexOf(".") < 0) //simple var
        // {
        // ClassNode guessedType = typeVisitor.vars.get(prefix);
        // if (guessedType != null)
        // return new ClassNodePair(guessedType, typeVisitor.isStatic);
        // }
        //            
        // ASTNode leaf = realPath.leaf();
        // if (leaf instanceof Expression)
        // {
        // //prop invocation ends with ConstantExpression
        // if(leaf instanceof ConstantExpression)
        // leaf = realPath.leafParent();
        // ClassNode n = typeVisitor.resolveExpression((Expression) leaf);
        // if(n != null) return new ClassNodePair(n, typeVisitor.isStatic);
        // }
        // return null;
        // }

        private boolean packageCompletion(CompletionResultSet resultSet)
        {
            if (prefix.indexOf("(") > -1) return false;
            NamedInfo[] info = ClassRepository.getInstance().findSubElements(
                    prefix);
            if (info.length > 0)
            {
                CompletionU.populatePackage(resultSet,
                        new CompletionU.DBPackageInfo(info, prefix),
                        queryCaretOffset);
                return true;
            }
            return false;
        }

        // private static String get_cls_name(ClassNode n)
        // {
        // return n != null ? n.getName() : "Object";
        // }

    }

    // private static class ClassNodePair
    // {
    // ClassNode node;
    // boolean isStatic;
    // public ClassNodePair(ClassNode node, boolean isStatic)
    // {
    // this.node = node;
    // this.isStatic = isStatic;
    // }
    // }

    public static class DocQuery extends AsyncCompletionQuery
    {
        private PyMethodItem item;
        private static Action goToSource = new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                if (e != null) Completion.get().hideDocumentation();
            }
        };

        public DocQuery(PyMethodItem item)
        {
            this.item = item;
        }

        protected void query(CompletionResultSet resultSet,
                NotebookDocument doc, int caretOffset)
        {
            if (item != null && JavaDocManager.SHOW_DOC)
                resultSet.setDocumentation(new DocItem(item));
            resultSet.finish();
        }

        private class DocItem implements CompletionDocumentation
        {
            private PyMethodItem item;

            public DocItem(PyMethodItem item)
            {
                this.item = item;
            }

            public CompletionDocumentation resolveLink(String link)
            {
                return null;
            }

            public String getText()
            {
                String doc = item.desc.fastGetDoc();
                if(doc == null) return "<br>Not found</br>";
                StringBuilder sb = new StringBuilder();
                sb.append("<pre style=\"margin: 5px 5px; border-size: 1px; padding: 5px\">"); 
                sb.append("\n");
                String[] lines = doc.split("\n");
                for (int i = 0; i < lines.length; i++)
                {
                    if(i == 0) sb.append("<b>");
                    appendEscaped(sb, lines[i]);
                    if(i == 0) sb.append("</b>");
                    sb.append("<br>"); 
                }
                sb.append("</pre>\n");
                System.out.println(sb.toString());
                return sb.toString();
            }

            private void appendEscaped(StringBuilder sb, CharSequence s)
            {
                for (int i = 0, n = s.length(); i < n; i++)
                {
                    char c = s.charAt(i);
                    if ('<' == c)
                    {
                        sb.append("&lt;"); // NOI18N
                    }
                    else if ('&' == c)
                    {
                        sb.append("&amp;"); // NOI18N
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }

            public URL getURL()
            {
                return null;
            }

            public Action getGotoSourceAction()
            {
                return goToSource;
            }

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
            if (sup == null || !(sup.getParser() instanceof PythonParser))
                return;
            PythonParser p = (PythonParser) sup.getParser();
            // TODO:
            // if (p.getRootNode() == null)
            // {
            // resultSet.finish();
            // return;
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

    static class PyMethodItem extends JavaResultItem.MethodItem
    {
        PyMethodDescr desc;

        public PyMethodItem(PyMethodDescr desc)
        {
            super(desc.getName(), "");
            this.desc = desc;
            // populateParams0(types, names);
        }

        void populateParams0(String[] prms, String names[])
        {
            // for (int i = 0; i < prms.length; i++)
            // params.add(new ParamStr(prms[i], prms[i], names[i], true,
            // getTypeColor(prms[i])));
        }

        protected boolean isAddParams()
        {
            return true;
        }

        public CompletionTask createDocumentationTask()
        {
            return new AsyncCompletionTask(new DocQuery(this), NotebookUI
                    .getFocusedNotebookUI());

        }
    }

}
