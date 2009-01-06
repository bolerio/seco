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

import org.hypergraphdb.*;
import org.hypergraphdb.event.*;

import seco.ThisNiche;
import seco.classloader.AdaptiveClassLoader;
import seco.notebook.storage.ClassRepository;
import seco.rtenv.RuntimeContext;
import seco.things.Scriptlet;



public class StartMeUp
{
	private static final String ESCAPE = "\\";
	private static final String ESCAPE_ESCAPE = "\\\\";
	private static final String	QUOTE = "\"";
	private static final String	QUOTE_ESCAPE = "\\\"";
    public static final String NICHELIST = ".scribaNiches";
    static File nichesFile = new File(new File(findUserHome()), NICHELIST);    
    static HashMap<String, File> niches = new HashMap<String, File>();

    public static boolean firstTime = false;
    
    public static String findUserHome()
    {
    	// unix and cygwin take precedence over the long and not often used by developers
    	// windows "user.home"
        String home = System.getenv().get("HOME");
        if(home == null)
           home = System.getProperty("user.home");
        //on my Windows System.getenv().get("HOME") return a quoted value 
        if(home != null && home.startsWith(QUOTE))
           home = unquote(home);
        return home;
    }

    static String quote(String s)
    {
    	if (s == null)
    		return s;
    	s.replace(ESCAPE, ESCAPE_ESCAPE).replace(QUOTE, QUOTE_ESCAPE);
    	StringBuffer result = new StringBuffer(s);
    	result.insert(0, QUOTE);
    	result.append(QUOTE);
    	return result.toString();
    }
    
    static String unquote(String s)
    {
    	s = s.substring(1, s.length() - 1);
    	return s.replace(QUOTE_ESCAPE, QUOTE).replace(ESCAPE_ESCAPE, ESCAPE);
    }
    
    static void readNiches()
    {
        try
        {
            niches.clear();
            if (!nichesFile.exists())
            {
            	firstTime = true;
                return;
            }
            FileReader reader = new FileReader(nichesFile);
            BufferedReader in = new BufferedReader(reader);
            for (String line = in.readLine(); line != null; line = in.readLine())
            {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                String [] tokens = line.split(",");
                if (tokens.length != 2)
                    continue;
                File location = new File(unquote(tokens[1]));
                niches.put(unquote(tokens[0]), location);
            }
            in.close();
            reader.close();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
    }

    static void saveNiches()
    {
        try
        {
        	FileWriter out = new FileWriter(nichesFile);
            for (Map.Entry<String, File> e : niches.entrySet())
            {
                out.write(quote(e.getKey()));
                out.write(",");
                out.write(quote(e.getValue().getAbsolutePath()));
                out.write("\n");
            }
            out.close();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
    }

    /**
     * <p>Recursively delete a directory with all its contents.</p>
     * @param dir
     */
    static void deleteDirectory(File dir)
    {
        try
        {
            for (File f : dir.listFiles())
                if (f.isDirectory())
                    deleteDirectory(f);
                else
                    f.delete();
            dir.delete();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);            
        }
    }
    static boolean isLocationOk(File location)
    {
        if (!location.exists())
            return true;
        else if (!location.isDirectory())
            return false;
        else if (location.list().length > 0)
            return false;
        else
            return true;
    }
    
    /**
     * <p>Test whether a given location on the file system is a "niche" HyperGraphDB.</p>
     */
    static boolean isNicheLocation(File location)
    {
        if (!new File(location, "hgstore_idx_HGATOMTYPE").exists())
        	return false;
        else
        {
        	HyperGraph hg = null;
        	try
        	{
        		hg = new HyperGraph(location.getAbsolutePath());
        		return hg.get(ThisNiche.NICHE_NAME_HANDLE) != null &&
        			   hg.get(ThisNiche.TOP_CONTEXT_HANDLE) != null;
        	}
        	catch (Throwable T)
        	{
        		return false;
        	}
        	finally
        	{
        		if (hg != null)
        			try { hg.close(); } catch (Throwable t) {}        		
        	}
        }
    }
    
    static void createNiche(String name, File path)
    {
        int levelsToDeleteOnFail = 0;
        for (File existing = path; !existing.exists(); existing = existing.getParentFile())
            levelsToDeleteOnFail++;
        HyperGraph hg = null;
        try
        {
            hg = new HyperGraph(path.getAbsolutePath());
            hg.add(new HGListenerAtom(HGOpenedEvent.class.getName(), 
            						  seco.NicheBootListener.class.getName()));
            hg.define(ThisNiche.NICHE_NAME_HANDLE, name);
            hg.define(ThisNiche.TOP_CONTEXT_HANDLE, new RuntimeContext("top"));
           // Scriptlet s = new Scriptlet("jscheme", "(load \"jscheme/scribaui.scm\")(install-runtime-menu)");            
          //  hg.add(new HGValueLink("on-load", new HGHandle[] {ThisNiche.TOP_CONTEXT_HANDLE, hg.add(s)}));
            hg.close();
        }
        catch (Throwable t)
        {
            if (hg != null) try { hg.close(); } catch (Throwable ex) { }
            for (int i = 0; i < levelsToDeleteOnFail; i++)
            {
                path.delete();
                path = path.getParentFile();
            }
            if (t instanceof RuntimeException)
                throw (RuntimeException)t;
            else
                throw new RuntimeException(t);
        }
    }

    public static void main(String[] argv)
    {
    	System.out.println("java.library.path: " + System.getProperty("java.library.path"));
    	ClassRepository.getInstance();
        readNiches();
        //Frame f = new Frame("Scriba");
        //f.setIconImage(Toolkit.getDefaultToolkit().getImage(
        //				AppForm.class.getResource(AppForm.LOGO_IMAGE_RESOURCE)));
        //NicheSelectDialog dlg = new NicheSelectDialog(f, true);
        NicheSelectDialog dlg = new NicheSelectDialog(null, true);
        dlg.setNiches(niches);
        /*        java.awt.Rectangle bounds = dlg.getBounds();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        bounds.x = dim.width / 2 - bounds.width / 2;
        bounds.y = dim.height / 2 - bounds.height / 2;
        dlg.setBounds(bounds); */
        dlg.setVisible(true);
        if (dlg.getSucceeded())
        {
        	String nicheLocation = niches.get(dlg.getSelectedNiche()).getAbsolutePath();
        	AdaptiveClassLoader cl = new AdaptiveClassLoader(new java.util.Vector(), true);
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
