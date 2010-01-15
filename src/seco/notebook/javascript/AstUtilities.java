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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node.LabelledNode;
import org.mozilla.nb.javascript.Token;
import seco.notebook.csl.*;
//import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
//import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
//import org.netbeans.modules.javascript.editing.lexer.LexUtilities;


/**
 *
 * @author Tor Norbye
 */
public final class AstUtilities {

    private AstUtilities() {
        // This is a utility class
    }

    public static final String DOT_PROTOTYPE = ".prototype"; // NOI18N

//    public static int getAstOffset(Parser.Result info, int lexOffset) {
//        JsParseResult result = getParseResult(info);
//        if (result != null) {
//            return result.getSnapshot().getEmbeddedOffset(lexOffset);
//        }
//        return lexOffset;
//    }
//
//    public static OffsetRange getAstOffsets(Parser.Result info, OffsetRange lexicalRange) {
//        JsParseResult result = getParseResult(info);
//        if (result != null) {
//            int rangeStart = lexicalRange.getStart();
//            int start = result.getSnapshot().getEmbeddedOffset(rangeStart);
//            if (start == rangeStart) {
//                return lexicalRange;
//            } else if (start == -1) {
//                return OffsetRange.NONE;
//            } else {
//                // Assumes the translated range maintains size
//                return new OffsetRange(start, start+lexicalRange.getLength());
//            }
//        }
//        return lexicalRange;
//    }

    /** SLOW - used from tests only right now */
//    public static boolean isGlobalVar(Parser.Result info, Node node) {
//        if (!isNameNode(node)) {
//            return false;
//        }
//        JsParseResult rpr = AstUtilities.getParseResult(info);
//        if (rpr == null) {
//            return false;
//        }
//        VariableVisitor v = rpr.getVariableVisitor();
//        List<Node> nodes = v.getVarOccurrences(node);
//        if (nodes == null) {
//            return true;
//        } else {
//            return nodes.contains(node);
//        }
//    }
    
    /** 
     * Return the comment sequence (if any) for the comment prior to the given offset.
     */
//    public static TokenSequence<? extends JsCommentTokenId> getCommentFor(JsParseResult info, Node node) {
//        int astOffset = node.getSourceStart();
//        CharSequence text = info.getSnapshot().getText();
//        try {
//            // Jump to the end of the previous line since that's typically where the block comments
//            // sit (I can't just iterate left in the document hierarchy since for functions in 
//            // object literals there could be names there -- e.g.
//            //     /** My document */
//            //     foo: function() {
//            //     }
//            // Here the function offset points to the beginning of "function", not "foo".
//            int rowStart = GsfUtilities.getRowStart(text, Math.min(astOffset, text.length()));
//            if (rowStart > 0) {
//                astOffset = GsfUtilities.getRowEnd(text, Math.min(rowStart - 1, text.length()));
//            }
//        } catch (BadLocationException ble) {
//            ble.printStackTrace();
//        }
//        return LexUtilities.getCommentFor(info.getSnapshot(), astOffset);
//    }
    
    public static Node getFirstChild(Node node) {
        return node.getFirstChild();
    }

    public static Node getSecondChild(Node node) {
        Node first = node.getFirstChild();
        if (first == null) {
            return null;
        } else {
            return first.getNext();
        }
    }
    
    public static JsParseResult getParseResult(ParserResult info) {
        assert info instanceof JsParseResult : "Expecting JsParseResult, but have " + info; //NOI18N
        return (JsParseResult) info;
    }

//    private static boolean isJavascript(String mimeType) {
//        return mimeType.equals(JsTokenId.JAVASCRIPT_MIME_TYPE) ||
//                mimeType.equals(JsTokenId.JSON_MIME_TYPE);
//    }

//    private static Parser.Result findJsParserResult(ResultIterator ri) throws ParseException {
//        if(isJavascript(ri.getSnapshot().getMimeType())) {
//            return ri.getParserResult();
//        } else {
//            //try to dive deeper
////            for(Embedding e : ri.getEmbeddings()) {
////                Parser.Result res = findJsParserResult(ri.getResultIterator(e));
////                if(res != null) {
////                    return res;
////                }
////            }
//        }
//        return null;
//    }


    /**
     * Return a range that matches the given node's source buffer range
     */
    public static OffsetRange getRange(Node node) {
        return new OffsetRange(node.getSourceStart(), node.getSourceEnd());
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    @SuppressWarnings("unchecked")
    public static OffsetRange getNameRange(Node node) {
        final int type = node.getType();
        switch (type) {
        case Token.FUNCTION: {
            if (node.hasChildren()) {
                for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                    if (child.getType() == Token.FUNCNAME) {
                        return getNameRange(child);
                    }
                }
            }
            
            return getRange(node);
        }
        case Token.NAME:
        case Token.BINDNAME:
        case Token.FUNCNAME:
        case Token.PARAMETER:
        case Token.OBJLITNAME:
            int start = node.getSourceStart();
            String name = node.getString();
            return new OffsetRange(start, start+name.length());
        case Token.CALL:
            Node nameNode = findCallNameNode(node);
            if (nameNode != null) {
                return getNameRange(nameNode);
            }
        }

        return getRange(node);
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    public static int findArgumentIndex(Node call, int astOffset, AstPath path) {
        assert call.getType() == Token.CALL || call.getType() == Token.NEW;

        // The first child is the call expression -- the name, or property lookup etc.
        Node child = call.getFirstChild();
        if (child == null) {
            return 0;
        }
        child = child.getNext();
        
        if (child == null || astOffset < child.getSourceStart()) {
            return -1;
        }

        int index = 0;
        while (child != null) {
            if (child.getSourceEnd() >= astOffset) {
                return index;
            }
            child = child.getNext();
            if (child != null) {
                index++;
            }
        }
        
        return index;
    }
    
    private static Node findCallNameNode(Node callNode) {
        if (callNode.hasChildren()) {
            Node child = callNode.getFirstChild();

            if (child != null) {
                if (child.getType() == Token.NAME) {
                    return child;
                } else if (child.getType() == Token.GETPROP) {
                    Node grandChild = child.getFirstChild();
                    assert grandChild.getNext().getNext() == null : grandChild.getNext().getNext();
                    return grandChild.getNext();
                }
            } else {
                assert false : "Unexpected call firstchild - " + child;
            }
        }

        return null;
    }
    
    /** Return the name of a method being called.
     * @param callNode the node with type == Token.CALL
     * @param fqn If true, return the full property name to the function being called,
     *   otherwise return just the basename.
     *   (Note - this isn't necessarily the function name...
     *      function foo(x,y) {
     *      }
     *      bar = foo
     *      bar()   --- here we're really calling the method named foo but getCallName will
     *                   return bar
     */
    public static String getCallName(Node callNode, boolean fqn) {
        assert callNode.getType() == Token.CALL || callNode.getType() == Token.NEW;
        
        if (!fqn) {
            Node nameNode = findCallNameNode(callNode);
            if (nameNode != null) {
                return nameNode.getString();
            }
        } else if (callNode.hasChildren()) {
            Node child = callNode.getFirstChild();
            
            if (child != null) {
                if (child.getType() == Token.GETELEM) {
                    child = child.getNext();
                    if (child == null) {
                        // Unexpected - but see http://statistics.netbeans.org/analytics/detail.do?id=43998
                        return "";
                    }
                }
                if (child.getType() == Token.NAME) {
                    return child.getString();
                } else if (child.getType() == Token.GETPROP) {
                    Node grandChild = child.getFirstChild();
                    if (fqn) {
                        StringBuilder sb = new StringBuilder();
                        addName(sb, child);
                        return sb.toString();
                    } else {
                        //assert grandChild.getNext().getNext() == null : grandChild.getNext().getNext();
                        return grandChild.getNext().getString();
                    }
                } else {
                    // WARNING: I can have something unexpected here, like a HOOK node in the following
                    // for the conditional -
                    //  (this.creator ? this.creator : this.defaultCreator)(item, hint);
                    // not sure how to handle this.
                    
                    //assert false : "Unexpected call firstchild - " + child;
                }
            }
        }
        
        return ""; // NOI18N
    }

    public static FunctionNode findMethodAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            if (node.getType() == Token.FUNCTION) {
                return (FunctionNode)node;
            }
        }

        return null;
    }

    
    
    public static Node findLocalScope(Node node, AstPath path) {
        // TODO - implement properly (using the VariableFinder)

        Iterator<Node> it = path.leafToRoot();
        while (it.hasNext()) {
            Node n = it.next();
            if (n.getType() == Token.FUNCTION) {
                return n;
            }
            
            // This isn't really right - there could be a series of nested functions!
        }
        
        
//        Node method = findMethod(path);
//
//        if (method == null) {
//            Iterator<Node> it = path.leafToRoot();
//            while (it.hasNext()) {
//                Node n = it.next();
//                switch (n.nodeId) {
//                case NodeTypes.DEFNNODE:
//                case NodeTypes.DEFSNODE:
//                case NodeTypes.CLASSNODE:
//                case NodeTypes.SCLASSNODE:
//                case NodeTypes.MODULENODE:
//                    return n;
//                }
//            }
//            
//            if (path.root() != null) {
//                return path.root();
//            }
//
//            method = findBlock(path);
//        }
//
//        if (method == null) {
//            method = path.leafParent();
//
//            if (method.nodeId == NodeTypes.NEWLINENODE) {
//                method = path.leafGrandParent();
//            }
//
//            if (method == null) {
//                method = node;
//            }
//        }
//
//        return method;
        
// TODO JS - implement properly        
        return path.root();
        
    }
    
    public static boolean isNameNode(Node node) {
        int type = node.getType();
        return type == Token.NAME || type == Token.BINDNAME || type == Token.PARAMETER || 
                    type == Token.FUNCNAME || type == Token.OBJLITNAME /*|| type == Token.CALL*/;
    }
    
    /** Collect nodes of the given types (node.nodeId==NodeTypes.x) under the given root */
    public static void addNodesByType(Node root, int[] nodeIds, List<Node> result) {
        for (int i = 0; i < nodeIds.length; i++) {
            if (root.getType() == nodeIds[i]) {
                result.add(root);
                break;
            }
        }

        if (root.hasChildren()) {
            Node child = root.getFirstChild();

            for (; child != null; child = child.getNext()) {
                addNodesByType(child, nodeIds, result);
            }
        }
    }
    
    public static boolean isLabelledFunction(Node objlitNode) {
        assert objlitNode.getType() == Token.OBJLITNAME;
        LabelledNode node = (LabelledNode)objlitNode;
        Node labelledNode = node.getLabelledNode();
        return labelledNode.getType() == Token.FUNCTION;
    }

    public static FunctionNode getLabelledFunction(Node objlitNode) {
        assert objlitNode.getType() == Token.OBJLITNAME;
        LabelledNode node = (LabelledNode)objlitNode;
        Node labelledNode = node.getLabelledNode();
        if (labelledNode.getType() == Token.FUNCTION) {
            return (FunctionNode)labelledNode;
        }
        
        return null;
    }

    public static Node getLabelledNode(Node objlitNode) {
        assert objlitNode.getType() == Token.OBJLITNAME;
        LabelledNode node = (LabelledNode)objlitNode;
        Node labelledNode = node.getLabelledNode();
        
        return labelledNode;
    }
    
    public static String getFunctionFqn(Node node, boolean[] isInstance) {
        if (isInstance != null) {
            isInstance[0] = true;
        }

        FunctionNode func = (FunctionNode) node;
        //AstElement js = AstElement.getElement(node);
        Node parent = node.getParentNode();
        int parentType = parent != null ? parent.getType() : 0;

        // If it's an anonymous function, I've gotta do some more stuff 
        // here. In particular, I should look for a pattern where the
        // anonymous function is assigned to a class property, and if
        // so, record that somehow
        //if (func.)
        // String derivedName = ...
        //js.setName(derivedName);
        String funcName = func.getFunctionName();
        if (funcName == null || funcName.length() == 0) {
            String name = null;
            boolean wasThis = false;
            if (parentType == Token.CALL && func.getNext() == null && 
                    parent.getParentNode() != null && parent.getFirstChild() == func) {
                Node grandParent = parent.getParentNode();
                int grandParentType = grandParent.getType();
                if (grandParentType == Token.SETPROP || grandParentType == Token.SETNAME) {
                    // Looks like there's a call in the middle; for example, Yahoo.util.Event has
                    // this weird definition:
                    //    YAHOO.util.Event = function() {
                    //       ...
                    //    }();
                    
                    // We'll just bypass it
                    parent = grandParent;
                    parentType = parent.getType();
                }
            }
            
            if (parentType == Token.NAME) {
                // Only accept "class" definitions here, e.g "var Foo = "...
                String n = parent.getString();
                if (Character.isUpperCase(n.charAt(0))) {
                    name = n;
                }
            } else if (parentType == Token.OBJECTLIT) {
                // Foo.Bar = { foo : function() }
                // We handle object literals but skip the ones
                // that don't have an associated name. This must
                // be one of those cases.
                //name = parent.getString();
            } else if (parentType == Token.SETPROP || parentType == Token.SETNAME) {
                // this.foo = function() { }
                Node firstChild = parent.getFirstChild();
                if (firstChild.getType() == Token.THIS) {
                    wasThis = true;
                    Node method = parent.getFirstChild().getNext();
                    if (method.getType() == Token.STRING) {
                        String methodName = method.getString();
                        Node clzNode = parent;
                        while (clzNode != null) {
                            if (clzNode.getType() == Token.FUNCTION) {
                                // Determine function name
                                String ancestorName = getFunctionFqn(clzNode, null);
                                if (ancestorName != null) {
                                    int lastDot = ancestorName.lastIndexOf('.');
                                    if (lastDot != -1 && Character.isUpperCase(ancestorName.charAt(lastDot+1))) {
                                            return ancestorName + '.' + methodName;
                                    } else if (Character.isUpperCase(ancestorName.charAt(0))) {
                                        return ancestorName + '.' + methodName;
                                    }
                                }
                            }
                            clzNode = clzNode.getParentNode();
                        }
                    }
                } else if (firstChild.getType() == Token.GETPROP &&
                        firstChild.hasChildren() && firstChild.getFirstChild().getType() == Token.THIS) {
                    // this.foo.bar = function() { }
                    wasThis = true;
                    Node method = parent.getFirstChild().getNext();
                    if (method.getType() == Token.STRING) {
                        String methodName = AstUtilities.getJoinedName(parent);
                        Node clzNode = parent;
                        while (clzNode != null) {
                            if (clzNode.getType() == Token.FUNCTION) {
                                // Determine function name
                                String ancestorName = getFunctionFqn(clzNode, null);
                                if (ancestorName != null) {
                                    int lastDot = ancestorName.lastIndexOf('.');
                                    if (lastDot != -1 && Character.isUpperCase(ancestorName.charAt(lastDot+1))) {
                                            return ancestorName + '.' + methodName;
                                    } else if (Character.isUpperCase(ancestorName.charAt(0))) {
                                        return ancestorName + '.' + methodName;
                                    }
                                }
                            }
                            clzNode = clzNode.getParentNode();
                        }
                    }
                }
                // Foo.Bar.baz = function() { }
                StringBuilder sb = new StringBuilder();
                if (addName(sb, parent)) {
                    name = sb.toString();
                }
            }
            
            if (name != null) {
                // See if we're inside a with-statement
                String in = getSurroundingWith(parent);
                if (in != null) {
                    name = in + "." + name; // NOI18N
                }
                
                // Determine is instance
                // function Foo() {} is an instance, Foo.Bar = function() is not.
                boolean instance = wasThis || name.indexOf('.') == -1;
                if (name.indexOf(DOT_PROTOTYPE) != -1) {
                    name = name.replace(DOT_PROTOTYPE, ""); // NOI18N
                    instance = true;
                }
                if (isInstance != null) {
                    isInstance[0] = instance;
                }
            }
            
            return name;
        }

        return funcName;
    }
    
    /** Return an array of strings: string[0]=class, string[1]=extends, defined by the objectlit. Both can be null. */
    public static String[] getObjectLitFqn(Node node) {
        assert node.getType() == Token.OBJECTLIT;
        // Foo.Bar = { foo : function() }
        Node parent = node.getParentNode();

        String className = null;
        String extendsName = null;
        int parentType = parent != null ? parent.getType() : 0;
        
        if (parentType == Token.RETURN) {
            // The function is returning an object literal containing a function;
            // this could be something like the syntax used in YAHOO.util.Event:
            // Locate the surrounding function.
            Node func = parent.getParentNode();
            for (; func != null; func = func.getParentNode()) {
                if (func.getType() == Token.FUNCTION) {
                    String funcName = getFunctionFqn(func, null);
                    if (funcName != null) {
                        if (!Character.isUpperCase(funcName.charAt(0))) {
                            // Don't treat something like
                            //   dojo._foo = function() {  foo(); return { x: 50, y:60 }
                            
                            return new String[] { null, null};
                        }
                        return new String[] { funcName, null };
                    }
                    break;
                } else if (func.getType() == Token.OBJLITNAME) {
                    return getObjectLitFqn(func);
                }
            }
        }
        
        if (parentType == Token.NAME) {
            className = parent.getString();
        } else if (parentType == Token.SETPROP || parentType == Token.SETNAME) {
            StringBuilder sb = new StringBuilder();
            if (AstUtilities.addName(sb, parent)) {
                className = sb.toString();
            }
        } else if (parentType == Token.CALL && parent.getParentNode() != null &&
                (parent.getParentNode().getType() == Token.SETPROP ||
                 parent.getParentNode().getType() == Token.NAME)) {
            // Look for patterns of the form
            //   Pirate.prototype = Object.extend(new Person(), { say: function() {} });
            // This looks like this:
            //
            //SETPROP
            //  NAME:"Pirate"
            //  STRING:"prototype"
            //  CALL
            //      GETPROP
            //          NAME:"Object"
            //          STRING:"extend"
            //      NEW
            //          NAME:"Person"
            //      OBJECTLIT
            Node callTarget = parent.getFirstChild();
            if (callTarget != null && callTarget.getType() == Token.GETPROP) {
                Node clz = callTarget.getFirstChild();
                Node mtd = clz != null ? clz.getNext() : null;
                if (clz != null && mtd != null) {
                    if (clz.getType() == Token.NAME && "Object".equals(clz.getString()) && // NOI18N
                        mtd.getType() == Token.STRING && "extend".equals(mtd.getString())) { // NOI18N
                        StringBuilder sb = new StringBuilder();
                        if (AstUtilities.addName(sb, parent.getParentNode())) {
                            className = sb.toString();
                            Node prev = parent.getFirstChild().getNext();
                            if (prev != null && prev.getType() == Token.NEW && prev.getFirstChild() != null) {
                                StringBuilder extend = new StringBuilder();
                                if (AstUtilities.addName(extend, prev.getFirstChild())) {
                                    extendsName = extend.toString();
                                    if (extendsName.length() == 0) {
                                        extendsName = null;
                                    }
                                }
                            }
                        }
                    } else if (clz.getType() == Token.NAME && "Class".equals(clz.getString()) && // NOI18N
                        mtd.getType() == Token.STRING && "create".equals(mtd.getString())) { // NOI18N
                        // Prototype.js new-style:
                        // var Person = Class.create({
                        // and
                        // var Pirate = Class.create(Person, {
                        StringBuilder sb = new StringBuilder();
                        if (AstUtilities.addName(sb, parent.getParentNode())) {
                            className = sb.toString();
                            Node firstArg = callTarget.getNext();
                            if (firstArg.getType() == Token.NAME || firstArg.getType() == Token.GETPROP) {
                                StringBuilder extend = new StringBuilder();
                                if (AstUtilities.addName(extend, firstArg)) {
                                    extendsName = extend.toString();
                                    if (extendsName.length() == 0) {
                                        extendsName = null;
                                    }
                                }
                            }
                        }
                    } else if (clz.getType() == Token.NAME && "Ext".equals(clz.getString()) && // NOI18N
                        mtd.getType() == Token.STRING && "extend".equals(mtd.getString())) { // NOI18N
                        // Ext extensions
                        // Ext.TabPanel = Ext.extend(Ext.Panel,  {
                        StringBuilder sb = new StringBuilder();
                        if (AstUtilities.addName(sb, parent.getParentNode())) {
                            className = sb.toString();
                            Node firstArg = callTarget.getNext();
                            if (firstArg.getType() == Token.NAME || firstArg.getType() == Token.GETPROP) {
                                StringBuilder extend = new StringBuilder();
                                if (AstUtilities.addName(extend, firstArg)) {
                                    extendsName = extend.toString();
                                    if (extendsName.length() == 0) {
                                        extendsName = null;
                                    }
                                }
                            }
                        }
                    } else if (parentType == Token.CALL && parent.getParentNode() != null &&
                            (parent.getFirstChild().getType() == Token.GETPROP &&
                            parent.getFirstChild().getNext() != null &&
                            parent.getFirstChild().getNext().getType() == Token.GETPROP &&
                            parent.getFirstChild().getNext().getNext() == node)) {
                        Node first = parent.getFirstChild();
                        String joined = AstUtilities.getJoinedName(first);
                        if (joined.endsWith(".extend") && joined.indexOf("dojo") != -1) {
                            className = AstUtilities.getJoinedName(first.getNext());
                        }
                    }
                }
            }
        } else if (parentType == Token.CALL) {
            // Ext:
            // Ext.extend(Ext.tree.AsyncTreeNode, Ext.tree.TreeNode, {
            Node callTarget = parent.getFirstChild();
            if (callTarget != null && callTarget.getType() == Token.GETPROP) {
                Node clz = callTarget.getFirstChild();
                Node mtd = clz != null ? clz.getNext() : null;
                if (clz != null && mtd != null) {
                    if (clz.getType() == Token.NAME && "Ext".equals(clz.getString()) && // NOI18N
                        mtd.getType() == Token.STRING && "extend".equals(mtd.getString())) { // NOI18N

                        Node first = callTarget.getNext();
                        if (first != null && (first.getType() == Token.NAME || first.getType() == Token.GETPROP)) {
                            Node second = first.getNext();
                            if (second != null && (second.getType() == Token.NAME || second.getType() == Token.GETPROP)) {
                                StringBuilder clzName = new StringBuilder();
                                if (AstUtilities.addName(clzName, first)) {
                                    className = clzName.toString();
                                    StringBuilder superName = new StringBuilder();
                                    if (AstUtilities.addName(superName, second)) {
                                        extendsName = superName.toString();
                                        if (extendsName.length() == 0) {
                                            extendsName = null;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (mtd.getType() == Token.STRING && "declare".equals(mtd.getString()) && // NOI18N
                            (clz.getType() == Token.NAME && "dojo".equals(clz.getString()) || // NOI18N
                            clz.getType() == Token.GETPROP && AstUtilities.getJoinedName(clz).endsWith("dojo"))) { // NOI18N
                        Node first = callTarget.getNext();
                        if (first != null) {
                            className = AstUtilities.getJoinedName(first);
                            Node second = first.getNext();
                            if (second != null) {
                                extendsName = AstUtilities.getJoinedName(second);
                                if (extendsName.length() == 0) {
                                    extendsName = null;
                                }
                            }
                        }
                    } else if (parent.getParentNode() != null &&
                            (parent.getFirstChild().getType() == Token.GETPROP &&
                            parent.getFirstChild().getNext() != null &&
                            parent.getFirstChild().getNext().getType() == Token.GETPROP &&
                            parent.getFirstChild().getNext().getNext() == node)) {
                        Node first = parent.getFirstChild();
                        String joined = AstUtilities.getJoinedName(first);
                        if (joined.endsWith(".extend") && joined.indexOf("dojo") != -1) { // NOI18N
                            className = AstUtilities.getJoinedName(first.getNext());
                        }
                    }
                }
            }
        }
        // TODO: Ext  "this.addEvents()"

        String[] result = new String[2];
        result[0] = className;
        result[1] = extendsName;
        
        return result;
    }

    public static String getJoinedName(Node node) {
        StringBuilder sb = new StringBuilder();
        addName(sb, node);
        return sb.toString();
    }
    
    public static boolean addName(StringBuilder sb, Node node) {
        switch (node.getType()) {
        case Token.BINDNAME:
        case Token.NAME:
        case Token.STRING: {
            String s = node.getString();

            // Skip prototype in name - but do we need this in the metadata
            // somewhere?
            //if ("prototype".equals(s)) { // NOI18N
            //    return true;
            //}
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(s);
            return true;
        }
        case Token.SETPROP:
            if (node.hasChildren()) {
                Node child = node.getFirstChild();
                addName(sb, child);
                child = child.getNext();
                if (child != null) {
                    addName(sb, child);
                }

                return true;
            }
            break;

        case Token.SETNAME:
        case Token.GETPROP: {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addName(sb, child);
            }
            break;
        }
        }

        return true;
    }
    
    public static String getFqn(AstPath path, boolean[] isInstance, String[] method) {
        Node clzNode = path.leaf();
        return getFqn(clzNode, isInstance, method);
    }
    
    public static String getFqn(Node node, boolean[] isInstance, String[] method) {
        Node methodNode = null;
        while (node != null) {
            if (node.getType() == Token.FUNCTION) {
                // Determine function name
                String fqn = getFunctionFqn(node, isInstance);
                if (fqn != null && methodNode == null) {
                    methodNode = node;
                }
                if (fqn != null && Character.isUpperCase(fqn.charAt(0))) {
                    
                    int lastDot = fqn.lastIndexOf('.');
                    if (lastDot != -1 && lastDot < fqn.length()-1) {
                        if (!Character.isUpperCase(fqn.charAt(lastDot+1))) {
                            if (method != null) {
                                method[0] = fqn.substring(lastDot+1);
                            }
                            fqn = fqn.substring(0, lastDot);
                        }
                    }
                    
                    return fqn;
                }
            } else if (node.getType() == Token.OBJECTLIT) {
                String fqn = getObjectLitFqn(node)[0];
                if (fqn != null) {
                    if (fqn.endsWith(DOT_PROTOTYPE)) {
                        fqn = fqn.substring(0, fqn.length()-DOT_PROTOTYPE.length());
                    }
                    
                    if (method != null && methodNode != null) {
                        // Find the method
                        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                            if (child.getType() == Token.OBJLITNAME) {
                                Node f = AstUtilities.getLabelledNode(child);
                                if (f == methodNode) {
                                    method[0] = child.getString();
                                    break;
                                }
                            }
                        }
                    }
                    
                    return fqn;
                }
            }
            node = node.getParentNode();
        }
        return null;
    }
    
    public static String getExpressionType(Node node) {
        // XXX See also JsTypeAnalyzer.expressionType
        String type;
        switch (node.getType()) {
        case Token.FALSE:
        case Token.TRUE:
        case Token.NE:
        case Token.EQ:
        case Token.LT:
        case Token.GT:
        case Token.LE:
        case Token.GE:
        case Token.AND:
        case Token.OR:
        case Token.NOT:
            type = "Boolean"; // NOI18N
            break;
        case Token.NUMBER:
            type = "Number"; // NOI18N
            break;
        case Token.STRING:
            if ("undefined".equals(node.getString())) {
                type = Node.UNKNOWN_TYPE;
            } else {
                type = "String"; // NOI18N
            }
            break;
        case Token.REGEXP:
            type = "RegExp"; // NOI18N
            break;
        case Token.ARRAYLIT:
            // TODO - iterate over the children to see if I can compute
            // the type of the children, e.g. Array<Number> for something
            // like  [0,1,2]
            type = "Array"; // NOI18N
            break;
        case Token.FUNCTION:
            type = "Function"; // NOI18N
            break;
        case Token.UNDEFINED:
            type = "Undefined";
            break;
        case Token.VOID:
            type = "void"; // NOI18N
            break;
        case Token.NEW: {
            Node first = AstUtilities.getFirstChild(node);
            if (first != null) {
                type = AstUtilities.getJoinedName(first);
            } else {
                type = Node.UNKNOWN_TYPE;
            }
            break;
        }
        case Token.GETPROP: {
            type = AstUtilities.getJoinedName(node);
            int dot = type.lastIndexOf('.');
            if (dot != -1) {
                // "foo.bar" is not a type, but "Foo.Bar" is, as is "foo"
                // (because many "classes" like jMaki start with a lowercase
                // letter so I can't just filter on these.)
                if (!Character.isUpperCase(type.charAt(dot+1))) {
                    type = null;
                }
            }
            break;
        }
        default:
            type = Node.UNKNOWN_TYPE;
        }
        return type;
    }
    
    public static String getSurroundingWith(Node node) {
        Node n = node;
        for (; n != null; n = n.getParentNode()) {
            int t = n.getType();
            if (t == Token.FUNCTION) {
                break;
            }
            if (t == Token.WITH) {
                // Find enterwith node
                Node p = n.getParentNode().getFirstChild();
                while (p != null && p.getNext() != n) {
                    p = p.getNext();
                }
                if (p != null && p.getType() == Token.ENTERWITH && p.getFirstChild() != null) {
                    String in = AstUtilities.getJoinedName(p.getFirstChild());
                    if (in != null && in.length() > 0) {
                        return in;
                    }
                }

                break;
            }
        }
        
        return null;
    }
}
