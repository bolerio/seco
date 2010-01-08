/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.things;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGAtomRef; 

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventDispatcher;
import seco.events.EventHandler;
import seco.notebook.XMLConstants;

public class Cell extends BaseCellGroupMember implements EventHandler
{
    HGAtomRef ref;

    public Cell(HGAtomRef ref)
    {
        if (ref == null)
            throw new NullPointerException(
                    "A Cell must always have a non-null atom reference.");
        this.ref = ref;
    }

    public Object getValue()
    {
        try{
          return ThisNiche.graph.get(ref.getReferent());
        }catch(Throwable t){
            t.printStackTrace();
            return null;
        }
    }

    public HGHandle getAtomHandle()
    {
        return ref.getReferent();
    }
    
    public void handle(HGHandle eventType, Object event, HGHandle publisher, HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            updateValue((EvalCellEvent) event);
        }
    }
    
    void updateValue(EvalCellEvent e)
    {
        //System.out.println("Cell - updateValue: " + e);
        Object val = (e.getValue().getComponent() != null) ?
                e.getValue().getComponent() : e.getValue().getText();
        attributes.put(XMLConstants.ATTR_ERROR, e.getValue().isError());        
        updateValue(val);
        EventDispatcher.dispatch(EvalCellEvent.HANDLE, ThisNiche.handleOf(this), e);
    }
    
    public void updateValue(Object val)
    {
       // HGHandle h = ThisNiche.handleOf(val);
       // if (h == null)
        HGHandle h = CellUtils.addSerializable(val);
        ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        ThisNiche.graph.update(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Cell)
            return ref.getReferent().equals(((Cell)obj).ref.getReferent());
        return false;
    }

    @Override
    public int hashCode()
    {
        return ref.getReferent().hashCode();
    }

    @Override
    public String toString()
    {
        String s =  "Cell: "; 
        s += (CellUtils.getName(this) != null) ?
           CellUtils.getName(this) + ":" : " :";
        if(getValue() != null)
            s += getValue().getClass().getName();
        return s;
        //"Cell: " + CellUtils.getName(this); 
        //ThisNiche.handleOf(this) + ":" + 
        //        this.getValue();
    }

   
   
   
}