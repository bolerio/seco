/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class ActionManager implements Serializable
{
	private static final long serialVersionUID = 6622882133321172241L;
    private static ActionManager instance;
	private static Map<String, Action> actions =
		new HashMap<String, Action>();
	
	public static ActionManager getInstance(){
		if(instance == null)
			instance = new ActionManager();
		return instance;
	}

	public Action getAction(String name){
		return actions.get(name);
	}
	
	public Action putAction(Action a)
	{
		actions.put((String) a.getValue(Action.NAME), a);
		return a;
	}
	
	public Action putAction(Action a, KeyStroke k)
	{
		a.putValue(Action.ACCELERATOR_KEY, k);
		actions.put((String)
				a.getValue(Action.NAME), a);
		return a;
	}
	
	public Action putAction(Action a, KeyStroke k, Icon icon)
	{
		a.putValue(Action.SMALL_ICON, icon);
		return putAction(a, k);
	}
	
	private ActionManager()
	{
	}
}
