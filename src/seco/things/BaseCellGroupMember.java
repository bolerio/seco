package seco.things;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGAtomRef;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventDispatcher;




public class BaseCellGroupMember implements CellGroupMember
{
    protected Map<Object, Object> attributes = new HashMap<Object, Object>(7);
  
    public Object getAttribute(Object key)
    {
        return attributes.get(key);
    }

    public void setAttribute(Object key, Object value)
    {
        Object old = attributes.get(key);
        if ((old != null && !old.equals(value))
                || (value != null && !value.equals(old)))
            attributes.put(key, value);
        CellUtils.updateCellGroupMember(this);
        HGHandle h = ThisNiche.handleOf(this);
        if (h == null)
            throw new NullPointerException("Cell with NULL handle: " + this);

        fireAttributeChanged(new AttributeChangeEvent(h, key, value, old));
    }

    public Map getAttributes(){
        return attributes;
    }
  
    void fireAttributeChanged(AttributeChangeEvent e)
    {
       EventDispatcher.dispatch(AttributeChangeEvent.HANDLE, ThisNiche.handleOf(this), e);
    }

}
