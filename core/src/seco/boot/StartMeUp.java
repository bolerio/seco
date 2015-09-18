/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;

import java.io.*;
import java.util.*;

import seco.ThisNiche;
import seco.U;
import seco.classloader.AdaptiveClassLoader;
import seco.gui.StandaloneFrame;
import seco.storage.ClassRepository;

public class StartMeUp
{
	static void die(String msg, boolean showSyntax)
	{
		System.out.println(msg);
		if (showSyntax)
			System.out.println("Syntax: seco.boot.StartMeUp [--nicheLocation directoryPath] [--showNicheDialog true|false] [--simpleUI true|false]");
		System.exit(-1);
	}
	
    static String defaultNicheLocation()
    {
    	String userHome = System.getProperty("user.home");
    	return userHome + File.separatorChar + ".secoDefaultNiche";
    }

	public static void main(String[] argv)
	{
		ClassRepository.getInstance();		
		System.out.println("java.library.path: " + System.getProperty("java.library.path"));
		System.out.println("java.home: " + System.getProperty("java.home"));
		System.out.println("user.home: " + U.findUserHome());
    	String nicheLocation = defaultNicheLocation();
    	boolean showNicheDialog = true;
    	boolean simpleUI = false;
    	for (int i = 0; i < argv.length; i++)
    	{
    		if ("--nicheLocation".equals(argv[i]))
    			nicheLocation = argv[++i];
    		else if ("--showNicheDialog".equals(argv[i]))
    			showNicheDialog = Boolean.valueOf(argv[++i]);
    		else if ("--simpleUI".equals(argv[i]))
    			simpleUI = Boolean.valueOf(argv[++i]);
    		else
    			die("Unknown option " + argv[i], true);
    	}
		if (showNicheDialog)
		{
			Map<String, File> niches = NicheManager.readNiches();
			NicheSelectDialog dlg = new NicheSelectDialog();
			dlg.setNiches(niches);
			dlg.setVisible(true);
			if (dlg.getSucceeded())
				nicheLocation = niches.get(dlg.getSelectedNiche()).getAbsolutePath();
			else
			{
				System.exit(-1);
			}
		}
		if (simpleUI)
		{
			ThisNiche.guiControllerClassName = StandaloneFrame.class.getName();
			File location = new File(nicheLocation);
			if (!NicheManager.isNicheLocation(location))
			{
				if (NicheManager.isLocationOk(location))
					NicheManager.createNiche("default", location);
				else
					die("Default location for niche is not empty: " + nicheLocation, false);
			}
//			seco.boot.Main.go(nicheLocation, null);
		}
//		else
//		{
			AdaptiveClassLoader cl = new AdaptiveClassLoader(new java.util.Vector<Object>(), true);
			try
			{
				Class<?> c = cl.loadClass("seco.boot.Main");
				c.getMethod("go", new Class[] { String.class, String.class }).invoke(null,
						new Object[] { nicheLocation, simpleUI ? StandaloneFrame.class.getName() : null });
			}
			catch (Throwable t)
			{
				t.printStackTrace(System.err);
				System.exit(-1);
			}        
//		}
	}
}
