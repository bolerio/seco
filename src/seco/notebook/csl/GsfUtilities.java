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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventObject;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
//import org.netbeans.api.editor.EditorRegistry;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.editor.Utilities;
//import org.netbeans.lib.editor.util.swing.DocumentUtilities;
//import org.netbeans.modules.csl.api.DataLoadersBridge;
//import org.netbeans.modules.editor.NbEditorUtilities;
//import org.netbeans.modules.editor.indent.api.IndentUtils;
//import org.netbeans.modules.parsing.api.Source;
//import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
//import org.openide.ErrorManager;
//import org.openide.cookies.EditorCookie;
//import org.openide.cookies.LineCookie;
//import org.openide.cookies.OpenCookie;
//import org.openide.filesystems.FileLock;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileSystem;
//import org.openide.filesystems.FileUtil;
//import org.openide.loaders.DataObject;
//import org.openide.text.Line;
//import org.openide.text.NbDocument;
//import org.openide.util.UserQuestionException;

/**
 * Misc utilities to avoid code duplication among the various language plugins
 *
 * @author Tor Norbye
 */
public final class GsfUtilities {
    private static final Logger LOG = Logger.getLogger(GsfUtilities.class.getName());

    private GsfUtilities() { // Utility class only, no instances
    }


//    public static void extractZip(final FileObject extract, final FileObject dest) throws IOException {
//        File extractFile = FileUtil.toFile(extract);
//        extractZip(dest, new BufferedInputStream(new FileInputStream(extractFile)));
//    }
//
//    // Based on openide/fs' FileUtil.extractJar
//    private static void extractZip(final FileObject fo, final InputStream is)
//    throws IOException {
//        FileSystem fs = fo.getFileSystem();
//
//        fs.runAtomicAction(
//            new FileSystem.AtomicAction() {
//                public void run() throws IOException {
//                    extractZipImpl(fo, is);
//                }
//            }
//        );
//    }
//
//    /** Does the actual extraction of the Jar file.
//     */
//    // Based on openide/fs' FileUtil.extractJarImpl
//    private static void extractZipImpl(FileObject fo, InputStream is)
//    throws IOException {
//        ZipEntry je;
//
//        ZipInputStream jis = new ZipInputStream(is);
//
//        while ((je = jis.getNextEntry()) != null) {
//            String name = je.getName();
//
//            if (name.toLowerCase().startsWith("meta-inf/")) {
//                continue; // NOI18N
//            }
//
//            if (je.isDirectory()) {
//                FileUtil.createFolder(fo, name);
//
//                continue;
//            }
//
//            // copy the file
//            FileObject fd = FileUtil.createData(fo, name);
//            FileLock lock = fd.lock();
//
//            try {
//                OutputStream os = fd.getOutputStream(lock);
//
//                try {
//                    FileUtil.copy(jis, os);
//                } finally {
//                    os.close();
//                }
//            } finally {
//                lock.releaseLock();
//            }
//        }
//    }

    /** Return true iff we're editing code templates */
    public static boolean isCodeTemplateEditing(Document doc) {
        // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
        String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
        String CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

        return doc.getProperty(EDITING_TEMPLATE_DOC_PROPERTY) == Boolean.TRUE ||
                doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
    }

    public static boolean isRowWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean isRowEmpty(CharSequence text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length()-1 || text.charAt(offset+1) == '\n')))) {
                    return false;
                }
            }

            if (!(offset == 0 || text.charAt(offset-1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowLastNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length()-1 || text.charAt(i+1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowFirstNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset-1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowStart(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i+1;
                }
            }

            return 0;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowEnd(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i;
                }
            }

            return text.length();
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean endsWith(StringBuilder sb, String s) {
        int len = s.length();

        if (sb.length() < len) {
            return false;
        }

        for (int i = sb.length()-len, j = 0; j < len; i++, j++) {
            if (sb.charAt(i) != s.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    public static String truncate(String s, int length) {
        assert length > 3; // Not for short strings
        if (s.length() <= length) {
            return s;
        } else {
            return s.substring(0, length-3) + "...";
        }
    }
}
