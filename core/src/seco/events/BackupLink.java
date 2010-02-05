package seco.events;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

import seco.ThisNiche;

/**
 * <p>
 * Represents backup info needed to restore all event handlers of a given 
 * cell. 
 * </p>
 */
public class BackupLink extends HGPlainLink 
{
    public BackupLink(HGHandle [] targetSet)
    {
        super(targetSet);
    }
    
    public BackupLink(HGHandle cell, 
                       List<EventPubSubInfo> pubs,
                       List<EventPubSubInfo> subs)
    {
        super(new HGHandle[] {cell, ThisNiche.graph.add(pubs), 
                ThisNiche.graph.add(subs)} );
    }
    
    public HGHandle getCell()
    {
        return getTargetAt(0);
    }
    
    public HGHandle getPubs()
    {
        return getTargetAt(1);
    }
    
    public HGHandle getSubs()
    {
        return getTargetAt(2);
    }
    
    @Override
    public int getArity()
    {
       return 3;
    }
    
    @Override
    public String toString()
    {
        return "BackupLink:" + ThisNiche.graph.get(getCell()) + ":" +
        ThisNiche.graph.get(getPubs()) + ":" + ThisNiche.graph.get(getSubs());
    }

    @Override
    public boolean equals(Object obj)
    {
       if(!(obj instanceof BackupLink)) return false;
       BackupLink in = (BackupLink) obj;
       return getCell().equals(in.getCell()) && 
       getPubs().equals(in.getPubs()) && 
       getSubs().equals(in.getSubs()); 
    }
    
    public static class EventPubSubInfo
    {
        HGHandle eventType; 
        HGHandle pubOrSub;
        HGHandle eventHandler;
        
        public EventPubSubInfo()
        {
            
        }
        
        public EventPubSubInfo(HGHandle eventType, HGHandle pub_or_sub,
                HGHandle eventHandler)
        {
            super();
            this.eventType = eventType;
            this.pubOrSub = pub_or_sub;
            this.eventHandler = eventHandler;
        }

        public HGHandle getEventType()
        {
            return eventType;
        }

        public HGHandle getPubOrSub()
        {
            return pubOrSub;
        }

        public HGHandle getEventHandler()
        {
            return eventHandler;
        }
        
        public void setEventType(HGHandle eventType)
        {
            this.eventType =eventType;
        }

        public void setPubOrSub(HGHandle pubOrSub)
        {
            this.pubOrSub = pubOrSub;
        }

        public void setEventHandler(HGHandle eventHandler)
        {
            this.eventHandler = eventHandler;
        }
    }

}