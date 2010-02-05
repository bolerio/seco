package seco.events;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.safehaus.uuid.UUIDGenerator;

import seco.ThisNiche;
import seco.things.CellGroupMember;



public class AttributeChangeEvent extends AbstractUndoableEdit 
{
    public static final HGPersistentHandle HANDLE = HGHandleFactory.makeHandle("bb9bdb36-cdcf-11dc-bd27-e1853813fbe2");
    
    private HGHandle owner;
    private Object name;
    private Object value;
    private Object old_value;

    public AttributeChangeEvent(HGHandle owner, 
            Object name, Object value, Object old_value)
    {
        this.owner = owner;
        this.name = name;
        this.value = value;
        this.old_value = old_value;
    }

    public HGHandle getCellGroupMember(){
        return owner;
    }
    public Object getName()
    {
        return name;
    }

    public Object getOldValue()
    {
        return old_value;
    }

    public Object getValue()
    {
        return value;
    }
  
    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();
        CellGroupMember cell = (CellGroupMember) ThisNiche.graph.get(owner);
        cell.setAttribute(name, value);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();
        CellGroupMember cell = (CellGroupMember) ThisNiche.graph.get(owner);
        cell.setAttribute(name, old_value);
    }
 
}
