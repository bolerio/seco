/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


class NotebookTreeModel implements TreeModel
{
	protected CellGroup book;
	
	public  NotebookTreeModel(CellGroup nb){
		book = nb;
	}
	
	public Object getChild(Object parent, int index)
	{
		if(parent instanceof CellGroup)
			return ((CellGroup)parent).getElement(index);
		else if(parent instanceof Cell && index == 0)
			return CellUtils.getOutCell(((Cell) parent));
		return null;
	}

	public int getChildCount(Object parent)
	{
	    if(parent instanceof CellGroup)
            return ((CellGroup)parent).getArity();
	    else if(parent instanceof Cell && CellUtils.getOutCell((Cell) parent) != null)
            return 1;
        else return 0;
	}

	public int getIndexOfChild(Object parent, Object child)
	{
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
		// TODO Auto-generated method stub
	}
	
	public void removeTreeModelListener(TreeModelListener l)
	{
		// TODO Auto-generated method stub
		
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// TODO Auto-generated method stub
		
	}

}
