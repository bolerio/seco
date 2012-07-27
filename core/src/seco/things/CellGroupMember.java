package seco.things;

import java.util.Map;
import org.hypergraphdb.HGHandle;

public interface CellGroupMember extends WithAttributes 
{    
    Map<Object, Object> getAttributes();
    HGHandle getVisual();
    void setVisual(HGHandle visual);
    Object getVisualInstance();
    void setVisualInstance(Object instance);
}