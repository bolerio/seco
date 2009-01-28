package seco.events;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.things.CellUtils;

public class EvalCellEvent extends AbstractUndoableEdit
{
    public static final HGPersistentHandle HANDLE = HGHandleFactory
            .makeHandle("22061f39-d011-11dc-b0cc-9759d91d7754");
    private HGHandle cellH;
    private EvalResult value;
    private EvalResult oldValue;

    public EvalCellEvent(HGHandle owner, EvalResult value, EvalResult old_value)
    {
        super();
        this.cellH = owner;
        this.value = value;
        this.oldValue = old_value;
    }

    public HGHandle getCellHandle()
    {
        return cellH;
    }

    public EvalResult getValue()
    {
        return value;
    }

    public EvalResult getOldValue()
    {
        return oldValue;
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
        EvalCellEvent e = new EvalCellEvent(cellH, value, oldValue); 
        EventDispatcher.dispatch(EvalCellEvent.HANDLE, cellH, e);
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
        EvalCellEvent e = new EvalCellEvent(cellH, oldValue, value); 
        EventDispatcher.dispatch(EvalCellEvent.HANDLE, cellH, e);
    }
    
    @Override
    public void die()
    {
       super.die();
       if(oldValue instanceof HGHandle)
       {
           System.out.println("EvalCelEvent - die: " + oldValue);
          CellUtils.removeHandlers((HGHandle)oldValue);
       }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cellH == null) ? 0 : cellH.hashCode());
        result = prime * result
                + ((oldValue == null) ? 0 : oldValue.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final EvalCellEvent other = (EvalCellEvent) obj;
        if (cellH == null)
        {
            if (other.cellH != null) return false;
        } else if (!cellH.equals(other.cellH)) return false;
        if (oldValue == null)
        {
            if (other.oldValue != null) return false;
        } else if (!oldValue.equals(other.oldValue)) return false;
        if (value == null)
        {
            if (other.value != null) return false;
        } else if (!value.equals(other.value)) return false;
        return true;
    }
}
