package seco.langs.groovy;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

/**
 * 
 * @author Petr Hejl
 */
public class TypeInferenceVisitor extends TypeVisitor
{

    private ClassNode guessedType;

    private boolean leafReached = false; // flag saying if visiting reached the
    // node that we are investigating

    private String var;

    public TypeInferenceVisitor(SourceUnit sourceUnit, AstPath path,
            Element doc, int cursorOffset, String var)
    {
        // we don't want to visit all classes in module
        super(sourceUnit, path, doc, cursorOffset, true);
        this.var = var;
    }

    /**
     * Tries to guess the type from the last assignment expression before actual
     * position of the leaf
     * 
     * @return guessed type or null if there is no way to calculate it
     */
    public ClassNode getGuessedType()
    {
        return guessedType;
    }

    @Override
    public void collect()
    {
        guessedType = null;
        leafReached = false;

        super.collect();
    }

    @Override
    public void visitVariableExpression(VariableExpression expression)
    {
        if (expression == leaf) leafReached = true;
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression)
    {
        if (!leafReached)
        {
            // have a look at assignment and try to get type from its right side
            Expression leftExpression = expression.getLeftExpression();
            if (leftExpression instanceof VariableExpression)
            {
                if (var.equals(((VariableExpression) leftExpression).getName()))
                {
                    Expression rightExpression = expression
                            .getRightExpression();
                    if (rightExpression instanceof ConstantExpression
                            && !rightExpression.getText().equals("null"))
                    {
                        guessedType = rightExpression.getType();
                    }
                    else if (rightExpression instanceof ConstructorCallExpression)
                    {
                        guessedType = rightExpression.getType();
                    }
                    else if (rightExpression instanceof ListExpression
                            || rightExpression instanceof MapExpression)
                    {
                        guessedType = rightExpression.getType();
                    }
                    else if (rightExpression instanceof RangeExpression)
                    {
                        try
                        {
                            guessedType = (new ClassNode(Class
                                    .forName("groovy.lang.Range")));
                        }
                        catch (ClassNotFoundException ex)
                        {
                            guessedType = new ClassNode("groovy.lang.Range",
                                    ClassNode.ACC_PUBLIC
                                            | ClassNode.ACC_INTERFACE, null);
                        }
                    }
                }

            }
        }
        super.visitBinaryExpression(expression);
    }

    private static boolean sameVariableName(Parameter param, Variable variable)
    {
        return param.getName().equals(variable.getName());
    }

    private static boolean sameVariableName(ASTNode node1, ASTNode node2)
    {
        return node1 instanceof VariableExpression
                && node2 instanceof VariableExpression
                && ((VariableExpression) node1).getName().equals(
                        ((VariableExpression) node2).getName());
    }

}
