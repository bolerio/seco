package seco.langs.ruby;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.swing.JTree;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import org.jruby.Ruby;
import org.jruby.ast.ConstNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.parser.Parser;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.scope.ManyVarsDynamicScope;

import seco.notebook.storage.ClassRepository;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.NBParser;
import seco.notebook.util.SegmentCache;


public class RubyParser extends NBParser
{
	boolean evaled_or_guessed = true;
	ParserRunnable parserRunnable;
	Parser parser;
	RootNode astRoot;
	boolean implicitExecutionOn = false;
	private List<Node> nodes = new LinkedList<Node>();
	Ruby runtime;
	JRubyScriptEngine engine;


	public RubyParser(final ScriptSupport support)
	{
		super(support);
		engine = (JRubyScriptEngine) 
	    support.getDocument().getEvaluationContext().getEngine("jruby");
	}

	public Node getRootNode()
	{
		return astRoot;
	}

	public Object resolveVar(String s, int offset) // throws EvalError
	{
		if (s == null || s.length() == 0) return null;
		if (getRootNode() != null)
		{
			Node n = ParserUtils.getASTNodeAtOffset(support.getElement(),
					getRootNode(), offset - 1);
			// AstPath ap = new AstPath();
			// Node n1 = ap.findPathTo(getRootNode(), offset-1);
			// System.out.println("RubyParser - resolveVar: " + n + ":" + n1
			// + ":" + (offset-1) + ":" + n.equals(n1));
			try
			{
				TypeAnalyzer a = new TypeAnalyzer(getRootNode(), n, 
						offset - 1,	support.getDocument(), this);
				String type = a.getType(s);
				System.out.println("Type: " + s + ":" + type);
				if (type == null) 
					type = a.expressionType(n);
				System.out.println("Type1: " + n + ":" + type);
				if (type == null)
					return null;
				evaled_or_guessed = false;
				if(type.indexOf('.') > 0)
				{
					Class[] info = ClassRepository.getInstance()
							.findClass(type);
					// System.out.println("Info: " + info + ":" + info.length);
					return (info.length > 0) ? info[0] : null;
				}else{
					return evalType(type);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

	Object evalType(String name) 
	{
		System.out.println("evalType: " + name + ":" + astRoot);
		if(astRoot == null) return null;
		StaticScope scope = astRoot.getStaticScope();
		DynamicScope dyn = astRoot.getScope(); 
		if(dyn == null)
			dyn = new ManyVarsDynamicScope(scope, null);
		Node node = new ConstNode(new IDESourcePosition(), name);
		ThreadContext ctx = runtime.getCurrentContext();
		ctx.preEvalScriptlet(dyn);
		try{
		   return engine.evalNode(node, new SimpleScriptContext());
		 }catch(ScriptException ex){
			 ex.printStackTrace();
			return null;
		}
	}

	private Parser getParser()
	{
		if (parser == null)
		{
			runtime = Ruby.getDefaultInstance();
			parser = new Parser(runtime);
		}
		return parser;
	}

	private Reader makeReader(Segment seg, int offset, int length)
	{
		CharArrayReader r = (seg.array.length > length) ? new CharArrayReader(
				seg.array, offset, length) : new CharArrayReader(seg.array);
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
					parser = getParser();
					// System.out.println("parser.start()..." + length);
					// long start = System.currentTimeMillis();
					nodes.clear();
					astRoot = (RootNode)
					     // runtime.parse(r, "<unknown>", null, 0);
					   runtime.parseEval(seg.toString(), "<unknown>", null, 0);
					if (implicitExecutionOn)
					{
						ScriptContext scriptContext = engine.getContext();
						r = makeReader(seg, offset, length);
						engine.eval(r, scriptContext);
					}
					// System.out.println("Time: " + (System.currentTimeMillis()
					// - start));
					support.unMarkErrors();
					return true;
				}
				catch (javax.script.ScriptException ex)
				{
					ScriptSupport.ErrorMark mark = new ScriptSupport.ErrorMark(
							stripMsg(ex.getMessage()), ex.getLineNumber() - 1,
							ex.getColumnNumber());
					support.markError(mark);
					// System.err.println(ex.getClass() + ":" +
					// ex.getMessage());
					return false;
				}
				catch (Throwable e)
				{
					// e.printStackTrace();
					// System.err.println(e.getClass() + ":" + e.getMessage());
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

	// TODO: following 2 methods could be further fine tuned
	private static String stripMsg(String s)
	{
		if (s == null) return s;
		s = s.substring(s.indexOf(":") + 1);
		s = s.substring(s.indexOf(":") + 1);
		// strip the rest after 2nd ":"
		int i = s.indexOf(":");
		i = s.indexOf(":", i + 1);
		return s.substring(0, i);
	}

	public JTree getAstTree()
	{
		if (astRoot == null) return null;
		JTree astTree = new JTree();
		RubyTreeModel treeModel = new RubyTreeModel(astRoot);
		astTree.setModel(treeModel);
		// astTree.collapsePath(new TreePath(astTree.getModel()
		// .getRoot()));
		return astTree;
	}
	
//	public List<Object> complete(ScriptSupport sup, 
//			int lexOffset, String prefix, int queryType,
//			boolean caseSensitive)
//	{
//		int astOffset = lexOffset;
//		if (astOffset == -1)
//		{
//			return null;
//		}
//		// Avoid all those annoying null checks
//		if (prefix == null)
//		{
//			prefix = "";
//		}
//		// Let's stick the keywords in there...
//		List<Object> proposals = new ArrayList<Object>();
//		int anchor = lexOffset - prefix.length();
//		NotebookDocument doc = sup.getDocument();
//		
//		// Discover whether we're in a require statement, and if so, use special
//		// completion
//		//TokenHierarchy<Document> th = TokenHierarchy.get(document);
//		
//		// See if we're inside a string or regular expression and if so,
//		// do completions applicable to strings - require-completion,
//		// escape codes for quoted strings and regular expressions, etc.
//		//if (completeStrings(proposals, index, prefix, astOffset, lexOffset, th))
//		//{
//		//	return proposals;
//		//}
//		boolean showLower = true;
//		boolean showUpper = true;
//		boolean showSymbols = false;
//		char first = 0;
//		if (prefix.length() > 0)
//		{
//			first = prefix.charAt(0);
//			// Foo::bar --> first char is "b" - we're looking for a method
//			int qualifier = prefix.lastIndexOf("::");
//			if ((qualifier != -1) && (qualifier < (prefix.length() - 2)))
//			{
//				first = prefix.charAt(qualifier + 2);
//			}
//			showLower = Character.isLowerCase(first);
//			// showLower is not necessarily !showUpper - prefix can be ":foo"
//			// for example
//			showUpper = Character.isUpperCase(first);
//			if (first == ':')
//			{
//				showSymbols = true;
//				if (prefix.length() > 1)
//				{
//					char second = prefix.charAt(1);
//					prefix = prefix.substring(1);
//					showLower = Character.isLowerCase(second);
//					showUpper = Character.isUpperCase(second);
//				}
//			}
//		}
//		// Fields
//		// This is a bit stupid at the moment, not looking at the current typing
//		// context etc.
//		Node root = astRoot;
//		if (root == null)
//		{
//			completeKeywords(proposals, prefix, showSymbols);
//			return proposals;
//		}
//		// Compute the bounds of the line that the caret is on, and suppress
//		// nodes overlapping the line.
//		// This will hide not only paritally typed identifiers, but surrounding
//		// contents like the current class and module
//		int astLineBegin = -1;
//		int astLineEnd = -1;
//		try
//		{
//			astLineBegin = Utilities.getRowStart(doc, lexOffset);
//			astLineEnd = Utilities.getRowEnd(doc, lexOffset);
//		}
//		catch (BadLocationException ble)
//		{
//			ble.printStackTrace();
//		}
//		AstPath path = new AstPath(root, astOffset);
//		Map<String, Node> variables = new HashMap<String, Node>();
//		Map<String, Node> fields = new HashMap<String, Node>();
//		Map<String, Node> globals = new HashMap<String, Node>();
//		Map<String, Node> constants = new HashMap<String, Node>();
//		Node closest = path.leaf();
//		//Call call = LexUtilities.getCallType(doc, th, lexOffset);
//		// Don't try to add local vars, globals etc. as part of calls or class
//		// fqns
//		if (call.getLhs() == null)
//		{
//			if (showLower && (closest != null))
//			{
//				Node block = AstUtilities.findDynamicScope(closest, path);
//				@SuppressWarnings("unchecked")
//				List<Node> list = block.childNodes();
//				for (Node child : list)
//				{
//					addDynamic(child, variables);
//				}
//				Node method = AstUtilities.findLocalScope(closest, path);
//				@SuppressWarnings("unchecked")
//				List<Node> list2 = method.childNodes();
//				for (Node child : list2)
//				{
//					addLocals(child, variables);
//				}
//			}
//			if ((prefix.length() == 0) || (first == '@') || showSymbols)
//			{
//				Node clz = AstUtilities.findClass(path);
//				if (clz != null)
//				{
//					@SuppressWarnings("unchecked")
//					List<Node> list = clz.childNodes();
//					for (Node child : list)
//					{
//						addFields(child, fields);
//					}
//				}
//			}
//			// $ is neither upper nor lower
//			if ((prefix.length() == 0) || (first == '$') || showSymbols)
//			{
//				@SuppressWarnings("unchecked")
//				List<Node> list = root.childNodes();
//				for (Node child : list)
//				{
//					addGlobals(child, globals);
//				}
//			}
//		}
//		// TODO: should only include fields etc. down to caret location???
//		// Decide. (Depends on language semantics. Can I have forward
//		// referemces?
//		if (showUpper || showSymbols)
//		{
//			addConstants(root, constants);
//		}
//		// If we're in a call, add in some info and help for the code completion
//		// call
//		// NOT YET ENABLED
//		// addMembers(proposals, info, path, caretOffset);
//		// Code completion from the index.
//		if (index != null)
//		{
//			if (showLower || showSymbols)
//			{
//				String fqn = AstUtilities.getFqnName(path);
//				if ((fqn == null) || (fqn.length() == 0))
//				{
//					fqn = "Object"; // NOI18N
//				}
//				if ((fqn != null)
//						&& completeDefMethod(proposals, index, prefix,
//								astOffset, lexOffset, th, fqn, kind, queryType))
//				{
//					if (queryType == QueryType.DOCUMENTATION)
//					{
//						proposals = filterDocumentation(proposals, root, doc,
//								info, astOffset, lexOffset, prefix, path, index);
//					}
//					return proposals;
//				}
//				if ((fqn != null)
//						&& completeObjectMethod(proposals, index, prefix,
//								astOffset, lexOffset, doc, th, fqn, path,
//								closest, kind, queryType, call, info
//										.getFileObject()))
//				{
//					return proposals;
//				}
//				// Only call local and inherited methods if we don't have an
//				// LHS, such as Foo::
//				if (call.getLhs() == null)
//				{
//					// TODO - pull this into a completeInheritedMethod call
//					// Complete inherited methods or local methods only (plus
//					// keywords) since there
//					// is no receiver so it must be a local or inherited method
//					// call
//					Set<IndexedMethod> inheritedMethods = index
//							.getInheritedMethods(fqn, prefix, kind);
//					// RHTML hack
//					if (RubyUtils.isRhtmlFile(info.getFileObject()))
//					{
//						// Hack - include controller and helper files as well
//						FileObject f = info.getFileObject().getParent();
//						String controllerName = null;
//						while (f != null && !f.getName().equals("views"))
//						{ // todo - make sure grandparent is app
//							String n = RubyUtils.underlinedNameToCamel(f
//									.getName());
//							if (controllerName == null)
//							{
//								controllerName = n;
//							} else
//							{
//								controllerName = n + "::" + controllerName;
//							}
//							f = f.getParent();
//						}
//						Set<IndexedMethod> helper = index.getInheritedMethods(
//								controllerName + "Helper", prefix, kind);
//						inheritedMethods.addAll(helper);
//						// TODO - pull in the fields (NOT THE METHODS) from the
//						// controller
//						// Set<IndexedMethod> controller =
//						// index.getInheritedMethods(controllerName+"Controller",
//						// prefix, kind);
//						// inheritedMethods.addAll(controller);
//					}
//					for (IndexedMethod method : inheritedMethods)
//					{
//						// This should not be necessary - filtering happens in
//						// getInheritedMethods right?
//						if ((prefix.length() > 0)
//								&& !method.getName().startsWith(prefix))
//						{
//							continue;
//						}
//						// If a method is an "initialize" method I should do
//						// something special so that
//						// it shows up as a "constructor" (in a new() statement)
//						// but not as a directly
//						// callable initialize method (it should already be
//						// culled because it's private)
//						MethodItem item = new MethodItem(method, anchor);
//						item.setSmart(method.isSmart());
//						if (showSymbols)
//						{
//							item.setSymbol(true);
//						}
//						proposals.add(item);
//					}
//				}
//			}
//			if (showUpper || showSymbols)
//			{
//				completeClasses(proposals, index, prefix, astOffset, lexOffset,
//						kind, queryType, showSymbols, call);
//			}
//		}
//		
//		// TODO
//		// Remove fields and variables whose names are already taken, e.g. do a
//		// fields.removeAll(variables) etc.
//		for (String variable : variables.keySet())
//		{
//			if ((&& prefix.equals(variable))
//					|| (startsWith(variable, prefix)))
//			{
//				Node node = variables.get(variable);
//				if (!overlapsLine(node, astLineBegin, astLineEnd))
//				{
//					AstVariableElement co = new AstVariableElement(node,
//							variable);
//					PlainItem item = new PlainItem(co, anchor);
//					item.setSmart(true);
//					if (showSymbols)
//					{
//						item.setSymbol(true);
//					}
//					proposals.add(item);
//				}
//			}
//		}
//		for (String field : fields.keySet())
//		{
//			if (((kind == NameKind.EXACT_NAME) && prefix.equals(field))
//					|| ((kind != NameKind.EXACT_NAME) && startsWith(field,
//							prefix)))
//			{
//				Node node = fields.get(field);
//				if (overlapsLine(node, astLineBegin, astLineEnd))
//				{
//					continue;
//				}
//				Element co = new AstFieldElement(node);
//				FieldItem item = new FieldItem(co, anchor);
//				item.setSmart(true);
//				if (showSymbols)
//				{
//					item.setSymbol(true);
//				}
//				proposals.add(item);
//			}
//		}
//		// TODO - model globals and constants using different icons / etc.
//		for (String variable : globals.keySet())
//		{
//			// TODO - kind.EXACT_NAME
//			if (startsWith(variable, prefix)
//					|| (showSymbols && startsWith(variable.substring(1), prefix)))
//			{
//				Node node = globals.get(variable);
//				if (overlapsLine(node, astLineBegin, astLineEnd))
//				{
//					continue;
//				}
//				AstElement co = new AstVariableElement(node, variable);
//				PlainItem item = new PlainItem(co, anchor);
//				item.setSmart(true);
//				if (showSymbols)
//				{
//					item.setSymbol(true);
//				}
//				proposals.add(item);
//			}
//		}
//		// TODO - model globals and constants using different icons / etc.
//		for (String variable : constants.keySet())
//		{
//			if (((kind == NameKind.EXACT_NAME) && prefix.equals(variable))
//					|| ((kind != NameKind.EXACT_NAME) && startsWith(variable,
//							prefix)))
//			{
//				// Skip constants that are known to be classes
//				Node node = constants.get(variable);
//				if (overlapsLine(node, astLineBegin, astLineEnd))
//				{
//					continue;
//				}
//				// ComObject co;
//				// if (isClassName(variable)) {
//				// co = JRubyNode.create(node, null);
//				// if (co == null) {
//				// continue;
//				// }
//				// } else {
//				// co = new DefaultComVariable(variable, false, -1, -1);
//				// ((DefaultComVariable)co).setNode(node);
//				AstElement co = new AstVariableElement(node, variable);
//				PlainItem item = new PlainItem(co, anchor);
//				item.setSmart(true);
//				if (showSymbols)
//				{
//					item.setSymbol(true);
//				}
//				proposals.add(item);
//			}
//		}
//		if (completeKeywords(proposals, prefix, showSymbols))
//		{
//			return proposals;
//		}
//		if (queryType == QueryType.DOCUMENTATION)
//		{
//			proposals = filterDocumentation(proposals, root, doc, info,
//					astOffset, lexOffset, prefix, path, index);
//		}
//		return proposals;
//	}
}
