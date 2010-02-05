package seco.langs.javascript;

import java.util.Iterator;

import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.Node.Jump;

public class JSUtils
{
       
    static final String propToString(int propType)
    {
        if (Token.printTrees) {
            // If Context.printTrees is false, the compiler
            // can remove all these strings.
            switch (propType) {
                case Node.FUNCTION_PROP:      return "function";
                case Node.LOCAL_PROP:         return "local";
                case Node.LOCAL_BLOCK_PROP:   return "local_block";
                case Node.REGEXP_PROP:        return "regexp";
                case Node.CASEARRAY_PROP:     return "casearray";

                case Node.TARGETBLOCK_PROP:   return "targetblock";
                case Node.VARIABLE_PROP:      return "variable";
                case Node.ISNUMBER_PROP:      return "isnumber";
                case Node.DIRECTCALL_PROP:    return "directcall";

                case Node.SPECIALCALL_PROP:   return "specialcall";
                case Node.SKIP_INDEXES_PROP:  return "skip_indexes";
                case Node.OBJECT_IDS_PROP:    return "object_ids_prop";
                case Node.INCRDECR_PROP:      return "incrdecr_prop";
                case Node.CATCH_SCOPE_PROP:   return "catch_scope_prop";
                case Node.LABEL_ID_PROP:      return "label_id_prop";
                case Node.MEMBER_TYPE_PROP:   return "member_type_prop";
                case Node.NAME_PROP:          return "name_prop";
                case Node.CONTROL_BLOCK_PROP: return "control_block_prop";
                case Node.PARENTHESIZED_PROP: return "parenthesized_prop";
                case Node.GENERATOR_END_PROP: return "generator_end";
                case Node.DESTRUCTURING_ARRAY_LENGTH:
                                         return "destructuring_array_length";
                case Node.DESTRUCTURING_NAMES:return "destructuring_names";

                default: Kit.codeBug();
            }
        }
        return null;
    }

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
