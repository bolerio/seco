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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;



/**
 * Perform type analysis on a given AST tree, attempting to provide a type
 * associated with each variable, field etc.
 *
 * @todo Track boolean types for simple operators; e.g.
 *    cc_no_width = letter == '[' && !width
 *    etc.  The operators here let me conclude cc_no_width is of type boolean!
 * @todo Use some statistical results to improve this; .to_s => String, .to_f => float,
 *   etc.
 * @todo I see some conventions in the JavaScript source for annotating returtn
 *   types and parameter types; see this from dojo.js for example:
 * <pre>
 *       dojo.trim = function(/*String*^/ str){
 *        return str.replace(/^\s\s*, '').replace(/\s\s*$/, ''); // String
 *  Watch out for this non-type:
 *     return o; /*anything*^/
 *  Look out for question marks too:
 *    /*int?*^/ delay, /*Boolean?*^/
 *  and nontypes like "int".
 *  (In all of the above, *^/ should really be the comment terminator.)
 * 
 * @todo prototype.js adds the $() function which extends objects passed through it as elements,
 *   so treat these as elements!
 * @todo When you see Element.extend, also resolve the result as an Element
        var my_div = document.createElement('div');

        Element.extend(my_div);
        my_div.addClassName('pending').hide();
 * 
 * </pre>
 * 
 * @todo Handle type unions (of the form Foo|Bar|Baz) - queries need to look at all.
 * @todo Document should be of type HTMLDocument
 * @todo "return this" -> should set the type of the current type (and make sure "return;" makes it void.)
 * @todo Handle type unions (String|Number|RegExp) - and make sure you remove void from these 
 * @todo For arrays, change {@code Array<String>} into String[] in the display name
 * @todo Handle type-parameters for arrays (strip out {@code <>}'s from Array when going to compute types
 * @todo Make sure I can handle common document.create() operations
 *    Nope! document.createElement("faen").
 * @todo If you call  jQuery.html(). and ask for the type of this expression, I sometimes don't get it
 *   right - because I have both jQuery.html(value) with no known return value, and jQuery.html() with
 *   a known return value. IF I pick the first html function to look up the type for, this fails.
 *   I need to consider the function arity and do a better job looking up the type in this case!
 *
 * @author Tor Norbye
 */
public class JsTypeAnalyzer {

    /** Map from variable or field(etc) name to type. */
    private Map<String, String> types;
    private final int astOffset;
    private final Node root;
    /** Node we are looking for;  */
    private Node target;
    private final JsParseResult info;
    private long startTime;
    
    // Generated with
    //  /bin/grep "^var" *.js  | grep new | awk '{print "BROWSER_BUILTINS.put(\"" $2 "\",\"" $5 "\");"}'
    private static Map<String,String> BROWSER_BUILTINS = new HashMap<String,String>();
    static {
        // Is this obsolete now?
        BROWSER_BUILTINS.put("java","Java");
        BROWSER_BUILTINS.put("netscape","Netscape");
        BROWSER_BUILTINS.put("sun","Sun");
        BROWSER_BUILTINS.put("cssRule","CssRule");
        BROWSER_BUILTINS.put("document","HTMLDocument");
        BROWSER_BUILTINS.put("element","HTMLElement");
        BROWSER_BUILTINS.put("event","Event");
        //BROWSER_BUILTINS.put("form","Form");
        BROWSER_BUILTINS.put("navigator","Navigator");
        //BROWSER_BUILTINS.put("range","Range");
        //BROWSER_BUILTINS.put("screen","Screen");
        BROWSER_BUILTINS.put("style","Style");
        BROWSER_BUILTINS.put("stylesheet","Stylesheet");
        BROWSER_BUILTINS.put("table","Table");
        //BROWSER_BUILTINS.put("tableRow","TableRow");
        BROWSER_BUILTINS.put("treeWalker","TreeWalker");
        BROWSER_BUILTINS.put("window","Window");
    }

    /** Creates a new instance of JsTypeAnalyzer for a given position.
     * The {@link #analyze} method will do the rest. */
    public JsTypeAnalyzer(JsParseResult info, Node root, Node target, int astOffset, int lexOffset) {
        this.info = info;
        this.root = root;
        this.target = target;
        this.astOffset = astOffset;
    }
    
    /**
     * Determine if the given expression depends on local variables.
     * If it does not, we can skip tracking variables through the functon
     * and only compute the current expression.
     */
    private boolean dependsOnLocals() {
        // Find current expression
        Node n = target;
        Node prev = null;
        while (n != null) {
            int type = n.getType();
            if (type == Token.EXPR_RESULT ||
                type == Token.EXPR_VOID ||
                type == Token.CALL ||
                type == Token.FUNCTION) {
                break;
            }
            
            prev = n;
            n = n.getParentNode();
        }
        
        if (n == null) {
            n = prev;
            if (n == null) {
                return false;
            }
        }
        
        // See if the tree contain any local-variable references
        return hasLocalRefs(n, n.getParentNode());
    }
    
    private boolean hasLocalRefs(Node n, Node p) {
        if (n.getType() == Token.NAME) {
            if (p == null) {
                return true;
            } else if (p.getType() != Token.GETPROP || Character.isLowerCase(n.getString().charAt(0))) {
                return true;
            }
        }
        
        if (root.hasChildren()) {
            for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
                boolean result = hasLocalRefs(child, n);
                if (result) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Analyze the given code block down to the given offset. The {@link #getType}
     * method can then be used to read out the symbol type if any at that point.
     * Returns the type of the current expression, if known.
     */
    private String analyze(Node node) {
        // Avoid including definitions appearing later in the
        // context than the caret. (This only works for local variable
        // analysis; for fields it could be complicated by code earlier
        // than the caret calling code later than the caret which initializes
        // the fild...
        if (node == target) {
            target = null;
        }

        if (target == null && node.getSourceStart() > astOffset) {
            return null;
        }

        // Algorithm: walk AST and look for assignments and such.
        // Attempt to compute the type of each expression and
        switch (node.getType()) {
        case Token.VAR: {
            Node first = AstUtilities.getFirstChild(node);
            if (first != null) {
                Node rhs = AstUtilities.getFirstChild(first);
                if (rhs != null) {
                    String name = first.getString();
                    String type = expressionType(rhs);
                    setType(type, name);
                }
            }
            break;
        }
        
        case Token.SETNAME: {
            String name = AstUtilities.getFirstChild(node).getString();
            Node expr = AstUtilities.getSecondChild(node);
            if (expr != null) {
                String type = expressionType(expr);
                setType(type, name);
            }
            break;
        }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            analyze(child);
        }
        
        return null;
    }

    /** Called on AsgnNodes to compute RHS 
     * XXX See also AstUtilities.getExpressionType
     */
    private String expressionType(Node node) {
        switch (node.getType()) {
        case Token.NUMBER:
            return "Number"; // NOI18N
        case Token.STRING:
            // If I try to compute the type of an expression involving a property lookup
            // where the second node is a String this isn't really a String type...
            if (node.getParentNode() == null || node.getParentNode().getType() != Token.GETPROP ||
                    node == node.getParentNode().getFirstChild()) {
                return "String"; // NOI18N
            } else {
                // String node used as name in property lookup
                return null;
            }
        case Token.GETELEM: {
            String lhs = expressionType(node.getFirstChild());
            if (lhs != null) {
                if (lhs.startsWith("Array<")) { // NOI18N
                    int end = lhs.lastIndexOf('>');
                    if (end != -1) {
                        return lhs.substring("Array<".length(), end); // NOI18N
                    }
                } else if ("HTMLCollection".equals(lhs)) { // NOI18N
                    return "Node"; // NOI18N
                }
            }
            
            return null;
        }
        case Token.REGEXP:
            return "RegExp"; // NOI18N
        case Token.NE:
        case Token.EQ:
        case Token.LT:
        case Token.GT:
        case Token.LE:
        case Token.GE:
        case Token.TRUE:
        case Token.FALSE:
            return "Boolean"; // NOI18N
        case Token.ARRAYLIT:
            return "Array"; // NOI18N
        case Token.FUNCTION:
            return "Function"; // NOI18N
        case Token.NEW: {
            Node first = AstUtilities.getFirstChild(node);
            if (first.getType() == Token.NAME) {
                return first.getString();
            } else {
                return expressionType(first);
            }
        }
        case Token.CALL: {
            Node first = node.getFirstChild();
            if (first.getType() == Token.NAME) {
                String s = first.getString();
                // TODO - check whether we're using prototype or jquery here
                if ("$".equals(s)) { // NOI18N
                    // Determine if we're using jquery or prototype
                    JsParseResult js = AstUtilities.getParseResult(info);
                    boolean jQuery = false;
                    for (String imp : js.getStructure().getImports()) {
                        if (imp.indexOf("jquery") != -1) { // NOI18N
                            jQuery = true;
                        }
                    }
                    if (jQuery) {
                        return "jQuery"; // NOI18N
                    } else {
                        return "Element"; // NOI18N
                    }
                }
                
                return FunctionCache.INSTANCE.getType(s);
            } else if (first.getType() == Token.GETPROP) {
                // Chained - figure out the type of this call before
                // continuing
                Node grandChild = first.getFirstChild();
                if (grandChild.getType() == Token.CALL) {
                    String lhs = expressionType(grandChild);
                    if (lhs != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = lhs + "." + method; // NOI18N
                            return FunctionCache.INSTANCE.getType(fqn);
                        }
                    }
                } else if (grandChild.getType() == Token.NAME) {
                    String name = grandChild.getString();
                    //String lhs = types.get(name);
                    String lhs = getTypeInternal(name);
                    if (lhs == null) {
                        lhs = FunctionCache.INSTANCE.getType(name);
                        if (lhs == null) {
                            lhs = name;
                        }
                    }
                    if (lhs != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = lhs + "." + method; // NOI18N
                            return FunctionCache.INSTANCE.getType(fqn);
                        }
                    }
                } else {
                    String type = expressionType(grandChild);
                    if (type != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = type + "." + method; // NOI18N
                            return FunctionCache.INSTANCE.getType(fqn);
                        }
                    }
                }
            } else {
                if (System.currentTimeMillis() > startTime+2000) {
                    // Don't do a huge amount of computation here
                    return null;
                }
                String s = AstUtilities.getCallName(node, true);
                if (s != null && s.length() > 0) {
                    return FunctionCache.INSTANCE.getType(s);
                }
            }
            break;
        }

        case Token.E4X: {
            if (node.getFirstChild() != null && node.getFirstChild().getType() == Token.STRING) {
                return "XML<" + node.getFirstChild().getString() + ">"; // NOI18N
            }
            return "XML"; // NOI18N
        }

        case Token.NAME: {
            //String name = node.getString();
            return getTypeInternal(node.getString());
            //return types.get(name);
        }
        case Token.GETPROP: {
            Node first = AstUtilities.getFirstChild(node);
            Node second = AstUtilities.getSecondChild(node);
            if (second.getType() == Token.MISSING_DOT) {
                String type = expressionType(first);
                if (type != null) {
                    if (!Character.isUpperCase(type.charAt(0))) {
                        String s = FunctionCache.INSTANCE.getType(type);
                        if (s != null) {
                            type = s;
                        }
                    }
                }
                
                return type;
            }
            String firstType = expressionType(first);
            if (firstType == null) {
                if (!(first instanceof Node.StringNode)) {
                    // I'm not sure why this happens... 
                    // but see http://statistics.netbeans.org/analytics/detail.do?id=39154
                    // Investigate this
                    return null;
                }
                firstType = first.getString();
            }
            String secondStr = second.getString();
            assert second.getType() == Token.STRING;
            String fqn = firstType + "." + secondStr;
            if (firstType.startsWith("XML<")) { // NOI18N
                // Special handling for E4X
                fqn = firstType;
            }
            String type = FunctionCache.INSTANCE.getType(fqn);
            if (type != null) {
                return type;
            } else {
                return fqn;
            }
        }
        }
        
        return null;
    }
    
    /** Return the type of the given expression node */
    public String getType(Node node) {
        if (dependsOnLocals()) {
            init();
        }
        
        String type = expressionType(node);

        if (type != null && type.startsWith("Array<")) { // NOI18N
            return "Array"; // NOI18N
        }
        
        return type;
    }
    
    public static String getCallFqn(JsParseResult info, Node callNode, boolean resolveLocals) {
        Node methodNode = callNode.getParentNode();
        while (methodNode != null) {
            if (methodNode.getType() == Token.FUNCTION) {
                break;
            }
            methodNode = methodNode.getParentNode();
        }
        if (methodNode == null) {
            methodNode = info.getRootNode();
        }
        JsTypeAnalyzer analyzer = new JsTypeAnalyzer(info, methodNode, callNode, 0, 0);
        if (resolveLocals && analyzer.dependsOnLocals()) {
            analyzer.init();
        }

        String type = analyzer.getCallExpressionType(callNode);

        return type;
    }
    
    private String getCallExpressionType(Node node) {
        switch (node.getType()) {
        case Token.NEW:
        case Token.CALL: {
            Node first = node.getFirstChild();
            if (first.getType() == Token.NAME) {
                String s = first.getString();
                return s;
            } else if (first.getType() == Token.GETPROP) {
                // Chained - figure out the type of this call before
                // continuing
                Node grandChild = first.getFirstChild();
                if (grandChild.getType() == Token.CALL) {
                    String lhs = expressionType(grandChild);
                    if (lhs != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = lhs + "." + method; // NOI18N
                            return fqn;
                        }
                    }
                } else if (grandChild.getType() == Token.NAME) {
                    String name = grandChild.getString();
                    //String lhs = types.get(name);
                    String lhs = getTypeInternal(name);
                    if (lhs == null) {
                        lhs = FunctionCache.INSTANCE.getType(name);
                        if (lhs == null) {
                            lhs = name;
                        }
                    }
                    if (lhs != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = lhs + "." + method; // NOI18N
                            return fqn;
                        }
                    }
                } else {
                    String type = expressionType(grandChild);
                    if (type != null) {
                        Node methodNode = grandChild.getNext();
                        if (methodNode.getType() == Token.STRING) {
                            String method = methodNode.getString();
                            String fqn = type + "." + method; // NOI18N
                            return fqn;
                        }
                    }
                }
            } else {
                if (System.currentTimeMillis() > startTime+2000) {
                    // Don't do a huge amount of computation here
                    return null;
                }
                String s = AstUtilities.getCallName(node, true);
                if (s != null && s.length() > 0) {
                    return s;
                }
            }
            break;
        }
        }
        
        return null;
    }
    
    private void init() {
        if (types == null) {
            startTime = System.currentTimeMillis();
            types = new HashMap<String, String>();

            analyze(root);
        }
    }

    /** Like getType(), but doesn't strip off array type parameters etc. */
    private String getTypeInternal(String symbol) {
        String type = null;
        
        if (types != null) {
            type = types.get(symbol);
        }
    
        if (type == null) {
            // Look for builtins
            type = BROWSER_BUILTINS.get(symbol);
            
            // Look in the index to see if this is a known type
            if (type == null) {
                // If the variable is local I shouldn't attempt to do this!!
                // Stash the result in the node itself
                type = FunctionCache.INSTANCE.getType(symbol);
//                // TODO - only do this if the symbol is a global variable (and on the index side,
//                // limit FQN matches to globals)
//                type = index.getType(symbol);
            }
        }
        
        return type;
    }

    /** Return the type of the given symbol */
    public String getType(String symbol) {
        init();

        String type = getTypeInternal(symbol);

        // We keep track of the types contained within Arrays
        // internally (and probably hashes as well, TODO)
        // such that we can do the right thing when you operate
        // on an Array. However, clients should only see the "raw" (and real)
        // type.
        if (type != null && type.startsWith("Array<")) { // NOI18N
            return "Array"; // NOI18N
        }
        
        return type;
    }
    
    private void setType(String type, String name) {

        if (type != null) {
            types.put(name, type);
        } else {
            // A more complicated expresion of some sort - we're no longer
            // sure of the type
            types.remove(name);
        }
    }
    
  
}
