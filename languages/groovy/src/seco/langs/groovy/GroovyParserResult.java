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
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ErrorCollector;

import seco.langs.groovy.GroovyScriptSupport.GroovyScriptParser;

/**
 *
 * @author Martin Adamek
 */
public class GroovyParserResult 
{

    final GroovyScriptParser parser;
    
    private List<Error> errors = new ArrayList<Error>();

    private ModuleNode rootElement;
   // private OffsetRange sanitizedRange = OffsetRange.NONE;
   // private String sanitizedContents;
   // private StructureAnalyzer.AnalysisResult analysisResult;
   // private GroovyParser.Sanitize sanitized;
    private ErrorCollector errorCollector;  // keep track of pending errors (if any)

    GroovyParserResult(GroovyScriptParser parser, ModuleNode rootNode,
            ErrorCollector errorCollector) {
        this.parser = parser;
        this.rootElement = rootNode;
        this.errorCollector = errorCollector;
    }

    // FIXME remove this
    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public ModuleNode getRootElement() {
        return rootElement;
    }

    public void setErrors(List<? extends Error> errors) {
        this.errors = new ArrayList<Error>(errors);
    }
 
}

