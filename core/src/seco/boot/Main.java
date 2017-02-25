/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;


import org.hypergraphdb.HGEnvironment;
import seco.ThisNiche;
import seco.U;
import seco.gui.TopFrame;

/**
 * Main class loaded reflectively with a custom class loader.
s */
public class Main
{
	/**
	 * <p>
	 * Start the program by opening the HyperGraphDB niche at the specified location. This
	 * method is normally called reflectively from the StartMeUp main program.
	 * </p>
	 * @param nicheLocation
	 */
	public static void go(String nicheLocation, String guiControllerClass)
	{
		if (guiControllerClass != null)
			ThisNiche.guiControllerClassName = guiControllerClass;
		TopFrame.PICCOLO = false;
        HGEnvironment.get(nicheLocation, U.dbConfig()); // boots from HG LOAD listeners

	}
}