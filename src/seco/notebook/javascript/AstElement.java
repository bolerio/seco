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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.mozilla.nb.javascript.FunctionNode; 
import seco.notebook.csl.*;
import seco.notebook.javascript.JsAnalyzer.AnalysisResult;
import seco.notebook.javascript.lexer.LexUtilities;

//import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;

/**
 * 
 * @author Tor Norbye
 */
public class AstElement extends JsElement
{
    public static final Set<Modifier> NONE = EnumSet.noneOf(Modifier.class);
    public static final Set<Modifier> STATIC = EnumSet.of(Modifier.STATIC);
    public static final Set<Modifier> PRIVATE = EnumSet.of(Modifier.PRIVATE);
    public static final Set<Modifier> STATIC_PRIVATE = EnumSet.of(
            Modifier.PRIVATE, Modifier.STATIC);

    protected List<AstElement> children;
    protected Node node;
    protected String name;
    protected String in;
    protected JsParseResult info;
    protected String signature;
    protected ElementKind kind;
    protected String type;
    protected Map<String, String> docProps;
    protected String fqn;

    @SuppressWarnings("unchecked")
    protected Set<Modifier> modifiers;

    AstElement(JsParseResult info, Node node)
    {
        this.info = info;
        this.node = node;
    }

    public String getSignature()
    {
        if (signature == null)
        {
            StringBuilder sb = new StringBuilder();
            String clz = getIn();
            if (clz != null && clz.length() > 0)
            {
                sb.append(clz);
                sb.append("."); // NOI18N
            }
            sb.append(getName());
            signature = sb.toString();
        }

        return signature;
    }

    public Node getNode()
    {
        return node;
    }

    public Map<String, String> getDocProps()
    {
        return docProps;
    }

    public String getFqn()
    {
        if (fqn == null)
        {
            assert name != null;
            if (in != null && in.length() > 0)
            {
                fqn = in + "." + name;
            }
            else
            {
                fqn = name;
            }
        }
        return fqn;
    }

    public String getName()
    {
        if (name == null)
        {
            if (fqn != null)
            {
                int dot = fqn.lastIndexOf('.');
                if (dot != -1)
                {
                    name = fqn.substring(dot + 1);
                    in = fqn.substring(0, dot);
                }
            }
            else if (node.getType() == Token.VAR)
            {
                // Must pull the name out of the child
                if (node.hasChildren())
                {
                    Node child = node.getFirstChild();
                    if (child.getType() == Token.NAME)
                    {
                        name = child.getString();
                    }
                }
            }
            else if (node.isStringNode())
            {
                name = node.getString();
            }
            else if (node.getType() == Token.CALL
                    || node.getType() == Token.NEW)
            {
                name = AstUtilities.getCallName(node, false);
            }
        }

        return name;
    }

    @Override
    public String getIn()
    {
        if (in == null)
        {
            in = ""; // NOI18N
        }
        return in;
    }

    void setKind(ElementKind kind)
    {
        this.kind = kind;
    }

    public ElementKind getKind()
    {
        if (kind == null)
        {
            switch (node.getType())
            {
            case Token.NAME:
            case Token.BINDNAME:
            case Token.PARAMETER:
                return ElementKind.VARIABLE;
            default:
                return ElementKind.OTHER;
            }
        }

        return kind;
    }

    public List<AstElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public String toString()
    {
        return "JsElement:" + getName() + "(" + getKind() + ")"; // NOI18N
    }

    public JsParseResult getParseResult()
    {
        return info;
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        if (modifiers == null)
        {
            boolean deprecated = false, priv = false, constructor = false;
            if (getName().startsWith("_"))
            {
                priv = true;
            }
            if (docProps != null)
            {
                if (docProps.containsKey("@deprecated"))
                { // NOI18N
                    deprecated = true;
                }
                if (docProps.containsKey("@private"))
                { // NOI18N
                    priv = true;
                }
                if (docProps.containsKey("@constructor"))
                { // NOI18N
                    constructor = true;
                }
                if (docProps.containsKey("@return"))
                { // NOI18N
                    type = docProps.get("@return"); // NOI18N
                }
                if (deprecated || priv || constructor)
                {
                    modifiers = EnumSet.noneOf(Modifier.class);
                    if (deprecated)
                    {
                        modifiers.add(Modifier.DEPRECATED);
                    }
                    if (priv)
                    {
                        modifiers.add(Modifier.PRIVATE);
                    }
                    if (constructor)
                    {
                        kind = ElementKind.CONSTRUCTOR;
                    }
                }
                else
                {
                    modifiers = NONE;
                }
            }
            else if (priv)
            {
                modifiers = PRIVATE;
            }
            else
            {
                modifiers = NONE;
            }
        }

        return modifiers;
    }

    void markStatic()
    {
        getModifiers();
        if (modifiers == NONE)
        {
            modifiers = STATIC;
        }
        else
        {
            modifiers = EnumSet.copyOf(modifiers);
            modifiers.add(Modifier.STATIC);
        }
    }

    private void initDocProps(JsParseResult info)
    {
        if (node == null) { return; }

        // Look for parameter hints etc. 
        //TODO:??
//        TokenSequence<? extends JsCommentTokenId> ts = AstUtilities
//                .getCommentFor(info, node);
//
//        if (ts != null)
//        {
//            Map<String, String> typeMap = JsCommentLexer.findFunctionTypes(ts);
//            if (typeMap != null)
//            {
//                docProps = typeMap;
//            }
//        }
    }

    public static AstElement createElement(JsParseResult info, Node node,
            String name, String in, AnalysisResult result)
    {
        // assert node.element == null : node + " in " + info.getText(); //
        // Don't expect to be called multiple times on the same element
        // For incremental compilation this is no longer true
        if (node.element != null)
        {
            node.element = null;
        }

        AstElement js = AstElement.getElement(info, node);

        if ("Element.Methods".equals(in))
        { // NOI18N
            in = "Element"; // NOI18N
        }

        js.name = name;
        js.in = in;

        return js;
    }

    @SuppressWarnings("fallthrough")
    public static AstElement getElement(JsParseResult info, Node node)
    {
        if (node.element != null) { return (AstElement) node.element; }

        switch (node.getType())
        {
        case Token.FUNCTION:
            if (node instanceof FunctionNode)
            {
                AstElement element = new FunctionAstElement(info,
                        (FunctionNode) node);
                element.initDocProps(info);
                node.element = element;
                return element;
            }
            else
            {
                // Fall through
            }
        default:
            AstElement element = new AstElement(info, node);
            element.initDocProps(info);
            node.element = element;
            return element;
        }
    }

    void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        if (type == null)
        {
            type = node.nodeType;
            if (node.nodeType == null)
            {
                getModifiers();
            }
        }

        return type;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result)
    {
        JsParseResult jspr = AstUtilities.getParseResult(result);
        Element object = JsParser.resolveHandle(jspr, this);

        if (object instanceof AstElement)
        {
            Node target = ((AstElement) object).getNode();
            if (target != null)
            {
                return LexUtilities.getLexerOffsets(jspr, new OffsetRange(
                        target.getSourceStart(), target.getSourceEnd()));
            }
            else
            {
                return OffsetRange.NONE;
            }

        }
        else if (object != null)
        {
            Logger.global.log(Level.WARNING, "Foreign element: " + object
                    + " of type " + // NOI18N
                    ((object != null) ? object.getClass().getName() : "null")); // NOI18N

        }
        else
        {
            if (getNode() != null)
            {
                OffsetRange astRange = AstUtilities.getRange(getNode());
                if (astRange != OffsetRange.NONE)
                {
                    JsParseResult oldInfo = info;
                    if (oldInfo == null)
                    {
                        oldInfo = jspr;
                    }
                    return LexUtilities.getLexerOffsets(oldInfo, astRange);
                }
                else
                {
                    return OffsetRange.NONE;
                }
            }
        }

        return OffsetRange.NONE;
    }

}
