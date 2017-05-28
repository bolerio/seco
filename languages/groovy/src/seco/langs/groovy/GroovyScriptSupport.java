package seco.langs.groovy;

import java.util.List;


import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import seco.AppConfig;
import seco.langs.groovy.jsr.GroovyScriptEngine;
import seco.langs.groovy.jsr.ShellLikeGroovyEngine;
import seco.notebook.syntax.Formatter;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.ScriptSupportFactory;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.java.JavaFormatter;
import seco.notebook.syntax.java.JavaFormatterOptions;
import seco.util.SegmentCache;

public class GroovyScriptSupport extends ScriptSupport
{
    
    public GroovyScriptSupport(ScriptSupportFactory factory, Element el)
    {
        super(factory, el);
    }

    private static CompletionProvider[] providers = 
              new CompletionProvider[]{new GroovyCompletionProvider()};
    @Override
    public CompletionProvider[] getCompletionProviders()
    {
        return providers;
    }

   private GroovyScriptParser parser = null;

    @Override
    public NBParser getParser()
    {
        if (parser == null)
             parser = new GroovyScriptParser(this);
        return parser;
   }
    
    private static Formatter formatter;
    public Formatter getFormatter()
    {
        if(formatter == null){
            formatter = new JavaFormatter((JavaFormatterOptions)
                    AppConfig.getInstance().getProperty(
                            AppConfig.FORMATTER_OPTIONS, new JavaFormatterOptions()));
        }
        return formatter;
    }
    
    static class GroovyScriptParser extends NBParser
    {
    	ShellLikeGroovyEngine engine;
        ParserRunnable parserRunnable;
        CompilationUnit compilationUnit;
        GroovyParserResult parserResult;

        public GroovyScriptParser(final ScriptSupport support)
        {
            super(support);
            engine = (ShellLikeGroovyEngine) support.getDocument()
                    .getEvaluationContext().getEngine("groovy");
        }
    
         
        @Override
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
                        compilationUnit = new CompilationUnit(engine.getClassLoader());
                        String fileName = seco.langs.groovy.jsr.GroovyScriptEngine.generateScriptName();
                        compilationUnit.addSource(fileName, seg.toString());
                        compilationUnit.compile(Phases.CLASS_GENERATION);
                        CompileUnit compileUnit = compilationUnit.getAST();
                        List<ModuleNode> modules = compileUnit.getModules();

                        // there are more modules if class references another class,
                        // there is one module per class
                        ModuleNode module = null;
                        for (ModuleNode moduleNode : modules) {
                            if (fileName.equals(moduleNode.getContext().getName())) {
                                module = moduleNode;
                            }
                        }
                        if (module != null) {
                             // FIXME parsing API
                            parserResult = new GroovyParserResult(
                                    GroovyScriptParser.this, module, compilationUnit.getErrorCollector());
                        }
                        
                        support.unMarkErrors();
                        return true;
                    }
                    catch (Throwable e)
                    {
                        ErrorCollector errorCollector = compilationUnit.getErrorCollector();
                        if (errorCollector.hasErrors()) {
                            Message message = errorCollector.getLastError();
                            if (message instanceof SyntaxErrorMessage) {
                                SyntaxException se = ((SyntaxErrorMessage)message).getCause();

                                // if you have a single line starting with: "$
                                // SyntaxException.getStartLine() returns 0 instead of 1
                                // we have to fix this here, before ending our life
                                // in an Assertion in AstUtilities.getOffset().

                                int line = se.getStartLine();

                                if(line < 1 )
                                    line = 1;

                                int col = se.getStartColumn();

                                if(col < 1 )
                                    col = 1;

                                ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
                                        se.getMessage(), line, col);
                                support.markError(mark);
                            }
                        } 

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
   
        public Object resolveVar(String s, int offset)
        {
            if (s == null || s.length() == 0) return null;
            try
            {
                Object o = null;//scope.get0(s, scope);
                if (s.indexOf("(") < 0)
                {
                    o = support.getDocument().getEvaluationContext().eval(
                            support.getFactory().getEngineName(), s);
                    if (o != null) return o;
                }else
                    resolveMethod(s, offset);
            }
            catch (Exception err)
            {
                //err.printStackTrace();
            }
            return null;
        }
        
        public Object resolveMethod(String s, int offset)
        {
            return null;
        }
        
        public JTree getAstTree()
        {
            if (parserResult == null) return null;
            JTree astTree = new JTree();
            GroovyTreeModel treeModel = new GroovyTreeModel(parserResult.getRootElement());
            astTree.setModel(treeModel);
            // astTree.collapsePath(new TreePath(astTree.getModel()
            // .getRoot()));
            return astTree;
        }
        
    }
    
    public static class GroovyTreeModel implements TreeModel
    {
        ASTNode root = null;
        // ArrayList listeners; // lazy instantiation
        protected EventListenerList listenerList = new EventListenerList();

        public GroovyTreeModel(ASTNode t)
        {
            if (t == null)
            {
                throw new IllegalArgumentException("root is null");
            }
            root = t;
        }

        public Object getChild(Object parent, int index)
        {
            if (parent == null)
            {
                return null;
            }
            // int i = 0;
            ASTNode p = (ASTNode) parent;
            List c = AstUtilities.children(p); 
            if(index >= c.size())
                throw new ArrayIndexOutOfBoundsException("node has no such child");
            return c.get(index);
        }

        public int getChildCount(Object parent)
        {
            if (parent == null)
            {
                throw new IllegalArgumentException("root is null");
            }
            if (parent instanceof String) return 0;
            ASTNode p = (ASTNode) parent;
            return AstUtilities.children(p).size();
        
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            if (parent == null || child == null)
            {
                throw new IllegalArgumentException("root or child is null");
            }
            int i = 0;
            ASTNode p = (ASTNode) parent;
            ASTNode c = AstUtilities.children(p).get(0);// getFirstChild();
            while (c != null && c != child)
            {
                c = AstUtilities.children(c).get(i); // getNextSibling();
                i++;
            }
            if (c == child)
            {
                return i;
            }
            throw new java.util.NoSuchElementException("node is not a child");
        }

        public Object getRoot()
        {
            return root;
        }

        public boolean isLeaf(Object node)
        {
            if (node == null)
            {
                throw new IllegalArgumentException("node is null");
            }
            if (node instanceof String)
                return true;
            else
            {
                ASTNode t = (ASTNode) node;
                return AstUtilities.children(t) == null || 
                AstUtilities.children(t).size() == 0;
            }
        }

        /**
         * Adds a listener for the TreeModelEvent posted after the tree changes.
         * 
         * @see #removeTreeModelListener
         * @param l the listener to add
         */
        public void addTreeModelListener(TreeModelListener l)
        {
            listenerList.add(TreeModelListener.class, l);
        }

        /**
         * Removes a listener previously added with <B>addTreeModelListener()</B>.
         * 
         * @see #addTreeModelListener
         * @param l the listener to remove
         */
        public void removeTreeModelListener(TreeModelListener l)
        {
            listenerList.remove(TreeModelListener.class, l);
        }

        /**
         * Returns an array of all the tree model listeners registered on this
         * model.
         * 
         * @return all of this model's <code>TreeModelListener</code>s or an
         * empty array if no tree model listeners are currently registered
         * 
         * @see #addTreeModelListener
         * @see #removeTreeModelListener
         * 
         * @since 1.4
         */
        public TreeModelListener[] getTreeModelListeners()
        {
            return (TreeModelListener[]) listenerList
                    .getListeners(TreeModelListener.class);
        }

        public void valueForPathChanged(TreePath path, Object newValue)
        {
            // System.out.println("\nvalueForPathChanged ... \n");
            fireTreeStructureChanged(path.getLastPathComponent(), path);
        }

        /*
         * ==================================================================
         * 
         * Borrowed from javax.swing.tree.DefaultTreeModel
         * 
         * ==================================================================
         */
        /*
         * Notifies all listeners that have registered interest for notification on
         * this event type. The event instance is lazily created using the
         * parameters passed into the fire method.
         * 
         * @param source the node where the tree model has changed @param path the
         * path to the root node
         * @see EventListenerList
         */
        private void fireTreeStructureChanged(Object source, TreePath path)
        {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
                if (listeners[i] == TreeModelListener.class)
                {
                    // Lazily create the event:
                    if (e == null) e = new TreeModelEvent(source, path);
                    ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
                }
            }
        }

        /**
         * @param nodeBefore
         * @return
         */
        public TreePath getTreePath(ASTNode node)
        {
            ASTNode[] nodes = getTreePathNodes((ASTNode) getRoot(), node, 0);
            if (nodes == null)
                return null;
            else
                return new TreePath(nodes);
        }

        public ASTNode[] getTreePathNodes(ASTNode root, ASTNode node,
                int depth)
        {
            if (node == null) return null;
            depth++;
            ASTNode[] retNodes = null;
            if (node == root)
            {
                retNodes = new ASTNode[depth];
                retNodes[depth - 1] = root;
            } else
            {
                int n = AstUtilities.children(root).size(); // .getNumberOfChildren();
                loop: for (int i = 0; i < n; i++)
                {
                    retNodes = getTreePathNodes((ASTNode) getChild(root, i),
                            node, depth);
                    if (retNodes != null)
                    {
                        retNodes[depth - 1] = root;
                        break loop;
                    }
                }
            }
            return retNodes;
        }
    }


}
