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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

/**
 * Visitor for finding direct chidren of AST node
 * 
 * @author Martin Adamek
 */
public class AstChildrenSupport implements GroovyCodeVisitor {

    private List<ASTNode> children = new ArrayList<ASTNode>();
    
    public List<ASTNode> children() {
        return children;
    }
    
    public void visitBlockStatement(BlockStatement block) {
        List statements = block.getStatements();
        for (Iterator iter = statements.iterator(); iter.hasNext(); ) {
            Statement statement = (Statement) iter.next();
            children.add(statement);
        }
    }

    public void visitForLoop(ForStatement forLoop) {
        children.add(forLoop.getCollectionExpression());
        children.add(forLoop.getLoopBlock());
    }

    public void visitWhileLoop(WhileStatement loop) {
        children.add(loop.getBooleanExpression());
        children.add(loop.getLoopBlock());
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        children.add(loop.getLoopBlock());
        children.add(loop.getBooleanExpression());
    }

    public void visitIfElse(IfStatement ifElse) {
        children.add(ifElse.getBooleanExpression());
        children.add(ifElse.getIfBlock());
        children.add(ifElse.getElseBlock());
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        children.add(statement.getExpression());
    }

    public void visitReturnStatement(ReturnStatement statement) {
        children.add(statement.getExpression());
    }

    public void visitAssertStatement(AssertStatement statement) {
        children.add(statement.getBooleanExpression());
        children.add(statement.getMessageExpression());
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        children.add(statement.getTryStatement());
        List list = statement.getCatchStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CatchStatement catchStatement = (CatchStatement) iter.next();
            children.add(catchStatement);
        }
        children.add(statement.getFinallyStatement());
    }

    public void visitSwitch(SwitchStatement statement) {
        children.add(statement.getExpression());
        List list = statement.getCaseStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CaseStatement caseStatement = (CaseStatement) iter.next();
            children.add(caseStatement);
        }
        children.add(statement.getDefaultStatement());
    }

    public void visitCaseStatement(CaseStatement statement) {
        children.add(statement.getExpression());
        children.add(statement.getCode());
    }

    public void visitBreakStatement(BreakStatement statement) {
    }

    public void visitContinueStatement(ContinueStatement statement) {
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        children.add(statement.getExpression());
        children.add(statement.getCode());
    }

    public void visitThrowStatement(ThrowStatement statement) {
        children.add(statement.getExpression());
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        children.add(call.getObjectExpression());
        children.add(call.getMethod());
        children.add(call.getArguments());
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        children.add(call.getArguments());
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        children.add(call.getArguments());
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        children.add(expression.getLeftExpression());
        children.add(expression.getRightExpression());
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        children.add(expression.getBooleanExpression());
        children.add(expression.getTrueExpression());
        children.add(expression.getFalseExpression());
    }
    
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitNotExpression(NotExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitClosureExpression(ClosureExpression expression) {
        children.add(expression.getCode());
    }
    
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }
    
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());
        
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        children.add(expression.getKeyExpression());
        children.add(expression.getValueExpression());
        
    }

    public void visitRangeExpression(RangeExpression expression) {
        children.add(expression.getFrom());
        children.add(expression.getTo());
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        children.add(expression.getExpression());
    }
 
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        children.add(expression.getExpression());
        children.add(expression.getMethodName());
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        children.add(expression.getExpression());
    }
    
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        children.add(expression.getExpression());
    }
    
    public void visitCastExpression(CastExpression expression) {
        children.add(expression.getExpression());
    }

    public void visitConstantExpression(ConstantExpression expression) {
    }

    public void visitClassExpression(ClassExpression expression) {
    }

    public void visitVariableExpression(VariableExpression expression) {
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }
    
    public void visitPropertyExpression(PropertyExpression expression) {
        children.add(expression.getObjectExpression());
        children.add(expression.getProperty());
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        children.add(expression.getObjectExpression());
        children.add(expression.getProperty());
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitRegexExpression(RegexExpression expression) {
    }

    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    private void visitListOfExpressions(List list) {
        if (list==null) return;
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Expression expression = (Expression) iter.next();
            if (expression instanceof SpreadExpression) {
                Expression spread = ((SpreadExpression) expression).getExpression();
                children.add(spread);
            } else {
                children.add(expression);
            }
        }
    }
    
    public void visitCatchStatement(CatchStatement statement) {
        children.add(statement.getCode());
    }
    
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        visitTupleExpression(ale);
    }
    
    public void visitClosureListExpression(ClosureListExpression cle) {
        visitListOfExpressions(cle.getExpressions());
    }

    // added in Groovy 1.6
    // TODO check this
    public void visitBytecodeExpression(BytecodeExpression bce) {
    }
}

