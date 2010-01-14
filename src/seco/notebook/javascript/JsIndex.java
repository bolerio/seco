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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.directory.SearchResult;
//import org.netbeans.modules.csl.api.ElementKind;
//import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
//import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileStateInvalidException;
//import org.openide.util.Exceptions;

import seco.notebook.csl.ElementKind;
import seco.notebook.csl.IndexResult;
import seco.notebook.csl.QuerySupport;

/**
 *
 * @author Tor Norbye
 */
public final class JsIndex {

    private static final Logger LOG = Logger.getLogger(JsIndex.class.getName());
    
    // Non-final for test suite
    static int MAX_SEARCH_ITEMS = 160;
    
    /** Set property to true to find ALL functions regardless of file includes */
    //private static final boolean ALL_REACHABLE = Boolean.getBoolean("javascript.findall");
    private static final boolean ALL_REACHABLE = !Boolean.getBoolean("javascript.checkincludes");

//    public static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
//    public static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);

    // XXX: make this accessible from IndexDocument
    private static final String FIELD_SOURCE_NAME = "_sn";  //NOI18N
    
    private static final String [] TERMS_FQN = new String [] { JsIndexer.FIELD_FQN, JsIndexer.FIELD_SDOC_URL };
    private static final String [] TERMS_BASE = new String [] { JsIndexer.FIELD_BASE, JsIndexer.FIELD_SDOC_URL };
    private static final String [] TERMS_EXTEND = new String [] { JsIndexer.FIELD_EXTEND };

    public static final JsIndex EMPTY = new JsIndex(null);
    
    private final QuerySupport querySupport;

    /** Creates a new instance of JsIndex */
    private JsIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

//    public static JsIndex get(Collection<FileObject> roots) {
//        try {
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.fine("JsIndex for roots: " + roots); //NOI18N
//            }
//            return new JsIndex(QuerySupport.forRoots(JsIndexer.Factory.NAME,
//                    JsIndexer.Factory.VERSION,
//                    roots.toArray(new FileObject[roots.size()])));
//        } catch (IOException ioe) {
//            LOG.log(Level.WARNING, null, ioe);
//            return EMPTY;
//        }
//    }

    private Collection<? extends IndexResult> query(
            final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad
    ) {
        if (querySupport != null) {
            try {
                return querySupport.query(fieldName, fieldValue, kind, fieldsToLoad);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }
        
        return Collections.<IndexResult>emptySet();
    }

    @SuppressWarnings("unchecked")
    public Pair<Set<IndexedElement>,Boolean> getConstructors(final String name, QuerySupport.Kind kind) {
        // TODO - search by the FIELD_CLASS thingy
        return getUnknownFunctions(name, kind, true, null, true, false);
    }
    
    @SuppressWarnings("unchecked")
    public Set<IndexedElement> getAllNames(final String name, QuerySupport.Kind kind, JsParseResult context) {
        // TODO - search by the FIELD_CLASS thingy
        return getUnknownFunctions(name, kind, false, context, true, true).getA();
    }

    @SuppressWarnings("unchecked")
    public Pair<Set<IndexedElement>,Boolean> getAllNamesTruncated(final String name, QuerySupport.Kind kind, JsParseResult context) {
        // TODO - search by the FIELD_CLASS thingy
        return getUnknownFunctions(name, kind, false, context, true, true);
    }
    
    public Map<String,String> getAllExtends() {
        Collection<? extends IndexResult> results = query(
                JsIndexer.FIELD_EXTEND, "", QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, TERMS_EXTEND); //NOI18N

        Map<String,String> classes = new HashMap<String,String>();
        for (IndexResult d : results) {
            String[] exts = d.getValues(JsIndexer.FIELD_EXTEND);
            
            if (exts != null) {
                for (String ext : exts) {
                    int clzBegin = ext.indexOf(';')+1;
                    int superBegin = ext.indexOf(';', clzBegin)+1;
                    
                    String clz = ext.substring(clzBegin, superBegin-1);
                    String superClz = ext.substring(superBegin);
                    classes.put(clz, superClz);
                }
            }
        }
        
        return classes;
    }
    
    public String getExtends(String className) {
        Collection<? extends IndexResult> results = query(
                JsIndexer.FIELD_EXTEND, className.toLowerCase(), QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, TERMS_EXTEND);

        String target = className.toLowerCase() + ";"; //NOI18N
        for (IndexResult d : results) {
            String[] exts = d.getValues(JsIndexer.FIELD_EXTEND);
            
            if (exts != null) {
                for (String ext : exts) {
                    if (ext.startsWith(target)) {
                        // Make sure it's a case match
                        int caseIndex = target.length();
                        int end = ext.indexOf(';', caseIndex); //NOI18N
                        if (className.equals(ext.substring(caseIndex, end))) {
                            return ext.substring(end+1);
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /** Return both functions and properties matching the given prefix, of the
     * given (possibly null) type
     */
    public Set<IndexedElement> getElements(String prefix, String type, QuerySupport.Kind kind, JsParseResult context) {
        return getByFqn(prefix, type, kind, false, context, true, true, false);
    }

    public Set<IndexedElement> getElementsByFqn(String fqn, QuerySupport.Kind kind, JsParseResult context) {
        String name = fqn;
        int dot = name.lastIndexOf('.'); //NOI18N
        String type = null;
        if (dot != -1) {
            type = name.substring(0, dot);
            name = name.substring(dot+1);
        }
        return getByFqn(name, type, kind, false, context, true, true, false);
    }
    
    public Set<IndexedElement> getAllElements(String prefix, String type, QuerySupport.Kind kind, JsParseResult context) {
        return getByFqn(prefix, type, kind, false, context, true, true, true);
    }
    
    @SuppressWarnings("unchecked")
    public Set<IndexedFunction> getFunctions(String name, String in, QuerySupport.Kind kind, JsParseResult context, boolean includeMethods) {
        return (Set<IndexedFunction>)(Set)getByFqn(name, in, kind, false, context, includeMethods, false, false);
    }
    
    private Pair<Set<IndexedElement>, Boolean> getUnknownFunctions(
        String name,
        QuerySupport.Kind kind,
        boolean onlyConstructors,
        JsParseResult context,
        boolean includeMethods, 
        boolean includeProperties
    ) {
        String field = JsIndexer.FIELD_BASE;
        String [] terms = TERMS_BASE;
        
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }
        
        String lcname = name.toLowerCase();
        Collection<? extends IndexResult> results = query(field, lcname, kind, terms);

        final Set<IndexedElement> elements = new HashSet<IndexedElement>();
        String searchUrl = null;
        if (context != null) {
          //  try {
                searchUrl = null; //TODO:?? context.getSnapshot().getSource().getFileObject().getURL().toExternalForm();
           // } catch (FileStateInvalidException ex) {
          //      Exceptions.printStackTrace(ex);
          //  }
        }

        for (IndexResult d : results) {
            String[] signatures = d.getValues(field);
            
            if (signatures != null) {
                // Check if this file even applies
                if (context != null) {
                    String fileUrl = d.getValue(FIELD_SOURCE_NAME);
                    if (searchUrl == null || !searchUrl.equals(fileUrl)) {
                        boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
                        if (!isLibrary && !isReachable(context, fileUrl)) {
                            continue;
                        }
                    }
                }
                
                for (String signature : signatures) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(lcname)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, lcname, 0, lcname.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                        int end = signature.indexOf(';');
                        assert end != -1;
                        String n = signature.substring(0, end);
                        try {
                            if (!n.matches(lcname)) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Silently ignore regexp failures in the search expression
                        }
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > lcname.length()) &&
                                (signature.charAt(lcname.length()) != ';'))) {
                            continue;
                        }
                    } // TODO - check camel case here too!

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!

                    String elementName = null;
                    int nameEndIdx = signature.indexOf(';'); //NOI18N
                    assert nameEndIdx != -1;
                    elementName = signature.substring(0, nameEndIdx);
                    nameEndIdx++;

                    String funcIn = null;
                    int inEndIdx = signature.indexOf(';', nameEndIdx); //NOI18N
                    assert inEndIdx != -1;
                    if (inEndIdx > nameEndIdx+1) {
                        funcIn = signature.substring(nameEndIdx, inEndIdx);
                    }
                    inEndIdx++;

                    int startCs = inEndIdx;
                    inEndIdx = signature.indexOf(';', startCs); //NOI18N
                    assert inEndIdx != -1;
                    if (inEndIdx > startCs) {
                        // Compute the case sensitive name
                        elementName = signature.substring(startCs, inEndIdx);
                        if (kind == QuerySupport.Kind.PREFIX && !elementName.startsWith(name)) {
                            continue;
                        } else if (kind == QuerySupport.Kind.EXACT && !elementName.equals(name)) {
                            continue;
                        }
                    }
                    inEndIdx++;
                    
                    // Filter out methods on other classes
                    if (!includeMethods && (funcIn != null)) {
                        continue;
                    }
                    
                    String fqn = null; // Compute lazily
                    IndexedElement element = IndexedElement.create(signature, fqn, elementName, funcIn, inEndIdx, this, d, false);
                    boolean isFunction = element instanceof IndexedFunction;
                    if (isFunction && !includeMethods) {
                        continue;
                    } else if (onlyConstructors) {
                        if (element.getKind() == ElementKind.PROPERTY && funcIn == null && Character.isUpperCase(elementName.charAt(0))) {
                            element.setKind(ElementKind.CONSTRUCTOR);
                        } else if (element.getKind() != ElementKind.CONSTRUCTOR) {
                            continue;
                        }
                    } else if (!isFunction && !includeProperties) {
                        continue;
                    }
                    elements.add(element);
                    
                    if (elements.size() == MAX_SEARCH_ITEMS) {
                        return new Pair<Set<IndexedElement>,Boolean>(elements, true);
                    }
                }
            }
        }
        
        return new Pair<Set<IndexedElement>,Boolean>(elements, false);
    }
    
    private Set<IndexedElement> getByFqn(
        String name,
        String type,
        QuerySupport.Kind kind,
        boolean onlyConstructors,
        JsParseResult context,
        boolean includeMethods,
        boolean includeProperties,
        boolean includeDuplicates
    ) {
        //assert in != null && in.length() > 0;
        
        String field = JsIndexer.FIELD_FQN;
        String [] terms = TERMS_FQN;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }
        
        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX || kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
            // TODO - can I do anything about this????
            //field = JsIndexer.FIELD_BASE_LOWER;
            //terms = FQN_BASE_LOWER;
        }

        final Set<IndexedElement> elements = //includeDuplicates ? new DuplicateElementSet() : 
             new HashSet<IndexedElement>();
        String searchUrl = null;
//        if (context != null) {
//            try {
//                if (context.getSnapshot().getSource().getFileObject() != null) {
//                    searchUrl = context.getSnapshot().getSource().getFileObject().getURL().toExternalForm();
//                }
//            } catch (FileStateInvalidException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

        Set<String> seenTypes = new HashSet<String>();
        seenTypes.add(type);
        boolean haveRedirected = false;
        boolean inheriting = type == null;
        
        while (true) {
        
            String fqn;
            if (type != null && type.length() > 0) {
                fqn = type + "." + name;
            } else {
                fqn = name;
            }

            String lcfqn = fqn.toLowerCase();
            Collection<? extends IndexResult> results = query(field, lcfqn, kind, terms);

            for (IndexResult d : results) {
                String[] signatures = d.getValues(field);

                if (signatures != null) {
                    // Check if this file even applies
                    if (context != null) {
                        String fileUrl = d.getValue(FIELD_SOURCE_NAME);
                        if (searchUrl == null || !searchUrl.equals(fileUrl)) {
                            boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
                            if (!isLibrary && !isReachable(context, fileUrl)) {
                                continue;
                            }
                        }
                    }

                    for (String signature : signatures) {
                        // Lucene returns some inexact matches, TODO investigate why this is necessary
                        if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(lcfqn)) {
                            continue;
                        } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, lcfqn, 0, lcfqn.length())) {
                            continue;
                        } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                            int end = signature.indexOf(';');
                            assert end != -1;
                            String n = signature.substring(0, end);
                            try {
                                if (!n.matches(lcfqn)) {
                                    continue;
                                }
                            } catch (Exception e) {
                                // Silently ignore regexp failures in the search expression
                            }
                        } else if (originalKind == QuerySupport.Kind.EXACT) {
                            // Make sure the name matches exactly
                            // We know that the prefix is correct from the first part of
                            // this if clause, by the signature may have more
                            if (((signature.length() > lcfqn.length()) &&
                                    (signature.charAt(lcfqn.length()) != ';'))) {
                                continue;
                            }
                        }

                        // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!

                        String elementName = null;
                        int nameEndIdx = signature.indexOf(';');
                        assert nameEndIdx != -1 : signature;
                        elementName = signature.substring(0, nameEndIdx);
                        nameEndIdx++;

                        String funcIn = null;
                        int inEndIdx = signature.indexOf(';', nameEndIdx);
                        assert inEndIdx != -1 : signature;
                        inEndIdx++;

                        int startCs = inEndIdx;
                        inEndIdx = signature.indexOf(';', startCs);
                        assert inEndIdx != -1;
                        if (inEndIdx > startCs) {
                            // Compute the case sensitive name
                            elementName = signature.substring(startCs, inEndIdx);
                            if (kind == QuerySupport.Kind.PREFIX && !elementName.startsWith(fqn)) {
                                continue;
                            } else if (kind == QuerySupport.Kind.EXACT && !elementName.equals(fqn)) {
                                continue;
                            }
                        }
                        inEndIdx++;

                        int lastDot = elementName.lastIndexOf('.');
                        IndexedElement element = null;
                        if (name.length() < lastDot) {
                            int nextDot = elementName.indexOf('.', fqn.length());
                            if (nextDot != -1) {
                                int flags = IndexedElement.decode(signature, inEndIdx, 0);
                                ElementKind k = ElementKind.PACKAGE;
                                // If there are no more dots after this one, it's a class, not a package
                                int nextNextDot = elementName.indexOf('.', nextDot+1);
                                if (nextNextDot == -1) {
                                    k = ElementKind.CLASS;
                                }
                                if (type != null && type.length() > 0) {
                                    String pkg = elementName.substring(type.length()+1, nextDot);
                                    element = new IndexedPackage(null, pkg, null, this, d, signature, flags, k);
                                } else {
                                    String pkg = elementName.substring(0, nextDot);
                                    element = new IndexedPackage(null, pkg, null, this, d, signature, flags, k);
                                }
                            } else {
                                funcIn = elementName.substring(0, lastDot);
                                elementName = elementName.substring(lastDot+1);
                            }
                        } else if (lastDot != -1) {
                            funcIn = elementName.substring(0, lastDot);
                            elementName = elementName.substring(lastDot+1);
                        }
                        if (element == null) {
                            element = IndexedElement.create(signature, null, elementName, funcIn, inEndIdx, this, d, false);
                        }
                        boolean isFunction = element instanceof IndexedFunction;
                        if (isFunction && !includeMethods) {
                            continue;
                        } else if (!isFunction && !includeProperties) {
                            continue;
                        }
                        if (onlyConstructors && element.getKind() != ElementKind.CONSTRUCTOR) {
                            continue;
                        }
                        if (!haveRedirected) {
                            element.setSmart(true);
                        }
                        if (!inheriting) {
                            element.setInherited(false);
                        }
                        elements.add(element);
                    }
                }
            }
            
            if (type == null || "Object".equals(type)) { // NOI18N
                break;
            }
            type = getExtends(type);
            if (type == null) {
                type = "Object"; // NOI18N
                haveRedirected = true;
            }
            // Prevent circularity in types
            if (seenTypes.contains(type)) {
                break;
            } else {
                seenTypes.add(type);
            }
            inheriting = true;
        }
        
        return elements;
    }
    
    public String getType(String fqn) {
        int baseIndex = fqn.lastIndexOf('.');
        if (baseIndex == -1) {
            return getSimpleType(fqn);
        }
        String clz = fqn.substring(0, baseIndex);
        List<String> ancestors = ClassCache.INSTANCE.getAncestors(clz, this);
        if (ancestors.size() <= 1) {
            return getSimpleType(fqn);
        }
        
        String base = fqn.substring(baseIndex+1);
        int baseLength = base.length();
        
        // Look for inheritance too, e.g. if you're searching for HTMLDocument.createElement
        // and no such entry is found it will look at Document.createElement and return it provided
        // Document looks related to HTMLDocument through inheritance
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = JsIndexer.FIELD_BASE;
        String [] terms = TERMS_BASE;
        String lcsymbol = base.toLowerCase();
        assert lcsymbol.length() == baseLength;
        Collection<? extends IndexResult> results = query(field, lcsymbol, QuerySupport.Kind.PREFIX, terms);

        for (IndexResult d : results) {
            String[] signatures = d.getValues(field);
            
            if (signatures != null) {
                for (String signature : signatures) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    // Make sure the name matches exactly
                    // We know that the prefix is correct from the first part of
                    // this if clause, by the signature may have more
                    if (!signature.startsWith(lcsymbol) || signature.charAt(baseLength) != ';') { //NOI18N
                        continue;
                    }

                    // Make sure the containing document is one of the superclasses
                    assert signature.charAt(baseLength) == ';';//NOI18N
                    int inBegin = baseLength+1;
                    int inEnd = signature.indexOf(';', inBegin); //NOI18N
                    if (inEnd == inBegin) {
                        // No in - only qualifies if the target has no in
                        // However, we're currently processing those separately so
                        // this is not a match
                        continue;
                    }
                    String in = signature.substring(inBegin, inEnd);
                    for (String ancestor : ancestors) {
                        if (ancestor.equals(in)) {
                            // This is a good one
                            
                            String type = getTypeInSignature(signature);
                            if (type != null) {
                                return type;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
        
    }
    
    private static String getTypeInSignature(String signature) {
        // Look for the type
        int typeIndex = 0;
        int section = IndexedElement.TYPE_INDEX;
        for (int i = 0; i < section; i++) {
            typeIndex = signature.indexOf(';', typeIndex+1); //NOI18N
        }
        typeIndex++;
        int endIndex = signature.indexOf(';', typeIndex); //NOI18N
        if (endIndex > typeIndex) {
            return signature.substring(typeIndex, endIndex);
        }
        
        return null;
    }
    
    /** 
     * Try to find the type of a symbol and return it. This method does not look for overridden
     * methods etc, it matches by exact signature.
     */
    private String getSimpleType(String fqn) {
        String field = JsIndexer.FIELD_FQN;
        String [] terms = TERMS_FQN;
        String lcsymbol = fqn.toLowerCase();
        int symbolLength = fqn.length();
        Collection<? extends IndexResult> results = query(field, lcsymbol, QuerySupport.Kind.PREFIX, terms);

        for (IndexResult d : results) {
            String[] signatures = d.getValues(field);
            
            if (signatures != null) {
                for (String signature : signatures) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    // Make sure the name matches exactly
                    // We know that the prefix is correct from the first part of
                    // this if clause, by the signature may have more
                    if (!signature.startsWith(lcsymbol) || signature.charAt(symbolLength) != ';') {
                        continue;
                    }

                    String type = getTypeInSignature(signature);
                    if (type != null) {
                        return type;
                    }
                }
            }
        }
        
        return null;
    }

    /** 
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     * 
     * @todo Add some smarts here to correlate remote URLs (http:// pointers to dojo etc)
     *   with local copies of these.
     * @todo Do some kind of transitive check? Probably not - there isn't a way to do
     *    includes of files that contain other files (you can import a .js file, but that
     *    js file can't include other files)
     */
    public boolean isReachable(JsParseResult result, String url) {
        if (ALL_REACHABLE) {
            return true;
        }
        List<String> imports = result.getStructure().getImports();
        if (imports.size() > 0) {
            // TODO - do some heuristics to deal with relative paths here,
            // e.g.   <script src="../../foo.js"></script>

            for (int i = 0, n = imports.size(); i < n; i++) {
                String imp = imports.get(i);
                if (imp.indexOf("../") != -1) { //NOI18N
                    int lastIndex = imp.lastIndexOf("../"); //NOI18N
                    imp = imp.substring(lastIndex+3);
                    if (imp.length() == 0) {
                        continue;
                    }
                }
                if (url.endsWith(imp)) {
                    return true;
                }
            }
        }

        return false;
    }
}
