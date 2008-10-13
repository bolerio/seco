package seco.notebook.ruby;

import java.util.LinkedList;
import java.util.List;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsCatNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.IScopingNode;
import org.jruby.ast.IScopingNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.lexer.yacc.SourcePosition;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.util.ByteList;

import seco.notebook.NotebookDocument;
import seco.notebook.Utilities;
import seco.notebook.syntax.ScriptSupport;


/**
 * 
 * @todo Create a NodePath abstraction, wrapping my ArrayList operation that I'm
 * using here?
 * 
 * @author Tor Norbye
 */
public class AstUtilities
{
	public static String getFqn(Colon2Node c2n)
	{
		StringBuilder sb = new StringBuilder();
		addAncestorParents(c2n, sb);
		return sb.toString();
	}

	private static void addAncestorParents(Node node, StringBuilder sb)
	{
		if (node instanceof Colon2Node)
		{
			Colon2Node c2n = (Colon2Node) node;
			addAncestorParents(c2n.getLeftNode(), sb);
			if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) != ':'))
			{
				sb.append("::");
			}
			sb.append(c2n.getName());
		} else if (node instanceof INameNode)
		{
			if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) != ':'))
			{
				sb.append("::");
			}
			sb.append(((INameNode) node).getName());
		}
	}

	/**
	 * Get the rdoc documentation associated with the given node in the given
	 * document. The node must have position information that matches the source
	 * in the document.
	 */
	public static List<String> gatherDocumentation(
	/* CompilationInfo info, */NotebookDocument baseDoc, Node node)
	{
		LinkedList<String> comments = new LinkedList<String>();
		int elementBegin = node.getPosition().getStartOffset();
		// if (info != null) {
		// elementBegin = LexUtilities.getLexerOffset(info, elementBegin);
		// }
		try
		{
			// Search to previous lines, locate comments. Once we have a
			// non-whitespace line that isn't
			// a comment, we're done
			int offset = Utilities.getRowStart(baseDoc, elementBegin);
			offset--;
			// Skip empty and whitespace lines
			while (offset >= 0)
			{
				// Find beginning of line
				offset = Utilities.getRowStart(baseDoc, offset);
				if (!Utilities.isRowEmpty(baseDoc, offset)
						&& !Utilities.isRowWhite(baseDoc, offset))
				{
					break;
				}
				offset--;
			}
			if (offset < 0)
			{
				return null;
			}
			while (offset >= 0)
			{
				// Find beginning of line
				offset = Utilities.getRowStart(baseDoc, offset);
				if (Utilities.isRowEmpty(baseDoc, offset)
						|| Utilities.isRowWhite(baseDoc, offset))
				{
					// Empty lines not allowed within an rdoc
					break;
				}
				// This is a comment line we should include
				int lineBegin = Utilities.getRowFirstNonWhite(baseDoc, offset);
				int lineEnd = Utilities.getRowLastNonWhite(baseDoc, offset) + 1;
				String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);
				// Tolerate "public", "private" and "protected" here --
				// Test::Unit::Assertions likes to put these in front of each
				// method.
				if (line.startsWith("#"))
				{
					comments.addFirst(line);
				} else if ((comments.size() == 0)
						&& line.startsWith("=end")
						&& (lineBegin == Utilities.getRowStart(baseDoc, offset)))
				{
					// It could be a =begin,=end document - see scanf.rb in Ruby
					// lib for example. Treat this differently.
					gatherInlineDocumentation(comments, baseDoc, offset);
					return comments;
				} else if (line.equals("public") || line.equals("private")
						|| line.equals("protected"))
				{ // NOI18N
					// Skip newlines back up to the comment
					offset--;
					while (offset >= 0)
					{
						// Find beginning of line
						offset = Utilities.getRowStart(baseDoc, offset);
						if (!Utilities.isRowEmpty(baseDoc, offset)
								&& !Utilities.isRowWhite(baseDoc, offset))
						{
							break;
						}
						offset--;
					}
					continue;
				} else
				{
					// No longer in a comment
					break;
				}
				// Previous line
				offset--;
			}
		}
		catch (BadLocationException ble)
		{
			ble.printStackTrace();
		}
		return comments;
	}

	private static void gatherInlineDocumentation(LinkedList<String> comments,
			NotebookDocument baseDoc, int offset) throws BadLocationException
	{
		// offset points to a line containing =end
		// Skip the =end list
		offset = Utilities.getRowStart(baseDoc, offset);
		offset--;
		// Search backwards in the document for the =begin (if any) and add all
		// lines in reverse
		// order in between.
		while (offset >= 0)
		{
			// Find beginning of line
			offset = Utilities.getRowStart(baseDoc, offset);
			// This is a comment line we should include
			int lineBegin = offset;
			int lineEnd = Utilities.getRowEnd(baseDoc, offset);
			String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);
			if (line.startsWith("=begin"))
			{
				// We're done!
				return;
			}
			comments.addFirst(line);
			// Previous line
			offset--;
		}
	}

	public static ClassNode findClass(AstPath path)
	{
		// Find the closest block node enclosing the given node
		for (Node curr : path)
		{
			if (curr instanceof ClassNode)
			{
				return (ClassNode) curr;
			}
		}
		return null;
	}
	
}
