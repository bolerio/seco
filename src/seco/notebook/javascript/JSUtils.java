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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package seco.notebook.javascript;

import javax.swing.text.Document;
//import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
//import org.openide.filesystems.FileObject;
//import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class JsUtils {

    private JsUtils() {
    }

    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    
    public static boolean isSafeIdentifierName(String name, int fromIndex) {
        int i = fromIndex;

        if (i >= name.length()) {
            if (i == 0) {
                return false;
            }
            return true;
        }

        if (i == 0) {
            if (isJsKeyword(name)) {
                return false;
            }

            // Digits not allwed in the first position
            if (Character.isDigit(name.charAt(0))) {
                return false;
            }
        }

        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\\') {
                // Unicode escape sequences are okay
                if (i == name.length()-1 || name.charAt(i+1) != 'u') {
                    return false;
                }
            } else if (!(c == '$' || c == '_' || Character.isLetterOrDigit(c))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidJsClassName(String name) {
        if (isJsKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!isStrictIdentifierChar(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isJsKeyword(String name) {
        for (String s : JAVASCRIPT_KEYWORDS) {
            if (s.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    /** Includes things you'd want selected as a unit when double clicking in the editor */
    public static boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (// Globals, fields and parameter prefixes (for blocks and symbols)
                c == '$' || c == '\\'); // \\u is valid
    }

    /** Includes things you'd want selected as a unit when double clicking in the editor */
    public static boolean isStrictIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) ||
                (c == '$' || c == '\\');
    }

    /** The following keywords apply inside a call expression */
    public static final String[] CALL_KEYWORDS =
            new String[] {
        "true", // NOI18N
        "false", // NOI18N
        "null" // NOI18N
    };
    
    // Section 7.5.2 in ECMAScript Language Specification, ECMA-262
    public static final String[] JAVASCRIPT_KEYWORDS =
            new String[]{
        // Uhm... what about "true" and "false" ? And "nil" ?
        "break",
        "case",
        "catch",
        "continue",
        "default",
        "delete",
        "do",
        "else",

        // Not included in the ECMAScript list of keywords - really a datatype
        "false", // NOI18N
        
        "finally",
        "for",
        "function",
        "if",
        "in",
        "instanceof",
        "let", // New in 1.7 -- do language-specific checks here?
        "new",

        // Not included in the ECMAScript list of keywords - really a datatype
        "null", // NOI18N
        
        "return",
        "switch",
        "this",
        "throw",
        
        // Not included in the ECMAScript list of keywords - really a datatype
        "true", // NOI18N
        
        "try",
        "typeof",

        // Not included in the ECMAScript list of keywords - really a datatype
        "undefined", // NOI18N
        
        "var",
        "void",
        "while",
        "with",
        "yield" // New in 1.7 -- do language-specific checks here?
    };

    // Section 7.5.3 in ECMAScript Language Specification, ECMA-262
    public static final String[] JAVASCRIPT_RESERVED_WORDS =
            new String[]{
        "abstract",
        "boolean",
        "byte",
        "char",
        "class",
        "const",
        "debugger",
        "double",
        "enum",
        "export",
        "extends",
        "final",
        "float",
        "goto",
        "implements",
        "import",
        "int",
        "interface",
        "long",
        "native",
        "package",
        "private",
        "protected",
        "public",
        "short",
        "static",
        "super",
        "synchronized",
        "throws",
        "transient",
        "volatile",
    };

    /**
     * Convert the display string used for types internally to something
     * suitable. For example, Array<String> is shown as String[].
     */
    public static String normalizeTypeString(String s) {
       if (s.indexOf("Array<") != -1) { // NOI18N
           String[] types = s.split("\\|"); // NOI18N
           StringBuilder sb = new StringBuilder();
           for (String t : types) {
               if (sb.length() > 0) {
                   sb.append("|"); // NOI18N
               }
               if (t.startsWith("Array<") && t.endsWith(">")) { // NOI18N
                   sb.append(t.substring(6, t.length()-1));
                   sb.append("[]"); // NOI18N
               } else {
                   sb.append(t);
               }
           }
           
           return sb.toString();
       } 
       
       return s;
    }
}
