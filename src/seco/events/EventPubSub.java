package seco.events;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

import seco.ThisNiche;

/**
 * <p>
 * Represents an event subscription for a particular event type. The 
 * subscription is always bound to a specific publisher and subscriber.
 * However, the event handler itself is an autonomous objects. Usually in
 * event handling frameworks, the interested party of the event is also
 * the one that handles it. In this framework however, the event handler
 * is an independent entity that receive both publisher and subscriber 
 * as parameters. Nothing prevents from a subscriber to also BE the event
 * handler or the event handler could further delegate to the subscriber
 * by some means. It is also possible to define an EventPubSub with 
 * no subscriber (nullHandle or anyHandle).
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
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