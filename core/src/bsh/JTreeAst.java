/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package bsh;

import static javax.script.ScriptContext.ENGINE_SCOPE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.script.ScriptContext;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import seco.notebook.NotebookUI;
import bsh.engine.ScriptContextEngineView;

public class JTreeAst extends JTree
{
	private static final long serialVersionUID = 1L;
	
	public JTreeAst()
	{
		super();
		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					TreePath currentSelection = getSelectionPath();
					TreePath selPath = getPathForLocation(e.getX(), e.getY());
					if (currentSelection != null && currentSelection == selPath)
					{
						JPopupMenu popup = makePopupMenu();
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});
	}

	static final String engineNameSpaceKey = "org_beanshell_engine_namespace";
	private static NameSpace getEngineNameSpace( ScriptContext scriptContext )
	{
		NameSpace ns = (NameSpace)scriptContext.getAttribute(
			engineNameSpaceKey, ENGINE_SCOPE );

		if ( ns == null )
		{
			// Create a global namespace for the interpreter
			Map engineView = new ScriptContextEngineView( scriptContext );
			ns = new ExternalNameSpace(
				null/*parent*/, "javax_script_context", engineView );

			scriptContext.setAttribute( engineNameSpaceKey, ns, ENGINE_SCOPE );
		}

		return ns;
	}
	
	private void setNS(Interpreter inter)
	{
	    NotebookUI ui = NotebookUI.getFocusedNotebookUI();
		if(ui == null)	return;
		ScriptContext scriptContext = ui.getDoc().getScriptingContext();
		bsh.NameSpace contextNameSpace = getEngineNameSpace( scriptContext);
		inter.setNameSpace( contextNameSpace );
	}
	
	private JPopupMenu makePopupMenu()
	{
		//DefaultMutableTreeNode dmtn 
		final SimpleNode node = (SimpleNode) getLastSelectedPathComponent();
		//node.getLineNumber();
		final Interpreter inter = new Interpreter();
		NotebookUI ui = NotebookUI.getFocusedNotebookUI();
		//ScriptContext scriptContext = ui.getDoc().getScriptingContext();
		//final CallStack callstack = new CallStack(getEngineNameSpace( scriptContext) ); //inter.get.globalNameSpace );
		
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Eval"); // NOI18N
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					setNS(inter);
				   //System.out.println(node.eval(callstack,inter));
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		popup.add(menuItem);
		menuItem = new JMenuItem("toClass"); // NOI18N
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					setNS(inter);
				   //System.out.println(((BSHAmbiguousName)node).toClass(
					//	   callstack,inter));
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		menuItem.setEnabled(node instanceof BSHAmbiguousName);
		popup.add(menuItem);
		menuItem = new JMenuItem("toObject"); // NOI18N
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					setNS(inter);
				   //System.out.println(((BSHAmbiguousName)node).toObject(
					//	   callstack,inter));
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		menuItem.setEnabled(node instanceof BSHAmbiguousName);
		popup.add(menuItem);
		menuItem = new JMenuItem("PrimarySuffix"); // NOI18N
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					setNS(inter);
					BSHPrimarySuffix suf = ((BSHPrimarySuffix)node);
				  // System.out.println("Op: " + suf.operation + ":" +
					//	   suf.getText() + ":" + suf.field);
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		menuItem.setEnabled(node instanceof BSHPrimarySuffix);
		popup.add(menuItem);
		return popup;
	}
}
