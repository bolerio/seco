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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
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
    private static final String JAVA_LANG_OBJECT = "java.lang.Object";
   // private ClassNode guessedType;

    private boolean leafReached = false; // flag saying if visiting reached the
    // node that we are investigating

    Map<String, ClassNode> vars = new HashMap<String, ClassNode>(); 

    public TypeInferenceVisitor(SourceUnit sourceUnit, AstPath path,
            Element doc, int cursorOffset, String var)
    {
        // we don't want to visit all classes in module
        super(sourceUnit, path, doc, cursorOffset, true);
    }

    @Override
    public void collect()
    {
       // guessedType = null;
        leafReached = false;
        vars.clear();
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
            Expression left = expression.getLeftExpression();
            if (left instanceof VariableExpression)
            {
                
                //if (var.equals(((VariableExpression) leftExpression).getName()))
               // {
                    Expression right = expression.getRightExpression();
                    ClassNode n = resolveExpression(right);
                    if(n != null) 
                        vars.put(((VariableExpression) left).getName(), n);
                        //guessedType = n;
               // }
            }
        }
        super.visitBinaryExpression(expression);
    }

    ClassNode resolveExpression(Expression exp)
    {
        
        if (exp instanceof ConstantExpression && !exp.getText().equals("null"))
        {
            return exp.getType();
        }else if(exp instanceof VariableExpression)
        {
            String name = ((VariableExpression) exp).getName();
          if ("this".equals(name))
          { 
              return getSurroundingClassNode(path);
          }
          if ("super".equals(name))
          { 
              ClassNode thisClass = getSurroundingClassNode(path);
              ClassNode superC = thisClass.getSuperClass();
              if (superC == null) { 
                  superC = new ClassNode(JAVA_LANG_OBJECT, ClassNode.ACC_PUBLIC, null); }
              return superC;
          }
            ClassNode redef = vars.get(name);
            return redef != null ? redef : exp.getType();
        }
        else if (exp instanceof ConstructorCallExpression)
        {
            return exp.getType();
        }
        else if (exp instanceof ListExpression || exp instanceof MapExpression)
        {
            return exp.getType();
        }
        else if (exp instanceof RangeExpression)
        {
            try
            {
                return (new ClassNode(Class.forName("groovy.lang.Range")));
            }
            catch (ClassNotFoundException ex)
            {
                return new ClassNode("groovy.lang.Range", ClassNode.ACC_PUBLIC
                        | ClassNode.ACC_INTERFACE, null);
            }
        }
        else if (exp instanceof MethodCallExpression)
        {
            ClassNode n = resolveMethod((MethodCallExpression) exp);
            if (n != null) return n;
        }
        else if (exp instanceof PropertyExpression)
        {
            return resolveProperty((PropertyExpression) exp);
        }
        return exp.getType();
    }
    
    ClassNode resolveProperty(PropertyExpression pe)
    {
        ClassNode cls = resolveExpression(pe.getObjectExpression());
        PropertyNode pn = cls.getProperty(pe.getPropertyAsString());
        FieldNode f = (pn!= null) ? pn.getField(): cls.getField(pe.getPropertyAsString()); 
        return (f!= null) ? f.getType() : null;
    }

    ClassNode resolveMethod(MethodCallExpression mce)
    {
        ClassNode cls = resolveExpression(mce.getObjectExpression());//.getType();
        List<MethodNode> meths = cls.getMethods(mce.getMethodAsString());
        if (meths.isEmpty()) return null;
        ArgumentListExpression args = (ArgumentListExpression) mce
                .getArguments();
        int arity = args.getExpressions().size();
        for (MethodNode mn : meths)
        {
            // for now we take the first method with the same number of params
            if (mn.getParameters().length == arity)
            {
                ClassNode ret = mn.getReturnType();
                if (ClassHelper.VOID_TYPE == ret) continue;
                System.out.println("Resolve: " + 
                        mce.getMethodAsString() +
                        ":" + cls + ":" + ret);
                return ret;
            }
        }
        return null;
    }
    
    static ClassNode getSurroundingClassNode(AstPath path)
    {
        if (path == null) return null;
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();)
        {
            ASTNode current = it.next();
            if (current instanceof ClassNode)
                return (ClassNode) current;
        }
        return null;
    }
   
}
