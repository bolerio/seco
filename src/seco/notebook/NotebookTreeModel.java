/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


class NotebookTreeModel implements TreeModel
{
	protected CellGroupMember book;
	
	public  NotebookTreeModel(CellGroupMember nb)
	{
		book = nb;
	}
	
	public Object getChild(Object parent, int index)
	{
		if(parent instanceof CellGroup)
			return ((CellGroup)parent).getElement(index);
		else if(parent instanceof Cell)
		{
			List<Cell> list = 
			    CellUtils.getOutCells(ThisNiche.handleOf(parent));
			return list.get(index);
		}
		return null;
	}

	public int getChildCount(Object parent)
	{
	    if(parent instanceof CellGroup)
            return ((CellGroup)parent).getArity();
	    else if(parent instanceof Cell)
            return CellUtils.getOutCells(ThisNiche.handleOf(parent)).size();
        else return 0;
	}

	public int getIndexOfChild(Object parent, Object child)
	{
	    if(!(parent instanceof CellGroup))
	    {
	        List<Cell> list = 
                CellUtils.getOutCells(ThisNiche.handleOf(parent));
	        for(int i = 0; i < list.size(); i++)
	            if(child.equals(list.get(i))) return i;
	        return -1;
	    }
	    return ((CellGroup) parent).indexOf((CellGroupMember)child);
	}

	public Object getRoot()
	{
		return book;
	}

	public boolean isLeaf(Object node)
	{
		return getChildCount(node) == 0;
	}

	public void addTreeModelListener(TreeModelListener l)
	{
	}
	
	public void removeTreeModelListener(TreeModelListener l)
	{
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

}
