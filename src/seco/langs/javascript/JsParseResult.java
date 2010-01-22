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
package seco.langs.javascript;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import seco.notebook.csl.*;

/**
 *
 * @author Tor Norbye
 */
public class JsParseResult extends ParserResult 
{

    private final PatchedParser parser;
    private final Node rootNode;

    private String source;
    private List<DefaultError> errors = new ArrayList<DefaultError>();
    
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String sanitizedContents;
    private PatchedParser.Sanitize sanitized;

    private JsAnalyzer.AnalysisResult analysisResult;
    private boolean commentsAdded;
    private IncrementalParse incrementalParse;

    public JsParseResult(PatchedParser parser, String source, Node rootNode) {
        super(source);
        this.parser = parser;
        this.rootNode = rootNode;
    }

    public @Override List<? extends DefaultError> getDiagnostics() {
        return errors;
    }

    public void setErrors(List<? extends DefaultError> errors) {
        this.errors = new ArrayList<DefaultError>(errors);
    }
    
    /** The root node of the AST produced by the parser.
     * Later, rip out the getAst part etc.
     */
    public Node getRootNode() {
        return rootNode;
    }

    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Return whether the source code for the parse result was "cleaned"
     * or "sanitized" (modified to reduce chance of parser errors) or not.
     * This method returns OffsetRange.NONE if the source was not sanitized,
     * otherwise returns the actual sanitized range.
     */
    public OffsetRange getSanitizedRange() {
        return sanitizedRange;
    }
    
    public String getSanitizedContents() {
        return sanitizedContents;
    }

    /**
     * Set the range of source that was sanitized, if any.
     */
    void setSanitized(PatchedParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
    }

    public PatchedParser.Sanitize getSanitized() {
        return sanitized;
    }    

    public void setStructure(JsAnalyzer.AnalysisResult result) {
        this.analysisResult = result;
    }

    public JsAnalyzer.AnalysisResult getStructure() {
        if (analysisResult == null) {
            analysisResult = JsAnalyzer.analyze(this);
        }
        return analysisResult;
    }

    public boolean isCommentsAdded() {
        return commentsAdded;
    }

    public void setCommentsAdded(boolean commentsAdded) {
        this.commentsAdded = commentsAdded;
    }

    private VariableVisitor variableVisitor;

    public VariableVisitor getVariableVisitor() {
        if (variableVisitor == null) {
            if (incrementalParse != null && incrementalParse.previousResult.variableVisitor != null) {
                variableVisitor = incrementalParse.previousResult.variableVisitor;
                variableVisitor.incrementalEdits(incrementalParse);
            } else {
                Node root = getRootNode();
                assert root != null : "Attempted to get variable visitor for broken source";
                variableVisitor = new VariableVisitor();
                new ParseTreeWalker(variableVisitor).walk(root);
            }
        }

        return variableVisitor;
    }
    
    @Override
    public String toString() {
        return "JsParseResult(file=" + 
        //getSnapshot().getSource().getFileObject() + 
        ",rootnode=" + rootNode + ")";
    }

    public void setIncrementalParse(IncrementalParse incrementalParse) {
        this.incrementalParse = incrementalParse;
    }

    public IncrementalParse getIncrementalParse() {
        return incrementalParse;
    }

    public static class IncrementalParse {
        public FunctionNode oldFunction;
        public FunctionNode newFunction;
        /**
         * The offset of the beginning of the function node that was replaced
         */
        public int incrementalOffset;
        /**
         * The offset at which the function node used to end
         */
        public int incrementalOffsetLimit;
        /**
         * The new offset at which the function node ends - old end, e.g. delta to apply
         * to all offsets greated than the incremental offset limit
         */
        public int incrementalOffsetDelta;
        public JsParseResult previousResult;

        public IncrementalParse(FunctionNode oldFunction, FunctionNode newFunction, int incrementalOffset, int incrementalOffsetLimit, int incrementalOffsetDelta, JsParseResult previousResult) {
            this.oldFunction = oldFunction;
            this.newFunction = newFunction;
            this.incrementalOffset = incrementalOffset;
            this.incrementalOffsetLimit = incrementalOffsetLimit;
            this.incrementalOffsetDelta = incrementalOffsetDelta;
            this.previousResult = previousResult;
        }

        // Cached for incremental support
        //public Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    }
}
