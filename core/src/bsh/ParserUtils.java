/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Element;

import seco.notebook.NotebookDocument;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.CompletionResultSet;
import seco.notebook.syntax.completion.MethodParamsTipPaintComponent;

public class ParserUtils
{
	static SimpleNode getParentOfType(SimpleNode n, Class clazz)
	{
		//System.out.println("ParserUtils - getParentOfType: " + n + ":" + clazz);
		if(n == null) return null;
		if(n.getClass().equals(clazz))	return n;
		SimpleNode par = (SimpleNode) n.parent;
		while(par != null)
		{
			//System.out.println("ParserUtils - getParentOfType - in: " + par);
			if(par.getClass().equals(clazz))
				return par;
			par = (SimpleNode) par.parent;
		}
		return null;
	}
	
	static SimpleNode getASTNodeAtOffset(Element el, SimpleNode root, int offset)
	{
		//System.out.println("ParserUtils - getASTNodeAtOffset - el: " + el+ ":" + 
		//	root + ":" + offset);
		int line = el.getElementIndex(offset);
		if(line < 0) return null;
		Element inner = el.getElement(line);
		int line_offset = offset - inner.getStartOffset();
        //parser line starts from 1
		line++;
		return getASTNode(root, line, line_offset);
	}
	
	static SimpleNode getASTNode(SimpleNode root, int line, int line_offset)
	{
		int begLine = 0;
		//System.out.println("ParserUtils - getASTNode: " + root+ ":" + 
		//		line + ":" + line_offset);
		if(root.children == null || root.children.length == 0) return null;
	       
		for(int i = 0; i< root.children.length; i++)
		{
			begLine = ((SimpleNode)root.children[i]).firstToken.beginLine;
			//System.out.println("ParserUtils - getASTNode - inner: " + begLine);
			if(begLine == line)
				return getInnerNode(((SimpleNode)root.children[i]), line_offset);
			if(begLine > line)
				return (i==0) ? null : getASTNode(((SimpleNode)root.children[i-1]), line,line_offset);
	    }
		 //check the last node from the loop
		SimpleNode last = ((SimpleNode)root.children[root.children.length-1]);
		if(last.lastToken.endLine > line)
		   return getASTNode(last, line, line_offset);
		return null;
	}
	
	private static SimpleNode getInnerNode(SimpleNode root, int pos) {
		//System.out.println("ParserUtils - getInnerNode - root: " + root + ":" + pos);
		if(root.children == null || root.children.length == 0)
			return root;
		for (Node e : root.children) 
		{
			int index = getElementIndex((SimpleNode) e, pos);
			//System.out.println("ParserUtils - getInnerNode - index: " + index);
			if(index != -1)
			{
		        SimpleNode in = getInnerNode((SimpleNode) ((SimpleNode)e).children[index], pos);
		        return (in != null) ? in : (SimpleNode) e;
		     }
		}
		return null;
	}
	
	static int getElementIndex(SimpleNode n, int offset)
	{
		//System.out.println("ParserUtils - getElementIndex: " + offset + ":" + getEndOffset(n));
		if (n == null || n.children == null ||
				n.children.length == 0 || offset >= getEndOffset(n))
			return -1;
		
		for(int i = 0; i < n.children.length; i++)
		{
			SimpleNode e = (SimpleNode) n.children[i];
			//System.out.println("ParserUtils - getElementIndex - in : " + e 
			//		+ ":" + getEndOffset(e) + ":" + getStartOffset(e));
			if(getEndOffset(e) >= offset && getStartOffset(e) <= offset)
				return i;
		}
		return -1;
	}
	
	private static int getStartOffset(SimpleNode n)
	{
		return n.firstToken.beginColumn;
	}
	
	private static int getEndOffset(SimpleNode n)
	{
		return n.lastToken.endColumn;
	}
	
//	 public  static Method resolveMethod(
//             NotebookDocument doc, int caretOffset)
//     {
//         ScriptSupport sup = doc.getScriptSupport(caretOffset);
//         if (sup == null || !(sup.getParser() instanceof BshAst)) return null;
//         BshAst p = (BshAst) sup.getParser();
//         if (p.getRootNode() == null)
//         {
//            
//         }
//         SimpleNode n = ParserUtils.getASTNodeAtOffset(sup.getElement(), p
//                 .getRootNode(), caretOffset - 1);
//         SimpleNode outer = n;
//         if (outer != null)
//             outer = ParserUtils.getParentOfType(n,
//                     BSHMethodInvocation.class);
//         String methodName = "";
//         int offset = 0;
//         if (outer == null)
//         {
//             outer = ParserUtils.getParentOfType(n, BSHPrimarySuffix.class);
//             if (outer != null
//                     && ((BSHPrimarySuffix) outer).operation == BSHPrimarySuffix.NAME)
//             {
//                 methodName = ((BSHPrimarySuffix) outer).field;
//                 offset = sup.lineToOffset(outer.firstToken.beginLine - 1,
//                         outer.firstToken.beginColumn);
//             }
//         }
//         else
//         {
//             methodName = ((SimpleNode) outer.children[0]).getText();
//         }
//         // System.out.println("BshCompletion - tooltipQuery - node: " + n +
//         // ":" + methodName);
//         if (n == null)
//         {
//             resultSet.finish();
//             return;
//         }
//         int idx = methodName.lastIndexOf(".");
//         List<List<String>> list = new ArrayList<List<String>>();
//         try
//         {
//             if (idx > 0)
//             {
//                 Object obj = p.resolveVar(methodName.substring(0, idx)
//                         .trim(), caretOffset);
//                 //if (obj != null)
//                 //    populateResult(list, obj.getClass(), methodName
//                 //            .substring(idx + 1).trim(), Modifier.PUBLIC);
//             }
//             else
//             {
//                 //if (offset == 0)
//                 //    offset = caretOffset - methodName.length();
//                 //Object obj = p.resolveVar(sup.getCommandBeforePt(offset),
//                //         sup.offsetToLineCol(offset)[1]);
//                // if (obj != null)
//                //     populateResult(list, obj.getClass(), methodName,
//                //             Modifier.PUBLIC);
//             }
//         }
//         catch (Exception ex)
//         {
//             // stay silent on eval error
//             if (!(ex instanceof UtilEvalError || ex instanceof EvalError))
//                 ex.printStackTrace();
//         }
//     }

    private ParserUtils()
    {
    }
	
	
}
