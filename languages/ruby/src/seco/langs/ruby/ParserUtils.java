package seco.langs.ruby;

import java.util.List;
import javax.swing.text.Element;
import org.jruby.Ruby;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class ParserUtils
{
	static Node getASTNodeAtOffset(Element el, Node root, int offset)
	{
		int line = el.getElementIndex(offset);
		//System.out.println("ParserUtils - getASTNodeAtOffset - el: " + el+ ":" + 
		//	root + ":" + offset + ":" + line);
		
		if(line < 0) return null;
		int line_offset = offset - el.getStartOffset();
        return getASTNode(root, line, line_offset);
	}
	
	static Node getASTNode(Node root, int line, int line_offset)
	{
		int begLine = 0;
		//System.out.println("ParserUtils - getASTNode: " + root+ ":" + 
			//	line + ":" + line_offset);
		List ch = root.childNodes();
		if(ch == null || ch.size()==0) return null;
	       
		for(int i = 0; i< ch.size(); i++)
		{
			Node inner = (Node)ch.get(i);
			begLine = inner.getPosition().getStartLine();
			//System.out.println("ParserUtils - getASTNode - inner: " + begLine +
				//	":" + inner);
			if(begLine == line)
				return getInnerNode(inner, line_offset);
			if(begLine > line)
				return (i==0) ? null : getASTNode(((Node)ch.get(i-1)), line,line_offset);
	    }
		 //check the last node from the loop
		Node last = ((Node)ch.get(ch.size()-1));
		//System.out.println("ParserUtils - getASTNode - inner: " + last.getPosition().getEndLine() +
			//	":" + last);
		
		if(last.getPosition().getEndLine() >= line)
		   return getASTNode(last, line, line_offset);
		return null;
	}
	
	private static Node getInnerNode(Node root, int pos) {
		//System.out.println("ParserUtils - getInnerNode - root: " + root + ":" + pos);
		List ch = root.childNodes();
		if(ch == null || ch.size() == 0) return root;
		for (Object e : ch) 
		{
			int index = getElementIndex((Node) e, pos);
			//System.out.println("ParserUtils - getInnerNode - index: " + index + ":" + e);
			if(index == -2)
				return (Node) e;
			if(index != -1)
			{
				Node in = getInnerNode((Node) ((Node)e).childNodes().get(index), pos);
		        return (in != null) ? in : (Node) e;
		     }
			
		}
		return null;
	}
	
	static int getElementIndex(Node n, int offset)
	{
		//System.out.println("ParserUtils - getElementIndex: " + offset + ":" + getEndOffset(n));
		List ch = (n == null) ? null : n.childNodes();
		if (ch == null || offset > getEndOffset(n))
			return -1;
		if(ch.size() == 0)
			return -2;
		
		for(int i = 0; i < ch.size(); i++)
		{
			Node e = (Node) ch.get(i);
			//System.out.println("ParserUtils - getElementIndex - in : " + e 
				//	+ ":" + getEndOffset(e) + ":" + getStartOffset(e));
			if(getEndOffset(e) >= offset && getStartOffset(e) <= offset)
				return i;
		}
		return -1;
	}
	
	private static int getStartOffset(Node n)
	{
		return n.getPosition().getStartOffset();
	}
	
	private static int getEndOffset(Node n)
	{
		return n.getPosition().getEndOffset();
	}
	
	
}
