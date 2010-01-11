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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package seco.notebook.javascript;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.nb.javascript.FunctionNode;

import seco.notebook.csl.ElementKind;

public class FunctionAstElement extends AstElement implements FunctionElement 
{
    private FunctionNode func;
    private List<String> parameters;
    private String extend;

    FunctionAstElement(JsParseResult info, FunctionNode func) {
        super(info, func);
        this.func = func;
    }

    @Override
    public String toString() {
        return "JsFunctionElement:" + getSignature();
    }

    @Override
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            String clz = getIn();
            if (clz != null && clz.length() > 0) {
                sb.append(clz);
                sb.append("."); // NOI18N
            }
            sb.append(getName());
            sb.append("("); // NOI18N
            for (int i = 0, n = func.getParamCount(); i < n; i++) {
                if (i > 0) {
                    sb.append(","); // NOI18N
                }
                sb.append(func.getParamOrVarName(i));
            }
            sb.append(")"); // NOI18N
            signature = sb.toString();
        }

        return signature;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = func.getFunctionName();
            if (name == null || name.length() == 0) {
                name = "<default>";
            } else {
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    name = name.substring(index+1);
                }
            }
        }

        return name;
    }

    @Override
    public String getIn() {
        if (in == null) {
            in = "";
            String funcName = func.getFunctionName();
            if (funcName != null && funcName.length() > 0) {
                int index = funcName.lastIndexOf('.');
                if (index != -1) {
                    in = funcName.substring(0, index);

                    // Prototype.js hack
                    if ("Element.Methods".equals(in)) { // NOI18N
                        in = "Element"; // NOI18N
                    }
                }
            }
        }

        return in;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<String>(func.getParamCount());
            for (int j = 0,  m = func.getParamCount(); j < m; j++) {
                parameters.add(func.getParamOrVarName(j));
            }
        }

        return parameters;
    }

    @Override
    public ElementKind getKind() {
        if (kind == null) {
            if (getName().length() > 0 && Character.isUpperCase(getName().charAt(0))) {
                kind = ElementKind.CONSTRUCTOR;
            } else if (getName().equals("initialize")) { // NOI18N
                kind = ElementKind.CONSTRUCTOR;
            } else {
                return ElementKind.METHOD;
            }
        }
        
        return kind;
    }
    
    public void setExtends(String extend) {
        this.extend = extend;
    }
    
    public String getExtends() {
        return extend;
    }
}
