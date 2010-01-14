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
import java.util.ListIterator;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import org.mozilla.nb.javascript.CompilerEnvirons;
import org.mozilla.nb.javascript.ContextFactory;
import org.mozilla.nb.javascript.ScriptOrFnNode;
import org.mozilla.nb.javascript.ErrorReporter;
import org.mozilla.nb.javascript.EvaluatorException;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
//import org.netbeans.modules.csl.api.EditHistory;
//import org.netbeans.modules.csl.spi.GsfUtilities;
//import org.netbeans.modules.csl.spi.ParserResult;
//import org.netbeans.modules.parsing.api.Source;
//import org.netbeans.modules.parsing.spi.Parser;
//import org.netbeans.modules.parsing.spi.SourceModificationEvent;
//import org.openide.filesystems.FileObject;

import seco.notebook.csl.DefaultError;
import seco.notebook.csl.ElementHandle;
import seco.notebook.csl.GsfUtilities;
import seco.notebook.csl.OffsetRange;
import seco.notebook.csl.ParseException;
import seco.notebook.csl.ParserResult;
import seco.notebook.csl.Severity;


/**
 * Wrapper around JRuby to parse a buffer into an AST.
 *
 * @todo Rename to JsParser for symmetry with RubyLexer
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
public class JsParser 
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
    public JsParser() {
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
                            for (String keyword : JsUtils.JAVASCRIPT_KEYWORDS) { // reserved words are okay
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

//    private final int lexToAst(Snapshot source, int offset) {
//        if (source != null) {
//            return source.getEmbeddedOffset(offset);
//        }
//
//        return offset;
//    }
//
//    private final int astToLex(Snapshot source, int offset) {
//        if (source != null) {
//            return source.getOriginalOffset(offset);
//        }
//
//        return offset;
//    }

//    public JsParseResult parse(Snapshot snapshot, EditHistory history, ParserResult prevResult) {
//        if (history == null) {
//            return null;
//        }
//
//       // if ("json".equals(snapshot.getSource().getFileObject().getExt())) { // NOI18N
//      //      return null;
//       // }
//
//        JsParseResult previousResult = AstUtilities.getParseResult(prevResult);
//        if (previousResult == null) {
//            return null;
//        }
//
//        Node previousRoot = previousResult.getRootNode();
//        if (previousRoot == null) {
//            return null;
//        }
//
//        if (history.getStart() == -1) {
//            // No edits - just reuse result?
//            return previousResult;
//        }
//
//        // TODO:
//        // Look at the EditHistory and determine if we're inside a function.
//        // Look up the corresponding old node. Then check to see if it fully
//        // contains the change-range.
//        // If it does, parse JUST that part of the source (do the sanitizing thing)
//        // If that succeeds, patch the node tree as appropriate.
//        // If that fails, consider whether I want to update the parse tree.
//        // In any case, I can now just reuse the old parse tree with the offsets
//        // updated, since I have them!!!
//
//        // The various services (hints provider, semantic highlighter, etc.) can stash
//        //   their data in the parser result since the old one is passed back.
//        //  They then need to update the data conditionally based on the edit positions
//        //  and the new offsets. For example, the semantic highlighter should remove
//        //  all regions in the old function's range, and set the new ones!
//
//        int startAst = lexToAst(previousResult.getSnapshot(), history.getStart());
//        if (startAst == -1) {
//            return null;
//        }
//        AstPath path = new AstPath(previousRoot, startAst);
//        ListIterator<Node> iterator = path.leafToRoot();
//        FunctionNode oldFunction = null;
//        while (iterator.hasNext()) {
//            Node node = iterator.next();
//            if (node.getType() == Token.FUNCTION) {
//                oldFunction = (FunctionNode) node;
//                break;
//            }
//        }
//        if (oldFunction == null) {
//            return null;
//        }
//
//        final int oldFunctionStart = oldFunction.getSourceStart();
//        final int oldFunctionEnd = oldFunction.getSourceEnd();
//
//        // Make sure the edits were all inside the old function
//        int originalEndAst = lexToAst(previousResult.getSnapshot(), history.getOriginalEnd());
//        if (startAst == -1) {
//            return null;
//        }
//
//        if (originalEndAst > oldFunctionEnd) {
//            return null;
//        }
//
//        int oldFuncEndLex = astToLex(previousResult.getSnapshot(), oldFunctionEnd);
//        if (oldFuncEndLex == -1) {
//            return null;
//        }
//        int newFunctionEndLex = history.convertOriginalToEdited(oldFuncEndLex);
//        int newFunctionEnd = lexToAst(previousResult.getSnapshot(), newFunctionEndLex);
//        if (newFunctionEnd == -1) {
//            return null;
//        }
//
//        Context context = new Context(snapshot);
//
//        // This should not happen unless there is an error in the EditHistory...
//        int docLength = context.source.length();
//        if (newFunctionEnd > docLength || newFunctionEnd <= 0) {
//            return null;
//        }
//        if (context.source.charAt(newFunctionEnd-1) != '}') {
//            return null;
//        }
//
//        if (oldFunctionStart > newFunctionEnd) {
//            return null;
//        }
//
//        String source = context.source.substring(oldFunctionStart, newFunctionEnd);
//        Sanitize sanitizing = Sanitize.NEVER;
//        boolean sanitizedSource = false;
//        org.mozilla.nb.javascript.Parser parser = createParser(context, sanitizedSource, sanitizing);
//        context.parser = parser;
//
//        if (sanitizing == Sanitize.NONE) {
//            context.errorOffset = -1;
//        }
//
//        int lineno = 0;
//
////todo: merge conflict, commented out
////            try {
////                final List<Error> allErrors = new ArrayList<Error>();
////                // Absorb all errors, don't pass to the main listeners. We'll
////                // adjust the errors later.
////                context.listener = new ParseListener() {
////
////                    public void started(ParseEvent e) {
////                    }
////
////                    public void finished(ParseEvent e) {
////                    }
////
////                    public void error(Error e) {
////                        DefaultError error = (DefaultError)e;
////                        int start = error.getStartPosition();
////                        int end = error.getEndPosition();
////                        if (start != -1) {
////                            // Fix the source offsets: I'm compiling
////                            // just the "function { }" part so the source offsets
////                            // are all wrong; add in the location of function
////                            start += oldFunctionStart;
////                        }
////                        if (end != -1) {
////                            end += oldFunctionStart;
////                        }
////                        error.setOffsets(start, end);
////                        allErrors.add(error);
////                    }
////
////                    public void exception(Exception e) {
////                    }
////                };
////
////
////                FunctionNode newFunction = parser.parseFunction(source, context.file.getNameExt(), lineno);
////                if (newFunction == null) {
////                    // Perform some basic cleanup of trailing dots, commas, etc.
////                    if (context.caretOffset >= oldFunctionStart && context.caretOffset <= newFunctionEnd) {
////                        String oldSource = context.source;
////                        int oldCaretOffset = context.caretOffset;
////                        context.source = source;
////                        context.caretOffset -= oldFunctionStart;
////                        boolean ok = sanitizeSource(context, Sanitize.EDITED_DOT);
////                        context.source = oldSource;
////                        context.caretOffset = oldCaretOffset;
////
////                        if (ok) {
////                            assert context.sanitizedSource != null;
////                            sanitizedSource = true;
////                            context.sanitized = Sanitize.EDITED_DOT;
////                            parser = createParser(context, source, sanitizedSource, sanitizing);
////                            newFunction = parser.parseFunction(context.sanitizedSource, context.file.getNameExt(), lineno);
////                        }
////                    }
////                }
////
////                if (newFunction == null) {
////                    return null;
////                }
////                // TODO:
////                // (0) Look up the AST node corresponding to the edit region. See if the damage
////                //    region goes beyond the function limit (uh oh, if you add inside the method body
////                //    the damage region will be new
////                // (0) Find out if the change happened inside a function, and if so, which one
////                // (1) Get the exact source code for the given function.
////                //    If the edit list is accurate, then the delta should let me access the }
////                //    -directly- !
////                // (1) Parse
////                // (2) Surgery on the AST to insert the node
////                // (3) Update all the offsets in the AST
////                // (4) Return a new parser result, or consider updating cached info in the parser result!
////                // (5) Deal with the AstNodeAdapter somehow? Ah, don't reuse that!
////                // Update offsets
////                // set current/modified function context in the result object
////                // update the error set! (notifyError) Note - update entire function body range, not just
////                //   the modified range!
////
////                // Adjust the offsets in function nodes: They should be relative to
////                // where the old function started in the document
////                adjustOffsets(newFunction, 0, oldFunctionStart);
////
////                // Adjust the offsets in the rest of the AST - the offsets up the chain as well, not just the following node.
////                int limit = lexToAst(translatedSource, history.getOriginalEnd());
////                if (limit == -1) {
////                    return null;
////                }
////                int delta = history.getSizeDelta();
////
////                adjustOffsets(root, limit, delta);
////
////                setParentRefs(newFunction, oldFunction.getParentNode());
////                oldFunction.getParentNode().replaceChild(oldFunction, newFunction);
////
////                if (oldFunction.labelNode != null) {
////                    newFunction.labelNode = oldFunction.labelNode;
////                    oldFunction.labelNode.setLabelledNode(newFunction);
////                }
////
////                context.sanitized = sanitizing;
////                //AstRootElement rootElement = new AstRootElement(context.file.getFileObject(), root, result);
////
////                AstNodeAdapter ast = new AstNodeAdapter(null, root);
////                JsParseResult r = createParseResult(context.file, root, ast /*, realRoot, result*/);
////
////                JsParseResult.IncrementalParse incrementalInfo =
////                        new JsParseResult.IncrementalParse(oldFunction, newFunction,
////                            oldFunctionStart, limit, delta, previousResult);
////                r.setIncrementalParse(incrementalInfo);
////
////                if (sanitizedSource) {
////                    OffsetRange sanitizedRange = new OffsetRange(
////                            context.sanitizedRange.getStart()+oldFunctionStart,
////                            context.sanitizedRange.getEnd()+oldFunctionStart);
////                    r.setSanitized(context.sanitized, sanitizedRange, context.sanitizedContents);
////                }
////                r.setSource(source);
////
////                // Add in the errors from last time
////                for (Error e : result.getDiagnostics()) {
//
//        try {
//            final List<DefaultError> allErrors = new ArrayList<DefaultError>();
//            // Absorb all errors, don't pass to the main listeners. We'll
//            // adjust the errors later.
//            context.errorHandler = new ParseErrorHandler() {
//                public void error(DefaultError error) {
////todo: end of merge conflict other
//                   // DefaultError error = (DefaultError)e;
//                    int start = error.getStartPosition();
//                    int end = error.getEndPosition();
//                    if (start != -1) {
//                        // Fix the source offsets: I'm compiling
//                        // just the "function { }" part so the source offsets
//                        // are all wrong; add in the location of function
//                        start += oldFunctionStart;
//                    }
//                    if (end != -1) {
//                        end += oldFunctionStart;
//                    }
//                    error.setOffsets(start, end);
//                    allErrors.add(error);
//                   
//                }
//            };
//
//
//            // Perform some basic cleanup of trailing dots, commas, etc.
//            String oldSource = context.source;
//            int oldCaretOffset = context.caretOffset;
//            context.source = source;
//            if (context.caretOffset >= oldFunctionStart && context.caretOffset <= newFunctionEnd) {
//                context.caretOffset -= oldFunctionStart;
//                boolean ok = sanitizeSource(context, Sanitize.EDITED_DOT);
//
//                if (ok) {
//                    assert context.sanitizedSource != null;
//                    sanitizedSource = true;
//                    source = context.sanitizedSource;
//                    context.sanitized = Sanitize.EDITED_DOT;
//                }
//            }
//
//            context.source = oldSource;
//            context.caretOffset = oldCaretOffset;
//
//            FunctionNode newFunction = parser.parseFunction(source, 
//  "unknown source", //context.snapshot.getSource().getFileObject().getNameExt(), 
//                    lineno);
//            if (newFunction == null) {
//                return null;
//            }
//            // TODO:
//            // (0) Look up the AST node corresponding to the edit region. See if the damage
//            //    region goes beyond the function limit (uh oh, if you add inside the method body
//            //    the damage region will be new
//            // (0) Find out if the change happened inside a function, and if so, which one
//            // (1) Get the exact source code for the given function.
//            //    If the edit list is accurate, then the delta should let me access the }
//            //    -directly- !
//            // (1) Parse
//            // (2) Surgery on the AST to insert the node
//            // (3) Update all the offsets in the AST
//            // (4) Return a new parser result, or consider updating cached info in the parser result!
//            // (5) Deal with the AstNodeAdapter somehow? Ah, don't reuse that!
//            // Update offsets
//            // set current/modified function context in the result object
//            // update the error set! (notifyError) Note - update entire function body range, not just
//            //   the modified range!
//
//            // Adjust the offsets in function nodes: They should be relative to
//            // where the old function started in the document
//            adjustOffsets(newFunction, 0, oldFunctionStart);
//
//            // Adjust the offsets in the rest of the AST - the offsets up the chain as well, not just the following node.
//            int limit = lexToAst(context.snapshot, history.getOriginalEnd());
//            if (limit == -1) {
//                return null;
//            }
//            int delta = history.getSizeDelta();
//
//            adjustOffsets(previousRoot, limit, delta);
//
//            setParentRefs(newFunction, oldFunction.getParentNode());
//            oldFunction.getParentNode().replaceChild(oldFunction, newFunction);
//
//            if (oldFunction.labelNode != null) {
//                newFunction.labelNode = oldFunction.labelNode;
//                oldFunction.labelNode.setLabelledNode(newFunction);
//            }
//
//            context.sanitized = sanitizing;
//            //AstRootElement rootElement = new AstRootElement(context.file.getFileObject(), root, result);
//
//            AstNodeAdapter ast = new AstNodeAdapter(null, previousRoot);
//            JsParseResult r = createParseResult(context.snapshot, previousRoot);
//
//            JsParseResult.IncrementalParse incrementalInfo =
//                    new JsParseResult.IncrementalParse(oldFunction, newFunction,
//                        oldFunctionStart, limit, delta, previousResult);
//            r.setIncrementalParse(incrementalInfo);
//
//            if (sanitizedSource) {
//                OffsetRange sanitizedRange = new OffsetRange(
//                        context.sanitizedRange.getStart()+oldFunctionStart,
//                        context.sanitizedRange.getEnd()+oldFunctionStart);
//                r.setSanitized(context.sanitized, sanitizedRange, context.sanitizedContents);
//            }
//            r.setSource(source);
//
//            // Add in the errors from last time
//            for (DefaultError error : previousResult.getDiagnostics()) {
//                int start = error.getStartPosition();
//                int end = error.getEndPosition();
//
//                if (start >= oldFunctionStart && start <= oldFunctionEnd) {
//                    // Replace functions from within the replaced function block!
//                    continue;
//                }
//
//                // Adjust offsets of other errors
//                if (start >= limit || end >= limit) {
//                    if (start >= limit) {
//                        start += delta;
//                    }
//                    if (end >= limit) {
//                        end += delta;
//                    }
//                    error.setOffsets(start, end);
//                }
//               allErrors.add(error);
//           }
//
//            r.setErrors(allErrors);
//
//            // Prevent accidental traversal of the old function
//            while (oldFunction.hasChildren()) {
//                Node child = oldFunction.getFirstChild();
//                oldFunction.removeChild(child);
//            }
//
//// XXX: parsingapi
////            r.setUpdateState(ParserResult.UpdateState.UPDATED);
//
//            return r;
//        } catch (IllegalStateException ise) {
//            // See issue #128983 for a way to get the compiler to assert for example
//            runtimeException = ise;
//        } catch (RuntimeException re) {
//            //notifyError(context, message, sourceName, line, lineSource, lineOffset, sanitizing, Severity.WARNING, "", null);
//            // XXX TODO - record this somehow
//            re.printStackTrace();
//            runtimeException = re;
//        }
//
//        return null;
//    }

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

    private void adjustOffsets(Node node, int offset, int delta) {
        int start = node.getSourceStart();
        int end = node.getSourceEnd();
        if (start >= offset) {
            start += delta;
        }
        if (end >= offset) {
            end += delta;
        }
        node.setSourceBounds(start, end);

        if (node.hasChildren()) {
            Node curr = node.getFirstChild();
            while (curr != null) {
                if (curr.getSourceEnd() >= offset) {
                    adjustOffsets(curr, offset, delta);
                }
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

//    private int getOffset(Context context, int line, int lineOffset) {
//        String source = context.source;
//        
//        int offset = 0;
//        // TODO - get an accurate line counter! Isn't there something in the NetBeans editor module?
//        for (int i = 0; i < line; offset++) {
//            if (source.charAt(offset) == '\n') { // \r's come first so are not a problem...
//                i++;
//            }
//        }
//
//        offset += lineOffset;
//        return offset;
//    }


    protected void notifyError(Context context, String message, String sourceName, int line,
                           String lineSource, int lineOffset, Sanitize sanitizing, Severity severity, String key, Object params) {
        // Replace a common but unwieldy JRuby error message with a shorter one
        
//        if (JsIndexer.PREINDEXING && severity == Severity.ERROR && context.snapshot.getSource().getFileObject().getNameExt().startsWith("stub_")) {
//            // Ensure there are no code generator bugs in the stubs
//            System.err.println("\n\n\n**********************************************************\n**********************************************************\n" + // NOI18N
//                    "Parsing error for " + message + ", sourceName= " + sourceName + ", line= " + line + ", lineSource=" + lineSource + ", lineOffset=" + lineOffset + ", key=" + key + "\n" + // NOI18N
//                    "**********************************************************\n**********************************************************\n"); // NOI18N
//            System.exit(0);
//        }

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
//            if (isJson(context)) {
//                root = parser.parseJson(source, getSourceUri(context.snapshot.getSource()), lineno);
//            } else {
                root = parser.parse(source, "unknown source", //getSourceUri(context.snapshot.getSource()), 
                        lineno);
//           }
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

        //final int targetVersion = SupportedBrowsers.getInstance().getLanguageVersion();
        //compilerEnv.setLanguageVersion(targetVersion);

        boolean e4x = true;//(targetVersion == org.mozilla.nb.javascript.Context.VERSION_DEFAULT) ||
            //(targetVersion >= org.mozilla.nb.javascript.Context.VERSION_1_7);
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

//    private boolean isJson(Context context) {
//        FileObject f = context.snapshot.getSource().getFileObject();
//        if (f != null) {
//            return "json".equals(f.getExt()); // NOI18N
//        } else {
//            return false;
//        }
//    }
    
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
        // TODO - perform these steps in the NodeFactory's addChild methods instead
       assert node.getParentNode() == null;

       node.setParentNode(parent);

       if (node.hasChildren()) {
           Node curr = node.getFirstChild();
           while (curr != null) {
               setParentRefs(curr, node);
               curr = curr.getNext();
           }
       }
    }
    
    @SuppressWarnings("unchecked")
    public static Element resolveHandle(JsParseResult info, ElementHandle handle) {
        if (handle instanceof AstElement) {
            AstElement element = (AstElement)handle;
            JsParseResult oldInfo = element.getParseResult();
            if (oldInfo == info) {
                return element;
            }
            Node oldNode = element.getNode(); // XXX Make it work for DefaultComObjects...
            Node oldRoot = oldInfo.getRootNode();
            
            Node newRoot = info.getRootNode();
            if (newRoot == null) {
                return null;
            }

            // Find newNode
            Node newNode = find(oldRoot, oldNode, newRoot);

            if (newNode != null) {
                AstElement co = AstElement.getElement(info, newNode);

                return co;
            }
        } else if (handle instanceof JsElement) {
            return (JsElement)handle;
        }

        return null;
    }

    private static Node find(Node oldRoot, Node oldObject, Node newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        if (oldRoot == oldObject) {
            // Found it!
            return newRoot;
        }

        Node o = oldRoot.getFirstChild();
        Node n = newRoot.getFirstChild();

        if (o == null || n == null) {
            return null;
        }
        
        while (o != null) {
            if (n == null) {
                return null; // No match - the trees have changed structure
            }
  
            if (o == oldObject) {
                // Found it!
                return n;
            }

            // Recurse
            Node match = find(o, oldObject, n);

            if (match != null) {
                return match;
            }
            
            o = o.getNext();
            n = n.getNext();
        }

        if (n != null) {
            return null; // No match - the trees have changed structure
        }

        return null;
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
        
        @Override
        public String toString() {
            return "JsParser.Context(" + //snapshot.getSource().getFileObject() +
            ")"; // NOI18N
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

//    private static String getSourceUri(Source source) {
//        FileObject f = source.getFileObject();
//        if (f != null) {
//            return f.getNameExt();
//        } else {
//            return "fileless"; //NOI18N
       // }
 //   }

    private static final class RhinoContext extends org.mozilla.nb.javascript.Context {
        public RhinoContext() {
            super(ContextFactory.getGlobal());
        }
    } // End of RhinoContext
}
