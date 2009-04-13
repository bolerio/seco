package seco.notebook.storage.swing.types;

import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.type.Record;
import org.hypergraphdb.type.TypeUtils; 

public abstract class GeneratedClass extends SwingBinding
{
  	public GeneratedClass(){
	}
	
	public GeneratedClass(HGHandle typeHandle, SwingType hgType) {
		super(typeHandle, hgType);
	}
	
	public Object make(HGPersistentHandle handle,
			LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet) {
		Object bean = null;
		//System.out.println("Make - res: " + hgType.getJavaClass().getName());
		try {
			Record record = (Record) hgType.make(handle, targetSet, incidenceSet);
			bean = instantiate(record); 
			TypeUtils.setValueFor(graph, handle, bean);
			if(bean == null) return null;
			if(!bean.getClass().isAssignableFrom(hgType.getJavaClass()))
			{
			    System.err.println("Wrong instantiation: " + bean.getClass() +
			            " .Expected: " + hgType.getJavaClass().getName());
			    return null;
			}    
			makeBean(bean, record);
			//System.out.println("Make - res: " + bean);
			
			AddOnLink addons = (AddOnLink) graph.get(hgType.getAddOnsHandle());
			if (addons != null)
				for (int i = 0; i < addons.getArity(); i++) {
					HGRelType l = (HGRelType) graph.get(addons.getTargetAt(i));
					AddOnFactory.processLink(graph, l, record, bean);
				}
		} catch (Throwable t) {
			//t.printStackTrace();
			//throw new HGException("GeneratedClass.make in " + hgType.getJavaClass() +
			//		":" + t.toString(), t);
		    System.err.println("Unable to make " + hgType.getJavaClass() +
		                  ":" + t.toString());
		    t.printStackTrace();
		    return null;
		}

		return bean;
	}
	
	protected void makeBean(Object bean, Record record){
		
	}
	
	protected void storeBean(Object bean, Record record){
		
	}
		
	
	public HGPersistentHandle store(final Object instance) {
		Map<Object, HGPersistentHandle> refMap = TypeUtils
				.getTransactionObjectRefMap(graph);
		HGPersistentHandle result = refMap.get(instance);
		if (result == null) {
			final Record record = new SwingRecord(typeHandle, instance);
			storeBean(instance, record);
			result = hgType.store(record);
		}
		return result;
	}

	public void release(HGPersistentHandle handle) {
		hgType.release(handle);
	}

	protected Object instantiate(Record record) {
		ConstructorLink link = (ConstructorLink) graph.get(hgType.getCtrHandle());
		//System.out.println("GenCls - instantiate" + 
		//		hgType.getJavaClass().getName() + ":" + link);
        if(link != null && link instanceof FactoryConstructorLink)
        	return AddOnFactory.instantiateFactoryConstructorLink(graph, hgType, (FactoryConstructorLink) link, record);
		return AddOnFactory.instantiateConstructorLink(graph, hgType, link, record);
	}
	
	public void init(HGHandle typeHandle, SwingType hgType)
	{
		this.typeHandle = typeHandle;
		this.hgType = hgType;
		this.hgType.setThisHandle(typeHandle);
	}
	
	public void dealWithListeners(Object instance, Record rec, Map<String, EventSetDescriptor> esd){
		
		for (String key: esd.keySet()) {
			EventSetDescriptor d = esd.get(key);
			Class<?> listenerType = d.getListenerType();
			EventListener[] oldL = new EventListener[0];
			try {
				Method m = d.getGetListenerMethod();
				oldL = (EventListener[]) m.invoke(instance, new Object[] {});
			} catch (Throwable e2) {
				try {
					Method m = hgType.getJavaClass().getMethod("getListeners",
							new Class[] { Class.class });
					oldL = (EventListener[]) m.invoke(instance,
							new Object[] { listenerType });
				} catch (Exception e3) {
					return;
				}
			}
			// System.out.println("addListeners: " + d.getName() + ":" + oldL);
			setValue(rec, key, filterListeners(instance, oldL));
		}
	}
	
	static class SwingRecord extends Record implements TypeUtils.WrappedRuntimeInstance
    {
    	Object bean;    	
    	SwingRecord(HGHandle h, Object bean) { super(h); this.bean = bean;}
    	public Object getRealInstance() { return bean; }
    }

}
