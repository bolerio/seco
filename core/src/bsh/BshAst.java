/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import java.io.CharArrayReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.tree.TreePath;

import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionU;
import seco.notebook.syntax.completion.NBParser;
import seco.storage.ClassRepository;
import seco.storage.NamedInfo;
import seco.util.SegmentCache;

public class BshAst extends NBParser
{
    boolean evaled_or_guessed = true;
    ParserRunnable parserRunnable;
    Parser parser;
    SimpleNode astRoot;
    boolean implicitExecutionOn = false;
    private List<SimpleNode> nodes = new LinkedList<SimpleNode>();

    public BshAst(final ScriptSupport support)
    {
        super(support);
    }

    SimpleNode getRootNode()
    {
        return astRoot;
    }

    
    public Object resolveVar(String s, int offset) // throws EvalError
    {
        // TODO: maybe we could infere somehow non-iniatilized or null values
        // e.g. String s; or String s = null;
        evaled_or_guessed = true;
        if (s == null || s.length() == 0) return null;
        try
        {
            if (implicitExecutionOn || s.indexOf("(") < 0)
            {
                Object o = support.getDocument().getEvaluationContext().eval(
                        support.getFactory().getEngineName(), s);
                if (o != null) return o;
            }

            // try package completion
            if (s.indexOf("(") < 0)
            {
                NamedInfo[] info = ClassRepository.getInstance()
                        .findSubElements(s);
                return (info.length > 0) ? new CompletionU.DBPackageInfo(info,
                        s) : null;
            }
            else
            // some method is present
            {
                Method res = resolveMethod(s, offset);
                evaled_or_guessed = false;
                return res != null ? res.getReturnType() : null;
            }
        }
        catch (Exception err)
        {
            // err.printStackTrace();
            NamedInfo[] info = ClassRepository.getInstance().findSubElements(s);
            return (info.length > 0) ? new CompletionU.DBPackageInfo(info, s)
                    : null;
        }
    }
    
    public Class<?> resolveVarAsClass(String s, int offset)
    {
        Object o = resolveVar(s, offset);
        if(o == null) return null;
        return (o.getClass().getName().equals(ClassIdentifier.class.getName()))?
                BshAst.getClsFromClassIdentifier(o) : o.getClass();
    }

    public Method resolveMethod(String s, int offset)
    {
        SimpleNode n = getRootNode();
        int[] lineCol = support.offsetToLineCol(offset);
        if (n == null || n.children.length <= lineCol[0])
        {
            // try to create AST only for the passed in String
            Reader r = new CharArrayReader((s + ";").toCharArray());
            Parser p = getParser(r);
            try
            {
                p.Line();
                n = p.popNode();
            }
            catch (ParseException ex)
            {
                return null;
            }
        }

        if (n instanceof BSHMethodInvocation)
            return resolveMethod((BSHMethodInvocation) n, offset);

        SimpleNode outer = ParserUtils.getASTNodeAtOffset(support.getElement(),
                n, offset - 1);
        if (outer == null) return null;

        BSHMethodInvocation m = (BSHMethodInvocation) ParserUtils
                .getParentOfType(outer, BSHMethodInvocation.class);
        if (m != null) return resolveMethod(m, offset);

        // search for BSHPrimarySuffix
        outer = ParserUtils.getParentOfType(outer, BSHPrimarySuffix.class);
        if (outer != null
                && ((BSHPrimarySuffix) outer).operation == BSHPrimarySuffix.NAME)
        {
            int new_offset = support.lineToOffset(
                    outer.firstToken.beginLine - 1,
                    outer.firstToken.beginColumn);
            int i = s.lastIndexOf(((BSHPrimarySuffix) outer).field);

            Object o = resolveVar(s.substring(0, i - 1), new_offset);
            return resolveSuffix(o, ((BSHPrimarySuffix) outer), offset);
        }

        return null;
    }

    static Class<?> getClsFromClassIdentifier(Object obj)
    {
        try
        {
            Method method = obj.getClass().getMethod("getTargetClass",
                    (Class[]) null);
            return (Class<?>) method.invoke(obj, (Object[]) null);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private Method resolveSuffix(Object o, BSHPrimarySuffix suff, int offset)
    {
        if (suff.operation == BSHPrimarySuffix.NAME)
        {
            Class<?>[] types = resolveArgs(((BSHArguments) suff.getChild(0)),
                    offset);
            Class<?> c = null;
            if(o instanceof Method)
                c =  ((Method) o).getReturnType();
            c = (!(o instanceof Class)) ? o.getClass() : (Class<?>) o;
            Method meth = findMethod(c, suff.field, types, false); // isPrivateAccessAllowed());
            return (meth != null) ? meth : null;

        }
        return null;
    }

    private Method resolveMethod(BSHMethodInvocation m, int offset)
    {
        String text = m.getNameNode().text;
        int dot = text.lastIndexOf('.');
        if (dot == -1) return null;
        Object var = resolveVar(text.substring(0, dot), offset);
        if (var == null) return null;
        Class<?>[] types = resolveArgs(m.getArgsNode(), offset);
        Class<?> inst_cls = null;
        if (var.getClass().getName().indexOf("bsh.ClassIdentifier") > -1) inst_cls = getClsFromClassIdentifier(var);
        else
            inst_cls = var.getClass();
        Method meth = findMethod(inst_cls, text.substring(dot + 1), types,
                false);
        if (meth == null) return null;

        return findMethod(inst_cls, text.substring(dot + 1), types, false);
    }

    private Class<?>[] resolveArgs(BSHArguments b, int offset)
    {
        int count = (b.children != null) ? b.children.length : 0;
        Object[] args = new Object[count];
        for (int i = 0; i < args.length; i++)
        {
            BSHPrimaryExpression bpe = (BSHPrimaryExpression) b.getChild(i);
            args[i] = resolveVar(bpe.getText(), offset);
        }
        return Types.getTypes(args);
    }

    private static Method findMethod(Class<?> baseClass, String methodName,
            Class<?>[] types, boolean publicOnly)
    {
        Method[] methods = Reflect.getCandidateMethods(baseClass, methodName,
                types.length, publicOnly);
        Method method = Reflect.findMostSpecificMethod(types, methods);
        return method;
    }

    private static final String ACCESSIBILITY_CHECK = "import bsh.*; Capabilities.accessibility;";

    boolean isPrivateAccessAllowed()
    {
        try
        {
            return ((Boolean) support.getDocument().getEvaluationContext()
                    .eval(support.getFactory().getEngineName(),
                            ACCESSIBILITY_CHECK)).booleanValue();
        }
        catch (Exception err)
        {
            return false;
        }
    }

    private Parser getParser(Reader r)
    {
        if (parser == null)
        {
            parser = new Parser(r);
            parser.setRetainComments(true);
        }
        else
            parser.ReInit(r);
        return parser;
    }

    private Reader makeReader(Segment seg, int offset, int length)
    {
        CharArrayReader r = (seg.array.length > length) ? new CharArrayReader(
                seg.array, offset, length) : new CharArrayReader(seg.array);
        return r;
    }

    public NBParser.ParserRunnable getParserRunnable()
    {
        if (parserRunnable != null) return parserRunnable;
        parserRunnable = new NBParser.ParserRunnable() {
            public boolean doJob()
            {
                Segment seg = SegmentCache.getSharedInstance().getSegment();
                try
                {
                    Element el = support.getElement();
                    int offset = el.getStartOffset();
                    int length = el.getEndOffset() - el.getStartOffset();
                    support.getDocument().getText(offset, length, seg);
                    Reader r = makeReader(seg, offset, length);
                    parser = getParser(r);
                    // System.out.println("parser.start()..." + length);
                    // long start = System.currentTimeMillis();
                    nodes.clear();
                    while (!(parser.Line()))
                    {
                        SimpleNode node = parser.popNode();
                        if (node != null) nodes.add(node);
                    }
                    astRoot = new SimpleNode(0);
                    astRoot.children = (SimpleNode[]) nodes
                            .toArray(new SimpleNode[nodes.size()]);

                    if (implicitExecutionOn)
                    {
                        ScriptEngine eng = support.getDocument()
                                .getEvaluationContext().getEngine(
                                        support.getFactory().getEngineName());
                        ScriptContext scriptContext = eng.getContext();
                        r = makeReader(seg, offset, length);
                        // r.reset();
                        eng.eval(r, scriptContext);
                    }
                    // System.out.println("Time: " + (System.currentTimeMillis()
                    // - start));
                    support.unMarkErrors();
                    return true;
                }
                catch (bsh.ParseException ex)
                {
                    // System.err.println("Error: " +
                    // stripBshMsg(ex.getMessage()));
                    // System.err.println("Seg: " + seg);
                    // Line begins from 1 in the parser
                    try
                    {
                        ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
                                stripBshMsg(ex.getMessage()),
                                ex.currentToken.next.beginLine - 1,
                                ex.currentToken.next.beginColumn);
                        support.markError(mark);
                    }
                    catch (Throwable t)
                    {
                    }
                    // System.err.println(ex.getClass() + ":" +
                    // ex.getMessage());
                    return false;
                }
                catch (javax.script.ScriptException ex)
                {
                    ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
                            stripMsg(ex.getMessage()), ex.getLineNumber() - 1,
                            ex.getColumnNumber());
                    support.markError(mark);
                    // System.err.println(ex.getClass() + ":" +
                    // ex.getMessage());
                    return false;
                }
                catch (Throwable e)
                {
                    // e.printStackTrace();
                    // System.err.println(e.getClass() + ":" + e.getMessage());
                    return false;
                }
                finally
                {
                    SegmentCache.getSharedInstance().releaseSegment(seg);
                }
            }
        };
        return parserRunnable;
    }

    // TODO: following 2 methods could be further fine tuned
    private static String stripMsg(String s)
    {
        if (s == null) return s;
        s = s.substring(s.indexOf(":") + 1);
        s = s.substring(s.indexOf(":") + 1);
        // strip the rest after 2nd ":"
        int i = s.indexOf(":");
        i = s.indexOf(":", i + 1);
        return s.substring(0, i);
    }

    private static String stripBshMsg(String s)
    {
        if (s == null) return s;
        int i = s.indexOf("<unknown>");
        return (i >= 0) ? s.substring(i + 10) : s;
    }

    public void insertUpdate(DocumentEvent e)
    {
        removeUpdate(e);
    }

    public void removeUpdate(DocumentEvent e)
    {
        // int line = support.offsetToLine(e.getOffset());
        // if(last_valid_line > line)
        // last_valid_line = line -1;
        update();
    }

    public JTree getAstTree()
    {
        if (astRoot == null) return null;
        JTreeAst astTree = new JTreeAst();
        JTreeASTModel treeModel = new JTreeASTModel(astRoot);
        astTree.setModel(treeModel);
        astTree.collapsePath(new TreePath(astTree.getModel().getRoot()));
        return astTree;
    }

}
