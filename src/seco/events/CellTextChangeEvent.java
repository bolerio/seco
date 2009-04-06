package seco.events;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.things.Cell;
import seco.things.CellUtils;
import seco.things.Scriptlet;



public class CellTextChangeEvent extends AbstractUndoableEdit 
{
    public static final HGPersistentHandle HANDLE = 
        HGHandleFactory.makeHandle("9033e77b-ce48-11dc-afb7-fbdaa25763d7");
    
    private HGHandle cellH;
    private EventType type;
    private String text;
    private int offset;
    private int length;
    
    public CellTextChangeEvent(HGHandle cell, EventType type, String text,
            int offset, int length)
    {
        super();
        this.cellH = cell;
        this.type = type;
        this.text = text;
        this.offset = offset;
        this.length = length;
    }

    public HGHandle getCell()
    {
        return cellH;
    }
    
    public int getLength()
    {
       return length;
    }

    public int getOffset()
    {
        return offset;
    }

    public String getText()
    {
        return text;
    }

    public EventType getType()
    {
       return type;
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();
        Cell cell = (Cell) ThisNiche.hg.get(cellH);
        Scriptlet s = (Scriptlet) cell.getValue();
        String code = s.getCode(); 
        StringBuffer res = new StringBuffer(code.substring(0, getOffset()));
        if(getType() == CellTextChangeEvent.EventType.INSERT){
            res.append(getText());
            res.append(code.substring(getOffset()));
        }else{
            res.append(code.substring(getOffset() + getLength()));
        }
        s.setCode(res.toString());
        ThisNiche.hg.update(s);
        type = (type == CellTextChangeEvent.EventType.REMOVE) ? 
                CellTextChangeEvent.EventType.INSERT : CellTextChangeEvent.EventType.REMOVE;
        CellUtils.fireCellTextChanged(cellH, this);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();
        Cell cell = (Cell) ThisNiche.hg.get(cellH);
        Scriptlet s = (Scriptlet) cell.getValue();
        String code = s.getCode(); 
        //System.out.println("CellTextChangeEvent - undo:" + getType() + ":" + getText());
        StringBuffer res = new StringBuffer(code.substring(0, getOffset()));
        if(getType() == CellTextChangeEvent.EventType.REMOVE){
            res.append(getText());
            res.append(code.substring(getOffset()));
        }else{
            res.append(code.substring(getOffset() + getLength()));
        }
        s.setCode(res.toString());
        ThisNiche.hg.update(s);
        type = (type == CellTextChangeEvent.EventType.REMOVE) ? 
                CellTextChangeEvent.EventType.INSERT : CellTextChangeEvent.EventType.REMOVE;
        CellUtils.fireCellTextChanged(cellH, this);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cellH == null) ? 0 : cellH.hashCode());
        result = prime * result + length;
        result = prime * result + offset;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CellTextChangeEvent other = (CellTextChangeEvent) obj;
        if (cellH == null)
        {
            if (other.cellH != null)
                return false;
        } else if (!cellH.equals(other.cellH))
            return false;
        if (length != other.length)
            return false;
        if (offset != other.offset)
            return false;
        if (text == null)
        {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (type == null)
        {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
    
    /**
     * Enumeration for document event types
     */
    public static final class EventType
    {

        private EventType(String s)
        {
            typeString = s;
        }

        /**
         * Insert type.
         */
        public static final EventType INSERT = new EventType("INSERT");

        /**
         * Remove type.
         */
        public static final EventType REMOVE = new EventType("REMOVE");

         /**
         * Converts the type to a string.
         * 
         * @return the string
         */
        public String toString()
        {
            return typeString;
        }

        private String typeString;
    }

    @Override
    public String toString()
    {
        return "" + cellH + ":" + type + ":" + text + ":" + length + ":" + offset;
    }

   
}
