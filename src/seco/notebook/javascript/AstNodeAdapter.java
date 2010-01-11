/*
 * AstNodeAdapter.java
 *
 * Created on October 17, 2006, 8:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package seco.notebook.javascript;

//import com.sun.phobos.script.javascript.lang.ast.AssignmentNode;
//import com.sun.phobos.script.javascript.lang.ast.CallNode;
//import com.sun.phobos.script.javascript.lang.ast.DeclarationNode;
//import com.sun.phobos.script.javascript.lang.ast.ExprNode;
//import com.sun.phobos.script.javascript.lang.ast.FunctionNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

//import org.mozilla.nb.javascript.Node;
//import com.sun.phobos.script.javascript.lang.ast.Node;
//import com.sun.phobos.script.javascript.lang.ast.PropertyGetNode;
//import com.sun.phobos.script.javascript.lang.ast.TreeWalker;
//import com.sun.phobos.script.javascript.lang.ast.VariableNode;
//import com.sun.semplice.javascript.AstNodeAdapter;
//import org.netbeans.api.languages.parsing.ParserResult;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
//import org.openide.util.Enumerations;


/** For debugging purposes only */
class AstNodeAdapter implements TreeNode {
    private Node node;
    private AstNodeAdapter parent;
    private AstNodeAdapter[] children;
    private List<AstNodeAdapter> childrenList;
    int endOffset = -1;

    AstNodeAdapter(AstNodeAdapter parent, Node node) {
        this.parent = parent;
        this.node = node;
    }

    
    
    private void ensureChildrenInitialized() {
        if (children != null) {
            return;
        }
//
        //            List<AstNodeAdapter> subnodes = new ArrayList<AstNodeAdapter>();
        //            Node child = node.getFirstChild();
        //            while (child != null) {
        //                subnodes.add(new AstNodeAdapter(this, child));
        //                child = child.getNext();
        //            }
        //            children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
        List<AstNodeAdapter> subnodes = new ArrayList<AstNodeAdapter>();

        // Functions need special treatment: Rhino has a weird thing in the AST
        // where the AST for each function is not made part of the overall tree...
        // So in these cases I've gotta go looking for it myself...
        if (node.getType() == Token.FUNCTION) {
            String name;

            if (node instanceof FunctionNode) {
                name = ((FunctionNode)node).getFunctionName();
            } else {
                name = node.getString();
            }
 
//            if (node.getParentNode() instanceof ScriptOrFnNode) {
//                ScriptOrFnNode sn = (ScriptOrFnNode)node.getParentNode();
//
//                for (int i = 0, n = sn.getFunctionCount(); i < n; i++) {
//                    FunctionNode func = sn.getFunctionNode(i);
//
//                    if (name.equals(func.getFunctionName())) {
//                        // Found the function
//                        Node current = func.getFirstChild();
//
//                        for (; current != null; current = current.getNext()) {
//                            subnodes.add(new AstNodeAdapter(this, current));
//                        }
//
//                        children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
//
//                        return;
//                    }
//                }
//            }
//
//            System.err.println("SURPRISE! It's (" + node +
//                " not a script node... revisit code--- some kind of error");
//            children = new AstNodeAdapter[0];
//            return;
        }

        if (node.hasChildren()) {
            Node current = node.getFirstChild();

            for (; current != null; current = current.getNext()) {
                // Already added above?
                //if (current.getType() == Token.FUNCTION) {
                //    continue;
                //}
                subnodes.add(new AstNodeAdapter(this, current));
            }

            children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
        } else {
            children = new AstNodeAdapter[0];
        }
        
    
//         children = new AstNodeAdapter[0];
        if (children == null) {
            // XXX This is likely a bug in the AST
            children = new AstNodeAdapter[0];
        }
    }

    
//    public static AstNodeAdapter createFromAst(Node root) {        
//        BuildingTreeWalker tw = new BuildingTreeWalker(root);
//        return tw.getAdapter();
//    }


    public TreeNode getChildAt(int i) {
        ensureChildrenInitialized();

        return children[i];
    }

    public int getChildCount() {
        ensureChildrenInitialized();

        return children.length;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode treeNode) {
        ensureChildrenInitialized();

        for (int i = 0; i < children.length; i++) {
            if (children[i] == treeNode) {
                return i;
            }
        }

        return -1;
    }

    public boolean getAllowsChildren() {
        ensureChildrenInitialized();

        return children.length > 0;
    }

    public boolean isLeaf() {
        ensureChildrenInitialized();

        return children.length == 0;
    }

    public Enumeration children() {
        ensureChildrenInitialized();

        return Collections.enumeration(Arrays.asList(children));
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
//        String type = node.getClass().getName();
//        type = type.substring(type.lastIndexOf('.')+1);
//        type = type.replace('$', '.');
//        sb.append(type);
//      
//        sb.append(":");
        sb.append(Token.fullName(node.getType()));
        
        String name = null;
        
        if (node instanceof Node.StringNode) {
            sb.append(":\"");
            sb.append(node.getString());
            sb.append("\"");
        } else if (node instanceof Node.NumberNode) {
            sb.append(":");
            sb.append(node.getDouble());
        }
//        if (node instanceof PropertyGetNode) {
//            name = ((PropertyGetNode)node).getName();
//        } else if (node instanceof CallNode) {
//            ExprNode target = ((CallNode)node).getTarget();
//            if (target instanceof PropertyGetNode) {
//                name = ((PropertyGetNode)target).getName();
//            }
//        } else if (node instanceof DeclarationNode) {
//            List<VariableNode> list = ((DeclarationNode)node).getVariables();
//            if (list != null && list.size() > 0) {
//                StringBuffer s = new StringBuffer();
//                for (VariableNode n : list) {
//                    if (s.length() > 0) {
//                        s.append(',');
//                    }
//                    s.append(n.getName());
//                }
//                name = s.toString();
//            }
//        } else if (node instanceof VariableNode) {
//            name = ((VariableNode)node).getName();
//        } else if (node instanceof FunctionNode) {
//            name = ((FunctionNode)node).getName();
//        } else if (node instanceof AssignmentNode) {
//            ExprNode lhs = ((AssignmentNode)node).getTarget();
//            if (lhs instanceof PropertyGetNode) {
//                name = ((PropertyGetNode)lhs).getName();
//            }
//        }
        if (name != null) {
            sb.append(" : ");
            sb.append(name);
            sb.append(' ');
            
        }
        
//        sb.append(Token.name(node.getType()));
        sb.append("(");
        sb.append(Integer.toString(getStartOffset()));
        sb.append("-");
        sb.append(Integer.toString(getEndOffset()));
        sb.append(") ");

//        if (node.isStringNode()) {
//            sb.append("\"");
//            sb.append(node.getString());
//            sb.append("\"");
//        } else {
//            String clz = node.getClass().getName();
//            sb.append(clz.substring(clz.lastIndexOf('.') + 1));
//        }
//
        return sb.toString();
    }

    public int getStartOffset() {
        return Math.max(0, node.getSourceStart());
    }

    public int getEndOffset() {
        return Math.max(0, node.getSourceEnd());
//        if (endOffset == -1) {
//            // Compute lazily, since it's not available in the AST.
//            // Take it to be the offset of the next sibling, or the offset of the 
//            // last child, whichever is greater
//            
//            if (parent != null) {
//                parent.ensureChildrenInitialized();
//                for (int i = 0; i < parent.children.length; i++) {
//                    if (parent.children[i] == this) {
//                        if (i < parent.children.length-1) {
//                            endOffset = parent.children[i+1].getStartOffset();
//                        }
//                        break;
//                    }
//                }
//            }
//            
//            if (endOffset == -1) {
//                ensureChildrenInitialized();
//                if (children.length > 0) {
//                    endOffset = children[children.length-1].getEndOffset(); // possibly recursive call
//                }
//            }
//            
//            if (endOffset == -1) {
//                endOffset = getStartOffset();
//            }
//        }
//        
//        return endOffset;
    }

    public Object getAstNode() {
        return node;
    }

//    static class BuildingTreeWalker implements TreeWalker {
//        private Node root;
//        private Stack<AstNodeAdapter> adapterStack = new Stack<AstNodeAdapter>();
//        private AstNodeAdapter rootAdapter;
//        
//        public BuildingTreeWalker(Node root) {
//            this.root = root;
//        }
//        
//        public AstNodeAdapter getAdapter() {
//            root.walk(this);
//            return rootAdapter;
//        }
//        
//        public void walk(Node node) {
//            //System.out.println("walking node " + node);
//            if (node != null) {
//                node.walk(this);
//            }
//        }
//
//        public void preWalk(Node node) {
//            AstNodeAdapter parent = null;
//            if (!adapterStack.empty()) {
//                parent = adapterStack.peek();
//            }
//            AstNodeAdapter adapter = new AstNodeAdapter(parent, node);
//            adapterStack.push(adapter);
//            if (node == root) {
//                rootAdapter = adapter;
//            }
//            
//            if (parent != null) {
//                if (parent.childrenList == null) {
//                    parent.childrenList = new ArrayList<AstNodeAdapter>();
//                }
//                parent.childrenList.add(adapter);
//            }
//        }
//
//        public void postWalk(Node node) {
//            adapterStack.pop();
//
//            AstNodeAdapter parent = null;
//            if (!adapterStack.empty()) {
//                parent = adapterStack.peek();
//            } else {
//                parent = rootAdapter;
//            }
//            if (parent.childrenList != null) {
//                parent.children = parent.childrenList.toArray(new AstNodeAdapter[parent.childrenList.size()]);
//            } else {
//                parent.children = new AstNodeAdapter[0];
//            }
//        }
//    };

}
