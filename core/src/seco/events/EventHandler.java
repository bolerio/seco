package seco.events;

import org.hypergraphdb.HGHandle;

public interface EventHandler 
{
	void handle(HGHandle eventType, Object event, HGHandle publisher, HGHandle subscriber);
}