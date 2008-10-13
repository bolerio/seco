package seco.things;

import java.util.HashMap;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;




public interface CellGroupMember 
{
	public Object getAttribute(Object key);
	public void setAttribute(Object key, Object value);
    public Map getAttributes(); 
}