package seco.notebook;

import java.util.HashMap;
import java.util.Map;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;

public class AbbreviationManager
{
    public static final HGPersistentHandle HANDLE = UUIDHandleFactory.I
                     .makeHandle("a4ad2ea0-ec0c-11df-98cf-0800200c9a66");
    private static AbbreviationManager instance;
    
    private Map<String, String> abbreviationMap = new HashMap<String, String>();
    private AbbreviationManager(){}
    
    public static AbbreviationManager getInstance()
    {
        if (instance == null)
        {
            instance = (AbbreviationManager) ThisNiche.graph.get(HANDLE);
            if (instance == null)
            {
                instance = new AbbreviationManager();
                ThisNiche.graph.define(HANDLE, instance);
            }
        }
        return instance;
    }
    
    /**
     * Returns abbreviation's full text
     */
    public String getAbbreviation(String key)
    {
        return abbreviationMap.get(key);
    }
    
    /**
     * Removes an abbreviation given its name
     */
    public void removeAbbreviation(String key)
    {
        abbreviationMap.remove(key);
        ThisNiche.graph.replace(HANDLE, this);
    }

   
    /**
     * Sets an abbreviation
     */
    public void addAbbreviation(String key, String def)
    {
        abbreviationMap.put(key, def);
        ThisNiche.graph.replace(HANDLE, this);
    }

    public Map<String, String> getAbbreviations()
    {
        return abbreviationMap;
    }

    public void setAbbreviations(Map<String, String> abbreviationMap)
    {
        this.abbreviationMap = abbreviationMap;
    }
}
