package seco.langs.groovy;

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import groovyjarjarasm.asm.Opcodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
//import org.openide.filesystems.FileObject;
//import org.openide.util.Exceptions;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
//import org.netbeans.api.annotations.common.NonNull;
//import org.netbeans.api.lexer.Token;
//import org.netbeans.api.lexer.TokenSequence;
//import org.netbeans.api.lexer.TokenUtilities;
//import org.netbeans.editor.FinderFactory;
//import org.netbeans.editor.Utilities;
//import org.netbeans.modules.csl.api.OffsetRange;
//import org.netbeans.modules.csl.spi.ParserResult;
//import org.netbeans.modules.groovy.editor.api.elements.AstElement;
//import org.netbeans.modules.groovy.editor.api.elements.IndexedElement;
//import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
//import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
//import org.netbeans.modules.parsing.api.ParserManager;
//import org.netbeans.modules.parsing.api.ResultIterator;
//import org.netbeans.modules.parsing.api.Source;
//import org.netbeans.modules.parsing.api.UserTask;
//import org.netbeans.modules.parsing.spi.ParseException;
//import org.netbeans.modules.parsing.spi.Parser;

import seco.notebook.NotebookDocument;
import seco.notebook.Utilities;
import seco.notebook.csl.OffsetRange;
import seco.notebook.csl.ParserResult;

/**
 *
 * @author Martin Adamek
 */
public class AstUtilities {

    private static final Logger LOGGER = Logger.getLogger(AstUtilities.class.getName());

  

    @SuppressWarnings("unchecked")
    public static List<ASTNode> children(ASTNode root) {

        List<ASTNode> children = new ArrayList<ASTNode>();

        if (root instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) root;
            //children.addAll(moduleNode.getImports());
            children.addAll(moduleNode.getClasses());
            children.add(moduleNode.getStatementBlock());
        } else if (root instanceof ClassNode) {
            ClassNode classNode = (ClassNode) root;

            Set<String> possibleMethods = new HashSet<String>();
            for (Object object : classNode.getProperties()) {
                PropertyNode property = (PropertyNode) object;
                if (property.getLineNumber() >= 0) {
                    children.add(property);

                    FieldNode field = property.getField();
                    String name = field.getName();
                    if (name.length() > 0 && (field.getModifiers() & Opcodes.ACC_STATIC) == 0
                            && (field.getModifiers() & Opcodes.ACC_PRIVATE) != 0) {

                        // this is the groovy way, they don't specify en locale :(
                        name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
                        if ((field.getModifiers() & Opcodes.ACC_FINAL) == 0) {
                            possibleMethods.add("set" + name); // NOI18N
                        }
                        possibleMethods.add("get" + name); // NOI18N
                    }
                }

            }

            for (Object object : classNode.getFields()) {
                FieldNode field = (FieldNode) object;
                if (field.getLineNumber() >= 0) {
                    children.add(field);
                }
            }

            for (Object object : classNode.getMethods()) {
                MethodNode method = (MethodNode) object;
                // getMethods() returns all methods also from superclasses
                // how to get only methods from source?
                // for now, just check line number, if < 0 it is not from source
                // Second part of condition is for generated accessors
                if ((!method.isSynthetic() && method.getCode() != null)
                        || (method.isSynthetic() && possibleMethods.contains(method.getName()))) {
                    children.add(method);
                }

            }

            for (Object object : classNode.getDeclaredConstructors()) {
                ConstructorNode constructor = (ConstructorNode) object;

                if (constructor.getLineNumber() >= 0) {
                    children.add(constructor);
                }
            }



        } else if (root instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) root;
            children.add(methodNode.getCode());
            for (Parameter parameter : methodNode.getParameters()) {
                children.add(parameter);
            }
        } else if (root instanceof Parameter) {
        } else if (root instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) root;
            Expression expression = fieldNode.getInitialExpression();
            if (expression != null) {
                children.add(expression);
            }
        } else if (root instanceof PropertyNode) {
            // FIXME (?)
        } else if (root != null) {
            AstChildrenSupport astChildrenSupport = new AstChildrenSupport();
            root.visit(astChildrenSupport);
            children = astChildrenSupport.children();
        }

        return children;
    }

     /**
     * Compute the surrounding class name for the given node path or empty string
     * if none was found
     */
    public static String getFqnName(AstPath path) {
        ClassNode classNode = getOwningClass(path);
        return classNode == null ? "" : classNode.getName(); // NOI18N
    }

    public static ClassNode getOwningClass(AstPath path) {
        Iterator<ASTNode> it = path.rootToLeaf();
        while (it.hasNext()) {
            ASTNode node = it.next();
            if (node instanceof ClassNode) {
                return (ClassNode) node;

            }
        }
        return null;
    }

    public static ASTNode getScope(AstPath path, Variable variable) {
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode scope = it.next();
            if (scope instanceof ClosureExpression) {
                VariableScope variableScope = ((ClosureExpression) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                } else {
                    // variables defined inside closure are not catched in VariableScope
                    // let's get the closure's code block and try there
                    Statement statement = ((ClosureExpression) scope).getCode();
                    if (statement instanceof BlockStatement) {
                        variableScope = ((BlockStatement) statement).getVariableScope();
                        if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                            return scope;
                        }
                    }
                }
            } else if (scope instanceof MethodNode || scope instanceof ConstructorNode) {
                VariableScope variableScope = ((MethodNode) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                } else {
                    // variables defined inside method are not catched in VariableScope
                    // let's get the method's code block and try there
                    Statement statement = ((MethodNode) scope).getCode();
                    if (statement instanceof BlockStatement) {
                        variableScope = ((BlockStatement) statement).getVariableScope();
                        if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                            return scope;
                        }
                    }
                }
            } else if (scope instanceof ForStatement) {
                VariableScope variableScope = ((ForStatement) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof BlockStatement) {
                VariableScope variableScope = ((BlockStatement) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ClosureListExpression) {
                VariableScope variableScope = ((ClosureListExpression) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ClassNode) {
                ClassNode classNode = (ClassNode) scope;
                if (classNode.getField(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ModuleNode) {
                ModuleNode moduleNode = (ModuleNode) scope;
                BlockStatement blockStatement = moduleNode.getStatementBlock();
                VariableScope variableScope = blockStatement.getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return blockStatement;
                }
                // probably in script where variable is defined withoud 'def' keyword:
                // myVar = 1
                // echo myVar
                Variable classVariable = variableScope.getReferencedClassVariable(variable.getName());
                if (classVariable != null) {
                    return moduleNode;
                }
            }
        }
        return null;
    }

    /**
     * Doesn't check VariableScope if variable is declared there,
     * but assumes it is there and makes search for given variable
     */
    public static ASTNode getVariable(ASTNode scope, String variable, AstPath path, Element doc, int cursorOffset) {
        if (scope instanceof ClosureExpression) {
            ClosureExpression closure = (ClosureExpression) scope;
            for (Parameter parameter : closure.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = closure.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof MethodNode) {
            MethodNode method = (MethodNode) scope;
            for (Parameter parameter : method.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = method.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof ConstructorNode) {
            ConstructorNode constructor = (ConstructorNode) scope;
            for (Parameter parameter : constructor.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = constructor.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof ForStatement) {
            ForStatement forStatement = (ForStatement) scope;
            Parameter parameter = forStatement.getVariable();
            if (variable.equals(parameter.getName())) {
                return parameter;
            }
            Expression collectionExpression = forStatement.getCollectionExpression();
            if (collectionExpression instanceof ClosureListExpression) {
                ASTNode result = getVariableInClosureList((ClosureListExpression) collectionExpression, variable);
                if (result != null) {
                    return result;
                }
            }
            Statement code = forStatement.getLoopBlock();
            if (code instanceof BlockStatement) {
                ASTNode result = getVariableInBlockStatement((BlockStatement) code, variable);
                if (result != null) {
                    return result;
                }
            }
        } else if (scope instanceof BlockStatement) {
            return getVariableInBlockStatement((BlockStatement) scope, variable);
        } else if (scope instanceof ClosureListExpression) {
            return getVariableInClosureList((ClosureListExpression) scope, variable);
        } else if (scope instanceof ClassNode) {
            return ((ClassNode) scope).getField(variable);
        } else if (scope instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) scope;
            BlockStatement blockStatement = moduleNode.getStatementBlock();
            ASTNode result = getVariableInBlockStatement(blockStatement, variable);
            if (result == null) {
                // probably in script where variable is defined withoud 'def' keyword:
                // myVar = 1
                // echo myVar
                VariableScope variableScope = blockStatement.getVariableScope();
                if (variableScope.getReferencedClassVariable(variable) != null) {
                    // let's take first occurrence of the variable
                    VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, doc, cursorOffset);
                    scopeVisitor.collect();
                    Set<ASTNode> occurrences = scopeVisitor.getOccurrences();
                    if (!occurrences.isEmpty()) {
                        result = occurrences.iterator().next();
                    }
                }
            }
            return result;
        }
        return null;
    }

    private static ASTNode getVariableInBlockStatement(BlockStatement block, String variable) {
        for (Object object : block.getStatements()) {
            if (object instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) object;
                Expression expression = expressionStatement.getExpression();
                if (expression instanceof DeclarationExpression) {
                    DeclarationExpression declaration = (DeclarationExpression) expression;
                    if (variable.equals(declaration.getVariableExpression().getName())) {
                        return declaration.getVariableExpression();
                    }
                }
            }
        }
        return null;
    }

    private static ASTNode getVariableInClosureList(ClosureListExpression closureList, String variable) {
        for (Object object : closureList.getExpressions()) {
            if (object instanceof DeclarationExpression) {
                DeclarationExpression declaration = (DeclarationExpression) object;
                if (variable.equals(declaration.getVariableExpression().getName())) {
                    return declaration.getVariableExpression();
                }
            }
        }
        return null;
    }

    /**
     * Use this if you need some part of node that is not available as node.
     * For example return type of method definition is not accessible as node,
     * so I am wrapping MethodNode in this FakeASTNode and I also provide
     * text to compute OffsetRange for...
     */
    public static final class FakeASTNode extends ASTNode {

        private final String text;
        private final ASTNode orig;

        public FakeASTNode(ASTNode orig, String text) {
            this.orig = orig;
            this.text = text;
        }

        public ASTNode getOriginalNode() { return orig; }

        @Override
        public String getText() { return text; }

        @Override
        public void visit(GroovyCodeVisitor visitor) {}

    }

}
