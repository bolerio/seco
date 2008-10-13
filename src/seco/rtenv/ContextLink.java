/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.rtenv;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class ContextLink extends HGPlainLink
{
	public ContextLink(HGHandle [] targetSet)
	{		
		super(targetSet);
	}
	
	public ContextLink(HGHandle entity, HGHandle context)
	{
		this(new HGHandle[] { entity, context} );
	}
	
	public HGHandle getEntity()
	{
		return getTargetAt(0);
	}
	
	public HGHandle getContext()
	{
		return getTargetAt(1);
	}
}
