package seco.langs.groovy;

import groovy.lang.Closure;

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.codehaus.groovy.runtime.MethodClosure;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.ScriptableObject;

import seco.langs.groovy.GroovyScriptSupport.GroovyScriptParser;
import seco.langs.groovy.jsr.GroovyScriptEngine;
import seco.langs.javascript.BuiltIns;
import seco.langs.javascript.JavaScriptParser;
import seco.langs.javascript.jsr.ExternalScriptable;
import seco.notebook.NotebookDocument;
import seco.notebook.storage.ClassRepository;
import seco.notebook.storage.NamedInfo;
import seco.notebook.storage.PackageInfo;
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
import seco.notebook.syntax.java.JavaResultItem.ParamStr;
import seco.notebook.syntax.util.JMIUtils;
import seco.notebook.util.DocumentUtilities;

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
        public Query(int caretOffset)
        {
            super(caretOffset);
        }

        protected void query(CompletionResultSet resultSet,
                NotebookDocument doc, int offset)
        {
            ScriptSupport sup = doc.getScriptSupport(offset);
            queryCaretOffset = offset;
            queryAnchorOffset = offset;
            GroovyScriptParser p = (GroovyScriptParser) sup.getParser();
            String s = sup.getCommandBeforePt(offset);
            // TODO: should consider inner scopes, etc.
            if ("this".equals(s))
            {
                populateThis(resultSet, p.engine);
                queryResult = resultSet;
                resultSet.finish();
                return;
            }

            CompletionHandler ch = new CompletionHandler();
            List<CompletionItem> props =  ch.complete(p.parserResult, sup.getElement(), s, offset);
            for(CompletionItem item: props)
                resultSet.addItem(item);
//            if (obj == null)
//            {
//                resultSet.finish();
//                return;
//            }
             
            queryResult = resultSet;
            resultSet.finish();
        }
        
        private void populateThis(CompletionResultSet resultSet, GroovyScriptEngine engine)
        {
            Bindings b = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            for(String key : b.keySet())
            {
                resultSet.addItem(new JavaResultItem.VarResultItem(
                        key, b.get(key).getClass(), Modifier.PUBLIC));
            }
            for(String s: engine.globalClosures.keySet())
            {
                Closure c = engine.globalClosures.get(s);
                String m = (String) c.getProperty("method");
                if(m == null || m.indexOf("$") > -1) continue;
                resultSet.addItem(new ClosureItem(c));
            }
        }
     }

    public static class DocQuery extends AsyncCompletionQuery
    {
        private Object item;
        private JTextComponent component;
        private static Action goToSource = new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                if (e != null) Completion.get().hideDocumentation();
            }
        };

        public DocQuery(Object item)
        {
            this.item = item;
        }

        protected void query(CompletionResultSet resultSet,
                NotebookDocument doc, int caretOffset)
        {
            if (item != null && JavaDocManager.SHOW_DOC)
                resultSet.setDocumentation(new DocItem(
                        getAssociatedObject(item), null));
            resultSet.finish();
        }

        protected void prepareQuery(JTextComponent component)
        {
            this.component = component;
        }

        private Object getAssociatedObject(Object item)
        {
            Object ret = item;
            if (item instanceof JavaResultItem)
                ret = ((JavaResultItem) item).getAssociatedObject();
            return ret;
        }

        private class DocItem implements CompletionDocumentation
        {
            private String text;
            private JavaDoc javaDoc;
            private Object item;
            private URL url;

            public DocItem(Object item, JavaDoc javaDoc)
            {
                this.javaDoc = javaDoc != null ? javaDoc : new JavaDoc(
                        component);
                this.javaDoc.docItem = this;
                this.javaDoc.setItem(item);
                this.url = getURL(item);
            }

            public CompletionDocumentation resolveLink(String link)
            {
                return null;
            }

            public String getText()
            {
                return text;
            }

            public URL getURL()
            {
                return url;
            }

            private URL getURL(Object item)
            {
                return javaDoc.getURL(item);
            }

            public Action getGotoSourceAction()
            {
                return item != null ? goToSource : null;
            }

            private class JavaDoc
            {
                public static final String CONTENT_NOT_FOUND = "JavaDoc Not Found.";
                private DocItem docItem;

                private JavaDoc(JTextComponent component)
                {
                }

                private void setItem(Object item)
                {
                    showJavaDoc(JavaDocManager.getInstance().getHTML(item));
                }

                private URL getURL(Object item)
                {
                    return null;
                }

                protected void showJavaDoc(String preparedText)
                {
                    if (preparedText == null) preparedText = CONTENT_NOT_FOUND;
                    docItem.text = preparedText;
                }
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
            if (sup == null || !(sup.getParser() instanceof JavaScriptParser))
                return;
            JavaScriptParser p = (JavaScriptParser) sup.getParser();
            // TODO:
            if (p.getRootNode() == null)
            {
                resultSet.finish();
                return;
            }
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

    static class ClosureItem extends JavaResultItem.MethodResultItem
    {
        Closure closure;
        public ClosureItem(Closure closure)
        {
            super(closure instanceof MethodClosure ?
                    ((MethodClosure) closure).getMethod(): "Closure", Object.class,
                    closure.getParameterTypes(), null);
            this.modifiers = Modifier.PUBLIC;
        }

        protected boolean isAddParams()
        {
            return true;
        }
    }



}
