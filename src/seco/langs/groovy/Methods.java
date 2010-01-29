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

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;

/**
 * Utilities related to methods
 * 
 * @author Martin Adamek
 */
public class Methods {

//    public static boolean isSameMethod(ExecutableElement javaMethod, MethodCallExpression methodCall) {
//        ConstantExpression methodName = (ConstantExpression) methodCall.getMethod();
//        if (javaMethod.getSimpleName().contentEquals(methodName.getText())) {
//            // not comparing parameter types for now, only their count
//            // is it even possible to make some check for parameter types?
//            if (getParameterCount(methodCall) == javaMethod.getParameters().size()) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static boolean isSameMethod(MethodNode methodNode, MethodCallExpression methodCall) {
        if (methodNode.getName().equals(methodCall.getMethodAsString())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (getParameterCount(methodCall) == methodNode.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameMethod(MethodNode methodNode1, MethodNode methodNode2) {
        if (methodNode1.getName().equals(methodNode2.getName())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (methodNode1.getParameters().length == methodNode2.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameMethod(MethodCallExpression methodCall1, MethodCallExpression methodCall2) {
        String method1 = methodCall1.getMethodAsString();
        if (method1 != null && method1.equals(methodCall2.getMethodAsString())) {
            int size1 = getParameterCount(methodCall1);
            int size2 = getParameterCount(methodCall2);
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (size1 >= 0 && size1 == size2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstructor(ConstructorNode constructor, ConstructorCallExpression call) {
        if (constructor.getDeclaringClass().getNameWithoutPackage().equals(call.getType().getNameWithoutPackage())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (getParameterCount(call) == constructor.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstuctor(ConstructorCallExpression call1, ConstructorCallExpression call2) {
        String constructor1 = call1.getType().getNameWithoutPackage();
        if (constructor1 != null && constructor1.equals(call2.getType().getNameWithoutPackage())) {
            int size1 = getParameterCount(call1);
            int size2 = getParameterCount(call2);
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (size1 >= 0 && size1 == size2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameConstructor(ConstructorNode constructor1, ConstructorNode constructor2) {
        return isSameMethod(constructor1, constructor2);
    }

    /**
     * Tries to calculate number of method parameters.
     *
     * @param methodCall called method
     * @return number of method parameters,
     * 1 in case of named parameters represented by map,
     * or -1 if it is unknown
     */
    private static int getParameterCount(MethodCallExpression methodCall) {
        Expression expression = methodCall.getArguments();
        if (expression instanceof ArgumentListExpression) {
            return ((ArgumentListExpression) expression).getExpressions().size();
        } else if (expression instanceof NamedArgumentListExpression) {
            // this is in fact map acting as named parameters
            // lets return size 1
            return 1;
        } else {
            return -1;
        }
    }
    
    private static int getParameterCount(ConstructorCallExpression constructorCall) {
        Expression expression = constructorCall.getArguments();
        if (expression instanceof ArgumentListExpression) {
            return ((ArgumentListExpression) expression).getExpressions().size();
        } else if (expression instanceof NamedArgumentListExpression) {
            // this is in fact map acting as named parameters
            // lets return size 1
            return 1;
        } else {
            return -1;
        }
    }

}
