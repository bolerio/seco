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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package seco.notebook.javascript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.mozilla.nb.javascript.Node;

import seco.notebook.csl.ElementKind;
import seco.notebook.csl.OffsetRange;
import seco.notebook.csl.ParserResult;
import seco.notebook.csl.QuerySupport;

//import org.netbeans.modules.csl.api.ElementHandle;
//import org.netbeans.modules.csl.api.HtmlFormatter;
//import org.netbeans.modules.csl.api.OffsetRange;
//import org.netbeans.api.lexer.Token;
//import org.netbeans.api.lexer.TokenHierarchy;
//import org.netbeans.api.lexer.TokenId;
//import org.netbeans.api.lexer.TokenSequence;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.csl.api.ElementKind;
//import org.netbeans.modules.csl.spi.ParserResult;
//import org.netbeans.modules.javascript.editing.lexer.Call;
//import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
//import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
//import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
//import org.openide.filesystems.FileObject;
//import org.openide.util.NbBundle;

/**
 * @todo Remove .prototype and .superclass properties from FQN's
 * @todo Handle $super - jump to the superclass' implementation of the given method
 * 
 * @author Tor Norbye
 */
public class JsDeclarationFinder //implements DeclarationFinder
{
    private static final boolean CHOOSE_ONE_DECLARATION = Boolean.getBoolean("javascript.choose_one_decl");

    public OffsetRange getReferenceSpan(Document document, int lexOffset) {
        return OffsetRange.NONE;
        //TokenHierarchy<Document> th = TokenHierarchy.get(document);
        
        //BaseDocument doc = (BaseDocument)document;
        
       // TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, lexOffset);

//        if (ts == null) {
//            return OffsetRange.NONE;
//        }
//
//        ts.move(lexOffset);
//
//        if (!ts.moveNext() && !ts.movePrevious()) {
//            return OffsetRange.NONE;
//        }
//
//        // Determine whether the caret position is right between two tokens
//        boolean isBetween = (lexOffset == ts.offset());
//
//        OffsetRange range = getReferenceSpan(ts, th, lexOffset);
//
//        if ((range == OffsetRange.NONE) && isBetween) {
//            // The caret is between two tokens, and the token on the right
//            // wasn't linkable. Try on the left instead.
//            if (ts.movePrevious()) {
//                range = getReferenceSpan(ts, th, lexOffset);
//            }
//        }
//        
//        return range;  
    }

    /** Locate the method declaration for the given method call */
    IndexedFunction findMethodDeclaration(ParserResult info, Node call, AstPath path, Set<IndexedFunction>[] alternativesHolder) {
        JsParseResult jspr = AstUtilities.getParseResult(info);
        JsIndex index = JsIndex.EMPTY; //E.get(QuerySupport.findRoots(info.getSnapshot().getSource().getFileObject(), null, Collections.<String>emptySet(), Collections.<String>emptySet()));
        Set<IndexedElement> functions = null;

        String fqn = JsTypeAnalyzer.getCallFqn(jspr, call, true);
        if (fqn != null) {
            functions = index.getElementsByFqn(fqn, QuerySupport.Kind.EXACT, jspr);
            // Prefer methods/constructors
            if (functions.size() > 0) {
                Set<IndexedElement> filtered = new HashSet<IndexedElement>();// new DuplicateElementSet(functions.size());
                for (IndexedElement e : functions) {
                    ElementKind kind = e.getKind();
                    if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
                        filtered.add(e);
                    }
                }
                if (filtered.size() > 0) {
                    functions = filtered;
                }
            }
        }
        
        if (functions == null || functions.size() == 0) {
            String prefix = AstUtilities.getCallName(call, false);
            if (prefix.length() > 0) {
                functions = index.getAllNames(prefix, QuerySupport.Kind.EXACT, jspr);
            }
        }

        if (functions != null && functions.size() > 0) {
            IndexedElement candidate =
                findBestElementMatch(jspr, /*name,*/ functions,/* (BaseDocument)info.getDocument(),
                    astOffset, lexOffset, path,*/ call, index);
            if (candidate instanceof IndexedFunction) {
                return (IndexedFunction)candidate;
            }
        }
        return null;
    }

 //   private OffsetRange getReferenceSpan(TokenSequence<?> ts,
//        TokenHierarchy<Document> th, int lexOffset) {
//        Token<?> token = ts.token();
//        TokenId id = token.id();
//
//        if (id == JsTokenId.IDENTIFIER) {
//            if (token.length() == 1 && id == JsTokenId.IDENTIFIER && token.text().toString().equals(",")) {
//                return OffsetRange.NONE;
//            }
//        }
//
//        // TODO: Tokens.SUPER, Tokens.THIS, Tokens.SELF ...
//        if (id == JsTokenId.IDENTIFIER) {
//            return new OffsetRange(ts.offset(), ts.offset() + token.length());
//        }
        
//        // Look for embedded RDoc comments:
//        TokenSequence<?> embedded = ts.embedded();
//
//        if (embedded != null) {
//            ts = embedded;
//            embedded.move(lexOffset);
//
//            if (embedded.moveNext()) {
//                Token<?> embeddedToken = embedded.token();
//
//                if (embeddedToken.id() == JsCommentTokenId.COMMENT_LINK) {
//                    return new OffsetRange(embedded.offset(),
//                        embedded.offset() + embeddedToken.length());
//                }
//                // Recurse into the range - perhaps there is Js code (identifiers
//
//                // etc.) to follow there
//                OffsetRange range = getReferenceSpan(embedded, th, lexOffset);
//
//                if (range != OffsetRange.NONE) {
//                    return range;
//                }
//            }
//        }

//        // Allow hyperlinking of some literal strings too, such as require strings
//        if ((id == JsTokenId.QUOTED_STRING_LITERAL) || (id == JsTokenId.STRING_LITERAL)) {
//            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);
//
//            if (requireStart != -1) {
//                String require = LexUtilities.getStringAt(lexOffset, th);
//
//                if (require != null) {
//                    return new OffsetRange(requireStart, requireStart + require.length());
//                }
//            }
//        }

//        return OffsetRange.NONE;
//    }

//    public DeclarationLocation findDeclaration(ParserResult info, int lexOffset) {
//
//        final Document document = info.getSnapshot().getSource().getDocument(false);
//        if (document == null) {
//            return DeclarationLocation.NONE;
//        }
//
//        final BaseDocument doc = (BaseDocument)document;
//
//        JsParseResult parseResult = AstUtilities.getParseResult(info);
//        doc.readLock(); // Read-lock due to Token hierarchy use
//        try {
//            Node root = parseResult.getRootNode();
//            final int astOffset = AstUtilities.getAstOffset(info, lexOffset);
//            if (astOffset == -1) {
//                return DeclarationLocation.NONE;
//            }
//            final TokenHierarchy<Document> th = TokenHierarchy.get(document);
//
//            AstPath path = null;
//            Node node = null;
//            if (root != null) {
//                path = new AstPath(root, astOffset);
//                node = path.leaf();
//            }
//
//            if (node != null) {
//                // TODO search for local variables
//                Call call = Call.getCallType(doc, th, lexOffset);
//                if (call.getLhs() == null && AstUtilities.isNameNode(node)) {
//                    // Local reference -- is it a local var?
//
//                    VariableVisitor v = parseResult.getVariableVisitor();
//                    List<Node> nodes = v.getVarOccurrences(node);
//                    if (nodes != null && nodes.size() > 0) { // Should always be true
//                        Map<Integer,Node> posToNode = new  HashMap<Integer, Node>();
//                        for (Node n : nodes) {
//                            posToNode.put(n.getSourceStart(), n);
//                        }
//                        List<Integer> starts = new ArrayList<Integer>(posToNode.keySet());
//                        Collections.sort(starts);
//                        Node first = posToNode.get(starts.get(0));
//                        return getLocation(parseResult, first);
//                    } else {
//                        // Probably a global variable.
//                        // TODO - perform global variable search here.
//                    }
//                }
//            }
//            
//            // TODO - global vars
//
//            String prefix = new JsCodeCompletion().getPrefix(info, lexOffset, false);
//            if (prefix != null) {
//                JsIndex index = JsIndex.EMPTY;//.get(QuerySupport.findRoots(info.getSnapshot().getSource().getFileObject(), null, Collections.<String>emptySet(), Collections.<String>emptySet()));
//                Set<IndexedElement> elements = index.getAllNames(prefix, QuerySupport.Kind.EXACT, parseResult);
//
//                String name = null; // unused!
//                return getMethodDeclaration(parseResult, name, elements, node, index/*, astOffset, lexOffset*/);
//            }
//        } finally {
//            doc.readUnlock();
//        }
//        return DeclarationLocation.NONE;
//    }
    
//    private DeclarationLocation getLocation(JsParseResult info, Node node) {
//        AstElement element = AstElement.getElement(info, node);
//        return new DeclarationLocation(
//            info.getSnapshot().getSource().getFileObject(),
//            LexUtilities.getLexerOffset(info, node.getSourceStart()),
//            element
//        );
//    }
//
//    DeclarationLocation findLinkedMethod(JsParseResult info, String url) {
//        JsIndex index = JsIndex.get(QuerySupport.findRoots(info.getSnapshot().getSource().getFileObject(), null, Collections.<String>emptySet(), Collections.<String>emptySet()));
//        JsParseResult parseResult = AstUtilities.getParseResult(info);
//        Set<IndexedElement> elements = index.getAllNames(url, QuerySupport.Kind.EXACT, parseResult);
//        IndexedElement function = findBestElementMatch(info, elements, null, null);
//        if (function != null) {
//            return new DeclarationLocation(function.getFileObject(), 0, function);
//        } else {
//            return DeclarationLocation.NONE;
//        }
//    }

    private IndexedElement findBestElementMatch(JsParseResult info, /*String name,*/ Set<IndexedElement> elements,/*
        BaseDocument doc, int astOffset, int lexOffset, AstPath path*/ Node callNode, JsIndex index) {
        Set<IndexedElement> candidates = new HashSet<IndexedElement>();
        
        // Prefer methods that match the given FQN. In other words, if I have say
        // both widgetBase.setProps and bubble.setProps where bubble extends widgetBase,
        // the inherited search will return both, but I want to prefer the derived one.
        
        // First prefer methods that have documentation
        
        
        
        // 1. First see if the reference is fully qualified. If so the job should
        //   be easier: prune the result set down
        // If I have the fqn, I can also call RubyIndex.getRDocLocation to pick the
        // best candidate
        if (callNode != null && (callNode.getType() == org.mozilla.nb.javascript.Token.CALL ||
                callNode.getType() == org.mozilla.nb.javascript.Token.NEW)) {
            // It's a call, so prefer method/constructor elements
            String fqn = JsTypeAnalyzer.getCallFqn(info, callNode, true);
            if (fqn != null && fqn.length() > 0 && fqn.indexOf('.') != -1) {
                while ((fqn != null) && (fqn.length() > 0)) {
                    for (IndexedElement method : elements) {
                        if (fqn.equals(method.getIn()+"."+method.getName())) { // NOI18N
                            candidates.add(method);
                        }
                    }

                    // Check inherited methods; for example, if we've determined
                    // that you're looking for Integer::foo, I should happily match
                    // Numeric::foo.
                    if (index != null && candidates.size() < elements.size()) {
                        // Repeat looking in the superclass
                        fqn = index.getExtends(fqn);
                    } else {
                        fqn = null;
                    }
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            elements = candidates;
        }
        
        // (2) Prefer matches in the same file as the reference
        candidates = new HashSet<IndexedElement>();
        //FileObject fo = info.getSnapshot().getSource().getFileObject();
        for (IndexedElement element : elements) {
           // if (fo == element.getFileObject()) {
                candidates.add(element);
           // }
        }
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            elements = candidates;
        }
        
        // For now no good heuristics to pick a method.
        // Possible things to consider:
        // -- scope - whether the method is local
        // -- builtins should get some priority over libraries
        // -- other methods called which can help disambiguate
        // -- documentation?
        if (elements.size() > 0) {
            IndexedElement e = elements.iterator().next();
            IndexedElement r = e.findRealFileElement();
            if (r != null) {
                return r;
            }
            
            return e;
        }
        
        return null;
    }

//    private DeclarationLocation getMethodDeclaration(JsParseResult info, String name, Set<IndexedElement> elements,
//            /*AstPath path,*/ Node closest, JsIndex index/*, int astOffset, int lexOffset*/) {
////        try {
//            IndexedElement candidate =
//                findBestElementMatch(info, /*name,*/ elements,/* (BaseDocument)info.getDocument(),
//                    astOffset, lexOffset, path,*/ closest, index);
//
//            if (candidate != null) {
//                boolean invalid = false;
//                if (candidate.getFilenameUrl() != null && candidate.getFilenameUrl().indexOf("jsstubs") != -1 &&
//                        // If it's in my sdocs.zip, I've gotta try to find the corresponding element
//                        candidate.getFilenameUrl().indexOf("sdocs.zip") == -1) {
//                    invalid = true;
//                }
//                IndexedElement com = candidate; // TODO - let's not do foreign node computation here!! Not needed yet!
//                JsParseResult[] infoRet = new JsParseResult[1];
//                Node node = AstUtilities.getForeignNode(com, infoRet);
//                DeclarationLocation loc;
//                if (node == null) {
//                    int offset = 0; // unknown - use top of the file
//                    // TODO - initialize in the index!
//                    loc = new DeclarationLocation(candidate.getFileObject(),
//                        offset, com);
//                } else {
//                    int astOffset = node.getSourceStart();
//                    int lexOffset = LexUtilities.getLexerOffset(infoRet[0], astOffset);
//                    loc = new DeclarationLocation(com.getFileObject(),
//                       lexOffset, com);
//                }
//                if (invalid) {
//                    if (candidate.isDocOnly()) {
//                        loc.setInvalidMessage(NbBundle.getMessage(JsDeclarationFinder.class, "NoSourceDocOnly", candidate.getName()));
//                    } else {
//                        loc.setInvalidMessage(NbBundle.getMessage(JsDeclarationFinder.class, "InvalidJsMethod", candidate.getName()));
//                    }
//                }
//
//                if (!CHOOSE_ONE_DECLARATION && elements.size() > 1) {
//                    // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
//                    // don't ask user!
////                    int not_nodoced = 0;
////                    for (final IndexedFunction mtd : methods) {
////                        if (!mtd.isNoDoc()) {
////                            not_nodoced++;
////                        }
////                    }
////                    if (not_nodoced >= 2) {
//                        for (final IndexedElement e : elements) {
//                            loc.addAlternative(new JsAltLocation(e, e == candidate));
//                        }
////                    }
//                }
//
//                return loc;
//            }
////        } catch (IOException ioe) {
////            Exceptions.printStackTrace(ioe);
////        }
//     
//        return DeclarationLocation.NONE;
//    }
    
//    private class JsAltLocation implements AlternativeLocation {
//        private IndexedElement element;
//        private boolean isPreferred;
//        private String cachedDisplayItem;
//        
//        JsAltLocation(IndexedElement element, boolean isPreferred) {
//            this.element = element;
//            this.isPreferred = isPreferred;
//        }
//
//        public String getDisplayHtml(HtmlFormatter formatter) {
//            formatter.setMaxLength(120);
//            if (cachedDisplayItem == null) {
//                formatter.reset();
//
//                boolean nodoc = element.isNoDoc();
//                boolean documented = element.isDocumented();
//                if (isPreferred) {
//                    formatter.emphasis(true);
//                } else if (nodoc) {
//                    formatter.deprecated(true);
//                }
//
//                if (element instanceof IndexedFunction) {
////                    if (element.getFqn() != null) {
////                        formatter.appendText(element.getFqn());
////                        formatter.appendText(".");
////                    }
//                    if (element.getIn() != null) {
//                        formatter.appendText(element.getIn());
//                        formatter.appendText(".");
//                    }
//                    formatter.appendText(element.getName());
//                    IndexedFunction method = (IndexedFunction)element;
//                    Collection<String> parameters = method.getParameters();
//
//                    if ((parameters != null) && (parameters.size() > 0)) {
//                        formatter.appendText("("); // NOI18N
//
//                        Iterator<String> it = parameters.iterator();
//
//                        while (it.hasNext()) { // && tIt.hasNext()) {
//                            formatter.parameters(true);
//                            formatter.appendText(it.next());
//                            formatter.parameters(false);
//
//                            if (it.hasNext()) {
//                                formatter.appendText(", "); // NOI18N
//                            }
//                        }
//
//                        formatter.appendText(")"); // NOI18N
//                    }
//                } else {
////                    formatter.appendText(element.getFqn());
//                    formatter.appendText(element.getName());
//                }
//
//                String filename = null;
//                String url = element.getFilenameUrl();
//                if (url == null) {
//                    // Deleted file?
//                    // Just leave out the file name
//                } else if (url.indexOf("jsstubs") != -1) {
//                    filename = NbBundle.getMessage(JsDeclarationFinder.class, "JsLib");
//                    
////                    if (url.indexOf("/stub_") == -1) {
////                        // Not a stub file, such as ftools.rb
////                        // TODO - don't hardcode for version 0.2
////                        String stub = "jsstubs/1.8.6-p110/";
////                        int stubStart = url.indexOf(stub);
////                        if (stubStart != -1) {
////                            filename = filename+": " + url.substring(stubStart);
////                        }
////                    }
//                } else {
//                    FileObject fo = element.getFileObject();
//                    if (fo != null) {
//                        filename = fo.getNameExt();
//                    } else {
//                        // Perhaps a file that isn't present here, such as something in site_Js
//                        int lastIndex = url.lastIndexOf('/');
//                        if (lastIndex != -1) {
//                            String s = url.substring(0, lastIndex);
//                            int almostLastIndex = s.lastIndexOf('/');
//                            if (almostLastIndex != -1 && ((url.length()-almostLastIndex) < 40)) {
//                                filename = url.substring(almostLastIndex+1);
//                                if (filename.indexOf(':') != -1) {
//                                    // Don't include prefix like cluster:, file:, etc.
//                                    filename = url.substring(lastIndex+1);
//                                }
//                            } else {
//                                filename = url.substring(lastIndex+1);
//                            }
//                        }
//                    }
//                }
//
//                if (filename != null) {
//                    formatter.appendText(" ");
//                    formatter.appendText(NbBundle.getMessage(JsDeclarationFinder.class, "In"));
//                    formatter.appendText(" ");
//                    formatter.appendText(filename);
//                }
//                
//                if (documented) {
//                    formatter.appendText(" ");
//                    formatter.appendText(NbBundle.getMessage(JsDeclarationFinder.class, "Documented"));
//                } else if (nodoc) {
//                    formatter.appendText(" ");
//                    formatter.appendText(NbBundle.getMessage(JsDeclarationFinder.class, "NoDoced"));
//                }
//
//                if (isPreferred) {
//                    formatter.emphasis(false);
//                } else if (nodoc) {
//                    formatter.deprecated(false);
//                }
//
//                cachedDisplayItem = formatter.getText();
//            }
//            
//            return cachedDisplayItem;
//        }
//
//        public DeclarationLocation getLocation() {
//            JsParseResult[] infoRet = new JsParseResult[1];
//            Node node = AstUtilities.getForeignNode(element, infoRet);
//            
//            DeclarationLocation loc = DeclarationLocation.NONE;
//            if (node == null) {
//                if (element instanceof IndexedElement) {
//                    FileObject fo = element.getFileObject();
//                    if (fo != null) {
//                        int astOffset = element.getNodeOffset();
//                        int lexOffset = LexUtilities.getLexerOffset(infoRet[0], astOffset);
//                        if (lexOffset == -1) {
//                            lexOffset = 0;
//                        }
//                        loc = new DeclarationLocation(element.getFileObject(), lexOffset, element);
//                    }
//                }
//            } else {
//                int astOffset = node.getSourceStart();
//                int lexOffset = LexUtilities.getLexerOffset(infoRet[0], astOffset);
//                if (lexOffset == -1) {
//                    lexOffset = 0;
//                }
//                loc = new DeclarationLocation(element.getFileObject(),
//                    lexOffset, element);
//            }
//
//            if (loc != null) {
//                if (element.getFilenameUrl() != null && element.getFilenameUrl().indexOf("jsstubs") != -1 &&
//                        // If it's in my sdocs.zip, I've gotta try to find the corresponding element
//                        element.getFilenameUrl().indexOf("sdocs.zip") == -1) {
//                    if (element.isDocOnly()) {
//                        loc.setInvalidMessage(NbBundle.getMessage(JsDeclarationFinder.class, "NoSourceDocOnly", element.getName()));
//                    } else {
//                        loc.setInvalidMessage(NbBundle.getMessage(JsDeclarationFinder.class, "InvalidJsMethod", element.getName()));
//                    }
//                }
//            }
//            
//            return loc;
//        }
//
//        public ElementHandle getElement() {
//            return element;
//        }
//
//        public int compareTo(AlternativeLocation alternative) {
//            JsAltLocation alt = (JsAltLocation)alternative;
//
//            // The preferred item should be chosen
//            if (isPreferred) {
//                return -1;
//            } else if (alt.isPreferred) {
//                return 1;
//            } // Can't both be so no else == check
//            
////            // Nodoced items last
////            if (element.isNoDoc() != alt.element.isNoDoc()) {
////                return element.isNoDoc() ? 1 : -1;
////            }
////            
////            // Documented items on top
////            if (element.isDocumented() != alt.element.isDocumented()) {
////                return element.isDocumented() ? -1 : 1;
////            }
//
//            // TODO: Sort by gem?
//            
//            // Sort by containing clz - just do fqn here?
//            String thisIn = element.getIn() != null ? element.getIn() : "";
//            String thatIn = alt.element.getIn() != null ? alt.element.getIn() : "";
//            int cmp = thisIn.compareTo(thatIn);
//            if (cmp != 0) {
//                return cmp;
//            }
//
//            // Sort by file
//            String thisFile = element.getFileObject() != null ? element.getFileObject().getNameExt() : "";
//            String thatFile = alt.element.getFileObject() != null ? alt.element.getFileObject().getNameExt() : "";
//            cmp = thisFile.compareTo(thatFile);
//            
//            return cmp;
//        }
//    }
}
