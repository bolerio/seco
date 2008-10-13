/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import java.io.CharArrayReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.tree.TreePath;

import seco.notebook.storage.ClassRepository;
import seco.notebook.storage.NamedInfo;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.util.SegmentCache;
import bsh.Parser;
import bsh.SimpleNode;


public class BshAst extends NBParser
{
	boolean evaled_or_guessed = true;
	ParserRunnable parserRunnable;
	Parser parser;
	SimpleNode astRoot; 
	boolean implicitExecutionOn = false;
	private List<SimpleNode> nodes = new LinkedList<SimpleNode>();

	public BshAst(final ScriptSupport support)
	{
		super(support);
	}

	SimpleNode getRootNode()
	{
		return astRoot;
	}

	public Object resolveVar(String s, int offset) // throws EvalError
	{
		//SimpleNode root = getRootNode();
		//System.out.println("BshAst - resolveVar: " + s);//":" + support.getDocument().getEvaluationContext());
		//TODO: maybe we could infere somehow non-iniatilized or null values
		//e.g. String s; or String s = null; 
		evaled_or_guessed = true;
		if (s == null || s.length() == 0) return null;
		try
		{
			if(implicitExecutionOn || s.indexOf("(")< 0)
			{
			   Object o = support.getDocument().getEvaluationContext().eval(
						   support.getScriptEngineName(), s);
			   if (o!= null) return o;
			}
			
			if(s.indexOf("(")< 0)
			{
			   NamedInfo[] info = ClassRepository.getInstance().findSubElements(s);
			   return (info.length > 0) ? new DBInfoEx(info, s) : null;
			}else //some method is present
			{
				Object res = resolveMethod(s, offset);
				evaled_or_guessed = false;
				return res;
			}
		}
		catch (Exception err)
		{
			//err.printStackTrace();
			NamedInfo[] info = ClassRepository.getInstance().findSubElements(s);
			return (info.length > 0) ? new DBInfoEx(info, s) : null;
		}
	}
	
	public Object resolveMethod(String s, int offset){
		SimpleNode root = getRootNode();
		int[] lineCol = support.offsetToLineCol(offset);
		if(root == null || root.children.length <= lineCol[0]) {
//			try to create AST only for the passed in String
			Reader r = new CharArrayReader((s+";").toCharArray());
			Parser p = getParser(r);
			try{
				p.Line();
				root = p.popNode();
			}catch(ParseException ex){
				return null;
			}
		}else
		  root = root.getChild(lineCol[0]);
		SimpleNode n = root.getChild(0);
		//System.out.println("resolveMethod0: " + n.getClass() + ":" + root);
		if(!(n instanceof BSHMethodInvocation)) 
			return null;
		BSHMethodInvocation m = (BSHMethodInvocation)n;
		String text = m.getNameNode().text;
		int dot = text.lastIndexOf('.');
		if(dot == -1) return null;
		//System.out.println("resolveMethod1: " + text + ":" + text.substring(0, dot));
		Object var = resolveVar(text.substring(0, dot), offset);
		if(var == null) return null;
		Class[] types = resolveArgs(m.getArgsNode(), offset);
		Method meth = findMethod(var.getClass(), 
				text.substring(dot+1), types, false); //isPrivateAccessAllowed());
		if(meth == null) return null;
		if(lineCol[1] - 1 == m.getArgsNode().lastToken.endColumn)
			return meth.getReturnType();
		if(root.children.length > 1){
			Class c = meth.getReturnType();
			for(int j = 1; j < root.children.length; j++)
			{
			    BSHPrimarySuffix suff = (BSHPrimarySuffix) root.getChild(j);
			    c = resolveSuffix(c, suff, offset);
			  //  System.out.println("resolveMethod - suff: " + suff.getText() +
				//		suff.operation + ":" + suff.field + ":" + meth + ":" + c + ":"
				//		+ lineCol[1] + ":" + suff.lastToken.endColumn);
				//evaluate only to the specified offset
			    if(lineCol[1] - 1 == suff.lastToken.endColumn)  
					return c;
			    if(c == null) return null;
			}
			return c;
		}
		return meth.getReturnType();
	}
	
	private Class resolveSuffix(Class type, BSHPrimarySuffix suff, int offset)
	{
		if(suff.operation == BSHPrimarySuffix.NAME){
			Class[] types = resolveArgs(((BSHArguments) suff.getChild(0)), offset);
			Method meth = findMethod(type, 
					suff.field, types, false); //isPrivateAccessAllowed());
			return (meth != null) ? meth.getReturnType(): null;
			
		}
		return null;
	}
	
	private Class[] resolveArgs(BSHArguments b, int offset){
		int count = (b.children != null) ?	b.children.length : 0;
		Object[] args = new Object[count];
		for(int i =0; i<args.length; i++)
		{
		   BSHPrimaryExpression bpe = (BSHPrimaryExpression)
		          b.getChild(i);
		   args[i] = resolveVar(bpe.getText(), offset);
		}
		return Types.getTypes(args);
	}
	
	private static Method findMethod(Class baseClass, String methodName, Class[] types, boolean publicOnly){
		Method [] methods = Reflect.getCandidateMethods(
				baseClass, methodName, types.length, publicOnly );
		Method method = Reflect.findMostSpecificMethod( types, methods );
		return method;
	} 
	
	private static final String ACCESSIBILITY_CHECK = 
		 "import bsh.*; Capabilities.accessibility;";
	boolean isPrivateAccessAllowed()
	{
		try
		{
			return ((Boolean) support.getDocument().getEvaluationContext().eval(
						   support.getScriptEngineName(), ACCESSIBILITY_CHECK)).booleanValue();
		}
		catch (Exception err)
		{
			return false;
		}
	}
	
	private Parser getParser(Reader r)
	{
		if (parser == null)
		{
			parser = new Parser(r);
			parser.setRetainComments(true);
		} else
			parser.ReInit(r);
		return parser;
	}
	
	private Reader makeReader(Segment seg, int offset, int length)
	{
		CharArrayReader r = (seg.array.length > length) ? new CharArrayReader(
				seg.array, offset, length)
				: new CharArrayReader(seg.array);
		return r;
	}

	public NBParser.ParserRunnable getParserRunnable()
	{
		if (parserRunnable != null) return parserRunnable;
		parserRunnable = new NBParser.ParserRunnable() {
			public boolean doJob()
			{
				Segment seg = SegmentCache.getSharedInstance().getSegment();
				try
				{
					Element el = support.getElement();
					int offset = el.getStartOffset();
					int length = el.getEndOffset() - el.getStartOffset();
					support.getDocument().getText(offset, length, seg);
					Reader r = makeReader(seg, offset, length);
					parser = getParser(r);
					// System.out.println("parser.start()..." + length);
					//long start = System.currentTimeMillis();
					nodes.clear();
					while (!(parser.Line()))
					{
						SimpleNode node = parser.popNode();
						if (node != null)
							nodes.add(node);
					}
					astRoot = new SimpleNode(0);
					astRoot.children = (SimpleNode[]) nodes
							.toArray(new SimpleNode[nodes.size()]);
						
					if(implicitExecutionOn){
					ScriptEngine eng = support.getDocument().getEvaluationContext().getEngine(
							support.getScriptEngineName());
					ScriptContext scriptContext = eng.getContext();
					r = makeReader(seg, offset, length);
					//r.reset();
					 eng.eval(r, scriptContext);
					}
					//System.out.println("Time: "	 + (System.currentTimeMillis() - start));
					support.unMarkErrors();
					return true;
				}
				catch (bsh.ParseException ex){
					//System.err.println("Error: " + stripBshMsg(ex.getMessage()));
					//System.err.println("Seg: " + seg);
					//Line begins from 1 in the parser
					try{
					   ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
							stripBshMsg(ex.getMessage()),
							ex.currentToken.next.beginLine - 1,
							ex.currentToken.next.beginColumn);
					 	support.markError(mark);
					}catch(Throwable t){}
					//System.err.println(ex.getClass() + ":" + ex.getMessage());
					return false;
				}
				catch(javax.script.ScriptException ex){
					ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
							stripMsg(ex.getMessage()),
							ex.getLineNumber() -1, ex.getColumnNumber());
					support.markError(mark);
					//System.err.println(ex.getClass() + ":" + ex.getMessage());
					return false;
				}
				catch (Throwable e)
				{
					//e.printStackTrace();
					//System.err.println(e.getClass() + ":" + e.getMessage());
					return false;
				}
				finally
				{
					SegmentCache.getSharedInstance().releaseSegment(seg);
				}
			}
		};
		return parserRunnable;
	}
	
	//TODO: following 2 methods could be further fine tuned
	private static String stripMsg(String s){
		if(s == null) return s;
		s = s.substring(s.indexOf(":") + 1); 
		s = s.substring(s.indexOf(":") + 1);
		//strip the rest after 2nd ":"
		int i = s.indexOf(":");
		i = s.indexOf(":", i+1);
		return s.substring(0, i);
	}
	
	private static String stripBshMsg(String s){
		if(s == null) return s;
		int i = s.indexOf("<unknown>");
		return (i>=0) ? s.substring(i + 10) : s;
	}

	public void insertUpdate(DocumentEvent e)
	{
		removeUpdate(e);
	}

	public void removeUpdate(DocumentEvent e)
	{
		// int line = support.offsetToLine(e.getOffset());
		// if(last_valid_line > line)
		// last_valid_line = line -1;
		update();
	}

	public JTree getAstTree()
	{
		if(astRoot == null) return null;
    	JTreeAst astTree = new JTreeAst();
		JTreeASTModel treeModel = new JTreeASTModel(astRoot);
		astTree.setModel(treeModel);
		astTree.collapsePath(new TreePath(astTree.getModel()
				.getRoot()));
		return astTree;
	}
	
	//Simple wrapper to query out the package info and simplify class instantiation 
	static class DBInfoEx{
		private NamedInfo[] info;
		private String pack;
		NamedInfo[] getInfo()
		{
			return info;
		}
		String getPackage()
		{
			return pack;
		}
		
		public DBInfoEx(NamedInfo[] info, String pack)
		{
			this.info = info;
			this.pack = pack;
		}
	}
	
}
