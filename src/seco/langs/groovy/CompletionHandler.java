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
//package org.netbeans.modules.groovy.editor.api.completion;
import groovy.lang.MetaMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Element;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import seco.langs.javascript.JSCompletionProvider;
import seco.notebook.syntax.completion.CompletionItem;
import seco.notebook.syntax.java.JavaResultItem;

public class CompletionHandler
{
    private final static List<String> defaultImports = new ArrayList<String>();
    static
    {
        Collections.addAll(defaultImports, "java.io", "java.lang", "java.net", // NOI18N
                "java.util", "groovy.util", "groovy.lang");
    }
    private int anchor;

    public CompletionHandler()
    {
    }

    boolean checkForPackageStatement(final CompletionRequest request)
    {
        // TokenSequence<?> ts =
        // LexUtilities.getGroovyTokenSequence(request.doc, 1);
        //
        // if (ts != null) {
        // ts.move(1);
        //
        // while (ts.isValid() && ts.moveNext() && ts.offset() <
        // request.doc.getLength()) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        //
        // if (t.id() == GroovyTokenId.LITERAL_package) {
        // return true;
        // }
        // }
        // }

        return false;
    }

    /**
     * Figure-out, where we are in the code (comment, CU, class, method, etc.)
     * 
     * @param request
     * @return
     */
    private CaretLocation getCaretLocationFromRequest(
            final CompletionRequest request)
    {

        // int position = request.lexOffset;
        // TokenSequence<?> ts =
        // LexUtilities.getGroovyTokenSequence(request.doc, position);
        //
        // // are we living inside a comment?
        //
        // ts.move(position);
        //
        // if (ts.isValid() && ts.moveNext() && ts.offset() <
        // request.doc.getLength()) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        //
        // if (t.id() == GroovyTokenId.LINE_COMMENT || t.id() ==
        // GroovyTokenId.BLOCK_COMMENT) {
        // return CaretLocation.INSIDE_COMMENT;
        // }
        //
        // if (t.id() == GroovyTokenId.STRING_LITERAL) {
        // return CaretLocation.INSIDE_STRING;
        // }
        // // This is a special case. If we have a NLS right behind a
        // LINE_COMMENT it
        // // should be treated as a CaretLocation.INSIDE_COMMENT. Therefore we
        // have to rewind.
        //
        // if (t.id() == GroovyTokenId.NLS) {
        // if ((ts.isValid() && ts.movePrevious() && ts.offset() >= 0)) {
        // Token<? extends GroovyTokenId> tparent = (Token<? extends
        // GroovyTokenId>) ts.token();
        // if (tparent.id() == GroovyTokenId.LINE_COMMENT) {
        // return CaretLocation.INSIDE_COMMENT;
        // }
        // }
        // }
        // }
        //
        //
        // // Are we above the package statement?
        // // We try to figure this out by moving down the lexer Stream
        //
        // ts.move(position);
        //
        // while (ts.isValid() && ts.moveNext() && ts.offset() <
        // request.doc.getLength()) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        //
        // if (t.id() == GroovyTokenId.LITERAL_package) {
        // return CaretLocation.ABOVE_PACKAGE;
        // }
        // }
        //
        // // Are we before the first class or interface statement?
        // // now were heading to the beginning to the document ...
        //
        boolean classDefBeforePosition = false;
        //
        // ts.move(position);
        //
        // while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        // if (t.id() == GroovyTokenId.LITERAL_class || t.id() ==
        // GroovyTokenId.LITERAL_interface) {
        // classDefBeforePosition = true;
        // break;
        // }
        // }
        //
        //
        boolean classDefAfterPosition = false;
        //
        // ts.move(position);
        //
        // while (ts.isValid() && ts.moveNext() && ts.offset() <
        // request.doc.getLength()) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        // if (t.id() == GroovyTokenId.LITERAL_class || t.id() ==
        // GroovyTokenId.LITERAL_interface) {
        // classDefAfterPosition = true;
        // break;
        // }
        // }
        request.scriptMode = true;

        // if (request.path != null)
        // {
        // ASTNode node = request.path.root();
        // if (node instanceof ModuleNode)
        // {
        // ModuleNode module = (ModuleNode) node;
        // String name = null;
        // for (Iterator it = module.getClasses().iterator(); it.hasNext();)
        // {
        // ClassNode clazz = (ClassNode) it.next();
        // if (clazz.isScript())
        // {
        // name = clazz.getName();
        // request.scriptMode = true;
        // break;
        // }
        // }

        // we have a script class - lets see if there is another
        // non-script class with same name that would mean we are just
        // broken class, not a script
        // if (name != null)
        // {
        // for (Iterator it = module.getClasses().iterator(); it
        // .hasNext();)
        // {
        // ClassNode clazz = (ClassNode) it.next();
        // if (!clazz.isScript() && name.equals(clazz.getName()))
        // {
        // request.scriptMode = false;
        // break;
        // }
        // }
        // }
        // }
        // }

        // if (!request.scriptMode && !classDefBeforePosition
        // && classDefAfterPosition) { return CaretLocation.ABOVE_FIRST_CLASS; }

        // If there's *no* class definition in the file we are running in a
        // script with synthetic wrapper class and wrapper method: run().
        // See GINA, ch. 7

        // if (!classDefBeforePosition && request.scriptMode) {
        // return CaretLocation.INSIDE_METHOD;
        // }
        //
        //
        // if (request.path == null) {
        // LOG.log(Level.FINEST, "path == null"); // NOI18N
        // return null;
        // }

        /*
         * here we loop from the tail of the path (innermost element) up to the
         * root to figure out where we are. Some of the trails are:
         * 
         * In main method:
         * Path(4)=[ModuleNode:ClassNode:MethodNode:ConstantExpression:]
         * 
         * In closure, which sits in a method:
         * Path(7)=[ModuleNode:ClassNode:MethodNode
         * :DeclarationExpression:DeclarationExpression
         * :VariableExpression:ClosureExpression:]
         * 
         * In closure directly attached to class:
         * Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]
         * 
         * In a class, outside method, right behind field declaration:
         * Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]
         * 
         * Right after a class declaration: Path(2)=[ModuleNode:ClassNode:]
         * 
         * Inbetween two classes: [ModuleNode:ConstantExpression:]
         * 
         * Outside of any class: Path(1)=[ModuleNode:]
         * 
         * Start of Parameter-list:
         * Path(4)=[ModuleNode:ClassNode:MethodNode:Parameter:]
         */

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();)
        {
            ASTNode current = it.next();
            if (current instanceof ClosureExpression)
            {
                return CaretLocation.INSIDE_CLOSURE;
            }
            else if (current instanceof FieldNode)
            {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable()) { return CaretLocation.INSIDE_CLOSURE; }
            }
            else if (current instanceof MethodNode)
            {
                return CaretLocation.INSIDE_METHOD;
            }
            else if (current instanceof ClassNode)
            {
                return CaretLocation.INSIDE_CLASS;
            }
            else if (current instanceof ModuleNode)
            {
                return CaretLocation.OUTSIDE_CLASSES;
            }
            else if (current instanceof Parameter) { return CaretLocation.INSIDE_PARAMETERS; }
        }
        return CaretLocation.UNDEFINED;

    }

    // private ArgumentListExpression getSurroundingArgumentList(AstPath path)
    // {
    // if (path == null)
    // {
    // LOG.log(Level.FINEST, "path == null"); // NOI18N
    // return null;
    // }
    //
    // LOG.log(Level.FINEST, "AEL, Path : {0}", path);
    //
    // for (Iterator<ASTNode> it = path.iterator(); it.hasNext();)
    // {
    // ASTNode current = it.next();
    // if (current instanceof ArgumentListExpression) {
    //
    // return (ArgumentListExpression) current; }
    // }
    // return null;
    //
    // }

    /**
     * returns the next enclosing MethodNode for the given request
     * 
     * @param request
     *            completion request which includes position information
     * @return the next surrouning MethodNode
     */
    private ASTNode getSurroundingMethodOrClosure(CompletionRequest request)
    {
        if (request.path == null)
            return null;
     
        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();)
        {
            ASTNode current = it.next();
            if (current instanceof MethodNode)
            {
                MethodNode mn = (MethodNode) current;
                return mn;
            }
            else if (current instanceof FieldNode)
            {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable())
                    return fn;
            }
            else if (current instanceof ClosureExpression)
            {
                return current;
            }
        }
        return null;
    }

    /**
     * returns the next enclosing ClassNode for the given request
     * 
     * @param request
     *            completion request which includes position information
     * @return the next surrouning ClassNode
     */
    private ClassNode getSurroundingClassNode(CompletionRequest request)
    {
        if (request.path == null)
            return null;
      
        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();)
        {
            ASTNode current = it.next();
            if (current instanceof ClassNode)
            {
                ClassNode classNode = (ClassNode) current;
                return classNode;
            }
        }
        return null;
    }

    /**
     * Calculate an AstPath from a given request or null if we can not get a AST
     * root-node from the request.
     * 
     * @param request
     * @return a freshly created AstPath object for the offset given in the
     *         request
     */
    private AstPath getPathFromRequest(final CompletionRequest request)
    {
        // figure out which class we are dealing with:
        ASTNode root = request.info.getRootElement();
        if (root == null) return null;

        return new AstPath(root, request.astOffset, request.doc);
    }

    private AstPath getPath(GroovyParserResult info, Element doc, int astOffset)
    {
        // figure out which class we are dealing with:
        ASTNode root = info.getRootElement();

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317

        if (root == null) return null;
        return new AstPath(root, astOffset, doc);
    }

     /**
     * Complete the fields for a class. There are two principal completions for
     * fields:
     * 
     * 1.) We are invoked right behind a dot. Then we have to retrieve the type
     * in front of this dot. 2.) We are located inside a type. Then we gotta get
     * the fields for this class.
     * 
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeFields(List<CompletionItem> proposals,
            CompletionRequest request)
    {
        if (request.location == CaretLocation.INSIDE_PARAMETERS
                && request.isBehindDot() == false) return false;

        if (request.dotContext != null && request.dotContext.isMethodsOnly()) { return false; }

        ClassNode declaringClass;

        if (request.isBehindDot())
        {
            PackageCompletionRequest packageRequest = getPackageRequest(request);

            if (packageRequest.basePackage.length() > 0)
            {
                // ClasspathInfo pathInfo =
                // getClasspathInfoFromRequest(request);

                // if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                // LOG.log(Level.FINEST,
                // "The string before the dot seems to be a valid package"); //
                // NOI18N
                return false;
                // }
            }

            declaringClass = getBeforeDotDeclaringClass(request);

            if (declaringClass == null) return false;
        }
        else
        {
            declaringClass = getSurroundingClassNode(request);
            if (declaringClass == null) return false;
        }

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        int anchorShift = 0;
        String fieldName = request.prefix;

        if (request.prefix.startsWith("$"))
        {
            fieldName = request.prefix.substring(1);
            anchorShift = 1;
        }

        // Map<FieldSignature, ? extends CompletionItem> result =
        // CompleteElementHandler
        // .forCompilationInfo(request.info).getFields(
        // getSurroundingClassNode(request), declaringClass,
        // fieldName, anchor + anchorShift);

        // proposals.addAll(result.values());

        return true;
    }

    private boolean completeLocalVars(List<CompletionItem> proposals,
            CompletionRequest request)
    {
        if (!(request.location == CaretLocation.INSIDE_CLOSURE || request.location == CaretLocation.INSIDE_METHOD)
                // handle $someprefix in string
                && !(request.location == CaretLocation.INSIDE_STRING && request.prefix
                        .matches("\\$[^\\{].*"))) return false;

        // If we are right behind a dot, there's no local-vars completion.

        if (request.isBehindDot())
            return false;
        
        VariableFinderVisitor vis = new VariableFinderVisitor(
                ((ModuleNode) request.path.root()).getContext(), request.path,
                request.doc, request.astOffset);
        vis.collect();

        boolean updated = false;

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        int anchorShift = 0;
        String varPrefix = request.prefix;

        if (request.prefix.startsWith("$"))
        {
            varPrefix = request.prefix.substring(1);
            anchorShift = 1;
        }

        for (Variable node : vis.getVariables())
        {
            String varName = node.getName();
            
            if (varPrefix.length() < 1)
            {
                proposals.add(new JavaResultItem.VarResultItem(varName,
                        Object.class, anchor + anchorShift));
                updated = true;
            }
            else if (!varName.equals(varPrefix)
                    && varName.startsWith(varPrefix))
            {
                proposals.add(new JavaResultItem.VarResultItem(varName,
                        Object.class, anchor + anchorShift));
                updated = true;
            }
        }

        return updated;
    }

 
    /**
     * Check whether this completion request was issued behind an import
     * statement.
     * 
     * @param request
     * @return
     */
    boolean checkForRequestBehindImportStatement(final CompletionRequest request)
    {

        // int rowStart = 0;
        // int nonWhite = 0;
        //
        // try {
        // rowStart = Utilities.getRowStart(request.doc, request.lexOffset);
        // nonWhite = Utilities.getFirstNonWhiteFwd(request.doc, rowStart);
        //
        // } catch (BadLocationException ex) {
        // LOG.log(Level.FINEST,
        // "Trouble doing getRowStart() or getFirstNonWhiteFwd(): {0}",
        // ex.getMessage());
        // }

        // Token<? extends GroovyTokenId> importToken =
        // LexUtilities.getToken(request.doc, nonWhite);
        //
        // if (importToken != null && importToken.id() ==
        // GroovyTokenId.LITERAL_import) {
        // LOG.log(Level.FINEST, "Right behind an import statement");
        // return true;
        // }

        return false;
    }

    PackageCompletionRequest getPackageRequest(final CompletionRequest request)
    {
        int position = request.lexOffset;
        PackageCompletionRequest result = new PackageCompletionRequest();

        // TokenSequence<?> ts =
        // LexUtilities.getGroovyTokenSequence(request.doc, position);
        // ts.move(position);

        // travel back on the token string till the token is neither a
        // DOT nor an IDENTIFIER

        // Token<? extends GroovyTokenId> token = null;
        // boolean remainingTokens = true;
        // while (ts.isValid() && (remainingTokens = ts.movePrevious()) &&
        // ts.offset() >= 0) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        // // LOG.log(Level.FINEST, "LexerToken(back): {0}",
        // t.text().toString());
        // if (!(t.id() == GroovyTokenId.DOT || t.id() ==
        // GroovyTokenId.IDENTIFIER)) {
        // break;
        // } else {
        // token = t;
        // }
        // }
        //
        // // now we are travelling in the opposite direction to construct
        // // the result
        //
        // StringBuffer buf = new StringBuffer();
        // Token<? extends GroovyTokenId> lastToken = null;
        //
        // // if we reached the beginning in the previous iteration we have to
        // get
        // // the first token too (without call to moveNext())
        // if (!remainingTokens && token != null && ts.isValid()) {
        // buf.append(token.text().toString());
        // lastToken = token;
        // }
        //
        // // iterate the rest of the sequence
        // while (ts.isValid() && ts.moveNext() && ts.offset() < position) {
        // Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>)
        // ts.token();
        //
        // // LOG.log(Level.FINEST, "LexerToken(fwd): {0}",
        // t.text().toString());
        // if (t.id() == GroovyTokenId.DOT || t.id() ==
        // GroovyTokenId.IDENTIFIER) {
        // buf.append(t.text().toString());
        // lastToken = t;
        // } else {
        // break;
        // }
        // }

        // construct the return value. These are the combinations:
        // string basePackage prefix
        // "" "" ""
        // "java" "" "java"
        // "java." "java" ""
        // "java.lan" "java" "lan"
        // "java.lang" "java" "lang"
        // "java.lang." "java.lang" ""

        // result.fullString = buf.toString();
        //
        // if (buf.length() == 0) {
        // result.basePackage = "";
        // result.prefix = "";
        // } else if (lastToken != null && lastToken.id() == GroovyTokenId.DOT)
        // {
        // String pkgString = buf.toString();
        // result.basePackage = pkgString.substring(0, pkgString.length() - 1);
        // result.prefix = "";
        // } else if (lastToken != null && lastToken.id() ==
        // GroovyTokenId.IDENTIFIER) {
        // String pkgString = buf.toString();
        // result.prefix = lastToken.text().toString();
        //
        // result.basePackage = pkgString.substring(0, pkgString.length() -
        // result.prefix.length());
        //
        // if (result.basePackage.endsWith(".")) {
        // result.basePackage = result.basePackage.substring(0,
        // result.basePackage.length() - 1);
        // }
        // }
        return result;
    }

    class PackageCompletionRequest
    {

        String fullString = "";
        String basePackage = "";
        String prefix = "";
    }

    private DotCompletionContext getDotCompletionContext(
            final CompletionRequest request)
    {
        if (request.dotContext != null) { return request.dotContext; }
        boolean methodsOnly = false;
        int lexOffset = request.lexOffset;
        int astOffset = request.lexOffset;
        AstPath realPath = getPath(request.info, request.doc, astOffset);
        return new DotCompletionContext(lexOffset, astOffset, realPath,
                methodsOnly);
    }

    private static class DotCompletionContext
    {
        private final int lexOffset;
        private final int astOffset;
        private final AstPath astPath;
        private final boolean methodsOnly;

        public DotCompletionContext(int lexOffset, int astOffset,
                AstPath astPath, boolean methodsOnly)
        {
            this.lexOffset = lexOffset;
            this.astOffset = astOffset;
            this.astPath = astPath;
            this.methodsOnly = methodsOnly;
        }

        public int getLexOffset()
        {
            return lexOffset;
        }

        public int getAstOffset()
        {
            return astOffset;
        }

        public AstPath getAstPath()
        {
            return astPath;
        }

        public boolean isMethodsOnly()
        {
            return methodsOnly;
        }

    }

  
    /**
     * Here we complete package-names like java.lan to java.lang ...
     * 
     * @param proposals
     *            the CompletionPropasal we should populate
     * @param request
     *            wrapper object for this specific request ( position etc.)
     * @return true if we found something suitable
     */
    private boolean completePackages(final List<CompletionItem> proposals,
            final CompletionRequest request)
    {
        // this can happen for ?. or similar constructs
        PackageCompletionRequest packageRequest = getPackageRequest(request);
        if (request.isBehindDot() && packageRequest.basePackage.length() <= 0) { return false; }

        // ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);
        //
        // assert pathInfo != null : "Can not get ClasspathInfo";
        //
        // if (request.ctx.before1 != null
        // && CharSequenceUtilities.textEquals(request.ctx.before1.text(), "*")
        // && request.behindImport) {
        // return false;
        // }

        // try to find suitable packages ...

        Set<String> pkgSet;

        // pkgSet =
        // pathInfo.getClassIndex().getPackageNames(packageRequest.fullString,
        // true, EnumSet.allOf(ClassIndex.SearchScope.class));
        //
        // for (String singlePackage : pkgSet) {
        // LOG.log(Level.FINEST, "PKG set item: {0}", singlePackage);
        //
        // if (packageRequest.prefix.equals("")) {
        // singlePackage =
        // singlePackage.substring(packageRequest.fullString.length());
        // } else if (!packageRequest.basePackage.equals("")) {
        // singlePackage =
        // singlePackage.substring(packageRequest.basePackage.length() + 1);
        // }
        //
        // if (singlePackage.startsWith(packageRequest.prefix) &&
        // singlePackage.length() > 0) {
        // CompletionItem.PackageItem item = new
        // CompletionItem.PackageItem(singlePackage, anchor, request.info);
        //
        // if (request.behindImport) {
        // item.setSmart(true);
        // }
        //
        // proposals.add(item);
        // }
        //
        // }

        return false;
    }

    // private boolean isValidPackage(ClasspathInfo pathInfo, String pkg) {
    // assert pathInfo != null : "ClasspathInfo can not be null";
    //
    // // the following statement gives us all the packages *starting* with the
    // // first parameter. We have to check for exact matches ourselves. See #
    // 142372
    //
    // Set<String> pkgSet = pathInfo.getClassIndex().getPackageNames(pkg, true,
    // EnumSet.allOf(ClassIndex.SearchScope.class));
    //
    // if (pkgSet.size() > 0) {
    // LOG.log(Level.FINEST, "Packages with prefix : {0}", pkg);
    // LOG.log(Level.FINEST, "               found : {0}", pkgSet);
    //
    // for (String singlePkg : pkgSet) {
    // if(singlePkg.equals(pkg)){
    // LOG.log(Level.FINEST, "Exact match found.");
    // return true;
    // }
    // }
    //
    // return false;
    // } else {
    // return false;
    // }
    // }

    /**
     * Complete the Groovy and Java types available at this position.
     * 
     * This could be either:
     * 
     * 1.) Completing all available Types in a given package. This is used for:
     * 
     * 1.1) import statements completion 1.2) If you simply want to give the
     * fq-name for something.
     * 
     * 2.) Complete the types which are available without having to give a fqn:
     * 
     * 2.1.) Types defined in the Groovy File where the completion is invoked.
     * (INDEX) 2.2.) Types located in the same package (source or binary).
     * (INDEX) 2.3.) Types manually imported via the "import" statement. (AST)
     * 2.4.) The Default imports for Groovy, which are a super-set of Java. (NB
     * JavaSource)
     * 
     * These are the Groovy default imports:
     * 
     * java.io.* java.lang.* java.math.BigDecimal java.math.BigInteger
     * java.net.* java.util.* groovy.lang.* groovy.util.*
     * 
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeTypes(final List<CompletionItem> proposals,
            final CompletionRequest request)
    {
        final PackageCompletionRequest packageRequest = getPackageRequest(request);

        // todo: we don't handle single dots in the source. In that case we
        // should
        // find the class we are living in. Disable it for now.

        if (packageRequest.basePackage.length() == 0
                && packageRequest.prefix.length() == 0
                && packageRequest.fullString.equals(".")) { return false; }

        // this is a new Something()| request for a constructor, which is
        // handled in completeMethods.

        // if (request.ctx.before1 != null
        // && request.ctx.before1.text().toString().equals("new") // NOI18N
        // && request.prefix.length() > 0) {
        // return false;
        // }

        // are we dealing with a class xyz implements | {
        // kind of completion?

        boolean onlyInterfaces = false;

        // if (request.ctx.beforeLiteral != null &&
        // request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) {
        // LOG.log(Level.FINEST,
        // "Completing only interfaces after implements keyword.");
        // onlyInterfaces = true;
        // }

        // This ModuleNode is used to retrieve the types defined here
        // and the package name.

        ModuleNode mn = null;
        AstPath path = request.path;
        if (path != null)
        {
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();)
            {
                ASTNode current = it.next();
                if (current instanceof ModuleNode)
                {
                    mn = (ModuleNode) current;
                }
            }
        }

        // Get current package
        String currentPackage = null;
        if (mn != null)
        {
            currentPackage = mn.getPackageName();
        }
        else
        {
            ClassNode node = getSurroundingClassNode(request);
            if (node != null)
            {
                currentPackage = node.getPackageName();
            }
        }

        Set<TypeHolder> addedTypes = new HashSet<TypeHolder>();

        // get the JavaSource for our file.
        // final JavaSource javaSource = getJavaSourceFromRequest(request);

        // if we are dealing with a basepackage we simply complete all the
        // packages given in the basePackage

        if (packageRequest.basePackage.length() > 0 || request.behindImport)
        {
            if (!(request.behindImport && packageRequest.basePackage.length() == 0))
            {

                // List<TypeHolder> stringTypelist =
                // getElementListForPackageAsTypeHolder(javaSource,
                // packageRequest.basePackage, currentPackage);
                //
                // if (stringTypelist == null) {
                // LOG.log(Level.FINEST, "Typelist is null for package : {0}",
                // packageRequest.basePackage);
                // return false;
                // }
                //
                // LOG.log(Level.FINEST, "Number of types found:  {0}",
                // stringTypelist.size());
                //
                // for (TypeHolder singleType : stringTypelist) {
                // addToProposalUsingFilter(addedTypes, proposals, request,
                // singleType, onlyInterfaces);
                // }
            }

            return true;

        }
        // already exited if package completion

        // dont want types for objectExpression.something
        if (request.isBehindDot()) { return false; }

        // Retrieve the package we are living in from AST and then
        // all classes from that package using the Groovy Index.

        // if (mn != null) {
        // LOG.log(Level.FINEST, "We are living in package : {0} ",
        // currentPackage);
        //
        // // FIXME parsing API
        // GroovyIndex index = null;
        // FileObject fo =
        // request.info.getSnapshot().getSource().getFileObject();
        // if (fo != null) {
        // index = GroovyIndex.get(QuerySupport.findRoots(fo,
        // Collections.singleton(ClassPath.SOURCE),
        // Collections.<String>emptyList(),
        // Collections.<String>emptyList()));
        // }
        //
        // if (index != null) {
        // Set<IndexedClass> classes = index.getClasses(request.prefix,
        // QuerySupport.Kind.CASE_INSENSITIVE_PREFIX,
        // true, false, false);
        //
        // if (classes.size() == 0) {
        // LOG.log(Level.FINEST, "Nothing found in GroovyIndex");
        // } else {
        // LOG.log(Level.FINEST, "Found this number of classes : {0} ",
        // classes.size());
        //
        // Set<TypeHolder> typelist = new HashSet<TypeHolder>();
        //
        // for (IndexedClass indexedClass : classes) {
        // LOG.log(Level.FINEST, "FQN classname from index : {0} ",
        // indexedClass.getFqn());
        //
        // ElementKind ek;
        // if (indexedClass.getKind() ==
        // org.netbeans.modules.csl.api.ElementKind.CLASS) {
        // ek = ElementKind.CLASS;
        // } else {
        // ek = ElementKind.INTERFACE;
        // }
        //
        // typelist.add(new TypeHolder(indexedClass.getFqn(), ek));
        // }
        //
        // for (TypeHolder type : typelist) {
        // addToProposalUsingFilter(addedTypes, proposals, request, type,
        // onlyInterfaces);
        // }
        // }
        // }
        // }

        List<String> localDefaultImports = new ArrayList<String>();

        // Are there any manually imported types?

        if (mn != null)
        {

            // this gets the list of full-qualified names of imports.
            List<ImportNode> imports = mn.getImports();

            if (imports != null)
            {
                for (ImportNode importNode : imports)
                {
                    ElementKind ek;

                    if (importNode.getClass().isInterface())
                    {
                        ek = ElementKind.INTERFACE;
                    }
                    else
                    {
                        ek = ElementKind.CLASS;
                    }

                    addToProposalUsingFilter(addedTypes, proposals, request,
                            new TypeHolder(importNode.getClassName(), ek),
                            onlyInterfaces);
                }
            }

            // this returns a list of String's of wildcard-like included types.
            List<String> importsPkg = mn.getImportPackages();

            for (String wildcardImport : importsPkg)
            {
                if (wildcardImport.endsWith("."))
                {
                    wildcardImport = wildcardImport.substring(0, wildcardImport
                            .length() - 1);
                }

                localDefaultImports.add(wildcardImport);

            }

        }

        // Now we compute the type-proposals for the default imports.
        // First, create a list of default JDK packages. These are reused,
        // so they are defined elsewhere.

        localDefaultImports.addAll(defaultImports);

        // adding types from default import, optionally filtered by
        // prefix

        for (String singlePackage : localDefaultImports)
        {
            // List<TypeHolder> typeList;
            //
            // typeList = getElementListForPackageAsTypeHolder(javaSource,
            // singlePackage, currentPackage);
            //
            // if (typeList == null) {
            // LOG.log(Level.FINEST, "Typelist is null for package : {0}",
            // singlePackage);
            // continue;
            // }
            //
            // LOG.log(Level.FINEST, "Number of types found:  {0}",
            // typeList.size());
            //
            // for (TypeHolder element : typeList) {
            // addToProposalUsingFilter(addedTypes, proposals, request, element,
            // onlyInterfaces);
            // }
        }

        // Adding two single classes per hand
        addToProposalUsingFilter(addedTypes, proposals, request,
                new TypeHolder("java.math.BigDecimal", ElementKind.CLASS),
                onlyInterfaces);
        addToProposalUsingFilter(addedTypes, proposals, request,
                new TypeHolder("java.math.BigInteger", ElementKind.CLASS),
                onlyInterfaces);
        return true;
    }

    /**
     * Adds the type given in fqn with its simple name to the proposals,
     * filtered by the prefix and the package name.
     * 
     * @param proposals
     * @param request
     * @param fqn
     */
    void addToProposalUsingFilter(Set<TypeHolder> alreadyPresent,
            List<CompletionItem> proposals, CompletionRequest request,
            TypeHolder type, boolean onlyInterfaces)
    {

        if ((onlyInterfaces && (type.getKind() != ElementKind.INTERFACE))
                || alreadyPresent.contains(type)) { return; }

        String typeName = stripPackage(type.getName());

        if (typeName.toUpperCase(Locale.ENGLISH).startsWith(
                request.prefix.toUpperCase(Locale.ENGLISH)))
        {
            alreadyPresent.add(type);
            // ????proposals.add(new CompletionItem.TypeItem(typeName, anchor,
            // type
            // .getKind()));
        }

        return;
    }

      /**
     * Get the ClassNode for the before-dot expression. This is important for
     * field and method completion.
     * <p>
     * If the <code>request.declaringClass</code> is not <code>null</code> this
     * value is immediately returned.
     * <p>
     * Returned value is stored to <code>request.declaringClass</code> too.
     * 
     * Here are some sample paths:
     * 
     * new String(). [ModuleNode:ConstructorCallExpression:ExpressionStatement:
     * ConstructorCallExpression:]
     * 
     * new String().[caret] something_unrelated
     * [ModuleNode:ClassNode:MethodCallExpression] for this case we have to go
     * for object expression of the method call
     * 
     * s.
     * [ModuleNode:VariableExpression:ExpressionStatement:VariableExpression:]
     * 
     * s.spli
     * [ModuleNode:PropertyExpression:ConstantExpression:ExpressionStatement
     * :PropertyExpression:ConstantExpression:]
     * 
     * l.
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:VariableExpression:]
     * 
     * l.ab
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression
     * :ConstantExpression:]
     * 
     * l.M
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression
     * :VariableExpression:ConstantExpression:]
     * 
     * @param request
     * @return a valid ASTNode or null
     */
    private ClassNode getBeforeDotDeclaringClass(CompletionRequest request)
    {

        if (request.declaringClass != null
                && request.declaringClass instanceof ClassNode)
            return request.declaringClass;

        // FIXME move this up
        DotCompletionContext dotCompletionContext = getDotCompletionContext(request);

        // FIXME static/script context...
        if (!request.isBehindDot()
                && (request.location == CaretLocation.INSIDE_CLOSURE || request.location == CaretLocation.INSIDE_METHOD))
        {
            request.declaringClass = getSurroundingClassNode(request);
            return request.declaringClass;
        }

        if (dotCompletionContext == null
                || dotCompletionContext.getAstPath() == null
                || dotCompletionContext.getAstPath().leaf() == null) { return null; }

        request.beforeDotPath = dotCompletionContext.getAstPath();

        ClassNode declClass = null;

        // experimental type inference
        GroovyTypeAnalyzer typeAnalyzer = new GroovyTypeAnalyzer(request.doc);
        Set<ClassNode> infered = typeAnalyzer.getTypes(dotCompletionContext
                .getAstPath(), dotCompletionContext.getAstOffset());
        // FIXME multiple types
        // FIXME is there any test (?)
        if (!infered.isEmpty()) { return infered.iterator().next(); }

        // type inferred
        if (declClass != null)
        {
            request.declaringClass = declClass;
            return request.declaringClass;
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof VariableExpression)
        {
            VariableExpression variable = (VariableExpression) dotCompletionContext
                    .getAstPath().leaf();
            if ("this".equals(variable.getName()))
            { // NOI18N
                request.declaringClass = getSurroundingClassNode(request);
                return request.declaringClass;
            }
            if ("super".equals(variable.getName()))
            { // NOI18N
                ClassNode thisClass = getSurroundingClassNode(request);
                request.declaringClass = thisClass.getSuperClass();
                if (request.declaringClass == null) { return new ClassNode(
                        "java.lang.Object", ClassNode.ACC_PUBLIC, null); }
                return request.declaringClass;
            }
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof Expression)
        {
            Expression expression = (Expression) dotCompletionContext
                    .getAstPath().leaf();

            // see http://jira.codehaus.org/browse/GROOVY-3050
            if (expression instanceof RangeExpression
                    && "java.lang.Object"
                            .equals(expression.getType().getName()))
            { // NOI18N
                try
                {
                    expression.setType(new ClassNode(Class
                            .forName("groovy.lang.Range"))); // NOI18N
                }
                catch (ClassNotFoundException ex)
                {
                    expression.setType(new ClassNode("groovy.lang.Range",
                            ClassNode.ACC_PUBLIC | ClassNode.ACC_INTERFACE,
                            null)); // NOI18N
                }
            }
            request.declaringClass = expression.getType();
        }

        return request.declaringClass;
    }

    /**
     * Get the parameter-list of this executable as String
     * 
     * @param exe
     * @return
     */
    public static String getParameterListForMethod(ExecutableElement exe)
    {
        StringBuffer sb = new StringBuffer();

        if (exe != null)
        {
            // generate a list of parameters
            // unfortunately, we have to work around # 139695 in an ugly fashion

            List<? extends VariableElement> params = null;

            try
            {
                params = exe.getParameters(); // this can cause NPE's

                for (VariableElement variableElement : params)
                {
                    TypeMirror tm = variableElement.asType();

                    if (sb.length() > 0)
                    {
                        sb.append(", ");
                    }

                    if (tm.getKind() == javax.lang.model.type.TypeKind.DECLARED
                            || tm.getKind() == javax.lang.model.type.TypeKind.ARRAY)
                    {
                        sb.append(stripPackage(tm.toString()));
                    }
                    else
                    {
                        sb.append(tm.toString());
                    }
                }
            }
            catch (NullPointerException e)
            {
                // simply do nothing.
            }

        }

        return sb.toString();
    }

    /**
     * Complete the methods invokable on a class.
     * 
     * @param proposals
     *            the CompletionProposal List we populate (return value)
     * @param request
     *            location information used as input
     * @return true if we found something usable
     */
    private boolean completeMethods(final List<CompletionItem> proposals,
            final CompletionRequest request)
    {
        if (request.location == CaretLocation.INSIDE_PARAMETERS) return false;
        // check whether we are either:
        //
        // 1.) This is a constructor-call like: String s = new String|
        // 2.) right behind a dot. Then we look for:
        // 2.1 method on collection type: List, Map or Range
        // 2.2 static/instance method on class or object
        // 2.3 Get apropriate groovy-methods from index.
        // 2.4 dynamic, mixin method on Groovy-object like getXbyY()

        // 2.2 static/instance method on class or object
        ClassNode declaringClass = getBeforeDotDeclaringClass(request);

        if (declaringClass == null) return false;

        /*
         * Here we need to figure out, whether we want to complete a variable:
         * 
         * s.|
         * 
         * where we want to complete fields and methodesOR a package prefix
         * like:
         * 
         * java.|
         * 
         * To achive this we only complete methods if there is no basePackage,
         * which is a valid package.
         */

        PackageCompletionRequest packageRequest = getPackageRequest(request);

        if (packageRequest.basePackage.length() > 0) return false;

        List<CompletionItem> result = getMethodsInner(
                getSurroundingClassNode(request), declaringClass,
                request.prefix, anchor, 0, request.dotContext != null);
        proposals.addAll(result);

        return true;
    }

    private List<CompletionItem> getMethodsInner(ClassNode source,
            ClassNode node, String prefix, int anchor, int level,
            boolean nameOnly)
    {

        List<CompletionItem> result = new ArrayList<CompletionItem>();
        boolean leaf = (level == 0);
        ClassNode typeNode = node;

        if (typeNode.getSuperClass() != null)
        {
            fillSuggestions(getMethodsInner(source, typeNode.getSuperClass(),
                    prefix, anchor, level + 1, nameOnly), result);
        }
        else if (leaf)
        {
            ;// fillSuggestions(JavaElementHandler.forCompilationInfo(info)
            // .getMethods("java.lang.Object", prefix, anchor, new String[]{},
            // false, nameOnly), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces())
        {
            fillSuggestions(getMethodsInner(source, inter, prefix, anchor,
                    level + 1, nameOnly), result);
        }
        
        for(MethodNode m : node.getMethods())
        {
            ClassNode cn = m.getReturnType();
            Parameter[] ps = m.getParameters();
            String types[] = new String[ps.length];
            String names[] = new String[ps.length];
            for(int i = 0; i < ps.length; i++)
            {
                types[i] = get_cls_name(ps[i].getDeclaringClass());
                names[i] = ps[i].getName();
            }
            result.add(new JSCompletionProvider.JSMethod(m.getName(), get_cls_name(cn), types, names, m.getModifiers())); 
        }

        return result;
    }
    
    private static String get_cls_name(ClassNode n)
    {
        return n != null ? n.getName(): "Object"; 
    }

    private static <T> void fillSuggestions(List<CompletionItem> input,
            List<CompletionItem> result)
    {
        for (CompletionItem entry : input)
        {
            if (!result.contains(entry))
            {
                result.add(entry);
            }
        }
    }

    public List<CompletionItem> complete(GroovyParserResult info, Element el,
            String prefix, int lexOffset)
    {
        final int astOffset = lexOffset;
        // Avoid all those annoying null checks
        if (prefix == null) prefix = "";

        List<CompletionItem> proposals = new ArrayList<CompletionItem>();

        anchor = lexOffset - prefix.length();

        CompletionRequest request = new CompletionRequest();
        request.lexOffset = lexOffset;
        request.astOffset = astOffset;
        request.doc = el;
        request.info = info;
        request.prefix = prefix;
        request.scriptMode = false;
        request.path = getPathFromRequest(request);
        // here we figure out once for all completions, where we are inside the
        // source (in method, in class, ouside class etc)

        request.location = getCaretLocationFromRequest(request);

        // // if we are above a package statement or inside a comment there's no
        // completion at all.
        // if (request.location == CaretLocation.ABOVE_PACKAGE ||
        // request.location == CaretLocation.INSIDE_COMMENT) {
        // return new DefaultCompletionResult(proposals, false);
        // }

        // now let's figure whether we are in some sort of definition line

        // request.ctx = getCompletionContext(request);

        // Are we invoked right behind a dot? This is information is used later
        // on in
        // a couple of completions.

        // assert request.ctx != null;

        request.dotContext = getDotCompletionContext(request);

        if (request.isBehindDot())
        {
            request.declaringClass = getBeforeDotDeclaringClass(request);
        }

        boolean definitionLine = false; // checkForVariableDefinition(request);

        // are we're right behind an import statement?
        request.behindImport = checkForRequestBehindImportStatement(request);

        if (!(request.location == CaretLocation.OUTSIDE_CLASSES || request.location == CaretLocation.INSIDE_STRING))
        {
            // complete packages
            completePackages(proposals, request);

            // complete classes, interfaces and enums

            completeTypes(proposals, request);

        }

        if (!request.behindImport)
        {

            if (request.location != CaretLocation.INSIDE_STRING)
            {
                // complete keywords
                // completeKeywords(proposals, request);

                // complete methods
                completeMethods(proposals, request);
            }

            // complete fields
            completeFields(proposals, request);

            // complete local variables
            completeLocalVars(proposals, request);

        }

        // proposals for new vars
        // completeNewVars(proposals, request, newVars);
        return proposals;
    }

    /**
     * create the signature-string of this method usable as a Javadoc URL suffix
     * (behind the # )
     * 
     * This was needed, since from groovy 1.5.4 to 1.5.5 the
     * MetaMethod.getSignature() changed from human-readable to Class.getName()
     * output.
     * 
     * To make matters worse, we have some subtle differences between JDK and
     * GDK MetaMethods
     * 
     * method.getSignature for the JDK gives the return- value right behind the
     * method and encodes like Class.getName():
     * 
     * codePointCount(II)I
     * 
     * GDK-methods look like this: java.lang.String center(java.lang.Number,
     * java.lang.String)
     * 
     * TODO: if groovy folks ever change this (again), we're falling flat on our
     * face.
     * 
     */
    public static String getMethodSignature(MetaMethod method, boolean forURL,
            boolean isGDK)
    {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK)
        {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1)
            {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL)
            {
                methodSignature = methodSignature.replaceAll(", ", ",%20");
            }

            return methodSignature;

        }
        else
        {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) { return ""; }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName()
     */
    static String decodeTypes(final String encodedType, boolean forURL)
    {

        String DELIMITER = ",";

        if (forURL)
        {
            DELIMITER = DELIMITER + "%20";
        }
        else
        {
            DELIMITER = DELIMITER + " ";
        }

        StringBuffer sb = new StringBuffer("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++)
        {
            char c = encodedType.charAt(i);

            if (c == '[')
            {
                nextIsAnArray = true;
                continue;
            }
            else if (c == 'Z')
            {
                sb.append("boolean");
            }
            else if (c == 'B')
            {
                sb.append("byte");
            }
            else if (c == 'C')
            {
                sb.append("char");
            }
            else if (c == 'D')
            {
                sb.append("double");
            }
            else if (c == 'F')
            {
                sb.append("float");
            }
            else if (c == 'I')
            {
                sb.append("int");
            }
            else if (c == 'J')
            {
                sb.append("long");
            }
            else if (c == 'S')
            {
                sb.append("short");
            }
            else if (c == 'L')
            { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                if (forURL)
                {
                    sb.append(typeName);
                }
                else
                {
                    sb.append(stripPackage(typeName));
                }

                i = semicolon;
            }

            if (nextIsAnArray)
            {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1)
            {
                sb.append(DELIMITER);
            }

        }

        return sb.toString();
    }

    // public String document(ParserResult info, ElementHandle element) {
    // LOG.log(Level.FINEST, "document(), ElementHandle : {0}", element);
    //
    // String error = "Groovy JavaDoc Not Found";
    // String doctext = null;
    //
    // if (element instanceof AstMethodElement) {
    // AstMethodElement ame = (AstMethodElement) element;
    //
    // String base = "";
    //
    // String javadoc = getGroovyJavadocBase();
    // if (jdkJavaDocBase != null && ame.isGDK() == false) {
    // base = jdkJavaDocBase;
    // } else if (javadoc != null && ame.isGDK() == true) {
    // base = javadoc;
    // } else {
    // LOG.log(Level.FINEST, "Neither JDK nor GDK or error locating: {0}",
    // ame.isGDK());
    // return error;
    // }
    //
    // MetaMethod mm = ame.getMethod();
    //
    // // enable this to troubleshoot subtle differences in JDK/GDK signatures
    // printMethod(mm);
    //
    // // figure out who originally defined this method
    //
    // String className;
    //
    // if (ame.isGDK()) {
    // className = mm.getDeclaringClass()/*.getCachedClass()*/.getName();
    // } else {
    //
    // String declName = null;
    //
    // if (mm != null) {
    // CachedClass cc = mm.getDeclaringClass();
    // if (cc != null) {
    // declName = cc.getName();
    // }
    // /*CachedClass cc = mm.getDeclaringClass();
    // if (cc != null) {
    // Class clz = cc.getCachedClass();
    // if (clz != null) {
    // declName = clz.getName();
    // }
    // }*/
    // }
    //
    // if (declName != null) {
    // className = declName;
    // } else {
    // className = ame.getClz().getName();
    // }
    // }
    //
    // // create path from fq java package name:
    // // java.lang.String -> java/lang/String.html
    // String classNamePath = className.replace(".", "/");
    // classNamePath = classNamePath + ".html"; // NOI18N
    //
    // // if the file can be located in the GAPI folder prefer it
    // // over the JDK
    // if (!ame.isGDK()) {
    //
    // URL url;
    // File testFile;
    //
    // String apiDoc = getGroovyApiDocBase();
    // try {
    // url = new URL(apiDoc + classNamePath);
    // testFile = new File(url.toURI());
    // } catch (MalformedURLException ex) {
    // LOG.log(Level.FINEST, "MalformedURLException: {0}", ex);
    // return error;
    // } catch (URISyntaxException uriEx) {
    // LOG.log(Level.FINEST, "URISyntaxException: {0}", uriEx);
    // return error;
    // }
    //
    // if (testFile != null && testFile.exists()) {
    // base = apiDoc;
    // }
    // }
    //
    // // create the signature-string of the method
    // String sig = getMethodSignature(ame.getMethod(), true, ame.isGDK());
    // String printSig = getMethodSignature(ame.getMethod(), false,
    // ame.isGDK());
    //
    // String urlName = base + classNamePath + "#" + sig;
    //
    // try {
    // LOG.log(Level.FINEST, "Trying to load URL = {0}", urlName); // NOI18N
    // doctext = HTMLJavadocParser.getJavadocText(
    // new URL(urlName),
    // false,
    // ame.isGDK());
    // } catch (MalformedURLException ex) {
    // LOG.log(Level.FINEST, "document(), URL trouble: {0}", ex); // NOI18N
    // return error;
    // }
    //
    // // If we could not find a suitable JavaDoc for the method - say so.
    // if (doctext == null) {
    // return error;
    // }
    //
    // doctext = "<h3>" + className + "." + printSig + "</h3><BR>" + doctext;
    // }
    // return doctext;
    // }

    // public ElementHandle resolveLink(String link, ElementHandle
    // originalHandle)
    // {
    // // pass the original handle back. That's better than to throw an
    // // unsupported-exception.
    // return originalHandle;
    // }

    // public String getPrefix(ParserResult info, int caretOffset,
    // boolean upToOffset)
    // {
    // return null;
    // }

    // public String resolveTemplateVariable(String variable, ParserResult info,
    // int caretOffset, String name, Map parameters)
    // {
    // return null;
    // }
    //
    // public Set<String> getApplicableTemplates(ParserResult info,
    // int selectionBegin, int selectionEnd)
    // {
    // return Collections.emptySet();
    // }

    public static String stripPackage(String fqn)
    {

        if (fqn.contains("."))
        {
            int idx = fqn.lastIndexOf(".");
            fqn = fqn.substring(idx + 1);
        }

        // every now and than groovy comes with tailing
        // semicolons. We got to get rid of them.

        return fqn.replace(";", "");
    }

    // public ParameterInfo parameters(ParserResult info, int caretOffset,
    // CompletionItem proposal) {
    // LOG.log(Level.FINEST, "parameters(), caretOffset = {0}", caretOffset); //
    // NOI18N
    //
    // // here we need to calculate the list of parameters for the methods under
    // the caret.
    // // proposal seems to be null all the time.
    //
    // List<String> paramList = new ArrayList<String>();
    //
    // AstPath path = getPathFromInfo(caretOffset, info);
    //
    // // FIXME parsing API
    // BaseDocument doc = (BaseDocument)
    // info.getSnapshot().getSource().getDocument(true);
    //
    // if (path != null) {
    //
    // ArgumentListExpression ael = getSurroundingArgumentList(path);
    //
    // if (ael != null) {
    //
    // List<ASTNode> children = AstUtilities.children(ael);
    //
    // // populate list with *all* parameters, but let index and offset
    // // point to a specific parameter.
    //
    // int idx = 1;
    // int index = -1;
    // int offset = -1;
    //
    // for (ASTNode node : children) {
    // OffsetRange range = AstUtilities.getRange(node, doc);
    // paramList.add(node.getText());
    //
    // if (range.containsInclusive(caretOffset)) {
    // offset = range.getStart();
    // index = idx;
    // }
    //
    // idx++;
    // }
    //
    // // calculate the parameter we are dealing with
    //
    // if (paramList != null && !paramList.isEmpty()) {
    // return new ParameterInfo(paramList, index, offset);
    // }
    // } else {
    // LOG.log(Level.FINEST, "ArgumentListExpression ==  null"); // NOI18N
    // return ParameterInfo.NONE;
    // }
    //
    // } else {
    // LOG.log(Level.FINEST, "path ==  null"); // NOI18N
    // return ParameterInfo.NONE;
    // }
    //
    // return ParameterInfo.NONE;
    //
    // }

    // FIXME make it ordinary class and/or split it
    static class CompletionRequest
    {
        GroovyParserResult info;
        int lexOffset;
        int astOffset;
        Element doc;
        String prefix = "";
        CaretLocation location;
        boolean scriptMode;
        boolean behindImport;
        AstPath path;
        AstPath beforeDotPath;
        ClassNode declaringClass;
        DotCompletionContext dotContext;

        public boolean isBehindDot()
        {
            return dotContext != null;
        }

    }

    private static class TypeHolder
    {
        private final String name;
        private final ElementKind kind;

        public TypeHolder(String name, ElementKind kind)
        {
            this.name = name;
            this.kind = kind;
        }

        public ElementKind getKind()
        {
            return kind;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final TypeHolder other = (TypeHolder) obj;
            if ((this.name == null) ? (other.name != null) : !this.name
                    .equals(other.name)) { return false; }
            if (this.kind != other.kind) { return false; }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

    }

    public enum CaretLocation
    {

        ABOVE_PACKAGE("ABOVE_PACKAGE"), // above the "package" statement (if
        // any).
        ABOVE_FIRST_CLASS("ABOVE_FIRST_CLASS"), // Outside any classs and above
        // the first class or interface
        // stmt.
        OUTSIDE_CLASSES("OUTSIDE_CLASSES"), // Outside any class but behind some
        // class or interface stmt.
        INSIDE_CLASS("INSIDE_CLASS"), // inside a class definition but not in a
        // method.
        INSIDE_METHOD("INSIDE_METHOD"), // in a method definition.
        INSIDE_CLOSURE("INSIDE_CLOSURE"), // inside a closure definition.
        INSIDE_PARAMETERS("INSIDE_PARAMETERS"), // inside a parameter-list
        // definition (signature) of a
        // method.
        INSIDE_COMMENT("INSIDE_COMMENT"), // inside a line or block comment
        INSIDE_STRING("INSIDE_STRING"), // inside string literal
        UNDEFINED("UNDEFINED");

        private String id;

        CaretLocation(String id)
        {
            this.id = id;
        }
    }
}
