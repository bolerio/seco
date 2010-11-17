package seco.langs.python;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.BadLocationException;

import org.python.antlr.runtime.ANTLRStringStream;
import org.python.antlr.runtime.BaseRecognizer;
import org.python.antlr.runtime.BitSet;
import org.python.antlr.runtime.CommonToken;
import org.python.antlr.runtime.CommonTokenStream;
import org.python.antlr.runtime.IntStream;
import org.python.antlr.runtime.Lexer;
import org.python.antlr.runtime.MismatchedTokenException;
import org.python.antlr.runtime.RecognitionException;

import org.python.antlr.BaseParser;
import org.python.antlr.ListErrorHandler;
import org.python.antlr.ParseException;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonTokenSource;
import org.python.antlr.PythonTree;
import org.python.antlr.PythonTreeAdaptor;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.antlr.runtime.ANTLRReaderStream;
import org.python.antlr.runtime.CharStream;

import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import seco.langs.python.jsr223.PyScriptEngine;
import seco.notebook.csl.GsfUtilities;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.syntax.completion.NBParser.ParserRunnable;
import seco.util.SegmentCache;

public class PythonParser extends NBParser
{
    PyScriptEngine engine;
    ParserRunnable parserRunnable;
    PythonTree parseTree;

    public PythonParser(final ScriptSupport support)
    {
        super(support);
        engine = (PyScriptEngine) support.getDocument()
                .getEvaluationContext().getEngine("python");
    }

    @Override
    public NBParser.ParserRunnable getParserRunnable()
    {
        if (parserRunnable != null) return parserRunnable;
        return parserRunnable = new NBParser.ParserRunnable() {
            public boolean doJob()
            {
                Segment seg = SegmentCache.getSharedInstance().getSegment();
                try
                {
                    Element el = support.getElement();
                    int offset = el.getStartOffset();
                    int length = el.getEndOffset() - el.getStartOffset();
                    support.getDocument().getText(offset, length, seg);
                    parseTree = parse(makeReader(seg, offset, length), "NONAME");
                    return true;
                }
                catch (Throwable e)
                {

                    // ScriptSupport.ErrorMark mark = new
                    // ScriptSupport.ErrorMark(
                    // se.getMessage(), line, col);
                    // support.markError(mark);

                    return false;
                }
                finally
                {
                    SegmentCache.getSharedInstance().releaseSegment(seg);
                }
            }
        };
    }
    
    static Throwable runtimeException;

    static {
        org.python.core.PySystemState.initialize();
    }
    
    private Reader makeReader(Segment seg, int offset, int length)
    {
        CharArrayReader r = (seg.array.length > length) ? new CharArrayReader(
                seg.array, offset, length) : new CharArrayReader(seg.array);
        return r;
    }
    
    public PythonTree parse(Reader reader, String fileName) throws Exception {
        return file_input(new ANTLRReaderStream(reader), fileName);
    }

    public mod file_input(CharStream charStream, String fileName) throws RecognitionException {
        ListErrorHandler eh = new ListErrorHandler();
        mod tree = null;
        PythonLexer lexer = new BaseParser.PyLexer(charStream);
        lexer.setErrorHandler(eh);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.discardOffChannelTokens(true);
        PythonTokenSource indentedSource = new PythonTokenSource(tokens, fileName);
        tokens = new CommonTokenStream(indentedSource);
        org.python.antlr.PythonParser parser = new org.python.antlr.PythonParser(tokens);
        parser.setTreeAdaptor(new PythonTreeAdaptor());
        parser.setErrorHandler(eh);
        org.python.antlr.PythonParser.file_input_return r = parser.file_input();
        tree = (mod)r.getTree();
        return tree;
    }

    public PythonParserResult parse(final Context context, Sanitize sanitizing) throws Exception {
        boolean sanitizedSource = false;
        String sourceCode = context.source;
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing);
            assert context.sanitizedSource != null;
            sanitizedSource = true;
            sourceCode = context.sanitizedSource;
            
        }
        final String source = sourceCode;

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = -1;
        }

       // final List<Error> errors = new ArrayList<Error>();
        try {
            String fileName = "<NONAME>";
            String charset = null;
            final boolean ignoreErrors = sanitizedSource;
            ListErrorHandler errorHandler = new ListErrorHandler() {
                @Override
                public void error(String message, PythonTree t) {
                   // errors.add(new DefaultError(null, message, null, file.getFileObject(), t.getCharStartIndex(), t.getCharStopIndex(), Severity.ERROR));
                    super.error(message, t);
                }

                @Override
                public expr errorExpr(PythonTree t) {
                    return super.errorExpr(t);
                }

                @Override
                public mod errorMod(PythonTree t) {
                    return super.errorMod(t);
                }

                @Override
                public slice errorSlice(PythonTree t) {
                    return super.errorSlice(t);
                }

                @Override
                public stmt errorStmt(PythonTree t) {
                    return super.errorStmt(t);
                }

                @Override
                public boolean mismatch(BaseRecognizer br, IntStream input, int ttype, BitSet follow) {
                    return super.mismatch(br, input, ttype, follow);
                }

                @Override
                public Object recoverFromMismatchedToken(BaseRecognizer br, IntStream input, int ttype, BitSet follow) {
                    MismatchedTokenException mt = new MismatchedTokenException(ttype, input);
                    String message = br.getErrorMessage(mt, br.getTokenNames());
                    if (mt.line >= 1) {
                        int lineOffset = findLineOffset(context.source, mt.line-1);
                        if (mt.charPositionInLine > 0) {
                            lineOffset += mt.charPositionInLine;
                        }
                        int start = lineOffset;//t.getCharStartIndex();
                        int stop = lineOffset;//t.getCharStopIndex();
                       // errors.add(new DefaultError(null, message, null, file.getFileObject(), start, stop, Severity.ERROR));
                    }
                    return super.recoverFromMismatchedToken(br, input, ttype, follow);
                }

                @Override
                public void recover(Lexer lex, RecognitionException re) {
                    super.recover(lex, re);
                }

                @Override
                public void recover(BaseRecognizer br, IntStream input, RecognitionException re) {
                    super.recover(br, input, re);
                }

                @Override
                public void reportError(BaseRecognizer br, RecognitionException re) {
                    if (!ignoreErrors) {
                        String message = br.getErrorMessage(re, br.getTokenNames());
                        if (message == null || message.length() == 0) {
                            message = re.getMessage();
                        }
                        if (message == null) {
                            //message = re.getUnexpectedType();
                            message = re.toString();
                        }
                        int start = re.index;

                        // Try to find the line offset. re.index doesn't do the trick.
                        start = 0; //????PythonUtils.getOffsetByLineCol(source, re.line - 1, 0); // -1: 0-based
                        int end = start;
                        if (re.charPositionInLine > 0) {
                            try {
                                end = GsfUtilities.getRowLastNonWhite(source, start) + 1;
                                start += re.charPositionInLine;
                                if (end < start) {
                                    end = start;
                                }
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                                end = start;
                            }
                            if (end == 0) {
                                end = start;
                            }
                        }

                        // Some errors have better offsets if we look at the token stream
                        if (re instanceof MismatchedTokenException) {
                            MismatchedTokenException m = (MismatchedTokenException)re;
                            if (m.token != null) {
                                if (m.token instanceof org.python.antlr.runtime.CommonToken) {
                                    CommonToken token = (org.python.antlr.runtime.CommonToken)m.token;
                                    start = token.getStartIndex();
                                    end = token.getStopIndex();
                                }
                            }
                        }

                        if (start > source.length()) {
                            start = source.length();
                            end = start;
                        }

                        //errors.add(new DefaultError(null, message, null, file.getFileObject(), start, end, Severity.ERROR));

                        super.reportError(br, re);
                    }
                }
            };

            PythonLexer lexer = new BaseParser.PyLexer(new ANTLRStringStream(sourceCode));
            lexer.setErrorHandler(errorHandler);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.discardOffChannelTokens(true);
            PythonTokenSource indentedSource = new PythonTokenSource(tokens, fileName);
            tokens = new CommonTokenStream(indentedSource);
            org.python.antlr.PythonParser parser = new org.python.antlr.PythonParser(tokens);
            parser.setTreeAdaptor(new PythonTreeAdaptor());
            parser.setErrorHandler(errorHandler);
            org.python.antlr.PythonParser.file_input_return r = parser.file_input();
            PythonTree t = (PythonTree)r.getTree();
            PythonParserResult result = createParseResult(t, true);
//            for (Error error : errors) {
//                result.addError(error);
//            }

 //           result.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
 //           result.setSource(sourceCode);

            return result;
        } catch (ParseException pe) {
            if (sanitizing == Sanitize.NONE) {
                PythonParserResult sanitizedResult = sanitize(context, sanitizing);
                if (sanitizedResult.isValid()) {
                    return sanitizedResult;
                } else {
                    int offset = pe.index;
                    assert offset >= 0;
                    String desc = pe.getLocalizedMessage();
                    if (desc == null) {
                        desc = pe.getMessage();
                    }
                    //DefaultError error = new DefaultError(null /*key*/, desc, null, file.getFileObject(), offset, offset, Severity.ERROR);
                    PythonParserResult parserResult = createParseResult(null, false);
                    //parserResult.addError(error);
//                    for (Error e : errors) {
//                        parserResult.addError(e);
//                    }

                    return parserResult;
                }
            } else {
                return sanitize(context, sanitizing);
            }
        } catch (NullPointerException e) {
            String fileName = "";
          //  Exceptions.attachMessage(e, "Was parsing " + fileName);
            return createParseResult(null, false);
        } catch (Throwable t) {
            runtimeException = t;
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0 && stackTrace[0].getClassName().equals("org.python.antlr.runtime.tree.RewriteRuleElementStream")) {
                // This is issue 150921
                // Don't bug user about it -- we already know
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Encountered issue #150921", t);
            } else {
                //Exceptions.attachMessage(t, "Was parsing " + FileUtil.getFileDisplayName(file.getFileObject()));
                t.printStackTrace();
            }
            return createParseResult(null, false);
        }
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

   
    private PythonParserResult createParseResult(PythonTree rootNode, boolean isValid) {
        return new PythonParserResult(rootNode, this, isValid);
    }

    @SuppressWarnings("fallthrough")
    private PythonParserResult sanitize(final Context context,
            final Sanitize sanitizing) throws Exception {

        switch (sanitizing) {
        case NEVER:
            return createParseResult(null, false);

        case NONE:
            if (context.caretOffset != -1) {
                return parse(context, Sanitize.EDITED_DOT);
            }

        case EDITED_DOT:
            // We've tried removing whitespace around the edit location
            // Fall through to try parsing with removing stuff around error location
            // (Don't bother doing this if errorOffset==caretOffset since that would try the same
            // source as EDITED_DOT which has no better chance of succeeding...)
            if (context.errorOffset != -1 && context.errorOffset != context.caretOffset) {
                return parse(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

            // We've tried removing dots - now try removing the whole line at the error position
            if (context.errorOffset != -1) {
                return parse(context, Sanitize.ERROR_LINE);
            }

        // Fall through to try the next trick
        case ERROR_LINE:

            // Messing with the error line didn't work - we could try "around" the error line
            // but I'm not attempting that now.
            // Finally try removing the whole line around the user editing position
            // (which could be far from where the error is showing up - but if you're typing
            // say a new "def" statement in a class, this will show up as an error on a mismatched
            // "end" statement rather than here
            if (context.caretOffset != -1) {
                return parse(context, Sanitize.EDITED_LINE);
            }

        // Fall through for default handling
        case EDITED_LINE:
        default:
            // We're out of tricks - just return the failed parse result
            return createParseResult(null, false);
        }
    }

    /**
     * Try cleaning up the source buffer around the current offset to increase
     * likelihood of parse success. Initially this method had a lot of
     * logic to determine whether a parse was likely to fail (e.g. invoking
     * the isEndMissing method from bracket completion etc.).
     * However, I am now trying a parse with the real source first, and then
     * only if that fails do I try parsing with sanitized source. Therefore,
     * this method has to be less conservative in ripping out code since it
     * will only be used when the regular source is failing.
     *
     * @todo Automatically close current statement by inserting ";"
     * @todo Handle sanitizing "new ^" from parse errors
     * @todo Replace "end" insertion fix with "}" insertion
     */
    private boolean sanitizeSource(Context context, Sanitize sanitizing) {
        int offset = context.caretOffset;

        // Let caretOffset represent the offset of the portion of the buffer we'll be operating on
        if ((sanitizing == Sanitize.ERROR_DOT) || (sanitizing == Sanitize.ERROR_LINE)) {
            offset = context.errorOffset;
        }

        // Don't attempt cleaning up the source if we don't have the buffer position we need
        if (offset == -1) {
            return false;
        }

        // The user might be editing around the given caretOffset.
        // See if it looks modified
        // Insert an end statement? Insert a } marker?
        String doc = context.source;
        if (offset > doc.length()) {
            return false;
        }

        try {
            // Sometimes the offset shows up on the next line
            if (GsfUtilities.isRowEmpty(doc, offset) || GsfUtilities.isRowWhite(doc, offset)) {
                offset = GsfUtilities.getRowStart(doc, offset) - 1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(GsfUtilities.isRowEmpty(doc, offset) || GsfUtilities.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = GsfUtilities.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1) {
                        lineEnd++; // lineEnd is exclusive, not inclusive
                        StringBuilder sb = new StringBuilder(doc.length());
                        int lineStart = GsfUtilities.getRowStart(doc, offset);
                        if (lineEnd >= lineStart + 2) {
                            sb.append(doc.substring(0, lineStart));
                            sb.append("//");
                            int rest = lineStart + 2;
                            if (rest < doc.length()) {
                                sb.append(doc.substring(rest, doc.length()));
                            }
                        } else {
                            // A line with just one character - can't replace with a comment
                            // Just replace the char with a space
                            sb.append(doc.substring(0, lineStart));
                            sb.append(" ");
                            int rest = lineStart + 1;
                            if (rest < doc.length()) {
                                sb.append(doc.substring(rest, doc.length()));
                            }

                        }

                        assert sb.length() == doc.length();

                       // context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(lineStart, lineEnd);
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;
                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineStart = GsfUtilities.getRowStart(doc, offset);
                    int lineEnd = offset - 1;
                    while (lineEnd >= lineStart && lineEnd < doc.length()) {
                        if (!Character.isWhitespace(doc.charAt(lineEnd))) {
                            break;
                        }
                        lineEnd--;
                    }
                    if (lineEnd > lineStart) {
                        StringBuilder sb = new StringBuilder(doc.length());
                        String line = doc.substring(lineStart, lineEnd + 1);
                        int removeChars = 0;
                        int removeEnd = lineEnd + 1;
                        boolean isLineEnd = GsfUtilities.getRowLastNonWhite(context.source, lineEnd) <= lineEnd;

                        if (line.endsWith(".")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith("(")) { // NOI18N
                            if (isLineEnd) {
                                removeChars = 1;
                            }
                        } else if (line.endsWith(",")) { // NOI18N                            removeChars = 1;
                            if (!isLineEnd) {
                                removeChars = 1;
                            }
                        } else if (line.endsWith(", ")) { // NOI18N
                            if (!isLineEnd) {
                                removeChars = 2;
                            }
                        } else if (line.endsWith(",)")) { // NOI18N
                            // Handle lone comma in parameter list - e.g.
                            // type "foo(a," -> you end up with "foo(a,|)" which doesn't parse - but
                            // the line ends with ")", not "," !
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd--;
                        } else if (line.endsWith(", )")) { // NOI18N
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd -= 2;
                        } else if (line.endsWith(" def") && isLineEnd) { // NOI18N
                            removeChars = 3;
                        } else {
//                            // Make sure the line doesn't end with one of the JavaScript keywords
//                            // (new, do, etc) - we can't handle that!
//                            for (String keyword : PythonUtils.PYTHON_KEYWORDS) { // reserved words are okay
//                                if (line.endsWith(keyword)) {
//                                    if ("print".equals(keyword)) { // NOI18N
//                                        // Only remove the keyword if it's the end of the line. Otherwise,
//                                        // it could have just been typed in front of something (e.g. inserted a print) and we don't
//                                        // want to confuse the parser with "va foo" instead of "var foo"
//                                        if (!isLineEnd) {
//                                            continue;
//                                        }
//                                    }
//                                    removeChars = 1;
//                                    break;
//                                }
//                            }
                        }

                        if (removeChars == 0) {
                            return false;
                        }

                        int removeStart = removeEnd - removeChars;

                        sb.append(doc.substring(0, removeStart));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (removeEnd < doc.length()) {
                            sb.append(doc.substring(removeEnd, doc.length()));
                        }
                        assert sb.length() == doc.length();

                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }

        return false;
    }

    private static int findLineOffset(String source, int line) {
        int offset = -1;
        for (int i = 0; i < line; i++) {
            offset = source.indexOf("\n", offset+1);
            if (offset == -1) {
                return source.length();
            }
        }

        return Math.min(source.length(), offset+1);
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER,
        /** Perform no sanitization */
        NONE,
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT,
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT,
        /** Try to cut out the error line */
        ERROR_LINE,
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
    }

    /** Parsing context */
    public static class Context {
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
     

        public Context(String source, int caretOffset)
        {
            this.source = source;
            this.caretOffset = caretOffset;
           
            if (caretOffset != -1) {
                sanitized = Sanitize.EDITED_DOT;
            }
        }

        public Sanitize getSanitized() {
            return sanitized;
        }

        public String getSanitizedSource() {
            return sanitizedSource;
        }

        public int getErrorOffset() {
            return errorOffset;
        }
    }
    
    public JTree getAstTree()
    {
        if (parseTree == null) return null;
        JTree astTree = new JTree();
        PyTreeModel treeModel = new PyTreeModel(parseTree);
        astTree.setModel(treeModel);
        // astTree.setCellRenderer(new JSNodeRenderer());

        return astTree;
    }

    class PyTreeModel implements TreeModel
    {
        PythonTree root = null;
        protected EventListenerList listenerList = new EventListenerList();

        public PyTreeModel(PythonTree t)
        {
            if (t == null) { throw new IllegalArgumentException("root is null"); }
            root = t;
        }

        public Object getChild(Object parent, int index)
        {
            if (parent == null) { return null; }
            PythonTree p = (PythonTree) parent;
            if (p.getChildCount() == 0) return null;
            if (p.getChildCount() <= index)
                throw new ArrayIndexOutOfBoundsException("node " + p
                        + " has no child with index: " + index);
            return p.getChild(index);
        }

        public int getChildCount(Object parent)
        {
            if (parent == null) { throw new IllegalArgumentException(
                    "root is null"); }
            return ((PythonTree) parent).getChildCount();
        }

        public int getIndexOfChild(Object parent, Object child)
        {
            if (parent == null || child == null) { throw new IllegalArgumentException(
                    "root or child is null"); }
            int i = 0;
            PythonTree p = (PythonTree) parent;
            int index = p.getChildren().indexOf(child);
            if(index > -1) return index;
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
            PythonTree t = (PythonTree) node;
            return t.getChildCount() == 0;
        }

        public void addTreeModelListener(TreeModelListener l)
        {
            listenerList.add(TreeModelListener.class, l);
        }
        
        public void removeTreeModelListener(TreeModelListener l)
        {
            listenerList.remove(TreeModelListener.class, l);
        }

        public TreeModelListener[] getTreeModelListeners()
        {
            return (TreeModelListener[]) listenerList
                    .getListeners(TreeModelListener.class);
        }

        public void valueForPathChanged(TreePath path, Object newValue)
        {
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
        public TreePath getTreePath(PythonTree node)
        {
            PythonTree[] nodes = getTreePathNodes((PythonTree) getRoot(), node, 0);
            if (nodes == null) return null;
            else
                return new TreePath(nodes);
        }

        public PythonTree[] getTreePathNodes(PythonTree root, PythonTree node, int depth)
        {
            if (node == null) return null;
            depth++;
            PythonTree[] retNodes = null;
            if (node == root)
            {
                retNodes = new PythonTree[depth];
                retNodes[depth - 1] = root;
            }
            else
            {
                int n = getChildCount(root); // .getNumberOfChildren();
                loop: for (int i = 0; i < n; i++)
                {
                    retNodes = getTreePathNodes((PythonTree) getChild(root, i), node,
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
}
