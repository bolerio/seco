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

package seco.notebook.csl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.netbeans.api.java.classpath.ClassPath;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.modules.parsing.api.indexing.IndexingManager;
//import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
//import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
//import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
//import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
//import org.netbeans.modules.parsing.impl.indexing.PathRecognizerRegistry;
//import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
//import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
//import org.netbeans.modules.parsing.impl.indexing.Util;
//import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileStateInvalidException;
//import org.openide.filesystems.URLMapper;
//import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class QuerySupport {

    /**
     * Gets classpath roots relevant for a file. This method tries to find
     * classpath roots for a given files. It looks at classpaths specified by
     * <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryLibraryPathIds</code> parameters.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param f The file to find roots for.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     *
     * @return The collection of roots for a given file. It may be empty, but never <code>null</code>.
     * 
     * @since 1.6
     */
//    public static Collection<FileObject> findRoots(
//            FileObject f,
//            Collection<String> sourcePathIds,
//            Collection<String> libraryPathIds,
//            Collection<String> binaryLibraryPathIds)
//    {
//        Collection<FileObject> roots = new HashSet<FileObject>();
//
//        if (sourcePathIds == null) {
//            sourcePathIds = PathRecognizerRegistry.getDefault().getSourceIds();
//        }
//
//        if (libraryPathIds == null) {
//            libraryPathIds = PathRecognizerRegistry.getDefault().getLibraryIds();
//        }
//
//        if (binaryLibraryPathIds == null) {
//            binaryLibraryPathIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
//        }
//
//        collectClasspathRoots(f, sourcePathIds, false, roots);
//        collectClasspathRoots(f, libraryPathIds, false, roots);
//        collectClasspathRoots(f, binaryLibraryPathIds, true, roots);
//
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.fine("Roots for file " + f //NOI18N
//                    + ", sourcePathIds=" + sourcePathIds //NOI18N
//                    + ", libraryPathIds=" + libraryPathIds //NOI18N
//                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
//                    + ": "); //NOI18N
//            for(FileObject root : roots) {
//                try {
//                    LOG.fine("  " + root.getURL()); //NOI18N
//                } catch (FileStateInvalidException ex) {
//                    //ignore
//                }
//            }
//            LOG.fine("----"); //NOI18N
//        }
//
//        return roots != null ? roots : Collections.<FileObject>emptySet();
//    }

    /**
     * Gets classpath roots relevant for a project. This method tries to find
     * classpaths with <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryPathIds</code> supplied by the <code>project</code>.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param project The project to find the roots for. Can be <code>null</code> in
     *   which case the method searches in all registered classpaths.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     *
     * @return The collection of roots for a given project. It may be empty, but never <code>null</code>.
     * 
     * @since 1.6
     */
//    public static Collection<FileObject> findRoots(
//            Project project,
//            Collection<String> sourcePathIds,
//            Collection<String> libraryPathIds,
//            Collection<String> binaryLibraryPathIds)
//    {
//        Set<FileObject> roots = new HashSet<FileObject>();
//
//        if (sourcePathIds == null) {
//            sourcePathIds = PathRecognizerRegistry.getDefault().getSourceIds();
//        }
//
//        if (libraryPathIds == null) {
//            libraryPathIds = PathRecognizerRegistry.getDefault().getLibraryIds();
//        }
//
//        if (binaryLibraryPathIds == null) {
//            binaryLibraryPathIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
//        }
//
//        collectClasspathRoots(null, sourcePathIds, false, roots);
//        collectClasspathRoots(null, libraryPathIds, false, roots);
//        collectClasspathRoots(null, binaryLibraryPathIds, true, roots);
//
//        if (project != null) {
//            Set<FileObject> rootsInProject = new HashSet<FileObject>();
//            for(FileObject root : roots) {
//                if (FileOwnerQuery.getOwner(root) == project) {
//                    rootsInProject.add(root);
//                }
//            }
//            roots = rootsInProject;
//        }
//
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.fine("Roots for project " + project //NOI18N
//                    + ", sourcePathIds=" + sourcePathIds //NOI18N
//                    + ", libraryPathIds=" + libraryPathIds //NOI18N
//                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
//                    + ": "); //NOI18N
//            for(FileObject root : roots) {
//                try {
//                    LOG.fine("  " + root.getURL()); //NOI18N
//                } catch (FileStateInvalidException ex) {
//                    //ignore
//                }
//            }
//            LOG.fine("----"); //NOI18N
//        }
//
//        return roots;
//    }

//    public static QuerySupport forRoots (final String indexerName, final int indexerVersion, final URL... roots) throws IOException {
//        Parameters.notNull("indexerName", indexerName); //NOI18N
//        Parameters.notNull("roots", roots); //NOI18N
//        return new QuerySupport(indexerName, indexerVersion, roots);
//    }
//
//    public static QuerySupport forRoots (final String indexerName, final int indexerVersion, final FileObject... roots) throws IOException {
//        Parameters.notNull("indexerName", indexerName); //NOI18N
//        Parameters.notNull("roots", roots); //NOI18N
//        final List<URL> rootsURL = new ArrayList<URL>(roots.length);
//        for (FileObject root : roots) {
//            rootsURL.add(root.getURL());
//        }
//        return new QuerySupport(indexerName, indexerVersion, rootsURL.toArray(new URL[rootsURL.size()]));
//    }

    public Collection<? extends IndexResult> query(
            final String fieldName,
            final String fieldValue,
            final Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        // check if there are stale indices
//        for (Map.Entry<URL, IndexImpl> ie : indexes.entrySet()) {
//            final IndexImpl index = ie.getValue();
//            final Collection<? extends String> staleFiles = index.getStaleFiles();
//
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.fine("Index: " + index + ", staleFiles: " + staleFiles); //NOI18N
//            }
//
//            if (staleFiles != null && staleFiles.size() > 0) {
//                final URL root = ie.getKey();
//                LinkedList<URL> list = new LinkedList<URL>();
//                for(String staleFile : staleFiles) {
//                    try {
//                        list.add(Util.resolveUrl(root, staleFile));
//                    } catch (MalformedURLException ex) {
//                        LOG.log(Level.WARNING, null, ex);
//                    }
//                }
//
//                IndexingManager.getDefault().refreshIndexAndWait(root, list);
//            }
//        }
//
        final List<IndexResult> result = new LinkedList<IndexResult>();
//        for (Map.Entry<URL,IndexImpl> ie : indexes.entrySet()) {
//            final IndexImpl index = ie.getValue();
//            final URL root = ie.getKey();
//            final Collection<? extends IndexDocumentImpl> pr = index.query(fieldName, fieldValue, kind, fieldsToLoad);
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.fine("query(\"" + fieldName + "\", \"" + fieldValue + "\", " + kind + ", "
//                        + printFiledToLoad(fieldsToLoad) + ") for " + indexerIdentification + ":"); //NOI18N
//                for(IndexDocumentImpl idi : pr) {
//                    LOG.fine(" " + idi); //NOI18N
//                }
//                LOG.fine("----"); //NOI18N
//            }
//            for (IndexDocumentImpl di : pr) {
//                result.add(new IndexResult(di,root));
//            }
//        }
        return result;
    }

    /**
     * Encodes a type of the name kind used by {@link QuerySupport#query}.
     *
     */
    public enum Kind {
        /**
         * The name parameter
         * is an exact simple name of the package or declared type.
         */
        EXACT,
        /**
         * The name parameter
         * is an case sensitive prefix of the package or declared type name.
         */
        PREFIX,
        /**
         * The name parameter is
         * an case insensitive prefix of the declared type name.
         */
        CASE_INSENSITIVE_PREFIX,
        /**
         * The name parameter is
         * an camel case of the declared type name.
         */
        CAMEL_CASE,
        /**
         * The name parameter is
         * an regular expression of the declared type name.
         */
        REGEXP,
        /**
         * The name parameter is
         * an case insensitive regular expression of the declared type name.
         */
        CASE_INSENSITIVE_REGEXP,

        CASE_INSENSITIVE_CAMEL_CASE;
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(QuerySupport.class.getName());
    
    private final String indexerIdentification;
    //private final IndexFactoryImpl spiFactory;
   // private final Map<URL,IndexImpl> indexes;

    private QuerySupport (final String indexerName, int indexerVersion, final URL... roots) throws IOException {
        this.indexerIdentification = indexerName + "/" + indexerVersion; //NOI18N
//        this.spiFactory = new LuceneIndexFactory();
//        this.indexes = new HashMap<URL, IndexImpl>();
//        final String indexerFolder = findIndexerFolder(indexerName, indexerVersion);
//        if (indexerFolder != null) {
//            for (URL root : roots) {
//                final FileObject cacheFolder = CacheFolder.getDataFolder(root);
//                assert cacheFolder != null;
//                final FileObject indexFolder = cacheFolder.getFileObject(indexerFolder);
//                if (indexFolder != null) {
//                    final IndexImpl index = this.spiFactory.getIndex(indexFolder);
//                    if (index != null) {
//                        this.indexes.put(root,index);
//                    }
//                }
//            }
//        }
//
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.fine("QuerySupport for " + indexerIdentification + ":"); //NOI18N
//            for(URL root : indexes.keySet()) {
//                LOG.fine(" " + root + " -> index: " + indexes.get(root)); //NOI18N
//            }
//            LOG.fine("----"); //NOI18N
//        }
    }

//    /**
//     * Unit test constructor
//     */
//    private QuerySupport (final FileObject srcRoot, final String indexerName, final int indexerVersion) throws IOException {
//        this.indexerIdentification = indexerName + "/" + indexerVersion; //NOI18N
//        this.spiFactory = new LuceneIndexFactory();
//        this.indexes = new HashMap<URL, IndexImpl>();
//        final FileObject cacheFolder = CacheFolder.getDataFolder(srcRoot.getURL());
//        FileObject fo = cacheFolder.getFileObject(SPIAccessor.getInstance().getIndexerPath(indexerName, indexerVersion));
//        fo.getClass();
//        this.indexes.put (srcRoot.getURL(),this.spiFactory.getIndex(fo));
//    }
//
//    private static void collectClasspathRoots(FileObject file, Collection<String> pathIds, boolean binaryPaths, Collection<FileObject> roots) {
//        for(String id : pathIds) {
//            Collection<FileObject> classpathRoots = getClasspathRoots(file, id);
//            if (binaryPaths) {
//                // Filter out roots that do not have source files available
//                for(FileObject binRoot : classpathRoots) {
//                    URL binRootUrl;
//                    try {
//                        binRootUrl = binRoot.getURL();
//                    } catch (FileStateInvalidException fsie) {
//                        continue;
//                    }
//
//                    URL[] srcRoots = PathRegistry.getDefault().sourceForBinaryQuery(binRootUrl, null, false);
//                    if (srcRoots != null) {
//                        LOG.log(Level.FINE, "Translating {0} -> {1}", new Object [] { binRootUrl, srcRoots }); //NOI18N
//                        for(URL srcRootUrl : srcRoots) {
//                            FileObject srcRoot = URLMapper.findFileObject(srcRootUrl);
//                            if (srcRoot != null) {
//                                roots.add(srcRoot);
//                            }
//                        }
//                    } else {
//                        LOG.log(Level.FINE, "No sources for {0}, adding bin root", binRootUrl); //NOI18N
//                        roots.add(binRoot);
//                    }
//                }
//            } else {
//                roots.addAll(classpathRoots);
//            }
//        }
//    }
//
//    private static Collection<FileObject> getClasspathRoots(FileObject file, String classpathId) {
//        Collection<FileObject> roots = Collections.<FileObject>emptySet();
//
//        if (file != null) {
//            ClassPath classpath = ClassPath.getClassPath(file, classpathId);
//            if (classpath != null) {
//                roots = Arrays.asList(classpath.getRoots());
//            }
//        } else {
//            roots = new HashSet<FileObject>();
//            Set<URL> urls = PathRegistry.getDefault().getRootsMarkedAs(classpathId);
//            for(URL url : urls) {
//                FileObject f = URLMapper.findFileObject(url);
//                if (f != null) {
//                    roots.add(f);
//                }
//            }
//        }
//
//        return roots;
//    }
//
//    private static String findIndexerFolder (final String indexerName, final int indexerVersion) {
//        return SPIAccessor.getInstance().getIndexerPath(indexerName, indexerVersion);
//    }

    private static String printFiledToLoad(String... fieldsToLoad) {
        if (fieldsToLoad == null || fieldsToLoad.length == 0) {
            return "<all-fields>"; //NOI18N
        } else {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < fieldsToLoad.length; i++) {
                sb.append("\"").append(fieldsToLoad[i]).append("\""); //NOI18N
                if (i + 1 < fieldsToLoad.length) {
                    sb.append(", "); //NOI18N
                }
            }
            return sb.toString();
        }
    }
}
