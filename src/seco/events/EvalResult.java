/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.events;

import java.awt.Component;


public class EvalResult 
{
	private String text;
	private Component component;
	private boolean isError = false;
	
	public EvalResult()
	{
	}
	
	public EvalResult(Object val, boolean error)
    {
        if(val instanceof Component)
           this.component = (Component) val;
        else
           this.text = "" + val; 
        this.isError = error;
    }
	
	public Component getComponent()
	{
		return component;
	}

	public void setComponent(Component component)
	{
		this.component = component;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public boolean isError()
	{
		return isError;
	}

	public void setError(boolean isError)
	{
		this.isError = isError;
	}

}
