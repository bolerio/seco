package seco.notebook.ruby;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
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
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.internal.runtime.methods.DefaultMethod;
import org.jruby.internal.runtime.methods.DynamicMethod;

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
import seco.notebook.syntax.java.JavaCompletion;
import seco.notebook.syntax.java.JavaPaintComponent;
import seco.notebook.syntax.java.JavaResultItem;
import seco.notebook.syntax.java.JavaPaintComponent.CallableFeaturePaintComponent;
import seco.notebook.syntax.util.JMIUtils;
import seco.notebook.util.DocumentUtilities;


public class RubyCompletionProvider implements CompletionProvider
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
		ScriptSupport sup = ((NotebookDocument) component.getDocument())
				.getScriptSupport(offset);
		if (sup.isCommentOrLiteral(offset - 1)) return null;
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
			// NotebookDocument doc = (NotebookDocument)
			// component.getDocument();
			if (caretOffset >= creationCaretOffset)
			{
				// try {
				//if (true) // isJavaIdentifierPart(DocumentUtilities.getText(doc,
					// creationCaretOffset, caretOffset -
					// creationCaretOffset)))
					return;
				// if(!isCommentOrLiteral(doc, creationCaretOffset))
				// return;
				// } catch (BadLocationException e) {
				// }
			}
			Completion.get().hideCompletion();
		}

		protected void query(CompletionResultSet resultSet,
				NotebookDocument doc, int offset)
		{
			ScriptSupport sup = doc.getScriptSupport(offset);
			queryCaretOffset = offset;
			queryAnchorOffset = offset;
			RubyParser p = (RubyParser) sup.getParser();
			try
			{
				String s = sup.getCommandBeforePt(offset);
				// System.out.println("RubyCompProv - query: " + s + ":" +
				// offset);
				Object obj = p.resolveVar(s, offset);
				// if (obj != null)
				//System.out.println("RubyCompProv - query - obj: " + obj);
				// " cls:" + ((obj != null) ? obj.getClass(): "null") + ":" +
				// p.evaled_or_guessed);
				if (obj == null)
				{
					resultSet.finish();
					return;
				}
				Class cls = obj.getClass();
				if (obj instanceof RubyClass)
				{
					populateRubyClass(resultSet, (RubyClass) obj);
					return;
				}else if (obj instanceof RubyModule)
				{
					populateRubyModule(resultSet, (RubyModule) obj);
					return;
				}
				int mod = Modifier.PUBLIC;
				if (!p.evaled_or_guessed) cls = (Class) obj;
				populateComplPopup(resultSet, cls, mod);
			}
			catch (Exception ex)
			{
				// stay silent on eval error
				// if (ex instanceof ScriptException)
				ex.printStackTrace();
			}
			resultSet.finish();
		}

		private void populateRubyClass(CompletionResultSet resultSet,
				RubyClass t)
		{
			// System.out.println("populateRubyClass: " + t);
			while (t != null)
			{
				for (Object key : t.getMethods().keySet())
				{
					DynamicMethod m = (DynamicMethod) t.getMethods().get(key);
					if (m.getVisibility().isPrivate() || 
					        m.getVisibility().isProtected()) continue;
					JavaResultItem item = new RubyMethodResultItem(
							(String) key, "void");
					item.setSubstituteOffset(queryCaretOffset);
					resultSet.addItem(item);
				}
				t = t.getSuperClass();
			}
			resultSet.finish();
			queryResult = resultSet;
		}
		
		private void populateRubyModule(CompletionResultSet resultSet,
				RubyModule t)
		{
			resultSet.setTitle("RubyModule: " + t.getName());
			System.out.println("populateRubyModule: " + t);
			while (t != null)
			{
				for (Object key : t.getMethods().keySet())
				{
					DynamicMethod m = (DynamicMethod) t.getMethods().get(key);
					//if (m.getVisibility().isPublic()) continue;
					JavaResultItem item = new RubyMethodResultItem(
							(String) key, "void");
					item.setSubstituteOffset(queryCaretOffset);
					resultSet.addItem(item);
				}
				t = t.getSuperClass();
			}
			resultSet.finish();
			
			queryResult = resultSet;
		}

		private void populateComplPopup(CompletionResultSet resultSet,
				Class cls, int modifiers)
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
			for (Class c : cls.getDeclaredClasses())
			{
				if (Modifier.isPrivate(c.getModifiers())) continue;
				// annonimous inner classes have empty simple name
				if (c.getSimpleName().length() == 0) continue;
				// System.out.println("BshCompl - inner classes: " + c + ":" +
				// c.getCanonicalName());
				JavaResultItem item = new JavaResultItem.ClassResultItem(c,
						false, false, false);
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

		private static Collection<Method> getMethods(Class cls, int comp_mod)
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

		private static Collection<Field> getFields(Class cls, int comp_mod)
		{
			Set<Field> set = new HashSet<Field>();
			Field[] ms = cls.getDeclaredFields();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) set.add(ms[i]);
			ms = cls.getFields();
			for (int i = 0; i < ms.length; i++)
				if (!filterMod(ms[i].getModifiers(), comp_mod)) set.add(ms[i]);
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
			// if (!stat && (mod & Modifier.STATIC) != 0) return true;
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
			System.out.println("filter: " + filterPrefix + ":" + queryResult);
			if (filterPrefix != null && queryResult != null)
			{
				// resultSet.setTitle(getFilteredTitle(queryResult.getTitle(),
				// filterPrefix));
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
			// if (item == null)
			// ??? item = JMIUtils.findItemAtCaretPos(component);
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
			// if (ret instanceof Feature)
			// ret = JMIUtils.getDefintion((Feature)ret);
			// if (ret instanceof ClassDefinition)
			// ret = JMIUtils.getSourceElementIfExists((ClassDefinition)ret);
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

			private class JavaDoc // extends NbJMICompletionJavaDoc
			{
				public static final String CONTENT_NOT_FOUND = "JavaDoc Not Found.";
				private DocItem docItem;

				private JavaDoc(JTextComponent component)
				{
					// super(component);
				}

				private void setItem(Object item)
				{
					showJavaDoc(JavaDocManager.getInstance().getHTML(item));
					// RequestProcessor.getDefault().post(new
					// MyJavaDocParser(item));
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
			if (sup == null || !(sup.getParser() instanceof RubyParser))
				return;
			RubyParser p = (RubyParser) sup.getParser();
			// TODO:
			if (p.getRootNode() == null)
			{
				resultSet.finish();
				return;
			}
		}

		private void populateResult(List list, Class cls, String name,
				int modifiers)
		{
			Method[] ms = cls.getMethods();
			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i].getModifiers() != modifiers) continue;
				if (!ms[i].getName().equals(name)) continue;
				JavaResultItem item = new JavaResultItem.MethodResultItem(ms[i]);
				List parms = new ArrayList();
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

	static class RubyMethodResultItem extends JavaResultItem.MethodResultItem
	{
		private static JavaPaintComponent.MethodPaintComponent mtdComponent = null;

		public RubyMethodResultItem(String mtdName, String type)
		{
			super(mtdName, type);
		}

		protected boolean isAddParams(){
			return false;
		}
		
		public Component getPaintComponent(boolean isSelected)
		{
			if (mtdComponent == null)
			{
				mtdComponent = new RubyMethodPaintComponent();
			}
			mtdComponent.setFeatureName(getName());
			mtdComponent.setModifiers(getModifiers());
			mtdComponent.setTypeName(getTypeName());
			mtdComponent.setTypeColor(getTypeColor());
			// mtdComponent.setParams(getParams());
			// mtdComponent.setExceptions(getExceptions());
			return mtdComponent;
		}
	}

	public static class RubyMethodPaintComponent extends
			JavaPaintComponent.MethodPaintComponent
	{
		 protected void drawParameterList(Graphics g, List prmList) {
	            
	     }
	}
}
