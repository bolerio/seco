/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package seco.notebook.javascript;


import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.mozilla.nb.javascript.CompilerEnvirons;
import org.mozilla.nb.javascript.ContextFactory;
import org.mozilla.nb.javascript.ErrorReporter;
import org.mozilla.nb.javascript.EvaluatorException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.ScriptOrFnNode;

import seco.notebook.csl.DefaultError;
import seco.notebook.csl.GsfUtilities;
import seco.notebook.csl.OffsetRange;
import seco.notebook.csl.ParseException;
import seco.notebook.csl.Severity;


/**
 * Wrapper around JavaScript to parse a buffer into an AST.
 *
 * @todo Idea: If you get a syntax error on the last line, it's probably a missing
 *   "end" much earlier. Go back and look for a method inside a method, and the outer
 *   method is probably missing an end (can use indentation to look for this as well).
 *   Create a quickfix to insert it.
 * @todo Only look for missing-end if there's an unexpected end
 * @todo If you get a "class definition in method body" error, there's a missing
 *   end - prior to the class!
 * 
 * @author Tor Norbye
 */
public class PatchedParser 
{
    /** For unit tests such that they can make sure we didn't have a parser abort */
    static RuntimeException runtimeException;

    private JsParseResult lastResult;
    
    public void parse(String source) throws ParseException 
    {
        Context context = new Context(source);
        final List<DefaultError> errors = new ArrayList<DefaultError>();
        context.errorHandler = new ParseErrorHandler() {
            public void error(DefaultError error) {
                errors.add(error);
            }
        };
        lastResult = parseBuffer(context, Sanitize.NONE);
        lastResult.setErrors(errors);
    }


    public JsParseResult getResult()
    {
        return lastResult;
    }

    /**
     * Creates a new instance of JsParser
     */
    public PatchedParser() {
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

        if (sanitizing == Sanitize.MISSING_END) {
            context.sanitizedSource = context.source + "}";
            int start = context.source.length();
            context.sanitizedRange = new OffsetRange(start, start+4);
            context.sanitizedContents = "";
            return true;
        }

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
                offset = GsfUtilities.getRowStart(doc, offset)-1;
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
                        if (lineEnd >= lineStart+2) {
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

                        context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(lineStart, lineEnd);
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;
                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineStart = GsfUtilities.getRowStart(doc, offset);
                    int lineEnd = offset-1;
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
                        int removeEnd = lineEnd+1;
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
                        } else {
                            // Make sure the line doesn't end with one of the JavaScript keywords
                            // (new, do, etc) - we can't handle that!
                            for (String keyword : JSUtils.JAVASCRIPT_KEYWORDS) { // reserved words are okay
                                if (line.endsWith(keyword)) {
                                    if ("var".equals(keyword)) { // NOI18N
                                        // Special case - see 149226
                                        // Only remove the keyword if it's the end of the line. Otherwise,
                                        // it could have just been typed in front of something and we don't
                                        // want to confuse the parser with "va foo" instead of "var foo"
                                        if (!isLineEnd) {
                                            continue;
                                        }
                                    }
                                    removeChars = 1;
                                    break;
                                }
                            }
                        }
                        
                        if (removeChars == 0) {
                            return false;
                        }

                        int removeStart = removeEnd-removeChars;

                        sb.append(doc.substring(0, removeStart));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (removeEnd < doc.length()) {
                            sb.append(doc.substring(removeEnd, doc.length()));
                        }
                        assert sb.length() == doc.length();

                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
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


    public static void dumpTree(Node node) {
        dumpTree(node, 0);
    }
    
    private static void dumpTree(Node node, int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("    ");
        }
        System.out.println(node.toString());
        if (node.hasChildren()) {
            Node curr = node.getFirstChild();
            while (curr != null) {
                dumpTree(curr, depth+1);
                curr = curr.getNext();
            }
        }
    }

       
    @SuppressWarnings("fallthrough")
    private JsParseResult sanitize(final Context context,
        final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return createParseResult(context.source, null);

        case NONE:

            // We've currently tried with no sanitization: try first level
            // of sanitization - removing dots/colons at the edited offset.
            // First try removing the dots or double colons around the failing position
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.EDITED_DOT);
            }

        // Fall through to try the next trick
        case EDITED_DOT:

            // We've tried editing the caret location - now try editing the error location
            // (Don't bother doing this if errorOffset==caretOffset since that would try the same
            // source as EDITED_DOT which has no better chance of succeeding...)
            if (context.errorOffset != -1 && context.errorOffset != context.caretOffset) {
                return parseBuffer(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

            // We've tried removing dots - now try removing the whole line at the error position
            if (context.errorOffset != -1) {
                return parseBuffer(context, Sanitize.ERROR_LINE);
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
                return parseBuffer(context, Sanitize.EDITED_LINE);
            }

        // Fall through to try the next trick
        case EDITED_LINE:
            return parseBuffer(context, Sanitize.MISSING_END);
            
        // Fall through for default handling
        case MISSING_END:
        default:
            // We're out of tricks - just return the failed parse result
            return createParseResult(context.source, null);
        }
    }

    protected void notifyError(Context context, String message, String sourceName, int line,
                           String lineSource, int lineOffset, Sanitize sanitizing, Severity severity, String key, Object params) {
        
        int offset = context.parser.getTokenStream().getBufferOffset();
        
        if ("msg.unexpected.eof".equals(key) && offset > 0) { // NOI18N
            // The offset should be within the source, not at the EOF - in embedded files this
            // would cause me to not be able to compute the position for example
            offset--;
        }

//        if (offset != getOffset(context, line, lineOffset)) {
//            assert offset == getOffset(context, line, lineOffset) : " offset=" + offset + " and computed offset=" + getOffset(context,line,lineOffset) + " and line/lineOffset = " + line + "/" + lineOffset;
//        }
        
        DefaultError error =
            new DefaultError(key, message, null, 
                offset, offset, severity);
        if (params != null) {
            if (params instanceof Object[]) {
                error.setParameters((Object[]) params);
            } else {
                error.setParameters(new Object[] { params });
            }
        }

        if (context.errorHandler != null) {
            context.errorHandler.error(error);
        }

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = offset;
        }
    }

    protected JsParseResult parseBuffer(final Context context, final Sanitize sanitizing) {
        boolean sanitizedSource = false;
        String source = context.source;
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing);

            if (ok) {
                assert context.sanitizedSource != null;
                sanitizedSource = true;
                source = context.sanitizedSource;
            } else {
                // Try next trick
                return sanitize(context, sanitizing);
            }
        }

        org.mozilla.nb.javascript.Parser parser = createParser(context, sanitizedSource, sanitizing);

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = -1;
        }

        int lineno = 0;
        ScriptOrFnNode root = null;

        try {
          root = parser.parse(source, "unknown source", lineno);
        } catch (IllegalStateException ise) {
            // See issue #128983 for a way to get the compiler to assert for example
            runtimeException = ise;
            ise.printStackTrace();
            //throw ise;
        } catch (RuntimeException re) {
            //notifyError(context, message, sourceName, line, lineSource, lineOffset, sanitizing, Severity.WARNING, "", null);
            // XXX TODO - record this somehow
            runtimeException = re;
            re.printStackTrace();
            //throw re;
        }
        
        if (root != null) {
            setParentRefs(root, null);
            context.sanitized = sanitizing;
            //AstRootElement rootElement = new AstRootElement(context.file.getFileObject(), root, result);
           //AstNodeAdapter ast = new AstNodeAdapter(null, root);
            JsParseResult r = createParseResult(context.source, root);
            r.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
            r.setSource(source);
            return r;
        } else {
            return sanitize(context, sanitizing);
        }
    }

    protected org.mozilla.nb.javascript.Parser createParser(final Context context, boolean sanitizedSource, final Sanitize sanitizing) {
        final boolean ignoreErrors = sanitizedSource;
        
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        ErrorReporter errorReporter =
            new ErrorReporter() {
                public void error(String message, String sourceName, int line,
                           String lineSource, int lineOffset, String id, Object params) {
                    if (!ignoreErrors) {
                        notifyError(context, message, sourceName, line, lineSource, lineOffset, sanitizing, Severity.ERROR, id, params);
                    }
                }

                public EvaluatorException runtimeError(String message, String sourceName,
                                                int line, String lineSource,
                                                int lineOffset) {
                    if (!ignoreErrors) {
                        notifyError(context, message, sourceName, line, lineSource, lineOffset, sanitizing, Severity.ERROR, "", null);
                    }
                    return null;
                }

                public void warning(String message, String sourceName, int line,
                             String lineSource, int lineOffset, String id, Object params) {
                    if (!ignoreErrors) {
                      notifyError(context, message, sourceName, line, lineSource, lineOffset, sanitizing, Severity.WARNING, id, params);
                    }
                }
            };

        // XXX What do I set here: compilerEnv.setReservedKeywordAsIdentifier();

        RhinoContext ctx = new RhinoContext();
        compilerEnv.initFromContext(ctx);
        
        compilerEnv.setErrorReporter(errorReporter);

       // compilerEnv.setLanguageVersion(targetVersion);

        boolean e4x = true;
        compilerEnv.setXmlAvailable(e4x);
        compilerEnv.setStrictMode(true);
        compilerEnv.setGeneratingSource(false);
        compilerEnv.setGenerateDebugInfo(false);
        // We have a quickfix which lets you turn these warnings off or turn them
        // to errors
        compilerEnv.setReservedKeywordAsIdentifier(true);

        
        // The parser is NOT used for parsing here, but the Rhino scanner
        // calls into the parser for error messages. So we register our own error
        // handler for the parser and pass it into the tokenizer to handle errors.
        org.mozilla.nb.javascript.Parser parser;
        parser = new org.mozilla.nb.javascript.Parser(compilerEnv, errorReporter);
        context.parser = parser;

        return parser;
    }

    private JsParseResult createParseResult(String source, Node rootNode) {
        if (rootNode != null) {
            return new JsParseResult(this, source, rootNode);
        } else {
            return new JsParseResult(this, source, null);
        }
    }
    
    /**
     * The Rhino Node tree doesn't include parent references. That's generally a good
     * thing - it means they can reuse subtrees etc.
     * But it makes it harder for me to work with the AST - in code completion, I want
     * to start in a subtree and walk out etc. Rather than needing to keep a stack of
     * parent pointers, I simply add my own parent pointers. This is fine because Rhino
     * doesn't actually reuse any subtrees (and I enforce that by checking that when I
     * initialize the parent pointers, they are always null (e.g. not set twice)
     */
    private void setParentRefs(Node node, Node parent) {
       node.setParentNode(parent);

       if (node.hasChildren()) {
           Node curr = node.getFirstChild();
           while (curr != null) {
               setParentRefs(curr, node);
               curr = curr.getNext();
           }
       }
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
        /** Attempt to add an "end" to the end of the buffer to make it compile */
        MISSING_END,
    }

    /** Parsing context */
    public static class Context {
      
        private /*final*/ String source;
        private /*final*/ int caretOffset;
        
        private org.mozilla.nb.javascript.Parser parser;
        private ParseErrorHandler errorHandler;
        private int errorOffset;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private Sanitize sanitized = Sanitize.NONE;
        
        public Context(String source) {
          
            this.source = source;
            this.caretOffset = -1; //TODO:???
                //GsfUtilities.getLastKnownCaretOffset(snapshot, event);
        }
        
        public OffsetRange getSanitizedRange() {
            return sanitizedRange;
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
    } // End of Context class

    private static interface ParseErrorHandler {
        void error(DefaultError error);
    }

   private static final class RhinoContext extends org.mozilla.nb.javascript.Context {
        public RhinoContext() {
            super(ContextFactory.getGlobal());
        }
    } // End of RhinoContext
}
