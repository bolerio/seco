package seco.notebook.javascript;

import java.util.Iterator;

import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.Node.Jump;

public class JSUtils0
{
    public static String toString(Node node)
    {
        StringBuffer sb = new StringBuffer();
        ObjToIntMap printIds = new ObjToIntMap();
        int type = node.getType();
        sb.append(TokenEx.name(type));
        if (node.getClass().getName().indexOf("StringNode") > 0)
        {
            sb.append(' ');
            sb.append(node.getString());
//            Node.Scope scope = node.getScope();
//            if (scope != null)
//            {
//                sb.append("[scope: ");
//                appendPrintId(scope, printIds, sb);
//                sb.append("]");
//            }
        }
        else if (node.getClass().getName().indexOf("Node.Scope") > 0)
        {
            if (node instanceof ScriptOrFnNode)
            {
                ScriptOrFnNode sof = (ScriptOrFnNode) node;
                if (node instanceof FunctionNode)
                {
                    FunctionNode fn = (FunctionNode) node;
                    sb.append(' ');
                    sb.append(fn.getFunctionName());
                }
                sb.append(" [source name: ");
                sb.append(sof.getSourceName());
                sb.append("] [encoded source length: ");
                sb.append(sof.getEncodedSourceEnd()
                        - sof.getEncodedSourceStart());
                sb.append("] [base line: ");
                sb.append(sof.getBaseLineno());
                sb.append("] [end line: ");
                sb.append(sof.getEndLineno());
                sb.append(']');
            }
//            if (((Node.Scope) node).symbolTable != null)
//            {
//                sb.append(" [scope ");
//                appendPrintId(node, printIds, sb);
//                sb.append(": ");
//                Iterator<String> iter = ((Node.Scope) node).symbolTable
//                        .keySet().iterator();
//                while (iter.hasNext())
//                {
//                    sb.append(iter.next());
//                    sb.append(" ");
//                }
//                sb.append("]");
//            }
        }
        else if (node instanceof Jump)
        {
            Jump jump = (Jump) node;
            if (type == Token.BREAK || type == Token.CONTINUE)
            {
                sb.append(" [label: ");
                appendPrintId(jump.getJumpStatement(), printIds, sb);
                sb.append(']');
            }
            else if (type == Token.TRY)
            {
                Node catchNode = jump.target;
                Node finallyTarget = jump.getFinally();
                if (catchNode != null)
                {
                    sb.append(" [catch: ");
                    appendPrintId(catchNode, printIds, sb);
                    sb.append(']');
                }
                if (finallyTarget != null)
                {
                    sb.append(" [finally: ");
                    appendPrintId(finallyTarget, printIds, sb);
                    sb.append(']');
                }
            }
            else if (type == Token.LABEL || type == Token.LOOP
                    || type == Token.SWITCH)
            {
                sb.append(" [break: ");
                appendPrintId(jump.target, printIds, sb);
                sb.append(']');
                if (type == Token.LOOP)
                {
                    sb.append(" [continue: ");
                    appendPrintId(jump.getContinue(), printIds, sb);
                    sb.append(']');
                }
            }
            else
            {
                sb.append(" [target: ");
                appendPrintId(jump.target, printIds, sb);
                sb.append(']');
            }
        }
        else if (type == Token.NUMBER)
        {
            sb.append(' ');
            sb.append(node.getDouble());
        }
        else if (type == Token.TARGET)
        {
            sb.append(' ');
            appendPrintId(node, printIds, sb);
        }
        if (node.getLineno() != -1)
        {
            sb.append(" line: ");
            sb.append(node.getLineno());
        }

//        for (PropListItem x = propListHead; x != null; x = x.next)
//        {
//            int t = x.type;
//            sb.append(" [");
//            sb.append(node.propToString(type));
//            sb.append(": ");
//            String value;
//            switch (t)
//            {
//            case Node.TARGETBLOCK_PROP: // can't add this as it recurses
//                value = "target block property";
//                break;
//            case  Node.LOCAL_BLOCK_PROP: // can't add this as it is dull
//                value = "last local block";
//                break;
//            case  Node.ISNUMBER_PROP:
//                switch (x.intValue)
//                {
//                case  Node.BOTH:
//                    value = "both";
//                    break;
//                case  Node.RIGHT:
//                    value = "right";
//                    break;
//                case  Node.LEFT:
//                    value = "left";
//                    break;
//                default:
//                    throw Kit.codeBug();
//                }
//                break;
//            case  Node.SPECIALCALL_PROP:
//                switch (x.intValue)
//                {
//                case  Node.SPECIALCALL_EVAL:
//                    value = "eval";
//                    break;
//                case  Node.SPECIALCALL_WITH:
//                    value = "with";
//                    break;
//                default:
//                    // NON_SPECIALCALL should not be stored
//                    throw Kit.codeBug();
//                }
//                break;
//            case  Node.OBJECT_IDS_PROP:
//            {
//                Object[] a = (Object[]) x.objectValue;
//                value = "[";
//                for (int i = 0; i < a.length; i++)
//                {
//                    value += a[i].toString();
//                    if (i + 1 < a.length) value += ", ";
//                }
//                value += "]";
//                break;
//            }
//            default:
//                Object obj = x.objectValue;
//                if (obj != null)
//                {
//                    value = obj.toString();
//                }
//                else
//                {
//                    value = String.valueOf(x.intValue);
//                }
//                break;
//            }
//            sb.append(value);
//            sb.append(']');
//        }
        return sb.toString();
    }
    
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

    private static void appendPrintId(Node n, ObjToIntMap printIds,
            StringBuffer sb)
    {
        if (n != null)
        {
            int id = printIds.get(n, -1);
            sb.append('#');
            if (id != -1)
            {
                sb.append(id + 1);
            }
            else
            {
                sb.append("<not_available>");
            }
        }
    }
}
