package seco.langs.python;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Comparator;
import java.util.List;
import javax.swing.text.Document;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.SourceGroup;
//import org.netbeans.api.project.Sources;
//import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
//import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
//import org.netbeans.modules.gsf.api.ParserFile;
//import org.netbeans.modules.python.api.PythonPlatform;
//import org.netbeans.modules.python.api.PythonPlatformManager;
//import org.netbeans.modules.python.editor.lexer.PythonTokenId;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileStateInvalidException;
//import org.openide.filesystems.FileUtil;
//import org.openide.util.Exceptions;
//import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Name;

/**
 *
 * @author Tor Norbye
 */
public class PythonUtils {
   

    public static final String DOT__INIT__ = ".__init__"; // NOI18N

    // From PythonProjectType
    public static final String SOURCES_TYPE_PYTHON = "python"; // NOI18N

     private static String prevRootUrl;

   
    // Keywords - according to http://docs.python.org/ref/keywords.html
    static final String[] PYTHON_KEYWORDS = new String[]{
        "and", // NOI18N
        "as", // NOI18N
        "assert", // NOI18N
        "break", // NOI18N
        "class", // NOI18N
        "continue", // NOI18N
        "def", // NOI18N
        "del", // NOI18N
        "elif", // NOI18N
        "else", // NOI18N
        "except", // NOI18N
        "exec", // NOI18N
        "finally", // NOI18N
        "for", // NOI18N
        "from", // NOI18N
        "global", // NOI18N
        "if", // NOI18N
        "import", // NOI18N
        "in", // NOI18N
        "is", // NOI18N
        "lambda", // NOI18N
        "not", // NOI18N
        "or", // NOI18N
        "pass", // NOI18N
        "print", // NOI18N
        "raise", // NOI18N
        "return", // NOI18N
        "try", // NOI18N
        "while", // NOI18N
        "with", // NOI18N
        "yield", // NOI18N
    };

    public static boolean isPythonKeyword(String name) {
        for (String s : PYTHON_KEYWORDS) {
            if (s.equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return true iff the name is a class name
     * @param name The name
     * @param emptyDefault Whether empty or _ names should be considered a class name or not
     * @return True iff the name looks like a class name
     */
    public static boolean isClassName(String name, boolean emptyDefault) {
        if (name == null || name.length() == 0) {
            return emptyDefault;
        }
        if (name.startsWith("_") && name.length() > 1) {
            return Character.isUpperCase(name.charAt(1));
        }

        return Character.isUpperCase(name.charAt(0));
    }

    /**
     * Return true iff the name is a method name
     * @param name The name
     * @param emptyDefault Whether empty or _ names should be considered a class name or not
     * @return True iff the name looks like a method name
     */
    public static boolean isMethodName(String name, boolean emptyDefault) {
        if (name == null || name.length() == 0) {
            return emptyDefault;
        }
        if (name.startsWith("__") && name.length() > 2) {
            return Character.isLowerCase(name.charAt(2));
        }
        if (name.startsWith("_") && name.length() > 1) {
            return Character.isLowerCase(name.charAt(1));
        }

        return Character.isLowerCase(name.charAt(0));
    }

    public static boolean isValidPythonClassName(String name) {
        if (isPythonKeyword(name)) {
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
            if (!Character.isJavaIdentifierPart(c)) {
                return false;
            }

        }

        return true;
    }

    /** Is this name a valid operator name? */
    public static boolean isOperator(String name) {
        // TODO - update to Python
        if (name.length() == 0) {
            return false;
        }

        switch (name.charAt(0)) {
        case '+':
            return name.equals("+") || name.equals("+@");
        case '-':
            return name.equals("-") || name.equals("-@");
        case '*':
            return name.equals("*") || name.equals("**");
        case '<':
            return name.equals("<") || name.equals("<<") || name.equals("<=") || name.equals("<=>");
        case '>':
            return name.equals(">") || name.equals(">>") || name.equals(">=");
        case '=':
            return name.equals("=") || name.equals("==") || name.equals("===") || name.equals("=~");
        case '!':
            return name.equals("!=") || name.equals("!~");
        case '&':
            return name.equals("&") || name.equals("&&");
        case '|':
            return name.equals("|") || name.equals("||");
        case '[':
            return name.equals("[]") || name.equals("[]=");
        case '%':
            return name.equals("%");
        case '/':
            return name.equals("/");
        case '~':
            return name.equals("~");
        case '^':
            return name.equals("^");
        case '`':
            return name.equals("`");
        default:
            return false;
        }
    }

    public static boolean isValidPythonMethodName(String name) {
        if (isPythonKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        // TODO - allow operators
        if (isOperator(name)) {
            return true;
        }

        if (Character.isUpperCase(name.charAt(0)) || Character.isWhitespace(name.charAt(0))) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }

        }

        return true;
    }

    public static boolean isValidPythonIdentifier(String name) {
        if (isPythonKeyword(name)) {
            return false;
        }

        if (name.trim().length() == 0) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            // Identifier char isn't really accurate - I can have a function named "[]" etc.
            // so just look for -obvious- mistakes
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }

        // TODO - make this more accurate, like the method validifier
        }

        return true;
    }

    /**
     * Ruby identifiers should consist of [a-zA-Z0-9_]
     * http://www.headius.com/rubyspec/index.php/Variables
     * <p>
     * This method also accepts the field/global chars
     * since it's unlikely
     */
    public static boolean isSafeIdentifierName(String name, int fromIndex) {
        int i = fromIndex;
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c == '$' || c == '@' || c == ':')) {
                break;
            }
        }
        for (; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c == '_') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    (c == '?') || (c == '=') || (c == '!'))) { // Method suffixes; only allowed on the last line

                if (isOperator(name)) {
                    return true;
                }

                return false;
            }
        }

        return true;
    }

//    /**
//     * Return null if the given identifier name is valid, otherwise a localized
//     * error message explaining the problem.
//     */
//    public static String getIdentifierWarning(String name, int fromIndex) {
//        if (isSafeIdentifierName(name, fromIndex)) {
//            return null;
//        } else {
//            return NbBundle.getMessage(PythonUtils.class, "UnsafeIdentifierName");
//        }
//    }

    /** @todo Move into GsfUtilities after 6.5 */
    public static int getOffsetByLineCol(String source, int line, int col) {
        int offset = 0;
        for (int i = 0; i < line; i++) {
            offset = source.indexOf('\n', offset);
            if (offset == -1) {
                offset = source.length();
                break;
            }
            offset++;
        }
        if (col > 0) { // -1: invalid
            offset += col;
        }

        return offset;
    }
    public static Comparator NAME_NODE_COMPARATOR = new Comparator<Name>() {
        public int compare(Name n1, Name n2) {
            return n1.getInternalId().compareTo(n2.getInternalId());
        }
    };
    public static Comparator ATTRIBUTE_NAME_NODE_COMPARATOR = new Comparator<Object>() {
        @SuppressWarnings("unchecked")
        public int compare(Object n1, Object n2) {
            String s1 = "";
            String s2 = "";

            if (n1 instanceof Name) {
                s1 = ((Name)n1).getInternalId();
            } else if (n1 instanceof Attribute) {
                Attribute a = (Attribute)n1;
                String v = PythonAstUtils.getName(a.getInternalValue());
                if (v != null) {
                    s1 = a.getInternalAttr() + "." + v;
                } else {
                    s1 = a.getInternalAttr();
                }
            }

            if (n2 instanceof Name) {
                s2 = ((Name)n2).getInternalId();
            } else if (n2 instanceof Attribute) {
                Attribute a = (Attribute)n2;
                String v = PythonAstUtils.getName(a.getInternalValue());
                if (v != null) {
                    s2 = a.getInternalAttr() + "." + v;
                } else {
                    s2 = a.getInternalAttr();
                }
            }

            return s1.compareTo(s2);
        }
    };
    public static Comparator NODE_POS_COMPARATOR = new Comparator<PythonTree>() {
        public int compare(PythonTree p1, PythonTree p2) {
            int ret = p1.getCharStartIndex() - p2.getCharStartIndex();
            if (ret != 0) {
                return ret;
            }
            ret = p2.getCharStopIndex() - p1.getCharStopIndex();
            if (ret != 0) {
                return ret;
            }
            return p2.getAntlrType() - p1.getAntlrType();
        }
    };
}

