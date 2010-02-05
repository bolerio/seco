package seco.things;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventListenerMap extends HashMap<Class, Set<EventListener>>
{
    public EventListenerMap()
    {
        super();
    }

    public EventListenerMap(Map<? extends Class, ? extends Set<EventListener>> m)
    {
        super(m);
    }

    public void addListener(Class c, EventListener l){
        Set<EventListener> set = get(c);
        if(set == null){
            set = new HashSet<EventListener>();
        }
        set.add(l);
        put(c, set);
    }
    
    public void removeListener(Class c, EventListener l){
        Set<EventListener> set = get(c);
        if(set != null) set.remove(l);
    }
}
