package seco.notebook.ruby;

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.BignumNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DRegexpNode;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DSymbolNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DXStrNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.FalseNode;
import org.jruby.ast.FixnumNode;
import org.jruby.ast.FloatNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.NilNode;
import org.jruby.ast.Node;
import org.jruby.ast.RegexpNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.TrueNode;
import org.jruby.ast.XStrNode;
import org.jruby.ast.ZArrayNode;
import org.jruby.ast.types.INameNode;

import seco.notebook.NotebookDocument;
// import org.netbeans.editor.BaseDocument;
// import org.openide.filesystems.FileObject;

/**
 * Perform type analysis on a given AST tree, attempting to provide a type
 * associated with each variable, field etc.
 * 
 * @todo Track boolean types for simple operators; e.g. cc_no_width = letter ==
 * '[' && !width etc. The operators here let me conclude cc_no_width is of type
 * boolean!
 * @todo Handle find* method in Rails to indicate object types
 * @todo A reference to "foo." in a method is an alias to "@foo" if the method
 * has not been defined explicitly. Attributes are especially clear, but an
 * index lookup from type analyzer may be too expensive.
 * 
 * @author Tor Norbye
 */
public class TypeAnalyzer
{
	static final String PARAM_HINT_ARG = "#:arg:"; // NOI18N
	static final String PARAM_HINT_RETURN = "#:return:=>"; // NOI18N
	/** Map from variable or field(etc) name to type. */
	private Map<String, String> types;
	private int astOffset;
	private Node root;
	/** Node we are looking for; */
	private Node target;
	private NotebookDocument doc;
    private RubyParser parser;
	
	/**
	 * Creates a new instance of TypeAnalyzer for a given position. The
	 * {@link #analyze} method will do the rest.
	 */
	public TypeAnalyzer(Node root, Node target, int astOffset, 
			NotebookDocument doc, RubyParser parser) 
	{
		this.root = root;
		this.target = target;
		this.astOffset = astOffset;
		this.doc = doc;
		this.parser = parser;
	}

	/**
	 * Analyze the given code block down to the given offset. The
	 * {@link #getType} method can then be used to read out the symbol type if
	 * any at that point.
	 */
	private void analyze(Node node)
	{
		// Avoid including definitions appearing later in the
		// context than the caret. (This only works for local variable
		// analysis; for fields it could be complicated by code earlier
		// than the caret calling code later than the caret which initializes
		// the fild...
		if (node == target)
		{
			target = null;
		}
		if (target == null && node.getPosition().getStartOffset() > astOffset)
		{
			return;
		}
		System.out.println("CallNode: " + node);
		
		// Algorithm: walk AST and look for assignments and such.
		// Attempt to compute the type of each expression and
		if (node instanceof LocalAsgnNode || node instanceof InstAsgnNode
				|| node instanceof GlobalAsgnNode
				|| node instanceof ClassVarAsgnNode
				|| node instanceof ClassVarDeclNode
				|| node instanceof DAsgnNode)
		{
			String symbol = ((INameNode) node).getName();
			String type = rhsType(node);
			if (type != null)
			{
				types.put(symbol, type);
			} else
			{
				// A more complicated expresion of some sort - we're no longer
				// sure of the type
				types.remove(symbol);
			}
		}
		if(node instanceof FCallNode){
			FCallNode call = (FCallNode) node;
			if("include_class".equals(call.getName()))
			{
				Node receiver = call.getArgsNode();
				if(receiver instanceof ArrayNode)
				{
					ArrayNode an = (ArrayNode) receiver;
					for(Object in : an.childNodes())
						included_classes.add(((StrNode)in).getValue().toString());
					return;
				}
			}
			if("include".equals(call.getName()))
			{
				Node receiver = call.getArgsNode();
				if(receiver instanceof ArrayNode)
				{
					ArrayNode an = (ArrayNode) receiver;
					for(Object in : an.childNodes())
						includes.add(((ConstNode)in).getName());
					return;
				}
			}
		}
		@SuppressWarnings("unchecked")
		List<Node> list = node.childNodes();
		for (Node child : list)
		{
			analyze(child);
		}
	}

	private String getFQNFromIncludes(String name)
	{
		for(String s: included_classes)
			if(s.endsWith("."+name))
				return s;
		return null;
	}
	/** Called on AsgnNodes to compute RHS */
	private String rhsType(Node node)
	{
//		 If it's a simple assignment, e.g. "= 5" it will have a single
		// child node
		// If it's a method call, it's slightly more complicated:
		// x = String.new("Whatever")
		// gives me a LocalAsgnNode with a Call node child with name "new",
		// and a ConstNode receiver (could be a composite too)
		@SuppressWarnings("unchecked")
		List<Node> list = node.childNodes();
		if (list.size() != 1)
		{
			return null;
		}
		Node child = list.get(0);
		return expressionType(child);
	}
	
	String expressionType(Node child)
	{
		
		if (child instanceof CallNode)
		{
			CallNode call = (CallNode) child;
			// If you call Foo.new I'm going to assume the type of the
			// expression if "Foo"
			if ("new".equals(call.getName()))
			{ // NOI18N
				Node receiver = call.getReceiverNode();
				if (receiver instanceof Colon2Node)
				{
					return AstUtilities.getFqn((Colon2Node) receiver);
				} else if (receiver instanceof INameNode)
				{
					// TODO - compute fqn (packages etc.)
					String name = ((INameNode) receiver).getName();
					String fqn = getFQNFromIncludes(name);
					return fqn != null ? fqn : name;
				}
			}else if (call.getReceiverNode() instanceof LocalVarNode){
				LocalVarNode lvn = (LocalVarNode)call.getReceiverNode();
				String type =  getType(lvn.getName());
				 System.out.println(call.getName() + ":" + type);
				 if(type != null){
				Object o = parser.evalType(type);
				//if(o != null){
					//o.getClass().getMethod(call.getName());
				//}
			   System.out.println("1" + call.getName() + ":" + type + ":" + lvn.getName() + o);
			   }
			}
			else if (call.getReceiverNode() instanceof ConstNode){
				String type = ((ConstNode)call.getReceiverNode()).getName();
				if(type != null){
					String fqn = getFQNFromIncludes(type);
					type =  fqn != null ? fqn : type;
				// System.out.println("2" + call.getName() + ":" + type + ":" + getType(type));
				 Object o = parser.evalType(type);
				// System.out.println("22" + o);
				}
			   
			}
		} 
		else if (child instanceof ConstNode)
		{
			String name = ((ConstNode) child).getName();
			if(includes.contains(name))
				return name;
			return types.get(name);
		}
		else if (child instanceof LocalVarNode)
		{
			return types.get(((LocalVarNode) child).getName());
		} else if (child instanceof DVarNode)
		{
			return types.get(((DVarNode) child).getName());
		} else if (child instanceof InstVarNode)
		{
			return types.get(((InstVarNode) child).getName());
		} else if (child instanceof GlobalVarNode)
		{
			return types.get(((GlobalVarNode) child).getName());
		} else if (child instanceof ClassVarNode)
		{
			return types.get(((ClassVarNode) child).getName());
		} else if (child instanceof ArrayNode || child instanceof ZArrayNode)
		{
			return "Array";
		} else if (child instanceof StrNode || child instanceof DStrNode
				|| child instanceof XStrNode || child instanceof DXStrNode)
		{
			return "String";
		} else if (child instanceof FixnumNode)
		{
			return "Fixnum";
		} else if (child instanceof BignumNode)
		{
			return "Bignum";
		} else if (child instanceof HashNode)
		{
			return "Hash";
		} else if (child instanceof RegexpNode || child instanceof DRegexpNode)
		{
			return "Regexp";
		} else if (child instanceof SymbolNode || child instanceof DSymbolNode)
		{
			return "Symbol";
		} else if (child instanceof FloatNode)
		{
			return "Float";
		} else if (child instanceof NilNode)
		{
			return "NilClass";
		} else if (child instanceof TrueNode)
		{
			return "TrueClass";
		} else if (child instanceof FalseNode)
		{
			return "FalseClass";
			// } else if (child instanceof RangeNode) {
			// return "Range";
		}
		return null;
	}

	private Set<String> included_classes = null;
	private Set<String> includes = null;
	
	public String getType(String symbol)
	{
		if (types == null)
		{
			types = new HashMap<String, String>();
			included_classes = new TreeSet<String>();
			includes = new TreeSet<String>();
			//???if (fileObject != null)
			//{
			//	initFileTypeVars();
			//}
			if (doc != null)
			{
				initTypeAssertions();
			}
			analyze(root);
		}
		//System.out.println("TypeAnalyzer - types: " + types +
			//	"\n" + included_classes);
		return types.get(symbol);
	}
	
	private static String[] RAILS_CONTROLLER_VARS = new String[] {
	// This is a bit of a trick. I really know the types of the
			// builtin fields here - @action_name, @assigns, @cookies,.
			// However, this usage is deprecated; people should be using
			// the corresponding accessor methods. Since I don't yet correctly
			// do type analysis of attribute to method mappings (because that
			// would
			// require consulting the index to make sure the given method has
			// not
			// been overridden), I'll just simulate this by pretending that
			// there
			// are -local- variables of the given name corresponding to the
			// return
			// value of these methods.
			"action_name", "String", // NOI18N
			"assigns", "Hash", // NOI18N
			"cookies", "ActionController::CookieJar", // NOI18N
			"flash", "ActionController::Flash::FlashHash", // NOI18N
			"headers", "Hash", // NOI18N
			"params", "Hash", // NOI18N
			"request", "ActionController::CgiRequest", // NOI18N
			"session", "CGI::Session", // NOI18N
			"url", "ActionController::UrlRewriter", // NOI18N
	};

	/** Look at the file type and see if we know about some known variables */
//	private void initFileTypeVars()
//	{
//		assert fileObject != null;
//		String ext = fileObject.getExt();
//		if (ext.equals("rb"))
//		{
//			String name = fileObject.getName();
//			if (name.endsWith("_controller"))
//			{ // NOI18N
//				// request, params, etc.
//				for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2)
//				{
//					String var = RAILS_CONTROLLER_VARS[i];
//					String type = RAILS_CONTROLLER_VARS[i + 1];
//					types.put(var, type);
//				}
//			}
//			// test files
//			// if (name.endsWith("_controller_test")) {
//			// For test files in Rails, get testing context (#105043). In
//			// particular, actionpack's
//			// ActionController::Assertions needs to be pulled in. This happens
//			// in action_controller/assertions.rb.
//		} else if (ext.equals("rhtml") || ext.equals("erb"))
//		{ // NOI18N
//			// Insert fields etc. as documented in actionpack's
//			// lib/action_view/base.rb (#105095)
//			// Insert request, params, etc.
//			for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2)
//			{
//				String var = RAILS_CONTROLLER_VARS[i];
//				String type = RAILS_CONTROLLER_VARS[i + 1];
//				types.put(var, type);
//			}
//		} else if (ext.equals("rjs"))
//		{ // #105088
//			types
//					.put("page",
//							"ActionView::Helpers::PrototypeHelper::JavaScriptGenerator::GeneratorMethods"); // NOI18N
//		} else if (ext.equals("builder") || ext.equals("rxml"))
//		{ // NOI18N
//			types.put("xml", "Builder::XmlMarkup"); // NOI18N
//			/*
//			 */
//		}
//	}

	/** Look at type assertions in the document and initialize name context */
	private void initTypeAssertions()
	{
		if (root instanceof MethodDefNode)
		{
			// Look for parameter hints
			List<String> rdoc = AstUtilities.gatherDocumentation(/*null,*/ doc,
					root);
			if ((rdoc != null) && (rdoc.size() > 0))
			{
				for (String line : rdoc)
				{
					if (line.startsWith(PARAM_HINT_ARG))
					{
						StringBuilder sb = new StringBuilder();
						String name = null;
						int max = line.length();
						int i = PARAM_HINT_ARG.length();
						for (; i < max; i++)
						{
							char c = line.charAt(i);
							if (c == ' ')
							{
								continue;
							} else if (c == '=')
							{
								break;
							} else
							{
								sb.append(c);
							}
						}
						if ((i == max) || (line.charAt(i) != '='))
						{
							continue;
						}
						i++;
						if (sb.length() > 0)
						{
							name = sb.toString();
							sb.setLength(0);
						} else
						{
							continue;
						}
						if ((i == max) || (line.charAt(i) != '>'))
						{
							continue;
						}
						i++;
						for (; i < max; i++)
						{
							char c = line.charAt(i);
							if (c == ' ')
							{
								continue;
							}
							if (!Character.isJavaIdentifierPart(c))
							{
								break;
							} else
							{
								sb.append(c);
							}
						}
						if (sb.length() > 0)
						{
							String type = sb.toString();
							types.put(name, type);
						}
					}
					// if (line.startsWith(":return:=>")) {
					// // I don't really need the return type yet
					// }
				}
			}
		}
	}
}
