/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/* ====================================================================
 *
 * This class is an adaptation of antlr.debug.misc.JTreeASTModel.
 * See:
 *
 * ANTLR Translator Generator
 * Project led by Terence Parr at http://www.jGuru.com
 * Software rights: http://www.antlr.org/RIGHTS.html
 *
 * $Id: JTreeASTModel.java,v 1.1 2006/07/14 17:22:44 bizi Exp $
 */




package bsh;


import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;



public class JTreeASTModel implements TreeModel {


    SimpleNode root = null;
//    ArrayList listeners; // lazy instantiation
	protected EventListenerList listenerList = new EventListenerList();


    public JTreeASTModel(SimpleNode t) {
        if (t == null) {
            throw new IllegalArgumentException("root is null");
        }
        root = t;
    }  
    
    public Object getChild( Object parent, int index) {
        if (parent == null) {
            return null;
        }
		SimpleNode p = (SimpleNode)parent;
		if(index >= 0 && index < p.children.length)
			return p.getChild(index);
		
		throw new ArrayIndexOutOfBoundsException("node has no such child");
	}
    
    public int getChildCount(Object parent) {
        if ( parent==null ) {
            throw new IllegalArgumentException("root is null");
        }
		if (parent instanceof String) 
			return 0;
			
		SimpleNode p = (SimpleNode)parent;
		
		return p.children.length;
     }  
    
    public int getIndexOfChild(Object parent, Object child) {
        if ( parent==null || child==null ) {
            throw new IllegalArgumentException("root or child is null");
        }
		int i = 0;
		Token t;
		SimpleNode p = (SimpleNode)parent;
		SimpleNode c = (SimpleNode)p.children[0];//getFirstChild();
        while ( c!=null && c!=child ) {
            c = (SimpleNode)c.children[i]; //getNextSibling();
            i++;
        }
        if (c == child) {
            return i;
        }
        throw new java.util.NoSuchElementException("node is not a child");
    }  

    public Object getRoot() {
        return root;
    } 

    public boolean isLeaf(Object node) {
        if ( node == null ) {
            throw new IllegalArgumentException("node is null");
        }
		if (node instanceof String)
			return true;
        else {
        	SimpleNode t = (SimpleNode)node;
        	return t.children == null || t.children.length == 0;
        }
    }
    
	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 *
	 * @see     #removeTreeModelListener
	 * @param   l       the listener to add
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * Removes a listener previously added with <B>addTreeModelListener()</B>.
	 *
	 * @see     #addTreeModelListener
	 * @param   l       the listener to remove
	 */  
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * Returns an array of all the tree model listeners
	 * registered on this model.
	 *
	 * @return all of this model's <code>TreeModelListener</code>s
	 *         or an empty
	 *         array if no tree model listeners are currently registered
	 *
	 * @see #addTreeModelListener
	 * @see #removeTreeModelListener
	 *
	 * @since 1.4
	 */
	public TreeModelListener[] getTreeModelListeners() {
		return (TreeModelListener[])listenerList.getListeners(
				TreeModelListener.class);
	}
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        //System.out.println("\nvalueForPathChanged ... \n");
		fireTreeStructureChanged( path.getLastPathComponent(), path);
    }  
    
 
/* ==================================================================
 * 
 *    Borrowed from javax.swing.tree.DefaultTreeModel
 * 
 * ==================================================================
 */
 
 /*
  * Notifies all listeners that have registered interest for
  * notification on this event type.  The event instance 
  * is lazily created using the parameters passed into 
  * the fire method.
  *
  * @param source the node where the tree model has changed
  * @param path the path to the root node
  * @see EventListenerList
  */
  private void fireTreeStructureChanged(Object source, TreePath path) {
	  // Guaranteed to return a non-null array
	  Object[] listeners = listenerList.getListenerList();
	  TreeModelEvent e = null;
	  // Process the listeners last to first, notifying
	  // those that are interested in this event
	  for (int i = listeners.length-2; i>=0; i-=2) {
		  if (listeners[i]==TreeModelListener.class) {
			  // Lazily create the event:
			  if (e == null)
				  e = new TreeModelEvent(source, path);
			  ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
		  }
	  }
  }

/**
 * @param nodeBefore
 * @return
 */
public TreePath getTreePath( SimpleNode node) {
	SimpleNode[] nodes = getTreePathNodes((SimpleNode) getRoot(), node, 0);
	if (nodes == null)
		return null;
	else
		return new TreePath( nodes);	
}

public SimpleNode[] getTreePathNodes( SimpleNode root, SimpleNode node, int depth) {
	if (node == null) return null;
	depth++;
	SimpleNode[] retNodes = null;
	if (node == root) {
		retNodes = new SimpleNode[depth];
		retNodes[depth-1] = root;
	} else {
		int n = root.children.length; //.getNumberOfChildren();
		loop:
		for (int i = 0; i < n; i++) {
			retNodes = getTreePathNodes((SimpleNode) getChild(root, i), node, depth);
			if (retNodes != null) {
				retNodes[depth-1] = root;
				break loop;
			}
		}
			
	}

	return retNodes;	
}
  
    
}
