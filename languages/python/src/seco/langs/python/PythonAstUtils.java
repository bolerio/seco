package seco.langs.python;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import seco.notebook.csl.ParserResult;
/**
 * Utility functions for dealing with the Jython AST
 *
 * @author Tor Norbye
 */
public class PythonAstUtils {
    private PythonAstUtils() {
        // This is just a utility class, no instances expected so private constructor
    }

    public static PythonTree getRoot(PythonParserResult r) {
        assert r instanceof PythonParserResult;

        PythonParserResult result = (PythonParserResult)r;

        return result.getRoot();
    }

   
    public static boolean isNameNode(PythonTree node) {
        if (node instanceof Name) {
            return true;
        }

        return false;
    }

    /** Return if a function is a staticmethod **/
    public static boolean isStaticMethod(PythonTree node) {
        if (node instanceof FunctionDef) {
            FunctionDef def = (FunctionDef)node;
            List<expr> decorators = def.getInternalDecorator_list();
            if (decorators != null && decorators.size() > 0) {
                for (expr decorator : decorators) {
                    if (decorator instanceof Name) {
                        String decoratorName = ((Name)decorator).getText();
                        if (decoratorName.equals("staticmethod")) { // NOI18N
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Compute the module/class name for the given node path */
    public static String getFqnName(AstPath path) {
        StringBuilder sb = new StringBuilder();

        Iterator<PythonTree> it = path.rootToLeaf();

        while (it.hasNext()) {
            PythonTree node = it.next();

            if (node instanceof ClassDef) {
                if (sb.length() > 0) {
                    sb.append('.'); // NOI18N
                }
                ClassDef cls = (ClassDef)node;
                sb.append(cls.getInternalName());
            }
        }

        return sb.toString();
    }

    /** Return the node for the local scope containing the given node */
    public static PythonTree getLocalScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return node;
            }
        }

        return path.root();
    }

    public static PythonTree getClassScope(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return node;
            }
        }

        return path.root();
    }

    public static ClassDef getClassDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return (ClassDef)node;
            }
        }

        return null;
    }

    public static boolean isClassMethod(AstPath path, FunctionDef def) {
        // Check to see if (a) the function is inside a class, and (b) it's
        // not nested in a function
        for (PythonTree node : path) {
            if (node instanceof ClassDef) {
                return true;
            }
            // Nested method private to this one?
            if (node instanceof FunctionDef && node != def) {
                return false;
            }
        }

        return false;
    }

    public static FunctionDef getFuncDef(AstPath path) {
        for (PythonTree node : path) {
            if (node instanceof FunctionDef) {
                return (FunctionDef)node;
            }
        }

        return null;
    }

    /**
     * Return true iff this call looks like a "getter". If we're not sure,
     * return the default value passed into this method, unknownDefault. 
     */
    public static boolean isGetter(Call call, boolean unknownDefault) {
        String name = PythonAstUtils.getCallName(call);
        if (name == null) {
            return unknownDefault;
        }

        return name.startsWith("get") || name.startsWith("_get"); // NOI18N
    }

    public static String getCallName(Call call) {
        expr func = call.getInternalFunc();

        return getExprName(func);
    }

    public static String getExprName(expr type) {
        if (type instanceof Attribute) {
            Attribute attr = (Attribute)type;
            return attr.getInternalAttr();
        } else if (type instanceof Name) {
            return ((Name)type).getInternalId();
        } else if (type instanceof Call) {
            Call call = (Call)type;
            return getExprName(call.getInternalFunc());
            //} else if (type instanceof Str) {
            //    return ((Str)type).getText();
        } else {
            return null;
        }
    }

    public static String getName(PythonTree node) {
        if (node instanceof Name) {
            return ((Name)node).getInternalId();
        }
        if (node instanceof Attribute) {
            Attribute attrib = (Attribute)node;
            String prefix = getName(attrib.getInternalValue());
            return (prefix + '.' + attrib.getInternalAttr());
        }
        NameVisitor visitor = new NameVisitor();
        try {
            Object result = visitor.visit(node);
            if (result instanceof String) {
                return (String)result;
            } else {
                // TODO HANDLE THIS!
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static List<String> getParameters(FunctionDef def) {
        arguments args = def.getInternalArgs();
        List<String> params = new ArrayList<String>();

        NameVisitor visitor = new NameVisitor();

        for (expr e : args.getInternalArgs()) {
            try {
                Object result = visitor.visit(e);
                if (result instanceof String) {
                    params.add((String)result);
                } else {
                    // TODO HANDLE THIS!
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        String vararg = args.getInternalVararg();
        if (vararg != null) {
            params.add(vararg);
        }
        String kwarg = args.getInternalKwarg();
        if (kwarg != null) {
            params.add(kwarg);
        }

        return params;
    }

    private static Str searchForDocNode(stmt stmt) {
        if (stmt instanceof Expr) {
            Expr expr = (Expr)stmt;
            expr value = expr.getInternalValue();
            if (value instanceof Str) {
                return (Str)value;
            }
        }

        return null;
    }

    public static Str getDocumentationNode(PythonTree node) {
        // DocString processing.
        // See http://www.python.org/dev/peps/pep-0257/

        // For modules, it's the first Str in the document.
        // For classes and methods, it's the first Str in the object.
        // For others, nothing.

        if (node instanceof FunctionDef) {
            // Function
            FunctionDef def = (FunctionDef)node;
            List<stmt> body = def.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        } else if (node instanceof ClassDef) {
            // Class
            ClassDef def = (ClassDef)node;
            List<stmt> body = def.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        } else if (node instanceof Module) {
            // Module
            Module module = (Module)node;
            List<stmt> body = module.getInternalBody();
            if (body != null && body.size() > 0) {
                return searchForDocNode(body.get(0));
            }
        }
        // TODO: As per http://www.python.org/dev/peps/pep-0257/ I should
        // also look for "additional docstrings" (Str node following a Str node)
        // and Assign str nodes

        return null;
    }

    public static String getStrContent(Str str) {
        String doc = str.getText();

        // Strip quotes
        // and U and/or R for unicode/raw string. U must always preceede r if present.
        if (doc.startsWith("ur") || doc.startsWith("UR") || // NOI18N
                doc.startsWith("Ur") || doc.startsWith("uR")) { // NOI18N
            doc = doc.substring(2);
        } else if (doc.startsWith("r") || doc.startsWith("u") || // NOI18N
                doc.startsWith("R") || doc.startsWith("U")) { // NOI18N
            doc = doc.substring(1);
        }

        if (doc.startsWith("\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r\"\"\"") && doc.endsWith("\"\"\"")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(3, doc.length() - 3);
        } else if (doc.startsWith("r'''") && doc.endsWith("'''")) { // NOI18N
            doc = doc.substring(4, doc.length() - 3);
        } else if (doc.startsWith("\"") && doc.endsWith("\"")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        } else if (doc.startsWith("'") && doc.endsWith("'")) { // NOI18N
            doc = doc.substring(1, doc.length() - 1);
        }

        return doc;
    }

    public static String getDocumentation(PythonTree node) {
        Str str = getDocumentationNode(node);
        if (str != null) {
            return getStrContent(str);
        }

        return null;
    }

   
   
  

    private static final class NameVisitor extends Visitor {
        @Override
        public Object visitName(Name name) throws Exception {
            return name.getInternalId();
        }
    }

  
    public static List<Name> getLocalVarNodes(PythonTree scope, String name) {
        LocalVarVisitor visitor = new LocalVarVisitor(name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<Name> getLocalVarAssignNodes(PythonTree scope, String name) {
        LocalVarAssignVisitor visitor = new LocalVarAssignVisitor(name, true, false);
        try {
            visitor.visit(scope);
            return visitor.getVars();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static class LocalVarVisitor extends Visitor {
        private List<Name> vars = new ArrayList<Name>();
        //private Set<OffsetRange> offsets = new HashSet<OffsetRange>();
        private String name;
       //
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarVisitor(String name, boolean collectNames, boolean collectOffsets) {
            //this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof Call && ((Call)parent).getInternalFunc() == node) {
                return super.visitName(node);
            }

            if ((name == null && !PythonUtils.isClassName(node.getInternalId(), false)) ||
                    (name != null && name.equals(node.getInternalId()))) {
//                if (collectOffsets) {
//                    OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
//                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
//                    if (lexRange != OffsetRange.NONE) {
//                        offsets.add(astRange);
//                    }
//                }
                if (collectNames) {
                    vars.add(node);
                }
            }

            return super.visitName(node);
        }

//        public Set<OffsetRange> getOffsets() {
//            return offsets;
//        }

        public List<Name> getVars() {
            return vars;
        }
    }

    private static class LocalVarAssignVisitor extends Visitor {
        private List<Name> vars = new ArrayList<Name>();
       // private Set<OffsetRange> offsets = new HashSet<OffsetRange>();
        private String name;
       // private CompilationInfo info;
        private boolean collectNames;
        private boolean collectOffsets;
        private PythonTree parent;

        private LocalVarAssignVisitor(String name, boolean collectNames, boolean collectOffsets) {
            //this.info = info;
            this.name = name;
            this.collectNames = collectNames;
            this.collectOffsets = collectOffsets;
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (parent instanceof FunctionDef || parent instanceof Assign) {
                if ((name == null && !PythonUtils.isClassName(node.getInternalId(), false)) ||
                        (name != null && name.equals(node.getInternalId()))) {
//                    if (collectOffsets) {
//                        OffsetRange astRange = PythonAstUtils.getNameRange(info, node);
//                        OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
//                        if (lexRange != OffsetRange.NONE) {
//                            offsets.add(astRange);
//                        }
//                    }
                    if (collectNames) {
                        vars.add(node);
                    }
                }
            }

            return super.visitName(node);
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            PythonTree oldParent = parent;
            parent = node;
            super.traverse(node);
            parent = oldParent;
        }

//        public Set<OffsetRange> getOffsets() {
//            return offsets;
//        }

        public List<Name> getVars() {
            return vars;
        }
    }

    /** Collect nodes of the given types (node.nodeId==NodeTypes.x) under the given root */
    public static void addNodesByType(PythonTree root, Class[] nodeClasses, List<PythonTree> result) {
        try {
            new NodeTypeVisitor(result, nodeClasses).visit(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class NodeTypeVisitor extends Visitor {
        private Class[] nodeClasses;
        private List<PythonTree> result;

        NodeTypeVisitor(List<PythonTree> result, Class[] nodeClasses) {
            this.result = result;
            this.nodeClasses = nodeClasses;
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            for (int i = 0; i < nodeClasses.length; i++) {
                if (node.getClass() == nodeClasses[i]) {
                    result.add(node);
                    break;
                }
            }

            super.traverse(node);
        }
    }

    public static Name getParentClassFromNode(AstPath path, PythonTree from, String name) {
        ClassDef curClass = (ClassDef)path.getTypedAncestor(ClassDef.class, from);
        if (curClass == null) {
            return null;
        }

        List<expr> baseClasses = curClass.getInternalBases();
        if (baseClasses == null) {
            return null; // no inheritance ;
        }
        int ii = 0;
        while (ii < baseClasses.size()) {
            if (baseClasses.get(ii) instanceof Name) {
                Name cur = (Name)baseClasses.get(ii);
                if (cur.getInternalId().equals(name)) {
                    return cur;
                }
            }
            ii++;
        }
        return null;
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    public static int findArgumentIndex(Call call, int astOffset, AstPath path) {

        // On the name part in the call rather than the args?
        if (astOffset <= call.getInternalFunc().getCharStopIndex()) {
            return -1;
        }
        List<expr> args = call.getInternalArgs();
        if (args != null) {
            int index = 0;
            for (; index < args.size(); index++) {
                expr et = args.get(index);
                if (et.getCharStopIndex() >= astOffset) {
                    return index;
                }
            }
        }

        // TODO what about the other stuff in there -- 
        //call.keywords;
        //call.kwargs;
        //call.starargs;

        return -1;
    }
}