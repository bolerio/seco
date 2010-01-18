package seco.notebook.javascript;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import bsh.BshCompletionProvider;

import seco.notebook.NotebookDocument;
import seco.notebook.javascript.jsr.ExternalScriptable;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.AsyncCompletionQuery;
import seco.notebook.syntax.completion.AsyncCompletionTask;
import seco.notebook.syntax.completion.BaseAsyncCompletionQuery;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionDocumentation;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.CompletionResultSet;
import seco.notebook.syntax.completion.CompletionTask;
import seco.notebook.syntax.completion.JavaDocManager;
import seco.notebook.syntax.java.JavaPaintComponent;
import seco.notebook.syntax.java.JavaResultItem;
import seco.notebook.syntax.java.JavaPaintComponent.MethodPaintComponent;
import seco.notebook.syntax.util.JMIUtils;
import seco.notebook.util.DocumentUtilities;

public class JSCompletionProvider implements CompletionProvider
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
            JavaScriptParser p = (JavaScriptParser) sup.getParser();
            String s = sup.getCommandBeforePt(offset);
            // TODO: should consider inner scopes, etc.
            if ("this".equals(s))
            {
                populateThis(resultSet, p);
                queryResult = resultSet;
                resultSet.finish();
                return;
            }
            
            Object obj = p.resolveVar(s, offset);
            if (obj == null)
            {
                resultSet.finish();
                return;
            }
            // Class<?> cls = obj.getClass();
            if (obj instanceof ScriptableObject)
            {
                String name = ((ScriptableObject) obj).getClassName();
                if (BUILDINS.isBuiltInType(name)
                        && !BUILDINS.OBJECT.equals(name)) populateBuiltInObject(
                        resultSet, name);
                else if (obj instanceof IdScriptableObject)
                    populateNativeObject(resultSet, (IdScriptableObject) obj);
                else if (obj instanceof NativeJavaPackage)
                    ;//TODO
            }else if(Object.class == obj)
                populateBuiltInObject(resultSet, BUILDINS.OBJECT);
            else if(jsEquivalent(obj, resultSet))
                    ;
            else {
                Class<?> cls = (Class<?>) obj.getClass();
                BshCompletionProvider.populateClass(resultSet, 
                        cls, Modifier.PUBLIC, queryCaretOffset);
            }
            queryResult = resultSet;
            resultSet.finish();
        }
        
        private boolean jsEquivalent(Object o, CompletionResultSet resultSet)
        {
            String name = null;
            if(o instanceof String) 
               name = BUILDINS.STRING;
            else if(o instanceof String) 
                name = BUILDINS.NUM;
            else if(o instanceof Date)
                name = BUILDINS.DATE;
            else if(o instanceof Boolean) 
                name = BUILDINS.BOOL;
            //else if(o.getClass().isArray())
            //    name = BUILDINS.ARRAY;
            if(name != null)
                populateBuiltInObject(resultSet, name);
            return name != null;
        }

        private void populateNativeObject(CompletionResultSet resultSet,
                IdScriptableObject obj)
        {
            for (JavaResultItem item : BUILDINS.getParams(BUILDINS.OBJECT))
            {
                item.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(item);
            }
            Object[] ids = obj.getAllIds();
            if (ids != null) for (Object id : ids)
                resultSet.addItem(new JSProperty("" + id, "Object"));

            resultSet.setTitle(obj.getClassName());
        }

        private void populateBuiltInObject(CompletionResultSet resultSet,
                String class_name)
        {
            List<JavaResultItem> params = BUILDINS.getParams(class_name);
            if (params != null)
            {
                for (JavaResultItem item : params)
                {
                    item.setSubstituteOffset(queryCaretOffset);
                    resultSet.addItem(item);
                }
                resultSet.setTitle(class_name);
            }
        }

        private void populateThis(CompletionResultSet resultSet, JavaScriptParser p)
        {
            // TODO: add all vars from the RuntimeContext.
            List<JavaResultItem> params = BUILDINS.getThisParams();

            for (JavaResultItem item : params)
            {
                item.setSubstituteOffset(queryCaretOffset);
                resultSet.addItem(item);
            }

            ScriptContext ctx = p.engine.getContext();
            ExternalScriptable scope = (ExternalScriptable) p.engine
                    .getRuntimeScope(ctx);
            Context.enter();
//            Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
//            for (String key : b.keySet())
//            {
//                Object o = b.get(key);
            for (Object k : scope.getIds())
            {
                String key = "" + k;
                Object o = scope.get(key, scope.getPrototype());
                if (o instanceof NativeFunction) resultSet.addItem(BUILDINS
                        .make_func(key, (NativeFunction) o));
                else
                    resultSet.addItem(new JSProperty(key, JMIUtils.getTypeName(
                            o.getClass(), false, false)));
            }
            Context.exit();
            resultSet.setTitle("Global");
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
            if (sup == null || !(sup.getParser() instanceof JavaScriptParser)) return;
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

    static class JSMethod extends JavaResultItem.MethodResultItem
    {
        private static JavaPaintComponent.MethodPaintComponent mtdComponent = null;

        public JSMethod(String mtdName, String type)
        {
            super(mtdName, type);
            this.modifiers = Modifier.PUBLIC;
        }

        public JSMethod(String mtdName, String type, int modifiers)
        {
            super(mtdName, type);
            this.modifiers = modifiers;
        }

        public JSMethod(String mtdName, String type, String[] types,
                String[] names, int modifiers)
        {
            super(mtdName, type);
            this.modifiers = modifiers;
            populateParams(types, names);
        }

        public JSMethod(String mtdName, String type, String[] types,
                String[] names)
        {
            this(mtdName, type, types, names, Modifier.PUBLIC);
        }

        protected boolean isAddParams()
        {
            return true;
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (mtdComponent == null)
                mtdComponent = new MethodPaintComponent();

            mtdComponent.setFeatureName(getName());
            mtdComponent.setModifiers(getModifiers());
            mtdComponent.setTypeName(getTypeName());
            mtdComponent.setTypeColor(getTypeColor());
            mtdComponent.setParams(getParams());
            // mtdComponent.setExceptions(getExceptions());
            return mtdComponent;
        }

        void populateParams(String[] prms, String names[])
        {
            for (int i = 0; i < prms.length; i++)
                params.add(new ParamStr(prms[i], prms[i], names[i], false,
                        getTypeColor(prms[i])));
        }
    }

    static class JSProperty extends JavaResultItem.FieldResultItem
    {

        public JSProperty(String name, String type)
        {
            super(name, type, Modifier.PUBLIC);
        }

        public JSProperty(String name, String type, int modifiers)
        {
            super(name, type, modifiers);
        }

    }

}
