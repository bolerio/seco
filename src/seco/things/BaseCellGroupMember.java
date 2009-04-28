package seco.things;

import java.util.HashMap;
import java.util.Map;
import org.hypergraphdb.HGHandle;
import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.EventDispatcher;

public class BaseCellGroupMember implements CellGroupMember
{
    private static final String VISUAL_HANDLE_KEY = "VISUAL_HANDLE_KEY";
    
    protected Object visualInstance;
    protected Map<Object, Object> attributes = new HashMap<Object, Object>(7);
    protected HGHandle visual;
    
    public Object getVisualInstance()
    {
        return visualInstance;
    }

    public void setVisualInstance(Object visual)
    {
        this.visualInstance = visual;
    }
    
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

    public Map<Object, Object> getAttributes()
    {
        return attributes;
    } 
    
    public HGHandle getVisual()
    {
        //return visual;
        return (HGHandle) getAttributes().get(VISUAL_HANDLE_KEY);
    }

    public void setVisual(HGHandle visual)
    {
        this.visual = visual;
        setAttribute(VISUAL_HANDLE_KEY, visual);
    }

    void fireAttributeChanged(AttributeChangeEvent e)
    {
       EventDispatcher.dispatch(AttributeChangeEvent.HANDLE, ThisNiche.handleOf(this), e);
    }
}