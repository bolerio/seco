package seco.events;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

import seco.ThisNiche;




public class EventPubSub extends HGPlainLink 
{
	public EventPubSub(HGHandle [] targetSet)
	{
		super(targetSet);
	}
	
	public EventPubSub(HGHandle eventType, 
					   HGHandle publisher, 
					   HGHandle subscriber,
					   HGHandle eventHandler)
	{
		super(new HGHandle[] { eventType, publisher, subscriber, eventHandler} );
	}
	
	public HGHandle getEventType()
	{
		return getTargetAt(0);
	}
	
	public HGHandle getPublisher()
	{
		return getTargetAt(1);
	}
	
	public HGHandle getSubscriber()
	{
		return getTargetAt(2);
	}
	
	public HGHandle getEventHandler()
	{
		return getTargetAt(3);
	}

    @Override
    public String toString()
    {
        return "EventPubSub:" + getEventType() + ":" + ThisNiche.hg.get(getEventHandler()) + ":" +
        getPublisher() + ":" + getSubscriber();
    }

    @Override
    public boolean equals(Object obj)
    {
       if(!(obj instanceof EventPubSub)) return false;
       EventPubSub in = (EventPubSub) obj;
       return getEventType().equals(in.getEventType()) && 
       getPublisher().equals(in.getPublisher()) && 
       getSubscriber().equals(in.getSubscriber()) && 
       getEventHandler().equals(in.getEventHandler()); 
    }
}