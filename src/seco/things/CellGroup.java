/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.things;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EventDispatcher;



public class CellGroup extends BaseCellGroupMember implements HGLink
{
    private String name;
    protected List<HGHandle> outgoingSet = new ArrayList<HGHandle>();

    protected CellGroup()
    {

    }

    public CellGroup(String name)
    {
        this.name = name;
    }

//    public CellGroup(String name, Map attribs)
//    {
//        this.name = name;
//        this.attributes = attribs;
//    }

    public CellGroup(List<HGHandle> outgoingSet)
    {
        if (outgoingSet == null)
            throw new HGException(
                    "Attempt to construct a link with a null outgoing set. If the link has arity 0, please constructor with a 0 length array of atoms.");
        this.outgoingSet = outgoingSet;
    }

    public int getArity()
    {
        return outgoingSet.size();
    }

    public HGHandle getTargetAt(int i)
    {
        // System.out.println("CG-getTargetAt: " + outgoingSet.get(i));
        return outgoingSet.get(i);
    }

    @Override
    public String toString()
    {
        return "CellGroup" + ":" + name + ":" + ThisNiche.handleOf(this) + ":" + getArity();
    }

    public void setTargetAt(int i, HGHandle h)
    {
        outgoingSet.add(i, h);
        //ThisNiche.hg.update(this);
        // System.out.println("CG-setTargetAt: " + elements.get(i));
    }

    public void notifyTargetHandleUpdate(int i, HGHandle handle)
    {
        outgoingSet.set(i, handle);
    }

    public void notifyTargetRemoved(int i)
    {
    	outgoingSet.remove(i);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
       this.name = name;
       ThisNiche.hg.update(this);
    }

    public CellGroupMember getElement(int ind)
    {
        return (CellGroupMember) ThisNiche.hg.get(getTargetAt(ind));
    }

    public int indexOf(CellGroupMember x)
    {
        HGHandle h = ThisNiche.handleOf(x);
        if (outgoingSet.indexOf(h) == -1)
            System.out.println("NOT IN GROUP: "
                    + ThisNiche.hg.getPersistentHandle(h) + ":" + outgoingSet);
        return outgoingSet.indexOf(h);
    }

    public int indexOf(HGHandle h)
    {
        return outgoingSet.indexOf(h);
    }

    /**
     * <p>
     * Insert a new <code>Cell</code> at the specified position in the cell
     * group. The passed in <code>Cell</code> must have been already added to
     * the niche.
     * </p>
     * 
     * <p>
     * Insertion is performed by adding a <code>CellContainment</code> between
     * this <code>CellGroup</code> and the <code>Cell</code> parameter.
     * </p>
     * 
     * @param ind
     *            The insertion position.
     * @param cell
     *            The cell to be inserted.
     */
    public void insert(int ind, CellGroupMember cell)
    {
        HGHandle h = ThisNiche.handleOf(cell);
        if (h == null)
            throw new NullPointerException("Attempt to add " + cell
                    + " with Null handle in: " + this);
        HGHandle grH = ThisNiche.handleOf(this);
        if (grH  == null)
            throw new NullPointerException("Group with NULL handle: " + this);
        outgoingSet.add(ind, h);
        ThisNiche.hg.update(this);
        fireCellGroupChanged(new CellGroupChangeEvent(grH, ind,
                new HGHandle[] { h }, new HGHandle[0]));
    }

    public void append(HGHandle h)
    {
        insert(getArity(), h);
    }
    
    public void append(CellGroupMember m)    
    {
        insert(getArity(), m);
    }
    
    public void insert(int ind, HGHandle h)
    {
        if (h == null)
            throw new NullPointerException(
                    "Attempt to add a cell with Null handle in: " + this);
        HGHandle grH = ThisNiche.handleOf(this);
        if (grH  == null)
            throw new NullPointerException("Group with NULL handle: " + this);
        outgoingSet.add(ind, h);
        ThisNiche.hg.update(this);
        fireCellGroupChanged(new CellGroupChangeEvent(grH, ind,
                new HGHandle[] { h }, new HGHandle[0]));
    }

    public void remove(int i)
    {
        CellGroupMember c = getElement(i);
        remove(c);
    }

    public void batchProcess(CellGroupChangeEvent e)
    {
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        int index = e.getIndex();

        if (removed != null && removed.length > 0)
        {
            for (int i = 0; i < removed.length; i++)
            {
                outgoingSet.remove(removed[i]);
            }
        }
        if (added != null && added.length > 0)
        {
            for (int i = 0; i < added.length; i++)
                outgoingSet.add(index, added[i]);
        }
        ThisNiche.hg.update(this);
        fireCellGroupChanged(e);
    }

    public void remove(CellGroupMember x)
    {
        int i = indexOf(x);
        if (i >= 0)
        {
            HGHandle rem = outgoingSet.get(i);
            outgoingSet.remove(i);
            ThisNiche.hg.update(this);
            HGHandle grH = ThisNiche.handleOf(this);
            if (grH  == null)
                throw new NullPointerException("Group with NULL handle: " + this);
            fireCellGroupChanged(new CellGroupChangeEvent(grH, i, new HGHandle[0], new HGHandle[] { rem }));
        }
    }
    
    public void removeAll()
    {
        HGHandle grH = ThisNiche.handleOf(this);
        if (grH  == null)
            throw new NullPointerException("Group with NULL handle: " + this);
        HGHandle[] rem = outgoingSet.toArray(new HGHandle[getArity()]);
        outgoingSet.clear();
        fireCellGroupChanged(new CellGroupChangeEvent(
                grH, 0, new HGHandle[0], rem));
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof CellGroup))
            return false;
        CellGroup rt = (CellGroup) other;
        if (!rt.getName().equals(name))
            return false;
        else 
        if (outgoingSet == rt.outgoingSet)
            return true;
        else if (outgoingSet.size() != rt.outgoingSet.size())
            return false;
        for (int i = 0; i < outgoingSet.size(); i++)
            if (!outgoingSet.get(i).equals(rt.outgoingSet.get(i)))
                return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : super.hashCode();
    }

    void fireCellGroupChanged(CellGroupChangeEvent e)
    {
        HGHandle h = ThisNiche.handleOf(this);
        EventDispatcher.dispatch(CellGroupChangeEvent.HANDLE, h, e);
    }
}
