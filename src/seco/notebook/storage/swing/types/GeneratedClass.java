package seco.notebook.storage.swing.types;

import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.type.Record;
import org.hypergraphdb.type.TypeUtils;

public abstract class GeneratedClass extends SwingBinding
{
    public GeneratedClass()
    {
    }

    public GeneratedClass(HGHandle typeHandle, SwingType hgType)
    {
        super(typeHandle, hgType);
    }

    //overriden in generated subclasses
    //protected void makeBean(Object bean, Record record);
    //protected void storeBean(Object bean, Record record);
 
    public void init(HGHandle typeHandle, SwingType hgType)
    {
        this.typeHandle = typeHandle;
        this.hgType = hgType;
        this.hgType.setThisHandle(typeHandle);
    }

    public void dealWithListeners(Object instance, Record rec,
            Map<String, EventSetDescriptor> esd)
    {

        for (String key : esd.keySet())
        {
            EventSetDescriptor d = esd.get(key);
            Class<?> listenerType = d.getListenerType();
            EventListener[] oldL = new EventListener[0];
            try
            {
                Method m = d.getGetListenerMethod();
                oldL = (EventListener[]) m.invoke(instance, new Object[] {});
            }
            catch (Throwable e2)
            {
                try
                {
                    Method m = hgType.getJavaClass().getMethod("getListeners",
                            new Class[] { Class.class });
                    oldL = (EventListener[]) m.invoke(instance,
                            new Object[] { listenerType });
                }
                catch (Exception e3)
                {
                    return;
                }
            }
            // System.out.println("addListeners: " + d.getName() + ":" + oldL);
            setValue(rec, key, filterListeners(instance, oldL));
        }
    }
}
