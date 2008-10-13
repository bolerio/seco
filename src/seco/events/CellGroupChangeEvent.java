package seco.events;

import java.util.Arrays;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.things.CellGroup;
import seco.things.CellUtils;



public class CellGroupChangeEvent extends AbstractUndoableEdit 
{
    public static final HGPersistentHandle HANDLE = HGHandleFactory.makeHandle("45e6d93f-cddf-11dc-a205-83ce1a342d9c");
    
    private HGHandle group;
    private int index;
    private HGHandle[] removed;
    private HGHandle[] added;
    
    /**
     * Constructs an edit record. This does not modify the element so it can
     * safely be used to <em>catch up</em> a view to the current model state
     * for views that just attached to a model.
     * 
     * @param e   the element
     * @param index the index into the model >= 0
     * @param removed
     *            a set of elements that were removed
     * @param added
     *            a set of elements that were added
     */
    public CellGroupChangeEvent(HGHandle e, int index, 
            HGHandle[] added, HGHandle[] removed)
    {
        super();
        this.group = e;
        this.index = index;
        this.removed = removed;
        this.added = added;
    }

    /**
     * Returns the underlying group.
     * 
     * @return the element
     */
    public HGHandle getCellGroup()
    {
        return group;
    }

    /**
     * Returns the index into the list of elements.
     * 
     * @return the index >= 0
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Gets a list of children that were removed.
     * @return the list
     */
    public HGHandle[] getChildrenRemoved()
    {
        return removed;
    }

    /**
     * Gets a list of children that were added.
     * @return the list
     */
    public HGHandle[] getChildrenAdded()
    {
        return added;
    }

    /**
     * Redoes a change.
     * 
     * @exception CannotRedoException
     *                if the change cannot be redone
     */
    public void redo() throws CannotRedoException
    {
        super.redo();
      
        // Since this event will be reused, switch around added/removed.
        HGHandle[] tmp = removed;
        removed = added;
        added = tmp;
       System.out.println("CellGroupChangeEventImpl - Redo"); 
       CellGroup gr = (CellGroup) ThisNiche.hg.get(group);
       for(int i = 0; i < removed.length; i++)
          gr.remove(index);
       for(int i = 0; i < added.length; i++)
           gr.insert(index, added[i]);
    }

    /**
     * Undoes a change.
     * 
     * @exception CannotUndoException
     *                if the change cannot be undone
     */
    public void undo() throws CannotUndoException
    {
        super.undo();
        System.out.println("CellGroupChangeEventImpl - Undo");  
        CellGroup gr = (CellGroup) ThisNiche.hg.get(group);
        for(int i = 0; i < added.length; i++)
            gr.remove(index);
         for(int i = 0; i < removed.length; i++)
             gr.insert(index, removed[i]);

        // Since this event will be reused, switch around added/removed.
        HGHandle[] tmp = removed;
        removed = added;
        added = tmp;
    }

    @Override
    public void die()
    {
       super.die();
       for(int i = 0; i < removed.length; i++)
       {
           System.out.println("CellGroupChangeEvent - die: " + removed[i]);
           CellUtils.removeHandlers(removed[i]);
       }
    }

    @Override
    public String toString()
    {
        return "" + group + ":" + added.length + ":" + removed.length + ":" + index;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(added);
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + index;
        result = prime * result + Arrays.hashCode(removed);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final CellGroupChangeEvent other = (CellGroupChangeEvent) obj;
        if (!Arrays.equals(added, other.added)) return false;
        if (group == null)
        {
            if (other.group != null) return false;
        } else if (!group.equals(other.group)) return false;
        if (index != other.index) return false;
        if (!Arrays.equals(removed, other.removed)) return false;
        return true;
    }

}