/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.things;

import javax.swing.JComponent;

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
        try
        {
            return ThisNiche.graph.get(ref.getReferent());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }

    public HGHandle getAtomHandle()
    {
        return ref.getReferent();
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
                       HGHandle subscriber)
    {
        if (eventType.equals(EvalCellEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            updateValue((EvalCellEvent) event);
        }
    }

    void updateValue(EvalCellEvent e)
    {
        // System.out.println("Cell - updateValue: " + e);
        Object val = (e.getValue().getComponent() != null) ? e.getValue()
                .getComponent() : e.getValue().getText();
        attributes.put(XMLConstants.ATTR_ERROR, e.getValue().isError());
        updateValue(val);
        EventDispatcher.dispatch(EvalCellEvent.HANDLE,
                                 ThisNiche.handleOf(this),
                                 e);
    }

    public void updateValue(Object val)
    {
        // HGHandle h = ThisNiche.handleOf(val);
        // if (h == null)
    	
    	// So we avoid serializing Swing components for now. There 
    	// are frequent problems with the approach. And it usually makes
    	// more sense to find a way to re-evaluate cells to recreate 
    	// the components, though clearly it would be nice not to need to
    	// and I still hope that that is achievable, maybe with some other UI
    	// framework. 
    	// The wrapping of JComponent into a NotSerializableValue and actually
    	// the reason that NotSerializableValue wrapper was created was one
    	// situation when a JTable model was implemented by an anonymous BeanShell
    	// class - the model could not be serialized because the BeanShell script
    	// context is not serializable. Not only that, but the mere attempt to serialize
    	// that table nullified it in the BasicTableUI instance which create constant
    	// NPEs on every redisplay and there was no way to get rid of that. 
    	if (val instanceof JComponent)
    		val = new NotSerializableValue(val);
        HGHandle h = CellUtils.addSerializable(val);
        ref = new HGAtomRef(h, HGAtomRef.Mode.symbolic);
        ThisNiche.graph.update(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Cell)
            return ref.getReferent().equals(((Cell) obj).ref.getReferent());
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
        String s = "Cell: ";
        s += (CellUtils.getName(this) != null) ? CellUtils.getName(this) + ":"
                : " :";
        if (getValue() != null)
            s += getValue().getClass().getName();
        return s;
        // "Cell: " + CellUtils.getName(this);
        // ThisNiche.handleOf(this) + ":" +
        // this.getValue();
    }
}