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

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;


import seco.langs.groovy.AstUtilities.FakeASTNode;
import seco.notebook.csl.OffsetRange;

/**
 * @todo we should check type of variable where property is called, now we check only name, see visitPropertyExpression
 *
 * @author Martin Adamek
 */
public final class VariableScopeVisitor extends TypeVisitor {

    private final Set<ASTNode> occurrences = new HashSet<ASTNode>();
    private final ASTNode leafParent;

    public VariableScopeVisitor(SourceUnit sourceUnit, AstPath path, Element doc, int cursorOffset) {
        super(sourceUnit, path, doc, cursorOffset, true);
        this.leafParent = path.leafParent();
    }

    public Set<ASTNode> getOccurrences() {
        return occurrences;
    }

    @Override
    protected void visitParameters(Parameter[] parameters, Variable variable) {
        // method is declaring given variable, let's visit only the method,
        // but we need to check also parameters as those are not part of method visit
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(variable.getName())) {
                occurrences.add(parameter);
                break;
            }
        }
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified() && (leaf instanceof Variable)) {
            visitParameters(expression.getParameters(), (Variable) leaf);
        }
        super.visitClosureExpression(expression);
    }


//    protected boolean isValidToken(Token<? extends GroovyTokenId> token) {
//        // cursor must be positioned on identifier, otherwise occurences doesn't make sense
//        return token.id() == GroovyTokenId.IDENTIFIER;
//    }
    
    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(variableExpression.getName())) {
            occurrences.add(variableExpression);
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (variableExpression.getName().equals(property.getPropertyAsString())) {
                occurrences.add(variableExpression);
                return;
            }
        }
        super.visitVariableExpression(variableExpression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (leaf instanceof DeclarationExpression) {
            DeclarationExpression visitedDeclaration = expression;
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            VariableExpression visited = visitedDeclaration.getVariableExpression();
            if (!variable.isDynamicTyped() && !visited.isDynamicTyped()) {
                String name = variable.getType().getNameWithoutPackage();
                if (name.equals(visited.getType().getNameWithoutPackage())) {
                    FakeASTNode fakeNode = new FakeASTNode(expression, name);
                    occurrences.add(fakeNode);
                }
            }
        } else if (leaf instanceof ClassExpression) {
            ClassExpression clazz = (ClassExpression) leaf;
            VariableExpression variable = expression.getVariableExpression();
            if (!variable.isDynamicTyped()) {
                if (clazz.getType().getName().equals(variable.getType().getName())) {
                    FakeASTNode fakeNode = new FakeASTNode(expression, clazz.getType().getNameWithoutPackage());
                    occurrences.add(fakeNode);
                }
            }
        } else if (leaf instanceof ClassNode) {
            ClassNode clazz = (ClassNode) leaf;
            VariableExpression variable = expression.getVariableExpression();
            if (!variable.isDynamicTyped()) {
                if (clazz.getName().equals(variable.getType().getName())) {
                    FakeASTNode fakeNode = new FakeASTNode(expression, clazz.getNameWithoutPackage());
                    occurrences.add(fakeNode);
                }
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            OffsetRange range = getMethodReturnType(method, doc, cursorOffset);
            if (range != OffsetRange.NONE) {
                VariableExpression variable = expression.getVariableExpression();
                if (!variable.isDynamicTyped() && !method.isDynamicReturnType()) {
                    if (variable.getType().getName().equals(method.getReturnType().getName())) {
                        FakeASTNode fakeNode = new FakeASTNode(expression, method.getReturnType().getNameWithoutPackage());
                        occurrences.add(fakeNode);
                    }
                }
            }
        }
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitField(FieldNode fieldNode) {
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(fieldNode.getName())) {
            occurrences.add(fieldNode);
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (fieldNode.getName().equals(property.getPropertyAsString())) {
                occurrences.add(fieldNode);
                return;
            }
        }
        super.visitField(fieldNode);
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        VariableScope variableScope = methodNode.getVariableScope();
        if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            String name = ((ConstantExpression) leaf).getText();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) leafParent;
            if (Methods.isSameMethod(methodNode, methodCallExpression)) {
                occurrences.add(methodNode);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (Methods.isSameMethod(methodNode, method)) {
                OffsetRange range = getMethodReturnType(method, doc, cursorOffset);
                if (range != OffsetRange.NONE) {
                    FakeASTNode fakeNode = new FakeASTNode(methodNode, methodNode.getReturnType().getNameWithoutPackage());
                    occurrences.add(fakeNode);
                } else {
                    occurrences.add(methodNode);
                }
            }
        } else if (leaf instanceof ClassExpression) {
            ClassExpression clazz = (ClassExpression) leaf;
            if (methodNode.getReturnType().getName().equals(clazz.getType().getName())) {
                String simpleName = clazz.getType().getNameWithoutPackage();
                FakeASTNode fakeNode = new FakeASTNode(methodNode, simpleName);
                occurrences.add(fakeNode);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            if (!variable.isDynamicTyped() && !methodNode.isDynamicReturnType()) {
                String name = variable.getType().getNameWithoutPackage();
                if (name.equals(methodNode.getReturnType().getNameWithoutPackage())) {
                    FakeASTNode fakeNode = new FakeASTNode(methodNode, name);
                    occurrences.add(fakeNode);
                }
            }
        } else if (leaf instanceof ClassNode) {
            ClassNode clazz = (ClassNode) leaf;
            if (!methodNode.isDynamicReturnType()) {
                if (clazz.getName().equals(methodNode.getReturnType().getName())) {
                    FakeASTNode fakeNode = new FakeASTNode(methodNode, clazz.getNameWithoutPackage());
                    occurrences.add(fakeNode);
                }
            }
        }
        super.visitMethod(methodNode);
    }

    @Override
    public void visitConstructor(ConstructorNode constructor) {
        VariableScope variableScope = constructor.getVariableScope();
        if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            String name = ((ConstantExpression) leaf).getText();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstructorCallExpression) {
            ConstructorCallExpression methodCallExpression = (ConstructorCallExpression) leaf;
            if (Methods.isSameConstructor(constructor, methodCallExpression)) {
                occurrences.add(constructor);
            }
        } else if (leaf instanceof ConstructorNode) {
            if (Methods.isSameConstructor(constructor, (ConstructorNode) leaf)) {
                occurrences.add(constructor);
            }
        }
        super.visitConstructor(constructor);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {

        if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (Methods.isSameMethod(method, methodCall) &&
                    // making sure cursor is not on method's return type
                    getMethodReturnType(method, doc, cursorOffset) == OffsetRange.NONE) {
                occurrences.add(methodCall);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            if (Methods.isSameMethod(methodCall, (MethodCallExpression) leafParent)) {
                occurrences.add(methodCall);
            }
        }
        super.visitMethodCallExpression(methodCall);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (leaf instanceof ConstructorNode) {
            ConstructorNode constructor = (ConstructorNode) leaf;
            if (Methods.isSameConstructor(constructor, call)) {
                occurrences.add(call);
            }
        } else if (leaf instanceof ConstructorCallExpression) {
            if (Methods.isSameConstuctor(call, (ConstructorCallExpression) leaf)) {
                occurrences.add(call);
            }
        }
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitClassExpression(ClassExpression clazz) {
        if (leaf instanceof ClassNode) {
            ClassNode classNode = (ClassNode) leaf;
            if (clazz.getType().getName().equals(classNode.getName())) {
                occurrences.add(clazz);
            }
        } else if (leaf instanceof ClassExpression) {
            if (clazz.getType().getName().equals(((ClassExpression) leaf).getText())) {
                occurrences.add(clazz);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            if (!variable.isDynamicTyped() && clazz.getType().getName().equals(variable.getType().getName())) {
                occurrences.add(clazz);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            OffsetRange range = getMethodReturnType(method, doc, cursorOffset);
            if (range != OffsetRange.NONE) {
                occurrences.add(clazz);
            }
        }
        super.visitClassExpression(clazz);
    }

    @Override
    public void visitClass(ClassNode classNode) {
        if (leaf instanceof ClassExpression) {
            if (classNode.getName().equals(((ClassExpression) leaf).getText())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof ClassNode) {
            if (classNode.getName().equals(((ClassNode) leaf).getName())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            if (!variable.isDynamicTyped() && classNode.getName().equals(variable.getType().getName())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            OffsetRange range = getMethodReturnType(method, doc, cursorOffset);
            if (range != OffsetRange.NONE && classNode.getName().equals(method.getReturnType().getName())) {
                occurrences.add(classNode);
            }
        }
        super.visitClass(classNode);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        Expression property = node.getProperty();
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(node.getPropertyAsString())) {
            occurrences.add(property);
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression propertyUnderCursor = (PropertyExpression) leafParent;
            String nodeAsString = node.getPropertyAsString();
            if (nodeAsString != null && nodeAsString.equals(propertyUnderCursor.getPropertyAsString())) {
                occurrences.add(property);
            }
        }
        super.visitPropertyExpression(node);
    }

    private static final OffsetRange getMethodReturnType(MethodNode method, Element doc, int cursorOffset) {
//        int offset = AstUtilities.getOffset(doc, method.getLineNumber(), method.getColumnNumber());
//        if (!method.isDynamicReturnType()) {
//            OffsetRange range = AstUtilities.getNextIdentifierByName(doc, method.getReturnType().getNameWithoutPackage(), offset);
//            if (range.containsInclusive(cursorOffset)) {
//                return range;
//            }
//        }
        return OffsetRange.NONE;
    }

}

