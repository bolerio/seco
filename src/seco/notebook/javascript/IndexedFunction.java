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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package seco.notebook.javascript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seco.notebook.csl.ElementKind;
import seco.notebook.csl.IndexResult;

/**
 *
 * @author Tor Norbye
 */
public class IndexedFunction extends IndexedElement implements FunctionElement {
    private String[] args;
    private List<String> parameters;
    
    IndexedFunction(String fqn, String name, String in, JsIndex index, IndexResult indexResult, String attributes, int flags, ElementKind kind) {
        super(fqn, name, in, index, indexResult, attributes, flags, kind);
    }
    
    @Override
    public String toString() {
        return getSignature() + ":" + getFilenameUrl() + ";" + decodeFlags(flags);
    }

    @Override
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            sb.append("(");
            List<String> parameterList = getParameters();
            if (parameterList.size() > 0) {
                for (int i = 0, n = parameterList.size(); i < n; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(parameterList.get(i));
                }
            }
            sb.append(")");
            signature = sb.toString();
        }

        return signature;
    }

    public String[] getArgs() {
        if (args == null) {
            int argIndex = getAttributeSection(ARG_INDEX);
            int endIndex = attributes.indexOf(';', argIndex);
            if (endIndex > argIndex) {
                String argsPortion = attributes.substring(argIndex, endIndex);
                args = argsPortion.split(","); // NOI18N
            } else {
                args = new String[0];
            }
        }

        return args;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            String[] a = getArgs();

            if ((a != null) && (a.length > 0)) {
                parameters = new ArrayList<String>(a.length);

                for (String arg : a) {
                    parameters.add(arg);
                }
            } else {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }
}
