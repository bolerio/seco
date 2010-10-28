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

import javax.swing.UIManager;

import seco.classloader.AdaptiveClassLoader;
import seco.notebook.storage.ClassRepository;

public class StartMeUp
{
    public static void main(String[] argv)
    {
    	System.out.println("java.library.path: " + System.getProperty("java.library.path"));
    	 System.out.println("java.home: " + System.getProperty("java.home"));
         //System.loadLibrary("libdb_java50.dll");
         //System.loadLibrary("libdb50.dll");
         ClassRepository.getInstance();
    	Map<String, File> niches = NicheManager.readNiches();
        NicheSelectDialog dlg = new NicheSelectDialog();
        dlg.setNiches(niches);
        dlg.setVisible(true);
//        try
//        {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        }
//        catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }        
        if (dlg.getSucceeded())
        {
        	String nicheLocation = niches.get(dlg.getSelectedNiche()).getAbsolutePath();
        	AdaptiveClassLoader cl = new AdaptiveClassLoader(new java.util.Vector<Object>(), true);
        	try
        	{
        		Class<?> c = cl.loadClass("seco.boot.Main");
        		c.getMethod("go", new Class[] {String.class}).invoke(null, new Object[] { nicheLocation} );
        	}
        	catch (Throwable t)
        	{
        		t.printStackTrace(System.err);
        		System.exit(-1);
        	}
        }
        else
            System.exit(0);
    }
}
