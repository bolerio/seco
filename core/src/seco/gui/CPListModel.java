/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import seco.rtenv.ClassPath;
import seco.rtenv.ClassPathEntry;

import java.util.ArrayList;
import java.util.Arrays;


public class CPListModel implements ListModel
{
    private ClassPath cp = new ClassPath();
    private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
    
    public CPListModel(ClassPath cp) { this.cp.addAll(cp); }
    public void addListDataListener(ListDataListener l) { listeners.add(l); }
    public Object getElementAt(int index) { return cp.get(index); }
    public int 	getSize() { return cp.size(); }
    public void removeListDataListener(ListDataListener l)    { listeners.remove(l); }
    
    public void addEntry(ClassPathEntry entry)
    {
    	cp.add(entry);
    	ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, cp.size() - 1, cp.size() - 1);    	
    	for (ListDataListener l:listeners)
    		l.intervalAdded(e);
    }
    
    public void removeEntry(int idx)
    {
    	cp.remove(idx);
    	ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, idx, idx);    	
    	for (ListDataListener l:listeners)
    		l.intervalRemoved(e);    	
    }
    
    public void removeEntries(int [] idxes)
    {
    	if (idxes == null) return;
    	Arrays.sort(idxes);
    	for (int i = 0; i < idxes.length; i++)
    		removeEntry(idxes[i] - i);
    }
    
    public void removeAll(int start, int end)
    {
    	for (int i = start; i <= end; i++)
    		cp.remove(start);
    	ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, start, end);    	
    	for (ListDataListener l:listeners)
    		l.intervalRemoved(e);    	
    }
    
    public ClassPath getClassPath()
    {
    	return cp;
    }
}
