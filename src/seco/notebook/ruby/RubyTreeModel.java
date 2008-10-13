package seco.notebook.ruby;

import java.util.List;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.jruby.ast.Node;

public class RubyTreeModel implements TreeModel
{
	Node root = null;
	// ArrayList listeners; // lazy instantiation
	protected EventListenerList listenerList = new EventListenerList();

	public RubyTreeModel(Node t)
	{
		if (t == null)
		{
			throw new IllegalArgumentException("root is null");
		}
		root = t;
	}

	public Object getChild(Object parent, int index)
	{
		if (parent == null)
		{
			return null;
		}
		// int i = 0;
		Node p = (Node) parent;
		List c = p.childNodes();
		if(index >= c.size())
			throw new ArrayIndexOutOfBoundsException("node has no such child");
		return c.get(index);
	}

	public int getChildCount(Object parent)
	{
		if (parent == null)
		{
			throw new IllegalArgumentException("root is null");
		}
		if (parent instanceof String) return 0;
		Node p = (Node) parent;
		return p.childNodes().size();
	
	}

	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent == null || child == null)
		{
			throw new IllegalArgumentException("root or child is null");
		}
		int i = 0;
		Node p = (Node) parent;
		Node c = (Node) p.childNodes().get(0);// getFirstChild();
		while (c != null && c != child)
		{
			c = (Node) c.childNodes().get(i); // getNextSibling();
			i++;
		}
		if (c == child)
		{
			return i;
		}
		throw new java.util.NoSuchElementException("node is not a child");
	}

	public Object getRoot()
	{
		return root;
	}

	public boolean isLeaf(Object node)
	{
		if (node == null)
		{
			throw new IllegalArgumentException("node is null");
		}
		if (node instanceof String)
			return true;
		else
		{
			Node t = (Node) node;
			return t.childNodes() == null || t.childNodes().size() == 0;
		}
	}

	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 * 
	 * @see #removeTreeModelListener
	 * @param l the listener to add
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * Removes a listener previously added with <B>addTreeModelListener()</B>.
	 * 
	 * @see #addTreeModelListener
	 * @param l the listener to remove
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * Returns an array of all the tree model listeners registered on this
	 * model.
	 * 
	 * @return all of this model's <code>TreeModelListener</code>s or an
	 * empty array if no tree model listeners are currently registered
	 * 
	 * @see #addTreeModelListener
	 * @see #removeTreeModelListener
	 * 
	 * @since 1.4
	 */
	public TreeModelListener[] getTreeModelListeners()
	{
		return (TreeModelListener[]) listenerList
				.getListeners(TreeModelListener.class);
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// System.out.println("\nvalueForPathChanged ... \n");
		fireTreeStructureChanged(path.getLastPathComponent(), path);
	}

	/*
	 * ==================================================================
	 * 
	 * Borrowed from javax.swing.tree.DefaultTreeModel
	 * 
	 * ==================================================================
	 */
	/*
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source the node where the tree model has changed @param path the
	 * path to the root node
	 * @see EventListenerList
	 */
	private void fireTreeStructureChanged(Object source, TreePath path)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null) e = new TreeModelEvent(source, path);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	/**
	 * @param nodeBefore
	 * @return
	 */
	public TreePath getTreePath(Node node)
	{
		Node[] nodes = getTreePathNodes((Node) getRoot(), node, 0);
		if (nodes == null)
			return null;
		else
			return new TreePath(nodes);
	}

	public Node[] getTreePathNodes(Node root, Node node,
			int depth)
	{
		if (node == null) return null;
		depth++;
		Node[] retNodes = null;
		if (node == root)
		{
			retNodes = new Node[depth];
			retNodes[depth - 1] = root;
		} else
		{
			int n = root.childNodes().size(); // .getNumberOfChildren();
			loop: for (int i = 0; i < n; i++)
			{
				retNodes = getTreePathNodes((Node) getChild(root, i),
						node, depth);
				if (retNodes != null)
				{
					retNodes[depth - 1] = root;
					break loop;
				}
			}
		}
		return retNodes;
	}
}
