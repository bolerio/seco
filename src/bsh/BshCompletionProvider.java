/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import seco.notebook.NotebookDocument;
import seco.notebook.storage.ClassInfo;
import seco.notebook.storage.NamedInfo;
import seco.notebook.storage.PackageInfo;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.AsyncCompletionQuery;
import seco.notebook.syntax.completion.AsyncCompletionTask;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionDocumentation;
import seco.notebook.syntax.completion.CompletionProvider;
import seco.notebook.syntax.completion.CompletionQuery;
import seco.notebook.syntax.completion.CompletionResultSet;
import seco.notebook.syntax.completion.CompletionTask;
import seco.notebook.syntax.completion.JavaDocManager;
import seco.notebook.syntax.completion.MethodParamsTipPaintComponent;
import seco.notebook.syntax.java.JavaResultItem;
import seco.notebook.syntax.util.JMIUtils;
import seco.notebook.util.DocumentUtilities;
import bsh.BshAst.DBInfoEx;

public class BshCompletionProvider implements CompletionProvider
{
	public int getAutoQueryTypes(JTextComponent component, String typedText)
	{
		if (".".equals(typedText)) // &&
									// !sup.isCompletionDisabled(component.getCaret().getDot()))
			return COMPLETION_QUERY_TYPE;
		if (" ".equals(typedText)) return TOOLTIP_QUERY_TYPE;
		return 0;
	}

	public CompletionTask createTask(int queryType, JTextComponent component)
	{
		int offset = component.getCaret().getDot();
		ScriptSupport sup =
		 ((NotebookDocument) component.getDocument()).getScriptSupport(offset);
		if (sup.isCommentOrLiteral(offset -1)) return null;
		if (queryType == COMPLETION_QUERY_TYPE)
			return new AsyncCompletionTask(new Query(component.getCaret()
					.getDot()), component);
		else if (queryType == DOCUMENTATION_QUERY_TYPE)
			return new AsyncCompletionTask(new DocQuery(null), component);
		else if (queryType == TOOLTIP_QUERY_TYPE)
			return new AsyncCompletionTask(new ToolTipQuery(), component);
		return null;
	}


	static final class Query extends AsyncCompletionQuery
	{
		private JTextComponent component;
		private CompletionResultSet queryResult;
		private int creationCaretOffset;
		private int queryCaretOffset;
		private int queryAnchorOffset;
		private String filterPrefix;

		Query(int caretOffset)
		{
			this.creationCaretOffset = caretOffset;
		}

		protected void preQueryUpdate(JTextComponent component)
		{
			int caretOffset = component.getCaretPosition();
			// NotebookDocument doc = (NotebookDocument) component.getDocument();
			if (caretOffset >= creationCaretOffset)
					return;
			Completion.get().hideCompletion();
		}

		protected void query(CompletionResultSet resultSet,
				NotebookDocument doc, int offset)
		{
			ScriptSupport sup = doc.getScriptSupport(offset);
			queryCaretOffset = offset;
			queryAnchorOffset = offset;
			BshAst p = (BshAst) sup.getParser();
			try
			{
				String s = sup.getCommandBeforePt(offset);
				// System.out.println("BshCompProv - query: " + s + ":" +
				// offset);
				Object obj = p.resolveVar(s, offset);
				//System.out.println("BshCompProv - query - obj: " + obj +
				// " cls:" + ((obj != null) ? obj.getClass(): "null") + ":" + p.evaled_or_guessed);
				if (obj == null) {resultSet.finish(); return;}
				Class<?> cls = obj.getClass();
				if (cls.getName().indexOf("bsh.XThis") >= 0)
				{
					populateXThisRef(resultSet, obj);
				}
				if (obj instanceof DBInfoEx)
				{
					populatePackages(resultSet, (DBInfoEx) obj);
					resultSet.finish();
					return;
				}
				int mod = Modifier.PUBLIC;
				if (cls.getName().equals(ClassIdentifier.class.getName()))
				{
					Method method = obj.getClass().getMethod("getTargetClass",
							(Class[]) null);
					cls = (Class<?>) method.invoke(obj, (Object[]) null);
					mod = Modifier.STATIC;
				}
				if (p.isPrivateAccessAllowed()) mod |= Modifier.PRIVATE;
				if(!p.evaled_or_guessed)
					cls = (Class<?>) obj;
				populateComplPopup(resultSet, cls, mod);
			}
			catch (Exception ex)
			{
				// stay silent on eval error
				//if (!(ex instanceof UtilEvalError || ex instanceof EvalError)
				//		|| ex instanceof ScriptException) ex.printStackTrace();
			}
			resultSet.finish();
		}

		private void populatePackages(CompletionResultSet resultSet,
				DBInfoEx info)
		{
			resultSet.setTitle(info.getPackage());
			resultSet.setAnchorOffset(queryAnchorOffset);
			for (NamedInfo p : info.getInfo())
			{
				if (p instanceof PackageInfo)
				{
					JavaResultItem item = new JavaResultItem.PackageResultItem(
							(PackageInfo) p, false);
					item.setSubstituteOffset(queryCaretOffset);
					resultSet.addItem(item);
				} else if (p instanceof ClassInfo)
				{
					try
					{
						Class<?> cls = Thread.currentThread()
								.getContextClassLoader().loadClass(
										info.getPackage() + "." + p.getName());
						JavaResultItem item = new JavaResultItem.ClassResultItem(
								cls, false, false, false);
						item.setSubstituteOffset(queryCaretOffset);
						resultSet.addItem(item);
					}
					catch (Exception ex)
					{
						//ex.printStackTrace();
						//System.err.println("WARNING: " + ex.toString());
					}
				}
			}
			queryResult = resultSet;
		}
		private static final Object[] empty = new Object[0];
		private static final String REGEX = "[a-zA-Z_]+[a-zA-Z0-9_]*";

		private void populateXThisRef(CompletionResultSet resultSet, Object t)
				throws UtilEvalError
		{
			try
			{
				Object ns = t.getClass().getMethod("getNameSpace").invoke(t,
						empty);
				String[] var_names = (String[]) ns.getClass().getMethod(
						"getVariableNames").invoke(ns, empty);
				for (int i = 0; i < var_names.length; i++)
				{
					if (!var_names[i].matches(REGEX)) continue;
					Object obj = ns.getClass().getMethod("getVariable",
							new Class[] { String.class }).invoke(ns,
							new Object[] { var_names[i] });
					Class<?> cls = (obj instanceof Primitive) ? ((Primitive) obj)
							.getType() : obj.getClass();
					if (cls.getName().startsWith("jsint.")) continue;
					// System.out.println("XThis - vars: " + var_names[i] + ":"
					// + cls.getName());
					JavaResultItem item = new JavaResultItem.VarResultItem(
							var_names[i], cls, Modifier.PUBLIC);
					item.setSubstituteOffset(queryCaretOffset);
					resultSet.addItem(item);
				}
				Object[] ms = (Object[]) ns.getClass().getMethod("getMethods")
						.invoke(ns, empty);
				for (int i = 0; i < ms.length; i++)
				{
					String name = (String) ms[i].getClass()
							.getMethod("getName").invoke(ms[i], empty);
					if (!name.matches(REGEX)) continue;
					Class<?> retType = (Class<?>) ms[i].getClass().getMethod(
							"getReturnType").invoke(ms[i], empty);
					Class<?>[] params = (Class[]) ms[i].getClass().getMethod(
							"getParameterTypes").invoke(ms[i], empty);
					// System.out.println("XThis - methods: " + name + ":" +
					// ((retType != null) ? retType.getName(): "null"));
					JavaResultItem item = new JavaResultItem.MethodResultItem(
							name, retType, params, null);
					item.setSubstituteOffset(queryCaretOffset);
					resultSet.addItem(item);
				}
				queryResult = resultSet;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		private void populateComplPopup(CompletionResultSet resultSet,
				Class<?> cls, int modifiers)
		{
			// System.out.println("BshCompProv - populateComplPopup: " + cls);
			 
			resultSet.setTitle(cls.getCanonicalName());
			resultSet.setAnchorOffset(queryAnchorOffset);
			if (cls.isArray())
			{
				JavaResultItem item = new JavaResultItem.FieldResultItem(
						"length", Integer.TYPE, Modifier.PUBLIC);
				item.setSubstituteOffset(queryCaretOffset);
				resultSet.addItem(item);
			}
			for (Class<?> c: cls.getDeclaredClasses())
			{
				if(Modifier.isPrivate(c.getModifiers())) continue;
				//anonymous inner classes have empty simple name
				if(c.getSimpleName().length() == 0) continue;
				//System.out.println("BshCompl - inner classes: " + c + ":" + c.getCanonicalName());
				JavaResultItem item = new JavaResultItem.ClassResultItem(
						c, false, false, false);
				item.setSubstituteOffset(queryCaretOffset);
				resultSet.addItem(item);
			}
			for (Field f : getFields(cls, modifiers))
			{
				// when we show the static and private fields some ugly inner
				// members arise too
				if (f.getName().indexOf('$') >= 0) continue;
				JavaResultItem item = new JavaResultItem.FieldResultItem(f, cls);
				item.setSubstituteOffset(queryCaretOffset);
				resultSet.addItem(item);
			}
			for (Method m : getMethods(cls, modifiers))
			{
				if (m.getName().indexOf('$') >= 0) continue;
				JavaResultItem item = new JavaResultItem.MethodResultItem(m);
				item.setSubstituteOffset(queryCaretOffset);
				resultSet.addItem(item);
			}
			queryResult = resultSet;
		}

		private static Collection<Method> getMethods(Class<?> cls, int comp_mod)
		{
			Set<Method> set = new HashSet<Method>();
			Method[] ms = cls.getDeclaredMethods();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) set.add(ms[i]);
			ms = cls.getMethods();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) set.add(ms[i]);
			return set;
		}

		private static Collection<Field> getFields(Class<?> cls, int comp_mod)
		{
			Set<Field> set = new HashSet<Field>();
			Field[] ms = cls.getDeclaredFields();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) set.add(ms[i]);
			ms = cls.getFields();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) 
					set.add(ms[i]);
			return set;
		}

		// needed because there's no package-private modifier,
		// when comp_mod contains Modifier.PRIVATE, we allow
		// everything to pass, otherwise only public members
		private static boolean filterMod(int mod, int comp_mod)
		{
			boolean priv = (comp_mod & Modifier.PRIVATE) != 0;
			boolean stat = (comp_mod & Modifier.STATIC) != 0;
			if (stat && (mod & Modifier.STATIC) == 0) return true;
			if (!priv && (mod & Modifier.PUBLIC) == 0) return true;
			//if (!stat && (mod & Modifier.STATIC) != 0) return true;
			return false;
		}

		protected void prepareQuery(JTextComponent component)
		{
			this.component = component;
		}

		protected boolean canFilter(JTextComponent component)
		{
			int caretOffset = component.getCaretPosition();
			Document doc = component.getDocument();
			filterPrefix = null;
			if (caretOffset >= queryCaretOffset)
			{
				if (queryAnchorOffset > -1)
				{
					try
					{
						filterPrefix = doc.getText(queryAnchorOffset,
								caretOffset - queryAnchorOffset);
						if (!isJavaIdentifierPart(filterPrefix))
						{
							filterPrefix = null;
						}
					}
					catch (BadLocationException e)
					{
						// filterPrefix stays null -> no filtering
					}
				}
			}
			return (filterPrefix != null);
		}

		protected void filter(CompletionResultSet resultSet)
		{
			// System.out.println("filter: " + filterPrefix + ":" +
			// queryResult);
			if (filterPrefix != null && queryResult != null)
			{
				//resultSet.setTitle(
				//		getFilteredTitle(queryResult.getTitle(),filterPrefix));
				resultSet.setAnchorOffset(queryAnchorOffset);
				resultSet.addAllItems(getFilteredData(queryResult.getData(),
						filterPrefix));
			}
			resultSet.finish();
		}

		private boolean isJavaIdentifierPart(CharSequence text)
		{
			for (int i = 0; i < text.length(); i++)
			{
				if (!(Character.isJavaIdentifierPart(text.charAt(i))))
				{
					return false;
				}
			}
			return true;
		}

		private Collection getFilteredData(Collection data, String prefix)
		{
			List<CompletionQuery.ResultItem> ret = new ArrayList<CompletionQuery.ResultItem>();
			boolean camelCase = prefix.length() > 1
					&& prefix.equals(prefix.toUpperCase());
			for (Iterator it = data.iterator(); it.hasNext();)
			{
				CompletionQuery.ResultItem itm = (CompletionQuery.ResultItem) it
						.next();
				if (JMIUtils.startsWith(itm.getItemText(), prefix)
						|| (camelCase
								&& (itm instanceof JavaResultItem.ClassResultItem) && JMIUtils
								.matchesCamelCase(itm.getItemText(), prefix)))
				{
					ret.add(itm);
				}
				// System.out.println("getFilteredData - in: " + itm + ":" +
				// itm.getItemText());
			}
			// System.out.println("getFilteredData: " + ret.size());
			return ret;
		}

		private String getFilteredTitle(String title, String prefix)
		{
			int lastIdx = title.lastIndexOf('.');
			String ret = lastIdx == -1 ? prefix : title.substring(0,
					lastIdx + 1)
					+ prefix;
			if (title.endsWith("*")) // NOI18N
				ret += "*"; // NOI18N
			return ret;
		}
	}

	public static class DocQuery extends AsyncCompletionQuery
	{
		private Object item;
		private JTextComponent component;
		private static Action goToSource = new AbstractAction() {
			public void actionPerformed(ActionEvent e)
			{
				// DocItem doc = (DocItem)e.getSource();
				// ???JMIUtils.openElement((Element)doc.item);
				if (e != null)
				{
					Completion.get().hideDocumentation();
				}
			}
		};

		public DocQuery(Object item)
		{
			this.item = item;
		}

		protected void query(CompletionResultSet resultSet,
				NotebookDocument doc, int caretOffset)
		{
			if (item != null && JavaDocManager.SHOW_DOC)
			{
				resultSet.setDocumentation(new DocItem(
						getAssociatedObject(item), null));
			}
			resultSet.finish();
		}

		protected void prepareQuery(JTextComponent component)
		{
			this.component = component;
		}

		private Object getAssociatedObject(Object item)
		{
			Object ret = item;
			if (item instanceof JavaResultItem)
			{
				ret = ((JavaResultItem) item).getAssociatedObject();
			}
			return ret;
		}

		private class DocItem implements CompletionDocumentation
		{
			private String text;
			private JavaDoc javaDoc;
			private Object item;
			private URL url;

			public DocItem(Object item, JavaDoc javaDoc)
			{
				this.javaDoc = javaDoc != null ? javaDoc : new JavaDoc(
						component);
				this.javaDoc.docItem = this;
				this.javaDoc.setItem(item);
				this.url = getURL(item);
			}

			public CompletionDocumentation resolveLink(String link)
			{
				// ???Object item = javaDoc.parseLink(link, (Class)null);
				return null;// item != null ? new DocItem(item, javaDoc) : null;
			}

			public String getText()
			{
				return text;
			}

			public URL getURL()
			{
				return url;
			}

			private URL getURL(Object item)
			{
				return javaDoc.getURL(item);
			}

			public Action getGotoSourceAction()
			{
				return item != null ? goToSource : null;
			}

			private class JavaDoc 
			{
				public static final String CONTENT_NOT_FOUND = "JavaDoc Not Found.";
				private DocItem docItem;

				private JavaDoc(JTextComponent component)
				{
				}

				private void setItem(Object item)
				{
					showJavaDoc(JavaDocManager.getInstance().getHTML(item));
				}

				private URL getURL(Object item)
				{
					// URL[] urls = getJMISyntaxSupport().getJavaDocURLs(item);
					return null;// (urls == null || urls.length < 1) ? null :
								// urls[0];
				}

				protected void showJavaDoc(String preparedText)
				{
					if (preparedText == null) preparedText = CONTENT_NOT_FOUND;
					docItem.text = preparedText;
				}
			}
		}
	}

	static class ToolTipQuery extends AsyncCompletionQuery
	{
		private JTextComponent component;
		private int queryCaretOffset;
		private int queryAnchorOffset;
		private JToolTip queryToolTip;
		/**
		 * Method/constructor '(' position for tracking whether the method is
		 * still being completed.
		 */
		private Position queryMethodParamsStartPos = null;
		private boolean otherMethodContext;

		protected void query(CompletionResultSet resultSet,
				NotebookDocument doc, int caretOffset)
		{
			// Position oldPos = queryMethodParamsStartPos;
			queryMethodParamsStartPos = null;
			ScriptSupport sup = doc.getScriptSupport(caretOffset);
			if (sup == null || !(sup.getParser() instanceof BshAst)) return;
			BshAst p = (BshAst) sup.getParser();
			if (p.getRootNode() == null)
			{
				resultSet.finish();
				return;
			}
			SimpleNode n = ParserUtils.getASTNodeAtOffset(sup.getElement(), p
					.getRootNode(), caretOffset - 1);
			SimpleNode outer = n;
			if (outer != null)
				outer = ParserUtils.getParentOfType(n,
						BSHMethodInvocation.class);
			// System.out.println("BshCompletion - tooltipQuery - outer: " +
			// outer);
			String methodName = "";
			int offset = 0;
			if (outer == null)
			{
				outer = ParserUtils.getParentOfType(n, BSHPrimarySuffix.class);
				if (outer != null
						&& ((BSHPrimarySuffix) outer).operation == BSHPrimarySuffix.NAME)
				{
					methodName = ((BSHPrimarySuffix) outer).field;
					offset = sup.lineToOffset(outer.firstToken.beginLine - 1,
							outer.firstToken.beginColumn);
				}
			} else
			{
				methodName = ((SimpleNode) outer.children[0]).getText();
			}
			// System.out.println("BshCompletion - tooltipQuery - node: " + n +
			// ":" + methodName);
			if (n == null)
			{
				resultSet.finish();
				return;
			}
			int idx = methodName.lastIndexOf(".");
			List<List<String>> list = new ArrayList<List<String>>();
			try
			{
				if (idx > 0)
				{
					Object obj = p.resolveVar(methodName.substring(0, idx)
							.trim(), caretOffset);
					if (obj != null)
						populateResult(list, obj.getClass(), methodName
								.substring(idx + 1).trim(), Modifier.PUBLIC);
				} else
				{
					if (offset == 0)
						offset = caretOffset - methodName.length();
					Object obj = p.resolveVar(sup.getCommandBeforePt(offset)
							,sup.offsetToLineCol(offset)[1]);
					if (obj != null)
						populateResult(list, obj.getClass(), methodName,
								Modifier.PUBLIC);
				}
			}
			catch (Exception ex)
			{
				// stay silent on eval error
				if (!(ex instanceof UtilEvalError || ex instanceof EvalError))
					ex.printStackTrace();
			}
			resultSet
					.setToolTip(queryToolTip = new MethodParamsTipPaintComponent(
							list, -1));
			resultSet.finish();
		}

		private void populateResult(List<List<String>> list, Class<?> cls, String name,
				int modifiers)
		{
			Method[] ms = cls.getMethods();
			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i].getModifiers() != modifiers) continue;
				if (!ms[i].getName().equals(name)) continue;
				JavaResultItem item = new JavaResultItem.MethodResultItem(ms[i]);
				List<String> parms = new ArrayList<String>();
				parms.add(item.toString());
				list.add(parms);
			}
		}

		protected void prepareQuery(JTextComponent component)
		{
			this.component = component;
		}

		protected boolean canFilter(JTextComponent component)
		{
			CharSequence text = null;
			int textLength = -1;
			int caretOffset = component.getCaretPosition();
			Document doc = component.getDocument();
			try
			{
				if (caretOffset - queryCaretOffset > 0)
					text = DocumentUtilities.getText(doc, queryCaretOffset,
							caretOffset - queryCaretOffset);
				else if (caretOffset - queryCaretOffset < 0)
					text = DocumentUtilities.getText(doc, caretOffset,
							queryCaretOffset - caretOffset);
				else
					textLength = 0;
			}
			catch (BadLocationException e)
			{
			}
			if (text != null)
			{
				textLength = text.length();
			} else if (textLength < 0)
			{
				return false;
			}
			boolean filter = true;
			int balance = 0;
			for (int i = 0; i < textLength; i++)
			{
				char ch = text.charAt(i);
				switch (ch)
				{
				case ',':
					filter = false;
					break;
				case '(':
					balance++;
					filter = false;
					break;
				case ')':
					balance--;
					filter = false;
					break;
				}
				if (balance < 0) otherMethodContext = true;
			}
			if (otherMethodContext && balance < 0) otherMethodContext = false;
			if (queryMethodParamsStartPos == null
					|| caretOffset <= queryMethodParamsStartPos.getOffset())
				filter = false;
			return otherMethodContext || filter;
		}

		protected void filter(CompletionResultSet resultSet)
		{
			if (!otherMethodContext)
			{
				resultSet.setAnchorOffset(queryAnchorOffset);
				resultSet.setToolTip(queryToolTip);
			}
			resultSet.finish();
		}
	}
}
