package seco.langs.javascript;

import java.io.CharArrayReader;
import java.io.Reader;

import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.nb.javascript.Node;

import seco.langs.javascript.jsr.ExternalScriptable;
import seco.langs.javascript.jsr.RhinoScriptEngine;
import seco.notebook.csl.ParseException;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.util.SegmentCache;


public class JavaScriptParser extends NBParser
{
    boolean evaled_or_guessed = true;
    ParserRunnable parserRunnable;
    PatchedParser parser;
    Node astRoot;
    boolean implicitExecutionOn = false;
    RhinoScriptEngine engine;

    public JavaScriptParser(final ScriptSupport support)
    {
        super(support);
        engine = (RhinoScriptEngine) support.getDocument()
                .getEvaluationContext().getEngine("javascript");
    }

    public Node getRootNode()
    {
        return astRoot;
    }

    public Object resolveVar(String s, int offset)
    {
        if (s == null || s.length() == 0) return null;
        if (getRootNode() == null) return null;

        ExternalScriptable scope = engine.getRuntimeScope(engine.getContext());
        try
        {
            Context.enter();
            Object o = scope.get0(s, scope);
            if (o != null && !(o instanceof UniqueTag)) return o;
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
            
        }finally
        {
            Context.exit();
        }

        //JsAnalyzer an = new JsAnalyzer();
        JsParseResult info = parser.getResult();
      
        Node n = ParserUtils.getASTNodeAtOffset(support.getElement(),
                getRootNode(), offset - 1);
        JsTypeAnalyzer a = new JsTypeAnalyzer(info, getRootNode(), n, offset - 1, offset - 1);
        String type = a.getType(s);
        return type != null ? new JsType(type) : null;
    }

    public Object resolveMethod(String s, int offset)
    {
        //TODO: big work with function/object indexing, doc parsing and stuff 
        return Object.class;
    }

    private PatchedParser getParser()
    {
        if (parser == null)
        {
            // CompilerEnvirons compilerEnv = new CompilerEnvirons();
            // compilerEnv.initFromContext(Context.enter());
            // ErrorReporter compilationErrorReporter = null; // TODO:
            // if (compilationErrorReporter == null)
            // {
            // compilationErrorReporter = compilerEnv.getErrorReporter();
            // }

            // parser = new Parser(compilerEnv, compilationErrorReporter);
            parser = new PatchedParser();
        }
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
                    // Reader r = makeReader(seg, offset, length);
                    parser = getParser();
                    // System.out.println("parser.start()..." + length);
                    // long start = System.currentTimeMillis();
                    // astRoot = parser.parse(r, "<Unknown Source>", 1);
                    parser.parse(seg.toString());
                    astRoot = parser.getResult().getRootNode();
                    // System.out.println("Time: "
                    // + (System.currentTimeMillis() - start));
                    // System.out.println("Root: " + astRoot);

                    support.unMarkErrors();
                    return true;
                }
                catch (ParseException ex)
                {
                    ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
                            stripMsg(ex.getMessage()), -1, // ex.getLineNumber()
                            // - 1,
                            -1// ex.getColumnNumber()
                    );
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

    public JTree getAstTree()
    {
        if (astRoot == null) return null;
        JTree astTree = new JTree();
        JSTreeModel treeModel = new JSTreeModel(astRoot);
        astTree.setModel(treeModel);
        // astTree.setCellRenderer(new JSNodeRenderer());

        return astTree;
    }

    class JSTreeModel implements TreeModel
    {
        Node root = null;
        protected EventListenerList listenerList = new EventListenerList();

        public JSTreeModel(Node t)
        {
            if (t == null) { throw new IllegalArgumentException("root is null"); }
            root = t;
        }

        public Object getChild(Object parent, int index)
        {
            if (parent == null) { return null; }
            Node p = (Node) parent;
            if (!p.hasChildren()) return null;
            Node c = (Node) p.getFirstChild();
            if (index == 0) return c;
            int i = 0;
            while (c != null && i <= index)
            {
                c = (Node) c.getNext();
                i++;
                if (c != null && i == index) return c;
            }
            // return c;
            // if (index >= c.size())
            throw new ArrayIndexOutOfBoundsException("node " + p
                    + " has no child with index: " + index);
            // return c.get(index);
        }

        public int getChildCount(Object parent)
        {
            if (parent == null) { throw new IllegalArgumentException(
                    "root is null"); }
            if (parent instanceof String) return 0;
            Node p = (Node) parent;
            if (!p.hasChildren()) return 0;
            Node n = p.getFirstChild();
            int count = 1;
            if (n == p.getLastChild()) return count;
            while (n != null)
            {
                n = n.getNext();
                if (n != null) count++;
            }
            return count;

        }

        public int getIndexOfChild(Object parent, Object child)
        {
            if (parent == null || child == null) { throw new IllegalArgumentException(
                    "root or child is null"); }
            int i = 0;
            Node p = (Node) parent;
            Node c = (Node) p.getFirstChild();
            while (c != null && c != child)
            {
                c = (Node) c.getNext();
                i++;
            }
            if (c == child) { return i; }
            throw new java.util.NoSuchElementException("node is not a child");
        }

        public Object getRoot()
        {
            return root;
        }

        public boolean isLeaf(Object node)
        {
            if (node == null) { throw new IllegalArgumentException(
                    "node is null"); }
            if (node instanceof String) return true;
            else
            {
                Node t = (Node) node;
                return !t.hasChildren();
            }
        }

        /**
         * Adds a listener for the TreeModelEvent posted after the tree changes.
         * 
         * @see #removeTreeModelListener
         * @param l
         *            the listener to add
         */
        public void addTreeModelListener(TreeModelListener l)
        {
            listenerList.add(TreeModelListener.class, l);
        }

        /**
         * Removes a listener previously added with
         * <B>addTreeModelListener()</B>.
         * 
         * @see #addTreeModelListener
         * @param l
         *            the listener to remove
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
         *         empty array if no tree model listeners are currently
         *         registered
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
         * Notifies all listeners that have registered interest for notification
         * on this event type. The event instance is lazily created using the
         * parameters passed into the fire method.
         * 
         * @param source the node where the tree model has changed @param path
         * the path to the root node
         * 
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
                    ((TreeModelListener) listeners[i + 1])
                            .treeStructureChanged(e);
                }
            }
        }

        /**
         * @param nodeBefore
         * @return
         */
        public TreePath getTreePath(Node node)
        {
            Node[] nodes = getTreePathNodes((Node) getRoot(), node, 0);
            if (nodes == null) return null;
            else
                return new TreePath(nodes);
        }

        public Node[] getTreePathNodes(Node root, Node node, int depth)
        {
            if (node == null) return null;
            depth++;
            Node[] retNodes = null;
            if (node == root)
            {
                retNodes = new Node[depth];
                retNodes[depth - 1] = root;
            }
            else
            {
                int n = getChildCount(root); // .getNumberOfChildren();
                loop: for (int i = 0; i < n; i++)
                {
                    retNodes = getTreePathNodes((Node) getChild(root, i), node,
                            depth);
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
    
    class JsType
    {
        private String type;

        public JsType(String type)
        {
            super();
            this.type = type;
        }

        public String getType()
        {
            return type;
        }
    }
}
