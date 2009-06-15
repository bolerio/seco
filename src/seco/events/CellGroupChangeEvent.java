package seco.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.things.CellGroup;
import seco.things.CellUtils;



public class CellGroupChangeEvent extends AbstractUndoableEdit 
{
    public static final HGPersistentHandle HANDLE = HGHandleFactory.makeHandle("45e6d93f-cddf-11dc-a205-83ce1a342d9c");
    
    private HGHandle groupH;
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
        this.groupH = e;
        this.index = index;
        this.removed = removed;
        this.added = added;
        remove_eps(removed);
    }

    /**
     * Returns the underlying group.
     * 
     * @return the element
     */
    public HGHandle getCellGroup()
    {
        return groupH;
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
       restore_eps();
       CellGroup gr = (CellGroup) ThisNiche.hg.get(groupH);
       gr.batchProcess(new CellGroupChangeEvent(groupH, index, added, removed));
       
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
        restore_eps();
        CellGroup gr = (CellGroup) ThisNiche.hg.get(groupH);
        gr.batchProcess(new CellGroupChangeEvent(groupH, index, removed, added));
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
           CellUtils.removePendingCellGroupMembers(removed[i]);
       }
    }

    @Override
    public String toString()
    {
        return "" + groupH + ":" + added.length + ":" + removed.length + ":" + index;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(added);
        result = prime * result + ((groupH == null) ? 0 : groupH.hashCode());
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
        if (groupH == null)
        {
            if (other.groupH != null) return false;
        } else if (!groupH.equals(other.groupH)) return false;
        if (index != other.index) return false;
        if (!Arrays.equals(removed, other.removed)) return false;
        return true;
    }
    
    Map<HGHandle, Set<HGHandle>> pub_subs = new HashMap<HGHandle, Set<HGHandle>>();
    
    private void remove_eps(HGHandle[] removed)
    {
        pub_subs.clear();
        if(removed == null) return;
        for(HGHandle h: removed)
        {
            List<EventPubSub> l = hg.getAll(ThisNiche.hg, hg.and(hg
                    .type(EventPubSub.class), hg.incident(h), hg
                    .orderedLink(new HGHandle[] { EvalCellEvent.HANDLE,
                            HGHandleFactory.anyHandle, h, h })));;
             if (!l.isEmpty())
             {
                 Set<HGHandle> set = new HashSet<HGHandle>(l.size());
                 for(EventPubSub eps: l)
                 {
                    set.add(eps.getPublisher());
                     //System.out.println("remove_eps:" + eps);
                 }
                 for(EventPubSub eps: l)
                     ThisNiche.hg.remove(ThisNiche.handleOf(eps));
                 pub_subs.put(h, set);
             }
         }
    }
    
   
    private void restore_eps()
    {
        for(HGHandle h: pub_subs.keySet())
        {
            Set<HGHandle> set = pub_subs.get(h);
            for(HGHandle pub: set)
            {
                EventPubSub e = new EventPubSub(EvalCellEvent.HANDLE, pub, h, h);
             // System.out.println("Adding " + e);
                ThisNiche.hg.add(e);
            }
         }
    }

}