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

import java.util.Iterator;

import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
//import org.netbeans.api.lexer.Token;
//import org.netbeans.api.lexer.TokenSequence;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
//import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;

/**
 *
 * @author Martin Adamek
 */
public class TypeVisitor extends ClassCodeVisitorSupport {

    protected final SourceUnit sourceUnit;

    protected final AstPath path;

    protected final ASTNode leaf;

    protected final Element doc;

    protected final int cursorOffset;

    private final boolean visitOtherClasses;

    public TypeVisitor(SourceUnit sourceUnit, AstPath path, Element doc,
            int cursorOffset, boolean visitOtherClasses) {
        this.sourceUnit = sourceUnit;
        this.path = path;
        this.leaf = path.leaf();
        this.doc = doc;
        this.cursorOffset = cursorOffset;
        this.visitOtherClasses = visitOtherClasses;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void collect() {
        // FIXME use snapshot TH
//        doc.readLock();
//        try {
//            TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, cursorOffset);
//            if (ts == null) {
//                return;
//            }
//            Token<? extends GroovyTokenId> token = ts.token();
//            if (token == null) {
//                return;
//            }
//            if (!isValidToken(token)) {
//                return;
//            }
//        } finally {
//            doc.readUnlock();
//        }

        if (leaf instanceof Variable) {
            Variable variable = (Variable) leaf;
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
                ASTNode scope = it.next();
                if (scope instanceof ClosureExpression) {
                    VariableScope variableScope = ((ClosureExpression) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitClosureExpression((ClosureExpression) scope);
                        return;
                    }
                } else if (scope instanceof MethodNode) {
                    MethodNode method = (MethodNode) scope;
                    VariableScope variableScope = method.getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitParameters(method.getParameters(), variable);
                        // call super method to avoid additional scope checks in our implementation
                        super.visitMethod(method);
                        return;
                    }
                } else if (scope instanceof ConstructorNode) {
                    ConstructorNode constructor = (ConstructorNode) scope;
                    VariableScope variableScope = (constructor).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitParameters(constructor.getParameters(), variable);
                        // call super method to avoid additional scope checks in our implementation
                        super.visitConstructor(constructor);
                        return;
                    }
                } else if (scope instanceof ForStatement) {
                    VariableScope variableScope = ((ForStatement) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitForLoop((ForStatement) scope);
                        return;
                    }
                } else if (scope instanceof BlockStatement) {
                    VariableScope variableScope = ((BlockStatement) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitBlockStatement((BlockStatement) scope);
                        return;
                    }
                } else if (scope instanceof ClosureListExpression) {
                    VariableScope variableScope = ((ClosureListExpression) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitClosureListExpression((ClosureListExpression) scope);
                        return;
                    }
                }
            }
        }

        if (visitOtherClasses) {
            ModuleNode moduleNode = (ModuleNode) path.root();
            for (Object object : moduleNode.getClasses()) {
                visitClass((ClassNode) object);
            }
        }
        // XXX it seems to me that this is not needed, it is just causing whole visitor
        // to run twice, but it needs to be checked again for scripts maybe?
//        for (Object object : moduleNode.getMethods()) {
//            visitMethod((MethodNode)object);
//        }
//        visitBlockStatement(moduleNode.getStatementBlock());
    }

//    /**
//     * Children can override this if it has special requirement on selected token.
//     */
//    protected boolean isValidToken(Token<? extends GroovyTokenId> token) {
//        return true;
//    }

    /**
     * Children can override this to do extra things with method/constructor parameters.
     */
    protected void visitParameters(Parameter[] parameters, Variable variable) {
    }

}
