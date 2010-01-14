/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.net.MalformedURLException;
import java.net.URL;
//import org.netbeans.modules.csl.api.ElementKind;
//import org.netbeans.modules.csl.api.Modifier;
//import org.netbeans.modules.csl.api.OffsetRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.csl.spi.GsfUtilities;
//import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
//import org.netbeans.modules.parsing.api.Source;
//import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
//import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileStateInvalidException;
//import org.openide.filesystems.URLMapper;
//import org.openide.util.Exceptions;

import seco.notebook.csl.ElementKind;
import seco.notebook.csl.IndexResult;
import seco.notebook.csl.Modifier;


/**
 * An element coming from the Lucene index - not tied to an AST.
 * To obtain an equivalent AST element, use AstUtilities.getForeignNode().
 * 
 * @author Tor Norbye
 */
public abstract class IndexedElement extends JsElement {

    private static final Logger LOG = Logger.getLogger(IndexedElement.class.getName());
    
    protected ElementKind kind;
    protected String fqn;
    protected String name;
    protected String in;
    protected JsIndex index;
    protected IndexResult indexResult;
    protected Document document;
    protected int flags;
    protected String attributes;
    //protected EnumSet<BrowserVersion> compatibility;
    protected String signature;
    protected boolean smart;
    protected boolean inherited = true;

    IndexedElement(String fqn, String name, String in, JsIndex index, IndexResult indexResult, String attributes, int flags, ElementKind kind) {
        this.fqn = fqn;
        this.name = name;
        this.in = in;
        this.index = index;
        this.indexResult = indexResult;
        this.attributes = attributes;
        this.flags = flags;
        this.kind = kind;
    }

    static IndexedElement create(String attributes, String fqn, String name, String in, int attrIndex, JsIndex index, IndexResult indexResult, boolean createPackage) {
        int flags = IndexedElement.decode(attributes, attrIndex, 0);
        if (createPackage) {
            IndexedPackage func = new IndexedPackage(fqn, name, in, index, indexResult, attributes, flags, ElementKind.PACKAGE);
            return func;
        }
        if ((flags & FUNCTION) != 0) {
            ElementKind kind =((flags & CONSTRUCTOR) != 0) ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
            IndexedFunction func = new IndexedFunction(fqn, name, in, index, indexResult, attributes, flags, kind);
            return func;
        } else if ((flags & GLOBAL) != 0) {
            ElementKind kind = Character.isUpperCase(name.charAt(0)) ? ElementKind.CLASS : ElementKind.GLOBAL;
            IndexedProperty property = new IndexedProperty(fqn, name, in, index, indexResult, attributes, flags, kind);
            return property;
        } else {
            IndexedProperty property = new IndexedProperty(fqn, name, in, index, indexResult, attributes, flags, ElementKind.PROPERTY);
            return property;
        }
    }

    static IndexedElement create(String name, String signature, JsIndex index, IndexResult indexResult, boolean createPackage) {
        String elementName = null;
        int nameEndIdx = signature.indexOf(';');
        assert nameEndIdx != -1;
        elementName = signature.substring(0, nameEndIdx);
        nameEndIdx++;

        String funcIn = null;
        int inEndIdx = signature.indexOf(';', nameEndIdx);
        assert inEndIdx != -1;
        if (inEndIdx > nameEndIdx+1) {
            funcIn = signature.substring(nameEndIdx, inEndIdx);
        }
        inEndIdx++;

        int startCs = inEndIdx;
        inEndIdx = signature.indexOf(';', startCs);
        assert inEndIdx != -1;
        if (inEndIdx > startCs) {
            // Compute the case sensitive name
            elementName = signature.substring(startCs, inEndIdx);
        }
        inEndIdx++;
        
        String fqn = null; // Compute lazily
        int lastDot = elementName.lastIndexOf('.');
        if (name.length() < lastDot) {
            int nextDot = elementName.indexOf('.', name.length());
            if (nextDot != -1) {
                String pkg = elementName.substring(0, nextDot);
                IndexedPackage element = new IndexedPackage(null, pkg, fqn, index, indexResult, signature, IndexedElement.decode(signature, inEndIdx, 0), ElementKind.PACKAGE);
                return element;
            }
        }
        
        IndexedElement element = IndexedElement.create(signature, fqn, elementName, funcIn, inEndIdx, index, indexResult, createPackage);
        
        return element;
    }

    
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            signature = sb.toString();
        }

        return signature;
    }
    
    private JsIndex getIndex() {
        return index;
    }

    public String getFqn() {
        if (fqn == null) {
            if (in != null && in.length() > 0) {
                fqn = in + "." + name;
            } else {
                fqn = name;
            }
        }
        return fqn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return in;
    }
    
    void setKind(ElementKind kind) {
        this.kind = kind;
    }

    @Override
    public ElementKind getKind() {
        return kind;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (isStatic()) {
            if (isPrivate()) {
                return AstElement.STATIC_PRIVATE;
            }
            return AstElement.STATIC;
        } else if (isPrivate()) {
            return AstElement.PRIVATE;
        }
        return Collections.emptySet();
    }

    protected final String getFilenameUrl() {
//        try {
//            FileObject entryFO = indexResult.getFile();
//            if(entryFO != null) {
//                //test if the entry fo is non null since the file may be already
//                //deleted (completion invoked after the file deletion but before
//                //index update
//                return entryFO.getURL().toExternalForm();
//            }
//        } catch (FileStateInvalidException ex) {
//            LOG.log(Level.WARNING, null, ex);
//        }
        return null;
    }

    public Document getDocument() {
        if (document == null) {
//            FileObject fo = getFileObject();
//
//            if (fo != null) {
//                document = GsfUtilities.getDocument(fo, true);
//            }
        }

        return document;
    }

//    @Override
//    public FileObject getFileObject() {
//        return indexResult.getFile();
//    }

    protected int getAttributeSection(int section) {
        assert section != 0; // Obtain directly, and logic below (+1) is wrong
        int attributeIndex = 0;
        for (int i = 0; i < section; i++) {
            attributeIndex = attributes.indexOf(';', attributeIndex+1);
        }
        
        assert attributeIndex != -1;
        return attributeIndex + 1;
    }
    
    int getDocOffset() {
        int docOffsetIndex = getAttributeSection(DOC_INDEX);
        if (docOffsetIndex != -1) {
            int docOffset = IndexedElement.decode(attributes, docOffsetIndex,-1);
            return docOffset;
        }
        return -1;
    }
    
    protected int getNodeOffset() {
        int docOffsetIndex = getAttributeSection(NODE_INDEX);
        if (docOffsetIndex != -1) {
            return IndexedElement.decode(attributes, docOffsetIndex, -1);
        }        
        
        return -1;
    }

    private Document getSdocDocument() {
//        try {
//            //read the sdocurl field in the indexdocument
//            String sdocurl = indexResult.getValue(JsIndexer.FIELD_SDOC_URL);
//            if (sdocurl != null) {
//                URL url = new URL(sdocurl);
//                FileObject resource = URLMapper.findFileObject(url);
//                if (resource != null) {
//                    Document doc = GsfUtilities.getDocument(resource, true);
//                    return doc;
//                } else {
//                    LOG.warning("Cannot find FileObject for " + url.toExternalForm());
//                }
//            }
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        return null;
    }

    protected List<String> getComments() {
//        int docOffsetIndex = getAttributeSection(DOC_INDEX);
//        if (docOffsetIndex != -1) {
//            int docOffset = IndexedElement.decode(attributes, docOffsetIndex,-1);
//            if (docOffset == -1) {
//                return null;
//            }
//            try {
//                //get the sdoc document if present
//                BaseDocument doc = (BaseDocument) getSdocDocument();
//                if (doc == null) {
//                    return null;
//                }
//                if (docOffset < doc.getLength()) {
//                    //return LexUtilities.gatherDocumentation(null, doc, docOffset);
//                    Source docSource = Source.create(doc);
//                    OffsetRange range = LexUtilities.getCommentBlock(docSource.createSnapshot(), docOffset, false);
//                    if (range != OffsetRange.NONE) {
//                        String comment = doc.getText(range.getStart(), range.getLength());
//                        String[] lines = comment.split("\n");
//                        List<String> comments = new ArrayList<String>();
//                        for (int i = 0, n = lines.length; i < n; i++) {
//                            String line = lines[i];
//                            line = line.trim();
//                            if (i == n-1 && line.endsWith("*/")) {
//                                line = line.substring(0,line.length()-2);
//                            }
//                            if (line.startsWith("/**")) {
//                                comments.add(line.substring(3));
//                            } else if (line.startsWith("/*")) {
//                                comments.add(line.substring(2));
//                            } else if (line.startsWith("//")) {
//                                comments.add(line.substring(2));
//                            } else if (line.startsWith("*")) {
//                                comments.add(line.substring(1));
//                            } else {
//                                comments.add(line);
//                            }
//                        }
//                        return comments;
//                    }
//                    return Collections.emptyList();
//                }
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
            
        return null;
    }
    
//    public EnumSet<BrowserVersion> getCompatibility() {
//        if (compatibility == null) {
//            int flagIndex = getAttributeSection(BROWSER_INDEX);
//            if (flagIndex != -1) {
//                int endIndex = attributes.indexOf(';', flagIndex);
//                assert endIndex != -1;
//                if (endIndex == flagIndex) {
//                    return BrowserVersion.ALL;
//                }
//                String compat = attributes.substring(flagIndex, endIndex);
//                compatibility = BrowserVersion.fromCompactFlags(compat);
//            } else {
//                compatibility = BrowserVersion.ALL;
//            }
//        }
//        
//        return compatibility;
//    }

    public String getType() {
        if (kind == ElementKind.CLASS || kind == ElementKind.PACKAGE) {
            return null;
        }
        int typeIndex = getAttributeSection(TYPE_INDEX);
        int endIndex = attributes.indexOf(';', typeIndex);
        if (endIndex > typeIndex) {
            return attributes.substring(typeIndex, endIndex);
        }
        
        return null;
    }

    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    public boolean isSmart() {
        return smart;
    }
    
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public boolean isInherited() {
        return inherited;
    }
    

    public IndexedElement findDocumentedSibling() {
//        if (!isDocumented()) {
//            String queryName = null;
//            String queryType = getFqn();
//            if (queryType.indexOf('.') == -1) {
//                queryName = queryType;
//                queryType = null;
//            }
//            Set<IndexedElement> elements = getIndex().getAllElements(queryName, queryType, QuerySupport.Kind.EXACT, null);
//            for (IndexedElement e : elements) {
//                if (e.isDocumented()) {
//                    return e;
//                }
//            }
//        }
        
        return null;
    }

    public IndexedElement findRealFileElement() {
//        if (isDocOnly()) {
//            String queryName = null;
//            String queryType = getFqn();
//            if (queryType.indexOf('.') == -1) {
//                queryName = queryType;
//                queryType = null;
//            }
//            Set<IndexedElement> elements = getIndex().getAllElements(queryName, queryType, QuerySupport.Kind.EXACT, null);
//            for (IndexedElement e : elements) {
//                if (!e.isDocOnly()) {
//                    return e;
//                }
//            }
//        }
        
        return null;
    }
    
    protected static final int NAME_INDEX = 0;
    protected static final int IN_INDEX = 1;
    protected static final int CASE_SENSITIVE_INDEX = 2;
    protected static final int FLAG_INDEX = 3;
    protected static final int ARG_INDEX = 4;
    protected static final int NODE_INDEX = 5;
    protected static final int DOC_INDEX = 6;
    protected static final int BROWSER_INDEX = 7;
    protected static final int TYPE_INDEX = 8;
    
    
    
    // ------------- Flags/attributes -----------------

    // This should go into IndexedElement
    
    // Other attributes:
    // is constructor? prototype?
    
    // Plan: Stash a single item for class entries so I can search by document for the class.
    // Add more types into the types
    /** This method is documented */
    public static final int DOCUMENTED = 1 << 0;
    /** This method is private */
    public static final int PRIVATE = 1 << 2;
    /** This is a function, not a property */
    public static final int FUNCTION = 1 << 3;
    /** This element is "static" (e.g. it's a classvar for fields, class method for methods etc) */
    public static final int STATIC = 1 << 4;
    /** This element is deliberately not documented (rdoc :nodoc:) */
    public static final int NODOC = 1 << 5;
    /** This is a global variable */
    public static final int GLOBAL = 1 << 6;
    /** This is a constructor */
    public static final int CONSTRUCTOR = 1 << 7;
    /** This is a deprecated */
    public static final int DEPRECATED = 1 << 8;
    /** This is a documentation-only definition */
    public static final int DOC_ONLY = 1 << 9;
    /** This is a constant/final */
    public static final int FINAL = 1 << 10;

    /** Return a string (suitable for persistence) encoding the given flags */
    public static String encode(int flags) {
        return Integer.toString(flags,16);
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int decode(String s, int startIndex, int defaultValue) {
        int value = 0;
        for (int i = startIndex, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c == ';') {
                if (i == startIndex) {
                    return defaultValue;
                }
                break;
            }

            value = value << 4;
 
            if (c > '9') {
                value += c-'a'+10;
            } else {
                value += c-'0';
            }
        }
        
        return value;
    }
    
    public static int getFlags(AstElement element) {
        // Return the flags corresponding to the given AST element
        int value = 0;

        ElementKind k = element.getKind();
        if (k == ElementKind.CONSTRUCTOR) {
            value = value | CONSTRUCTOR;
        }
        if (k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) {
            value = value | FUNCTION;
        } else if (k == ElementKind.GLOBAL) {
            value = value | GLOBAL;
        }
        if (element.getModifiers().contains(Modifier.STATIC)) {
            value = value | STATIC;
        }
        if (element.getModifiers().contains(Modifier.DEPRECATED)) {
            value = value | DEPRECATED;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            value = value | PRIVATE;
        }

        return value;
    }
    
    public boolean isDocumented() {
        return (flags & DOCUMENTED) != 0;
    }
    
    public boolean isPublic() {
        return (flags & PRIVATE) == 0;
    }

    public boolean isPrivate() {
        return (flags & PRIVATE) != 0;
    }
    
    public boolean isFunction() {
        return (flags & FUNCTION) != 0;
    }

    public boolean isStatic() {
        return (flags & STATIC) != 0;
    }
    
    public boolean isNoDoc() {
        return (flags & NODOC) != 0;
    }

    public boolean isFinal() {
        return (flags & FINAL) != 0;
    }
    
    public boolean isConstructor() {
        return (flags & CONSTRUCTOR) != 0;
    }

    public boolean isDeprecated() {
        return (flags & DEPRECATED) != 0;
    }
    
    public boolean isDocOnly() {
        return (flags & DOC_ONLY) != 0;
    }

    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & DOCUMENTED) != 0) {
            sb.append("|DOCUMENTED");
        }
        
        if ((flags & PRIVATE) != 0) {
            sb.append("|PRIVATE");
        }
        
        if ((flags & CONSTRUCTOR) != 0) {
            sb.append("|CONSTRUCTOR");
        } else if ((flags & FUNCTION) != 0) {
            sb.append("|FUNCTION");
        } else if ((flags & GLOBAL) != 0) {
            sb.append("|GLOBAL");
        } else {
            sb.append("|PROPERTY");
        }
        
        if ((flags & STATIC) != 0) {
            sb.append("|STATIC");
        }

        if ((flags & NODOC) != 0) {
            sb.append("|NODOC");
        }

        if ((flags & DEPRECATED) != 0) {
            sb.append("|DEPRECATED");
        }

        if ((flags & DOC_ONLY) != 0) {
            sb.append("|DOC_ONLY");
        }

        if ((flags & FINAL) != 0) {
            sb.append("|FINAL");
        }

        if (sb.length() > 0) {
            sb.append("|");
        }
        return sb.toString();
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedElement other = (IndexedElement) obj;
        if (!getSignature().equals(other.getSignature())) {
            return false;
        }
//        if (this.flags != other.flags) {
//            return false;
//        }
        if (!getKind().equals(other.getKind())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + getSignature().hashCode();
//        hash = 53 * hash + flags;
        hash = 53 * hash + getKind().hashCode();
        return hash;
    }
    
    public String getOrigin() {
        String filename = getFilenameUrl();
        if (filename != null) {
            int lastSlash = filename.lastIndexOf('/');
            if (lastSlash == -1) {
                return null;
            }
            lastSlash++;
            if (filename.startsWith("stub_core", lastSlash)) { // NOI18N
                return "Core JS";
            } else if (filename.startsWith("stub_", lastSlash)) { // NOI18N
                return "DOM";
            } else if (filename.startsWith("jquery", lastSlash)) { // NOI18N
                return "jQuery";
            } else if (filename.startsWith("dojo", lastSlash)) { // NOI18N
                return "dojo";
            } else if (filename.startsWith("yui", lastSlash)) { // NOI18N
                return "YUI";
            }
            // TODO: Map to sdocs somehow. Tricky because sometimes I get the source
            // element rather than the sdoc when doing equals
            //} else if (filename.endsWith("sdoc")) {
        }

        return null;
    }
}
